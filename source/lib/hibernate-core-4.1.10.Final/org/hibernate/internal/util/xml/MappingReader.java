package org.hibernate.internal.util.xml;

import java.io.StringReader;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.hibernate.InvalidMappingException;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class MappingReader {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, MappingReader.class.getName());
   public static final MappingReader INSTANCE = new MappingReader();

   private MappingReader() {
   }

   public XmlDocument readMappingDocument(EntityResolver entityResolver, InputSource source, Origin origin) {
      ErrorLogger errorHandler = new ErrorLogger();
      SAXReader saxReader = new SAXReader();
      saxReader.setEntityResolver(entityResolver);
      saxReader.setErrorHandler(errorHandler);
      saxReader.setMergeAdjacentText(true);
      saxReader.setValidation(true);
      Document document = null;

      try {
         this.setValidationFor(saxReader, "orm_2_0.xsd");
         document = saxReader.read(source);
         if (errorHandler.hasErrors()) {
            throw (SAXParseException)errorHandler.getErrors().get(0);
         } else {
            return new XmlDocumentImpl(document, origin.getType(), origin.getName());
         }
      } catch (Exception failure) {
         if (LOG.isDebugEnabled()) {
            LOG.debugf("Problem parsing XML using orm 2 xsd : %s", failure.getMessage());
         }

         errorHandler.reset();
         if (document != null) {
            try {
               this.setValidationFor(saxReader, "orm_1_0.xsd");
               document = saxReader.read(new StringReader(document.asXML()));
               if (errorHandler.hasErrors()) {
                  errorHandler.logErrors();
                  throw (SAXParseException)errorHandler.getErrors().get(0);
               }

               return new XmlDocumentImpl(document, origin.getType(), origin.getName());
            } catch (Exception orm1Problem) {
               if (LOG.isDebugEnabled()) {
                  LOG.debugf("Problem parsing XML using orm 1 xsd : %s", orm1Problem.getMessage());
               }
            }
         }

         throw new InvalidMappingException("Unable to read XML", origin.getType(), origin.getName(), failure);
      }
   }

   private void setValidationFor(SAXReader saxReader, String xsd) {
      try {
         saxReader.setFeature("http://apache.org/xml/features/validation/schema", true);
         saxReader.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation", "http://java.sun.com/xml/ns/persistence/orm " + xsd);
      } catch (SAXException var4) {
         saxReader.setValidation(false);
      }

   }
}
