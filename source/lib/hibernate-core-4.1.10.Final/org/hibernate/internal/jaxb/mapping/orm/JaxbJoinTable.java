package org.hibernate.internal.jaxb.mapping.orm;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "join-table",
   propOrder = {"joinColumn", "inverseJoinColumn", "uniqueConstraint"}
)
public class JaxbJoinTable {
   @XmlElement(
      name = "join-column"
   )
   protected List joinColumn;
   @XmlElement(
      name = "inverse-join-column"
   )
   protected List inverseJoinColumn;
   @XmlElement(
      name = "unique-constraint"
   )
   protected List uniqueConstraint;
   @XmlAttribute
   protected String name;
   @XmlAttribute
   protected String catalog;
   @XmlAttribute
   protected String schema;

   public List getJoinColumn() {
      if (this.joinColumn == null) {
         this.joinColumn = new ArrayList();
      }

      return this.joinColumn;
   }

   public List getInverseJoinColumn() {
      if (this.inverseJoinColumn == null) {
         this.inverseJoinColumn = new ArrayList();
      }

      return this.inverseJoinColumn;
   }

   public List getUniqueConstraint() {
      if (this.uniqueConstraint == null) {
         this.uniqueConstraint = new ArrayList();
      }

      return this.uniqueConstraint;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String value) {
      this.name = value;
   }

   public String getCatalog() {
      return this.catalog;
   }

   public void setCatalog(String value) {
      this.catalog = value;
   }

   public String getSchema() {
      return this.schema;
   }

   public void setSchema(String value) {
      this.schema = value;
   }
}
