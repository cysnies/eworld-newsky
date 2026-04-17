package org.hibernate.loader.collection;

import org.hibernate.MappingException;
import org.hibernate.engine.spi.LoadQueryInfluencers;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.loader.JoinWalker;
import org.hibernate.persister.collection.QueryableCollection;
import org.jboss.logging.Logger;

public class OneToManyLoader extends CollectionLoader {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, OneToManyLoader.class.getName());

   public OneToManyLoader(QueryableCollection oneToManyPersister, SessionFactoryImplementor session, LoadQueryInfluencers loadQueryInfluencers) throws MappingException {
      this(oneToManyPersister, 1, session, loadQueryInfluencers);
   }

   public OneToManyLoader(QueryableCollection oneToManyPersister, int batchSize, SessionFactoryImplementor factory, LoadQueryInfluencers loadQueryInfluencers) throws MappingException {
      this(oneToManyPersister, batchSize, (String)null, factory, loadQueryInfluencers);
   }

   public OneToManyLoader(QueryableCollection oneToManyPersister, int batchSize, String subquery, SessionFactoryImplementor factory, LoadQueryInfluencers loadQueryInfluencers) throws MappingException {
      super(oneToManyPersister, factory, loadQueryInfluencers);
      JoinWalker walker = new OneToManyJoinWalker(oneToManyPersister, batchSize, subquery, factory, loadQueryInfluencers);
      this.initFromWalker(walker);
      this.postInstantiate();
      if (LOG.isDebugEnabled()) {
         LOG.debugf("Static select for one-to-many %s: %s", oneToManyPersister.getRole(), this.getSQLString());
      }

   }
}
