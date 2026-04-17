package com.comphenix.protocol.reflect;

import java.lang.reflect.Field;

public class VolatileField {
   private Field field;
   private Object container;
   private Object previous;
   private Object current;
   private boolean previousLoaded;
   private boolean currentSet;
   private boolean forceAccess;

   public VolatileField(Field field, Object container) {
      this.field = field;
      this.container = container;
   }

   public VolatileField(Field field, Object container, boolean forceAccess) {
      this.field = field;
      this.container = container;
      this.forceAccess = forceAccess;
   }

   public Field getField() {
      return this.field;
   }

   public Object getContainer() {
      return this.container;
   }

   public boolean isForceAccess() {
      return this.forceAccess;
   }

   public void setForceAccess(boolean forceAccess) {
      this.forceAccess = forceAccess;
   }

   public Object getValue() {
      if (!this.currentSet) {
         this.ensureLoaded();
         return this.previous;
      } else {
         return this.current;
      }
   }

   public Object getOldValue() {
      this.ensureLoaded();
      return this.previous;
   }

   public void setValue(Object newValue) {
      this.ensureLoaded();

      try {
         FieldUtils.writeField(this.field, this.container, newValue, this.forceAccess);
         this.current = newValue;
         this.currentSet = true;
      } catch (IllegalAccessException e) {
         throw new RuntimeException("Unable to read field " + this.field.getName(), e);
      }
   }

   public void saveValue() {
      this.previous = this.current;
      this.currentSet = false;
   }

   public void revertValue() {
      if (this.currentSet) {
         if (this.getValue() == this.current) {
            this.setValue(this.previous);
            this.currentSet = false;
         } else {
            System.out.println(String.format("[ProtocolLib] Unable to switch %s to %s. Expected %s but got %s.", this.field.toGenericString(), this.previous, this.current, this.getValue()));
         }
      }

   }

   public boolean isCurrentSet() {
      return this.currentSet;
   }

   private void ensureLoaded() {
      if (!this.previousLoaded) {
         try {
            this.previous = FieldUtils.readField(this.field, this.container, this.forceAccess);
            this.previousLoaded = true;
         } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to read field " + this.field.getName(), e);
         }
      }

   }

   protected void finalize() throws Throwable {
      this.revertValue();
   }

   public String toString() {
      return "VolatileField [field=" + this.field + ", container=" + this.container + ", previous=" + this.previous + ", current=" + this.current + "]";
   }
}
