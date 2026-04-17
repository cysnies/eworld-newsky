package org.hibernate.property;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.PropertyNotFoundException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;

public class EmbeddedPropertyAccessor implements PropertyAccessor {
   public Getter getGetter(Class theClass, String propertyName) throws PropertyNotFoundException {
      return new EmbeddedGetter(theClass);
   }

   public Setter getSetter(Class theClass, String propertyName) throws PropertyNotFoundException {
      return new EmbeddedSetter(theClass);
   }

   public static final class EmbeddedGetter implements Getter {
      private final Class clazz;

      EmbeddedGetter(Class clazz) {
         this.clazz = clazz;
      }

      public Object get(Object target) throws HibernateException {
         return target;
      }

      public Object getForInsert(Object target, Map mergeMap, SessionImplementor session) {
         return this.get(target);
      }

      public Member getMember() {
         return null;
      }

      public Method getMethod() {
         return null;
      }

      public String getMethodName() {
         return null;
      }

      public Class getReturnType() {
         return this.clazz;
      }

      public String toString() {
         return "EmbeddedGetter(" + this.clazz.getName() + ')';
      }
   }

   public static final class EmbeddedSetter implements Setter {
      private final Class clazz;

      EmbeddedSetter(Class clazz) {
         this.clazz = clazz;
      }

      public Method getMethod() {
         return null;
      }

      public String getMethodName() {
         return null;
      }

      public void set(Object target, Object value, SessionFactoryImplementor factory) {
      }

      public String toString() {
         return "EmbeddedSetter(" + this.clazz.getName() + ')';
      }
   }
}
