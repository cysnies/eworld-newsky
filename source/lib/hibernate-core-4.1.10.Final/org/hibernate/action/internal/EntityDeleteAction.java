package org.hibernate.action.internal;

import java.io.Serializable;
import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.cache.spi.CacheKey;
import org.hibernate.cache.spi.access.SoftLock;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.service.spi.EventListenerGroup;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PreDeleteEvent;
import org.hibernate.event.spi.PreDeleteEventListener;
import org.hibernate.persister.entity.EntityPersister;

public final class EntityDeleteAction extends EntityAction {
   private final Object version;
   private final boolean isCascadeDeleteEnabled;
   private final Object[] state;
   private SoftLock lock;
   private Object[] naturalIdValues;

   public EntityDeleteAction(Serializable id, Object[] state, Object version, Object instance, EntityPersister persister, boolean isCascadeDeleteEnabled, SessionImplementor session) {
      super(session, id, instance, persister);
      this.version = version;
      this.isCascadeDeleteEnabled = isCascadeDeleteEnabled;
      this.state = state;
      this.naturalIdValues = session.getPersistenceContext().getNaturalIdHelper().removeLocalNaturalIdCrossReference(this.getPersister(), this.getId(), state);
   }

   public void execute() throws HibernateException {
      Serializable id = this.getId();
      EntityPersister persister = this.getPersister();
      SessionImplementor session = this.getSession();
      Object instance = this.getInstance();
      boolean veto = this.preDelete();
      Object version = this.version;
      if (persister.isVersionPropertyGenerated()) {
         version = persister.getVersion(instance);
      }

      CacheKey ck;
      if (persister.hasCache()) {
         ck = session.generateCacheKey(id, persister.getIdentifierType(), persister.getRootEntityName());
         this.lock = persister.getCacheAccessStrategy().lockItem(ck, version);
      } else {
         ck = null;
      }

      if (!this.isCascadeDeleteEnabled && !veto) {
         persister.delete(id, version, instance, session);
      }

      PersistenceContext persistenceContext = session.getPersistenceContext();
      EntityEntry entry = persistenceContext.removeEntry(instance);
      if (entry == null) {
         throw new AssertionFailure("possible nonthreadsafe access to session");
      } else {
         entry.postDelete();
         persistenceContext.removeEntity(entry.getEntityKey());
         persistenceContext.removeProxy(entry.getEntityKey());
         if (persister.hasCache()) {
            persister.getCacheAccessStrategy().remove(ck);
         }

         persistenceContext.getNaturalIdHelper().removeSharedNaturalIdCrossReference(persister, id, this.naturalIdValues);
         this.postDelete();
         if (this.getSession().getFactory().getStatistics().isStatisticsEnabled() && !veto) {
            this.getSession().getFactory().getStatisticsImplementor().deleteEntity(this.getPersister().getEntityName());
         }

      }
   }

   private boolean preDelete() {
      boolean veto = false;
      EventListenerGroup<PreDeleteEventListener> listenerGroup = this.listenerGroup(EventType.PRE_DELETE);
      if (listenerGroup.isEmpty()) {
         return veto;
      } else {
         PreDeleteEvent event = new PreDeleteEvent(this.getInstance(), this.getId(), this.state, this.getPersister(), this.eventSource());

         for(PreDeleteEventListener listener : listenerGroup.listeners()) {
            veto |= listener.onPreDelete(event);
         }

         return veto;
      }
   }

   private void postDelete() {
      EventListenerGroup<PostDeleteEventListener> listenerGroup = this.listenerGroup(EventType.POST_DELETE);
      if (!listenerGroup.isEmpty()) {
         PostDeleteEvent event = new PostDeleteEvent(this.getInstance(), this.getId(), this.state, this.getPersister(), this.eventSource());

         for(PostDeleteEventListener listener : listenerGroup.listeners()) {
            listener.onPostDelete(event);
         }

      }
   }

   private void postCommitDelete() {
      EventListenerGroup<PostDeleteEventListener> listenerGroup = this.listenerGroup(EventType.POST_COMMIT_DELETE);
      if (!listenerGroup.isEmpty()) {
         PostDeleteEvent event = new PostDeleteEvent(this.getInstance(), this.getId(), this.state, this.getPersister(), this.eventSource());

         for(PostDeleteEventListener listener : listenerGroup.listeners()) {
            listener.onPostDelete(event);
         }

      }
   }

   public void doAfterTransactionCompletion(boolean success, SessionImplementor session) throws HibernateException {
      if (this.getPersister().hasCache()) {
         CacheKey ck = this.getSession().generateCacheKey(this.getId(), this.getPersister().getIdentifierType(), this.getPersister().getRootEntityName());
         this.getPersister().getCacheAccessStrategy().unlockItem(ck, this.lock);
      }

      this.postCommitDelete();
   }

   protected boolean hasPostCommitEventListeners() {
      return !this.listenerGroup(EventType.POST_COMMIT_DELETE).isEmpty();
   }
}
