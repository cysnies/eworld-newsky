package org.hibernate.internal.jaxb.mapping.hbm;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(
   name = "lock-mode-attribute"
)
@XmlEnum
public enum JaxbLockModeAttribute {
   @XmlEnumValue("none")
   NONE("none"),
   @XmlEnumValue("read")
   READ("read"),
   @XmlEnumValue("upgrade")
   UPGRADE("upgrade"),
   @XmlEnumValue("upgrade-nowait")
   UPGRADE_NOWAIT("upgrade-nowait"),
   @XmlEnumValue("write")
   WRITE("write");

   private final String value;

   private JaxbLockModeAttribute(String v) {
      this.value = v;
   }

   public String value() {
      return this.value;
   }

   public static JaxbLockModeAttribute fromValue(String v) {
      for(JaxbLockModeAttribute c : values()) {
         if (c.value.equals(v)) {
            return c;
         }
      }

      throw new IllegalArgumentException(v);
   }
}
