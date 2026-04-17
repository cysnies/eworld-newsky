package org.dom4j.io;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

public class SAXEventRecorder extends DefaultHandler implements LexicalHandler, DeclHandler, DTDHandler, Externalizable {
   public static final long serialVersionUID = 1L;
   private static final byte STRING = 0;
   private static final byte OBJECT = 1;
   private static final byte NULL = 2;
   private List events = new ArrayList();
   private Map prefixMappings = new HashMap();
   private static final String XMLNS = "xmlns";
   private static final String EMPTY_STRING = "";

   public void replay(ContentHandler handler) throws SAXException {
      for(SAXEvent saxEvent : this.events) {
         switch (saxEvent.event) {
            case 1:
               handler.processingInstruction((String)saxEvent.getParm(0), (String)saxEvent.getParm(1));
               break;
            case 2:
               handler.startPrefixMapping((String)saxEvent.getParm(0), (String)saxEvent.getParm(1));
               break;
            case 3:
               handler.endPrefixMapping((String)saxEvent.getParm(0));
               break;
            case 4:
               handler.startDocument();
               break;
            case 5:
               handler.endDocument();
               break;
            case 6:
               AttributesImpl attributes = new AttributesImpl();
               List attParmList = (List)saxEvent.getParm(3);
               if (attParmList != null) {
                  for(String[] attParms : attParmList) {
                     attributes.addAttribute(attParms[0], attParms[1], attParms[2], attParms[3], attParms[4]);
                  }
               }

               handler.startElement((String)saxEvent.getParm(0), (String)saxEvent.getParm(1), (String)saxEvent.getParm(2), attributes);
               break;
            case 7:
               handler.endElement((String)saxEvent.getParm(0), (String)saxEvent.getParm(1), (String)saxEvent.getParm(2));
               break;
            case 8:
               char[] chars = (char[])saxEvent.getParm(0);
               int start = (Integer)saxEvent.getParm(1);
               int end = (Integer)saxEvent.getParm(2);
               handler.characters(chars, start, end);
               break;
            case 9:
               ((LexicalHandler)handler).startDTD((String)saxEvent.getParm(0), (String)saxEvent.getParm(1), (String)saxEvent.getParm(2));
               break;
            case 10:
               ((LexicalHandler)handler).endDTD();
               break;
            case 11:
               ((LexicalHandler)handler).startEntity((String)saxEvent.getParm(0));
               break;
            case 12:
               ((LexicalHandler)handler).endEntity((String)saxEvent.getParm(0));
               break;
            case 13:
               ((LexicalHandler)handler).startCDATA();
               break;
            case 14:
               ((LexicalHandler)handler).endCDATA();
               break;
            case 15:
               char[] cchars = (char[])saxEvent.getParm(0);
               int cstart = (Integer)saxEvent.getParm(1);
               int cend = (Integer)saxEvent.getParm(2);
               ((LexicalHandler)handler).comment(cchars, cstart, cend);
               break;
            case 16:
               ((DeclHandler)handler).elementDecl((String)saxEvent.getParm(0), (String)saxEvent.getParm(1));
               break;
            case 17:
               ((DeclHandler)handler).attributeDecl((String)saxEvent.getParm(0), (String)saxEvent.getParm(1), (String)saxEvent.getParm(2), (String)saxEvent.getParm(3), (String)saxEvent.getParm(4));
               break;
            case 18:
               ((DeclHandler)handler).internalEntityDecl((String)saxEvent.getParm(0), (String)saxEvent.getParm(1));
               break;
            case 19:
               ((DeclHandler)handler).externalEntityDecl((String)saxEvent.getParm(0), (String)saxEvent.getParm(1), (String)saxEvent.getParm(2));
               break;
            default:
               throw new SAXException("Unrecognized event: " + saxEvent.event);
         }
      }

   }

   public void processingInstruction(String target, String data) throws SAXException {
      SAXEvent saxEvent = new SAXEvent((byte)1);
      saxEvent.addParm(target);
      saxEvent.addParm(data);
      this.events.add(saxEvent);
   }

