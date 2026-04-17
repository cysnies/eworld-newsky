package org.hibernate.mapping;

public interface PersistentClassVisitor {
   Object accept(RootClass var1);

   Object accept(UnionSubclass var1);

   Object accept(SingleTableSubclass var1);

   Object accept(JoinedSubclass var1);

   Object accept(Subclass var1);
}
