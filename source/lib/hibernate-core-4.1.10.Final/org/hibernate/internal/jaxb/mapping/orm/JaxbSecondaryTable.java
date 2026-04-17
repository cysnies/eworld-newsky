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
   name = "secondary-table",
   propOrder = {"primaryKeyJoinColumn", "uniqueConstraint"}
)
public class JaxbSecondaryTable {
   @XmlElement(
      name = "primary-key-join-column"
   )
   protected List primaryKeyJoinColumn;
   @XmlElement(
      name = "unique-constraint"
   )
   protected List uniqueConstraint;
   @XmlAttribute(
      required = true
   )
   protected String name;
   @XmlAttribute
   protected String catalog;
   @XmlAttribute
   protected String schema;

   public List getPrimaryKeyJoinColumn() {
      if (this.primaryKeyJoinColumn == null) {
         this.primaryKeyJoinColumn = new ArrayList();
      }

      return this.primaryKeyJoinColumn;
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
