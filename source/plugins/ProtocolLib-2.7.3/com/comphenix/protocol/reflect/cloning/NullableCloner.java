package com.comphenix.protocol.reflect.cloning;

public class NullableCloner implements Cloner {
   protected Cloner wrapped;

   public NullableCloner(Cloner wrapped) {
      this.wrapped = wrapped;
   }

   public boolean canClone(Object source) {
      return true;
   }

   public Object clone(Object source) {
      return source == null ? null : this.wrapped.clone(source);
   }

   public Cloner getWrapped() {
      return this.wrapped;
   }
}
