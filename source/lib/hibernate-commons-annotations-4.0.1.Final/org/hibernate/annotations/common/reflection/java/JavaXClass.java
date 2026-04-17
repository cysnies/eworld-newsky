package org.hibernate.annotations.common.reflection.java;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import org.hibernate.annotations.common.reflection.Filter;
import org.hibernate.annotations.common.reflection.ReflectionUtil;
import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.annotations.common.reflection.XMethod;
import org.hibernate.annotations.common.reflection.XProperty;
import org.hibernate.annotations.common.reflection.java.generics.CompoundTypeEnvironment;
import org.hibernate.annotations.common.reflection.java.generics.TypeEnvironment;

class JavaXClass extends JavaXAnnotatedElement implements XClass {
   private final TypeEnvironment context;
   private final Class clazz;

   public JavaXClass(Class clazz, TypeEnvironment env, JavaReflectionManager factory) {
      super(clazz, factory);
      this.clazz = clazz;
      this.context = env;
   }

   public String getName() {
      return this.toClass().getName();
   }

   public XClass getSuperclass() {
      return this.getFactory().toXClass(this.toClass().getSuperclass(), CompoundTypeEnvironment.create(this.getTypeEnvironment(), this.getFactory().getTypeEnvironment(this.toClass())));
   }

   public XClass[] getInterfaces() {
      Class[] classes = this.toClass().getInterfaces();
      int length = classes.length;
      XClass[] xClasses = new XClass[length];
      if (length != 0) {
         TypeEnvironment environment = CompoundTypeEnvironment.create(this.getTypeEnvironment(), this.getFactory().getTypeEnvironment(this.toClass()));

         for(int index = 0; index < length; ++index) {
            xClasses[index] = this.getFactory().toXClass(classes[index], environment);
         }
      }

      return xClasses;
   }

   public boolean isInterface() {
      return this.toClass().isInterface();
   }

   public boolean isAbstract() {
      return Modifier.isAbstract(this.toClass().getModifiers());
   }

   public boolean isPrimitive() {
      return this.toClass().isPrimitive();
   }

   public boolean isEnum() {
      return this.toClass().isEnum();
   }

   private List getDeclaredFieldProperties(Filter filter) {
      List<XProperty> result = new LinkedList();

      for(Field f : this.toClass().getDeclaredFields()) {
         if (ReflectionUtil.isProperty(f, this.getTypeEnvironment().bind(f.getGenericType()), filter)) {
            result.add(this.getFactory().getXProperty(f, this.getTypeEnvironment()));
         }
      }

      return result;
   }

   private List getDeclaredMethodProperties(Filter filter) {
      List<XProperty> result = new LinkedList();

      for(Method m : this.toClass().getDeclaredMethods()) {
         if (ReflectionUtil.isProperty(m, this.getTypeEnvironment().bind(m.getGenericReturnType()), filter)) {
            result.add(this.getFactory().getXProperty(m, this.getTypeEnvironment()));
         }
      }

      return result;
   }

   public List getDeclaredProperties(String accessType) {
      return this.getDeclaredProperties(accessType, XClass.DEFAULT_FILTER);
   }

   public List getDeclaredProperties(String accessType, Filter filter) {
      if (accessType.equals("field")) {
         return this.getDeclaredFieldProperties(filter);
      } else if (accessType.equals("property")) {
         return this.getDeclaredMethodProperties(filter);
      } else {
         throw new IllegalArgumentException("Unknown access type " + accessType);
      }
   }

   public List getDeclaredMethods() {
      List<XMethod> result = new LinkedList();

      for(Method m : this.toClass().getDeclaredMethods()) {
         result.add(this.getFactory().getXMethod(m, this.getTypeEnvironment()));
      }

      return result;
   }

   public Class toClass() {
      return this.clazz;
   }

   public boolean isAssignableFrom(XClass c) {
      return this.toClass().isAssignableFrom(((JavaXClass)c).toClass());
   }

   boolean isArray() {
      return this.toClass().isArray();
   }

   TypeEnvironment getTypeEnvironment() {
      return this.context;
   }

   public String toString() {
      return this.getName();
   }
}
