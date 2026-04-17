package org.hibernate.persister.collection;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.hibernate.AssertionFailure;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.QueryException;
import org.hibernate.TransientObjectException;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.access.CollectionRegionAccessStrategy;
import org.hibernate.cache.spi.entry.CacheEntryStructure;
import org.hibernate.cache.spi.entry.StructuredCollectionCacheEntry;
import org.hibernate.cache.spi.entry.StructuredMapCacheEntry;
import org.hibernate.cache.spi.entry.UnstructuredCacheEntry;
import org.hibernate.cfg.Configuration;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.batch.internal.BasicBatchKey;
import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.ExecuteUpdateResultCheckStyle;
import org.hibernate.engine.spi.LoadQueryInfluencers;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SubselectFetch;
import org.hibernate.exception.spi.SQLExceptionConverter;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.FilterAliasGenerator;
import org.hibernate.internal.FilterHelper;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.jdbc.Expectation;
import org.hibernate.jdbc.Expectations;
import org.hibernate.loader.collection.CollectionInitializer;
import org.hibernate.mapping.Array;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Formula;
import org.hibernate.mapping.IdentifierCollection;
import org.hibernate.mapping.IndexedCollection;
import org.hibernate.mapping.List;
import org.hibernate.mapping.RootClass;
import org.hibernate.mapping.Selectable;
import org.hibernate.mapping.Table;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.Loadable;
import org.hibernate.persister.entity.PropertyMapping;
import org.hibernate.persister.entity.Queryable;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.sql.Alias;
import org.hibernate.sql.SelectFragment;
import org.hibernate.sql.SimpleSelect;
import org.hibernate.sql.Template;
import org.hibernate.sql.ordering.antlr.ColumnMapper;
import org.hibernate.sql.ordering.antlr.ColumnReference;
import org.hibernate.sql.ordering.antlr.FormulaReference;
import org.hibernate.sql.ordering.antlr.OrderByAliasResolver;
import org.hibernate.sql.ordering.antlr.OrderByTranslation;
import org.hibernate.sql.ordering.antlr.SqlValueReference;
import org.hibernate.type.CollectionType;
import org.hibernate.type.CompositeType;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public abstract class AbstractCollectionPersister implements CollectionMetadata, SQLLoadableCollection {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, AbstractCollectionPersister.class.getName());
   private final String role;
   private final String sqlDeleteString;
   private final String sqlInsertRowString;
   private final String sqlUpdateRowString;
   private final String sqlDeleteRowString;
   private final String sqlSelectSizeString;
   private final String sqlSelectRowByIndexString;
   private final String sqlDetectRowByIndexString;
   private final String sqlDetectRowByElementString;
   protected final boolean hasWhere;
   protected final String sqlWhereString;
   private final String sqlWhereStringTemplate;
   private final boolean hasOrder;
   private final OrderByTranslation orderByTranslation;
   private final boolean hasManyToManyOrder;
   private final OrderByTranslation manyToManyOrderByTranslation;
   private final int baseIndex;
   private final String nodeName;
   private final String elementNodeName;
   private final String indexNodeName;
   protected final boolean indexContainsFormula;
   protected final boolean elementIsPureFormula;
   private final Type keyType;
   private final Type indexType;
   protected final Type elementType;
   private final Type identifierType;
   protected final String[] keyColumnNames;
   protected final String[] indexColumnNames;
   protected final String[] indexFormulaTemplates;
   protected final String[] indexFormulas;
   protected final boolean[] indexColumnIsSettable;
   protected final String[] elementColumnNames;
   protected final String[] elementColumnWriters;
   protected final String[] elementColumnReaders;
   protected final String[] elementColumnReaderTemplates;
   protected final String[] elementFormulaTemplates;
   protected final String[] elementFormulas;
   protected final boolean[] elementColumnIsSettable;
   protected final boolean[] elementColumnIsInPrimaryKey;
   protected final String[] indexColumnAliases;
   protected final String[] elementColumnAliases;
   protected final String[] keyColumnAliases;
   protected final String identifierColumnName;
   private final String identifierColumnAlias;
   protected final String qualifiedTableName;
   private final String queryLoaderName;
   private final boolean isPrimitiveArray;
   private final boolean isArray;
   protected final boolean hasIndex;
   protected final boolean hasIdentifier;
   private final boolean isLazy;
   private final boolean isExtraLazy;
   private final boolean isInverse;
   private final boolean isMutable;
   private final boolean isVersioned;
   protected final int batchSize;
   private final FetchMode fetchMode;
   private final boolean hasOrphanDelete;
   private final boolean subselectLoadable;
   private final Class elementClass;
   private final String entityName;
   private final Dialect dialect;
   private final SqlExceptionHelper sqlExceptionHelper;
   private final SessionFactoryImplementor factory;
   private final EntityPersister ownerPersister;
   private final IdentifierGenerator identifierGenerator;
   private final PropertyMapping elementPropertyMapping;
   private final EntityPersister elementPersister;
   private final CollectionRegionAccessStrategy cacheAccessStrategy;
   private final CollectionType collectionType;
   private CollectionInitializer initializer;
   private final CacheEntryStructure cacheEntryStructure;
   private final FilterHelper filterHelper;
   private final FilterHelper manyToManyFilterHelper;
   private final String manyToManyWhereString;
   private final String manyToManyWhereTemplate;
   private final boolean insertCallable;
   private final boolean updateCallable;
   private final boolean deleteCallable;
   private final boolean deleteAllCallable;
   private ExecuteUpdateResultCheckStyle insertCheckStyle;
   private ExecuteUpdateResultCheckStyle updateCheckStyle;
   private ExecuteUpdateResultCheckStyle deleteCheckStyle;
   private ExecuteUpdateResultCheckStyle deleteAllCheckStyle;
   private final Serializable[] spaces;
   private Map collectionPropertyColumnAliases = new HashMap();
   private Map collectionPropertyColumnNames = new HashMap();
   private BasicBatchKey removeBatchKey;
   private BasicBatchKey recreateBatchKey;
   private BasicBatchKey deleteBatchKey;
   private BasicBatchKey insertBatchKey;
   private String[] indexFragments;

   public AbstractCollectionPersister(Collection collection, CollectionRegionAccessStrategy cacheAccessStrategy, Configuration cfg, SessionFactoryImplementor factory) throws MappingException, CacheException {
      this.factory = factory;
      this.cacheAccessStrategy = cacheAccessStrategy;
      if (factory.getSettings().isStructuredCacheEntriesEnabled()) {
         this.cacheEntryStructure = (CacheEntryStructure)(collection.isMap() ? new StructuredMapCacheEntry() : new StructuredCollectionCacheEntry());
      } else {
         this.cacheEntryStructure = new UnstructuredCacheEntry();
      }

      this.dialect = factory.getDialect();
      this.sqlExceptionHelper = factory.getSQLExceptionHelper();
      this.collectionType = collection.getCollectionType();
      this.role = collection.getRole();
      this.entityName = collection.getOwnerEntityName();
      this.ownerPersister = factory.getEntityPersister(this.entityName);
      this.queryLoaderName = collection.getLoaderName();
      this.nodeName = collection.getNodeName();
      this.isMutable = collection.isMutable();
      Table table = collection.getCollectionTable();
      this.fetchMode = collection.getElement().getFetchMode();
      this.elementType = collection.getElement().getType();
      this.isPrimitiveArray = collection.isPrimitiveArray();
      this.isArray = collection.isArray();
      this.subselectLoadable = collection.isSubselectLoadable();
      this.qualifiedTableName = table.getQualifiedName(this.dialect, factory.getSettings().getDefaultCatalogName(), factory.getSettings().getDefaultSchemaName());
      int spacesSize = 1 + collection.getSynchronizedTables().size();
      this.spaces = new String[spacesSize];
      this.spaces[0] = this.qualifiedTableName;
      Iterator iter = collection.getSynchronizedTables().iterator();

      for(int i = 1; i < spacesSize; ++i) {
         this.spaces[i] = (String)iter.next();
      }

      this.sqlWhereString = StringHelper.isNotEmpty(collection.getWhere()) ? "( " + collection.getWhere() + ") " : null;
      this.hasWhere = this.sqlWhereString != null;
      this.sqlWhereStringTemplate = this.hasWhere ? Template.renderWhereStringTemplate(this.sqlWhereString, this.dialect, factory.getSqlFunctionRegistry()) : null;
      this.hasOrphanDelete = collection.hasOrphanDelete();
      int batch = collection.getBatchSize();
      if (batch == -1) {
         batch = factory.getSettings().getDefaultBatchFetchSize();
      }

      this.batchSize = batch;
      this.isVersioned = collection.isOptimisticLocked();
      this.keyType = collection.getKey().getType();
      iter = collection.getKey().getColumnIterator();
      int keySpan = collection.getKey().getColumnSpan();
      this.keyColumnNames = new String[keySpan];
      this.keyColumnAliases = new String[keySpan];

      for(int k = 0; iter.hasNext(); ++k) {
         Column col = (Column)iter.next();
         this.keyColumnNames[k] = col.getQuotedName(this.dialect);
         this.keyColumnAliases[k] = col.getAlias(this.dialect, collection.getOwner().getRootTable());
      }

      String elemNode = collection.getElementNodeName();
      if (this.elementType.isEntityType()) {
         String entityName = ((EntityType)this.elementType).getAssociatedEntityName();
         this.elementPersister = factory.getEntityPersister(entityName);
         if (elemNode == null) {
            elemNode = cfg.getClassMapping(entityName).getNodeName();
         }
      } else {
         this.elementPersister = null;
      }

      this.elementNodeName = elemNode;
      int elementSpan = collection.getElement().getColumnSpan();
      this.elementColumnAliases = new String[elementSpan];
      this.elementColumnNames = new String[elementSpan];
      this.elementColumnWriters = new String[elementSpan];
      this.elementColumnReaders = new String[elementSpan];
      this.elementColumnReaderTemplates = new String[elementSpan];
      this.elementFormulaTemplates = new String[elementSpan];
      this.elementFormulas = new String[elementSpan];
      this.elementColumnIsSettable = new boolean[elementSpan];
      this.elementColumnIsInPrimaryKey = new boolean[elementSpan];
      boolean isPureFormula = true;
      boolean hasNotNullableColumns = false;
      int j = 0;

      for(Iterator var23 = collection.getElement().getColumnIterator(); var23.hasNext(); ++j) {
         Selectable selectable = (Selectable)var23.next();
         this.elementColumnAliases[j] = selectable.getAlias(this.dialect, table);
         if (selectable.isFormula()) {
            Formula form = (Formula)selectable;
            this.elementFormulaTemplates[j] = form.getTemplate(this.dialect, factory.getSqlFunctionRegistry());
            this.elementFormulas[j] = form.getFormula();
         } else {
            Column col = (Column)selectable;
            this.elementColumnNames[j] = col.getQuotedName(this.dialect);
            this.elementColumnWriters[j] = col.getWriteExpr();
            this.elementColumnReaders[j] = col.getReadExpr(this.dialect);
            this.elementColumnReaderTemplates[j] = col.getTemplate(this.dialect, factory.getSqlFunctionRegistry());
            this.elementColumnIsSettable[j] = true;
            this.elementColumnIsInPrimaryKey[j] = !col.isNullable();
            if (!col.isNullable()) {
               hasNotNullableColumns = true;
            }

            isPureFormula = false;
         }
      }

      this.elementIsPureFormula = isPureFormula;
      if (!hasNotNullableColumns) {
         Arrays.fill(this.elementColumnIsInPrimaryKey, true);
      }

      this.hasIndex = collection.isIndexed();
      if (this.hasIndex) {
         IndexedCollection indexedCollection = (IndexedCollection)collection;
         this.indexType = indexedCollection.getIndex().getType();
         int indexSpan = indexedCollection.getIndex().getColumnSpan();
         iter = indexedCollection.getIndex().getColumnIterator();
         this.indexColumnNames = new String[indexSpan];
         this.indexFormulaTemplates = new String[indexSpan];
         this.indexFormulas = new String[indexSpan];
         this.indexColumnIsSettable = new boolean[indexSpan];
         this.indexColumnAliases = new String[indexSpan];
         int i = 0;

         boolean hasFormula;
         for(hasFormula = false; iter.hasNext(); ++i) {
            Selectable s = (Selectable)iter.next();
            this.indexColumnAliases[i] = s.getAlias(this.dialect);
            if (s.isFormula()) {
               Formula indexForm = (Formula)s;
               this.indexFormulaTemplates[i] = indexForm.getTemplate(this.dialect, factory.getSqlFunctionRegistry());
               this.indexFormulas[i] = indexForm.getFormula();
               hasFormula = true;
            } else {
               Column indexCol = (Column)s;
               this.indexColumnNames[i] = indexCol.getQuotedName(this.dialect);
               this.indexColumnIsSettable[i] = true;
            }
         }

         this.indexContainsFormula = hasFormula;
         this.baseIndex = indexedCollection.isList() ? ((List)indexedCollection).getBaseIndex() : 0;
         this.indexNodeName = indexedCollection.getIndexNodeName();
      } else {
         this.indexContainsFormula = false;
         this.indexColumnIsSettable = null;
         this.indexFormulaTemplates = null;
         this.indexFormulas = null;
         this.indexType = null;
         this.indexColumnNames = null;
         this.indexColumnAliases = null;
         this.baseIndex = 0;
         this.indexNodeName = null;
      }

      this.hasIdentifier = collection.isIdentified();
      if (this.hasIdentifier) {
         if (collection.isOneToMany()) {
            throw new MappingException("one-to-many collections with identifiers are not supported");
         }

         IdentifierCollection idColl = (IdentifierCollection)collection;
         this.identifierType = idColl.getIdentifier().getType();
         iter = idColl.getIdentifier().getColumnIterator();
         Column col = (Column)iter.next();
         this.identifierColumnName = col.getQuotedName(this.dialect);
         this.identifierColumnAlias = col.getAlias(this.dialect);
         this.identifierGenerator = idColl.getIdentifier().createIdentifierGenerator(cfg.getIdentifierGeneratorFactory(), factory.getDialect(), factory.getSettings().getDefaultCatalogName(), factory.getSettings().getDefaultSchemaName(), (RootClass)null);
      } else {
         this.identifierType = null;
         this.identifierColumnName = null;
         this.identifierColumnAlias = null;
         this.identifierGenerator = null;
      }

      if (collection.getCustomSQLInsert() == null) {
         this.sqlInsertRowString = this.generateInsertRowString();
         this.insertCallable = false;
         this.insertCheckStyle = ExecuteUpdateResultCheckStyle.COUNT;
      } else {
         this.sqlInsertRowString = collection.getCustomSQLInsert();
         this.insertCallable = collection.isCustomInsertCallable();
         this.insertCheckStyle = collection.getCustomSQLInsertCheckStyle() == null ? ExecuteUpdateResultCheckStyle.determineDefault(collection.getCustomSQLInsert(), this.insertCallable) : collection.getCustomSQLInsertCheckStyle();
      }

      if (collection.getCustomSQLUpdate() == null) {
         this.sqlUpdateRowString = this.generateUpdateRowString();
         this.updateCallable = false;
         this.updateCheckStyle = ExecuteUpdateResultCheckStyle.COUNT;
      } else {
         this.sqlUpdateRowString = collection.getCustomSQLUpdate();
         this.updateCallable = collection.isCustomUpdateCallable();
         this.updateCheckStyle = collection.getCustomSQLUpdateCheckStyle() == null ? ExecuteUpdateResultCheckStyle.determineDefault(collection.getCustomSQLUpdate(), this.insertCallable) : collection.getCustomSQLUpdateCheckStyle();
      }

      if (collection.getCustomSQLDelete() == null) {
         this.sqlDeleteRowString = this.generateDeleteRowString();
         this.deleteCallable = false;
         this.deleteCheckStyle = ExecuteUpdateResultCheckStyle.NONE;
      } else {
         this.sqlDeleteRowString = collection.getCustomSQLDelete();
         this.deleteCallable = collection.isCustomDeleteCallable();
         this.deleteCheckStyle = ExecuteUpdateResultCheckStyle.NONE;
      }

      if (collection.getCustomSQLDeleteAll() == null) {
         this.sqlDeleteString = this.generateDeleteString();
         this.deleteAllCallable = false;
         this.deleteAllCheckStyle = ExecuteUpdateResultCheckStyle.NONE;
      } else {
         this.sqlDeleteString = collection.getCustomSQLDeleteAll();
         this.deleteAllCallable = collection.isCustomDeleteAllCallable();
         this.deleteAllCheckStyle = ExecuteUpdateResultCheckStyle.NONE;
      }

      this.sqlSelectSizeString = this.generateSelectSizeString(collection.isIndexed() && !collection.isMap());
      this.sqlDetectRowByIndexString = this.generateDetectRowByIndexString();
      this.sqlDetectRowByElementString = this.generateDetectRowByElementString();
      this.sqlSelectRowByIndexString = this.generateSelectRowByIndexString();
      this.logStaticSQL();
      this.isLazy = collection.isLazy();
      this.isExtraLazy = collection.isExtraLazy();
      this.isInverse = collection.isInverse();
      if (collection.isArray()) {
         this.elementClass = ((Array)collection).getElementClass();
      } else {
         this.elementClass = null;
      }

      if (this.elementType.isComponentType()) {
         this.elementPropertyMapping = new CompositeElementPropertyMapping(this.elementColumnNames, this.elementColumnReaders, this.elementColumnReaderTemplates, this.elementFormulaTemplates, (CompositeType)this.elementType, factory);
      } else if (!this.elementType.isEntityType()) {
         this.elementPropertyMapping = new ElementPropertyMapping(this.elementColumnNames, this.elementType);
      } else if (this.elementPersister instanceof PropertyMapping) {
         this.elementPropertyMapping = (PropertyMapping)this.elementPersister;
      } else {
         this.elementPropertyMapping = new ElementPropertyMapping(this.elementColumnNames, this.elementType);
      }

      this.hasOrder = collection.getOrderBy() != null;
      if (this.hasOrder) {
         this.orderByTranslation = Template.translateOrderBy(collection.getOrderBy(), new ColumnMapperImpl(), factory, this.dialect, factory.getSqlFunctionRegistry());
      } else {
         this.orderByTranslation = null;
      }

      this.filterHelper = new FilterHelper(collection.getFilters(), factory);
      this.manyToManyFilterHelper = new FilterHelper(collection.getManyToManyFilters(), factory);
      this.manyToManyWhereString = StringHelper.isNotEmpty(collection.getManyToManyWhere()) ? "( " + collection.getManyToManyWhere() + ")" : null;
      this.manyToManyWhereTemplate = this.manyToManyWhereString == null ? null : Template.renderWhereStringTemplate(this.manyToManyWhereString, factory.getDialect(), factory.getSqlFunctionRegistry());
      this.hasManyToManyOrder = collection.getManyToManyOrdering() != null;
      if (this.hasManyToManyOrder) {
         this.manyToManyOrderByTranslation = Template.translateOrderBy(collection.getManyToManyOrdering(), new ColumnMapperImpl(), factory, this.dialect, factory.getSqlFunctionRegistry());
      } else {
         this.manyToManyOrderByTranslation = null;
      }

      this.initCollectionPropertyMap();
   }

   private String[] formulaTemplates(String reference, int expectedSize) {
      try {
         int propertyIndex = this.elementPersister.getEntityMetamodel().getPropertyIndex(reference);
         return ((Queryable)this.elementPersister).getSubclassPropertyFormulaTemplateClosure()[propertyIndex];
      } catch (Exception var4) {
         return new String[expectedSize];
      }
   }

   public void postInstantiate() throws MappingException {
      this.initializer = (CollectionInitializer)(this.queryLoaderName == null ? this.createCollectionInitializer(LoadQueryInfluencers.NONE) : new NamedQueryCollectionInitializer(this.queryLoaderName, this));
   }

   protected void logStaticSQL() {
      if (LOG.isDebugEnabled()) {
         LOG.debugf("Static SQL for collection: %s", this.getRole());
         if (this.getSQLInsertRowString() != null) {
            LOG.debugf(" Row insert: %s", this.getSQLInsertRowString());
         }

         if (this.getSQLUpdateRowString() != null) {
            LOG.debugf(" Row update: %s", this.getSQLUpdateRowString());
         }

         if (this.getSQLDeleteRowString() != null) {
            LOG.debugf(" Row delete: %s", this.getSQLDeleteRowString());
         }

         if (this.getSQLDeleteString() != null) {
            LOG.debugf(" One-shot delete: %s", this.getSQLDeleteString());
         }
      }

   }

   public void initialize(Serializable key, SessionImplementor session) throws HibernateException {
      this.getAppropriateInitializer(key, session).initialize(key, session);
   }

   protected CollectionInitializer getAppropriateInitializer(Serializable key, SessionImplementor session) {
      if (this.queryLoaderName != null) {
         return this.initializer;
      } else {
         CollectionInitializer subselectInitializer = this.getSubselectInitializer(key, session);
         if (subselectInitializer != null) {
            return subselectInitializer;
         } else {
            return session.getEnabledFilters().isEmpty() ? this.initializer : this.createCollectionInitializer(session.getLoadQueryInfluencers());
         }
      }
   }

   private CollectionInitializer getSubselectInitializer(Serializable key, SessionImplementor session) {
      if (!this.isSubselectLoadable()) {
         return null;
      } else {
         PersistenceContext persistenceContext = session.getPersistenceContext();
         SubselectFetch subselect = persistenceContext.getBatchFetchQueue().getSubselect(session.generateEntityKey(key, this.getOwnerEntityPersister()));
         if (subselect == null) {
            return null;
         } else {
            Iterator iter = subselect.getResult().iterator();

            while(iter.hasNext()) {
               if (!persistenceContext.containsEntity((EntityKey)iter.next())) {
                  iter.remove();
               }
            }

            return this.createSubselectInitializer(subselect, session);
         }
      }
   }

   protected abstract CollectionInitializer createSubselectInitializer(SubselectFetch var1, SessionImplementor var2);

   protected abstract CollectionInitializer createCollectionInitializer(LoadQueryInfluencers var1) throws MappingException;

   public CollectionRegionAccessStrategy getCacheAccessStrategy() {
      return this.cacheAccessStrategy;
   }

   public boolean hasCache() {
      return this.cacheAccessStrategy != null;
   }

   public CollectionType getCollectionType() {
      return this.collectionType;
   }

   protected String getSQLWhereString(String alias) {
      return StringHelper.replace(this.sqlWhereStringTemplate, "$PlaceHolder$", alias);
   }

   public String getSQLOrderByString(String alias) {
      return this.hasOrdering() ? this.orderByTranslation.injectAliases(new StandardOrderByAliasResolver(alias)) : "";
   }

   public String getManyToManyOrderByString(String alias) {
      return this.hasManyToManyOrdering() ? this.manyToManyOrderByTranslation.injectAliases(new StandardOrderByAliasResolver(alias)) : "";
   }

   public FetchMode getFetchMode() {
      return this.fetchMode;
   }

   public boolean hasOrdering() {
      return this.hasOrder;
   }

   public boolean hasManyToManyOrdering() {
      return this.isManyToMany() && this.hasManyToManyOrder;
   }

   public boolean hasWhere() {
      return this.hasWhere;
   }

   protected String getSQLDeleteString() {
      return this.sqlDeleteString;
   }

   protected String getSQLInsertRowString() {
      return this.sqlInsertRowString;
   }

   protected String getSQLUpdateRowString() {
      return this.sqlUpdateRowString;
   }

   protected String getSQLDeleteRowString() {
      return this.sqlDeleteRowString;
   }

   public Type getKeyType() {
      return this.keyType;
   }

   public Type getIndexType() {
      return this.indexType;
   }

   public Type getElementType() {
      return this.elementType;
   }

   public Class getElementClass() {
      return this.elementClass;
   }

   public Object readElement(ResultSet rs, Object owner, String[] aliases, SessionImplementor session) throws HibernateException, SQLException {
      return this.getElementType().nullSafeGet(rs, aliases, session, owner);
   }

   public Object readIndex(ResultSet rs, String[] aliases, SessionImplementor session) throws HibernateException, SQLException {
      Object index = this.getIndexType().nullSafeGet(rs, (String[])aliases, session, (Object)null);
      if (index == null) {
         throw new HibernateException("null index column for collection: " + this.role);
      } else {
         index = this.decrementIndexByBase(index);
         return index;
      }
   }

   protected Object decrementIndexByBase(Object index) {
      if (this.baseIndex != 0) {
         index = (Integer)index - this.baseIndex;
      }

      return index;
   }

   public Object readIdentifier(ResultSet rs, String alias, SessionImplementor session) throws HibernateException, SQLException {
      Object id = this.getIdentifierType().nullSafeGet(rs, (String)alias, session, (Object)null);
      if (id == null) {
         throw new HibernateException("null identifier column for collection: " + this.role);
      } else {
         return id;
      }
   }

   public Object readKey(ResultSet rs, String[] aliases, SessionImplementor session) throws HibernateException, SQLException {
      return this.getKeyType().nullSafeGet(rs, (String[])aliases, session, (Object)null);
   }

   protected int writeKey(PreparedStatement st, Serializable key, int i, SessionImplementor session) throws HibernateException, SQLException {
      if (key == null) {
         throw new NullPointerException("null key for collection: " + this.role);
      } else {
         this.getKeyType().nullSafeSet(st, key, i, session);
         return i + this.keyColumnAliases.length;
      }
   }

   protected int writeElement(PreparedStatement st, Object elt, int i, SessionImplementor session) throws HibernateException, SQLException {
      this.getElementType().nullSafeSet(st, elt, i, this.elementColumnIsSettable, session);
      return i + ArrayHelper.countTrue(this.elementColumnIsSettable);
   }

   protected int writeIndex(PreparedStatement st, Object index, int i, SessionImplementor session) throws HibernateException, SQLException {
      this.getIndexType().nullSafeSet(st, this.incrementIndexByBase(index), i, this.indexColumnIsSettable, session);
      return i + ArrayHelper.countTrue(this.indexColumnIsSettable);
   }

   protected Object incrementIndexByBase(Object index) {
      if (this.baseIndex != 0) {
         index = (Integer)index + this.baseIndex;
      }

      return index;
   }

   protected int writeElementToWhere(PreparedStatement st, Object elt, int i, SessionImplementor session) throws HibernateException, SQLException {
      if (this.elementIsPureFormula) {
         throw new AssertionFailure("cannot use a formula-based element in the where condition");
      } else {
         this.getElementType().nullSafeSet(st, elt, i, this.elementColumnIsInPrimaryKey, session);
         return i + this.elementColumnAliases.length;
      }
   }

   protected int writeIndexToWhere(PreparedStatement st, Object index, int i, SessionImplementor session) throws HibernateException, SQLException {
      if (this.indexContainsFormula) {
         throw new AssertionFailure("cannot use a formula-based index in the where condition");
      } else {
         this.getIndexType().nullSafeSet(st, this.incrementIndexByBase(index), i, session);
         return i + this.indexColumnAliases.length;
      }
   }

   public int writeIdentifier(PreparedStatement st, Object id, int i, SessionImplementor session) throws HibernateException, SQLException {
      this.getIdentifierType().nullSafeSet(st, id, i, session);
      return i + 1;
   }

   public boolean isPrimitiveArray() {
      return this.isPrimitiveArray;
   }

   public boolean isArray() {
      return this.isArray;
   }

   public String[] getKeyColumnAliases(String suffix) {
      return (new Alias(suffix)).toAliasStrings(this.keyColumnAliases);
   }

   public String[] getElementColumnAliases(String suffix) {
      return (new Alias(suffix)).toAliasStrings(this.elementColumnAliases);
   }

   public String[] getIndexColumnAliases(String suffix) {
      return this.hasIndex ? (new Alias(suffix)).toAliasStrings(this.indexColumnAliases) : null;
   }

   public String getIdentifierColumnAlias(String suffix) {
      return this.hasIdentifier ? (new Alias(suffix)).toAliasString(this.identifierColumnAlias) : null;
   }

   public String getIdentifierColumnName() {
      return this.hasIdentifier ? this.identifierColumnName : null;
   }

   public String selectFragment(String alias, String columnSuffix) {
      SelectFragment frag = this.generateSelectFragment(alias, columnSuffix);
      this.appendElementColumns(frag, alias);
      this.appendIndexColumns(frag, alias);
      this.appendIdentifierColumns(frag, alias);
      return frag.toFragmentString().substring(2);
   }

   protected String generateSelectSizeString(boolean isIntegerIndexed) {
      String selectValue = isIntegerIndexed ? "max(" + this.getIndexColumnNames()[0] + ") + 1" : "count(" + this.getElementColumnNames()[0] + ")";
      return (new SimpleSelect(this.dialect)).setTableName(this.getTableName()).addCondition(this.getKeyColumnNames(), "=?").addColumn(selectValue).toStatementString();
   }

   protected String generateDetectRowByIndexString() {
      return !this.hasIndex() ? null : (new SimpleSelect(this.dialect)).setTableName(this.getTableName()).addCondition(this.getKeyColumnNames(), "=?").addCondition(this.getIndexColumnNames(), "=?").addCondition(this.indexFormulas, "=?").addColumn("1").toStatementString();
   }

   protected String generateSelectRowByIndexString() {
      return !this.hasIndex() ? null : (new SimpleSelect(this.dialect)).setTableName(this.getTableName()).addCondition(this.getKeyColumnNames(), "=?").addCondition(this.getIndexColumnNames(), "=?").addCondition(this.indexFormulas, "=?").addColumns(this.getElementColumnNames(), this.elementColumnAliases).addColumns(this.indexFormulas, this.indexColumnAliases).toStatementString();
   }

   protected String generateDetectRowByElementString() {
      return (new SimpleSelect(this.dialect)).setTableName(this.getTableName()).addCondition(this.getKeyColumnNames(), "=?").addCondition(this.getElementColumnNames(), "=?").addCondition(this.elementFormulas, "=?").addColumn("1").toStatementString();
   }

   protected SelectFragment generateSelectFragment(String alias, String columnSuffix) {
      return (new SelectFragment()).setSuffix(columnSuffix).addColumns(alias, this.keyColumnNames, this.keyColumnAliases);
   }

   protected void appendElementColumns(SelectFragment frag, String elemAlias) {
      for(int i = 0; i < this.elementColumnIsSettable.length; ++i) {
         if (this.elementColumnIsSettable[i]) {
            frag.addColumnTemplate(elemAlias, this.elementColumnReaderTemplates[i], this.elementColumnAliases[i]);
         } else {
            frag.addFormula(elemAlias, this.elementFormulaTemplates[i], this.elementColumnAliases[i]);
         }
      }

   }

   protected void appendIndexColumns(SelectFragment frag, String alias) {
      if (this.hasIndex) {
         for(int i = 0; i < this.indexColumnIsSettable.length; ++i) {
            if (this.indexColumnIsSettable[i]) {
               frag.addColumn(alias, this.indexColumnNames[i], this.indexColumnAliases[i]);
            } else {
               frag.addFormula(alias, this.indexFormulaTemplates[i], this.indexColumnAliases[i]);
            }
         }
      }

   }

   protected void appendIdentifierColumns(SelectFragment frag, String alias) {
      if (this.hasIdentifier) {
         frag.addColumn(alias, this.identifierColumnName, this.identifierColumnAlias);
      }

   }

   public String[] getIndexColumnNames() {
      return this.indexColumnNames;
   }

   public String[] getIndexFormulas() {
      return this.indexFormulas;
   }

   public String[] getIndexColumnNames(String alias) {
      return qualify(alias, this.indexColumnNames, this.indexFormulaTemplates);
   }

   public String[] getElementColumnNames(String alias) {
      return qualify(alias, this.elementColumnNames, this.elementFormulaTemplates);
   }

   private static String[] qualify(String alias, String[] columnNames, String[] formulaTemplates) {
      int span = columnNames.length;
      String[] result = new String[span];

      for(int i = 0; i < span; ++i) {
         if (columnNames[i] == null) {
            result[i] = StringHelper.replace(formulaTemplates[i], "$PlaceHolder$", alias);
         } else {
            result[i] = StringHelper.qualify(alias, columnNames[i]);
         }
      }

      return result;
   }

   public String[] getElementColumnNames() {
      return this.elementColumnNames;
   }

   public String[] getKeyColumnNames() {
      return this.keyColumnNames;
   }

   public boolean hasIndex() {
      return this.hasIndex;
   }

   public boolean isLazy() {
      return this.isLazy;
   }

   public boolean isInverse() {
      return this.isInverse;
   }

   public String getTableName() {
      return this.qualifiedTableName;
   }

   public void remove(Serializable id, SessionImplementor session) throws HibernateException {
      if (!this.isInverse && this.isRowDeleteEnabled()) {
         if (LOG.isDebugEnabled()) {
            LOG.debugf("Deleting collection: %s", MessageHelper.collectionInfoString(this, (Serializable)id, this.getFactory()));
         }

         try {
            int offset = 1;
            PreparedStatement st = null;
            Expectation expectation = Expectations.appropriateExpectation(this.getDeleteAllCheckStyle());
            boolean callable = this.isDeleteAllCallable();
            boolean useBatch = expectation.canBeBatched();
            String sql = this.getSQLDeleteString();
            if (useBatch) {
               if (this.removeBatchKey == null) {
                  this.removeBatchKey = new BasicBatchKey(this.getRole() + "#REMOVE", expectation);
               }

               st = session.getTransactionCoordinator().getJdbcCoordinator().getBatch(this.removeBatchKey).getBatchStatement(sql, callable);
            } else {
               st = session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(sql, callable);
            }

            try {
               offset += expectation.prepare(st);
               this.writeKey(st, id, offset, session);
               if (useBatch) {
                  session.getTransactionCoordinator().getJdbcCoordinator().getBatch(this.removeBatchKey).addToBatch();
               } else {
                  expectation.verifyOutcome(st.executeUpdate(), st, -1);
               }
            } catch (SQLException sqle) {
               if (useBatch) {
                  session.getTransactionCoordinator().getJdbcCoordinator().abortBatch();
               }

               throw sqle;
            } finally {
               if (!useBatch) {
                  st.close();
               }

            }

            LOG.debug("Done deleting collection");
         } catch (SQLException sqle) {
            throw this.sqlExceptionHelper.convert(sqle, "could not delete collection: " + MessageHelper.collectionInfoString(this, (Serializable)id, this.getFactory()), this.getSQLDeleteString());
         }
      }

   }

   public void recreate(PersistentCollection collection, Serializable id, SessionImplementor session) throws HibernateException {
      if (!this.isInverse && this.isRowInsertEnabled()) {
         if (LOG.isDebugEnabled()) {
            LOG.debugf("Inserting collection: %s", MessageHelper.collectionInfoString(this, collection, id, session));
         }

         try {
            Iterator entries = collection.entries(this);
            if (entries.hasNext()) {
               Expectation expectation = Expectations.appropriateExpectation(this.getInsertCheckStyle());
               collection.preInsert(this);
               int i = 0;

               int count;
               for(count = 0; entries.hasNext(); ++i) {
                  Object entry = entries.next();
                  if (collection.entryExists(entry, i)) {
                     int offset = 1;
                     PreparedStatement st = null;
                     boolean callable = this.isInsertCallable();
                     boolean useBatch = expectation.canBeBatched();
                     String sql = this.getSQLInsertRowString();
                     if (useBatch) {
                        if (this.recreateBatchKey == null) {
                           this.recreateBatchKey = new BasicBatchKey(this.getRole() + "#RECREATE", expectation);
                        }

                        st = session.getTransactionCoordinator().getJdbcCoordinator().getBatch(this.recreateBatchKey).getBatchStatement(sql, callable);
                     } else {
                        st = session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(sql, callable);
                     }

                     try {
                        offset += expectation.prepare(st);
                        int loc = this.writeKey(st, id, offset, session);
                        if (this.hasIdentifier) {
                           loc = this.writeIdentifier(st, collection.getIdentifier(entry, i), loc, session);
                        }

                        if (this.hasIndex) {
                           loc = this.writeIndex(st, collection.getIndex(entry, i, this), loc, session);
                        }

                        this.writeElement(st, collection.getElement(entry), loc, session);
                        if (useBatch) {
                           session.getTransactionCoordinator().getJdbcCoordinator().getBatch(this.recreateBatchKey).addToBatch();
                        } else {
                           expectation.verifyOutcome(st.executeUpdate(), st, -1);
                        }

                        collection.afterRowInsert(this, entry, i);
                        ++count;
                     } catch (SQLException sqle) {
                        if (useBatch) {
                           session.getTransactionCoordinator().getJdbcCoordinator().abortBatch();
                        }

                        throw sqle;
                     } finally {
                        if (!useBatch) {
                           st.close();
                        }

                     }
                  }
               }

               LOG.debugf("Done inserting collection: %s rows inserted", count);
            } else {
               LOG.debug("Collection was empty");
            }
         } catch (SQLException sqle) {
            throw this.sqlExceptionHelper.convert(sqle, "could not insert collection: " + MessageHelper.collectionInfoString(this, collection, id, session), this.getSQLInsertRowString());
         }
      }

   }

   protected boolean isRowDeleteEnabled() {
      return true;
   }

   public void deleteRows(PersistentCollection collection, Serializable id, SessionImplementor session) throws HibernateException {
      if (!this.isInverse && this.isRowDeleteEnabled()) {
         if (LOG.isDebugEnabled()) {
            LOG.debugf("Deleting rows of collection: %s", MessageHelper.collectionInfoString(this, collection, id, session));
         }

         boolean deleteByIndex = !this.isOneToMany() && this.hasIndex && !this.indexContainsFormula;
         Expectation expectation = Expectations.appropriateExpectation(this.getDeleteCheckStyle());

         try {
            Iterator deletes = collection.getDeletes(this, !deleteByIndex);
            if (deletes.hasNext()) {
               int offset = 1;

               for(int count = 0; deletes.hasNext(); LOG.debugf("Done deleting collection rows: %s deleted", count)) {
                  PreparedStatement st = null;
                  boolean callable = this.isDeleteCallable();
                  boolean useBatch = expectation.canBeBatched();
                  String sql = this.getSQLDeleteRowString();
                  if (useBatch) {
                     if (this.deleteBatchKey == null) {
                        this.deleteBatchKey = new BasicBatchKey(this.getRole() + "#DELETE", expectation);
                     }

                     st = session.getTransactionCoordinator().getJdbcCoordinator().getBatch(this.deleteBatchKey).getBatchStatement(sql, callable);
                  } else {
                     st = session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(sql, callable);
                  }

                  try {
                     expectation.prepare(st);
                     Object entry = deletes.next();
                     if (this.hasIdentifier) {
                        this.writeIdentifier(st, entry, offset, session);
                     } else {
                        int loc = this.writeKey(st, id, offset, session);
                        if (deleteByIndex) {
                           this.writeIndexToWhere(st, entry, loc, session);
                        } else {
                           this.writeElementToWhere(st, entry, loc, session);
                        }
                     }

                     if (useBatch) {
                        session.getTransactionCoordinator().getJdbcCoordinator().getBatch(this.deleteBatchKey).addToBatch();
                     } else {
                        expectation.verifyOutcome(st.executeUpdate(), st, -1);
                     }

                     ++count;
                  } catch (SQLException sqle) {
                     if (useBatch) {
                        session.getTransactionCoordinator().getJdbcCoordinator().abortBatch();
                     }

                     throw sqle;
                  } finally {
                     if (!useBatch) {
                        st.close();
                     }

                  }
               }
            } else {
               LOG.debug("No rows to delete");
            }
         } catch (SQLException sqle) {
            throw this.sqlExceptionHelper.convert(sqle, "could not delete collection rows: " + MessageHelper.collectionInfoString(this, collection, id, session), this.getSQLDeleteRowString());
         }
      }

   }

   protected boolean isRowInsertEnabled() {
      return true;
   }

   public void insertRows(PersistentCollection collection, Serializable id, SessionImplementor session) throws HibernateException {
      if (!this.isInverse && this.isRowInsertEnabled()) {
         if (LOG.isDebugEnabled()) {
            LOG.debugf("Inserting rows of collection: %s", MessageHelper.collectionInfoString(this, collection, id, session));
         }

         try {
            collection.preInsert(this);
            Iterator entries = collection.entries(this);
            Expectation expectation = Expectations.appropriateExpectation(this.getInsertCheckStyle());
            boolean callable = this.isInsertCallable();
            boolean useBatch = expectation.canBeBatched();
            String sql = this.getSQLInsertRowString();
            int i = 0;

            int count;
            for(count = 0; entries.hasNext(); ++i) {
               int offset = 1;
               Object entry = entries.next();
               PreparedStatement st = null;
               if (collection.needsInserting(entry, i, this.elementType)) {
                  if (useBatch) {
                     if (this.insertBatchKey == null) {
                        this.insertBatchKey = new BasicBatchKey(this.getRole() + "#INSERT", expectation);
                     }

                     if (st == null) {
                        st = session.getTransactionCoordinator().getJdbcCoordinator().getBatch(this.insertBatchKey).getBatchStatement(sql, callable);
                     }
                  } else {
                     st = session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(sql, callable);
                  }

                  try {
                     offset += expectation.prepare(st);
                     offset = this.writeKey(st, id, offset, session);
                     if (this.hasIdentifier) {
                        offset = this.writeIdentifier(st, collection.getIdentifier(entry, i), offset, session);
                     }

                     if (this.hasIndex) {
                        offset = this.writeIndex(st, collection.getIndex(entry, i, this), offset, session);
                     }

                     this.writeElement(st, collection.getElement(entry), offset, session);
                     if (useBatch) {
                        session.getTransactionCoordinator().getJdbcCoordinator().getBatch(this.insertBatchKey).addToBatch();
                     } else {
                        expectation.verifyOutcome(st.executeUpdate(), st, -1);
                     }

                     collection.afterRowInsert(this, entry, i);
                     ++count;
                  } catch (SQLException sqle) {
                     if (useBatch) {
                        session.getTransactionCoordinator().getJdbcCoordinator().abortBatch();
                     }

                     throw sqle;
                  } finally {
                     if (!useBatch) {
                        st.close();
                     }

                  }
               }
            }

            LOG.debugf("Done inserting rows: %s inserted", count);
         } catch (SQLException sqle) {
            throw this.sqlExceptionHelper.convert(sqle, "could not insert collection rows: " + MessageHelper.collectionInfoString(this, collection, id, session), this.getSQLInsertRowString());
         }
      }

   }

   public String getRole() {
      return this.role;
   }

   public String getOwnerEntityName() {
      return this.entityName;
   }

   public EntityPersister getOwnerEntityPersister() {
      return this.ownerPersister;
   }

   public IdentifierGenerator getIdentifierGenerator() {
      return this.identifierGenerator;
   }

   public Type getIdentifierType() {
      return this.identifierType;
   }

   public boolean hasOrphanDelete() {
      return this.hasOrphanDelete;
   }

   public Type toType(String propertyName) throws QueryException {
      return "index".equals(propertyName) ? this.indexType : this.elementPropertyMapping.toType(propertyName);
   }

   public abstract boolean isManyToMany();

   public String getManyToManyFilterFragment(String alias, Map enabledFilters) {
      StringBuilder buffer = new StringBuilder();
      this.manyToManyFilterHelper.render(buffer, this.elementPersister.getFilterAliasGenerator(alias), enabledFilters);
      if (this.manyToManyWhereString != null) {
         buffer.append(" and ").append(StringHelper.replace(this.manyToManyWhereTemplate, "$PlaceHolder$", alias));
      }

      return buffer.toString();
   }

   public String[] toColumns(String alias, String propertyName) throws QueryException {
      return "index".equals(propertyName) ? qualify(alias, this.indexColumnNames, this.indexFormulaTemplates) : this.elementPropertyMapping.toColumns(alias, propertyName);
   }

   public String[] toColumns(String propertyName) throws QueryException {
      if (!"index".equals(propertyName)) {
         return this.elementPropertyMapping.toColumns(propertyName);
      } else {
         if (this.indexFragments == null) {
            String[] tmp = new String[this.indexColumnNames.length];

            for(int i = 0; i < this.indexColumnNames.length; ++i) {
               tmp[i] = this.indexColumnNames[i] == null ? this.indexFormulas[i] : this.indexColumnNames[i];
               this.indexFragments = tmp;
            }
         }

         return this.indexFragments;
      }
   }

   public Type getType() {
      return this.elementPropertyMapping.getType();
   }

   public String getName() {
      return this.getRole();
   }

   public EntityPersister getElementPersister() {
      if (this.elementPersister == null) {
         throw new AssertionFailure("not an association");
      } else {
         return this.elementPersister;
      }
   }

   public boolean isCollection() {
      return true;
   }

   public Serializable[] getCollectionSpaces() {
      return this.spaces;
   }

   protected abstract String generateDeleteString();

   protected abstract String generateDeleteRowString();

   protected abstract String generateUpdateRowString();

   protected abstract String generateInsertRowString();

   public void updateRows(PersistentCollection collection, Serializable id, SessionImplementor session) throws HibernateException {
      if (!this.isInverse && collection.isRowUpdatePossible()) {
         LOG.debugf("Updating rows of collection: %s#%s", this.role, id);
         int count = this.doUpdateRows(id, collection, session);
         LOG.debugf("Done updating rows: %s updated", count);
      }

   }

   protected abstract int doUpdateRows(Serializable var1, PersistentCollection var2, SessionImplementor var3) throws HibernateException;

   public CollectionMetadata getCollectionMetadata() {
      return this;
   }

   public SessionFactoryImplementor getFactory() {
      return this.factory;
   }

   protected String filterFragment(String alias) throws MappingException {
      return this.hasWhere() ? " and " + this.getSQLWhereString(alias) : "";
   }

   public String filterFragment(String alias, Map enabledFilters) throws MappingException {
      StringBuilder sessionFilterFragment = new StringBuilder();
      this.filterHelper.render(sessionFilterFragment, this.getFilterAliasGenerator(alias), enabledFilters);
      return sessionFilterFragment.append(this.filterFragment(alias)).toString();
   }

   public String oneToManyFilterFragment(String alias) throws MappingException {
      return "";
   }

   protected boolean isInsertCallable() {
      return this.insertCallable;
   }

   protected ExecuteUpdateResultCheckStyle getInsertCheckStyle() {
      return this.insertCheckStyle;
   }

   protected boolean isUpdateCallable() {
      return this.updateCallable;
   }

   protected ExecuteUpdateResultCheckStyle getUpdateCheckStyle() {
      return this.updateCheckStyle;
   }

   protected boolean isDeleteCallable() {
      return this.deleteCallable;
   }

   protected ExecuteUpdateResultCheckStyle getDeleteCheckStyle() {
      return this.deleteCheckStyle;
   }

   protected boolean isDeleteAllCallable() {
      return this.deleteAllCallable;
   }

   protected ExecuteUpdateResultCheckStyle getDeleteAllCheckStyle() {
      return this.deleteAllCheckStyle;
   }

   public String toString() {
      return StringHelper.unqualify(this.getClass().getName()) + '(' + this.role + ')';
   }

   public boolean isVersioned() {
      return this.isVersioned && this.getOwnerEntityPersister().isVersioned();
   }

   public String getNodeName() {
      return this.nodeName;
   }

   public String getElementNodeName() {
      return this.elementNodeName;
   }

   public String getIndexNodeName() {
      return this.indexNodeName;
   }

   protected SQLExceptionConverter getSQLExceptionConverter() {
      return this.getSQLExceptionHelper().getSqlExceptionConverter();
   }

   protected SqlExceptionHelper getSQLExceptionHelper() {
      return this.sqlExceptionHelper;
   }

   public CacheEntryStructure getCacheEntryStructure() {
      return this.cacheEntryStructure;
   }

   public boolean isAffectedByEnabledFilters(SessionImplementor session) {
      return this.filterHelper.isAffectedBy(session.getEnabledFilters()) || this.isManyToMany() && this.manyToManyFilterHelper.isAffectedBy(session.getEnabledFilters());
   }

   public boolean isSubselectLoadable() {
      return this.subselectLoadable;
   }

   public boolean isMutable() {
      return this.isMutable;
   }

   public String[] getCollectionPropertyColumnAliases(String propertyName, String suffix) {
      String[] rawAliases = (String[])this.collectionPropertyColumnAliases.get(propertyName);
      if (rawAliases == null) {
         return null;
      } else {
         String[] result = new String[rawAliases.length];

         for(int i = 0; i < rawAliases.length; ++i) {
            result[i] = (new Alias(suffix)).toUnquotedAliasString(rawAliases[i]);
         }

         return result;
      }
   }

   public void initCollectionPropertyMap() {
      this.initCollectionPropertyMap("key", this.keyType, this.keyColumnAliases, this.keyColumnNames);
      this.initCollectionPropertyMap("element", this.elementType, this.elementColumnAliases, this.elementColumnNames);
      if (this.hasIndex) {
         this.initCollectionPropertyMap("index", this.indexType, this.indexColumnAliases, this.indexColumnNames);
      }

      if (this.hasIdentifier) {
         this.initCollectionPropertyMap("id", this.identifierType, new String[]{this.identifierColumnAlias}, new String[]{this.identifierColumnName});
      }

   }

   private void initCollectionPropertyMap(String aliasName, Type type, String[] columnAliases, String[] columnNames) {
      this.collectionPropertyColumnAliases.put(aliasName, columnAliases);
      this.collectionPropertyColumnNames.put(aliasName, columnNames);
      if (type.isComponentType()) {
         CompositeType ct = (CompositeType)type;
         String[] propertyNames = ct.getPropertyNames();

         for(int i = 0; i < propertyNames.length; ++i) {
            String name = propertyNames[i];
            this.collectionPropertyColumnAliases.put(aliasName + "." + name, columnAliases[i]);
            this.collectionPropertyColumnNames.put(aliasName + "." + name, columnNames[i]);
         }
      }

   }

   public int getSize(Serializable key, SessionImplementor session) {
      try {
         PreparedStatement st = session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(this.sqlSelectSizeString);

         int var5;
         try {
            this.getKeyType().nullSafeSet(st, key, 1, session);
            ResultSet rs = st.executeQuery();

            try {
               var5 = rs.next() ? rs.getInt(1) - this.baseIndex : 0;
            } finally {
               rs.close();
            }
         } finally {
            st.close();
         }

         return var5;
      } catch (SQLException sqle) {
         throw this.getFactory().getSQLExceptionHelper().convert(sqle, "could not retrieve collection size: " + MessageHelper.collectionInfoString(this, (Serializable)key, this.getFactory()), this.sqlSelectSizeString);
      }
   }

   public boolean indexExists(Serializable key, Object index, SessionImplementor session) {
      return this.exists(key, this.incrementIndexByBase(index), this.getIndexType(), this.sqlDetectRowByIndexString, session);
   }

   public boolean elementExists(Serializable key, Object element, SessionImplementor session) {
      return this.exists(key, element, this.getElementType(), this.sqlDetectRowByElementString, session);
   }

   private boolean exists(Serializable key, Object indexOrElement, Type indexOrElementType, String sql, SessionImplementor session) {
      try {
         PreparedStatement st = session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(sql);

         boolean var8;
         try {
            this.getKeyType().nullSafeSet(st, key, 1, session);
            indexOrElementType.nullSafeSet(st, indexOrElement, this.keyColumnNames.length + 1, session);
            ResultSet rs = st.executeQuery();

            try {
               var8 = rs.next();
               return var8;
            } finally {
               rs.close();
            }
         } catch (TransientObjectException var20) {
            var8 = false;
         } finally {
            st.close();
         }

         return var8;
      } catch (SQLException sqle) {
         throw this.getFactory().getSQLExceptionHelper().convert(sqle, "could not check row existence: " + MessageHelper.collectionInfoString(this, (Serializable)key, this.getFactory()), this.sqlSelectSizeString);
      }
   }

   public Object getElementByIndex(Serializable key, Object index, SessionImplementor session, Object owner) {
      try {
         PreparedStatement st = session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(this.sqlSelectRowByIndexString);

         Object var7;
         try {
            this.getKeyType().nullSafeSet(st, key, 1, session);
            this.getIndexType().nullSafeSet(st, this.incrementIndexByBase(index), this.keyColumnNames.length + 1, session);
            ResultSet rs = st.executeQuery();

            try {
               if (!rs.next()) {
                  var7 = null;
                  return var7;
               }

               var7 = this.getElementType().nullSafeGet(rs, this.elementColumnAliases, session, owner);
            } finally {
               rs.close();
            }
         } finally {
            st.close();
         }

         return var7;
      } catch (SQLException sqle) {
         throw this.getFactory().getSQLExceptionHelper().convert(sqle, "could not read row: " + MessageHelper.collectionInfoString(this, (Serializable)key, this.getFactory()), this.sqlSelectSizeString);
      }
   }

   public boolean isExtraLazy() {
      return this.isExtraLazy;
   }

   protected Dialect getDialect() {
      return this.dialect;
   }

   public CollectionInitializer getInitializer() {
      return this.initializer;
   }

   public int getBatchSize() {
      return this.batchSize;
   }

   public abstract FilterAliasGenerator getFilterAliasGenerator(String var1);

   private class ColumnMapperImpl implements ColumnMapper {
      private ColumnMapperImpl() {
      }

      public SqlValueReference[] map(String reference) {
         String[] columnNames;
         String[] formulaTemplates;
         if ("$element$".equals(reference)) {
            columnNames = AbstractCollectionPersister.this.elementColumnNames;
            formulaTemplates = AbstractCollectionPersister.this.elementFormulaTemplates;
         } else {
            columnNames = AbstractCollectionPersister.this.elementPropertyMapping.toColumns(reference);
            formulaTemplates = AbstractCollectionPersister.this.formulaTemplates(reference, columnNames.length);
         }

         SqlValueReference[] result = new SqlValueReference[columnNames.length];
         int i = 0;

         for(final String columnName : columnNames) {
            if (columnName == null) {
               int propertyIndex = AbstractCollectionPersister.this.elementPersister.getEntityMetamodel().getPropertyIndex(reference);
               final String formulaTemplate = formulaTemplates[i];
               result[i] = new FormulaReference() {
                  public String getFormulaFragment() {
                     return formulaTemplate;
                  }
               };
            } else {
               result[i] = new ColumnReference() {
                  public String getColumnName() {
                     return columnName;
                  }
               };
            }

            ++i;
         }

         return result;
      }
   }

   private class StandardOrderByAliasResolver implements OrderByAliasResolver {
      private final String rootAlias;

      private StandardOrderByAliasResolver(String rootAlias) {
         this.rootAlias = rootAlias;
      }

      public String resolveTableAlias(String columnReference) {
         return AbstractCollectionPersister.this.elementPersister == null ? this.rootAlias : ((Loadable)AbstractCollectionPersister.this.elementPersister).getTableAliasForColumn(columnReference, this.rootAlias);
      }
   }
}
