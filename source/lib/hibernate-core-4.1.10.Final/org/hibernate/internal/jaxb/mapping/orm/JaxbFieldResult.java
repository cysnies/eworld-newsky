package org.hibernate.internal.jaxb.mapping.orm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "field-result"
)
public class JaxbFieldResult {
   @XmlAttribute(
      required = true
   )
   protected String name;
   @XmlAttribute(
      required = true
   )
   protected String column;

   public String getName() {
      return this.name;
   }

   public void setName(String value) {
      this.name = value;
   }

   public String getColumn() {
      return this.column;
   }

   public void setColumn(String value) {
      this.column = value;
   }
}
