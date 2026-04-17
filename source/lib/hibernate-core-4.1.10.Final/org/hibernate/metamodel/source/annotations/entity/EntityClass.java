package org.hibernate.metamodel.source.annotations.entity;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.AccessType;
import javax.persistence.DiscriminatorType;
import javax.persistence.PersistenceException;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import org.hibernate.AnnotationException;
import org.hibernate.MappingException;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.OptimisticLockType;
import org.hibernate.annotations.PolymorphismType;
import org.hibernate.engine.OptimisticLockStyle;
import org.hibernate.engine.spi.ExecuteUpdateResultCheckStyle;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.metamodel.binding.Caching;
import org.hibernate.metamodel.binding.CustomSQL;
import org.hibernate.metamodel.binding.InheritanceType;
import org.hibernate.metamodel.source.annotations.AnnotationBindingContext;
import org.hibernate.metamodel.source.annotations.HibernateDotNames;
import org.hibernate.metamodel.source.annotations.JPADotNames;
import org.hibernate.metamodel.source.annotations.JandexHelper;
import org.hibernate.metamodel.source.annotations.attribute.ColumnValues;
import org.hibernate.metamodel.source.annotations.attribute.FormulaValue;
import org.hibernate.metamodel.source.annotations.xml.PseudoJpaDotNames;
import org.hibernate.metamodel.source.binder.JpaCallbackClass;
import org.hibernate.metamodel.source.binder.TableSource;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.jandex.Type.Kind;

public class EntityClass extends ConfiguredClass {
   private final IdType idType;
   private final InheritanceType inheritanceType;
   private final String explicitEntityName;
   private final String customLoaderQueryName;
   private final List synchronizedTableNames;
   private final int batchSize;
   private final TableSource primaryTableSource;
   private final Set secondaryTableSources;
   private final Set constraintSources;
   private boolean isMutable;
   private boolean isExplicitPolymorphism;
   private OptimisticLockStyle optimisticLockStyle;
   private String whereClause;
   private String rowId;
   private Caching caching;
   private boolean isDynamicInsert;
   private boolean isDynamicUpdate;
   private boolean isSelectBeforeUpdate;
   private String customPersister;
   private CustomSQL customInsert;
   private CustomSQL customUpdate;
   private CustomSQL customDelete;
   private boolean isLazy;
   private String proxy;
   private ColumnValues discriminatorColumnValues;
   private FormulaValue discriminatorFormula;
   private Class discriminatorType;
   private String discriminatorMatchValue;
   private boolean isDiscriminatorForced = true;
   private boolean isDiscriminatorIncludedInSql = true;
   private final List jpaCallbacks;

   public EntityClass(ClassInfo classInfo, EntityClass parent, AccessType hierarchyAccessType, InheritanceType inheritanceType, AnnotationBindingContext context) {
      super(classInfo, hierarchyAccessType, parent, context);
      this.inheritanceType = inheritanceType;
      this.idType = this.determineIdType();
      boolean hasOwnTable = this.definesItsOwnTable();
      this.explicitEntityName = this.determineExplicitEntityName();
      this.constraintSources = new HashSet();
      if (hasOwnTable) {
         AnnotationInstance tableAnnotation = JandexHelper.getSingleAnnotation(this.getClassInfo(), JPADotNames.TABLE);
         this.primaryTableSource = this.createTableSource(tableAnnotation);
      } else {
         this.primaryTableSource = null;
      }

      this.secondaryTableSources = this.createSecondaryTableSources();
      this.customLoaderQueryName = this.determineCustomLoader();
      this.synchronizedTableNames = this.determineSynchronizedTableNames();
      this.batchSize = this.determineBatchSize();
      this.jpaCallbacks = this.determineEntityListeners();
      this.processHibernateEntitySpecificAnnotations();
      this.processCustomSqlAnnotations();
      this.processProxyGeneration();
      this.processDiscriminator();
   }

   public ColumnValues getDiscriminatorColumnValues() {
      return this.discriminatorColumnValues;
   }

