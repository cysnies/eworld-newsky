package org.hibernate.internal.jaxb.mapping.hbm;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "key-many-to-one-element",
   propOrder = {"meta", "column"}
)
public class JaxbKeyManyToOneElement {
   protected List meta;
   protected List column;
   @XmlAttribute
   protected String access;
   @XmlAttribute(
      name = "class"
   )
   protected String clazz;
   @XmlAttribute(
      name = "column"
   )
   protected String columnAttribute;
   @XmlAttribute(
      name = "entity-name"
   )
   protected String entityName;
   @XmlAttribute(
      name = "foreign-key"
   )
   protected String foreignKey;
   @XmlAttribute
   protected JaxbLazyAttribute lazy;
   @XmlAttribute(
      required = true
   )
   protected String name;

   public List getMeta() {
      if (this.meta == null) {
         this.meta = new ArrayList();
      }

      return this.meta;
   }

   public List getColumn() {
      if (this.column == null) {
         this.column = new ArrayList();
      }

      return this.column;
   }

   public String getAccess() {
      return this.access;
   }

   public void setAccess(String value) {
      this.access = value;
   }

   public String getClazz() {
      return this.clazz;
   }

   public void setClazz(String value) {
      this.clazz = value;
   }

   public String getColumnAttribute() {
      return this.columnAttribute;
   }

   public void setColumnAttribute(String value) {
      this.columnAttribute = value;
   }

   public String getEntityName() {
      return this.entityName;
   }

   public void setEntityName(String value) {
      this.entityName = value;
   }

   public String getForeignKey() {
      return this.foreignKey;
   }

   public void setForeignKey(String value) {
      this.foreignKey = value;
   }

   public JaxbLazyAttribute getLazy() {
      return this.lazy;
   }

   public void setLazy(JaxbLazyAttribute value) {
      this.lazy = value;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String value) {
      this.name = value;
   }
}
