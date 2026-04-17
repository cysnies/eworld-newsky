package org.hibernate.mapping;

public interface ValueVisitor {
   Object accept(Bag var1);

   Object accept(IdentifierBag var1);

   Object accept(List var1);

   Object accept(PrimitiveArray var1);

   Object accept(Array var1);

   Object accept(Map var1);

   Object accept(OneToMany var1);

   Object accept(Set var1);

   Object accept(Any var1);

   Object accept(SimpleValue var1);

   Object accept(DependantValue var1);

   Object accept(Component var1);

   Object accept(ManyToOne var1);

   Object accept(OneToOne var1);
}
