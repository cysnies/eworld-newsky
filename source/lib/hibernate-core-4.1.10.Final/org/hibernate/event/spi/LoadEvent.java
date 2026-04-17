package org.hibernate.event.spi;

import java.io.Serializable;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;

public class LoadEvent extends AbstractEvent {
   public static final LockMode DEFAULT_LOCK_MODE;
   private Serializable entityId;
   private String entityClassName;
   private Object instanceToLoad;
   private LockOptions lockOptions;
   private boolean isAssociationFetch;
   private Object result;

   public LoadEvent(Serializable entityId, Object instanceToLoad, EventSource source) {
      this(entityId, (String)null, instanceToLoad, (LockOptions)(new LockOptions()), false, source);
   }

   public LoadEvent(Serializable entityId, String entityClassName, LockMode lockMode, EventSource source) {
      this(entityId, entityClassName, (Object)null, (LockMode)lockMode, false, source);
   }

   public LoadEvent(Serializable entityId, String entityClassName, LockOptions lockOptions, EventSource source) {
      this(entityId, entityClassName, (Object)null, (LockOptions)lockOptions, false, source);
   }

   public LoadEvent(Serializable entityId, String entityClassName, boolean isAssociationFetch, EventSource source) {
      this(entityId, entityClassName, (Object)null, (LockOptions)(new LockOptions()), isAssociationFetch, source);
   }

   public boolean isAssociationFetch() {
      return this.isAssociationFetch;
   }

   private LoadEvent(Serializable entityId, String entityClassName, Object instanceToLoad, LockMode lockMode, boolean isAssociationFetch, EventSource source) {
      this(entityId, entityClassName, instanceToLoad, (new LockOptions()).setLockMode(lockMode), isAssociationFetch, source);
   }

   private LoadEvent(Serializable entityId, String entityClassName, Object instanceToLoad, LockOptions lockOptions, boolean isAssociationFetch, EventSource source) {
      super(source);
      if (entityId == null) {
         throw new IllegalArgumentException("id to load is required for loading");
      } else if (lockOptions.getLockMode() == LockMode.WRITE) {
         throw new IllegalArgumentException("Invalid lock mode for loading");
      } else {
         if (lockOptions.getLockMode() == null) {
            lockOptions.setLockMode(DEFAULT_LOCK_MODE);
         }

         this.entityId = entityId;
         this.entityClassName = entityClassName;
         this.instanceToLoad = instanceToLoad;
         this.lockOptions = lockOptions;
         this.isAssociationFetch = isAssociationFetch;
      }
   }

   public Serializable getEntityId() {
      return this.entityId;
   }

   public void setEntityId(Serializable entityId) {
      this.entityId = entityId;
   }

   public String getEntityClassName() {
      return this.entityClassName;
   }

   public void setEntityClassName(String entityClassName) {
      this.entityClassName = entityClassName;
   }

   public Object getInstanceToLoad() {
      return this.instanceToLoad;
   }

   public void setInstanceToLoad(Object instanceToLoad) {
      this.instanceToLoad = instanceToLoad;
   }

   public LockOptions getLockOptions() {
      return this.lockOptions;
   }

   public LockMode getLockMode() {
      return this.lockOptions.getLockMode();
   }

   public void setLockMode(LockMode lockMode) {
      this.lockOptions.setLockMode(lockMode);
   }

   public void setLockTimeout(int timeout) {
      this.lockOptions.setTimeOut(timeout);
   }

   public int getLockTimeout() {
      return this.lockOptions.getTimeOut();
   }

   public void setLockScope(boolean cascade) {
      this.lockOptions.setScope(cascade);
   }

   public boolean getLockScope() {
      return this.lockOptions.getScope();
   }

   public Object getResult() {
      return this.result;
   }

   public void setResult(Object result) {
      this.result = result;
   }

   static {
      DEFAULT_LOCK_MODE = LockMode.NONE;
   }
}
