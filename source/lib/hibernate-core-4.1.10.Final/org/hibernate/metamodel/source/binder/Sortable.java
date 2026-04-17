package org.hibernate.metamodel.source.binder;

public interface Sortable {
   boolean isSorted();

   String getComparatorName();
}
