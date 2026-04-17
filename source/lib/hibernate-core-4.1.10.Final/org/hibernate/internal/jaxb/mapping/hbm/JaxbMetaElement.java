package org.hibernate.internal.jaxb.mapping.hbm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "meta-element",
   propOrder = {"value"}
)
public class JaxbMetaElement {
   @XmlValue
   protected String value;
   @XmlAttribute(
      required = true
   )
   protected String attribute;
   @XmlAttribute
   protected Boolean inherit;

   public String getValue() {
      return this.value;
   }

   public void setValue(String value) {
      this.value = value;
   }

   public String getAttribute() {
      return this.attribute;
   }

   public void setAttribute(String value) {
      this.attribute = value;
   }

   public boolean isInherit() {
      return this.inherit == null ? true : this.inherit;
   }

   public void setInherit(Boolean value) {
      this.inherit = value;
   }
}
