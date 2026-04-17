package org.hibernate.annotations.common.reflection.java.generics;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

public class TypeUtils {
   public static boolean isResolved(Type t) {
      return (Boolean)(new TypeSwitch() {
         public Boolean caseClass(Class classType) {
            return true;
         }

         public Boolean caseGenericArrayType(GenericArrayType genericArrayType) {
            return TypeUtils.isResolved(genericArrayType.getGenericComponentType());
         }

         public Boolean caseParameterizedType(ParameterizedType parameterizedType) {
            Type[] typeArgs = parameterizedType.getActualTypeArguments();

            for(Type arg : typeArgs) {
               if (!TypeUtils.isResolved(arg)) {
                  return false;
               }
            }

            return TypeUtils.isResolved(parameterizedType.getRawType());
         }

         public Boolean caseTypeVariable(TypeVariable typeVariable) {
            return false;
         }

         public Boolean caseWildcardType(WildcardType wildcardType) {
            return TypeUtils.areResolved(wildcardType.getUpperBounds()) && TypeUtils.areResolved(wildcardType.getLowerBounds());
         }
      }).doSwitch(t);
   }

   private static Boolean areResolved(Type[] types) {
      for(Type t : types) {
         if (!isResolved(t)) {
            return false;
         }
      }

      return true;
   }

   public static Class getCollectionClass(Type type) {
      return (Class)(new TypeSwitch() {
         public Class caseClass(Class clazz) {
            return TypeUtils.isCollectionClass(clazz) ? clazz : null;
         }

         public Class caseParameterizedType(ParameterizedType parameterizedType) {
            return TypeUtils.getCollectionClass(parameterizedType.getRawType());
         }

         public Class caseWildcardType(WildcardType wildcardType) {
            Type[] upperBounds = wildcardType.getUpperBounds();
            return upperBounds.length == 0 ? null : TypeUtils.getCollectionClass(upperBounds[0]);
         }

         public Class defaultCase(Type t) {
            return null;
         }
      }).doSwitch(type);
   }

   private static boolean isCollectionClass(Class clazz) {
      return clazz == Collection.class || clazz == List.class || clazz == Set.class || clazz == Map.class || clazz == SortedSet.class || clazz == SortedMap.class;
   }

   public static boolean isSimple(Type type) {
      return (Boolean)(new TypeSwitch() {
         public Boolean caseClass(Class clazz) {
            return !clazz.isArray() && !TypeUtils.isCollectionClass(clazz);
         }

         public Boolean caseParameterizedType(ParameterizedType parameterizedType) {
            return TypeUtils.isSimple(parameterizedType.getRawType());
         }

         public Boolean caseWildcardType(WildcardType wildcardType) {
            return TypeUtils.areSimple(wildcardType.getUpperBounds()) && TypeUtils.areSimple(wildcardType.getLowerBounds());
         }

         public Boolean defaultCase(Type t) {
            return false;
         }
      }).doSwitch(type);
   }

   private static Boolean areSimple(Type[] types) {
      for(Type t : types) {
         if (!isSimple(t)) {
            return false;
         }
      }

      return true;
   }

   public static boolean isVoid(Type type) {
      return Void.TYPE.equals(type);
   }

   public static boolean isArray(Type t) {
      return (Boolean)(new TypeSwitch() {
         public Boolean caseClass(Class clazz) {
            return clazz.isArray();
         }

         public Boolean caseGenericArrayType(GenericArrayType genericArrayType) {
            return TypeUtils.isSimple(genericArrayType.getGenericComponentType());
         }

         public Boolean defaultCase(Type type) {
            return false;
         }
      }).doSwitch(t);
   }

   public static boolean isCollection(Type t) {
      return getCollectionClass(t) != null;
   }
}
