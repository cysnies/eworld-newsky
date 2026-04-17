package org.dom4j.datatype;

import com.sun.msv.datatype.xsd.DatatypeFactory;
import com.sun.msv.datatype.xsd.TypeIncubator;
import com.sun.msv.datatype.xsd.XSDatatype;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.SAXReader;
import org.dom4j.util.AttributeHelper;
import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.ValidationContext;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

public class SchemaParser {
   private static final Namespace XSD_NAMESPACE = Namespace.get("xsd", "http://www.w3.org/2001/XMLSchema");
   private static final QName XSD_ELEMENT;
   private static final QName XSD_ATTRIBUTE;
   private static final QName XSD_SIMPLETYPE;
   private static final QName XSD_COMPLEXTYPE;
   private static final QName XSD_RESTRICTION;
   private static final QName XSD_SEQUENCE;
   private static final QName XSD_CHOICE;
   private static final QName XSD_ALL;
   private static final QName XSD_INCLUDE;
   private DatatypeDocumentFactory documentFactory;
   private Map dataTypeCache;
   private NamedTypeResolver namedTypeResolver;
   private Namespace targetNamespace;

   public SchemaParser() {
      this(DatatypeDocumentFactory.singleton);
   }

   public SchemaParser(DatatypeDocumentFactory documentFactory) {
      this.dataTypeCache = new HashMap();
      this.documentFactory = documentFactory;
      this.namedTypeResolver = new NamedTypeResolver(documentFactory);
   }

   public void build(Document schemaDocument) {
      this.targetNamespace = null;
      this.internalBuild(schemaDocument);
   }

   public void build(Document schemaDocument, Namespace namespace) {
      this.targetNamespace = namespace;
      this.internalBuild(schemaDocument);
   }

   private synchronized void internalBuild(Document schemaDocument) {
      Element root = schemaDocument.getRootElement();
      if (root != null) {
         Iterator includeIter = root.elementIterator(XSD_INCLUDE);

         while(includeIter.hasNext()) {
            Element includeElement = (Element)includeIter.next();
            String inclSchemaInstanceURI = includeElement.attributeValue("schemaLocation");
            EntityResolver resolver = schemaDocument.getEntityResolver();

            try {
               if (resolver == null) {
                  String msg = "No EntityResolver available";
                  throw new InvalidSchemaException(msg);
               }

               InputSource inputSource = resolver.resolveEntity((String)null, inclSchemaInstanceURI);
               if (inputSource == null) {
                  String msg = "Could not resolve the schema URI: " + inclSchemaInstanceURI;
                  throw new InvalidSchemaException(msg);
               }

               SAXReader reader = new SAXReader();
               Document inclSchemaDocument = reader.read(inputSource);
               this.build(inclSchemaDocument);
            } catch (Exception e) {
               System.out.println("Failed to load schema: " + inclSchemaInstanceURI);
               System.out.println("Caught: " + e);
               e.printStackTrace();
               throw new InvalidSchemaException("Failed to load schema: " + inclSchemaInstanceURI);
            }
         }

         Iterator iter = root.elementIterator(XSD_ELEMENT);

         while(iter.hasNext()) {
            this.onDatatypeElement((Element)iter.next(), this.documentFactory);
         }

         iter = root.elementIterator(XSD_SIMPLETYPE);

         while(iter.hasNext()) {
            this.onNamedSchemaSimpleType((Element)iter.next());
         }

         iter = root.elementIterator(XSD_COMPLEXTYPE);

         while(iter.hasNext()) {
            this.onNamedSchemaComplexType((Element)iter.next());
         }

         this.namedTypeResolver.resolveNamedTypes();
      }

   }

