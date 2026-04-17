package org.dom4j.datatype;

import com.sun.msv.datatype.xsd.XSDatatype;
import java.util.HashMap;
import java.util.Map;
import org.dom4j.Attribute;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;

public class DatatypeElementFactory extends DocumentFactory {
   private QName elementQName;
   private Map attributeXSDatatypes = new HashMap();
   private Map childrenXSDatatypes = new HashMap();

   public DatatypeElementFactory(QName elementQName) {
      this.elementQName = elementQName;
   }

   public QName getQName() {
      return this.elementQName;
   }

   public XSDatatype getAttributeXSDatatype(QName attributeQName) {
      return (XSDatatype)this.attributeXSDatatypes.get(attributeQName);
   }

   public void setAttributeXSDatatype(QName attributeQName, XSDatatype type) {
      this.attributeXSDatatypes.put(attributeQName, type);
   }

   public XSDatatype getChildElementXSDatatype(QName qname) {
      return (XSDatatype)this.childrenXSDatatypes.get(qname);
   }

   public void setChildElementXSDatatype(QName qname, XSDatatype dataType) {
      this.childrenXSDatatypes.put(qname, dataType);
   }

   public Element createElement(QName qname) {
      XSDatatype dataType = this.getChildElementXSDatatype(qname);
      if (dataType != null) {
         return new DatatypeElement(qname, dataType);
      } else {
         DocumentFactory factory = qname.getDocumentFactory();
         if (factory instanceof DatatypeElementFactory) {
            DatatypeElementFactory dtFactory = (DatatypeElementFactory)factory;
            dataType = dtFactory.getChildElementXSDatatype(qname);
            if (dataType != null) {
               return new DatatypeElement(qname, dataType);
            }
         }

         return super.createElement(qname);
      }
   }

   public Attribute createAttribute(Element owner, QName qname, String value) {
      XSDatatype dataType = this.getAttributeXSDatatype(qname);
      return (Attribute)(dataType == null ? super.createAttribute(owner, qname, value) : new DatatypeAttribute(qname, dataType, value));
   }
}
