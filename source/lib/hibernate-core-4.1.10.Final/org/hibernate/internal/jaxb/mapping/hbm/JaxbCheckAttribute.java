package org.hibernate.internal.jaxb.mapping.hbm;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(
   name = "check-attribute"
)
@XmlEnum
public enum JaxbCheckAttribute {
   @XmlEnumValue("none")
   NONE("none"),
   @XmlEnumValue("param")
   PARAM("param"),
   @XmlEnumValue("rowcount")
   ROWCOUNT("rowcount");

   private final String value;

   private JaxbCheckAttribute(String v) {
      this.value = v;
   }

   public String value() {
      return this.value;
   }

   public static JaxbCheckAttribute fromValue(String v) {
      for(JaxbCheckAttribute c : values()) {
         if (c.value.equals(v)) {
            return c;
         }
      }

      throw new IllegalArgumentException(v);
   }
}
