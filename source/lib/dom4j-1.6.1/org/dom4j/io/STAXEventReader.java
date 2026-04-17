package org.dom4j.io;

import java.io.InputStream;
import java.io.Reader;
import java.util.Iterator;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.EntityReference;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.dom4j.Attribute;
import org.dom4j.CharacterData;
import org.dom4j.Comment;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Entity;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.ProcessingInstruction;

public class STAXEventReader {
   private DocumentFactory factory;
   private XMLInputFactory inputFactory = XMLInputFactory.newInstance();

   public STAXEventReader() {
      this.factory = DocumentFactory.getInstance();
   }

   public STAXEventReader(DocumentFactory factory) {
      if (factory != null) {
         this.factory = factory;
      } else {
         this.factory = DocumentFactory.getInstance();
      }

   }

   public void setDocumentFactory(DocumentFactory documentFactory) {
      if (documentFactory != null) {
         this.factory = documentFactory;
      } else {
         this.factory = DocumentFactory.getInstance();
      }

   }

   public Document readDocument(InputStream is) throws XMLStreamException {
      return this.readDocument((InputStream)is, (String)null);
   }

   public Document readDocument(Reader reader) throws XMLStreamException {
      return this.readDocument((Reader)reader, (String)null);
   }

   public Document readDocument(InputStream is, String systemId) throws XMLStreamException {
      XMLEventReader eventReader = this.inputFactory.createXMLEventReader(systemId, is);

      Document var4;
      try {
         var4 = this.readDocument(eventReader);
      } finally {
         eventReader.close();
      }

      return var4;
   }

   public Document readDocument(Reader reader, String systemId) throws XMLStreamException {
      XMLEventReader eventReader = this.inputFactory.createXMLEventReader(systemId, reader);

      Document var4;
      try {
         var4 = this.readDocument(eventReader);
      } finally {
         eventReader.close();
      }

      return var4;
   }

   public Node readNode(XMLEventReader reader) throws XMLStreamException {
      XMLEvent event = reader.peek();
      if (event.isStartElement()) {
         return this.readElement(reader);
      } else if (event.isCharacters()) {
         return this.readCharacters(reader);
      } else if (event.isStartDocument()) {
         return this.readDocument(reader);
      } else if (event.isProcessingInstruction()) {
         return this.readProcessingInstruction(reader);
      } else if (event.isEntityReference()) {
         return this.readEntityReference(reader);
      } else if (event.isAttribute()) {
         return this.readAttribute(reader);
      } else if (event.isNamespace()) {
         return this.readNamespace(reader);
      } else {
         throw new XMLStreamException("Unsupported event: " + event);
      }
   }

   public Document readDocument(XMLEventReader reader) throws XMLStreamException {
      Document doc = null;

      while(reader.hasNext()) {
         XMLEvent nextEvent = reader.peek();
         int type = nextEvent.getEventType();
         switch (type) {
            case 4:
            case 6:
            case 8:
               reader.nextEvent();
               break;
            case 5:
            default:
               if (doc == null) {
                  doc = this.factory.createDocument();
               }

               Node n = this.readNode(reader);
               doc.add(n);
               break;
            case 7:
               StartDocument event = (StartDocument)reader.nextEvent();
               if (doc != null) {
                  String msg = "Unexpected StartDocument event";
                  throw new XMLStreamException(msg, event.getLocation());
               }

               if (event.encodingSet()) {
                  String encodingScheme = event.getCharacterEncodingScheme();
                  doc = this.factory.createDocument(encodingScheme);
               } else {
                  doc = this.factory.createDocument();
               }
         }
      }

      return doc;
   }

   public Element readElement(XMLEventReader eventReader) throws XMLStreamException {
      XMLEvent event = eventReader.peek();
      if (!event.isStartElement()) {
         throw new XMLStreamException("Expected Element event, found: " + event);
      } else {
         StartElement startTag = eventReader.nextEvent().asStartElement();
         Element elem = this.createElement(startTag);

         while(eventReader.hasNext()) {
            XMLEvent nextEvent = eventReader.peek();
            if (nextEvent.isEndElement()) {
               EndElement endElem = eventReader.nextEvent().asEndElement();
               if (!endElem.getName().equals(startTag.getName())) {
                  throw new XMLStreamException("Expected " + startTag.getName() + " end-tag, but found" + endElem.getName());
               }

               return elem;
            }

            Node child = this.readNode(eventReader);
            elem.add((Node)child);
         }

         String msg = "Unexpected end of stream while reading element content";
         throw new XMLStreamException(msg);
      }
   }

