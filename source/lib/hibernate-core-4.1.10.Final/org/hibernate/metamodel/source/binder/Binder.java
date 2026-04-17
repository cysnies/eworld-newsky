package org.hibernate.metamodel.source.binder;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.AssertionFailure;
import org.hibernate.EntityMode;
import org.hibernate.cfg.NotYetImplementedException;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.ValueHolder;
import org.hibernate.internal.util.beans.BeanInfoHelper;
import org.hibernate.metamodel.binding.AbstractPluralAttributeBinding;
import org.hibernate.metamodel.binding.AttributeBinding;
import org.hibernate.metamodel.binding.AttributeBindingContainer;
import org.hibernate.metamodel.binding.BasicAttributeBinding;
import org.hibernate.metamodel.binding.BasicCollectionElement;
import org.hibernate.metamodel.binding.CollectionElementNature;
import org.hibernate.metamodel.binding.CollectionLaziness;
import org.hibernate.metamodel.binding.ComponentAttributeBinding;
import org.hibernate.metamodel.binding.EntityBinding;
import org.hibernate.metamodel.binding.EntityDiscriminator;
import org.hibernate.metamodel.binding.HibernateTypeDescriptor;
import org.hibernate.metamodel.binding.IdGenerator;
import org.hibernate.metamodel.binding.InheritanceType;
import org.hibernate.metamodel.binding.ManyToOneAttributeBinding;
import org.hibernate.metamodel.binding.MetaAttribute;
import org.hibernate.metamodel.binding.SimpleValueBinding;
import org.hibernate.metamodel.binding.SingularAttributeBinding;
import org.hibernate.metamodel.binding.TypeDef;
import org.hibernate.metamodel.domain.Component;
import org.hibernate.metamodel.domain.Entity;
import org.hibernate.metamodel.domain.Hierarchical;
import org.hibernate.metamodel.domain.PluralAttribute;
import org.hibernate.metamodel.domain.SingularAttribute;
import org.hibernate.metamodel.relational.Column;
import org.hibernate.metamodel.relational.DerivedValue;
import org.hibernate.metamodel.relational.Identifier;
import org.hibernate.metamodel.relational.Schema;
import org.hibernate.metamodel.relational.SimpleValue;
import org.hibernate.metamodel.relational.Table;
import org.hibernate.metamodel.relational.TableSpecification;
import org.hibernate.metamodel.relational.Tuple;
import org.hibernate.metamodel.relational.UniqueKey;
import org.hibernate.metamodel.relational.Value;
import org.hibernate.metamodel.source.LocalBindingContext;
import org.hibernate.metamodel.source.MappingException;
import org.hibernate.metamodel.source.MetaAttributeContext;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.metamodel.source.hbm.Helper;

public class Binder {
   private final MetadataImplementor metadata;
   private final List processedEntityNames;
   private InheritanceType currentInheritanceType;
   private EntityMode currentHierarchyEntityMode;
   private LocalBindingContext currentBindingContext;

   public Binder(MetadataImplementor metadata, List processedEntityNames) {
      this.metadata = metadata;
      this.processedEntityNames = processedEntityNames;
   }

   public void processEntityHierarchy(EntityHierarchy entityHierarchy) {
      this.currentInheritanceType = entityHierarchy.getHierarchyInheritanceType();
      EntityBinding rootEntityBinding = this.createEntityBinding(entityHierarchy.getRootEntitySource(), (EntityBinding)null);
      if (this.currentInheritanceType != InheritanceType.NO_INHERITANCE) {
         this.processHierarchySubEntities(entityHierarchy.getRootEntitySource(), rootEntityBinding);
      }

      this.currentHierarchyEntityMode = null;
   }

   private void processHierarchySubEntities(SubclassEntityContainer subclassEntitySource, EntityBinding superEntityBinding) {
      for(SubclassEntitySource subEntity : subclassEntitySource.subclassEntitySources()) {
         EntityBinding entityBinding = this.createEntityBinding(subEntity, superEntityBinding);
         this.processHierarchySubEntities(subEntity, entityBinding);
      }

   }

   private EntityBinding createEntityBinding(EntitySource entitySource, EntityBinding superEntityBinding) {
      if (this.processedEntityNames.contains(entitySource.getEntityName())) {
         return this.metadata.getEntityBinding(entitySource.getEntityName());
      } else {
         this.currentBindingContext = entitySource.getLocalBindingContext();

         EntityBinding var4;
         try {
            EntityBinding entityBinding = this.doCreateEntityBinding(entitySource, superEntityBinding);
            this.metadata.addEntity(entityBinding);
            this.processedEntityNames.add(entityBinding.getEntity().getName());
            this.processFetchProfiles(entitySource, entityBinding);
            var4 = entityBinding;
         } finally {
            this.currentBindingContext = null;
         }

         return var4;
      }
   }

   private EntityBinding doCreateEntityBinding(EntitySource entitySource, EntityBinding superEntityBinding) {
      EntityBinding entityBinding = this.createBasicEntityBinding(entitySource, superEntityBinding);
      this.bindSecondaryTables(entitySource, entityBinding);
      this.bindAttributes(entitySource, entityBinding);
      this.bindTableUniqueConstraints(entitySource, entityBinding);
      return entityBinding;
   }

