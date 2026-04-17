package com.comphenix.protocol.reflect;

import com.comphenix.protocol.reflect.fuzzy.AbstractFuzzyMatcher;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class FuzzyReflection {
   private Class source;
   private boolean forceAccess;

   public FuzzyReflection(Class source, boolean forceAccess) {
      this.source = source;
      this.forceAccess = forceAccess;
   }

   public static FuzzyReflection fromClass(Class source) {
      return fromClass(source, false);
   }

   public static FuzzyReflection fromClass(Class source, boolean forceAccess) {
      return new FuzzyReflection(source, forceAccess);
   }

   public static FuzzyReflection fromObject(Object reference) {
      return new FuzzyReflection(reference.getClass(), false);
   }

   public static FuzzyReflection fromObject(Object reference, boolean forceAccess) {
      return new FuzzyReflection(reference.getClass(), forceAccess);
   }

   public Class getSource() {
      return this.source;
   }

   public Method getMethod(AbstractFuzzyMatcher matcher) {
      List<Method> result = this.getMethodList(matcher);
      if (result.size() > 0) {
         return (Method)result.get(0);
      } else {
         throw new IllegalArgumentException("Unable to find a method that matches " + matcher);
      }
   }

   public List getMethodList(AbstractFuzzyMatcher matcher) {
      List<Method> methods = Lists.newArrayList();

      for(Method method : this.getMethods()) {
         if (matcher.isMatch(MethodInfo.fromMethod(method), this.source)) {
            methods.add(method);
         }
      }

      return methods;
   }

   public Method getMethodByName(String nameRegex) {
      Pattern match = Pattern.compile(nameRegex);

      for(Method method : this.getMethods()) {
         if (match.matcher(method.getName()).matches()) {
            return method;
         }
      }

      throw new IllegalArgumentException("Unable to find a method with the pattern " + nameRegex + " in " + this.source.getName());
   }

   public Method getMethodByParameters(String name, Class... args) {
      for(Method method : this.getMethods()) {
         if (Arrays.equals(method.getParameterTypes(), args)) {
            return method;
         }
      }

      throw new IllegalArgumentException("Unable to find " + name + " in " + this.source.getName());
   }

   public Method getMethodByParameters(String name, Class returnType, Class[] args) {
      List<Method> methods = this.getMethodListByParameters(returnType, args);
      if (methods.size() > 0) {
         return (Method)methods.get(0);
      } else {
         throw new IllegalArgumentException("Unable to find " + name + " in " + this.source.getName());
      }
   }

   public Method getMethodByParameters(String name, String returnTypeRegex, String[] argsRegex) {
      Pattern match = Pattern.compile(returnTypeRegex);
      Pattern[] argMatch = new Pattern[argsRegex.length];

      for(int i = 0; i < argsRegex.length; ++i) {
         argMatch[i] = Pattern.compile(argsRegex[i]);
      }

      for(Method method : this.getMethods()) {
         if (match.matcher(method.getReturnType().getName()).matches() && this.matchParameters(argMatch, method.getParameterTypes())) {
            return method;
         }
      }

      throw new IllegalArgumentException("Unable to find " + name + " in " + this.source.getName());
   }

   private boolean matchParameters(Pattern[] parameterMatchers, Class[] argTypes) {
      if (parameterMatchers.length != argTypes.length) {
         throw new IllegalArgumentException("Arrays must have the same cardinality.");
      } else {
         for(int i = 0; i < argTypes.length; ++i) {
            if (!parameterMatchers[i].matcher(argTypes[i].getName()).matches()) {
               return false;
            }
         }

         return true;
      }
   }

   public List getMethodListByParameters(Class returnType, Class[] args) {
      List<Method> methods = new ArrayList();

      for(Method method : this.getMethods()) {
         if (method.getReturnType().equals(returnType) && Arrays.equals(method.getParameterTypes(), args)) {
            methods.add(method);
         }
      }

      return methods;
   }

   public Field getFieldByName(String nameRegex) {
      Pattern match = Pattern.compile(nameRegex);

      for(Field field : this.getFields()) {
         if (match.matcher(field.getName()).matches()) {
            return field;
         }
      }

      throw new IllegalArgumentException("Unable to find a field with the pattern " + nameRegex + " in " + this.source.getName());
   }

   public Field getFieldByType(String name, Class type) {
      List<Field> fields = this.getFieldListByType(type);
      if (fields.size() > 0) {
         return (Field)fields.get(0);
      } else {
         throw new IllegalArgumentException(String.format("Unable to find a field %s with the type %s in %s", name, type.getName(), this.source.getName()));
      }
   }

   public List getFieldListByType(Class type) {
      List<Field> fields = new ArrayList();

      for(Field field : this.getFields()) {
         if (type.isAssignableFrom(field.getType())) {
            fields.add(field);
         }
      }

      return fields;
   }

   public Field getField(AbstractFuzzyMatcher matcher) {
      List<Field> result = this.getFieldList(matcher);
      if (result.size() > 0) {
         return (Field)result.get(0);
      } else {
         throw new IllegalArgumentException("Unable to find a field that matches " + matcher);
      }
   }

   public List getFieldList(AbstractFuzzyMatcher matcher) {
      List<Field> fields = Lists.newArrayList();

      for(Field field : this.getFields()) {
         if (matcher.isMatch(field, this.source)) {
            fields.add(field);
         }
      }

      return fields;
   }

   public Field getFieldByType(String typeRegex) {
      Pattern match = Pattern.compile(typeRegex);

      for(Field field : this.getFields()) {
         String name = field.getType().getName();
         if (match.matcher(name).matches()) {
            return field;
         }
      }

      throw new IllegalArgumentException("Unable to find a field with the type " + typeRegex + " in " + this.source.getName());
   }

   public Field getFieldByType(String typeRegex, Set ignored) {
      Pattern match = Pattern.compile(typeRegex);

      for(Field field : this.getFields()) {
         Class type = field.getType();
         if (!ignored.contains(type) && match.matcher(type.getName()).matches()) {
            return field;
         }
      }

      throw new IllegalArgumentException("Unable to find a field with the type " + typeRegex + " in " + this.source.getName());
   }

   public Constructor getConstructor(AbstractFuzzyMatcher matcher) {
      List<Constructor<?>> result = this.getConstructorList(matcher);
      if (result.size() > 0) {
         return (Constructor)result.get(0);
      } else {
         throw new IllegalArgumentException("Unable to find a method that matches " + matcher);
      }
   }

   public Map getMappedMethods(List methods) {
      Map<String, Method> map = Maps.newHashMap();

      for(Method method : methods) {
         map.put(method.getName(), method);
      }

      return map;
   }

   public List getConstructorList(AbstractFuzzyMatcher matcher) {
      List<Constructor<?>> constructors = Lists.newArrayList();

      for(Constructor constructor : this.getConstructors()) {
         if (matcher.isMatch(MethodInfo.fromConstructor(constructor), this.source)) {
            constructors.add(constructor);
         }
      }

      return constructors;
   }

   public Set getFields() {
      return this.forceAccess ? setUnion(this.source.getDeclaredFields(), this.source.getFields()) : setUnion(this.source.getFields());
   }

   public Set getMethods() {
      return this.forceAccess ? setUnion(this.source.getDeclaredMethods(), this.source.getMethods()) : setUnion(this.source.getMethods());
   }

   public Set getConstructors() {
      return this.forceAccess ? setUnion(this.source.getDeclaredConstructors()) : setUnion(this.source.getConstructors());
   }

   private static Set setUnion(Object[]... array) {
      Set<T> result = new LinkedHashSet();

      for(Object[] elements : array) {
         for(Object element : elements) {
            result.add(element);
         }
      }

      return result;
   }

   public boolean isForceAccess() {
      return this.forceAccess;
   }

   public void setForceAccess(boolean forceAccess) {
      this.forceAccess = forceAccess;
   }
}
