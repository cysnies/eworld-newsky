package org.hibernate.internal.jaxb.mapping.orm;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(
   name = "lock-mode-type"
)
@XmlEnum
public enum JaxbLockModeType {
   READ,
   WRITE,
   OPTIMISTIC,
   OPTIMISTIC_FORCE_INCREMENT,
   PESSIMISTIC_READ,
   PESSIMISTIC_WRITE,
   PESSIMISTIC_FORCE_INCREMENT,
   NONE;

   public String value() {
      return this.name();
   }

   public static JaxbLockModeType fromValue(String v) {
      return valueOf(v);
   }
}
