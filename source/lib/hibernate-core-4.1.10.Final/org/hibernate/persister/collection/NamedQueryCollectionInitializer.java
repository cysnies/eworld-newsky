package org.hibernate.persister.collection;

import java.io.Serializable;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.AbstractQueryImpl;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.loader.collection.CollectionInitializer;
import org.jboss.logging.Logger;

public final class NamedQueryCollectionInitializer implements CollectionInitializer {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, NamedQueryCollectionInitializer.class.getName());
   private final String queryName;
   private final CollectionPersister persister;

   public NamedQueryCollectionInitializer(String queryName, CollectionPersister persister) {
      this.queryName = queryName;
      this.persister = persister;
   }

   public void initialize(Serializable key, SessionImplementor session) throws HibernateException {
      LOG.debugf("Initializing collection: %s using named query: %s", this.persister.getRole(), this.queryName);
      AbstractQueryImpl query = (AbstractQueryImpl)session.getNamedSQLQuery(this.queryName);
      if (query.getNamedParameters().length > 0) {
         query.setParameter(query.getNamedParameters()[0], key, this.persister.getKeyType());
      } else {
         query.setParameter(0, key, this.persister.getKeyType());
      }

      query.setCollectionKey(key).setFlushMode(FlushMode.MANUAL).list();
   }
}
