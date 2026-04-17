package org.hibernate.internal.jaxb.mapping.hbm;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(
   name = "outer-join-attribute"
)
@XmlEnum
public enum JaxbOuterJoinAttribute {
   @XmlEnumValue("auto")
   AUTO("auto"),
   @XmlEnumValue("false")
   FALSE("false"),
   @XmlEnumValue("true")
   TRUE("true");

   private final String value;

   private JaxbOuterJoinAttribute(String v) {
      this.value = v;
   }

   public String value() {
      return this.value;
   }

   public static JaxbOuterJoinAttribute fromValue(String v) {
      for(JaxbOuterJoinAttribute c : values()) {
         if (c.value.equals(v)) {
            return c;
         }
      }

      throw new IllegalArgumentException(v);
   }
}
