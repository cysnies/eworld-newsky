package org.hibernate.internal.jaxb.mapping.orm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "basic",
   propOrder = {"column", "lob", "temporal", "enumerated"}
)
public class JaxbBasic {
   protected JaxbColumn column;
   protected JaxbLob lob;
   protected JaxbTemporalType temporal;
   protected JaxbEnumType enumerated;
   @XmlAttribute(
      required = true
   )
   protected String name;
   @XmlAttribute
   protected JaxbFetchType fetch;
   @XmlAttribute
   protected Boolean optional;
   @XmlAttribute
   protected JaxbAccessType access;

   public JaxbColumn getColumn() {
      return this.column;
   }

   public void setColumn(JaxbColumn value) {
      this.column = value;
   }

   public JaxbLob getLob() {
      return this.lob;
   }

   public void setLob(JaxbLob value) {
      this.lob = value;
   }

   public JaxbTemporalType getTemporal() {
      return this.temporal;
   }

   public void setTemporal(JaxbTemporalType value) {
      this.temporal = value;
   }

   public JaxbEnumType getEnumerated() {
      return this.enumerated;
   }

   public void setEnumerated(JaxbEnumType value) {
      this.enumerated = value;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String value) {
      this.name = value;
   }

   public JaxbFetchType getFetch() {
      return this.fetch;
   }

   public void setFetch(JaxbFetchType value) {
      this.fetch = value;
   }

   public Boolean isOptional() {
      return this.optional;
   }

   public void setOptional(Boolean value) {
      this.optional = value;
   }

   public JaxbAccessType getAccess() {
      return this.access;
   }

   public void setAccess(JaxbAccessType value) {
      this.access = value;
   }
}
