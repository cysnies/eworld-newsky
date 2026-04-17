package org.dom4j.io;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.dom4j.Attribute;
import org.dom4j.Branch;
import org.dom4j.CDATA;
import org.dom4j.CharacterData;
import org.dom4j.Comment;
import org.dom4j.Document;
import org.dom4j.DocumentType;
import org.dom4j.Element;
import org.dom4j.Entity;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.ProcessingInstruction;
import org.dom4j.Text;
import org.dom4j.tree.NamespaceStack;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.LocatorImpl;

public class SAXWriter implements XMLReader {
   protected static final String[] LEXICAL_HANDLER_NAMES = new String[]{"http://xml.org/sax/properties/lexical-handler", "http://xml.org/sax/handlers/LexicalHandler"};
   protected static final String FEATURE_NAMESPACE_PREFIXES = "http://xml.org/sax/features/namespace-prefixes";
   protected static final String FEATURE_NAMESPACES = "http://xml.org/sax/features/namespaces";
   private ContentHandler contentHandler;
   private DTDHandler dtdHandler;
   private EntityResolver entityResolver;
   private ErrorHandler errorHandler;
   private LexicalHandler lexicalHandler;
   private AttributesImpl attributes;
   private Map features;
   private Map properties;
   private boolean declareNamespaceAttributes;

   public SAXWriter() {
      this.attributes = new AttributesImpl();
      this.features = new HashMap();
      this.properties = new HashMap();
      this.properties.put("http://xml.org/sax/features/namespace-prefixes", Boolean.FALSE);
      this.properties.put("http://xml.org/sax/features/namespace-prefixes", Boolean.TRUE);
   }

   public SAXWriter(ContentHandler contentHandler) {
      this();
      this.contentHandler = contentHandler;
   }

   public SAXWriter(ContentHandler contentHandler, LexicalHandler lexicalHandler) {
      this();
      this.contentHandler = contentHandler;
      this.lexicalHandler = lexicalHandler;
   }

   public SAXWriter(ContentHandler contentHandler, LexicalHandler lexicalHandler, EntityResolver entityResolver) {
      this();
      this.contentHandler = contentHandler;
      this.lexicalHandler = lexicalHandler;
      this.entityResolver = entityResolver;
   }

   public void write(Node node) throws SAXException {
      int nodeType = node.getNodeType();
      switch (nodeType) {
         case 1:
            this.write((Element)node);
            break;
         case 2:
            this.write((Node)((Attribute)node));
            break;
         case 3:
            this.write(node.getText());
            break;
         case 4:
            this.write((CDATA)node);
            break;
         case 5:
            this.write((Entity)node);
            break;
         case 6:
         case 11:
         case 12:
         default:
            throw new SAXException("Invalid node type: " + node);
         case 7:
            this.write((ProcessingInstruction)node);
            break;
         case 8:
            this.write((Comment)node);
            break;
         case 9:
            this.write((Document)node);
            break;
         case 10:
            this.write((Node)((DocumentType)node));
         case 13:
      }

   }

   public void write(Document document) throws SAXException {
      if (document != null) {
         this.checkForNullHandlers();
         this.documentLocator(document);
         this.startDocument();
         this.entityResolver(document);
         this.dtdHandler(document);
         this.writeContent(document, new NamespaceStack());
         this.endDocument();
      }

   }

   public void write(Element element) throws SAXException {
      this.write(element, new NamespaceStack());
   }

   public void writeOpen(Element element) throws SAXException {
      this.startElement(element, (AttributesImpl)null);
   }

   public void writeClose(Element element) throws SAXException {
      this.endElement(element);
   }

   public void write(String text) throws SAXException {
      if (text != null) {
         char[] chars = text.toCharArray();
         this.contentHandler.characters(chars, 0, chars.length);
      }

   }

   public void write(CDATA cdata) throws SAXException {
      String text = cdata.getText();
      if (this.lexicalHandler != null) {
         this.lexicalHandler.startCDATA();
         this.write(text);
         this.lexicalHandler.endCDATA();
      } else {
         this.write(text);
      }

   }

   public void write(Comment comment) throws SAXException {
      if (this.lexicalHandler != null) {
         String text = comment.getText();
         char[] chars = text.toCharArray();
         this.lexicalHandler.comment(chars, 0, chars.length);
      }

   }

   public void write(Entity entity) throws SAXException {
      String text = entity.getText();
      if (this.lexicalHandler != null) {
         String name = entity.getName();
         this.lexicalHandler.startEntity(name);
         this.write(text);
         this.lexicalHandler.endEntity(name);
      } else {
         this.write(text);
      }

   }

   public void write(ProcessingInstruction pi) throws SAXException {
      String target = pi.getTarget();
      String text = pi.getText();
      this.contentHandler.processingInstruction(target, text);
   }

