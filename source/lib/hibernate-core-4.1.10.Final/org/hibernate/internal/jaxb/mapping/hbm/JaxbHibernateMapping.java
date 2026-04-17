package org.hibernate.internal.jaxb.mapping.hbm;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "",
   propOrder = {"meta", "identifierGenerator", "typedef", "filterDef", "_import", "clazzOrSubclassOrJoinedSubclass", "resultset", "queryOrSqlQuery", "fetchProfile", "databaseObject"}
)
@XmlRootElement(
   name = "hibernate-mapping"
)
public class JaxbHibernateMapping {
   protected List meta;
   @XmlElement(
      name = "identifier-generator"
   )
   protected List identifierGenerator;
   protected List typedef;
   @XmlElement(
      name = "filter-def"
   )
   protected List filterDef;
   @XmlElement(
      name = "import"
   )
   protected List _import;
   @XmlElements({@XmlElement(
   name = "subclass",
   type = JaxbSubclassElement.class
), @XmlElement(
   name = "union-subclass",
   type = JaxbUnionSubclassElement.class
), @XmlElement(
   name = "joined-subclass",
   type = JaxbJoinedSubclassElement.class
), @XmlElement(
   name = "class",
   type = JaxbClass.class
)})
   protected List clazzOrSubclassOrJoinedSubclass;
   protected List resultset;
   @XmlElements({@XmlElement(
   name = "query",
   type = JaxbQueryElement.class
), @XmlElement(
   name = "sql-query",
   type = JaxbSqlQueryElement.class
)})
   protected List queryOrSqlQuery;
   @XmlElement(
      name = "fetch-profile"
   )
   protected List fetchProfile;
   @XmlElement(
      name = "database-object"
   )
   protected List databaseObject;
   @XmlAttribute(
      name = "auto-import"
   )
   protected Boolean autoImport;
   @XmlAttribute
   protected String catalog;
   @XmlAttribute(
      name = "default-access"
   )
   protected String defaultAccess;
   @XmlAttribute(
      name = "default-cascade"
   )
   protected String defaultCascade;
   @XmlAttribute(
      name = "default-lazy"
   )
   protected Boolean defaultLazy;
   @XmlAttribute(
      name = "package"
   )
   protected String _package;
   @XmlAttribute
   protected String schema;

   public List getMeta() {
      if (this.meta == null) {
         this.meta = new ArrayList();
      }

      return this.meta;
   }

   public List getIdentifierGenerator() {
      if (this.identifierGenerator == null) {
         this.identifierGenerator = new ArrayList();
      }

      return this.identifierGenerator;
   }

   public List getTypedef() {
      if (this.typedef == null) {
         this.typedef = new ArrayList();
      }

      return this.typedef;
   }

   public List getFilterDef() {
      if (this.filterDef == null) {
         this.filterDef = new ArrayList();
      }

      return this.filterDef;
   }

   public List getImport() {
      if (this._import == null) {
         this._import = new ArrayList();
      }

      return this._import;
   }

