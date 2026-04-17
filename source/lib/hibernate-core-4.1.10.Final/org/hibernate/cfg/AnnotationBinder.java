package org.hibernate.cfg;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.MapKeyColumn;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.MapKeyJoinColumns;
import javax.persistence.MappedSuperclass;
import javax.persistence.MapsId;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.OrderColumn;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.PrimaryKeyJoinColumns;
import javax.persistence.SecondaryTable;
import javax.persistence.SecondaryTables;
import javax.persistence.SequenceGenerator;
import javax.persistence.SharedCacheMode;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import org.hibernate.AnnotationException;
import org.hibernate.AssertionFailure;
import org.hibernate.EntityMode;
import org.hibernate.MappingException;
import org.hibernate.annotations.Any;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CollectionId;
import org.hibernate.annotations.Columns;
import org.hibernate.annotations.DiscriminatorFormula;
import org.hibernate.annotations.DiscriminatorOptions;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.FetchProfile;
import org.hibernate.annotations.FetchProfiles;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.FilterDefs;
import org.hibernate.annotations.Filters;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.GenericGenerators;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;
import org.hibernate.annotations.ManyToAny;
import org.hibernate.annotations.MapKeyType;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Parent;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.Source;
import org.hibernate.annotations.Tables;
import org.hibernate.annotations.Tuplizer;
import org.hibernate.annotations.Tuplizers;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.hibernate.annotations.Where;
import org.hibernate.annotations.common.reflection.ReflectionManager;
import org.hibernate.annotations.common.reflection.XAnnotatedElement;
import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.annotations.common.reflection.XMethod;
import org.hibernate.annotations.common.reflection.XPackage;
import org.hibernate.annotations.common.reflection.XProperty;
import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.cfg.annotations.CollectionBinder;
import org.hibernate.cfg.annotations.EntityBinder;
import org.hibernate.cfg.annotations.MapKeyColumnDelegator;
import org.hibernate.cfg.annotations.MapKeyJoinColumnDelegator;
import org.hibernate.cfg.annotations.Nullability;
import org.hibernate.cfg.annotations.PropertyBinder;
import org.hibernate.cfg.annotations.QueryBinder;
import org.hibernate.cfg.annotations.SimpleValueBinder;
import org.hibernate.cfg.annotations.TableBinder;
import org.hibernate.engine.spi.FilterDefinition;
import org.hibernate.id.MultipleHiLoPerTableGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.DependantValue;
import org.hibernate.mapping.IdGenerator;
import org.hibernate.mapping.Join;
import org.hibernate.mapping.JoinedSubclass;
import org.hibernate.mapping.KeyValue;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.RootClass;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.mapping.SingleTableSubclass;
import org.hibernate.mapping.Subclass;
import org.hibernate.mapping.ToOne;
import org.hibernate.mapping.UnionSubclass;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public final class AnnotationBinder {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, AnnotationBinder.class.getName());
   private static CacheConcurrencyStrategy DEFAULT_CACHE_CONCURRENCY_STRATEGY;

   private AnnotationBinder() {
   }

   public static void bindDefaults(Mappings mappings) {
      Map defaults = mappings.getReflectionManager().getDefaults();
      List<SequenceGenerator> anns = (List)defaults.get(SequenceGenerator.class);
      if (anns != null) {
         for(SequenceGenerator ann : anns) {
            IdGenerator idGen = buildIdGenerator(ann, mappings);
            if (idGen != null) {
               mappings.addDefaultGenerator(idGen);
            }
         }
      }

      anns = (List)defaults.get(TableGenerator.class);
      if (anns != null) {
         for(TableGenerator ann : anns) {
            IdGenerator idGen = buildIdGenerator(ann, mappings);
            if (idGen != null) {
               mappings.addDefaultGenerator(idGen);
            }
         }
      }

      anns = (List)defaults.get(NamedQuery.class);
      if (anns != null) {
         for(NamedQuery ann : anns) {
            QueryBinder.bindQuery(ann, mappings, true);
         }
      }

      anns = (List)defaults.get(NamedNativeQuery.class);
      if (anns != null) {
         for(NamedNativeQuery ann : anns) {
            QueryBinder.bindNativeQuery(ann, mappings, true);
         }
      }

      anns = (List)defaults.get(SqlResultSetMapping.class);
      if (anns != null) {
         for(SqlResultSetMapping ann : anns) {
            QueryBinder.bindSqlResultsetMapping(ann, mappings, true);
         }
      }

   }

   public static void bindPackage(String packageName, Mappings mappings) {
      XPackage pckg;
      try {
         pckg = mappings.getReflectionManager().packageForName(packageName);
      } catch (ClassNotFoundException var5) {
         LOG.packageNotFound(packageName);
         return;
      }

      if (pckg.isAnnotationPresent(SequenceGenerator.class)) {
         SequenceGenerator ann = (SequenceGenerator)pckg.getAnnotation(SequenceGenerator.class);
         IdGenerator idGen = buildIdGenerator(ann, mappings);
         mappings.addGenerator(idGen);
         if (LOG.isTraceEnabled()) {
            LOG.tracev("Add sequence generator with name: {0}", idGen.getName());
         }
      }

      if (pckg.isAnnotationPresent(TableGenerator.class)) {
         TableGenerator ann = (TableGenerator)pckg.getAnnotation(TableGenerator.class);
         IdGenerator idGen = buildIdGenerator(ann, mappings);
         mappings.addGenerator(idGen);
      }

      bindGenericGenerators(pckg, mappings);
      bindQueries(pckg, mappings);
      bindFilterDefs(pckg, mappings);
      bindTypeDefs(pckg, mappings);
      bindFetchProfiles(pckg, mappings);
      BinderHelper.bindAnyMetaDefs(pckg, mappings);
   }

   private static void bindGenericGenerators(XAnnotatedElement annotatedElement, Mappings mappings) {
      GenericGenerator defAnn = (GenericGenerator)annotatedElement.getAnnotation(GenericGenerator.class);
      GenericGenerators defsAnn = (GenericGenerators)annotatedElement.getAnnotation(GenericGenerators.class);
      if (defAnn != null) {
         bindGenericGenerator(defAnn, mappings);
      }

      if (defsAnn != null) {
         for(GenericGenerator def : defsAnn.value()) {
            bindGenericGenerator(def, mappings);
         }
      }

   }

   private static void bindGenericGenerator(GenericGenerator def, Mappings mappings) {
      IdGenerator idGen = buildIdGenerator(def, mappings);
      mappings.addGenerator(idGen);
   }

   private static void bindQueries(XAnnotatedElement annotatedElement, Mappings mappings) {
      SqlResultSetMapping ann = (SqlResultSetMapping)annotatedElement.getAnnotation(SqlResultSetMapping.class);
      QueryBinder.bindSqlResultsetMapping(ann, mappings, false);
      SqlResultSetMappings ann = (SqlResultSetMappings)annotatedElement.getAnnotation(SqlResultSetMappings.class);
      if (ann != null) {
         for(SqlResultSetMapping current : ann.value()) {
            QueryBinder.bindSqlResultsetMapping(current, mappings, false);
         }
      }

      NamedQuery ann = (NamedQuery)annotatedElement.getAnnotation(NamedQuery.class);
      QueryBinder.bindQuery(ann, mappings, false);
      org.hibernate.annotations.NamedQuery ann = (org.hibernate.annotations.NamedQuery)annotatedElement.getAnnotation(org.hibernate.annotations.NamedQuery.class);
      QueryBinder.bindQuery(ann, mappings);
      NamedQueries ann = (NamedQueries)annotatedElement.getAnnotation(NamedQueries.class);
      QueryBinder.bindQueries(ann, mappings, false);
      org.hibernate.annotations.NamedQueries ann = (org.hibernate.annotations.NamedQueries)annotatedElement.getAnnotation(org.hibernate.annotations.NamedQueries.class);
      QueryBinder.bindQueries(ann, mappings);
      NamedNativeQuery ann = (NamedNativeQuery)annotatedElement.getAnnotation(NamedNativeQuery.class);
      QueryBinder.bindNativeQuery(ann, mappings, false);
      org.hibernate.annotations.NamedNativeQuery ann = (org.hibernate.annotations.NamedNativeQuery)annotatedElement.getAnnotation(org.hibernate.annotations.NamedNativeQuery.class);
      QueryBinder.bindNativeQuery(ann, mappings);
      NamedNativeQueries ann = (NamedNativeQueries)annotatedElement.getAnnotation(NamedNativeQueries.class);
      QueryBinder.bindNativeQueries(ann, mappings, false);
      org.hibernate.annotations.NamedNativeQueries ann = (org.hibernate.annotations.NamedNativeQueries)annotatedElement.getAnnotation(org.hibernate.annotations.NamedNativeQueries.class);
      QueryBinder.bindNativeQueries(ann, mappings);
   }

   private static IdGenerator buildIdGenerator(Annotation ann, Mappings mappings) {
      IdGenerator idGen = new IdGenerator();
      if (mappings.getSchemaName() != null) {
         idGen.addParam("schema", mappings.getSchemaName());
      }

      if (mappings.getCatalogName() != null) {
         idGen.addParam("catalog", mappings.getCatalogName());
      }

      boolean useNewGeneratorMappings = mappings.useNewGeneratorMappings();
      if (ann == null) {
         idGen = null;
      } else if (ann instanceof TableGenerator) {
         TableGenerator tabGen = (TableGenerator)ann;
         idGen.setName(tabGen.name());
         if (useNewGeneratorMappings) {
            idGen.setIdentifierGeneratorStrategy(org.hibernate.id.enhanced.TableGenerator.class.getName());
            idGen.addParam("prefer_entity_table_as_segment_value", "true");
            if (!BinderHelper.isEmptyAnnotationValue(tabGen.catalog())) {
               idGen.addParam("catalog", tabGen.catalog());
            }

            if (!BinderHelper.isEmptyAnnotationValue(tabGen.schema())) {
               idGen.addParam("schema", tabGen.schema());
            }

            if (!BinderHelper.isEmptyAnnotationValue(tabGen.table())) {
               idGen.addParam("table_name", tabGen.table());
            }

            if (!BinderHelper.isEmptyAnnotationValue(tabGen.pkColumnName())) {
               idGen.addParam("segment_column_name", tabGen.pkColumnName());
            }

            if (!BinderHelper.isEmptyAnnotationValue(tabGen.pkColumnValue())) {
               idGen.addParam("segment_value", tabGen.pkColumnValue());
            }

            if (!BinderHelper.isEmptyAnnotationValue(tabGen.valueColumnName())) {
               idGen.addParam("value_column_name", tabGen.valueColumnName());
            }

            idGen.addParam("increment_size", String.valueOf(tabGen.allocationSize()));
            idGen.addParam("initial_value", String.valueOf(tabGen.initialValue() + 1));
            if (tabGen.uniqueConstraints() != null && tabGen.uniqueConstraints().length > 0) {
               LOG.warn(tabGen.name());
            }
         } else {
            idGen.setIdentifierGeneratorStrategy(MultipleHiLoPerTableGenerator.class.getName());
            if (!BinderHelper.isEmptyAnnotationValue(tabGen.table())) {
               idGen.addParam("table", tabGen.table());
            }

            if (!BinderHelper.isEmptyAnnotationValue(tabGen.catalog())) {
               idGen.addParam("catalog", tabGen.catalog());
            }

            if (!BinderHelper.isEmptyAnnotationValue(tabGen.schema())) {
               idGen.addParam("schema", tabGen.schema());
            }

            if (tabGen.uniqueConstraints() != null && tabGen.uniqueConstraints().length > 0) {
               LOG.ignoringTableGeneratorConstraints(tabGen.name());
            }

            if (!BinderHelper.isEmptyAnnotationValue(tabGen.pkColumnName())) {
               idGen.addParam("primary_key_column", tabGen.pkColumnName());
            }

            if (!BinderHelper.isEmptyAnnotationValue(tabGen.valueColumnName())) {
               idGen.addParam("value_column", tabGen.valueColumnName());
            }

            if (!BinderHelper.isEmptyAnnotationValue(tabGen.pkColumnValue())) {
               idGen.addParam("primary_key_value", tabGen.pkColumnValue());
            }

            idGen.addParam("max_lo", String.valueOf(tabGen.allocationSize() - 1));
         }

         if (LOG.isTraceEnabled()) {
            LOG.tracev("Add table generator with name: {0}", idGen.getName());
         }
      } else if (ann instanceof SequenceGenerator) {
         SequenceGenerator seqGen = (SequenceGenerator)ann;
         idGen.setName(seqGen.name());
         if (useNewGeneratorMappings) {
            idGen.setIdentifierGeneratorStrategy(SequenceStyleGenerator.class.getName());
            if (!BinderHelper.isEmptyAnnotationValue(seqGen.catalog())) {
               idGen.addParam("catalog", seqGen.catalog());
            }

            if (!BinderHelper.isEmptyAnnotationValue(seqGen.schema())) {
               idGen.addParam("schema", seqGen.schema());
            }

            if (!BinderHelper.isEmptyAnnotationValue(seqGen.sequenceName())) {
               idGen.addParam("sequence_name", seqGen.sequenceName());
            }

            idGen.addParam("increment_size", String.valueOf(seqGen.allocationSize()));
            idGen.addParam("initial_value", String.valueOf(seqGen.initialValue()));
         } else {
            idGen.setIdentifierGeneratorStrategy("seqhilo");
            if (!BinderHelper.isEmptyAnnotationValue(seqGen.sequenceName())) {
               idGen.addParam("sequence", seqGen.sequenceName());
            }

            if (seqGen.initialValue() != 1) {
               LOG.unsupportedInitialValue("hibernate.id.new_generator_mappings");
            }

            idGen.addParam("max_lo", String.valueOf(seqGen.allocationSize() - 1));
            if (LOG.isTraceEnabled()) {
               LOG.tracev("Add sequence generator with name: {0}", idGen.getName());
            }
         }
      } else {
         if (!(ann instanceof GenericGenerator)) {
            throw new AssertionFailure("Unknown Generator annotation: " + ann);
         }

         GenericGenerator genGen = (GenericGenerator)ann;
         idGen.setName(genGen.name());
         idGen.setIdentifierGeneratorStrategy(genGen.strategy());
         Parameter[] params = genGen.parameters();

         for(Parameter parameter : params) {
            idGen.addParam(parameter.name(), parameter.value());
         }

         if (LOG.isTraceEnabled()) {
            LOG.tracev("Add generic generator with name: {0}", idGen.getName());
         }
      }

      return idGen;
   }

   public static void bindClass(XClass clazzToProcess, Map inheritanceStatePerClass, Mappings mappings) throws MappingException {
      if (clazzToProcess.isAnnotationPresent(Entity.class) && clazzToProcess.isAnnotationPresent(MappedSuperclass.class)) {
         throw new AnnotationException("An entity cannot be annotated with both @Entity and @MappedSuperclass: " + clazzToProcess.getName());
      } else {
         InheritanceState inheritanceState = (InheritanceState)inheritanceStatePerClass.get(clazzToProcess);
         AnnotatedClassType classType = mappings.getClassType(clazzToProcess);
         if (AnnotatedClassType.EMBEDDABLE_SUPERCLASS.equals(classType)) {
            bindQueries(clazzToProcess, mappings);
            bindTypeDefs(clazzToProcess, mappings);
            bindFilterDefs(clazzToProcess, mappings);
         }

         if (isEntityClassType(clazzToProcess, classType)) {
            if (LOG.isDebugEnabled()) {
               LOG.debugf("Binding entity from annotated class: %s", clazzToProcess.getName());
            }

            PersistentClass superEntity = getSuperEntity(clazzToProcess, inheritanceStatePerClass, mappings, inheritanceState);
            PersistentClass persistentClass = makePersistentClass(inheritanceState, superEntity);
            Entity entityAnn = (Entity)clazzToProcess.getAnnotation(Entity.class);
            org.hibernate.annotations.Entity hibEntityAnn = (org.hibernate.annotations.Entity)clazzToProcess.getAnnotation(org.hibernate.annotations.Entity.class);
            EntityBinder entityBinder = new EntityBinder(entityAnn, hibEntityAnn, clazzToProcess, persistentClass, mappings);
            entityBinder.setInheritanceState(inheritanceState);
            bindQueries(clazzToProcess, mappings);
            bindFilterDefs(clazzToProcess, mappings);
            bindTypeDefs(clazzToProcess, mappings);
            bindFetchProfiles(clazzToProcess, mappings);
            BinderHelper.bindAnyMetaDefs(clazzToProcess, mappings);
            String schema = "";
            String table = "";
            String catalog = "";
            List<UniqueConstraintHolder> uniqueConstraints = new ArrayList();
            if (clazzToProcess.isAnnotationPresent(Table.class)) {
               Table tabAnn = (Table)clazzToProcess.getAnnotation(Table.class);
               table = tabAnn.name();
               schema = tabAnn.schema();
               catalog = tabAnn.catalog();
               uniqueConstraints = TableBinder.buildUniqueConstraintHolders(tabAnn.uniqueConstraints());
            }

            Ejb3JoinColumn[] inheritanceJoinedColumns = makeInheritanceJoinColumns(clazzToProcess, mappings, inheritanceState, superEntity);
            Ejb3DiscriminatorColumn discriminatorColumn = null;
            if (InheritanceType.SINGLE_TABLE.equals(inheritanceState.getType())) {
               discriminatorColumn = processDiscriminatorProperties(clazzToProcess, mappings, inheritanceState, entityBinder);
            }

            entityBinder.setProxy((Proxy)clazzToProcess.getAnnotation(Proxy.class));
            entityBinder.setBatchSize((BatchSize)clazzToProcess.getAnnotation(BatchSize.class));
            entityBinder.setWhere((Where)clazzToProcess.getAnnotation(Where.class));
            entityBinder.setCache(determineCacheSettings(clazzToProcess, mappings));
            entityBinder.setNaturalIdCache(clazzToProcess, (NaturalIdCache)clazzToProcess.getAnnotation(NaturalIdCache.class));
            bindFilters(clazzToProcess, entityBinder, mappings);
            entityBinder.bindEntity();
            if (inheritanceState.hasTable()) {
               Check checkAnn = (Check)clazzToProcess.getAnnotation(Check.class);
               String constraints = checkAnn == null ? null : checkAnn.constraints();
               entityBinder.bindTable(schema, catalog, table, uniqueConstraints, constraints, inheritanceState.hasDenormalizedTable() ? superEntity.getTable() : null);
            } else if (clazzToProcess.isAnnotationPresent(Table.class)) {
               LOG.invalidTableAnnotation(clazzToProcess.getName());
            }

            PropertyHolder propertyHolder = PropertyHolderBuilder.buildPropertyHolder(clazzToProcess, persistentClass, entityBinder, mappings, inheritanceStatePerClass);
            SecondaryTable secTabAnn = (SecondaryTable)clazzToProcess.getAnnotation(SecondaryTable.class);
            SecondaryTables secTabsAnn = (SecondaryTables)clazzToProcess.getAnnotation(SecondaryTables.class);
            entityBinder.firstLevelSecondaryTablesBinding(secTabAnn, secTabsAnn);
            OnDelete onDeleteAnn = (OnDelete)clazzToProcess.getAnnotation(OnDelete.class);
            boolean onDeleteAppropriate = false;
            if (InheritanceType.JOINED.equals(inheritanceState.getType()) && inheritanceState.hasParents()) {
               onDeleteAppropriate = true;
               JoinedSubclass jsc = (JoinedSubclass)persistentClass;
               SimpleValue key = new DependantValue(mappings, jsc.getTable(), jsc.getIdentifier());
               jsc.setKey(key);
               ForeignKey fk = (ForeignKey)clazzToProcess.getAnnotation(ForeignKey.class);
               if (fk != null && !BinderHelper.isEmptyAnnotationValue(fk.name())) {
                  key.setForeignKeyName(fk.name());
               }

               if (onDeleteAnn != null) {
                  key.setCascadeDeleteEnabled(OnDeleteAction.CASCADE.equals(onDeleteAnn.action()));
               } else {
                  key.setCascadeDeleteEnabled(false);
               }

               SecondPass sp = new JoinedSubclassFkSecondPass(jsc, inheritanceJoinedColumns, key, mappings);
               mappings.addSecondPass(sp);
               mappings.addSecondPass(new CreateKeySecondPass(jsc));
            } else if (InheritanceType.SINGLE_TABLE.equals(inheritanceState.getType())) {
               if (!inheritanceState.hasParents() && (inheritanceState.hasSiblings() || !discriminatorColumn.isImplicit())) {
                  bindDiscriminatorToPersistentClass((RootClass)persistentClass, discriminatorColumn, entityBinder.getSecondaryTables(), propertyHolder, mappings);
                  entityBinder.bindDiscriminatorValue();
               }
            } else if (InheritanceType.TABLE_PER_CLASS.equals(inheritanceState.getType())) {
            }

            if (onDeleteAnn != null && !onDeleteAppropriate) {
               LOG.invalidOnDeleteAnnotation(propertyHolder.getEntityName());
            }

            HashMap<String, IdGenerator> classGenerators = buildLocalGenerators(clazzToProcess, mappings);
            InheritanceState.ElementsToProcess elementsToProcess = inheritanceState.getElementsToProcess();
            inheritanceState.postProcess(persistentClass, entityBinder);
            boolean subclassAndSingleTableStrategy = inheritanceState.getType() == InheritanceType.SINGLE_TABLE && inheritanceState.hasParents();
            Set<String> idPropertiesIfIdClass = new HashSet();
            boolean isIdClass = mapAsIdClass(inheritanceStatePerClass, inheritanceState, persistentClass, entityBinder, propertyHolder, elementsToProcess, idPropertiesIfIdClass, mappings);
            if (!isIdClass) {
               entityBinder.setWrapIdsInEmbeddedComponents(elementsToProcess.getIdPropertyCount() > 1);
            }

            processIdPropertiesIfNotAlready(inheritanceStatePerClass, mappings, persistentClass, entityBinder, propertyHolder, classGenerators, elementsToProcess, subclassAndSingleTableStrategy, idPropertiesIfIdClass);
            if (!inheritanceState.hasParents()) {
               RootClass rootClass = (RootClass)persistentClass;
               mappings.addSecondPass(new CreateKeySecondPass(rootClass));
            } else {
               superEntity.addSubclass((Subclass)persistentClass);
            }

            mappings.addClass(persistentClass);
            mappings.addSecondPass(new SecondaryTableSecondPass(entityBinder, propertyHolder, clazzToProcess));
            entityBinder.processComplementaryTableDefinitions((org.hibernate.annotations.Table)clazzToProcess.getAnnotation(org.hibernate.annotations.Table.class));
            entityBinder.processComplementaryTableDefinitions((Tables)clazzToProcess.getAnnotation(Tables.class));
         }
      }
   }

   private static Ejb3DiscriminatorColumn processDiscriminatorProperties(XClass clazzToProcess, Mappings mappings, InheritanceState inheritanceState, EntityBinder entityBinder) {
      Ejb3DiscriminatorColumn discriminatorColumn = null;
      DiscriminatorColumn discAnn = (DiscriminatorColumn)clazzToProcess.getAnnotation(DiscriminatorColumn.class);
      DiscriminatorType discriminatorType = discAnn != null ? discAnn.discriminatorType() : DiscriminatorType.STRING;
      DiscriminatorFormula discFormulaAnn = (DiscriminatorFormula)clazzToProcess.getAnnotation(DiscriminatorFormula.class);
      if (!inheritanceState.hasParents()) {
         discriminatorColumn = Ejb3DiscriminatorColumn.buildDiscriminatorColumn(discriminatorType, discAnn, discFormulaAnn, mappings);
      }

      if (discAnn != null && inheritanceState.hasParents()) {
         LOG.invalidDiscriminatorAnnotation(clazzToProcess.getName());
      }

      String discrimValue = clazzToProcess.isAnnotationPresent(DiscriminatorValue.class) ? ((DiscriminatorValue)clazzToProcess.getAnnotation(DiscriminatorValue.class)).value() : null;
      entityBinder.setDiscriminatorValue(discrimValue);
      DiscriminatorOptions discriminatorOptions = (DiscriminatorOptions)clazzToProcess.getAnnotation(DiscriminatorOptions.class);
      if (discriminatorOptions != null) {
         entityBinder.setForceDiscriminator(discriminatorOptions.force());
         entityBinder.setInsertableDiscriminator(discriminatorOptions.insert());
      }

      return discriminatorColumn;
   }

   private static void processIdPropertiesIfNotAlready(Map inheritanceStatePerClass, Mappings mappings, PersistentClass persistentClass, EntityBinder entityBinder, PropertyHolder propertyHolder, HashMap classGenerators, InheritanceState.ElementsToProcess elementsToProcess, boolean subclassAndSingleTableStrategy, Set idPropertiesIfIdClass) {
      Set<String> missingIdProperties = new HashSet(idPropertiesIfIdClass);

      for(PropertyData propertyAnnotatedElement : elementsToProcess.getElements()) {
         String propertyName = propertyAnnotatedElement.getPropertyName();
         if (!idPropertiesIfIdClass.contains(propertyName)) {
            processElementAnnotations(propertyHolder, subclassAndSingleTableStrategy ? Nullability.FORCED_NULL : Nullability.NO_CONSTRAINT, propertyAnnotatedElement, classGenerators, entityBinder, false, false, false, mappings, inheritanceStatePerClass);
         } else {
            missingIdProperties.remove(propertyName);
         }
      }

      if (missingIdProperties.size() != 0) {
         StringBuilder missings = new StringBuilder();

         for(String property : missingIdProperties) {
            missings.append(property).append(", ");
         }

         throw new AnnotationException("Unable to find properties (" + missings.substring(0, missings.length() - 2) + ") in entity annotated with @IdClass:" + persistentClass.getEntityName());
      }
   }

   private static boolean mapAsIdClass(Map inheritanceStatePerClass, InheritanceState inheritanceState, PersistentClass persistentClass, EntityBinder entityBinder, PropertyHolder propertyHolder, InheritanceState.ElementsToProcess elementsToProcess, Set idPropertiesIfIdClass, Mappings mappings) {
      XClass classWithIdClass = inheritanceState.getClassWithIdClass(false);
      if (classWithIdClass == null) {
         return false;
      } else {
         IdClass idClass = (IdClass)classWithIdClass.getAnnotation(IdClass.class);
         XClass compositeClass = mappings.getReflectionManager().toXClass(idClass.value());
         PropertyData inferredData = new PropertyPreloadedData(entityBinder.getPropertyAccessType(), "id", compositeClass);
         PropertyData baseInferredData = new PropertyPreloadedData(entityBinder.getPropertyAccessType(), "id", classWithIdClass);
         AccessType propertyAccessor = entityBinder.getPropertyAccessor(compositeClass);
         boolean isFakeIdClass = isIdClassPkOfTheAssociatedEntity(elementsToProcess, compositeClass, inferredData, baseInferredData, propertyAccessor, inheritanceStatePerClass, mappings);
         if (isFakeIdClass) {
            return false;
         } else {
            boolean isComponent = true;
            String generatorType = "assigned";
            String generator = "";
            boolean ignoreIdAnnotations = entityBinder.isIgnoreIdAnnotations();
            entityBinder.setIgnoreIdAnnotations(true);
            propertyHolder.setInIdClass(true);
            bindIdClass(generatorType, generator, inferredData, baseInferredData, (Ejb3Column[])null, propertyHolder, isComponent, propertyAccessor, entityBinder, true, false, mappings, inheritanceStatePerClass);
            propertyHolder.setInIdClass((Boolean)null);
            PropertyData var23 = new PropertyPreloadedData(propertyAccessor, "_identifierMapper", compositeClass);
            Component mapper = fillComponent(propertyHolder, var23, baseInferredData, propertyAccessor, false, entityBinder, true, true, false, mappings, inheritanceStatePerClass);
            entityBinder.setIgnoreIdAnnotations(ignoreIdAnnotations);
            persistentClass.setIdentifierMapper(mapper);
            org.hibernate.mapping.MappedSuperclass superclass = BinderHelper.getMappedSuperclassOrNull(var23.getDeclaringClass(), inheritanceStatePerClass, mappings);
            if (superclass != null) {
               superclass.setDeclaredIdentifierMapper(mapper);
            } else {
               persistentClass.setDeclaredIdentifierMapper(mapper);
            }

            Property property = new Property();
            property.setName("_identifierMapper");
            property.setNodeName("id");
            property.setUpdateable(false);
            property.setInsertable(false);
            property.setValue(mapper);
            property.setPropertyAccessorName("embedded");
            persistentClass.addProperty(property);
            entityBinder.setIgnoreIdAnnotations(true);
            Iterator properties = mapper.getPropertyIterator();

            while(properties.hasNext()) {
               idPropertiesIfIdClass.add(((Property)properties.next()).getName());
            }

            return true;
         }
      }
   }

   private static boolean isIdClassPkOfTheAssociatedEntity(InheritanceState.ElementsToProcess elementsToProcess, XClass compositeClass, PropertyData inferredData, PropertyData baseInferredData, AccessType propertyAccessor, Map inheritanceStatePerClass, Mappings mappings) {
      if (elementsToProcess.getIdPropertyCount() != 1) {
         return false;
      } else {
         PropertyData idPropertyOnBaseClass = getUniqueIdPropertyFromBaseClass(inferredData, baseInferredData, propertyAccessor, mappings);
         InheritanceState state = (InheritanceState)inheritanceStatePerClass.get(idPropertyOnBaseClass.getClassOrElement());
         if (state == null) {
            return false;
         } else {
            XClass associatedClassWithIdClass = state.getClassWithIdClass(true);
            if (associatedClassWithIdClass != null) {
               XClass idClass = mappings.getReflectionManager().toXClass(((IdClass)associatedClassWithIdClass.getAnnotation(IdClass.class)).value());
               return idClass.equals(compositeClass);
            } else {
               XProperty property = idPropertyOnBaseClass.getProperty();
               return property.isAnnotationPresent(ManyToOne.class) || property.isAnnotationPresent(OneToOne.class);
            }
         }
      }
   }

   private static Cache determineCacheSettings(XClass clazzToProcess, Mappings mappings) {
      Cache cacheAnn = (Cache)clazzToProcess.getAnnotation(Cache.class);
      if (cacheAnn != null) {
         return cacheAnn;
      } else {
         Cacheable cacheableAnn = (Cacheable)clazzToProcess.getAnnotation(Cacheable.class);
         SharedCacheMode mode = determineSharedCacheMode(mappings);
         switch (mode) {
            case ALL:
               cacheAnn = buildCacheMock(clazzToProcess.getName(), mappings);
               break;
            case ENABLE_SELECTIVE:
               if (cacheableAnn != null && cacheableAnn.value()) {
                  cacheAnn = buildCacheMock(clazzToProcess.getName(), mappings);
               }
               break;
            case DISABLE_SELECTIVE:
               if (cacheableAnn == null || cacheableAnn.value()) {
                  cacheAnn = buildCacheMock(clazzToProcess.getName(), mappings);
               }
         }

         return cacheAnn;
      }
   }

   private static SharedCacheMode determineSharedCacheMode(Mappings mappings) {
      Object value = mappings.getConfigurationProperties().get("javax.persistence.sharedCache.mode");
      SharedCacheMode mode;
      if (value == null) {
         LOG.debug("No value specified for 'javax.persistence.sharedCache.mode'; using UNSPECIFIED");
         mode = SharedCacheMode.UNSPECIFIED;
      } else if (SharedCacheMode.class.isInstance(value)) {
         mode = (SharedCacheMode)value;
      } else {
         try {
            mode = SharedCacheMode.valueOf(value.toString());
         } catch (Exception e) {
            LOG.debugf("Unable to resolve given mode name [%s]; using UNSPECIFIED : %s", value, e);
            mode = SharedCacheMode.UNSPECIFIED;
         }
      }

      return mode;
   }

   private static Cache buildCacheMock(String region, Mappings mappings) {
      return new LocalCacheAnnotationImpl(region, determineCacheConcurrencyStrategy(mappings));
   }

   static void prepareDefaultCacheConcurrencyStrategy(Properties properties) {
      if (DEFAULT_CACHE_CONCURRENCY_STRATEGY != null) {
         LOG.trace("Default cache concurrency strategy already defined");
      } else if (!properties.containsKey("hibernate.cache.default_cache_concurrency_strategy")) {
         LOG.trace("Given properties did not contain any default cache concurrency strategy setting");
      } else {
         String strategyName = properties.getProperty("hibernate.cache.default_cache_concurrency_strategy");
         LOG.tracev("Discovered default cache concurrency strategy via config [{0}]", strategyName);
         CacheConcurrencyStrategy strategy = CacheConcurrencyStrategy.parse(strategyName);
         if (strategy == null) {
            LOG.trace("Discovered default cache concurrency strategy specified nothing");
         } else {
            LOG.debugf("Setting default cache concurrency strategy via config [%s]", strategy.name());
            DEFAULT_CACHE_CONCURRENCY_STRATEGY = strategy;
         }
      }
   }

   private static CacheConcurrencyStrategy determineCacheConcurrencyStrategy(Mappings mappings) {
      if (DEFAULT_CACHE_CONCURRENCY_STRATEGY == null) {
         RegionFactory cacheRegionFactory = SettingsFactory.createRegionFactory(mappings.getConfigurationProperties(), true);
         DEFAULT_CACHE_CONCURRENCY_STRATEGY = CacheConcurrencyStrategy.fromAccessType(cacheRegionFactory.getDefaultAccessType());
      }

      return DEFAULT_CACHE_CONCURRENCY_STRATEGY;
   }

   private static PersistentClass makePersistentClass(InheritanceState inheritanceState, PersistentClass superEntity) {
      PersistentClass persistentClass;
      if (!inheritanceState.hasParents()) {
         persistentClass = new RootClass();
      } else if (InheritanceType.SINGLE_TABLE.equals(inheritanceState.getType())) {
         persistentClass = new SingleTableSubclass(superEntity);
      } else if (InheritanceType.JOINED.equals(inheritanceState.getType())) {
         persistentClass = new JoinedSubclass(superEntity);
      } else {
         if (!InheritanceType.TABLE_PER_CLASS.equals(inheritanceState.getType())) {
            throw new AssertionFailure("Unknown inheritance type: " + inheritanceState.getType());
         }

         persistentClass = new UnionSubclass(superEntity);
      }

      return persistentClass;
   }

   private static Ejb3JoinColumn[] makeInheritanceJoinColumns(XClass clazzToProcess, Mappings mappings, InheritanceState inheritanceState, PersistentClass superEntity) {
      Ejb3JoinColumn[] inheritanceJoinedColumns = null;
      boolean hasJoinedColumns = inheritanceState.hasParents() && InheritanceType.JOINED.equals(inheritanceState.getType());
      if (hasJoinedColumns) {
         PrimaryKeyJoinColumns jcsAnn = (PrimaryKeyJoinColumns)clazzToProcess.getAnnotation(PrimaryKeyJoinColumns.class);
         boolean explicitInheritanceJoinedColumns = jcsAnn != null && jcsAnn.value().length != 0;
         if (explicitInheritanceJoinedColumns) {
            int nbrOfInhJoinedColumns = jcsAnn.value().length;
            inheritanceJoinedColumns = new Ejb3JoinColumn[nbrOfInhJoinedColumns];

            for(int colIndex = 0; colIndex < nbrOfInhJoinedColumns; ++colIndex) {
               PrimaryKeyJoinColumn jcAnn = jcsAnn.value()[colIndex];
               inheritanceJoinedColumns[colIndex] = Ejb3JoinColumn.buildJoinColumn(jcAnn, (JoinColumn)null, superEntity.getIdentifier(), (Map)null, (PropertyHolder)null, mappings);
            }
         } else {
            PrimaryKeyJoinColumn jcAnn = (PrimaryKeyJoinColumn)clazzToProcess.getAnnotation(PrimaryKeyJoinColumn.class);
            inheritanceJoinedColumns = new Ejb3JoinColumn[]{Ejb3JoinColumn.buildJoinColumn(jcAnn, (JoinColumn)null, superEntity.getIdentifier(), (Map)null, (PropertyHolder)null, mappings)};
         }

         LOG.trace("Subclass joined column(s) created");
      } else if (clazzToProcess.isAnnotationPresent(PrimaryKeyJoinColumns.class) || clazzToProcess.isAnnotationPresent(PrimaryKeyJoinColumn.class)) {
         LOG.invalidPrimaryKeyJoinColumnAnnotation();
      }

      return inheritanceJoinedColumns;
   }

   private static PersistentClass getSuperEntity(XClass clazzToProcess, Map inheritanceStatePerClass, Mappings mappings, InheritanceState inheritanceState) {
      InheritanceState superEntityState = InheritanceState.getInheritanceStateOfSuperEntity(clazzToProcess, inheritanceStatePerClass);
      PersistentClass superEntity = superEntityState != null ? mappings.getClass(superEntityState.getClazz().getName()) : null;
      if (superEntity == null && inheritanceState.hasParents()) {
         throw new AssertionFailure("Subclass has to be binded after it's mother class: " + superEntityState.getClazz().getName());
      } else {
         return superEntity;
      }
   }

   private static boolean isEntityClassType(XClass clazzToProcess, AnnotatedClassType classType) {
      if (!AnnotatedClassType.EMBEDDABLE_SUPERCLASS.equals(classType) && !AnnotatedClassType.NONE.equals(classType) && !AnnotatedClassType.EMBEDDABLE.equals(classType)) {
         if (!classType.equals(AnnotatedClassType.ENTITY)) {
            throw new AnnotationException("Annotated class should have a @javax.persistence.Entity, @javax.persistence.Embeddable or @javax.persistence.EmbeddedSuperclass annotation: " + clazzToProcess.getName());
         } else {
            return true;
         }
      } else {
         if (AnnotatedClassType.NONE.equals(classType) && clazzToProcess.isAnnotationPresent(org.hibernate.annotations.Entity.class)) {
            LOG.missingEntityAnnotation(clazzToProcess.getName());
         }

         return false;
      }
   }

   private static void bindFilters(XClass annotatedClass, EntityBinder entityBinder, Mappings mappings) {
      bindFilters(annotatedClass, entityBinder);

      for(XClass classToProcess = annotatedClass.getSuperclass(); classToProcess != null; classToProcess = classToProcess.getSuperclass()) {
         AnnotatedClassType classType = mappings.getClassType(classToProcess);
         if (AnnotatedClassType.EMBEDDABLE_SUPERCLASS.equals(classType)) {
            bindFilters(classToProcess, entityBinder);
         }
      }

   }

   private static void bindFilters(XAnnotatedElement annotatedElement, EntityBinder entityBinder) {
      Filters filtersAnn = (Filters)annotatedElement.getAnnotation(Filters.class);
      if (filtersAnn != null) {
         for(Filter filter : filtersAnn.value()) {
            entityBinder.addFilter(filter);
         }
      }

      Filter filterAnn = (Filter)annotatedElement.getAnnotation(Filter.class);
      if (filterAnn != null) {
         entityBinder.addFilter(filterAnn);
      }

   }

   private static void bindFilterDefs(XAnnotatedElement annotatedElement, Mappings mappings) {
      FilterDef defAnn = (FilterDef)annotatedElement.getAnnotation(FilterDef.class);
      FilterDefs defsAnn = (FilterDefs)annotatedElement.getAnnotation(FilterDefs.class);
      if (defAnn != null) {
         bindFilterDef(defAnn, mappings);
      }

      if (defsAnn != null) {
         for(FilterDef def : defsAnn.value()) {
            bindFilterDef(def, mappings);
         }
      }

   }

   private static void bindFilterDef(FilterDef defAnn, Mappings mappings) {
      Map<String, Type> params = new HashMap();

      for(ParamDef param : defAnn.parameters()) {
         params.put(param.name(), mappings.getTypeResolver().heuristicType(param.type()));
      }

      FilterDefinition def = new FilterDefinition(defAnn.name(), defAnn.defaultCondition(), params);
      LOG.debugf("Binding filter definition: %s", def.getFilterName());
      mappings.addFilterDefinition(def);
   }

   private static void bindTypeDefs(XAnnotatedElement annotatedElement, Mappings mappings) {
      TypeDef defAnn = (TypeDef)annotatedElement.getAnnotation(TypeDef.class);
      TypeDefs defsAnn = (TypeDefs)annotatedElement.getAnnotation(TypeDefs.class);
      if (defAnn != null) {
         bindTypeDef(defAnn, mappings);
      }

      if (defsAnn != null) {
         for(TypeDef def : defsAnn.value()) {
            bindTypeDef(def, mappings);
         }
      }

   }

   private static void bindFetchProfiles(XAnnotatedElement annotatedElement, Mappings mappings) {
      FetchProfile fetchProfileAnnotation = (FetchProfile)annotatedElement.getAnnotation(FetchProfile.class);
      FetchProfiles fetchProfileAnnotations = (FetchProfiles)annotatedElement.getAnnotation(FetchProfiles.class);
      if (fetchProfileAnnotation != null) {
         bindFetchProfile(fetchProfileAnnotation, mappings);
      }

      if (fetchProfileAnnotations != null) {
         for(FetchProfile profile : fetchProfileAnnotations.value()) {
            bindFetchProfile(profile, mappings);
         }
      }

   }

   private static void bindFetchProfile(FetchProfile fetchProfileAnnotation, Mappings mappings) {
      for(FetchProfile.FetchOverride fetch : fetchProfileAnnotation.fetchOverrides()) {
         FetchMode mode = fetch.mode();
         if (!mode.equals(FetchMode.JOIN)) {
            throw new MappingException("Only FetchMode.JOIN is currently supported");
         }

         SecondPass sp = new VerifyFetchProfileReferenceSecondPass(fetchProfileAnnotation.name(), fetch, mappings);
         mappings.addSecondPass(sp);
      }

   }

   private static void bindTypeDef(TypeDef defAnn, Mappings mappings) {
      Properties params = new Properties();

      for(Parameter param : defAnn.parameters()) {
         params.setProperty(param.name(), param.value());
      }

      if (BinderHelper.isEmptyAnnotationValue(defAnn.name()) && defAnn.defaultForType().equals(Void.TYPE)) {
         throw new AnnotationException("Either name or defaultForType (or both) attribute should be set in TypeDef having typeClass " + defAnn.typeClass().getName());
      } else {
         String typeBindMessageF = "Binding type definition: %s";
         if (!BinderHelper.isEmptyAnnotationValue(defAnn.name())) {
            if (LOG.isDebugEnabled()) {
               LOG.debugf("Binding type definition: %s", defAnn.name());
            }

            mappings.addTypeDef(defAnn.name(), defAnn.typeClass().getName(), params);
         }

         if (!defAnn.defaultForType().equals(Void.TYPE)) {
            if (LOG.isDebugEnabled()) {
               LOG.debugf("Binding type definition: %s", defAnn.defaultForType().getName());
            }

            mappings.addTypeDef(defAnn.defaultForType().getName(), defAnn.typeClass().getName(), params);
         }

      }
   }

   private static void bindDiscriminatorToPersistentClass(RootClass rootClass, Ejb3DiscriminatorColumn discriminatorColumn, Map secondaryTables, PropertyHolder propertyHolder, Mappings mappings) {
      if (rootClass.getDiscriminator() == null) {
         if (discriminatorColumn == null) {
            throw new AssertionFailure("discriminator column should have been built");
         }

         discriminatorColumn.setJoins(secondaryTables);
         discriminatorColumn.setPropertyHolder(propertyHolder);
         SimpleValue discrim = new SimpleValue(mappings, rootClass.getTable());
         rootClass.setDiscriminator(discrim);
         discriminatorColumn.linkWithValue(discrim);
         discrim.setTypeName(discriminatorColumn.getDiscriminatorTypeName());
         rootClass.setPolymorphic(true);
         if (LOG.isTraceEnabled()) {
            LOG.tracev("Setting discriminator for entity {0}", rootClass.getEntityName());
         }
      }

   }

   static int addElementsOfClass(List elements, AccessType defaultAccessType, PropertyContainer propertyContainer, Mappings mappings) {
      int idPropertyCounter = 0;
      AccessType accessType = defaultAccessType;
      if (propertyContainer.hasExplicitAccessStrategy()) {
         accessType = propertyContainer.getExplicitAccessStrategy();
      }

      for(XProperty p : propertyContainer.getProperties(accessType)) {
         int currentIdPropertyCounter = addProperty(propertyContainer, p, elements, accessType.getType(), mappings);
         idPropertyCounter += currentIdPropertyCounter;
      }

      return idPropertyCounter;
   }

   private static int addProperty(PropertyContainer propertyContainer, XProperty property, List annElts, String propertyAccessor, Mappings mappings) {
      XClass declaringClass = propertyContainer.getDeclaringClass();
      XClass entity = propertyContainer.getEntityAtStake();
      int idPropertyCounter = 0;
      PropertyData propertyAnnotatedElement = new PropertyInferredData(declaringClass, property, propertyAccessor, mappings.getReflectionManager());
      XAnnotatedElement element = propertyAnnotatedElement.getProperty();
      if (!element.isAnnotationPresent(Id.class) && !element.isAnnotationPresent(EmbeddedId.class)) {
         annElts.add(propertyAnnotatedElement);
      } else {
         annElts.add(0, propertyAnnotatedElement);
         if (mappings.isSpecjProprietarySyntaxEnabled() && element.isAnnotationPresent(Id.class) && element.isAnnotationPresent(Column.class)) {
            String columnName = ((Column)element.getAnnotation(Column.class)).name();

            for(XProperty prop : declaringClass.getDeclaredProperties(AccessType.FIELD.getType())) {
               if (!prop.isAnnotationPresent(MapsId.class)) {
                  boolean isRequiredAnnotationPresent = false;
                  JoinColumns groupAnnotation = (JoinColumns)prop.getAnnotation(JoinColumns.class);
                  if (prop.isAnnotationPresent(JoinColumn.class) && ((JoinColumn)prop.getAnnotation(JoinColumn.class)).name().equals(columnName)) {
                     isRequiredAnnotationPresent = true;
                  } else if (prop.isAnnotationPresent(JoinColumns.class)) {
                     for(JoinColumn columnAnnotation : groupAnnotation.value()) {
                        if (columnName.equals(columnAnnotation.name())) {
                           isRequiredAnnotationPresent = true;
                           break;
                        }
                     }
                  }

                  if (isRequiredAnnotationPresent) {
                     PropertyData specJPropertyData = new PropertyInferredData(declaringClass, prop, propertyAccessor, mappings.getReflectionManager());
                     mappings.addPropertyAnnotatedWithMapsIdSpecj(entity, specJPropertyData, element.toString());
                  }
               }
            }
         }

         if (element.isAnnotationPresent(ManyToOne.class) || element.isAnnotationPresent(OneToOne.class)) {
            mappings.addToOneAndIdProperty(entity, propertyAnnotatedElement);
         }

         ++idPropertyCounter;
      }

      if (element.isAnnotationPresent(MapsId.class)) {
         mappings.addPropertyAnnotatedWithMapsId(entity, propertyAnnotatedElement);
      }

      return idPropertyCounter;
   }

   private static void processElementAnnotations(PropertyHolder propertyHolder, Nullability nullability, PropertyData inferredData, HashMap classGenerators, EntityBinder entityBinder, boolean isIdentifierMapper, boolean isComponentEmbedded, boolean inSecondPass, Mappings mappings, Map inheritanceStatePerClass) throws MappingException {
      if (LOG.isTraceEnabled()) {
         LOG.tracev("Processing annotations of {0}.{1}", propertyHolder.getEntityName(), inferredData.getPropertyName());
      }

      XProperty property = inferredData.getProperty();
      if (property.isAnnotationPresent(Parent.class)) {
         if (propertyHolder.isComponent()) {
            propertyHolder.setParentProperty(property.getName());
         } else {
            throw new AnnotationException("@Parent cannot be applied outside an embeddable object: " + BinderHelper.getPath(propertyHolder, inferredData));
         }
      } else {
         ColumnsBuilder columnsBuilder = (new ColumnsBuilder(propertyHolder, nullability, property, inferredData, entityBinder, mappings)).extractMetadata();
         Ejb3Column[] columns = columnsBuilder.getColumns();
         Ejb3JoinColumn[] joinColumns = columnsBuilder.getJoinColumns();
         XClass returnedClass = inferredData.getClassOrElement();
         PropertyBinder propertyBinder = new PropertyBinder();
         propertyBinder.setName(inferredData.getPropertyName());
         propertyBinder.setReturnedClassName(inferredData.getTypeName());
         propertyBinder.setAccessType(inferredData.getDefaultAccess());
         propertyBinder.setHolder(propertyHolder);
         propertyBinder.setProperty(property);
         propertyBinder.setReturnedClass(inferredData.getPropertyClass());
         propertyBinder.setMappings(mappings);
         if (isIdentifierMapper) {
            propertyBinder.setInsertable(false);
            propertyBinder.setUpdatable(false);
         }

         propertyBinder.setDeclaringClass(inferredData.getDeclaringClass());
         propertyBinder.setEntityBinder(entityBinder);
         propertyBinder.setInheritanceStatePerClass(inheritanceStatePerClass);
         boolean isId = !entityBinder.isIgnoreIdAnnotations() && (property.isAnnotationPresent(Id.class) || property.isAnnotationPresent(EmbeddedId.class));
         propertyBinder.setId(isId);
         if (property.isAnnotationPresent(Version.class)) {
            if (isIdentifierMapper) {
               throw new AnnotationException("@IdClass class should not have @Version property");
            }

            if (!(propertyHolder.getPersistentClass() instanceof RootClass)) {
               throw new AnnotationException("Unable to define/override @Version on a subclass: " + propertyHolder.getEntityName());
            }

            if (!propertyHolder.isEntity()) {
               throw new AnnotationException("Unable to define @Version on an embedded class: " + propertyHolder.getEntityName());
            }

            if (LOG.isTraceEnabled()) {
               LOG.tracev("{0} is a version property", inferredData.getPropertyName());
            }

            RootClass rootClass = (RootClass)propertyHolder.getPersistentClass();
            propertyBinder.setColumns(columns);
            Property prop = propertyBinder.makePropertyValueAndBind();
            setVersionInformation(property, propertyBinder);
            rootClass.setVersion(prop);
            org.hibernate.mapping.MappedSuperclass superclass = BinderHelper.getMappedSuperclassOrNull(inferredData.getDeclaringClass(), inheritanceStatePerClass, mappings);
            if (superclass != null) {
               superclass.setDeclaredVersion(prop);
            } else {
               rootClass.setDeclaredVersion(prop);
            }

            SimpleValue simpleValue = (SimpleValue)prop.getValue();
            simpleValue.setNullValue("undefined");
            rootClass.setOptimisticLockMode(0);
            if (LOG.isTraceEnabled()) {
               LOG.tracev("Version name: {0}, unsavedValue: {1}", rootClass.getVersion().getName(), ((SimpleValue)rootClass.getVersion().getValue()).getNullValue());
            }
         } else {
            boolean forcePersist = property.isAnnotationPresent(MapsId.class) || property.isAnnotationPresent(Id.class);
            if (property.isAnnotationPresent(ManyToOne.class)) {
               ManyToOne ann = (ManyToOne)property.getAnnotation(ManyToOne.class);
               if (property.isAnnotationPresent(Column.class) || property.isAnnotationPresent(Columns.class)) {
                  throw new AnnotationException("@Column(s) not allowed on a @ManyToOne property: " + BinderHelper.getPath(propertyHolder, inferredData));
               }

               Cascade hibernateCascade = (Cascade)property.getAnnotation(Cascade.class);
               NotFound notFound = (NotFound)property.getAnnotation(NotFound.class);
               boolean ignoreNotFound = notFound != null && notFound.action().equals(NotFoundAction.IGNORE);
               OnDelete onDeleteAnn = (OnDelete)property.getAnnotation(OnDelete.class);
               boolean onDeleteCascade = onDeleteAnn != null && OnDeleteAction.CASCADE.equals(onDeleteAnn.action());
               JoinTable assocTable = propertyHolder.getJoinTable(property);
               if (assocTable != null) {
                  Join join = propertyHolder.addJoin(assocTable, false);

                  for(Ejb3JoinColumn joinColumn : joinColumns) {
                     joinColumn.setSecondaryTableName(join.getTable().getName());
                  }
               }

               boolean mandatory = !ann.optional() || forcePersist;
               bindManyToOne(getCascadeStrategy(ann.cascade(), hibernateCascade, false, forcePersist), joinColumns, !mandatory, ignoreNotFound, onDeleteCascade, ToOneBinder.getTargetEntity(inferredData, mappings), propertyHolder, inferredData, false, isIdentifierMapper, inSecondPass, propertyBinder, mappings);
            } else if (property.isAnnotationPresent(OneToOne.class)) {
               OneToOne ann = (OneToOne)property.getAnnotation(OneToOne.class);
               if (property.isAnnotationPresent(Column.class) || property.isAnnotationPresent(Columns.class)) {
                  throw new AnnotationException("@Column(s) not allowed on a @OneToOne property: " + BinderHelper.getPath(propertyHolder, inferredData));
               }

               boolean trueOneToOne = property.isAnnotationPresent(PrimaryKeyJoinColumn.class) || property.isAnnotationPresent(PrimaryKeyJoinColumns.class);
               Cascade hibernateCascade = (Cascade)property.getAnnotation(Cascade.class);
               NotFound notFound = (NotFound)property.getAnnotation(NotFound.class);
               boolean ignoreNotFound = notFound != null && notFound.action().equals(NotFoundAction.IGNORE);
               OnDelete onDeleteAnn = (OnDelete)property.getAnnotation(OnDelete.class);
               boolean onDeleteCascade = onDeleteAnn != null && OnDeleteAction.CASCADE.equals(onDeleteAnn.action());
               JoinTable assocTable = propertyHolder.getJoinTable(property);
               if (assocTable != null) {
                  Join join = propertyHolder.addJoin(assocTable, false);

                  for(Ejb3JoinColumn joinColumn : joinColumns) {
                     joinColumn.setSecondaryTableName(join.getTable().getName());
                  }
               }

               boolean mandatory = !ann.optional() || forcePersist;
               bindOneToOne(getCascadeStrategy(ann.cascade(), hibernateCascade, ann.orphanRemoval(), forcePersist), joinColumns, !mandatory, getFetchMode(ann.fetch()), ignoreNotFound, onDeleteCascade, ToOneBinder.getTargetEntity(inferredData, mappings), propertyHolder, inferredData, ann.mappedBy(), trueOneToOne, isIdentifierMapper, inSecondPass, propertyBinder, mappings);
            } else if (property.isAnnotationPresent(Any.class)) {
               if (property.isAnnotationPresent(Column.class) || property.isAnnotationPresent(Columns.class)) {
                  throw new AnnotationException("@Column(s) not allowed on a @Any property: " + BinderHelper.getPath(propertyHolder, inferredData));
               }

               Cascade hibernateCascade = (Cascade)property.getAnnotation(Cascade.class);
               OnDelete onDeleteAnn = (OnDelete)property.getAnnotation(OnDelete.class);
               boolean onDeleteCascade = onDeleteAnn != null && OnDeleteAction.CASCADE.equals(onDeleteAnn.action());
               JoinTable assocTable = propertyHolder.getJoinTable(property);
               if (assocTable != null) {
                  Join join = propertyHolder.addJoin(assocTable, false);

                  for(Ejb3JoinColumn joinColumn : joinColumns) {
                     joinColumn.setSecondaryTableName(join.getTable().getName());
                  }
               }

               bindAny(getCascadeStrategy((javax.persistence.CascadeType[])null, hibernateCascade, false, forcePersist), joinColumns, onDeleteCascade, nullability, propertyHolder, inferredData, entityBinder, isIdentifierMapper, mappings);
            } else if (!property.isAnnotationPresent(OneToMany.class) && !property.isAnnotationPresent(ManyToMany.class) && !property.isAnnotationPresent(ElementCollection.class) && !property.isAnnotationPresent(ManyToAny.class)) {
               if (!isId || !entityBinder.isIgnoreIdAnnotations()) {
                  boolean isComponent = false;
                  boolean isOverridden = false;
                  if (isId || propertyHolder.isOrWithinEmbeddedId() || propertyHolder.isInIdClass()) {
                     PropertyData overridingProperty = BinderHelper.getPropertyOverriddenByMapperOrMapsId(isId, propertyHolder, property.getName(), mappings);
                     if (overridingProperty != null) {
                        isOverridden = true;
                        InheritanceState state = (InheritanceState)inheritanceStatePerClass.get(overridingProperty.getClassOrElement());
                        if (state != null) {
                           isComponent = isComponent || state.hasIdClassOrEmbeddedId();
                        }

                        columns = columnsBuilder.overrideColumnFromMapperOrMapsIdProperty(isId);
                     }
                  }

                  isComponent = isComponent || property.isAnnotationPresent(Embedded.class) || property.isAnnotationPresent(EmbeddedId.class) || returnedClass.isAnnotationPresent(Embeddable.class);
                  if (isComponent) {
                     String referencedEntityName = null;
                     if (isOverridden) {
                        PropertyData mapsIdProperty = BinderHelper.getPropertyOverriddenByMapperOrMapsId(isId, propertyHolder, property.getName(), mappings);
                        referencedEntityName = mapsIdProperty.getClassOrElementName();
                     }

                     AccessType propertyAccessor = entityBinder.getPropertyAccessor(property);
                     propertyBinder = bindComponent(inferredData, propertyHolder, propertyAccessor, entityBinder, isIdentifierMapper, mappings, isComponentEmbedded, isId, inheritanceStatePerClass, referencedEntityName, isOverridden ? (Ejb3JoinColumn[])((Ejb3JoinColumn[])columns) : null);
                  } else {
                     boolean optional = true;
                     boolean lazy = false;
                     if (property.isAnnotationPresent(Basic.class)) {
                        Basic ann = (Basic)property.getAnnotation(Basic.class);
                        optional = ann.optional();
                        lazy = ann.fetch() == FetchType.LAZY;
                     }

                     if (isId || !optional && nullability != Nullability.FORCED_NULL) {
                        for(Ejb3Column col : columns) {
                           col.forceNotNull();
                        }
                     }

                     propertyBinder.setLazy(lazy);
                     propertyBinder.setColumns(columns);
                     if (isOverridden) {
                        PropertyData mapsIdProperty = BinderHelper.getPropertyOverriddenByMapperOrMapsId(isId, propertyHolder, property.getName(), mappings);
                        propertyBinder.setReferencedEntityName(mapsIdProperty.getClassOrElementName());
                     }

                     propertyBinder.makePropertyValueAndBind();
                  }

                  if (isOverridden) {
                     PropertyData mapsIdProperty = BinderHelper.getPropertyOverriddenByMapperOrMapsId(isId, propertyHolder, property.getName(), mappings);
                     Map<String, IdGenerator> localGenerators = (HashMap)classGenerators.clone();
                     IdGenerator foreignGenerator = new IdGenerator();
                     foreignGenerator.setIdentifierGeneratorStrategy("assigned");
                     foreignGenerator.setName("Hibernate-local--foreign generator");
                     foreignGenerator.setIdentifierGeneratorStrategy("foreign");
                     foreignGenerator.addParam("property", mapsIdProperty.getPropertyName());
                     localGenerators.put(foreignGenerator.getName(), foreignGenerator);
                     BinderHelper.makeIdGenerator((SimpleValue)propertyBinder.getValue(), foreignGenerator.getIdentifierGeneratorStrategy(), foreignGenerator.getName(), mappings, localGenerators);
                  }

                  if (isId) {
                     SimpleValue value = (SimpleValue)propertyBinder.getValue();
                     if (!isOverridden) {
                        processId(propertyHolder, inferredData, value, classGenerators, isIdentifierMapper, mappings);
                     }
                  }
               }
            } else {
               OneToMany oneToManyAnn = (OneToMany)property.getAnnotation(OneToMany.class);
               ManyToMany manyToManyAnn = (ManyToMany)property.getAnnotation(ManyToMany.class);
               ElementCollection elementCollectionAnn = (ElementCollection)property.getAnnotation(ElementCollection.class);
               IndexColumn indexColumn;
               if (property.isAnnotationPresent(OrderColumn.class)) {
                  indexColumn = IndexColumn.buildColumnFromAnnotation((OrderColumn)property.getAnnotation(OrderColumn.class), propertyHolder, inferredData, entityBinder.getSecondaryTables(), mappings);
               } else {
                  indexColumn = IndexColumn.buildColumnFromAnnotation((org.hibernate.annotations.IndexColumn)property.getAnnotation(org.hibernate.annotations.IndexColumn.class), propertyHolder, inferredData, mappings);
               }

               CollectionBinder collectionBinder = CollectionBinder.getCollectionBinder(propertyHolder.getEntityName(), property, !indexColumn.isImplicit(), property.isAnnotationPresent(MapKeyType.class), mappings);
               collectionBinder.setIndexColumn(indexColumn);
               collectionBinder.setMapKey((MapKey)property.getAnnotation(MapKey.class));
               collectionBinder.setPropertyName(inferredData.getPropertyName());
               BatchSize batchAnn = (BatchSize)property.getAnnotation(BatchSize.class);
               collectionBinder.setBatchSize(batchAnn);
               OrderBy ejb3OrderByAnn = (OrderBy)property.getAnnotation(OrderBy.class);
               org.hibernate.annotations.OrderBy orderByAnn = (org.hibernate.annotations.OrderBy)property.getAnnotation(org.hibernate.annotations.OrderBy.class);
               collectionBinder.setEjb3OrderBy(ejb3OrderByAnn);
               collectionBinder.setSqlOrderBy(orderByAnn);
               Sort sortAnn = (Sort)property.getAnnotation(Sort.class);
               collectionBinder.setSort(sortAnn);
               Cache cachAnn = (Cache)property.getAnnotation(Cache.class);
               collectionBinder.setCache(cachAnn);
               collectionBinder.setPropertyHolder(propertyHolder);
               Cascade hibernateCascade = (Cascade)property.getAnnotation(Cascade.class);
               NotFound notFound = (NotFound)property.getAnnotation(NotFound.class);
               boolean ignoreNotFound = notFound != null && notFound.action().equals(NotFoundAction.IGNORE);
               collectionBinder.setIgnoreNotFound(ignoreNotFound);
               collectionBinder.setCollectionType(inferredData.getProperty().getElementClass());
               collectionBinder.setMappings(mappings);
               collectionBinder.setAccessType(inferredData.getDefaultAccess());
               boolean isJPA2ForValueMapping = property.isAnnotationPresent(ElementCollection.class);
               PropertyData virtualProperty = (PropertyData)(isJPA2ForValueMapping ? inferredData : new WrappedInferredData(inferredData, "element"));
               Ejb3Column[] elementColumns;
               if (!property.isAnnotationPresent(Column.class) && !property.isAnnotationPresent(Formula.class)) {
                  if (property.isAnnotationPresent(Columns.class)) {
                     Columns anns = (Columns)property.getAnnotation(Columns.class);
                     elementColumns = Ejb3Column.buildColumnFromAnnotation(anns.columns(), (Formula)null, nullability, propertyHolder, virtualProperty, entityBinder.getSecondaryTables(), mappings);
                  } else {
                     elementColumns = Ejb3Column.buildColumnFromAnnotation((Column[])null, (Formula)null, nullability, propertyHolder, virtualProperty, entityBinder.getSecondaryTables(), mappings);
                  }
               } else {
                  Column ann = (Column)property.getAnnotation(Column.class);
                  Formula formulaAnn = (Formula)property.getAnnotation(Formula.class);
                  elementColumns = Ejb3Column.buildColumnFromAnnotation(new Column[]{ann}, formulaAnn, nullability, propertyHolder, virtualProperty, entityBinder.getSecondaryTables(), mappings);
               }

               Column[] keyColumns = null;
               Boolean isJPA2 = null;
               if (property.isAnnotationPresent(MapKeyColumn.class)) {
                  isJPA2 = Boolean.TRUE;
                  keyColumns = new Column[]{new MapKeyColumnDelegator((MapKeyColumn)property.getAnnotation(MapKeyColumn.class))};
               }

               if (isJPA2 == null) {
                  isJPA2 = Boolean.TRUE;
               }

               keyColumns = keyColumns != null && keyColumns.length > 0 ? keyColumns : null;
               PropertyData mapKeyVirtualProperty = new WrappedInferredData(inferredData, "mapkey");
               Ejb3Column[] mapColumns = Ejb3Column.buildColumnFromAnnotation(keyColumns, (Formula)null, Nullability.FORCED_NOT_NULL, propertyHolder, isJPA2 ? inferredData : mapKeyVirtualProperty, isJPA2 ? "_KEY" : null, entityBinder.getSecondaryTables(), mappings);
               collectionBinder.setMapKeyColumns(mapColumns);
               JoinColumn[] joinKeyColumns = null;
               isJPA2 = null;
               if (!property.isAnnotationPresent(MapKeyJoinColumns.class)) {
                  if (property.isAnnotationPresent(MapKeyJoinColumn.class)) {
                     isJPA2 = Boolean.TRUE;
                     joinKeyColumns = new JoinColumn[]{new MapKeyJoinColumnDelegator((MapKeyJoinColumn)property.getAnnotation(MapKeyJoinColumn.class))};
                  }
               } else {
                  isJPA2 = Boolean.TRUE;
                  MapKeyJoinColumn[] mapKeyJoinColumns = ((MapKeyJoinColumns)property.getAnnotation(MapKeyJoinColumns.class)).value();
                  joinKeyColumns = new JoinColumn[mapKeyJoinColumns.length];
                  int index = 0;

                  for(MapKeyJoinColumn joinColumn : mapKeyJoinColumns) {
                     joinKeyColumns[index] = new MapKeyJoinColumnDelegator(joinColumn);
                     ++index;
                  }

                  if (property.isAnnotationPresent(MapKeyJoinColumn.class)) {
                     throw new AnnotationException("@MapKeyJoinColumn and @MapKeyJoinColumns used on the same property: " + BinderHelper.getPath(propertyHolder, inferredData));
                  }
               }

               if (isJPA2 == null) {
                  isJPA2 = Boolean.TRUE;
               }

               mapKeyVirtualProperty = new WrappedInferredData(inferredData, "mapkey");
               Ejb3JoinColumn[] mapJoinColumns = Ejb3JoinColumn.buildJoinColumnsWithDefaultColumnSuffix(joinKeyColumns, (String)null, entityBinder.getSecondaryTables(), propertyHolder, isJPA2 ? inferredData.getPropertyName() : mapKeyVirtualProperty.getPropertyName(), isJPA2 ? "_KEY" : null, mappings);
               collectionBinder.setMapKeyManyToManyColumns(mapJoinColumns);
               collectionBinder.setEmbedded(property.isAnnotationPresent(Embedded.class));
               collectionBinder.setElementColumns(elementColumns);
               collectionBinder.setProperty(property);
               if (oneToManyAnn != null && manyToManyAnn != null) {
                  throw new AnnotationException("@OneToMany and @ManyToMany on the same property is not allowed: " + propertyHolder.getEntityName() + "." + inferredData.getPropertyName());
               }

               String mappedBy = null;
               if (oneToManyAnn != null) {
                  for(Ejb3JoinColumn column : joinColumns) {
                     if (column.isSecondary()) {
                        throw new NotYetImplementedException("Collections having FK in secondary table");
                     }
                  }

                  collectionBinder.setFkJoinColumns(joinColumns);
                  mappedBy = oneToManyAnn.mappedBy();
                  collectionBinder.setTargetEntity(mappings.getReflectionManager().toXClass(oneToManyAnn.targetEntity()));
                  collectionBinder.setCascadeStrategy(getCascadeStrategy(oneToManyAnn.cascade(), hibernateCascade, oneToManyAnn.orphanRemoval(), false));
                  collectionBinder.setOneToMany(true);
               } else if (elementCollectionAnn == null) {
                  if (manyToManyAnn != null) {
                     mappedBy = manyToManyAnn.mappedBy();
                     collectionBinder.setTargetEntity(mappings.getReflectionManager().toXClass(manyToManyAnn.targetEntity()));
                     collectionBinder.setCascadeStrategy(getCascadeStrategy(manyToManyAnn.cascade(), hibernateCascade, false, false));
                     collectionBinder.setOneToMany(false);
                  } else if (property.isAnnotationPresent(ManyToAny.class)) {
                     mappedBy = "";
                     collectionBinder.setTargetEntity(mappings.getReflectionManager().toXClass(Void.TYPE));
                     collectionBinder.setCascadeStrategy(getCascadeStrategy((javax.persistence.CascadeType[])null, hibernateCascade, false, false));
                     collectionBinder.setOneToMany(false);
                  }
               } else {
                  for(Ejb3JoinColumn column : joinColumns) {
                     if (column.isSecondary()) {
                        throw new NotYetImplementedException("Collections having FK in secondary table");
                     }
                  }

                  collectionBinder.setFkJoinColumns(joinColumns);
                  mappedBy = "";
                  Class<?> targetElement = elementCollectionAnn.targetClass();
                  collectionBinder.setTargetEntity(mappings.getReflectionManager().toXClass(targetElement));
                  collectionBinder.setOneToMany(true);
               }

               collectionBinder.setMappedBy(mappedBy);
               bindJoinedTableAssociation(property, mappings, entityBinder, collectionBinder, propertyHolder, inferredData, mappedBy);
               OnDelete onDeleteAnn = (OnDelete)property.getAnnotation(OnDelete.class);
               boolean onDeleteCascade = onDeleteAnn != null && OnDeleteAction.CASCADE.equals(onDeleteAnn.action());
               collectionBinder.setCascadeDeleteEnabled(onDeleteCascade);
               if (isIdentifierMapper) {
                  collectionBinder.setInsertable(false);
                  collectionBinder.setUpdatable(false);
               }

               if (property.isAnnotationPresent(CollectionId.class)) {
                  HashMap<String, IdGenerator> localGenerators = (HashMap)classGenerators.clone();
                  localGenerators.putAll(buildLocalGenerators(property, mappings));
                  collectionBinder.setLocalGenerators(localGenerators);
               }

               collectionBinder.setInheritanceStatePerClass(inheritanceStatePerClass);
               collectionBinder.setDeclaringClass(inferredData.getDeclaringClass());
               collectionBinder.bind();
            }
         }

         Index index = (Index)property.getAnnotation(Index.class);
         if (index != null) {
            if (joinColumns != null) {
               for(Ejb3Column column : joinColumns) {
                  column.addIndex(index, inSecondPass);
               }
            } else if (columns != null) {
               for(Ejb3Column column : columns) {
                  column.addIndex(index, inSecondPass);
               }
            }
         }

         NaturalId naturalIdAnn = (NaturalId)property.getAnnotation(NaturalId.class);
         if (naturalIdAnn != null) {
            if (joinColumns != null) {
               for(Ejb3Column column : joinColumns) {
                  column.addUniqueKey("_UniqueKey", inSecondPass);
               }
            } else {
               for(Ejb3Column column : columns) {
                  column.addUniqueKey("_UniqueKey", inSecondPass);
               }
            }
         }

      }
   }

   private static void setVersionInformation(XProperty property, PropertyBinder propertyBinder) {
      propertyBinder.getSimpleValueBinder().setVersion(true);
      if (property.isAnnotationPresent(Source.class)) {
         Source source = (Source)property.getAnnotation(Source.class);
         propertyBinder.getSimpleValueBinder().setTimestampVersionType(source.value().typeName());
      }

   }

   private static void processId(PropertyHolder propertyHolder, PropertyData inferredData, SimpleValue idValue, HashMap classGenerators, boolean isIdentifierMapper, Mappings mappings) {
      if (isIdentifierMapper) {
         throw new AnnotationException("@IdClass class should not have @Id nor @EmbeddedId properties: " + BinderHelper.getPath(propertyHolder, inferredData));
      } else {
         XClass returnedClass = inferredData.getClassOrElement();
         XProperty property = inferredData.getProperty();
         HashMap<String, IdGenerator> localGenerators = (HashMap)classGenerators.clone();
         localGenerators.putAll(buildLocalGenerators(property, mappings));
         boolean isComponent = returnedClass.isAnnotationPresent(Embeddable.class) || property.isAnnotationPresent(EmbeddedId.class);
         GeneratedValue generatedValue = (GeneratedValue)property.getAnnotation(GeneratedValue.class);
         String generatorType = generatedValue != null ? generatorType(generatedValue.strategy(), mappings) : "assigned";
         String generatorName = generatedValue != null ? generatedValue.generator() : "";
         if (isComponent) {
            generatorType = "assigned";
         }

         BinderHelper.makeIdGenerator(idValue, generatorType, generatorName, mappings, localGenerators);
         if (LOG.isTraceEnabled()) {
            LOG.tracev("Bind {0} on {1}", isComponent ? "@EmbeddedId" : "@Id", inferredData.getPropertyName());
         }

      }
   }

   private static void bindJoinedTableAssociation(XProperty property, Mappings mappings, EntityBinder entityBinder, CollectionBinder collectionBinder, PropertyHolder propertyHolder, PropertyData inferredData, String mappedBy) {
      TableBinder associationTableBinder = new TableBinder();
      JoinTable assocTable = propertyHolder.getJoinTable(property);
      CollectionTable collectionTable = (CollectionTable)property.getAnnotation(CollectionTable.class);
      JoinColumn[] annJoins;
      JoinColumn[] annInverseJoins;
      if (assocTable == null && collectionTable == null) {
         annJoins = null;
         annInverseJoins = null;
      } else {
         String catalog;
         String schema;
         String tableName;
         UniqueConstraint[] uniqueConstraints;
         JoinColumn[] joins;
         JoinColumn[] inverseJoins;
         if (collectionTable != null) {
            catalog = collectionTable.catalog();
            schema = collectionTable.schema();
            tableName = collectionTable.name();
            uniqueConstraints = collectionTable.uniqueConstraints();
            joins = collectionTable.joinColumns();
            inverseJoins = null;
         } else {
            catalog = assocTable.catalog();
            schema = assocTable.schema();
            tableName = assocTable.name();
            uniqueConstraints = assocTable.uniqueConstraints();
            joins = assocTable.joinColumns();
            inverseJoins = assocTable.inverseJoinColumns();
         }

         collectionBinder.setExplicitAssociationTable(true);
         if (!BinderHelper.isEmptyAnnotationValue(schema)) {
            associationTableBinder.setSchema(schema);
         }

         if (!BinderHelper.isEmptyAnnotationValue(catalog)) {
            associationTableBinder.setCatalog(catalog);
         }

         if (!BinderHelper.isEmptyAnnotationValue(tableName)) {
            associationTableBinder.setName(tableName);
         }

         associationTableBinder.setUniqueConstraints(uniqueConstraints);
         annJoins = joins.length == 0 ? null : joins;
         annInverseJoins = inverseJoins != null && inverseJoins.length != 0 ? inverseJoins : null;
      }

      Ejb3JoinColumn[] joinColumns = Ejb3JoinColumn.buildJoinTableJoinColumns(annJoins, entityBinder.getSecondaryTables(), propertyHolder, inferredData.getPropertyName(), mappedBy, mappings);
      Ejb3JoinColumn[] inverseJoinColumns = Ejb3JoinColumn.buildJoinTableJoinColumns(annInverseJoins, entityBinder.getSecondaryTables(), propertyHolder, inferredData.getPropertyName(), mappedBy, mappings);
      associationTableBinder.setMappings(mappings);
      collectionBinder.setTableBinder(associationTableBinder);
      collectionBinder.setJoinColumns(joinColumns);
      collectionBinder.setInverseJoinColumns(inverseJoinColumns);
   }

   private static PropertyBinder bindComponent(PropertyData inferredData, PropertyHolder propertyHolder, AccessType propertyAccessor, EntityBinder entityBinder, boolean isIdentifierMapper, Mappings mappings, boolean isComponentEmbedded, boolean isId, Map inheritanceStatePerClass, String referencedEntityName, Ejb3JoinColumn[] columns) {
      Component comp;
      if (referencedEntityName != null) {
         comp = createComponent(propertyHolder, inferredData, isComponentEmbedded, isIdentifierMapper, mappings);
         SecondPass sp = new CopyIdentifierComponentSecondPass(comp, referencedEntityName, columns, mappings);
         mappings.addSecondPass(sp);
      } else {
         comp = fillComponent(propertyHolder, inferredData, propertyAccessor, !isId, entityBinder, isComponentEmbedded, isIdentifierMapper, false, mappings, inheritanceStatePerClass);
      }

      if (isId) {
         comp.setKey(true);
         if (propertyHolder.getPersistentClass().getIdentifier() != null) {
            throw new AnnotationException(comp.getComponentClassName() + " must not have @Id properties when used as an @EmbeddedId: " + BinderHelper.getPath(propertyHolder, inferredData));
         }

         if (referencedEntityName == null && comp.getPropertySpan() == 0) {
            throw new AnnotationException(comp.getComponentClassName() + " has no persistent id property: " + BinderHelper.getPath(propertyHolder, inferredData));
         }
      }

      XProperty property = inferredData.getProperty();
      setupComponentTuplizer(property, comp);
      PropertyBinder binder = new PropertyBinder();
      binder.setName(inferredData.getPropertyName());
      binder.setValue(comp);
      binder.setProperty(inferredData.getProperty());
      binder.setAccessType(inferredData.getDefaultAccess());
      binder.setEmbedded(isComponentEmbedded);
      binder.setHolder(propertyHolder);
      binder.setId(isId);
      binder.setEntityBinder(entityBinder);
      binder.setInheritanceStatePerClass(inheritanceStatePerClass);
      binder.setMappings(mappings);
      binder.makePropertyAndBind();
      return binder;
   }

   public static Component fillComponent(PropertyHolder propertyHolder, PropertyData inferredData, AccessType propertyAccessor, boolean isNullable, EntityBinder entityBinder, boolean isComponentEmbedded, boolean isIdentifierMapper, boolean inSecondPass, Mappings mappings, Map inheritanceStatePerClass) {
      return fillComponent(propertyHolder, inferredData, (PropertyData)null, propertyAccessor, isNullable, entityBinder, isComponentEmbedded, isIdentifierMapper, inSecondPass, mappings, inheritanceStatePerClass);
   }

   public static Component fillComponent(PropertyHolder propertyHolder, PropertyData inferredData, PropertyData baseInferredData, AccessType propertyAccessor, boolean isNullable, EntityBinder entityBinder, boolean isComponentEmbedded, boolean isIdentifierMapper, boolean inSecondPass, Mappings mappings, Map inheritanceStatePerClass) {
      Component comp = createComponent(propertyHolder, inferredData, isComponentEmbedded, isIdentifierMapper, mappings);
      String subpath = BinderHelper.getPath(propertyHolder, inferredData);
      LOG.tracev("Binding component with path: {0}", subpath);
      PropertyHolder subHolder = PropertyHolderBuilder.buildPropertyHolder(comp, subpath, inferredData, propertyHolder, mappings);
      XClass xClassProcessed = inferredData.getPropertyClass();
      List<PropertyData> classElements = new ArrayList();
      XClass returnedClassOrElement = inferredData.getClassOrElement();
      List<PropertyData> baseClassElements = null;
      Map<String, PropertyData> orderedBaseClassElements = new HashMap();
      if (baseInferredData != null) {
         baseClassElements = new ArrayList();
         XClass baseReturnedClassOrElement = baseInferredData.getClassOrElement();
         bindTypeDefs(baseReturnedClassOrElement, mappings);
         PropertyContainer propContainer = new PropertyContainer(baseReturnedClassOrElement, xClassProcessed);
         addElementsOfClass(baseClassElements, propertyAccessor, propContainer, mappings);

         for(PropertyData element : baseClassElements) {
            orderedBaseClassElements.put(element.getPropertyName(), element);
         }
      }

      bindTypeDefs(returnedClassOrElement, mappings);
      PropertyContainer propContainer = new PropertyContainer(returnedClassOrElement, xClassProcessed);
      addElementsOfClass(classElements, propertyAccessor, propContainer, mappings);

      for(XClass superClass = xClassProcessed.getSuperclass(); superClass != null && superClass.isAnnotationPresent(MappedSuperclass.class); superClass = superClass.getSuperclass()) {
         propContainer = new PropertyContainer(superClass, xClassProcessed);
         addElementsOfClass(classElements, propertyAccessor, propContainer, mappings);
      }

      if (baseClassElements != null && !hasAnnotationsOnIdClass(xClassProcessed)) {
         for(int i = 0; i < classElements.size(); ++i) {
            PropertyData idClassPropertyData = (PropertyData)classElements.get(i);
            PropertyData entityPropertyData = (PropertyData)orderedBaseClassElements.get(idClassPropertyData.getPropertyName());
            if (propertyHolder.isInIdClass()) {
               if (entityPropertyData == null) {
                  throw new AnnotationException("Property of @IdClass not found in entity " + baseInferredData.getPropertyClass().getName() + ": " + idClassPropertyData.getPropertyName());
               }

               boolean hasXToOneAnnotation = entityPropertyData.getProperty().isAnnotationPresent(ManyToOne.class) || entityPropertyData.getProperty().isAnnotationPresent(OneToOne.class);
               boolean isOfDifferentType = !entityPropertyData.getClassOrElement().equals(idClassPropertyData.getClassOrElement());
               if (!hasXToOneAnnotation || !isOfDifferentType) {
                  classElements.set(i, entityPropertyData);
               }
            } else {
               classElements.set(i, entityPropertyData);
            }
         }
      }

      for(PropertyData propertyAnnotatedElement : classElements) {
         processElementAnnotations(subHolder, isNullable ? Nullability.NO_CONSTRAINT : Nullability.FORCED_NOT_NULL, propertyAnnotatedElement, new HashMap(), entityBinder, isIdentifierMapper, isComponentEmbedded, inSecondPass, mappings, inheritanceStatePerClass);
         XProperty property = propertyAnnotatedElement.getProperty();
         if (property.isAnnotationPresent(GeneratedValue.class) && property.isAnnotationPresent(Id.class)) {
            Map<String, IdGenerator> localGenerators = new HashMap();
            localGenerators.putAll(buildLocalGenerators(property, mappings));
            GeneratedValue generatedValue = (GeneratedValue)property.getAnnotation(GeneratedValue.class);
            String generatorType = generatedValue != null ? generatorType(generatedValue.strategy(), mappings) : "assigned";
            String generator = generatedValue != null ? generatedValue.generator() : "";
            BinderHelper.makeIdGenerator((SimpleValue)comp.getProperty(property.getName()).getValue(), generatorType, generator, mappings, localGenerators);
         }
      }

      return comp;
   }

   public static Component createComponent(PropertyHolder propertyHolder, PropertyData inferredData, boolean isComponentEmbedded, boolean isIdentifierMapper, Mappings mappings) {
      Component comp = new Component(mappings, propertyHolder.getPersistentClass());
      comp.setEmbedded(isComponentEmbedded);
      comp.setTable(propertyHolder.getTable());
      if (!isIdentifierMapper && (!isComponentEmbedded || inferredData.getPropertyName() != null)) {
         comp.setComponentClassName(inferredData.getClassOrElementName());
      } else {
         comp.setComponentClassName(comp.getOwner().getClassName());
      }

      comp.setNodeName(inferredData.getPropertyName());
      return comp;
   }

   private static void bindIdClass(String generatorType, String generatorName, PropertyData inferredData, PropertyData baseInferredData, Ejb3Column[] columns, PropertyHolder propertyHolder, boolean isComposite, AccessType propertyAccessor, EntityBinder entityBinder, boolean isEmbedded, boolean isIdentifierMapper, Mappings mappings, Map inheritanceStatePerClass) {
      PersistentClass persistentClass = propertyHolder.getPersistentClass();
      if (!(persistentClass instanceof RootClass)) {
         throw new AnnotationException("Unable to define/override @Id(s) on a subclass: " + propertyHolder.getEntityName());
      } else {
         RootClass rootClass = (RootClass)persistentClass;
         String persistentClassName = rootClass.getClassName();
         String propertyName = inferredData.getPropertyName();
         HashMap<String, IdGenerator> localGenerators = new HashMap();
         SimpleValue id;
         if (isComposite) {
            id = fillComponent(propertyHolder, inferredData, baseInferredData, propertyAccessor, false, entityBinder, isEmbedded, isIdentifierMapper, false, mappings, inheritanceStatePerClass);
            Component componentId = (Component)id;
            componentId.setKey(true);
            if (rootClass.getIdentifier() != null) {
               throw new AnnotationException(componentId.getComponentClassName() + " must not have @Id properties when used as an @EmbeddedId");
            }

            if (componentId.getPropertySpan() == 0) {
               throw new AnnotationException(componentId.getComponentClassName() + " has no persistent id property");
            }

            XProperty property = inferredData.getProperty();
            setupComponentTuplizer(property, componentId);
         } else {
            for(Ejb3Column column : columns) {
               column.forceNotNull();
            }

            SimpleValueBinder value = new SimpleValueBinder();
            value.setPropertyName(propertyName);
            value.setReturnedClassName(inferredData.getTypeName());
            value.setColumns(columns);
            value.setPersistentClassName(persistentClassName);
            value.setMappings(mappings);
            value.setType(inferredData.getProperty(), inferredData.getClassOrElement(), persistentClassName);
            value.setAccessType(propertyAccessor);
            id = value.make();
         }

         rootClass.setIdentifier(id);
         BinderHelper.makeIdGenerator(id, generatorType, generatorName, mappings, localGenerators);
         if (isEmbedded) {
            rootClass.setEmbeddedIdentifier(inferredData.getPropertyClass() == null);
         } else {
            PropertyBinder binder = new PropertyBinder();
            binder.setName(propertyName);
            binder.setValue(id);
            binder.setAccessType(inferredData.getDefaultAccess());
            binder.setProperty(inferredData.getProperty());
            Property prop = binder.makeProperty();
            rootClass.setIdentifierProperty(prop);
            org.hibernate.mapping.MappedSuperclass superclass = BinderHelper.getMappedSuperclassOrNull(inferredData.getDeclaringClass(), inheritanceStatePerClass, mappings);
            if (superclass != null) {
               superclass.setDeclaredIdentifierProperty(prop);
            } else {
               rootClass.setDeclaredIdentifierProperty(prop);
            }
         }

      }
   }

   private static PropertyData getUniqueIdPropertyFromBaseClass(PropertyData inferredData, PropertyData baseInferredData, AccessType propertyAccessor, Mappings mappings) {
      List<PropertyData> baseClassElements = new ArrayList();
      XClass baseReturnedClassOrElement = baseInferredData.getClassOrElement();
      PropertyContainer propContainer = new PropertyContainer(baseReturnedClassOrElement, inferredData.getPropertyClass());
      addElementsOfClass(baseClassElements, propertyAccessor, propContainer, mappings);
      return (PropertyData)baseClassElements.get(0);
   }

   private static void setupComponentTuplizer(XProperty property, Component component) {
      if (property != null) {
         if (property.isAnnotationPresent(Tuplizers.class)) {
            for(Tuplizer tuplizer : ((Tuplizers)property.getAnnotation(Tuplizers.class)).value()) {
               EntityMode mode = EntityMode.parse(tuplizer.entityMode());
               component.addTuplizer(mode, tuplizer.impl().getName());
            }
         }

         if (property.isAnnotationPresent(Tuplizer.class)) {
            Tuplizer tuplizer = (Tuplizer)property.getAnnotation(Tuplizer.class);
            EntityMode mode = EntityMode.parse(tuplizer.entityMode());
            component.addTuplizer(mode, tuplizer.impl().getName());
         }

      }
   }

   private static void bindManyToOne(String cascadeStrategy, Ejb3JoinColumn[] columns, boolean optional, boolean ignoreNotFound, boolean cascadeOnDelete, XClass targetEntity, PropertyHolder propertyHolder, PropertyData inferredData, boolean unique, boolean isIdentifierMapper, boolean inSecondPass, PropertyBinder propertyBinder, Mappings mappings) {
      org.hibernate.mapping.ManyToOne value = new org.hibernate.mapping.ManyToOne(mappings, columns[0].getTable());
      if (unique) {
         value.markAsLogicalOneToOne();
      }

      value.setReferencedEntityName(ToOneBinder.getReferenceEntityName(inferredData, targetEntity, mappings));
      XProperty property = inferredData.getProperty();
      defineFetchingStrategy(value, property);
      value.setIgnoreNotFound(ignoreNotFound);
      value.setCascadeDeleteEnabled(cascadeOnDelete);
      if (!optional) {
         for(Ejb3JoinColumn column : columns) {
            column.setNullable(false);
         }
      }

      if (property.isAnnotationPresent(MapsId.class)) {
         for(Ejb3JoinColumn column : columns) {
            column.setInsertable(false);
            column.setUpdatable(false);
         }
      }

      boolean hasSpecjManyToOne = false;
      if (mappings.isSpecjProprietarySyntaxEnabled()) {
         String columnName = "";

         for(XProperty prop : inferredData.getDeclaringClass().getDeclaredProperties(AccessType.FIELD.getType())) {
            if (prop.isAnnotationPresent(Id.class) && prop.isAnnotationPresent(Column.class)) {
               columnName = ((Column)prop.getAnnotation(Column.class)).name();
            }

            JoinColumn joinColumn = (JoinColumn)property.getAnnotation(JoinColumn.class);
            if (property.isAnnotationPresent(ManyToOne.class) && joinColumn != null && !BinderHelper.isEmptyAnnotationValue(joinColumn.name()) && joinColumn.name().equals(columnName) && !property.isAnnotationPresent(MapsId.class)) {
               hasSpecjManyToOne = true;

               for(Ejb3JoinColumn column : columns) {
                  column.setInsertable(false);
                  column.setUpdatable(false);
               }
            }
         }
      }

      value.setTypeName(inferredData.getClassOrElementName());
      String propertyName = inferredData.getPropertyName();
      value.setTypeUsingReflection(propertyHolder.getClassName(), propertyName);
      ForeignKey fk = (ForeignKey)property.getAnnotation(ForeignKey.class);
      String fkName = fk != null ? fk.name() : "";
      if (!BinderHelper.isEmptyAnnotationValue(fkName)) {
         value.setForeignKeyName(fkName);
      }

      String path = propertyHolder.getPath() + "." + propertyName;
      FkSecondPass secondPass = new ToOneFkSecondPass(value, columns, !optional && unique, propertyHolder.getEntityOwnerClassName(), path, mappings);
      if (inSecondPass) {
         secondPass.doSecondPass(mappings.getClasses());
      } else {
         mappings.addSecondPass(secondPass);
      }

      Ejb3Column.checkPropertyConsistency(columns, propertyHolder.getEntityName() + propertyName);
      propertyBinder.setName(propertyName);
      propertyBinder.setValue(value);
      if (isIdentifierMapper) {
         propertyBinder.setInsertable(false);
         propertyBinder.setUpdatable(false);
      } else if (hasSpecjManyToOne) {
         propertyBinder.setInsertable(false);
         propertyBinder.setUpdatable(false);
      } else {
         propertyBinder.setInsertable(columns[0].isInsertable());
         propertyBinder.setUpdatable(columns[0].isUpdatable());
      }

      propertyBinder.setColumns(columns);
      propertyBinder.setAccessType(inferredData.getDefaultAccess());
      propertyBinder.setCascade(cascadeStrategy);
      propertyBinder.setProperty(property);
      propertyBinder.setXToMany(true);
      propertyBinder.makePropertyAndBind();
   }

   protected static void defineFetchingStrategy(ToOne toOne, XProperty property) {
      LazyToOne lazy = (LazyToOne)property.getAnnotation(LazyToOne.class);
      Fetch fetch = (Fetch)property.getAnnotation(Fetch.class);
      ManyToOne manyToOne = (ManyToOne)property.getAnnotation(ManyToOne.class);
      OneToOne oneToOne = (OneToOne)property.getAnnotation(OneToOne.class);
      FetchType fetchType;
      if (manyToOne != null) {
         fetchType = manyToOne.fetch();
      } else {
         if (oneToOne == null) {
            throw new AssertionFailure("Define fetch strategy on a property not annotated with @OneToMany nor @OneToOne");
         }

         fetchType = oneToOne.fetch();
      }

      if (lazy != null) {
         toOne.setLazy(lazy.value() != LazyToOneOption.FALSE);
         toOne.setUnwrapProxy(lazy.value() == LazyToOneOption.NO_PROXY);
      } else {
         toOne.setLazy(fetchType == FetchType.LAZY);
         toOne.setUnwrapProxy(false);
      }

      if (fetch != null) {
         if (fetch.value() == FetchMode.JOIN) {
            toOne.setFetchMode(org.hibernate.FetchMode.JOIN);
            toOne.setLazy(false);
            toOne.setUnwrapProxy(false);
         } else {
            if (fetch.value() != FetchMode.SELECT) {
               if (fetch.value() == FetchMode.SUBSELECT) {
                  throw new AnnotationException("Use of FetchMode.SUBSELECT not allowed on ToOne associations");
               }

               throw new AssertionFailure("Unknown FetchMode: " + fetch.value());
            }

            toOne.setFetchMode(org.hibernate.FetchMode.SELECT);
         }
      } else {
         toOne.setFetchMode(getFetchMode(fetchType));
      }

   }

   private static void bindOneToOne(String cascadeStrategy, Ejb3JoinColumn[] joinColumns, boolean optional, org.hibernate.FetchMode fetchMode, boolean ignoreNotFound, boolean cascadeOnDelete, XClass targetEntity, PropertyHolder propertyHolder, PropertyData inferredData, String mappedBy, boolean trueOneToOne, boolean isIdentifierMapper, boolean inSecondPass, PropertyBinder propertyBinder, Mappings mappings) {
      String propertyName = inferredData.getPropertyName();
      LOG.tracev("Fetching {0} with {1}", propertyName, fetchMode);
      boolean mapToPK = true;
      if (!trueOneToOne) {
         KeyValue identifier = propertyHolder.getIdentifier();
         if (identifier == null) {
            mapToPK = false;
         } else {
            Iterator idColumns = identifier.getColumnIterator();
            List<String> idColumnNames = new ArrayList();
            if (identifier.getColumnSpan() != joinColumns.length) {
               mapToPK = false;
            } else {
               while(idColumns.hasNext()) {
                  org.hibernate.mapping.Column currentColumn = (org.hibernate.mapping.Column)idColumns.next();
                  idColumnNames.add(currentColumn.getName());
               }

               for(Ejb3JoinColumn col : joinColumns) {
                  if (!idColumnNames.contains(col.getMappingColumn().getName())) {
                     mapToPK = false;
                     break;
                  }
               }
            }
         }
      }

      if (!trueOneToOne && !mapToPK && BinderHelper.isEmptyAnnotationValue(mappedBy)) {
         bindManyToOne(cascadeStrategy, joinColumns, optional, ignoreNotFound, cascadeOnDelete, targetEntity, propertyHolder, inferredData, true, isIdentifierMapper, inSecondPass, propertyBinder, mappings);
      } else {
         OneToOneSecondPass secondPass = new OneToOneSecondPass(mappedBy, propertyHolder.getEntityName(), propertyName, propertyHolder, inferredData, targetEntity, ignoreNotFound, cascadeOnDelete, optional, cascadeStrategy, joinColumns, mappings);
         if (inSecondPass) {
            secondPass.doSecondPass(mappings.getClasses());
         } else {
            mappings.addSecondPass(secondPass, BinderHelper.isEmptyAnnotationValue(mappedBy));
         }
      }

   }

   private static void bindAny(String cascadeStrategy, Ejb3JoinColumn[] columns, boolean cascadeOnDelete, Nullability nullability, PropertyHolder propertyHolder, PropertyData inferredData, EntityBinder entityBinder, boolean isIdentifierMapper, Mappings mappings) {
      Any anyAnn = (Any)inferredData.getProperty().getAnnotation(Any.class);
      if (anyAnn == null) {
         throw new AssertionFailure("Missing @Any annotation: " + BinderHelper.getPath(propertyHolder, inferredData));
      } else {
         org.hibernate.mapping.Any value = BinderHelper.buildAnyValue(anyAnn.metaDef(), columns, anyAnn.metaColumn(), inferredData, cascadeOnDelete, nullability, propertyHolder, entityBinder, anyAnn.optional(), mappings);
         PropertyBinder binder = new PropertyBinder();
         binder.setName(inferredData.getPropertyName());
         binder.setValue(value);
         binder.setLazy(anyAnn.fetch() == FetchType.LAZY);
         if (isIdentifierMapper) {
            binder.setInsertable(false);
            binder.setUpdatable(false);
         } else {
            binder.setInsertable(columns[0].isInsertable());
            binder.setUpdatable(columns[0].isUpdatable());
         }

         binder.setAccessType(inferredData.getDefaultAccess());
         binder.setCascade(cascadeStrategy);
         Property prop = binder.makeProperty();
         propertyHolder.addProperty(prop, columns, inferredData.getDeclaringClass());
      }
   }

   private static String generatorType(GenerationType generatorEnum, Mappings mappings) {
      boolean useNewGeneratorMappings = mappings.useNewGeneratorMappings();
      switch (generatorEnum) {
         case IDENTITY:
            return "identity";
         case AUTO:
            return useNewGeneratorMappings ? SequenceStyleGenerator.class.getName() : "native";
         case TABLE:
            return useNewGeneratorMappings ? org.hibernate.id.enhanced.TableGenerator.class.getName() : MultipleHiLoPerTableGenerator.class.getName();
         case SEQUENCE:
            return useNewGeneratorMappings ? SequenceStyleGenerator.class.getName() : "seqhilo";
         default:
            throw new AssertionFailure("Unknown GeneratorType: " + generatorEnum);
      }
   }

   private static EnumSet convertToHibernateCascadeType(javax.persistence.CascadeType[] ejbCascades) {
      EnumSet<CascadeType> hibernateCascadeSet = EnumSet.noneOf(CascadeType.class);
      if (ejbCascades != null && ejbCascades.length > 0) {
         for(javax.persistence.CascadeType cascade : ejbCascades) {
            switch (cascade) {
               case ALL:
                  hibernateCascadeSet.add(CascadeType.ALL);
                  break;
               case PERSIST:
                  hibernateCascadeSet.add(CascadeType.PERSIST);
                  break;
               case MERGE:
                  hibernateCascadeSet.add(CascadeType.MERGE);
                  break;
               case REMOVE:
                  hibernateCascadeSet.add(CascadeType.REMOVE);
                  break;
               case REFRESH:
                  hibernateCascadeSet.add(CascadeType.REFRESH);
                  break;
               case DETACH:
                  hibernateCascadeSet.add(CascadeType.DETACH);
            }
         }
      }

      return hibernateCascadeSet;
   }

   private static String getCascadeStrategy(javax.persistence.CascadeType[] ejbCascades, Cascade hibernateCascadeAnnotation, boolean orphanRemoval, boolean forcePersist) {
      EnumSet<CascadeType> hibernateCascadeSet = convertToHibernateCascadeType(ejbCascades);
      CascadeType[] hibernateCascades = hibernateCascadeAnnotation == null ? null : hibernateCascadeAnnotation.value();
      if (hibernateCascades != null && hibernateCascades.length > 0) {
         hibernateCascadeSet.addAll(Arrays.asList(hibernateCascades));
      }

      if (orphanRemoval) {
         hibernateCascadeSet.add(CascadeType.DELETE_ORPHAN);
         hibernateCascadeSet.add(CascadeType.REMOVE);
      }

      if (forcePersist) {
         hibernateCascadeSet.add(CascadeType.PERSIST);
      }

      StringBuilder cascade = new StringBuilder();

      for(CascadeType aHibernateCascadeSet : hibernateCascadeSet) {
         switch (aHibernateCascadeSet) {
            case ALL:
               cascade.append(",").append("all");
               break;
            case SAVE_UPDATE:
               cascade.append(",").append("save-update");
               break;
            case PERSIST:
               cascade.append(",").append("persist");
               break;
            case MERGE:
               cascade.append(",").append("merge");
               break;
            case LOCK:
               cascade.append(",").append("lock");
               break;
            case REFRESH:
               cascade.append(",").append("refresh");
               break;
            case REPLICATE:
               cascade.append(",").append("replicate");
               break;
            case EVICT:
            case DETACH:
               cascade.append(",").append("evict");
               break;
            case DELETE:
               cascade.append(",").append("delete");
               break;
            case DELETE_ORPHAN:
               cascade.append(",").append("delete-orphan");
               break;
            case REMOVE:
               cascade.append(",").append("delete");
         }
      }

      return cascade.length() > 0 ? cascade.substring(1) : "none";
   }

   public static org.hibernate.FetchMode getFetchMode(FetchType fetch) {
      return fetch == FetchType.EAGER ? org.hibernate.FetchMode.JOIN : org.hibernate.FetchMode.SELECT;
   }

   private static HashMap buildLocalGenerators(XAnnotatedElement annElt, Mappings mappings) {
      HashMap<String, IdGenerator> generators = new HashMap();
      TableGenerator tabGen = (TableGenerator)annElt.getAnnotation(TableGenerator.class);
      SequenceGenerator seqGen = (SequenceGenerator)annElt.getAnnotation(SequenceGenerator.class);
      GenericGenerator genGen = (GenericGenerator)annElt.getAnnotation(GenericGenerator.class);
      if (tabGen != null) {
         IdGenerator idGen = buildIdGenerator(tabGen, mappings);
         generators.put(idGen.getName(), idGen);
      }

      if (seqGen != null) {
         IdGenerator idGen = buildIdGenerator(seqGen, mappings);
         generators.put(idGen.getName(), idGen);
      }

      if (genGen != null) {
         IdGenerator idGen = buildIdGenerator(genGen, mappings);
         generators.put(idGen.getName(), idGen);
      }

      return generators;
   }

   public static boolean isDefault(XClass clazz, Mappings mappings) {
      return mappings.getReflectionManager().equals(clazz, Void.TYPE);
   }

   public static Map buildInheritanceStates(List orderedClasses, Mappings mappings) {
      ReflectionManager reflectionManager = mappings.getReflectionManager();
      Map<XClass, InheritanceState> inheritanceStatePerClass = new HashMap(orderedClasses.size());

      for(XClass clazz : orderedClasses) {
         InheritanceState superclassState = InheritanceState.getSuperclassInheritanceState(clazz, inheritanceStatePerClass);
         InheritanceState state = new InheritanceState(clazz, inheritanceStatePerClass, mappings);
         if (superclassState != null) {
            superclassState.setHasSiblings(true);
            InheritanceState superEntityState = InheritanceState.getInheritanceStateOfSuperEntity(clazz, inheritanceStatePerClass);
            state.setHasParents(superEntityState != null);
            boolean nonDefault = state.getType() != null && !InheritanceType.SINGLE_TABLE.equals(state.getType());
            if (superclassState.getType() != null) {
               boolean mixingStrategy = state.getType() != null && !state.getType().equals(superclassState.getType());
               if (nonDefault && mixingStrategy) {
                  LOG.invalidSubStrategy(clazz.getName());
               }

               state.setType(superclassState.getType());
            }
         }

         inheritanceStatePerClass.put(clazz, state);
      }

      return inheritanceStatePerClass;
   }

   private static boolean hasAnnotationsOnIdClass(XClass idClass) {
      for(XProperty property : idClass.getDeclaredProperties("field")) {
         if (property.isAnnotationPresent(Column.class) || property.isAnnotationPresent(OneToMany.class) || property.isAnnotationPresent(ManyToOne.class) || property.isAnnotationPresent(Id.class) || property.isAnnotationPresent(GeneratedValue.class) || property.isAnnotationPresent(OneToOne.class) || property.isAnnotationPresent(ManyToMany.class)) {
            return true;
         }
      }

      for(XMethod method : idClass.getDeclaredMethods()) {
         if (method.isAnnotationPresent(Column.class) || method.isAnnotationPresent(OneToMany.class) || method.isAnnotationPresent(ManyToOne.class) || method.isAnnotationPresent(Id.class) || method.isAnnotationPresent(GeneratedValue.class) || method.isAnnotationPresent(OneToOne.class) || method.isAnnotationPresent(ManyToMany.class)) {
            return true;
         }
      }

      return false;
   }

   private static class LocalCacheAnnotationImpl implements Cache {
      private final String region;
      private final CacheConcurrencyStrategy usage;

      private LocalCacheAnnotationImpl(String region, CacheConcurrencyStrategy usage) {
         this.region = region;
         this.usage = usage;
      }

      public CacheConcurrencyStrategy usage() {
         return this.usage;
      }

      public String region() {
         return this.region;
      }

      public String include() {
         return "all";
      }

      public Class annotationType() {
         return Cache.class;
      }
   }
}
