package org.hibernate.cfg.annotations;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.MapKey;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import org.hibernate.AnnotationException;
import org.hibernate.MappingException;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CollectionId;
import org.hibernate.annotations.CollectionType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterJoinTable;
import org.hibernate.annotations.FilterJoinTables;
import org.hibernate.annotations.Filters;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Loader;
import org.hibernate.annotations.ManyToAny;
import org.hibernate.annotations.OptimisticLock;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Persister;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLDeleteAll;
import org.hibernate.annotations.SQLInsert;
import org.hibernate.annotations.SQLUpdate;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.hibernate.annotations.Where;
import org.hibernate.annotations.WhereJoinTable;
import org.hibernate.annotations.common.AssertionFailure;
import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.annotations.common.reflection.XProperty;
import org.hibernate.cfg.AccessType;
import org.hibernate.cfg.AnnotatedClassType;
import org.hibernate.cfg.AnnotationBinder;
import org.hibernate.cfg.BinderHelper;
import org.hibernate.cfg.CollectionSecondPass;
import org.hibernate.cfg.Ejb3Column;
import org.hibernate.cfg.Ejb3JoinColumn;
import org.hibernate.cfg.IndexColumn;
import org.hibernate.cfg.InheritanceState;
import org.hibernate.cfg.Mappings;
import org.hibernate.cfg.PropertyData;
import org.hibernate.cfg.PropertyHolder;
import org.hibernate.cfg.PropertyHolderBuilder;
import org.hibernate.cfg.PropertyInferredData;
import org.hibernate.cfg.PropertyPreloadedData;
import org.hibernate.cfg.SecondPass;
import org.hibernate.engine.spi.ExecuteUpdateResultCheckStyle;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.mapping.Any;
import org.hibernate.mapping.Backref;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.DependantValue;
import org.hibernate.mapping.IdGenerator;
import org.hibernate.mapping.Join;
import org.hibernate.mapping.KeyValue;
import org.hibernate.mapping.ManyToOne;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.TypeDef;
import org.jboss.logging.Logger;

