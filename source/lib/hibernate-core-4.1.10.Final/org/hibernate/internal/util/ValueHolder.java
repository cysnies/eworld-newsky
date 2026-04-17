package org.hibernate.internal.util;

public class ValueHolder {
   private final DeferredInitializer valueInitializer;
   private Object value;
   private static final DeferredInitializer NO_DEFERRED_INITIALIZER = new DeferredInitializer() {
      public Void initialize() {
         return null;
      }
   };

   public ValueHolder(DeferredInitializer valueInitializer) {
      this.valueInitializer = valueInitializer;
   }

   public ValueHolder(Object value) {
      this(NO_DEFERRED_INITIALIZER);
      this.value = value;
   }

   public Object getValue() {
      if (this.value == null) {
         this.value = this.valueInitializer.initialize();
      }

      return this.value;
   }

   public interface DeferredInitializer {
      Object initialize();
   }
}
