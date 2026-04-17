package com.comphenix.protocol.reflect;

import com.google.common.collect.Lists;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public abstract class MethodInfo implements GenericDeclaration, Member {
   public static MethodInfo fromMethod(final Method method) {
      return new MethodInfo() {
         public String getName() {
            return method.getName();
         }

         public Class[] getParameterTypes() {
            return method.getParameterTypes();
         }

         public Class getDeclaringClass() {
            return method.getDeclaringClass();
         }

         public Class getReturnType() {
            return method.getReturnType();
         }

         public int getModifiers() {
            return method.getModifiers();
         }

         public Class[] getExceptionTypes() {
            return method.getExceptionTypes();
         }

         public TypeVariable[] getTypeParameters() {
            return method.getTypeParameters();
         }

         public String toGenericString() {
            return method.toGenericString();
         }

         public String toString() {
            return method.toString();
         }

         public boolean isSynthetic() {
            return method.isSynthetic();
         }

         public int hashCode() {
            return method.hashCode();
         }

         public boolean isConstructor() {
            return false;
         }
      };
   }

   public static Collection fromMethods(Method[] methods) {
      return fromMethods((Collection)Arrays.asList(methods));
   }

   public static List fromMethods(Collection methods) {
      List<MethodInfo> infos = Lists.newArrayList();

      for(Method method : methods) {
         infos.add(fromMethod(method));
      }

      return infos;
   }

   public static MethodInfo fromConstructor(final Constructor constructor) {
      return new MethodInfo() {
         public String getName() {
            return constructor.getName();
         }

         public Class[] getParameterTypes() {
            return constructor.getParameterTypes();
         }

         public Class getDeclaringClass() {
            return constructor.getDeclaringClass();
         }

         public Class getReturnType() {
            return Void.class;
         }

         public int getModifiers() {
            return constructor.getModifiers();
         }

         public Class[] getExceptionTypes() {
            return constructor.getExceptionTypes();
         }

         public TypeVariable[] getTypeParameters() {
            return constructor.getTypeParameters();
         }

         public String toGenericString() {
            return constructor.toGenericString();
         }

         public String toString() {
            return constructor.toString();
         }

         public boolean isSynthetic() {
            return constructor.isSynthetic();
         }

         public int hashCode() {
            return constructor.hashCode();
         }

         public boolean isConstructor() {
            return true;
         }
      };
   }

   public static Collection fromConstructors(Constructor[] constructors) {
      return fromConstructors((Collection)Arrays.asList(constructors));
   }

   public static List fromConstructors(Collection constructors) {
      List<MethodInfo> infos = Lists.newArrayList();

      for(Constructor constructor : constructors) {
         infos.add(fromConstructor(constructor));
      }

      return infos;
   }

   public String toString() {
      throw new UnsupportedOperationException();
   }

   public abstract String toGenericString();

   public abstract Class[] getExceptionTypes();

   public abstract Class getReturnType();

   public abstract Class[] getParameterTypes();

   public abstract boolean isConstructor();
}