   public FormulaValue getDiscriminatorFormula() {
      return this.discriminatorFormula;
   }

   public Class getDiscriminatorType() {
      return this.discriminatorType;
   }

   public IdType getIdType() {
      return this.idType;
   }

   public boolean isExplicitPolymorphism() {
      return this.isExplicitPolymorphism;
   }

   public boolean isMutable() {
      return this.isMutable;
   }

   public OptimisticLockStyle getOptimisticLockStyle() {
      return this.optimisticLockStyle;
   }

   public String getWhereClause() {
      return this.whereClause;
   }

   public String getRowId() {
      return this.rowId;
   }

   public Caching getCaching() {
      return this.caching;
   }

   public TableSource getPrimaryTableSource() {
      return this.definesItsOwnTable() ? this.primaryTableSource : ((EntityClass)this.getParent()).getPrimaryTableSource();
   }

   public Set getSecondaryTableSources() {
      return this.secondaryTableSources;
   }

   public Set getConstraintSources() {
      return this.constraintSources;
   }

   public String getExplicitEntityName() {
      return this.explicitEntityName;
   }

   public String getEntityName() {
      return this.getConfiguredClass().getSimpleName();
   }

   public boolean isDynamicInsert() {
      return this.isDynamicInsert;
   }

   public boolean isDynamicUpdate() {
      return this.isDynamicUpdate;
   }

   public boolean isSelectBeforeUpdate() {
      return this.isSelectBeforeUpdate;
   }

   public String getCustomLoaderQueryName() {
      return this.customLoaderQueryName;
   }

   public CustomSQL getCustomInsert() {
      return this.customInsert;
   }

   public CustomSQL getCustomUpdate() {
      return this.customUpdate;
   }

   public CustomSQL getCustomDelete() {
      return this.customDelete;
   }

   public List getSynchronizedTableNames() {
      return this.synchronizedTableNames;
   }

   public String getCustomPersister() {
      return this.customPersister;
   }

   public boolean isLazy() {
      return this.isLazy;
   }

   public String getProxy() {
      return this.proxy;
   }

   public int getBatchSize() {
      return this.batchSize;
   }

   public boolean isEntityRoot() {
      return this.getParent() == null;
   }

   public boolean isDiscriminatorForced() {
      return this.isDiscriminatorForced;
   }

   public boolean isDiscriminatorIncludedInSql() {
      return this.isDiscriminatorIncludedInSql;
   }

   public String getDiscriminatorMatchValue() {
      return this.discriminatorMatchValue;
   }

   public List getJpaCallbacks() {
      return this.jpaCallbacks;
   }

   private String determineExplicitEntityName() {
      AnnotationInstance jpaEntityAnnotation = JandexHelper.getSingleAnnotation(this.getClassInfo(), JPADotNames.ENTITY);
      return (String)JandexHelper.getValue(jpaEntityAnnotation, "name", String.class);
   }

   private boolean definesItsOwnTable() {
      return !InheritanceType.SINGLE_TABLE.equals(this.inheritanceType) || this.isEntityRoot();
   }

   private IdType determineIdType() {
      List<AnnotationInstance> idAnnotations = this.findIdAnnotations(JPADotNames.ID);
      List<AnnotationInstance> embeddedIdAnnotations = this.findIdAnnotations(JPADotNames.EMBEDDED_ID);
      if (!idAnnotations.isEmpty() && !embeddedIdAnnotations.isEmpty()) {
         throw new MappingException("@EmbeddedId and @Id cannot be used together. Check the configuration for " + this.getName() + ".");
      } else if (!embeddedIdAnnotations.isEmpty()) {
         if (embeddedIdAnnotations.size() == 1) {
            return IdType.EMBEDDED;
         } else {
            throw new AnnotationException("Multiple @EmbeddedId annotations are not allowed");
         }
      } else if (!idAnnotations.isEmpty()) {
         return idAnnotations.size() == 1 ? IdType.SIMPLE : IdType.COMPOSED;
      } else {
         return IdType.NONE;
      }
   }

