package org.hibernate.type.descriptor.java;

import java.io.Serializable;

public class ImmutableMutabilityPlan implements MutabilityPlan {
   public static final ImmutableMutabilityPlan INSTANCE = new ImmutableMutabilityPlan();

   public boolean isMutable() {
      return false;
   }

   public Object deepCopy(Object value) {
      return value;
   }

   public Serializable disassemble(Object value) {
      return (Serializable)value;
   }

   public Object assemble(Serializable cached) {
      return cached;
   }
}
