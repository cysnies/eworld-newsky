package org.hibernate.loader.collection;

import org.hibernate.MappingException;
import org.hibernate.engine.spi.LoadQueryInfluencers;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.loader.JoinWalker;
import org.hibernate.persister.collection.QueryableCollection;
import org.jboss.logging.Logger;

public class BasicCollectionLoader extends CollectionLoader {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, BasicCollectionLoader.class.getName());

   public BasicCollectionLoader(QueryableCollection collectionPersister, SessionFactoryImplementor session, LoadQueryInfluencers loadQueryInfluencers) throws MappingException {
      this(collectionPersister, 1, session, loadQueryInfluencers);
   }

   public BasicCollectionLoader(QueryableCollection collectionPersister, int batchSize, SessionFactoryImplementor factory, LoadQueryInfluencers loadQueryInfluencers) throws MappingException {
      this(collectionPersister, batchSize, (String)null, factory, loadQueryInfluencers);
   }

   protected BasicCollectionLoader(QueryableCollection collectionPersister, int batchSize, String subquery, SessionFactoryImplementor factory, LoadQueryInfluencers loadQueryInfluencers) throws MappingException {
      super(collectionPersister, factory, loadQueryInfluencers);
      JoinWalker walker = new BasicCollectionJoinWalker(collectionPersister, batchSize, subquery, factory, loadQueryInfluencers);
      this.initFromWalker(walker);
      this.postInstantiate();
      if (LOG.isDebugEnabled()) {
         LOG.debugf("Static select for collection %s: %s", collectionPersister.getRole(), this.getSQLString());
      }

   }
}
