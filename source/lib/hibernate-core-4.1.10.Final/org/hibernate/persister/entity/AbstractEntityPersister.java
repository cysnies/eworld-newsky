package org.hibernate.persister.entity;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.AssertionFailure;
import org.hibernate.EntityMode;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.MappingException;
import org.hibernate.QueryException;
import org.hibernate.StaleObjectStateException;
import org.hibernate.StaleStateException;
import org.hibernate.bytecode.instrumentation.spi.FieldInterceptor;
import org.hibernate.bytecode.instrumentation.spi.LazyPropertyInitializer;
import org.hibernate.bytecode.spi.EntityInstrumentationMetadata;
import org.hibernate.cache.spi.CacheKey;
import org.hibernate.cache.spi.access.EntityRegionAccessStrategy;
import org.hibernate.cache.spi.access.NaturalIdRegionAccessStrategy;
import org.hibernate.cache.spi.entry.CacheEntry;
import org.hibernate.cache.spi.entry.CacheEntryStructure;
import org.hibernate.cache.spi.entry.StructuredCacheEntry;
import org.hibernate.cache.spi.entry.UnstructuredCacheEntry;
import org.hibernate.dialect.lock.LockingStrategy;
import org.hibernate.engine.OptimisticLockStyle;
import org.hibernate.engine.internal.StatefulPersistenceContext;
import org.hibernate.engine.internal.Versioning;
import org.hibernate.engine.jdbc.batch.internal.BasicBatchKey;
import org.hibernate.engine.spi.CachedNaturalIdValueSource;
import org.hibernate.engine.spi.CascadeStyle;
import org.hibernate.engine.spi.CascadingAction;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.ExecuteUpdateResultCheckStyle;
import org.hibernate.engine.spi.FilterDefinition;
import org.hibernate.engine.spi.LoadQueryInfluencers;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.ValueInclusion;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.PostInsertIdentifierGenerator;
import org.hibernate.id.PostInsertIdentityPersister;
import org.hibernate.id.insert.Binder;
import org.hibernate.id.insert.InsertGeneratedIdentifierDelegate;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.FilterConfiguration;
import org.hibernate.internal.FilterHelper;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.jdbc.Expectation;
import org.hibernate.jdbc.Expectations;
import org.hibernate.jdbc.TooManyRowsAffectedException;
import org.hibernate.loader.entity.BatchingEntityLoader;
import org.hibernate.loader.entity.CascadeEntityLoader;
import org.hibernate.loader.entity.EntityLoader;
import org.hibernate.loader.entity.UniqueEntityLoader;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.Selectable;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metamodel.binding.AssociationAttributeBinding;
import org.hibernate.metamodel.binding.AttributeBinding;
import org.hibernate.metamodel.binding.EntityBinding;
import org.hibernate.metamodel.binding.SimpleValueBinding;
import org.hibernate.metamodel.binding.SingularAttributeBinding;
import org.hibernate.metamodel.relational.DerivedValue;
import org.hibernate.metamodel.relational.Value;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.property.BackrefPropertyAccessor;
import org.hibernate.sql.Alias;
import org.hibernate.sql.Delete;
import org.hibernate.sql.Insert;
import org.hibernate.sql.JoinFragment;
import org.hibernate.sql.JoinType;
import org.hibernate.sql.Select;
import org.hibernate.sql.SelectFragment;
import org.hibernate.sql.SimpleSelect;
import org.hibernate.sql.Template;
import org.hibernate.sql.Update;
import org.hibernate.tuple.entity.EntityMetamodel;
import org.hibernate.tuple.entity.EntityTuplizer;
import org.hibernate.type.AssociationType;
import org.hibernate.type.CompositeType;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;
import org.hibernate.type.TypeHelper;
import org.hibernate.type.VersionType;
import org.jboss.logging.Logger;

