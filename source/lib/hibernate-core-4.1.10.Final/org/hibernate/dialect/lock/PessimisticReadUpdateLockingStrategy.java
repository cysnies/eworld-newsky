package org.hibernate.dialect.lock;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.hibernate.HibernateException;
import org.hibernate.JDBCException;
import org.hibernate.LockMode;
import org.hibernate.StaleObjectStateException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.Lockable;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.sql.Update;
import org.jboss.logging.Logger;

public class PessimisticReadUpdateLockingStrategy implements LockingStrategy {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, PessimisticReadUpdateLockingStrategy.class.getName());
   private final Lockable lockable;
   private final LockMode lockMode;
   private final String sql;

   public PessimisticReadUpdateLockingStrategy(Lockable lockable, LockMode lockMode) {
      this.lockable = lockable;
      this.lockMode = lockMode;
      if (lockMode.lessThan(LockMode.PESSIMISTIC_READ)) {
         throw new HibernateException("[" + lockMode + "] not valid for update statement");
      } else {
         if (!lockable.isVersioned()) {
            LOG.writeLocksNotSupported(lockable.getEntityName());
            this.sql = null;
         } else {
            this.sql = this.generateLockString();
         }

      }
   }

   public void lock(Serializable id, Object version, Object object, int timeout, SessionImplementor session) {
      if (!this.lockable.isVersioned()) {
         throw new HibernateException("write locks via update not supported for non-versioned entities [" + this.lockable.getEntityName() + "]");
      } else {
         SessionFactoryImplementor factory = session.getFactory();

         try {
            try {
               PreparedStatement st = session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(this.sql);

               try {
                  this.lockable.getVersionType().nullSafeSet(st, version, 1, session);
                  int offset = 2;
                  this.lockable.getIdentifierType().nullSafeSet(st, id, offset, session);
                  offset += this.lockable.getIdentifierType().getColumnSpan(factory);
                  if (this.lockable.isVersioned()) {
                     this.lockable.getVersionType().nullSafeSet(st, version, offset, session);
                  }

                  int affected = st.executeUpdate();
                  if (affected < 0) {
                     if (factory.getStatistics().isStatisticsEnabled()) {
                        factory.getStatisticsImplementor().optimisticFailure(this.lockable.getEntityName());
                     }

                     throw new StaleObjectStateException(this.lockable.getEntityName(), id);
                  }
               } finally {
                  st.close();
               }

            } catch (SQLException e) {
               throw session.getFactory().getSQLExceptionHelper().convert(e, "could not lock: " + MessageHelper.infoString((EntityPersister)this.lockable, (Object)id, (SessionFactoryImplementor)session.getFactory()), this.sql);
            }
         } catch (JDBCException e) {
            throw new PessimisticEntityLockException(object, "could not obtain pessimistic lock", e);
         }
      }
   }

   protected String generateLockString() {
      SessionFactoryImplementor factory = this.lockable.getFactory();
      Update update = new Update(factory.getDialect());
      update.setTableName(this.lockable.getRootTableName());
      update.addPrimaryKeyColumns(this.lockable.getRootTableIdentifierColumnNames());
      update.setVersionColumnName(this.lockable.getVersionColumnName());
      update.addColumn(this.lockable.getVersionColumnName());
      if (factory.getSettings().isCommentsEnabled()) {
         update.setComment(this.lockMode + " lock " + this.lockable.getEntityName());
      }

      return update.toStatementString();
   }

   protected LockMode getLockMode() {
      return this.lockMode;
   }
}
