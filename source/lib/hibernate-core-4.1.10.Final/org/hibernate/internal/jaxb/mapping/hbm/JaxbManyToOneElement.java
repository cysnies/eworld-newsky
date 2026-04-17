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
   name = "many-to-one-element",
   propOrder = {"meta", "columnOrFormula"}
)
public class JaxbManyToOneElement {
   protected List meta;
   @XmlElements({@XmlElement(
   name = "column",
   type = JaxbColumnElement.class
), @XmlElement(
   name = "formula",
   type = String.class
)})
   protected List columnOrFormula;
   @XmlAttribute
   protected String access;
   @XmlAttribute
   protected String cascade;
   @XmlAttribute(
      name = "class"
   )
   protected String clazz;
   @XmlAttribute
   protected String column;
   @XmlAttribute(
      name = "embed-xml"
   )
   protected Boolean embedXml;
   @XmlAttribute(
      name = "entity-name"
   )
   protected String entityName;
   @XmlAttribute
   protected JaxbFetchAttribute fetch;
   @XmlAttribute(
      name = "foreign-key"
   )
   protected String foreignKey;
   @XmlAttribute
   protected String formula;
   @XmlAttribute
   protected String index;
   @XmlAttribute
   protected Boolean insert;
   @XmlAttribute
   protected JaxbLazyAttributeWithNoProxy lazy;
   @XmlAttribute(
      required = true
   )
   protected String name;
   @XmlAttribute
   protected String node;
   @XmlAttribute(
      name = "not-found"
   )
   protected JaxbNotFoundAttribute notFound;
   @XmlAttribute(
      name = "not-null"
   )
   protected Boolean notNull;
   @XmlAttribute(
      name = "optimistic-lock"
   )
   protected Boolean optimisticLock;
   @XmlAttribute(
      name = "outer-join"
   )
   protected JaxbOuterJoinAttribute outerJoin;
   @XmlAttribute(
      name = "property-ref"
   )
   protected String propertyRef;
   @XmlAttribute
   protected Boolean unique;
   @XmlAttribute(
      name = "unique-key"
   )
   protected String uniqueKey;
   @XmlAttribute
   protected Boolean update;

   public List getMeta() {
      if (this.meta == null) {
         this.meta = new ArrayList();
      }

      return this.meta;
   }

   public List getColumnOrFormula() {
      if (this.columnOrFormula == null) {
         this.columnOrFormula = new ArrayList();
      }

      return this.columnOrFormula;
   }

   public String getAccess() {
      return this.access;
   }

   public void setAccess(String value) {
      this.access = value;
   }

   public String getCascade() {
      return this.cascade;
   }

   public void setCascade(String value) {
      this.cascade = value;
   }

   public String getClazz() {
      return this.clazz;
   }

   public void setClazz(String value) {
      this.clazz = value;
   }

   public String getColumn() {
      return this.column;
   }

   public void setColumn(String value) {
      this.column = value;
   }

   public boolean isEmbedXml() {
      return this.embedXml == null ? true : this.embedXml;
   }

   public void setEmbedXml(Boolean value) {
      this.embedXml = value;
   }

   public String getEntityName() {
      return this.entityName;
   }

   public void setEntityName(String value) {
      this.entityName = value;
   }

   public JaxbFetchAttribute getFetch() {
      return this.fetch;
   }

   public void setFetch(JaxbFetchAttribute value) {
      this.fetch = value;
   }

   public String getForeignKey() {
      return this.foreignKey;
   }

   public void setForeignKey(String value) {
      this.foreignKey = value;
   }

   public String getFormula() {
      return this.formula;
   }

   public void setFormula(String value) {
      this.formula = value;
   }

   public String getIndex() {
      return this.index;
   }

   public void setIndex(String value) {
      this.index = value;
   }

   public boolean isInsert() {
      return this.insert == null ? true : this.insert;
   }

   public void setInsert(Boolean value) {
      this.insert = value;
   }

   public JaxbLazyAttributeWithNoProxy getLazy() {
      return this.lazy;
   }

   public void setLazy(JaxbLazyAttributeWithNoProxy value) {
      this.lazy = value;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String value) {
      this.name = value;
   }

   public String getNode() {
      return this.node;
   }

   public void setNode(String value) {
      this.node = value;
   }

   public JaxbNotFoundAttribute getNotFound() {
      return this.notFound == null ? JaxbNotFoundAttribute.EXCEPTION : this.notFound;
   }

   public void setNotFound(JaxbNotFoundAttribute value) {
      this.notFound = value;
   }

   public Boolean isNotNull() {
      return this.notNull;
   }

   public void setNotNull(Boolean value) {
      this.notNull = value;
   }

   public boolean isOptimisticLock() {
      return this.optimisticLock == null ? true : this.optimisticLock;
   }

   public void setOptimisticLock(Boolean value) {
      this.optimisticLock = value;
   }

   public JaxbOuterJoinAttribute getOuterJoin() {
      return this.outerJoin;
   }

   public void setOuterJoin(JaxbOuterJoinAttribute value) {
      this.outerJoin = value;
   }

   public String getPropertyRef() {
      return this.propertyRef;
   }

   public void setPropertyRef(String value) {
      this.propertyRef = value;
   }

   public boolean isUnique() {
      return this.unique == null ? false : this.unique;
   }

   public void setUnique(Boolean value) {
      this.unique = value;
   }

   public String getUniqueKey() {
      return this.uniqueKey;
   }

   public void setUniqueKey(String value) {
      this.uniqueKey = value;
   }

   public boolean isUpdate() {
      return this.update == null ? true : this.update;
   }

   public void setUpdate(Boolean value) {
      this.update = value;
   }
}
