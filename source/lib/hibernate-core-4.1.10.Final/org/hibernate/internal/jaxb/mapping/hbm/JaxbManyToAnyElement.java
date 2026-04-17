package org.hibernate.internal.jaxb.mapping.hbm;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "many-to-any-element",
   propOrder = {"metaValue", "column"}
)
public class JaxbManyToAnyElement {
   @XmlElement(
      name = "meta-value"
   )
   protected List metaValue;
   @XmlElement(
      required = true
   )
   protected JaxbColumnElement column;
   @XmlAttribute(
      name = "id-type",
      required = true
   )
   protected String idType;
   @XmlAttribute(
      name = "meta-type"
   )
   protected String metaType;

   public List getMetaValue() {
      if (this.metaValue == null) {
         this.metaValue = new ArrayList();
      }

      return this.metaValue;
   }

   public JaxbColumnElement getColumn() {
      return this.column;
   }

   public void setColumn(JaxbColumnElement value) {
      this.column = value;
   }

   public String getIdType() {
      return this.idType;
   }

   public void setIdType(String value) {
      this.idType = value;
   }

   public String getMetaType() {
      return this.metaType;
   }

   public void setMetaType(String value) {
      this.metaType = value;
   }
}
