package org.hibernate.proxy.dom4j;

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
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

public class Dom4jProxy implements HibernateProxy, Element, Serializable {
   private Dom4jLazyInitializer li;

   public Dom4jProxy(Dom4jLazyInitializer li) {
      this.li = li;
   }

   public Object writeReplace() {
      return this;
   }

   public LazyInitializer getHibernateLazyInitializer() {
      return this.li;
   }

   public QName getQName() {
      return this.target().getQName();
   }

   public QName getQName(String s) {
      return this.target().getQName(s);
   }

   public void setQName(QName qName) {
      this.target().setQName(qName);
   }

   public Namespace getNamespace() {
      return this.target().getNamespace();
   }

   public Namespace getNamespaceForPrefix(String s) {
      return this.target().getNamespaceForPrefix(s);
   }

   public Namespace getNamespaceForURI(String s) {
      return this.target().getNamespaceForURI(s);
   }

   public List getNamespacesForURI(String s) {
      return this.target().getNamespacesForURI(s);
   }

   public String getNamespacePrefix() {
      return this.target().getNamespacePrefix();
   }

   public String getNamespaceURI() {
      return this.target().getNamespaceURI();
   }

   public String getQualifiedName() {
      return this.target().getQualifiedName();
   }

   public List additionalNamespaces() {
      return this.target().additionalNamespaces();
   }

   public List declaredNamespaces() {
      return this.target().declaredNamespaces();
   }

   public Element addAttribute(String attrName, String text) {
      return this.target().addAttribute(attrName, text);
   }

   public Element addAttribute(QName attrName, String text) {
      return this.target().addAttribute(attrName, text);
   }

   public Element addComment(String text) {
      return this.target().addComment(text);
   }

   public Element addCDATA(String text) {
      return this.target().addCDATA(text);
   }

   public Element addEntity(String name, String text) {
      return this.target().addEntity(name, text);
   }

   public Element addNamespace(String prefix, String uri) {
      return this.target().addNamespace(prefix, uri);
   }

   public Element addProcessingInstruction(String target, String text) {
      return this.target().addProcessingInstruction(target, text);
   }

   public Element addProcessingInstruction(String target, Map data) {
      return this.target().addProcessingInstruction(target, data);
   }

   public Element addText(String text) {
      return this.target().addText(text);
   }

   public void add(Attribute attribute) {
      this.target().add(attribute);
   }

   public void add(CDATA cdata) {
      this.target().add(cdata);
   }

   public void add(Entity entity) {
      this.target().add(entity);
   }

   public void add(Text text) {
      this.target().add(text);
   }

   public void add(Namespace namespace) {
      this.target().add(namespace);
   }

   public boolean remove(Attribute attribute) {
      return this.target().remove(attribute);
   }

   public boolean remove(CDATA cdata) {
      return this.target().remove(cdata);
   }

   public boolean remove(Entity entity) {
      return this.target().remove(entity);
   }

   public boolean remove(Namespace namespace) {
      return this.target().remove(namespace);
   }

   public boolean remove(Text text) {
      return this.target().remove(text);
   }

   public boolean supportsParent() {
      return this.target().supportsParent();
   }

   public Element getParent() {
      return this.target().getParent();
   }

   public void setParent(Element element) {
      this.target().setParent(element);
   }

   public Document getDocument() {
      return this.target().getDocument();
   }

   public void setDocument(Document document) {
      this.target().setDocument(document);
   }

   public boolean isReadOnly() {
      return this.target().isReadOnly();
   }

   public boolean hasContent() {
      return this.target().hasContent();
   }

   public String getName() {
      return this.target().getName();
   }

   public void setName(String name) {
      this.target().setName(name);
   }

   public String getText() {
      return this.target().getText();
   }

   public void setText(String text) {
      this.target().setText(text);
   }

   public String getTextTrim() {
      return this.target().getTextTrim();
   }

   public String getStringValue() {
      return this.target().getStringValue();
   }

   public String getPath() {
      return this.target().getPath();
   }

   public String getPath(Element element) {
      return this.target().getPath(element);
   }

   public String getUniquePath() {
      return this.target().getUniquePath();
   }

   public String getUniquePath(Element element) {
      return this.target().getUniquePath(element);
   }

   public String asXML() {
      return this.target().asXML();
   }

   public void write(Writer writer) throws IOException {
      this.target().write(writer);
   }

   public short getNodeType() {
      return this.target().getNodeType();
   }

   public String getNodeTypeName() {
      return this.target().getNodeTypeName();
   }

   public Node detach() {
      Element parent = this.target().getParent();
      if (parent != null) {
         parent.remove(this);
      }

      return this.target().detach();
   }

   public List selectNodes(String xpath) {
      return this.target().selectNodes(xpath);
   }

   public Object selectObject(String xpath) {
      return this.target().selectObject(xpath);
   }

   public List selectNodes(String xpath, String comparison) {
      return this.target().selectNodes(xpath, comparison);
   }

   public List selectNodes(String xpath, String comparison, boolean removeDups) {
      return this.target().selectNodes(xpath, comparison, removeDups);
   }

   public Node selectSingleNode(String xpath) {
      return this.target().selectSingleNode(xpath);
   }

   public String valueOf(String xpath) {
      return this.target().valueOf(xpath);
   }

   public Number numberValueOf(String xpath) {
      return this.target().numberValueOf(xpath);
   }

