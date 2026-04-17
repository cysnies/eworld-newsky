package org.hibernate.metamodel.source.binder;

import org.hibernate.EntityMode;
import org.hibernate.engine.OptimisticLockStyle;
import org.hibernate.metamodel.binding.Caching;

public interface RootEntitySource extends EntitySource {
   IdentifierSource getIdentifierSource();

   SingularAttributeSource getVersioningAttributeSource();

   DiscriminatorSource getDiscriminatorSource();

   EntityMode getEntityMode();

   boolean isMutable();

   boolean isExplicitPolymorphism();

   String getWhere();

   String getRowId();

   OptimisticLockStyle getOptimisticLockStyle();

   Caching getCaching();
}