   private List findIdAnnotations(DotName idAnnotationType) {
      List<AnnotationInstance> idAnnotationList = new ArrayList();
      if (this.getClassInfo().annotations().containsKey(idAnnotationType)) {
         idAnnotationList.addAll((Collection)this.getClassInfo().annotations().get(idAnnotationType));
      }

      for(ConfiguredClass parent = this.getParent(); parent != null; parent = parent.getParent()) {
         if (parent.getClassInfo().annotations().containsKey(idAnnotationType)) {
            idAnnotationList.addAll((Collection)parent.getClassInfo().annotations().get(idAnnotationType));
         }
      }

      return idAnnotationList;
   }

   private void processDiscriminator() {
      if (InheritanceType.SINGLE_TABLE.equals(this.inheritanceType)) {
         AnnotationInstance discriminatorValueAnnotation = JandexHelper.getSingleAnnotation(this.getClassInfo(), JPADotNames.DISCRIMINATOR_VALUE);
         if (discriminatorValueAnnotation != null) {
            this.discriminatorMatchValue = discriminatorValueAnnotation.value().asString();
         }

         AnnotationInstance discriminatorColumnAnnotation = JandexHelper.getSingleAnnotation(this.getClassInfo(), JPADotNames.DISCRIMINATOR_COLUMN);
         AnnotationInstance discriminatorFormulaAnnotation = JandexHelper.getSingleAnnotation(this.getClassInfo(), HibernateDotNames.DISCRIMINATOR_FORMULA);
         Class<?> type = String.class;
         if (discriminatorFormulaAnnotation != null) {
            String expression = (String)JandexHelper.getValue(discriminatorFormulaAnnotation, "value", String.class);
            this.discriminatorFormula = new FormulaValue(this.getPrimaryTableSource().getExplicitTableName(), expression);
         }

         this.discriminatorColumnValues = new ColumnValues((AnnotationInstance)null);
         this.discriminatorColumnValues.setNullable(false);
         if (discriminatorColumnAnnotation != null) {
            DiscriminatorType discriminatorType = (DiscriminatorType)Enum.valueOf(DiscriminatorType.class, discriminatorColumnAnnotation.value("discriminatorType").asEnum());
            switch (discriminatorType) {
               case STRING:
                  type = String.class;
                  break;
               case CHAR:
                  type = Character.class;
                  break;
               case INTEGER:
                  type = Integer.class;
                  break;
               default:
                  throw new AnnotationException("Unsupported discriminator type: " + discriminatorType);
            }

            this.discriminatorColumnValues.setName((String)JandexHelper.getValue(discriminatorColumnAnnotation, "name", String.class));
            this.discriminatorColumnValues.setLength((Integer)JandexHelper.getValue(discriminatorColumnAnnotation, "length", Integer.class));
            this.discriminatorColumnValues.setColumnDefinition((String)JandexHelper.getValue(discriminatorColumnAnnotation, "columnDefinition", String.class));
         }

         this.discriminatorType = type;
         AnnotationInstance discriminatorOptionsAnnotation = JandexHelper.getSingleAnnotation(this.getClassInfo(), HibernateDotNames.DISCRIMINATOR_OPTIONS);
         if (discriminatorOptionsAnnotation != null) {
            this.isDiscriminatorForced = discriminatorOptionsAnnotation.value("force").asBoolean();
            this.isDiscriminatorIncludedInSql = discriminatorOptionsAnnotation.value("insert").asBoolean();
         } else {
            this.isDiscriminatorForced = false;
            this.isDiscriminatorIncludedInSql = true;
         }

      }
   }

