package org.hibernate.bytecode.internal.javassist;

import java.lang.reflect.Method;
import java.util.HashMap;
import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyObject;
import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.bytecode.spi.BasicProxyFactory;
import org.hibernate.bytecode.spi.ProxyFactoryFactory;
import org.hibernate.proxy.ProxyFactory;
import org.hibernate.proxy.pojo.javassist.JavassistProxyFactory;

public class ProxyFactoryFactoryImpl implements ProxyFactoryFactory {
   private static final MethodFilter FINALIZE_FILTER = new MethodFilter() {
      public boolean isHandled(Method m) {
         return m.getParameterTypes().length != 0 || !m.getName().equals("finalize");
      }
   };

   public ProxyFactory buildProxyFactory() {
      return new JavassistProxyFactory();
   }

   public BasicProxyFactory buildBasicProxyFactory(Class superClass, Class[] interfaces) {
      return new BasicProxyFactoryImpl(superClass, interfaces);
   }

   private static class BasicProxyFactoryImpl implements BasicProxyFactory {
      private final Class proxyClass;

      public BasicProxyFactoryImpl(Class superClass, Class[] interfaces) {
         if (superClass != null || interfaces != null && interfaces.length >= 1) {
            javassist.util.proxy.ProxyFactory factory = new javassist.util.proxy.ProxyFactory();
            factory.setFilter(ProxyFactoryFactoryImpl.FINALIZE_FILTER);
            if (superClass != null) {
               factory.setSuperclass(superClass);
            }

            if (interfaces != null && interfaces.length > 0) {
               factory.setInterfaces(interfaces);
            }

            this.proxyClass = factory.createClass();
         } else {
            throw new AssertionFailure("attempting to build proxy without any superclass or interfaces");
         }
      }

      public Object getProxy() {
         try {
            ProxyObject proxy = (ProxyObject)this.proxyClass.newInstance();
            proxy.setHandler(new PassThroughHandler(proxy, this.proxyClass.getName()));
            return proxy;
         } catch (Throwable var2) {
            throw new HibernateException("Unable to instantiated proxy instance");
         }
      }

      public boolean isInstance(Object object) {
         return this.proxyClass.isInstance(object);
      }
   }

   private static class PassThroughHandler implements MethodHandler {
      private HashMap data = new HashMap();
      private final Object proxiedObject;
      private final String proxiedClassName;

      public PassThroughHandler(Object proxiedObject, String proxiedClassName) {
         this.proxiedObject = proxiedObject;
         this.proxiedClassName = proxiedClassName;
      }

      public Object invoke(Object object, Method method, Method method1, Object[] args) throws Exception {
         String name = method.getName();
         if ("toString".equals(name)) {
            return this.proxiedClassName + "@" + System.identityHashCode(object);
         } else if ("equals".equals(name)) {
            return this.proxiedObject == object;
         } else if ("hashCode".equals(name)) {
            return System.identityHashCode(object);
         } else {
            boolean hasGetterSignature = method.getParameterTypes().length == 0 && method.getReturnType() != null;
            boolean hasSetterSignature = method.getParameterTypes().length == 1 && (method.getReturnType() == null || method.getReturnType() == Void.TYPE);
            if (name.startsWith("get") && hasGetterSignature) {
               String propName = name.substring(3);
               return this.data.get(propName);
            } else if (name.startsWith("is") && hasGetterSignature) {
               String propName = name.substring(2);
               return this.data.get(propName);
            } else if (name.startsWith("set") && hasSetterSignature) {
               String propName = name.substring(3);
               this.data.put(propName, args[0]);
               return null;
            } else {
               return null;
            }
         }
      }
   }
}