   private EntityBinding createBasicEntityBinding(EntitySource entitySource, EntityBinding superEntityBinding) {
      if (superEntityBinding == null) {
         return this.makeRootEntityBinding((RootEntitySource)entitySource);
      } else {
         switch (this.currentInheritanceType) {
            case SINGLE_TABLE:
               return this.makeDiscriminatedSubclassBinding((SubclassEntitySource)entitySource, superEntityBinding);
            case JOINED:
               return this.makeJoinedSubclassBinding((SubclassEntitySource)entitySource, superEntityBinding);
            case TABLE_PER_CLASS:
               return this.makeUnionedSubclassBinding((SubclassEntitySource)entitySource, superEntityBinding);
            default:
               throw new AssertionFailure("Internal condition failure");
         }
      }
   }

   private EntityBinding makeRootEntityBinding(RootEntitySource entitySource) {
      this.currentHierarchyEntityMode = entitySource.getEntityMode();
      EntityBinding entityBinding = this.buildBasicEntityBinding(entitySource, (EntityBinding)null);
      this.bindPrimaryTable(entitySource, entityBinding);
      this.bindIdentifier(entitySource, entityBinding);
      this.bindVersion(entityBinding, entitySource);
      this.bindDiscriminator(entitySource, entityBinding);
      entityBinding.getHierarchyDetails().setCaching(entitySource.getCaching());
      entityBinding.getHierarchyDetails().setExplicitPolymorphism(entitySource.isExplicitPolymorphism());
      entityBinding.getHierarchyDetails().setOptimisticLockStyle(entitySource.getOptimisticLockStyle());
      entityBinding.setMutable(entitySource.isMutable());
      entityBinding.setWhereFilter(entitySource.getWhere());
      entityBinding.setRowId(entitySource.getRowId());
      return entityBinding;
   }

   private EntityBinding buildBasicEntityBinding(EntitySource entitySource, EntityBinding superEntityBinding) {
      EntityBinding entityBinding = superEntityBinding == null ? new EntityBinding(this.currentInheritanceType, this.currentHierarchyEntityMode) : new EntityBinding(superEntityBinding);
      String entityName = entitySource.getEntityName();
      String className = this.currentHierarchyEntityMode == EntityMode.POJO ? entitySource.getClassName() : null;
      Entity entity = new Entity(entityName, className, this.currentBindingContext.makeClassReference(className), superEntityBinding == null ? null : superEntityBinding.getEntity());
      entityBinding.setEntity(entity);
      entityBinding.setJpaEntityName(entitySource.getJpaEntityName());
      if (this.currentHierarchyEntityMode == EntityMode.POJO) {
         String proxy = entitySource.getProxy();
         if (proxy != null) {
            entityBinding.setProxyInterfaceType(this.currentBindingContext.makeClassReference(this.currentBindingContext.qualifyClassName(proxy)));
            entityBinding.setLazy(true);
         } else if (entitySource.isLazy()) {
            entityBinding.setProxyInterfaceType(entityBinding.getEntity().getClassReferenceUnresolved());
            entityBinding.setLazy(true);
         }
      } else {
         entityBinding.setProxyInterfaceType((ValueHolder)null);
         entityBinding.setLazy(entitySource.isLazy());
      }

      String customTuplizerClassName = entitySource.getCustomTuplizerClassName();
      if (customTuplizerClassName != null) {
         entityBinding.setCustomEntityTuplizerClass(this.currentBindingContext.locateClassByName(customTuplizerClassName));
      }

      String customPersisterClassName = entitySource.getCustomPersisterClassName();
      if (customPersisterClassName != null) {
         entityBinding.setCustomEntityPersisterClass(this.currentBindingContext.locateClassByName(customPersisterClassName));
      }

      entityBinding.setMetaAttributeContext(this.buildMetaAttributeContext(entitySource));
      entityBinding.setDynamicUpdate(entitySource.isDynamicUpdate());
      entityBinding.setDynamicInsert(entitySource.isDynamicInsert());
      entityBinding.setBatchSize(entitySource.getBatchSize());
      entityBinding.setSelectBeforeUpdate(entitySource.isSelectBeforeUpdate());
      entityBinding.setAbstract(entitySource.isAbstract());
      entityBinding.setCustomLoaderName(entitySource.getCustomLoaderName());
      entityBinding.setCustomInsert(entitySource.getCustomSqlInsert());
      entityBinding.setCustomUpdate(entitySource.getCustomSqlUpdate());
      entityBinding.setCustomDelete(entitySource.getCustomSqlDelete());
      if (entitySource.getSynchronizedTableNames() != null) {
         entityBinding.addSynchronizedTableNames(entitySource.getSynchronizedTableNames());
      }

      entityBinding.setJpaCallbackClasses(entitySource.getJpaCallbackClasses());
      return entityBinding;
   }

