package org.hibernate.internal.jaxb.mapping.orm;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(
   name = "inheritance-type"
)
@XmlEnum
public enum JaxbInheritanceType {
   SINGLE_TABLE,
   JOINED,
   TABLE_PER_CLASS;

   public String value() {
      return this.name();
   }

   public static JaxbInheritanceType fromValue(String v) {
      return valueOf(v);
   }
}
