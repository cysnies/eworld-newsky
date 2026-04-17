package org.hibernate.internal.jaxb.mapping.hbm;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(
   name = "fetch-attribute"
)
@XmlEnum
public enum JaxbFetchAttribute {
   @XmlEnumValue("join")
   JOIN("join"),
   @XmlEnumValue("select")
   SELECT("select");

   private final String value;

   private JaxbFetchAttribute(String v) {
      this.value = v;
   }

   public String value() {
      return this.value;
   }

   public static JaxbFetchAttribute fromValue(String v) {
      for(JaxbFetchAttribute c : values()) {
         if (c.value.equals(v)) {
            return c;
         }
      }

      throw new IllegalArgumentException(v);
   }
}
