package org.hibernate.metamodel.binding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.AssertionFailure;
import org.hibernate.EntityMode;
import org.hibernate.engine.spi.FilterDefinition;
import org.hibernate.internal.util.ValueHolder;
import org.hibernate.internal.util.collections.JoinedIterable;
import org.hibernate.metamodel.domain.AttributeContainer;
import org.hibernate.metamodel.domain.Entity;
import org.hibernate.metamodel.domain.PluralAttribute;
import org.hibernate.metamodel.domain.PluralAttributeNature;
import org.hibernate.metamodel.domain.SingularAttribute;
import org.hibernate.metamodel.relational.TableSpecification;
import org.hibernate.metamodel.source.MetaAttributeContext;
import org.hibernate.metamodel.source.binder.JpaCallbackClass;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.tuple.entity.EntityTuplizer;

public class EntityBinding implements AttributeBindingContainer {
   private static final String NULL_DISCRIMINATOR_MATCH_VALUE = "null";
   private static final String NOT_NULL_DISCRIMINATOR_MATCH_VALUE = "not null";
   private final EntityBinding superEntityBinding;
   private final List subEntityBindings = new ArrayList();
   private final HierarchyDetails hierarchyDetails;
   private Entity entity;
   private TableSpecification primaryTable;
   private String primaryTableName;
   private Map secondaryTables = new HashMap();
   private ValueHolder proxyInterfaceType;
   private String jpaEntityName;
   private Class customEntityPersisterClass;
   private Class customEntityTuplizerClass;
   private String discriminatorMatchValue;
   private Set filterDefinitions = new HashSet();
   private Set entityReferencingAttributeBindings = new HashSet();
   private MetaAttributeContext metaAttributeContext;
   private boolean lazy;
   private boolean mutable;
   private String whereFilter;
   private String rowId;
   private boolean dynamicUpdate;
   private boolean dynamicInsert;
   private int batchSize;
   private boolean selectBeforeUpdate;
   private boolean hasSubselectLoadableCollections;
   private Boolean isAbstract;
   private String customLoaderName;
   private CustomSQL customInsert;
   private CustomSQL customUpdate;
   private CustomSQL customDelete;
   private Set synchronizedTableNames = new HashSet();
   private Map attributeBindingMap = new HashMap();
   private List jpaCallbackClasses = new ArrayList();

   public EntityBinding(InheritanceType inheritanceType, EntityMode entityMode) {
      this.superEntityBinding = null;
      this.hierarchyDetails = new HierarchyDetails(this, inheritanceType, entityMode);
   }

   public EntityBinding(EntityBinding superEntityBinding) {
      this.superEntityBinding = superEntityBinding;
      this.superEntityBinding.subEntityBindings.add(this);
      this.hierarchyDetails = superEntityBinding.getHierarchyDetails();
   }

   public HierarchyDetails getHierarchyDetails() {
      return this.hierarchyDetails;
   }

   public EntityBinding getSuperEntityBinding() {
      return this.superEntityBinding;
   }

   public boolean isRoot() {
      return this.superEntityBinding == null;
   }

   public boolean isPolymorphic() {
      return this.superEntityBinding != null || this.hierarchyDetails.getEntityDiscriminator() != null || !this.subEntityBindings.isEmpty();
   }

   public boolean hasSubEntityBindings() {
      return this.subEntityBindings.size() > 0;
   }

   public int getSubEntityBindingClosureSpan() {
      int n = this.subEntityBindings.size();

      for(EntityBinding subEntityBinding : this.subEntityBindings) {
         n += subEntityBinding.getSubEntityBindingClosureSpan();
      }

      return n;
   }

   public Iterable getDirectSubEntityBindings() {
      return this.subEntityBindings;
   }

   public Iterable getPostOrderSubEntityBindingClosure() {
      List<Iterable<EntityBinding>> subclassIterables = new ArrayList(this.subEntityBindings.size() + 1);

      for(EntityBinding subEntityBinding : this.subEntityBindings) {
         Iterable<EntityBinding> subSubEntityBindings = subEntityBinding.getPostOrderSubEntityBindingClosure();
         if (subSubEntityBindings.iterator().hasNext()) {
            subclassIterables.add(subSubEntityBindings);
         }
      }

      if (!this.subEntityBindings.isEmpty()) {
         subclassIterables.add(this.subEntityBindings);
      }

      return new JoinedIterable(subclassIterables);
   }

   public Iterable getPreOrderSubEntityBindingClosure() {
      return this.getPreOrderSubEntityBindingClosure(false);
   }

   private Iterable getPreOrderSubEntityBindingClosure(boolean includeThis) {
      List<Iterable<EntityBinding>> iterables = new ArrayList();
      if (includeThis) {
         iterables.add(Collections.singletonList(this));
      }

      for(EntityBinding subEntityBinding : this.subEntityBindings) {
         Iterable<EntityBinding> subSubEntityBindingClosure = subEntityBinding.getPreOrderSubEntityBindingClosure(true);
         if (subSubEntityBindingClosure.iterator().hasNext()) {
            iterables.add(subSubEntityBindingClosure);
         }
      }

      return new JoinedIterable(iterables);
   }

