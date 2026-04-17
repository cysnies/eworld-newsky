package org.hibernate.annotations.common.reflection.java;

import java.lang.reflect.Type;
import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.annotations.common.reflection.java.generics.TypeEnvironment;

class JavaXSimpleType extends JavaXType {
   public JavaXSimpleType(Type type, TypeEnvironment context, JavaReflectionManager factory) {
      super(type, context, factory);
   }

   public boolean isArray() {
      return false;
   }

   public boolean isCollection() {
      return false;
   }

   public XClass getElementClass() {
      return this.toXClass(this.approximate());
   }

   public XClass getClassOrElementClass() {
      return this.getElementClass();
   }

   public Class getCollectionClass() {
      return null;
   }

   public XClass getType() {
      return this.toXClass(this.approximate());
   }

   public XClass getMapKey() {
      return null;
   }
}
