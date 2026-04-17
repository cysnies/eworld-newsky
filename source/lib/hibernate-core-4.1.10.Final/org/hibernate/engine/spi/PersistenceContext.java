package org.hibernate.engine.spi;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.MappingException;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.loading.internal.LoadContexts;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;

public interface PersistenceContext {
   boolean isStateless();

   SessionImplementor getSession();

   LoadContexts getLoadContexts();

   void addUnownedCollection(CollectionKey var1, PersistentCollection var2);

   PersistentCollection useUnownedCollection(CollectionKey var1);

   BatchFetchQueue getBatchFetchQueue();

   void clear();

   boolean hasNonReadOnlyEntities();

   void setEntryStatus(EntityEntry var1, Status var2);

   void afterTransactionCompletion();

   Object[] getDatabaseSnapshot(Serializable var1, EntityPersister var2);

   Object[] getCachedDatabaseSnapshot(EntityKey var1);

   Object[] getNaturalIdSnapshot(Serializable var1, EntityPersister var2);

   void addEntity(EntityKey var1, Object var2);

   Object getEntity(EntityKey var1);

   boolean containsEntity(EntityKey var1);

   Object removeEntity(EntityKey var1);

   void addEntity(EntityUniqueKey var1, Object var2);

   Object getEntity(EntityUniqueKey var1);

   EntityEntry getEntry(Object var1);

   EntityEntry removeEntry(Object var1);

   boolean isEntryFor(Object var1);

   CollectionEntry getCollectionEntry(PersistentCollection var1);

   EntityEntry addEntity(Object var1, Status var2, Object[] var3, EntityKey var4, Object var5, LockMode var6, boolean var7, EntityPersister var8, boolean var9, boolean var10);

   EntityEntry addEntry(Object var1, Status var2, Object[] var3, Object var4, Serializable var5, Object var6, LockMode var7, boolean var8, EntityPersister var9, boolean var10, boolean var11);

   boolean containsCollection(PersistentCollection var1);

   boolean containsProxy(Object var1);

   boolean reassociateIfUninitializedProxy(Object var1) throws MappingException;

   void reassociateProxy(Object var1, Serializable var2) throws MappingException;

   Object unproxy(Object var1) throws HibernateException;

   Object unproxyAndReassociate(Object var1) throws HibernateException;

   void checkUniqueness(EntityKey var1, Object var2) throws HibernateException;

   Object narrowProxy(Object var1, EntityPersister var2, EntityKey var3, Object var4) throws HibernateException;

   Object proxyFor(EntityPersister var1, EntityKey var2, Object var3) throws HibernateException;

   Object proxyFor(Object var1) throws HibernateException;

   Object getCollectionOwner(Serializable var1, CollectionPersister var2) throws MappingException;

   Object getLoadedCollectionOwnerOrNull(PersistentCollection var1);

   Serializable getLoadedCollectionOwnerIdOrNull(PersistentCollection var1);

   void addUninitializedCollection(CollectionPersister var1, PersistentCollection var2, Serializable var3);

   void addUninitializedDetachedCollection(CollectionPersister var1, PersistentCollection var2);

   void addNewCollection(CollectionPersister var1, PersistentCollection var2) throws HibernateException;

   void addInitializedDetachedCollection(CollectionPersister var1, PersistentCollection var2) throws HibernateException;

   CollectionEntry addInitializedCollection(CollectionPersister var1, PersistentCollection var2, Serializable var3) throws HibernateException;

   PersistentCollection getCollection(CollectionKey var1);

   void addNonLazyCollection(PersistentCollection var1);

   void initializeNonLazyCollections() throws HibernateException;

   PersistentCollection getCollectionHolder(Object var1);

   void addCollectionHolder(PersistentCollection var1);

   PersistentCollection removeCollectionHolder(Object var1);

   Serializable getSnapshot(PersistentCollection var1);

   CollectionEntry getCollectionEntryOrNull(Object var1);

   Object getProxy(EntityKey var1);

   void addProxy(EntityKey var1, Object var2);

   Object removeProxy(EntityKey var1);

   HashSet getNullifiableEntityKeys();

   Map getEntitiesByKey();

   Map getEntityEntries();

   Map getCollectionEntries();

   Map getCollectionsByKey();

   int getCascadeLevel();

   int incrementCascadeLevel();

   int decrementCascadeLevel();

   boolean isFlushing();

   void setFlushing(boolean var1);

   void beforeLoad();

   void afterLoad();

   boolean isLoadFinished();

   String toString();

   Serializable getOwnerId(String var1, String var2, Object var3, Map var4);

   Object getIndexInOwner(String var1, String var2, Object var3, Map var4);

   void addNullProperty(EntityKey var1, String var2);

   boolean isPropertyNull(EntityKey var1, String var2);

   boolean isDefaultReadOnly();

   void setDefaultReadOnly(boolean var1);

   boolean isReadOnly(Object var1);

   void setReadOnly(Object var1, boolean var2);

   void replaceDelayedEntityIdentityInsertKeys(EntityKey var1, Serializable var2);

   void addChildParent(Object var1, Object var2);

   void removeChildParent(Object var1);

   void registerInsertedKey(EntityPersister var1, Serializable var2);

   boolean wasInsertedDuringTransaction(EntityPersister var1, Serializable var2);

   NaturalIdHelper getNaturalIdHelper();

   public interface NaturalIdHelper {
      Serializable INVALID_NATURAL_ID_REFERENCE = new Serializable() {
      };

      Object[] extractNaturalIdValues(Object[] var1, EntityPersister var2);

      Object[] extractNaturalIdValues(Object var1, EntityPersister var2);

      void cacheNaturalIdCrossReferenceFromLoad(EntityPersister var1, Serializable var2, Object[] var3);

      void manageLocalNaturalIdCrossReference(EntityPersister var1, Serializable var2, Object[] var3, Object[] var4, CachedNaturalIdValueSource var5);

      Object[] removeLocalNaturalIdCrossReference(EntityPersister var1, Serializable var2, Object[] var3);

      void manageSharedNaturalIdCrossReference(EntityPersister var1, Serializable var2, Object[] var3, Object[] var4, CachedNaturalIdValueSource var5);

      void removeSharedNaturalIdCrossReference(EntityPersister var1, Serializable var2, Object[] var3);

      Object[] findCachedNaturalId(EntityPersister var1, Serializable var2);

      Serializable findCachedNaturalIdResolution(EntityPersister var1, Object[] var2);

      Collection getCachedPkResolutions(EntityPersister var1);

      void handleSynchronization(EntityPersister var1, Serializable var2, Object var3);

      void cleanupFromSynchronizations();

      void handleEviction(Object var1, EntityPersister var2, Serializable var3);
   }
}
