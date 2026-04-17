package org.hibernate.property;

import java.io.Serializable;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Map;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;

public class BackrefPropertyAccessor implements PropertyAccessor {
   private final String propertyName;
   private final String entityName;
   private final BackrefSetter setter;
   private final BackrefGetter getter;
   public static final Serializable UNKNOWN = new Serializable() {
      public String toString() {
         return "<unknown>";
      }

      public Object readResolve() {
         return BackrefPropertyAccessor.UNKNOWN;
      }
   };

   public BackrefPropertyAccessor(String collectionRole, String entityName) {
      this.propertyName = collectionRole.substring(entityName.length() + 1);
      this.entityName = entityName;
      this.setter = new BackrefSetter();
      this.getter = new BackrefGetter();
   }

   public Setter getSetter(Class theClass, String propertyName) {
      return this.setter;
   }

   public Getter getGetter(Class theClass, String propertyName) {
      return this.getter;
   }

   public static final class BackrefSetter implements Setter {
      public Method getMethod() {
         return null;
      }

      public String getMethodName() {
         return null;
      }

      public void set(Object target, Object value, SessionFactoryImplementor factory) {
      }
   }

   public class BackrefGetter implements Getter {
      public Object getForInsert(Object target, Map mergeMap, SessionImplementor session) {
         return session == null ? BackrefPropertyAccessor.UNKNOWN : session.getPersistenceContext().getOwnerId(BackrefPropertyAccessor.this.entityName, BackrefPropertyAccessor.this.propertyName, target, mergeMap);
      }

      public Member getMember() {
         return null;
      }

      public Object get(Object target) {
         return BackrefPropertyAccessor.UNKNOWN;
      }

      public Method getMethod() {
         return null;
      }

      public String getMethodName() {
         return null;
      }

      public Class getReturnType() {
         return Object.class;
      }
   }
}
