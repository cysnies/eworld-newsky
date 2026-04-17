package org.dom4j.io;

import javax.xml.transform.sax.SAXSource;
import org.dom4j.Document;
import org.dom4j.Node;
import org.xml.sax.InputSource;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;

public class DocumentSource extends SAXSource {
   public static final String DOM4J_FEATURE = "http://org.dom4j.io.DoucmentSource/feature";
   private XMLReader xmlReader = new SAXWriter();

   public DocumentSource(Node node) {
      this.setDocument(node.getDocument());
   }

   public DocumentSource(Document document) {
      this.setDocument(document);
   }

   public Document getDocument() {
      DocumentInputSource source = (DocumentInputSource)this.getInputSource();
      return source.getDocument();
   }

   public void setDocument(Document document) {
      super.setInputSource(new DocumentInputSource(document));
   }

   public XMLReader getXMLReader() {
      return this.xmlReader;
   }

   public void setInputSource(InputSource inputSource) throws UnsupportedOperationException {
      if (inputSource instanceof DocumentInputSource) {
         super.setInputSource((DocumentInputSource)inputSource);
      } else {
         throw new UnsupportedOperationException();
      }
   }

   public void setXMLReader(XMLReader reader) throws UnsupportedOperationException {
      if (reader instanceof SAXWriter) {
         this.xmlReader = (SAXWriter)reader;
      } else {
         if (!(reader instanceof XMLFilter)) {
            throw new UnsupportedOperationException();
         }

         XMLFilter filter = (XMLFilter)reader;

         while(true) {
            XMLReader parent = filter.getParent();
            if (!(parent instanceof XMLFilter)) {
               filter.setParent(this.xmlReader);
               this.xmlReader = filter;
               break;
            }

            filter = (XMLFilter)parent;
         }
      }

   }
}