   private EntityBinding makeDiscriminatedSubclassBinding(SubclassEntitySource entitySource, EntityBinding superEntityBinding) {
      EntityBinding entityBinding = this.buildBasicEntityBinding(entitySource, superEntityBinding);
      entityBinding.setPrimaryTable(superEntityBinding.getPrimaryTable());
      entityBinding.setPrimaryTableName(superEntityBinding.getPrimaryTableName());
      this.bindDiscriminatorValue(entitySource, entityBinding);
      return entityBinding;
   }

   private EntityBinding makeJoinedSubclassBinding(SubclassEntitySource entitySource, EntityBinding superEntityBinding) {
      EntityBinding entityBinding = this.buildBasicEntityBinding(entitySource, superEntityBinding);
      this.bindPrimaryTable(entitySource, entityBinding);
      return entityBinding;
   }

   private EntityBinding makeUnionedSubclassBinding(SubclassEntitySource entitySource, EntityBinding superEntityBinding) {
      EntityBinding entityBinding = this.buildBasicEntityBinding(entitySource, superEntityBinding);
      this.bindPrimaryTable(entitySource, entityBinding);
      return entityBinding;
   }

   private void bindIdentifier(RootEntitySource entitySource, EntityBinding entityBinding) {
      if (entitySource.getIdentifierSource() == null) {
         throw new AssertionFailure("Expecting identifier information on root entity descriptor");
      } else {
         switch (entitySource.getIdentifierSource().getNature()) {
            case SIMPLE:
               this.bindSimpleIdentifier((SimpleIdentifierSource)entitySource.getIdentifierSource(), entityBinding);
            case AGGREGATED_COMPOSITE:
            case COMPOSITE:
            default:
         }
      }
   }

   private void bindSimpleIdentifier(SimpleIdentifierSource identifierSource, EntityBinding entityBinding) {
      BasicAttributeBinding idAttributeBinding = this.doBasicSingularAttributeBindingCreation(identifierSource.getIdentifierAttributeSource(), entityBinding);
      entityBinding.getHierarchyDetails().getEntityIdentifier().setValueBinding(idAttributeBinding);
      IdGenerator generator = identifierSource.getIdentifierGeneratorDescriptor();
      if (generator == null) {
         Map<String, String> params = new HashMap();
         params.put("entity_name", entityBinding.getEntity().getName());
         generator = new IdGenerator("default_assign_identity_generator", "assigned", params);
      }

      entityBinding.getHierarchyDetails().getEntityIdentifier().setIdGenerator(generator);
      Value relationalValue = idAttributeBinding.getValue();
      if (SimpleValue.class.isInstance(relationalValue)) {
         if (!Column.class.isInstance(relationalValue)) {
            throw new AssertionFailure("Simple-id was not a column.");
         }

         entityBinding.getPrimaryTable().getPrimaryKey().addColumn((Column)Column.class.cast(relationalValue));
      } else {
         for(SimpleValue subValue : ((Tuple)relationalValue).values()) {
            if (Column.class.isInstance(subValue)) {
               entityBinding.getPrimaryTable().getPrimaryKey().addColumn((Column)Column.class.cast(subValue));
            }
         }
      }

   }

   private void bindVersion(EntityBinding entityBinding, RootEntitySource entitySource) {
      SingularAttributeSource versioningAttributeSource = entitySource.getVersioningAttributeSource();
      if (versioningAttributeSource != null) {
         BasicAttributeBinding attributeBinding = this.doBasicSingularAttributeBindingCreation(versioningAttributeSource, entityBinding);
         entityBinding.getHierarchyDetails().setVersioningAttributeBinding(attributeBinding);
      }
   }

   private void bindDiscriminator(RootEntitySource entitySource, EntityBinding entityBinding) {
      DiscriminatorSource discriminatorSource = entitySource.getDiscriminatorSource();
      if (discriminatorSource != null) {
         EntityDiscriminator discriminator = new EntityDiscriminator();
         SimpleValue relationalValue = this.makeSimpleValue(entityBinding, discriminatorSource.getDiscriminatorRelationalValueSource());
         discriminator.setBoundValue(relationalValue);
         discriminator.getExplicitHibernateTypeDescriptor().setExplicitTypeName(discriminatorSource.getExplicitHibernateTypeName() != null ? discriminatorSource.getExplicitHibernateTypeName() : "string");
         discriminator.setInserted(discriminatorSource.isInserted());
         discriminator.setForced(discriminatorSource.isForced());
         entityBinding.getHierarchyDetails().setEntityDiscriminator(discriminator);
         entityBinding.setDiscriminatorMatchValue(entitySource.getDiscriminatorMatchValue());
      }
   }

   private void bindDiscriminatorValue(SubclassEntitySource entitySource, EntityBinding entityBinding) {
      String discriminatorValue = entitySource.getDiscriminatorMatchValue();
      if (discriminatorValue != null) {
         entityBinding.setDiscriminatorMatchValue(discriminatorValue);
      }
   }

   private void bindAttributes(AttributeSourceContainer attributeSourceContainer, AttributeBindingContainer attributeBindingContainer) {
      for(AttributeSource attributeSource : attributeSourceContainer.attributeSources()) {
         if (attributeSource.isSingular()) {
            SingularAttributeSource singularAttributeSource = (SingularAttributeSource)attributeSource;
            if (singularAttributeSource.getNature() == SingularAttributeNature.COMPONENT) {
               this.bindComponent((ComponentAttributeSource)singularAttributeSource, attributeBindingContainer);
            } else {
               this.doBasicSingularAttributeBindingCreation(singularAttributeSource, attributeBindingContainer);
            }
         } else {
            this.bindPersistentCollection((PluralAttributeSource)attributeSource, attributeBindingContainer);
         }
      }

   }

