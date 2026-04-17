package org.hibernate.internal.jaxb.mapping.hbm;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(
   name = "not-found-attribute"
)
@XmlEnum
public enum JaxbNotFoundAttribute {
   @XmlEnumValue("exception")
   EXCEPTION("exception"),
   @XmlEnumValue("ignore")
   IGNORE("ignore");

   private final String value;

   private JaxbNotFoundAttribute(String v) {
      this.value = v;
   }

   public String value() {
      return this.value;
   }

   public static JaxbNotFoundAttribute fromValue(String v) {
      for(JaxbNotFoundAttribute c : values()) {
         if (c.value.equals(v)) {
            return c;
         }
      }

      throw new IllegalArgumentException(v);
   }
}
