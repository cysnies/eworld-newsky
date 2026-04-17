package org.hibernate.annotations.common.reflection.java.generics;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public class TypeEnvironmentFactory {
   public TypeEnvironment getEnvironment(Class context) {
      return context == null ? IdentityTypeEnvironment.INSTANCE : this.createEnvironment((Type)context);
   }

   public TypeEnvironment getEnvironment(Type context) {
      return context == null ? IdentityTypeEnvironment.INSTANCE : this.createEnvironment(context);
   }

   public TypeEnvironment getEnvironment(Type t, TypeEnvironment context) {
      return CompoundTypeEnvironment.create(this.getEnvironment(t), context);
   }

   public TypeEnvironment toApproximatingEnvironment(TypeEnvironment context) {
      return CompoundTypeEnvironment.create(new ApproximatingTypeEnvironment(), context);
   }

   private TypeEnvironment createEnvironment(Type context) {
      return (TypeEnvironment)(new TypeSwitch() {
         public TypeEnvironment caseClass(Class classType) {
            return CompoundTypeEnvironment.create(TypeEnvironmentFactory.this.createSuperTypeEnvironment(classType), TypeEnvironmentFactory.this.getEnvironment(classType.getSuperclass()));
         }

         public TypeEnvironment caseParameterizedType(ParameterizedType parameterizedType) {
            return TypeEnvironmentFactory.this.createEnvironment(parameterizedType);
         }

         public TypeEnvironment defaultCase(Type t) {
            throw new IllegalArgumentException("Invalid type for generating environment: " + t);
         }
      }).doSwitch(context);
   }

   private TypeEnvironment createSuperTypeEnvironment(Class clazz) {
      Class superclass = clazz.getSuperclass();
      if (superclass == null) {
         return IdentityTypeEnvironment.INSTANCE;
      } else {
         Type[] formalArgs = superclass.getTypeParameters();
         Type genericSuperclass = clazz.getGenericSuperclass();
         if (genericSuperclass instanceof Class) {
            return IdentityTypeEnvironment.INSTANCE;
         } else if (genericSuperclass instanceof ParameterizedType) {
            Type[] actualArgs = ((ParameterizedType)genericSuperclass).getActualTypeArguments();
            return new SimpleTypeEnvironment(formalArgs, actualArgs);
         } else {
            throw new AssertionError("Should be unreachable");
         }
      }
   }

   private TypeEnvironment createEnvironment(ParameterizedType t) {
      Type[] tactuals = t.getActualTypeArguments();
      Type rawType = t.getRawType();
      if (rawType instanceof Class) {
         TypeVariable[] tparms = ((Class)rawType).getTypeParameters();
         return new SimpleTypeEnvironment(tparms, tactuals);
      } else {
         return IdentityTypeEnvironment.INSTANCE;
      }
   }
}
