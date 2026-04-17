package org.hibernate.internal.jaxb.mapping.orm;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(
   name = "access-type"
)
@XmlEnum
public enum JaxbAccessType {
   PROPERTY,
   FIELD;

   public String value() {
      return this.name();
   }

   public static JaxbAccessType fromValue(String v) {
      return valueOf(v);
   }
}
