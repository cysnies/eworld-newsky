package org.hibernate.internal.jaxb.mapping.hbm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "list-index-element",
   propOrder = {"column"}
)
public class JaxbListIndexElement {
   protected JaxbColumnElement column;
   @XmlAttribute
   protected String base;
   @XmlAttribute(
      name = "column"
   )
   protected String columnAttribute;

   public JaxbColumnElement getColumn() {
      return this.column;
   }

   public void setColumn(JaxbColumnElement value) {
      this.column = value;
   }

   public String getBase() {
      return this.base == null ? "0" : this.base;
   }

   public void setBase(String value) {
      this.base = value;
   }

   public String getColumnAttribute() {
      return this.columnAttribute;
   }

   public void setColumnAttribute(String value) {
      this.columnAttribute = value;
   }
}
