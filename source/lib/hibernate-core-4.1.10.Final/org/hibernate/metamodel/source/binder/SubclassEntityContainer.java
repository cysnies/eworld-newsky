package org.hibernate.metamodel.source.binder;

public interface SubclassEntityContainer {
   void add(SubclassEntitySource var1);

   Iterable subclassEntitySources();
}
