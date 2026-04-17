package org.dom4j.tree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.dom4j.Attribute;
import org.dom4j.Branch;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.IllegalAddException;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.ProcessingInstruction;
import org.dom4j.QName;

public class DefaultElement extends AbstractElement {
   private static final transient DocumentFactory DOCUMENT_FACTORY = DocumentFactory.getInstance();
   private QName qname;
   private Branch parentBranch;
   private Object content;
   private Object attributes;

   public DefaultElement(String name) {
      this.qname = DOCUMENT_FACTORY.createQName(name);
   }

   public DefaultElement(QName qname) {
      this.qname = qname;
   }

   public DefaultElement(QName qname, int attributeCount) {
      this.qname = qname;
      if (attributeCount > 1) {
         this.attributes = new ArrayList(attributeCount);
      }

   }

   public DefaultElement(String name, Namespace namespace) {
      this.qname = DOCUMENT_FACTORY.createQName(name, namespace);
   }

   public Element getParent() {
      Element result = null;
      if (this.parentBranch instanceof Element) {
         result = (Element)this.parentBranch;
      }

      return result;
   }

   public void setParent(Element parent) {
      if (this.parentBranch instanceof Element || parent != null) {
         this.parentBranch = parent;
      }

   }

   public Document getDocument() {
      if (this.parentBranch instanceof Document) {
         return (Document)this.parentBranch;
      } else if (this.parentBranch instanceof Element) {
         Element parent = (Element)this.parentBranch;
         return parent.getDocument();
      } else {
         return null;
      }
   }

   public void setDocument(Document document) {
      if (this.parentBranch instanceof Document || document != null) {
         this.parentBranch = document;
      }

   }

   public boolean supportsParent() {
      return true;
   }

   public QName getQName() {
      return this.qname;
   }

   public void setQName(QName name) {
      this.qname = name;
   }

   public String getText() {
      Object contentShadow = this.content;
      if (contentShadow instanceof List) {
         return super.getText();
      } else {
         return contentShadow != null ? this.getContentAsText(contentShadow) : "";
      }
   }

   public String getStringValue() {
      Object contentShadow = this.content;
      if (contentShadow instanceof List) {
         List list = (List)contentShadow;
         int size = list.size();
         if (size > 0) {
            if (size == 1) {
               return this.getContentAsStringValue(list.get(0));
            }

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
      } else if (contentShadow != null) {
         return this.getContentAsStringValue(contentShadow);
      }

      return "";
   }

   public Object clone() {
      DefaultElement answer = (DefaultElement)super.clone();
      if (answer != this) {
         answer.content = null;
         answer.attributes = null;
         answer.appendAttributes(this);
         answer.appendContent(this);
      }

      return answer;
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
         Object contentShadow = this.content;
         if (contentShadow instanceof List) {
            List list = (List)contentShadow;
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
         } else if (contentShadow instanceof Namespace) {
            Namespace namespace = (Namespace)contentShadow;
            if (prefix.equals(namespace.getPrefix())) {
               return namespace;
            }
         }

         contentShadow = this.getParent();
         if (contentShadow != null) {
            Namespace answer = ((Element)contentShadow).getNamespaceForPrefix(prefix);
            if (answer != null) {
               return answer;
            }
         }

         return prefix != null && prefix.length() > 0 ? null : Namespace.NO_NAMESPACE;
      }
   }

   public Namespace getNamespaceForURI(String uri) {
      if (uri != null && uri.length() > 0) {
         if (uri.equals(this.getNamespaceURI())) {
            return this.getNamespace();
         } else {
            Object contentShadow = this.content;
            if (contentShadow instanceof List) {
               List list = (List)contentShadow;
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
            } else if (contentShadow instanceof Namespace) {
               Namespace namespace = (Namespace)contentShadow;
               if (uri.equals(namespace.getURI())) {
                  return namespace;
               }
            }

            Element parent = this.getParent();
            return parent != null ? parent.getNamespaceForURI(uri) : null;
         }
      } else {
         return Namespace.NO_NAMESPACE;
      }
   }

