package org.hibernate.event.internal;

import java.io.Serializable;
import java.util.IdentityHashMap;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.LockOptions;
import org.hibernate.PersistentObjectException;
import org.hibernate.UnresolvableObjectException;
import org.hibernate.cache.spi.CacheKey;
import org.hibernate.engine.internal.Cascade;
import org.hibernate.engine.spi.CascadingAction;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.RefreshEvent;
import org.hibernate.event.spi.RefreshEventListener;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.type.CollectionType;
import org.hibernate.type.CompositeType;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public class DefaultRefreshEventListener implements RefreshEventListener {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, DefaultRefreshEventListener.class.getName());

   public void onRefresh(RefreshEvent event) throws HibernateException {
      this.onRefresh(event, new IdentityHashMap(10));
   }

   public void onRefresh(RefreshEvent event, Map refreshedAlready) {
      EventSource source = event.getSession();
      boolean isTransient = !source.contains(event.getObject());
      if (source.getPersistenceContext().reassociateIfUninitializedProxy(event.getObject())) {
         if (isTransient) {
            source.setReadOnly(event.getObject(), source.isDefaultReadOnly());
         }

      } else {
         Object object = source.getPersistenceContext().unproxyAndReassociate(event.getObject());
         if (refreshedAlready.containsKey(object)) {
            LOG.trace("Already refreshed");
         } else {
            EntityEntry e = source.getPersistenceContext().getEntry(object);
            EntityPersister persister;
            Serializable id;
            if (e == null) {
               persister = source.getEntityPersister(event.getEntityName(), object);
               id = persister.getIdentifier(object, event.getSession());
               if (LOG.isTraceEnabled()) {
                  LOG.tracev("Refreshing transient {0}", MessageHelper.infoString((EntityPersister)persister, (Object)id, (SessionFactoryImplementor)source.getFactory()));
               }

               EntityKey key = source.generateEntityKey(id, persister);
               if (source.getPersistenceContext().getEntry(key) != null) {
                  throw new PersistentObjectException("attempted to refresh transient instance when persistent instance was already associated with the Session: " + MessageHelper.infoString((EntityPersister)persister, (Object)id, (SessionFactoryImplementor)source.getFactory()));
               }
            } else {
               if (LOG.isTraceEnabled()) {
                  LOG.tracev("Refreshing ", MessageHelper.infoString((EntityPersister)e.getPersister(), (Object)e.getId(), (SessionFactoryImplementor)source.getFactory()));
               }

               if (!e.isExistsInDatabase()) {
                  throw new HibernateException("this instance does not yet exist as a row in the database");
               }

               persister = e.getPersister();
               id = e.getId();
            }

            refreshedAlready.put(object, object);
            (new Cascade(CascadingAction.REFRESH, 0, source)).cascade(persister, object, refreshedAlready);
            if (e != null) {
               EntityKey key = source.generateEntityKey(id, persister);
               source.getPersistenceContext().removeEntity(key);
               if (persister.hasCollections()) {
                  (new EvictVisitor(source)).process(object, persister);
               }
            }

            if (persister.hasCache()) {
               CacheKey ck = source.generateCacheKey(id, persister.getIdentifierType(), persister.getRootEntityName());
               persister.getCacheAccessStrategy().evict(ck);
            }

            this.evictCachedCollections(persister, id, source.getFactory());
            String previousFetchProfile = source.getLoadQueryInfluencers().getInternalFetchProfile();
            source.getLoadQueryInfluencers().setInternalFetchProfile("refresh");
            Object result = persister.load(id, object, (LockOptions)event.getLockOptions(), source);
            if (result != null) {
               if (!persister.isMutable()) {
                  source.setReadOnly(result, true);
               } else {
                  source.setReadOnly(result, e == null ? source.isDefaultReadOnly() : e.isReadOnly());
               }
            }

            source.getLoadQueryInfluencers().setInternalFetchProfile(previousFetchProfile);
            UnresolvableObjectException.throwIfNull(result, id, persister.getEntityName());
         }
      }
   }

   private void evictCachedCollections(EntityPersister persister, Serializable id, SessionFactoryImplementor factory) {
      this.evictCachedCollections(persister.getPropertyTypes(), id, factory);
   }

   private void evictCachedCollections(Type[] types, Serializable id, SessionFactoryImplementor factory) throws HibernateException {
      for(Type type : types) {
         if (type.isCollectionType()) {
            factory.getCache().evictCollection(((CollectionType)type).getRole(), id);
         } else if (type.isComponentType()) {
            CompositeType actype = (CompositeType)type;
            this.evictCachedCollections(actype.getSubtypes(), id, factory);
         }
      }

   }
}
