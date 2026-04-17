package org.hibernate.event.internal;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import org.hibernate.LockMode;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.action.internal.AbstractEntityInsertAction;
import org.hibernate.action.internal.EntityIdentityInsertAction;
import org.hibernate.action.internal.EntityInsertAction;
import org.hibernate.bytecode.instrumentation.spi.FieldInterceptor;
import org.hibernate.classic.Lifecycle;
import org.hibernate.engine.internal.Cascade;
import org.hibernate.engine.internal.ForeignKeys;
import org.hibernate.engine.internal.Versioning;
import org.hibernate.engine.spi.CascadingAction;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.Status;
import org.hibernate.event.spi.EventSource;
import org.hibernate.id.IdentifierGenerationException;
import org.hibernate.id.IdentifierGeneratorHelper;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.type.Type;
import org.hibernate.type.TypeHelper;
import org.jboss.logging.Logger;

public abstract class AbstractSaveEventListener extends AbstractReassociateEventListener {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, AbstractSaveEventListener.class.getName());

   protected Serializable saveWithRequestedId(Object entity, Serializable requestedId, String entityName, Object anything, EventSource source) {
      return this.performSave(entity, requestedId, source.getEntityPersister(entityName, entity), false, anything, source, true);
   }

   protected Serializable saveWithGeneratedId(Object entity, String entityName, Object anything, EventSource source, boolean requiresImmediateIdAccess) {
      EntityPersister persister = source.getEntityPersister(entityName, entity);
      Serializable generatedId = persister.getIdentifierGenerator().generate(source, entity);
      if (generatedId == null) {
         throw new IdentifierGenerationException("null id generated for:" + entity.getClass());
      } else if (generatedId == IdentifierGeneratorHelper.SHORT_CIRCUIT_INDICATOR) {
         return source.getIdentifier(entity);
      } else if (generatedId == IdentifierGeneratorHelper.POST_INSERT_INDICATOR) {
         return this.performSave(entity, (Serializable)null, persister, true, anything, source, requiresImmediateIdAccess);
      } else {
         if (LOG.isDebugEnabled()) {
            LOG.debugf("Generated identifier: %s, using strategy: %s", persister.getIdentifierType().toLoggableString(generatedId, source.getFactory()), persister.getIdentifierGenerator().getClass().getName());
         }

         return this.performSave(entity, generatedId, persister, false, anything, source, true);
      }
   }

   protected Serializable performSave(Object entity, Serializable id, EntityPersister persister, boolean useIdentityColumn, Object anything, EventSource source, boolean requiresImmediateIdAccess) {
      if (LOG.isTraceEnabled()) {
         LOG.tracev("Saving {0}", MessageHelper.infoString((EntityPersister)persister, (Object)id, (SessionFactoryImplementor)source.getFactory()));
      }

      EntityKey key;
      if (!useIdentityColumn) {
         key = source.generateEntityKey(id, persister);
         Object old = source.getPersistenceContext().getEntity(key);
         if (old != null) {
            if (source.getPersistenceContext().getEntry(old).getStatus() != Status.DELETED) {
               throw new NonUniqueObjectException(id, persister.getEntityName());
            }

            source.forceFlush(source.getPersistenceContext().getEntry(old));
         }

         persister.setIdentifier(entity, id, source);
      } else {
         key = null;
      }

      return this.invokeSaveLifecycle(entity, persister, source) ? id : this.performSaveOrReplicate(entity, key, persister, useIdentityColumn, anything, source, requiresImmediateIdAccess);
   }

   protected boolean invokeSaveLifecycle(Object entity, EntityPersister persister, EventSource source) {
      if (persister.implementsLifecycle()) {
         LOG.debug("Calling onSave()");
         if (((Lifecycle)entity).onSave(source)) {
            LOG.debug("Insertion vetoed by onSave()");
            return true;
         }
      }

      return false;
   }

   protected Serializable performSaveOrReplicate(Object entity, EntityKey key, EntityPersister persister, boolean useIdentityColumn, Object anything, EventSource source, boolean requiresImmediateIdAccess) {
      Serializable id = key == null ? null : key.getIdentifier();
      boolean inTxn = source.getTransactionCoordinator().isTransactionInProgress();
      boolean shouldDelayIdentityInserts = !inTxn && !requiresImmediateIdAccess;
      source.getPersistenceContext().addEntry(entity, Status.SAVING, (Object[])null, (Object)null, id, (Object)null, LockMode.WRITE, useIdentityColumn, persister, false, false);
      this.cascadeBeforeSave(source, persister, entity, anything);
      Object[] values = persister.getPropertyValuesToInsert(entity, this.getMergeMap(anything), source);
      Type[] types = persister.getPropertyTypes();
      boolean substitute = this.substituteValuesIfNecessary(entity, id, values, persister, source);
      if (persister.hasCollections()) {
         substitute = substitute || this.visitCollectionsBeforeSave(entity, id, values, types, source);
      }

      if (substitute) {
         persister.setPropertyValues(entity, values);
      }

      TypeHelper.deepCopy(values, types, persister.getPropertyUpdateability(), values, source);
      AbstractEntityInsertAction insert = this.addInsertAction(values, id, entity, persister, useIdentityColumn, source, shouldDelayIdentityInserts);
      this.cascadeAfterSave(source, persister, entity, anything);
      if (useIdentityColumn && insert.isEarlyInsert()) {
         if (!EntityIdentityInsertAction.class.isInstance(insert)) {
            throw new IllegalStateException("Insert should be using an identity column, but action is of unexpected type: " + insert.getClass().getName());
         }

         id = ((EntityIdentityInsertAction)insert).getGeneratedId();
         insert.handleNaturalIdPostSaveNotifications(id);
      }

      this.markInterceptorDirty(entity, persister, source);
      return id;
   }

   private AbstractEntityInsertAction addInsertAction(Object[] values, Serializable id, Object entity, EntityPersister persister, boolean useIdentityColumn, EventSource source, boolean shouldDelayIdentityInserts) {
      if (useIdentityColumn) {
         EntityIdentityInsertAction insert = new EntityIdentityInsertAction(values, entity, persister, this.isVersionIncrementDisabled(), source, shouldDelayIdentityInserts);
         source.getActionQueue().addAction(insert);
         return insert;
      } else {
         Object version = Versioning.getVersion(values, persister);
         EntityInsertAction insert = new EntityInsertAction(id, values, entity, version, persister, this.isVersionIncrementDisabled(), source);
         source.getActionQueue().addAction(insert);
         return insert;
      }
   }

   private void markInterceptorDirty(Object entity, EntityPersister persister, EventSource source) {
      if (persister.getInstrumentationMetadata().isInstrumented()) {
         FieldInterceptor interceptor = persister.getInstrumentationMetadata().injectInterceptor(entity, persister.getEntityName(), (Set)null, source);
         interceptor.dirty();
      }

   }

   protected Map getMergeMap(Object anything) {
      return null;
   }

   protected boolean isVersionIncrementDisabled() {
      return false;
   }

   protected boolean visitCollectionsBeforeSave(Object entity, Serializable id, Object[] values, Type[] types, EventSource source) {
      WrapVisitor visitor = new WrapVisitor(source);
      visitor.processEntityPropertyValues(values, types);
      return visitor.isSubstitutionRequired();
   }

   protected boolean substituteValuesIfNecessary(Object entity, Serializable id, Object[] values, EntityPersister persister, SessionImplementor source) {
      boolean substitute = source.getInterceptor().onSave(entity, id, values, persister.getPropertyNames(), persister.getPropertyTypes());
      if (persister.isVersioned()) {
         substitute = Versioning.seedVersion(values, persister.getVersionProperty(), persister.getVersionType(), source) || substitute;
      }

      return substitute;
   }

   protected void cascadeBeforeSave(EventSource source, EntityPersister persister, Object entity, Object anything) {
      source.getPersistenceContext().incrementCascadeLevel();

      try {
         (new Cascade(this.getCascadeAction(), 2, source)).cascade(persister, entity, anything);
      } finally {
         source.getPersistenceContext().decrementCascadeLevel();
      }

   }

   protected void cascadeAfterSave(EventSource source, EntityPersister persister, Object entity, Object anything) {
      source.getPersistenceContext().incrementCascadeLevel();

      try {
         (new Cascade(this.getCascadeAction(), 1, source)).cascade(persister, entity, anything);
      } finally {
         source.getPersistenceContext().decrementCascadeLevel();
      }

   }

   protected abstract CascadingAction getCascadeAction();

   protected EntityState getEntityState(Object entity, String entityName, EntityEntry entry, SessionImplementor source) {
      if (entry != null) {
         if (entry.getStatus() != Status.DELETED) {
            if (LOG.isTraceEnabled()) {
               LOG.tracev("Persistent instance of: {0}", this.getLoggableName(entityName, entity));
            }

            return AbstractSaveEventListener.EntityState.PERSISTENT;
         } else {
            if (LOG.isTraceEnabled()) {
               LOG.tracev("Deleted instance of: {0}", this.getLoggableName(entityName, entity));
            }

            return AbstractSaveEventListener.EntityState.DELETED;
         }
      } else if (ForeignKeys.isTransient(entityName, entity, this.getAssumedUnsaved(), source)) {
         if (LOG.isTraceEnabled()) {
            LOG.tracev("Transient instance of: {0}", this.getLoggableName(entityName, entity));
         }

         return AbstractSaveEventListener.EntityState.TRANSIENT;
      } else {
         if (LOG.isTraceEnabled()) {
            LOG.tracev("Detached instance of: {0}", this.getLoggableName(entityName, entity));
         }

         return AbstractSaveEventListener.EntityState.DETACHED;
      }
   }

   protected String getLoggableName(String entityName, Object entity) {
      return entityName == null ? entity.getClass().getName() : entityName;
   }

   protected Boolean getAssumedUnsaved() {
      return null;
   }

   public static enum EntityState {
      PERSISTENT,
      TRANSIENT,
      DETACHED,
      DELETED;
   }
}
