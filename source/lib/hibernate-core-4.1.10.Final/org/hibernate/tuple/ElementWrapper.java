package org.hibernate.tuple;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.dom4j.Attribute;
import org.dom4j.Branch;
import org.dom4j.CDATA;
import org.dom4j.Comment;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Entity;
import org.dom4j.InvalidXPathException;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.ProcessingInstruction;
import org.dom4j.QName;
import org.dom4j.Text;
import org.dom4j.Visitor;
import org.dom4j.XPath;

public class ElementWrapper implements Element, Serializable {
   private Element element;
   private Element parent;

   public Element getElement() {
      return this.element;
   }

   public ElementWrapper(Element element) {
      this.element = element;
   }

   public QName getQName() {
      return this.element.getQName();
   }

   public QName getQName(String s) {
      return this.element.getQName(s);
   }

   public void setQName(QName qName) {
      this.element.setQName(qName);
   }

   public Namespace getNamespace() {
      return this.element.getNamespace();
   }

   public Namespace getNamespaceForPrefix(String s) {
      return this.element.getNamespaceForPrefix(s);
   }

   public Namespace getNamespaceForURI(String s) {
      return this.element.getNamespaceForURI(s);
   }

   public List getNamespacesForURI(String s) {
      return this.element.getNamespacesForURI(s);
   }

   public String getNamespacePrefix() {
      return this.element.getNamespacePrefix();
   }

   public String getNamespaceURI() {
      return this.element.getNamespaceURI();
   }

   public String getQualifiedName() {
      return this.element.getQualifiedName();
   }

   public List additionalNamespaces() {
      return this.element.additionalNamespaces();
   }

   public List declaredNamespaces() {
      return this.element.declaredNamespaces();
   }

   public Element addAttribute(String attrName, String text) {
      return this.element.addAttribute(attrName, text);
   }

   public Element addAttribute(QName attrName, String text) {
      return this.element.addAttribute(attrName, text);
   }

   public Element addComment(String text) {
      return this.element.addComment(text);
   }

   public Element addCDATA(String text) {
      return this.element.addCDATA(text);
   }

   public Element addEntity(String name, String text) {
      return this.element.addEntity(name, text);
   }

   public Element addNamespace(String prefix, String uri) {
      return this.element.addNamespace(prefix, uri);
   }

   public Element addProcessingInstruction(String target, String text) {
      return this.element.addProcessingInstruction(target, text);
   }

   public Element addProcessingInstruction(String target, Map data) {
      return this.element.addProcessingInstruction(target, data);
   }

   public Element addText(String text) {
      return this.element.addText(text);
   }

   public void add(Attribute attribute) {
      this.element.add(attribute);
   }

   public void add(CDATA cdata) {
      this.element.add(cdata);
   }

   public void add(Entity entity) {
      this.element.add(entity);
   }

   public void add(Text text) {
      this.element.add(text);
   }

   public void add(Namespace namespace) {
      this.element.add(namespace);
   }

   public boolean remove(Attribute attribute) {
      return this.element.remove(attribute);
   }

   public boolean remove(CDATA cdata) {
      return this.element.remove(cdata);
   }

   public boolean remove(Entity entity) {
      return this.element.remove(entity);
   }

   public boolean remove(Namespace namespace) {
      return this.element.remove(namespace);
   }

   public boolean remove(Text text) {
      return this.element.remove(text);
   }

   public boolean supportsParent() {
      return this.element.supportsParent();
   }

   public Element getParent() {
      return this.parent == null ? this.element.getParent() : this.parent;
   }

   public void setParent(Element parent) {
      this.element.setParent(parent);
      this.parent = parent;
   }

   public Document getDocument() {
      return this.element.getDocument();
   }

   public void setDocument(Document document) {
      this.element.setDocument(document);
   }

   public boolean isReadOnly() {
      return this.element.isReadOnly();
   }

   public boolean hasContent() {
      return this.element.hasContent();
   }

   public String getName() {
      return this.element.getName();
   }

   public void setName(String name) {
      this.element.setName(name);
   }

   public String getText() {
      return this.element.getText();
   }

   public void setText(String text) {
      this.element.setText(text);
   }

   public String getTextTrim() {
      return this.element.getTextTrim();
   }

   public String getStringValue() {
      return this.element.getStringValue();
   }

   public String getPath() {
      return this.element.getPath();
   }

   public String getPath(Element element) {
      return element.getPath(element);
   }

   public String getUniquePath() {
      return this.element.getUniquePath();
   }

   public String getUniquePath(Element element) {
      return element.getUniquePath(element);
   }

   public String asXML() {
      return this.element.asXML();
   }

   public void write(Writer writer) throws IOException {
      this.element.write(writer);
   }

   public short getNodeType() {
      return this.element.getNodeType();
   }

   public String getNodeTypeName() {
      return this.element.getNodeTypeName();
   }

   public Node detach() {
      if (this.parent != null) {
         this.parent.remove(this);
         this.parent = null;
      }

      return this.element.detach();
   }

   public List selectNodes(String xpath) {
      return this.element.selectNodes(xpath);
   }

   public Object selectObject(String xpath) {
      return this.element.selectObject(xpath);
   }

   public List selectNodes(String xpath, String comparison) {
      return this.element.selectNodes(xpath, comparison);
   }

   public List selectNodes(String xpath, String comparison, boolean removeDups) {
      return this.element.selectNodes(xpath, comparison, removeDups);
   }

   public Node selectSingleNode(String xpath) {
      return this.element.selectSingleNode(xpath);
   }

   public String valueOf(String xpath) {
      return this.element.valueOf(xpath);
   }

