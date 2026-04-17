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
   name = "table-generator",
   propOrder = {"description", "uniqueConstraint"}
)
public class JaxbTableGenerator {
   protected String description;
   @XmlElement(
      name = "unique-constraint"
   )
   protected List uniqueConstraint;
   @XmlAttribute(
      required = true
   )
   protected String name;
   @XmlAttribute
   protected String table;
   @XmlAttribute
   protected String catalog;
   @XmlAttribute
   protected String schema;
   @XmlAttribute(
      name = "pk-column-name"
   )
   protected String pkColumnName;
   @XmlAttribute(
      name = "value-column-name"
   )
   protected String valueColumnName;
   @XmlAttribute(
      name = "pk-column-value"
   )
   protected String pkColumnValue;
   @XmlAttribute(
      name = "initial-value"
   )
   protected Integer initialValue;
   @XmlAttribute(
      name = "allocation-size"
   )
   protected Integer allocationSize;

   public String getDescription() {
      return this.description;
   }

   public void setDescription(String value) {
      this.description = value;
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

   public String getTable() {
      return this.table;
   }

   public void setTable(String value) {
      this.table = value;
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

   public String getPkColumnName() {
      return this.pkColumnName;
   }

   public void setPkColumnName(String value) {
      this.pkColumnName = value;
   }

   public String getValueColumnName() {
      return this.valueColumnName;
   }

   public void setValueColumnName(String value) {
      this.valueColumnName = value;
   }

   public String getPkColumnValue() {
      return this.pkColumnValue;
   }

   public void setPkColumnValue(String value) {
      this.pkColumnValue = value;
   }

   public Integer getInitialValue() {
      return this.initialValue;
   }

   public void setInitialValue(Integer value) {
      this.initialValue = value;
   }

   public Integer getAllocationSize() {
      return this.allocationSize;
   }

   public void setAllocationSize(Integer value) {
      this.allocationSize = value;
   }
}
