package org.hibernate.id;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public class GUIDGenerator implements IdentifierGenerator {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, GUIDGenerator.class.getName());
   private static boolean warned = false;

   public GUIDGenerator() {
      if (!warned) {
         warned = true;
         LOG.deprecatedUuidGenerator(UUIDGenerator.class.getName(), UUIDGenerationStrategy.class.getName());
      }

   }

   public Serializable generate(SessionImplementor session, Object obj) throws HibernateException {
      String sql = session.getFactory().getDialect().getSelectGUIDString();

      try {
         PreparedStatement st = session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(sql);

         String var7;
         try {
            ResultSet rs = st.executeQuery();

            String result;
            try {
               rs.next();
               result = rs.getString(1);
            } finally {
               rs.close();
            }

            LOG.guidGenerated(result);
            var7 = result;
         } finally {
            st.close();
         }

         return var7;
      } catch (SQLException sqle) {
         throw session.getFactory().getSQLExceptionHelper().convert(sqle, "could not retrieve GUID", sql);
      }
   }
}
