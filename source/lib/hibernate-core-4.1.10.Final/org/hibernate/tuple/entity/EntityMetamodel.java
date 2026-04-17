package org.hibernate.tuple.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.bytecode.spi.EntityInstrumentationMetadata;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.OptimisticLockStyle;
import org.hibernate.engine.spi.CascadeStyle;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.ValueInclusion;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.PropertyGeneration;
import org.hibernate.metamodel.binding.AttributeBinding;
import org.hibernate.metamodel.binding.BasicAttributeBinding;
import org.hibernate.metamodel.binding.EntityBinding;
import org.hibernate.metamodel.domain.Attribute;
import org.hibernate.metamodel.domain.SingularAttribute;
import org.hibernate.tuple.IdentifierProperty;
import org.hibernate.tuple.PropertyFactory;
import org.hibernate.tuple.StandardProperty;
import org.hibernate.tuple.VersionProperty;
import org.hibernate.type.AssociationType;
import org.hibernate.type.CompositeType;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public class EntityMetamodel implements Serializable {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, EntityMetamodel.class.getName());
   private static final int NO_VERSION_INDX = -66;
   private final SessionFactoryImplementor sessionFactory;
   private final String name;
   private final String rootName;
   private final EntityType entityType;
   private final IdentifierProperty identifierProperty;
   private final boolean versioned;
   private final int propertySpan;
   private final int versionPropertyIndex;
   private final StandardProperty[] properties;
   private final String[] propertyNames;
   private final Type[] propertyTypes;
   private final boolean[] propertyLaziness;
   private final boolean[] propertyUpdateability;
   private final boolean[] nonlazyPropertyUpdateability;
   private final boolean[] propertyCheckability;
   private final boolean[] propertyInsertability;
   private final ValueInclusion[] insertInclusions;
   private final ValueInclusion[] updateInclusions;
   private final boolean[] propertyNullability;
   private final boolean[] propertyVersionability;
   private final CascadeStyle[] cascadeStyles;
   private final boolean hasInsertGeneratedValues;
   private final boolean hasUpdateGeneratedValues;
   private final Map propertyIndexes = new HashMap();
   private final boolean hasCollections;
   private final boolean hasMutableProperties;
   private final boolean hasLazyProperties;
   private final boolean hasNonIdentifierPropertyNamedId;
   private final int[] naturalIdPropertyNumbers;
   private final boolean hasImmutableNaturalId;
   private final boolean hasCacheableNaturalId;
   private boolean lazy;
   private final boolean hasCascades;
   private final boolean mutable;
   private final boolean isAbstract;
   private final boolean selectBeforeUpdate;
   private final boolean dynamicUpdate;
   private final boolean dynamicInsert;
   private final OptimisticLockStyle optimisticLockStyle;
   private final boolean polymorphic;
   private final String superclass;
   private final boolean explicitPolymorphism;
   private final boolean inherited;
   private final boolean hasSubclasses;
   private final Set subclassEntityNames = new HashSet();
   private final Map entityNameByInheritenceClassMap = new HashMap();
   private final EntityMode entityMode;
   private final EntityTuplizer entityTuplizer;
   private final EntityInstrumentationMetadata instrumentationMetadata;

   public EntityMetamodel(PersistentClass persistentClass, SessionFactoryImplementor sessionFactory) {
      this.sessionFactory = sessionFactory;
      this.name = persistentClass.getEntityName();
      this.rootName = persistentClass.getRootClass().getEntityName();
      this.entityType = sessionFactory.getTypeResolver().getTypeFactory().manyToOne(this.name);
      this.identifierProperty = PropertyFactory.buildIdentifierProperty(persistentClass, sessionFactory.getIdentifierGenerator(this.rootName));
      this.versioned = persistentClass.isVersioned();
      this.instrumentationMetadata = (EntityInstrumentationMetadata)(persistentClass.hasPojoRepresentation() ? Environment.getBytecodeProvider().getEntityInstrumentationMetadata(persistentClass.getMappedClass()) : new NonPojoInstrumentationMetadata(persistentClass.getEntityName()));
      boolean hasLazy = false;
      this.propertySpan = persistentClass.getPropertyClosureSpan();
      this.properties = new StandardProperty[this.propertySpan];
      List<Integer> naturalIdNumbers = new ArrayList();
      this.propertyNames = new String[this.propertySpan];
      this.propertyTypes = new Type[this.propertySpan];
      this.propertyUpdateability = new boolean[this.propertySpan];
      this.propertyInsertability = new boolean[this.propertySpan];
      this.insertInclusions = new ValueInclusion[this.propertySpan];
      this.updateInclusions = new ValueInclusion[this.propertySpan];
      this.nonlazyPropertyUpdateability = new boolean[this.propertySpan];
      this.propertyCheckability = new boolean[this.propertySpan];
      this.propertyNullability = new boolean[this.propertySpan];
      this.propertyVersionability = new boolean[this.propertySpan];
      this.propertyLaziness = new boolean[this.propertySpan];
      this.cascadeStyles = new CascadeStyle[this.propertySpan];
      Iterator iter = persistentClass.getPropertyClosureIterator();
      int i = 0;
      int tempVersionProperty = -66;
      boolean foundCascade = false;
      boolean foundCollection = false;
      boolean foundMutable = false;
      boolean foundNonIdentifierPropertyNamedId = false;
      boolean foundInsertGeneratedValue = false;
      boolean foundUpdateGeneratedValue = false;

      boolean foundUpdateableNaturalIdProperty;
      for(foundUpdateableNaturalIdProperty = false; iter.hasNext(); ++i) {
         Property prop = (Property)iter.next();
         if (prop == persistentClass.getVersion()) {
            tempVersionProperty = i;
            this.properties[i] = PropertyFactory.buildVersionProperty(prop, this.instrumentationMetadata.isInstrumented());
         } else {
            this.properties[i] = PropertyFactory.buildStandardProperty(prop, this.instrumentationMetadata.isInstrumented());
         }

         if (prop.isNaturalIdentifier()) {
            naturalIdNumbers.add(i);
            if (prop.isUpdateable()) {
               foundUpdateableNaturalIdProperty = true;
            }
         }

         if ("id".equals(prop.getName())) {
            foundNonIdentifierPropertyNamedId = true;
         }

         boolean lazy = prop.isLazy() && this.instrumentationMetadata.isInstrumented();
         if (lazy) {
            hasLazy = true;
         }

         this.propertyLaziness[i] = lazy;
         this.propertyNames[i] = this.properties[i].getName();
         this.propertyTypes[i] = this.properties[i].getType();
         this.propertyNullability[i] = this.properties[i].isNullable();
         this.propertyUpdateability[i] = this.properties[i].isUpdateable();
         this.propertyInsertability[i] = this.properties[i].isInsertable();
         this.insertInclusions[i] = this.determineInsertValueGenerationType(prop, this.properties[i]);
         this.updateInclusions[i] = this.determineUpdateValueGenerationType(prop, this.properties[i]);
         this.propertyVersionability[i] = this.properties[i].isVersionable();
         this.nonlazyPropertyUpdateability[i] = this.properties[i].isUpdateable() && !lazy;
         this.propertyCheckability[i] = this.propertyUpdateability[i] || this.propertyTypes[i].isAssociationType() && ((AssociationType)this.propertyTypes[i]).isAlwaysDirtyChecked();
         this.cascadeStyles[i] = this.properties[i].getCascadeStyle();
         if (this.properties[i].isLazy()) {
            hasLazy = true;
         }

         if (this.properties[i].getCascadeStyle() != CascadeStyle.NONE) {
            foundCascade = true;
         }

         if (this.indicatesCollection(this.properties[i].getType())) {
            foundCollection = true;
         }

         if (this.propertyTypes[i].isMutable() && this.propertyCheckability[i]) {
            foundMutable = true;
         }

         if (this.insertInclusions[i] != ValueInclusion.NONE) {
            foundInsertGeneratedValue = true;
         }

         if (this.updateInclusions[i] != ValueInclusion.NONE) {
            foundUpdateGeneratedValue = true;
         }

         this.mapPropertyToIndex(prop, i);
      }

      if (naturalIdNumbers.size() == 0) {
         this.naturalIdPropertyNumbers = null;
         this.hasImmutableNaturalId = false;
         this.hasCacheableNaturalId = false;
      } else {
         this.naturalIdPropertyNumbers = ArrayHelper.toIntArray(naturalIdNumbers);
         this.hasImmutableNaturalId = !foundUpdateableNaturalIdProperty;
         this.hasCacheableNaturalId = persistentClass.getNaturalIdCacheRegionName() != null;
      }

      this.hasInsertGeneratedValues = foundInsertGeneratedValue;
      this.hasUpdateGeneratedValues = foundUpdateGeneratedValue;
      this.hasCascades = foundCascade;
      this.hasNonIdentifierPropertyNamedId = foundNonIdentifierPropertyNamedId;
      this.versionPropertyIndex = tempVersionProperty;
      this.hasLazyProperties = hasLazy;
      if (this.hasLazyProperties) {
         LOG.lazyPropertyFetchingAvailable(this.name);
      }

      this.lazy = persistentClass.isLazy() && (!persistentClass.hasPojoRepresentation() || !ReflectHelper.isFinalClass(persistentClass.getProxyInterface()));
      this.mutable = persistentClass.isMutable();
      if (persistentClass.isAbstract() == null) {
         this.isAbstract = persistentClass.hasPojoRepresentation() && ReflectHelper.isAbstractClass(persistentClass.getMappedClass());
      } else {
         this.isAbstract = persistentClass.isAbstract();
         if (!this.isAbstract && persistentClass.hasPojoRepresentation() && ReflectHelper.isAbstractClass(persistentClass.getMappedClass())) {
            LOG.entityMappedAsNonAbstract(this.name);
         }
      }

      this.selectBeforeUpdate = persistentClass.hasSelectBeforeUpdate();
      this.dynamicUpdate = persistentClass.useDynamicUpdate();
      this.dynamicInsert = persistentClass.useDynamicInsert();
      this.polymorphic = persistentClass.isPolymorphic();
      this.explicitPolymorphism = persistentClass.isExplicitPolymorphism();
      this.inherited = persistentClass.isInherited();
      this.superclass = this.inherited ? persistentClass.getSuperclass().getEntityName() : null;
      this.hasSubclasses = persistentClass.hasSubclasses();
      this.optimisticLockStyle = this.interpretOptLockMode(persistentClass.getOptimisticLockMode());
      boolean isAllOrDirty = this.optimisticLockStyle == OptimisticLockStyle.ALL || this.optimisticLockStyle == OptimisticLockStyle.DIRTY;
      if (isAllOrDirty && !this.dynamicUpdate) {
         throw new MappingException("optimistic-lock=all|dirty requires dynamic-update=\"true\": " + this.name);
      } else if (this.versionPropertyIndex != -66 && isAllOrDirty) {
         throw new MappingException("version and optimistic-lock=all|dirty are not a valid combination : " + this.name);
      } else {
         this.hasCollections = foundCollection;
         this.hasMutableProperties = foundMutable;
         iter = persistentClass.getSubclassIterator();

         while(iter.hasNext()) {
            this.subclassEntityNames.add(((PersistentClass)iter.next()).getEntityName());
         }

         this.subclassEntityNames.add(this.name);
         if (persistentClass.hasPojoRepresentation()) {
            this.entityNameByInheritenceClassMap.put(persistentClass.getMappedClass(), persistentClass.getEntityName());
            iter = persistentClass.getSubclassIterator();

            while(iter.hasNext()) {
               PersistentClass pc = (PersistentClass)iter.next();
               this.entityNameByInheritenceClassMap.put(pc.getMappedClass(), pc.getEntityName());
            }
         }

         this.entityMode = persistentClass.hasPojoRepresentation() ? EntityMode.POJO : EntityMode.MAP;
         EntityTuplizerFactory entityTuplizerFactory = sessionFactory.getSettings().getEntityTuplizerFactory();
         String tuplizerClassName = persistentClass.getTuplizerImplClassName(this.entityMode);
         if (tuplizerClassName == null) {
            this.entityTuplizer = entityTuplizerFactory.constructDefaultTuplizer(this.entityMode, this, persistentClass);
         } else {
            this.entityTuplizer = entityTuplizerFactory.constructTuplizer(tuplizerClassName, this, persistentClass);
         }

      }
   }

   private OptimisticLockStyle interpretOptLockMode(int optimisticLockMode) {
      switch (optimisticLockMode) {
         case -1:
            return OptimisticLockStyle.NONE;
         case 0:
         default:
            return OptimisticLockStyle.VERSION;
         case 1:
            return OptimisticLockStyle.DIRTY;
         case 2:
            return OptimisticLockStyle.ALL;
      }
   }

   public EntityMetamodel(EntityBinding entityBinding, SessionFactoryImplementor sessionFactory) {
      this.sessionFactory = sessionFactory;
      this.name = entityBinding.getEntity().getName();
      this.rootName = entityBinding.getHierarchyDetails().getRootEntityBinding().getEntity().getName();
      this.entityType = sessionFactory.getTypeResolver().getTypeFactory().manyToOne(this.name);
      this.identifierProperty = PropertyFactory.buildIdentifierProperty(entityBinding, sessionFactory.getIdentifierGenerator(this.rootName));
      this.versioned = entityBinding.isVersioned();
      boolean hasPojoRepresentation = false;
      Class<?> mappedClass = null;
      Class<?> proxyInterfaceClass = null;
      if (entityBinding.getEntity().getClassReferenceUnresolved() != null) {
         hasPojoRepresentation = true;
         mappedClass = entityBinding.getEntity().getClassReference();
         proxyInterfaceClass = (Class)entityBinding.getProxyInterfaceType().getValue();
      }

      this.instrumentationMetadata = Environment.getBytecodeProvider().getEntityInstrumentationMetadata(mappedClass);
      boolean hasLazy = false;
      BasicAttributeBinding rootEntityIdentifier = entityBinding.getHierarchyDetails().getEntityIdentifier().getValueBinding();
      this.propertySpan = rootEntityIdentifier == null ? entityBinding.getAttributeBindingClosureSpan() : entityBinding.getAttributeBindingClosureSpan() - 1;
      this.properties = new StandardProperty[this.propertySpan];
      List naturalIdNumbers = new ArrayList();
      this.propertyNames = new String[this.propertySpan];
      this.propertyTypes = new Type[this.propertySpan];
      this.propertyUpdateability = new boolean[this.propertySpan];
      this.propertyInsertability = new boolean[this.propertySpan];
      this.insertInclusions = new ValueInclusion[this.propertySpan];
      this.updateInclusions = new ValueInclusion[this.propertySpan];
      this.nonlazyPropertyUpdateability = new boolean[this.propertySpan];
      this.propertyCheckability = new boolean[this.propertySpan];
      this.propertyNullability = new boolean[this.propertySpan];
      this.propertyVersionability = new boolean[this.propertySpan];
      this.propertyLaziness = new boolean[this.propertySpan];
      this.cascadeStyles = new CascadeStyle[this.propertySpan];
      int i = 0;
      int tempVersionProperty = -66;
      boolean foundCascade = false;
      boolean foundCollection = false;
      boolean foundMutable = false;
      boolean foundNonIdentifierPropertyNamedId = false;
      boolean foundInsertGeneratedValue = false;
      boolean foundUpdateGeneratedValue = false;
      boolean foundUpdateableNaturalIdProperty = false;

      for(AttributeBinding attributeBinding : entityBinding.getAttributeBindingClosure()) {
         if (attributeBinding != rootEntityIdentifier) {
            if (attributeBinding == entityBinding.getHierarchyDetails().getVersioningAttributeBinding()) {
               tempVersionProperty = i;
               this.properties[i] = PropertyFactory.buildVersionProperty(entityBinding.getHierarchyDetails().getVersioningAttributeBinding(), this.instrumentationMetadata.isInstrumented());
            } else {
               this.properties[i] = PropertyFactory.buildStandardProperty(attributeBinding, this.instrumentationMetadata.isInstrumented());
            }

            if ("id".equals(attributeBinding.getAttribute().getName())) {
               foundNonIdentifierPropertyNamedId = true;
            }

            boolean lazy = attributeBinding.isLazy() && this.instrumentationMetadata.isInstrumented();
            if (lazy) {
               hasLazy = true;
            }

            this.propertyLaziness[i] = lazy;
            this.propertyNames[i] = this.properties[i].getName();
            this.propertyTypes[i] = this.properties[i].getType();
            this.propertyNullability[i] = this.properties[i].isNullable();
            this.propertyUpdateability[i] = this.properties[i].isUpdateable();
            this.propertyInsertability[i] = this.properties[i].isInsertable();
            this.insertInclusions[i] = this.determineInsertValueGenerationType(attributeBinding, this.properties[i]);
            this.updateInclusions[i] = this.determineUpdateValueGenerationType(attributeBinding, this.properties[i]);
            this.propertyVersionability[i] = this.properties[i].isVersionable();
            this.nonlazyPropertyUpdateability[i] = this.properties[i].isUpdateable() && !lazy;
            this.propertyCheckability[i] = this.propertyUpdateability[i] || this.propertyTypes[i].isAssociationType() && ((AssociationType)this.propertyTypes[i]).isAlwaysDirtyChecked();
            this.cascadeStyles[i] = this.properties[i].getCascadeStyle();
            if (this.properties[i].isLazy()) {
               hasLazy = true;
            }

            if (this.properties[i].getCascadeStyle() != CascadeStyle.NONE) {
               foundCascade = true;
            }

            if (this.indicatesCollection(this.properties[i].getType())) {
               foundCollection = true;
            }

            if (this.propertyTypes[i].isMutable() && this.propertyCheckability[i]) {
               foundMutable = true;
            }

            if (this.insertInclusions[i] != ValueInclusion.NONE) {
               foundInsertGeneratedValue = true;
            }

            if (this.updateInclusions[i] != ValueInclusion.NONE) {
               foundUpdateGeneratedValue = true;
            }

            this.mapPropertyToIndex(attributeBinding.getAttribute(), i);
            ++i;
         }
      }

      if (naturalIdNumbers.size() == 0) {
         this.naturalIdPropertyNumbers = null;
         this.hasImmutableNaturalId = false;
         this.hasCacheableNaturalId = false;
      } else {
         this.naturalIdPropertyNumbers = ArrayHelper.toIntArray(naturalIdNumbers);
         this.hasImmutableNaturalId = !foundUpdateableNaturalIdProperty;
         this.hasCacheableNaturalId = false;
      }

      this.hasInsertGeneratedValues = foundInsertGeneratedValue;
      this.hasUpdateGeneratedValues = foundUpdateGeneratedValue;
      this.hasCascades = foundCascade;
      this.hasNonIdentifierPropertyNamedId = foundNonIdentifierPropertyNamedId;
      this.versionPropertyIndex = tempVersionProperty;
      this.hasLazyProperties = hasLazy;
      if (this.hasLazyProperties) {
         LOG.lazyPropertyFetchingAvailable(this.name);
      }

      this.lazy = entityBinding.isLazy() && (!hasPojoRepresentation || !ReflectHelper.isFinalClass(proxyInterfaceClass));
      this.mutable = entityBinding.isMutable();
      if (entityBinding.isAbstract() == null) {
         this.isAbstract = hasPojoRepresentation && ReflectHelper.isAbstractClass(mappedClass);
      } else {
         this.isAbstract = entityBinding.isAbstract();
         if (!this.isAbstract && hasPojoRepresentation && ReflectHelper.isAbstractClass(mappedClass)) {
            LOG.entityMappedAsNonAbstract(this.name);
         }
      }

      this.selectBeforeUpdate = entityBinding.isSelectBeforeUpdate();
      this.dynamicUpdate = entityBinding.isDynamicUpdate();
      this.dynamicInsert = entityBinding.isDynamicInsert();
      this.hasSubclasses = entityBinding.hasSubEntityBindings();
      this.polymorphic = entityBinding.isPolymorphic();
      this.explicitPolymorphism = entityBinding.getHierarchyDetails().isExplicitPolymorphism();
      this.inherited = !entityBinding.isRoot();
      this.superclass = this.inherited ? entityBinding.getEntity().getSuperType().getName() : null;
      this.optimisticLockStyle = entityBinding.getHierarchyDetails().getOptimisticLockStyle();
      boolean isAllOrDirty = this.optimisticLockStyle == OptimisticLockStyle.ALL || this.optimisticLockStyle == OptimisticLockStyle.DIRTY;
      if (isAllOrDirty && !this.dynamicUpdate) {
         throw new MappingException("optimistic-lock=all|dirty requires dynamic-update=\"true\": " + this.name);
      } else if (this.versionPropertyIndex != -66 && isAllOrDirty) {
         throw new MappingException("version and optimistic-lock=all|dirty are not a valid combination : " + this.name);
      } else {
         this.hasCollections = foundCollection;
         this.hasMutableProperties = foundMutable;

         for(EntityBinding subEntityBinding : entityBinding.getPostOrderSubEntityBindingClosure()) {
            this.subclassEntityNames.add(subEntityBinding.getEntity().getName());
            if (subEntityBinding.getEntity().getClassReference() != null) {
               this.entityNameByInheritenceClassMap.put(subEntityBinding.getEntity().getClassReference(), subEntityBinding.getEntity().getName());
            }
         }

         this.subclassEntityNames.add(this.name);
         if (mappedClass != null) {
            this.entityNameByInheritenceClassMap.put(mappedClass, this.name);
         }

         this.entityMode = hasPojoRepresentation ? EntityMode.POJO : EntityMode.MAP;
         EntityTuplizerFactory entityTuplizerFactory = sessionFactory.getSettings().getEntityTuplizerFactory();
         Class<? extends EntityTuplizer> tuplizerClass = entityBinding.getCustomEntityTuplizerClass();
         if (tuplizerClass == null) {
            this.entityTuplizer = entityTuplizerFactory.constructDefaultTuplizer(this.entityMode, this, entityBinding);
         } else {
            this.entityTuplizer = entityTuplizerFactory.constructTuplizer(tuplizerClass, this, entityBinding);
         }

      }
   }

   private ValueInclusion determineInsertValueGenerationType(Property mappingProperty, StandardProperty runtimeProperty) {
      if (runtimeProperty.isInsertGenerated()) {
         return ValueInclusion.FULL;
      } else {
         return mappingProperty.getValue() instanceof Component && this.hasPartialInsertComponentGeneration((Component)mappingProperty.getValue()) ? ValueInclusion.PARTIAL : ValueInclusion.NONE;
      }
   }

   private ValueInclusion determineInsertValueGenerationType(AttributeBinding mappingProperty, StandardProperty runtimeProperty) {
      return runtimeProperty.isInsertGenerated() ? ValueInclusion.FULL : ValueInclusion.NONE;
   }

   private boolean hasPartialInsertComponentGeneration(Component component) {
      Iterator subProperties = component.getPropertyIterator();

      while(subProperties.hasNext()) {
         Property prop = (Property)subProperties.next();
         if (prop.getGeneration() == PropertyGeneration.ALWAYS || prop.getGeneration() == PropertyGeneration.INSERT) {
            return true;
         }

         if (prop.getValue() instanceof Component && this.hasPartialInsertComponentGeneration((Component)prop.getValue())) {
            return true;
         }
      }

      return false;
   }

   private ValueInclusion determineUpdateValueGenerationType(Property mappingProperty, StandardProperty runtimeProperty) {
      if (runtimeProperty.isUpdateGenerated()) {
         return ValueInclusion.FULL;
      } else {
         return mappingProperty.getValue() instanceof Component && this.hasPartialUpdateComponentGeneration((Component)mappingProperty.getValue()) ? ValueInclusion.PARTIAL : ValueInclusion.NONE;
      }
   }

   private ValueInclusion determineUpdateValueGenerationType(AttributeBinding mappingProperty, StandardProperty runtimeProperty) {
      return runtimeProperty.isUpdateGenerated() ? ValueInclusion.FULL : ValueInclusion.NONE;
   }

   private boolean hasPartialUpdateComponentGeneration(Component component) {
      Iterator subProperties = component.getPropertyIterator();

      while(subProperties.hasNext()) {
         Property prop = (Property)subProperties.next();
         if (prop.getGeneration() == PropertyGeneration.ALWAYS) {
            return true;
         }

         if (prop.getValue() instanceof Component && this.hasPartialUpdateComponentGeneration((Component)prop.getValue())) {
            return true;
         }
      }

      return false;
   }

   private void mapPropertyToIndex(Property prop, int i) {
      this.propertyIndexes.put(prop.getName(), i);
      if (prop.getValue() instanceof Component) {
         Iterator iter = ((Component)prop.getValue()).getPropertyIterator();

         while(iter.hasNext()) {
            Property subprop = (Property)iter.next();
            this.propertyIndexes.put(prop.getName() + '.' + subprop.getName(), i);
         }
      }

   }

   private void mapPropertyToIndex(Attribute attribute, int i) {
      this.propertyIndexes.put(attribute.getName(), i);
      if (attribute.isSingular() && ((SingularAttribute)attribute).getSingularAttributeType().isComponent()) {
         org.hibernate.metamodel.domain.Component component = (org.hibernate.metamodel.domain.Component)((SingularAttribute)attribute).getSingularAttributeType();

         for(Attribute subAttribute : component.attributes()) {
            this.propertyIndexes.put(attribute.getName() + '.' + subAttribute.getName(), i);
         }
      }

   }

   public EntityTuplizer getTuplizer() {
      return this.entityTuplizer;
   }

   public int[] getNaturalIdentifierProperties() {
      return this.naturalIdPropertyNumbers;
   }

   public boolean hasNaturalIdentifier() {
      return this.naturalIdPropertyNumbers != null;
   }

   public boolean isNaturalIdentifierCached() {
      return this.hasNaturalIdentifier() && this.hasCacheableNaturalId;
   }

   public boolean hasImmutableNaturalId() {
      return this.hasImmutableNaturalId;
   }

   public Set getSubclassEntityNames() {
      return this.subclassEntityNames;
   }

   private boolean indicatesCollection(Type type) {
      if (type.isCollectionType()) {
         return true;
      } else {
         if (type.isComponentType()) {
            Type[] subtypes = ((CompositeType)type).getSubtypes();

            for(int i = 0; i < subtypes.length; ++i) {
               if (this.indicatesCollection(subtypes[i])) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   public SessionFactoryImplementor getSessionFactory() {
      return this.sessionFactory;
   }

   public String getName() {
      return this.name;
   }

   public String getRootName() {
      return this.rootName;
   }

   public EntityType getEntityType() {
      return this.entityType;
   }

   public IdentifierProperty getIdentifierProperty() {
      return this.identifierProperty;
   }

   public int getPropertySpan() {
      return this.propertySpan;
   }

   public int getVersionPropertyIndex() {
      return this.versionPropertyIndex;
   }

   public VersionProperty getVersionProperty() {
      return -66 == this.versionPropertyIndex ? null : (VersionProperty)this.properties[this.versionPropertyIndex];
   }

   public StandardProperty[] getProperties() {
      return this.properties;
   }

   public int getPropertyIndex(String propertyName) {
      Integer index = this.getPropertyIndexOrNull(propertyName);
      if (index == null) {
         throw new HibernateException("Unable to resolve property: " + propertyName);
      } else {
         return index;
      }
   }

   public Integer getPropertyIndexOrNull(String propertyName) {
      return (Integer)this.propertyIndexes.get(propertyName);
   }

   public boolean hasCollections() {
      return this.hasCollections;
   }

   public boolean hasMutableProperties() {
      return this.hasMutableProperties;
   }

   public boolean hasNonIdentifierPropertyNamedId() {
      return this.hasNonIdentifierPropertyNamedId;
   }

   public boolean hasLazyProperties() {
      return this.hasLazyProperties;
   }

   public boolean hasCascades() {
      return this.hasCascades;
   }

   public boolean isMutable() {
      return this.mutable;
   }

   public boolean isSelectBeforeUpdate() {
      return this.selectBeforeUpdate;
   }

   public boolean isDynamicUpdate() {
      return this.dynamicUpdate;
   }

   public boolean isDynamicInsert() {
      return this.dynamicInsert;
   }

   public OptimisticLockStyle getOptimisticLockStyle() {
      return this.optimisticLockStyle;
   }

   public boolean isPolymorphic() {
      return this.polymorphic;
   }

   public String getSuperclass() {
      return this.superclass;
   }

   public boolean isExplicitPolymorphism() {
      return this.explicitPolymorphism;
   }

   public boolean isInherited() {
      return this.inherited;
   }

   public boolean hasSubclasses() {
      return this.hasSubclasses;
   }

   public boolean isLazy() {
      return this.lazy;
   }

   public void setLazy(boolean lazy) {
      this.lazy = lazy;
   }

   public boolean isVersioned() {
      return this.versioned;
   }

   public boolean isAbstract() {
      return this.isAbstract;
   }

   public String findEntityNameByEntityClass(Class inheritenceClass) {
      return (String)this.entityNameByInheritenceClassMap.get(inheritenceClass);
   }

   public String toString() {
      return "EntityMetamodel(" + this.name + ':' + ArrayHelper.toString(this.properties) + ')';
   }

   public String[] getPropertyNames() {
      return this.propertyNames;
   }

   public Type[] getPropertyTypes() {
      return this.propertyTypes;
   }

   public boolean[] getPropertyLaziness() {
      return this.propertyLaziness;
   }

   public boolean[] getPropertyUpdateability() {
      return this.propertyUpdateability;
   }

   public boolean[] getPropertyCheckability() {
      return this.propertyCheckability;
   }

   public boolean[] getNonlazyPropertyUpdateability() {
      return this.nonlazyPropertyUpdateability;
   }

   public boolean[] getPropertyInsertability() {
      return this.propertyInsertability;
   }

   public ValueInclusion[] getPropertyInsertGenerationInclusions() {
      return this.insertInclusions;
   }

   public ValueInclusion[] getPropertyUpdateGenerationInclusions() {
      return this.updateInclusions;
   }

   public boolean[] getPropertyNullability() {
      return this.propertyNullability;
   }

   public boolean[] getPropertyVersionability() {
      return this.propertyVersionability;
   }

   public CascadeStyle[] getCascadeStyles() {
      return this.cascadeStyles;
   }

   public boolean hasInsertGeneratedValues() {
      return this.hasInsertGeneratedValues;
   }

   public boolean hasUpdateGeneratedValues() {
      return this.hasUpdateGeneratedValues;
   }

   public EntityMode getEntityMode() {
      return this.entityMode;
   }

   public boolean isInstrumented() {
      return this.instrumentationMetadata.isInstrumented();
   }

   public EntityInstrumentationMetadata getInstrumentationMetadata() {
      return this.instrumentationMetadata;
   }
}
