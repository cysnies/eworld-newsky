package org.hibernate.internal.util.xml;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.DOMReader;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;

public final class XMLHelper {
   public static final EntityResolver DEFAULT_DTD_RESOLVER = new DTDEntityResolver();
   private DOMReader domReader;
   private SAXReader saxReader;

   public SAXReader createSAXReader(ErrorHandler errorHandler, EntityResolver entityResolver) {
      SAXReader saxReader = this.resolveSAXReader();
      saxReader.setEntityResolver(entityResolver);
      saxReader.setErrorHandler(errorHandler);
      return saxReader;
   }

   private SAXReader resolveSAXReader() {
      if (this.saxReader == null) {
         this.saxReader = new SAXReader();
         this.saxReader.setMergeAdjacentText(true);
         this.saxReader.setValidation(true);
      }

      return this.saxReader;
   }

   public DOMReader createDOMReader() {
      if (this.domReader == null) {
         this.domReader = new DOMReader();
      }

      return this.domReader;
   }

   public static Element generateDom4jElement(String elementName) {
      return getDocumentFactory().createElement(elementName);
   }

   public static DocumentFactory getDocumentFactory() {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();

      DocumentFactory factory;
      try {
         Thread.currentThread().setContextClassLoader(XMLHelper.class.getClassLoader());
         factory = DocumentFactory.getInstance();
      } finally {
         Thread.currentThread().setContextClassLoader(cl);
      }

      return factory;
   }

   public static void dump(Element element) {
      try {
         OutputFormat outFormat = OutputFormat.createPrettyPrint();
         XMLWriter writer = new XMLWriter(System.out, outFormat);
         writer.write(element);
         writer.flush();
         System.out.println("");
      } catch (Throwable var3) {
         System.out.println(element.asXML());
      }

   }
}
