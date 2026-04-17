package org.hibernate.loader.entity;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.hibernate.HibernateException;
import org.hibernate.LockOptions;
import org.hibernate.engine.spi.LoadQueryInfluencers;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.loader.OuterJoinLoader;
import org.hibernate.persister.entity.OuterJoinLoadable;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.type.Type;

public abstract class AbstractEntityLoader extends OuterJoinLoader implements UniqueEntityLoader {
   protected final OuterJoinLoadable persister;
   protected final Type uniqueKeyType;
   protected final String entityName;

   public AbstractEntityLoader(OuterJoinLoadable persister, Type uniqueKeyType, SessionFactoryImplementor factory, LoadQueryInfluencers loadQueryInfluencers) {
      super(factory, loadQueryInfluencers);
      this.uniqueKeyType = uniqueKeyType;
      this.entityName = persister.getEntityName();
      this.persister = persister;
   }

   public Object load(Serializable id, Object optionalObject, SessionImplementor session) {
      return this.load(id, optionalObject, session, LockOptions.NONE);
   }

   public Object load(Serializable id, Object optionalObject, SessionImplementor session, LockOptions lockOptions) {
      return this.load(session, id, optionalObject, id, lockOptions);
   }

   protected Object load(SessionImplementor session, Object id, Object optionalObject, Serializable optionalId, LockOptions lockOptions) {
      List list = this.loadEntity(session, id, this.uniqueKeyType, optionalObject, this.entityName, optionalId, this.persister, lockOptions);
      if (list.size() == 1) {
         return list.get(0);
      } else if (list.size() == 0) {
         return null;
      } else if (this.getCollectionOwners() != null) {
         return list.get(0);
      } else {
         throw new HibernateException("More than one row with the given identifier was found: " + id + ", for class: " + this.persister.getEntityName());
      }
   }

   protected Object getResultColumnOrRow(Object[] row, ResultTransformer transformer, ResultSet rs, SessionImplementor session) throws SQLException, HibernateException {
      return row[row.length - 1];
   }

   protected boolean isSingleRowLoader() {
      return true;
   }
}
