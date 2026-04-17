package org.hibernate.event.internal;

import org.hibernate.AssertionFailure;
import org.hibernate.LockMode;
import org.hibernate.action.internal.EntityIncrementVersionProcess;
import org.hibernate.action.internal.EntityVerifyVersionProcess;
import org.hibernate.action.spi.BeforeTransactionCompletionProcess;
import org.hibernate.classic.Lifecycle;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.event.spi.PostLoadEvent;
import org.hibernate.event.spi.PostLoadEventListener;
import org.hibernate.persister.entity.EntityPersister;

public class DefaultPostLoadEventListener implements PostLoadEventListener {
   public void onPostLoad(PostLoadEvent event) {
      Object entity = event.getEntity();
      EntityEntry entry = event.getSession().getPersistenceContext().getEntry(entity);
      if (entry == null) {
         throw new AssertionFailure("possible non-threadsafe access to the session");
      } else {
         LockMode lockMode = entry.getLockMode();
         if (LockMode.PESSIMISTIC_FORCE_INCREMENT.equals(lockMode)) {
            EntityPersister persister = entry.getPersister();
            Object nextVersion = persister.forceVersionIncrement(entry.getId(), entry.getVersion(), event.getSession());
            entry.forceLocked(entity, nextVersion);
         } else if (LockMode.OPTIMISTIC_FORCE_INCREMENT.equals(lockMode)) {
            EntityIncrementVersionProcess incrementVersion = new EntityIncrementVersionProcess(entity, entry);
            event.getSession().getActionQueue().registerProcess((BeforeTransactionCompletionProcess)incrementVersion);
         } else if (LockMode.OPTIMISTIC.equals(lockMode)) {
            EntityVerifyVersionProcess verifyVersion = new EntityVerifyVersionProcess(entity, entry);
            event.getSession().getActionQueue().registerProcess((BeforeTransactionCompletionProcess)verifyVersion);
         }

         if (event.getPersister().implementsLifecycle()) {
            ((Lifecycle)event.getEntity()).onLoad(event.getSession(), event.getId());
         }

      }
   }
}
