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
   name = "map-element",
   propOrder = {"meta", "subselect", "cache", "synchronize", "comment", "key", "mapKey", "compositeMapKey", "mapKeyManyToMany", "index", "compositeIndex", "indexManyToMany", "indexManyToAny", "element", "oneToMany", "manyToMany", "compositeElement", "manyToAny", "loader", "sqlInsert", "sqlUpdate", "sqlDelete", "sqlDeleteAll", "filter"}
)
public class JaxbMapElement implements PluralAttributeElement {
   protected List meta;
   protected String subselect;
   protected JaxbCacheElement cache;
   protected List synchronize;
   protected String comment;
   @XmlElement(
      required = true
   )
   protected JaxbKeyElement key;
   @XmlElement(
      name = "map-key"
   )
   protected JaxbMapKey mapKey;
   @XmlElement(
      name = "composite-map-key"
   )
   protected JaxbCompositeMapKey compositeMapKey;
   @XmlElement(
      name = "map-key-many-to-many"
   )
   protected JaxbMapKeyManyToMany mapKeyManyToMany;
   protected JaxbIndexElement index;
   @XmlElement(
      name = "composite-index"
   )
   protected JaxbCompositeIndex compositeIndex;
   @XmlElement(
      name = "index-many-to-many"
   )
   protected JaxbIndexManyToMany indexManyToMany;
   @XmlElement(
      name = "index-many-to-any"
   )
   protected JaxbIndexManyToAny indexManyToAny;
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
   @XmlAttribute
   protected String sort;
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

   public JaxbMapKey getMapKey() {
      return this.mapKey;
   }

   public void setMapKey(JaxbMapKey value) {
      this.mapKey = value;
   }

   public JaxbCompositeMapKey getCompositeMapKey() {
      return this.compositeMapKey;
   }

   public void setCompositeMapKey(JaxbCompositeMapKey value) {
      this.compositeMapKey = value;
   }

   public JaxbMapKeyManyToMany getMapKeyManyToMany() {
      return this.mapKeyManyToMany;
   }

   public void setMapKeyManyToMany(JaxbMapKeyManyToMany value) {
      this.mapKeyManyToMany = value;
   }

   public JaxbIndexElement getIndex() {
      return this.index;
   }

   public void setIndex(JaxbIndexElement value) {
      this.index = value;
   }

   public JaxbCompositeIndex getCompositeIndex() {
      return this.compositeIndex;
   }

   public void setCompositeIndex(JaxbCompositeIndex value) {
      this.compositeIndex = value;
   }

   public JaxbIndexManyToMany getIndexManyToMany() {
      return this.indexManyToMany;
   }

   public void setIndexManyToMany(JaxbIndexManyToMany value) {
      this.indexManyToMany = value;
   }

   public JaxbIndexManyToAny getIndexManyToAny() {
      return this.indexManyToAny;
   }

