package org.hibernate.metamodel.source.annotations;

import org.hibernate.metamodel.binding.InheritanceType;
import org.hibernate.metamodel.source.binder.EntityHierarchy;
import org.hibernate.metamodel.source.binder.RootEntitySource;

public class EntityHierarchyImpl implements EntityHierarchy {
   private final RootEntitySource rootEntitySource;
   private final InheritanceType inheritanceType;

   public EntityHierarchyImpl(RootEntitySource source, InheritanceType inheritanceType) {
      this.rootEntitySource = source;
      this.inheritanceType = inheritanceType;
   }

   public InheritanceType getHierarchyInheritanceType() {
      return this.inheritanceType;
   }

   public RootEntitySource getRootEntitySource() {
      return this.rootEntitySource;
   }
}