   private void processHibernateEntitySpecificAnnotations() {
      AnnotationInstance hibernateEntityAnnotation = JandexHelper.getSingleAnnotation(this.getClassInfo(), HibernateDotNames.ENTITY);
      PolymorphismType polymorphism = PolymorphismType.IMPLICIT;
      if (hibernateEntityAnnotation != null && hibernateEntityAnnotation.value("polymorphism") != null) {
         polymorphism = PolymorphismType.valueOf(hibernateEntityAnnotation.value("polymorphism").asEnum());
      }

      this.isExplicitPolymorphism = polymorphism == PolymorphismType.EXPLICIT;
      OptimisticLockType optimisticLockType = OptimisticLockType.VERSION;
      if (hibernateEntityAnnotation != null && hibernateEntityAnnotation.value("optimisticLock") != null) {
         optimisticLockType = OptimisticLockType.valueOf(hibernateEntityAnnotation.value("optimisticLock").asEnum());
      }

      this.optimisticLockStyle = OptimisticLockStyle.valueOf(optimisticLockType.name());
      AnnotationInstance hibernateImmutableAnnotation = JandexHelper.getSingleAnnotation(this.getClassInfo(), HibernateDotNames.IMMUTABLE);
      this.isMutable = hibernateImmutableAnnotation == null && hibernateEntityAnnotation != null && hibernateEntityAnnotation.value("mutable") != null && hibernateEntityAnnotation.value("mutable").asBoolean();
      AnnotationInstance whereAnnotation = JandexHelper.getSingleAnnotation(this.getClassInfo(), HibernateDotNames.WHERE);
      this.whereClause = whereAnnotation != null && whereAnnotation.value("clause") != null ? whereAnnotation.value("clause").asString() : null;
      AnnotationInstance rowIdAnnotation = JandexHelper.getSingleAnnotation(this.getClassInfo(), HibernateDotNames.ROW_ID);
      this.rowId = rowIdAnnotation != null && rowIdAnnotation.value() != null ? rowIdAnnotation.value().asString() : null;
      this.caching = this.determineCachingSettings();
      this.isDynamicInsert = hibernateEntityAnnotation != null && hibernateEntityAnnotation.value("dynamicInsert") != null && hibernateEntityAnnotation.value("dynamicInsert").asBoolean();
      this.isDynamicUpdate = hibernateEntityAnnotation != null && hibernateEntityAnnotation.value("dynamicUpdate") != null && hibernateEntityAnnotation.value("dynamicUpdate").asBoolean();
      this.isSelectBeforeUpdate = hibernateEntityAnnotation != null && hibernateEntityAnnotation.value("selectBeforeUpdate") != null && hibernateEntityAnnotation.value("selectBeforeUpdate").asBoolean();
      AnnotationInstance persisterAnnotation = JandexHelper.getSingleAnnotation(this.getClassInfo(), HibernateDotNames.PERSISTER);
      String entityPersisterClass;
      if (persisterAnnotation != null && persisterAnnotation.value("impl") != null) {
         if (hibernateEntityAnnotation != null && hibernateEntityAnnotation.value("persister") != null) {
         }

         entityPersisterClass = persisterAnnotation.value("impl").asString();
      } else if (hibernateEntityAnnotation != null && hibernateEntityAnnotation.value("persister") != null) {
         entityPersisterClass = hibernateEntityAnnotation.value("persister").asString();
      } else {
         entityPersisterClass = null;
      }

      this.customPersister = entityPersisterClass;
   }

   private Caching determineCachingSettings() {
      AnnotationInstance hibernateCacheAnnotation = JandexHelper.getSingleAnnotation(this.getClassInfo(), HibernateDotNames.CACHE);
      if (hibernateCacheAnnotation != null) {
         org.hibernate.cache.spi.access.AccessType accessType = hibernateCacheAnnotation.value("usage") == null ? this.getLocalBindingContext().getMappingDefaults().getCacheAccessType() : CacheConcurrencyStrategy.parse(hibernateCacheAnnotation.value("usage").asEnum()).toAccessType();
         return new Caching(hibernateCacheAnnotation.value("region") == null ? this.getName() : hibernateCacheAnnotation.value("region").asString(), accessType, hibernateCacheAnnotation.value("include") != null && "all".equals(hibernateCacheAnnotation.value("include").asString()));
      } else {
         AnnotationInstance jpaCacheableAnnotation = JandexHelper.getSingleAnnotation(this.getClassInfo(), JPADotNames.CACHEABLE);
         boolean cacheable = true;
         if (jpaCacheableAnnotation != null && jpaCacheableAnnotation.value() != null) {
            cacheable = jpaCacheableAnnotation.value().asBoolean();
         }

         boolean doCaching;
         switch (this.getLocalBindingContext().getMetadataImplementor().getOptions().getSharedCacheMode()) {
            case ALL:
               doCaching = true;
               break;
            case ENABLE_SELECTIVE:
               doCaching = cacheable;
               break;
            case DISABLE_SELECTIVE:
               doCaching = jpaCacheableAnnotation == null || cacheable;
               break;
            default:
               doCaching = false;
         }

         return !doCaching ? null : new Caching(this.getName(), this.getLocalBindingContext().getMappingDefaults().getCacheAccessType(), true);
      }
   }

