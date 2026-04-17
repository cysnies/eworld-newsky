package org.dom4j.tree;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.dom4j.Attribute;
import org.dom4j.CDATA;
import org.dom4j.CharacterData;
import org.dom4j.Comment;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Entity;
import org.dom4j.IllegalAddException;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.ProcessingInstruction;
import org.dom4j.QName;
import org.dom4j.Text;
import org.dom4j.Visitor;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.xml.sax.Attributes;

public abstract class AbstractElement extends AbstractBranch implements Element {
   private static final DocumentFactory DOCUMENT_FACTORY = DocumentFactory.getInstance();
   protected static final List EMPTY_LIST;
   protected static final Iterator EMPTY_ITERATOR;
   protected static final boolean VERBOSE_TOSTRING = false;
   protected static final boolean USE_STRINGVALUE_SEPARATOR = false;

   public short getNodeType() {
      return 1;
   }

   public boolean isRootElement() {
      Document document = this.getDocument();
      if (document != null) {
         Element root = document.getRootElement();
         if (root == this) {
            return true;
         }
      }

      return false;
   }

   public void setName(String name) {
      this.setQName(this.getDocumentFactory().createQName(name));
   }

   public void setNamespace(Namespace namespace) {
      this.setQName(this.getDocumentFactory().createQName(this.getName(), namespace));
   }

   public String getXPathNameStep() {
      String uri = this.getNamespaceURI();
      if (uri != null && uri.length() != 0) {
         String prefix = this.getNamespacePrefix();
         return prefix != null && prefix.length() != 0 ? this.getQualifiedName() : "*[name()='" + this.getName() + "']";
      } else {
         return this.getName();
      }
   }

   public String getPath(Element context) {
      if (this == context) {
         return ".";
      } else {
         Element parent = this.getParent();
         if (parent == null) {
            return "/" + this.getXPathNameStep();
         } else {
            return parent == context ? this.getXPathNameStep() : parent.getPath(context) + "/" + this.getXPathNameStep();
         }
      }
   }

   public String getUniquePath(Element context) {
      Element parent = this.getParent();
      if (parent == null) {
         return "/" + this.getXPathNameStep();
      } else {
         StringBuffer buffer = new StringBuffer();
         if (parent != context) {
            buffer.append(parent.getUniquePath(context));
            buffer.append("/");
         }

         buffer.append(this.getXPathNameStep());
         List mySiblings = parent.elements(this.getQName());
         if (mySiblings.size() > 1) {
            int idx = mySiblings.indexOf(this);
            if (idx >= 0) {
               buffer.append("[");
               ++idx;
               buffer.append(Integer.toString(idx));
               buffer.append("]");
            }
         }

         return buffer.toString();
      }
   }

   public String asXML() {
      try {
         StringWriter out = new StringWriter();
         XMLWriter writer = new XMLWriter(out, new OutputFormat());
         writer.write((Element)this);
         writer.flush();
         return out.toString();
      } catch (IOException e) {
         throw new RuntimeException("IOException while generating textual representation: " + e.getMessage());
      }
   }

   public void write(Writer out) throws IOException {
      XMLWriter writer = new XMLWriter(out, new OutputFormat());
      writer.write((Element)this);
   }

   public void accept(Visitor visitor) {
      visitor.visit((Element)this);
      int i = 0;

      for(int size = this.attributeCount(); i < size; ++i) {
         Attribute attribute = this.attribute(i);
         visitor.visit(attribute);
      }

      i = 0;

      for(int size = this.nodeCount(); i < size; ++i) {
         Node node = this.node(i);
         node.accept(visitor);
      }

   }

   public String toString() {
      String uri = this.getNamespaceURI();
      return uri != null && uri.length() > 0 ? super.toString() + " [Element: <" + this.getQualifiedName() + " uri: " + uri + " attributes: " + this.attributeList() + "/>]" : super.toString() + " [Element: <" + this.getQualifiedName() + " attributes: " + this.attributeList() + "/>]";
   }

   public Namespace getNamespace() {
      return this.getQName().getNamespace();
   }

