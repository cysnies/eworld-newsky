package org.hibernate.annotations.common.reflection.java;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.annotations.common.reflection.java.generics.TypeEnvironment;
import org.hibernate.annotations.common.reflection.java.generics.TypeSwitch;

class JavaXArrayType extends JavaXType {
   public JavaXArrayType(Type type, TypeEnvironment context, JavaReflectionManager factory) {
      super(type, context, factory);
   }

   public boolean isArray() {
      return true;
   }

   public boolean isCollection() {
      return false;
   }

   public XClass getElementClass() {
      return this.toXClass(this.getElementType());
   }

   private Type getElementType() {
      return (Type)(new TypeSwitch() {
         public Type caseClass(Class classType) {
            return classType.getComponentType();
         }

         public Type caseGenericArrayType(GenericArrayType genericArrayType) {
            return genericArrayType.getGenericComponentType();
         }

         public Type defaultCase(Type t) {
            throw new IllegalArgumentException(t + " is not an array type");
         }
      }).doSwitch(this.approximate());
   }

   public XClass getClassOrElementClass() {
      return this.getElementClass();
   }

   public Class getCollectionClass() {
      return null;
   }

   public XClass getMapKey() {
      return null;
   }

   public XClass getType() {
      Type boundType = this.getElementType();
      if (boundType instanceof Class) {
         boundType = this.arrayTypeOf((Class)boundType);
      }

      return this.toXClass(boundType);
   }

   private Class arrayTypeOf(Class componentType) {
      return Array.newInstance(componentType, 0).getClass();
   }
}