   public boolean isDeclareNamespaceAttributes() {
      return this.declareNamespaceAttributes;
   }

   public void setDeclareNamespaceAttributes(boolean declareNamespaceAttrs) {
      this.declareNamespaceAttributes = declareNamespaceAttrs;
   }

   public ContentHandler getContentHandler() {
      return this.contentHandler;
   }

   public void setContentHandler(ContentHandler contentHandler) {
      this.contentHandler = contentHandler;
   }

   public DTDHandler getDTDHandler() {
      return this.dtdHandler;
   }

   public void setDTDHandler(DTDHandler handler) {
      this.dtdHandler = handler;
   }

   public ErrorHandler getErrorHandler() {
      return this.errorHandler;
   }

   public void setErrorHandler(ErrorHandler errorHandler) {
      this.errorHandler = errorHandler;
   }

   public EntityResolver getEntityResolver() {
      return this.entityResolver;
   }

   public void setEntityResolver(EntityResolver entityResolver) {
      this.entityResolver = entityResolver;
   }

   public LexicalHandler getLexicalHandler() {
      return this.lexicalHandler;
   }

   public void setLexicalHandler(LexicalHandler lexicalHandler) {
      this.lexicalHandler = lexicalHandler;
   }

   public void setXMLReader(XMLReader xmlReader) {
      this.setContentHandler(xmlReader.getContentHandler());
      this.setDTDHandler(xmlReader.getDTDHandler());
      this.setEntityResolver(xmlReader.getEntityResolver());
      this.setErrorHandler(xmlReader.getErrorHandler());
   }

