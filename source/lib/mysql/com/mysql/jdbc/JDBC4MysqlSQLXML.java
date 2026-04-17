package com.mysql.jdbc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.sql.SQLException;
import java.sql.SQLXML;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stax.StAXResult;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class JDBC4MysqlSQLXML implements SQLXML {
   private XMLInputFactory inputFactory;
   private XMLOutputFactory outputFactory;
   private String stringRep;
   private ResultSetInternalMethods owningResultSet;
   private int columnIndexOfXml;
   private boolean fromResultSet;
   private boolean isClosed = false;
   private boolean workingWithResult;
   private DOMResult asDOMResult;
   private SAXResult asSAXResult;
   private SimpleSaxToReader saxToReaderConverter;
   private StringWriter asStringWriter;
   private ByteArrayOutputStream asByteArrayOutputStream;

   protected JDBC4MysqlSQLXML(ResultSetInternalMethods owner, int index) {
      this.owningResultSet = owner;
      this.columnIndexOfXml = index;
      this.fromResultSet = true;
   }

   protected JDBC4MysqlSQLXML() {
      this.fromResultSet = false;
   }

   public synchronized void free() throws SQLException {
      this.stringRep = null;
      this.asDOMResult = null;
      this.asSAXResult = null;
      this.inputFactory = null;
      this.outputFactory = null;
      this.owningResultSet = null;
      this.workingWithResult = false;
      this.isClosed = true;
   }

   public synchronized String getString() throws SQLException {
      this.checkClosed();
      this.checkWorkingWithResult();
      return this.fromResultSet ? this.owningResultSet.getString(this.columnIndexOfXml) : this.stringRep;
   }

   private synchronized void checkClosed() throws SQLException {
      if (this.isClosed) {
         throw SQLError.createSQLException("SQLXMLInstance has been free()d");
      }
   }

   private synchronized void checkWorkingWithResult() throws SQLException {
      if (this.workingWithResult) {
         throw SQLError.createSQLException("Can't perform requested operation after getResult() has been called to write XML data", "S1009");
      }
   }

   public synchronized void setString(String str) throws SQLException {
      this.checkClosed();
      this.checkWorkingWithResult();
      this.stringRep = str;
      this.fromResultSet = false;
   }

   public synchronized boolean isEmpty() throws SQLException {
      this.checkClosed();
      this.checkWorkingWithResult();
      if (this.fromResultSet) {
         return false;
      } else {
         return this.stringRep == null || this.stringRep.length() == 0;
      }
   }

   public synchronized InputStream getBinaryStream() throws SQLException {
      this.checkClosed();
      this.checkWorkingWithResult();
      return this.owningResultSet.getBinaryStream(this.columnIndexOfXml);
   }

   public synchronized Reader getCharacterStream() throws SQLException {
      this.checkClosed();
      this.checkWorkingWithResult();
      return this.owningResultSet.getCharacterStream(this.columnIndexOfXml);
   }

   public synchronized Source getSource(Class clazz) throws SQLException {
      this.checkClosed();
      this.checkWorkingWithResult();
      if (clazz != null && !clazz.equals(SAXSource.class)) {
         if (clazz.equals(DOMSource.class)) {
            try {
               DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
               builderFactory.setNamespaceAware(true);
               DocumentBuilder builder = builderFactory.newDocumentBuilder();
               InputSource inputSource = null;
               if (this.fromResultSet) {
                  inputSource = new InputSource(this.owningResultSet.getCharacterStream(this.columnIndexOfXml));
               } else {
                  inputSource = new InputSource(new StringReader(this.stringRep));
               }

               return new DOMSource(builder.parse(inputSource));
            } catch (Throwable t) {
               SQLException sqlEx = SQLError.createSQLException(t.getMessage(), "S1009");
               sqlEx.initCause(t);
               throw sqlEx;
            }
         } else if (clazz.equals(StreamSource.class)) {
            Reader reader = null;
            Object var11;
            if (this.fromResultSet) {
               var11 = this.owningResultSet.getCharacterStream(this.columnIndexOfXml);
            } else {
               var11 = new StringReader(this.stringRep);
            }

            return new StreamSource((Reader)var11);
         } else if (clazz.equals(StAXSource.class)) {
            try {
               Reader reader = null;
               Object ex;
               if (this.fromResultSet) {
                  ex = this.owningResultSet.getCharacterStream(this.columnIndexOfXml);
               } else {
                  ex = new StringReader(this.stringRep);
               }

               return new StAXSource(this.inputFactory.createXMLStreamReader((Reader)ex));
            } catch (XMLStreamException ex) {
               SQLException sqlEx = SQLError.createSQLException(ex.getMessage(), "S1009");
               sqlEx.initCause(ex);
               throw sqlEx;
            }
         } else {
            throw SQLError.createSQLException("XML Source of type \"" + clazz.toString() + "\" Not supported.", "S1009");
         }
      } else {
         InputSource inputSource = null;
         if (this.fromResultSet) {
            inputSource = new InputSource(this.owningResultSet.getCharacterStream(this.columnIndexOfXml));
         } else {
            inputSource = new InputSource(new StringReader(this.stringRep));
         }

         return new SAXSource(inputSource);
      }
   }

   public synchronized OutputStream setBinaryStream() throws SQLException {
      this.checkClosed();
      this.checkWorkingWithResult();
      this.workingWithResult = true;
      return this.setBinaryStreamInternal();
   }

   private synchronized OutputStream setBinaryStreamInternal() throws SQLException {
      this.asByteArrayOutputStream = new ByteArrayOutputStream();
      return this.asByteArrayOutputStream;
   }

   public synchronized Writer setCharacterStream() throws SQLException {
      this.checkClosed();
      this.checkWorkingWithResult();
      this.workingWithResult = true;
      return this.setCharacterStreamInternal();
   }

   private synchronized Writer setCharacterStreamInternal() throws SQLException {
      this.asStringWriter = new StringWriter();
      return this.asStringWriter;
   }

   public synchronized Result setResult(Class clazz) throws SQLException {
      this.checkClosed();
      this.checkWorkingWithResult();
      this.workingWithResult = true;
      this.asDOMResult = null;
      this.asSAXResult = null;
      this.saxToReaderConverter = null;
      this.stringRep = null;
      this.asStringWriter = null;
      this.asByteArrayOutputStream = null;
      if (clazz != null && !clazz.equals(SAXResult.class)) {
         if (clazz.equals(DOMResult.class)) {
            this.asDOMResult = new DOMResult();
            return this.asDOMResult;
         } else if (clazz.equals(StreamResult.class)) {
            return new StreamResult(this.setCharacterStreamInternal());
         } else if (clazz.equals(StAXResult.class)) {
            try {
               if (this.outputFactory == null) {
                  this.outputFactory = XMLOutputFactory.newInstance();
               }

               return new StAXResult(this.outputFactory.createXMLEventWriter(this.setCharacterStreamInternal()));
            } catch (XMLStreamException ex) {
               SQLException sqlEx = SQLError.createSQLException(ex.getMessage(), "S1009");
               sqlEx.initCause(ex);
               throw sqlEx;
            }
         } else {
            throw SQLError.createSQLException("XML Result of type \"" + clazz.toString() + "\" Not supported.", "S1009");
         }
      } else {
         this.saxToReaderConverter = new SimpleSaxToReader();
         this.asSAXResult = new SAXResult(this.saxToReaderConverter);
         return this.asSAXResult;
      }
   }

   private Reader binaryInputStreamStreamToReader(ByteArrayOutputStream out) {
      try {
         String encoding = "UTF-8";

         try {
            ByteArrayInputStream bIn = new ByteArrayInputStream(out.toByteArray());
            XMLStreamReader reader = this.inputFactory.createXMLStreamReader(bIn);
            int eventType = 0;

            while((eventType = reader.next()) != 8) {
               if (eventType == 7) {
                  String possibleEncoding = reader.getEncoding();
                  if (possibleEncoding != null) {
                     encoding = possibleEncoding;
                  }
                  break;
               }
            }
         } catch (Throwable var7) {
         }

         return new StringReader(new String(out.toByteArray(), encoding));
      } catch (UnsupportedEncodingException badEnc) {
         throw new RuntimeException(badEnc);
      }
   }

   protected String readerToString(Reader reader) throws SQLException {
      StringBuffer buf = new StringBuffer();
      int charsRead = 0;
      char[] charBuf = new char[512];

      try {
         while((charsRead = reader.read(charBuf)) != -1) {
            buf.append(charBuf, 0, charsRead);
         }
      } catch (IOException ioEx) {
         SQLException sqlEx = SQLError.createSQLException(ioEx.getMessage(), "S1009");
         sqlEx.initCause(ioEx);
         throw sqlEx;
      }

      return buf.toString();
   }

   protected synchronized Reader serializeAsCharacterStream() throws SQLException {
      this.checkClosed();
      if (this.workingWithResult) {
         if (this.stringRep != null) {
            return new StringReader(this.stringRep);
         }

         if (this.asDOMResult != null) {
            return new StringReader(this.domSourceToString());
         }

         if (this.asStringWriter != null) {
            return new StringReader(this.asStringWriter.toString());
         }

         if (this.asSAXResult != null) {
            return this.saxToReaderConverter.toReader();
         }

         if (this.asByteArrayOutputStream != null) {
            return this.binaryInputStreamStreamToReader(this.asByteArrayOutputStream);
         }
      }

      return this.owningResultSet.getCharacterStream(this.columnIndexOfXml);
   }

   protected String domSourceToString() throws SQLException {
      try {
         DOMSource source = new DOMSource(this.asDOMResult.getNode());
         Transformer identity = TransformerFactory.newInstance().newTransformer();
         StringWriter stringOut = new StringWriter();
         Result result = new StreamResult(stringOut);
         identity.transform(source, result);
         return stringOut.toString();
      } catch (Throwable t) {
         SQLException sqlEx = SQLError.createSQLException(t.getMessage(), "S1009");
         sqlEx.initCause(t);
         throw sqlEx;
      }
   }

   protected synchronized String serializeAsString() throws SQLException {
      this.checkClosed();
      if (this.workingWithResult) {
         if (this.stringRep != null) {
            return this.stringRep;
         }

         if (this.asDOMResult != null) {
            return this.domSourceToString();
         }

         if (this.asStringWriter != null) {
            return this.asStringWriter.toString();
         }

         if (this.asSAXResult != null) {
            return this.readerToString(this.saxToReaderConverter.toReader());
         }

         if (this.asByteArrayOutputStream != null) {
            return this.readerToString(this.binaryInputStreamStreamToReader(this.asByteArrayOutputStream));
         }
      }

      return this.owningResultSet.getString(this.columnIndexOfXml);
   }

   class SimpleSaxToReader extends DefaultHandler {
      StringBuffer buf = new StringBuffer();
      private boolean inCDATA = false;

      public void startDocument() throws SAXException {
         this.buf.append("<?xml version='1.0' encoding='UTF-8'?>");
      }

      public void endDocument() throws SAXException {
      }

      public void startElement(String namespaceURI, String sName, String qName, Attributes attrs) throws SAXException {
         this.buf.append("<");
         this.buf.append(qName);
         if (attrs != null) {
            for(int i = 0; i < attrs.getLength(); ++i) {
               this.buf.append(" ");
               this.buf.append(attrs.getQName(i)).append("=\"");
               this.escapeCharsForXml(attrs.getValue(i), true);
               this.buf.append("\"");
            }
         }

         this.buf.append(">");
      }

      public void characters(char[] buf, int offset, int len) throws SAXException {
         if (!this.inCDATA) {
            this.escapeCharsForXml(buf, offset, len, false);
         } else {
            this.buf.append(buf, offset, len);
         }

      }

      public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
         this.characters(ch, start, length);
      }

      public void startCDATA() throws SAXException {
         this.buf.append("<![CDATA[");
         this.inCDATA = true;
      }

      public void endCDATA() throws SAXException {
         this.inCDATA = false;
         this.buf.append("]]>");
      }

      public void comment(char[] ch, int start, int length) throws SAXException {
         this.buf.append("<!--");

         for(int i = 0; i < length; ++i) {
            this.buf.append(ch[start + i]);
         }

         this.buf.append("-->");
      }

      Reader toReader() {
         return new StringReader(this.buf.toString());
      }

      private void escapeCharsForXml(String str, boolean isAttributeData) {
         if (str != null) {
            int strLen = str.length();

            for(int i = 0; i < strLen; ++i) {
               this.escapeCharsForXml(str.charAt(i), isAttributeData);
            }

         }
      }

      private void escapeCharsForXml(char[] buf, int offset, int len, boolean isAttributeData) {
         if (buf != null) {
            for(int i = 0; i < len; ++i) {
               this.escapeCharsForXml(buf[offset + i], isAttributeData);
            }

         }
      }

      private void escapeCharsForXml(char c, boolean isAttributeData) {
         switch (c) {
            case '\r':
               this.buf.append("&#xD;");
               break;
            case '"':
               if (!isAttributeData) {
                  this.buf.append("\"");
               } else {
                  this.buf.append("&quot;");
               }
               break;
            case '&':
               this.buf.append("&amp;");
               break;
            case '<':
               this.buf.append("&lt;");
               break;
            case '>':
               this.buf.append("&gt;");
               break;
            default:
               if ((c < 1 || c > 31 || c == '\t' || c == '\n') && (c < 127 || c > 159) && c != 8232 && (!isAttributeData || c != '\t' && c != '\n')) {
                  this.buf.append(c);
               } else {
                  this.buf.append("&#x");
                  this.buf.append(Integer.toHexString(c).toUpperCase());
                  this.buf.append(";");
               }
         }

      }
   }
}
