package org.hibernate.internal.jaxb.mapping.hbm;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(
   name = "lazy-attribute-with-no-proxy"
)
@XmlEnum
public enum JaxbLazyAttributeWithNoProxy {
   @XmlEnumValue("false")
   FALSE("false"),
   @XmlEnumValue("no-proxy")
   NO_PROXY("no-proxy"),
   @XmlEnumValue("proxy")
   PROXY("proxy");

   private final String value;

   private JaxbLazyAttributeWithNoProxy(String v) {
      this.value = v;
   }

   public String value() {
      return this.value;
   }

   public static JaxbLazyAttributeWithNoProxy fromValue(String v) {
      for(JaxbLazyAttributeWithNoProxy c : values()) {
         if (c.value.equals(v)) {
            return c;
         }
      }

      throw new IllegalArgumentException(v);
   }
}
