package org.hibernate.persister.entity;

import java.io.Serializable;
import org.hibernate.FlushMode;
import org.hibernate.LockOptions;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.AbstractQueryImpl;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.loader.entity.UniqueEntityLoader;
import org.jboss.logging.Logger;

public final class NamedQueryLoader implements UniqueEntityLoader {
   private final String queryName;
   private final EntityPersister persister;
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, NamedQueryLoader.class.getName());

   public NamedQueryLoader(String queryName, EntityPersister persister) {
      this.queryName = queryName;
      this.persister = persister;
   }

   public Object load(Serializable id, Object optionalObject, SessionImplementor session, LockOptions lockOptions) {
      if (lockOptions != null) {
         LOG.debug("Ignoring lock-options passed to named query loader");
      }

      return this.load(id, optionalObject, session);
   }

   public Object load(Serializable id, Object optionalObject, SessionImplementor session) {
      LOG.debugf("Loading entity: %s using named query: %s", this.persister.getEntityName(), this.queryName);
      AbstractQueryImpl query = (AbstractQueryImpl)session.getNamedQuery(this.queryName);
      if (query.hasNamedParameters()) {
         query.setParameter(query.getNamedParameters()[0], id, this.persister.getIdentifierType());
      } else {
         query.setParameter(0, id, this.persister.getIdentifierType());
      }

      query.setOptionalId(id);
      query.setOptionalEntityName(this.persister.getEntityName());
      query.setOptionalObject(optionalObject);
      query.setFlushMode(FlushMode.MANUAL);
      query.list();
      return session.getPersistenceContext().getEntity(session.generateEntityKey(id, this.persister));
   }
}
