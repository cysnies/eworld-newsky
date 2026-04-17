package org.hibernate.engine.spi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.hibernate.CustomEntityDirtinessStrategy;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.bytecode.instrumentation.spi.FieldInterceptor;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.UniqueKeyLoadable;
import org.hibernate.pretty.MessageHelper;

public final class EntityEntry implements Serializable {
   private LockMode lockMode;
   private Status status;
   private Status previousStatus;
   private final Serializable id;
   private Object[] loadedState;
   private Object[] deletedState;
   private boolean existsInDatabase;
   private Object version;
   private transient EntityPersister persister;
   private final EntityMode entityMode;
   private final String tenantId;
   private final String entityName;
   private transient EntityKey cachedEntityKey;
   private boolean isBeingReplicated;
   private boolean loadedWithLazyPropertiesUnfetched;
   private final transient Object rowId;
   private final transient PersistenceContext persistenceContext;

   public EntityEntry(Status status, Object[] loadedState, Object rowId, Serializable id, Object version, LockMode lockMode, boolean existsInDatabase, EntityPersister persister, EntityMode entityMode, String tenantId, boolean disableVersionIncrement, boolean lazyPropertiesAreUnfetched, PersistenceContext persistenceContext) {
      this.status = status;
      this.previousStatus = null;
      if (status != Status.READ_ONLY) {
         this.loadedState = loadedState;
      }

      this.id = id;
      this.rowId = rowId;
      this.existsInDatabase = existsInDatabase;
      this.version = version;
      this.lockMode = lockMode;
      this.isBeingReplicated = disableVersionIncrement;
      this.loadedWithLazyPropertiesUnfetched = lazyPropertiesAreUnfetched;
      this.persister = persister;
      this.entityMode = entityMode;
      this.tenantId = tenantId;
      this.entityName = persister == null ? null : persister.getEntityName();
      this.persistenceContext = persistenceContext;
   }

   private EntityEntry(SessionFactoryImplementor factory, String entityName, Serializable id, EntityMode entityMode, String tenantId, Status status, Status previousStatus, Object[] loadedState, Object[] deletedState, Object version, LockMode lockMode, boolean existsInDatabase, boolean isBeingReplicated, boolean loadedWithLazyPropertiesUnfetched, PersistenceContext persistenceContext) {
      this.entityName = entityName;
      this.persister = factory == null ? null : factory.getEntityPersister(entityName);
      this.id = id;
      this.entityMode = entityMode;
      this.tenantId = tenantId;
      this.status = status;
      this.previousStatus = previousStatus;
      this.loadedState = loadedState;
      this.deletedState = deletedState;
      this.version = version;
      this.lockMode = lockMode;
      this.existsInDatabase = existsInDatabase;
      this.isBeingReplicated = isBeingReplicated;
      this.loadedWithLazyPropertiesUnfetched = loadedWithLazyPropertiesUnfetched;
      this.rowId = null;
      this.persistenceContext = persistenceContext;
   }

   public LockMode getLockMode() {
      return this.lockMode;
   }

   public void setLockMode(LockMode lockMode) {
      this.lockMode = lockMode;
   }

   public Status getStatus() {
      return this.status;
   }

   public void setStatus(Status status) {
      if (status == Status.READ_ONLY) {
         this.loadedState = null;
      }

      if (this.status != status) {
         this.previousStatus = this.status;
         this.status = status;
      }

   }

   public Serializable getId() {
      return this.id;
   }

   public Object[] getLoadedState() {
      return this.loadedState;
   }

   public Object[] getDeletedState() {
      return this.deletedState;
   }

   public void setDeletedState(Object[] deletedState) {
      this.deletedState = deletedState;
   }

   public boolean isExistsInDatabase() {
      return this.existsInDatabase;
   }

   public Object getVersion() {
      return this.version;
   }

   public EntityPersister getPersister() {
      return this.persister;
   }

   public EntityKey getEntityKey() {
      if (this.cachedEntityKey == null) {
         if (this.getId() == null) {
            throw new IllegalStateException("cannot generate an EntityKey when id is null.");
         }

         this.cachedEntityKey = new EntityKey(this.getId(), this.getPersister(), this.tenantId);
      }

      return this.cachedEntityKey;
   }

   public String getEntityName() {
      return this.entityName;
   }

   public boolean isBeingReplicated() {
      return this.isBeingReplicated;
   }

   public Object getRowId() {
      return this.rowId;
   }

   public void postUpdate(Object entity, Object[] updatedState, Object nextVersion) {
      this.loadedState = updatedState;
      this.setLockMode(LockMode.WRITE);
      if (this.getPersister().isVersioned()) {
         this.version = nextVersion;
         this.getPersister().setPropertyValue(entity, this.getPersister().getVersionProperty(), nextVersion);
      }

      if (this.getPersister().getInstrumentationMetadata().isInstrumented()) {
         FieldInterceptor interceptor = this.getPersister().getInstrumentationMetadata().extractInterceptor(entity);
         if (interceptor != null) {
            interceptor.clearDirty();
         }
      }

      this.persistenceContext.getSession().getFactory().getCustomEntityDirtinessStrategy().resetDirty(entity, this.getPersister(), (Session)this.persistenceContext.getSession());
   }

