package org.hibernate.metamodel.domain;

public interface Attribute {
   String getName();

   AttributeContainer getAttributeContainer();

   boolean isSingular();
}
