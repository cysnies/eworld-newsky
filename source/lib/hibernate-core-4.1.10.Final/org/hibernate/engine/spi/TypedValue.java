package org.hibernate.engine.spi;

import java.io.Serializable;
import org.hibernate.EntityMode;
import org.hibernate.type.Type;

public final class TypedValue implements Serializable {
   private final Type type;
   private final Object value;
   private final EntityMode entityMode;

   public TypedValue(Type type, Object value) {
      this(type, value, EntityMode.POJO);
   }

   public TypedValue(Type type, Object value, EntityMode entityMode) {
      this.type = type;
      this.value = value;
      this.entityMode = entityMode;
   }

   public Object getValue() {
      return this.value;
   }

   public Type getType() {
      return this.type;
   }

   public String toString() {
      return this.value == null ? "null" : this.value.toString();
   }

   public int hashCode() {
      return this.value == null ? 0 : this.type.getHashCode(this.value);
   }

   public boolean equals(Object other) {
      if (!(other instanceof TypedValue)) {
         return false;
      } else {
         TypedValue that = (TypedValue)other;
         return this.type.getReturnedClass() == that.type.getReturnedClass() && this.type.isEqual(that.value, this.value);
      }
   }
}