   public void postDelete() {
      this.previousStatus = this.status;
      this.status = Status.GONE;
      this.existsInDatabase = false;
   }

   public void postInsert(Object[] insertedState) {
      this.existsInDatabase = true;
   }

   public boolean isNullifiable(boolean earlyInsert, SessionImplementor session) {
      boolean var10000;
      if (this.getStatus() != Status.SAVING) {
         label29: {
            if (earlyInsert) {
               if (!this.isExistsInDatabase()) {
                  break label29;
               }
            } else if (session.getPersistenceContext().getNullifiableEntityKeys().contains(this.getEntityKey())) {
               break label29;
            }

            var10000 = false;
            return var10000;
         }
      }

      var10000 = true;
      return var10000;
   }

   public Object getLoadedValue(String propertyName) {
      if (this.loadedState == null) {
         return null;
      } else {
         int propertyIndex = ((UniqueKeyLoadable)this.persister).getPropertyIndex(propertyName);
         return this.loadedState[propertyIndex];
      }
   }

   public boolean requiresDirtyCheck(Object entity) {
      return this.isModifiableEntity() && !this.isUnequivocallyNonDirty(entity);
   }

   private boolean isUnequivocallyNonDirty(Object entity) {
      CustomEntityDirtinessStrategy customEntityDirtinessStrategy = this.persistenceContext.getSession().getFactory().getCustomEntityDirtinessStrategy();
      if (customEntityDirtinessStrategy.canDirtyCheck(entity, this.getPersister(), (Session)this.persistenceContext.getSession())) {
         return !customEntityDirtinessStrategy.isDirty(entity, this.getPersister(), (Session)this.persistenceContext.getSession());
      } else if (this.getPersister().hasMutableProperties()) {
         return false;
      } else if (this.getPersister().getInstrumentationMetadata().isInstrumented()) {
         return !this.getPersister().getInstrumentationMetadata().extractInterceptor(entity).isDirty();
      } else {
         return false;
      }
   }

   public boolean isModifiableEntity() {
      return this.getPersister().isMutable() && this.status != Status.READ_ONLY && (this.status != Status.DELETED || this.previousStatus != Status.READ_ONLY);
   }

   public void forceLocked(Object entity, Object nextVersion) {
      this.version = nextVersion;
      this.loadedState[this.persister.getVersionProperty()] = this.version;
      this.setLockMode(LockMode.FORCE);
      this.persister.setPropertyValue(entity, this.getPersister().getVersionProperty(), nextVersion);
   }

   public boolean isReadOnly() {
      if (this.status != Status.MANAGED && this.status != Status.READ_ONLY) {
         throw new HibernateException("instance was not in a valid state");
      } else {
         return this.status == Status.READ_ONLY;
      }
   }

   public void setReadOnly(boolean readOnly, Object entity) {
      if (readOnly != this.isReadOnly()) {
         if (readOnly) {
            this.setStatus(Status.READ_ONLY);
            this.loadedState = null;
         } else {
            if (!this.persister.isMutable()) {
               throw new IllegalStateException("Cannot make an immutable entity modifiable.");
            }

            this.setStatus(Status.MANAGED);
            this.loadedState = this.getPersister().getPropertyValues(entity);
            this.persistenceContext.getNaturalIdHelper().manageLocalNaturalIdCrossReference(this.persister, this.id, this.loadedState, (Object[])null, CachedNaturalIdValueSource.LOAD);
         }

      }
   }

   public String toString() {
      return "EntityEntry" + MessageHelper.infoString(this.entityName, this.id) + '(' + this.status + ')';
   }

   public boolean isLoadedWithLazyPropertiesUnfetched() {
      return this.loadedWithLazyPropertiesUnfetched;
   }

   public void serialize(ObjectOutputStream oos) throws IOException {
      oos.writeObject(this.entityName);
      oos.writeObject(this.id);
      oos.writeObject(this.entityMode.toString());
      oos.writeObject(this.tenantId);
      oos.writeObject(this.status.name());
      oos.writeObject(this.previousStatus == null ? "" : this.previousStatus.name());
      oos.writeObject(this.loadedState);
      oos.writeObject(this.deletedState);
      oos.writeObject(this.version);
      oos.writeObject(this.lockMode.toString());
      oos.writeBoolean(this.existsInDatabase);
      oos.writeBoolean(this.isBeingReplicated);
      oos.writeBoolean(this.loadedWithLazyPropertiesUnfetched);
   }

   public static EntityEntry deserialize(ObjectInputStream ois, PersistenceContext persistenceContext) throws IOException, ClassNotFoundException {
      String previousStatusString;
      return new EntityEntry(persistenceContext.getSession() == null ? null : persistenceContext.getSession().getFactory(), (String)ois.readObject(), (Serializable)ois.readObject(), EntityMode.parse((String)ois.readObject()), (String)ois.readObject(), Status.valueOf((String)ois.readObject()), (previousStatusString = (String)ois.readObject()).length() == 0 ? null : Status.valueOf(previousStatusString), ois.readObject(), ois.readObject(), ois.readObject(), LockMode.valueOf((String)ois.readObject()), ois.readBoolean(), ois.readBoolean(), ois.readBoolean(), persistenceContext);
   }
}
