package org.hibernate.annotations.common.reflection.java.generics;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

class ApproximatingTypeEnvironment implements TypeEnvironment {
   public Type bind(Type type) {
      Type result = this.fineApproximation(type);

      assert TypeUtils.isResolved(result);

      return result;
   }

   private Type fineApproximation(Type type) {
      return (Type)(new TypeSwitch() {
         public Type caseWildcardType(WildcardType wildcardType) {
            return wildcardType;
         }

         public Type caseClass(Class classType) {
            return classType;
         }

         public Type caseGenericArrayType(GenericArrayType genericArrayType) {
            if (TypeUtils.isResolved(genericArrayType)) {
               return genericArrayType;
            } else {
               Type componentType = genericArrayType.getGenericComponentType();
               Type boundComponentType = ApproximatingTypeEnvironment.this.bind(componentType);
               return boundComponentType instanceof Class ? Array.newInstance((Class)boundComponentType, 0).getClass() : Object[].class;
            }
         }

         public Type caseParameterizedType(ParameterizedType parameterizedType) {
            if (TypeUtils.isResolved(parameterizedType)) {
               return parameterizedType;
            } else if (!TypeUtils.isCollection(parameterizedType)) {
               return Object.class;
            } else {
               Type[] typeArguments = parameterizedType.getActualTypeArguments();
               Type[] approximatedTypeArguments = new Type[typeArguments.length];

               for(int i = 0; i < typeArguments.length; ++i) {
                  approximatedTypeArguments[i] = ApproximatingTypeEnvironment.this.coarseApproximation(typeArguments[i]);
               }

               return TypeFactory.createParameterizedType(ApproximatingTypeEnvironment.this.bind(parameterizedType.getRawType()), approximatedTypeArguments, parameterizedType.getOwnerType());
            }
         }

         public Type defaultCase(Type t) {
            return ApproximatingTypeEnvironment.this.coarseApproximation(t);
         }
      }).doSwitch(type);
   }

   private Type coarseApproximation(Type type) {
      Type result = (Type)(new TypeSwitch() {
         public Type caseWildcardType(WildcardType wildcardType) {
            return this.approximateTo(wildcardType.getUpperBounds());
         }

         public Type caseGenericArrayType(GenericArrayType genericArrayType) {
            return (Type)(TypeUtils.isResolved(genericArrayType) ? genericArrayType : Object[].class);
         }

         public Type caseParameterizedType(ParameterizedType parameterizedType) {
            return (Type)(TypeUtils.isResolved(parameterizedType) ? parameterizedType : Object.class);
         }

         public Type caseTypeVariable(TypeVariable typeVariable) {
            return this.approximateTo(typeVariable.getBounds());
         }

         private Type approximateTo(Type[] bounds) {
            return (Type)(bounds.length != 1 ? Object.class : ApproximatingTypeEnvironment.this.coarseApproximation(bounds[0]));
         }

         public Type defaultCase(Type t) {
            return t;
         }
      }).doSwitch(type);

      assert TypeUtils.isResolved(result);

      return result;
   }

   public String toString() {
      return "approximated_types";
   }
}
