package org.hibernate.action.internal;

import org.hibernate.OptimisticLockException;
import org.hibernate.action.spi.BeforeTransactionCompletionProcess;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.pretty.MessageHelper;

public class EntityVerifyVersionProcess implements BeforeTransactionCompletionProcess {
   private final Object object;
   private final EntityEntry entry;

   public EntityVerifyVersionProcess(Object object, EntityEntry entry) {
      this.object = object;
      this.entry = entry;
   }

   public void doBeforeTransactionCompletion(SessionImplementor session) {
      EntityPersister persister = this.entry.getPersister();
      Object latestVersion = persister.getCurrentVersion(this.entry.getId(), session);
      if (!this.entry.getVersion().equals(latestVersion)) {
         throw new OptimisticLockException(this.object, "Newer version [" + latestVersion + "] of entity [" + MessageHelper.infoString(this.entry.getEntityName(), this.entry.getId()) + "] found in database");
      }
   }
}
