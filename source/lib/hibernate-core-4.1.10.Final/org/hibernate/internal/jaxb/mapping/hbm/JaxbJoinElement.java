package org.hibernate.internal.jaxb.mapping.hbm;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "join-element",
   propOrder = {"subselect", "comment", "key", "propertyOrManyToOneOrComponent", "sqlInsert", "sqlUpdate", "sqlDelete"}
)
public class JaxbJoinElement {
   protected String subselect;
   protected String comment;
   @XmlElement(
      required = true
   )
   protected JaxbKeyElement key;
   @XmlElements({@XmlElement(
   name = "property",
   type = JaxbPropertyElement.class
), @XmlElement(
   name = "component",
   type = JaxbComponentElement.class
), @XmlElement(
   name = "dynamic-component",
   type = JaxbDynamicComponentElement.class
), @XmlElement(
   name = "many-to-one",
   type = JaxbManyToOneElement.class
), @XmlElement(
   name = "any",
   type = JaxbAnyElement.class
)})
   protected List propertyOrManyToOneOrComponent;
   @XmlElement(
      name = "sql-insert"
   )
   protected JaxbSqlInsertElement sqlInsert;
   @XmlElement(
      name = "sql-update"
   )
   protected JaxbSqlUpdateElement sqlUpdate;
   @XmlElement(
      name = "sql-delete"
   )
   protected JaxbSqlDeleteElement sqlDelete;
   @XmlAttribute
   protected String catalog;
   @XmlAttribute
   protected JaxbFetchAttribute fetch;
   @XmlAttribute
   protected Boolean inverse;
   @XmlAttribute
   protected Boolean optional;
   @XmlAttribute
   protected String schema;
   @XmlAttribute(
      name = "subselect"
   )
   protected String subselectAttribute;
   @XmlAttribute(
      required = true
   )
   protected String table;

   public String getSubselect() {
      return this.subselect;
   }

   public void setSubselect(String value) {
      this.subselect = value;
   }

   public String getComment() {
      return this.comment;
   }

   public void setComment(String value) {
      this.comment = value;
   }

   public JaxbKeyElement getKey() {
      return this.key;
   }

   public void setKey(JaxbKeyElement value) {
      this.key = value;
   }

   public List getPropertyOrManyToOneOrComponent() {
      if (this.propertyOrManyToOneOrComponent == null) {
         this.propertyOrManyToOneOrComponent = new ArrayList();
      }

      return this.propertyOrManyToOneOrComponent;
   }

   public JaxbSqlInsertElement getSqlInsert() {
      return this.sqlInsert;
   }

   public void setSqlInsert(JaxbSqlInsertElement value) {
      this.sqlInsert = value;
   }

   public JaxbSqlUpdateElement getSqlUpdate() {
      return this.sqlUpdate;
   }

   public void setSqlUpdate(JaxbSqlUpdateElement value) {
      this.sqlUpdate = value;
   }

   public JaxbSqlDeleteElement getSqlDelete() {
      return this.sqlDelete;
   }

   public void setSqlDelete(JaxbSqlDeleteElement value) {
      this.sqlDelete = value;
   }

   public String getCatalog() {
      return this.catalog;
   }

   public void setCatalog(String value) {
      this.catalog = value;
   }

   public JaxbFetchAttribute getFetch() {
      return this.fetch == null ? JaxbFetchAttribute.JOIN : this.fetch;
   }

   public void setFetch(JaxbFetchAttribute value) {
      this.fetch = value;
   }

   public boolean isInverse() {
      return this.inverse == null ? false : this.inverse;
   }

   public void setInverse(Boolean value) {
      this.inverse = value;
   }

   public boolean isOptional() {
      return this.optional == null ? false : this.optional;
   }

   public void setOptional(Boolean value) {
      this.optional = value;
   }

   public String getSchema() {
      return this.schema;
   }

   public void setSchema(String value) {
      this.schema = value;
   }

   public String getSubselectAttribute() {
      return this.subselectAttribute;
   }

   public void setSubselectAttribute(String value) {
      this.subselectAttribute = value;
   }

   public String getTable() {
      return this.table;
   }

   public void setTable(String value) {
      this.table = value;
   }
}
