package org.hibernate.action.internal;

import java.io.Serializable;
import org.hibernate.AssertionFailure;
import org.hibernate.action.spi.AfterTransactionCompletionProcess;
import org.hibernate.action.spi.BeforeTransactionCompletionProcess;
import org.hibernate.action.spi.Executable;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.service.spi.EventListenerGroup;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.pretty.MessageHelper;

public abstract class EntityAction implements Executable, Serializable, Comparable, AfterTransactionCompletionProcess {
   private final String entityName;
   private final Serializable id;
   private transient Object instance;
   private transient SessionImplementor session;
   private transient EntityPersister persister;

   protected EntityAction(SessionImplementor session, Serializable id, Object instance, EntityPersister persister) {
      this.entityName = persister.getEntityName();
      this.id = id;
      this.instance = instance;
      this.session = session;
      this.persister = persister;
   }

   public BeforeTransactionCompletionProcess getBeforeTransactionCompletionProcess() {
      return null;
   }

   public AfterTransactionCompletionProcess getAfterTransactionCompletionProcess() {
      return this.needsAfterTransactionCompletion() ? this : null;
   }

   protected abstract boolean hasPostCommitEventListeners();

   public boolean needsAfterTransactionCompletion() {
      return this.persister.hasCache() || this.hasPostCommitEventListeners();
   }

   public String getEntityName() {
      return this.entityName;
   }

   public final Serializable getId() {
      if (this.id instanceof DelayedPostInsertIdentifier) {
         Serializable eeId = this.session.getPersistenceContext().getEntry(this.instance).getId();
         return eeId instanceof DelayedPostInsertIdentifier ? null : eeId;
      } else {
         return this.id;
      }
   }

   public final DelayedPostInsertIdentifier getDelayedId() {
      return DelayedPostInsertIdentifier.class.isInstance(this.id) ? (DelayedPostInsertIdentifier)DelayedPostInsertIdentifier.class.cast(this.id) : null;
   }

   public final Object getInstance() {
      return this.instance;
   }

   public final SessionImplementor getSession() {
      return this.session;
   }

   public final EntityPersister getPersister() {
      return this.persister;
   }

   public final Serializable[] getPropertySpaces() {
      return this.persister.getPropertySpaces();
   }

   public void beforeExecutions() {
      throw new AssertionFailure("beforeExecutions() called for non-collection action");
   }

   public String toString() {
      return StringHelper.unqualify(this.getClass().getName()) + MessageHelper.infoString(this.entityName, this.id);
   }

   public int compareTo(Object other) {
      EntityAction action = (EntityAction)other;
      int roleComparison = this.entityName.compareTo(action.entityName);
      return roleComparison != 0 ? roleComparison : this.persister.getIdentifierType().compare(this.id, action.id);
   }

   public void afterDeserialize(SessionImplementor session) {
      if (this.session == null && this.persister == null) {
         if (session != null) {
            this.session = session;
            this.persister = session.getFactory().getEntityPersister(this.entityName);
            this.instance = session.getPersistenceContext().getEntity(session.generateEntityKey(this.id, this.persister));
         }

      } else {
         throw new IllegalStateException("already attached to a session.");
      }
   }

   protected EventListenerGroup listenerGroup(EventType eventType) {
      return ((EventListenerRegistry)this.getSession().getFactory().getServiceRegistry().getService(EventListenerRegistry.class)).getEventListenerGroup(eventType);
   }

   protected EventSource eventSource() {
      return (EventSource)this.getSession();
   }
}
