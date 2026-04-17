package org.hibernate.internal.jaxb.mapping.orm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "order-column"
)
public class JaxbOrderColumn {
   @XmlAttribute
   protected String name;
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

   public String getName() {
      return this.name;
   }

   public void setName(String value) {
      this.name = value;
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
}
