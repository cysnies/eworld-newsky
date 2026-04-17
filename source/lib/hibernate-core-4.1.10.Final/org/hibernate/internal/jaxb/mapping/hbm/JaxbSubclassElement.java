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
   name = "subclass-element",
   propOrder = {"meta", "tuplizer", "synchronize", "propertyOrManyToOneOrOneToOne", "join", "subclass", "loader", "sqlInsert", "sqlUpdate", "sqlDelete", "filter", "fetchProfile", "resultset", "queryOrSqlQuery"}
)
public class JaxbSubclassElement implements JoinElementSource, SubEntityElement {
   protected List meta;
   protected List tuplizer;
   protected List synchronize;
   @XmlElements({@XmlElement(
   name = "set",
   type = JaxbSetElement.class
), @XmlElement(
   name = "component",
   type = JaxbComponentElement.class
), @XmlElement(
   name = "bag",
   type = JaxbBagElement.class
), @XmlElement(
   name = "idbag",
   type = JaxbIdbagElement.class
), @XmlElement(
   name = "one-to-one",
   type = JaxbOneToOneElement.class
), @XmlElement(
   name = "primitive-array",
   type = JaxbPrimitiveArrayElement.class
), @XmlElement(
   name = "dynamic-component",
   type = JaxbDynamicComponentElement.class
), @XmlElement(
   name = "map",
   type = JaxbMapElement.class
), @XmlElement(
   name = "property",
   type = JaxbPropertyElement.class
), @XmlElement(
   name = "list",
   type = JaxbListElement.class
), @XmlElement(
   name = "any",
   type = JaxbAnyElement.class
), @XmlElement(
   name = "array",
   type = JaxbArrayElement.class
), @XmlElement(
   name = "many-to-one",
   type = JaxbManyToOneElement.class
)})
   protected List propertyOrManyToOneOrOneToOne;
   protected List join;
   protected List subclass;
   protected JaxbLoaderElement loader;
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
   protected List filter;
   @XmlElement(
      name = "fetch-profile"
   )
   protected List fetchProfile;
   protected List resultset;
   @XmlElements({@XmlElement(
   name = "sql-query",
   type = JaxbSqlQueryElement.class
), @XmlElement(
   name = "query",
   type = JaxbQueryElement.class
)})
   protected List queryOrSqlQuery;
   @XmlAttribute(
      name = "abstract"
   )
   protected Boolean _abstract;
   @XmlAttribute(
      name = "batch-size"
   )
   protected String batchSize;
   @XmlAttribute(
      name = "discriminator-value"
   )
   protected String discriminatorValue;
   @XmlAttribute(
      name = "dynamic-insert"
   )
   protected Boolean dynamicInsert;
   @XmlAttribute(
      name = "dynamic-update"
   )
   protected Boolean dynamicUpdate;
   @XmlAttribute(
      name = "entity-name"
   )
   protected String entityName;
   @XmlAttribute(
      name = "extends"
   )
   protected String _extends;
   @XmlAttribute
   protected Boolean lazy;
   @XmlAttribute
   protected String name;
   @XmlAttribute
   protected String node;
   @XmlAttribute
   protected String persister;
   @XmlAttribute
   protected String proxy;
   @XmlAttribute(
      name = "select-before-update"
   )
   protected Boolean selectBeforeUpdate;

   public List getMeta() {
      if (this.meta == null) {
         this.meta = new ArrayList();
      }

      return this.meta;
   }

   public List getTuplizer() {
      if (this.tuplizer == null) {
         this.tuplizer = new ArrayList();
      }

      return this.tuplizer;
   }

   public List getSynchronize() {
      if (this.synchronize == null) {
         this.synchronize = new ArrayList();
      }

      return this.synchronize;
   }

   public List getPropertyOrManyToOneOrOneToOne() {
      if (this.propertyOrManyToOneOrOneToOne == null) {
         this.propertyOrManyToOneOrOneToOne = new ArrayList();
      }

      return this.propertyOrManyToOneOrOneToOne;
   }

   public List getJoin() {
      if (this.join == null) {
         this.join = new ArrayList();
      }

      return this.join;
   }

   public List getSubclass() {
      if (this.subclass == null) {
         this.subclass = new ArrayList();
      }

      return this.subclass;
   }

   public JaxbLoaderElement getLoader() {
      return this.loader;
   }

   public void setLoader(JaxbLoaderElement value) {
      this.loader = value;
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

   public List getFilter() {
      if (this.filter == null) {
         this.filter = new ArrayList();
      }

      return this.filter;
   }

   public List getFetchProfile() {
      if (this.fetchProfile == null) {
         this.fetchProfile = new ArrayList();
      }

      return this.fetchProfile;
   }

   public List getResultset() {
      if (this.resultset == null) {
         this.resultset = new ArrayList();
      }

      return this.resultset;
   }

   public List getQueryOrSqlQuery() {
      if (this.queryOrSqlQuery == null) {
         this.queryOrSqlQuery = new ArrayList();
      }

      return this.queryOrSqlQuery;
   }

   public Boolean isAbstract() {
      return this._abstract;
   }

   public void setAbstract(Boolean value) {
      this._abstract = value;
   }

   public String getBatchSize() {
      return this.batchSize;
   }

   public void setBatchSize(String value) {
      this.batchSize = value;
   }

   public String getDiscriminatorValue() {
      return this.discriminatorValue;
   }

   public void setDiscriminatorValue(String value) {
      this.discriminatorValue = value;
   }

   public boolean isDynamicInsert() {
      return this.dynamicInsert == null ? false : this.dynamicInsert;
   }

   public void setDynamicInsert(Boolean value) {
      this.dynamicInsert = value;
   }

   public boolean isDynamicUpdate() {
      return this.dynamicUpdate == null ? false : this.dynamicUpdate;
   }

   public void setDynamicUpdate(Boolean value) {
      this.dynamicUpdate = value;
   }

   public String getEntityName() {
      return this.entityName;
   }

   public void setEntityName(String value) {
      this.entityName = value;
   }

   public String getExtends() {
      return this._extends;
   }

   public void setExtends(String value) {
      this._extends = value;
   }

   public Boolean isLazy() {
      return this.lazy;
   }

   public void setLazy(Boolean value) {
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

   public String getPersister() {
      return this.persister;
   }

   public void setPersister(String value) {
      this.persister = value;
   }

   public String getProxy() {
      return this.proxy;
   }

   public void setProxy(String value) {
      this.proxy = value;
   }

   public boolean isSelectBeforeUpdate() {
      return this.selectBeforeUpdate == null ? false : this.selectBeforeUpdate;
   }

   public void setSelectBeforeUpdate(Boolean value) {
      this.selectBeforeUpdate = value;
   }
}
