package org.hibernate.internal.jaxb.mapping.hbm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "return-scalar-element"
)
public class JaxbReturnScalarElement {
   @XmlAttribute(
      required = true
   )
   protected String column;
   @XmlAttribute
   protected String type;

   public String getColumn() {
      return this.column;
   }

   public void setColumn(String value) {
      this.column = value;
   }

   public String getType() {
      return this.type;
   }

   public void setType(String value) {
      this.type = value;
   }
}