   public void setIndexManyToAny(JaxbIndexManyToAny value) {
      this.indexManyToAny = value;
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

   public String getSort() {
      return this.sort == null ? "unsorted" : this.sort;
   }

   public void setSort(String value) {
      this.sort = value;
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

   @XmlAccessorType(XmlAccessType.FIELD)
   @XmlType(
      name = "",
      propOrder = {"keyPropertyOrKeyManyToOne"}
   )
   public static class JaxbCompositeIndex {
      @XmlElements({@XmlElement(
   name = "key-many-to-one",
   type = JaxbKeyManyToOneElement.class
), @XmlElement(
   name = "key-property",
   type = JaxbKeyPropertyElement.class
)})
      protected List keyPropertyOrKeyManyToOne;
      @XmlAttribute(
         name = "class",
         required = true
      )
      protected String clazz;

      public List getKeyPropertyOrKeyManyToOne() {
         if (this.keyPropertyOrKeyManyToOne == null) {
            this.keyPropertyOrKeyManyToOne = new ArrayList();
         }

         return this.keyPropertyOrKeyManyToOne;
      }

      public String getClazz() {
         return this.clazz;
      }

      public void setClazz(String value) {
         this.clazz = value;
      }
   }

   @XmlAccessorType(XmlAccessType.FIELD)
   @XmlType(
      name = "",
      propOrder = {"keyPropertyOrKeyManyToOne"}
   )
   public static class JaxbCompositeMapKey {
      @XmlElements({@XmlElement(
   name = "key-many-to-one",
   type = JaxbKeyManyToOneElement.class
), @XmlElement(
   name = "key-property",
   type = JaxbKeyPropertyElement.class
)})
      protected List keyPropertyOrKeyManyToOne;
      @XmlAttribute(
         name = "class",
         required = true
      )
      protected String clazz;

      public List getKeyPropertyOrKeyManyToOne() {
         if (this.keyPropertyOrKeyManyToOne == null) {
            this.keyPropertyOrKeyManyToOne = new ArrayList();
         }

         return this.keyPropertyOrKeyManyToOne;
      }

      public String getClazz() {
         return this.clazz;
      }

      public void setClazz(String value) {
         this.clazz = value;
      }
   }

   @XmlAccessorType(XmlAccessType.FIELD)
   @XmlType(
      name = "",
      propOrder = {"column"}
   )
   public static class JaxbIndexManyToAny {
      @XmlElement(
         required = true
      )
      protected JaxbColumnElement column;
      @XmlAttribute(
         name = "id-type",
         required = true
      )
      protected String idType;
      @XmlAttribute(
         name = "meta-type"
      )
      protected String metaType;

      public JaxbColumnElement getColumn() {
         return this.column;
      }

      public void setColumn(JaxbColumnElement value) {
         this.column = value;
      }

      public String getIdType() {
         return this.idType;
      }

      public void setIdType(String value) {
         this.idType = value;
      }

      public String getMetaType() {
         return this.metaType;
      }

      public void setMetaType(String value) {
         this.metaType = value;
      }
   }

   @XmlAccessorType(XmlAccessType.FIELD)
   @XmlType(
      name = "",
      propOrder = {"column"}
   )
   public static class JaxbIndexManyToMany {
      protected List column;
      @XmlAttribute(
         name = "class",
         required = true
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

      public List getColumn() {
         if (this.column == null) {
            this.column = new ArrayList();
         }

         return this.column;
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
   }

   @XmlAccessorType(XmlAccessType.FIELD)
   @XmlType(
      name = "",
      propOrder = {"columnOrFormula", "type"}
   )
   public static class JaxbMapKey {
      @XmlElements({@XmlElement(
   name = "column",
   type = JaxbColumnElement.class
), @XmlElement(
   name = "formula",
   type = String.class
)})
      protected List columnOrFormula;
      protected JaxbTypeElement type;
      @XmlAttribute
      protected String column;
      @XmlAttribute
      protected String formula;
      @XmlAttribute
      protected String length;
      @XmlAttribute
      protected String node;
      @XmlAttribute(
         name = "type"
      )
      protected String typeAttribute;

      public List getColumnOrFormula() {
         if (this.columnOrFormula == null) {
            this.columnOrFormula = new ArrayList();
         }

         return this.columnOrFormula;
      }

      public JaxbTypeElement getType() {
         return this.type;
      }

      public void setType(JaxbTypeElement value) {
         this.type = value;
      }

      public String getColumn() {
         return this.column;
      }

      public void setColumn(String value) {
         this.column = value;
      }

      public String getFormula() {
         return this.formula;
      }

      public void setFormula(String value) {
         this.formula = value;
      }

      public String getLength() {
         return this.length;
      }

      public void setLength(String value) {
         this.length = value;
      }

      public String getNode() {
         return this.node;
      }

      public void setNode(String value) {
         this.node = value;
      }

      public String getTypeAttribute() {
         return this.typeAttribute;
      }

      public void setTypeAttribute(String value) {
         this.typeAttribute = value;
      }
   }

   @XmlAccessorType(XmlAccessType.FIELD)
   @XmlType(
      name = "",
      propOrder = {"columnOrFormula"}
   )
   public static class JaxbMapKeyManyToMany {
      @XmlElements({@XmlElement(
   name = "column",
   type = JaxbColumnElement.class
), @XmlElement(
   name = "formula",
   type = String.class
)})
      protected List columnOrFormula;
      @XmlAttribute(
         name = "class"
      )
      protected String clazz;
      @XmlAttribute
      protected String column;
      @XmlAttribute(
         name = "entity-name"
      )
      protected String entityName;
      @XmlAttribute(
         name = "foreign-key"
      )
      protected String foreignKey;
      @XmlAttribute
      protected String formula;

      public List getColumnOrFormula() {
         if (this.columnOrFormula == null) {
            this.columnOrFormula = new ArrayList();
         }

         return this.columnOrFormula;
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

      public String getFormula() {
         return this.formula;
      }

      public void setFormula(String value) {
         this.formula = value;
      }
   }
}
