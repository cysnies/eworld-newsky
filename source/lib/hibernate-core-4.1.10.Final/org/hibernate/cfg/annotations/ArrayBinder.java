package org.hibernate.cfg.annotations;

import org.hibernate.mapping.Array;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.PersistentClass;

public class ArrayBinder extends ListBinder {
   protected Collection createCollection(PersistentClass persistentClass) {
      return new Array(this.getMappings(), persistentClass);
   }
}