   public void startPrefixMapping(String prefix, String uri) throws SAXException {
      SAXEvent saxEvent = new SAXEvent((byte)2);
      saxEvent.addParm(prefix);
      saxEvent.addParm(uri);
      this.events.add(saxEvent);
   }

   public void endPrefixMapping(String prefix) throws SAXException {
      SAXEvent saxEvent = new SAXEvent((byte)3);
      saxEvent.addParm(prefix);
      this.events.add(saxEvent);
   }

   public void startDocument() throws SAXException {
      SAXEvent saxEvent = new SAXEvent((byte)4);
      this.events.add(saxEvent);
   }

   public void endDocument() throws SAXException {
      SAXEvent saxEvent = new SAXEvent((byte)5);
      this.events.add(saxEvent);
   }

   public void startElement(String namespaceURI, String localName, String qualifiedName, Attributes attributes) throws SAXException {
      SAXEvent saxEvent = new SAXEvent((byte)6);
      saxEvent.addParm(namespaceURI);
      saxEvent.addParm(localName);
      saxEvent.addParm(qualifiedName);
      QName qName = null;
      if (namespaceURI != null) {
         qName = new QName(localName, Namespace.get(namespaceURI));
      } else {
         qName = new QName(localName);
      }

      if (attributes != null && attributes.getLength() > 0) {
         List attParmList = new ArrayList(attributes.getLength());
         String[] attParms = null;

         for(int i = 0; i < attributes.getLength(); ++i) {
            String attLocalName = attributes.getLocalName(i);
            if (attLocalName.startsWith("xmlns")) {
               String prefix = null;
               if (attLocalName.length() > 5) {
                  prefix = attLocalName.substring(6);
               } else {
                  prefix = "";
               }

               SAXEvent prefixEvent = new SAXEvent((byte)2);
               prefixEvent.addParm(prefix);
               prefixEvent.addParm(attributes.getValue(i));
               this.events.add(prefixEvent);
               List prefixes = (List)this.prefixMappings.get(qName);
               if (prefixes == null) {
                  prefixes = new ArrayList();
                  this.prefixMappings.put(qName, prefixes);
               }

               prefixes.add(prefix);
            } else {
               attParms = new String[]{attributes.getURI(i), attLocalName, attributes.getQName(i), attributes.getType(i), attributes.getValue(i)};
               attParmList.add(attParms);
            }
         }

         saxEvent.addParm(attParmList);
      }

      this.events.add(saxEvent);
   }

   public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
      SAXEvent saxEvent = new SAXEvent((byte)7);
      saxEvent.addParm(namespaceURI);
      saxEvent.addParm(localName);
      saxEvent.addParm(qName);
      this.events.add(saxEvent);
      QName elementName = null;
      if (namespaceURI != null) {
         elementName = new QName(localName, Namespace.get(namespaceURI));
      } else {
         elementName = new QName(localName);
      }

