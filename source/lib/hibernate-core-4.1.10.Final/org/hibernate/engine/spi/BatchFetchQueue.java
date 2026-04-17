package org.hibernate.engine.spi;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import org.hibernate.EntityMode;
import org.hibernate.cache.spi.CacheKey;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.jboss.logging.Logger;

public class BatchFetchQueue {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, BatchFetchQueue.class.getName());
   private final PersistenceContext context;
   private final Map subselectsByEntityKey = new HashMap(8);
   private final Map batchLoadableEntityKeys = new HashMap(8);
   private final Map batchLoadableCollections = new HashMap(8);

   public BatchFetchQueue(PersistenceContext context) {
      this.context = context;
   }

   public void clear() {
      this.batchLoadableEntityKeys.clear();
      this.batchLoadableCollections.clear();
      this.subselectsByEntityKey.clear();
   }

   public SubselectFetch getSubselect(EntityKey key) {
      return (SubselectFetch)this.subselectsByEntityKey.get(key);
   }

   public void addSubselect(EntityKey key, SubselectFetch subquery) {
      this.subselectsByEntityKey.put(key, subquery);
   }

   public void removeSubselect(EntityKey key) {
      this.subselectsByEntityKey.remove(key);
   }

   public void addBatchLoadableEntityKey(EntityKey key) {
      if (key.isBatchLoadable()) {
         LinkedHashSet<EntityKey> set = (LinkedHashSet)this.batchLoadableEntityKeys.get(key.getEntityName());
         if (set == null) {
            set = new LinkedHashSet(8);
            this.batchLoadableEntityKeys.put(key.getEntityName(), set);
         }

         set.add(key);
      }

   }

   public void removeBatchLoadableEntityKey(EntityKey key) {
      if (key.isBatchLoadable()) {
         LinkedHashSet<EntityKey> set = (LinkedHashSet)this.batchLoadableEntityKeys.get(key.getEntityName());
         if (set != null) {
            set.remove(key);
         }
      }

   }

   public Serializable[] getEntityBatch(EntityPersister persister, Serializable id, int batchSize, EntityMode entityMode) {
      Serializable[] ids = new Serializable[batchSize];
      ids[0] = id;
      int i = 1;
      int end = -1;
      boolean checkForEnd = false;
      LinkedHashSet<EntityKey> set = (LinkedHashSet)this.batchLoadableEntityKeys.get(persister.getEntityName());
      if (set != null) {
         for(EntityKey key : set) {
            if (checkForEnd && i == end) {
               return ids;
            }

            if (persister.getIdentifierType().isEqual(id, key.getIdentifier())) {
               end = i;
            } else if (!this.isCached(key, persister)) {
               ids[i++] = key.getIdentifier();
            }

            if (i == batchSize) {
               i = 1;
               if (end != -1) {
                  checkForEnd = true;
               }
            }
         }
      }

      return ids;
   }

   private boolean isCached(EntityKey entityKey, EntityPersister persister) {
      if (persister.hasCache()) {
         CacheKey key = this.context.getSession().generateCacheKey(entityKey.getIdentifier(), persister.getIdentifierType(), entityKey.getEntityName());
         return persister.getCacheAccessStrategy().get(key, this.context.getSession().getTimestamp()) != null;
      } else {
         return false;
      }
   }

   public void addBatchLoadableCollection(PersistentCollection collection, CollectionEntry ce) {
      CollectionPersister persister = ce.getLoadedPersister();
      LinkedHashMap<CollectionEntry, PersistentCollection> map = (LinkedHashMap)this.batchLoadableCollections.get(persister.getRole());
      if (map == null) {
         map = new LinkedHashMap(16);
         this.batchLoadableCollections.put(persister.getRole(), map);
      }

      map.put(ce, collection);
   }

   public void removeBatchLoadableCollection(CollectionEntry ce) {
      LinkedHashMap<CollectionEntry, PersistentCollection> map = (LinkedHashMap)this.batchLoadableCollections.get(ce.getLoadedPersister().getRole());
      if (map != null) {
         map.remove(ce);
      }

   }

   public Serializable[] getCollectionBatch(CollectionPersister collectionPersister, Serializable id, int batchSize) {
      Serializable[] keys = new Serializable[batchSize];
      keys[0] = id;
      int i = 1;
      int end = -1;
      boolean checkForEnd = false;
      LinkedHashMap<CollectionEntry, PersistentCollection> map = (LinkedHashMap)this.batchLoadableCollections.get(collectionPersister.getRole());
      if (map != null) {
         for(Map.Entry me : map.entrySet()) {
            CollectionEntry ce = (CollectionEntry)me.getKey();
            PersistentCollection collection = (PersistentCollection)me.getValue();
            if (ce.getLoadedKey() != null) {
               if (collection.wasInitialized()) {
                  LOG.warn("Encountered initialized collection in BatchFetchQueue, this should not happen.");
               } else {
                  if (checkForEnd && i == end) {
                     return keys;
                  }

                  boolean isEqual = collectionPersister.getKeyType().isEqual(id, ce.getLoadedKey(), collectionPersister.getFactory());
                  if (isEqual) {
                     end = i;
                  } else if (!this.isCached(ce.getLoadedKey(), collectionPersister)) {
                     keys[i++] = ce.getLoadedKey();
                  }

                  if (i == batchSize) {
                     i = 1;
                     if (end != -1) {
                        checkForEnd = true;
                     }
                  }
               }
            }
         }
      }

      return keys;
   }

   private boolean isCached(Serializable collectionKey, CollectionPersister persister) {
      if (persister.hasCache()) {
         CacheKey cacheKey = this.context.getSession().generateCacheKey(collectionKey, persister.getKeyType(), persister.getRole());
         return persister.getCacheAccessStrategy().get(cacheKey, this.context.getSession().getTimestamp()) != null;
      } else {
         return false;
      }
   }
}
