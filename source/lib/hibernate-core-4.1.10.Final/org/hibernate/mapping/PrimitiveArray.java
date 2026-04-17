package org.hibernate.mapping;

import org.hibernate.cfg.Mappings;

public class PrimitiveArray extends Array {
   public PrimitiveArray(Mappings mappings, PersistentClass owner) {
      super(mappings, owner);
   }

   public boolean isPrimitiveArray() {
      return true;
   }

   public Object accept(ValueVisitor visitor) {
      return visitor.accept(this);
   }
}
