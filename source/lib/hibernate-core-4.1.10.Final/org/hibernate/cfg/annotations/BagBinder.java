package org.hibernate.cfg.annotations;

import org.hibernate.mapping.Bag;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.PersistentClass;

public class BagBinder extends CollectionBinder {
   protected Collection createCollection(PersistentClass persistentClass) {
      return new Bag(this.getMappings(), persistentClass);
   }
}