public abstract class CollectionBinder {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, CollectionBinder.class.getName());
   protected Collection collection;
   protected String propertyName;
   PropertyHolder propertyHolder;
   int batchSize;
   private String mappedBy;
   private XClass collectionType;
   private XClass targetEntity;
   private Mappings mappings;
   private Ejb3JoinColumn[] inverseJoinColumns;
   private String cascadeStrategy;
   String cacheConcurrencyStrategy;
   String cacheRegionName;
   private boolean oneToMany;
   protected IndexColumn indexColumn;
   private String orderBy;
   protected String hqlOrderBy;
   private boolean isSorted;
   private Class comparator;
   private boolean hasToBeSorted;
   protected boolean cascadeDeleteEnabled;
   protected String mapKeyPropertyName;
   private boolean insertable = true;
   private boolean updatable = true;
   private Ejb3JoinColumn[] fkJoinColumns;
   private boolean isExplicitAssociationTable;
   private Ejb3Column[] elementColumns;
   private boolean isEmbedded;
   private XProperty property;
   private boolean ignoreNotFound;
   private TableBinder tableBinder;
   private Ejb3Column[] mapKeyColumns;
   private Ejb3JoinColumn[] mapKeyManyToManyColumns;
   protected HashMap localGenerators;
   protected Map inheritanceStatePerClass;
   private XClass declaringClass;
   private boolean declaringClassSet;
   private AccessType accessType;
   private boolean hibernateExtensionMapping;
   private String explicitType;
   private Properties explicitTypeParameters = new Properties();
   private Ejb3JoinColumn[] joinColumns;

   protected Mappings getMappings() {
      return this.mappings;
   }

   public boolean isMap() {
      return false;
   }

   public void setIsHibernateExtensionMapping(boolean hibernateExtensionMapping) {
      this.hibernateExtensionMapping = hibernateExtensionMapping;
   }

   protected boolean isHibernateExtensionMapping() {
      return this.hibernateExtensionMapping;
   }

   public void setUpdatable(boolean updatable) {
      this.updatable = updatable;
   }

   public void setInheritanceStatePerClass(Map inheritanceStatePerClass) {
      this.inheritanceStatePerClass = inheritanceStatePerClass;
   }

   public void setInsertable(boolean insertable) {
      this.insertable = insertable;
   }

   public void setCascadeStrategy(String cascadeStrategy) {
      this.cascadeStrategy = cascadeStrategy;
   }

   public void setAccessType(AccessType accessType) {
      this.accessType = accessType;
   }

   public void setInverseJoinColumns(Ejb3JoinColumn[] inverseJoinColumns) {
      this.inverseJoinColumns = inverseJoinColumns;
   }

   public void setJoinColumns(Ejb3JoinColumn[] joinColumns) {
      this.joinColumns = joinColumns;
   }

   public void setPropertyHolder(PropertyHolder propertyHolder) {
      this.propertyHolder = propertyHolder;
   }

   public void setBatchSize(BatchSize batchSize) {
      this.batchSize = batchSize == null ? -1 : batchSize.size();
   }

   public void setEjb3OrderBy(OrderBy orderByAnn) {
      if (orderByAnn != null) {
         this.hqlOrderBy = orderByAnn.value();
      }

   }

   public void setSqlOrderBy(org.hibernate.annotations.OrderBy orderByAnn) {
      if (orderByAnn != null && !BinderHelper.isEmptyAnnotationValue(orderByAnn.clause())) {
         this.orderBy = orderByAnn.clause();
      }

   }

   public void setSort(Sort sortAnn) {
      if (sortAnn != null) {
         this.isSorted = !SortType.UNSORTED.equals(sortAnn.type());
         if (this.isSorted && SortType.COMPARATOR.equals(sortAnn.type())) {
            this.comparator = sortAnn.comparator();
         }
      }

   }

   public static CollectionBinder getCollectionBinder(String entityName, XProperty property, boolean isIndexed, boolean isHibernateExtensionMapping, Mappings mappings) {
      CollectionBinder result;
      if (property.isArray()) {
         if (property.getElementClass().isPrimitive()) {
            result = new PrimitiveArrayBinder();
         } else {
            result = new ArrayBinder();
         }
      } else {
         if (!property.isCollection()) {
            throw new AnnotationException("Illegal attempt to map a non collection as a @OneToMany, @ManyToMany or @CollectionOfElements: " + StringHelper.qualify(entityName, property.getName()));
         }

         Class returnedClass = property.getCollectionClass();
         if (Set.class.equals(returnedClass)) {
            if (property.isAnnotationPresent(CollectionId.class)) {
               throw new AnnotationException("Set do not support @CollectionId: " + StringHelper.qualify(entityName, property.getName()));
            }

            result = new SetBinder();
         } else if (SortedSet.class.equals(returnedClass)) {
            if (property.isAnnotationPresent(CollectionId.class)) {
               throw new AnnotationException("Set do not support @CollectionId: " + StringHelper.qualify(entityName, property.getName()));
            }

            result = new SetBinder(true);
         } else if (Map.class.equals(returnedClass)) {
            if (property.isAnnotationPresent(CollectionId.class)) {
               throw new AnnotationException("Map do not support @CollectionId: " + StringHelper.qualify(entityName, property.getName()));
            }

            result = new MapBinder();
         } else if (SortedMap.class.equals(returnedClass)) {
            if (property.isAnnotationPresent(CollectionId.class)) {
               throw new AnnotationException("Map do not support @CollectionId: " + StringHelper.qualify(entityName, property.getName()));
            }

            result = new MapBinder(true);
         } else if (java.util.Collection.class.equals(returnedClass)) {
            if (property.isAnnotationPresent(CollectionId.class)) {
               result = new IdBagBinder();
            } else {
               result = new BagBinder();
            }
         } else {
            if (!List.class.equals(returnedClass)) {
               throw new AnnotationException(returnedClass.getName() + " collection not yet supported: " + StringHelper.qualify(entityName, property.getName()));
            }

            if (isIndexed) {
               if (property.isAnnotationPresent(CollectionId.class)) {
                  throw new AnnotationException("List do not support @CollectionId and @OrderColumn (or @IndexColumn) at the same time: " + StringHelper.qualify(entityName, property.getName()));
               }

               result = new ListBinder();
            } else if (property.isAnnotationPresent(CollectionId.class)) {
               result = new IdBagBinder();
            } else {
               result = new BagBinder();
            }
         }
      }

      result.setIsHibernateExtensionMapping(isHibernateExtensionMapping);
      CollectionType typeAnnotation = (CollectionType)property.getAnnotation(CollectionType.class);
      if (typeAnnotation != null) {
         String typeName = typeAnnotation.type();
         TypeDef typeDef = mappings.getTypeDef(typeName);
         if (typeDef != null) {
            result.explicitType = typeDef.getTypeClass();
            result.explicitTypeParameters.putAll(typeDef.getParameters());
         } else {
            result.explicitType = typeName;

            for(Parameter param : typeAnnotation.parameters()) {
               result.explicitTypeParameters.setProperty(param.name(), param.value());
            }
         }
      }

      return result;
   }

   protected CollectionBinder() {
   }

   protected CollectionBinder(boolean sorted) {
      this.hasToBeSorted = sorted;
   }

   public void setMappedBy(String mappedBy) {
      this.mappedBy = mappedBy;
   }

   public void setTableBinder(TableBinder tableBinder) {
      this.tableBinder = tableBinder;
   }

   public void setCollectionType(XClass collectionType) {
      this.collectionType = collectionType;
   }

   public void setTargetEntity(XClass targetEntity) {
      this.targetEntity = targetEntity;
   }

   public void setMappings(Mappings mappings) {
      this.mappings = mappings;
   }

   protected abstract Collection createCollection(PersistentClass var1);

   public Collection getCollection() {
      return this.collection;
   }

   public void setPropertyName(String propertyName) {
      this.propertyName = propertyName;
   }

   public void setDeclaringClass(XClass declaringClass) {
      this.declaringClass = declaringClass;
      this.declaringClassSet = true;
   }

   public void bind() {
      this.collection = this.createCollection(this.propertyHolder.getPersistentClass());
      String role = StringHelper.qualify(this.propertyHolder.getPath(), this.propertyName);
      LOG.debugf("Collection role: %s", role);
      this.collection.setRole(role);
      this.collection.setNodeName(this.propertyName);
      if (this.property.isAnnotationPresent(MapKeyColumn.class) && this.mapKeyPropertyName != null) {
         throw new AnnotationException("Cannot mix @javax.persistence.MapKey and @MapKeyColumn or @org.hibernate.annotations.MapKey on the same collection: " + StringHelper.qualify(this.propertyHolder.getPath(), this.propertyName));
      } else {
         if (this.explicitType != null) {
            TypeDef typeDef = this.mappings.getTypeDef(this.explicitType);
            if (typeDef == null) {
               this.collection.setTypeName(this.explicitType);
               this.collection.setTypeParameters(this.explicitTypeParameters);
            } else {
               this.collection.setTypeName(typeDef.getTypeClass());
               this.collection.setTypeParameters(typeDef.getParameters());
            }
         }

         this.defineFetchingStrategy();
         this.collection.setBatchSize(this.batchSize);
         if (this.orderBy != null && this.hqlOrderBy != null) {
            throw new AnnotationException("Cannot use sql order by clause in conjunction of EJB3 order by clause: " + this.safeCollectionRole());
         } else {
            this.collection.setMutable(!this.property.isAnnotationPresent(Immutable.class));
            boolean isMappedBy = !BinderHelper.isEmptyAnnotationValue(this.mappedBy);
            OptimisticLock lockAnn = (OptimisticLock)this.property.getAnnotation(OptimisticLock.class);
            boolean includeInOptimisticLockChecks = lockAnn != null ? !lockAnn.excluded() : !isMappedBy;
            this.collection.setOptimisticLocked(includeInOptimisticLockChecks);
            Persister persisterAnn = (Persister)this.property.getAnnotation(Persister.class);
            if (persisterAnn != null) {
               this.collection.setCollectionPersisterClass(persisterAnn.impl());
            }

            if (this.orderBy != null) {
               this.collection.setOrderBy(this.orderBy);
            }

            if (this.isSorted) {
               this.collection.setSorted(true);
               if (this.comparator != null) {
                  try {
                     this.collection.setComparator((Comparator)this.comparator.newInstance());
                  } catch (ClassCastException var15) {
                     throw new AnnotationException("Comparator not implementing java.util.Comparator class: " + this.comparator.getName() + "(" + this.safeCollectionRole() + ")");
                  } catch (Exception var16) {
                     throw new AnnotationException("Could not instantiate comparator class: " + this.comparator.getName() + "(" + this.safeCollectionRole() + ")");
                  }
               }
            } else if (this.hasToBeSorted) {
               throw new AnnotationException("A sorted collection has to define @Sort: " + this.safeCollectionRole());
            }

            if (StringHelper.isNotEmpty(this.cacheConcurrencyStrategy)) {
               this.collection.setCacheConcurrencyStrategy(this.cacheConcurrencyStrategy);
               this.collection.setCacheRegionName(this.cacheRegionName);
            }

            SQLInsert sqlInsert = (SQLInsert)this.property.getAnnotation(SQLInsert.class);
            SQLUpdate sqlUpdate = (SQLUpdate)this.property.getAnnotation(SQLUpdate.class);
            SQLDelete sqlDelete = (SQLDelete)this.property.getAnnotation(SQLDelete.class);
            SQLDeleteAll sqlDeleteAll = (SQLDeleteAll)this.property.getAnnotation(SQLDeleteAll.class);
            Loader loader = (Loader)this.property.getAnnotation(Loader.class);
            if (sqlInsert != null) {
               this.collection.setCustomSQLInsert(sqlInsert.sql().trim(), sqlInsert.callable(), ExecuteUpdateResultCheckStyle.fromExternalName(sqlInsert.check().toString().toLowerCase()));
            }

            if (sqlUpdate != null) {
               this.collection.setCustomSQLUpdate(sqlUpdate.sql(), sqlUpdate.callable(), ExecuteUpdateResultCheckStyle.fromExternalName(sqlUpdate.check().toString().toLowerCase()));
            }

            if (sqlDelete != null) {
               this.collection.setCustomSQLDelete(sqlDelete.sql(), sqlDelete.callable(), ExecuteUpdateResultCheckStyle.fromExternalName(sqlDelete.check().toString().toLowerCase()));
            }

            if (sqlDeleteAll != null) {
               this.collection.setCustomSQLDeleteAll(sqlDeleteAll.sql(), sqlDeleteAll.callable(), ExecuteUpdateResultCheckStyle.fromExternalName(sqlDeleteAll.check().toString().toLowerCase()));
            }

            if (loader != null) {
               this.collection.setLoaderName(loader.namedQuery());
            }

            if (!isMappedBy || !this.property.isAnnotationPresent(JoinColumn.class) && !this.property.isAnnotationPresent(JoinColumns.class) && this.propertyHolder.getJoinTable(this.property) == null) {
               this.collection.setInverse(isMappedBy);
               if (!this.oneToMany && isMappedBy) {
                  this.mappings.addMappedBy(this.getCollectionType().getName(), this.mappedBy, this.propertyName);
               }

               XClass collectionType = this.getCollectionType();
               if (this.inheritanceStatePerClass == null) {
                  throw new AssertionFailure("inheritanceStatePerClass not set");
               } else {
                  SecondPass sp = this.getSecondPass(this.fkJoinColumns, this.joinColumns, this.inverseJoinColumns, this.elementColumns, this.mapKeyColumns, this.mapKeyManyToManyColumns, this.isEmbedded, this.property, collectionType, this.ignoreNotFound, this.oneToMany, this.tableBinder, this.mappings);
                  if (!collectionType.isAnnotationPresent(Embeddable.class) && !this.property.isAnnotationPresent(ElementCollection.class)) {
                     this.mappings.addSecondPass(sp, !isMappedBy);
                  } else {
                     this.mappings.addSecondPass(sp, !isMappedBy);
                  }

                  this.mappings.addCollection(this.collection);
                  PropertyBinder binder = new PropertyBinder();
                  binder.setName(this.propertyName);
                  binder.setValue(this.collection);
                  binder.setCascade(this.cascadeStrategy);
                  if (this.cascadeStrategy != null && this.cascadeStrategy.indexOf("delete-orphan") >= 0) {
                     this.collection.setOrphanDelete(true);
                  }

                  binder.setAccessType(this.accessType);
                  binder.setProperty(this.property);
                  binder.setInsertable(this.insertable);
                  binder.setUpdatable(this.updatable);
                  Property prop = binder.makeProperty();
                  if (!this.declaringClassSet) {
                     throw new AssertionFailure("DeclaringClass is not set in CollectionBinder while binding");
                  } else {
                     this.propertyHolder.addProperty(prop, this.declaringClass);
                  }
               }
            } else {
               String message = "Associations marked as mappedBy must not define database mappings like @JoinTable or @JoinColumn: ";
               message = message + StringHelper.qualify(this.propertyHolder.getPath(), this.propertyName);
               throw new AnnotationException(message);
            }
         }
      }
   }

   private void defineFetchingStrategy() {
      LazyCollection lazy = (LazyCollection)this.property.getAnnotation(LazyCollection.class);
      Fetch fetch = (Fetch)this.property.getAnnotation(Fetch.class);
      OneToMany oneToMany = (OneToMany)this.property.getAnnotation(OneToMany.class);
      ManyToMany manyToMany = (ManyToMany)this.property.getAnnotation(ManyToMany.class);
      ElementCollection elementCollection = (ElementCollection)this.property.getAnnotation(ElementCollection.class);
      ManyToAny manyToAny = (ManyToAny)this.property.getAnnotation(ManyToAny.class);
      FetchType fetchType;
      if (oneToMany != null) {
         fetchType = oneToMany.fetch();
      } else if (manyToMany != null) {
         fetchType = manyToMany.fetch();
      } else if (elementCollection != null) {
         fetchType = elementCollection.fetch();
      } else {
         if (manyToAny == null) {
            throw new AssertionFailure("Define fetch strategy on a property not annotated with @ManyToOne nor @OneToMany nor @CollectionOfElements");
         }

         fetchType = FetchType.LAZY;
      }

      if (lazy != null) {
         this.collection.setLazy(lazy.value() != LazyCollectionOption.FALSE);
         this.collection.setExtraLazy(lazy.value() == LazyCollectionOption.EXTRA);
      } else {
         this.collection.setLazy(fetchType == FetchType.LAZY);
         this.collection.setExtraLazy(false);
      }

      if (fetch != null) {
         if (fetch.value() == FetchMode.JOIN) {
            this.collection.setFetchMode(org.hibernate.FetchMode.JOIN);
            this.collection.setLazy(false);
         } else if (fetch.value() == FetchMode.SELECT) {
            this.collection.setFetchMode(org.hibernate.FetchMode.SELECT);
         } else {
            if (fetch.value() != FetchMode.SUBSELECT) {
               throw new AssertionFailure("Unknown FetchMode: " + fetch.value());
            }

            this.collection.setFetchMode(org.hibernate.FetchMode.SELECT);
            this.collection.setSubselectLoadable(true);
            this.collection.getOwner().setSubselectLoadableCollections(true);
         }
      } else {
         this.collection.setFetchMode(AnnotationBinder.getFetchMode(fetchType));
      }

   }

   private XClass getCollectionType() {
      if (AnnotationBinder.isDefault(this.targetEntity, this.mappings)) {
         if (this.collectionType != null) {
            return this.collectionType;
         } else {
            String errorMsg = "Collection has neither generic type or OneToMany.targetEntity() defined: " + this.safeCollectionRole();
            throw new AnnotationException(errorMsg);
         }
      } else {
         return this.targetEntity;
      }
   }

   public SecondPass getSecondPass(final Ejb3JoinColumn[] fkJoinColumns, final Ejb3JoinColumn[] keyColumns, final Ejb3JoinColumn[] inverseColumns, final Ejb3Column[] elementColumns, Ejb3Column[] mapKeyColumns, Ejb3JoinColumn[] mapKeyManyToManyColumns, final boolean isEmbedded, final XProperty property, final XClass collType, final boolean ignoreNotFound, final boolean unique, final TableBinder assocTableBinder, final Mappings mappings) {
      return new CollectionSecondPass(mappings, this.collection) {
         public void secondPass(Map persistentClasses, Map inheritedMetas) throws MappingException {
            CollectionBinder.this.bindStarToManySecondPass(persistentClasses, collType, fkJoinColumns, keyColumns, inverseColumns, elementColumns, isEmbedded, property, unique, assocTableBinder, ignoreNotFound, mappings);
         }
      };
   }

   protected boolean bindStarToManySecondPass(Map persistentClasses, XClass collType, Ejb3JoinColumn[] fkJoinColumns, Ejb3JoinColumn[] keyColumns, Ejb3JoinColumn[] inverseColumns, Ejb3Column[] elementColumns, boolean isEmbedded, XProperty property, boolean unique, TableBinder associationTableBinder, boolean ignoreNotFound, Mappings mappings) {
      PersistentClass persistentClass = (PersistentClass)persistentClasses.get(collType.getName());
      boolean reversePropertyInJoin = false;
      if (persistentClass != null && StringHelper.isNotEmpty(this.mappedBy)) {
         try {
            reversePropertyInJoin = 0 != persistentClass.getJoinNumber(persistentClass.getRecursiveProperty(this.mappedBy));
         } catch (MappingException var17) {
            StringBuilder error = new StringBuilder(80);
            error.append("mappedBy reference an unknown target entity property: ").append(collType).append(".").append(this.mappedBy).append(" in ").append(this.collection.getOwnerEntityName()).append(".").append(property.getName());
            throw new AnnotationException(error.toString());
         }
      }

      if (persistentClass == null || reversePropertyInJoin || !this.oneToMany || this.isExplicitAssociationTable || (!this.joinColumns[0].isImplicit() || BinderHelper.isEmptyAnnotationValue(this.mappedBy)) && fkJoinColumns[0].isImplicit()) {
         this.bindManyToManySecondPass(this.collection, persistentClasses, keyColumns, inverseColumns, elementColumns, isEmbedded, collType, ignoreNotFound, unique, this.cascadeDeleteEnabled, associationTableBinder, property, this.propertyHolder, this.hqlOrderBy, mappings);
         return false;
      } else {
         this.bindOneToManySecondPass(this.getCollection(), persistentClasses, fkJoinColumns, collType, this.cascadeDeleteEnabled, ignoreNotFound, this.hqlOrderBy, mappings, this.inheritanceStatePerClass);
         return true;
      }
   }

   protected void bindOneToManySecondPass(Collection collection, Map persistentClasses, Ejb3JoinColumn[] fkJoinColumns, XClass collectionType, boolean cascadeDeleteEnabled, boolean ignoreNotFound, String hqlOrderBy, Mappings mappings, Map inheritanceStatePerClass) {
      if (LOG.isDebugEnabled()) {
         LOG.debugf("Binding a OneToMany: %s.%s through a foreign key", this.propertyHolder.getEntityName(), this.propertyName);
      }

      org.hibernate.mapping.OneToMany oneToMany = new org.hibernate.mapping.OneToMany(mappings, collection.getOwner());
      collection.setElement(oneToMany);
      oneToMany.setReferencedEntityName(collectionType.getName());
      oneToMany.setIgnoreNotFound(ignoreNotFound);
      String assocClass = oneToMany.getReferencedEntityName();
      PersistentClass associatedClass = (PersistentClass)persistentClasses.get(assocClass);
      String orderBy = buildOrderByClauseFromHql(hqlOrderBy, associatedClass, collection.getRole());
      if (orderBy != null) {
         collection.setOrderBy(orderBy);
      }

      if (mappings == null) {
         throw new AssertionFailure("CollectionSecondPass for oneToMany should not be called with null mappings");
      } else {
         Map<String, Join> joins = mappings.getJoins(assocClass);
         if (associatedClass == null) {
            throw new MappingException("Association references unmapped class: " + assocClass);
         } else {
            oneToMany.setAssociatedClass(associatedClass);

            for(Ejb3JoinColumn column : fkJoinColumns) {
               column.setPersistentClass(associatedClass, joins, inheritanceStatePerClass);
               column.setJoins(joins);
               collection.setCollectionTable(column.getTable());
            }

            if (LOG.isDebugEnabled()) {
               LOG.debugf("Mapping collection: %s -> %s", collection.getRole(), collection.getCollectionTable().getName());
            }

            this.bindFilters(false);
            bindCollectionSecondPass(collection, (PersistentClass)null, fkJoinColumns, cascadeDeleteEnabled, this.property, mappings);
            if (!collection.isInverse() && !collection.getKey().isNullable()) {
               String entityName = oneToMany.getReferencedEntityName();
               PersistentClass referenced = mappings.getClass(entityName);
               Backref prop = new Backref();
               prop.setName('_' + fkJoinColumns[0].getPropertyName() + '_' + fkJoinColumns[0].getLogicalColumnName() + "Backref");
               prop.setUpdateable(false);
               prop.setSelectable(false);
               prop.setCollectionRole(collection.getRole());
               prop.setEntityName(collection.getOwner().getEntityName());
               prop.setValue(collection.getKey());
               referenced.addProperty(prop);
            }

         }
      }
   }

   private void bindFilters(boolean hasAssociationTable) {
      Filter simpleFilter = (Filter)this.property.getAnnotation(Filter.class);
      if (simpleFilter != null) {
         if (hasAssociationTable) {
            this.collection.addManyToManyFilter(simpleFilter.name(), this.getCondition(simpleFilter), simpleFilter.deduceAliasInjectionPoints(), BinderHelper.toAliasTableMap(simpleFilter.aliases()), BinderHelper.toAliasEntityMap(simpleFilter.aliases()));
         } else {
            this.collection.addFilter(simpleFilter.name(), this.getCondition(simpleFilter), simpleFilter.deduceAliasInjectionPoints(), BinderHelper.toAliasTableMap(simpleFilter.aliases()), BinderHelper.toAliasEntityMap(simpleFilter.aliases()));
         }
      }

      Filters filters = (Filters)this.property.getAnnotation(Filters.class);
      if (filters != null) {
         for(Filter filter : filters.value()) {
            if (hasAssociationTable) {
               this.collection.addManyToManyFilter(filter.name(), this.getCondition(filter), filter.deduceAliasInjectionPoints(), BinderHelper.toAliasTableMap(filter.aliases()), BinderHelper.toAliasEntityMap(filter.aliases()));
            } else {
               this.collection.addFilter(filter.name(), this.getCondition(filter), filter.deduceAliasInjectionPoints(), BinderHelper.toAliasTableMap(filter.aliases()), BinderHelper.toAliasEntityMap(filter.aliases()));
            }
         }
      }

      FilterJoinTable simpleFilterJoinTable = (FilterJoinTable)this.property.getAnnotation(FilterJoinTable.class);
      if (simpleFilterJoinTable != null) {
         if (!hasAssociationTable) {
            throw new AnnotationException("Illegal use of @FilterJoinTable on an association without join table:" + StringHelper.qualify(this.propertyHolder.getPath(), this.propertyName));
         }

         this.collection.addFilter(simpleFilterJoinTable.name(), simpleFilterJoinTable.condition(), simpleFilterJoinTable.deduceAliasInjectionPoints(), BinderHelper.toAliasTableMap(simpleFilterJoinTable.aliases()), BinderHelper.toAliasEntityMap(simpleFilterJoinTable.aliases()));
      }

      FilterJoinTables filterJoinTables = (FilterJoinTables)this.property.getAnnotation(FilterJoinTables.class);
      if (filterJoinTables != null) {
         for(FilterJoinTable filter : filterJoinTables.value()) {
            if (!hasAssociationTable) {
               throw new AnnotationException("Illegal use of @FilterJoinTable on an association without join table:" + StringHelper.qualify(this.propertyHolder.getPath(), this.propertyName));
            }

            this.collection.addFilter(filter.name(), filter.condition(), filter.deduceAliasInjectionPoints(), BinderHelper.toAliasTableMap(filter.aliases()), BinderHelper.toAliasEntityMap(filter.aliases()));
         }
      }

      Where where = (Where)this.property.getAnnotation(Where.class);
      String whereClause = where == null ? null : where.clause();
      if (StringHelper.isNotEmpty(whereClause)) {
         if (hasAssociationTable) {
            this.collection.setManyToManyWhere(whereClause);
         } else {
            this.collection.setWhere(whereClause);
         }
      }

      WhereJoinTable whereJoinTable = (WhereJoinTable)this.property.getAnnotation(WhereJoinTable.class);
      String whereJoinTableClause = whereJoinTable == null ? null : whereJoinTable.clause();
      if (StringHelper.isNotEmpty(whereJoinTableClause)) {
         if (!hasAssociationTable) {
            throw new AnnotationException("Illegal use of @WhereJoinTable on an association without join table:" + StringHelper.qualify(this.propertyHolder.getPath(), this.propertyName));
         }

         this.collection.setWhere(whereJoinTableClause);
      }

   }

   private String getCondition(FilterJoinTable filter) {
      String name = filter.name();
      String cond = filter.condition();
      return this.getCondition(cond, name);
   }

   private String getCondition(Filter filter) {
      String name = filter.name();
      String cond = filter.condition();
      return this.getCondition(cond, name);
   }

   private String getCondition(String cond, String name) {
      if (BinderHelper.isEmptyAnnotationValue(cond)) {
         cond = this.mappings.getFilterDefinition(name).getDefaultFilterCondition();
         if (StringHelper.isEmpty(cond)) {
            throw new AnnotationException("no filter condition found for filter " + name + " in " + StringHelper.qualify(this.propertyHolder.getPath(), this.propertyName));
         }
      }

      return cond;
   }

   public void setCache(Cache cacheAnn) {
      if (cacheAnn != null) {
         this.cacheRegionName = BinderHelper.isEmptyAnnotationValue(cacheAnn.region()) ? null : cacheAnn.region();
         this.cacheConcurrencyStrategy = EntityBinder.getCacheConcurrencyStrategy(cacheAnn.usage());
      } else {
         this.cacheConcurrencyStrategy = null;
         this.cacheRegionName = null;
      }

   }

   public void setOneToMany(boolean oneToMany) {
      this.oneToMany = oneToMany;
   }

   public void setIndexColumn(IndexColumn indexColumn) {
      this.indexColumn = indexColumn;
   }

   public void setMapKey(MapKey key) {
      if (key != null) {
         this.mapKeyPropertyName = key.name();
      }

   }

   private static String buildOrderByClauseFromHql(String orderByFragment, PersistentClass associatedClass, String role) {
      if (orderByFragment != null) {
         if (orderByFragment.length() == 0) {
            return "id asc";
         }

         if ("desc".equals(orderByFragment)) {
            return "id desc";
         }
      }

      return orderByFragment;
   }

   private static String adjustUserSuppliedValueCollectionOrderingFragment(String orderByFragment) {
      if (orderByFragment != null) {
         if (orderByFragment.length() == 0) {
            return "$element$ asc";
         }

         if ("desc".equals(orderByFragment)) {
            return "$element$ desc";
         }
      }

      return orderByFragment;
   }

   private static SimpleValue buildCollectionKey(Collection collValue, Ejb3JoinColumn[] joinColumns, boolean cascadeDeleteEnabled, XProperty property, Mappings mappings) {
      if (joinColumns.length > 0 && StringHelper.isNotEmpty(joinColumns[0].getMappedBy())) {
         String entityName = joinColumns[0].getManyToManyOwnerSideEntityName() != null ? "inverse__" + joinColumns[0].getManyToManyOwnerSideEntityName() : joinColumns[0].getPropertyHolder().getEntityName();
         String propRef = mappings.getPropertyReferencedAssociation(entityName, joinColumns[0].getMappedBy());
         if (propRef != null) {
            collValue.setReferencedPropertyName(propRef);
            mappings.addPropertyReference(collValue.getOwnerEntityName(), propRef);
         }
      }

      String propRef = collValue.getReferencedPropertyName();
      KeyValue keyVal;
      if (propRef == null) {
         keyVal = collValue.getOwner().getIdentifier();
      } else {
         keyVal = (KeyValue)collValue.getOwner().getReferencedProperty(propRef).getValue();
      }

      DependantValue key = new DependantValue(mappings, collValue.getCollectionTable(), keyVal);
      key.setTypeName((String)null);
      Ejb3Column.checkPropertyConsistency(joinColumns, collValue.getOwnerEntityName());
      key.setNullable(joinColumns.length == 0 || joinColumns[0].isNullable());
      key.setUpdateable(joinColumns.length == 0 || joinColumns[0].isUpdatable());
      key.setCascadeDeleteEnabled(cascadeDeleteEnabled);
      collValue.setKey(key);
      ForeignKey fk = property != null ? (ForeignKey)property.getAnnotation(ForeignKey.class) : null;
      String fkName = fk != null ? fk.name() : "";
      if (!BinderHelper.isEmptyAnnotationValue(fkName)) {
         key.setForeignKeyName(fkName);
      }

      return key;
   }

   protected void bindManyToManySecondPass(Collection collValue, Map persistentClasses, Ejb3JoinColumn[] joinColumns, Ejb3JoinColumn[] inverseJoinColumns, Ejb3Column[] elementColumns, boolean isEmbedded, XClass collType, boolean ignoreNotFound, boolean unique, boolean cascadeDeleteEnabled, TableBinder associationTableBinder, XProperty property, PropertyHolder parentPropertyHolder, String hqlOrderBy, Mappings mappings) throws MappingException {
      PersistentClass collectionEntity = (PersistentClass)persistentClasses.get(collType.getName());
      boolean isCollectionOfEntities = collectionEntity != null;
      ManyToAny anyAnn = (ManyToAny)property.getAnnotation(ManyToAny.class);
      if (LOG.isDebugEnabled()) {
         String path = collValue.getOwnerEntityName() + "." + joinColumns[0].getPropertyName();
         if (isCollectionOfEntities && unique) {
            LOG.debugf("Binding a OneToMany: %s through an association table", path);
         } else if (isCollectionOfEntities) {
            LOG.debugf("Binding as ManyToMany: %s", path);
         } else if (anyAnn != null) {
            LOG.debugf("Binding a ManyToAny: %s", path);
         } else {
            LOG.debugf("Binding a collection of element: %s", path);
         }
      }

      if (!isCollectionOfEntities) {
         if (property.isAnnotationPresent(ManyToMany.class) || property.isAnnotationPresent(OneToMany.class)) {
            String path = collValue.getOwnerEntityName() + "." + joinColumns[0].getPropertyName();
            throw new AnnotationException("Use of @OneToMany or @ManyToMany targeting an unmapped class: " + path + "[" + collType + "]");
         }

         if (anyAnn != null) {
            if (parentPropertyHolder.getJoinTable(property) == null) {
               String path = collValue.getOwnerEntityName() + "." + joinColumns[0].getPropertyName();
               throw new AnnotationException("@JoinTable is mandatory when @ManyToAny is used: " + path);
            }
         } else {
            JoinTable joinTableAnn = parentPropertyHolder.getJoinTable(property);
            if (joinTableAnn != null && joinTableAnn.inverseJoinColumns().length > 0) {
               String path = collValue.getOwnerEntityName() + "." + joinColumns[0].getPropertyName();
               throw new AnnotationException("Use of @JoinTable.inverseJoinColumns targeting an unmapped class: " + path + "[" + collType + "]");
            }
         }
      }

      boolean mappedBy = !BinderHelper.isEmptyAnnotationValue(joinColumns[0].getMappedBy());
      if (mappedBy) {
         if (!isCollectionOfEntities) {
            StringBuilder error = (new StringBuilder(80)).append("Collection of elements must not have mappedBy or association reference an unmapped entity: ").append(collValue.getOwnerEntityName()).append(".").append(joinColumns[0].getPropertyName());
            throw new AnnotationException(error.toString());
         }

         Property otherSideProperty;
         try {
            otherSideProperty = collectionEntity.getRecursiveProperty(joinColumns[0].getMappedBy());
         } catch (MappingException var31) {
            StringBuilder error = new StringBuilder(80);
            error.append("mappedBy reference an unknown target entity property: ").append(collType).append(".").append(joinColumns[0].getMappedBy()).append(" in ").append(collValue.getOwnerEntityName()).append(".").append(joinColumns[0].getPropertyName());
            throw new AnnotationException(error.toString());
         }

         Table table;
         if (otherSideProperty.getValue() instanceof Collection) {
            table = ((Collection)otherSideProperty.getValue()).getCollectionTable();
         } else {
            table = otherSideProperty.getValue().getTable();
         }

         collValue.setCollectionTable(table);
         String entityName = collectionEntity.getEntityName();

         for(Ejb3JoinColumn column : joinColumns) {
            column.setManyToManyOwnerSideEntityName(entityName);
         }
      } else {
         for(Ejb3JoinColumn column : joinColumns) {
            String mappedByProperty = mappings.getFromMappedBy(collValue.getOwnerEntityName(), column.getPropertyName());
            Table ownerTable = collValue.getOwner().getTable();
            column.setMappedBy(collValue.getOwner().getEntityName(), mappings.getLogicalTableName(ownerTable), mappedByProperty);
         }

         if (StringHelper.isEmpty(associationTableBinder.getName())) {
            associationTableBinder.setDefaultName(collValue.getOwner().getEntityName(), mappings.getLogicalTableName(collValue.getOwner().getTable()), collectionEntity != null ? collectionEntity.getEntityName() : null, collectionEntity != null ? mappings.getLogicalTableName(collectionEntity.getTable()) : null, joinColumns[0].getPropertyName());
         }

         associationTableBinder.setJPA2ElementCollection(!isCollectionOfEntities && property.isAnnotationPresent(ElementCollection.class));
         collValue.setCollectionTable(associationTableBinder.bind());
      }

      this.bindFilters(isCollectionOfEntities);
      bindCollectionSecondPass(collValue, collectionEntity, joinColumns, cascadeDeleteEnabled, property, mappings);
      ManyToOne element = null;
      if (isCollectionOfEntities) {
         element = new ManyToOne(mappings, collValue.getCollectionTable());
         collValue.setElement(element);
         element.setReferencedEntityName(collType.getName());
         element.setFetchMode(org.hibernate.FetchMode.JOIN);
         element.setLazy(false);
         element.setIgnoreNotFound(ignoreNotFound);
         if (hqlOrderBy != null) {
            collValue.setManyToManyOrdering(buildOrderByClauseFromHql(hqlOrderBy, collectionEntity, collValue.getRole()));
         }

         ForeignKey fk = property != null ? (ForeignKey)property.getAnnotation(ForeignKey.class) : null;
         String fkName = fk != null ? fk.inverseName() : "";
         if (!BinderHelper.isEmptyAnnotationValue(fkName)) {
            element.setForeignKeyName(fkName);
         }
      } else if (anyAnn != null) {
         PropertyData inferredData = new PropertyInferredData((XClass)null, property, "unsupported", mappings.getReflectionManager());

         for(Ejb3Column column : inverseJoinColumns) {
            column.setTable(collValue.getCollectionTable());
         }

         Any any = BinderHelper.buildAnyValue(anyAnn.metaDef(), inverseJoinColumns, anyAnn.metaColumn(), inferredData, cascadeDeleteEnabled, Nullability.NO_CONSTRAINT, this.propertyHolder, new EntityBinder(), true, mappings);
         collValue.setElement(any);
      } else {
         PropertyHolder holder = null;
         XClass elementClass;
         AnnotatedClassType classType;
         if (BinderHelper.PRIMITIVE_NAMES.contains(collType.getName())) {
            classType = AnnotatedClassType.NONE;
            elementClass = null;
         } else {
            elementClass = collType;
            classType = mappings.getClassType(collType);
            holder = PropertyHolderBuilder.buildPropertyHolder(collValue, collValue.getRole(), collType, property, parentPropertyHolder, mappings);
            boolean attributeOverride = property.isAnnotationPresent(AttributeOverride.class) || property.isAnnotationPresent(AttributeOverrides.class);
            if (isEmbedded || attributeOverride) {
               classType = AnnotatedClassType.EMBEDDABLE;
            }
         }

         if (AnnotatedClassType.EMBEDDABLE.equals(classType)) {
            EntityBinder entityBinder = new EntityBinder();
            PersistentClass owner = collValue.getOwner();
            boolean isPropertyAnnotated;
            if (owner.getIdentifierProperty() != null) {
               isPropertyAnnotated = owner.getIdentifierProperty().getPropertyAccessorName().equals("property");
            } else {
               if (owner.getIdentifierMapper() == null || owner.getIdentifierMapper().getPropertySpan() <= 0) {
                  throw new AssertionFailure("Unable to guess collection property accessor name");
               }

               Property prop = (Property)owner.getIdentifierMapper().getPropertyIterator().next();
               isPropertyAnnotated = prop.getPropertyAccessorName().equals("property");
            }

            PropertyData inferredData;
            if (this.isMap()) {
               if (this.isHibernateExtensionMapping()) {
                  inferredData = new PropertyPreloadedData(AccessType.PROPERTY, "element", elementClass);
               } else {
                  inferredData = new PropertyPreloadedData(AccessType.PROPERTY, "value", elementClass);
               }
            } else if (this.isHibernateExtensionMapping()) {
               inferredData = new PropertyPreloadedData(AccessType.PROPERTY, "element", elementClass);
            } else {
               inferredData = new PropertyPreloadedData(AccessType.PROPERTY, "collection&&element", elementClass);
            }

            Component component = AnnotationBinder.fillComponent(holder, inferredData, isPropertyAnnotated ? AccessType.PROPERTY : AccessType.FIELD, true, entityBinder, false, false, true, mappings, this.inheritanceStatePerClass);
            collValue.setElement(component);
            if (StringHelper.isNotEmpty(hqlOrderBy)) {
               (new StringBuilder()).append(collValue.getOwnerEntityName()).append(".").append(joinColumns[0].getPropertyName()).toString();
               String orderBy = adjustUserSuppliedValueCollectionOrderingFragment(hqlOrderBy);
               if (orderBy != null) {
                  collValue.setOrderBy(orderBy);
               }
            }
         } else {
            SimpleValueBinder elementBinder = new SimpleValueBinder();
            elementBinder.setMappings(mappings);
            elementBinder.setReturnedClassName(collType.getName());
            if (elementColumns == null || elementColumns.length == 0) {
               elementColumns = new Ejb3Column[1];
               Ejb3Column column = new Ejb3Column();
               column.setImplicit(false);
               column.setNullable(true);
               column.setLength(255);
               column.setLogicalColumnName("elt");
               column.setJoins(new HashMap());
               column.setMappings(mappings);
               column.bind();
               elementColumns[0] = column;
            }

            for(Ejb3Column column : elementColumns) {
               column.setTable(collValue.getCollectionTable());
            }

            elementBinder.setColumns(elementColumns);
            elementBinder.setType(property, elementClass, collValue.getOwnerEntityName());
            elementBinder.setPersistentClassName(this.propertyHolder.getEntityName());
            elementBinder.setAccessType(this.accessType);
            collValue.setElement(elementBinder.make());
            String orderBy = adjustUserSuppliedValueCollectionOrderingFragment(hqlOrderBy);
            if (orderBy != null) {
               collValue.setOrderBy(orderBy);
            }
         }
      }

      checkFilterConditions(collValue);
      if (isCollectionOfEntities) {
         bindManytoManyInverseFk(collectionEntity, inverseJoinColumns, element, unique, mappings);
      }

   }

   private static void checkFilterConditions(Collection collValue) {
      if ((collValue.getFilters().size() != 0 || StringHelper.isNotEmpty(collValue.getWhere())) && collValue.getFetchMode() == org.hibernate.FetchMode.JOIN && !(collValue.getElement() instanceof SimpleValue) && collValue.getElement().getFetchMode() != org.hibernate.FetchMode.JOIN) {
         throw new MappingException("@ManyToMany or @CollectionOfElements defining filter or where without join fetching not valid within collection using join fetching[" + collValue.getRole() + "]");
      }
   }

   private static void bindCollectionSecondPass(Collection collValue, PersistentClass collectionEntity, Ejb3JoinColumn[] joinColumns, boolean cascadeDeleteEnabled, XProperty property, Mappings mappings) {
      BinderHelper.createSyntheticPropertyReference(joinColumns, collValue.getOwner(), collectionEntity, collValue, false, mappings);
      SimpleValue key = buildCollectionKey(collValue, joinColumns, cascadeDeleteEnabled, property, mappings);
      if (property.isAnnotationPresent(ElementCollection.class) && joinColumns.length > 0) {
         joinColumns[0].setJPA2ElementCollection(true);
      }

      TableBinder.bindFk(collValue.getOwner(), collectionEntity, joinColumns, key, false, mappings);
   }

   public void setCascadeDeleteEnabled(boolean onDeleteCascade) {
      this.cascadeDeleteEnabled = onDeleteCascade;
   }

   private String safeCollectionRole() {
      return this.propertyHolder != null ? this.propertyHolder.getEntityName() + "." + this.propertyName : "";
   }

   public static void bindManytoManyInverseFk(PersistentClass referencedEntity, Ejb3JoinColumn[] columns, SimpleValue value, boolean unique, Mappings mappings) {
      String mappedBy = columns[0].getMappedBy();
      if (StringHelper.isNotEmpty(mappedBy)) {
         Property property = referencedEntity.getRecursiveProperty(mappedBy);
         Iterator mappedByColumns;
         if (property.getValue() instanceof Collection) {
            mappedByColumns = ((Collection)property.getValue()).getKey().getColumnIterator();
         } else {
            Iterator joinsIt = referencedEntity.getJoinIterator();
            KeyValue key = null;

            while(joinsIt.hasNext()) {
               Join join = (Join)joinsIt.next();
               if (join.containsProperty(property)) {
                  key = join.getKey();
                  break;
               }
            }

            if (key == null) {
               key = property.getPersistentClass().getIdentifier();
            }

            mappedByColumns = key.getColumnIterator();
         }

         while(mappedByColumns.hasNext()) {
            Column column = (Column)mappedByColumns.next();
            columns[0].linkValueUsingAColumnCopy(column, value);
         }

         String referencedPropertyName = mappings.getPropertyReferencedAssociation("inverse__" + referencedEntity.getEntityName(), mappedBy);
         if (referencedPropertyName != null) {
            ((ManyToOne)value).setReferencedPropertyName(referencedPropertyName);
            mappings.addUniquePropertyReference(referencedEntity.getEntityName(), referencedPropertyName);
         }

         value.createForeignKey();
      } else {
         BinderHelper.createSyntheticPropertyReference(columns, referencedEntity, (PersistentClass)null, value, true, mappings);
         TableBinder.bindFk(referencedEntity, (PersistentClass)null, columns, value, unique, mappings);
      }

   }

   public void setFkJoinColumns(Ejb3JoinColumn[] ejb3JoinColumns) {
      this.fkJoinColumns = ejb3JoinColumns;
   }

   public void setExplicitAssociationTable(boolean explicitAssocTable) {
      this.isExplicitAssociationTable = explicitAssocTable;
   }

   public void setElementColumns(Ejb3Column[] elementColumns) {
      this.elementColumns = elementColumns;
   }

   public void setEmbedded(boolean annotationPresent) {
      this.isEmbedded = annotationPresent;
   }

   public void setProperty(XProperty property) {
      this.property = property;
   }

   public void setIgnoreNotFound(boolean ignoreNotFound) {
      this.ignoreNotFound = ignoreNotFound;
   }

   public void setMapKeyColumns(Ejb3Column[] mapKeyColumns) {
      this.mapKeyColumns = mapKeyColumns;
   }

   public void setMapKeyManyToManyColumns(Ejb3JoinColumn[] mapJoinColumns) {
      this.mapKeyManyToManyColumns = mapJoinColumns;
   }

   public void setLocalGenerators(HashMap localGenerators) {
      this.localGenerators = localGenerators;
   }
}