   public String getName() {
      return this.getQName().getName();
   }

   public String getNamespacePrefix() {
      return this.getQName().getNamespacePrefix();
   }

   public String getNamespaceURI() {
      return this.getQName().getNamespaceURI();
   }

   public String getQualifiedName() {
      return this.getQName().getQualifiedName();
   }

   public Object getData() {
      return this.getText();
   }

   public void setData(Object data) {
   }

   public Node node(int index) {
      if (index >= 0) {
         List list = this.contentList();
         if (index >= list.size()) {
            return null;
         }

         Object node = list.get(index);
         if (node != null) {
            if (node instanceof Node) {
               return (Node)node;
            }

            return this.getDocumentFactory().createText(node.toString());
         }
      }

      return null;
   }

   public int indexOf(Node node) {
      return this.contentList().indexOf(node);
   }

   public int nodeCount() {
      return this.contentList().size();
   }

   public Iterator nodeIterator() {
      return this.contentList().iterator();
   }

   public Element element(String name) {
      List list = this.contentList();
      int size = list.size();

      for(int i = 0; i < size; ++i) {
         Object object = list.get(i);
         if (object instanceof Element) {
            Element element = (Element)object;
            if (name.equals(element.getName())) {
               return element;
            }
         }
      }

      return null;
   }

   public Element element(QName qName) {
      List list = this.contentList();
      int size = list.size();

      for(int i = 0; i < size; ++i) {
         Object object = list.get(i);
         if (object instanceof Element) {
            Element element = (Element)object;
            if (qName.equals(element.getQName())) {
               return element;
            }
         }
      }

      return null;
   }

   public Element element(String name, Namespace namespace) {
      return this.element(this.getDocumentFactory().createQName(name, namespace));
   }

   public List elements() {
      List list = this.contentList();
      BackedList answer = this.createResultList();
      int size = list.size();

      for(int i = 0; i < size; ++i) {
         Object object = list.get(i);
         if (object instanceof Element) {
            answer.addLocal(object);
         }
      }

      return answer;
   }

   public List elements(String name) {
      List list = this.contentList();
      BackedList answer = this.createResultList();
      int size = list.size();

      for(int i = 0; i < size; ++i) {
         Object object = list.get(i);
         if (object instanceof Element) {
            Element element = (Element)object;
            if (name.equals(element.getName())) {
               answer.addLocal(element);
            }
         }
      }

      return answer;
   }

   public List elements(QName qName) {
      List list = this.contentList();
      BackedList answer = this.createResultList();
      int size = list.size();

      for(int i = 0; i < size; ++i) {
         Object object = list.get(i);
         if (object instanceof Element) {
            Element element = (Element)object;
            if (qName.equals(element.getQName())) {
               answer.addLocal(element);
            }
         }
      }

      return answer;
   }

   public List elements(String name, Namespace namespace) {
      return this.elements(this.getDocumentFactory().createQName(name, namespace));
   }

   public Iterator elementIterator() {
      List list = this.elements();
      return list.iterator();
   }

   public Iterator elementIterator(String name) {
      List list = this.elements(name);
      return list.iterator();
   }

   public Iterator elementIterator(QName qName) {
      List list = this.elements(qName);
      return list.iterator();
   }

   public Iterator elementIterator(String name, Namespace ns) {
      return this.elementIterator(this.getDocumentFactory().createQName(name, ns));
   }

   public List attributes() {
      return new ContentListFacade(this, this.attributeList());
   }

   public Iterator attributeIterator() {
      return this.attributeList().iterator();
   }

   public Attribute attribute(int index) {
      return (Attribute)this.attributeList().get(index);
   }

   public int attributeCount() {
      return this.attributeList().size();
   }

   public Attribute attribute(String name) {
      List list = this.attributeList();
      int size = list.size();

      for(int i = 0; i < size; ++i) {
         Attribute attribute = (Attribute)list.get(i);
         if (name.equals(attribute.getName())) {
            return attribute;
         }
      }

      return null;
   }

