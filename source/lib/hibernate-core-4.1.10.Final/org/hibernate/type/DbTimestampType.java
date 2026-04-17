package org.hibernate.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public class DbTimestampType extends TimestampType {
   public static final DbTimestampType INSTANCE = new DbTimestampType();
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, DbTimestampType.class.getName());

   public String getName() {
      return "dbtimestamp";
   }

   public String[] getRegistrationKeys() {
      return new String[]{this.getName()};
   }

   public Date seed(SessionImplementor session) {
      if (session == null) {
         LOG.trace("Incoming session was null; using current jvm time");
         return super.seed(session);
      } else if (!session.getFactory().getDialect().supportsCurrentTimestampSelection()) {
         LOG.debug("Falling back to vm-based timestamp, as dialect does not support current timestamp selection");
         return super.seed(session);
      } else {
         return this.getCurrentTimestamp(session);
      }
   }

   private Date getCurrentTimestamp(SessionImplementor session) {
      Dialect dialect = session.getFactory().getDialect();
      String timestampSelectString = dialect.getCurrentTimestampSelectString();
      return dialect.isCurrentTimestampSelectStringCallable() ? this.useCallableStatement(timestampSelectString, session) : this.usePreparedStatement(timestampSelectString, session);
   }

   private Timestamp usePreparedStatement(String timestampSelectString, SessionImplementor session) {
      PreparedStatement ps = null;

      Timestamp var6;
      try {
         ps = session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(timestampSelectString, false);
         ResultSet rs = ps.executeQuery();
         rs.next();
         Timestamp ts = rs.getTimestamp(1);
         if (LOG.isTraceEnabled()) {
            LOG.tracev("Current timestamp retreived from db : {0} (nanos={1}, time={2})", ts, ts.getNanos(), ts.getTime());
         }

         var6 = ts;
      } catch (SQLException e) {
         throw session.getFactory().getSQLExceptionHelper().convert(e, "could not select current db timestamp", timestampSelectString);
      } finally {
         if (ps != null) {
            try {
               ps.close();
            } catch (SQLException sqle) {
               LOG.unableToCleanUpPreparedStatement(sqle);
            }
         }

      }

      return var6;
   }

   private Timestamp useCallableStatement(String callString, SessionImplementor session) {
      CallableStatement cs = null;

      Timestamp var5;
      try {
         cs = (CallableStatement)session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(callString, true);
         cs.registerOutParameter(1, 93);
         cs.execute();
         Timestamp ts = cs.getTimestamp(1);
         if (LOG.isTraceEnabled()) {
            LOG.tracev("Current timestamp retreived from db : {0} (nanos={1}, time={2})", ts, ts.getNanos(), ts.getTime());
         }

         var5 = ts;
      } catch (SQLException e) {
         throw session.getFactory().getSQLExceptionHelper().convert(e, "could not call current db timestamp function", callString);
      } finally {
         if (cs != null) {
            try {
               cs.close();
            } catch (SQLException sqle) {
               LOG.unableToCleanUpCallableStatement(sqle);
            }
         }

      }

      return var5;
   }
}