   private void bindComponent(ComponentAttributeSource attributeSource, AttributeBindingContainer container) {
      String attributeName = attributeSource.getName();
      SingularAttribute attribute = container.getAttributeContainer().locateComponentAttribute(attributeName);
      if (attribute == null) {
         Component component = new Component(attributeSource.getPath(), attributeSource.getClassName(), attributeSource.getClassReference(), (Hierarchical)null);
         attribute = container.getAttributeContainer().createComponentAttribute(attributeName, component);
      }

      ComponentAttributeBinding componentAttributeBinding = container.makeComponentAttributeBinding(attribute);
      if (StringHelper.isNotEmpty(attributeSource.getParentReferenceAttributeName())) {
         SingularAttribute parentReferenceAttribute = componentAttributeBinding.getComponent().createSingularAttribute(attributeSource.getParentReferenceAttributeName());
         componentAttributeBinding.setParentReference(parentReferenceAttribute);
      }

      componentAttributeBinding.setMetaAttributeContext(buildMetaAttributeContext(attributeSource.metaAttributes(), container.getMetaAttributeContext()));
      this.bindAttributes(attributeSource, componentAttributeBinding);
   }

   private void bindPersistentCollection(PluralAttributeSource attributeSource, AttributeBindingContainer attributeBindingContainer) {
      PluralAttribute existingAttribute = attributeBindingContainer.getAttributeContainer().locatePluralAttribute(attributeSource.getName());
      AbstractPluralAttributeBinding pluralAttributeBinding;
      if (attributeSource.getPluralAttributeNature() == PluralAttributeNature.BAG) {
         PluralAttribute attribute = existingAttribute != null ? existingAttribute : attributeBindingContainer.getAttributeContainer().createBag(attributeSource.getName());
         pluralAttributeBinding = attributeBindingContainer.makeBagAttributeBinding(attribute, this.convert(attributeSource.getElementSource().getNature()));
      } else {
         if (attributeSource.getPluralAttributeNature() != PluralAttributeNature.SET) {
            throw new NotYetImplementedException("Collections other than bag and set not yet implemented :(");
         }

         PluralAttribute attribute = existingAttribute != null ? existingAttribute : attributeBindingContainer.getAttributeContainer().createSet(attributeSource.getName());
         pluralAttributeBinding = attributeBindingContainer.makeSetAttributeBinding(attribute, this.convert(attributeSource.getElementSource().getNature()));
      }

      this.doBasicPluralAttributeBinding(attributeSource, pluralAttributeBinding);
      this.bindCollectionTable(attributeSource, pluralAttributeBinding);
      this.bindSortingAndOrdering(attributeSource, pluralAttributeBinding);
      this.bindCollectionKey(attributeSource, pluralAttributeBinding);
      this.bindCollectionElement(attributeSource, pluralAttributeBinding);
      this.bindCollectionIndex(attributeSource, pluralAttributeBinding);
      this.metadata.addCollection(pluralAttributeBinding);
   }

   private void doBasicPluralAttributeBinding(PluralAttributeSource source, AbstractPluralAttributeBinding binding) {
      binding.setFetchTiming(source.getFetchTiming());
      binding.setFetchStyle(source.getFetchStyle());
      binding.setCascadeStyles(source.getCascadeStyles());
      binding.setCaching(source.getCaching());
      binding.getHibernateTypeDescriptor().setJavaTypeName(source.getPluralAttributeNature().reportedJavaType().getName());
      binding.getHibernateTypeDescriptor().setExplicitTypeName(source.getTypeInformation().getName());
      binding.getHibernateTypeDescriptor().getTypeParameters().putAll(source.getTypeInformation().getParameters());
      if (StringHelper.isNotEmpty(source.getCustomPersisterClassName())) {
         binding.setCollectionPersisterClass(this.currentBindingContext.locateClassByName(source.getCustomPersisterClassName()));
      }

      if (source.getCustomPersisterClassName() != null) {
         binding.setCollectionPersisterClass(this.metadata.locateClassByName(source.getCustomPersisterClassName()));
      }

      binding.setCustomLoaderName(source.getCustomLoaderName());
      binding.setCustomSqlInsert(source.getCustomSqlInsert());
      binding.setCustomSqlUpdate(source.getCustomSqlUpdate());
      binding.setCustomSqlDelete(source.getCustomSqlDelete());
      binding.setCustomSqlDeleteAll(source.getCustomSqlDeleteAll());
      binding.setMetaAttributeContext(buildMetaAttributeContext(source.metaAttributes(), binding.getContainer().getMetaAttributeContext()));
      this.doBasicAttributeBinding(source, binding);
   }

