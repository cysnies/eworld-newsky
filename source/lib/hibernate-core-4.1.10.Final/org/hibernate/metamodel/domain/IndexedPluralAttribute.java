package org.hibernate.metamodel.domain;

public interface IndexedPluralAttribute extends PluralAttribute {
   Type getIndexType();

   void setIndexType(Type var1);
}
