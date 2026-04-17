package org.hibernate.proxy.pojo.javassist;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.pojo.BasicLazyInitializer;
import org.hibernate.type.CompositeType;
import org.jboss.logging.Logger;

public class JavassistLazyInitializer extends BasicLazyInitializer implements MethodHandler {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, JavassistLazyInitializer.class.getName());
   private static final MethodFilter FINALIZE_FILTER = new MethodFilter() {
      public boolean isHandled(Method m) {
         return m.getParameterTypes().length != 0 || !m.getName().equals("finalize");
      }
   };
   private Class[] interfaces;
   private boolean constructed = false;

   private JavassistLazyInitializer(String entityName, Class persistentClass, Class[] interfaces, Serializable id, Method getIdentifierMethod, Method setIdentifierMethod, CompositeType componentIdType, SessionImplementor session, boolean overridesEquals) {
      super(entityName, persistentClass, id, getIdentifierMethod, setIdentifierMethod, componentIdType, session, overridesEquals);
      this.interfaces = interfaces;
   }

   public static HibernateProxy getProxy(String entityName, Class persistentClass, Class[] interfaces, Method getIdentifierMethod, Method setIdentifierMethod, CompositeType componentIdType, Serializable id, SessionImplementor session) throws HibernateException {
      try {
         JavassistLazyInitializer instance = new JavassistLazyInitializer(entityName, persistentClass, interfaces, id, getIdentifierMethod, setIdentifierMethod, componentIdType, session, ReflectHelper.overridesEquals(persistentClass));
         ProxyFactory factory = new ProxyFactory();
         factory.setSuperclass(interfaces.length == 1 ? persistentClass : null);
         factory.setInterfaces(interfaces);
         factory.setFilter(FINALIZE_FILTER);
         Class cl = factory.createClass();
         HibernateProxy proxy = (HibernateProxy)cl.newInstance();
         ((ProxyObject)proxy).setHandler(instance);
         instance.constructed = true;
         return proxy;
      } catch (Throwable t) {
         LOG.error(LOG.javassistEnhancementFailed(entityName), t);
         throw new HibernateException(LOG.javassistEnhancementFailed(entityName), t);
      }
   }

   public static HibernateProxy getProxy(Class factory, String entityName, Class persistentClass, Class[] interfaces, Method getIdentifierMethod, Method setIdentifierMethod, CompositeType componentIdType, Serializable id, SessionImplementor session, boolean classOverridesEquals) throws HibernateException {
      JavassistLazyInitializer instance = new JavassistLazyInitializer(entityName, persistentClass, interfaces, id, getIdentifierMethod, setIdentifierMethod, componentIdType, session, classOverridesEquals);

      HibernateProxy proxy;
      try {
         proxy = (HibernateProxy)factory.newInstance();
      } catch (Exception e) {
         throw new HibernateException("Javassist Enhancement failed: " + persistentClass.getName(), e);
      }

      ((ProxyObject)proxy).setHandler(instance);
      instance.constructed = true;
      return proxy;
   }

   public static Class getProxyFactory(Class persistentClass, Class[] interfaces) throws HibernateException {
      try {
         ProxyFactory factory = new ProxyFactory();
         factory.setSuperclass(interfaces.length == 1 ? persistentClass : null);
         factory.setInterfaces(interfaces);
         factory.setFilter(FINALIZE_FILTER);
         return factory.createClass();
      } catch (Throwable t) {
         LOG.error(LOG.javassistEnhancementFailed(persistentClass.getName()), t);
         throw new HibernateException(LOG.javassistEnhancementFailed(persistentClass.getName()), t);
      }
   }

   public Object invoke(Object proxy, Method thisMethod, Method proceed, Object[] args) throws Throwable {
      if (this.constructed) {
         Object result;
         try {
            result = this.invoke(thisMethod, args, proxy);
         } catch (Throwable t) {
            throw new Exception(t.getCause());
         }

         if (result == INVOKE_IMPLEMENTATION) {
            Object target = this.getImplementation();

            try {
               Object returnValue;
               if (ReflectHelper.isPublic(this.persistentClass, thisMethod)) {
                  if (!thisMethod.getDeclaringClass().isInstance(target)) {
                     throw new ClassCastException(target.getClass().getName());
                  }

                  returnValue = thisMethod.invoke(target, args);
               } else {
                  if (!thisMethod.isAccessible()) {
                     thisMethod.setAccessible(true);
                  }

                  returnValue = thisMethod.invoke(target, args);
               }

               return returnValue == target ? proxy : returnValue;
            } catch (InvocationTargetException ite) {
               throw ite.getTargetException();
            }
         } else {
            return result;
         }
      } else {
         return thisMethod.getName().equals("getHibernateLazyInitializer") ? this : proceed.invoke(proxy, args);
      }
   }

   protected Object serializableProxy() {
      return new SerializableProxy(this.getEntityName(), this.persistentClass, this.interfaces, this.getIdentifier(), this.isReadOnlySettingAvailable() ? this.isReadOnly() : this.isReadOnlyBeforeAttachedToSession(), this.getIdentifierMethod, this.setIdentifierMethod, this.componentIdType);
   }
}
