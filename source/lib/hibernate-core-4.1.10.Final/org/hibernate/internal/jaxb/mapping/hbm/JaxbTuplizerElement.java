package org.hibernate.internal.jaxb.mapping.hbm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "tuplizer-element"
)
public class JaxbTuplizerElement {
   @XmlAttribute(
      name = "class",
      required = true
   )
   protected String clazz;
   @XmlAttribute(
      name = "entity-mode"
   )
   @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
   protected String entityMode;

   public String getClazz() {
      return this.clazz;
   }

   public void setClazz(String value) {
      this.clazz = value;
   }

   public String getEntityMode() {
      return this.entityMode;
   }

   public void setEntityMode(String value) {
      this.entityMode = value;
   }
}