   public Entity getEntity() {
      return this.entity;
   }

   public void setEntity(Entity entity) {
      this.entity = entity;
   }

   public TableSpecification getPrimaryTable() {
      return this.primaryTable;
   }

   public void setPrimaryTable(TableSpecification primaryTable) {
      this.primaryTable = primaryTable;
   }

   public TableSpecification locateTable(String tableName) {
      if (tableName != null && !tableName.equals(this.getPrimaryTableName())) {
         TableSpecification tableSpec = (TableSpecification)this.secondaryTables.get(tableName);
         if (tableSpec == null) {
            throw new AssertionFailure(String.format("Unable to find table %s amongst tables %s", tableName, this.secondaryTables.keySet()));
         } else {
            return tableSpec;
         }
      } else {
         return this.primaryTable;
      }
   }

   public String getPrimaryTableName() {
      return this.primaryTableName;
   }

   public void setPrimaryTableName(String primaryTableName) {
      this.primaryTableName = primaryTableName;
   }

   public void addSecondaryTable(String tableName, TableSpecification table) {
      this.secondaryTables.put(tableName, table);
   }

   public boolean isVersioned() {
      return this.getHierarchyDetails().getVersioningAttributeBinding() != null;
   }

   public boolean isDiscriminatorMatchValueNull() {
      return "null".equals(this.discriminatorMatchValue);
   }

   public boolean isDiscriminatorMatchValueNotNull() {
      return "not null".equals(this.discriminatorMatchValue);
   }

   public String getDiscriminatorMatchValue() {
      return this.discriminatorMatchValue;
   }

   public void setDiscriminatorMatchValue(String discriminatorMatchValue) {
      this.discriminatorMatchValue = discriminatorMatchValue;
   }

   public Iterable getFilterDefinitions() {
      return this.filterDefinitions;
   }

   public void addFilterDefinition(FilterDefinition filterDefinition) {
      this.filterDefinitions.add(filterDefinition);
   }

   public Iterable getEntityReferencingAttributeBindings() {
      return this.entityReferencingAttributeBindings;
   }

   public EntityBinding seekEntityBinding() {
      return this;
   }

   public String getPathBase() {
      return this.getEntity().getName();
   }

   public Class getClassReference() {
      return this.getEntity().getClassReference();
   }

   public AttributeContainer getAttributeContainer() {
      return this.getEntity();
   }

   protected void registerAttributeBinding(String name, AttributeBinding attributeBinding) {
      if (SingularAssociationAttributeBinding.class.isInstance(attributeBinding)) {
         this.entityReferencingAttributeBindings.add((SingularAssociationAttributeBinding)attributeBinding);
      }

      this.attributeBindingMap.put(name, attributeBinding);
   }

   public MetaAttributeContext getMetaAttributeContext() {
      return this.metaAttributeContext;
   }

   public void setMetaAttributeContext(MetaAttributeContext metaAttributeContext) {
      this.metaAttributeContext = metaAttributeContext;
   }

   public boolean isMutable() {
      return this.mutable;
   }

   public void setMutable(boolean mutable) {
      this.mutable = mutable;
   }

   public boolean isLazy() {
      return this.lazy;
   }

   public void setLazy(boolean lazy) {
      this.lazy = lazy;
   }

   public ValueHolder getProxyInterfaceType() {
      return this.proxyInterfaceType;
   }

   public void setProxyInterfaceType(ValueHolder proxyInterfaceType) {
      this.proxyInterfaceType = proxyInterfaceType;
   }

   public String getWhereFilter() {
      return this.whereFilter;
   }

   public void setWhereFilter(String whereFilter) {
      this.whereFilter = whereFilter;
   }

   public String getRowId() {
      return this.rowId;
   }

   public void setRowId(String rowId) {
      this.rowId = rowId;
   }

   public boolean isDynamicUpdate() {
      return this.dynamicUpdate;
   }

   public void setDynamicUpdate(boolean dynamicUpdate) {
      this.dynamicUpdate = dynamicUpdate;
   }

   public boolean isDynamicInsert() {
      return this.dynamicInsert;
   }

   public void setDynamicInsert(boolean dynamicInsert) {
      this.dynamicInsert = dynamicInsert;
   }

   public int getBatchSize() {
      return this.batchSize;
   }

   public void setBatchSize(int batchSize) {
      this.batchSize = batchSize;
   }

   public boolean isSelectBeforeUpdate() {
      return this.selectBeforeUpdate;
   }

   public void setSelectBeforeUpdate(boolean selectBeforeUpdate) {
      this.selectBeforeUpdate = selectBeforeUpdate;
   }

   public boolean hasSubselectLoadableCollections() {
      return this.hasSubselectLoadableCollections;
   }

   void setSubselectLoadableCollections(boolean hasSubselectLoadableCollections) {
      this.hasSubselectLoadableCollections = hasSubselectLoadableCollections;
   }

   public Class getCustomEntityPersisterClass() {
      return this.customEntityPersisterClass;
   }

   public void setCustomEntityPersisterClass(Class customEntityPersisterClass) {
      this.customEntityPersisterClass = customEntityPersisterClass;
   }

