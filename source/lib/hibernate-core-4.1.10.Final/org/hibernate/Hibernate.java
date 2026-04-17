package org.hibernate;

import java.util.Iterator;
import org.hibernate.bytecode.instrumentation.internal.FieldInterceptionHelper;
import org.hibernate.bytecode.instrumentation.spi.FieldInterceptor;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.HibernateIterator;
import org.hibernate.engine.jdbc.LobCreator;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

public final class Hibernate {
   private Hibernate() {
      throw new UnsupportedOperationException();
   }

   public static void initialize(Object proxy) throws HibernateException {
      if (proxy != null) {
         if (proxy instanceof HibernateProxy) {
            ((HibernateProxy)proxy).getHibernateLazyInitializer().initialize();
         } else if (proxy instanceof PersistentCollection) {
            ((PersistentCollection)proxy).forceInitialization();
         }

      }
   }

   public static boolean isInitialized(Object proxy) {
      if (proxy instanceof HibernateProxy) {
         return !((HibernateProxy)proxy).getHibernateLazyInitializer().isUninitialized();
      } else {
         return proxy instanceof PersistentCollection ? ((PersistentCollection)proxy).wasInitialized() : true;
      }
   }

   public static Class getClass(Object proxy) {
      return proxy instanceof HibernateProxy ? ((HibernateProxy)proxy).getHibernateLazyInitializer().getImplementation().getClass() : proxy.getClass();
   }

   public static LobCreator getLobCreator(Session session) {
      return getLobCreator((SessionImplementor)session);
   }

   public static LobCreator getLobCreator(SessionImplementor session) {
      return session.getFactory().getJdbcServices().getLobCreator(session);
   }

   public static void close(Iterator iterator) throws HibernateException {
      if (iterator instanceof HibernateIterator) {
         ((HibernateIterator)iterator).close();
      } else {
         throw new IllegalArgumentException("not a Hibernate iterator");
      }
   }

   public static boolean isPropertyInitialized(Object proxy, String propertyName) {
      Object entity;
      if (proxy instanceof HibernateProxy) {
         LazyInitializer li = ((HibernateProxy)proxy).getHibernateLazyInitializer();
         if (li.isUninitialized()) {
            return false;
         }

         entity = li.getImplementation();
      } else {
         entity = proxy;
      }

      if (!FieldInterceptionHelper.isInstrumented(entity)) {
         return true;
      } else {
         FieldInterceptor interceptor = FieldInterceptionHelper.extractFieldInterceptor(entity);
         return interceptor == null || interceptor.isInitialized(propertyName);
      }
   }
}
