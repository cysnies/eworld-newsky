package org.hibernate.internal.jaxb.mapping.hbm;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(
   name = "generated-attribute"
)
@XmlEnum
public enum JaxbGeneratedAttribute {
   @XmlEnumValue("always")
   ALWAYS("always"),
   @XmlEnumValue("never")
   NEVER("never");

   private final String value;

   private JaxbGeneratedAttribute(String v) {
      this.value = v;
   }

   public String value() {
      return this.value;
   }

   public static JaxbGeneratedAttribute fromValue(String v) {
      for(JaxbGeneratedAttribute c : values()) {
         if (c.value.equals(v)) {
            return c;
         }
      }

      throw new IllegalArgumentException(v);
   }
}
