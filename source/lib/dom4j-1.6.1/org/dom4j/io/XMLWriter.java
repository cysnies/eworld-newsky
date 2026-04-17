package org.dom4j.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.dom4j.Attribute;
import org.dom4j.CDATA;
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
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.XMLFilterImpl;

public class XMLWriter extends XMLFilterImpl implements LexicalHandler {
   private static final String PAD_TEXT = " ";
   protected static final String[] LEXICAL_HANDLER_NAMES = new String[]{"http://xml.org/sax/properties/lexical-handler", "http://xml.org/sax/handlers/LexicalHandler"};
   protected static final OutputFormat DEFAULT_FORMAT = new OutputFormat();
   private boolean resolveEntityRefs;
   protected int lastOutputNodeType;
   private boolean lastElementClosed;
   protected boolean preserve;
   protected Writer writer;
   private NamespaceStack namespaceStack;
   private OutputFormat format;
   private boolean escapeText;
   private int indentLevel;
   private StringBuffer buffer;
   private boolean charsAdded;
   private char lastChar;
   private boolean autoFlush;
   private LexicalHandler lexicalHandler;
   private boolean showCommentsInDTDs;
   private boolean inDTD;
   private Map namespacesMap;
   private int maximumAllowedCharacter;

   public XMLWriter(Writer writer) {
      this(writer, DEFAULT_FORMAT);
   }

   public XMLWriter(Writer writer, OutputFormat format) {
      this.resolveEntityRefs = true;
      this.lastElementClosed = false;
      this.preserve = false;
      this.namespaceStack = new NamespaceStack();
      this.escapeText = true;
      this.indentLevel = 0;
      this.buffer = new StringBuffer();
      this.charsAdded = false;
      this.writer = writer;
      this.format = format;
      this.namespaceStack.push(Namespace.NO_NAMESPACE);
   }

   public XMLWriter() {
      this.resolveEntityRefs = true;
      this.lastElementClosed = false;
      this.preserve = false;
      this.namespaceStack = new NamespaceStack();
      this.escapeText = true;
      this.indentLevel = 0;
      this.buffer = new StringBuffer();
      this.charsAdded = false;
      this.format = DEFAULT_FORMAT;
      this.writer = new BufferedWriter(new OutputStreamWriter(System.out));
      this.autoFlush = true;
      this.namespaceStack.push(Namespace.NO_NAMESPACE);
   }

   public XMLWriter(OutputStream out) throws UnsupportedEncodingException {
      this.resolveEntityRefs = true;
      this.lastElementClosed = false;
      this.preserve = false;
      this.namespaceStack = new NamespaceStack();
      this.escapeText = true;
      this.indentLevel = 0;
      this.buffer = new StringBuffer();
      this.charsAdded = false;
      this.format = DEFAULT_FORMAT;
      this.writer = this.createWriter(out, this.format.getEncoding());
      this.autoFlush = true;
      this.namespaceStack.push(Namespace.NO_NAMESPACE);
   }

   public XMLWriter(OutputStream out, OutputFormat format) throws UnsupportedEncodingException {
      this.resolveEntityRefs = true;
      this.lastElementClosed = false;
      this.preserve = false;
      this.namespaceStack = new NamespaceStack();
      this.escapeText = true;
      this.indentLevel = 0;
      this.buffer = new StringBuffer();
      this.charsAdded = false;
      this.format = format;
      this.writer = this.createWriter(out, format.getEncoding());
      this.autoFlush = true;
      this.namespaceStack.push(Namespace.NO_NAMESPACE);
   }

   public XMLWriter(OutputFormat format) throws UnsupportedEncodingException {
      this.resolveEntityRefs = true;
      this.lastElementClosed = false;
      this.preserve = false;
      this.namespaceStack = new NamespaceStack();
      this.escapeText = true;
      this.indentLevel = 0;
      this.buffer = new StringBuffer();
      this.charsAdded = false;
      this.format = format;
      this.writer = this.createWriter(System.out, format.getEncoding());
      this.autoFlush = true;
      this.namespaceStack.push(Namespace.NO_NAMESPACE);
   }

   public void setWriter(Writer writer) {
      this.writer = writer;
      this.autoFlush = false;
   }

