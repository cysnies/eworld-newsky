package org.hibernate.annotations.common.reflection.java.generics;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

class TypeFactory {
   static ParameterizedType createParameterizedType(final Type rawType, final Type[] substTypeArgs, final Type ownerType) {
      return new ParameterizedType() {
         public Type[] getActualTypeArguments() {
            return substTypeArgs;
         }

         public Type getRawType() {
            return rawType;
         }

         public Type getOwnerType() {
            return ownerType;
         }

         public boolean equals(Object obj) {
            if (!(obj instanceof ParameterizedType)) {
               return false;
            } else {
               ParameterizedType other = (ParameterizedType)obj;
               return Arrays.equals(this.getActualTypeArguments(), other.getActualTypeArguments()) && TypeFactory.safeEquals(this.getRawType(), other.getRawType()) && TypeFactory.safeEquals(this.getOwnerType(), other.getOwnerType());
            }
         }

         public int hashCode() {
            return TypeFactory.safeHashCode(this.getActualTypeArguments()) ^ TypeFactory.safeHashCode(this.getRawType()) ^ TypeFactory.safeHashCode(this.getOwnerType());
         }
      };
   }

   static Type createArrayType(Type componentType) {
      return (Type)(componentType instanceof Class ? Array.newInstance((Class)componentType, 0).getClass() : createGenericArrayType(componentType));
   }

   private static GenericArrayType createGenericArrayType(final Type componentType) {
      return new GenericArrayType() {
         public Type getGenericComponentType() {
            return componentType;
         }

         public boolean equals(Object obj) {
            if (!(obj instanceof GenericArrayType)) {
               return false;
            } else {
               GenericArrayType other = (GenericArrayType)obj;
               return TypeFactory.safeEquals(this.getGenericComponentType(), other.getGenericComponentType());
            }
         }

         public int hashCode() {
            return TypeFactory.safeHashCode(this.getGenericComponentType());
         }
      };
   }

   private static boolean safeEquals(Type t1, Type t2) {
      if (t1 == null) {
         return t2 == null;
      } else {
         return t1.equals(t2);
      }
   }

   private static int safeHashCode(Object o) {
      return o == null ? 1 : o.hashCode();
   }
}
