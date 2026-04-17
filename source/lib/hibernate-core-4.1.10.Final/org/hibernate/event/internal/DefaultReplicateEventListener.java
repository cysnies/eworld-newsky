package org.hibernate.event.internal;

import java.io.Serializable;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.ReplicationMode;
import org.hibernate.TransientObjectException;
import org.hibernate.engine.internal.Cascade;
import org.hibernate.engine.spi.CascadingAction;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.Status;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.ReplicateEvent;
import org.hibernate.event.spi.ReplicateEventListener;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public class DefaultReplicateEventListener extends AbstractSaveEventListener implements ReplicateEventListener {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, DefaultReplicateEventListener.class.getName());

   public void onReplicate(ReplicateEvent event) {
      EventSource source = event.getSession();
      if (source.getPersistenceContext().reassociateIfUninitializedProxy(event.getObject())) {
         LOG.trace("Uninitialized proxy passed to replicate()");
      } else {
         Object entity = source.getPersistenceContext().unproxyAndReassociate(event.getObject());
         if (source.getPersistenceContext().isEntryFor(entity)) {
            LOG.trace("Ignoring persistent instance passed to replicate()");
         } else {
            EntityPersister persister = source.getEntityPersister(event.getEntityName(), entity);
            Serializable id = persister.getIdentifier(entity, source);
            if (id == null) {
               throw new TransientObjectException("instance with null id passed to replicate()");
            } else {
               ReplicationMode replicationMode = event.getReplicationMode();
               Object oldVersion;
               if (replicationMode == ReplicationMode.EXCEPTION) {
                  oldVersion = null;
               } else {
                  oldVersion = persister.getCurrentVersion(id, source);
               }

               if (oldVersion != null) {
                  if (LOG.isTraceEnabled()) {
                     LOG.tracev("Found existing row for {0}", MessageHelper.infoString((EntityPersister)persister, (Object)id, (SessionFactoryImplementor)source.getFactory()));
                  }

                  Object realOldVersion = persister.isVersioned() ? oldVersion : null;
                  boolean canReplicate = replicationMode.shouldOverwriteCurrentVersion(entity, realOldVersion, persister.getVersion(entity), persister.getVersionType());
                  if (canReplicate) {
                     this.performReplication(entity, id, realOldVersion, persister, replicationMode, source);
                  } else {
                     LOG.trace("No need to replicate");
                  }
               } else {
                  if (LOG.isTraceEnabled()) {
                     LOG.tracev("No existing row, replicating new instance {0}", MessageHelper.infoString((EntityPersister)persister, (Object)id, (SessionFactoryImplementor)source.getFactory()));
                  }

                  boolean regenerate = persister.isIdentifierAssignedByInsert();
                  EntityKey key = regenerate ? null : source.generateEntityKey(id, persister);
                  this.performSaveOrReplicate(entity, key, persister, regenerate, replicationMode, source, true);
               }

            }
         }
      }
   }

   protected boolean visitCollectionsBeforeSave(Object entity, Serializable id, Object[] values, Type[] types, EventSource source) {
      OnReplicateVisitor visitor = new OnReplicateVisitor(source, id, entity, false);
      visitor.processEntityPropertyValues(values, types);
      return super.visitCollectionsBeforeSave(entity, id, values, types, source);
   }

   protected boolean substituteValuesIfNecessary(Object entity, Serializable id, Object[] values, EntityPersister persister, SessionImplementor source) {
      return false;
   }

   protected boolean isVersionIncrementDisabled() {
      return true;
   }

   private void performReplication(Object entity, Serializable id, Object version, EntityPersister persister, ReplicationMode replicationMode, EventSource source) throws HibernateException {
      if (LOG.isTraceEnabled()) {
         LOG.tracev("Replicating changes to {0}", MessageHelper.infoString((EntityPersister)persister, (Object)id, (SessionFactoryImplementor)source.getFactory()));
      }

      (new OnReplicateVisitor(source, id, entity, true)).process(entity, persister);
      source.getPersistenceContext().addEntity(entity, persister.isMutable() ? Status.MANAGED : Status.READ_ONLY, (Object[])null, source.generateEntityKey(id, persister), version, LockMode.NONE, true, persister, true, false);
      this.cascadeAfterReplicate(entity, persister, replicationMode, source);
   }

   private void cascadeAfterReplicate(Object entity, EntityPersister persister, ReplicationMode replicationMode, EventSource source) {
      source.getPersistenceContext().incrementCascadeLevel();

      try {
         (new Cascade(CascadingAction.REPLICATE, 0, source)).cascade(persister, entity, replicationMode);
      } finally {
         source.getPersistenceContext().decrementCascadeLevel();
      }

   }

   protected CascadingAction getCascadeAction() {
      return CascadingAction.REPLICATE;
   }
}
