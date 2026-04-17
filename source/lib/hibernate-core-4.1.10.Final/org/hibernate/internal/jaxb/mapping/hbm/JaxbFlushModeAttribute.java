package org.hibernate.internal.jaxb.mapping.hbm;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(
   name = "flush-mode-attribute"
)
@XmlEnum
public enum JaxbFlushModeAttribute {
   @XmlEnumValue("always")
   ALWAYS("always"),
   @XmlEnumValue("auto")
   AUTO("auto"),
   @XmlEnumValue("never")
   NEVER("never");

   private final String value;

   private JaxbFlushModeAttribute(String v) {
      this.value = v;
   }

   public String value() {
      return this.value;
   }

   public static JaxbFlushModeAttribute fromValue(String v) {
      for(JaxbFlushModeAttribute c : values()) {
         if (c.value.equals(v)) {
            return c;
         }
      }

      throw new IllegalArgumentException(v);
   }
}
