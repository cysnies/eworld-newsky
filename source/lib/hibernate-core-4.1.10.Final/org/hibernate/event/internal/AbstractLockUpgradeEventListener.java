package org.hibernate.event.internal;

import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.ObjectDeletedException;
import org.hibernate.cache.spi.CacheKey;
import org.hibernate.cache.spi.access.SoftLock;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.Status;
import org.hibernate.event.spi.EventSource;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.pretty.MessageHelper;
import org.jboss.logging.Logger;

public class AbstractLockUpgradeEventListener extends AbstractReassociateEventListener {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, AbstractLockUpgradeEventListener.class.getName());

   protected void upgradeLock(Object object, EntityEntry entry, LockOptions lockOptions, EventSource source) {
      LockMode requestedLockMode = lockOptions.getLockMode();
      if (requestedLockMode.greaterThan(entry.getLockMode())) {
         if (entry.getStatus() != Status.MANAGED) {
            throw new ObjectDeletedException("attempted to lock a deleted instance", entry.getId(), entry.getPersister().getEntityName());
         }

         EntityPersister persister = entry.getPersister();
         if (LOG.isTraceEnabled()) {
            LOG.tracev("Locking {0} in mode: {1}", MessageHelper.infoString((EntityPersister)persister, (Object)entry.getId(), (SessionFactoryImplementor)source.getFactory()), requestedLockMode);
         }

         SoftLock lock;
         CacheKey ck;
         if (persister.hasCache()) {
            ck = source.generateCacheKey(entry.getId(), persister.getIdentifierType(), persister.getRootEntityName());
            lock = persister.getCacheAccessStrategy().lockItem(ck, entry.getVersion());
         } else {
            ck = null;
            lock = null;
         }

         try {
            if (persister.isVersioned() && requestedLockMode == LockMode.FORCE) {
               Object nextVersion = persister.forceVersionIncrement(entry.getId(), entry.getVersion(), source);
               entry.forceLocked(object, nextVersion);
            } else {
               persister.lock(entry.getId(), entry.getVersion(), object, (LockOptions)lockOptions, source);
            }

            entry.setLockMode(requestedLockMode);
         } finally {
            if (persister.hasCache()) {
               persister.getCacheAccessStrategy().unlockItem(ck, lock);
            }

         }
      }

   }
}
