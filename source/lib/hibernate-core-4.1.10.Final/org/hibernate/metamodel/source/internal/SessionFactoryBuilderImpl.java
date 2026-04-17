package org.hibernate.metamodel.source.internal;

import java.io.Serializable;
import org.hibernate.EmptyInterceptor;
import org.hibernate.Interceptor;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.SessionFactory;
import org.hibernate.SessionFactoryObserver;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.metamodel.SessionFactoryBuilder;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.proxy.EntityNotFoundDelegate;

public class SessionFactoryBuilderImpl implements SessionFactoryBuilder {
   SessionFactoryOptionsImpl options;
   private final MetadataImplementor metadata;

   SessionFactoryBuilderImpl(MetadataImplementor metadata) {
      this.metadata = metadata;
      this.options = new SessionFactoryOptionsImpl();
   }

   public SessionFactoryBuilder with(Interceptor interceptor) {
      this.options.interceptor = interceptor;
      return this;
   }

   public SessionFactoryBuilder with(EntityNotFoundDelegate entityNotFoundDelegate) {
      this.options.entityNotFoundDelegate = entityNotFoundDelegate;
      return this;
   }

   public SessionFactory buildSessionFactory() {
      return new SessionFactoryImpl(this.metadata, this.options, (SessionFactoryObserver)null);
   }

   private static class SessionFactoryOptionsImpl implements SessionFactory.SessionFactoryOptions {
      private Interceptor interceptor;
      private EntityNotFoundDelegate entityNotFoundDelegate;

      private SessionFactoryOptionsImpl() {
         this.interceptor = EmptyInterceptor.INSTANCE;
         this.entityNotFoundDelegate = new EntityNotFoundDelegate() {
            public void handleEntityNotFound(String entityName, Serializable id) {
               throw new ObjectNotFoundException(id, entityName);
            }
         };
      }

      public Interceptor getInterceptor() {
         return this.interceptor;
      }

      public EntityNotFoundDelegate getEntityNotFoundDelegate() {
         return this.entityNotFoundDelegate;
      }
   }
}
