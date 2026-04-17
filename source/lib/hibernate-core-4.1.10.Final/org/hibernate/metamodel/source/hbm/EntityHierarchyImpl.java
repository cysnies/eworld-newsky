package org.hibernate.metamodel.source.hbm;

import org.hibernate.metamodel.binding.InheritanceType;
import org.hibernate.metamodel.source.MappingException;
import org.hibernate.metamodel.source.binder.EntityHierarchy;
import org.hibernate.metamodel.source.binder.RootEntitySource;

public class EntityHierarchyImpl implements EntityHierarchy {
   private final RootEntitySourceImpl rootEntitySource;
   private InheritanceType hierarchyInheritanceType;

   public EntityHierarchyImpl(RootEntitySourceImpl rootEntitySource) {
      this.hierarchyInheritanceType = InheritanceType.NO_INHERITANCE;
      this.rootEntitySource = rootEntitySource;
      this.rootEntitySource.injectHierarchy(this);
   }

   public InheritanceType getHierarchyInheritanceType() {
      return this.hierarchyInheritanceType;
   }

   public RootEntitySource getRootEntitySource() {
      return this.rootEntitySource;
   }

   public void processSubclass(SubclassEntitySourceImpl subclassEntitySource) {
      InheritanceType inheritanceType = Helper.interpretInheritanceType(subclassEntitySource.entityElement());
      if (this.hierarchyInheritanceType == InheritanceType.NO_INHERITANCE) {
         this.hierarchyInheritanceType = inheritanceType;
      } else if (this.hierarchyInheritanceType != inheritanceType) {
         throw new MappingException("Mixed inheritance strategies not supported", subclassEntitySource.getOrigin());
      }

   }
}
