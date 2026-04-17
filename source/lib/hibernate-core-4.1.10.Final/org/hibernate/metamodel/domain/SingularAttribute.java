package org.hibernate.metamodel.domain;

public interface SingularAttribute extends Attribute {
   Type getSingularAttributeType();

   boolean isTypeResolved();

   void resolveType(Type var1);
}