   public Attribute readAttribute(XMLEventReader reader) throws XMLStreamException {
      XMLEvent event = reader.peek();
      if (event.isAttribute()) {
         javax.xml.stream.events.Attribute attr = (javax.xml.stream.events.Attribute)reader.nextEvent();
         return this.createAttribute((Element)null, attr);
      } else {
         throw new XMLStreamException("Expected Attribute event, found: " + event);
      }
   }

   public Namespace readNamespace(XMLEventReader reader) throws XMLStreamException {
      XMLEvent event = reader.peek();
      if (event.isNamespace()) {
         javax.xml.stream.events.Namespace ns = (javax.xml.stream.events.Namespace)reader.nextEvent();
         return this.createNamespace(ns);
      } else {
         throw new XMLStreamException("Expected Namespace event, found: " + event);
      }
   }

   public CharacterData readCharacters(XMLEventReader reader) throws XMLStreamException {
      XMLEvent event = reader.peek();
      if (event.isCharacters()) {
         Characters characters = reader.nextEvent().asCharacters();
         return this.createCharacterData(characters);
      } else {
         throw new XMLStreamException("Expected Characters event, found: " + event);
      }
   }

   public Comment readComment(XMLEventReader reader) throws XMLStreamException {
      XMLEvent event = reader.peek();
      if (event instanceof javax.xml.stream.events.Comment) {
         return this.createComment((javax.xml.stream.events.Comment)reader.nextEvent());
      } else {
         throw new XMLStreamException("Expected Comment event, found: " + event);
      }
   }

   public Entity readEntityReference(XMLEventReader reader) throws XMLStreamException {
      XMLEvent event = reader.peek();
      if (event.isEntityReference()) {
         EntityReference entityRef = (EntityReference)reader.nextEvent();
         return this.createEntity(entityRef);
      } else {
         throw new XMLStreamException("Expected EntityRef event, found: " + event);
      }
   }

   public ProcessingInstruction readProcessingInstruction(XMLEventReader reader) throws XMLStreamException {
      XMLEvent event = reader.peek();
      if (event.isProcessingInstruction()) {
         javax.xml.stream.events.ProcessingInstruction pi = (javax.xml.stream.events.ProcessingInstruction)reader.nextEvent();
         return this.createProcessingInstruction(pi);
      } else {
         throw new XMLStreamException("Expected PI event, found: " + event);
      }
   }

   public Element createElement(StartElement startEvent) {
      QName qname = startEvent.getName();
      org.dom4j.QName elemName = this.createQName(qname);
      Element elem = this.factory.createElement(elemName);
      Iterator i = startEvent.getAttributes();

      while(i.hasNext()) {
         javax.xml.stream.events.Attribute attr = (javax.xml.stream.events.Attribute)i.next();
         elem.addAttribute(this.createQName(attr.getName()), attr.getValue());
      }

      i = startEvent.getNamespaces();

      while(i.hasNext()) {
         javax.xml.stream.events.Namespace ns = (javax.xml.stream.events.Namespace)i.next();
         elem.addNamespace(ns.getPrefix(), ns.getNamespaceURI());
      }

      return elem;
   }

   public Attribute createAttribute(Element elem, javax.xml.stream.events.Attribute attr) {
      return this.factory.createAttribute(elem, this.createQName(attr.getName()), attr.getValue());
   }

   public Namespace createNamespace(javax.xml.stream.events.Namespace ns) {
      return this.factory.createNamespace(ns.getPrefix(), ns.getNamespaceURI());
   }

   public CharacterData createCharacterData(Characters characters) {
      String data = characters.getData();
      return (CharacterData)(characters.isCData() ? this.factory.createCDATA(data) : this.factory.createText(data));
   }

   public Comment createComment(javax.xml.stream.events.Comment comment) {
      return this.factory.createComment(comment.getText());
   }

   public Entity createEntity(EntityReference entityRef) {
      return this.factory.createEntity(entityRef.getName(), entityRef.getDeclaration().getReplacementText());
   }

   public ProcessingInstruction createProcessingInstruction(javax.xml.stream.events.ProcessingInstruction pi) {
      return this.factory.createProcessingInstruction(pi.getTarget(), pi.getData());
   }

   public org.dom4j.QName createQName(QName qname) {
      return this.factory.createQName(qname.getLocalPart(), qname.getPrefix(), qname.getNamespaceURI());
   }
}