   private CollectionLaziness interpretLaziness(String laziness) {
      if (laziness == null) {
         laziness = Boolean.toString(this.metadata.getMappingDefaults().areAssociationsLazy());
      }

      if ("extra".equals(laziness)) {
         return CollectionLaziness.EXTRA;
      } else if ("false".equals(laziness)) {
         return CollectionLaziness.NOT;
      } else if ("true".equals(laziness)) {
         return CollectionLaziness.LAZY;
      } else {
         throw new MappingException(String.format("Unexpected collection laziness value %s", laziness), this.currentBindingContext.getOrigin());
      }
   }

   private void bindCollectionTable(PluralAttributeSource attributeSource, AbstractPluralAttributeBinding pluralAttributeBinding) {
      if (attributeSource.getElementSource().getNature() != PluralAttributeElementNature.ONE_TO_MANY) {
         Schema.Name schemaName = Helper.determineDatabaseSchemaName(attributeSource.getExplicitSchemaName(), attributeSource.getExplicitCatalogName(), this.currentBindingContext);
         Schema schema = this.metadata.getDatabase().locateSchema(schemaName);
         String tableName = attributeSource.getExplicitCollectionTableName();
         if (StringHelper.isNotEmpty(tableName)) {
            Identifier tableIdentifier = Identifier.toIdentifier(this.currentBindingContext.getNamingStrategy().tableName(tableName));
            Table collectionTable = schema.locateTable(tableIdentifier);
            if (collectionTable == null) {
               collectionTable = schema.createTable(tableIdentifier);
            }

            pluralAttributeBinding.setCollectionTable(collectionTable);
         } else {
            EntityBinding owner = pluralAttributeBinding.getContainer().seekEntityBinding();
            String ownerTableLogicalName = Table.class.isInstance(owner.getPrimaryTable()) ? ((Table)Table.class.cast(owner.getPrimaryTable())).getTableName().getName() : null;
            String collectionTableName = this.currentBindingContext.getNamingStrategy().collectionTableName(owner.getEntity().getName(), ownerTableLogicalName, (String)null, (String)null, pluralAttributeBinding.getContainer().getPathBase() + '.' + attributeSource.getName());
            collectionTableName = this.quoteIdentifier(collectionTableName);
            pluralAttributeBinding.setCollectionTable(schema.locateOrCreateTable(Identifier.toIdentifier(collectionTableName)));
         }

         if (StringHelper.isNotEmpty(attributeSource.getCollectionTableComment())) {
            pluralAttributeBinding.getCollectionTable().addComment(attributeSource.getCollectionTableComment());
         }

         if (StringHelper.isNotEmpty(attributeSource.getCollectionTableCheck())) {
            pluralAttributeBinding.getCollectionTable().addCheckConstraint(attributeSource.getCollectionTableCheck());
         }

         pluralAttributeBinding.setWhere(attributeSource.getWhere());
      }
   }

   private void bindCollectionKey(PluralAttributeSource attributeSource, AbstractPluralAttributeBinding pluralAttributeBinding) {
      pluralAttributeBinding.getCollectionKey().prepareForeignKey(attributeSource.getKeySource().getExplicitForeignKeyName(), (String)null);
      pluralAttributeBinding.getCollectionKey().getForeignKey().setDeleteRule(attributeSource.getKeySource().getOnDeleteAction());
   }

   private void bindCollectionElement(PluralAttributeSource attributeSource, AbstractPluralAttributeBinding pluralAttributeBinding) {
      PluralAttributeElementSource elementSource = attributeSource.getElementSource();
      if (elementSource.getNature() == PluralAttributeElementNature.BASIC) {
         BasicPluralAttributeElementSource basicElementSource = (BasicPluralAttributeElementSource)elementSource;
         BasicCollectionElement basicCollectionElement = (BasicCollectionElement)pluralAttributeBinding.getCollectionElement();
         this.resolveTypeInformation(basicElementSource.getExplicitHibernateTypeSource(), pluralAttributeBinding.getAttribute(), basicCollectionElement);
      } else {
         throw new NotYetImplementedException(String.format("Support for collection elements of type %s not yet implemented", elementSource.getNature()));
      }
   }

   private void bindCollectionIndex(PluralAttributeSource attributeSource, AbstractPluralAttributeBinding pluralAttributeBinding) {
      if (attributeSource.getPluralAttributeNature() == PluralAttributeNature.LIST || attributeSource.getPluralAttributeNature() == PluralAttributeNature.MAP) {
         throw new NotYetImplementedException();
      }
   }

   private void bindSortingAndOrdering(PluralAttributeSource attributeSource, AbstractPluralAttributeBinding pluralAttributeBinding) {
      if (Sortable.class.isInstance(attributeSource)) {
         Sortable sortable = (Sortable)Sortable.class.cast(attributeSource);
         if (sortable.isSorted()) {
            return;
         }
      }

      if (Orderable.class.isInstance(attributeSource)) {
         Orderable orderable = (Orderable)Orderable.class.cast(attributeSource);
         if (orderable.isOrdered()) {
         }
      }

   }

   private void doBasicAttributeBinding(AttributeSource attributeSource, AttributeBinding attributeBinding) {
      attributeBinding.setPropertyAccessorName(attributeSource.getPropertyAccessorName());
      attributeBinding.setIncludedInOptimisticLocking(attributeSource.isIncludedInOptimisticLocking());
   }

