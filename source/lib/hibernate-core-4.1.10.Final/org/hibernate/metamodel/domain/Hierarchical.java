package org.hibernate.metamodel.domain;

public interface Hierarchical extends AttributeContainer {
   Hierarchical getSuperType();
}
