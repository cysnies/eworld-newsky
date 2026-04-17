package org.hibernate.id.insert;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.PostInsertIdentityPersister;
import org.hibernate.pretty.MessageHelper;

public abstract class AbstractSelectingDelegate implements InsertGeneratedIdentifierDelegate {
   private final PostInsertIdentityPersister persister;

   protected AbstractSelectingDelegate(PostInsertIdentityPersister persister) {
      this.persister = persister;
   }

   public final Serializable performInsert(String insertSQL, SessionImplementor session, Binder binder) {
      try {
         PreparedStatement insert = session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(insertSQL, 2);

         try {
            binder.bindValues(insert);
            insert.executeUpdate();
         } finally {
            insert.close();
         }
      } catch (SQLException sqle) {
         throw session.getFactory().getSQLExceptionHelper().convert(sqle, "could not insert: " + MessageHelper.infoString(this.persister), insertSQL);
      }

      String selectSQL = this.getSelectSQL();

      try {
         PreparedStatement idSelect = session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(selectSQL, false);

         Serializable var7;
         try {
            this.bindParameters(session, idSelect, binder.getEntity());
            ResultSet rs = idSelect.executeQuery();

            try {
               var7 = this.getResult(session, rs, binder.getEntity());
            } finally {
               rs.close();
            }
         } finally {
            idSelect.close();
         }

         return var7;
      } catch (SQLException sqle) {
         throw session.getFactory().getSQLExceptionHelper().convert(sqle, "could not retrieve generated id after insert: " + MessageHelper.infoString(this.persister), insertSQL);
      }
   }

   protected abstract String getSelectSQL();

   protected void bindParameters(SessionImplementor session, PreparedStatement ps, Object entity) throws SQLException {
   }

   protected abstract Serializable getResult(SessionImplementor var1, ResultSet var2, Object var3) throws SQLException;
}
