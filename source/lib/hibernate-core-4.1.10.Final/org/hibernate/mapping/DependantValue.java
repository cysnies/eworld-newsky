package org.hibernate.mapping;

import org.hibernate.MappingException;
import org.hibernate.cfg.Mappings;
import org.hibernate.type.Type;

public class DependantValue extends SimpleValue {
   private KeyValue wrappedValue;
   private boolean nullable;
   private boolean updateable;

   public DependantValue(Mappings mappings, Table table, KeyValue prototype) {
      super(mappings, table);
      this.wrappedValue = prototype;
   }

   public Type getType() throws MappingException {
      return this.wrappedValue.getType();
   }

   public void setTypeUsingReflection(String className, String propertyName) {
   }

   public Object accept(ValueVisitor visitor) {
      return visitor.accept(this);
   }

   public boolean isNullable() {
      return this.nullable;
   }

   public void setNullable(boolean nullable) {
      this.nullable = nullable;
   }

   public boolean isUpdateable() {
      return this.updateable;
   }

   public void setUpdateable(boolean updateable) {
      this.updateable = updateable;
   }
}
