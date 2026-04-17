package org.hibernate.internal.jaxb.cfg;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "listener-element"
)
public class JaxbListenerElement {
   @XmlAttribute(
      name = "class",
      required = true
   )
   protected String clazz;
   @XmlAttribute
   protected JaxbTypeAttribute type;

   public String getClazz() {
      return this.clazz;
   }

   public void setClazz(String value) {
      this.clazz = value;
   }

   public JaxbTypeAttribute getType() {
      return this.type;
   }

   public void setType(JaxbTypeAttribute value) {
      this.type = value;
   }
}
