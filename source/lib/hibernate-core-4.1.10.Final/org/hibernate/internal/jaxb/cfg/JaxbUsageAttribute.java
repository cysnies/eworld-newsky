package org.hibernate.internal.jaxb.cfg;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(
   name = "usage-attribute"
)
@XmlEnum
public enum JaxbUsageAttribute {
   @XmlEnumValue("nonstrict-read-write")
   NONSTRICT_READ_WRITE("nonstrict-read-write"),
   @XmlEnumValue("read-only")
   READ_ONLY("read-only"),
   @XmlEnumValue("read-write")
   READ_WRITE("read-write"),
   @XmlEnumValue("transactional")
   TRANSACTIONAL("transactional");

   private final String value;

   private JaxbUsageAttribute(String v) {
      this.value = v;
   }

   public String value() {
      return this.value;
   }

   public static JaxbUsageAttribute fromValue(String v) {
      for(JaxbUsageAttribute c : values()) {
         if (c.value.equals(v)) {
            return c;
         }
      }

      throw new IllegalArgumentException(v);
   }
}
