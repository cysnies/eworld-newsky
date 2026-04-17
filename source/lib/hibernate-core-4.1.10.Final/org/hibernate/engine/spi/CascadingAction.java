package org.hibernate.engine.spi;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.ReplicationMode;
import org.hibernate.TransientPropertyValueException;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.internal.ForeignKeys;
import org.hibernate.event.spi.EventSource;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.CollectionType;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public abstract class CascadingAction {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, CascadingAction.class.getName());
   public static final CascadingAction DELETE = new CascadingAction() {
      public void cascade(EventSource session, Object child, String entityName, Object anything, boolean isCascadeDeleteEnabled) throws HibernateException {
         CascadingAction.LOG.tracev("Cascading to delete: {0}", entityName);
         session.delete(entityName, child, isCascadeDeleteEnabled, (Set)anything);
      }

      public Iterator getCascadableChildrenIterator(EventSource session, CollectionType collectionType, Object collection) {
         return CascadingAction.getAllElementsIterator(session, collectionType, collection);
      }

      public boolean deleteOrphans() {
         return true;
      }

      public String toString() {
         return "ACTION_DELETE";
      }
   };
   public static final CascadingAction LOCK = new CascadingAction() {
      public void cascade(EventSource session, Object child, String entityName, Object anything, boolean isCascadeDeleteEnabled) throws HibernateException {
         CascadingAction.LOG.tracev("Cascading to lock: {0}", entityName);
         LockMode lockMode = LockMode.NONE;
         LockOptions lr = new LockOptions();
         if (anything instanceof LockOptions) {
            LockOptions lockOptions = (LockOptions)anything;
            lr.setTimeOut(lockOptions.getTimeOut());
            lr.setScope(lockOptions.getScope());
            if (lockOptions.getScope()) {
               lockMode = lockOptions.getLockMode();
            }
         }

         lr.setLockMode(lockMode);
         session.buildLockRequest(lr).lock(entityName, child);
      }

      public Iterator getCascadableChildrenIterator(EventSource session, CollectionType collectionType, Object collection) {
         return getLoadedElementsIterator(session, collectionType, collection);
      }

      public boolean deleteOrphans() {
         return false;
      }

      public String toString() {
         return "ACTION_LOCK";
      }
   };
   public static final CascadingAction REFRESH = new CascadingAction() {
      public void cascade(EventSource session, Object child, String entityName, Object anything, boolean isCascadeDeleteEnabled) throws HibernateException {
         CascadingAction.LOG.tracev("Cascading to refresh: {0}", entityName);
         session.refresh(child, (Map)anything);
      }

      public Iterator getCascadableChildrenIterator(EventSource session, CollectionType collectionType, Object collection) {
         return getLoadedElementsIterator(session, collectionType, collection);
      }

      public boolean deleteOrphans() {
         return false;
      }

      public String toString() {
         return "ACTION_REFRESH";
      }
   };
   public static final CascadingAction EVICT = new CascadingAction() {
      public void cascade(EventSource session, Object child, String entityName, Object anything, boolean isCascadeDeleteEnabled) throws HibernateException {
         CascadingAction.LOG.tracev("Cascading to evict: {0}", entityName);
         session.evict(child);
      }

      public Iterator getCascadableChildrenIterator(EventSource session, CollectionType collectionType, Object collection) {
         return getLoadedElementsIterator(session, collectionType, collection);
      }

      public boolean deleteOrphans() {
         return false;
      }

      public boolean performOnLazyProperty() {
         return false;
      }

      public String toString() {
         return "ACTION_EVICT";
      }
   };
   public static final CascadingAction SAVE_UPDATE = new CascadingAction() {
      public void cascade(EventSource session, Object child, String entityName, Object anything, boolean isCascadeDeleteEnabled) throws HibernateException {
         CascadingAction.LOG.tracev("Cascading to save or update: {0}", entityName);
         session.saveOrUpdate(entityName, child);
      }

      public Iterator getCascadableChildrenIterator(EventSource session, CollectionType collectionType, Object collection) {
         return getLoadedElementsIterator(session, collectionType, collection);
      }

      public boolean deleteOrphans() {
         return true;
      }

      public boolean performOnLazyProperty() {
         return false;
      }

      public String toString() {
         return "ACTION_SAVE_UPDATE";
      }
   };
   public static final CascadingAction MERGE = new CascadingAction() {
      public void cascade(EventSource session, Object child, String entityName, Object anything, boolean isCascadeDeleteEnabled) throws HibernateException {
         CascadingAction.LOG.tracev("Cascading to merge: {0}", entityName);
         session.merge(entityName, child, (Map)anything);
      }

      public Iterator getCascadableChildrenIterator(EventSource session, CollectionType collectionType, Object collection) {
         return getLoadedElementsIterator(session, collectionType, collection);
      }

      public boolean deleteOrphans() {
         return false;
      }

      public String toString() {
         return "ACTION_MERGE";
      }
   };
   public static final CascadingAction PERSIST = new CascadingAction() {
      public void cascade(EventSource session, Object child, String entityName, Object anything, boolean isCascadeDeleteEnabled) throws HibernateException {
         CascadingAction.LOG.tracev("Cascading to persist: {0}" + entityName, new Object[0]);
         session.persist(entityName, child, (Map)anything);
      }

      public Iterator getCascadableChildrenIterator(EventSource session, CollectionType collectionType, Object collection) {
         return CascadingAction.getAllElementsIterator(session, collectionType, collection);
      }

      public boolean deleteOrphans() {
         return false;
      }

      public boolean performOnLazyProperty() {
         return false;
      }

      public String toString() {
         return "ACTION_PERSIST";
      }
   };
   public static final CascadingAction PERSIST_ON_FLUSH = new CascadingAction() {
      public void cascade(EventSource session, Object child, String entityName, Object anything, boolean isCascadeDeleteEnabled) throws HibernateException {
         CascadingAction.LOG.tracev("Cascading to persist on flush: {0}", entityName);
         session.persistOnFlush(entityName, child, (Map)anything);
      }

      public Iterator getCascadableChildrenIterator(EventSource session, CollectionType collectionType, Object collection) {
         return CascadingAction.getLoadedElementsIterator(session, collectionType, collection);
      }

      public boolean deleteOrphans() {
         return true;
      }

      public boolean requiresNoCascadeChecking() {
         return true;
      }

      public void noCascade(EventSource session, Object child, Object parent, EntityPersister persister, int propertyIndex) {
         if (child != null) {
            Type type = persister.getPropertyTypes()[propertyIndex];
            if (type.isEntityType()) {
               String childEntityName = ((EntityType)type).getAssociatedEntityName(session.getFactory());
               if (!this.isInManagedState(child, session) && !(child instanceof HibernateProxy) && ForeignKeys.isTransient(childEntityName, child, (Boolean)null, session)) {
                  String parentEntiytName = persister.getEntityName();
                  String propertyName = persister.getPropertyNames()[propertyIndex];
                  throw new TransientPropertyValueException("object references an unsaved transient instance - save the transient instance before flushing", childEntityName, parentEntiytName, propertyName);
               }
            }

         }
      }

      public boolean performOnLazyProperty() {
         return false;
      }

      private boolean isInManagedState(Object child, EventSource session) {
         EntityEntry entry = session.getPersistenceContext().getEntry(child);
         return entry != null && (entry.getStatus() == Status.MANAGED || entry.getStatus() == Status.READ_ONLY || entry.getStatus() == Status.SAVING);
      }

      public String toString() {
         return "ACTION_PERSIST_ON_FLUSH";
      }
   };
   public static final CascadingAction REPLICATE = new CascadingAction() {
      public void cascade(EventSource session, Object child, String entityName, Object anything, boolean isCascadeDeleteEnabled) throws HibernateException {
         CascadingAction.LOG.tracev("Cascading to replicate: {0}", entityName);
         session.replicate(entityName, child, (ReplicationMode)anything);
      }

      public Iterator getCascadableChildrenIterator(EventSource session, CollectionType collectionType, Object collection) {
         return getLoadedElementsIterator(session, collectionType, collection);
      }

      public boolean deleteOrphans() {
         return false;
      }

      public String toString() {
         return "ACTION_REPLICATE";
      }
   };

   public abstract void cascade(EventSource var1, Object var2, String var3, Object var4, boolean var5) throws HibernateException;

   public abstract Iterator getCascadableChildrenIterator(EventSource var1, CollectionType var2, Object var3);

   public abstract boolean deleteOrphans();

   public boolean requiresNoCascadeChecking() {
      return false;
   }

   public void noCascade(EventSource session, Object child, Object parent, EntityPersister persister, int propertyIndex) {
   }

   public boolean performOnLazyProperty() {
      return true;
   }

   private static Iterator getAllElementsIterator(EventSource session, CollectionType collectionType, Object collection) {
      return collectionType.getElementsIterator(collection, session);
   }

   public static Iterator getLoadedElementsIterator(SessionImplementor session, CollectionType collectionType, Object collection) {
      return collectionIsInitialized(collection) ? collectionType.getElementsIterator(collection, session) : ((PersistentCollection)collection).queuedAdditionIterator();
   }

   private static boolean collectionIsInitialized(Object collection) {
      return !(collection instanceof PersistentCollection) || ((PersistentCollection)collection).wasInitialized();
   }
}
