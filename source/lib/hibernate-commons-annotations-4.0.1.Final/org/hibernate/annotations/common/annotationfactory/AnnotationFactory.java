package org.hibernate.annotations.common.annotationfactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

public class AnnotationFactory {
   public static Annotation create(AnnotationDescriptor descriptor) {
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      Class<T> proxyClass = Proxy.getProxyClass(classLoader, descriptor.type());
      InvocationHandler handler = new AnnotationProxy(descriptor);

      try {
         return getProxyInstance(proxyClass, handler);
      } catch (RuntimeException e) {
         throw e;
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   private static Annotation getProxyInstance(Class proxyClass, InvocationHandler handler) throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
      Constructor<T> constructor = proxyClass.getConstructor(InvocationHandler.class);
      return (Annotation)constructor.newInstance(handler);
   }
}
