package org.hibernate.cfg.annotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.persistence.Access;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.SecondaryTables;
import org.hibernate.AnnotationException;
import org.hibernate.AssertionFailure;
import org.hibernate.EntityMode;
import org.hibernate.MappingException;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Loader;
import org.hibernate.annotations.NaturalIdCache;
import org.hibernate.annotations.OptimisticLockType;
import org.hibernate.annotations.OptimisticLocking;
import org.hibernate.annotations.Persister;
import org.hibernate.annotations.Polymorphism;
import org.hibernate.annotations.PolymorphismType;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.RowId;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLDeleteAll;
import org.hibernate.annotations.SQLInsert;
import org.hibernate.annotations.SQLUpdate;
import org.hibernate.annotations.SelectBeforeUpdate;
import org.hibernate.annotations.Subselect;
import org.hibernate.annotations.Synchronize;
import org.hibernate.annotations.Tables;
import org.hibernate.annotations.Tuplizer;
import org.hibernate.annotations.Tuplizers;
import org.hibernate.annotations.Where;
import org.hibernate.annotations.common.reflection.XAnnotatedElement;
import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.cfg.AccessType;
import org.hibernate.cfg.AnnotationBinder;
import org.hibernate.cfg.BinderHelper;
import org.hibernate.cfg.Ejb3JoinColumn;
import org.hibernate.cfg.InheritanceState;
import org.hibernate.cfg.Mappings;
import org.hibernate.cfg.NamingStrategy;
import org.hibernate.cfg.ObjectNameNormalizer;
import org.hibernate.cfg.ObjectNameSource;
import org.hibernate.cfg.PropertyHolder;
import org.hibernate.cfg.UniqueConstraintHolder;
import org.hibernate.engine.spi.ExecuteUpdateResultCheckStyle;
import org.hibernate.engine.spi.FilterDefinition;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.mapping.DependantValue;
import org.hibernate.mapping.Join;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.RootClass;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.TableOwner;
import org.hibernate.mapping.Value;
import org.jboss.logging.Logger;

