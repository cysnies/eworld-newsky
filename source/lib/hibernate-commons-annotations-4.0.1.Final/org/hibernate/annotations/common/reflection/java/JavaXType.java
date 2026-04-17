package org.hibernate.annotations.common.reflection.java;

import java.lang.reflect.Type;
import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.annotations.common.reflection.java.generics.TypeEnvironment;
import org.hibernate.annotations.common.reflection.java.generics.TypeUtils;

abstract class JavaXType {
   private final TypeEnvironment context;
   private final JavaReflectionManager factory;
   private final Type approximatedType;
   private final Type boundType;

   protected JavaXType(Type unboundType, TypeEnvironment context, JavaReflectionManager factory) {
      this.context = context;
      this.factory = factory;
      this.boundType = context.bind(unboundType);
      this.approximatedType = factory.toApproximatingEnvironment(context).bind(unboundType);
   }

   public abstract boolean isArray();

   public abstract boolean isCollection();

   public abstract XClass getElementClass();

   public abstract XClass getClassOrElementClass();

   public abstract Class getCollectionClass();

   public abstract XClass getMapKey();

   public abstract XClass getType();

   public boolean isResolved() {
      return TypeUtils.isResolved(this.boundType);
   }

   protected Type approximate() {
      return this.approximatedType;
   }

   protected XClass toXClass(Type type) {
      return this.factory.toXClass(type, this.context);
   }
}
