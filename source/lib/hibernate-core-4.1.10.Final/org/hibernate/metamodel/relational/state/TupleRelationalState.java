package org.hibernate.metamodel.relational.state;

import java.util.List;

public interface TupleRelationalState extends ValueRelationalState {
   List getRelationalStates();
}