   private CollectionElementNature convert(PluralAttributeElementNature pluralAttributeElementNature) {
      return CollectionElementNature.valueOf(pluralAttributeElementNature.name());
   }

   private BasicAttributeBinding doBasicSingularAttributeBindingCreation(SingularAttributeSource attributeSource, AttributeBindingContainer attributeBindingContainer) {
      SingularAttribute existingAttribute = attributeBindingContainer.getAttributeContainer().locateSingularAttribute(attributeSource.getName());
      SingularAttribute attribute;
      if (existingAttribute != null) {
         attribute = existingAttribute;
      } else if (attributeSource.isVirtualAttribute()) {
         attribute = attributeBindingContainer.getAttributeContainer().createVirtualSingularAttribute(attributeSource.getName());
      } else {
         attribute = attributeBindingContainer.getAttributeContainer().createSingularAttribute(attributeSource.getName());
      }

      BasicAttributeBinding attributeBinding;
      if (attributeSource.getNature() == SingularAttributeNature.BASIC) {
         attributeBinding = attributeBindingContainer.makeBasicAttributeBinding(attribute);
         this.resolveTypeInformation(attributeSource.getTypeInformation(), attributeBinding);
      } else {
         if (attributeSource.getNature() != SingularAttributeNature.MANY_TO_ONE) {
            throw new NotYetImplementedException();
         }

         attributeBinding = attributeBindingContainer.makeManyToOneAttributeBinding(attribute);
         this.resolveTypeInformation(attributeSource.getTypeInformation(), attributeBinding);
         this.resolveToOneInformation((ToOneAttributeSource)attributeSource, (ManyToOneAttributeBinding)attributeBinding);
      }

      attributeBinding.setGeneration(attributeSource.getGeneration());
      attributeBinding.setLazy(attributeSource.isLazy());
      attributeBinding.setIncludedInOptimisticLocking(attributeSource.isIncludedInOptimisticLocking());
      attributeBinding.setPropertyAccessorName(Helper.getPropertyAccessorName(attributeSource.getPropertyAccessorName(), false, this.currentBindingContext.getMappingDefaults().getPropertyAccessorName()));
      this.bindRelationalValues(attributeSource, attributeBinding);
      attributeBinding.setMetaAttributeContext(buildMetaAttributeContext(attributeSource.metaAttributes(), attributeBindingContainer.getMetaAttributeContext()));
      return attributeBinding;
   }

   private void resolveTypeInformation(ExplicitHibernateTypeSource typeSource, BasicAttributeBinding attributeBinding) {
      Class<?> attributeJavaType = this.determineJavaType(attributeBinding.getAttribute());
      if (attributeJavaType != null) {
         attributeBinding.getAttribute().resolveType(this.currentBindingContext.makeJavaType(attributeJavaType.getName()));
      }

      this.resolveTypeInformation(typeSource, attributeBinding.getHibernateTypeDescriptor(), attributeJavaType);
   }

   private void resolveTypeInformation(ExplicitHibernateTypeSource typeSource, PluralAttribute attribute, BasicCollectionElement collectionElement) {
      Class<?> attributeJavaType = this.determineJavaType(attribute);
      this.resolveTypeInformation(typeSource, collectionElement.getHibernateTypeDescriptor(), attributeJavaType);
   }

   private void resolveTypeInformation(ExplicitHibernateTypeSource typeSource, HibernateTypeDescriptor hibernateTypeDescriptor, Class discoveredJavaType) {
      if (discoveredJavaType != null) {
         hibernateTypeDescriptor.setJavaTypeName(discoveredJavaType.getName());
      }

      String explicitTypeName = typeSource.getName();
      if (explicitTypeName != null) {
         TypeDef typeDef = this.currentBindingContext.getMetadataImplementor().getTypeDefinition(explicitTypeName);
         if (typeDef != null) {
            hibernateTypeDescriptor.setExplicitTypeName(typeDef.getTypeClass());
            hibernateTypeDescriptor.getTypeParameters().putAll(typeDef.getParameters());
         } else {
            hibernateTypeDescriptor.setExplicitTypeName(explicitTypeName);
         }

         Map<String, String> parameters = typeSource.getParameters();
         if (parameters != null) {
            hibernateTypeDescriptor.getTypeParameters().putAll(parameters);
         }
      } else if (discoveredJavaType == null) {
      }

   }

   private Class determineJavaType(SingularAttribute attribute) {
      try {
         Class<?> ownerClass = attribute.getAttributeContainer().getClassReference();
         AttributeJavaTypeDeterminerDelegate delegate = new AttributeJavaTypeDeterminerDelegate(attribute.getName());
         BeanInfoHelper.visitBeanInfo(ownerClass, delegate);
         return delegate.javaType;
      } catch (Exception var4) {
         return null;
      }
   }

   private Class determineJavaType(PluralAttribute attribute) {
      try {
         Class<?> ownerClass = attribute.getAttributeContainer().getClassReference();
         PluralAttributeJavaTypeDeterminerDelegate delegate = new PluralAttributeJavaTypeDeterminerDelegate(ownerClass, attribute.getName());
         BeanInfoHelper.visitBeanInfo(ownerClass, delegate);
         return delegate.javaType;
      } catch (Exception var4) {
         return null;
      }
   }

