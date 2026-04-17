package org.hibernate.annotations.common.reflection.java.generics;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

public class TypeSwitch {
   public final Object doSwitch(Type type) {
      if (type instanceof Class) {
         return this.caseClass((Class)type);
      } else if (type instanceof GenericArrayType) {
         return this.caseGenericArrayType((GenericArrayType)type);
      } else if (type instanceof ParameterizedType) {
         return this.caseParameterizedType((ParameterizedType)type);
      } else if (type instanceof TypeVariable) {
         return this.caseTypeVariable((TypeVariable)type);
      } else {
         return type instanceof WildcardType ? this.caseWildcardType((WildcardType)type) : this.defaultCase(type);
      }
   }

   public Object caseWildcardType(WildcardType wildcardType) {
      return this.defaultCase(wildcardType);
   }

   public Object caseTypeVariable(TypeVariable typeVariable) {
      return this.defaultCase(typeVariable);
   }

   public Object caseClass(Class classType) {
      return this.defaultCase(classType);
   }

   public Object caseGenericArrayType(GenericArrayType genericArrayType) {
      return this.defaultCase(genericArrayType);
   }

   public Object caseParameterizedType(ParameterizedType parameterizedType) {
      return this.defaultCase(parameterizedType);
   }

   public Object defaultCase(Type t) {
      return null;
   }
}
