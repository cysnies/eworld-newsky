package org.hibernate.annotations.common.reflection.java.generics;

import java.lang.reflect.Type;

public class CompoundTypeEnvironment implements TypeEnvironment {
   private final TypeEnvironment f;
   private final TypeEnvironment g;
   private final int hashCode;

   public static TypeEnvironment create(TypeEnvironment f, TypeEnvironment g) {
      if (g == IdentityTypeEnvironment.INSTANCE) {
         return f;
      } else {
         return (TypeEnvironment)(f == IdentityTypeEnvironment.INSTANCE ? g : new CompoundTypeEnvironment(f, g));
      }
   }

   private CompoundTypeEnvironment(TypeEnvironment f, TypeEnvironment g) {
      this.f = f;
      this.g = g;
      this.hashCode = this.doHashCode();
   }

   public Type bind(Type type) {
      return this.f.bind(this.g.bind(type));
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof CompoundTypeEnvironment)) {
         return false;
      } else {
         CompoundTypeEnvironment that = (CompoundTypeEnvironment)o;
         if (this.differentHashCode(that)) {
            return false;
         } else {
            return !this.f.equals(that.f) ? false : this.g.equals(that.g);
         }
      }
   }

   private boolean differentHashCode(CompoundTypeEnvironment that) {
      return this.hashCode != that.hashCode;
   }

   private int doHashCode() {
      int result = this.f.hashCode();
      result = 29 * result + this.g.hashCode();
      return result;
   }

   public int hashCode() {
      return this.hashCode;
   }

   public String toString() {
      return this.f.toString() + "(" + this.g.toString() + ")";
   }
}
