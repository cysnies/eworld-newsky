package org.hibernate.internal.jaxb.mapping.hbm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "sql-delete-element",
   propOrder = {"value"}
)
public class JaxbSqlDeleteElement implements CustomSqlElement {
   @XmlValue
   protected String value;
   @XmlAttribute
   protected Boolean callable;
   @XmlAttribute
   protected JaxbCheckAttribute check;

   public String getValue() {
      return this.value;
   }

   public void setValue(String value) {
      this.value = value;
   }

   public boolean isCallable() {
      return this.callable == null ? false : this.callable;
   }

   public void setCallable(Boolean value) {
      this.callable = value;
   }

   public JaxbCheckAttribute getCheck() {
      return this.check;
   }

   public void setCheck(JaxbCheckAttribute value) {
      this.check = value;
   }
}
