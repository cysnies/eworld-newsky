package org.hibernate.metamodel.source.annotations.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.hibernate.internal.jaxb.Origin;
import org.hibernate.metamodel.binding.CustomSQL;
import org.hibernate.metamodel.source.LocalBindingContext;
import org.hibernate.metamodel.source.annotations.attribute.AssociationAttribute;
import org.hibernate.metamodel.source.annotations.attribute.BasicAttribute;
import org.hibernate.metamodel.source.annotations.attribute.SingularAttributeSourceImpl;
import org.hibernate.metamodel.source.annotations.attribute.ToOneAttributeSourceImpl;
import org.hibernate.metamodel.source.binder.AttributeSource;
import org.hibernate.metamodel.source.binder.EntitySource;
import org.hibernate.metamodel.source.binder.SubclassEntitySource;
import org.hibernate.metamodel.source.binder.TableSource;

public class EntitySourceImpl implements EntitySource {
   private final EntityClass entityClass;
   private final Set subclassEntitySources;

   public EntitySourceImpl(EntityClass entityClass) {
      this.entityClass = entityClass;
      this.subclassEntitySources = new HashSet();
   }

   public EntityClass getEntityClass() {
      return this.entityClass;
   }

   public Origin getOrigin() {
      return this.entityClass.getLocalBindingContext().getOrigin();
   }

   public LocalBindingContext getLocalBindingContext() {
      return this.entityClass.getLocalBindingContext();
   }

   public String getEntityName() {
      return this.entityClass.getName();
   }

   public String getClassName() {
      return this.entityClass.getName();
   }

   public String getJpaEntityName() {
      return this.entityClass.getExplicitEntityName();
   }

   public TableSource getPrimaryTable() {
      return this.entityClass.getPrimaryTableSource();
   }

   public boolean isAbstract() {
      return false;
   }

   public boolean isLazy() {
      return this.entityClass.isLazy();
   }

   public String getProxy() {
      return this.entityClass.getProxy();
   }

   public int getBatchSize() {
      return this.entityClass.getBatchSize();
   }

   public boolean isDynamicInsert() {
      return this.entityClass.isDynamicInsert();
   }

   public boolean isDynamicUpdate() {
      return this.entityClass.isDynamicUpdate();
   }

   public boolean isSelectBeforeUpdate() {
      return this.entityClass.isSelectBeforeUpdate();
   }

   public String getCustomTuplizerClassName() {
      return this.entityClass.getCustomTuplizer();
   }

   public String getCustomPersisterClassName() {
      return this.entityClass.getCustomPersister();
   }

   public String getCustomLoaderName() {
      return this.entityClass.getCustomLoaderQueryName();
   }

   public CustomSQL getCustomSqlInsert() {
      return this.entityClass.getCustomInsert();
   }

   public CustomSQL getCustomSqlUpdate() {
      return this.entityClass.getCustomUpdate();
   }

   public CustomSQL getCustomSqlDelete() {
      return this.entityClass.getCustomDelete();
   }

   public List getSynchronizedTableNames() {
      return this.entityClass.getSynchronizedTableNames();
   }

   public Iterable metaAttributes() {
      return Collections.emptySet();
   }

   public String getPath() {
      return this.entityClass.getName();
   }

   public Iterable attributeSources() {
      List<AttributeSource> attributeList = new ArrayList();

      for(BasicAttribute attribute : this.entityClass.getSimpleAttributes()) {
         attributeList.add(new SingularAttributeSourceImpl(attribute));
      }

      for(EmbeddableClass component : this.entityClass.getEmbeddedClasses().values()) {
         attributeList.add(new ComponentAttributeSourceImpl(component, "", this.entityClass.getAttributeOverrideMap()));
      }

      for(AssociationAttribute associationAttribute : this.entityClass.getAssociationAttributes()) {
         attributeList.add(new ToOneAttributeSourceImpl(associationAttribute));
      }

      return attributeList;
   }

   public void add(SubclassEntitySource subclassEntitySource) {
      this.subclassEntitySources.add(subclassEntitySource);
   }

   public Iterable subclassEntitySources() {
      return this.subclassEntitySources;
   }

   public String getDiscriminatorMatchValue() {
      return this.entityClass.getDiscriminatorMatchValue();
   }

   public Iterable getConstraints() {
      return this.entityClass.getConstraintSources();
   }

   public List getJpaCallbackClasses() {
      return this.entityClass.getJpaCallbacks();
   }

   public Iterable getSecondaryTables() {
      return this.entityClass.getSecondaryTableSources();
   }
}