   public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
      Boolean answer = (Boolean)this.features.get(name);
      return answer != null && answer;
   }

   public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
      if ("http://xml.org/sax/features/namespace-prefixes".equals(name)) {
         this.setDeclareNamespaceAttributes(value);
      } else if ("http://xml.org/sax/features/namespace-prefixes".equals(name) && !value) {
         String msg = "Namespace feature is always supported in dom4j";
         throw new SAXNotSupportedException(msg);
      }

      this.features.put(name, value ? Boolean.TRUE : Boolean.FALSE);
   }

   public void setProperty(String name, Object value) {
      for(int i = 0; i < LEXICAL_HANDLER_NAMES.length; ++i) {
         if (LEXICAL_HANDLER_NAMES[i].equals(name)) {
            this.setLexicalHandler((LexicalHandler)value);
            return;
         }
      }

      this.properties.put(name, value);
   }

   public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
      for(int i = 0; i < LEXICAL_HANDLER_NAMES.length; ++i) {
         if (LEXICAL_HANDLER_NAMES[i].equals(name)) {
            return this.getLexicalHandler();
         }
      }

      return this.properties.get(name);
   }

   public void parse(String systemId) throws SAXNotSupportedException {
      throw new SAXNotSupportedException("This XMLReader can only accept <dom4j> InputSource objects");
   }

   public void parse(InputSource input) throws SAXException {
      if (input instanceof DocumentInputSource) {
         DocumentInputSource documentInput = (DocumentInputSource)input;
         Document document = documentInput.getDocument();
         this.write(document);
      } else {
         throw new SAXNotSupportedException("This XMLReader can only accept <dom4j> InputSource objects");
      }
   }

   protected void writeContent(Branch branch, NamespaceStack namespaceStack) throws SAXException {
      Iterator iter = branch.nodeIterator();

      while(iter.hasNext()) {
         Object object = iter.next();
         if (object instanceof Element) {
            this.write((Element)object, namespaceStack);
         } else if (object instanceof CharacterData) {
            if (object instanceof Text) {
               Text text = (Text)object;
               this.write(text.getText());
            } else if (object instanceof CDATA) {
               this.write((CDATA)object);
            } else {
               if (!(object instanceof Comment)) {
                  throw new SAXException("Invalid Node in DOM4J content: " + object + " of type: " + object.getClass());
               }

               this.write((Comment)object);
            }
         } else if (object instanceof String) {
            this.write((String)object);
         } else if (object instanceof Entity) {
            this.write((Entity)object);
         } else if (object instanceof ProcessingInstruction) {
            this.write((ProcessingInstruction)object);
         } else {
            if (!(object instanceof Namespace)) {
               throw new SAXException("Invalid Node in DOM4J content: " + object);
            }

            this.write((Node)((Namespace)object));
         }
      }

   }

   protected void documentLocator(Document document) throws SAXException {
      LocatorImpl locator = new LocatorImpl();
      String publicID = null;
      String systemID = null;
      DocumentType docType = document.getDocType();
      if (docType != null) {
         publicID = docType.getPublicID();
         systemID = docType.getSystemID();
      }

      if (publicID != null) {
         locator.setPublicId(publicID);
      }

      if (systemID != null) {
         locator.setSystemId(systemID);
      }

      locator.setLineNumber(-1);
      locator.setColumnNumber(-1);
      this.contentHandler.setDocumentLocator(locator);
   }

   protected void entityResolver(Document document) throws SAXException {
      if (this.entityResolver != null) {
         DocumentType docType = document.getDocType();
         if (docType != null) {
            String publicID = docType.getPublicID();
            String systemID = docType.getSystemID();
            if (publicID != null || systemID != null) {
               try {
                  this.entityResolver.resolveEntity(publicID, systemID);
               } catch (IOException e) {
                  throw new SAXException("Could not resolve publicID: " + publicID + " systemID: " + systemID, e);
               }
            }
         }
      }

   }

   protected void dtdHandler(Document document) throws SAXException {
   }

   protected void startDocument() throws SAXException {
      this.contentHandler.startDocument();
   }

   protected void endDocument() throws SAXException {
      this.contentHandler.endDocument();
   }

   protected void write(Element element, NamespaceStack namespaceStack) throws SAXException {
      int stackSize = namespaceStack.size();
      AttributesImpl namespaceAttributes = this.startPrefixMapping(element, namespaceStack);
      this.startElement(element, namespaceAttributes);
      this.writeContent(element, namespaceStack);
      this.endElement(element);
      this.endPrefixMapping(namespaceStack, stackSize);
   }

   protected AttributesImpl startPrefixMapping(Element element, NamespaceStack namespaceStack) throws SAXException {
      AttributesImpl namespaceAttributes = null;
      Namespace elementNamespace = element.getNamespace();
      if (elementNamespace != null && !this.isIgnoreableNamespace(elementNamespace, namespaceStack)) {
         namespaceStack.push(elementNamespace);
         this.contentHandler.startPrefixMapping(elementNamespace.getPrefix(), elementNamespace.getURI());
         namespaceAttributes = this.addNamespaceAttribute(namespaceAttributes, elementNamespace);
      }

      List declaredNamespaces = element.declaredNamespaces();
      int i = 0;

      for(int size = declaredNamespaces.size(); i < size; ++i) {
         Namespace namespace = (Namespace)declaredNamespaces.get(i);
         if (!this.isIgnoreableNamespace(namespace, namespaceStack)) {
            namespaceStack.push(namespace);
            this.contentHandler.startPrefixMapping(namespace.getPrefix(), namespace.getURI());
            namespaceAttributes = this.addNamespaceAttribute(namespaceAttributes, namespace);
         }
      }

      return namespaceAttributes;
   }

   protected void endPrefixMapping(NamespaceStack stack, int stackSize) throws SAXException {
      while(stack.size() > stackSize) {
         Namespace namespace = stack.pop();
         if (namespace != null) {
            this.contentHandler.endPrefixMapping(namespace.getPrefix());
         }
      }

   }

   protected void startElement(Element element, AttributesImpl namespaceAttributes) throws SAXException {
      this.contentHandler.startElement(element.getNamespaceURI(), element.getName(), element.getQualifiedName(), this.createAttributes(element, namespaceAttributes));
   }

   protected void endElement(Element element) throws SAXException {
      this.contentHandler.endElement(element.getNamespaceURI(), element.getName(), element.getQualifiedName());
   }

   protected Attributes createAttributes(Element element, Attributes namespaceAttributes) throws SAXException {
      this.attributes.clear();
      if (namespaceAttributes != null) {
         this.attributes.setAttributes(namespaceAttributes);
      }

      Iterator iter = element.attributeIterator();

      while(iter.hasNext()) {
         Attribute attribute = (Attribute)iter.next();
         this.attributes.addAttribute(attribute.getNamespaceURI(), attribute.getName(), attribute.getQualifiedName(), "CDATA", attribute.getValue());
      }

      return this.attributes;
   }

   protected AttributesImpl addNamespaceAttribute(AttributesImpl attrs, Namespace namespace) {
      if (this.declareNamespaceAttributes) {
         if (attrs == null) {
            attrs = new AttributesImpl();
         }

         String prefix = namespace.getPrefix();
         String qualifiedName = "xmlns";
         if (prefix != null && prefix.length() > 0) {
            qualifiedName = "xmlns:" + prefix;
         }

         String uri = "";
         String type = "CDATA";
         String value = namespace.getURI();
         attrs.addAttribute(uri, prefix, qualifiedName, type, value);
      }

      return attrs;
   }

   protected boolean isIgnoreableNamespace(Namespace namespace, NamespaceStack namespaceStack) {
      if (!namespace.equals(Namespace.NO_NAMESPACE) && !namespace.equals(Namespace.XML_NAMESPACE)) {
         String uri = namespace.getURI();
         return uri != null && uri.length() > 0 ? namespaceStack.contains(namespace) : true;
      } else {
         return true;
      }
   }

   protected void checkForNullHandlers() {
   }
}
