package org.hibernate.internal.jaxb.mapping.hbm;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(
   name = "fetch-attribute-with-subselect"
)
@XmlEnum
public enum JaxbFetchAttributeWithSubselect {
   @XmlEnumValue("join")
   JOIN("join"),
   @XmlEnumValue("select")
   SELECT("select"),
   @XmlEnumValue("subselect")
   SUBSELECT("subselect");

   private final String value;

   private JaxbFetchAttributeWithSubselect(String v) {
      this.value = v;
   }

   public String value() {
      return this.value;
   }

   public static JaxbFetchAttributeWithSubselect fromValue(String v) {
      for(JaxbFetchAttributeWithSubselect c : values()) {
         if (c.value.equals(v)) {
            return c;
         }
      }

      throw new IllegalArgumentException(v);
   }
}