   public boolean matches(String xpath) {
      return this.target().matches(xpath);
   }

   public XPath createXPath(String xpath) throws InvalidXPathException {
      return this.target().createXPath(xpath);
   }

   public Node asXPathResult(Element element) {
      return this.target().asXPathResult(element);
   }

   public void accept(Visitor visitor) {
      this.target().accept(visitor);
   }

   public Object clone() {
      return this.target().clone();
   }

   public Object getData() {
      return this.target().getData();
   }

   public void setData(Object data) {
      this.target().setData(data);
   }

   public List attributes() {
      return this.target().attributes();
   }

   public void setAttributes(List list) {
      this.target().setAttributes(list);
   }

   public int attributeCount() {
      return this.target().attributeCount();
   }

   public Iterator attributeIterator() {
      return this.target().attributeIterator();
   }

   public Attribute attribute(int i) {
      return this.target().attribute(i);
   }

   public Attribute attribute(String name) {
      return this.target().attribute(name);
   }

   public Attribute attribute(QName qName) {
      return this.target().attribute(qName);
   }

   public String attributeValue(String name) {
      return this.target().attributeValue(name);
   }

   public String attributeValue(String name, String defaultValue) {
      return this.target().attributeValue(name, defaultValue);
   }

   public String attributeValue(QName qName) {
      return this.target().attributeValue(qName);
   }

   public String attributeValue(QName qName, String defaultValue) {
      return this.target().attributeValue(qName, defaultValue);
   }

   /** @deprecated */
   public void setAttributeValue(String name, String value) {
      this.target().setAttributeValue(name, value);
   }

   /** @deprecated */
   public void setAttributeValue(QName qName, String value) {
      this.target().setAttributeValue(qName, value);
   }

   public Element element(String name) {
      return this.target().element(name);
   }

   public Element element(QName qName) {
      return this.target().element(qName);
   }

   public List elements() {
      return this.target().elements();
   }

   public List elements(String name) {
      return this.target().elements(name);
   }

   public List elements(QName qName) {
      return this.target().elements(qName);
   }

   public Iterator elementIterator() {
      return this.target().elementIterator();
   }

   public Iterator elementIterator(String name) {
      return this.target().elementIterator(name);
   }

   public Iterator elementIterator(QName qName) {
      return this.target().elementIterator(qName);
   }

   public boolean isRootElement() {
      return this.target().isRootElement();
   }

   public boolean hasMixedContent() {
      return this.target().hasMixedContent();
   }

   public boolean isTextOnly() {
      return this.target().isTextOnly();
   }

   public void appendAttributes(Element element) {
      this.target().appendAttributes(element);
   }

   public Element createCopy() {
      return this.target().createCopy();
   }

   public Element createCopy(String name) {
      return this.target().createCopy(name);
   }

   public Element createCopy(QName qName) {
      return this.target().createCopy(qName);
   }

   public String elementText(String name) {
      return this.target().elementText(name);
   }

   public String elementText(QName qName) {
      return this.target().elementText(qName);
   }

   public String elementTextTrim(String name) {
      return this.target().elementTextTrim(name);
   }

   public String elementTextTrim(QName qName) {
      return this.target().elementTextTrim(qName);
   }

   public Node getXPathResult(int i) {
      return this.target().getXPathResult(i);
   }

   public Node node(int i) {
      return this.target().node(i);
   }

   public int indexOf(Node node) {
      return this.target().indexOf(node);
   }

   public int nodeCount() {
      return this.target().nodeCount();
   }

   public Element elementByID(String id) {
      return this.target().elementByID(id);
   }

   public List content() {
      return this.target().content();
   }

   public Iterator nodeIterator() {
      return this.target().nodeIterator();
   }

   public void setContent(List list) {
      this.target().setContent(list);
   }

   public void appendContent(Branch branch) {
      this.target().appendContent(branch);
   }

   public void clearContent() {
      this.target().clearContent();
   }

   public List processingInstructions() {
      return this.target().processingInstructions();
   }

   public List processingInstructions(String name) {
      return this.target().processingInstructions(name);
   }

   public ProcessingInstruction processingInstruction(String name) {
      return this.target().processingInstruction(name);
   }

   public void setProcessingInstructions(List list) {
      this.target().setProcessingInstructions(list);
   }

   public Element addElement(String name) {
      return this.target().addElement(name);
   }

   public Element addElement(QName qName) {
      return this.target().addElement(qName);
   }

   public Element addElement(String name, String text) {
      return this.target().addElement(name, text);
   }

   public boolean removeProcessingInstruction(String name) {
      return this.target().removeProcessingInstruction(name);
   }

   public void add(Node node) {
      this.target().add(node);
   }

   public void add(Comment comment) {
      this.target().add(comment);
   }

   public void add(Element element) {
      this.target().add(element);
   }

   public void add(ProcessingInstruction processingInstruction) {
      this.target().add(processingInstruction);
   }

   public boolean remove(Node node) {
      return this.target().remove(node);
   }

   public boolean remove(Comment comment) {
      return this.target().remove(comment);
   }

   public boolean remove(Element element) {
      return this.target().remove(element);
   }

   public boolean remove(ProcessingInstruction processingInstruction) {
      return this.target().remove(processingInstruction);
   }

   public void normalize() {
      this.target().normalize();
   }

   private Element target() {
      return this.li.getElement();
   }
}
