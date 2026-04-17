package org.hibernate.internal.jaxb.mapping.hbm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "alias-table",
   propOrder = {"value"}
)
public class JaxbAliasTable {
   @XmlValue
   protected String value;
   @XmlAttribute(
      required = true
   )
   protected String alias;
   @XmlAttribute(
      required = true
   )
   protected String table;

   public String getValue() {
      return this.value;
   }

   public void setValue(String value) {
      this.value = value;
   }

   public String getAlias() {
      return this.alias;
   }

   public void setAlias(String value) {
      this.alias = value;
   }

   public String getTable() {
      return this.table;
   }

   public void setTable(String value) {
      this.table = value;
   }
}
