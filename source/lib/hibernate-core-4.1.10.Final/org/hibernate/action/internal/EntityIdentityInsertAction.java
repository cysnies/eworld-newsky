package org.hibernate.action.internal;

import java.io.Serializable;
import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.service.spi.EventListenerGroup;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreInsertEventListener;
import org.hibernate.persister.entity.EntityPersister;

public final class EntityIdentityInsertAction extends AbstractEntityInsertAction {
   private final boolean isDelayed;
   private final EntityKey delayedEntityKey;
   private EntityKey entityKey;
   private Serializable generatedId;

   public EntityIdentityInsertAction(Object[] state, Object instance, EntityPersister persister, boolean isVersionIncrementDisabled, SessionImplementor session, boolean isDelayed) throws HibernateException {
      super(isDelayed ? generateDelayedPostInsertIdentifier() : null, state, instance, isVersionIncrementDisabled, persister, session);
      this.isDelayed = isDelayed;
      this.delayedEntityKey = isDelayed ? this.generateDelayedEntityKey() : null;
   }

   public void execute() throws HibernateException {
      this.nullifyTransientReferencesIfNotAlready();
      EntityPersister persister = this.getPersister();
      SessionImplementor session = this.getSession();
      Object instance = this.getInstance();
      boolean veto = this.preInsert();
      if (!veto) {
         this.generatedId = persister.insert(this.getState(), instance, session);
         if (persister.hasInsertGeneratedProperties()) {
            persister.processInsertGeneratedProperties(this.generatedId, instance, this.getState(), session);
         }

         persister.setIdentifier(instance, this.generatedId, session);
         session.getPersistenceContext().registerInsertedKey(this.getPersister(), this.generatedId);
         this.entityKey = session.generateEntityKey(this.generatedId, persister);
         session.getPersistenceContext().checkUniqueness(this.entityKey, this.getInstance());
      }

      this.postInsert();
      if (session.getFactory().getStatistics().isStatisticsEnabled() && !veto) {
         session.getFactory().getStatisticsImplementor().insertEntity(this.getPersister().getEntityName());
      }

      this.markExecuted();
   }

   public boolean needsAfterTransactionCompletion() {
      return this.hasPostCommitEventListeners();
   }

   protected boolean hasPostCommitEventListeners() {
      return !this.listenerGroup(EventType.POST_COMMIT_INSERT).isEmpty();
   }

   public void doAfterTransactionCompletion(boolean success, SessionImplementor session) {
      this.postCommitInsert();
   }

   private void postInsert() {
      if (this.isDelayed) {
         this.getSession().getPersistenceContext().replaceDelayedEntityIdentityInsertKeys(this.delayedEntityKey, this.generatedId);
      }

      EventListenerGroup<PostInsertEventListener> listenerGroup = this.listenerGroup(EventType.POST_INSERT);
      if (!listenerGroup.isEmpty()) {
         PostInsertEvent event = new PostInsertEvent(this.getInstance(), this.generatedId, this.getState(), this.getPersister(), this.eventSource());

         for(PostInsertEventListener listener : listenerGroup.listeners()) {
            listener.onPostInsert(event);
         }

      }
   }

   private void postCommitInsert() {
      EventListenerGroup<PostInsertEventListener> listenerGroup = this.listenerGroup(EventType.POST_COMMIT_INSERT);
      if (!listenerGroup.isEmpty()) {
         PostInsertEvent event = new PostInsertEvent(this.getInstance(), this.generatedId, this.getState(), this.getPersister(), this.eventSource());

         for(PostInsertEventListener listener : listenerGroup.listeners()) {
            listener.onPostInsert(event);
         }

      }
   }

   private boolean preInsert() {
      EventListenerGroup<PreInsertEventListener> listenerGroup = this.listenerGroup(EventType.PRE_INSERT);
      if (listenerGroup.isEmpty()) {
         return false;
      } else {
         boolean veto = false;
         PreInsertEvent event = new PreInsertEvent(this.getInstance(), (Serializable)null, this.getState(), this.getPersister(), this.eventSource());

         for(PreInsertEventListener listener : listenerGroup.listeners()) {
            veto |= listener.onPreInsert(event);
         }

         return veto;
      }
   }

   public final Serializable getGeneratedId() {
      return this.generatedId;
   }

   public EntityKey getDelayedEntityKey() {
      return this.delayedEntityKey;
   }

   public boolean isEarlyInsert() {
      return !this.isDelayed;
   }

   protected EntityKey getEntityKey() {
      return this.entityKey != null ? this.entityKey : this.delayedEntityKey;
   }

   private static synchronized DelayedPostInsertIdentifier generateDelayedPostInsertIdentifier() {
      return new DelayedPostInsertIdentifier();
   }

   private EntityKey generateDelayedEntityKey() {
      if (!this.isDelayed) {
         throw new AssertionFailure("cannot request delayed entity-key for early-insert post-insert-id generation");
      } else {
         return this.getSession().generateEntityKey(this.getDelayedId(), this.getPersister());
      }
   }
}
