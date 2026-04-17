package org.hibernate.event.internal;

import java.io.Serializable;
import org.hibernate.HibernateException;
import org.hibernate.engine.internal.Cascade;
import org.hibernate.engine.spi.CascadingAction;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.EvictEvent;
import org.hibernate.event.spi.EvictEventListener;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.jboss.logging.Logger;

public class DefaultEvictEventListener implements EvictEventListener {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, DefaultEvictEventListener.class.getName());

   public void onEvict(EvictEvent event) throws HibernateException {
      EventSource source = event.getSession();
      Object object = event.getObject();
      PersistenceContext persistenceContext = source.getPersistenceContext();
      if (object instanceof HibernateProxy) {
         LazyInitializer li = ((HibernateProxy)object).getHibernateLazyInitializer();
         Serializable id = li.getIdentifier();
         EntityPersister persister = source.getFactory().getEntityPersister(li.getEntityName());
         if (id == null) {
            throw new IllegalArgumentException("null identifier");
         }

         EntityKey key = source.generateEntityKey(id, persister);
         persistenceContext.removeProxy(key);
         if (!li.isUninitialized()) {
            Object entity = persistenceContext.removeEntity(key);
            if (entity != null) {
               EntityEntry e = persistenceContext.removeEntry(entity);
               this.doEvict(entity, key, e.getPersister(), event.getSession());
            }
         }

         li.unsetSession();
      } else {
         EntityEntry e = persistenceContext.removeEntry(object);
         if (e != null) {
            persistenceContext.removeEntity(e.getEntityKey());
            this.doEvict(object, e.getEntityKey(), e.getPersister(), source);
         }
      }

   }

   protected void doEvict(Object object, EntityKey key, EntityPersister persister, EventSource session) throws HibernateException {
      if (LOG.isTraceEnabled()) {
         LOG.tracev("Evicting {0}", MessageHelper.infoString(persister));
      }

      if (persister.hasNaturalIdentifier()) {
         session.getPersistenceContext().getNaturalIdHelper().handleEviction(object, persister, key.getIdentifier());
      }

      if (persister.hasCollections()) {
         (new EvictVisitor(session)).process(object, persister);
      }

      (new Cascade(CascadingAction.EVICT, 0, session)).cascade(persister, object);
   }
}
