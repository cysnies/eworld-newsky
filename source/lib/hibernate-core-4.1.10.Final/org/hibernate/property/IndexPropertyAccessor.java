package org.hibernate.property;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;

public class IndexPropertyAccessor implements PropertyAccessor {
   private final String propertyName;
   private final String entityName;

   public IndexPropertyAccessor(String collectionRole, String entityName) {
      this.propertyName = collectionRole.substring(entityName.length() + 1);
      this.entityName = entityName;
   }

   public Setter getSetter(Class theClass, String propertyName) {
      return new IndexSetter();
   }

   public Getter getGetter(Class theClass, String propertyName) {
      return new IndexGetter();
   }

   public static final class IndexSetter implements Setter {
      public Method getMethod() {
         return null;
      }

      public String getMethodName() {
         return null;
      }

      public void set(Object target, Object value, SessionFactoryImplementor factory) {
      }
   }

   public class IndexGetter implements Getter {
      public Object getForInsert(Object target, Map mergeMap, SessionImplementor session) throws HibernateException {
         return session == null ? BackrefPropertyAccessor.UNKNOWN : session.getPersistenceContext().getIndexInOwner(IndexPropertyAccessor.this.entityName, IndexPropertyAccessor.this.propertyName, target, mergeMap);
      }

      public Object get(Object target) {
         return BackrefPropertyAccessor.UNKNOWN;
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
         return Object.class;
      }
   }
}