   private void resolveToOneInformation(ToOneAttributeSource attributeSource, ManyToOneAttributeBinding attributeBinding) {
      String referencedEntityName = attributeSource.getReferencedEntityName() != null ? attributeSource.getReferencedEntityName() : attributeBinding.getAttribute().getSingularAttributeType().getClassName();
      attributeBinding.setReferencedEntityName(referencedEntityName);
      attributeBinding.setReferencedAttributeName(attributeSource.getReferencedEntityAttributeName());
      attributeBinding.setCascadeStyles(attributeSource.getCascadeStyles());
      attributeBinding.setFetchTiming(attributeSource.getFetchTiming());
      attributeBinding.setFetchStyle(attributeSource.getFetchStyle());
   }

   private MetaAttributeContext buildMetaAttributeContext(EntitySource entitySource) {
      return buildMetaAttributeContext(entitySource.metaAttributes(), true, this.currentBindingContext.getMetadataImplementor().getGlobalMetaAttributeContext());
   }

   private static MetaAttributeContext buildMetaAttributeContext(Iterable metaAttributeSources, MetaAttributeContext parentContext) {
      return buildMetaAttributeContext(metaAttributeSources, false, parentContext);
   }

   private static MetaAttributeContext buildMetaAttributeContext(Iterable metaAttributeSources, boolean onlyInheritable, MetaAttributeContext parentContext) {
      MetaAttributeContext subContext = new MetaAttributeContext(parentContext);

      for(MetaAttributeSource metaAttributeSource : metaAttributeSources) {
         if (!(onlyInheritable & !metaAttributeSource.isInheritable())) {
            String name = metaAttributeSource.getName();
            MetaAttribute inheritedMetaAttribute = parentContext.getMetaAttribute(name);
            MetaAttribute metaAttribute = subContext.getLocalMetaAttribute(name);
            if (metaAttribute == null || metaAttribute == inheritedMetaAttribute) {
               metaAttribute = new MetaAttribute(name);
               subContext.add(metaAttribute);
            }

            metaAttribute.addValue(metaAttributeSource.getValue());
         }
      }

      return subContext;
   }

   private void bindPrimaryTable(EntitySource entitySource, EntityBinding entityBinding) {
      TableSource tableSource = entitySource.getPrimaryTable();
      Table table = this.createTable(entityBinding, tableSource);
      entityBinding.setPrimaryTable(table);
      entityBinding.setPrimaryTableName(table.getTableName().getName());
   }

   private void bindSecondaryTables(EntitySource entitySource, EntityBinding entityBinding) {
      for(TableSource secondaryTableSource : entitySource.getSecondaryTables()) {
         Table table = this.createTable(entityBinding, secondaryTableSource);
         entityBinding.addSecondaryTable(secondaryTableSource.getLogicalName(), table);
      }

   }

   private Table createTable(EntityBinding entityBinding, TableSource tableSource) {
      String tableName = tableSource.getExplicitTableName();
      if (StringHelper.isEmpty(tableName)) {
         tableName = this.currentBindingContext.getNamingStrategy().classToTableName(entityBinding.getEntity().getClassName());
      } else {
         tableName = this.currentBindingContext.getNamingStrategy().tableName(tableName);
      }

      tableName = this.quoteIdentifier(tableName);
      Schema.Name databaseSchemaName = Helper.determineDatabaseSchemaName(tableSource.getExplicitSchemaName(), tableSource.getExplicitCatalogName(), this.currentBindingContext);
      return this.currentBindingContext.getMetadataImplementor().getDatabase().locateSchema(databaseSchemaName).locateOrCreateTable(Identifier.toIdentifier(tableName));
   }

   private void bindTableUniqueConstraints(EntitySource entitySource, EntityBinding entityBinding) {
      for(ConstraintSource constraintSource : entitySource.getConstraints()) {
         if (constraintSource instanceof UniqueConstraintSource) {
            TableSpecification table = entityBinding.locateTable(constraintSource.getTableName());
            if (table == null) {
            }

            String constraintName = constraintSource.name();
            if (constraintName == null) {
            }

            UniqueKey uniqueKey = table.getOrCreateUniqueKey(constraintName);

            for(String columnName : constraintSource.columnNames()) {
               uniqueKey.addColumn(table.locateOrCreateColumn(this.quoteIdentifier(columnName)));
            }
         }
      }

   }