   public Attribute attribute(QName qName) {
      List list = this.attributeList();
      int size = list.size();

      for(int i = 0; i < size; ++i) {
         Attribute attribute = (Attribute)list.get(i);
         if (qName.equals(attribute.getQName())) {
            return attribute;
         }
      }

      return null;
   }

   public Attribute attribute(String name, Namespace namespace) {
      return this.attribute(this.getDocumentFactory().createQName(name, namespace));
   }

   public void setAttributes(Attributes attributes, NamespaceStack namespaceStack, boolean noNamespaceAttributes) {
      int size = attributes.getLength();
      if (size > 0) {
         DocumentFactory factory = this.getDocumentFactory();
         if (size == 1) {
            String name = attributes.getQName(0);
            if (noNamespaceAttributes || !name.startsWith("xmlns")) {
               String attributeURI = attributes.getURI(0);
               String attributeLocalName = attributes.getLocalName(0);
               String attributeValue = attributes.getValue(0);
               QName attributeQName = namespaceStack.getAttributeQName(attributeURI, attributeLocalName, name);
               this.add(factory.createAttribute(this, (QName)attributeQName, attributeValue));
            }
         } else {
            List list = this.attributeList(size);
            list.clear();

            for(int i = 0; i < size; ++i) {
               String attributeName = attributes.getQName(i);
               if (noNamespaceAttributes || !attributeName.startsWith("xmlns")) {
                  String attributeURI = attributes.getURI(i);
                  String attributeLocalName = attributes.getLocalName(i);
                  String attributeValue = attributes.getValue(i);
                  QName attributeQName = namespaceStack.getAttributeQName(attributeURI, attributeLocalName, attributeName);
                  Attribute attribute = factory.createAttribute(this, (QName)attributeQName, attributeValue);
                  list.add(attribute);
                  this.childAdded(attribute);
               }
            }
         }
      }

   }

   public String attributeValue(String name) {
      Attribute attrib = this.attribute(name);
      return attrib == null ? null : attrib.getValue();
   }

   public String attributeValue(QName qName) {
      Attribute attrib = this.attribute(qName);
      return attrib == null ? null : attrib.getValue();
   }

   public String attributeValue(String name, String defaultValue) {
      String answer = this.attributeValue(name);
      return answer != null ? answer : defaultValue;
   }

   public String attributeValue(QName qName, String defaultValue) {
      String answer = this.attributeValue(qName);
      return answer != null ? answer : defaultValue;
   }

   /** @deprecated */
   public void setAttributeValue(String name, String value) {
      this.addAttribute(name, value);
   }

   /** @deprecated */
   public void setAttributeValue(QName qName, String value) {
      this.addAttribute(qName, value);
   }

   public void add(Attribute attribute) {
      if (attribute.getParent() != null) {
         String message = "The Attribute already has an existing parent \"" + attribute.getParent().getQualifiedName() + "\"";
         throw new IllegalAddException(this, attribute, message);
      } else {
         if (attribute.getValue() == null) {
            Attribute oldAttribute = this.attribute(attribute.getQName());
            if (oldAttribute != null) {
               this.remove(oldAttribute);
            }
         } else {
            this.attributeList().add(attribute);
            this.childAdded(attribute);
         }

      }
   }

   public boolean remove(Attribute attribute) {
      List list = this.attributeList();
      boolean answer = list.remove(attribute);
      if (answer) {
         this.childRemoved(attribute);
      } else {
         Attribute copy = this.attribute(attribute.getQName());
         if (copy != null) {
            list.remove(copy);
            answer = true;
         }
      }

      return answer;
   }

   public List processingInstructions() {
      List list = this.contentList();
      BackedList answer = this.createResultList();
      int size = list.size();

      for(int i = 0; i < size; ++i) {
         Object object = list.get(i);
         if (object instanceof ProcessingInstruction) {
            answer.addLocal(object);
         }
      }

      return answer;
   }

