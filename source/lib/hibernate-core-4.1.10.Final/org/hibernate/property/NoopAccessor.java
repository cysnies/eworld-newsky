package org.hibernate.property;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.PropertyNotFoundException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;

public class NoopAccessor implements PropertyAccessor {
   public Getter getGetter(Class arg0, String arg1) throws PropertyNotFoundException {
      return new NoopGetter();
   }

   public Setter getSetter(Class arg0, String arg1) throws PropertyNotFoundException {
      return new NoopSetter();
   }

   private static class NoopGetter implements Getter {
      private NoopGetter() {
      }

      public Object get(Object target) throws HibernateException {
         return null;
      }

      public Object getForInsert(Object target, Map map, SessionImplementor arg1) throws HibernateException {
         return null;
      }

      public Class getReturnType() {
         return Object.class;
      }

      public Member getMember() {
         return null;
      }

      public String getMethodName() {
         return null;
      }

      public Method getMethod() {
         return null;
      }
   }

   private static class NoopSetter implements Setter {
      private NoopSetter() {
      }

      public void set(Object target, Object value, SessionFactoryImplementor arg2) {
      }

      public String getMethodName() {
         return null;
      }

      public Method getMethod() {
         return null;
      }
   }
}
