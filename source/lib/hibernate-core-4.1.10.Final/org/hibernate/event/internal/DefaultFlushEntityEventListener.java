package org.hibernate.event.internal;

import java.io.Serializable;
import java.util.Arrays;
import org.hibernate.AssertionFailure;
import org.hibernate.CustomEntityDirtinessStrategy;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.hibernate.action.internal.DelayedPostInsertIdentifier;
import org.hibernate.action.internal.EntityUpdateAction;
import org.hibernate.engine.internal.Nullability;
import org.hibernate.engine.internal.Versioning;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.Status;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.FlushEntityEvent;
import org.hibernate.event.spi.FlushEntityEventListener;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public class DefaultFlushEntityEventListener implements FlushEntityEventListener {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, DefaultFlushEntityEventListener.class.getName());

   public void checkId(Object object, EntityPersister persister, Serializable id, SessionImplementor session) throws HibernateException {
      if (id == null || !(id instanceof DelayedPostInsertIdentifier)) {
         if (persister.canExtractIdOutOfEntity()) {
            Serializable oid = persister.getIdentifier(object, session);
            if (id == null) {
               throw new AssertionFailure("null id in " + persister.getEntityName() + " entry (don't flush the Session after an exception occurs)");
            }

            if (!persister.getIdentifierType().isEqual(id, oid, session.getFactory())) {
               throw new HibernateException("identifier of an instance of " + persister.getEntityName() + " was altered from " + id + " to " + oid);
            }
         }

      }
   }

   private void checkNaturalId(EntityPersister persister, EntityEntry entry, Object[] current, Object[] loaded, SessionImplementor session) {
      if (persister.hasNaturalIdentifier() && entry.getStatus() != Status.READ_ONLY) {
         if (!persister.getEntityMetamodel().hasImmutableNaturalId()) {
            return;
         }

         int[] naturalIdentifierPropertiesIndexes = persister.getNaturalIdentifierProperties();
         Type[] propertyTypes = persister.getPropertyTypes();
         boolean[] propertyUpdateability = persister.getPropertyUpdateability();
         Object[] snapshot = loaded == null ? session.getPersistenceContext().getNaturalIdSnapshot(entry.getId(), persister) : session.getPersistenceContext().getNaturalIdHelper().extractNaturalIdValues(loaded, persister);

         for(int i = 0; i < naturalIdentifierPropertiesIndexes.length; ++i) {
            int naturalIdentifierPropertyIndex = naturalIdentifierPropertiesIndexes[i];
            if (!propertyUpdateability[naturalIdentifierPropertyIndex]) {
               Type propertyType = propertyTypes[naturalIdentifierPropertyIndex];
               if (!propertyType.isEqual(current[naturalIdentifierPropertyIndex], snapshot[i])) {
                  throw new HibernateException(String.format("An immutable natural identifier of entity %s was altered from %s to %s", persister.getEntityName(), propertyTypes[naturalIdentifierPropertyIndex].toLoggableString(snapshot[i], session.getFactory()), propertyTypes[naturalIdentifierPropertyIndex].toLoggableString(current[naturalIdentifierPropertyIndex], session.getFactory())));
               }
            }
         }
      }

   }

   public void onFlushEntity(FlushEntityEvent event) throws HibernateException {
      Object entity = event.getEntity();
      EntityEntry entry = event.getEntityEntry();
      EventSource session = event.getSession();
      EntityPersister persister = entry.getPersister();
      Status status = entry.getStatus();
      Type[] types = persister.getPropertyTypes();
      boolean mightBeDirty = entry.requiresDirtyCheck(entity);
      Object[] values = this.getValues(entity, entry, mightBeDirty, session);
      event.setPropertyValues(values);
      boolean substitute = this.wrapCollections(session, persister, types, values);
      if (this.isUpdateNecessary(event, mightBeDirty)) {
         substitute = this.scheduleUpdate(event) || substitute;
      }

      if (status != Status.DELETED) {
         if (substitute) {
            persister.setPropertyValues(entity, values);
         }

         if (persister.hasCollections()) {
            (new FlushVisitor(session, entity)).processEntityPropertyValues(values, types);
         }
      }

   }

   private Object[] getValues(Object entity, EntityEntry entry, boolean mightBeDirty, SessionImplementor session) {
      Object[] loadedState = entry.getLoadedState();
      Status status = entry.getStatus();
      EntityPersister persister = entry.getPersister();
      Object[] values;
      if (status == Status.DELETED) {
         values = entry.getDeletedState();
      } else if (!mightBeDirty && loadedState != null) {
         values = loadedState;
      } else {
         this.checkId(entity, persister, entry.getId(), session);
         values = persister.getPropertyValues(entity);
         this.checkNaturalId(persister, entry, values, loadedState, session);
      }

      return values;
   }

   private boolean wrapCollections(EventSource session, EntityPersister persister, Type[] types, Object[] values) {
      if (persister.hasCollections()) {
         WrapVisitor visitor = new WrapVisitor(session);
         visitor.processEntityPropertyValues(values, types);
         return visitor.isSubstitutionRequired();
      } else {
         return false;
      }
   }

   private boolean isUpdateNecessary(FlushEntityEvent event, boolean mightBeDirty) {
      Status status = event.getEntityEntry().getStatus();
      if (!mightBeDirty && status != Status.DELETED) {
         return this.hasDirtyCollections(event, event.getEntityEntry().getPersister(), status);
      } else {
         this.dirtyCheck(event);
         if (this.isUpdateNecessary(event)) {
            return true;
         } else {
            if (event.getEntityEntry().getPersister().getInstrumentationMetadata().isInstrumented()) {
               event.getEntityEntry().getPersister().getInstrumentationMetadata().extractInterceptor(event.getEntity()).clearDirty();
            }

            event.getSession().getFactory().getCustomEntityDirtinessStrategy().resetDirty(event.getEntity(), event.getEntityEntry().getPersister(), event.getSession());
            return false;
         }
      }
   }

   private boolean scheduleUpdate(FlushEntityEvent event) {
      EntityEntry entry = event.getEntityEntry();
      EventSource session = event.getSession();
      Object entity = event.getEntity();
      Status status = entry.getStatus();
      EntityPersister persister = entry.getPersister();
      Object[] values = event.getPropertyValues();
      if (LOG.isTraceEnabled()) {
         if (status == Status.DELETED) {
            if (!persister.isMutable()) {
               LOG.tracev("Updating immutable, deleted entity: {0}", MessageHelper.infoString((EntityPersister)persister, (Object)entry.getId(), (SessionFactoryImplementor)session.getFactory()));
            } else if (!entry.isModifiableEntity()) {
               LOG.tracev("Updating non-modifiable, deleted entity: {0}", MessageHelper.infoString((EntityPersister)persister, (Object)entry.getId(), (SessionFactoryImplementor)session.getFactory()));
            } else {
               LOG.tracev("Updating deleted entity: ", MessageHelper.infoString((EntityPersister)persister, (Object)entry.getId(), (SessionFactoryImplementor)session.getFactory()));
            }
         } else {
            LOG.tracev("Updating entity: {0}", MessageHelper.infoString((EntityPersister)persister, (Object)entry.getId(), (SessionFactoryImplementor)session.getFactory()));
         }
      }

      boolean intercepted = !entry.isBeingReplicated() && this.handleInterception(event);
      Object nextVersion = this.getNextVersion(event);
      int[] dirtyProperties = event.getDirtyProperties();
      if (event.isDirtyCheckPossible() && dirtyProperties == null) {
         if (!intercepted && !event.hasDirtyCollection()) {
            throw new AssertionFailure("dirty, but no dirty properties");
         }

         dirtyProperties = ArrayHelper.EMPTY_INT_ARRAY;
      }

      (new Nullability(session)).checkNullability(values, persister, true);
      session.getActionQueue().addAction(new EntityUpdateAction(entry.getId(), values, dirtyProperties, event.hasDirtyCollection(), status == Status.DELETED && !entry.isModifiableEntity() ? persister.getPropertyValues(entity) : entry.getLoadedState(), entry.getVersion(), nextVersion, entity, entry.getRowId(), persister, session));
      return intercepted;
   }

   protected boolean handleInterception(FlushEntityEvent event) {
      SessionImplementor session = event.getSession();
      EntityEntry entry = event.getEntityEntry();
      EntityPersister persister = entry.getPersister();
      Object entity = event.getEntity();
      Object[] values = event.getPropertyValues();
      boolean intercepted = this.invokeInterceptor(session, entity, entry, values, persister);
      if (intercepted && event.isDirtyCheckPossible() && !event.isDirtyCheckHandledByInterceptor()) {
         int[] dirtyProperties;
         if (event.hasDatabaseSnapshot()) {
            dirtyProperties = persister.findModified(event.getDatabaseSnapshot(), values, entity, session);
         } else {
            dirtyProperties = persister.findDirty(values, entry.getLoadedState(), entity, session);
         }

         event.setDirtyProperties(dirtyProperties);
      }

      return intercepted;
   }

   protected boolean invokeInterceptor(SessionImplementor session, Object entity, EntityEntry entry, Object[] values, EntityPersister persister) {
      return session.getInterceptor().onFlushDirty(entity, entry.getId(), values, entry.getLoadedState(), persister.getPropertyNames(), persister.getPropertyTypes());
   }

   private Object getNextVersion(FlushEntityEvent event) throws HibernateException {
      EntityEntry entry = event.getEntityEntry();
      EntityPersister persister = entry.getPersister();
      if (persister.isVersioned()) {
         Object[] values = event.getPropertyValues();
         if (entry.isBeingReplicated()) {
            return Versioning.getVersion(values, persister);
         } else {
            int[] dirtyProperties = event.getDirtyProperties();
            boolean isVersionIncrementRequired = this.isVersionIncrementRequired(event, entry, persister, dirtyProperties);
            Object nextVersion = isVersionIncrementRequired ? Versioning.increment(entry.getVersion(), persister.getVersionType(), event.getSession()) : entry.getVersion();
            Versioning.setVersion(values, nextVersion, persister);
            return nextVersion;
         }
      } else {
         return null;
      }
   }

   private boolean isVersionIncrementRequired(FlushEntityEvent event, EntityEntry entry, EntityPersister persister, int[] dirtyProperties) {
      boolean isVersionIncrementRequired = entry.getStatus() != Status.DELETED && (dirtyProperties == null || Versioning.isVersionIncrementRequired(dirtyProperties, event.hasDirtyCollection(), persister.getPropertyVersionability()));
      return isVersionIncrementRequired;
   }

   protected final boolean isUpdateNecessary(FlushEntityEvent event) throws HibernateException {
      EntityPersister persister = event.getEntityEntry().getPersister();
      Status status = event.getEntityEntry().getStatus();
      if (!event.isDirtyCheckPossible()) {
         return true;
      } else {
         int[] dirtyProperties = event.getDirtyProperties();
         return dirtyProperties != null && dirtyProperties.length != 0 ? true : this.hasDirtyCollections(event, persister, status);
      }
   }

   private boolean hasDirtyCollections(FlushEntityEvent event, EntityPersister persister, Status status) {
      if (this.isCollectionDirtyCheckNecessary(persister, status)) {
         DirtyCollectionSearchVisitor visitor = new DirtyCollectionSearchVisitor(event.getSession(), persister.getPropertyVersionability());
         visitor.processEntityPropertyValues(event.getPropertyValues(), persister.getPropertyTypes());
         boolean hasDirtyCollections = visitor.wasDirtyCollectionFound();
         event.setHasDirtyCollection(hasDirtyCollections);
         return hasDirtyCollections;
      } else {
         return false;
      }
   }

   private boolean isCollectionDirtyCheckNecessary(EntityPersister persister, Status status) {
      return (status == Status.MANAGED || status == Status.READ_ONLY) && persister.isVersioned() && persister.hasCollections();
   }

   protected void dirtyCheck(final FlushEntityEvent event) throws HibernateException {
      Object entity = event.getEntity();
      Object[] values = event.getPropertyValues();
      SessionImplementor session = event.getSession();
      EntityEntry entry = event.getEntityEntry();
      EntityPersister persister = entry.getPersister();
      Serializable id = entry.getId();
      Object[] loadedState = entry.getLoadedState();
      int[] dirtyProperties = session.getInterceptor().findDirty(entity, id, values, loadedState, persister.getPropertyNames(), persister.getPropertyTypes());
      if (dirtyProperties == null) {
         class DirtyCheckContextImpl implements CustomEntityDirtinessStrategy.DirtyCheckContext {
            int[] found = null;

            public void doDirtyChecking(CustomEntityDirtinessStrategy.AttributeChecker attributeChecker) {
               this.found = (DefaultFlushEntityEventListener.this.new DirtyCheckAttributeInfoImpl(event)).visitAttributes(attributeChecker);
               if (this.found != null && this.found.length == 0) {
                  this.found = null;
               }

            }
         }

         DirtyCheckContextImpl context = new DirtyCheckContextImpl();
         session.getFactory().getCustomEntityDirtinessStrategy().findDirty(entity, persister, (Session)session, context);
         dirtyProperties = context.found;
      }

      event.setDatabaseSnapshot((Object[])null);
      boolean cannotDirtyCheck;
      boolean interceptorHandledDirtyCheck;
      if (dirtyProperties == null) {
         interceptorHandledDirtyCheck = false;
         cannotDirtyCheck = loadedState == null;
         if (!cannotDirtyCheck) {
            dirtyProperties = persister.findDirty(values, loadedState, entity, session);
         } else if (entry.getStatus() == Status.DELETED && !event.getEntityEntry().isModifiableEntity()) {
            if (values != entry.getDeletedState()) {
               throw new IllegalStateException("Entity has status Status.DELETED but values != entry.getDeletedState");
            }

            Object[] currentState = persister.getPropertyValues(event.getEntity());
            dirtyProperties = persister.findDirty(entry.getDeletedState(), currentState, entity, session);
            cannotDirtyCheck = false;
         } else {
            Object[] databaseSnapshot = this.getDatabaseSnapshot(session, persister, id);
            if (databaseSnapshot != null) {
               dirtyProperties = persister.findModified(databaseSnapshot, values, entity, session);
               cannotDirtyCheck = false;
               event.setDatabaseSnapshot(databaseSnapshot);
            }
         }
      } else {
         cannotDirtyCheck = false;
         interceptorHandledDirtyCheck = true;
      }

      this.logDirtyProperties(id, dirtyProperties, persister);
      event.setDirtyProperties(dirtyProperties);
      event.setDirtyCheckHandledByInterceptor(interceptorHandledDirtyCheck);
      event.setDirtyCheckPossible(!cannotDirtyCheck);
   }

   private void logDirtyProperties(Serializable id, int[] dirtyProperties, EntityPersister persister) {
      if (LOG.isTraceEnabled() && dirtyProperties != null && dirtyProperties.length > 0) {
         String[] allPropertyNames = persister.getPropertyNames();
         String[] dirtyPropertyNames = new String[dirtyProperties.length];

         for(int i = 0; i < dirtyProperties.length; ++i) {
            dirtyPropertyNames[i] = allPropertyNames[dirtyProperties[i]];
         }

         LOG.tracev("Found dirty properties [{0}] : {1}", MessageHelper.infoString(persister.getEntityName(), id), dirtyPropertyNames);
      }

   }

   private Object[] getDatabaseSnapshot(SessionImplementor session, EntityPersister persister, Serializable id) {
      if (persister.isSelectBeforeUpdateRequired()) {
         Object[] snapshot = session.getPersistenceContext().getDatabaseSnapshot(id, persister);
         if (snapshot == null) {
            if (session.getFactory().getStatistics().isStatisticsEnabled()) {
               session.getFactory().getStatisticsImplementor().optimisticFailure(persister.getEntityName());
            }

            throw new StaleObjectStateException(persister.getEntityName(), id);
         } else {
            return snapshot;
         }
      } else {
         EntityKey entityKey = session.generateEntityKey(id, persister);
         return session.getPersistenceContext().getCachedDatabaseSnapshot(entityKey);
      }
   }

   private class DirtyCheckAttributeInfoImpl implements CustomEntityDirtinessStrategy.AttributeInformation {
      private final FlushEntityEvent event;
      private final EntityPersister persister;
      private final int numberOfAttributes;
      private int index;
      Object[] databaseSnapshot;

      private DirtyCheckAttributeInfoImpl(FlushEntityEvent event) {
         this.index = 0;
         this.event = event;
         this.persister = event.getEntityEntry().getPersister();
         this.numberOfAttributes = this.persister.getPropertyNames().length;
      }

      public EntityPersister getContainingPersister() {
         return this.persister;
      }

      public int getAttributeIndex() {
         return this.index;
      }

      public String getName() {
         return this.persister.getPropertyNames()[this.index];
      }

      public Type getType() {
         return this.persister.getPropertyTypes()[this.index];
      }

      public Object getCurrentValue() {
         return this.event.getPropertyValues()[this.index];
      }

      public Object getLoadedValue() {
         if (this.databaseSnapshot == null) {
            this.databaseSnapshot = DefaultFlushEntityEventListener.this.getDatabaseSnapshot(this.event.getSession(), this.persister, this.event.getEntityEntry().getId());
         }

         return this.databaseSnapshot[this.index];
      }

      public int[] visitAttributes(CustomEntityDirtinessStrategy.AttributeChecker attributeChecker) {
         this.databaseSnapshot = null;
         this.index = 0;
         int[] indexes = new int[this.numberOfAttributes];

         int count;
         for(count = 0; this.index < this.numberOfAttributes; ++this.index) {
            if (attributeChecker.isDirty(this)) {
               indexes[count++] = this.index;
            }
         }

         return Arrays.copyOf(indexes, count);
      }
   }
}