   public List processingInstructions(String target) {
      List list = this.contentList();
      BackedList answer = this.createResultList();
      int size = list.size();

      for(int i = 0; i < size; ++i) {
         Object object = list.get(i);
         if (object instanceof ProcessingInstruction) {
            ProcessingInstruction pi = (ProcessingInstruction)object;
            if (target.equals(pi.getName())) {
               answer.addLocal(pi);
            }
         }
      }

      return answer;
   }

   public ProcessingInstruction processingInstruction(String target) {
      List list = this.contentList();
      int size = list.size();

      for(int i = 0; i < size; ++i) {
         Object object = list.get(i);
         if (object instanceof ProcessingInstruction) {
            ProcessingInstruction pi = (ProcessingInstruction)object;
            if (target.equals(pi.getName())) {
               return pi;
            }
         }
      }

      return null;
   }

   public boolean removeProcessingInstruction(String target) {
      List list = this.contentList();
      Iterator iter = list.iterator();

      while(iter.hasNext()) {
         Object object = iter.next();
         if (object instanceof ProcessingInstruction) {
            ProcessingInstruction pi = (ProcessingInstruction)object;
            if (target.equals(pi.getName())) {
               iter.remove();
               return true;
            }
         }
      }

      return false;
   }

   public Node getXPathResult(int index) {
      Node answer = this.node(index);
      return answer != null && !answer.supportsParent() ? answer.asXPathResult(this) : answer;
   }

   public Element addAttribute(String name, String value) {
      Attribute attribute = this.attribute(name);
      if (value != null) {
         if (attribute == null) {
            this.add(this.getDocumentFactory().createAttribute(this, (String)name, value));
         } else if (attribute.isReadOnly()) {
            this.remove(attribute);
            this.add(this.getDocumentFactory().createAttribute(this, (String)name, value));
         } else {
            attribute.setValue(value);
         }
      } else if (attribute != null) {
         this.remove(attribute);
      }

      return this;
   }

   public Element addAttribute(QName qName, String value) {
      Attribute attribute = this.attribute(qName);
      if (value != null) {
         if (attribute == null) {
            this.add(this.getDocumentFactory().createAttribute(this, (QName)qName, value));
         } else if (attribute.isReadOnly()) {
            this.remove(attribute);
            this.add(this.getDocumentFactory().createAttribute(this, (QName)qName, value));
         } else {
            attribute.setValue(value);
         }
      } else if (attribute != null) {
         this.remove(attribute);
      }

      return this;
   }

   public Element addCDATA(String cdata) {
      CDATA node = this.getDocumentFactory().createCDATA(cdata);
      this.addNewNode(node);
      return this;
   }

   public Element addComment(String comment) {
      Comment node = this.getDocumentFactory().createComment(comment);
      this.addNewNode(node);
      return this;
   }

   public Element addElement(String name) {
      DocumentFactory factory = this.getDocumentFactory();
      int index = name.indexOf(":");
      String prefix = "";
      String localName = name;
      Namespace namespace = null;
      if (index > 0) {
         prefix = name.substring(0, index);
         localName = name.substring(index + 1);
         namespace = this.getNamespaceForPrefix(prefix);
         if (namespace == null) {
            throw new IllegalAddException("No such namespace prefix: " + prefix + " is in scope on: " + this + " so cannot add element: " + name);
         }
      } else {
         namespace = this.getNamespaceForPrefix("");
      }

      Element node;
      if (namespace != null) {
         QName qname = factory.createQName(localName, namespace);
         node = factory.createElement(qname);
      } else {
         node = factory.createElement(name);
      }

      this.addNewNode(node);
      return node;
   }

   public Element addEntity(String name, String text) {
      Entity node = this.getDocumentFactory().createEntity(name, text);
      this.addNewNode(node);
      return this;
   }

   public Element addNamespace(String prefix, String uri) {
      Namespace node = this.getDocumentFactory().createNamespace(prefix, uri);
      this.addNewNode(node);
      return this;
   }

   public Element addProcessingInstruction(String target, String data) {
      ProcessingInstruction node = this.getDocumentFactory().createProcessingInstruction(target, data);
      this.addNewNode(node);
      return this;
   }

