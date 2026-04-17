package org.hibernate.metamodel.source.binder;

public interface AttributeSource {
   String getName();

   boolean isSingular();

   ExplicitHibernateTypeSource getTypeInformation();

   String getPropertyAccessorName();

   boolean isIncludedInOptimisticLocking();

   Iterable metaAttributes();
}