   private TableSource createTableSource(AnnotationInstance tableAnnotation) {
      String schema = null;
      String catalog = null;
      if (tableAnnotation != null) {
         schema = (String)JandexHelper.getValue(tableAnnotation, "schema", String.class);
         catalog = (String)JandexHelper.getValue(tableAnnotation, "catalog", String.class);
      }

      String tableName = null;
      String logicalTableName = null;
      if (tableAnnotation != null) {
         logicalTableName = (String)JandexHelper.getValue(tableAnnotation, "name", String.class);
         if (StringHelper.isNotEmpty(logicalTableName)) {
            tableName = logicalTableName;
         }

         this.createUniqueConstraints(tableAnnotation, tableName);
      }

      TableSourceImpl tableSourceImpl;
      if (tableAnnotation != null && !JPADotNames.TABLE.equals(tableAnnotation.name())) {
         tableSourceImpl = new TableSourceImpl(schema, catalog, tableName, logicalTableName);
      } else {
         tableSourceImpl = new TableSourceImpl(schema, catalog, tableName, (String)null);
      }

      return tableSourceImpl;
   }

   private Set createSecondaryTableSources() {
      Set<TableSource> secondaryTableSources = new HashSet();
      AnnotationInstance secondaryTables = JandexHelper.getSingleAnnotation(this.getClassInfo(), JPADotNames.SECONDARY_TABLES);
      AnnotationInstance secondaryTable = JandexHelper.getSingleAnnotation(this.getClassInfo(), JPADotNames.SECONDARY_TABLE);
      List<AnnotationInstance> secondaryTableAnnotations = new ArrayList();
      if (secondaryTable != null) {
         secondaryTableAnnotations.add(secondaryTable);
      }

      if (secondaryTables != null) {
         secondaryTableAnnotations.addAll(Arrays.asList(JandexHelper.getValue(secondaryTables, "value", AnnotationInstance[].class)));
      }

      for(AnnotationInstance annotationInstance : secondaryTableAnnotations) {
         secondaryTableSources.add(this.createTableSource(annotationInstance));
      }

      return secondaryTableSources;
   }

   private void createUniqueConstraints(AnnotationInstance tableAnnotation, String tableName) {
      AnnotationValue value = tableAnnotation.value("uniqueConstraints");
      if (value != null) {
         AnnotationInstance[] uniqueConstraints = value.asNestedArray();

         for(AnnotationInstance unique : uniqueConstraints) {
            String name = unique.value("name") == null ? null : unique.value("name").asString();
            String[] columnNames = unique.value("columnNames").asStringArray();
            UniqueConstraintSourceImpl uniqueConstraintSource = new UniqueConstraintSourceImpl(name, tableName, Arrays.asList(columnNames));
            this.constraintSources.add(uniqueConstraintSource);
         }

      }
   }

   private String determineCustomLoader() {
      String customLoader = null;
      AnnotationInstance sqlLoaderAnnotation = JandexHelper.getSingleAnnotation(this.getClassInfo(), HibernateDotNames.LOADER);
      if (sqlLoaderAnnotation != null) {
         customLoader = sqlLoaderAnnotation.value("namedQuery").asString();
      }

      return customLoader;
   }