   private void onDatatypeElement(Element xsdElement, DocumentFactory parentFactory) {
      String name = xsdElement.attributeValue("name");
      String type = xsdElement.attributeValue("type");
      QName qname = this.getQName(name);
      DatatypeElementFactory factory = this.getDatatypeElementFactory(qname);
      if (type != null) {
         XSDatatype dataType = this.getTypeByName(type);
         if (dataType != null) {
            factory.setChildElementXSDatatype(qname, dataType);
         } else {
            QName typeQName = this.getQName(type);
            this.namedTypeResolver.registerTypedElement(xsdElement, typeQName, parentFactory);
         }

      } else {
         Element xsdSimpleType = xsdElement.element(XSD_SIMPLETYPE);
         if (xsdSimpleType != null) {
            XSDatatype dataType = this.loadXSDatatypeFromSimpleType(xsdSimpleType);
            if (dataType != null) {
               factory.setChildElementXSDatatype(qname, dataType);
            }
         }

         Element schemaComplexType = xsdElement.element(XSD_COMPLEXTYPE);
         if (schemaComplexType != null) {
            this.onSchemaComplexType(schemaComplexType, factory);
         }

         Iterator iter = xsdElement.elementIterator(XSD_ATTRIBUTE);
         if (iter.hasNext()) {
            do {
               this.onDatatypeAttribute(xsdElement, factory, (Element)iter.next());
            } while(iter.hasNext());
         }

      }
   }

   private void onNamedSchemaComplexType(Element schemaComplexType) {
      Attribute nameAttr = schemaComplexType.attribute("name");
      if (nameAttr != null) {
         String name = nameAttr.getText();
         QName qname = this.getQName(name);
         DatatypeElementFactory factory = this.getDatatypeElementFactory(qname);
         this.onSchemaComplexType(schemaComplexType, factory);
         this.namedTypeResolver.registerComplexType(qname, factory);
      }
   }

   private void onSchemaComplexType(Element schemaComplexType, DatatypeElementFactory elementFactory) {
      Iterator iter = schemaComplexType.elementIterator(XSD_ATTRIBUTE);

      while(iter.hasNext()) {
         Element xsdAttribute = (Element)iter.next();
         String name = xsdAttribute.attributeValue("name");
         QName qname = this.getQName(name);
         XSDatatype dataType = this.dataTypeForXsdAttribute(xsdAttribute);
         if (dataType != null) {
            elementFactory.setAttributeXSDatatype(qname, dataType);
         }
      }

      Element schemaSequence = schemaComplexType.element(XSD_SEQUENCE);
      if (schemaSequence != null) {
         this.onChildElements(schemaSequence, elementFactory);
      }

      Element schemaChoice = schemaComplexType.element(XSD_CHOICE);
      if (schemaChoice != null) {
         this.onChildElements(schemaChoice, elementFactory);
      }

      Element schemaAll = schemaComplexType.element(XSD_ALL);
      if (schemaAll != null) {
         this.onChildElements(schemaAll, elementFactory);
      }

   }

   private void onChildElements(Element element, DatatypeElementFactory fact) {
      Iterator iter = element.elementIterator(XSD_ELEMENT);

      while(iter.hasNext()) {
         Element xsdElement = (Element)iter.next();
         this.onDatatypeElement(xsdElement, fact);
      }

   }

   private void onDatatypeAttribute(Element xsdElement, DatatypeElementFactory elementFactory, Element xsdAttribute) {
      String name = xsdAttribute.attributeValue("name");
      QName qname = this.getQName(name);
      XSDatatype dataType = this.dataTypeForXsdAttribute(xsdAttribute);
      if (dataType != null) {
         elementFactory.setAttributeXSDatatype(qname, dataType);
      } else {
         String type = xsdAttribute.attributeValue("type");
         System.out.println("Warning: Couldn't find XSDatatype for type: " + type + " attribute: " + name);
      }

   }

   private XSDatatype dataTypeForXsdAttribute(Element xsdAttribute) {
      String type = xsdAttribute.attributeValue("type");
      XSDatatype dataType = null;
      if (type != null) {
         dataType = this.getTypeByName(type);
      } else {
         Element xsdSimpleType = xsdAttribute.element(XSD_SIMPLETYPE);
         if (xsdSimpleType == null) {
            String name = xsdAttribute.attributeValue("name");
            String msg = "The attribute: " + name + " has no type attribute and does not contain a " + "<simpleType/> element";
            throw new InvalidSchemaException(msg);
         }

         dataType = this.loadXSDatatypeFromSimpleType(xsdSimpleType);
      }

      return dataType;
   }

   private void onNamedSchemaSimpleType(Element schemaSimpleType) {
      Attribute nameAttr = schemaSimpleType.attribute("name");
      if (nameAttr != null) {
         String name = nameAttr.getText();
         QName qname = this.getQName(name);
         XSDatatype datatype = this.loadXSDatatypeFromSimpleType(schemaSimpleType);
         this.namedTypeResolver.registerSimpleType(qname, datatype);
      }
   }

