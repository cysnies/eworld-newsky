package org.hibernate.id.insert;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.PostInsertIdentityPersister;
import org.hibernate.pretty.MessageHelper;

public abstract class AbstractReturningDelegate implements InsertGeneratedIdentifierDelegate {
   private final PostInsertIdentityPersister persister;

   public AbstractReturningDelegate(PostInsertIdentityPersister persister) {
      this.persister = persister;
   }

   public final Serializable performInsert(String insertSQL, SessionImplementor session, Binder binder) {
      try {
         PreparedStatement insert = this.prepare(insertSQL, session);

         Serializable var5;
         try {
            binder.bindValues(insert);
            var5 = this.executeAndExtract(insert);
         } finally {
            this.releaseStatement(insert, session);
         }

         return var5;
      } catch (SQLException sqle) {
         throw session.getFactory().getSQLExceptionHelper().convert(sqle, "could not insert: " + MessageHelper.infoString(this.persister), insertSQL);
      }
   }

   protected PostInsertIdentityPersister getPersister() {
      return this.persister;
   }

   protected abstract PreparedStatement prepare(String var1, SessionImplementor var2) throws SQLException;

   protected abstract Serializable executeAndExtract(PreparedStatement var1) throws SQLException;

   protected void releaseStatement(PreparedStatement insert, SessionImplementor session) throws SQLException {
      insert.close();
   }
}