   private CustomSQL createCustomSQL(AnnotationInstance customSqlAnnotation) {
      if (customSqlAnnotation == null) {
         return null;
      } else {
         String sql = customSqlAnnotation.value("sql").asString();
         boolean isCallable = customSqlAnnotation.value("callable") != null && customSqlAnnotation.value("callable").asBoolean();
         ExecuteUpdateResultCheckStyle checkStyle = customSqlAnnotation.value("check") == null ? (isCallable ? ExecuteUpdateResultCheckStyle.NONE : ExecuteUpdateResultCheckStyle.COUNT) : ExecuteUpdateResultCheckStyle.valueOf(customSqlAnnotation.value("check").asEnum());
         return new CustomSQL(sql, isCallable, checkStyle);
      }
   }

   private void processCustomSqlAnnotations() {
      AnnotationInstance sqlInsertAnnotation = JandexHelper.getSingleAnnotation(this.getClassInfo(), HibernateDotNames.SQL_INSERT);
      this.customInsert = this.createCustomSQL(sqlInsertAnnotation);
      AnnotationInstance sqlUpdateAnnotation = JandexHelper.getSingleAnnotation(this.getClassInfo(), HibernateDotNames.SQL_UPDATE);
      this.customUpdate = this.createCustomSQL(sqlUpdateAnnotation);
      AnnotationInstance sqlDeleteAnnotation = JandexHelper.getSingleAnnotation(this.getClassInfo(), HibernateDotNames.SQL_DELETE);
      this.customDelete = this.createCustomSQL(sqlDeleteAnnotation);
   }

   private List determineSynchronizedTableNames() {
      AnnotationInstance synchronizeAnnotation = JandexHelper.getSingleAnnotation(this.getClassInfo(), HibernateDotNames.SYNCHRONIZE);
      if (synchronizeAnnotation != null) {
         String[] tableNames = synchronizeAnnotation.value().asStringArray();
         return Arrays.asList(tableNames);
      } else {
         return Collections.emptyList();
      }
   }

   private void processProxyGeneration() {
      AnnotationInstance hibernateProxyAnnotation = JandexHelper.getSingleAnnotation(this.getClassInfo(), HibernateDotNames.PROXY);
      if (hibernateProxyAnnotation != null) {
         this.isLazy = hibernateProxyAnnotation.value("lazy") == null || hibernateProxyAnnotation.value("lazy").asBoolean();
         if (this.isLazy) {
            AnnotationValue proxyClassValue = hibernateProxyAnnotation.value("proxyClass");
            if (proxyClassValue == null) {
               this.proxy = this.getName();
            } else {
               this.proxy = proxyClassValue.asString();
            }
         } else {
            this.proxy = null;
         }
      } else {
         this.isLazy = true;
         this.proxy = this.getName();
      }

   }

   private int determineBatchSize() {
      AnnotationInstance batchSizeAnnotation = JandexHelper.getSingleAnnotation(this.getClassInfo(), HibernateDotNames.BATCH_SIZE);
      return batchSizeAnnotation == null ? -1 : batchSizeAnnotation.value("size").asInt();
   }

   private List determineEntityListeners() {
      List<JpaCallbackClass> callbackClassList = new ArrayList();
      if (JandexHelper.getSingleAnnotation(this.getClassInfo(), JPADotNames.EXCLUDE_DEFAULT_LISTENERS) == null) {
         for(AnnotationInstance annotation : this.getLocalBindingContext().getIndex().getAnnotations(PseudoJpaDotNames.DEFAULT_ENTITY_LISTENERS)) {
            for(Type callbackClass : annotation.value().asClassArray()) {
               String callbackClassName = callbackClass.name().toString();

               try {
                  this.processDefaultJpaCallbacks(callbackClassName, callbackClassList);
               } catch (PersistenceException error) {
                  throw new PersistenceException(error.getMessage() + "default entity listener " + callbackClassName);
               }
            }
         }
      }

      List<AnnotationInstance> annotationList = (List)this.getClassInfo().annotations().get(JPADotNames.ENTITY_LISTENERS);
      if (annotationList != null) {
         for(AnnotationInstance annotation : annotationList) {
            for(Type callbackClass : annotation.value().asClassArray()) {
               String callbackClassName = callbackClass.name().toString();

               try {
                  this.processJpaCallbacks(callbackClassName, true, callbackClassList);
               } catch (PersistenceException error) {
                  throw new PersistenceException(error.getMessage() + "entity listener " + callbackClassName);
               }
            }
         }
      }

      try {
         this.processJpaCallbacks(this.getName(), false, callbackClassList);
         return callbackClassList;
      } catch (PersistenceException error) {
         throw new PersistenceException(error.getMessage() + "entity/mapped superclass " + this.getClassInfo().name().toString());
      }
   }

