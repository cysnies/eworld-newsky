package org.hibernate.internal.jaxb.mapping.orm;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(
   name = "discriminator-type"
)
@XmlEnum
public enum JaxbDiscriminatorType {
   STRING,
   CHAR,
   INTEGER;

   public String value() {
      return this.name();
   }

   public static JaxbDiscriminatorType fromValue(String v) {
      return valueOf(v);
   }
}
