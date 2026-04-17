package org.hibernate.annotations.common.annotationfactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class AnnotationProxy implements Annotation, InvocationHandler {
   private final Class annotationType;
   private final Map values;

   public AnnotationProxy(AnnotationDescriptor descriptor) {
      this.annotationType = descriptor.type();
      this.values = this.getAnnotationValues(descriptor);
   }

   private Map getAnnotationValues(AnnotationDescriptor descriptor) {
      Map<Method, Object> result = new HashMap();
      int processedValuesFromDescriptor = 0;

      for(Method m : this.annotationType.getDeclaredMethods()) {
         if (descriptor.containsElement(m.getName())) {
            result.put(m, descriptor.valueOf(m.getName()));
            ++processedValuesFromDescriptor;
         } else {
            if (m.getDefaultValue() == null) {
               throw new IllegalArgumentException("No value provided for " + m.getName());
            }

            result.put(m, m.getDefaultValue());
         }
      }

      if (processedValuesFromDescriptor != descriptor.numberOfElements()) {
         throw new RuntimeException("Trying to instanciate " + this.annotationType + " with unknown elements");
      } else {
         return result;
      }
   }

   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      return this.values.containsKey(method) ? this.values.get(method) : method.invoke(this, args);
   }

   public Class annotationType() {
      return this.annotationType;
   }

   public String toString() {
      StringBuilder result = new StringBuilder();
      result.append('@').append(this.annotationType().getName()).append('(');

      for(Method m : this.getRegisteredMethodsInAlphabeticalOrder()) {
         result.append(m.getName()).append('=').append(this.values.get(m)).append(", ");
      }

      if (this.values.size() > 0) {
         result.delete(result.length() - 2, result.length());
         result.append(")");
      } else {
         result.delete(result.length() - 1, result.length());
      }

      return result.toString();
   }

   private SortedSet getRegisteredMethodsInAlphabeticalOrder() {
      SortedSet<Method> result = new TreeSet(new Comparator() {
         public int compare(Method o1, Method o2) {
            return o1.getName().compareTo(o2.getName());
         }
      });
      result.addAll(this.values.keySet());
      return result;
   }
}