   private void processDefaultJpaCallbacks(String instanceCallbackClassName, List jpaCallbackClassList) {
      ClassInfo callbackClassInfo = this.getLocalBindingContext().getClassInfo(instanceCallbackClassName);
      if (JandexHelper.getSingleAnnotation(callbackClassInfo, JPADotNames.EXCLUDE_SUPERCLASS_LISTENERS) != null) {
         DotName superName = callbackClassInfo.superName();
         if (superName != null) {
            this.processDefaultJpaCallbacks(instanceCallbackClassName, jpaCallbackClassList);
         }
      }

      String callbackClassName = callbackClassInfo.name().toString();
      Map<Class<?>, String> callbacksByType = new HashMap();
      this.createDefaultCallback(PrePersist.class, PseudoJpaDotNames.DEFAULT_PRE_PERSIST, callbackClassName, callbacksByType);
      this.createDefaultCallback(PreRemove.class, PseudoJpaDotNames.DEFAULT_PRE_REMOVE, callbackClassName, callbacksByType);
      this.createDefaultCallback(PreUpdate.class, PseudoJpaDotNames.DEFAULT_PRE_UPDATE, callbackClassName, callbacksByType);
      this.createDefaultCallback(PostLoad.class, PseudoJpaDotNames.DEFAULT_POST_LOAD, callbackClassName, callbacksByType);
      this.createDefaultCallback(PostPersist.class, PseudoJpaDotNames.DEFAULT_POST_PERSIST, callbackClassName, callbacksByType);
      this.createDefaultCallback(PostRemove.class, PseudoJpaDotNames.DEFAULT_POST_REMOVE, callbackClassName, callbacksByType);
      this.createDefaultCallback(PostUpdate.class, PseudoJpaDotNames.DEFAULT_POST_UPDATE, callbackClassName, callbacksByType);
      if (!callbacksByType.isEmpty()) {
         jpaCallbackClassList.add(new JpaCallbackClassImpl(instanceCallbackClassName, callbacksByType, true));
      }

   }

   private void processJpaCallbacks(String instanceCallbackClassName, boolean isListener, List callbackClassList) {
      ClassInfo callbackClassInfo = this.getLocalBindingContext().getClassInfo(instanceCallbackClassName);
      if (JandexHelper.getSingleAnnotation(callbackClassInfo, JPADotNames.EXCLUDE_SUPERCLASS_LISTENERS) != null) {
         DotName superName = callbackClassInfo.superName();
         if (superName != null) {
            this.processJpaCallbacks(instanceCallbackClassName, isListener, callbackClassList);
         }
      }

      Map<Class<?>, String> callbacksByType = new HashMap();
      this.createCallback(PrePersist.class, JPADotNames.PRE_PERSIST, callbacksByType, callbackClassInfo, isListener);
      this.createCallback(PreRemove.class, JPADotNames.PRE_REMOVE, callbacksByType, callbackClassInfo, isListener);
      this.createCallback(PreUpdate.class, JPADotNames.PRE_UPDATE, callbacksByType, callbackClassInfo, isListener);
      this.createCallback(PostLoad.class, JPADotNames.POST_LOAD, callbacksByType, callbackClassInfo, isListener);
      this.createCallback(PostPersist.class, JPADotNames.POST_PERSIST, callbacksByType, callbackClassInfo, isListener);
      this.createCallback(PostRemove.class, JPADotNames.POST_REMOVE, callbacksByType, callbackClassInfo, isListener);
      this.createCallback(PostUpdate.class, JPADotNames.POST_UPDATE, callbacksByType, callbackClassInfo, isListener);
      if (!callbacksByType.isEmpty()) {
         callbackClassList.add(new JpaCallbackClassImpl(instanceCallbackClassName, callbacksByType, isListener));
      }

   }

