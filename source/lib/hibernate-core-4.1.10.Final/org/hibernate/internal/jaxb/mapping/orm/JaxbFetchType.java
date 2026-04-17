package org.hibernate.internal.jaxb.mapping.orm;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(
   name = "fetch-type"
)
@XmlEnum
public enum JaxbFetchType {
   LAZY,
   EAGER;

   public String value() {
      return this.name();
   }

   public static JaxbFetchType fromValue(String v) {
      return valueOf(v);
   }
}
