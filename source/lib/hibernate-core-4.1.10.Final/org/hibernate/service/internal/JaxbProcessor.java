package org.hibernate.service.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.hibernate.internal.jaxb.Origin;
import org.hibernate.internal.jaxb.cfg.JaxbHibernateConfiguration;
import org.hibernate.internal.util.config.ConfigurationException;
import org.hibernate.metamodel.source.MappingException;
import org.hibernate.metamodel.source.XsdException;
import org.hibernate.service.classloading.spi.ClassLoaderService;
import org.jboss.logging.Logger;
import org.xml.sax.SAXException;

public class JaxbProcessor {
   private static final Logger log = Logger.getLogger(JaxbProcessor.class);
   private final ClassLoaderService classLoaderService;
   private XMLInputFactory staxFactory;
   private Schema schema;

   public JaxbProcessor(ClassLoaderService classLoaderService) {
      this.classLoaderService = classLoaderService;
   }

   public JaxbHibernateConfiguration unmarshal(InputStream stream, Origin origin) {
      try {
         XMLStreamReader staxReader = this.staxFactory().createXMLStreamReader(stream);

         JaxbHibernateConfiguration var4;
         try {
            var4 = this.unmarshal(staxReader, origin);
         } finally {
            try {
               staxReader.close();
            } catch (Exception var12) {
            }

         }

         return var4;
      } catch (XMLStreamException e) {
         throw new MappingException("Unable to create stax reader", e, origin);
      }
   }

   private XMLInputFactory staxFactory() {
      if (this.staxFactory == null) {
         this.staxFactory = this.buildStaxFactory();
      }

      return this.staxFactory;
   }

   private XMLInputFactory buildStaxFactory() {
      XMLInputFactory staxFactory = XMLInputFactory.newInstance();
      return staxFactory;
   }

   private JaxbHibernateConfiguration unmarshal(XMLStreamReader staxReader, Origin origin) {
      ContextProvidingValidationEventHandler handler = new ContextProvidingValidationEventHandler();

      try {
         JAXBContext jaxbContext = JAXBContext.newInstance(new Class[]{JaxbHibernateConfiguration.class});
         Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
         unmarshaller.setSchema(this.schema());
         unmarshaller.setEventHandler(handler);
         Object target = unmarshaller.unmarshal(staxReader);
         return (JaxbHibernateConfiguration)target;
      } catch (JAXBException e) {
         StringBuilder builder = new StringBuilder();
         builder.append("Unable to perform unmarshalling at line number ").append(handler.getLineNumber()).append(" and column ").append(handler.getColumnNumber()).append(" in ").append(origin.getType().name()).append(" ").append(origin.getName()).append(". Message: ").append(handler.getMessage());
         throw new ConfigurationException(builder.toString(), e);
      }
   }

   private Schema schema() {
      if (this.schema == null) {
         this.schema = this.resolveLocalSchema("org/hibernate/hibernate-configuration-4.0.xsd");
      }

      return this.schema;
   }

   private Schema resolveLocalSchema(String schemaName) {
      return this.resolveLocalSchema(schemaName, "http://www.w3.org/2001/XMLSchema");
   }

   private Schema resolveLocalSchema(String schemaName, String schemaLanguage) {
      URL url = this.classLoaderService.locateResource(schemaName);
      if (url == null) {
         throw new XsdException("Unable to locate schema [" + schemaName + "] via classpath", schemaName);
      } else {
         try {
            InputStream schemaStream = url.openStream();

            Schema var7;
            try {
               StreamSource source = new StreamSource(url.openStream());
               SchemaFactory schemaFactory = SchemaFactory.newInstance(schemaLanguage);
               var7 = schemaFactory.newSchema(source);
            } catch (SAXException e) {
               throw new XsdException("Unable to load schema [" + schemaName + "]", e, schemaName);
            } catch (IOException e) {
               throw new XsdException("Unable to load schema [" + schemaName + "]", e, schemaName);
            } finally {
               try {
                  schemaStream.close();
               } catch (IOException e) {
                  log.debugf("Problem closing schema stream [%s]", e.toString());
               }

            }

            return var7;
         } catch (IOException var21) {
            throw new XsdException("Stream error handling schema url [" + url.toExternalForm() + "]", schemaName);
         }
      }
   }

   static class ContextProvidingValidationEventHandler implements ValidationEventHandler {
      private int lineNumber;
      private int columnNumber;
      private String message;

      public boolean handleEvent(ValidationEvent validationEvent) {
         ValidationEventLocator locator = validationEvent.getLocator();
         this.lineNumber = locator.getLineNumber();
         this.columnNumber = locator.getColumnNumber();
         this.message = validationEvent.getMessage();
         return false;
      }

      public int getLineNumber() {
         return this.lineNumber;
      }

      public int getColumnNumber() {
         return this.columnNumber;
      }

      public String getMessage() {
         return this.message;
      }
   }
}
