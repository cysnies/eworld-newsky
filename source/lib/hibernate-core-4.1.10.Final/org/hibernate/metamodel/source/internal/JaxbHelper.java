package org.hibernate.metamodel.source.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.hibernate.internal.jaxb.JaxbRoot;
import org.hibernate.internal.jaxb.Origin;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbHibernateMapping;
import org.hibernate.internal.jaxb.mapping.orm.JaxbEntityMappings;
import org.hibernate.metamodel.MetadataSources;
import org.hibernate.metamodel.source.MappingException;
import org.hibernate.metamodel.source.XsdException;
import org.hibernate.service.classloading.spi.ClassLoaderService;
import org.jboss.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class JaxbHelper {
   private static final Logger log = Logger.getLogger(JaxbHelper.class);
   public static final String ASSUMED_ORM_XSD_VERSION = "2.0";
   private final MetadataSources metadataSources;
   private XMLInputFactory staxFactory;
   private static final QName ORM_VERSION_ATTRIBUTE_QNAME = new QName("version");
   public static final String HBM_SCHEMA_NAME = "org/hibernate/hibernate-mapping-4.0.xsd";
   public static final String ORM_1_SCHEMA_NAME = "org/hibernate/ejb/orm_1_0.xsd";
   public static final String ORM_2_SCHEMA_NAME = "org/hibernate/ejb/orm_2_0.xsd";
   private Schema hbmSchema;
   private Schema orm1Schema;
   private Schema orm2Schema;

   public JaxbHelper(MetadataSources metadataSources) {
      this.metadataSources = metadataSources;
   }

   public JaxbRoot unmarshal(InputStream stream, Origin origin) {
      try {
         XMLEventReader staxReader = this.staxFactory().createXMLEventReader(stream);

         JaxbRoot var4;
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

   private JaxbRoot unmarshal(XMLEventReader staxEventReader, Origin origin) {
      XMLEvent event;
      try {
         for(event = staxEventReader.peek(); event != null && !event.isStartElement(); event = staxEventReader.peek()) {
            staxEventReader.nextEvent();
         }
      } catch (Exception e) {
         throw new MappingException("Error accessing stax stream", e, origin);
      }

      if (event == null) {
         throw new MappingException("Could not locate root element", origin);
      } else {
         String elementName = event.asStartElement().getName().getLocalPart();
         Schema validationSchema;
         Class jaxbTarget;
         if ("entity-mappings".equals(elementName)) {
            Attribute attribute = event.asStartElement().getAttributeByName(ORM_VERSION_ATTRIBUTE_QNAME);
            String explicitVersion = attribute == null ? null : attribute.getValue();
            validationSchema = this.resolveSupportedOrmXsd(explicitVersion);
            jaxbTarget = JaxbEntityMappings.class;
         } else {
            validationSchema = this.hbmSchema();
            jaxbTarget = JaxbHibernateMapping.class;
         }

         ContextProvidingValidationEventHandler handler = new ContextProvidingValidationEventHandler();

         Object target;
         try {
            JAXBContext jaxbContext = JAXBContext.newInstance(new Class[]{jaxbTarget});
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setSchema(validationSchema);
            unmarshaller.setEventHandler(handler);
            target = unmarshaller.unmarshal(staxEventReader);
         } catch (JAXBException e) {
            StringBuilder builder = new StringBuilder();
            builder.append("Unable to perform unmarshalling at line number ");
            builder.append(handler.getLineNumber());
            builder.append(" and column ");
            builder.append(handler.getColumnNumber());
            builder.append(". Message: ");
            builder.append(handler.getMessage());
            throw new MappingException(builder.toString(), e, origin);
         }

         return new JaxbRoot(target, origin);
      }
   }

   public JaxbRoot unmarshal(Document document, Origin origin) {
      Element rootElement = document.getDocumentElement();
      if (rootElement == null) {
         throw new MappingException("No root element found", origin);
      } else {
         Schema validationSchema;
         Class jaxbTarget;
         if ("entity-mappings".equals(rootElement.getNodeName())) {
            String explicitVersion = rootElement.getAttribute("version");
            validationSchema = this.resolveSupportedOrmXsd(explicitVersion);
            jaxbTarget = JaxbEntityMappings.class;
         } else {
            validationSchema = this.hbmSchema();
            jaxbTarget = JaxbHibernateMapping.class;
         }

         Object target;
         try {
            JAXBContext jaxbContext = JAXBContext.newInstance(new Class[]{jaxbTarget});
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setSchema(validationSchema);
            target = unmarshaller.unmarshal(new DOMSource(document));
         } catch (JAXBException e) {
            throw new MappingException("Unable to perform unmarshalling", e, origin);
         }

         return new JaxbRoot(target, origin);
      }
   }

   private Schema resolveSupportedOrmXsd(String explicitVersion) {
      String xsdVersionString = explicitVersion == null ? "2.0" : explicitVersion;
      if ("1.0".equals(xsdVersionString)) {
         return this.orm1Schema();
      } else if ("2.0".equals(xsdVersionString)) {
         return this.orm2Schema();
      } else {
         throw new IllegalArgumentException("Unsupported orm.xml XSD version encountered [" + xsdVersionString + "]");
      }
   }

   private Schema hbmSchema() {
      if (this.hbmSchema == null) {
         this.hbmSchema = this.resolveLocalSchema("org/hibernate/hibernate-mapping-4.0.xsd");
      }

      return this.hbmSchema;
   }

   private Schema orm1Schema() {
      if (this.orm1Schema == null) {
         this.orm1Schema = this.resolveLocalSchema("org/hibernate/ejb/orm_1_0.xsd");
      }

      return this.orm1Schema;
   }

   private Schema orm2Schema() {
      if (this.orm2Schema == null) {
         this.orm2Schema = this.resolveLocalSchema("org/hibernate/ejb/orm_2_0.xsd");
      }

      return this.orm2Schema;
   }

   private Schema resolveLocalSchema(String schemaName) {
      return this.resolveLocalSchema(schemaName, "http://www.w3.org/2001/XMLSchema");
   }

   private Schema resolveLocalSchema(String schemaName, String schemaLanguage) {
      URL url = ((ClassLoaderService)this.metadataSources.getServiceRegistry().getService(ClassLoaderService.class)).locateResource(schemaName);
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