   public Class getCustomEntityTuplizerClass() {
      return this.customEntityTuplizerClass;
   }

   public void setCustomEntityTuplizerClass(Class customEntityTuplizerClass) {
      this.customEntityTuplizerClass = customEntityTuplizerClass;
   }

   public Boolean isAbstract() {
      return this.isAbstract;
   }

   public void setAbstract(Boolean isAbstract) {
      this.isAbstract = isAbstract;
   }

   public Set getSynchronizedTableNames() {
      return this.synchronizedTableNames;
   }

   public void addSynchronizedTableNames(Collection synchronizedTableNames) {
      this.synchronizedTableNames.addAll(synchronizedTableNames);
   }

   public String getJpaEntityName() {
      return this.jpaEntityName;
   }

   public void setJpaEntityName(String jpaEntityName) {
      this.jpaEntityName = jpaEntityName;
   }

   public String getCustomLoaderName() {
      return this.customLoaderName;
   }

   public void setCustomLoaderName(String customLoaderName) {
      this.customLoaderName = customLoaderName;
   }

   public CustomSQL getCustomInsert() {
      return this.customInsert;
   }

   public void setCustomInsert(CustomSQL customInsert) {
      this.customInsert = customInsert;
   }

   public CustomSQL getCustomUpdate() {
      return this.customUpdate;
   }

   public void setCustomUpdate(CustomSQL customUpdate) {
      this.customUpdate = customUpdate;
   }

   public CustomSQL getCustomDelete() {
      return this.customDelete;
   }

   public void setCustomDelete(CustomSQL customDelete) {
      this.customDelete = customDelete;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("EntityBinding");
      sb.append("{entity=").append(this.entity != null ? this.entity.getName() : "not set");
      sb.append('}');
      return sb.toString();
   }

   public BasicAttributeBinding makeBasicAttributeBinding(SingularAttribute attribute) {
      return this.makeSimpleAttributeBinding(attribute, false, false);
   }

   private BasicAttributeBinding makeSimpleAttributeBinding(SingularAttribute attribute, boolean forceNonNullable, boolean forceUnique) {
      BasicAttributeBinding binding = new BasicAttributeBinding(this, attribute, forceNonNullable, forceUnique);
      this.registerAttributeBinding(attribute.getName(), binding);
      return binding;
   }

   public ComponentAttributeBinding makeComponentAttributeBinding(SingularAttribute attribute) {
      ComponentAttributeBinding binding = new ComponentAttributeBinding(this, attribute);
      this.registerAttributeBinding(attribute.getName(), binding);
      return binding;
   }

   public ManyToOneAttributeBinding makeManyToOneAttributeBinding(SingularAttribute attribute) {
      ManyToOneAttributeBinding binding = new ManyToOneAttributeBinding(this, attribute);
      this.registerAttributeBinding(attribute.getName(), binding);
      return binding;
   }

   public BagBinding makeBagAttributeBinding(PluralAttribute attribute, CollectionElementNature nature) {
      Helper.checkPluralAttributeNature(attribute, PluralAttributeNature.BAG);
      BagBinding binding = new BagBinding(this, attribute, nature);
      this.registerAttributeBinding(attribute.getName(), binding);
      return binding;
   }

   public SetBinding makeSetAttributeBinding(PluralAttribute attribute, CollectionElementNature nature) {
      Helper.checkPluralAttributeNature(attribute, PluralAttributeNature.SET);
      SetBinding binding = new SetBinding(this, attribute, nature);
      this.registerAttributeBinding(attribute.getName(), binding);
      return binding;
   }

   public AttributeBinding locateAttributeBinding(String name) {
      return (AttributeBinding)this.attributeBindingMap.get(name);
   }

   public Iterable attributeBindings() {
      return this.attributeBindingMap.values();
   }

   public int getAttributeBindingClosureSpan() {
      return this.superEntityBinding != null ? this.superEntityBinding.getAttributeBindingClosureSpan() + this.attributeBindingMap.size() : this.attributeBindingMap.size();
   }

   public Iterable getAttributeBindingClosure() {
      Iterable<AttributeBinding> iterable;
      if (this.superEntityBinding != null) {
         List<Iterable<AttributeBinding>> iterables = new ArrayList(2);
         iterables.add(this.superEntityBinding.getAttributeBindingClosure());
         iterables.add(this.attributeBindings());
         iterable = new JoinedIterable(iterables);
      } else {
         iterable = this.attributeBindings();
      }

      return iterable;
   }

   public Iterable getSubEntityAttributeBindingClosure() {
      List<Iterable<AttributeBinding>> iterables = new ArrayList();
      iterables.add(this.getAttributeBindingClosure());

      for(EntityBinding subEntityBinding : this.getPreOrderSubEntityBindingClosure()) {
         iterables.add(subEntityBinding.attributeBindings());
      }

      return new JoinedIterable(iterables);
   }

   public void setJpaCallbackClasses(List jpaCallbackClasses) {
      this.jpaCallbackClasses = jpaCallbackClasses;
   }

   public Iterable getJpaCallbackClasses() {
      return this.jpaCallbackClasses;
   }
}