public abstract class AbstractEntityPersister implements OuterJoinLoadable, Queryable, ClassMetadata, UniqueKeyLoadable, SQLLoadable, LazyPropertyInitializer, PostInsertIdentityPersister, Lockable {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, AbstractEntityPersister.class.getName());
   public static final String ENTITY_CLASS = "class";
   private final SessionFactoryImplementor factory;
   private final EntityRegionAccessStrategy cacheAccessStrategy;
   private final NaturalIdRegionAccessStrategy naturalIdRegionAccessStrategy;
   private final boolean isLazyPropertiesCacheable;
   private final CacheEntryStructure cacheEntryStructure;
   private final EntityMetamodel entityMetamodel;
   private final EntityTuplizer entityTuplizer;
   private final String[] rootTableKeyColumnNames;
   private final String[] rootTableKeyColumnReaders;
   private final String[] rootTableKeyColumnReaderTemplates;
   private final String[] identifierAliases;
   private final int identifierColumnSpan;
   private final String versionColumnName;
   private final boolean hasFormulaProperties;
   private final int batchSize;
   private final boolean hasSubselectLoadableCollections;
   protected final String rowIdName;
   private final Set lazyProperties;
   private final String sqlWhereString;
   private final String sqlWhereStringTemplate;
   private final int[] propertyColumnSpans;
   private final String[] propertySubclassNames;
   private final String[][] propertyColumnAliases;
   private final String[][] propertyColumnNames;
   private final String[][] propertyColumnFormulaTemplates;
   private final String[][] propertyColumnReaderTemplates;
   private final String[][] propertyColumnWriters;
   private final boolean[][] propertyColumnUpdateable;
   private final boolean[][] propertyColumnInsertable;
   private final boolean[] propertyUniqueness;
   private final boolean[] propertySelectable;
   private final List lobProperties = new ArrayList();
   private final String[] lazyPropertyNames;
   private final int[] lazyPropertyNumbers;
   private final Type[] lazyPropertyTypes;
   private final String[][] lazyPropertyColumnAliases;
   private final String[] subclassPropertyNameClosure;
   private final String[] subclassPropertySubclassNameClosure;
   private final Type[] subclassPropertyTypeClosure;
   private final String[][] subclassPropertyFormulaTemplateClosure;
   private final String[][] subclassPropertyColumnNameClosure;
   private final String[][] subclassPropertyColumnReaderClosure;
   private final String[][] subclassPropertyColumnReaderTemplateClosure;
   private final FetchMode[] subclassPropertyFetchModeClosure;
   private final boolean[] subclassPropertyNullabilityClosure;
   private final boolean[] propertyDefinedOnSubclass;
   private final int[][] subclassPropertyColumnNumberClosure;
   private final int[][] subclassPropertyFormulaNumberClosure;
   private final CascadeStyle[] subclassPropertyCascadeStyleClosure;
   private final String[] subclassColumnClosure;
   private final boolean[] subclassColumnLazyClosure;
   private final String[] subclassColumnAliasClosure;
   private final boolean[] subclassColumnSelectableClosure;
   private final String[] subclassColumnReaderTemplateClosure;
   private final String[] subclassFormulaClosure;
   private final String[] subclassFormulaTemplateClosure;
   private final String[] subclassFormulaAliasClosure;
   private final boolean[] subclassFormulaLazyClosure;
   private final FilterHelper filterHelper;
   private final Set affectingFetchProfileNames = new HashSet();
   private final Map uniqueKeyLoaders = new HashMap();
   private final Map lockers = new HashMap();
   private final Map loaders = new HashMap();
   private String sqlVersionSelectString;
   private String sqlSnapshotSelectString;
   private String sqlLazySelectString;
   private String sqlIdentityInsertString;
   private String sqlUpdateByRowIdString;
   private String sqlLazyUpdateByRowIdString;
   private String[] sqlDeleteStrings;
   private String[] sqlInsertStrings;
   private String[] sqlUpdateStrings;
   private String[] sqlLazyUpdateStrings;
   private String sqlInsertGeneratedValuesSelectString;
   private String sqlUpdateGeneratedValuesSelectString;
   protected boolean[] insertCallable;
   protected boolean[] updateCallable;
   protected boolean[] deleteCallable;
   protected String[] customSQLInsert;
   protected String[] customSQLUpdate;
   protected String[] customSQLDelete;
   protected ExecuteUpdateResultCheckStyle[] insertResultCheckStyles;
   protected ExecuteUpdateResultCheckStyle[] updateResultCheckStyles;
   protected ExecuteUpdateResultCheckStyle[] deleteResultCheckStyles;
   private InsertGeneratedIdentifierDelegate identityDelegate;
   private boolean[] tableHasColumns;
   private final String loaderName;
   private UniqueEntityLoader queryLoader;
   private final String temporaryIdTableName;
   private final String temporaryIdTableDDL;
   private final Map subclassPropertyAliases = new HashMap();
   private final Map subclassPropertyColumnNames = new HashMap();
   protected final BasicEntityPropertyMapping propertyMapping;
   private static final String DISCRIMINATOR_ALIAS = "clazz_";
   private DiscriminatorMetadata discriminatorMetadata;
   private BasicBatchKey inserBatchKey;
   private BasicBatchKey updateBatchKey;
   private BasicBatchKey deleteBatchKey;
   private Boolean naturalIdIsNonNullable;
   private String cachedPkByNonNullableNaturalIdQuery;

   protected void addDiscriminatorToInsert(Insert insert) {
   }

   protected void addDiscriminatorToSelect(SelectFragment select, String name, String suffix) {
   }

   protected abstract int[] getSubclassColumnTableNumberClosure();

   protected abstract int[] getSubclassFormulaTableNumberClosure();

   public abstract String getSubclassTableName(int var1);

   protected abstract String[] getSubclassTableKeyColumns(int var1);

   protected abstract boolean isClassOrSuperclassTable(int var1);

   protected abstract int getSubclassTableSpan();

   protected abstract int getTableSpan();

   protected abstract boolean isTableCascadeDeleteEnabled(int var1);

   protected abstract String getTableName(int var1);

   protected abstract String[] getKeyColumns(int var1);

   protected abstract boolean isPropertyOfTable(int var1, int var2);

   protected abstract int[] getPropertyTableNumbersInSelect();

   protected abstract int[] getPropertyTableNumbers();

   protected abstract int getSubclassPropertyTableNumber(int var1);

   protected abstract String filterFragment(String var1) throws MappingException;

   public String getDiscriminatorColumnName() {
      return "clazz_";
   }

   public String getDiscriminatorColumnReaders() {
      return "clazz_";
   }

   public String getDiscriminatorColumnReaderTemplate() {
      return "clazz_";
   }

   protected String getDiscriminatorAlias() {
      return "clazz_";
   }

   protected String getDiscriminatorFormulaTemplate() {
      return null;
   }

   protected boolean isInverseTable(int j) {
      return false;
   }

   protected boolean isNullableTable(int j) {
      return false;
   }

   protected boolean isNullableSubclassTable(int j) {
      return false;
   }

   protected boolean isInverseSubclassTable(int j) {
      return false;
   }

   public boolean isSubclassEntityName(String entityName) {
      return this.entityMetamodel.getSubclassEntityNames().contains(entityName);
   }

   private boolean[] getTableHasColumns() {
      return this.tableHasColumns;
   }

   public String[] getRootTableKeyColumnNames() {
      return this.rootTableKeyColumnNames;
   }

   protected String[] getSQLUpdateByRowIdStrings() {
      if (this.sqlUpdateByRowIdString == null) {
         throw new AssertionFailure("no update by row id");
      } else {
         String[] result = new String[this.getTableSpan() + 1];
         result[0] = this.sqlUpdateByRowIdString;
         System.arraycopy(this.sqlUpdateStrings, 0, result, 1, this.getTableSpan());
         return result;
      }
   }

   protected String[] getSQLLazyUpdateByRowIdStrings() {
      if (this.sqlLazyUpdateByRowIdString == null) {
         throw new AssertionFailure("no update by row id");
      } else {
         String[] result = new String[this.getTableSpan()];
         result[0] = this.sqlLazyUpdateByRowIdString;

         for(int i = 1; i < this.getTableSpan(); ++i) {
            result[i] = this.sqlLazyUpdateStrings[i];
         }

         return result;
      }
   }

   protected String getSQLSnapshotSelectString() {
      return this.sqlSnapshotSelectString;
   }

   protected String getSQLLazySelectString() {
      return this.sqlLazySelectString;
   }

   protected String[] getSQLDeleteStrings() {
      return this.sqlDeleteStrings;
   }

   protected String[] getSQLInsertStrings() {
      return this.sqlInsertStrings;
   }

   protected String[] getSQLUpdateStrings() {
      return this.sqlUpdateStrings;
   }

   protected String[] getSQLLazyUpdateStrings() {
      return this.sqlLazyUpdateStrings;
   }

   protected String getSQLIdentityInsertString() {
      return this.sqlIdentityInsertString;
   }

   protected String getVersionSelectString() {
      return this.sqlVersionSelectString;
   }

   protected boolean isInsertCallable(int j) {
      return this.insertCallable[j];
   }

   protected boolean isUpdateCallable(int j) {
      return this.updateCallable[j];
   }

   protected boolean isDeleteCallable(int j) {
      return this.deleteCallable[j];
   }

   protected boolean isSubclassPropertyDeferred(String propertyName, String entityName) {
      return false;
   }

   protected boolean isSubclassTableSequentialSelect(int j) {
      return false;
   }

   public boolean hasSequentialSelect() {
      return false;
   }

   protected boolean[] getTableUpdateNeeded(int[] dirtyProperties, boolean hasDirtyCollection) {
      if (dirtyProperties == null) {
         return this.getTableHasColumns();
      } else {
         boolean[] updateability = this.getPropertyUpdateability();
         int[] propertyTableNumbers = this.getPropertyTableNumbers();
         boolean[] tableUpdateNeeded = new boolean[this.getTableSpan()];

         for(int i = 0; i < dirtyProperties.length; ++i) {
            int property = dirtyProperties[i];
            int table = propertyTableNumbers[property];
            tableUpdateNeeded[table] = tableUpdateNeeded[table] || this.getPropertyColumnSpan(property) > 0 && updateability[property];
         }

         if (this.isVersioned()) {
            tableUpdateNeeded[0] = tableUpdateNeeded[0] || Versioning.isVersionIncrementRequired(dirtyProperties, hasDirtyCollection, this.getPropertyVersionability());
         }

         return tableUpdateNeeded;
      }
   }

   public boolean hasRowId() {
      return this.rowIdName != null;
   }

   protected boolean[][] getPropertyColumnUpdateable() {
      return this.propertyColumnUpdateable;
   }

   protected boolean[][] getPropertyColumnInsertable() {
      return this.propertyColumnInsertable;
   }

   protected boolean[] getPropertySelectable() {
      return this.propertySelectable;
   }

   public AbstractEntityPersister(PersistentClass persistentClass, EntityRegionAccessStrategy cacheAccessStrategy, NaturalIdRegionAccessStrategy naturalIdRegionAccessStrategy, SessionFactoryImplementor factory) throws HibernateException {
      this.factory = factory;
      this.cacheAccessStrategy = cacheAccessStrategy;
      this.naturalIdRegionAccessStrategy = naturalIdRegionAccessStrategy;
      this.isLazyPropertiesCacheable = persistentClass.isLazyPropertiesCacheable();
      this.cacheEntryStructure = (CacheEntryStructure)(factory.getSettings().isStructuredCacheEntriesEnabled() ? new StructuredCacheEntry(this) : new UnstructuredCacheEntry());
      this.entityMetamodel = new EntityMetamodel(persistentClass, factory);
      this.entityTuplizer = this.entityMetamodel.getTuplizer();
      int batch = persistentClass.getBatchSize();
      if (batch == -1) {
         batch = factory.getSettings().getDefaultBatchFetchSize();
      }

      this.batchSize = batch;
      this.hasSubselectLoadableCollections = persistentClass.hasSubselectLoadableCollections();
      this.propertyMapping = new BasicEntityPropertyMapping(this);
      this.identifierColumnSpan = persistentClass.getIdentifier().getColumnSpan();
      this.rootTableKeyColumnNames = new String[this.identifierColumnSpan];
      this.rootTableKeyColumnReaders = new String[this.identifierColumnSpan];
      this.rootTableKeyColumnReaderTemplates = new String[this.identifierColumnSpan];
      this.identifierAliases = new String[this.identifierColumnSpan];
      this.rowIdName = persistentClass.getRootTable().getRowId();
      this.loaderName = persistentClass.getLoaderName();
      Iterator iter = persistentClass.getIdentifier().getColumnIterator();

      for(int i = 0; iter.hasNext(); ++i) {
         Column col = (Column)iter.next();
         this.rootTableKeyColumnNames[i] = col.getQuotedName(factory.getDialect());
         this.rootTableKeyColumnReaders[i] = col.getReadExpr(factory.getDialect());
         this.rootTableKeyColumnReaderTemplates[i] = col.getTemplate(factory.getDialect(), factory.getSqlFunctionRegistry());
         this.identifierAliases[i] = col.getAlias(factory.getDialect(), persistentClass.getRootTable());
      }

      if (persistentClass.isVersioned()) {
         this.versionColumnName = ((Column)persistentClass.getVersion().getColumnIterator().next()).getQuotedName(factory.getDialect());
      } else {
         this.versionColumnName = null;
      }

      this.sqlWhereString = StringHelper.isNotEmpty(persistentClass.getWhere()) ? "( " + persistentClass.getWhere() + ") " : null;
      this.sqlWhereStringTemplate = this.sqlWhereString == null ? null : Template.renderWhereStringTemplate(this.sqlWhereString, factory.getDialect(), factory.getSqlFunctionRegistry());
      boolean lazyAvailable = this.isInstrumented();
      int hydrateSpan = this.entityMetamodel.getPropertySpan();
      this.propertyColumnSpans = new int[hydrateSpan];
      this.propertySubclassNames = new String[hydrateSpan];
      this.propertyColumnAliases = new String[hydrateSpan][];
      this.propertyColumnNames = new String[hydrateSpan][];
      this.propertyColumnFormulaTemplates = new String[hydrateSpan][];
      this.propertyColumnReaderTemplates = new String[hydrateSpan][];
      this.propertyColumnWriters = new String[hydrateSpan][];
      this.propertyUniqueness = new boolean[hydrateSpan];
      this.propertySelectable = new boolean[hydrateSpan];
      this.propertyColumnUpdateable = new boolean[hydrateSpan][];
      this.propertyColumnInsertable = new boolean[hydrateSpan][];
      HashSet thisClassProperties = new HashSet();
      this.lazyProperties = new HashSet();
      ArrayList lazyNames = new ArrayList();
      ArrayList lazyNumbers = new ArrayList();
      ArrayList lazyTypes = new ArrayList();
      ArrayList lazyColAliases = new ArrayList();
      iter = persistentClass.getPropertyClosureIterator();
      int var58 = 0;

      boolean foundFormula;
      for(foundFormula = false; iter.hasNext(); ++var58) {
         Property prop = (Property)iter.next();
         thisClassProperties.add(prop);
         int span = prop.getColumnSpan();
         this.propertyColumnSpans[var58] = span;
         this.propertySubclassNames[var58] = prop.getPersistentClass().getEntityName();
         String[] colNames = new String[span];
         String[] colAliases = new String[span];
         String[] colReaderTemplates = new String[span];
         String[] colWriters = new String[span];
         String[] formulaTemplates = new String[span];
         Iterator colIter = prop.getColumnIterator();

         for(int k = 0; colIter.hasNext(); ++k) {
            Selectable thing = (Selectable)colIter.next();
            colAliases[k] = thing.getAlias(factory.getDialect(), prop.getValue().getTable());
            if (thing.isFormula()) {
               foundFormula = true;
               formulaTemplates[k] = thing.getTemplate(factory.getDialect(), factory.getSqlFunctionRegistry());
            } else {
               Column col = (Column)thing;
               colNames[k] = col.getQuotedName(factory.getDialect());
               colReaderTemplates[k] = col.getTemplate(factory.getDialect(), factory.getSqlFunctionRegistry());
               colWriters[k] = col.getWriteExpr();
            }
         }

         this.propertyColumnNames[var58] = colNames;
         this.propertyColumnFormulaTemplates[var58] = formulaTemplates;
         this.propertyColumnReaderTemplates[var58] = colReaderTemplates;
         this.propertyColumnWriters[var58] = colWriters;
         this.propertyColumnAliases[var58] = colAliases;
         if (lazyAvailable && prop.isLazy()) {
            this.lazyProperties.add(prop.getName());
            lazyNames.add(prop.getName());
            lazyNumbers.add(var58);
            lazyTypes.add(prop.getValue().getType());
            lazyColAliases.add(colAliases);
         }

         this.propertyColumnUpdateable[var58] = prop.getValue().getColumnUpdateability();
         this.propertyColumnInsertable[var58] = prop.getValue().getColumnInsertability();
         this.propertySelectable[var58] = prop.isSelectable();
         this.propertyUniqueness[var58] = prop.getValue().isAlternateUniqueKey();
         if (prop.isLob() && this.getFactory().getDialect().forceLobAsLastValue()) {
            this.lobProperties.add(var58);
         }
      }

      this.hasFormulaProperties = foundFormula;
      this.lazyPropertyColumnAliases = ArrayHelper.to2DStringArray(lazyColAliases);
      this.lazyPropertyNames = ArrayHelper.toStringArray((Collection)lazyNames);
      this.lazyPropertyNumbers = ArrayHelper.toIntArray(lazyNumbers);
      this.lazyPropertyTypes = ArrayHelper.toTypeArray(lazyTypes);
      ArrayList columns = new ArrayList();
      ArrayList columnsLazy = new ArrayList();
      ArrayList columnReaderTemplates = new ArrayList();
      ArrayList aliases = new ArrayList();
      ArrayList formulas = new ArrayList();
      ArrayList formulaAliases = new ArrayList();
      ArrayList formulaTemplates = new ArrayList();
      ArrayList formulasLazy = new ArrayList();
      ArrayList types = new ArrayList();
      ArrayList names = new ArrayList();
      ArrayList classes = new ArrayList();
      ArrayList templates = new ArrayList();
      ArrayList propColumns = new ArrayList();
      ArrayList propColumnReaders = new ArrayList();
      ArrayList propColumnReaderTemplates = new ArrayList();
      ArrayList joinedFetchesList = new ArrayList();
      ArrayList cascades = new ArrayList();
      ArrayList definedBySubclass = new ArrayList();
      ArrayList propColumnNumbers = new ArrayList();
      ArrayList propFormulaNumbers = new ArrayList();
      ArrayList columnSelectables = new ArrayList();
      ArrayList propNullables = new ArrayList();
      iter = persistentClass.getSubclassPropertyClosureIterator();

      while(iter.hasNext()) {
         Property prop = (Property)iter.next();
         names.add(prop.getName());
         classes.add(prop.getPersistentClass().getEntityName());
         boolean isDefinedBySubclass = !thisClassProperties.contains(prop);
         definedBySubclass.add(isDefinedBySubclass);
         propNullables.add(prop.isOptional() || isDefinedBySubclass);
         types.add(prop.getType());
         Iterator colIter = prop.getColumnIterator();
         String[] cols = new String[prop.getColumnSpan()];
         String[] readers = new String[prop.getColumnSpan()];
         String[] readerTemplates = new String[prop.getColumnSpan()];
         String[] forms = new String[prop.getColumnSpan()];
         int[] colnos = new int[prop.getColumnSpan()];
         int[] formnos = new int[prop.getColumnSpan()];
         int l = 0;

         for(Boolean lazy = prop.isLazy() && lazyAvailable; colIter.hasNext(); ++l) {
            Selectable thing = (Selectable)colIter.next();
            if (thing.isFormula()) {
               String template = thing.getTemplate(factory.getDialect(), factory.getSqlFunctionRegistry());
               formnos[l] = formulaTemplates.size();
               colnos[l] = -1;
               formulaTemplates.add(template);
               forms[l] = template;
               formulas.add(thing.getText(factory.getDialect()));
               formulaAliases.add(thing.getAlias(factory.getDialect()));
               formulasLazy.add(lazy);
            } else {
               Column col = (Column)thing;
               String colName = col.getQuotedName(factory.getDialect());
               colnos[l] = columns.size();
               formnos[l] = -1;
               columns.add(colName);
               cols[l] = colName;
               aliases.add(thing.getAlias(factory.getDialect(), prop.getValue().getTable()));
               columnsLazy.add(lazy);
               columnSelectables.add(prop.isSelectable());
               readers[l] = col.getReadExpr(factory.getDialect());
               String readerTemplate = col.getTemplate(factory.getDialect(), factory.getSqlFunctionRegistry());
               readerTemplates[l] = readerTemplate;
               columnReaderTemplates.add(readerTemplate);
            }
         }

         propColumns.add(cols);
         propColumnReaders.add(readers);
         propColumnReaderTemplates.add(readerTemplates);
         templates.add(forms);
         propColumnNumbers.add(colnos);
         propFormulaNumbers.add(formnos);
         joinedFetchesList.add(prop.getValue().getFetchMode());
         cascades.add(prop.getCascadeStyle());
      }

      this.subclassColumnClosure = ArrayHelper.toStringArray((Collection)columns);
      this.subclassColumnAliasClosure = ArrayHelper.toStringArray((Collection)aliases);
      this.subclassColumnLazyClosure = ArrayHelper.toBooleanArray(columnsLazy);
      this.subclassColumnSelectableClosure = ArrayHelper.toBooleanArray(columnSelectables);
      this.subclassColumnReaderTemplateClosure = ArrayHelper.toStringArray((Collection)columnReaderTemplates);
      this.subclassFormulaClosure = ArrayHelper.toStringArray((Collection)formulas);
      this.subclassFormulaTemplateClosure = ArrayHelper.toStringArray((Collection)formulaTemplates);
      this.subclassFormulaAliasClosure = ArrayHelper.toStringArray((Collection)formulaAliases);
      this.subclassFormulaLazyClosure = ArrayHelper.toBooleanArray(formulasLazy);
      this.subclassPropertyNameClosure = ArrayHelper.toStringArray((Collection)names);
      this.subclassPropertySubclassNameClosure = ArrayHelper.toStringArray((Collection)classes);
      this.subclassPropertyTypeClosure = ArrayHelper.toTypeArray(types);
      this.subclassPropertyNullabilityClosure = ArrayHelper.toBooleanArray(propNullables);
      this.subclassPropertyFormulaTemplateClosure = ArrayHelper.to2DStringArray(templates);
      this.subclassPropertyColumnNameClosure = ArrayHelper.to2DStringArray(propColumns);
      this.subclassPropertyColumnReaderClosure = ArrayHelper.to2DStringArray(propColumnReaders);
      this.subclassPropertyColumnReaderTemplateClosure = ArrayHelper.to2DStringArray(propColumnReaderTemplates);
      this.subclassPropertyColumnNumberClosure = ArrayHelper.to2DIntArray(propColumnNumbers);
      this.subclassPropertyFormulaNumberClosure = ArrayHelper.to2DIntArray(propFormulaNumbers);
      this.subclassPropertyCascadeStyleClosure = new CascadeStyle[cascades.size()];
      iter = cascades.iterator();

      for(int j = 0; iter.hasNext(); this.subclassPropertyCascadeStyleClosure[j++] = (CascadeStyle)iter.next()) {
      }

      this.subclassPropertyFetchModeClosure = new FetchMode[joinedFetchesList.size()];
      iter = joinedFetchesList.iterator();

      for(int var72 = 0; iter.hasNext(); this.subclassPropertyFetchModeClosure[var72++] = (FetchMode)iter.next()) {
      }

      this.propertyDefinedOnSubclass = new boolean[definedBySubclass.size()];
      iter = definedBySubclass.iterator();

      for(int var73 = 0; iter.hasNext(); this.propertyDefinedOnSubclass[var73++] = (Boolean)iter.next()) {
      }

      this.filterHelper = new FilterHelper(persistentClass.getFilters(), factory);
      this.temporaryIdTableName = persistentClass.getTemporaryIdTableName();
      this.temporaryIdTableDDL = persistentClass.getTemporaryIdTableDDL();
   }

   public AbstractEntityPersister(EntityBinding entityBinding, EntityRegionAccessStrategy cacheAccessStrategy, NaturalIdRegionAccessStrategy naturalIdRegionAccessStrategy, SessionFactoryImplementor factory) throws HibernateException {
      this.factory = factory;
      this.cacheAccessStrategy = cacheAccessStrategy;
      this.naturalIdRegionAccessStrategy = naturalIdRegionAccessStrategy;
      this.isLazyPropertiesCacheable = entityBinding.getHierarchyDetails().getCaching() == null ? false : entityBinding.getHierarchyDetails().getCaching().isCacheLazyProperties();
      this.cacheEntryStructure = (CacheEntryStructure)(factory.getSettings().isStructuredCacheEntriesEnabled() ? new StructuredCacheEntry(this) : new UnstructuredCacheEntry());
      this.entityMetamodel = new EntityMetamodel(entityBinding, factory);
      this.entityTuplizer = this.entityMetamodel.getTuplizer();
      int batch = entityBinding.getBatchSize();
      if (batch == -1) {
         batch = factory.getSettings().getDefaultBatchFetchSize();
      }

      this.batchSize = batch;
      this.hasSubselectLoadableCollections = entityBinding.hasSubselectLoadableCollections();
      this.propertyMapping = new BasicEntityPropertyMapping(this);
      this.identifierColumnSpan = entityBinding.getHierarchyDetails().getEntityIdentifier().getValueBinding().getSimpleValueSpan();
      this.rootTableKeyColumnNames = new String[this.identifierColumnSpan];
      this.rootTableKeyColumnReaders = new String[this.identifierColumnSpan];
      this.rootTableKeyColumnReaderTemplates = new String[this.identifierColumnSpan];
      this.identifierAliases = new String[this.identifierColumnSpan];
      this.rowIdName = entityBinding.getRowId();
      this.loaderName = entityBinding.getCustomLoaderName();
      int i = 0;

      for(org.hibernate.metamodel.relational.Column col : entityBinding.getPrimaryTable().getPrimaryKey().getColumns()) {
         this.rootTableKeyColumnNames[i] = col.getColumnName().encloseInQuotesIfQuoted(factory.getDialect());
         if (col.getReadFragment() == null) {
            this.rootTableKeyColumnReaders[i] = this.rootTableKeyColumnNames[i];
            this.rootTableKeyColumnReaderTemplates[i] = this.getTemplateFromColumn(col, factory);
         } else {
            this.rootTableKeyColumnReaders[i] = col.getReadFragment();
            this.rootTableKeyColumnReaderTemplates[i] = getTemplateFromString(col.getReadFragment(), factory);
         }

         this.identifierAliases[i] = col.getAlias(factory.getDialect());
         ++i;
      }

      if (entityBinding.isVersioned()) {
         Value versioningValue = entityBinding.getHierarchyDetails().getVersioningAttributeBinding().getValue();
         if (!org.hibernate.metamodel.relational.Column.class.isInstance(versioningValue)) {
            throw new AssertionFailure("Bad versioning attribute binding : " + versioningValue);
         }

         org.hibernate.metamodel.relational.Column versionColumn = (org.hibernate.metamodel.relational.Column)org.hibernate.metamodel.relational.Column.class.cast(versioningValue);
         this.versionColumnName = versionColumn.getColumnName().encloseInQuotesIfQuoted(factory.getDialect());
      } else {
         this.versionColumnName = null;
      }

      this.sqlWhereString = StringHelper.isNotEmpty(entityBinding.getWhereFilter()) ? "( " + entityBinding.getWhereFilter() + ") " : null;
      this.sqlWhereStringTemplate = getTemplateFromString(this.sqlWhereString, factory);
      boolean lazyAvailable = this.isInstrumented();
      int hydrateSpan = this.entityMetamodel.getPropertySpan();
      this.propertyColumnSpans = new int[hydrateSpan];
      this.propertySubclassNames = new String[hydrateSpan];
      this.propertyColumnAliases = new String[hydrateSpan][];
      this.propertyColumnNames = new String[hydrateSpan][];
      this.propertyColumnFormulaTemplates = new String[hydrateSpan][];
      this.propertyColumnReaderTemplates = new String[hydrateSpan][];
      this.propertyColumnWriters = new String[hydrateSpan][];
      this.propertyUniqueness = new boolean[hydrateSpan];
      this.propertySelectable = new boolean[hydrateSpan];
      this.propertyColumnUpdateable = new boolean[hydrateSpan][];
      this.propertyColumnInsertable = new boolean[hydrateSpan][];
      HashSet thisClassProperties = new HashSet();
      this.lazyProperties = new HashSet();
      ArrayList lazyNames = new ArrayList();
      ArrayList lazyNumbers = new ArrayList();
      ArrayList lazyTypes = new ArrayList();
      ArrayList lazyColAliases = new ArrayList();
      i = 0;
      boolean foundFormula = false;

      for(AttributeBinding attributeBinding : entityBinding.getAttributeBindingClosure()) {
         if (attributeBinding != entityBinding.getHierarchyDetails().getEntityIdentifier().getValueBinding() && attributeBinding.getAttribute().isSingular()) {
            SingularAttributeBinding singularAttributeBinding = (SingularAttributeBinding)attributeBinding;
            thisClassProperties.add(singularAttributeBinding);
            this.propertySubclassNames[i] = ((EntityBinding)singularAttributeBinding.getContainer()).getEntity().getName();
            int span = singularAttributeBinding.getSimpleValueSpan();
            this.propertyColumnSpans[i] = span;
            String[] colNames = new String[span];
            String[] colAliases = new String[span];
            String[] colReaderTemplates = new String[span];
            String[] colWriters = new String[span];
            String[] formulaTemplates = new String[span];
            boolean[] propertyColumnInsertability = new boolean[span];
            boolean[] propertyColumnUpdatability = new boolean[span];
            int k = 0;

            for(SimpleValueBinding valueBinding : singularAttributeBinding.getSimpleValueBindings()) {
               colAliases[k] = valueBinding.getSimpleValue().getAlias(factory.getDialect());
               if (valueBinding.isDerived()) {
                  foundFormula = true;
                  formulaTemplates[k] = getTemplateFromString(((DerivedValue)valueBinding.getSimpleValue()).getExpression(), factory);
               } else {
                  org.hibernate.metamodel.relational.Column col = (org.hibernate.metamodel.relational.Column)valueBinding.getSimpleValue();
                  colNames[k] = col.getColumnName().encloseInQuotesIfQuoted(factory.getDialect());
                  colReaderTemplates[k] = this.getTemplateFromColumn(col, factory);
                  colWriters[k] = col.getWriteFragment() == null ? "?" : col.getWriteFragment();
               }

               propertyColumnInsertability[k] = valueBinding.isIncludeInInsert();
               propertyColumnUpdatability[k] = valueBinding.isIncludeInUpdate();
               ++k;
            }

            this.propertyColumnNames[i] = colNames;
            this.propertyColumnFormulaTemplates[i] = formulaTemplates;
            this.propertyColumnReaderTemplates[i] = colReaderTemplates;
            this.propertyColumnWriters[i] = colWriters;
            this.propertyColumnAliases[i] = colAliases;
            this.propertyColumnUpdateable[i] = propertyColumnUpdatability;
            this.propertyColumnInsertable[i] = propertyColumnInsertability;
            if (lazyAvailable && singularAttributeBinding.isLazy()) {
               this.lazyProperties.add(singularAttributeBinding.getAttribute().getName());
               lazyNames.add(singularAttributeBinding.getAttribute().getName());
               lazyNumbers.add(i);
               lazyTypes.add(singularAttributeBinding.getHibernateTypeDescriptor().getResolvedTypeMapping());
               lazyColAliases.add(colAliases);
            }

            this.propertySelectable[i] = true;
            this.propertyUniqueness[i] = singularAttributeBinding.isAlternateUniqueKey();
            ++i;
         }
      }

      this.hasFormulaProperties = foundFormula;
      this.lazyPropertyColumnAliases = ArrayHelper.to2DStringArray(lazyColAliases);
      this.lazyPropertyNames = ArrayHelper.toStringArray((Collection)lazyNames);
      this.lazyPropertyNumbers = ArrayHelper.toIntArray(lazyNumbers);
      this.lazyPropertyTypes = ArrayHelper.toTypeArray(lazyTypes);
      List<String> columns = new ArrayList();
      List<Boolean> columnsLazy = new ArrayList();
      List<String> columnReaderTemplates = new ArrayList();
      List<String> aliases = new ArrayList();
      List<String> formulas = new ArrayList();
      List<String> formulaAliases = new ArrayList();
      List<String> formulaTemplates = new ArrayList();
      List<Boolean> formulasLazy = new ArrayList();
      List<Type> types = new ArrayList();
      List<String> names = new ArrayList();
      List<String> classes = new ArrayList();
      List<String[]> templates = new ArrayList();
      List<String[]> propColumns = new ArrayList();
      List<String[]> propColumnReaders = new ArrayList();
      List<String[]> propColumnReaderTemplates = new ArrayList();
      List<FetchMode> joinedFetchesList = new ArrayList();
      List<CascadeStyle> cascades = new ArrayList();
      List<Boolean> definedBySubclass = new ArrayList();
      List<int[]> propColumnNumbers = new ArrayList();
      List<int[]> propFormulaNumbers = new ArrayList();
      List<Boolean> columnSelectables = new ArrayList();
      List<Boolean> propNullables = new ArrayList();

      for(AttributeBinding attributeBinding : entityBinding.getSubEntityAttributeBindingClosure()) {
         if (attributeBinding != entityBinding.getHierarchyDetails().getEntityIdentifier().getValueBinding() && attributeBinding.getAttribute().isSingular()) {
            SingularAttributeBinding singularAttributeBinding = (SingularAttributeBinding)attributeBinding;
            names.add(singularAttributeBinding.getAttribute().getName());
            classes.add(((EntityBinding)singularAttributeBinding.getContainer()).getEntity().getName());
            boolean isDefinedBySubclass = !thisClassProperties.contains(singularAttributeBinding);
            definedBySubclass.add(isDefinedBySubclass);
            propNullables.add(singularAttributeBinding.isNullable() || isDefinedBySubclass);
            types.add(singularAttributeBinding.getHibernateTypeDescriptor().getResolvedTypeMapping());
            int span = singularAttributeBinding.getSimpleValueSpan();
            String[] cols = new String[span];
            String[] readers = new String[span];
            String[] readerTemplates = new String[span];
            String[] forms = new String[span];
            int[] colnos = new int[span];
            int[] formnos = new int[span];
            int l = 0;
            Boolean lazy = singularAttributeBinding.isLazy() && lazyAvailable;

            for(SimpleValueBinding valueBinding : singularAttributeBinding.getSimpleValueBindings()) {
               if (valueBinding.isDerived()) {
                  DerivedValue derivedValue = (DerivedValue)DerivedValue.class.cast(valueBinding.getSimpleValue());
                  String template = getTemplateFromString(derivedValue.getExpression(), factory);
                  formnos[l] = formulaTemplates.size();
                  colnos[l] = -1;
                  formulaTemplates.add(template);
                  forms[l] = template;
                  formulas.add(derivedValue.getExpression());
                  formulaAliases.add(derivedValue.getAlias(factory.getDialect()));
                  formulasLazy.add(lazy);
               } else {
                  org.hibernate.metamodel.relational.Column col = (org.hibernate.metamodel.relational.Column)org.hibernate.metamodel.relational.Column.class.cast(valueBinding.getSimpleValue());
                  String colName = col.getColumnName().encloseInQuotesIfQuoted(factory.getDialect());
                  colnos[l] = columns.size();
                  formnos[l] = -1;
                  columns.add(colName);
                  cols[l] = colName;
                  aliases.add(col.getAlias(factory.getDialect()));
                  columnsLazy.add(lazy);
                  columnSelectables.add(singularAttributeBinding.getAttribute().isSingular());
                  readers[l] = col.getReadFragment() == null ? col.getColumnName().encloseInQuotesIfQuoted(factory.getDialect()) : col.getReadFragment();
                  String readerTemplate = this.getTemplateFromColumn(col, factory);
                  readerTemplates[l] = readerTemplate;
                  columnReaderTemplates.add(readerTemplate);
               }

               ++l;
            }

            propColumns.add(cols);
            propColumnReaders.add(readers);
            propColumnReaderTemplates.add(readerTemplates);
            templates.add(forms);
            propColumnNumbers.add(colnos);
            propFormulaNumbers.add(formnos);
            if (singularAttributeBinding.isAssociation()) {
               AssociationAttributeBinding associationAttributeBinding = (AssociationAttributeBinding)singularAttributeBinding;
               cascades.add(associationAttributeBinding.getCascadeStyle());
               joinedFetchesList.add(associationAttributeBinding.getFetchMode());
            } else {
               cascades.add(CascadeStyle.NONE);
               joinedFetchesList.add(FetchMode.SELECT);
            }
         }
      }

      this.subclassColumnClosure = ArrayHelper.toStringArray((Collection)columns);
      this.subclassColumnAliasClosure = ArrayHelper.toStringArray((Collection)aliases);
      this.subclassColumnLazyClosure = ArrayHelper.toBooleanArray(columnsLazy);
      this.subclassColumnSelectableClosure = ArrayHelper.toBooleanArray(columnSelectables);
      this.subclassColumnReaderTemplateClosure = ArrayHelper.toStringArray((Collection)columnReaderTemplates);
      this.subclassFormulaClosure = ArrayHelper.toStringArray((Collection)formulas);
      this.subclassFormulaTemplateClosure = ArrayHelper.toStringArray((Collection)formulaTemplates);
      this.subclassFormulaAliasClosure = ArrayHelper.toStringArray((Collection)formulaAliases);
      this.subclassFormulaLazyClosure = ArrayHelper.toBooleanArray(formulasLazy);
      this.subclassPropertyNameClosure = ArrayHelper.toStringArray((Collection)names);
      this.subclassPropertySubclassNameClosure = ArrayHelper.toStringArray((Collection)classes);
      this.subclassPropertyTypeClosure = ArrayHelper.toTypeArray(types);
      this.subclassPropertyNullabilityClosure = ArrayHelper.toBooleanArray(propNullables);
      this.subclassPropertyFormulaTemplateClosure = ArrayHelper.to2DStringArray(templates);
      this.subclassPropertyColumnNameClosure = ArrayHelper.to2DStringArray(propColumns);
      this.subclassPropertyColumnReaderClosure = ArrayHelper.to2DStringArray(propColumnReaders);
      this.subclassPropertyColumnReaderTemplateClosure = ArrayHelper.to2DStringArray(propColumnReaderTemplates);
      this.subclassPropertyColumnNumberClosure = ArrayHelper.to2DIntArray(propColumnNumbers);
      this.subclassPropertyFormulaNumberClosure = ArrayHelper.to2DIntArray(propFormulaNumbers);
      this.subclassPropertyCascadeStyleClosure = (CascadeStyle[])cascades.toArray(new CascadeStyle[cascades.size()]);
      this.subclassPropertyFetchModeClosure = (FetchMode[])joinedFetchesList.toArray(new FetchMode[joinedFetchesList.size()]);
      this.propertyDefinedOnSubclass = ArrayHelper.toBooleanArray(definedBySubclass);
      List<FilterConfiguration> filterDefaultConditions = new ArrayList();

      for(FilterDefinition filterDefinition : entityBinding.getFilterDefinitions()) {
         filterDefaultConditions.add(new FilterConfiguration(filterDefinition.getFilterName(), filterDefinition.getDefaultFilterCondition(), true, (Map)null, (Map)null, (PersistentClass)null));
      }

      this.filterHelper = new FilterHelper(filterDefaultConditions, factory);
      this.temporaryIdTableName = null;
      this.temporaryIdTableDDL = null;
   }

   protected static String getTemplateFromString(String string, SessionFactoryImplementor factory) {
      return string == null ? null : Template.renderWhereStringTemplate(string, factory.getDialect(), factory.getSqlFunctionRegistry());
   }

   public String getTemplateFromColumn(org.hibernate.metamodel.relational.Column column, SessionFactoryImplementor factory) {
      String templateString;
      if (column.getReadFragment() != null) {
         templateString = getTemplateFromString(column.getReadFragment(), factory);
      } else {
         String columnName = column.getColumnName().encloseInQuotesIfQuoted(factory.getDialect());
         templateString = "$PlaceHolder$." + columnName;
      }

      return templateString;
   }

   protected String generateLazySelectString() {
      if (!this.entityMetamodel.hasLazyProperties()) {
         return null;
      } else {
         HashSet tableNumbers = new HashSet();
         ArrayList columnNumbers = new ArrayList();
         ArrayList formulaNumbers = new ArrayList();

         for(int i = 0; i < this.lazyPropertyNames.length; ++i) {
            int propertyNumber = this.getSubclassPropertyIndex(this.lazyPropertyNames[i]);
            int tableNumber = this.getSubclassPropertyTableNumber(propertyNumber);
            tableNumbers.add(tableNumber);
            int[] colNumbers = this.subclassPropertyColumnNumberClosure[propertyNumber];

            for(int j = 0; j < colNumbers.length; ++j) {
               if (colNumbers[j] != -1) {
                  columnNumbers.add(colNumbers[j]);
               }
            }

            int[] formNumbers = this.subclassPropertyFormulaNumberClosure[propertyNumber];

            for(int j = 0; j < formNumbers.length; ++j) {
               if (formNumbers[j] != -1) {
                  formulaNumbers.add(formNumbers[j]);
               }
            }
         }

         if (columnNumbers.size() == 0 && formulaNumbers.size() == 0) {
            return null;
         } else {
            return this.renderSelect(ArrayHelper.toIntArray(tableNumbers), ArrayHelper.toIntArray(columnNumbers), ArrayHelper.toIntArray(formulaNumbers));
         }
      }
   }

   public Object initializeLazyProperty(String fieldName, Object entity, SessionImplementor session) throws HibernateException {
      Serializable id = session.getContextEntityIdentifier(entity);
      EntityEntry entry = session.getPersistenceContext().getEntry(entity);
      if (entry == null) {
         throw new HibernateException("entity is not associated with the session: " + id);
      } else {
         if (LOG.isTraceEnabled()) {
            LOG.tracev("Initializing lazy properties of: {0}, field access: {1}", MessageHelper.infoString((EntityPersister)this, (Object)id, (SessionFactoryImplementor)this.getFactory()), fieldName);
         }

         if (this.hasCache()) {
            CacheKey cacheKey = session.generateCacheKey(id, this.getIdentifierType(), this.getEntityName());
            Object ce = this.getCacheAccessStrategy().get(cacheKey, session.getTimestamp());
            if (ce != null) {
               CacheEntry cacheEntry = (CacheEntry)this.getCacheEntryStructure().destructure(ce, this.factory);
               if (!cacheEntry.areLazyPropertiesUnfetched()) {
                  return this.initializeLazyPropertiesFromCache(fieldName, entity, session, entry, cacheEntry);
               }
            }
         }

         return this.initializeLazyPropertiesFromDatastore(fieldName, entity, session, id, entry);
      }
   }

   private Object initializeLazyPropertiesFromDatastore(String fieldName, Object entity, SessionImplementor session, Serializable id, EntityEntry entry) {
      if (!this.hasLazyProperties()) {
         throw new AssertionFailure("no lazy properties");
      } else {
         LOG.trace("Initializing lazy properties from datastore");

         try {
            Object result = null;
            PreparedStatement ps = null;

            try {
               String lazySelect = this.getSQLLazySelectString();
               ResultSet rs = null;

               try {
                  if (lazySelect != null) {
                     ps = session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(lazySelect);
                     this.getIdentifierType().nullSafeSet(ps, id, 1, session);
                     rs = ps.executeQuery();
                     rs.next();
                  }

                  Object[] snapshot = entry.getLoadedState();

                  for(int j = 0; j < this.lazyPropertyNames.length; ++j) {
                     Object propValue = this.lazyPropertyTypes[j].nullSafeGet(rs, this.lazyPropertyColumnAliases[j], session, entity);
                     if (this.initializeLazyProperty(fieldName, entity, session, snapshot, j, propValue)) {
                        result = propValue;
                     }
                  }
               } finally {
                  if (rs != null) {
                     rs.close();
                  }

               }
            } finally {
               if (ps != null) {
                  ps.close();
               }

            }

            LOG.trace("Done initializing lazy properties");
            return result;
         } catch (SQLException sqle) {
            throw this.getFactory().getSQLExceptionHelper().convert(sqle, "could not initialize lazy properties: " + MessageHelper.infoString((EntityPersister)this, (Object)id, (SessionFactoryImplementor)this.getFactory()), this.getSQLLazySelectString());
         }
      }
   }

   private Object initializeLazyPropertiesFromCache(String fieldName, Object entity, SessionImplementor session, EntityEntry entry, CacheEntry cacheEntry) {
      LOG.trace("Initializing lazy properties from second-level cache");
      Object result = null;
      Serializable[] disassembledValues = cacheEntry.getDisassembledState();
      Object[] snapshot = entry.getLoadedState();

      for(int j = 0; j < this.lazyPropertyNames.length; ++j) {
         Object propValue = this.lazyPropertyTypes[j].assemble(disassembledValues[this.lazyPropertyNumbers[j]], session, entity);
         if (this.initializeLazyProperty(fieldName, entity, session, snapshot, j, propValue)) {
            result = propValue;
         }
      }

      LOG.trace("Done initializing lazy properties");
      return result;
   }

   private boolean initializeLazyProperty(String fieldName, Object entity, SessionImplementor session, Object[] snapshot, int j, Object propValue) {
      this.setPropertyValue(entity, this.lazyPropertyNumbers[j], propValue);
      if (snapshot != null) {
         snapshot[this.lazyPropertyNumbers[j]] = this.lazyPropertyTypes[j].deepCopy(propValue, this.factory);
      }

      return fieldName.equals(this.lazyPropertyNames[j]);
   }

   public boolean isBatchable() {
      return this.optimisticLockStyle() == OptimisticLockStyle.NONE || !this.isVersioned() && this.optimisticLockStyle() == OptimisticLockStyle.VERSION || this.getFactory().getSettings().isJdbcBatchVersionedData();
   }

   public Serializable[] getQuerySpaces() {
      return this.getPropertySpaces();
   }

   protected Set getLazyProperties() {
      return this.lazyProperties;
   }

   public boolean isBatchLoadable() {
      return this.batchSize > 1;
   }

   public String[] getIdentifierColumnNames() {
      return this.rootTableKeyColumnNames;
   }

   public String[] getIdentifierColumnReaders() {
      return this.rootTableKeyColumnReaders;
   }

   public String[] getIdentifierColumnReaderTemplates() {
      return this.rootTableKeyColumnReaderTemplates;
   }

   protected int getIdentifierColumnSpan() {
      return this.identifierColumnSpan;
   }

   protected String[] getIdentifierAliases() {
      return this.identifierAliases;
   }

   public String getVersionColumnName() {
      return this.versionColumnName;
   }

   protected String getVersionedTableName() {
      return this.getTableName(0);
   }

   protected boolean[] getSubclassColumnLazyiness() {
      return this.subclassColumnLazyClosure;
   }

   protected boolean[] getSubclassFormulaLazyiness() {
      return this.subclassFormulaLazyClosure;
   }

   public boolean isCacheInvalidationRequired() {
      return this.hasFormulaProperties() || !this.isVersioned() && (this.entityMetamodel.isDynamicUpdate() || this.getTableSpan() > 1);
   }

   public boolean isLazyPropertiesCacheable() {
      return this.isLazyPropertiesCacheable;
   }

   public String selectFragment(String alias, String suffix) {
      return this.identifierSelectFragment(alias, suffix) + this.propertySelectFragment(alias, suffix, false);
   }

   public String[] getIdentifierAliases(String suffix) {
      return (new Alias(suffix)).toAliasStrings(this.getIdentifierAliases());
   }

   public String[] getPropertyAliases(String suffix, int i) {
      return (new Alias(suffix)).toUnquotedAliasStrings(this.propertyColumnAliases[i]);
   }

   public String getDiscriminatorAlias(String suffix) {
      return this.entityMetamodel.hasSubclasses() ? (new Alias(suffix)).toAliasString(this.getDiscriminatorAlias()) : null;
   }

   public String identifierSelectFragment(String name, String suffix) {
      return (new SelectFragment()).setSuffix(suffix).addColumns(name, this.getIdentifierColumnNames(), this.getIdentifierAliases()).toFragmentString().substring(2);
   }

   public String propertySelectFragment(String tableAlias, String suffix, boolean allProperties) {
      return this.propertySelectFragmentFragment(tableAlias, suffix, allProperties).toFragmentString();
   }

   public SelectFragment propertySelectFragmentFragment(String tableAlias, String suffix, boolean allProperties) {
      SelectFragment select = (new SelectFragment()).setSuffix(suffix).setUsedAliases(this.getIdentifierAliases());
      int[] columnTableNumbers = this.getSubclassColumnTableNumberClosure();
      String[] columnAliases = this.getSubclassColumnAliasClosure();
      String[] columnReaderTemplates = this.getSubclassColumnReaderTemplateClosure();

      for(int i = 0; i < this.getSubclassColumnClosure().length; ++i) {
         boolean selectable = (allProperties || !this.subclassColumnLazyClosure[i]) && !this.isSubclassTableSequentialSelect(columnTableNumbers[i]) && this.subclassColumnSelectableClosure[i];
         if (selectable) {
            String subalias = generateTableAlias(tableAlias, columnTableNumbers[i]);
            select.addColumnTemplate(subalias, columnReaderTemplates[i], columnAliases[i]);
         }
      }

      int[] formulaTableNumbers = this.getSubclassFormulaTableNumberClosure();
      String[] formulaTemplates = this.getSubclassFormulaTemplateClosure();
      String[] formulaAliases = this.getSubclassFormulaAliasClosure();

      for(int i = 0; i < this.getSubclassFormulaTemplateClosure().length; ++i) {
         boolean selectable = (allProperties || !this.subclassFormulaLazyClosure[i]) && !this.isSubclassTableSequentialSelect(formulaTableNumbers[i]);
         if (selectable) {
            String subalias = generateTableAlias(tableAlias, formulaTableNumbers[i]);
            select.addFormula(subalias, formulaTemplates[i], formulaAliases[i]);
         }
      }

      if (this.entityMetamodel.hasSubclasses()) {
         this.addDiscriminatorToSelect(select, tableAlias, suffix);
      }

      if (this.hasRowId()) {
         select.addColumn(tableAlias, this.rowIdName, "rowid_");
      }

      return select;
   }

   public Object[] getDatabaseSnapshot(Serializable id, SessionImplementor session) throws HibernateException {
      if (LOG.isTraceEnabled()) {
         LOG.tracev("Getting current persistent state for: {0}", MessageHelper.infoString((EntityPersister)this, (Object)id, (SessionFactoryImplementor)this.getFactory()));
      }

      try {
         PreparedStatement ps = session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(this.getSQLSnapshotSelectString());

         Type[] types;
         try {
            this.getIdentifierType().nullSafeSet(ps, id, 1, session);
            ResultSet rs = ps.executeQuery();

            try {
               if (rs.next()) {
                  types = this.getPropertyTypes();
                  Object[] values = new Object[types.length];
                  boolean[] includeProperty = this.getPropertyUpdateability();

                  for(int i = 0; i < types.length; ++i) {
                     if (includeProperty[i]) {
                        values[i] = types[i].hydrate(rs, this.getPropertyAliases("", i), session, (Object)null);
                     }
                  }

                  Object[] var20 = values;
                  return var20;
               }

               types = null;
            } finally {
               rs.close();
            }
         } finally {
            ps.close();
         }

         return types;
      } catch (SQLException e) {
         throw this.getFactory().getSQLExceptionHelper().convert(e, "could not retrieve snapshot: " + MessageHelper.infoString((EntityPersister)this, (Object)id, (SessionFactoryImplementor)this.getFactory()), this.getSQLSnapshotSelectString());
      }
   }

   public Serializable getIdByUniqueKey(Serializable key, String uniquePropertyName, SessionImplementor session) throws HibernateException {
      if (LOG.isTraceEnabled()) {
         LOG.tracef("resolving unique key [%s] to identifier for entity [%s]", key, this.getEntityName());
      }

      int propertyIndex = this.getSubclassPropertyIndex(uniquePropertyName);
      if (propertyIndex < 0) {
         throw new HibernateException("Could not determine Type for property [" + uniquePropertyName + "] on entity [" + this.getEntityName() + "]");
      } else {
         Type propertyType = this.getSubclassPropertyType(propertyIndex);

         try {
            PreparedStatement ps = session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(this.generateIdByUniqueKeySelectString(uniquePropertyName));

            Serializable var8;
            try {
               propertyType.nullSafeSet(ps, key, 1, session);
               ResultSet rs = ps.executeQuery();

               try {
                  if (rs.next()) {
                     var8 = (Serializable)this.getIdentifierType().nullSafeGet(rs, (String[])this.getIdentifierAliases(), session, (Object)null);
                     return var8;
                  }

                  var8 = null;
               } finally {
                  rs.close();
               }
            } finally {
               ps.close();
            }

            return var8;
         } catch (SQLException e) {
            throw this.getFactory().getSQLExceptionHelper().convert(e, String.format("could not resolve unique property [%s] to identifier for entity [%s]", uniquePropertyName, this.getEntityName()), this.getSQLSnapshotSelectString());
         }
      }
   }

   protected String generateIdByUniqueKeySelectString(String uniquePropertyName) {
      Select select = new Select(this.getFactory().getDialect());
      if (this.getFactory().getSettings().isCommentsEnabled()) {
         select.setComment("resolve id by unique property [" + this.getEntityName() + "." + uniquePropertyName + "]");
      }

      String rooAlias = this.getRootAlias();
      select.setFromClause(this.fromTableFragment(rooAlias) + this.fromJoinFragment(rooAlias, true, false));
      SelectFragment selectFragment = new SelectFragment();
      selectFragment.addColumns(rooAlias, this.getIdentifierColumnNames(), this.getIdentifierAliases());
      select.setSelectClause(selectFragment);
      StringBuilder whereClauseBuffer = new StringBuilder();
      int uniquePropertyIndex = this.getSubclassPropertyIndex(uniquePropertyName);
      String uniquePropertyTableAlias = generateTableAlias(rooAlias, this.getSubclassPropertyTableNumber(uniquePropertyIndex));
      String sep = "";

      for(String columnTemplate : this.getSubclassPropertyColumnReaderTemplateClosure()[uniquePropertyIndex]) {
         if (columnTemplate != null) {
            String columnReference = StringHelper.replace(columnTemplate, "$PlaceHolder$", uniquePropertyTableAlias);
            whereClauseBuffer.append(sep).append(columnReference).append("=?");
            sep = " and ";
         }
      }

      for(String formulaTemplate : this.getSubclassPropertyFormulaTemplateClosure()[uniquePropertyIndex]) {
         if (formulaTemplate != null) {
            String formulaReference = StringHelper.replace(formulaTemplate, "$PlaceHolder$", uniquePropertyTableAlias);
            whereClauseBuffer.append(sep).append(formulaReference).append("=?");
            sep = " and ";
         }
      }

      whereClauseBuffer.append(this.whereJoinFragment(rooAlias, true, false));
      select.setWhereClause(whereClauseBuffer.toString());
      return select.setOuterJoins("", "").toStatementString();
   }

   protected String generateSelectVersionString() {
      SimpleSelect select = (new SimpleSelect(this.getFactory().getDialect())).setTableName(this.getVersionedTableName());
      if (this.isVersioned()) {
         select.addColumn(this.versionColumnName);
      } else {
         select.addColumns(this.rootTableKeyColumnNames);
      }

      if (this.getFactory().getSettings().isCommentsEnabled()) {
         select.setComment("get version " + this.getEntityName());
      }

      return select.addCondition(this.rootTableKeyColumnNames, "=?").toStatementString();
   }

   public boolean[] getPropertyUniqueness() {
      return this.propertyUniqueness;
   }

   protected String generateInsertGeneratedValuesSelectString() {
      return this.generateGeneratedValuesSelectString(this.getPropertyInsertGenerationInclusions());
   }

   protected String generateUpdateGeneratedValuesSelectString() {
      return this.generateGeneratedValuesSelectString(this.getPropertyUpdateGenerationInclusions());
   }

   private String generateGeneratedValuesSelectString(ValueInclusion[] inclusions) {
      Select select = new Select(this.getFactory().getDialect());
      if (this.getFactory().getSettings().isCommentsEnabled()) {
         select.setComment("get generated state " + this.getEntityName());
      }

      String[] aliasedIdColumns = StringHelper.qualify(this.getRootAlias(), this.getIdentifierColumnNames());
      String selectClause = this.concretePropertySelectFragment(this.getRootAlias(), inclusions);
      selectClause = selectClause.substring(2);
      String fromClause = this.fromTableFragment(this.getRootAlias()) + this.fromJoinFragment(this.getRootAlias(), true, false);
      String whereClause = StringHelper.join("=? and ", aliasedIdColumns) + "=?" + this.whereJoinFragment(this.getRootAlias(), true, false);
      return select.setSelectClause(selectClause).setFromClause(fromClause).setOuterJoins("", "").setWhereClause(whereClause).toStatementString();
   }

   protected String concretePropertySelectFragment(String alias, final ValueInclusion[] inclusions) {
      return this.concretePropertySelectFragment(alias, new InclusionChecker() {
         public boolean includeProperty(int propertyNumber) {
            return inclusions[propertyNumber] != ValueInclusion.NONE;
         }
      });
   }

   protected String concretePropertySelectFragment(String alias, final boolean[] includeProperty) {
      return this.concretePropertySelectFragment(alias, new InclusionChecker() {
         public boolean includeProperty(int propertyNumber) {
            return includeProperty[propertyNumber];
         }
      });
   }

   protected String concretePropertySelectFragment(String alias, InclusionChecker inclusionChecker) {
      int propertyCount = this.getPropertyNames().length;
      int[] propertyTableNumbers = this.getPropertyTableNumbersInSelect();
      SelectFragment frag = new SelectFragment();

      for(int i = 0; i < propertyCount; ++i) {
         if (inclusionChecker.includeProperty(i)) {
            frag.addColumnTemplates(generateTableAlias(alias, propertyTableNumbers[i]), this.propertyColumnReaderTemplates[i], this.propertyColumnAliases[i]);
            frag.addFormulas(generateTableAlias(alias, propertyTableNumbers[i]), this.propertyColumnFormulaTemplates[i], this.propertyColumnAliases[i]);
         }
      }

      return frag.toFragmentString();
   }

   protected String generateSnapshotSelectString() {
      Select select = new Select(this.getFactory().getDialect());
      if (this.getFactory().getSettings().isCommentsEnabled()) {
         select.setComment("get current state " + this.getEntityName());
      }

      String[] aliasedIdColumns = StringHelper.qualify(this.getRootAlias(), this.getIdentifierColumnNames());
      String selectClause = StringHelper.join(", ", aliasedIdColumns) + this.concretePropertySelectFragment(this.getRootAlias(), this.getPropertyUpdateability());
      String fromClause = this.fromTableFragment(this.getRootAlias()) + this.fromJoinFragment(this.getRootAlias(), true, false);
      String whereClause = StringHelper.join("=? and ", aliasedIdColumns) + "=?" + this.whereJoinFragment(this.getRootAlias(), true, false);
      return select.setSelectClause(selectClause).setFromClause(fromClause).setOuterJoins("", "").setWhereClause(whereClause).toStatementString();
   }

   public Object forceVersionIncrement(Serializable id, Object currentVersion, SessionImplementor session) {
      if (!this.isVersioned()) {
         throw new AssertionFailure("cannot force version increment on non-versioned entity");
      } else if (this.isVersionPropertyGenerated()) {
         throw new HibernateException("LockMode.FORCE is currently not supported for generated version properties");
      } else {
         Object nextVersion = this.getVersionType().next(currentVersion, session);
         if (LOG.isTraceEnabled()) {
            LOG.trace("Forcing version increment [" + MessageHelper.infoString((EntityPersister)this, (Object)id, (SessionFactoryImplementor)this.getFactory()) + "; " + this.getVersionType().toLoggableString(currentVersion, this.getFactory()) + " -> " + this.getVersionType().toLoggableString(nextVersion, this.getFactory()) + "]");
         }

         String versionIncrementString = this.generateVersionIncrementUpdateString();
         PreparedStatement st = null;

         try {
            st = session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(versionIncrementString, false);

            try {
               this.getVersionType().nullSafeSet(st, nextVersion, 1, session);
               this.getIdentifierType().nullSafeSet(st, id, 2, session);
               this.getVersionType().nullSafeSet(st, currentVersion, 2 + this.getIdentifierColumnSpan(), session);
               int rows = st.executeUpdate();
               if (rows != 1) {
                  throw new StaleObjectStateException(this.getEntityName(), id);
               }
            } finally {
               st.close();
            }

            return nextVersion;
         } catch (SQLException sqle) {
            throw this.getFactory().getSQLExceptionHelper().convert(sqle, "could not retrieve version: " + MessageHelper.infoString((EntityPersister)this, (Object)id, (SessionFactoryImplementor)this.getFactory()), this.getVersionSelectString());
         }
      }
   }

   private String generateVersionIncrementUpdateString() {
      Update update = new Update(this.getFactory().getDialect());
      update.setTableName(this.getTableName(0));
      if (this.getFactory().getSettings().isCommentsEnabled()) {
         update.setComment("forced version increment");
      }

      update.addColumn(this.getVersionColumnName());
      update.addPrimaryKeyColumns(this.getIdentifierColumnNames());
      update.setVersionColumnName(this.getVersionColumnName());
      return update.toStatementString();
   }

   public Object getCurrentVersion(Serializable id, SessionImplementor session) throws HibernateException {
      if (LOG.isTraceEnabled()) {
         LOG.tracev("Getting version: {0}", MessageHelper.infoString((EntityPersister)this, (Object)id, (SessionFactoryImplementor)this.getFactory()));
      }

      try {
         PreparedStatement st = session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(this.getVersionSelectString());

         Object var5;
         try {
            this.getIdentifierType().nullSafeSet(st, id, 1, session);
            ResultSet rs = st.executeQuery();

            try {
               if (rs.next()) {
                  if (!this.isVersioned()) {
                     var5 = this;
                     return var5;
                  }

                  var5 = this.getVersionType().nullSafeGet(rs, this.getVersionColumnName(), session, (Object)null);
                  return var5;
               }

               var5 = null;
            } finally {
               rs.close();
            }
         } finally {
            st.close();
         }

         return var5;
      } catch (SQLException e) {
         throw this.getFactory().getSQLExceptionHelper().convert(e, "could not retrieve version: " + MessageHelper.infoString((EntityPersister)this, (Object)id, (SessionFactoryImplementor)this.getFactory()), this.getVersionSelectString());
      }
   }

   protected void initLockers() {
      this.lockers.put(LockMode.READ, this.generateLocker(LockMode.READ));
      this.lockers.put(LockMode.UPGRADE, this.generateLocker(LockMode.UPGRADE));
      this.lockers.put(LockMode.UPGRADE_NOWAIT, this.generateLocker(LockMode.UPGRADE_NOWAIT));
      this.lockers.put(LockMode.FORCE, this.generateLocker(LockMode.FORCE));
      this.lockers.put(LockMode.PESSIMISTIC_READ, this.generateLocker(LockMode.PESSIMISTIC_READ));
      this.lockers.put(LockMode.PESSIMISTIC_WRITE, this.generateLocker(LockMode.PESSIMISTIC_WRITE));
      this.lockers.put(LockMode.PESSIMISTIC_FORCE_INCREMENT, this.generateLocker(LockMode.PESSIMISTIC_FORCE_INCREMENT));
      this.lockers.put(LockMode.OPTIMISTIC, this.generateLocker(LockMode.OPTIMISTIC));
      this.lockers.put(LockMode.OPTIMISTIC_FORCE_INCREMENT, this.generateLocker(LockMode.OPTIMISTIC_FORCE_INCREMENT));
   }

   protected LockingStrategy generateLocker(LockMode lockMode) {
      return this.factory.getDialect().getLockingStrategy(this, lockMode);
   }

   private LockingStrategy getLocker(LockMode lockMode) {
      return (LockingStrategy)this.lockers.get(lockMode);
   }

   public void lock(Serializable id, Object version, Object object, LockMode lockMode, SessionImplementor session) throws HibernateException {
      this.getLocker(lockMode).lock(id, version, object, -1, session);
   }

   public void lock(Serializable id, Object version, Object object, LockOptions lockOptions, SessionImplementor session) throws HibernateException {
      this.getLocker(lockOptions.getLockMode()).lock(id, version, object, lockOptions.getTimeOut(), session);
   }

   public String getRootTableName() {
      return this.getSubclassTableName(0);
   }

   public String getRootTableAlias(String drivingAlias) {
      return drivingAlias;
   }

   public String[] getRootTableIdentifierColumnNames() {
      return this.getRootTableKeyColumnNames();
   }

   public String[] toColumns(String alias, String propertyName) throws QueryException {
      return this.propertyMapping.toColumns(alias, propertyName);
   }

   public String[] toColumns(String propertyName) throws QueryException {
      return this.propertyMapping.getColumnNames(propertyName);
   }

   public Type toType(String propertyName) throws QueryException {
      return this.propertyMapping.toType(propertyName);
   }

   public String[] getPropertyColumnNames(String propertyName) {
      return this.propertyMapping.getColumnNames(propertyName);
   }

   public int getSubclassPropertyTableNumber(String propertyPath) {
      String rootPropertyName = StringHelper.root(propertyPath);
      Type type = this.propertyMapping.toType(rootPropertyName);
      if (type.isAssociationType()) {
         AssociationType assocType = (AssociationType)type;
         if (assocType.useLHSPrimaryKey()) {
            return 0;
         }

         if (type.isCollectionType()) {
            rootPropertyName = assocType.getLHSPropertyName();
         }
      }

      int index = ArrayHelper.indexOf(this.getSubclassPropertyNameClosure(), rootPropertyName);
      return index == -1 ? 0 : this.getSubclassPropertyTableNumber(index);
   }

   public Queryable.Declarer getSubclassPropertyDeclarer(String propertyPath) {
      int tableIndex = this.getSubclassPropertyTableNumber(propertyPath);
      if (tableIndex == 0) {
         return Queryable.Declarer.CLASS;
      } else {
         return this.isClassOrSuperclassTable(tableIndex) ? Queryable.Declarer.SUPERCLASS : Queryable.Declarer.SUBCLASS;
      }
   }

   public DiscriminatorMetadata getTypeDiscriminatorMetadata() {
      if (this.discriminatorMetadata == null) {
         this.discriminatorMetadata = this.buildTypeDiscriminatorMetadata();
      }

      return this.discriminatorMetadata;
   }

   private DiscriminatorMetadata buildTypeDiscriminatorMetadata() {
      return new DiscriminatorMetadata() {
         public String getSqlFragment(String sqlQualificationAlias) {
            return AbstractEntityPersister.this.toColumns(sqlQualificationAlias, "class")[0];
         }

         public Type getResolutionType() {
            return new DiscriminatorType(AbstractEntityPersister.this.getDiscriminatorType(), AbstractEntityPersister.this);
         }
      };
   }

   public static String generateTableAlias(String rootAlias, int tableNumber) {
      if (tableNumber == 0) {
         return rootAlias;
      } else {
         StringBuilder buf = (new StringBuilder()).append(rootAlias);
         if (!rootAlias.endsWith("_")) {
            buf.append('_');
         }

         return buf.append(tableNumber).append('_').toString();
      }
   }

   public String[] toColumns(String name, int i) {
      String alias = generateTableAlias(name, this.getSubclassPropertyTableNumber(i));
      String[] cols = this.getSubclassPropertyColumnNames(i);
      String[] templates = this.getSubclassPropertyFormulaTemplateClosure()[i];
      String[] result = new String[cols.length];

      for(int j = 0; j < cols.length; ++j) {
         if (cols[j] == null) {
            result[j] = StringHelper.replace(templates[j], "$PlaceHolder$", alias);
         } else {
            result[j] = StringHelper.qualify(alias, cols[j]);
         }
      }

      return result;
   }

   private int getSubclassPropertyIndex(String propertyName) {
      return ArrayHelper.indexOf(this.subclassPropertyNameClosure, propertyName);
   }

   protected String[] getPropertySubclassNames() {
      return this.propertySubclassNames;
   }

   public String[] getPropertyColumnNames(int i) {
      return this.propertyColumnNames[i];
   }

   public String[] getPropertyColumnWriters(int i) {
      return this.propertyColumnWriters[i];
   }

   protected int getPropertyColumnSpan(int i) {
      return this.propertyColumnSpans[i];
   }

   protected boolean hasFormulaProperties() {
      return this.hasFormulaProperties;
   }

   public FetchMode getFetchMode(int i) {
      return this.subclassPropertyFetchModeClosure[i];
   }

   public CascadeStyle getCascadeStyle(int i) {
      return this.subclassPropertyCascadeStyleClosure[i];
   }

   public Type getSubclassPropertyType(int i) {
      return this.subclassPropertyTypeClosure[i];
   }

   public String getSubclassPropertyName(int i) {
      return this.subclassPropertyNameClosure[i];
   }

   public int countSubclassProperties() {
      return this.subclassPropertyTypeClosure.length;
   }

   public String[] getSubclassPropertyColumnNames(int i) {
      return this.subclassPropertyColumnNameClosure[i];
   }

   public boolean isDefinedOnSubclass(int i) {
      return this.propertyDefinedOnSubclass[i];
   }

   public String[][] getSubclassPropertyFormulaTemplateClosure() {
      return this.subclassPropertyFormulaTemplateClosure;
   }

   protected Type[] getSubclassPropertyTypeClosure() {
      return this.subclassPropertyTypeClosure;
   }

   protected String[][] getSubclassPropertyColumnNameClosure() {
      return this.subclassPropertyColumnNameClosure;
   }

   public String[][] getSubclassPropertyColumnReaderClosure() {
      return this.subclassPropertyColumnReaderClosure;
   }

   public String[][] getSubclassPropertyColumnReaderTemplateClosure() {
      return this.subclassPropertyColumnReaderTemplateClosure;
   }

   protected String[] getSubclassPropertyNameClosure() {
      return this.subclassPropertyNameClosure;
   }

   protected String[] getSubclassPropertySubclassNameClosure() {
      return this.subclassPropertySubclassNameClosure;
   }

   protected String[] getSubclassColumnClosure() {
      return this.subclassColumnClosure;
   }

   protected String[] getSubclassColumnAliasClosure() {
      return this.subclassColumnAliasClosure;
   }

   public String[] getSubclassColumnReaderTemplateClosure() {
      return this.subclassColumnReaderTemplateClosure;
   }

   protected String[] getSubclassFormulaClosure() {
      return this.subclassFormulaClosure;
   }

   protected String[] getSubclassFormulaTemplateClosure() {
      return this.subclassFormulaTemplateClosure;
   }

   protected String[] getSubclassFormulaAliasClosure() {
      return this.subclassFormulaAliasClosure;
   }

   public String[] getSubclassPropertyColumnAliases(String propertyName, String suffix) {
      String[] rawAliases = (String[])this.subclassPropertyAliases.get(propertyName);
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

   public String[] getSubclassPropertyColumnNames(String propertyName) {
      return (String[])this.subclassPropertyColumnNames.get(propertyName);
   }

   protected void initSubclassPropertyAliasesMap(PersistentClass model) throws MappingException {
      this.internalInitSubclassPropertyAliasesMap((String)null, model.getSubclassPropertyClosureIterator());
      if (!this.entityMetamodel.hasNonIdentifierPropertyNamedId()) {
         this.subclassPropertyAliases.put("id", this.getIdentifierAliases());
         this.subclassPropertyColumnNames.put("id", this.getIdentifierColumnNames());
      }

      if (this.hasIdentifierProperty()) {
         this.subclassPropertyAliases.put(this.getIdentifierPropertyName(), this.getIdentifierAliases());
         this.subclassPropertyColumnNames.put(this.getIdentifierPropertyName(), this.getIdentifierColumnNames());
      }

      if (this.getIdentifierType().isComponentType()) {
         CompositeType componentId = (CompositeType)this.getIdentifierType();
         String[] idPropertyNames = componentId.getPropertyNames();
         String[] idAliases = this.getIdentifierAliases();
         String[] idColumnNames = this.getIdentifierColumnNames();

         for(int i = 0; i < idPropertyNames.length; ++i) {
            if (this.entityMetamodel.hasNonIdentifierPropertyNamedId()) {
               this.subclassPropertyAliases.put("id." + idPropertyNames[i], new String[]{idAliases[i]});
               this.subclassPropertyColumnNames.put("id." + this.getIdentifierPropertyName() + "." + idPropertyNames[i], new String[]{idColumnNames[i]});
            }

            if (this.hasIdentifierProperty()) {
               this.subclassPropertyAliases.put(this.getIdentifierPropertyName() + "." + idPropertyNames[i], new String[]{idAliases[i]});
               this.subclassPropertyColumnNames.put(this.getIdentifierPropertyName() + "." + idPropertyNames[i], new String[]{idColumnNames[i]});
            } else {
               this.subclassPropertyAliases.put(idPropertyNames[i], new String[]{idAliases[i]});
               this.subclassPropertyColumnNames.put(idPropertyNames[i], new String[]{idColumnNames[i]});
            }
         }
      }

      if (this.entityMetamodel.isPolymorphic()) {
         this.subclassPropertyAliases.put("class", new String[]{this.getDiscriminatorAlias()});
         this.subclassPropertyColumnNames.put("class", new String[]{this.getDiscriminatorColumnName()});
      }

   }

   protected void initSubclassPropertyAliasesMap(EntityBinding model) throws MappingException {
      if (!this.entityMetamodel.hasNonIdentifierPropertyNamedId()) {
         this.subclassPropertyAliases.put("id", this.getIdentifierAliases());
         this.subclassPropertyColumnNames.put("id", this.getIdentifierColumnNames());
      }

      if (this.hasIdentifierProperty()) {
         this.subclassPropertyAliases.put(this.getIdentifierPropertyName(), this.getIdentifierAliases());
         this.subclassPropertyColumnNames.put(this.getIdentifierPropertyName(), this.getIdentifierColumnNames());
      }

      if (this.getIdentifierType().isComponentType()) {
         CompositeType componentId = (CompositeType)this.getIdentifierType();
         String[] idPropertyNames = componentId.getPropertyNames();
         String[] idAliases = this.getIdentifierAliases();
         String[] idColumnNames = this.getIdentifierColumnNames();

         for(int i = 0; i < idPropertyNames.length; ++i) {
            if (this.entityMetamodel.hasNonIdentifierPropertyNamedId()) {
               this.subclassPropertyAliases.put("id." + idPropertyNames[i], new String[]{idAliases[i]});
               this.subclassPropertyColumnNames.put("id." + this.getIdentifierPropertyName() + "." + idPropertyNames[i], new String[]{idColumnNames[i]});
            }

            if (this.hasIdentifierProperty()) {
               this.subclassPropertyAliases.put(this.getIdentifierPropertyName() + "." + idPropertyNames[i], new String[]{idAliases[i]});
               this.subclassPropertyColumnNames.put(this.getIdentifierPropertyName() + "." + idPropertyNames[i], new String[]{idColumnNames[i]});
            } else {
               this.subclassPropertyAliases.put(idPropertyNames[i], new String[]{idAliases[i]});
               this.subclassPropertyColumnNames.put(idPropertyNames[i], new String[]{idColumnNames[i]});
            }
         }
      }

      if (this.entityMetamodel.isPolymorphic()) {
         this.subclassPropertyAliases.put("class", new String[]{this.getDiscriminatorAlias()});
         this.subclassPropertyColumnNames.put("class", new String[]{this.getDiscriminatorColumnName()});
      }

   }

   private void internalInitSubclassPropertyAliasesMap(String path, Iterator propertyIterator) {
      while(propertyIterator.hasNext()) {
         Property prop = (Property)propertyIterator.next();
         String propname = path == null ? prop.getName() : path + "." + prop.getName();
         if (prop.isComposite()) {
            Component component = (Component)prop.getValue();
            Iterator compProps = component.getPropertyIterator();
            this.internalInitSubclassPropertyAliasesMap(propname, compProps);
         } else {
            String[] aliases = new String[prop.getColumnSpan()];
            String[] cols = new String[prop.getColumnSpan()];
            Iterator colIter = prop.getColumnIterator();

            for(int l = 0; colIter.hasNext(); ++l) {
               Selectable thing = (Selectable)colIter.next();
               aliases[l] = thing.getAlias(this.getFactory().getDialect(), prop.getValue().getTable());
               cols[l] = thing.getText(this.getFactory().getDialect());
            }

            this.subclassPropertyAliases.put(propname, aliases);
            this.subclassPropertyColumnNames.put(propname, cols);
         }
      }

   }

   public Object loadByUniqueKey(String propertyName, Object uniqueKey, SessionImplementor session) throws HibernateException {
      return this.getAppropriateUniqueKeyLoader(propertyName, session).loadByUniqueKey(session, uniqueKey);
   }

   private EntityLoader getAppropriateUniqueKeyLoader(String propertyName, SessionImplementor session) {
      boolean useStaticLoader = !session.getLoadQueryInfluencers().hasEnabledFilters() && !session.getLoadQueryInfluencers().hasEnabledFetchProfiles() && propertyName.indexOf(46) < 0;
      return useStaticLoader ? (EntityLoader)this.uniqueKeyLoaders.get(propertyName) : this.createUniqueKeyLoader(this.propertyMapping.toType(propertyName), this.propertyMapping.toColumns(propertyName), session.getLoadQueryInfluencers());
   }

   public int getPropertyIndex(String propertyName) {
      return this.entityMetamodel.getPropertyIndex(propertyName);
   }

   protected void createUniqueKeyLoaders() throws MappingException {
      Type[] propertyTypes = this.getPropertyTypes();
      String[] propertyNames = this.getPropertyNames();

      for(int i = 0; i < this.entityMetamodel.getPropertySpan(); ++i) {
         if (this.propertyUniqueness[i]) {
            this.uniqueKeyLoaders.put(propertyNames[i], this.createUniqueKeyLoader(propertyTypes[i], this.getPropertyColumnNames(i), LoadQueryInfluencers.NONE));
         }
      }

   }

   private EntityLoader createUniqueKeyLoader(Type uniqueKeyType, String[] columns, LoadQueryInfluencers loadQueryInfluencers) {
      if (uniqueKeyType.isEntityType()) {
         String className = ((EntityType)uniqueKeyType).getAssociatedEntityName();
         uniqueKeyType = this.getFactory().getEntityPersister(className).getIdentifierType();
      }

      return new EntityLoader(this, columns, uniqueKeyType, 1, LockMode.NONE, this.getFactory(), loadQueryInfluencers);
   }

   protected String getSQLWhereString(String alias) {
      return StringHelper.replace(this.sqlWhereStringTemplate, "$PlaceHolder$", alias);
   }

   protected boolean hasWhere() {
      return this.sqlWhereString != null;
   }

   private void initOrdinaryPropertyPaths(Mapping mapping) throws MappingException {
      for(int i = 0; i < this.getSubclassPropertyNameClosure().length; ++i) {
         this.propertyMapping.initPropertyPaths(this.getSubclassPropertyNameClosure()[i], this.getSubclassPropertyTypeClosure()[i], this.getSubclassPropertyColumnNameClosure()[i], this.getSubclassPropertyColumnReaderClosure()[i], this.getSubclassPropertyColumnReaderTemplateClosure()[i], this.getSubclassPropertyFormulaTemplateClosure()[i], mapping);
      }

   }

   private void initIdentifierPropertyPaths(Mapping mapping) throws MappingException {
      String idProp = this.getIdentifierPropertyName();
      if (idProp != null) {
         this.propertyMapping.initPropertyPaths(idProp, this.getIdentifierType(), this.getIdentifierColumnNames(), this.getIdentifierColumnReaders(), this.getIdentifierColumnReaderTemplates(), (String[])null, mapping);
      }

      if (this.entityMetamodel.getIdentifierProperty().isEmbedded()) {
         this.propertyMapping.initPropertyPaths((String)null, this.getIdentifierType(), this.getIdentifierColumnNames(), this.getIdentifierColumnReaders(), this.getIdentifierColumnReaderTemplates(), (String[])null, mapping);
      }

      if (!this.entityMetamodel.hasNonIdentifierPropertyNamedId()) {
         this.propertyMapping.initPropertyPaths("id", this.getIdentifierType(), this.getIdentifierColumnNames(), this.getIdentifierColumnReaders(), this.getIdentifierColumnReaderTemplates(), (String[])null, mapping);
      }

   }

   private void initDiscriminatorPropertyPath(Mapping mapping) throws MappingException {
      this.propertyMapping.initPropertyPaths("class", this.getDiscriminatorType(), new String[]{this.getDiscriminatorColumnName()}, new String[]{this.getDiscriminatorColumnReaders()}, new String[]{this.getDiscriminatorColumnReaderTemplate()}, new String[]{this.getDiscriminatorFormulaTemplate()}, this.getFactory());
   }

   protected void initPropertyPaths(Mapping mapping) throws MappingException {
      this.initOrdinaryPropertyPaths(mapping);
      this.initOrdinaryPropertyPaths(mapping);
      this.initIdentifierPropertyPaths(mapping);
      if (this.entityMetamodel.isPolymorphic()) {
         this.initDiscriminatorPropertyPath(mapping);
      }

   }

   protected UniqueEntityLoader createEntityLoader(LockMode lockMode, LoadQueryInfluencers loadQueryInfluencers) throws MappingException {
      return BatchingEntityLoader.createBatchingEntityLoader(this, this.batchSize, (LockMode)lockMode, this.getFactory(), loadQueryInfluencers);
   }

   protected UniqueEntityLoader createEntityLoader(LockOptions lockOptions, LoadQueryInfluencers loadQueryInfluencers) throws MappingException {
      return BatchingEntityLoader.createBatchingEntityLoader(this, this.batchSize, (LockOptions)lockOptions, this.getFactory(), loadQueryInfluencers);
   }

   protected UniqueEntityLoader createEntityLoader(LockMode lockMode) throws MappingException {
      return this.createEntityLoader(lockMode, LoadQueryInfluencers.NONE);
   }

   protected boolean check(int rows, Serializable id, int tableNumber, Expectation expectation, PreparedStatement statement) throws HibernateException {
      try {
         expectation.verifyOutcome(rows, statement, -1);
         return true;
      } catch (StaleStateException var7) {
         if (!this.isNullableTable(tableNumber)) {
            if (this.getFactory().getStatistics().isStatisticsEnabled()) {
               this.getFactory().getStatisticsImplementor().optimisticFailure(this.getEntityName());
            }

            throw new StaleObjectStateException(this.getEntityName(), id);
         } else {
            return false;
         }
      } catch (TooManyRowsAffectedException var8) {
         throw new HibernateException("Duplicate identifier in table for: " + MessageHelper.infoString((EntityPersister)this, (Object)id, (SessionFactoryImplementor)this.getFactory()));
      } catch (Throwable var9) {
         return false;
      }
   }

   protected String generateUpdateString(boolean[] includeProperty, int j, boolean useRowId) {
      return this.generateUpdateString(includeProperty, j, (Object[])null, useRowId);
   }

   protected String generateUpdateString(boolean[] includeProperty, int j, Object[] oldFields, boolean useRowId) {
      Update update = (new Update(this.getFactory().getDialect())).setTableName(this.getTableName(j));
      if (useRowId) {
         update.addPrimaryKeyColumns(new String[]{this.rowIdName});
      } else {
         update.addPrimaryKeyColumns(this.getKeyColumns(j));
      }

      boolean hasColumns = false;

      for(int i = 0; i < this.entityMetamodel.getPropertySpan(); ++i) {
         if (includeProperty[i] && this.isPropertyOfTable(i, j) && !this.lobProperties.contains(i)) {
            update.addColumns(this.getPropertyColumnNames(i), this.propertyColumnUpdateable[i], this.propertyColumnWriters[i]);
            hasColumns = hasColumns || this.getPropertyColumnSpan(i) > 0;
         }
      }

      for(int i : this.lobProperties) {
         if (includeProperty[i] && this.isPropertyOfTable(i, j)) {
            update.addColumns(this.getPropertyColumnNames(i), this.propertyColumnUpdateable[i], this.propertyColumnWriters[i]);
            hasColumns = true;
         }
      }

      if (j == 0 && this.isVersioned() && this.entityMetamodel.getOptimisticLockStyle() == OptimisticLockStyle.VERSION) {
         if (this.checkVersion(includeProperty)) {
            update.setVersionColumnName(this.getVersionColumnName());
            hasColumns = true;
         }
      } else if (this.isAllOrDirtyOptLocking() && oldFields != null) {
         boolean[] includeInWhere = this.entityMetamodel.getOptimisticLockStyle() == OptimisticLockStyle.ALL ? this.getPropertyUpdateability() : includeProperty;
         boolean[] versionability = this.getPropertyVersionability();
         Type[] types = this.getPropertyTypes();

         for(int i = 0; i < this.entityMetamodel.getPropertySpan(); ++i) {
            boolean include = includeInWhere[i] && this.isPropertyOfTable(i, j) && versionability[i];
            if (include) {
               String[] propertyColumnNames = this.getPropertyColumnNames(i);
               String[] propertyColumnWriters = this.getPropertyColumnWriters(i);
               boolean[] propertyNullness = types[i].toColumnNullness(oldFields[i], this.getFactory());

               for(int k = 0; k < propertyNullness.length; ++k) {
                  if (propertyNullness[k]) {
                     update.addWhereColumn(propertyColumnNames[k], "=" + propertyColumnWriters[k]);
                  } else {
                     update.addWhereColumn(propertyColumnNames[k], " is null");
                  }
               }
            }
         }
      }

      if (this.getFactory().getSettings().isCommentsEnabled()) {
         update.setComment("update " + this.getEntityName());
      }

      return hasColumns ? update.toStatementString() : null;
   }

   private boolean checkVersion(boolean[] includeProperty) {
      return includeProperty[this.getVersionProperty()] || this.entityMetamodel.getPropertyUpdateGenerationInclusions()[this.getVersionProperty()] != ValueInclusion.NONE;
   }

   protected String generateInsertString(boolean[] includeProperty, int j) {
      return this.generateInsertString(false, includeProperty, j);
   }

   protected String generateInsertString(boolean identityInsert, boolean[] includeProperty) {
      return this.generateInsertString(identityInsert, includeProperty, 0);
   }

   protected String generateInsertString(boolean identityInsert, boolean[] includeProperty, int j) {
      Insert insert = (new Insert(this.getFactory().getDialect())).setTableName(this.getTableName(j));

      for(int i = 0; i < this.entityMetamodel.getPropertySpan(); ++i) {
         if (includeProperty[i] && this.isPropertyOfTable(i, j) && !this.lobProperties.contains(i)) {
            insert.addColumns(this.getPropertyColumnNames(i), this.propertyColumnInsertable[i], this.propertyColumnWriters[i]);
         }
      }

      if (j == 0) {
         this.addDiscriminatorToInsert(insert);
      }

      if (j == 0 && identityInsert) {
         insert.addIdentityColumn(this.getKeyColumns(0)[0]);
      } else {
         insert.addColumns(this.getKeyColumns(j));
      }

      if (this.getFactory().getSettings().isCommentsEnabled()) {
         insert.setComment("insert " + this.getEntityName());
      }

      for(int i : this.lobProperties) {
         if (includeProperty[i] && this.isPropertyOfTable(i, j)) {
            insert.addColumns(this.getPropertyColumnNames(i), this.propertyColumnInsertable[i], this.propertyColumnWriters[i]);
         }
      }

      String result = insert.toStatementString();
      if (j == 0 && identityInsert && this.useInsertSelectIdentity()) {
         result = this.getFactory().getDialect().appendIdentitySelectToInsert(result);
      }

      return result;
   }

   protected String generateIdentityInsertString(boolean[] includeProperty) {
      Insert insert = this.identityDelegate.prepareIdentifierGeneratingInsert();
      insert.setTableName(this.getTableName(0));

      for(int i = 0; i < this.entityMetamodel.getPropertySpan(); ++i) {
         if (includeProperty[i] && this.isPropertyOfTable(i, 0)) {
            insert.addColumns(this.getPropertyColumnNames(i), this.propertyColumnInsertable[i], this.propertyColumnWriters[i]);
         }
      }

      this.addDiscriminatorToInsert(insert);
      if (this.getFactory().getSettings().isCommentsEnabled()) {
         insert.setComment("insert " + this.getEntityName());
      }

      return insert.toStatementString();
   }

   protected String generateDeleteString(int j) {
      Delete delete = (new Delete()).setTableName(this.getTableName(j)).addPrimaryKeyColumns(this.getKeyColumns(j));
      if (j == 0) {
         delete.setVersionColumnName(this.getVersionColumnName());
      }

      if (this.getFactory().getSettings().isCommentsEnabled()) {
         delete.setComment("delete " + this.getEntityName());
      }

      return delete.toStatementString();
   }

   protected int dehydrate(Serializable id, Object[] fields, boolean[] includeProperty, boolean[][] includeColumns, int j, PreparedStatement st, SessionImplementor session, boolean isUpdate) throws HibernateException, SQLException {
      return this.dehydrate(id, fields, (Object)null, includeProperty, includeColumns, j, st, session, 1, isUpdate);
   }

   protected int dehydrate(Serializable id, Object[] fields, Object rowId, boolean[] includeProperty, boolean[][] includeColumns, int j, PreparedStatement ps, SessionImplementor session, int index, boolean isUpdate) throws SQLException, HibernateException {
      if (LOG.isTraceEnabled()) {
         LOG.tracev("Dehydrating entity: {0}", MessageHelper.infoString((EntityPersister)this, (Object)id, (SessionFactoryImplementor)this.getFactory()));
      }

      for(int i = 0; i < this.entityMetamodel.getPropertySpan(); ++i) {
         if (includeProperty[i] && this.isPropertyOfTable(i, j) && !this.lobProperties.contains(i)) {
            this.getPropertyTypes()[i].nullSafeSet(ps, fields[i], index, includeColumns[i], session);
            index += ArrayHelper.countTrue(includeColumns[i]);
         }
      }

      if (!isUpdate) {
         index += this.dehydrateId(id, rowId, ps, session, index);
      }

      for(int i : this.lobProperties) {
         if (includeProperty[i] && this.isPropertyOfTable(i, j)) {
            this.getPropertyTypes()[i].nullSafeSet(ps, fields[i], index, includeColumns[i], session);
            index += ArrayHelper.countTrue(includeColumns[i]);
         }
      }

      if (isUpdate) {
         index += this.dehydrateId(id, rowId, ps, session, index);
      }

      return index;
   }

   private int dehydrateId(Serializable id, Object rowId, PreparedStatement ps, SessionImplementor session, int index) throws SQLException {
      if (rowId != null) {
         ps.setObject(index, rowId);
         return 1;
      } else if (id != null) {
         this.getIdentifierType().nullSafeSet(ps, id, index, session);
         return this.getIdentifierColumnSpan();
      } else {
         return 0;
      }
   }

   public Object[] hydrate(ResultSet rs, Serializable id, Object object, Loadable rootLoadable, String[][] suffixedPropertyColumns, boolean allProperties, SessionImplementor session) throws SQLException, HibernateException {
      if (LOG.isTraceEnabled()) {
         LOG.tracev("Hydrating entity: {0}", MessageHelper.infoString((EntityPersister)this, (Object)id, (SessionFactoryImplementor)this.getFactory()));
      }

      AbstractEntityPersister rootPersister = (AbstractEntityPersister)rootLoadable;
      boolean hasDeferred = rootPersister.hasSequentialSelect();
      PreparedStatement sequentialSelect = null;
      ResultSet sequentialResultSet = null;
      boolean sequentialSelectEmpty = false;

      Object[] var26;
      try {
         if (hasDeferred) {
            String sql = rootPersister.getSequentialSelect(this.getEntityName());
            if (sql != null) {
               sequentialSelect = session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(sql);
               rootPersister.getIdentifierType().nullSafeSet(sequentialSelect, id, 1, session);
               sequentialResultSet = sequentialSelect.executeQuery();
               if (!sequentialResultSet.next()) {
                  sequentialSelectEmpty = true;
               }
            }
         }

         String[] propNames = this.getPropertyNames();
         Type[] types = this.getPropertyTypes();
         Object[] values = new Object[types.length];
         boolean[] laziness = this.getPropertyLaziness();
         String[] propSubclassNames = this.getSubclassPropertySubclassNameClosure();

         for(int i = 0; i < types.length; ++i) {
            if (!this.propertySelectable[i]) {
               values[i] = BackrefPropertyAccessor.UNKNOWN;
            } else if (!allProperties && laziness[i]) {
               values[i] = LazyPropertyInitializer.UNFETCHED_PROPERTY;
            } else {
               boolean propertyIsDeferred = hasDeferred && rootPersister.isSubclassPropertyDeferred(propNames[i], propSubclassNames[i]);
               if (propertyIsDeferred && sequentialSelectEmpty) {
                  values[i] = null;
               } else {
                  ResultSet propertyResultSet = propertyIsDeferred ? sequentialResultSet : rs;
                  String[] cols = propertyIsDeferred ? this.propertyColumnAliases[i] : suffixedPropertyColumns[i];
                  values[i] = types[i].hydrate(propertyResultSet, cols, session, object);
               }
            }
         }

         if (sequentialResultSet != null) {
            sequentialResultSet.close();
         }

         var26 = values;
      } finally {
         if (sequentialSelect != null) {
            sequentialSelect.close();
         }

      }

      return var26;
   }

   protected boolean useInsertSelectIdentity() {
      return !this.useGetGeneratedKeys() && this.getFactory().getDialect().supportsInsertSelectIdentity();
   }

   protected boolean useGetGeneratedKeys() {
      return this.getFactory().getSettings().isGetGeneratedKeysEnabled();
   }

   protected String getSequentialSelect(String entityName) {
      throw new UnsupportedOperationException("no sequential selects");
   }

   protected Serializable insert(final Object[] fields, final boolean[] notNull, String sql, final Object object, final SessionImplementor session) throws HibernateException {
      if (LOG.isTraceEnabled()) {
         LOG.tracev("Inserting entity: {0} (native id)", this.getEntityName());
         if (this.isVersioned()) {
            LOG.tracev("Version: {0}", Versioning.getVersion(fields, this));
         }
      }

      Binder binder = new Binder() {
         public void bindValues(PreparedStatement ps) throws SQLException {
            AbstractEntityPersister.this.dehydrate((Serializable)null, fields, notNull, AbstractEntityPersister.this.propertyColumnInsertable, 0, ps, session, false);
         }

         public Object getEntity() {
            return object;
         }
      };
      return this.identityDelegate.performInsert(sql, session, binder);
   }

   public String getIdentitySelectString() {
      return this.getFactory().getDialect().getIdentitySelectString(this.getTableName(0), this.getKeyColumns(0)[0], this.getIdentifierType().sqlTypes(this.getFactory())[0]);
   }

   public String getSelectByUniqueKeyString(String propertyName) {
      return (new SimpleSelect(this.getFactory().getDialect())).setTableName(this.getTableName(0)).addColumns(this.getKeyColumns(0)).addCondition(this.getPropertyColumnNames(propertyName), "=?").toStatementString();
   }

   protected void insert(Serializable id, Object[] fields, boolean[] notNull, int j, String sql, Object object, SessionImplementor session) throws HibernateException {
      if (!this.isInverseTable(j)) {
         if (!this.isNullableTable(j) || !this.isAllNull(fields, j)) {
            if (LOG.isTraceEnabled()) {
               LOG.tracev("Inserting entity: {0}", MessageHelper.infoString((EntityPersister)this, (Object)id, (SessionFactoryImplementor)this.getFactory()));
               if (j == 0 && this.isVersioned()) {
                  LOG.tracev("Version: {0}", Versioning.getVersion(fields, this));
               }
            }

            Expectation expectation = Expectations.appropriateExpectation(this.insertResultCheckStyles[j]);
            boolean useBatch = j == 0 && expectation.canBeBatched();
            if (useBatch && this.inserBatchKey == null) {
               this.inserBatchKey = new BasicBatchKey(this.getEntityName() + "#INSERT", expectation);
            }

            boolean callable = this.isInsertCallable(j);

            try {
               PreparedStatement insert;
               if (useBatch) {
                  insert = session.getTransactionCoordinator().getJdbcCoordinator().getBatch(this.inserBatchKey).getBatchStatement(sql, callable);
               } else {
                  insert = session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(sql, callable);
               }

               try {
                  int index = 1;
                  index += expectation.prepare(insert);
                  this.dehydrate(id, fields, (Object)null, notNull, this.propertyColumnInsertable, j, insert, session, index, false);
                  if (useBatch) {
                     session.getTransactionCoordinator().getJdbcCoordinator().getBatch(this.inserBatchKey).addToBatch();
                  } else {
                     expectation.verifyOutcome(insert.executeUpdate(), insert, -1);
                  }
               } catch (SQLException e) {
                  if (useBatch) {
                     session.getTransactionCoordinator().getJdbcCoordinator().abortBatch();
                  }

                  throw e;
               } finally {
                  if (!useBatch) {
                     insert.close();
                  }

               }

            } catch (SQLException e) {
               throw this.getFactory().getSQLExceptionHelper().convert(e, "could not insert: " + MessageHelper.infoString(this), sql);
            }
         }
      }
   }

   protected void updateOrInsert(Serializable id, Object[] fields, Object[] oldFields, Object rowId, boolean[] includeProperty, int j, Object oldVersion, Object object, String sql, SessionImplementor session) throws HibernateException {
      if (!this.isInverseTable(j)) {
         boolean isRowToUpdate;
         if (this.isNullableTable(j) && oldFields != null && this.isAllNull(oldFields, j)) {
            isRowToUpdate = false;
         } else if (this.isNullableTable(j) && this.isAllNull(fields, j)) {
            isRowToUpdate = true;
            this.delete(id, oldVersion, j, object, this.getSQLDeleteStrings()[j], session, (Object[])null);
         } else {
            isRowToUpdate = this.update(id, fields, oldFields, rowId, includeProperty, j, oldVersion, object, sql, session);
         }

         if (!isRowToUpdate && !this.isAllNull(fields, j)) {
            this.insert(id, fields, this.getPropertyInsertability(), j, this.getSQLInsertStrings()[j], object, session);
         }
      }

   }

   protected boolean update(Serializable id, Object[] fields, Object[] oldFields, Object rowId, boolean[] includeProperty, int j, Object oldVersion, Object object, String sql, SessionImplementor session) throws HibernateException {
      Expectation expectation = Expectations.appropriateExpectation(this.updateResultCheckStyles[j]);
      boolean useBatch = j == 0 && expectation.canBeBatched() && this.isBatchable();
      if (useBatch && this.updateBatchKey == null) {
         this.updateBatchKey = new BasicBatchKey(this.getEntityName() + "#UPDATE", expectation);
      }

      boolean callable = this.isUpdateCallable(j);
      boolean useVersion = j == 0 && this.isVersioned();
      if (LOG.isTraceEnabled()) {
         LOG.tracev("Updating entity: {0}", MessageHelper.infoString((EntityPersister)this, (Object)id, (SessionFactoryImplementor)this.getFactory()));
         if (useVersion) {
            LOG.tracev("Existing version: {0} -> New version:{1}", oldVersion, fields[this.getVersionProperty()]);
         }
      }

      try {
         int index = 1;
         PreparedStatement update;
         if (useBatch) {
            update = session.getTransactionCoordinator().getJdbcCoordinator().getBatch(this.updateBatchKey).getBatchStatement(sql, callable);
         } else {
            update = session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(sql, callable);
         }

         boolean[] versionability;
         try {
            index += expectation.prepare(update);
            index = this.dehydrate(id, fields, rowId, includeProperty, this.propertyColumnUpdateable, j, update, session, index, true);
            if (useVersion && this.entityMetamodel.getOptimisticLockStyle() == OptimisticLockStyle.VERSION) {
               if (this.checkVersion(includeProperty)) {
                  this.getVersionType().nullSafeSet(update, oldVersion, index, session);
               }
            } else if (this.isAllOrDirtyOptLocking() && oldFields != null) {
               versionability = this.getPropertyVersionability();
               boolean[] includeOldField = this.entityMetamodel.getOptimisticLockStyle() == OptimisticLockStyle.ALL ? this.getPropertyUpdateability() : includeProperty;
               Type[] types = this.getPropertyTypes();

               for(int i = 0; i < this.entityMetamodel.getPropertySpan(); ++i) {
                  boolean include = includeOldField[i] && this.isPropertyOfTable(i, j) && versionability[i];
                  if (include) {
                     boolean[] settable = types[i].toColumnNullness(oldFields[i], this.getFactory());
                     types[i].nullSafeSet(update, oldFields[i], index, settable, session);
                     index += ArrayHelper.countTrue(settable);
                  }
               }
            }

            if (!useBatch) {
               versionability = (boolean[])this.check(update.executeUpdate(), id, j, expectation, update);
               return (boolean)versionability;
            }

            session.getTransactionCoordinator().getJdbcCoordinator().getBatch(this.updateBatchKey).addToBatch();
            versionability = (boolean[])true;
         } catch (SQLException e) {
            if (useBatch) {
               session.getTransactionCoordinator().getJdbcCoordinator().abortBatch();
            }

            throw e;
         } finally {
            if (!useBatch) {
               update.close();
            }

         }

         return (boolean)versionability;
      } catch (SQLException e) {
         throw this.getFactory().getSQLExceptionHelper().convert(e, "could not update: " + MessageHelper.infoString((EntityPersister)this, (Object)id, (SessionFactoryImplementor)this.getFactory()), sql);
      }
   }

   protected void delete(Serializable id, Object version, int j, Object object, String sql, SessionImplementor session, Object[] loadedState) throws HibernateException {
      if (!this.isInverseTable(j)) {
         boolean useVersion = j == 0 && this.isVersioned();
         boolean callable = this.isDeleteCallable(j);
         Expectation expectation = Expectations.appropriateExpectation(this.deleteResultCheckStyles[j]);
         boolean useBatch = j == 0 && this.isBatchable() && expectation.canBeBatched();
         if (useBatch && this.deleteBatchKey == null) {
            this.deleteBatchKey = new BasicBatchKey(this.getEntityName() + "#DELETE", expectation);
         }

         if (LOG.isTraceEnabled()) {
            LOG.tracev("Deleting entity: {0}", MessageHelper.infoString((EntityPersister)this, (Object)id, (SessionFactoryImplementor)this.getFactory()));
            if (useVersion) {
               LOG.tracev("Version: {0}", version);
            }
         }

         if (this.isTableCascadeDeleteEnabled(j)) {
            if (LOG.isTraceEnabled()) {
               LOG.tracev("Delete handled by foreign key constraint: {0}", this.getTableName(j));
            }

         } else {
            try {
               int index = 1;
               PreparedStatement delete;
               if (useBatch) {
                  delete = session.getTransactionCoordinator().getJdbcCoordinator().getBatch(this.deleteBatchKey).getBatchStatement(sql, callable);
               } else {
                  delete = session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(sql, callable);
               }

               try {
                  index += expectation.prepare(delete);
                  this.getIdentifierType().nullSafeSet(delete, id, index, session);
                  index += this.getIdentifierColumnSpan();
                  if (useVersion) {
                     this.getVersionType().nullSafeSet(delete, version, index, session);
                  } else if (this.isAllOrDirtyOptLocking() && loadedState != null) {
                     boolean[] versionability = this.getPropertyVersionability();
                     Type[] types = this.getPropertyTypes();

                     for(int i = 0; i < this.entityMetamodel.getPropertySpan(); ++i) {
                        if (this.isPropertyOfTable(i, j) && versionability[i]) {
                           boolean[] settable = types[i].toColumnNullness(loadedState[i], this.getFactory());
                           types[i].nullSafeSet(delete, loadedState[i], index, settable, session);
                           index += ArrayHelper.countTrue(settable);
                        }
                     }
                  }

                  if (useBatch) {
                     session.getTransactionCoordinator().getJdbcCoordinator().getBatch(this.deleteBatchKey).addToBatch();
                  } else {
                     this.check(delete.executeUpdate(), id, j, expectation, delete);
                  }
               } catch (SQLException sqle) {
                  if (useBatch) {
                     session.getTransactionCoordinator().getJdbcCoordinator().abortBatch();
                  }

                  throw sqle;
               } finally {
                  if (!useBatch) {
                     delete.close();
                  }

               }

            } catch (SQLException sqle) {
               throw this.getFactory().getSQLExceptionHelper().convert(sqle, "could not delete: " + MessageHelper.infoString((EntityPersister)this, (Object)id, (SessionFactoryImplementor)this.getFactory()), sql);
            }
         }
      }
   }

   private String[] getUpdateStrings(boolean byRowId, boolean lazy) {
      if (byRowId) {
         return lazy ? this.getSQLLazyUpdateByRowIdStrings() : this.getSQLUpdateByRowIdStrings();
      } else {
         return lazy ? this.getSQLLazyUpdateStrings() : this.getSQLUpdateStrings();
      }
   }

   public void update(Serializable id, Object[] fields, int[] dirtyFields, boolean hasDirtyCollection, Object[] oldFields, Object oldVersion, Object object, Object rowId, SessionImplementor session) throws HibernateException {
      boolean[] tableUpdateNeeded = this.getTableUpdateNeeded(dirtyFields, hasDirtyCollection);
      int span = this.getTableSpan();
      EntityEntry entry = session.getPersistenceContext().getEntry(object);
      if (entry == null && !this.isMutable()) {
         throw new IllegalStateException("Updating immutable entity that is not in session yet!");
      } else {
         boolean[] propsToUpdate;
         String[] updateStrings;
         if (this.entityMetamodel.isDynamicUpdate() && dirtyFields != null) {
            propsToUpdate = this.getPropertiesToUpdate(dirtyFields, hasDirtyCollection);
            updateStrings = new String[span];

            for(int j = 0; j < span; ++j) {
               updateStrings[j] = tableUpdateNeeded[j] ? this.generateUpdateString(propsToUpdate, j, oldFields, j == 0 && rowId != null) : null;
            }
         } else if (!this.isModifiableEntity(entry)) {
            propsToUpdate = this.getPropertiesToUpdate(dirtyFields == null ? ArrayHelper.EMPTY_INT_ARRAY : dirtyFields, hasDirtyCollection);
            updateStrings = new String[span];

            for(int j = 0; j < span; ++j) {
               updateStrings[j] = tableUpdateNeeded[j] ? this.generateUpdateString(propsToUpdate, j, oldFields, j == 0 && rowId != null) : null;
            }
         } else {
            updateStrings = this.getUpdateStrings(rowId != null, this.hasUninitializedLazyProperties(object));
            propsToUpdate = this.getPropertyUpdateability(object);
         }

         for(int j = 0; j < span; ++j) {
            if (tableUpdateNeeded[j]) {
               this.updateOrInsert(id, fields, oldFields, j == 0 ? rowId : null, propsToUpdate, j, oldVersion, object, updateStrings[j], session);
            }
         }

      }
   }

   public Serializable insert(Object[] fields, Object object, SessionImplementor session) throws HibernateException {
      int span = this.getTableSpan();
      Serializable id;
      if (this.entityMetamodel.isDynamicInsert()) {
         boolean[] notNull = this.getPropertiesToInsert(fields);
         id = this.insert(fields, notNull, this.generateInsertString(true, notNull), object, session);

         for(int j = 1; j < span; ++j) {
            this.insert(id, fields, notNull, j, this.generateInsertString(notNull, j), object, session);
         }
      } else {
         id = this.insert(fields, this.getPropertyInsertability(), this.getSQLIdentityInsertString(), object, session);

         for(int j = 1; j < span; ++j) {
            this.insert(id, fields, this.getPropertyInsertability(), j, this.getSQLInsertStrings()[j], object, session);
         }
      }

      return id;
   }

   public void insert(Serializable id, Object[] fields, Object object, SessionImplementor session) throws HibernateException {
      int span = this.getTableSpan();
      if (this.entityMetamodel.isDynamicInsert()) {
         boolean[] notNull = this.getPropertiesToInsert(fields);

         for(int j = 0; j < span; ++j) {
            this.insert(id, fields, notNull, j, this.generateInsertString(notNull, j), object, session);
         }
      } else {
         for(int j = 0; j < span; ++j) {
            this.insert(id, fields, this.getPropertyInsertability(), j, this.getSQLInsertStrings()[j], object, session);
         }
      }

   }

   public void delete(Serializable id, Object version, Object object, SessionImplementor session) throws HibernateException {
      int span = this.getTableSpan();
      boolean isImpliedOptimisticLocking = !this.entityMetamodel.isVersioned() && this.isAllOrDirtyOptLocking();
      Object[] loadedState = null;
      if (isImpliedOptimisticLocking) {
         EntityKey key = session.generateEntityKey(id, this);
         Object entity = session.getPersistenceContext().getEntity(key);
         if (entity != null) {
            EntityEntry entry = session.getPersistenceContext().getEntry(entity);
            loadedState = entry.getLoadedState();
         }
      }

      String[] deleteStrings;
      if (isImpliedOptimisticLocking && loadedState != null) {
         deleteStrings = this.generateSQLDeletStrings(loadedState);
      } else {
         deleteStrings = this.getSQLDeleteStrings();
      }

      for(int j = span - 1; j >= 0; --j) {
         this.delete(id, version, j, object, deleteStrings[j], session, loadedState);
      }

   }

   private boolean isAllOrDirtyOptLocking() {
      return this.entityMetamodel.getOptimisticLockStyle() == OptimisticLockStyle.DIRTY || this.entityMetamodel.getOptimisticLockStyle() == OptimisticLockStyle.ALL;
   }

   private String[] generateSQLDeletStrings(Object[] loadedState) {
      int span = this.getTableSpan();
      String[] deleteStrings = new String[span];

      for(int j = span - 1; j >= 0; --j) {
         Delete delete = (new Delete()).setTableName(this.getTableName(j)).addPrimaryKeyColumns(this.getKeyColumns(j));
         if (this.getFactory().getSettings().isCommentsEnabled()) {
            delete.setComment("delete " + this.getEntityName() + " [" + j + "]");
         }

         boolean[] versionability = this.getPropertyVersionability();
         Type[] types = this.getPropertyTypes();

         for(int i = 0; i < this.entityMetamodel.getPropertySpan(); ++i) {
            if (this.isPropertyOfTable(i, j) && versionability[i]) {
               String[] propertyColumnNames = this.getPropertyColumnNames(i);
               boolean[] propertyNullness = types[i].toColumnNullness(loadedState[i], this.getFactory());

               for(int k = 0; k < propertyNullness.length; ++k) {
                  if (propertyNullness[k]) {
                     delete.addWhereFragment(propertyColumnNames[k] + " = ?");
                  } else {
                     delete.addWhereFragment(propertyColumnNames[k] + " is null");
                  }
               }
            }
         }

         deleteStrings[j] = delete.toStatementString();
      }

      return deleteStrings;
   }

   protected void logStaticSQL() {
      if (LOG.isDebugEnabled()) {
         LOG.debugf("Static SQL for entity: %s", this.getEntityName());
         if (this.sqlLazySelectString != null) {
            LOG.debugf(" Lazy select: %s", this.sqlLazySelectString);
         }

         if (this.sqlVersionSelectString != null) {
            LOG.debugf(" Version select: %s", this.sqlVersionSelectString);
         }

         if (this.sqlSnapshotSelectString != null) {
            LOG.debugf(" Snapshot select: %s", this.sqlSnapshotSelectString);
         }

         for(int j = 0; j < this.getTableSpan(); ++j) {
            LOG.debugf(" Insert %s: %s", j, this.getSQLInsertStrings()[j]);
            LOG.debugf(" Update %s: %s", j, this.getSQLUpdateStrings()[j]);
            LOG.debugf(" Delete %s: %s", j, this.getSQLDeleteStrings()[j]);
         }

         if (this.sqlIdentityInsertString != null) {
            LOG.debugf(" Identity insert: %s", this.sqlIdentityInsertString);
         }

         if (this.sqlUpdateByRowIdString != null) {
            LOG.debugf(" Update by row id (all fields): %s", this.sqlUpdateByRowIdString);
         }

         if (this.sqlLazyUpdateByRowIdString != null) {
            LOG.debugf(" Update by row id (non-lazy fields): %s", this.sqlLazyUpdateByRowIdString);
         }

         if (this.sqlInsertGeneratedValuesSelectString != null) {
            LOG.debugf(" Insert-generated property select: %s", this.sqlInsertGeneratedValuesSelectString);
         }

         if (this.sqlUpdateGeneratedValuesSelectString != null) {
            LOG.debugf(" Update-generated property select: %s", this.sqlUpdateGeneratedValuesSelectString);
         }
      }

   }

   public String filterFragment(String alias, Map enabledFilters) throws MappingException {
      StringBuilder sessionFilterFragment = new StringBuilder();
      this.filterHelper.render(sessionFilterFragment, this.getFilterAliasGenerator(alias), enabledFilters);
      return sessionFilterFragment.append(this.filterFragment(alias)).toString();
   }

   public String generateFilterConditionAlias(String rootAlias) {
      return rootAlias;
   }

   public String oneToManyFilterFragment(String alias) throws MappingException {
      return "";
   }

   public String fromJoinFragment(String alias, boolean innerJoin, boolean includeSubclasses) {
      return this.getSubclassTableSpan() == 1 ? "" : this.createJoin(alias, innerJoin, includeSubclasses).toFromFragmentString();
   }

   public String whereJoinFragment(String alias, boolean innerJoin, boolean includeSubclasses) {
      return this.getSubclassTableSpan() == 1 ? "" : this.createJoin(alias, innerJoin, includeSubclasses).toWhereFragmentString();
   }

   protected boolean isSubclassTableLazy(int j) {
      return false;
   }

   protected JoinFragment createJoin(String name, boolean innerJoin, boolean includeSubclasses) {
      String[] idCols = StringHelper.qualify(name, this.getIdentifierColumnNames());
      JoinFragment join = this.getFactory().getDialect().createOuterJoinFragment();
      int tableSpan = this.getSubclassTableSpan();

      for(int j = 1; j < tableSpan; ++j) {
         boolean joinIsIncluded = this.isClassOrSuperclassTable(j) || includeSubclasses && !this.isSubclassTableSequentialSelect(j) && !this.isSubclassTableLazy(j);
         if (joinIsIncluded) {
            join.addJoin(this.getSubclassTableName(j), generateTableAlias(name, j), idCols, this.getSubclassTableKeyColumns(j), innerJoin && this.isClassOrSuperclassTable(j) && !this.isInverseTable(j) && !this.isNullableTable(j) ? JoinType.INNER_JOIN : JoinType.LEFT_OUTER_JOIN);
         }
      }

      return join;
   }

   protected JoinFragment createJoin(int[] tableNumbers, String drivingAlias) {
      String[] keyCols = StringHelper.qualify(drivingAlias, this.getSubclassTableKeyColumns(tableNumbers[0]));
      JoinFragment jf = this.getFactory().getDialect().createOuterJoinFragment();

      for(int i = 1; i < tableNumbers.length; ++i) {
         int j = tableNumbers[i];
         jf.addJoin(this.getSubclassTableName(j), generateTableAlias(this.getRootAlias(), j), keyCols, this.getSubclassTableKeyColumns(j), !this.isInverseSubclassTable(j) && !this.isNullableSubclassTable(j) ? JoinType.INNER_JOIN : JoinType.LEFT_OUTER_JOIN);
      }

      return jf;
   }

   protected SelectFragment createSelect(int[] subclassColumnNumbers, int[] subclassFormulaNumbers) {
      SelectFragment selectFragment = new SelectFragment();
      int[] columnTableNumbers = this.getSubclassColumnTableNumberClosure();
      String[] columnAliases = this.getSubclassColumnAliasClosure();
      String[] columnReaderTemplates = this.getSubclassColumnReaderTemplateClosure();

      for(int i = 0; i < subclassColumnNumbers.length; ++i) {
         int columnNumber = subclassColumnNumbers[i];
         if (this.subclassColumnSelectableClosure[columnNumber]) {
            String subalias = generateTableAlias(this.getRootAlias(), columnTableNumbers[columnNumber]);
            selectFragment.addColumnTemplate(subalias, columnReaderTemplates[columnNumber], columnAliases[columnNumber]);
         }
      }

      int[] formulaTableNumbers = this.getSubclassFormulaTableNumberClosure();
      String[] formulaTemplates = this.getSubclassFormulaTemplateClosure();
      String[] formulaAliases = this.getSubclassFormulaAliasClosure();

      for(int i = 0; i < subclassFormulaNumbers.length; ++i) {
         int formulaNumber = subclassFormulaNumbers[i];
         String subalias = generateTableAlias(this.getRootAlias(), formulaTableNumbers[formulaNumber]);
         selectFragment.addFormula(subalias, formulaTemplates[formulaNumber], formulaAliases[formulaNumber]);
      }

      return selectFragment;
   }

   protected String createFrom(int tableNumber, String alias) {
      return this.getSubclassTableName(tableNumber) + ' ' + alias;
   }

   protected String createWhereByKey(int tableNumber, String alias) {
      return StringHelper.join("=? and ", StringHelper.qualify(alias, this.getSubclassTableKeyColumns(tableNumber))) + "=?";
   }

   protected String renderSelect(int[] tableNumbers, int[] columnNumbers, int[] formulaNumbers) {
      Arrays.sort(tableNumbers);
      int drivingTable = tableNumbers[0];
      String drivingAlias = generateTableAlias(this.getRootAlias(), drivingTable);
      String where = this.createWhereByKey(drivingTable, drivingAlias);
      String from = this.createFrom(drivingTable, drivingAlias);
      JoinFragment jf = this.createJoin(tableNumbers, drivingAlias);
      SelectFragment selectFragment = this.createSelect(columnNumbers, formulaNumbers);
      Select select = new Select(this.getFactory().getDialect());
      select.setSelectClause(selectFragment.toFragmentString().substring(2));
      select.setFromClause(from);
      select.setWhereClause(where);
      select.setOuterJoins(jf.toFromFragmentString(), jf.toWhereFragmentString());
      if (this.getFactory().getSettings().isCommentsEnabled()) {
         select.setComment("sequential select " + this.getEntityName());
      }

      return select.toStatementString();
   }

   private String getRootAlias() {
      return StringHelper.generateAlias(this.getEntityName());
   }

   protected void postConstruct(Mapping mapping) throws MappingException {
      this.initPropertyPaths(mapping);
      int joinSpan = this.getTableSpan();
      this.sqlDeleteStrings = new String[joinSpan];
      this.sqlInsertStrings = new String[joinSpan];
      this.sqlUpdateStrings = new String[joinSpan];
      this.sqlLazyUpdateStrings = new String[joinSpan];
      this.sqlUpdateByRowIdString = this.rowIdName == null ? null : this.generateUpdateString(this.getPropertyUpdateability(), 0, true);
      this.sqlLazyUpdateByRowIdString = this.rowIdName == null ? null : this.generateUpdateString(this.getNonLazyPropertyUpdateability(), 0, true);

      for(int j = 0; j < joinSpan; ++j) {
         this.sqlInsertStrings[j] = this.customSQLInsert[j] == null ? this.generateInsertString(this.getPropertyInsertability(), j) : this.customSQLInsert[j];
         this.sqlUpdateStrings[j] = this.customSQLUpdate[j] == null ? this.generateUpdateString(this.getPropertyUpdateability(), j, false) : this.customSQLUpdate[j];
         this.sqlLazyUpdateStrings[j] = this.customSQLUpdate[j] == null ? this.generateUpdateString(this.getNonLazyPropertyUpdateability(), j, false) : this.customSQLUpdate[j];
         this.sqlDeleteStrings[j] = this.customSQLDelete[j] == null ? this.generateDeleteString(j) : this.customSQLDelete[j];
      }

      this.tableHasColumns = new boolean[joinSpan];

      for(int j = 0; j < joinSpan; ++j) {
         this.tableHasColumns[j] = this.sqlUpdateStrings[j] != null;
      }

      this.sqlSnapshotSelectString = this.generateSnapshotSelectString();
      this.sqlLazySelectString = this.generateLazySelectString();
      this.sqlVersionSelectString = this.generateSelectVersionString();
      if (this.hasInsertGeneratedProperties()) {
         this.sqlInsertGeneratedValuesSelectString = this.generateInsertGeneratedValuesSelectString();
      }

      if (this.hasUpdateGeneratedProperties()) {
         this.sqlUpdateGeneratedValuesSelectString = this.generateUpdateGeneratedValuesSelectString();
      }

      if (this.isIdentifierAssignedByInsert()) {
         this.identityDelegate = ((PostInsertIdentifierGenerator)this.getIdentifierGenerator()).getInsertGeneratedIdentifierDelegate(this, this.getFactory().getDialect(), this.useGetGeneratedKeys());
         this.sqlIdentityInsertString = this.customSQLInsert[0] == null ? this.generateIdentityInsertString(this.getPropertyInsertability()) : this.customSQLInsert[0];
      } else {
         this.sqlIdentityInsertString = null;
      }

      this.logStaticSQL();
   }

   public void postInstantiate() throws MappingException {
      this.createLoaders();
      this.createUniqueKeyLoaders();
      this.createQueryLoader();
   }

   protected Map getLoaders() {
      return this.loaders;
   }

   protected void createLoaders() {
      Map loaders = this.getLoaders();
      loaders.put(LockMode.NONE, this.createEntityLoader(LockMode.NONE));
      UniqueEntityLoader readLoader = this.createEntityLoader(LockMode.READ);
      loaders.put(LockMode.READ, readLoader);
      boolean disableForUpdate = this.getSubclassTableSpan() > 1 && this.hasSubclasses() && !this.getFactory().getDialect().supportsOuterJoinForUpdate();
      loaders.put(LockMode.UPGRADE, disableForUpdate ? readLoader : this.createEntityLoader(LockMode.UPGRADE));
      loaders.put(LockMode.UPGRADE_NOWAIT, disableForUpdate ? readLoader : this.createEntityLoader(LockMode.UPGRADE_NOWAIT));
      loaders.put(LockMode.FORCE, disableForUpdate ? readLoader : this.createEntityLoader(LockMode.FORCE));
      loaders.put(LockMode.PESSIMISTIC_READ, disableForUpdate ? readLoader : this.createEntityLoader(LockMode.PESSIMISTIC_READ));
      loaders.put(LockMode.PESSIMISTIC_WRITE, disableForUpdate ? readLoader : this.createEntityLoader(LockMode.PESSIMISTIC_WRITE));
      loaders.put(LockMode.PESSIMISTIC_FORCE_INCREMENT, disableForUpdate ? readLoader : this.createEntityLoader(LockMode.PESSIMISTIC_FORCE_INCREMENT));
      loaders.put(LockMode.OPTIMISTIC, this.createEntityLoader(LockMode.OPTIMISTIC));
      loaders.put(LockMode.OPTIMISTIC_FORCE_INCREMENT, this.createEntityLoader(LockMode.OPTIMISTIC_FORCE_INCREMENT));
      loaders.put("merge", new CascadeEntityLoader(this, CascadingAction.MERGE, this.getFactory()));
      loaders.put("refresh", new CascadeEntityLoader(this, CascadingAction.REFRESH, this.getFactory()));
   }

   protected void createQueryLoader() {
      if (this.loaderName != null) {
         this.queryLoader = new NamedQueryLoader(this.loaderName, this);
      }

   }

   public Object load(Serializable id, Object optionalObject, LockMode lockMode, SessionImplementor session) {
      return this.load(id, optionalObject, (new LockOptions()).setLockMode(lockMode), session);
   }

   public Object load(Serializable id, Object optionalObject, LockOptions lockOptions, SessionImplementor session) throws HibernateException {
      if (LOG.isTraceEnabled()) {
         LOG.tracev("Fetching entity: {0}", MessageHelper.infoString((EntityPersister)this, (Object)id, (SessionFactoryImplementor)this.getFactory()));
      }

      UniqueEntityLoader loader = this.getAppropriateLoader(lockOptions, session);
      return loader.load(id, optionalObject, session, lockOptions);
   }

   public void registerAffectingFetchProfile(String fetchProfileName) {
      this.affectingFetchProfileNames.add(fetchProfileName);
   }

   private boolean isAffectedByEnabledFetchProfiles(SessionImplementor session) {
      Iterator itr = session.getLoadQueryInfluencers().getEnabledFetchProfileNames().iterator();

      while(itr.hasNext()) {
         if (this.affectingFetchProfileNames.contains(itr.next())) {
            return true;
         }
      }

      return false;
   }

   private boolean isAffectedByEnabledFilters(SessionImplementor session) {
      return session.getLoadQueryInfluencers().hasEnabledFilters() && this.filterHelper.isAffectedBy(session.getLoadQueryInfluencers().getEnabledFilters());
   }

   private UniqueEntityLoader getAppropriateLoader(LockOptions lockOptions, SessionImplementor session) {
      if (this.queryLoader != null) {
         return this.queryLoader;
      } else if (this.isAffectedByEnabledFilters(session)) {
         return this.createEntityLoader(lockOptions, session.getLoadQueryInfluencers());
      } else if (session.getLoadQueryInfluencers().getInternalFetchProfile() != null && LockMode.UPGRADE.greaterThan(lockOptions.getLockMode())) {
         return (UniqueEntityLoader)this.getLoaders().get(session.getLoadQueryInfluencers().getInternalFetchProfile());
      } else if (this.isAffectedByEnabledFetchProfiles(session)) {
         return this.createEntityLoader(lockOptions, session.getLoadQueryInfluencers());
      } else {
         return lockOptions.getTimeOut() != -1 ? this.createEntityLoader(lockOptions, session.getLoadQueryInfluencers()) : (UniqueEntityLoader)this.getLoaders().get(lockOptions.getLockMode());
      }
   }

   private boolean isAllNull(Object[] array, int tableNumber) {
      for(int i = 0; i < array.length; ++i) {
         if (this.isPropertyOfTable(i, tableNumber) && array[i] != null) {
            return false;
         }
      }

      return true;
   }

   public boolean isSubclassPropertyNullable(int i) {
      return this.subclassPropertyNullabilityClosure[i];
   }

   protected final boolean[] getPropertiesToUpdate(int[] dirtyProperties, boolean hasDirtyCollection) {
      boolean[] propsToUpdate = new boolean[this.entityMetamodel.getPropertySpan()];
      boolean[] updateability = this.getPropertyUpdateability();

      for(int j = 0; j < dirtyProperties.length; ++j) {
         int property = dirtyProperties[j];
         if (updateability[property]) {
            propsToUpdate[property] = true;
         }
      }

      if (this.isVersioned() && updateability[this.getVersionProperty()]) {
         propsToUpdate[this.getVersionProperty()] = Versioning.isVersionIncrementRequired(dirtyProperties, hasDirtyCollection, this.getPropertyVersionability());
      }

      return propsToUpdate;
   }

   protected boolean[] getPropertiesToInsert(Object[] fields) {
      boolean[] notNull = new boolean[fields.length];
      boolean[] insertable = this.getPropertyInsertability();

      for(int i = 0; i < fields.length; ++i) {
         notNull[i] = insertable[i] && fields[i] != null;
      }

      return notNull;
   }

   public int[] findDirty(Object[] currentState, Object[] previousState, Object entity, SessionImplementor session) throws HibernateException {
      int[] props = TypeHelper.findDirty(this.entityMetamodel.getProperties(), currentState, previousState, this.propertyColumnUpdateable, this.hasUninitializedLazyProperties(entity), session);
      if (props == null) {
         return null;
      } else {
         this.logDirtyProperties(props);
         return props;
      }
   }

   public int[] findModified(Object[] old, Object[] current, Object entity, SessionImplementor session) throws HibernateException {
      int[] props = TypeHelper.findModified(this.entityMetamodel.getProperties(), current, old, this.propertyColumnUpdateable, this.hasUninitializedLazyProperties(entity), session);
      if (props == null) {
         return null;
      } else {
         this.logDirtyProperties(props);
         return props;
      }
   }

   protected boolean[] getPropertyUpdateability(Object entity) {
      return this.hasUninitializedLazyProperties(entity) ? this.getNonLazyPropertyUpdateability() : this.getPropertyUpdateability();
   }

   private void logDirtyProperties(int[] props) {
      if (LOG.isTraceEnabled()) {
         for(int i = 0; i < props.length; ++i) {
            String propertyName = this.entityMetamodel.getProperties()[props[i]].getName();
            LOG.trace(StringHelper.qualify(this.getEntityName(), propertyName) + " is dirty");
         }
      }

   }

   public SessionFactoryImplementor getFactory() {
      return this.factory;
   }

   public EntityMetamodel getEntityMetamodel() {
      return this.entityMetamodel;
   }

   public boolean hasCache() {
      return this.cacheAccessStrategy != null;
   }

   public EntityRegionAccessStrategy getCacheAccessStrategy() {
      return this.cacheAccessStrategy;
   }

   public CacheEntryStructure getCacheEntryStructure() {
      return this.cacheEntryStructure;
   }

   public boolean hasNaturalIdCache() {
      return this.naturalIdRegionAccessStrategy != null;
   }

   public NaturalIdRegionAccessStrategy getNaturalIdCacheAccessStrategy() {
      return this.naturalIdRegionAccessStrategy;
   }

   public Comparator getVersionComparator() {
      return this.isVersioned() ? this.getVersionType().getComparator() : null;
   }

   public final String getEntityName() {
      return this.entityMetamodel.getName();
   }

   public EntityType getEntityType() {
      return this.entityMetamodel.getEntityType();
   }

   public boolean isPolymorphic() {
      return this.entityMetamodel.isPolymorphic();
   }

   public boolean isInherited() {
      return this.entityMetamodel.isInherited();
   }

   public boolean hasCascades() {
      return this.entityMetamodel.hasCascades();
   }

   public boolean hasIdentifierProperty() {
      return !this.entityMetamodel.getIdentifierProperty().isVirtual();
   }

   public VersionType getVersionType() {
      return (VersionType)this.locateVersionType();
   }

   private Type locateVersionType() {
      return this.entityMetamodel.getVersionProperty() == null ? null : this.entityMetamodel.getVersionProperty().getType();
   }

   public int getVersionProperty() {
      return this.entityMetamodel.getVersionPropertyIndex();
   }

   public boolean isVersioned() {
      return this.entityMetamodel.isVersioned();
   }

   public boolean isIdentifierAssignedByInsert() {
      return this.entityMetamodel.getIdentifierProperty().isIdentifierAssignedByInsert();
   }

   public boolean hasLazyProperties() {
      return this.entityMetamodel.hasLazyProperties();
   }

   public void afterReassociate(Object entity, SessionImplementor session) {
      if (this.getEntityMetamodel().getInstrumentationMetadata().isInstrumented()) {
         FieldInterceptor interceptor = this.getEntityMetamodel().getInstrumentationMetadata().extractInterceptor(entity);
         if (interceptor != null) {
            interceptor.setSession(session);
         } else {
            FieldInterceptor fieldInterceptor = this.getEntityMetamodel().getInstrumentationMetadata().injectInterceptor(entity, this.getEntityName(), (Set)null, session);
            fieldInterceptor.dirty();
         }
      }

      this.handleNaturalIdReattachment(entity, session);
   }

   private void handleNaturalIdReattachment(Object entity, SessionImplementor session) {
      if (this.hasNaturalIdentifier()) {
         if (!this.getEntityMetamodel().hasImmutableNaturalId()) {
            PersistenceContext.NaturalIdHelper naturalIdHelper = session.getPersistenceContext().getNaturalIdHelper();
            Serializable id = this.getIdentifier(entity, session);
            Object[] entitySnapshot = session.getPersistenceContext().getDatabaseSnapshot(id, this);
            Object[] naturalIdSnapshot;
            if (entitySnapshot == StatefulPersistenceContext.NO_ROW) {
               naturalIdSnapshot = null;
            } else {
               naturalIdSnapshot = naturalIdHelper.extractNaturalIdValues((Object[])entitySnapshot, this);
            }

            naturalIdHelper.removeSharedNaturalIdCrossReference(this, id, naturalIdSnapshot);
            naturalIdHelper.manageLocalNaturalIdCrossReference(this, id, naturalIdHelper.extractNaturalIdValues((Object)entity, this), naturalIdSnapshot, CachedNaturalIdValueSource.UPDATE);
         }
      }
   }

   public Boolean isTransient(Object entity, SessionImplementor session) throws HibernateException {
      Serializable id;
      if (this.canExtractIdOutOfEntity()) {
         id = this.getIdentifier(entity, session);
      } else {
         id = null;
      }

      if (id == null) {
         return Boolean.TRUE;
      } else {
         Object version = this.getVersion(entity);
         if (this.isVersioned()) {
            Boolean result = this.entityMetamodel.getVersionProperty().getUnsavedValue().isUnsaved(version);
            if (result != null) {
               return result;
            }
         }

         Boolean result = this.entityMetamodel.getIdentifierProperty().getUnsavedValue().isUnsaved(id);
         if (result != null) {
            return result;
         } else {
            if (this.hasCache()) {
               CacheKey ck = session.generateCacheKey(id, this.getIdentifierType(), this.getRootEntityName());
               if (this.getCacheAccessStrategy().get(ck, session.getTimestamp()) != null) {
                  return Boolean.FALSE;
               }
            }

            return null;
         }
      }
   }

   public boolean hasCollections() {
      return this.entityMetamodel.hasCollections();
   }

   public boolean hasMutableProperties() {
      return this.entityMetamodel.hasMutableProperties();
   }

   public boolean isMutable() {
      return this.entityMetamodel.isMutable();
   }

   private boolean isModifiableEntity(EntityEntry entry) {
      return entry == null ? this.isMutable() : entry.isModifiableEntity();
   }

   public boolean isAbstract() {
      return this.entityMetamodel.isAbstract();
   }

   public boolean hasSubclasses() {
      return this.entityMetamodel.hasSubclasses();
   }

   public boolean hasProxy() {
      return this.entityMetamodel.isLazy();
   }

   public IdentifierGenerator getIdentifierGenerator() throws HibernateException {
      return this.entityMetamodel.getIdentifierProperty().getIdentifierGenerator();
   }

   public String getRootEntityName() {
      return this.entityMetamodel.getRootName();
   }

   public ClassMetadata getClassMetadata() {
      return this;
   }

   public String getMappedSuperclass() {
      return this.entityMetamodel.getSuperclass();
   }

   public boolean isExplicitPolymorphism() {
      return this.entityMetamodel.isExplicitPolymorphism();
   }

   protected boolean useDynamicUpdate() {
      return this.entityMetamodel.isDynamicUpdate();
   }

   protected boolean useDynamicInsert() {
      return this.entityMetamodel.isDynamicInsert();
   }

   protected boolean hasEmbeddedCompositeIdentifier() {
      return this.entityMetamodel.getIdentifierProperty().isEmbedded();
   }

   public boolean canExtractIdOutOfEntity() {
      return this.hasIdentifierProperty() || this.hasEmbeddedCompositeIdentifier() || this.hasIdentifierMapper();
   }

   private boolean hasIdentifierMapper() {
      return this.entityMetamodel.getIdentifierProperty().hasIdentifierMapper();
   }

   public String[] getKeyColumnNames() {
      return this.getIdentifierColumnNames();
   }

   public String getName() {
      return this.getEntityName();
   }

   public boolean isCollection() {
      return false;
   }

   public boolean consumesEntityAlias() {
      return true;
   }

   public boolean consumesCollectionAlias() {
      return false;
   }

   public Type getPropertyType(String propertyName) throws MappingException {
      return this.propertyMapping.toType(propertyName);
   }

   public Type getType() {
      return this.entityMetamodel.getEntityType();
   }

   public boolean isSelectBeforeUpdateRequired() {
      return this.entityMetamodel.isSelectBeforeUpdate();
   }

   protected final OptimisticLockStyle optimisticLockStyle() {
      return this.entityMetamodel.getOptimisticLockStyle();
   }

   public Object createProxy(Serializable id, SessionImplementor session) throws HibernateException {
      return this.entityMetamodel.getTuplizer().createProxy(id, session);
   }

   public String toString() {
      return StringHelper.unqualify(this.getClass().getName()) + '(' + this.entityMetamodel.getName() + ')';
   }

   public final String selectFragment(Joinable rhs, String rhsAlias, String lhsAlias, String entitySuffix, String collectionSuffix, boolean includeCollectionColumns) {
      return this.selectFragment(lhsAlias, entitySuffix);
   }

   public boolean isInstrumented() {
      return this.entityMetamodel.isInstrumented();
   }

   public boolean hasInsertGeneratedProperties() {
      return this.entityMetamodel.hasInsertGeneratedValues();
   }

   public boolean hasUpdateGeneratedProperties() {
      return this.entityMetamodel.hasUpdateGeneratedValues();
   }

   public boolean isVersionPropertyGenerated() {
      return this.isVersioned() && this.getPropertyUpdateGenerationInclusions()[this.getVersionProperty()] != ValueInclusion.NONE;
   }

   public boolean isVersionPropertyInsertable() {
      return this.isVersioned() && this.getPropertyInsertability()[this.getVersionProperty()];
   }

   public void afterInitialize(Object entity, boolean lazyPropertiesAreUnfetched, SessionImplementor session) {
      this.getEntityTuplizer().afterInitialize(entity, lazyPropertiesAreUnfetched, session);
   }

   public String[] getPropertyNames() {
      return this.entityMetamodel.getPropertyNames();
   }

   public Type[] getPropertyTypes() {
      return this.entityMetamodel.getPropertyTypes();
   }

   public boolean[] getPropertyLaziness() {
      return this.entityMetamodel.getPropertyLaziness();
   }

   public boolean[] getPropertyUpdateability() {
      return this.entityMetamodel.getPropertyUpdateability();
   }

   public boolean[] getPropertyCheckability() {
      return this.entityMetamodel.getPropertyCheckability();
   }

   public boolean[] getNonLazyPropertyUpdateability() {
      return this.entityMetamodel.getNonlazyPropertyUpdateability();
   }

   public boolean[] getPropertyInsertability() {
      return this.entityMetamodel.getPropertyInsertability();
   }

   public ValueInclusion[] getPropertyInsertGenerationInclusions() {
      return this.entityMetamodel.getPropertyInsertGenerationInclusions();
   }

   public ValueInclusion[] getPropertyUpdateGenerationInclusions() {
      return this.entityMetamodel.getPropertyUpdateGenerationInclusions();
   }

   public boolean[] getPropertyNullability() {
      return this.entityMetamodel.getPropertyNullability();
   }

   public boolean[] getPropertyVersionability() {
      return this.entityMetamodel.getPropertyVersionability();
   }

   public CascadeStyle[] getPropertyCascadeStyles() {
      return this.entityMetamodel.getCascadeStyles();
   }

   public final Class getMappedClass() {
      return this.getEntityTuplizer().getMappedClass();
   }

   public boolean implementsLifecycle() {
      return this.getEntityTuplizer().isLifecycleImplementor();
   }

   public Class getConcreteProxyClass() {
      return this.getEntityTuplizer().getConcreteProxyClass();
   }

   public void setPropertyValues(Object object, Object[] values) {
      this.getEntityTuplizer().setPropertyValues(object, values);
   }

   public void setPropertyValue(Object object, int i, Object value) {
      this.getEntityTuplizer().setPropertyValue(object, i, value);
   }

   public Object[] getPropertyValues(Object object) {
      return this.getEntityTuplizer().getPropertyValues(object);
   }

   public Object getPropertyValue(Object object, int i) {
      return this.getEntityTuplizer().getPropertyValue(object, i);
   }

   public Object getPropertyValue(Object object, String propertyName) {
      return this.getEntityTuplizer().getPropertyValue(object, propertyName);
   }

   public Serializable getIdentifier(Object object) {
      return this.getEntityTuplizer().getIdentifier(object, (SessionImplementor)null);
   }

   public Serializable getIdentifier(Object entity, SessionImplementor session) {
      return this.getEntityTuplizer().getIdentifier(entity, session);
   }

   public void setIdentifier(Object entity, Serializable id, SessionImplementor session) {
      this.getEntityTuplizer().setIdentifier(entity, id, session);
   }

   public Object getVersion(Object object) {
      return this.getEntityTuplizer().getVersion(object);
   }

   public Object instantiate(Serializable id, SessionImplementor session) {
      return this.getEntityTuplizer().instantiate(id, session);
   }

   public boolean isInstance(Object object) {
      return this.getEntityTuplizer().isInstance(object);
   }

   public boolean hasUninitializedLazyProperties(Object object) {
      return this.getEntityTuplizer().hasUninitializedLazyProperties(object);
   }

   public void resetIdentifier(Object entity, Serializable currentId, Object currentVersion, SessionImplementor session) {
      this.getEntityTuplizer().resetIdentifier(entity, currentId, currentVersion, session);
   }

   public EntityPersister getSubclassEntityPersister(Object instance, SessionFactoryImplementor factory) {
      if (!this.hasSubclasses()) {
         return this;
      } else {
         String concreteEntityName = this.getEntityTuplizer().determineConcreteSubclassEntityName(instance, factory);
         return (EntityPersister)(concreteEntityName != null && !this.getEntityName().equals(concreteEntityName) ? factory.getEntityPersister(concreteEntityName) : this);
      }
   }

   public boolean isMultiTable() {
      return false;
   }

   public String getTemporaryIdTableName() {
      return this.temporaryIdTableName;
   }

   public String getTemporaryIdTableDDL() {
      return this.temporaryIdTableDDL;
   }

   protected int getPropertySpan() {
      return this.entityMetamodel.getPropertySpan();
   }

   public Object[] getPropertyValuesToInsert(Object object, Map mergeMap, SessionImplementor session) throws HibernateException {
      return this.getEntityTuplizer().getPropertyValuesToInsert(object, mergeMap, session);
   }

   public void processInsertGeneratedProperties(Serializable id, Object entity, Object[] state, SessionImplementor session) {
      if (!this.hasInsertGeneratedProperties()) {
         throw new AssertionFailure("no insert-generated properties");
      } else {
         this.processGeneratedProperties(id, entity, state, session, this.sqlInsertGeneratedValuesSelectString, this.getPropertyInsertGenerationInclusions());
      }
   }

   public void processUpdateGeneratedProperties(Serializable id, Object entity, Object[] state, SessionImplementor session) {
      if (!this.hasUpdateGeneratedProperties()) {
         throw new AssertionFailure("no update-generated properties");
      } else {
         this.processGeneratedProperties(id, entity, state, session, this.sqlUpdateGeneratedValuesSelectString, this.getPropertyUpdateGenerationInclusions());
      }
   }

   private void processGeneratedProperties(Serializable id, Object entity, Object[] state, SessionImplementor session, String selectionSQL, ValueInclusion[] includeds) {
      session.getTransactionCoordinator().getJdbcCoordinator().executeBatch();

      try {
         PreparedStatement ps = session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(selectionSQL);

         try {
            this.getIdentifierType().nullSafeSet(ps, id, 1, session);
            ResultSet rs = ps.executeQuery();

            try {
               if (!rs.next()) {
                  throw new HibernateException("Unable to locate row for retrieval of generated properties: " + MessageHelper.infoString((EntityPersister)this, (Object)id, (SessionFactoryImplementor)this.getFactory()));
               }

               for(int i = 0; i < this.getPropertySpan(); ++i) {
                  if (includeds[i] != ValueInclusion.NONE) {
                     Object hydratedState = this.getPropertyTypes()[i].hydrate(rs, this.getPropertyAliases("", i), session, entity);
                     state[i] = this.getPropertyTypes()[i].resolve(hydratedState, session, entity);
                     this.setPropertyValue(entity, i, state[i]);
                  }
               }
            } finally {
               if (rs != null) {
                  rs.close();
               }

            }
         } finally {
            ps.close();
         }

      } catch (SQLException e) {
         throw this.getFactory().getSQLExceptionHelper().convert(e, "unable to select generated column values", selectionSQL);
      }
   }

   public String getIdentifierPropertyName() {
      return this.entityMetamodel.getIdentifierProperty().getName();
   }

   public Type getIdentifierType() {
      return this.entityMetamodel.getIdentifierProperty().getType();
   }

   public boolean hasSubselectLoadableCollections() {
      return this.hasSubselectLoadableCollections;
   }

   public int[] getNaturalIdentifierProperties() {
      return this.entityMetamodel.getNaturalIdentifierProperties();
   }

   public Object[] getNaturalIdentifierSnapshot(Serializable id, SessionImplementor session) throws HibernateException {
      if (!this.hasNaturalIdentifier()) {
         throw new MappingException("persistent class did not define a natural-id : " + MessageHelper.infoString(this));
      } else {
         if (LOG.isTraceEnabled()) {
            LOG.tracev("Getting current natural-id snapshot state for: {0}", MessageHelper.infoString((EntityPersister)this, (Object)id, (SessionFactoryImplementor)this.getFactory()));
         }

         int[] naturalIdPropertyIndexes = this.getNaturalIdentifierProperties();
         int naturalIdPropertyCount = naturalIdPropertyIndexes.length;
         boolean[] naturalIdMarkers = new boolean[this.getPropertySpan()];
         Type[] extractionTypes = new Type[naturalIdPropertyCount];

         for(int i = 0; i < naturalIdPropertyCount; ++i) {
            extractionTypes[i] = this.getPropertyTypes()[naturalIdPropertyIndexes[i]];
            naturalIdMarkers[naturalIdPropertyIndexes[i]] = true;
         }

         Select select = new Select(this.getFactory().getDialect());
         if (this.getFactory().getSettings().isCommentsEnabled()) {
            select.setComment("get current natural-id state " + this.getEntityName());
         }

         select.setSelectClause(this.concretePropertySelectFragmentSansLeadingComma(this.getRootAlias(), naturalIdMarkers));
         select.setFromClause(this.fromTableFragment(this.getRootAlias()) + this.fromJoinFragment(this.getRootAlias(), true, false));
         String[] aliasedIdColumns = StringHelper.qualify(this.getRootAlias(), this.getIdentifierColumnNames());
         String whereClause = StringHelper.join("=? and ", aliasedIdColumns) + "=?" + this.whereJoinFragment(this.getRootAlias(), true, false);
         String sql = select.setOuterJoins("", "").setWhereClause(whereClause).toStatementString();
         Object[] snapshot = new Object[naturalIdPropertyCount];

         try {
            PreparedStatement ps = session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(sql);

            EntityKey key;
            try {
               this.getIdentifierType().nullSafeSet(ps, id, 1, session);
               ResultSet rs = ps.executeQuery();

               try {
                  if (rs.next()) {
                     key = session.generateEntityKey(id, this);
                     Object owner = session.getPersistenceContext().getEntity(key);

                     for(int i = 0; i < naturalIdPropertyCount; ++i) {
                        snapshot[i] = extractionTypes[i].hydrate(rs, this.getPropertyAliases("", naturalIdPropertyIndexes[i]), session, (Object)null);
                        if (extractionTypes[i].isEntityType()) {
                           snapshot[i] = extractionTypes[i].resolve(snapshot[i], session, owner);
                        }
                     }

                     Object[] var29 = snapshot;
                     return var29;
                  }

                  key = null;
               } finally {
                  rs.close();
               }
            } finally {
               ps.close();
            }

            return key;
         } catch (SQLException e) {
            throw this.getFactory().getSQLExceptionHelper().convert(e, "could not retrieve snapshot: " + MessageHelper.infoString((EntityPersister)this, (Object)id, (SessionFactoryImplementor)this.getFactory()), sql);
         }
      }
   }

   public Serializable loadEntityIdByNaturalId(Object[] naturalIdValues, LockOptions lockOptions, SessionImplementor session) {
      if (LOG.isTraceEnabled()) {
         LOG.tracef("Resolving natural-id [%s] to id : %s ", naturalIdValues, MessageHelper.infoString(this));
      }

      boolean[] valueNullness = this.determineValueNullness(naturalIdValues);
      String sqlEntityIdByNaturalIdString = this.determinePkByNaturalIdQuery(valueNullness);

      try {
         PreparedStatement ps = session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(sqlEntityIdByNaturalIdString);

         Serializable len$;
         try {
            int positions = 1;
            int loop = 0;

            for(int idPosition : this.getNaturalIdentifierProperties()) {
               Object naturalIdValue = naturalIdValues[loop++];
               if (naturalIdValue != null) {
                  Type type = this.getPropertyTypes()[idPosition];
                  type.nullSafeSet(ps, naturalIdValue, positions, session);
                  positions += type.getColumnSpan(session.getFactory());
               }
            }

            ResultSet rs = ps.executeQuery();

            try {
               if (rs.next()) {
                  len$ = (Serializable)this.getIdentifierType().hydrate(rs, this.getIdentifierAliases(), session, (Object)null);
                  return len$;
               }

               len$ = null;
            } finally {
               rs.close();
            }
         } finally {
            ps.close();
         }

         return len$;
      } catch (SQLException e) {
         throw this.getFactory().getSQLExceptionHelper().convert(e, String.format("could not resolve natural-id [%s] to id : %s", naturalIdValues, MessageHelper.infoString(this)), sqlEntityIdByNaturalIdString);
      }
   }

   private boolean[] determineValueNullness(Object[] naturalIdValues) {
      boolean[] nullness = new boolean[naturalIdValues.length];

      for(int i = 0; i < naturalIdValues.length; ++i) {
         nullness[i] = naturalIdValues[i] == null;
      }

      return nullness;
   }

   private String determinePkByNaturalIdQuery(boolean[] valueNullness) {
      if (!this.hasNaturalIdentifier()) {
         throw new HibernateException("Attempt to build natural-id -> PK resolution query for entity that does not define natural id");
      } else if (this.isNaturalIdNonNullable()) {
         if (valueNullness != null && !ArrayHelper.isAllFalse(valueNullness)) {
            throw new HibernateException("Null value(s) passed to lookup by non-nullable natural-id");
         } else {
            if (this.cachedPkByNonNullableNaturalIdQuery == null) {
               this.cachedPkByNonNullableNaturalIdQuery = this.generateEntityIdByNaturalIdSql((boolean[])null);
            }

            return this.cachedPkByNonNullableNaturalIdQuery;
         }
      } else {
         return this.generateEntityIdByNaturalIdSql(valueNullness);
      }
   }

   protected boolean isNaturalIdNonNullable() {
      if (this.naturalIdIsNonNullable == null) {
         this.naturalIdIsNonNullable = this.determineNaturalIdNullability();
      }

      return this.naturalIdIsNonNullable;
   }

   private boolean determineNaturalIdNullability() {
      boolean[] nullability = this.getPropertyNullability();

      for(int position : this.getNaturalIdentifierProperties()) {
         if (nullability[position]) {
            return false;
         }
      }

      return true;
   }

   private String generateEntityIdByNaturalIdSql(boolean[] valueNullness) {
      EntityPersister rootPersister = this.getFactory().getEntityPersister(this.getRootEntityName());
      if (rootPersister != this && rootPersister instanceof AbstractEntityPersister) {
         return ((AbstractEntityPersister)rootPersister).generateEntityIdByNaturalIdSql(valueNullness);
      } else {
         Select select = new Select(this.getFactory().getDialect());
         if (this.getFactory().getSettings().isCommentsEnabled()) {
            select.setComment("get current natural-id->entity-id state " + this.getEntityName());
         }

         String rootAlias = this.getRootAlias();
         select.setSelectClause(this.identifierSelectFragment(rootAlias, ""));
         select.setFromClause(this.fromTableFragment(rootAlias) + this.fromJoinFragment(rootAlias, true, false));
         StringBuilder whereClause = new StringBuilder();
         int[] propertyTableNumbers = this.getPropertyTableNumbers();
         int[] naturalIdPropertyIndexes = this.getNaturalIdentifierProperties();
         int valuesIndex = -1;

         for(int propIdx = 0; propIdx < naturalIdPropertyIndexes.length; ++propIdx) {
            ++valuesIndex;
            if (propIdx > 0) {
               whereClause.append(" and ");
            }

            int naturalIdIdx = naturalIdPropertyIndexes[propIdx];
            String tableAlias = generateTableAlias(rootAlias, propertyTableNumbers[naturalIdIdx]);
            String[] propertyColumnNames = this.getPropertyColumnNames(naturalIdIdx);
            String[] aliasedPropertyColumns = StringHelper.qualify(tableAlias, propertyColumnNames);
            if (valueNullness != null && valueNullness[valuesIndex]) {
               whereClause.append(StringHelper.join(" is null and ", aliasedPropertyColumns)).append(" is null");
            } else {
               whereClause.append(StringHelper.join("=? and ", aliasedPropertyColumns)).append("=?");
            }
         }

         whereClause.append(this.whereJoinFragment(this.getRootAlias(), true, false));
         return select.setOuterJoins("", "").setWhereClause(whereClause.toString()).toStatementString();
      }
   }

   protected String concretePropertySelectFragmentSansLeadingComma(String alias, boolean[] include) {
      String concretePropertySelectFragment = this.concretePropertySelectFragment(alias, include);
      int firstComma = concretePropertySelectFragment.indexOf(", ");
      if (firstComma == 0) {
         concretePropertySelectFragment = concretePropertySelectFragment.substring(2);
      }

      return concretePropertySelectFragment;
   }

   public boolean hasNaturalIdentifier() {
      return this.entityMetamodel.hasNaturalIdentifier();
   }

   public void setPropertyValue(Object object, String propertyName, Object value) {
      this.getEntityTuplizer().setPropertyValue(object, propertyName, value);
   }

   public static int getTableId(String tableName, String[] tables) {
      for(int j = 0; j < tables.length; ++j) {
         if (tableName.equalsIgnoreCase(tables[j])) {
            return j;
         }
      }

      throw new AssertionFailure("Table " + tableName + " not found");
   }

   public EntityMode getEntityMode() {
      return this.entityMetamodel.getEntityMode();
   }

   public EntityTuplizer getEntityTuplizer() {
      return this.entityTuplizer;
   }

   public EntityInstrumentationMetadata getInstrumentationMetadata() {
      return this.entityMetamodel.getInstrumentationMetadata();
   }

   public String getTableAliasForColumn(String columnName, String rootAlias) {
      return generateTableAlias(rootAlias, this.determineTableNumberForColumn(columnName));
   }

   public int determineTableNumberForColumn(String columnName) {
      return 0;
   }

   protected interface InclusionChecker {
      boolean includeProperty(int var1);
   }
}