   public Number numberValueOf(String xpath) {
      return this.element.numberValueOf(xpath);
   }

   public boolean matches(String xpath) {
      return this.element.matches(xpath);
   }

   public XPath createXPath(String xpath) throws InvalidXPathException {
      return this.element.createXPath(xpath);
   }

   public Node asXPathResult(Element element) {
      return element.asXPathResult(element);
   }

   public void accept(Visitor visitor) {
      this.element.accept(visitor);
   }

   public Object clone() {
      return this.element.clone();
   }

   public Object getData() {
      return this.element.getData();
   }

   public void setData(Object data) {
      this.element.setData(data);
   }

   public List attributes() {
      return this.element.attributes();
   }

   public void setAttributes(List list) {
      this.element.setAttributes(list);
   }

   public int attributeCount() {
      return this.element.attributeCount();
   }

   public Iterator attributeIterator() {
      return this.element.attributeIterator();
   }

   public Attribute attribute(int i) {
      return this.element.attribute(i);
   }

   public Attribute attribute(String name) {
      return this.element.attribute(name);
   }

   public Attribute attribute(QName qName) {
      return this.element.attribute(qName);
   }

   public String attributeValue(String name) {
      return this.element.attributeValue(name);
   }

   public String attributeValue(String name, String defaultValue) {
      return this.element.attributeValue(name, defaultValue);
   }

   public String attributeValue(QName qName) {
      return this.element.attributeValue(qName);
   }

   public String attributeValue(QName qName, String defaultValue) {
      return this.element.attributeValue(qName, defaultValue);
   }

   /** @deprecated */
   public void setAttributeValue(String name, String value) {
      this.element.setAttributeValue(name, value);
   }

   /** @deprecated */
   public void setAttributeValue(QName qName, String value) {
      this.element.setAttributeValue(qName, value);
   }

   public Element element(String name) {
      return this.element.element(name);
   }

   public Element element(QName qName) {
      return this.element.element(qName);
   }

   public List elements() {
      return this.element.elements();
   }

   public List elements(String name) {
      return this.element.elements(name);
   }

   public List elements(QName qName) {
      return this.element.elements(qName);
   }

   public Iterator elementIterator() {
      return this.element.elementIterator();
   }

   public Iterator elementIterator(String name) {
      return this.element.elementIterator(name);
   }

   public Iterator elementIterator(QName qName) {
      return this.element.elementIterator(qName);
   }

   public boolean isRootElement() {
      return this.element.isRootElement();
   }

   public boolean hasMixedContent() {
      return this.element.hasMixedContent();
   }

   public boolean isTextOnly() {
      return this.element.isTextOnly();
   }

   public void appendAttributes(Element element) {
      element.appendAttributes(element);
   }

   public Element createCopy() {
      return this.element.createCopy();
   }

   public Element createCopy(String name) {
      return this.element.createCopy(name);
   }

   public Element createCopy(QName qName) {
      return this.element.createCopy(qName);
   }

   public String elementText(String name) {
      return this.element.elementText(name);
   }

   public String elementText(QName qName) {
      return this.element.elementText(qName);
   }

   public String elementTextTrim(String name) {
      return this.element.elementTextTrim(name);
   }

   public String elementTextTrim(QName qName) {
      return this.element.elementTextTrim(qName);
   }

   public Node getXPathResult(int i) {
      return this.element.getXPathResult(i);
   }

   public Node node(int i) {
      return this.element.node(i);
   }

   public int indexOf(Node node) {
      return this.element.indexOf(node);
   }

   public int nodeCount() {
      return this.element.nodeCount();
   }

   public Element elementByID(String id) {
      return this.element.elementByID(id);
   }

   public List content() {
      return this.element.content();
   }

   public Iterator nodeIterator() {
      return this.element.nodeIterator();
   }

   public void setContent(List list) {
      this.element.setContent(list);
   }

   public void appendContent(Branch branch) {
      this.element.appendContent(branch);
   }

   public void clearContent() {
      this.element.clearContent();
   }

   public List processingInstructions() {
      return this.element.processingInstructions();
   }

   public List processingInstructions(String name) {
      return this.element.processingInstructions(name);
   }

   public ProcessingInstruction processingInstruction(String name) {
      return this.element.processingInstruction(name);
   }

   public void setProcessingInstructions(List list) {
      this.element.setProcessingInstructions(list);
   }

   public Element addElement(String name) {
      return this.element.addElement(name);
   }

   public Element addElement(QName qName) {
      return this.element.addElement(qName);
   }

   public Element addElement(String name, String text) {
      return this.element.addElement(name, text);
   }

   public boolean removeProcessingInstruction(String name) {
      return this.element.removeProcessingInstruction(name);
   }

   public void add(Node node) {
      this.element.add(node);
   }

   public void add(Comment comment) {
      this.element.add(comment);
   }

   public void add(Element element) {
      element.add(element);
   }

   public void add(ProcessingInstruction processingInstruction) {
      this.element.add(processingInstruction);
   }

   public boolean remove(Node node) {
      return this.element.remove(node);
   }

   public boolean remove(Comment comment) {
      return this.element.remove(comment);
   }

   public boolean remove(Element element) {
      return element.remove(element);
   }

   public boolean remove(ProcessingInstruction processingInstruction) {
      return this.element.remove(processingInstruction);
   }

   public void normalize() {
      this.element.normalize();
   }

   public boolean equals(Object other) {
      return this.element.equals(other);
   }

   public int hashCode() {
      return this.element.hashCode();
   }

   public String toString() {
      return this.element.toString();
   }
}
