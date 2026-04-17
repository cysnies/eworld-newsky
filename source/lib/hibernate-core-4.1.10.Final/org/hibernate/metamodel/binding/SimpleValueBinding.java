package org.hibernate.metamodel.binding;

import org.hibernate.metamodel.relational.Column;
import org.hibernate.metamodel.relational.DerivedValue;
import org.hibernate.metamodel.relational.SimpleValue;

public class SimpleValueBinding {
   private SimpleValue simpleValue;
   private boolean includeInInsert;
   private boolean includeInUpdate;

   public SimpleValueBinding() {
      this(true, true);
   }

   public SimpleValueBinding(SimpleValue simpleValue) {
      this();
      this.setSimpleValue(simpleValue);
   }

   public SimpleValueBinding(SimpleValue simpleValue, boolean includeInInsert, boolean includeInUpdate) {
      this(includeInInsert, includeInUpdate);
      this.setSimpleValue(simpleValue);
   }

   public SimpleValueBinding(boolean includeInInsert, boolean includeInUpdate) {
      this.includeInInsert = includeInInsert;
      this.includeInUpdate = includeInUpdate;
   }

   public SimpleValue getSimpleValue() {
      return this.simpleValue;
   }

   public void setSimpleValue(SimpleValue simpleValue) {
      this.simpleValue = simpleValue;
      if (DerivedValue.class.isInstance(simpleValue)) {
         this.includeInInsert = false;
         this.includeInUpdate = false;
      }

   }

   public boolean isDerived() {
      return DerivedValue.class.isInstance(this.simpleValue);
   }

   public boolean isNullable() {
      return this.isDerived() || ((Column)Column.class.cast(this.simpleValue)).isNullable();
   }

   public boolean isIncludeInInsert() {
      return this.includeInInsert;
   }

   public void setIncludeInInsert(boolean includeInInsert) {
      this.includeInInsert = includeInInsert;
   }

   public boolean isIncludeInUpdate() {
      return this.includeInUpdate;
   }

   public void setIncludeInUpdate(boolean includeInUpdate) {
      this.includeInUpdate = includeInUpdate;
   }
}
