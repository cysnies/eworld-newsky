package org.hibernate.metamodel.relational;

public interface ValueContainer {
   Iterable values();

   String getLoggableValueQualifier();
}