   private XSDatatype loadXSDatatypeFromSimpleType(Element xsdSimpleType) {
      Element xsdRestriction = xsdSimpleType.element(XSD_RESTRICTION);
      if (xsdRestriction != null) {
         String base = xsdRestriction.attributeValue("base");
         if (base != null) {
            XSDatatype baseType = this.getTypeByName(base);
            if (baseType != null) {
               return this.deriveSimpleType(baseType, xsdRestriction);
            }

            this.onSchemaError("Invalid base type: " + base + " when trying to build restriction: " + xsdRestriction);
         } else {
            Element xsdSubType = xsdSimpleType.element(XSD_SIMPLETYPE);
            if (xsdSubType != null) {
               return this.loadXSDatatypeFromSimpleType(xsdSubType);
            }

            String msg = "The simpleType element: " + xsdSimpleType + " must contain a base attribute or simpleType" + " element";
            this.onSchemaError(msg);
         }
      } else {
         this.onSchemaError("No <restriction>. Could not create XSDatatype for simpleType: " + xsdSimpleType);
      }

      return null;
   }

   private XSDatatype deriveSimpleType(XSDatatype baseType, Element xsdRestriction) {
      TypeIncubator incubator = new TypeIncubator(baseType);
      ValidationContext context = null;

      try {
         Iterator iter = xsdRestriction.elementIterator();

         while(iter.hasNext()) {
            Element element = (Element)iter.next();
            String name = element.getName();
            String value = element.attributeValue("value");
            boolean fixed = AttributeHelper.booleanValue(element, "fixed");
            incubator.addFacet(name, value, fixed, context);
         }

         String newTypeName = null;
         return incubator.derive("", newTypeName);
      } catch (DatatypeException e) {
         this.onSchemaError("Invalid restriction: " + e.getMessage() + " when trying to build restriction: " + xsdRestriction);
         return null;
      }
   }

   private DatatypeElementFactory getDatatypeElementFactory(QName name) {
      DatatypeElementFactory factory = this.documentFactory.getElementFactory(name);
      if (factory == null) {
         factory = new DatatypeElementFactory(name);
         name.setDocumentFactory(factory);
      }

      return factory;
   }

   private XSDatatype getTypeByName(String type) {
      XSDatatype dataType = (XSDatatype)this.dataTypeCache.get(type);
      if (dataType == null) {
         int idx = type.indexOf(58);
         if (idx >= 0) {
            String localName = type.substring(idx + 1);

            try {
               dataType = DatatypeFactory.getTypeByName(localName);
            } catch (DatatypeException var7) {
            }
         }

         if (dataType == null) {
            try {
               dataType = DatatypeFactory.getTypeByName(type);
            } catch (DatatypeException var6) {
            }
         }

         if (dataType == null) {
            QName typeQName = this.getQName(type);
            dataType = (XSDatatype)this.namedTypeResolver.simpleTypeMap.get(typeQName);
         }

         if (dataType != null) {
            this.dataTypeCache.put(type, dataType);
         }
      }

      return dataType;
   }

   private QName getQName(String name) {
      return this.targetNamespace == null ? this.documentFactory.createQName(name) : this.documentFactory.createQName(name, this.targetNamespace);
   }

   private void onSchemaError(String message) {
      throw new InvalidSchemaException(message);
   }

   static {
      XSD_ELEMENT = QName.get("element", XSD_NAMESPACE);
      XSD_ATTRIBUTE = QName.get("attribute", XSD_NAMESPACE);
      XSD_SIMPLETYPE = QName.get("simpleType", XSD_NAMESPACE);
      XSD_COMPLEXTYPE = QName.get("complexType", XSD_NAMESPACE);
      XSD_RESTRICTION = QName.get("restriction", XSD_NAMESPACE);
      XSD_SEQUENCE = QName.get("sequence", XSD_NAMESPACE);
      XSD_CHOICE = QName.get("choice", XSD_NAMESPACE);
      XSD_ALL = QName.get("all", XSD_NAMESPACE);
      XSD_INCLUDE = QName.get("include", XSD_NAMESPACE);
   }
}
