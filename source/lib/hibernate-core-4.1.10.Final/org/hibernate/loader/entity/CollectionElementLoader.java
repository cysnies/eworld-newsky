package org.hibernate.loader.entity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.LoadQueryInfluencers;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.loader.JoinWalker;
import org.hibernate.loader.OuterJoinLoader;
import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.persister.entity.OuterJoinLoadable;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public class CollectionElementLoader extends OuterJoinLoader {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, CollectionElementLoader.class.getName());
   private final OuterJoinLoadable persister;
   private final Type keyType;
   private final Type indexType;
   private final String entityName;

   public CollectionElementLoader(QueryableCollection collectionPersister, SessionFactoryImplementor factory, LoadQueryInfluencers loadQueryInfluencers) throws MappingException {
      super(factory, loadQueryInfluencers);
      this.keyType = collectionPersister.getKeyType();
      this.indexType = collectionPersister.getIndexType();
      this.persister = (OuterJoinLoadable)collectionPersister.getElementPersister();
      this.entityName = this.persister.getEntityName();
      JoinWalker walker = new EntityJoinWalker(this.persister, ArrayHelper.join(collectionPersister.getKeyColumnNames(), collectionPersister.getIndexColumnNames()), 1, LockMode.NONE, factory, loadQueryInfluencers);
      this.initFromWalker(walker);
      this.postInstantiate();
      if (LOG.isDebugEnabled()) {
         LOG.debugf("Static select for entity %s: %s", this.entityName, this.getSQLString());
      }

   }

   public Object loadElement(SessionImplementor session, Object key, Object index) throws HibernateException {
      List list = this.loadEntity(session, key, index, this.keyType, this.indexType, this.persister);
      if (list.size() == 1) {
         return list.get(0);
      } else if (list.size() == 0) {
         return null;
      } else if (this.getCollectionOwners() != null) {
         return list.get(0);
      } else {
         throw new HibernateException("More than one row was found");
      }
   }

   protected Object getResultColumnOrRow(Object[] row, ResultTransformer transformer, ResultSet rs, SessionImplementor session) throws SQLException, HibernateException {
      return row[row.length - 1];
   }

   protected boolean isSingleRowLoader() {
      return true;
   }
}
