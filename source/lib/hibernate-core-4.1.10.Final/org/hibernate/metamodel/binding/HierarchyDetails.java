package org.hibernate.metamodel.binding;

import org.hibernate.EntityMode;
import org.hibernate.engine.OptimisticLockStyle;

public class HierarchyDetails {
   private final EntityBinding rootEntityBinding;
   private final InheritanceType inheritanceType;
   private final EntityMode entityMode;
   private final EntityIdentifier entityIdentifier;
   private EntityDiscriminator entityDiscriminator;
   private OptimisticLockStyle optimisticLockStyle;
   private BasicAttributeBinding versioningAttributeBinding;
   private Caching caching;
   private boolean explicitPolymorphism;

   public HierarchyDetails(EntityBinding rootEntityBinding, InheritanceType inheritanceType, EntityMode entityMode) {
      this.rootEntityBinding = rootEntityBinding;
      this.inheritanceType = inheritanceType;
      this.entityMode = entityMode;
      this.entityIdentifier = new EntityIdentifier(rootEntityBinding);
   }

   public EntityBinding getRootEntityBinding() {
      return this.rootEntityBinding;
   }

   public InheritanceType getInheritanceType() {
      return this.inheritanceType;
   }

   public EntityMode getEntityMode() {
      return this.entityMode;
   }

   public EntityIdentifier getEntityIdentifier() {
      return this.entityIdentifier;
   }

   public EntityDiscriminator getEntityDiscriminator() {
      return this.entityDiscriminator;
   }

   public OptimisticLockStyle getOptimisticLockStyle() {
      return this.optimisticLockStyle;
   }

   public void setOptimisticLockStyle(OptimisticLockStyle optimisticLockStyle) {
      this.optimisticLockStyle = optimisticLockStyle;
   }

   public void setEntityDiscriminator(EntityDiscriminator entityDiscriminator) {
      this.entityDiscriminator = entityDiscriminator;
   }

   public BasicAttributeBinding getVersioningAttributeBinding() {
      return this.versioningAttributeBinding;
   }

   public void setVersioningAttributeBinding(BasicAttributeBinding versioningAttributeBinding) {
      this.versioningAttributeBinding = versioningAttributeBinding;
   }

   public Caching getCaching() {
      return this.caching;
   }

   public void setCaching(Caching caching) {
      this.caching = caching;
   }

   public boolean isExplicitPolymorphism() {
      return this.explicitPolymorphism;
   }

   public void setExplicitPolymorphism(boolean explicitPolymorphism) {
      this.explicitPolymorphism = explicitPolymorphism;
   }
}
