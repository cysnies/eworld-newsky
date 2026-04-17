package org.hibernate.metamodel.source.annotations.entity;

import org.hibernate.metamodel.source.binder.SubclassEntitySource;

public class SubclassEntitySourceImpl extends EntitySourceImpl implements SubclassEntitySource {
   public SubclassEntitySourceImpl(EntityClass entityClass) {
      super(entityClass);
   }
}
