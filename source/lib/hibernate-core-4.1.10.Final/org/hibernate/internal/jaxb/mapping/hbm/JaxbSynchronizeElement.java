package org.hibernate.internal.jaxb.mapping.hbm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "synchronize-element"
)
public class JaxbSynchronizeElement {
   @XmlAttribute(
      required = true
   )
   protected String table;

   public String getTable() {
      return this.table;
   }

   public void setTable(String value) {
      this.table = value;
   }
}