   private void bindRelationalValues(RelationalValueSourceContainer relationalValueSourceContainer, SingularAttributeBinding attributeBinding) {
      List<SimpleValueBinding> valueBindings = new ArrayList();
      if (!relationalValueSourceContainer.relationalValueSources().isEmpty()) {
         for(RelationalValueSource valueSource : relationalValueSourceContainer.relationalValueSources()) {
            TableSpecification table = attributeBinding.getContainer().seekEntityBinding().locateTable(valueSource.getContainingTableName());
            if (ColumnSource.class.isInstance(valueSource)) {
               ColumnSource columnSource = (ColumnSource)ColumnSource.class.cast(valueSource);
               Column column = this.makeColumn((ColumnSource)valueSource, table);
               valueBindings.add(new SimpleValueBinding(column, columnSource.isIncludedInInsert(), columnSource.isIncludedInUpdate()));
            } else {
               valueBindings.add(new SimpleValueBinding(this.makeDerivedValue((DerivedValueSource)valueSource, table)));
            }
         }
      } else {
         String name = this.metadata.getOptions().getNamingStrategy().propertyToColumnName(attributeBinding.getAttribute().getName());
         name = this.quoteIdentifier(name);
         Column column = attributeBinding.getContainer().seekEntityBinding().getPrimaryTable().locateOrCreateColumn(name);
         column.setNullable(relationalValueSourceContainer.areValuesNullableByDefault());
         valueBindings.add(new SimpleValueBinding(column, relationalValueSourceContainer.areValuesIncludedInInsertByDefault(), relationalValueSourceContainer.areValuesIncludedInUpdateByDefault()));
      }

      attributeBinding.setSimpleValueBindings(valueBindings);
   }

   private String quoteIdentifier(String identifier) {
      return this.currentBindingContext.isGloballyQuotedIdentifiers() ? StringHelper.quote(identifier) : identifier;
   }

   private SimpleValue makeSimpleValue(EntityBinding entityBinding, RelationalValueSource valueSource) {
      TableSpecification table = entityBinding.locateTable(valueSource.getContainingTableName());
      return (SimpleValue)(ColumnSource.class.isInstance(valueSource) ? this.makeColumn((ColumnSource)valueSource, table) : this.makeDerivedValue((DerivedValueSource)valueSource, table));
   }

   private Column makeColumn(ColumnSource columnSource, TableSpecification table) {
      String name = columnSource.getName();
      name = this.metadata.getOptions().getNamingStrategy().columnName(name);
      name = this.quoteIdentifier(name);
      Column column = table.locateOrCreateColumn(name);
      column.setNullable(columnSource.isNullable());
      column.setDefaultValue(columnSource.getDefaultValue());
      column.setSqlType(columnSource.getSqlType());
      column.setSize(columnSource.getSize());
      column.setDatatype(columnSource.getDatatype());
      column.setReadFragment(columnSource.getReadFragment());
      column.setWriteFragment(columnSource.getWriteFragment());
      column.setUnique(columnSource.isUnique());
      column.setCheckCondition(columnSource.getCheckCondition());
      column.setComment(columnSource.getComment());
      return column;
   }

   private DerivedValue makeDerivedValue(DerivedValueSource derivedValueSource, TableSpecification table) {
      return table.locateOrCreateDerivedValue(derivedValueSource.getExpression());
   }

   private void processFetchProfiles(EntitySource entitySource, EntityBinding entityBinding) {
   }

   private class PluralAttributeJavaTypeDeterminerDelegate implements BeanInfoHelper.BeanInfoDelegate {
      private final Class ownerClass;
      private final String attributeName;
      private Class javaType;

      private PluralAttributeJavaTypeDeterminerDelegate(Class ownerClass, String attributeName) {
         this.javaType = null;
         this.ownerClass = ownerClass;
         this.attributeName = attributeName;
      }

      public void processBeanInfo(BeanInfo beanInfo) throws Exception {
         for(PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
            if (propertyDescriptor.getName().equals(this.attributeName)) {
               this.javaType = this.extractCollectionComponentType(beanInfo, propertyDescriptor);
               break;
            }
         }

      }

      private Class extractCollectionComponentType(BeanInfo beanInfo, PropertyDescriptor propertyDescriptor) {
         Type collectionAttributeType;
         if (propertyDescriptor.getReadMethod() != null) {
            collectionAttributeType = propertyDescriptor.getReadMethod().getGenericReturnType();
         } else if (propertyDescriptor.getWriteMethod() != null) {
            collectionAttributeType = propertyDescriptor.getWriteMethod().getGenericParameterTypes()[0];
         } else {
            try {
               collectionAttributeType = this.ownerClass.getField(propertyDescriptor.getName()).getGenericType();
            } catch (Exception var5) {
               return null;
            }
         }

         if (ParameterizedType.class.isInstance(collectionAttributeType)) {
            Type[] types = ((ParameterizedType)collectionAttributeType).getActualTypeArguments();
            if (types == null) {
               return null;
            }

            if (types.length == 1) {
               return (Class)types[0];
            }

            if (types.length == 2) {
               return (Class)types[1];
            }
         }

         return null;
      }
   }

   private static class AttributeJavaTypeDeterminerDelegate implements BeanInfoHelper.BeanInfoDelegate {
      private final String attributeName;
      private Class javaType;

      private AttributeJavaTypeDeterminerDelegate(String attributeName) {
         this.javaType = null;
         this.attributeName = attributeName;
      }

      public void processBeanInfo(BeanInfo beanInfo) throws Exception {
         for(PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
            if (propertyDescriptor.getName().equals(this.attributeName)) {
               this.javaType = propertyDescriptor.getPropertyType();
               break;
            }
         }

      }
   }
}
