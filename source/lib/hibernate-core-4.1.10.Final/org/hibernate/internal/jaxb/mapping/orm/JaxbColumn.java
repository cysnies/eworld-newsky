package org.hibernate.internal.jaxb.mapping.orm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "column"
)
public class JaxbColumn {
   @XmlAttribute
   protected String name;
   @XmlAttribute
   protected Boolean unique;
   @XmlAttribute
   protected Boolean nullable;
   @XmlAttribute
   protected Boolean insertable;
   @XmlAttribute
   protected Boolean updatable;
   @XmlAttribute(
      name = "column-definition"
   )
   protected String columnDefinition;
   @XmlAttribute
   protected String table;
   @XmlAttribute
   protected Integer length;
   @XmlAttribute
   protected Integer precision;
   @XmlAttribute
   protected Integer scale;

   public String getName() {
      return this.name;
   }

   public void setName(String value) {
      this.name = value;
   }

   public Boolean isUnique() {
      return this.unique;
   }

   public void setUnique(Boolean value) {
      this.unique = value;
   }

   public Boolean isNullable() {
      return this.nullable;
   }

   public void setNullable(Boolean value) {
      this.nullable = value;
   }

   public Boolean isInsertable() {
      return this.insertable;
   }

   public void setInsertable(Boolean value) {
      this.insertable = value;
   }

   public Boolean isUpdatable() {
      return this.updatable;
   }

   public void setUpdatable(Boolean value) {
      this.updatable = value;
   }

   public String getColumnDefinition() {
      return this.columnDefinition;
   }

   public void setColumnDefinition(String value) {
      this.columnDefinition = value;
   }

   public String getTable() {
      return this.table;
   }

   public void setTable(String value) {
      this.table = value;
   }

   public Integer getLength() {
      return this.length;
   }

   public void setLength(Integer value) {
      this.length = value;
   }

   public Integer getPrecision() {
      return this.precision;
   }

   public void setPrecision(Integer value) {
      this.precision = value;
   }

   public Integer getScale() {
      return this.scale;
   }

   public void setScale(Integer value) {
      this.scale = value;
   }
}