   public List declaredNamespaces() {
      BackedList answer = this.createResultList();
      Object contentShadow = this.content;
      if (contentShadow instanceof List) {
         List list = (List)contentShadow;
         int size = list.size();

         for(int i = 0; i < size; ++i) {
            Object object = list.get(i);
            if (object instanceof Namespace) {
               answer.addLocal(object);
            }
         }
      } else if (contentShadow instanceof Namespace) {
         answer.addLocal(contentShadow);
      }

      return answer;
   }

   public List additionalNamespaces() {
      Object contentShadow = this.content;
      if (contentShadow instanceof List) {
         List list = (List)contentShadow;
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
      } else if (contentShadow instanceof Namespace) {
         Namespace namespace = (Namespace)contentShadow;
         return namespace.equals(this.getNamespace()) ? this.createEmptyList() : this.createSingleResultList(namespace);
      } else {
         return this.createEmptyList();
      }
   }

   public List additionalNamespaces(String defaultNamespaceURI) {
      Object contentShadow = this.content;
      if (contentShadow instanceof List) {
         List list = (List)contentShadow;
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
      } else {
         if (contentShadow instanceof Namespace) {
            Namespace namespace = (Namespace)contentShadow;
            if (!defaultNamespaceURI.equals(namespace.getURI())) {
               return this.createSingleResultList(namespace);
            }
         }

         return this.createEmptyList();
      }
   }

   public List processingInstructions() {
      Object contentShadow = this.content;
      if (contentShadow instanceof List) {
         List list = (List)contentShadow;
         BackedList answer = this.createResultList();
         int size = list.size();

         for(int i = 0; i < size; ++i) {
            Object object = list.get(i);
            if (object instanceof ProcessingInstruction) {
               answer.addLocal(object);
            }
         }

         return answer;
      } else {
         return contentShadow instanceof ProcessingInstruction ? this.createSingleResultList(contentShadow) : this.createEmptyList();
      }
   }

   public List processingInstructions(String target) {
      Object shadow = this.content;
      if (shadow instanceof List) {
         List list = (List)shadow;
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
      } else {
         if (shadow instanceof ProcessingInstruction) {
            ProcessingInstruction pi = (ProcessingInstruction)shadow;
            if (target.equals(pi.getName())) {
               return this.createSingleResultList(pi);
            }
         }

         return this.createEmptyList();
      }
   }

   public ProcessingInstruction processingInstruction(String target) {
      Object shadow = this.content;
      if (shadow instanceof List) {
         List list = (List)shadow;
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
      } else if (shadow instanceof ProcessingInstruction) {
         ProcessingInstruction pi = (ProcessingInstruction)shadow;
         if (target.equals(pi.getName())) {
            return pi;
         }
      }

      return null;
   }

   public boolean removeProcessingInstruction(String target) {
      Object shadow = this.content;
      if (shadow instanceof List) {
         List list = (List)shadow;
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
      } else if (shadow instanceof ProcessingInstruction) {
         ProcessingInstruction pi = (ProcessingInstruction)shadow;
         if (target.equals(pi.getName())) {
            this.content = null;
            return true;
         }
      }

      return false;
   }

   public Element element(String name) {
      Object contentShadow = this.content;
      if (contentShadow instanceof List) {
         List list = (List)contentShadow;
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
      } else if (contentShadow instanceof Element) {
         Element element = (Element)contentShadow;
         if (name.equals(element.getName())) {
            return element;
         }
      }

      return null;
   }

   public Element element(QName qName) {
      Object contentShadow = this.content;
      if (contentShadow instanceof List) {
         List list = (List)contentShadow;
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
      } else if (contentShadow instanceof Element) {
         Element element = (Element)contentShadow;
         if (qName.equals(element.getQName())) {
            return element;
         }
      }

      return null;
   }

