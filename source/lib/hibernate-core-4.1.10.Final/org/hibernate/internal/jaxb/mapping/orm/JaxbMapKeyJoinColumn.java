package org.hibernate.internal.jaxb.mapping.orm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "map-key-join-column"
)
public class JaxbMapKeyJoinColumn {
   @XmlAttribute
   protected String name;
   @XmlAttribute(
      name = "referenced-column-name"
   )
   protected String referencedColumnName;
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

   public String getName() {
      return this.name;
   }

   public void setName(String value) {
      this.name = value;
   }

   public String getReferencedColumnName() {
      return this.referencedColumnName;
   }

   public void setReferencedColumnName(String value) {
      this.referencedColumnName = value;
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
}
