package org.hibernate.type.descriptor.java;

import java.io.Serializable;

public abstract class MutableMutabilityPlan implements MutabilityPlan {
   public boolean isMutable() {
      return true;
   }

   public Serializable disassemble(Object value) {
      return (Serializable)this.deepCopy(value);
   }

   public Object assemble(Serializable cached) {
      return this.deepCopy(cached);
   }

   public final Object deepCopy(Object value) {
      return value == null ? null : this.deepCopyNotNull(value);
   }

   protected abstract Object deepCopyNotNull(Object var1);
}