   private void createDefaultCallback(Class callbackTypeClass, DotName callbackTypeName, String callbackClassName, Map callbacksByClass) {
      for(AnnotationInstance callback : this.getLocalBindingContext().getIndex().getAnnotations(callbackTypeName)) {
         MethodInfo methodInfo = (MethodInfo)callback.target();
         this.validateMethod(methodInfo, callbackTypeClass, callbacksByClass, true);
         if (methodInfo.declaringClass().name().toString().equals(callbackClassName)) {
            if (methodInfo.args().length != 1) {
               throw new PersistenceException(String.format("Callback method %s must have exactly one argument defined as either Object or %s in ", methodInfo.name(), this.getEntityName()));
            }

            callbacksByClass.put(callbackTypeClass, methodInfo.name());
         }
      }

   }

   private void createCallback(Class callbackTypeClass, DotName callbackTypeName, Map callbacksByClass, ClassInfo callbackClassInfo, boolean isListener) {
      Map<DotName, List<AnnotationInstance>> annotations = callbackClassInfo.annotations();
      List<AnnotationInstance> annotationInstances = (List)annotations.get(callbackTypeName);
      if (annotationInstances != null) {
         for(AnnotationInstance callbackAnnotation : annotationInstances) {
            MethodInfo methodInfo = (MethodInfo)callbackAnnotation.target();
            this.validateMethod(methodInfo, callbackTypeClass, callbacksByClass, isListener);
            callbacksByClass.put(callbackTypeClass, methodInfo.name());
         }

      }
   }

   private void validateMethod(MethodInfo methodInfo, Class callbackTypeClass, Map callbacksByClass, boolean isListener) {
      if (methodInfo.returnType().kind() != Kind.VOID) {
         throw new PersistenceException("Callback method " + methodInfo.name() + " must have a void return type in ");
      } else if (!Modifier.isStatic(methodInfo.flags()) && !Modifier.isFinal(methodInfo.flags())) {
         Type[] argTypes = methodInfo.args();
         if (isListener) {
            if (argTypes.length != 1) {
               throw new PersistenceException("Callback method " + methodInfo.name() + " must have exactly one argument in ");
            }

            String argTypeName = argTypes[0].name().toString();
            if (!argTypeName.equals(Object.class.getName()) && !argTypeName.equals(this.getName())) {
               throw new PersistenceException("The argument for callback method " + methodInfo.name() + " must be defined as either Object or " + this.getEntityName() + " in ");
            }
         } else if (argTypes.length != 0) {
            throw new PersistenceException("Callback method " + methodInfo.name() + " must have no arguments in ");
         }

         if (callbacksByClass.containsKey(callbackTypeClass)) {
            throw new PersistenceException("Only one method may be annotated as a " + callbackTypeClass.getSimpleName() + " callback method in ");
         }
      } else {
         throw new PersistenceException("Callback method " + methodInfo.name() + " must not be static or final in ");
      }
   }

   private class JpaCallbackClassImpl implements JpaCallbackClass {
      private final Map callbacksByType;
      private final String name;
      private final boolean isListener;

      private JpaCallbackClassImpl(String name, Map callbacksByType, boolean isListener) {
         this.name = name;
         this.callbacksByType = callbacksByType;
         this.isListener = isListener;
      }

      public String getCallbackMethod(Class callbackType) {
         return (String)this.callbacksByType.get(callbackType);
      }

      public String getName() {
         return this.name;
      }

      public boolean isListener() {
         return this.isListener;
      }
   }
}