   public Element addProcessingInstruction(String target, Map data) {
      ProcessingInstruction node = this.getDocumentFactory().createProcessingInstruction(target, data);
      this.addNewNode(node);
      return this;
   }

   public Element addText(String text) {
      Text node = this.getDocumentFactory().createText(text);
      this.addNewNode(node);
      return this;
   }

   public void add(Node node) {
      switch (node.getNodeType()) {
         case 1:
            this.add((Element)node);
            break;
         case 2:
            this.add((Attribute)node);
            break;
         case 3:
            this.add((Text)node);
            break;
         case 4:
            this.add((CDATA)node);
            break;
         case 5:
            this.add((Entity)node);
            break;
         case 6:
         case 9:
         case 10:
         case 11:
         case 12:
         default:
            this.invalidNodeTypeAddException(node);
            break;
         case 7:
            this.add((ProcessingInstruction)node);
            break;
         case 8:
            this.add((Comment)node);
            break;
         case 13:
            this.add((Namespace)node);
      }

   }

   public boolean remove(Node node) {
      switch (node.getNodeType()) {
         case 1:
            return this.remove((Element)node);
         case 2:
            return this.remove((Attribute)node);
         case 3:
            return this.remove((Text)node);
         case 4:
            return this.remove((CDATA)node);
         case 5:
            return this.remove((Entity)node);
         case 6:
         case 9:
         case 10:
         case 11:
         case 12:
         default:
            return false;
         case 7:
            return this.remove((ProcessingInstruction)node);
         case 8:
            return this.remove((Comment)node);
         case 13:
            return this.remove((Namespace)node);
      }
   }

   public void add(CDATA cdata) {
      this.addNode(cdata);
   }

   public void add(Comment comment) {
      this.addNode(comment);
   }

   public void add(Element element) {
      this.addNode(element);
   }

   public void add(Entity entity) {
      this.addNode(entity);
   }

   public void add(Namespace namespace) {
      this.addNode(namespace);
   }

   public void add(ProcessingInstruction pi) {
      this.addNode(pi);
   }

   public void add(Text text) {
      this.addNode(text);
   }

   public boolean remove(CDATA cdata) {
      return this.removeNode(cdata);
   }

   public boolean remove(Comment comment) {
      return this.removeNode(comment);
   }

   public boolean remove(Element element) {
      return this.removeNode(element);
   }

   public boolean remove(Entity entity) {
      return this.removeNode(entity);
   }

   public boolean remove(Namespace namespace) {
      return this.removeNode(namespace);
   }

   public boolean remove(ProcessingInstruction pi) {
      return this.removeNode(pi);
   }

   public boolean remove(Text text) {
      return this.removeNode(text);
   }

