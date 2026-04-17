package org.hibernate.internal.jaxb.mapping.hbm;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(
   name = "cache-mode-attribute"
)
@XmlEnum
public enum JaxbCacheModeAttribute {
   @XmlEnumValue("get")
   GET("get"),
   @XmlEnumValue("ignore")
   IGNORE("ignore"),
   @XmlEnumValue("normal")
   NORMAL("normal"),
   @XmlEnumValue("put")
   PUT("put"),
   @XmlEnumValue("refresh")
   REFRESH("refresh");

   private final String value;

   private JaxbCacheModeAttribute(String v) {
      this.value = v;
   }

   public String value() {
      return this.value;
   }

   public static JaxbCacheModeAttribute fromValue(String v) {
      for(JaxbCacheModeAttribute c : values()) {
         if (c.value.equals(v)) {
            return c;
         }
      }

      throw new IllegalArgumentException(v);
   }
}
