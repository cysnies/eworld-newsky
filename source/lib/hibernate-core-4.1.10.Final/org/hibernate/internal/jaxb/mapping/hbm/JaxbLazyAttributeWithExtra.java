package org.hibernate.internal.jaxb.mapping.hbm;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(
   name = "lazy-attribute-with-extra"
)
@XmlEnum
public enum JaxbLazyAttributeWithExtra {
   @XmlEnumValue("extra")
   EXTRA("extra"),
   @XmlEnumValue("false")
   FALSE("false"),
   @XmlEnumValue("true")
   TRUE("true");

   private final String value;

   private JaxbLazyAttributeWithExtra(String v) {
      this.value = v;
   }

   public String value() {
      return this.value;
   }

   public static JaxbLazyAttributeWithExtra fromValue(String v) {
      for(JaxbLazyAttributeWithExtra c : values()) {
         if (c.value.equals(v)) {
            return c;
         }
      }

      throw new IllegalArgumentException(v);
   }
}
