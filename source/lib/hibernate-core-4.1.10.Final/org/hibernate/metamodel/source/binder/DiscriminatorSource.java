package org.hibernate.metamodel.source.binder;

public interface DiscriminatorSource {
   RelationalValueSource getDiscriminatorRelationalValueSource();

   String getExplicitHibernateTypeName();

   boolean isForced();

   boolean isInserted();
}