      List prefixes = (List)this.prefixMappings.get(elementName);
      if (prefixes != null) {
         Iterator itr = prefixes.iterator();

         while(itr.hasNext()) {
            SAXEvent prefixEvent = new SAXEvent((byte)3);
            prefixEvent.addParm(itr.next());
            this.events.add(prefixEvent);
         }
      }

   }

   public void characters(char[] ch, int start, int end) throws SAXException {
      SAXEvent saxEvent = new SAXEvent((byte)8);
      saxEvent.addParm(ch);
      saxEvent.addParm(new Integer(start));
      saxEvent.addParm(new Integer(end));
      this.events.add(saxEvent);
   }

   public void startDTD(String name, String publicId, String systemId) throws SAXException {
      SAXEvent saxEvent = new SAXEvent((byte)9);
      saxEvent.addParm(name);
      saxEvent.addParm(publicId);
      saxEvent.addParm(systemId);
      this.events.add(saxEvent);
   }

   public void endDTD() throws SAXException {
      SAXEvent saxEvent = new SAXEvent((byte)10);
      this.events.add(saxEvent);
   }

   public void startEntity(String name) throws SAXException {
      SAXEvent saxEvent = new SAXEvent((byte)11);
      saxEvent.addParm(name);
      this.events.add(saxEvent);
   }

   public void endEntity(String name) throws SAXException {
      SAXEvent saxEvent = new SAXEvent((byte)12);
      saxEvent.addParm(name);
      this.events.add(saxEvent);
   }

   public void startCDATA() throws SAXException {
      SAXEvent saxEvent = new SAXEvent((byte)13);
      this.events.add(saxEvent);
   }

   public void endCDATA() throws SAXException {
      SAXEvent saxEvent = new SAXEvent((byte)14);
      this.events.add(saxEvent);
   }

   public void comment(char[] ch, int start, int end) throws SAXException {
      SAXEvent saxEvent = new SAXEvent((byte)15);
      saxEvent.addParm(ch);
      saxEvent.addParm(new Integer(start));
      saxEvent.addParm(new Integer(end));
      this.events.add(saxEvent);
   }

   public void elementDecl(String name, String model) throws SAXException {
      SAXEvent saxEvent = new SAXEvent((byte)16);
      saxEvent.addParm(name);
      saxEvent.addParm(model);
      this.events.add(saxEvent);
   }

   public void attributeDecl(String eName, String aName, String type, String valueDefault, String value) throws SAXException {
      SAXEvent saxEvent = new SAXEvent((byte)17);
      saxEvent.addParm(eName);
      saxEvent.addParm(aName);
      saxEvent.addParm(type);
      saxEvent.addParm(valueDefault);
      saxEvent.addParm(value);
      this.events.add(saxEvent);
   }

   public void internalEntityDecl(String name, String value) throws SAXException {
      SAXEvent saxEvent = new SAXEvent((byte)18);
      saxEvent.addParm(name);
      saxEvent.addParm(value);
      this.events.add(saxEvent);
   }

   public void externalEntityDecl(String name, String publicId, String sysId) throws SAXException {
      SAXEvent saxEvent = new SAXEvent((byte)19);
      saxEvent.addParm(name);
      saxEvent.addParm(publicId);
      saxEvent.addParm(sysId);
      this.events.add(saxEvent);
   }

   public void writeExternal(ObjectOutput out) throws IOException {
      if (this.events == null) {
         out.writeByte(2);
      } else {
         out.writeByte(1);
         out.writeObject(this.events);
      }

   }

   public void readExternal(ObjectInput in) throws ClassNotFoundException, IOException {
      if (in.readByte() != 2) {
         this.events = (List)in.readObject();
      }

   }

   static class SAXEvent implements Externalizable {
      public static final long serialVersionUID = 1L;
      static final byte PROCESSING_INSTRUCTION = 1;
      static final byte START_PREFIX_MAPPING = 2;
      static final byte END_PREFIX_MAPPING = 3;
      static final byte START_DOCUMENT = 4;
      static final byte END_DOCUMENT = 5;
      static final byte START_ELEMENT = 6;
      static final byte END_ELEMENT = 7;
      static final byte CHARACTERS = 8;
      static final byte START_DTD = 9;
      static final byte END_DTD = 10;
      static final byte START_ENTITY = 11;
      static final byte END_ENTITY = 12;
      static final byte START_CDATA = 13;
      static final byte END_CDATA = 14;
      static final byte COMMENT = 15;
      static final byte ELEMENT_DECL = 16;
      static final byte ATTRIBUTE_DECL = 17;
      static final byte INTERNAL_ENTITY_DECL = 18;
      static final byte EXTERNAL_ENTITY_DECL = 19;
      protected byte event;
      protected List parms;

      public SAXEvent() {
      }

      SAXEvent(byte event) {
         this.event = event;
      }

      void addParm(Object parm) {
         if (this.parms == null) {
            this.parms = new ArrayList(3);
         }

         this.parms.add(parm);
      }

      Object getParm(int index) {
         return this.parms != null && index < this.parms.size() ? this.parms.get(index) : null;
      }

      public void writeExternal(ObjectOutput out) throws IOException {
         out.writeByte(this.event);
         if (this.parms == null) {
            out.writeByte(2);
         } else {
            out.writeByte(1);
            out.writeObject(this.parms);
         }

      }

      public void readExternal(ObjectInput in) throws ClassNotFoundException, IOException {
         this.event = in.readByte();
         if (in.readByte() != 2) {
            this.parms = (List)in.readObject();
         }

      }
   }
}
