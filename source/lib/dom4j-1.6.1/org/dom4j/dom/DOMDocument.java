package org.dom4j.dom;

import java.util.ArrayList;
import org.dom4j.DocumentFactory;
import org.dom4j.DocumentType;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.tree.DefaultDocument;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

public class DOMDocument extends DefaultDocument implements Document {
   private static final DOMDocumentFactory DOCUMENT_FACTORY = (DOMDocumentFactory)DOMDocumentFactory.getInstance();

   public DOMDocument() {
      this.init();
   }

   public DOMDocument(String name) {
      super(name);
      this.init();
   }

   public DOMDocument(DOMElement rootElement) {
      super((Element)rootElement);
      this.init();
   }

   public DOMDocument(DOMDocumentType docType) {
      super((DocumentType)docType);
      this.init();
   }

   public DOMDocument(DOMElement rootElement, DOMDocumentType docType) {
      super(rootElement, docType);
      this.init();
   }

   public DOMDocument(String name, DOMElement rootElement, DOMDocumentType docType) {
      super(name, rootElement, docType);
      this.init();
   }

   private void init() {
      this.setDocumentFactory(DOCUMENT_FACTORY);
   }

   public boolean supports(String feature, String version) {
      return DOMNodeHelper.supports(this, feature, version);
   }

   public String getNamespaceURI() {
      return DOMNodeHelper.getNamespaceURI(this);
   }

   public String getPrefix() {
      return DOMNodeHelper.getPrefix(this);
   }

   public void setPrefix(String prefix) throws DOMException {
      DOMNodeHelper.setPrefix(this, prefix);
   }

   public String getLocalName() {
      return DOMNodeHelper.getLocalName(this);
   }

   public String getNodeName() {
      return "#document";
   }

   public String getNodeValue() throws DOMException {
      return null;
   }

   public void setNodeValue(String nodeValue) throws DOMException {
   }

   public Node getParentNode() {
      return DOMNodeHelper.getParentNode(this);
   }

   public NodeList getChildNodes() {
      return DOMNodeHelper.createNodeList(this.content());
   }

   public Node getFirstChild() {
      return DOMNodeHelper.asDOMNode(this.node(0));
   }

   public Node getLastChild() {
      return DOMNodeHelper.asDOMNode(this.node(this.nodeCount() - 1));
   }

   public Node getPreviousSibling() {
      return DOMNodeHelper.getPreviousSibling(this);
   }

   public Node getNextSibling() {
      return DOMNodeHelper.getNextSibling(this);
   }

   public NamedNodeMap getAttributes() {
      return null;
   }

   public Document getOwnerDocument() {
      return null;
   }

   public Node insertBefore(Node newChild, Node refChild) throws DOMException {
      this.checkNewChildNode(newChild);
      return DOMNodeHelper.insertBefore(this, newChild, refChild);
   }

   public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
      this.checkNewChildNode(newChild);
      return DOMNodeHelper.replaceChild(this, newChild, oldChild);
   }

   public Node removeChild(Node oldChild) throws DOMException {
      return DOMNodeHelper.removeChild(this, oldChild);
   }

   public Node appendChild(Node newChild) throws DOMException {
      this.checkNewChildNode(newChild);
      return DOMNodeHelper.appendChild(this, newChild);
   }

   private void checkNewChildNode(Node newChild) throws DOMException {
      int nodeType = newChild.getNodeType();
      if (nodeType != 1 && nodeType != 8 && nodeType != 7 && nodeType != 10) {
         throw new DOMException((short)3, "Given node cannot be a child of document");
      }
   }

   public boolean hasChildNodes() {
      return this.nodeCount() > 0;
   }

   public Node cloneNode(boolean deep) {
      return DOMNodeHelper.cloneNode(this, deep);
   }

   public boolean isSupported(String feature, String version) {
      return DOMNodeHelper.isSupported(this, feature, version);
   }

   public boolean hasAttributes() {
      return DOMNodeHelper.hasAttributes(this);
   }

   public NodeList getElementsByTagName(String name) {
      ArrayList list = new ArrayList();
      DOMNodeHelper.appendElementsByTagName(list, this, name);
      return DOMNodeHelper.createNodeList(list);
   }

   public NodeList getElementsByTagNameNS(String namespace, String name) {
      ArrayList list = new ArrayList();
      DOMNodeHelper.appendElementsByTagNameNS(list, this, namespace, name);
      return DOMNodeHelper.createNodeList(list);
   }

   public org.w3c.dom.DocumentType getDoctype() {
      return DOMNodeHelper.asDOMDocumentType(this.getDocType());
   }

   public DOMImplementation getImplementation() {
      return (DOMImplementation)(this.getDocumentFactory() instanceof DOMImplementation ? (DOMImplementation)this.getDocumentFactory() : DOCUMENT_FACTORY);
   }

   public org.w3c.dom.Element getDocumentElement() {
      return DOMNodeHelper.asDOMElement(this.getRootElement());
   }

   public org.w3c.dom.Element createElement(String name) throws DOMException {
      return (org.w3c.dom.Element)this.getDocumentFactory().createElement(name);
   }

   public DocumentFragment createDocumentFragment() {
      DOMNodeHelper.notSupported();
      return null;
   }

   public Text createTextNode(String data) {
      return (Text)this.getDocumentFactory().createText(data);
   }

   public Comment createComment(String data) {
      return (Comment)this.getDocumentFactory().createComment(data);
   }

   public CDATASection createCDATASection(String data) throws DOMException {
      return (CDATASection)this.getDocumentFactory().createCDATA(data);
   }

   public ProcessingInstruction createProcessingInstruction(String target, String data) throws DOMException {
      return (ProcessingInstruction)this.getDocumentFactory().createProcessingInstruction(target, data);
   }

   public Attr createAttribute(String name) throws DOMException {
      QName qname = this.getDocumentFactory().createQName(name);
      return (Attr)this.getDocumentFactory().createAttribute((Element)null, (QName)qname, "");
   }

   public EntityReference createEntityReference(String name) throws DOMException {
      return (EntityReference)this.getDocumentFactory().createEntity(name, (String)null);
   }

   public Node importNode(Node importedNode, boolean deep) throws DOMException {
      DOMNodeHelper.notSupported();
      return null;
   }

   public org.w3c.dom.Element createElementNS(String namespaceURI, String qualifiedName) throws DOMException {
      QName qname = this.getDocumentFactory().createQName(qualifiedName, namespaceURI);
      return (org.w3c.dom.Element)this.getDocumentFactory().createElement(qname);
   }

   public Attr createAttributeNS(String namespaceURI, String qualifiedName) throws DOMException {
      QName qname = this.getDocumentFactory().createQName(qualifiedName, namespaceURI);
      return (Attr)this.getDocumentFactory().createAttribute((Element)null, (QName)qname, (String)null);
   }

   public org.w3c.dom.Element getElementById(String elementId) {
      return DOMNodeHelper.asDOMElement(this.elementByID(elementId));
   }

   protected DocumentFactory getDocumentFactory() {
      return (DocumentFactory)(super.getDocumentFactory() == null ? DOCUMENT_FACTORY : super.getDocumentFactory());
   }
}