   public boolean hasMixedContent() {
      List content = this.contentList();
      if (content != null && !content.isEmpty() && content.size() >= 2) {
         Class prevClass = null;

         for(Object object : content) {
            Class newClass = object.getClass();
            if (newClass != prevClass) {
               if (prevClass != null) {
                  return true;
               }

               prevClass = newClass;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public boolean isTextOnly() {
      List content = this.contentList();
      if (content != null && !content.isEmpty()) {
         for(Object object : content) {
            if (!(object instanceof CharacterData) && !(object instanceof String)) {
               return false;
            }
         }

         return true;
      } else {
         return true;
      }
   }

   public void setText(String text) {
      List allContent = this.contentList();
      if (allContent != null) {
         Iterator it = allContent.iterator();

         while(it.hasNext()) {
            Node node = (Node)it.next();
            switch (node.getNodeType()) {
               case 3:
               case 4:
               case 5:
                  it.remove();
            }
         }
      }

      this.addText(text);
   }

   public String getStringValue() {
      List list = this.contentList();
      int size = list.size();
      if (size > 0) {
         if (size == 1) {
            return this.getContentAsStringValue(list.get(0));
         } else {
            StringBuffer buffer = new StringBuffer();

            for(int i = 0; i < size; ++i) {
               Object node = list.get(i);
               String string = this.getContentAsStringValue(node);
               if (string.length() > 0) {
                  buffer.append(string);
               }
            }

            return buffer.toString();
         }
      } else {
         return "";
      }
   }

   public void normalize() {
      List content = this.contentList();
      Text previousText = null;
      int i = 0;

      while(i < content.size()) {
         Node node = (Node)content.get(i);
         if (node instanceof Text) {
            Text text = (Text)node;
            if (previousText != null) {
               previousText.appendText(text.getText());
               this.remove(text);
            } else {
               String value = text.getText();
               if (value != null && value.length() > 0) {
                  previousText = text;
                  ++i;
               } else {
                  this.remove(text);
               }
            }
         } else {
            if (node instanceof Element) {
               Element element = (Element)node;
               element.normalize();
            }

            previousText = null;
            ++i;
         }
      }

   }

   public String elementText(String name) {
      Element element = this.element(name);
      return element != null ? element.getText() : null;
   }

   public String elementText(QName qName) {
      Element element = this.element(qName);
      return element != null ? element.getText() : null;
   }

   public String elementTextTrim(String name) {
      Element element = this.element(name);
      return element != null ? element.getTextTrim() : null;
   }

   public String elementTextTrim(QName qName) {
      Element element = this.element(qName);
      return element != null ? element.getTextTrim() : null;
   }

   public void appendAttributes(Element element) {
      int i = 0;

      for(int size = element.attributeCount(); i < size; ++i) {
         Attribute attribute = element.attribute(i);
         if (attribute.supportsParent()) {
            this.addAttribute(attribute.getQName(), attribute.getValue());
         } else {
            this.add(attribute);
         }
      }

   }

   public Element createCopy() {
      Element clone = this.createElement(this.getQName());
      clone.appendAttributes(this);
      clone.appendContent(this);
      return clone;
   }

   public Element createCopy(String name) {
      Element clone = this.createElement(name);
      clone.appendAttributes(this);
      clone.appendContent(this);
      return clone;
   }

   public Element createCopy(QName qName) {
      Element clone = this.createElement(qName);
      clone.appendAttributes(this);
      clone.appendContent(this);
      return clone;
   }

   public QName getQName(String qualifiedName) {
      String prefix = "";
      String localName = qualifiedName;
      int index = qualifiedName.indexOf(":");
      if (index > 0) {
         prefix = qualifiedName.substring(0, index);
         localName = qualifiedName.substring(index + 1);
      }

      Namespace namespace = this.getNamespaceForPrefix(prefix);
      return namespace != null ? this.getDocumentFactory().createQName(localName, namespace) : this.getDocumentFactory().createQName(localName);
   }

   public Namespace getNamespaceForPrefix(String prefix) {
      if (prefix == null) {
         prefix = "";
      }

      if (prefix.equals(this.getNamespacePrefix())) {
         return this.getNamespace();
      } else if (prefix.equals("xml")) {
         return Namespace.XML_NAMESPACE;
      } else {
         List list = this.contentList();
         int size = list.size();

         for(int i = 0; i < size; ++i) {
            Object object = list.get(i);
            if (object instanceof Namespace) {
               Namespace namespace = (Namespace)object;
               if (prefix.equals(namespace.getPrefix())) {
                  return namespace;
               }
            }
         }

         Element parent = this.getParent();
         if (parent != null) {
            Namespace answer = parent.getNamespaceForPrefix(prefix);
            if (answer != null) {
               return answer;
            }
         }

         if (prefix != null && prefix.length() > 0) {
            return null;
         } else {
            return Namespace.NO_NAMESPACE;
         }
      }
   }

   public Namespace getNamespaceForURI(String uri) {
      if (uri != null && uri.length() > 0) {
         if (uri.equals(this.getNamespaceURI())) {
            return this.getNamespace();
         } else {
            List list = this.contentList();
            int size = list.size();

            for(int i = 0; i < size; ++i) {
               Object object = list.get(i);
               if (object instanceof Namespace) {
                  Namespace namespace = (Namespace)object;
                  if (uri.equals(namespace.getURI())) {
                     return namespace;
                  }
               }
            }

            return null;
         }
      } else {
         return Namespace.NO_NAMESPACE;
      }
   }

   public List getNamespacesForURI(String uri) {
      BackedList answer = this.createResultList();
      List list = this.contentList();
      int size = list.size();

      for(int i = 0; i < size; ++i) {
         Object object = list.get(i);
         if (object instanceof Namespace && ((Namespace)object).getURI().equals(uri)) {
            answer.addLocal(object);
         }
      }

      return answer;
   }

   public List declaredNamespaces() {
      BackedList answer = this.createResultList();
      List list = this.contentList();
      int size = list.size();

      for(int i = 0; i < size; ++i) {
         Object object = list.get(i);
         if (object instanceof Namespace) {
            answer.addLocal(object);
         }
      }

      return answer;
   }

   public List additionalNamespaces() {
      List list = this.contentList();
      int size = list.size();
      BackedList answer = this.createResultList();

      for(int i = 0; i < size; ++i) {
         Object object = list.get(i);
         if (object instanceof Namespace) {
            Namespace namespace = (Namespace)object;
            if (!namespace.equals(this.getNamespace())) {
               answer.addLocal(namespace);
            }
         }
      }

      return answer;
   }

   public List additionalNamespaces(String defaultNamespaceURI) {
      List list = this.contentList();
      BackedList answer = this.createResultList();
      int size = list.size();

      for(int i = 0; i < size; ++i) {
         Object object = list.get(i);
         if (object instanceof Namespace) {
            Namespace namespace = (Namespace)object;
            if (!defaultNamespaceURI.equals(namespace.getURI())) {
               answer.addLocal(namespace);
            }
         }
      }

      return answer;
   }

   public void ensureAttributesCapacity(int minCapacity) {
      if (minCapacity > 1) {
         List list = this.attributeList();
         if (list instanceof ArrayList) {
            ArrayList arrayList = (ArrayList)list;
            arrayList.ensureCapacity(minCapacity);
         }
      }

   }

   protected Element createElement(String name) {
      return this.getDocumentFactory().createElement(name);
   }

   protected Element createElement(QName qName) {
      return this.getDocumentFactory().createElement(qName);
   }

   protected void addNode(Node node) {
      if (node.getParent() != null) {
         String message = "The Node already has an existing parent of \"" + node.getParent().getQualifiedName() + "\"";
         throw new IllegalAddException(this, node, message);
      } else {
         this.addNewNode(node);
      }
   }

   protected void addNode(int index, Node node) {
      if (node.getParent() != null) {
         String message = "The Node already has an existing parent of \"" + node.getParent().getQualifiedName() + "\"";
         throw new IllegalAddException(this, node, message);
      } else {
         this.addNewNode(index, node);
      }
   }

   protected void addNewNode(Node node) {
      this.contentList().add(node);
      this.childAdded(node);
   }

   protected void addNewNode(int index, Node node) {
      this.contentList().add(index, node);
      this.childAdded(node);
   }

   protected boolean removeNode(Node node) {
      boolean answer = this.contentList().remove(node);
      if (answer) {
         this.childRemoved(node);
      }

      return answer;
   }

   protected void childAdded(Node node) {
      if (node != null) {
         node.setParent(this);
      }

   }

   protected void childRemoved(Node node) {
      if (node != null) {
         node.setParent((Element)null);
         node.setDocument((Document)null);
      }

   }

   protected abstract List attributeList();

   protected abstract List attributeList(int var1);

   protected DocumentFactory getDocumentFactory() {
      QName qName = this.getQName();
      if (qName != null) {
         DocumentFactory factory = qName.getDocumentFactory();
         if (factory != null) {
            return factory;
         }
      }

      return DOCUMENT_FACTORY;
   }

   protected List createAttributeList() {
      return this.createAttributeList(5);
   }

   protected List createAttributeList(int size) {
      return new ArrayList(size);
   }

   protected Iterator createSingleIterator(Object result) {
      return new SingleIterator(result);
   }

   static {
      EMPTY_LIST = Collections.EMPTY_LIST;
      EMPTY_ITERATOR = EMPTY_LIST.iterator();
   }
}
