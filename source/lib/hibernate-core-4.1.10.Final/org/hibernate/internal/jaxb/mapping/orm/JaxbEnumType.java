package org.hibernate.internal.jaxb.mapping.orm;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(
   name = "enum-type"
)
@XmlEnum
public enum JaxbEnumType {
   ORDINAL,
   STRING;

   public String value() {
      return this.name();
   }

   public static JaxbEnumType fromValue(String v) {
      return valueOf(v);
   }
}
