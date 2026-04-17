package org.hibernate.internal.jaxb.mapping.orm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "version",
   propOrder = {"column", "temporal"}
)
public class JaxbVersion {
   protected JaxbColumn column;
   protected JaxbTemporalType temporal;
   @XmlAttribute(
      required = true
   )
   protected String name;
   @XmlAttribute
   protected JaxbAccessType access;

   public JaxbColumn getColumn() {
      return this.column;
   }

   public void setColumn(JaxbColumn value) {
      this.column = value;
   }

   public JaxbTemporalType getTemporal() {
      return this.temporal;
   }

   public void setTemporal(JaxbTemporalType value) {
      this.temporal = value;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String value) {
      this.name = value;
   }

   public JaxbAccessType getAccess() {
      return this.access;
   }

   public void setAccess(JaxbAccessType value) {
      this.access = value;
   }
}
