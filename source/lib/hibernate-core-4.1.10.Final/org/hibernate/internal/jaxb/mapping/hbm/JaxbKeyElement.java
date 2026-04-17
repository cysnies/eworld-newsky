package org.hibernate.internal.jaxb.mapping.hbm;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "key-element",
   propOrder = {"column"}
)
public class JaxbKeyElement {
   protected List column;
   @XmlAttribute(
      name = "column"
   )
   protected String columnAttribute;
   @XmlAttribute(
      name = "foreign-key"
   )
   protected String foreignKey;
   @XmlAttribute(
      name = "not-null"
   )
   protected Boolean notNull;
   @XmlAttribute(
      name = "on-delete"
   )
   @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
   protected String onDelete;
   @XmlAttribute(
      name = "property-ref"
   )
   protected String propertyRef;
   @XmlAttribute
   protected Boolean unique;
   @XmlAttribute
   protected Boolean update;

   public List getColumn() {
      if (this.column == null) {
         this.column = new ArrayList();
      }

      return this.column;
   }

   public String getColumnAttribute() {
      return this.columnAttribute;
   }

   public void setColumnAttribute(String value) {
      this.columnAttribute = value;
   }

   public String getForeignKey() {
      return this.foreignKey;
   }

   public void setForeignKey(String value) {
      this.foreignKey = value;
   }

   public Boolean isNotNull() {
      return this.notNull;
   }

   public void setNotNull(Boolean value) {
      this.notNull = value;
   }

   public String getOnDelete() {
      return this.onDelete == null ? "noaction" : this.onDelete;
   }

   public void setOnDelete(String value) {
      this.onDelete = value;
   }

   public String getPropertyRef() {
      return this.propertyRef;
   }

   public void setPropertyRef(String value) {
      this.propertyRef = value;
   }

   public Boolean isUnique() {
      return this.unique;
   }

   public void setUnique(Boolean value) {
      this.unique = value;
   }

   public Boolean isUpdate() {
      return this.update;
   }

   public void setUpdate(Boolean value) {
      this.update = value;
   }
}
