package org.hibernate.action.internal;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import org.hibernate.HibernateException;
import org.hibernate.action.spi.AfterTransactionCompletionProcess;
import org.hibernate.action.spi.BeforeTransactionCompletionProcess;
import org.hibernate.action.spi.Executable;
import org.hibernate.cache.spi.access.CollectionRegionAccessStrategy;
import org.hibernate.cache.spi.access.EntityRegionAccessStrategy;
import org.hibernate.cache.spi.access.NaturalIdRegionAccessStrategy;
import org.hibernate.cache.spi.access.SoftLock;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.Queryable;

public class BulkOperationCleanupAction implements Executable, Serializable {
   private final Serializable[] affectedTableSpaces;
   private final Set entityCleanups = new HashSet();
   private final Set collectionCleanups = new HashSet();
   private final Set naturalIdCleanups = new HashSet();

   public BulkOperationCleanupAction(SessionImplementor session, Queryable... affectedQueryables) {
      SessionFactoryImplementor factory = session.getFactory();
      LinkedHashSet<String> spacesList = new LinkedHashSet();

      for(Queryable persister : affectedQueryables) {
         spacesList.addAll(Arrays.asList((String[])persister.getQuerySpaces()));
         if (persister.hasCache()) {
            this.entityCleanups.add(new EntityCleanup(persister.getCacheAccessStrategy()));
         }

         if (persister.hasNaturalIdentifier() && persister.hasNaturalIdCache()) {
            this.naturalIdCleanups.add(new NaturalIdCleanup(persister.getNaturalIdCacheAccessStrategy()));
         }

         Set<String> roles = factory.getCollectionRolesByEntityParticipant(persister.getEntityName());
         if (roles != null) {
            for(String role : roles) {
               CollectionPersister collectionPersister = factory.getCollectionPersister(role);
               if (collectionPersister.hasCache()) {
                  this.collectionCleanups.add(new CollectionCleanup(collectionPersister.getCacheAccessStrategy()));
               }
            }
         }
      }

      this.affectedTableSpaces = (Serializable[])spacesList.toArray(new String[spacesList.size()]);
   }

   public BulkOperationCleanupAction(SessionImplementor session, Set tableSpaces) {
      LinkedHashSet<String> spacesList = new LinkedHashSet();
      spacesList.addAll(tableSpaces);
      SessionFactoryImplementor factory = session.getFactory();

      for(String entityName : factory.getAllClassMetadata().keySet()) {
         EntityPersister persister = factory.getEntityPersister(entityName);
         String[] entitySpaces = (String[])persister.getQuerySpaces();
         if (this.affectedEntity(tableSpaces, entitySpaces)) {
            spacesList.addAll(Arrays.asList(entitySpaces));
            if (persister.hasCache()) {
               this.entityCleanups.add(new EntityCleanup(persister.getCacheAccessStrategy()));
            }

            if (persister.hasNaturalIdentifier() && persister.hasNaturalIdCache()) {
               this.naturalIdCleanups.add(new NaturalIdCleanup(persister.getNaturalIdCacheAccessStrategy()));
            }

            Set<String> roles = session.getFactory().getCollectionRolesByEntityParticipant(persister.getEntityName());
            if (roles != null) {
               for(String role : roles) {
                  CollectionPersister collectionPersister = factory.getCollectionPersister(role);
                  if (collectionPersister.hasCache()) {
                     this.collectionCleanups.add(new CollectionCleanup(collectionPersister.getCacheAccessStrategy()));
                  }
               }
            }
         }
      }

      this.affectedTableSpaces = (Serializable[])spacesList.toArray(new String[spacesList.size()]);
   }

   private boolean affectedEntity(Set affectedTableSpaces, Serializable[] checkTableSpaces) {
      if (affectedTableSpaces != null && !affectedTableSpaces.isEmpty()) {
         for(Serializable checkTableSpace : checkTableSpaces) {
            if (affectedTableSpaces.contains(checkTableSpace)) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   public Serializable[] getPropertySpaces() {
      return this.affectedTableSpaces;
   }

   public BeforeTransactionCompletionProcess getBeforeTransactionCompletionProcess() {
      return null;
   }

   public AfterTransactionCompletionProcess getAfterTransactionCompletionProcess() {
      return new AfterTransactionCompletionProcess() {
         public void doAfterTransactionCompletion(boolean success, SessionImplementor session) {
            for(EntityCleanup cleanup : BulkOperationCleanupAction.this.entityCleanups) {
               cleanup.release();
            }

            BulkOperationCleanupAction.this.entityCleanups.clear();

            for(NaturalIdCleanup cleanup : BulkOperationCleanupAction.this.naturalIdCleanups) {
               cleanup.release();
            }

            BulkOperationCleanupAction.this.entityCleanups.clear();

            for(CollectionCleanup cleanup : BulkOperationCleanupAction.this.collectionCleanups) {
               cleanup.release();
            }

            BulkOperationCleanupAction.this.collectionCleanups.clear();
         }
      };
   }

   public void beforeExecutions() throws HibernateException {
   }

   public void execute() throws HibernateException {
   }

   private static class EntityCleanup {
      private final EntityRegionAccessStrategy cacheAccess;
      private final SoftLock cacheLock;

      private EntityCleanup(EntityRegionAccessStrategy cacheAccess) {
         this.cacheAccess = cacheAccess;
         this.cacheLock = cacheAccess.lockRegion();
         cacheAccess.removeAll();
      }

      private void release() {
         this.cacheAccess.unlockRegion(this.cacheLock);
      }
   }

   private static class CollectionCleanup {
      private final CollectionRegionAccessStrategy cacheAccess;
      private final SoftLock cacheLock;

      private CollectionCleanup(CollectionRegionAccessStrategy cacheAccess) {
         this.cacheAccess = cacheAccess;
         this.cacheLock = cacheAccess.lockRegion();
         cacheAccess.removeAll();
      }

      private void release() {
         this.cacheAccess.unlockRegion(this.cacheLock);
      }
   }

   private class NaturalIdCleanup {
      private final NaturalIdRegionAccessStrategy naturalIdCacheAccessStrategy;
      private final SoftLock cacheLock;

      public NaturalIdCleanup(NaturalIdRegionAccessStrategy naturalIdCacheAccessStrategy) {
         this.naturalIdCacheAccessStrategy = naturalIdCacheAccessStrategy;
         this.cacheLock = naturalIdCacheAccessStrategy.lockRegion();
         naturalIdCacheAccessStrategy.removeAll();
      }

      private void release() {
         this.naturalIdCacheAccessStrategy.unlockRegion(this.cacheLock);
      }
   }
}
