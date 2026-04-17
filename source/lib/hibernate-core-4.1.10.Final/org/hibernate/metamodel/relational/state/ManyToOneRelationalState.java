package org.hibernate.metamodel.relational.state;

public interface ManyToOneRelationalState extends ValueRelationalState {
   boolean isLogicalOneToOne();

   String getForeignKeyName();
}
