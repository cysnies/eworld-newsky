package org.hibernate.loader.collection;

import java.io.Serializable;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.LoadQueryInfluencers;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.loader.Loader;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.collection.QueryableCollection;

public class BatchingCollectionInitializer implements CollectionInitializer {
   private final Loader[] loaders;
   private final int[] batchSizes;
   private final CollectionPersister collectionPersister;

   public BatchingCollectionInitializer(CollectionPersister collPersister, int[] batchSizes, Loader[] loaders) {
      this.loaders = loaders;
      this.batchSizes = batchSizes;
      this.collectionPersister = collPersister;
   }

   public CollectionPersister getCollectionPersister() {
      return this.collectionPersister;
   }

   public Loader[] getLoaders() {
      return this.loaders;
   }

   public int[] getBatchSizes() {
      return this.batchSizes;
   }

   public void initialize(Serializable id, SessionImplementor session) throws HibernateException {
      Serializable[] batch = session.getPersistenceContext().getBatchFetchQueue().getCollectionBatch(this.collectionPersister, id, this.batchSizes[0]);

      for(int i = 0; i < this.batchSizes.length - 1; ++i) {
         int smallBatchSize = this.batchSizes[i];
         if (batch[smallBatchSize - 1] != null) {
            Serializable[] smallBatch = new Serializable[smallBatchSize];
            System.arraycopy(batch, 0, smallBatch, 0, smallBatchSize);
            this.loaders[i].loadCollectionBatch(session, smallBatch, this.collectionPersister.getKeyType());
            return;
         }
      }

      this.loaders[this.batchSizes.length - 1].loadCollection(session, id, this.collectionPersister.getKeyType());
   }

   public static CollectionInitializer createBatchingOneToManyInitializer(QueryableCollection persister, int maxBatchSize, SessionFactoryImplementor factory, LoadQueryInfluencers loadQueryInfluencers) throws MappingException {
      if (maxBatchSize <= 1) {
         return new OneToManyLoader(persister, factory, loadQueryInfluencers);
      } else {
         int[] batchSizesToCreate = ArrayHelper.getBatchSizes(maxBatchSize);
         Loader[] loadersToCreate = new Loader[batchSizesToCreate.length];

         for(int i = 0; i < batchSizesToCreate.length; ++i) {
            loadersToCreate[i] = new OneToManyLoader(persister, batchSizesToCreate[i], factory, loadQueryInfluencers);
         }

         return new BatchingCollectionInitializer(persister, batchSizesToCreate, loadersToCreate);
      }
   }

   public static CollectionInitializer createBatchingCollectionInitializer(QueryableCollection persister, int maxBatchSize, SessionFactoryImplementor factory, LoadQueryInfluencers loadQueryInfluencers) throws MappingException {
      if (maxBatchSize <= 1) {
         return new BasicCollectionLoader(persister, factory, loadQueryInfluencers);
      } else {
         int[] batchSizesToCreate = ArrayHelper.getBatchSizes(maxBatchSize);
         Loader[] loadersToCreate = new Loader[batchSizesToCreate.length];

         for(int i = 0; i < batchSizesToCreate.length; ++i) {
            loadersToCreate[i] = new BasicCollectionLoader(persister, batchSizesToCreate[i], factory, loadQueryInfluencers);
         }

         return new BatchingCollectionInitializer(persister, batchSizesToCreate, loadersToCreate);
      }
   }
}
