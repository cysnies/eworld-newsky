package org.hibernate.dialect.lock;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.hibernate.JDBCException;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.StaleObjectStateException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.Lockable;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.sql.SimpleSelect;

public class SelectLockingStrategy extends AbstractSelectLockingStrategy {
   public SelectLockingStrategy(Lockable lockable, LockMode lockMode) {
      super(lockable, lockMode);
   }

   public void lock(Serializable id, Object version, Object object, int timeout, SessionImplementor session) throws StaleObjectStateException, JDBCException {
      String sql = this.determineSql(timeout);
      SessionFactoryImplementor factory = session.getFactory();

      try {
         PreparedStatement st = session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(sql);

         try {
            this.getLockable().getIdentifierType().nullSafeSet(st, id, 1, session);
            if (this.getLockable().isVersioned()) {
               this.getLockable().getVersionType().nullSafeSet(st, version, this.getLockable().getIdentifierType().getColumnSpan(factory) + 1, session);
            }

            ResultSet rs = st.executeQuery();

            try {
               if (!rs.next()) {
                  if (factory.getStatistics().isStatisticsEnabled()) {
                     factory.getStatisticsImplementor().optimisticFailure(this.getLockable().getEntityName());
                  }

                  throw new StaleObjectStateException(this.getLockable().getEntityName(), id);
               }
            } finally {
               rs.close();
            }
         } finally {
            st.close();
         }

      } catch (SQLException sqle) {
         throw session.getFactory().getSQLExceptionHelper().convert(sqle, "could not lock: " + MessageHelper.infoString((EntityPersister)this.getLockable(), (Object)id, (SessionFactoryImplementor)session.getFactory()), sql);
      }
   }

   protected String generateLockString(int timeout) {
      SessionFactoryImplementor factory = this.getLockable().getFactory();
      LockOptions lockOptions = new LockOptions(this.getLockMode());
      lockOptions.setTimeOut(timeout);
      SimpleSelect select = (new SimpleSelect(factory.getDialect())).setLockOptions(lockOptions).setTableName(this.getLockable().getRootTableName()).addColumn(this.getLockable().getRootTableIdentifierColumnNames()[0]).addCondition(this.getLockable().getRootTableIdentifierColumnNames(), "=?");
      if (this.getLockable().isVersioned()) {
         select.addCondition(this.getLockable().getVersionColumnName(), "=?");
      }

      if (factory.getSettings().isCommentsEnabled()) {
         select.setComment(this.getLockMode() + " lock " + this.getLockable().getEntityName());
      }

      return select.toStatementString();
   }
}
