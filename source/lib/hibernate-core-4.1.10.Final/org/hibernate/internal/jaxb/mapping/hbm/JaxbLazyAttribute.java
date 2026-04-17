package org.hibernate.internal.jaxb.mapping.hbm;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(
   name = "lazy-attribute"
)
@XmlEnum
public enum JaxbLazyAttribute {
   @XmlEnumValue("false")
   FALSE("false"),
   @XmlEnumValue("proxy")
   PROXY("proxy");

   private final String value;

   private JaxbLazyAttribute(String v) {
      this.value = v;
   }

   public String value() {
      return this.value;
   }

   public static JaxbLazyAttribute fromValue(String v) {
      for(JaxbLazyAttribute c : values()) {
         if (c.value.equals(v)) {
            return c;
         }
      }

      throw new IllegalArgumentException(v);
   }
}