   public void setOutputStream(OutputStream out) throws UnsupportedEncodingException {
      this.writer = this.createWriter(out, this.format.getEncoding());
      this.autoFlush = true;
   }

   public boolean isEscapeText() {
      return this.escapeText;
   }

   public void setEscapeText(boolean escapeText) {
      this.escapeText = escapeText;
   }

   public void setIndentLevel(int indentLevel) {
      this.indentLevel = indentLevel;
   }

   public int getMaximumAllowedCharacter() {
      if (this.maximumAllowedCharacter == 0) {
         this.maximumAllowedCharacter = this.defaultMaximumAllowedCharacter();
      }

      return this.maximumAllowedCharacter;
   }

   public void setMaximumAllowedCharacter(int maximumAllowedCharacter) {
      this.maximumAllowedCharacter = maximumAllowedCharacter;
   }

   public void flush() throws IOException {
      this.writer.flush();
   }

   public void close() throws IOException {
      this.writer.close();
   }

   public void println() throws IOException {
      this.writer.write(this.format.getLineSeparator());
   }

   public void write(Attribute attribute) throws IOException {
      this.writeAttribute(attribute);
      if (this.autoFlush) {
         this.flush();
      }

   }

   public void write(Document doc) throws IOException {
      this.writeDeclaration();
      if (doc.getDocType() != null) {
         this.indent();
         this.writeDocType(doc.getDocType());
      }

      int i = 0;

      for(int size = doc.nodeCount(); i < size; ++i) {
         Node node = doc.node(i);
         this.writeNode(node);
      }

      this.writePrintln();
      if (this.autoFlush) {
         this.flush();
      }

   }

   public void write(Element element) throws IOException {
      this.writeElement(element);
      if (this.autoFlush) {
         this.flush();
      }

   }

   public void write(CDATA cdata) throws IOException {
      this.writeCDATA(cdata.getText());
      if (this.autoFlush) {
         this.flush();
      }

   }

   public void write(Comment comment) throws IOException {
      this.writeComment(comment.getText());
      if (this.autoFlush) {
         this.flush();
      }

   }

   public void write(DocumentType docType) throws IOException {
      this.writeDocType(docType);
      if (this.autoFlush) {
         this.flush();
      }

   }

   public void write(Entity entity) throws IOException {
      this.writeEntity(entity);
      if (this.autoFlush) {
         this.flush();
      }

   }

   public void write(Namespace namespace) throws IOException {
      this.writeNamespace(namespace);
      if (this.autoFlush) {
         this.flush();
      }

   }

   public void write(ProcessingInstruction processingInstruction) throws IOException {
      this.writeProcessingInstruction(processingInstruction);
      if (this.autoFlush) {
         this.flush();
      }

   }

   public void write(String text) throws IOException {
      this.writeString(text);
      if (this.autoFlush) {
         this.flush();
      }

   }

   public void write(Text text) throws IOException {
      this.writeString(text.getText());
      if (this.autoFlush) {
         this.flush();
      }

   }

   public void write(Node node) throws IOException {
      this.writeNode(node);
      if (this.autoFlush) {
         this.flush();
      }

   }

   public void write(Object object) throws IOException {
      if (object instanceof Node) {
         this.write((Node)object);
      } else if (object instanceof String) {
         this.write((String)object);
      } else if (object instanceof List) {
         List list = (List)object;
         int i = 0;

         for(int size = list.size(); i < size; ++i) {
            this.write(list.get(i));
         }
      } else if (object != null) {
         throw new IOException("Invalid object: " + object);
      }

   }

   public void writeOpen(Element element) throws IOException {
      this.writer.write("<");
      this.writer.write(element.getQualifiedName());
      this.writeAttributes(element);
      this.writer.write(">");
   }

   public void writeClose(Element element) throws IOException {
      this.writeClose(element.getQualifiedName());
   }

   public void parse(InputSource source) throws IOException, SAXException {
      this.installLexicalHandler();
      super.parse(source);
   }

   public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
      for(int i = 0; i < LEXICAL_HANDLER_NAMES.length; ++i) {
         if (LEXICAL_HANDLER_NAMES[i].equals(name)) {
            this.setLexicalHandler((LexicalHandler)value);
            return;
         }
      }

