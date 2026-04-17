package org.hibernate.loader.entity;

import java.io.Serializable;
import java.util.List;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.LoadQueryInfluencers;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.loader.Loader;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.OuterJoinLoadable;
import org.hibernate.type.Type;

public class BatchingEntityLoader implements UniqueEntityLoader {
   private final Loader[] loaders;
   private final int[] batchSizes;
   private final EntityPersister persister;
   private final Type idType;

   public BatchingEntityLoader(EntityPersister persister, int[] batchSizes, Loader[] loaders) {
      this.batchSizes = batchSizes;
      this.loaders = loaders;
      this.persister = persister;
      this.idType = persister.getIdentifierType();
   }

   private Object getObjectFromList(List results, Serializable id, SessionImplementor session) {
      for(Object obj : results) {
         boolean equal = this.idType.isEqual(id, session.getContextEntityIdentifier(obj), session.getFactory());
         if (equal) {
            return obj;
         }
      }

      return null;
   }

   public Object load(Serializable id, Object optionalObject, SessionImplementor session) {
      return this.load(id, optionalObject, session, LockOptions.NONE);
   }

   public Object load(Serializable id, Object optionalObject, SessionImplementor session, LockOptions lockOptions) {
      Serializable[] batch = session.getPersistenceContext().getBatchFetchQueue().getEntityBatch(this.persister, id, this.batchSizes[0], this.persister.getEntityMode());

      for(int i = 0; i < this.batchSizes.length - 1; ++i) {
         int smallBatchSize = this.batchSizes[i];
         if (batch[smallBatchSize - 1] != null) {
            Serializable[] smallBatch = new Serializable[smallBatchSize];
            System.arraycopy(batch, 0, smallBatch, 0, smallBatchSize);
            List results = this.loaders[i].loadEntityBatch(session, smallBatch, this.idType, optionalObject, this.persister.getEntityName(), id, this.persister, lockOptions);
            return this.getObjectFromList(results, id, session);
         }
      }

      return ((UniqueEntityLoader)this.loaders[this.batchSizes.length - 1]).load(id, optionalObject, session);
   }

   public static UniqueEntityLoader createBatchingEntityLoader(OuterJoinLoadable persister, int maxBatchSize, LockMode lockMode, SessionFactoryImplementor factory, LoadQueryInfluencers loadQueryInfluencers) throws MappingException {
      if (maxBatchSize <= 1) {
         return new EntityLoader(persister, lockMode, factory, loadQueryInfluencers);
      } else {
         int[] batchSizesToCreate = ArrayHelper.getBatchSizes(maxBatchSize);
         Loader[] loadersToCreate = new Loader[batchSizesToCreate.length];

         for(int i = 0; i < batchSizesToCreate.length; ++i) {
            loadersToCreate[i] = new EntityLoader(persister, batchSizesToCreate[i], lockMode, factory, loadQueryInfluencers);
         }

         return new BatchingEntityLoader(persister, batchSizesToCreate, loadersToCreate);
      }
   }

   public static UniqueEntityLoader createBatchingEntityLoader(OuterJoinLoadable persister, int maxBatchSize, LockOptions lockOptions, SessionFactoryImplementor factory, LoadQueryInfluencers loadQueryInfluencers) throws MappingException {
      if (maxBatchSize <= 1) {
         return new EntityLoader(persister, lockOptions, factory, loadQueryInfluencers);
      } else {
         int[] batchSizesToCreate = ArrayHelper.getBatchSizes(maxBatchSize);
         Loader[] loadersToCreate = new Loader[batchSizesToCreate.length];

         for(int i = 0; i < batchSizesToCreate.length; ++i) {
            loadersToCreate[i] = new EntityLoader(persister, batchSizesToCreate[i], lockOptions, factory, loadQueryInfluencers);
         }

         return new BatchingEntityLoader(persister, batchSizesToCreate, loadersToCreate);
      }
   }
}