   public Element element(String name, Namespace namespace) {
      return this.element(this.getDocumentFactory().createQName(name, namespace));
   }

   public void setContent(List content) {
      this.contentRemoved();
      if (content instanceof ContentListFacade) {
         content = ((ContentListFacade)content).getBackingList();
      }

      if (content == null) {
         this.content = null;
      } else {
         int size = content.size();
         List newContent = this.createContentList(size);

         for(int i = 0; i < size; ++i) {
            Object object = content.get(i);
            if (object instanceof Node) {
               Node node = (Node)object;
               Element parent = node.getParent();
               if (parent != null && parent != this) {
                  node = (Node)node.clone();
               }

               newContent.add(node);
               this.childAdded(node);
            } else if (object != null) {
               String text = object.toString();
               Node node = this.getDocumentFactory().createText(text);
               newContent.add(node);
               this.childAdded(node);
            }
         }

         this.content = newContent;
      }

   }

   public void clearContent() {
      if (this.content != null) {
         this.contentRemoved();
         this.content = null;
      }

   }

   public Node node(int index) {
      if (index >= 0) {
         Object contentShadow = this.content;
         Object node;
         if (contentShadow instanceof List) {
            List list = (List)contentShadow;
            if (index >= list.size()) {
               return null;
            }

            node = list.get(index);
         } else {
            node = index == 0 ? contentShadow : null;
         }

         if (node != null) {
            if (node instanceof Node) {
               return (Node)node;
            }

            return new DefaultText(node.toString());
         }
      }

      return null;
   }

   public int indexOf(Node node) {
      Object contentShadow = this.content;
      if (contentShadow instanceof List) {
         List list = (List)contentShadow;
         return list.indexOf(node);
      } else {
         return contentShadow != null && contentShadow.equals(node) ? 0 : -1;
      }
   }

   public int nodeCount() {
      Object contentShadow = this.content;
      if (contentShadow instanceof List) {
         List list = (List)contentShadow;
         return list.size();
      } else {
         return contentShadow != null ? 1 : 0;
      }
   }

   public Iterator nodeIterator() {
      Object contentShadow = this.content;
      if (contentShadow instanceof List) {
         List list = (List)contentShadow;
         return list.iterator();
      } else {
         return contentShadow != null ? this.createSingleIterator(contentShadow) : EMPTY_ITERATOR;
      }
   }

   public List attributes() {
      return new ContentListFacade(this, this.attributeList());
   }

   public void setAttributes(List attributes) {
      if (attributes instanceof ContentListFacade) {
         attributes = ((ContentListFacade)attributes).getBackingList();
      }

      this.attributes = attributes;
   }

   public Iterator attributeIterator() {
      Object attributesShadow = this.attributes;
      if (attributesShadow instanceof List) {
         List list = (List)attributesShadow;
         return list.iterator();
      } else {
         return attributesShadow != null ? this.createSingleIterator(attributesShadow) : EMPTY_ITERATOR;
      }
   }

   public Attribute attribute(int index) {
      Object attributesShadow = this.attributes;
      if (attributesShadow instanceof List) {
         List list = (List)attributesShadow;
         return (Attribute)list.get(index);
      } else {
         return attributesShadow != null && index == 0 ? (Attribute)attributesShadow : null;
      }
   }

   public int attributeCount() {
      Object attributesShadow = this.attributes;
      if (attributesShadow instanceof List) {
         List list = (List)attributesShadow;
         return list.size();
      } else {
         return attributesShadow != null ? 1 : 0;
      }
   }

   public Attribute attribute(String name) {
      Object attributesShadow = this.attributes;
      if (attributesShadow instanceof List) {
         List list = (List)attributesShadow;
         int size = list.size();

         for(int i = 0; i < size; ++i) {
            Attribute attribute = (Attribute)list.get(i);
            if (name.equals(attribute.getName())) {
               return attribute;
            }
         }
      } else if (attributesShadow != null) {
         Attribute attribute = (Attribute)attributesShadow;
         if (name.equals(attribute.getName())) {
            return attribute;
         }
      }

      return null;
   }