      super.setProperty(name, value);
   }

   public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
      for(int i = 0; i < LEXICAL_HANDLER_NAMES.length; ++i) {
         if (LEXICAL_HANDLER_NAMES[i].equals(name)) {
            return this.getLexicalHandler();
         }
      }

      return super.getProperty(name);
   }

   public void setLexicalHandler(LexicalHandler handler) {
      if (handler == null) {
         throw new NullPointerException("Null lexical handler");
      } else {
         this.lexicalHandler = handler;
      }
   }

   public LexicalHandler getLexicalHandler() {
      return this.lexicalHandler;
   }

   public void setDocumentLocator(Locator locator) {
      super.setDocumentLocator(locator);
   }

   public void startDocument() throws SAXException {
      try {
         this.writeDeclaration();
         super.startDocument();
      } catch (IOException e) {
         this.handleException(e);
      }

   }

   public void endDocument() throws SAXException {
      super.endDocument();
      if (this.autoFlush) {
         try {
            this.flush();
         } catch (IOException var2) {
         }
      }

   }

   public void startPrefixMapping(String prefix, String uri) throws SAXException {
      if (this.namespacesMap == null) {
         this.namespacesMap = new HashMap();
      }

      this.namespacesMap.put(prefix, uri);
      super.startPrefixMapping(prefix, uri);
   }

   public void endPrefixMapping(String prefix) throws SAXException {
      super.endPrefixMapping(prefix);
   }

   public void startElement(String namespaceURI, String localName, String qName, Attributes attributes) throws SAXException {
      try {
         this.charsAdded = false;
         this.writePrintln();
         this.indent();
         this.writer.write("<");
         this.writer.write(qName);
         this.writeNamespaces();
         this.writeAttributes(attributes);
         this.writer.write(">");
         ++this.indentLevel;
         this.lastOutputNodeType = 1;
         this.lastElementClosed = false;
         super.startElement(namespaceURI, localName, qName, attributes);
      } catch (IOException e) {
         this.handleException(e);
      }

   }

   public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
      try {
         this.charsAdded = false;
         --this.indentLevel;
         if (this.lastElementClosed) {
            this.writePrintln();
            this.indent();
         }

         boolean hadContent = true;
         if (hadContent) {
            this.writeClose(qName);
         } else {
            this.writeEmptyElementClose(qName);
         }

         this.lastOutputNodeType = 1;
         this.lastElementClosed = true;
         super.endElement(namespaceURI, localName, qName);
      } catch (IOException e) {
         this.handleException(e);
      }

   }

   public void characters(char[] ch, int start, int length) throws SAXException {
      if (ch != null && ch.length != 0 && length > 0) {
         try {
            String string = String.valueOf(ch, start, length);
            if (this.escapeText) {
               string = this.escapeElementEntities(string);
            }

            if (!this.format.isTrimText()) {
               this.writer.write(string);
            } else {
               if (this.lastOutputNodeType == 3 && !this.charsAdded) {
                  this.writer.write(32);
               } else if (this.charsAdded && Character.isWhitespace(this.lastChar)) {
                  this.writer.write(32);
               } else if (this.lastOutputNodeType == 1 && this.format.isPadText() && this.lastElementClosed && Character.isWhitespace(ch[0])) {
                  this.writer.write(" ");
               }

               String delim = "";

               for(StringTokenizer tokens = new StringTokenizer(string); tokens.hasMoreTokens(); delim = " ") {
                  this.writer.write(delim);
                  this.writer.write(tokens.nextToken());
               }
            }

            this.charsAdded = true;
            this.lastChar = ch[start + length - 1];
            this.lastOutputNodeType = 3;
            super.characters(ch, start, length);
         } catch (IOException e) {
            this.handleException(e);
         }

      }
   }

   public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
      super.ignorableWhitespace(ch, start, length);
   }

   public void processingInstruction(String target, String data) throws SAXException {
      try {
         this.indent();
         this.writer.write("<?");
         this.writer.write(target);
         this.writer.write(" ");
         this.writer.write(data);
         this.writer.write("?>");
         this.writePrintln();
         this.lastOutputNodeType = 7;
         super.processingInstruction(target, data);
      } catch (IOException e) {
         this.handleException(e);
      }

   }

   public void notationDecl(String name, String publicID, String systemID) throws SAXException {
      super.notationDecl(name, publicID, systemID);
   }

   public void unparsedEntityDecl(String name, String publicID, String systemID, String notationName) throws SAXException {
      super.unparsedEntityDecl(name, publicID, systemID, notationName);
   }

   public void startDTD(String name, String publicID, String systemID) throws SAXException {
      this.inDTD = true;

      try {
         this.writeDocType(name, publicID, systemID);
      } catch (IOException e) {
         this.handleException(e);
      }

      if (this.lexicalHandler != null) {
         this.lexicalHandler.startDTD(name, publicID, systemID);
      }

   }

   public void endDTD() throws SAXException {
      this.inDTD = false;
      if (this.lexicalHandler != null) {
         this.lexicalHandler.endDTD();
      }

   }

   public void startCDATA() throws SAXException {
      try {
         this.writer.write("<![CDATA[");
      } catch (IOException e) {
         this.handleException(e);
      }

      if (this.lexicalHandler != null) {
         this.lexicalHandler.startCDATA();
      }

   }

   public void endCDATA() throws SAXException {
      try {
         this.writer.write("]]>");
      } catch (IOException e) {
         this.handleException(e);
      }

      if (this.lexicalHandler != null) {
         this.lexicalHandler.endCDATA();
      }

   }

   public void startEntity(String name) throws SAXException {
      try {
         this.writeEntityRef(name);
      } catch (IOException e) {
         this.handleException(e);
      }

      if (this.lexicalHandler != null) {
         this.lexicalHandler.startEntity(name);
      }

   }

   public void endEntity(String name) throws SAXException {
      if (this.lexicalHandler != null) {
         this.lexicalHandler.endEntity(name);
      }

   }

   public void comment(char[] ch, int start, int length) throws SAXException {
      if (this.showCommentsInDTDs || !this.inDTD) {
         try {
            this.charsAdded = false;
            this.writeComment(new String(ch, start, length));
         } catch (IOException e) {
            this.handleException(e);
         }
      }

      if (this.lexicalHandler != null) {
         this.lexicalHandler.comment(ch, start, length);
      }

   }

   protected void writeElement(Element element) throws IOException {
      int size = element.nodeCount();
      String qualifiedName = element.getQualifiedName();
      this.writePrintln();
      this.indent();
      this.writer.write("<");
      this.writer.write(qualifiedName);
      int previouslyDeclaredNamespaces = this.namespaceStack.size();
      Namespace ns = element.getNamespace();
      if (this.isNamespaceDeclaration(ns)) {
         this.namespaceStack.push(ns);
         this.writeNamespace(ns);
      }

      boolean textOnly = true;

      for(int i = 0; i < size; ++i) {
         Node node = element.node(i);
         if (node instanceof Namespace) {
            Namespace additional = (Namespace)node;
            if (this.isNamespaceDeclaration(additional)) {
               this.namespaceStack.push(additional);
               this.writeNamespace(additional);
            }
         } else if (node instanceof Element) {
            textOnly = false;
         } else if (node instanceof Comment) {
            textOnly = false;
         }
      }

      this.writeAttributes(element);
      this.lastOutputNodeType = 1;
      if (size <= 0) {
         this.writeEmptyElementClose(qualifiedName);
      } else {
         this.writer.write(">");
         if (textOnly) {
            this.writeElementContent(element);
         } else {
            ++this.indentLevel;
            this.writeElementContent(element);
            --this.indentLevel;
            this.writePrintln();
            this.indent();
         }

         this.writer.write("</");
         this.writer.write(qualifiedName);
         this.writer.write(">");
      }

      while(this.namespaceStack.size() > previouslyDeclaredNamespaces) {
         this.namespaceStack.pop();
      }

      this.lastOutputNodeType = 1;
   }

   protected final boolean isElementSpacePreserved(Element element) {
      Attribute attr = element.attribute("space");
      boolean preserveFound = this.preserve;
      if (attr != null) {
         if ("xml".equals(attr.getNamespacePrefix()) && "preserve".equals(attr.getText())) {
            preserveFound = true;
         } else {
            preserveFound = false;
         }
      }

      return preserveFound;
   }

   protected void writeElementContent(Element element) throws IOException {
      boolean trim = this.format.isTrimText();
      boolean oldPreserve = this.preserve;
      if (trim) {
         this.preserve = this.isElementSpacePreserved(element);
         trim = !this.preserve;
      }

      if (trim) {
         Text lastTextNode = null;
         StringBuffer buff = null;
         boolean textOnly = true;
         int i = 0;

         for(int size = element.nodeCount(); i < size; ++i) {
            Node node = element.node(i);
            if (node instanceof Text) {
               if (lastTextNode == null) {
                  lastTextNode = (Text)node;
               } else {
                  if (buff == null) {
                     buff = new StringBuffer(lastTextNode.getText());
                  }

                  buff.append(((Text)node).getText());
               }
            } else {
               if (!textOnly && this.format.isPadText()) {
                  char firstChar = 'a';
                  if (buff != null) {
                     firstChar = buff.charAt(0);
                  } else if (lastTextNode != null) {
                     firstChar = lastTextNode.getText().charAt(0);
                  }

                  if (Character.isWhitespace(firstChar)) {
                     this.writer.write(" ");
                  }
               }

               if (lastTextNode != null) {
                  if (buff != null) {
                     this.writeString(buff.toString());
                     buff = null;
                  } else {
                     this.writeString(lastTextNode.getText());
                  }

                  if (this.format.isPadText()) {
                     char lastTextChar = 'a';
                     if (buff != null) {
                        lastTextChar = buff.charAt(buff.length() - 1);
                     } else if (lastTextNode != null) {
                        String txt = lastTextNode.getText();
                        lastTextChar = txt.charAt(txt.length() - 1);
                     }

                     if (Character.isWhitespace(lastTextChar)) {
                        this.writer.write(" ");
                     }
                  }

                  lastTextNode = null;
               }

               textOnly = false;
               this.writeNode(node);
            }
         }

         if (lastTextNode != null) {
            if (!textOnly && this.format.isPadText()) {
               i = 97;
               if (buff != null) {
                  i = buff.charAt(0);
               } else {
                  i = lastTextNode.getText().charAt(0);
               }

               if (Character.isWhitespace((char)i)) {
                  this.writer.write(" ");
               }
            }

            if (buff != null) {
               this.writeString(buff.toString());
               StringBuffer var14 = null;
            } else {
               this.writeString(lastTextNode.getText());
            }

            Object var12 = null;
         }
      } else {
         Node lastTextNode = null;
         int i = 0;

         for(int size = element.nodeCount(); i < size; ++i) {
            Node node = element.node(i);
            if (node instanceof Text) {
               this.writeNode(node);
               lastTextNode = node;
            } else {
               if (lastTextNode != null && this.format.isPadText()) {
                  String txt = lastTextNode.getText();
                  char lastTextChar = txt.charAt(txt.length() - 1);
                  if (Character.isWhitespace(lastTextChar)) {
                     this.writer.write(" ");
                  }
               }

               this.writeNode(node);
               lastTextNode = null;
            }
         }
      }

      this.preserve = oldPreserve;
   }

   protected void writeCDATA(String text) throws IOException {
      this.writer.write("<![CDATA[");
      if (text != null) {
         this.writer.write(text);
      }

      this.writer.write("]]>");
      this.lastOutputNodeType = 4;
   }

   protected void writeDocType(DocumentType docType) throws IOException {
      if (docType != null) {
         docType.write(this.writer);
         this.writePrintln();
      }

   }

   protected void writeNamespace(Namespace namespace) throws IOException {
      if (namespace != null) {
         this.writeNamespace(namespace.getPrefix(), namespace.getURI());
      }

   }

   protected void writeNamespaces() throws IOException {
      if (this.namespacesMap != null) {
         for(Map.Entry entry : this.namespacesMap.entrySet()) {
            String prefix = (String)entry.getKey();
            String uri = (String)entry.getValue();
            this.writeNamespace(prefix, uri);
         }

         this.namespacesMap = null;
      }

   }

   protected void writeNamespace(String prefix, String uri) throws IOException {
      if (prefix != null && prefix.length() > 0) {
         this.writer.write(" xmlns:");
         this.writer.write(prefix);
         this.writer.write("=\"");
      } else {
         this.writer.write(" xmlns=\"");
      }

      this.writer.write(uri);
      this.writer.write("\"");
   }

   protected void writeProcessingInstruction(ProcessingInstruction pi) throws IOException {
      this.writer.write("<?");
      this.writer.write(pi.getName());
      this.writer.write(" ");
      this.writer.write(pi.getText());
      this.writer.write("?>");
      this.writePrintln();
      this.lastOutputNodeType = 7;
   }

   protected void writeString(String text) throws IOException {
      if (text != null && text.length() > 0) {
         if (this.escapeText) {
            text = this.escapeElementEntities(text);
         }

         if (this.format.isTrimText()) {
            boolean first = true;

            String token;
            for(StringTokenizer tokenizer = new StringTokenizer(text); tokenizer.hasMoreTokens(); this.lastChar = token.charAt(token.length() - 1)) {
               token = tokenizer.nextToken();
               if (first) {
                  first = false;
                  if (this.lastOutputNodeType == 3) {
                     this.writer.write(" ");
                  }
               } else {
                  this.writer.write(" ");
               }

               this.writer.write(token);
               this.lastOutputNodeType = 3;
            }
         } else {
            this.lastOutputNodeType = 3;
            this.writer.write(text);
            this.lastChar = text.charAt(text.length() - 1);
         }
      }

   }

   protected void writeNodeText(Node node) throws IOException {
      String text = node.getText();
      if (text != null && text.length() > 0) {
         if (this.escapeText) {
            text = this.escapeElementEntities(text);
         }

         this.lastOutputNodeType = 3;
         this.writer.write(text);
         this.lastChar = text.charAt(text.length() - 1);
      }

   }

   protected void writeNode(Node node) throws IOException {
      int nodeType = node.getNodeType();
      switch (nodeType) {
         case 1:
            this.writeElement((Element)node);
            break;
         case 2:
            this.writeAttribute((Attribute)node);
            break;
         case 3:
            this.writeNodeText(node);
            break;
         case 4:
            this.writeCDATA(node.getText());
            break;
         case 5:
            this.writeEntity((Entity)node);
            break;
         case 6:
         case 11:
         case 12:
         default:
            throw new IOException("Invalid node type: " + node);
         case 7:
            this.writeProcessingInstruction((ProcessingInstruction)node);
            break;
         case 8:
            this.writeComment(node.getText());
            break;
         case 9:
            this.write((Document)node);
            break;
         case 10:
            this.writeDocType((DocumentType)node);
         case 13:
      }

   }

   protected void installLexicalHandler() {
      XMLReader parent = this.getParent();
      if (parent == null) {
         throw new NullPointerException("No parent for filter");
      } else {
         for(int i = 0; i < LEXICAL_HANDLER_NAMES.length; ++i) {
            try {
               parent.setProperty(LEXICAL_HANDLER_NAMES[i], this);
               break;
            } catch (SAXNotRecognizedException var4) {
            } catch (SAXNotSupportedException var5) {
            }
         }

      }
   }

   protected void writeDocType(String name, String publicID, String systemID) throws IOException {
      boolean hasPublic = false;
      this.writer.write("<!DOCTYPE ");
      this.writer.write(name);
      if (publicID != null && !publicID.equals("")) {
         this.writer.write(" PUBLIC \"");
         this.writer.write(publicID);
         this.writer.write("\"");
         hasPublic = true;
      }

      if (systemID != null && !systemID.equals("")) {
         if (!hasPublic) {
            this.writer.write(" SYSTEM");
         }

         this.writer.write(" \"");
         this.writer.write(systemID);
         this.writer.write("\"");
      }

      this.writer.write(">");
      this.writePrintln();
   }

   protected void writeEntity(Entity entity) throws IOException {
      if (!this.resolveEntityRefs()) {
         this.writeEntityRef(entity.getName());
      } else {
         this.writer.write(entity.getText());
      }

   }

   protected void writeEntityRef(String name) throws IOException {
      this.writer.write("&");
      this.writer.write(name);
      this.writer.write(";");
      this.lastOutputNodeType = 5;
   }

   protected void writeComment(String text) throws IOException {
      if (this.format.isNewlines()) {
         this.println();
         this.indent();
      }

      this.writer.write("<!--");
      this.writer.write(text);
      this.writer.write("-->");
      this.lastOutputNodeType = 8;
   }

   protected void writeAttributes(Element element) throws IOException {
      int i = 0;

      for(int size = element.attributeCount(); i < size; ++i) {
         Attribute attribute = element.attribute(i);
         Namespace ns = attribute.getNamespace();
         if (ns != null && ns != Namespace.NO_NAMESPACE && ns != Namespace.XML_NAMESPACE) {
            String prefix = ns.getPrefix();
            String uri = this.namespaceStack.getURI(prefix);
            if (!ns.getURI().equals(uri)) {
               this.writeNamespace(ns);
               this.namespaceStack.push(ns);
            }
         }

         String attName = attribute.getName();
         if (attName.startsWith("xmlns:")) {
            String prefix = attName.substring(6);
            if (this.namespaceStack.getNamespaceForPrefix(prefix) == null) {
               String uri = attribute.getValue();
               this.namespaceStack.push(prefix, uri);
               this.writeNamespace(prefix, uri);
            }
         } else if (attName.equals("xmlns")) {
            if (this.namespaceStack.getDefaultNamespace() == null) {
               String uri = attribute.getValue();
               this.namespaceStack.push((String)null, uri);
               this.writeNamespace((String)null, uri);
            }
         } else {
            char quote = this.format.getAttributeQuoteCharacter();
            this.writer.write(" ");
            this.writer.write(attribute.getQualifiedName());
            this.writer.write("=");
            this.writer.write(quote);
            this.writeEscapeAttributeEntities(attribute.getValue());
            this.writer.write(quote);
         }
      }

   }

   protected void writeAttribute(Attribute attribute) throws IOException {
      this.writer.write(" ");
      this.writer.write(attribute.getQualifiedName());
      this.writer.write("=");
      char quote = this.format.getAttributeQuoteCharacter();
      this.writer.write(quote);
      this.writeEscapeAttributeEntities(attribute.getValue());
      this.writer.write(quote);
      this.lastOutputNodeType = 2;
   }

   protected void writeAttributes(Attributes attributes) throws IOException {
      int i = 0;

      for(int size = attributes.getLength(); i < size; ++i) {
         this.writeAttribute(attributes, i);
      }

   }

   protected void writeAttribute(Attributes attributes, int index) throws IOException {
      char quote = this.format.getAttributeQuoteCharacter();
      this.writer.write(" ");
      this.writer.write(attributes.getQName(index));
      this.writer.write("=");
      this.writer.write(quote);
      this.writeEscapeAttributeEntities(attributes.getValue(index));
      this.writer.write(quote);
   }

   protected void indent() throws IOException {
      String indent = this.format.getIndent();
      if (indent != null && indent.length() > 0) {
         for(int i = 0; i < this.indentLevel; ++i) {
            this.writer.write(indent);
         }
      }

   }

   protected void writePrintln() throws IOException {
      if (this.format.isNewlines()) {
         String seperator = this.format.getLineSeparator();
         if (this.lastChar != seperator.charAt(seperator.length() - 1)) {
            this.writer.write(this.format.getLineSeparator());
         }
      }

   }

   protected Writer createWriter(OutputStream outStream, String encoding) throws UnsupportedEncodingException {
      return new BufferedWriter(new OutputStreamWriter(outStream, encoding));
   }

   protected void writeDeclaration() throws IOException {
      String encoding = this.format.getEncoding();
      if (!this.format.isSuppressDeclaration()) {
         if (encoding.equals("UTF8")) {
            this.writer.write("<?xml version=\"1.0\"");
            if (!this.format.isOmitEncoding()) {
               this.writer.write(" encoding=\"UTF-8\"");
            }

            this.writer.write("?>");
         } else {
            this.writer.write("<?xml version=\"1.0\"");
            if (!this.format.isOmitEncoding()) {
               this.writer.write(" encoding=\"" + encoding + "\"");
            }

            this.writer.write("?>");
         }

         if (this.format.isNewLineAfterDeclaration()) {
            this.println();
         }
      }

   }

   protected void writeClose(String qualifiedName) throws IOException {
      this.writer.write("</");
      this.writer.write(qualifiedName);
      this.writer.write(">");
   }

   protected void writeEmptyElementClose(String qualifiedName) throws IOException {
      if (!this.format.isExpandEmptyElements()) {
         this.writer.write("/>");
      } else {
         this.writer.write("></");
         this.writer.write(qualifiedName);
         this.writer.write(">");
      }

   }

   protected boolean isExpandEmptyElements() {
      return this.format.isExpandEmptyElements();
   }

   protected String escapeElementEntities(String text) {
      char[] block = null;
      int last = 0;
      int size = text.length();

      int i;
      for(i = 0; i < size; ++i) {
         String entity = null;
         char c = text.charAt(i);
         switch (c) {
            case '\t':
            case '\n':
            case '\r':
               if (this.preserve) {
                  entity = String.valueOf(c);
               }
               break;
            case '&':
               entity = "&amp;";
               break;
            case '<':
               entity = "&lt;";
               break;
            case '>':
               entity = "&gt;";
               break;
            default:
               if (c < ' ' || this.shouldEncodeChar(c)) {
                  entity = "&#" + c + ";";
               }
         }

         if (entity != null) {
            if (block == null) {
               block = text.toCharArray();
            }

            this.buffer.append(block, last, i - last);
            this.buffer.append(entity);
            last = i + 1;
         }
      }

      if (last == 0) {
         return text;
      } else {
         if (last < size) {
            if (block == null) {
               block = text.toCharArray();
            }

            this.buffer.append(block, last, i - last);
         }

         String answer = this.buffer.toString();
         this.buffer.setLength(0);
         return answer;
      }
   }

   protected void writeEscapeAttributeEntities(String txt) throws IOException {
      if (txt != null) {
         String escapedText = this.escapeAttributeEntities(txt);
         this.writer.write(escapedText);
      }

   }

   protected String escapeAttributeEntities(String text) {
      char quote = this.format.getAttributeQuoteCharacter();
      char[] block = null;
      int last = 0;
      int size = text.length();

      int i;
      for(i = 0; i < size; ++i) {
         String entity = null;
         char c = text.charAt(i);
         switch (c) {
            case '\t':
            case '\n':
            case '\r':
               break;
            case '"':
               if (quote == '"') {
                  entity = "&quot;";
               }
               break;
            case '&':
               entity = "&amp;";
               break;
            case '\'':
               if (quote == '\'') {
                  entity = "&apos;";
               }
               break;
            case '<':
               entity = "&lt;";
               break;
            case '>':
               entity = "&gt;";
               break;
            default:
               if (c < ' ' || this.shouldEncodeChar(c)) {
                  entity = "&#" + c + ";";
               }
         }

         if (entity != null) {
            if (block == null) {
               block = text.toCharArray();
            }

            this.buffer.append(block, last, i - last);
            this.buffer.append(entity);
            last = i + 1;
         }
      }

      if (last == 0) {
         return text;
      } else {
         if (last < size) {
            if (block == null) {
               block = text.toCharArray();
            }

            this.buffer.append(block, last, i - last);
         }

         String answer = this.buffer.toString();
         this.buffer.setLength(0);
         return answer;
      }
   }

   protected boolean shouldEncodeChar(char c) {
      int max = this.getMaximumAllowedCharacter();
      return max > 0 && c > max;
   }

   protected int defaultMaximumAllowedCharacter() {
      String encoding = this.format.getEncoding();
      return encoding != null && encoding.equals("US-ASCII") ? 127 : -1;
   }

   protected boolean isNamespaceDeclaration(Namespace ns) {
      if (ns != null && ns != Namespace.XML_NAMESPACE) {
         String uri = ns.getURI();
         if (uri != null && !this.namespaceStack.contains(ns)) {
            return true;
         }
      }

      return false;
   }

   protected void handleException(IOException e) throws SAXException {
      throw new SAXException(e);
   }

   protected OutputFormat getOutputFormat() {
      return this.format;
   }

   public boolean resolveEntityRefs() {
      return this.resolveEntityRefs;
   }

   public void setResolveEntityRefs(boolean resolve) {
      this.resolveEntityRefs = resolve;
   }
}
