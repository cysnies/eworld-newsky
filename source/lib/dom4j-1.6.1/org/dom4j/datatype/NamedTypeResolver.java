package org.dom4j.datatype;

import com.sun.msv.datatype.xsd.XSDatatype;
import java.util.HashMap;
import java.util.Map;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;

class NamedTypeResolver {
   protected Map complexTypeMap = new HashMap();
   protected Map simpleTypeMap = new HashMap();
   protected Map typedElementMap = new HashMap();
   protected Map elementFactoryMap = new HashMap();
   protected DocumentFactory documentFactory;

   NamedTypeResolver(DocumentFactory documentFactory) {
      this.documentFactory = documentFactory;
   }

   void registerComplexType(QName type, DocumentFactory factory) {
      this.complexTypeMap.put(type, factory);
   }

   void registerSimpleType(QName type, XSDatatype datatype) {
      this.simpleTypeMap.put(type, datatype);
   }

   void registerTypedElement(Element element, QName type, DocumentFactory parentFactory) {
      this.typedElementMap.put(element, type);
      this.elementFactoryMap.put(element, parentFactory);
   }

   void resolveElementTypes() {
      for(Element element : this.typedElementMap.keySet()) {
         QName elementQName = this.getQNameOfSchemaElement(element);
         QName type = (QName)this.typedElementMap.get(element);
         if (this.complexTypeMap.containsKey(type)) {
            DocumentFactory factory = (DocumentFactory)this.complexTypeMap.get(type);
            elementQName.setDocumentFactory(factory);
         } else if (this.simpleTypeMap.containsKey(type)) {
            XSDatatype datatype = (XSDatatype)this.simpleTypeMap.get(type);
            DocumentFactory factory = (DocumentFactory)this.elementFactoryMap.get(element);
            if (factory instanceof DatatypeElementFactory) {
               ((DatatypeElementFactory)factory).setChildElementXSDatatype(elementQName, datatype);
            }
         }
      }

   }

   void resolveNamedTypes() {
      this.resolveElementTypes();
   }

   private QName getQNameOfSchemaElement(Element element) {
      String name = element.attributeValue("name");
      return this.getQName(name);
   }

   private QName getQName(String name) {
      return this.documentFactory.createQName(name);
   }
}