   public List getClazzOrSubclassOrJoinedSubclass() {
      if (this.clazzOrSubclassOrJoinedSubclass == null) {
         this.clazzOrSubclassOrJoinedSubclass = new ArrayList();
      }

      return this.clazzOrSubclassOrJoinedSubclass;
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

   public List getFetchProfile() {
      if (this.fetchProfile == null) {
         this.fetchProfile = new ArrayList();
      }

      return this.fetchProfile;
   }

   public List getDatabaseObject() {
      if (this.databaseObject == null) {
         this.databaseObject = new ArrayList();
      }

      return this.databaseObject;
   }

   public boolean isAutoImport() {
      return this.autoImport == null ? true : this.autoImport;
   }

   public void setAutoImport(Boolean value) {
      this.autoImport = value;
   }

   public String getCatalog() {
      return this.catalog;
   }

   public void setCatalog(String value) {
      this.catalog = value;
   }

   public String getDefaultAccess() {
      return this.defaultAccess == null ? "property" : this.defaultAccess;
   }

   public void setDefaultAccess(String value) {
      this.defaultAccess = value;
   }

   public String getDefaultCascade() {
      return this.defaultCascade == null ? "none" : this.defaultCascade;
   }

   public void setDefaultCascade(String value) {
      this.defaultCascade = value;
   }

   public boolean isDefaultLazy() {
      return this.defaultLazy == null ? true : this.defaultLazy;
   }

   public void setDefaultLazy(Boolean value) {
      this.defaultLazy = value;
   }

   public String getPackage() {
      return this._package;
   }

   public void setPackage(String value) {
      this._package = value;
   }

   public String getSchema() {
      return this.schema;
   }

   public void setSchema(String value) {
      this.schema = value;
   }

   @XmlAccessorType(XmlAccessType.FIELD)
   @XmlType(
      name = "",
      propOrder = {"meta", "subselect", "cache", "synchronize", "comment", "tuplizer", "id", "compositeId", "discriminator", "naturalId", "version", "timestamp", "propertyOrManyToOneOrOneToOne", "join", "subclass", "joinedSubclass", "unionSubclass", "loader", "sqlInsert", "sqlUpdate", "sqlDelete", "filter", "fetchProfile", "resultset", "queryOrSqlQuery"}
   )
   public static class JaxbClass implements EntityElement, JoinElementSource {
      protected List meta;
      protected String subselect;
      protected JaxbCacheElement cache;
      protected List synchronize;
      protected String comment;
      protected List tuplizer;
      protected JaxbId id;
      @XmlElement(
         name = "composite-id"
      )
      protected JaxbCompositeId compositeId;
      protected JaxbDiscriminator discriminator;
      @XmlElement(
         name = "natural-id"
      )
      protected JaxbNaturalId naturalId;
      protected JaxbVersion version;
      protected JaxbTimestamp timestamp;
      @XmlElements({@XmlElement(
   name = "component",
   type = JaxbComponentElement.class
), @XmlElement(
   name = "property",
   type = JaxbPropertyElement.class
), @XmlElement(
   name = "any",
   type = JaxbAnyElement.class
), @XmlElement(
   name = "many-to-one",
   type = JaxbManyToOneElement.class
), @XmlElement(
   name = "set",
   type = JaxbSetElement.class
), @XmlElement(
   name = "one-to-one",
   type = JaxbOneToOneElement.class
), @XmlElement(
   name = "primitive-array",
   type = JaxbPrimitiveArrayElement.class
), @XmlElement(
   name = "properties",
   type = JaxbPropertiesElement.class
), @XmlElement(
   name = "map",
   type = JaxbMapElement.class
), @XmlElement(
   name = "list",
   type = JaxbListElement.class
), @XmlElement(
   name = "dynamic-component",
   type = JaxbDynamicComponentElement.class
), @XmlElement(
   name = "idbag",
   type = JaxbIdbagElement.class
), @XmlElement(
   name = "array",
   type = JaxbArrayElement.class
), @XmlElement(
   name = "bag",
   type = JaxbBagElement.class
)})
      protected List propertyOrManyToOneOrOneToOne;
      protected List join;
      protected List subclass;
      @XmlElement(
         name = "joined-subclass"
      )
      protected List joinedSubclass;
      @XmlElement(
         name = "union-subclass"
      )
      protected List unionSubclass;
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
   name = "query",
   type = JaxbQueryElement.class
), @XmlElement(
   name = "sql-query",
   type = JaxbSqlQueryElement.class
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
      @XmlAttribute
      protected String catalog;
      @XmlAttribute
      protected String check;
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
      @XmlAttribute
      protected Boolean lazy;
      @XmlAttribute
      protected Boolean mutable;
      @XmlAttribute
      protected String name;
      @XmlAttribute
      protected String node;
      @XmlAttribute(
         name = "optimistic-lock"
      )
      @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
      protected String optimisticLock;
      @XmlAttribute
      protected String persister;
      @XmlAttribute
      @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
      protected String polymorphism;
      @XmlAttribute
      protected String proxy;
      @XmlAttribute
      protected String rowid;
      @XmlAttribute
      protected String schema;
      @XmlAttribute(
         name = "select-before-update"
      )
      protected Boolean selectBeforeUpdate;
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

      public List getTuplizer() {
         if (this.tuplizer == null) {
            this.tuplizer = new ArrayList();
         }

         return this.tuplizer;
      }

      public JaxbId getId() {
         return this.id;
      }

      public void setId(JaxbId value) {
         this.id = value;
      }

      public JaxbCompositeId getCompositeId() {
         return this.compositeId;
      }

      public void setCompositeId(JaxbCompositeId value) {
         this.compositeId = value;
      }

      public JaxbDiscriminator getDiscriminator() {
         return this.discriminator;
      }

      public void setDiscriminator(JaxbDiscriminator value) {
         this.discriminator = value;
      }

      public JaxbNaturalId getNaturalId() {
         return this.naturalId;
      }

      public void setNaturalId(JaxbNaturalId value) {
         this.naturalId = value;
      }

      public JaxbVersion getVersion() {
         return this.version;
      }

      public void setVersion(JaxbVersion value) {
         this.version = value;
      }

      public JaxbTimestamp getTimestamp() {
         return this.timestamp;
      }

      public void setTimestamp(JaxbTimestamp value) {
         this.timestamp = value;
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

      public List getJoinedSubclass() {
         if (this.joinedSubclass == null) {
            this.joinedSubclass = new ArrayList();
         }

         return this.joinedSubclass;
      }

      public List getUnionSubclass() {
         if (this.unionSubclass == null) {
            this.unionSubclass = new ArrayList();
         }

         return this.unionSubclass;
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

      public Boolean isLazy() {
         return this.lazy;
      }

      public void setLazy(Boolean value) {
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

      public String getOptimisticLock() {
         return this.optimisticLock == null ? "version" : this.optimisticLock;
      }

      public void setOptimisticLock(String value) {
         this.optimisticLock = value;
      }

      public String getPersister() {
         return this.persister;
      }

      public void setPersister(String value) {
         this.persister = value;
      }

      public String getPolymorphism() {
         return this.polymorphism == null ? "implicit" : this.polymorphism;
      }

      public void setPolymorphism(String value) {
         this.polymorphism = value;
      }

      public String getProxy() {
         return this.proxy;
      }

      public void setProxy(String value) {
         this.proxy = value;
      }

      public String getRowid() {
         return this.rowid;
      }

      public void setRowid(String value) {
         this.rowid = value;
      }

      public String getSchema() {
         return this.schema;
      }

      public void setSchema(String value) {
         this.schema = value;
      }

      public boolean isSelectBeforeUpdate() {
         return this.selectBeforeUpdate == null ? false : this.selectBeforeUpdate;
      }

      public void setSelectBeforeUpdate(Boolean value) {
         this.selectBeforeUpdate = value;
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
         propOrder = {"meta", "keyPropertyOrKeyManyToOne", "generator"}
      )
      public static class JaxbCompositeId {
         protected List meta;
         @XmlElements({@XmlElement(
   name = "key-many-to-one",
   type = JaxbKeyManyToOneElement.class
), @XmlElement(
   name = "key-property",
   type = JaxbKeyPropertyElement.class
)})
         protected List keyPropertyOrKeyManyToOne;
         protected JaxbGeneratorElement generator;
         @XmlAttribute
         protected String access;
         @XmlAttribute(
            name = "class"
         )
         protected String clazz;
         @XmlAttribute
         protected Boolean mapped;
         @XmlAttribute
         protected String name;
         @XmlAttribute
         protected String node;
         @XmlAttribute(
            name = "unsaved-value"
         )
         @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
         protected String unsavedValue;

         public List getMeta() {
            if (this.meta == null) {
               this.meta = new ArrayList();
            }

            return this.meta;
         }

         public List getKeyPropertyOrKeyManyToOne() {
            if (this.keyPropertyOrKeyManyToOne == null) {
               this.keyPropertyOrKeyManyToOne = new ArrayList();
            }

            return this.keyPropertyOrKeyManyToOne;
         }

         public JaxbGeneratorElement getGenerator() {
            return this.generator;
         }

         public void setGenerator(JaxbGeneratorElement value) {
            this.generator = value;
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

         public boolean isMapped() {
            return this.mapped == null ? false : this.mapped;
         }

         public void setMapped(Boolean value) {
            this.mapped = value;
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

         public String getUnsavedValue() {
            return this.unsavedValue == null ? "undefined" : this.unsavedValue;
         }

         public void setUnsavedValue(String value) {
            this.unsavedValue = value;
         }
      }

      @XmlAccessorType(XmlAccessType.FIELD)
      @XmlType(
         name = "",
         propOrder = {"column", "formula"}
      )
      public static class JaxbDiscriminator {
         protected JaxbColumnElement column;
         protected String formula;
         @XmlAttribute(
            name = "column"
         )
         protected String columnAttribute;
         @XmlAttribute
         protected Boolean force;
         @XmlAttribute(
            name = "formula"
         )
         protected String formulaAttribute;
         @XmlAttribute
         protected Boolean insert;
         @XmlAttribute
         protected String length;
         @XmlAttribute(
            name = "not-null"
         )
         protected Boolean notNull;
         @XmlAttribute
         protected String type;

         public JaxbColumnElement getColumn() {
            return this.column;
         }

         public void setColumn(JaxbColumnElement value) {
            this.column = value;
         }

         public String getFormula() {
            return this.formula;
         }

         public void setFormula(String value) {
            this.formula = value;
         }

         public String getColumnAttribute() {
            return this.columnAttribute;
         }

         public void setColumnAttribute(String value) {
            this.columnAttribute = value;
         }

         public boolean isForce() {
            return this.force == null ? false : this.force;
         }

         public void setForce(Boolean value) {
            this.force = value;
         }

         public String getFormulaAttribute() {
            return this.formulaAttribute;
         }

         public void setFormulaAttribute(String value) {
            this.formulaAttribute = value;
         }

         public boolean isInsert() {
            return this.insert == null ? true : this.insert;
         }

         public void setInsert(Boolean value) {
            this.insert = value;
         }

         public String getLength() {
            return this.length;
         }

         public void setLength(String value) {
            this.length = value;
         }

         public boolean isNotNull() {
            return this.notNull == null ? true : this.notNull;
         }

         public void setNotNull(Boolean value) {
            this.notNull = value;
         }

         public String getType() {
            return this.type == null ? "string" : this.type;
         }

         public void setType(String value) {
            this.type = value;
         }
      }

      @XmlAccessorType(XmlAccessType.FIELD)
      @XmlType(
         name = "",
         propOrder = {"meta", "column", "type", "generator"}
      )
      public static class JaxbId implements SingularAttributeSource {
         protected List meta;
         protected List column;
         protected JaxbTypeElement type;
         protected JaxbGeneratorElement generator;
         @XmlAttribute
         protected String access;
         @XmlAttribute(
            name = "column"
         )
         protected String columnAttribute;
         @XmlAttribute
         protected String length;
         @XmlAttribute
         protected String name;
         @XmlAttribute
         protected String node;
         @XmlAttribute(
            name = "type"
         )
         protected String typeAttribute;
         @XmlAttribute(
            name = "unsaved-value"
         )
         protected String unsavedValue;

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

         public JaxbTypeElement getType() {
            return this.type;
         }

         public void setType(JaxbTypeElement value) {
            this.type = value;
         }

         public JaxbGeneratorElement getGenerator() {
            return this.generator;
         }

         public void setGenerator(JaxbGeneratorElement value) {
            this.generator = value;
         }

         public String getAccess() {
            return this.access;
         }

         public void setAccess(String value) {
            this.access = value;
         }

         public String getColumnAttribute() {
            return this.columnAttribute;
         }

         public void setColumnAttribute(String value) {
            this.columnAttribute = value;
         }

         public String getLength() {
            return this.length;
         }

         public void setLength(String value) {
            this.length = value;
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

         public String getTypeAttribute() {
            return this.typeAttribute;
         }

         public void setTypeAttribute(String value) {
            this.typeAttribute = value;
         }

         public String getUnsavedValue() {
            return this.unsavedValue;
         }

         public void setUnsavedValue(String value) {
            this.unsavedValue = value;
         }
      }

      @XmlAccessorType(XmlAccessType.FIELD)
      @XmlType(
         name = "",
         propOrder = {"propertyOrManyToOneOrComponent"}
      )
      public static class JaxbNaturalId {
         @XmlElements({@XmlElement(
   name = "dynamic-component",
   type = JaxbDynamicComponentElement.class
), @XmlElement(
   name = "property",
   type = JaxbPropertyElement.class
), @XmlElement(
   name = "component",
   type = JaxbComponentElement.class
), @XmlElement(
   name = "many-to-one",
   type = JaxbManyToOneElement.class
), @XmlElement(
   name = "any",
   type = JaxbAnyElement.class
)})
         protected List propertyOrManyToOneOrComponent;
         @XmlAttribute
         protected Boolean mutable;

         public List getPropertyOrManyToOneOrComponent() {
            if (this.propertyOrManyToOneOrComponent == null) {
               this.propertyOrManyToOneOrComponent = new ArrayList();
            }

            return this.propertyOrManyToOneOrComponent;
         }

         public boolean isMutable() {
            return this.mutable == null ? false : this.mutable;
         }

         public void setMutable(Boolean value) {
            this.mutable = value;
         }
      }

      @XmlAccessorType(XmlAccessType.FIELD)
      @XmlType(
         name = "",
         propOrder = {"meta"}
      )
      public static class JaxbTimestamp {
         protected List meta;
         @XmlAttribute
         protected String access;
         @XmlAttribute
         protected String column;
         @XmlAttribute
         protected JaxbGeneratedAttribute generated;
         @XmlAttribute(
            required = true
         )
         protected String name;
         @XmlAttribute
         protected String node;
         @XmlAttribute
         @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
         protected String source;
         @XmlAttribute(
            name = "unsaved-value"
         )
         @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
         protected String unsavedValue;

         public List getMeta() {
            if (this.meta == null) {
               this.meta = new ArrayList();
            }

            return this.meta;
         }

         public String getAccess() {
            return this.access;
         }

         public void setAccess(String value) {
            this.access = value;
         }

         public String getColumn() {
            return this.column;
         }

         public void setColumn(String value) {
            this.column = value;
         }

         public JaxbGeneratedAttribute getGenerated() {
            return this.generated == null ? JaxbGeneratedAttribute.NEVER : this.generated;
         }

         public void setGenerated(JaxbGeneratedAttribute value) {
            this.generated = value;
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

         public String getSource() {
            return this.source == null ? "vm" : this.source;
         }

         public void setSource(String value) {
            this.source = value;
         }

         public String getUnsavedValue() {
            return this.unsavedValue == null ? "null" : this.unsavedValue;
         }

         public void setUnsavedValue(String value) {
            this.unsavedValue = value;
         }
      }

      @XmlAccessorType(XmlAccessType.FIELD)
      @XmlType(
         name = "",
         propOrder = {"meta", "column"}
      )
      public static class JaxbVersion {
         protected List meta;
         protected List column;
         @XmlAttribute
         protected String access;
         @XmlAttribute(
            name = "column"
         )
         protected String columnAttribute;
         @XmlAttribute
         protected JaxbGeneratedAttribute generated;
         @XmlAttribute
         protected Boolean insert;
         @XmlAttribute(
            required = true
         )
         protected String name;
         @XmlAttribute
         protected String node;
         @XmlAttribute
         protected String type;
         @XmlAttribute(
            name = "unsaved-value"
         )
         @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
         protected String unsavedValue;

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

         public String getColumnAttribute() {
            return this.columnAttribute;
         }

         public void setColumnAttribute(String value) {
            this.columnAttribute = value;
         }

         public JaxbGeneratedAttribute getGenerated() {
            return this.generated == null ? JaxbGeneratedAttribute.NEVER : this.generated;
         }

         public void setGenerated(JaxbGeneratedAttribute value) {
            this.generated = value;
         }

         public Boolean isInsert() {
            return this.insert;
         }

         public void setInsert(Boolean value) {
            this.insert = value;
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

         public String getType() {
            return this.type == null ? "integer" : this.type;
         }

         public void setType(String value) {
            this.type = value;
         }

         public String getUnsavedValue() {
            return this.unsavedValue == null ? "undefined" : this.unsavedValue;
         }

         public void setUnsavedValue(String value) {
            this.unsavedValue = value;
         }
      }
   }

   @XmlAccessorType(XmlAccessType.FIELD)
   @XmlType(
      name = "",
      propOrder = {"definition", "create", "drop", "dialectScope"}
   )
   public static class JaxbDatabaseObject {
      protected JaxbDefinition definition;
      protected String create;
      protected String drop;
      @XmlElement(
         name = "dialect-scope"
      )
      protected List dialectScope;

      public JaxbDefinition getDefinition() {
         return this.definition;
      }

      public void setDefinition(JaxbDefinition value) {
         this.definition = value;
      }

      public String getCreate() {
         return this.create;
      }

      public void setCreate(String value) {
         this.create = value;
      }

      public String getDrop() {
         return this.drop;
      }

      public void setDrop(String value) {
         this.drop = value;
      }

      public List getDialectScope() {
         if (this.dialectScope == null) {
            this.dialectScope = new ArrayList();
         }

         return this.dialectScope;
      }

      @XmlAccessorType(XmlAccessType.FIELD)
      @XmlType(
         name = ""
      )
      public static class JaxbDefinition {
         @XmlAttribute(
            name = "class",
            required = true
         )
         protected String clazz;

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
         propOrder = {"value"}
      )
      public static class JaxbDialectScope {
         @XmlValue
         protected String value;
         @XmlAttribute(
            required = true
         )
         protected String name;

         public String getValue() {
            return this.value;
         }

         public void setValue(String value) {
            this.value = value;
         }

         public String getName() {
            return this.name;
         }

         public void setName(String value) {
            this.name = value;
         }
      }
   }

   @XmlAccessorType(XmlAccessType.FIELD)
   @XmlType(
      name = "",
      propOrder = {"content"}
   )
   public static class JaxbFilterDef {
      @XmlElementRef(
         name = "filter-param",
         namespace = "http://www.hibernate.org/xsd/hibernate-mapping",
         type = JAXBElement.class
      )
      @XmlMixed
      protected List content;
      @XmlAttribute
      protected String condition;
      @XmlAttribute(
         required = true
      )
      protected String name;

      public List getContent() {
         if (this.content == null) {
            this.content = new ArrayList();
         }

         return this.content;
      }

      public String getCondition() {
         return this.condition;
      }

      public void setCondition(String value) {
         this.condition = value;
      }

      public String getName() {
         return this.name;
      }

      public void setName(String value) {
         this.name = value;
      }

      @XmlAccessorType(XmlAccessType.FIELD)
      @XmlType(
         name = ""
      )
      public static class JaxbFilterParam {
         @XmlAttribute(
            required = true
         )
         protected String name;
         @XmlAttribute(
            required = true
         )
         protected String type;

         public String getName() {
            return this.name;
         }

         public void setName(String value) {
            this.name = value;
         }

         public String getType() {
            return this.type;
         }

         public void setType(String value) {
            this.type = value;
         }
      }
   }

   @XmlAccessorType(XmlAccessType.FIELD)
   @XmlType(
      name = ""
   )
   public static class JaxbIdentifierGenerator {
      @XmlAttribute(
         name = "class",
         required = true
      )
      protected String clazz;
      @XmlAttribute(
         required = true
      )
      protected String name;

      public String getClazz() {
         return this.clazz;
      }

      public void setClazz(String value) {
         this.clazz = value;
      }

      public String getName() {
         return this.name;
      }

      public void setName(String value) {
         this.name = value;
      }
   }

   @XmlAccessorType(XmlAccessType.FIELD)
   @XmlType(
      name = ""
   )
   public static class JaxbImport {
      @XmlAttribute(
         name = "class",
         required = true
      )
      protected String clazz;
      @XmlAttribute
      protected String rename;

      public String getClazz() {
         return this.clazz;
      }

      public void setClazz(String value) {
         this.clazz = value;
      }

      public String getRename() {
         return this.rename;
      }

      public void setRename(String value) {
         this.rename = value;
      }
   }

   @XmlAccessorType(XmlAccessType.FIELD)
   @XmlType(
      name = "",
      propOrder = {"param"}
   )
   public static class JaxbTypedef {
      protected List param;
      @XmlAttribute(
         name = "class",
         required = true
      )
      protected String clazz;
      @XmlAttribute(
         required = true
      )
      protected String name;

      public List getParam() {
         if (this.param == null) {
            this.param = new ArrayList();
         }

         return this.param;
      }

      public String getClazz() {
         return this.clazz;
      }

      public void setClazz(String value) {
         this.clazz = value;
      }

      public String getName() {
         return this.name;
      }

      public void setName(String value) {
         this.name = value;
      }
   }
}