public class EntityBinder {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, EntityBinder.class.getName());
   private static final String NATURAL_ID_CACHE_SUFFIX = "##NaturalId";
   private String name;
   private XClass annotatedClass;
   private PersistentClass persistentClass;
   private Mappings mappings;
   private String discriminatorValue = "";
   private Boolean forceDiscriminator;
   private Boolean insertableDiscriminator;
   private boolean dynamicInsert;
   private boolean dynamicUpdate;
   private boolean explicitHibernateEntityAnnotation;
   private OptimisticLockType optimisticLockType;
   private PolymorphismType polymorphismType;
   private boolean selectBeforeUpdate;
   private int batchSize;
   private boolean lazy;
   private XClass proxyClass;
   private String where;
   private Map secondaryTables = new HashMap();
   private Map secondaryTableJoins = new HashMap();
   private String cacheConcurrentStrategy;
   private String cacheRegion;
   private String naturalIdCacheRegion;
   private List filters = new ArrayList();
   private InheritanceState inheritanceState;
   private boolean ignoreIdAnnotations;
   private boolean cacheLazyProperty;
   private AccessType propertyAccessType;
   private boolean wrapIdsInEmbeddedComponents;
   private String subselect;
   private static SecondaryTableNamingStrategyHelper SEC_TBL_NS_HELPER = new SecondaryTableNamingStrategyHelper();

   public boolean wrapIdsInEmbeddedComponents() {
      return this.wrapIdsInEmbeddedComponents;
   }

   public EntityBinder() {
      this.propertyAccessType = AccessType.DEFAULT;
   }

   public EntityBinder(Entity ejb3Ann, org.hibernate.annotations.Entity hibAnn, XClass annotatedClass, PersistentClass persistentClass, Mappings mappings) {
      this.propertyAccessType = AccessType.DEFAULT;
      this.mappings = mappings;
      this.persistentClass = persistentClass;
      this.annotatedClass = annotatedClass;
      this.bindEjb3Annotation(ejb3Ann);
      this.bindHibernateAnnotation(hibAnn);
   }

   private void bindHibernateAnnotation(org.hibernate.annotations.Entity hibAnn) {
      DynamicInsert dynamicInsertAnn = (DynamicInsert)this.annotatedClass.getAnnotation(DynamicInsert.class);
      this.dynamicInsert = dynamicInsertAnn == null ? (hibAnn == null ? false : hibAnn.dynamicInsert()) : dynamicInsertAnn.value();
      DynamicUpdate dynamicUpdateAnn = (DynamicUpdate)this.annotatedClass.getAnnotation(DynamicUpdate.class);
      this.dynamicUpdate = dynamicUpdateAnn == null ? (hibAnn == null ? false : hibAnn.dynamicUpdate()) : dynamicUpdateAnn.value();
      SelectBeforeUpdate selectBeforeUpdateAnn = (SelectBeforeUpdate)this.annotatedClass.getAnnotation(SelectBeforeUpdate.class);
      this.selectBeforeUpdate = selectBeforeUpdateAnn == null ? (hibAnn == null ? false : hibAnn.selectBeforeUpdate()) : selectBeforeUpdateAnn.value();
      OptimisticLocking optimisticLockingAnn = (OptimisticLocking)this.annotatedClass.getAnnotation(OptimisticLocking.class);
      this.optimisticLockType = optimisticLockingAnn == null ? (hibAnn == null ? OptimisticLockType.VERSION : hibAnn.optimisticLock()) : optimisticLockingAnn.type();
      Polymorphism polymorphismAnn = (Polymorphism)this.annotatedClass.getAnnotation(Polymorphism.class);
      this.polymorphismType = polymorphismAnn == null ? (hibAnn == null ? PolymorphismType.IMPLICIT : hibAnn.polymorphism()) : polymorphismAnn.type();
      if (hibAnn != null) {
         this.explicitHibernateEntityAnnotation = true;
      }

   }

   private void bindEjb3Annotation(Entity ejb3Ann) {
      if (ejb3Ann == null) {
         throw new AssertionFailure("@Entity should always be not null");
      } else {
         if (BinderHelper.isEmptyAnnotationValue(ejb3Ann.name())) {
            this.name = StringHelper.unqualify(this.annotatedClass.getName());
         } else {
            this.name = ejb3Ann.name();
         }

      }
   }

   public boolean isRootEntity() {
      return this.persistentClass instanceof RootClass;
   }

   public void setDiscriminatorValue(String discriminatorValue) {
      this.discriminatorValue = discriminatorValue;
   }

   public void setForceDiscriminator(boolean forceDiscriminator) {
      this.forceDiscriminator = forceDiscriminator;
   }

   public void setInsertableDiscriminator(boolean insertableDiscriminator) {
      this.insertableDiscriminator = insertableDiscriminator;
   }

   public void bindEntity() {
      this.persistentClass.setAbstract(this.annotatedClass.isAbstract());
      this.persistentClass.setClassName(this.annotatedClass.getName());
      this.persistentClass.setNodeName(this.name);
      this.persistentClass.setJpaEntityName(this.name);
      this.persistentClass.setEntityName(this.annotatedClass.getName());
      this.bindDiscriminatorValue();
      this.persistentClass.setLazy(this.lazy);
      if (this.proxyClass != null) {
         this.persistentClass.setProxyInterfaceName(this.proxyClass.getName());
      }

      this.persistentClass.setDynamicInsert(this.dynamicInsert);
      this.persistentClass.setDynamicUpdate(this.dynamicUpdate);
      if (this.persistentClass instanceof RootClass) {
         RootClass rootClass = (RootClass)this.persistentClass;
         boolean mutable = true;
         if (this.annotatedClass.isAnnotationPresent(Immutable.class)) {
            mutable = false;
         } else {
            org.hibernate.annotations.Entity entityAnn = (org.hibernate.annotations.Entity)this.annotatedClass.getAnnotation(org.hibernate.annotations.Entity.class);
            if (entityAnn != null) {
               mutable = entityAnn.mutable();
            }
         }

         rootClass.setMutable(mutable);
         rootClass.setExplicitPolymorphism(this.isExplicitPolymorphism(this.polymorphismType));
         if (StringHelper.isNotEmpty(this.where)) {
            rootClass.setWhere(this.where);
         }

         if (this.cacheConcurrentStrategy != null) {
            rootClass.setCacheConcurrencyStrategy(this.cacheConcurrentStrategy);
            rootClass.setCacheRegionName(this.cacheRegion);
            rootClass.setLazyPropertiesCacheable(this.cacheLazyProperty);
         }

         rootClass.setNaturalIdCacheRegionName(this.naturalIdCacheRegion);
         boolean forceDiscriminatorInSelects = this.forceDiscriminator == null ? this.mappings.forceDiscriminatorInSelectsByDefault() : this.forceDiscriminator;
         rootClass.setForceDiscriminator(forceDiscriminatorInSelects);
         if (this.insertableDiscriminator != null) {
            rootClass.setDiscriminatorInsertable(this.insertableDiscriminator);
         }
      } else {
         if (this.explicitHibernateEntityAnnotation) {
            LOG.entityAnnotationOnNonRoot(this.annotatedClass.getName());
         }

         if (this.annotatedClass.isAnnotationPresent(Immutable.class)) {
            LOG.immutableAnnotationOnNonRoot(this.annotatedClass.getName());
         }
      }

      this.persistentClass.setOptimisticLockMode(this.getVersioning(this.optimisticLockType));
      this.persistentClass.setSelectBeforeUpdate(this.selectBeforeUpdate);
      Persister persisterAnn = (Persister)this.annotatedClass.getAnnotation(Persister.class);
      Class persister = null;
      if (persisterAnn != null) {
         persister = persisterAnn.impl();
      } else {
         org.hibernate.annotations.Entity entityAnn = (org.hibernate.annotations.Entity)this.annotatedClass.getAnnotation(org.hibernate.annotations.Entity.class);
         if (entityAnn != null && !BinderHelper.isEmptyAnnotationValue(entityAnn.persister())) {
            try {
               persister = ReflectHelper.classForName(entityAnn.persister());
            } catch (ClassNotFoundException var15) {
               throw new AnnotationException("Could not find persister class: " + persister);
            }
         }
      }

      if (persister != null) {
         this.persistentClass.setEntityPersisterClass(persister);
      }

      this.persistentClass.setBatchSize(this.batchSize);
      SQLInsert sqlInsert = (SQLInsert)this.annotatedClass.getAnnotation(SQLInsert.class);
      SQLUpdate sqlUpdate = (SQLUpdate)this.annotatedClass.getAnnotation(SQLUpdate.class);
      SQLDelete sqlDelete = (SQLDelete)this.annotatedClass.getAnnotation(SQLDelete.class);
      SQLDeleteAll sqlDeleteAll = (SQLDeleteAll)this.annotatedClass.getAnnotation(SQLDeleteAll.class);
      Loader loader = (Loader)this.annotatedClass.getAnnotation(Loader.class);
      if (sqlInsert != null) {
         this.persistentClass.setCustomSQLInsert(sqlInsert.sql().trim(), sqlInsert.callable(), ExecuteUpdateResultCheckStyle.fromExternalName(sqlInsert.check().toString().toLowerCase()));
      }

      if (sqlUpdate != null) {
         this.persistentClass.setCustomSQLUpdate(sqlUpdate.sql(), sqlUpdate.callable(), ExecuteUpdateResultCheckStyle.fromExternalName(sqlUpdate.check().toString().toLowerCase()));
      }

      if (sqlDelete != null) {
         this.persistentClass.setCustomSQLDelete(sqlDelete.sql(), sqlDelete.callable(), ExecuteUpdateResultCheckStyle.fromExternalName(sqlDelete.check().toString().toLowerCase()));
      }

      if (sqlDeleteAll != null) {
         this.persistentClass.setCustomSQLDelete(sqlDeleteAll.sql(), sqlDeleteAll.callable(), ExecuteUpdateResultCheckStyle.fromExternalName(sqlDeleteAll.check().toString().toLowerCase()));
      }

      if (loader != null) {
         this.persistentClass.setLoaderName(loader.namedQuery());
      }

      if (this.annotatedClass.isAnnotationPresent(Synchronize.class)) {
         Synchronize synchronizedWith = (Synchronize)this.annotatedClass.getAnnotation(Synchronize.class);
         String[] tables = synchronizedWith.value();

         for(String table : tables) {
            this.persistentClass.addSynchronizedTable(table);
         }
      }

      if (this.annotatedClass.isAnnotationPresent(Subselect.class)) {
         Subselect subselect = (Subselect)this.annotatedClass.getAnnotation(Subselect.class);
         this.subselect = subselect.value();
      }

      if (this.annotatedClass.isAnnotationPresent(Tuplizers.class)) {
         for(Tuplizer tuplizer : ((Tuplizers)this.annotatedClass.getAnnotation(Tuplizers.class)).value()) {
            EntityMode mode = EntityMode.parse(tuplizer.entityMode());
            this.persistentClass.addTuplizer(mode, tuplizer.impl().getName());
         }
      }

      if (this.annotatedClass.isAnnotationPresent(Tuplizer.class)) {
         Tuplizer tuplizer = (Tuplizer)this.annotatedClass.getAnnotation(Tuplizer.class);
         EntityMode mode = EntityMode.parse(tuplizer.entityMode());
         this.persistentClass.addTuplizer(mode, tuplizer.impl().getName());
      }

      for(Filter filter : this.filters) {
         String filterName = filter.name();
         String cond = filter.condition();
         if (BinderHelper.isEmptyAnnotationValue(cond)) {
            FilterDefinition definition = this.mappings.getFilterDefinition(filterName);
            cond = definition == null ? null : definition.getDefaultFilterCondition();
            if (StringHelper.isEmpty(cond)) {
               throw new AnnotationException("no filter condition found for filter " + filterName + " in " + this.name);
            }
         }

         this.persistentClass.addFilter(filterName, cond, filter.deduceAliasInjectionPoints(), BinderHelper.toAliasTableMap(filter.aliases()), BinderHelper.toAliasEntityMap(filter.aliases()));
      }

      LOG.debugf("Import with entity name %s", this.name);

      try {
         this.mappings.addImport(this.persistentClass.getEntityName(), this.name);
         String entityName = this.persistentClass.getEntityName();
         if (!entityName.equals(this.name)) {
            this.mappings.addImport(entityName, entityName);
         }

      } catch (MappingException me) {
         throw new AnnotationException("Use of the same entity name twice: " + this.name, me);
      }
   }

   public void bindDiscriminatorValue() {
      if (StringHelper.isEmpty(this.discriminatorValue)) {
         Value discriminator = this.persistentClass.getDiscriminator();
         if (discriminator == null) {
            this.persistentClass.setDiscriminatorValue(this.name);
         } else {
            if ("character".equals(discriminator.getType().getName())) {
               throw new AnnotationException("Using default @DiscriminatorValue for a discriminator of type CHAR is not safe");
            }

            if ("integer".equals(discriminator.getType().getName())) {
               this.persistentClass.setDiscriminatorValue(String.valueOf(this.name.hashCode()));
            } else {
               this.persistentClass.setDiscriminatorValue(this.name);
            }
         }
      } else {
         this.persistentClass.setDiscriminatorValue(this.discriminatorValue);
      }

   }

   int getVersioning(OptimisticLockType type) {
      switch (type) {
         case VERSION:
            return 0;
         case NONE:
            return -1;
         case DIRTY:
            return 1;
         case ALL:
            return 2;
         default:
            throw new AssertionFailure("optimistic locking not supported: " + type);
      }
   }

   private boolean isExplicitPolymorphism(PolymorphismType type) {
      switch (type) {
         case IMPLICIT:
            return false;
         case EXPLICIT:
            return true;
         default:
            throw new AssertionFailure("Unknown polymorphism type: " + type);
      }
   }

   public void setBatchSize(BatchSize sizeAnn) {
      if (sizeAnn != null) {
         this.batchSize = sizeAnn.size();
      } else {
         this.batchSize = -1;
      }

   }

   public void setProxy(Proxy proxy) {
      if (proxy != null) {
         this.lazy = proxy.lazy();
         if (!this.lazy) {
            this.proxyClass = null;
         } else if (AnnotationBinder.isDefault(this.mappings.getReflectionManager().toXClass(proxy.proxyClass()), this.mappings)) {
            this.proxyClass = this.annotatedClass;
         } else {
            this.proxyClass = this.mappings.getReflectionManager().toXClass(proxy.proxyClass());
         }
      } else {
         this.lazy = true;
         this.proxyClass = this.annotatedClass;
      }

   }

   public void setWhere(Where whereAnn) {
      if (whereAnn != null) {
         this.where = whereAnn.clause();
      }

   }

   public void setWrapIdsInEmbeddedComponents(boolean wrapIdsInEmbeddedComponents) {
      this.wrapIdsInEmbeddedComponents = wrapIdsInEmbeddedComponents;
   }

   public void bindTable(String schema, String catalog, String tableName, List uniqueConstraints, String constraints, Table denormalizedSuperclassTable) {
      EntityTableObjectNameSource tableNameContext = new EntityTableObjectNameSource(tableName, this.name);
      EntityTableNamingStrategyHelper namingStrategyHelper = new EntityTableNamingStrategyHelper(this.name);
      Table table = TableBinder.buildAndFillTable(schema, catalog, tableNameContext, namingStrategyHelper, this.persistentClass.isAbstract(), uniqueConstraints, constraints, denormalizedSuperclassTable, this.mappings, this.subselect);
      RowId rowId = (RowId)this.annotatedClass.getAnnotation(RowId.class);
      if (rowId != null) {
         table.setRowId(rowId.value());
      }

      if (this.persistentClass instanceof TableOwner) {
         LOG.debugf("Bind entity %s on table %s", this.persistentClass.getEntityName(), table.getName());
         ((TableOwner)this.persistentClass).setTable(table);
      } else {
         throw new AssertionFailure("binding a table for a subclass");
      }
   }

   public void finalSecondaryTableBinding(PropertyHolder propertyHolder) {
      Iterator joins = this.secondaryTables.values().iterator();

      for(Object uncastedColumn : this.secondaryTableJoins.values()) {
         Join join = (Join)joins.next();
         this.createPrimaryColumnsToSecondaryTable(uncastedColumn, propertyHolder, join);
      }

      this.mappings.addJoins(this.persistentClass, this.secondaryTables);
   }

   private void createPrimaryColumnsToSecondaryTable(Object uncastedColumn, PropertyHolder propertyHolder, Join join) {
      PrimaryKeyJoinColumn[] pkColumnsAnn = null;
      JoinColumn[] joinColumnsAnn = null;
      if (uncastedColumn instanceof PrimaryKeyJoinColumn[]) {
         pkColumnsAnn = (PrimaryKeyJoinColumn[])uncastedColumn;
      }

      if (uncastedColumn instanceof JoinColumn[]) {
         joinColumnsAnn = (JoinColumn[])uncastedColumn;
      }

      Ejb3JoinColumn[] ejb3JoinColumns;
      if (pkColumnsAnn == null && joinColumnsAnn == null) {
         ejb3JoinColumns = new Ejb3JoinColumn[1];
         ejb3JoinColumns[0] = Ejb3JoinColumn.buildJoinColumn((PrimaryKeyJoinColumn)null, (JoinColumn)null, this.persistentClass.getIdentifier(), this.secondaryTables, propertyHolder, this.mappings);
      } else {
         int nbrOfJoinColumns = pkColumnsAnn != null ? pkColumnsAnn.length : joinColumnsAnn.length;
         if (nbrOfJoinColumns == 0) {
            ejb3JoinColumns = new Ejb3JoinColumn[]{Ejb3JoinColumn.buildJoinColumn((PrimaryKeyJoinColumn)null, (JoinColumn)null, this.persistentClass.getIdentifier(), this.secondaryTables, propertyHolder, this.mappings)};
         } else {
            ejb3JoinColumns = new Ejb3JoinColumn[nbrOfJoinColumns];
            if (pkColumnsAnn != null) {
               for(int colIndex = 0; colIndex < nbrOfJoinColumns; ++colIndex) {
                  ejb3JoinColumns[colIndex] = Ejb3JoinColumn.buildJoinColumn(pkColumnsAnn[colIndex], (JoinColumn)null, this.persistentClass.getIdentifier(), this.secondaryTables, propertyHolder, this.mappings);
               }
            } else {
               for(int colIndex = 0; colIndex < nbrOfJoinColumns; ++colIndex) {
                  ejb3JoinColumns[colIndex] = Ejb3JoinColumn.buildJoinColumn((PrimaryKeyJoinColumn)null, joinColumnsAnn[colIndex], this.persistentClass.getIdentifier(), this.secondaryTables, propertyHolder, this.mappings);
               }
            }
         }
      }

      for(Ejb3JoinColumn joinColumn : ejb3JoinColumns) {
         joinColumn.forceNotNull();
      }

      this.bindJoinToPersistentClass(join, ejb3JoinColumns, this.mappings);
   }

   private void bindJoinToPersistentClass(Join join, Ejb3JoinColumn[] ejb3JoinColumns, Mappings mappings) {
      SimpleValue key = new DependantValue(mappings, join.getTable(), this.persistentClass.getIdentifier());
      join.setKey(key);
      this.setFKNameIfDefined(join);
      key.setCascadeDeleteEnabled(false);
      TableBinder.bindFk(this.persistentClass, (PersistentClass)null, ejb3JoinColumns, key, false, mappings);
      join.createPrimaryKey();
      join.createForeignKey();
      this.persistentClass.addJoin(join);
   }

   private void setFKNameIfDefined(Join join) {
      org.hibernate.annotations.Table matchingTable = this.findMatchingComplimentTableAnnotation(join);
      if (matchingTable != null && !BinderHelper.isEmptyAnnotationValue(matchingTable.foreignKey().name())) {
         ((SimpleValue)join.getKey()).setForeignKeyName(matchingTable.foreignKey().name());
      }

   }

   private org.hibernate.annotations.Table findMatchingComplimentTableAnnotation(Join join) {
      String tableName = join.getTable().getQuotedName();
      org.hibernate.annotations.Table table = (org.hibernate.annotations.Table)this.annotatedClass.getAnnotation(org.hibernate.annotations.Table.class);
      org.hibernate.annotations.Table matchingTable = null;
      if (table != null && tableName.equals(table.appliesTo())) {
         matchingTable = table;
      } else {
         Tables tables = (Tables)this.annotatedClass.getAnnotation(Tables.class);
         if (tables != null) {
            for(org.hibernate.annotations.Table current : tables.value()) {
               if (tableName.equals(current.appliesTo())) {
                  matchingTable = current;
                  break;
               }
            }
         }
      }

      return matchingTable;
   }

   public void firstLevelSecondaryTablesBinding(SecondaryTable secTable, SecondaryTables secTables) {
      if (secTables != null) {
         for(SecondaryTable tab : secTables.value()) {
            this.addJoin(tab, (JoinTable)null, (PropertyHolder)null, false);
         }
      } else if (secTable != null) {
         this.addJoin(secTable, (JoinTable)null, (PropertyHolder)null, false);
      }

   }

   public Join addJoin(JoinTable joinTable, PropertyHolder holder, boolean noDelayInPkColumnCreation) {
      return this.addJoin((SecondaryTable)null, joinTable, holder, noDelayInPkColumnCreation);
   }

   private Join addJoin(SecondaryTable secondaryTable, JoinTable joinTable, PropertyHolder propertyHolder, boolean noDelayInPkColumnCreation) {
      Join join = new Join();
      join.setPersistentClass(this.persistentClass);
      String schema;
      String catalog;
      SecondaryTableNameSource secondaryTableNameContext;
      Object joinColumns;
      List<UniqueConstraintHolder> uniqueConstraintHolders;
      if (secondaryTable != null) {
         schema = secondaryTable.schema();
         catalog = secondaryTable.catalog();
         secondaryTableNameContext = new SecondaryTableNameSource(secondaryTable.name());
         joinColumns = secondaryTable.pkJoinColumns();
         uniqueConstraintHolders = TableBinder.buildUniqueConstraintHolders(secondaryTable.uniqueConstraints());
      } else {
         if (joinTable == null) {
            throw new AssertionFailure("Both JoinTable and SecondaryTable are null");
         }

         schema = joinTable.schema();
         catalog = joinTable.catalog();
         secondaryTableNameContext = new SecondaryTableNameSource(joinTable.name());
         joinColumns = joinTable.joinColumns();
         uniqueConstraintHolders = TableBinder.buildUniqueConstraintHolders(joinTable.uniqueConstraints());
      }

      Table table = TableBinder.buildAndFillTable(schema, catalog, secondaryTableNameContext, SEC_TBL_NS_HELPER, false, uniqueConstraintHolders, (String)null, (Table)null, this.mappings, (String)null);
      join.setTable(table);
      LOG.debugf("Adding secondary table to entity %s -> %s", this.persistentClass.getEntityName(), join.getTable().getName());
      org.hibernate.annotations.Table matchingTable = this.findMatchingComplimentTableAnnotation(join);
      if (matchingTable != null) {
         join.setSequentialSelect(FetchMode.JOIN != matchingTable.fetch());
         join.setInverse(matchingTable.inverse());
         join.setOptional(matchingTable.optional());
         if (!BinderHelper.isEmptyAnnotationValue(matchingTable.sqlInsert().sql())) {
            join.setCustomSQLInsert(matchingTable.sqlInsert().sql().trim(), matchingTable.sqlInsert().callable(), ExecuteUpdateResultCheckStyle.fromExternalName(matchingTable.sqlInsert().check().toString().toLowerCase()));
         }

         if (!BinderHelper.isEmptyAnnotationValue(matchingTable.sqlUpdate().sql())) {
            join.setCustomSQLUpdate(matchingTable.sqlUpdate().sql().trim(), matchingTable.sqlUpdate().callable(), ExecuteUpdateResultCheckStyle.fromExternalName(matchingTable.sqlUpdate().check().toString().toLowerCase()));
         }

         if (!BinderHelper.isEmptyAnnotationValue(matchingTable.sqlDelete().sql())) {
            join.setCustomSQLDelete(matchingTable.sqlDelete().sql().trim(), matchingTable.sqlDelete().callable(), ExecuteUpdateResultCheckStyle.fromExternalName(matchingTable.sqlDelete().check().toString().toLowerCase()));
         }
      } else {
         join.setSequentialSelect(false);
         join.setInverse(false);
         join.setOptional(true);
      }

      if (noDelayInPkColumnCreation) {
         this.createPrimaryColumnsToSecondaryTable(joinColumns, propertyHolder, join);
      } else {
         this.secondaryTables.put(table.getQuotedName(), join);
         this.secondaryTableJoins.put(table.getQuotedName(), joinColumns);
      }

      return join;
   }

   public Map getSecondaryTables() {
      return this.secondaryTables;
   }

   public void setCache(Cache cacheAnn) {
      if (cacheAnn != null) {
         this.cacheRegion = BinderHelper.isEmptyAnnotationValue(cacheAnn.region()) ? null : cacheAnn.region();
         this.cacheConcurrentStrategy = getCacheConcurrencyStrategy(cacheAnn.usage());
         if ("all".equalsIgnoreCase(cacheAnn.include())) {
            this.cacheLazyProperty = true;
         } else {
            if (!"non-lazy".equalsIgnoreCase(cacheAnn.include())) {
               throw new AnnotationException("Unknown lazy property annotations: " + cacheAnn.include());
            }

            this.cacheLazyProperty = false;
         }
      } else {
         this.cacheConcurrentStrategy = null;
         this.cacheRegion = null;
         this.cacheLazyProperty = true;
      }

   }

   public void setNaturalIdCache(XClass clazzToProcess, NaturalIdCache naturalIdCacheAnn) {
      if (naturalIdCacheAnn != null) {
         if (BinderHelper.isEmptyAnnotationValue(naturalIdCacheAnn.region())) {
            if (this.cacheRegion != null) {
               this.naturalIdCacheRegion = this.cacheRegion + "##NaturalId";
            } else {
               this.naturalIdCacheRegion = clazzToProcess.getName() + "##NaturalId";
            }
         } else {
            this.naturalIdCacheRegion = naturalIdCacheAnn.region();
         }
      } else {
         this.naturalIdCacheRegion = null;
      }

   }

   public static String getCacheConcurrencyStrategy(CacheConcurrencyStrategy strategy) {
      org.hibernate.cache.spi.access.AccessType accessType = strategy.toAccessType();
      return accessType == null ? null : accessType.getExternalName();
   }

   public void addFilter(Filter filter) {
      this.filters.add(filter);
   }

   public void setInheritanceState(InheritanceState inheritanceState) {
      this.inheritanceState = inheritanceState;
   }

   public boolean isIgnoreIdAnnotations() {
      return this.ignoreIdAnnotations;
   }

   public void setIgnoreIdAnnotations(boolean ignoreIdAnnotations) {
      this.ignoreIdAnnotations = ignoreIdAnnotations;
   }

   public void processComplementaryTableDefinitions(org.hibernate.annotations.Table table) {
      if (table != null) {
         String appliedTable = table.appliesTo();
         Iterator tables = this.persistentClass.getTableClosureIterator();

         Table hibTable;
         for(hibTable = null; tables.hasNext(); hibTable = null) {
            Table pcTable = (Table)tables.next();
            if (pcTable.getQuotedName().equals(appliedTable)) {
               hibTable = pcTable;
               break;
            }
         }

         if (hibTable == null) {
            for(Join join : this.secondaryTables.values()) {
               if (join.getTable().getQuotedName().equals(appliedTable)) {
                  hibTable = join.getTable();
                  break;
               }
            }
         }

         if (hibTable == null) {
            throw new AnnotationException("@org.hibernate.annotations.Table references an unknown table: " + appliedTable);
         } else {
            if (!BinderHelper.isEmptyAnnotationValue(table.comment())) {
               hibTable.setComment(table.comment());
            }

            TableBinder.addIndexes(hibTable, table.indexes(), this.mappings);
         }
      }
   }

   public void processComplementaryTableDefinitions(Tables tables) {
      if (tables != null) {
         for(org.hibernate.annotations.Table table : tables.value()) {
            this.processComplementaryTableDefinitions(table);
         }

      }
   }

   public AccessType getPropertyAccessType() {
      return this.propertyAccessType;
   }

   public void setPropertyAccessType(AccessType propertyAccessor) {
      this.propertyAccessType = this.getExplicitAccessType(this.annotatedClass);
      if (this.propertyAccessType == null) {
         this.propertyAccessType = propertyAccessor;
      }

   }

   public AccessType getPropertyAccessor(XAnnotatedElement element) {
      AccessType accessType = this.getExplicitAccessType(element);
      if (accessType == null) {
         accessType = this.propertyAccessType;
      }

      return accessType;
   }

   public AccessType getExplicitAccessType(XAnnotatedElement element) {
      AccessType accessType = null;
      AccessType hibernateAccessType = null;
      AccessType jpaAccessType = null;
      org.hibernate.annotations.AccessType accessTypeAnnotation = (org.hibernate.annotations.AccessType)element.getAnnotation(org.hibernate.annotations.AccessType.class);
      if (accessTypeAnnotation != null) {
         hibernateAccessType = AccessType.getAccessStrategy(accessTypeAnnotation.value());
      }

      Access access = (Access)element.getAnnotation(Access.class);
      if (access != null) {
         jpaAccessType = AccessType.getAccessStrategy(access.value());
      }

      if (hibernateAccessType != null && jpaAccessType != null && hibernateAccessType != jpaAccessType) {
         throw new MappingException("Found @Access and @AccessType with conflicting values on a property in class " + this.annotatedClass.toString());
      } else {
         if (hibernateAccessType != null) {
            accessType = hibernateAccessType;
         } else if (jpaAccessType != null) {
            accessType = jpaAccessType;
         }

         return accessType;
      }
   }

   private static class EntityTableObjectNameSource implements ObjectNameSource {
      private final String explicitName;
      private final String logicalName;

      private EntityTableObjectNameSource(String explicitName, String entityName) {
         this.explicitName = explicitName;
         this.logicalName = StringHelper.isNotEmpty(explicitName) ? explicitName : StringHelper.unqualify(entityName);
      }

      public String getExplicitName() {
         return this.explicitName;
      }

      public String getLogicalName() {
         return this.logicalName;
      }
   }

   private static class EntityTableNamingStrategyHelper implements ObjectNameNormalizer.NamingStrategyHelper {
      private final String entityName;

      private EntityTableNamingStrategyHelper(String entityName) {
         this.entityName = entityName;
      }

      public String determineImplicitName(NamingStrategy strategy) {
         return strategy.classToTableName(this.entityName);
      }

      public String handleExplicitName(NamingStrategy strategy, String name) {
         return strategy.tableName(name);
      }
   }

   private static class SecondaryTableNameSource implements ObjectNameSource {
      private final String explicitName;

      private SecondaryTableNameSource(String explicitName) {
         this.explicitName = explicitName;
      }

      public String getExplicitName() {
         return this.explicitName;
      }

      public String getLogicalName() {
         return this.explicitName;
      }
   }

   private static class SecondaryTableNamingStrategyHelper implements ObjectNameNormalizer.NamingStrategyHelper {
      private SecondaryTableNamingStrategyHelper() {
      }

      public String determineImplicitName(NamingStrategy strategy) {
         return null;
      }

      public String handleExplicitName(NamingStrategy strategy, String name) {
         return strategy.tableName(name);
      }
   }
}
