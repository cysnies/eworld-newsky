package org.hibernate.property;

import java.beans.Introspector;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.PropertyAccessException;
import org.hibernate.PropertyNotFoundException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.ReflectHelper;
import org.jboss.logging.Logger;

public class BasicPropertyAccessor implements PropertyAccessor {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, BasicPropertyAccessor.class.getName());

   public Setter getSetter(Class theClass, String propertyName) throws PropertyNotFoundException {
      return createSetter(theClass, propertyName);
   }

   private static Setter createSetter(Class theClass, String propertyName) throws PropertyNotFoundException {
      BasicSetter result = getSetterOrNull(theClass, propertyName);
      if (result == null) {
         throw new PropertyNotFoundException("Could not find a setter for property " + propertyName + " in class " + theClass.getName());
      } else {
         return result;
      }
   }

   private static BasicSetter getSetterOrNull(Class theClass, String propertyName) {
      if (theClass != Object.class && theClass != null) {
         Method method = setterMethod(theClass, propertyName);
         if (method != null) {
            if (!ReflectHelper.isPublic(theClass, method)) {
               method.setAccessible(true);
            }

            return new BasicSetter(theClass, method, propertyName);
         } else {
            BasicSetter setter = getSetterOrNull(theClass.getSuperclass(), propertyName);
            if (setter == null) {
               Class[] interfaces = theClass.getInterfaces();

               for(int i = 0; setter == null && i < interfaces.length; ++i) {
                  setter = getSetterOrNull(interfaces[i], propertyName);
               }
            }

            return setter;
         }
      } else {
         return null;
      }
   }

   private static Method setterMethod(Class theClass, String propertyName) {
      BasicGetter getter = getGetterOrNull(theClass, propertyName);
      Class returnType = getter == null ? null : getter.getReturnType();
      Method[] methods = theClass.getDeclaredMethods();
      Method potentialSetter = null;

      for(Method method : methods) {
         String methodName = method.getName();
         if (method.getParameterTypes().length == 1 && methodName.startsWith("set")) {
            String testStdMethod = Introspector.decapitalize(methodName.substring(3));
            String testOldMethod = methodName.substring(3);
            if (testStdMethod.equals(propertyName) || testOldMethod.equals(propertyName)) {
               potentialSetter = method;
               if (returnType == null || method.getParameterTypes()[0].equals(returnType)) {
                  return method;
               }
            }
         }
      }

      return potentialSetter;
   }

   public Getter getGetter(Class theClass, String propertyName) throws PropertyNotFoundException {
      return createGetter(theClass, propertyName);
   }

   public static Getter createGetter(Class theClass, String propertyName) throws PropertyNotFoundException {
      BasicGetter result = getGetterOrNull(theClass, propertyName);
      if (result == null) {
         throw new PropertyNotFoundException("Could not find a getter for " + propertyName + " in class " + theClass.getName());
      } else {
         return result;
      }
   }

   private static BasicGetter getGetterOrNull(Class theClass, String propertyName) {
      if (theClass != Object.class && theClass != null) {
         Method method = getterMethod(theClass, propertyName);
         if (method != null) {
            if (!ReflectHelper.isPublic(theClass, method)) {
               method.setAccessible(true);
            }

            return new BasicGetter(theClass, method, propertyName);
         } else {
            BasicGetter getter = getGetterOrNull(theClass.getSuperclass(), propertyName);
            if (getter == null) {
               Class[] interfaces = theClass.getInterfaces();

               for(int i = 0; getter == null && i < interfaces.length; ++i) {
                  getter = getGetterOrNull(interfaces[i], propertyName);
               }
            }

            return getter;
         }
      } else {
         return null;
      }
   }

   private static Method getterMethod(Class theClass, String propertyName) {
      Method[] methods = theClass.getDeclaredMethods();

      for(Method method : methods) {
         if (method.getParameterTypes().length == 0 && !method.isBridge()) {
            String methodName = method.getName();
            if (methodName.startsWith("get")) {
               String testStdMethod = Introspector.decapitalize(methodName.substring(3));
               String testOldMethod = methodName.substring(3);
               if (testStdMethod.equals(propertyName) || testOldMethod.equals(propertyName)) {
                  return method;
               }
            }

            if (methodName.startsWith("is")) {
               String testStdMethod = Introspector.decapitalize(methodName.substring(2));
               String testOldMethod = methodName.substring(2);
               if (testStdMethod.equals(propertyName) || testOldMethod.equals(propertyName)) {
                  return method;
               }
            }
         }
      }

      return null;
   }

   public static final class BasicSetter implements Setter {
      private Class clazz;
      private final transient Method method;
      private final String propertyName;

      private BasicSetter(Class clazz, Method method, String propertyName) {
         this.clazz = clazz;
         this.method = method;
         this.propertyName = propertyName;
      }

      public void set(Object target, Object value, SessionFactoryImplementor factory) throws HibernateException {
         try {
            this.method.invoke(target, value);
         } catch (NullPointerException npe) {
            if (value == null && this.method.getParameterTypes()[0].isPrimitive()) {
               throw new PropertyAccessException(npe, "Null value was assigned to a property of primitive type", true, this.clazz, this.propertyName);
            } else {
               throw new PropertyAccessException(npe, "NullPointerException occurred while calling", true, this.clazz, this.propertyName);
            }
         } catch (InvocationTargetException ite) {
            throw new PropertyAccessException(ite, "Exception occurred inside", true, this.clazz, this.propertyName);
         } catch (IllegalAccessException iae) {
            throw new PropertyAccessException(iae, "IllegalAccessException occurred while calling", true, this.clazz, this.propertyName);
         } catch (IllegalArgumentException iae) {
            if (value == null && this.method.getParameterTypes()[0].isPrimitive()) {
               throw new PropertyAccessException(iae, "Null value was assigned to a property of primitive type", true, this.clazz, this.propertyName);
            } else {
               BasicPropertyAccessor.LOG.illegalPropertySetterArgument(this.clazz.getName(), this.propertyName);
               BasicPropertyAccessor.LOG.expectedType(this.method.getParameterTypes()[0].getName(), value == null ? null : value.getClass().getName());
               throw new PropertyAccessException(iae, "IllegalArgumentException occurred while calling", true, this.clazz, this.propertyName);
            }
         }
      }

      public Method getMethod() {
         return this.method;
      }

      public String getMethodName() {
         return this.method.getName();
      }

      Object readResolve() {
         return BasicPropertyAccessor.createSetter(this.clazz, this.propertyName);
      }

      public String toString() {
         return "BasicSetter(" + this.clazz.getName() + '.' + this.propertyName + ')';
      }
   }

   public static final class BasicGetter implements Getter {
      private Class clazz;
      private final transient Method method;
      private final String propertyName;

      private BasicGetter(Class clazz, Method method, String propertyName) {
         this.clazz = clazz;
         this.method = method;
         this.propertyName = propertyName;
      }

      public Object get(Object target) throws HibernateException {
         try {
            return this.method.invoke(target, (Object[])null);
         } catch (InvocationTargetException ite) {
            throw new PropertyAccessException(ite, "Exception occurred inside", false, this.clazz, this.propertyName);
         } catch (IllegalAccessException iae) {
            throw new PropertyAccessException(iae, "IllegalAccessException occurred while calling", false, this.clazz, this.propertyName);
         } catch (IllegalArgumentException iae) {
            BasicPropertyAccessor.LOG.illegalPropertyGetterArgument(this.clazz.getName(), this.propertyName);
            throw new PropertyAccessException(iae, "IllegalArgumentException occurred calling", false, this.clazz, this.propertyName);
         }
      }

      public Object getForInsert(Object target, Map mergeMap, SessionImplementor session) {
         return this.get(target);
      }

      public Class getReturnType() {
         return this.method.getReturnType();
      }

      public Member getMember() {
         return this.method;
      }

      public Method getMethod() {
         return this.method;
      }

      public String getMethodName() {
         return this.method.getName();
      }

      public String toString() {
         return "BasicGetter(" + this.clazz.getName() + '.' + this.propertyName + ')';
      }

      Object readResolve() {
         return BasicPropertyAccessor.createGetter(this.clazz, this.propertyName);
      }
   }
}
