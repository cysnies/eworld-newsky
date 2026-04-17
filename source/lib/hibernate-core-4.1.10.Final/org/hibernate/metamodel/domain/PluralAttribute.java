package org.hibernate.metamodel.domain;

public interface PluralAttribute extends Attribute {
   String getRole();

   PluralAttributeNature getNature();

   Type getElementType();

   void setElementType(Type var1);
}