   public Attribute attribute(QName qName) {
      Object attributesShadow = this.attributes;
      if (attributesShadow instanceof List) {
         List list = (List)attributesShadow;
         int size = list.size();

         for(int i = 0; i < size; ++i) {
            Attribute attribute = (Attribute)list.get(i);
            if (qName.equals(attribute.getQName())) {
               return attribute;
            }
         }
      } else if (attributesShadow != null) {
         Attribute attribute = (Attribute)attributesShadow;
         if (qName.equals(attribute.getQName())) {
            return attribute;
         }
      }

      return null;
   }

   public Attribute attribute(String name, Namespace namespace) {
      return this.attribute(this.getDocumentFactory().createQName(name, namespace));
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
            if (this.attributes == null) {
               this.attributes = attribute;
            } else {
               this.attributeList().add(attribute);
            }

            this.childAdded(attribute);
         }

      }
   }

   public boolean remove(Attribute attribute) {
      boolean answer = false;
      Object attributesShadow = this.attributes;
      if (attributesShadow instanceof List) {
         List list = (List)attributesShadow;
         answer = list.remove(attribute);
         if (!answer) {
            Attribute copy = this.attribute(attribute.getQName());
            if (copy != null) {
               list.remove(copy);
               answer = true;
            }
         }
      } else if (attributesShadow != null) {
         if (attribute.equals(attributesShadow)) {
            this.attributes = null;
            answer = true;
         } else {
            Attribute other = (Attribute)attributesShadow;
            if (attribute.getQName().equals(other.getQName())) {
               this.attributes = null;
               answer = true;
            }
         }
      }

      if (answer) {
         this.childRemoved(attribute);
      }

      return answer;
   }

   protected void addNewNode(Node node) {
      Object contentShadow = this.content;
      if (contentShadow == null) {
         this.content = node;
      } else if (contentShadow instanceof List) {
         List list = (List)contentShadow;
         list.add(node);
      } else {
         List list = this.createContentList();
         list.add(contentShadow);
         list.add(node);
         this.content = list;
      }

      this.childAdded(node);
   }

   protected boolean removeNode(Node node) {
      boolean answer = false;
      Object contentShadow = this.content;
      if (contentShadow != null) {
         if (contentShadow == node) {
            this.content = null;
            answer = true;
         } else if (contentShadow instanceof List) {
            List list = (List)contentShadow;
            answer = list.remove(node);
         }
      }

      if (answer) {
         this.childRemoved(node);
      }

      return answer;
   }

   protected List contentList() {
      Object contentShadow = this.content;
      if (contentShadow instanceof List) {
         return (List)contentShadow;
      } else {
         List list = this.createContentList();
         if (contentShadow != null) {
            list.add(contentShadow);
         }

         this.content = list;
         return list;
      }
   }

   protected List attributeList() {
      Object attributesShadow = this.attributes;
      if (attributesShadow instanceof List) {
         return (List)attributesShadow;
      } else if (attributesShadow != null) {
         List list = this.createAttributeList();
         list.add(attributesShadow);
         this.attributes = list;
         return list;
      } else {
         List list = this.createAttributeList();
         this.attributes = list;
         return list;
      }
   }

   protected List attributeList(int size) {
      Object attributesShadow = this.attributes;
      if (attributesShadow instanceof List) {
         return (List)attributesShadow;
      } else if (attributesShadow != null) {
         List list = this.createAttributeList(size);
         list.add(attributesShadow);
         this.attributes = list;
         return list;
      } else {
         List list = this.createAttributeList(size);
         this.attributes = list;
         return list;
      }
   }

   protected void setAttributeList(List attributeList) {
      this.attributes = attributeList;
   }

   protected DocumentFactory getDocumentFactory() {
      DocumentFactory factory = this.qname.getDocumentFactory();
      return factory != null ? factory : DOCUMENT_FACTORY;
   }
}
