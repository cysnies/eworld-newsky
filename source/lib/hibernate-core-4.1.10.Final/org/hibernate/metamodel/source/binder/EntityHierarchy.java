package org.hibernate.metamodel.source.binder;

import org.hibernate.metamodel.binding.InheritanceType;

public interface EntityHierarchy {
   InheritanceType getHierarchyInheritanceType();

   RootEntitySource getRootEntitySource();
}
