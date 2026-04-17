package org.hibernate.internal.jaxb.mapping.orm;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(
   name = "temporal-type"
)
@XmlEnum
public enum JaxbTemporalType {
   DATE,
   TIME,
   TIMESTAMP;

   public String value() {
      return this.name();
   }

   public static JaxbTemporalType fromValue(String v) {
      return valueOf(v);
   }
}
