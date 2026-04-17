package org.hibernate.annotations.common.reflection.java.generics;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashMap;

class SimpleTypeEnvironment extends HashMap implements TypeEnvironment {
   private static final long serialVersionUID = 1L;
   private final TypeSwitch substitute = new TypeSwitch() {
      public Type caseClass(Class classType) {
         return classType;
      }

      public Type caseGenericArrayType(GenericArrayType genericArrayType) {
         Type originalComponentType = genericArrayType.getGenericComponentType();
         Type boundComponentType = SimpleTypeEnvironment.this.bind(originalComponentType);
         return (Type)(originalComponentType == boundComponentType ? genericArrayType : TypeFactory.createArrayType(boundComponentType));
      }

      public Type caseParameterizedType(ParameterizedType parameterizedType) {
         Type[] originalArguments = parameterizedType.getActualTypeArguments();
         Type[] boundArguments = SimpleTypeEnvironment.this.substitute(originalArguments);
         return this.areSame(originalArguments, boundArguments) ? parameterizedType : TypeFactory.createParameterizedType(parameterizedType.getRawType(), boundArguments, parameterizedType.getOwnerType());
      }

      private boolean areSame(Object[] array1, Object[] array2) {
         if (array1.length != array2.length) {
            return false;
         } else {
            for(int i = 0; i < array1.length; ++i) {
               if (array1[i] != array2[i]) {
                  return false;
               }
            }

            return true;
         }
      }

      public Type caseTypeVariable(TypeVariable typeVariable) {
         return (Type)(!SimpleTypeEnvironment.this.containsKey(typeVariable) ? typeVariable : (Type)SimpleTypeEnvironment.this.get(typeVariable));
      }

      public Type caseWildcardType(WildcardType wildcardType) {
         return wildcardType;
      }
   };

   public SimpleTypeEnvironment(Type[] formalTypeArgs, Type[] actualTypeArgs) {
      for(int i = 0; i < formalTypeArgs.length; ++i) {
         this.put(formalTypeArgs[i], actualTypeArgs[i]);
      }

   }

   public Type bind(Type type) {
      return (Type)this.substitute.doSwitch(type);
   }

   private Type[] substitute(Type[] types) {
      Type[] substTypes = new Type[types.length];

      for(int i = 0; i < substTypes.length; ++i) {
         substTypes[i] = this.bind(types[i]);
      }

      return substTypes;
   }
}
