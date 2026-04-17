package org.hibernate.event.internal;

import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.spi.EventSource;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.CollectionType;
import org.hibernate.type.CompositeType;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public class WrapVisitor extends ProxyVisitor {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, WrapVisitor.class.getName());
   boolean substitute = false;

   boolean isSubstitutionRequired() {
      return this.substitute;
   }

   WrapVisitor(EventSource session) {
      super(session);
   }

   Object processCollection(Object collection, CollectionType collectionType) throws HibernateException {
      if (collection != null && collection instanceof PersistentCollection) {
         SessionImplementor session = this.getSession();
         PersistentCollection coll = (PersistentCollection)collection;
         if (coll.setCurrentSession(session)) {
            this.reattachCollection(coll, collectionType);
         }

         return null;
      } else {
         return this.processArrayOrNewCollection(collection, collectionType);
      }
   }

   final Object processArrayOrNewCollection(Object collection, CollectionType collectionType) throws HibernateException {
      SessionImplementor session = this.getSession();
      if (collection == null) {
         return null;
      } else {
         CollectionPersister persister = session.getFactory().getCollectionPersister(collectionType.getRole());
         PersistenceContext persistenceContext = session.getPersistenceContext();
         if (collectionType.hasHolder()) {
            if (collection == CollectionType.UNFETCHED_COLLECTION) {
               return null;
            } else {
               PersistentCollection ah = persistenceContext.getCollectionHolder(collection);
               if (ah == null) {
                  ah = collectionType.wrap(session, collection);
                  persistenceContext.addNewCollection(persister, ah);
                  persistenceContext.addCollectionHolder(ah);
               }

               return null;
            }
         } else {
            PersistentCollection persistentCollection = collectionType.wrap(session, collection);
            persistenceContext.addNewCollection(persister, persistentCollection);
            if (LOG.isTraceEnabled()) {
               LOG.tracev("Wrapped collection in role: {0}", collectionType.getRole());
            }

            return persistentCollection;
         }
      }
   }

   void processValue(int i, Object[] values, Type[] types) {
      Object result = this.processValue(values[i], types[i]);
      if (result != null) {
         this.substitute = true;
         values[i] = result;
      }

   }

   Object processComponent(Object component, CompositeType componentType) throws HibernateException {
      if (component != null) {
         Object[] values = componentType.getPropertyValues(component, (SessionImplementor)this.getSession());
         Type[] types = componentType.getSubtypes();
         boolean substituteComponent = false;

         for(int i = 0; i < types.length; ++i) {
            Object result = this.processValue(values[i], types[i]);
            if (result != null) {
               values[i] = result;
               substituteComponent = true;
            }
         }

         if (substituteComponent) {
            componentType.setPropertyValues(component, values, EntityMode.POJO);
         }
      }

      return null;
   }

   void process(Object object, EntityPersister persister) throws HibernateException {
      Object[] values = persister.getPropertyValues(object);
      Type[] types = persister.getPropertyTypes();
      this.processEntityPropertyValues(values, types);
      if (this.isSubstitutionRequired()) {
         persister.setPropertyValues(object, values);
      }

   }
}
