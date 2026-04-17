package org.hibernate.action.internal;

import org.hibernate.action.spi.BeforeTransactionCompletionProcess;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.entity.EntityPersister;

public class EntityIncrementVersionProcess implements BeforeTransactionCompletionProcess {
   private final Object object;
   private final EntityEntry entry;

   public EntityIncrementVersionProcess(Object object, EntityEntry entry) {
      this.object = object;
      this.entry = entry;
   }

   public void doBeforeTransactionCompletion(SessionImplementor session) {
      EntityPersister persister = this.entry.getPersister();
      Object nextVersion = persister.forceVersionIncrement(this.entry.getId(), this.entry.getVersion(), session);
      this.entry.forceLocked(this.object, nextVersion);
   }
}
