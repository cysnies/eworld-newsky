package org.hibernate.internal.jaxb.mapping.hbm;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "bag-element",
   propOrder = {"meta", "subselect", "cache", "synchronize", "comment", "key", "element", "oneToMany", "manyToMany", "compositeElement", "manyToAny", "loader", "sqlInsert", "sqlUpdate", "sqlDelete", "sqlDeleteAll", "filter"}
)
public class JaxbBagElement implements PluralAttributeElement {
   protected List meta;
   protected String subselect;
   protected JaxbCacheElement cache;
   protected List synchronize;
   protected String comment;
   @XmlElement(
      required = true
   )
   protected JaxbKeyElement key;
   protected JaxbElementElement element;
   @XmlElement(
      name = "one-to-many"
   )
   protected JaxbOneToManyElement oneToMany;
   @XmlElement(
      name = "many-to-many"
   )
   protected JaxbManyToManyElement manyToMany;
   @XmlElement(
      name = "composite-element"
   )
   protected JaxbCompositeElementElement compositeElement;
   @XmlElement(
      name = "many-to-any"
   )
   protected JaxbManyToAnyElement manyToAny;
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
   @XmlElement(
      name = "sql-delete-all"
   )
   protected JaxbSqlDeleteAllElement sqlDeleteAll;
   protected List filter;
   @XmlAttribute
   protected String access;
   @XmlAttribute(
      name = "batch-size"
   )
   protected String batchSize;
   @XmlAttribute
   protected String cascade;
   @XmlAttribute
   protected String catalog;
   @XmlAttribute
   protected String check;
   @XmlAttribute(
      name = "collection-type"
   )
   protected String collectionType;
   @XmlAttribute(
      name = "embed-xml"
   )
   protected Boolean embedXml;
   @XmlAttribute
   protected JaxbFetchAttributeWithSubselect fetch;
   @XmlAttribute
   protected Boolean inverse;
   @XmlAttribute
   protected JaxbLazyAttributeWithExtra lazy;
   @XmlAttribute
   protected Boolean mutable;
   @XmlAttribute(
      required = true
   )
   protected String name;
   @XmlAttribute
   protected String node;
   @XmlAttribute(
      name = "optimistic-lock"
   )
   protected Boolean optimisticLock;
   @XmlAttribute(
      name = "order-by"
   )
   protected String orderBy;
   @XmlAttribute(
      name = "outer-join"
   )
   protected JaxbOuterJoinAttribute outerJoin;
   @XmlAttribute
   protected String persister;
   @XmlAttribute
   protected String schema;
   @XmlAttribute(
      name = "subselect"
   )
   protected String subselectAttribute;
   @XmlAttribute
   protected String table;
   @XmlAttribute
   protected String where;

   public List getMeta() {
      if (this.meta == null) {
         this.meta = new ArrayList();
      }

      return this.meta;
   }

   public String getSubselect() {
      return this.subselect;
   }

   public void setSubselect(String value) {
      this.subselect = value;
   }

   public JaxbCacheElement getCache() {
      return this.cache;
   }

   public void setCache(JaxbCacheElement value) {
      this.cache = value;
   }

   public List getSynchronize() {
      if (this.synchronize == null) {
         this.synchronize = new ArrayList();
      }

      return this.synchronize;
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

   public JaxbElementElement getElement() {
      return this.element;
   }

   public void setElement(JaxbElementElement value) {
      this.element = value;
   }

   public JaxbOneToManyElement getOneToMany() {
      return this.oneToMany;
   }

   public void setOneToMany(JaxbOneToManyElement value) {
      this.oneToMany = value;
   }

   public JaxbManyToManyElement getManyToMany() {
      return this.manyToMany;
   }

   public void setManyToMany(JaxbManyToManyElement value) {
      this.manyToMany = value;
   }

   public JaxbCompositeElementElement getCompositeElement() {
      return this.compositeElement;
   }

   public void setCompositeElement(JaxbCompositeElementElement value) {
      this.compositeElement = value;
   }

   public JaxbManyToAnyElement getManyToAny() {
      return this.manyToAny;
   }

   public void setManyToAny(JaxbManyToAnyElement value) {
      this.manyToAny = value;
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

   public JaxbSqlDeleteAllElement getSqlDeleteAll() {
      return this.sqlDeleteAll;
   }

   public void setSqlDeleteAll(JaxbSqlDeleteAllElement value) {
      this.sqlDeleteAll = value;
   }

   public List getFilter() {
      if (this.filter == null) {
         this.filter = new ArrayList();
      }

      return this.filter;
   }

   public String getAccess() {
      return this.access;
   }

   public void setAccess(String value) {
      this.access = value;
   }

   public String getBatchSize() {
      return this.batchSize;
   }

   public void setBatchSize(String value) {
      this.batchSize = value;
   }

   public String getCascade() {
      return this.cascade;
   }

   public void setCascade(String value) {
      this.cascade = value;
   }

   public String getCatalog() {
      return this.catalog;
   }

   public void setCatalog(String value) {
      this.catalog = value;
   }

   public String getCheck() {
      return this.check;
   }

   public void setCheck(String value) {
      this.check = value;
   }

   public String getCollectionType() {
      return this.collectionType;
   }

   public void setCollectionType(String value) {
      this.collectionType = value;
   }

   public boolean isEmbedXml() {
      return this.embedXml == null ? true : this.embedXml;
   }

   public void setEmbedXml(Boolean value) {
      this.embedXml = value;
   }

   public JaxbFetchAttributeWithSubselect getFetch() {
      return this.fetch;
   }

   public void setFetch(JaxbFetchAttributeWithSubselect value) {
      this.fetch = value;
   }

   public boolean isInverse() {
      return this.inverse == null ? false : this.inverse;
   }

   public void setInverse(Boolean value) {
      this.inverse = value;
   }

   public JaxbLazyAttributeWithExtra getLazy() {
      return this.lazy;
   }

   public void setLazy(JaxbLazyAttributeWithExtra value) {
      this.lazy = value;
   }

   public boolean isMutable() {
      return this.mutable == null ? true : this.mutable;
   }

   public void setMutable(Boolean value) {
      this.mutable = value;
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

   public boolean isOptimisticLock() {
      return this.optimisticLock == null ? true : this.optimisticLock;
   }

   public void setOptimisticLock(Boolean value) {
      this.optimisticLock = value;
   }

   public String getOrderBy() {
      return this.orderBy;
   }

   public void setOrderBy(String value) {
      this.orderBy = value;
   }

   public JaxbOuterJoinAttribute getOuterJoin() {
      return this.outerJoin;
   }

   public void setOuterJoin(JaxbOuterJoinAttribute value) {
      this.outerJoin = value;
   }

   public String getPersister() {
      return this.persister;
   }

   public void setPersister(String value) {
      this.persister = value;
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

   public String getWhere() {
      return this.where;
   }

   public void setWhere(String value) {
      this.where = value;
   }
}
