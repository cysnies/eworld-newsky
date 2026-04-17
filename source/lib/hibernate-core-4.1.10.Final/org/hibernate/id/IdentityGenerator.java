package org.hibernate.id;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.insert.AbstractReturningDelegate;
import org.hibernate.id.insert.AbstractSelectingDelegate;
import org.hibernate.id.insert.IdentifierGeneratingInsert;
import org.hibernate.id.insert.InsertGeneratedIdentifierDelegate;
import org.hibernate.id.insert.InsertSelectIdentityInsert;

public class IdentityGenerator extends AbstractPostInsertGenerator {
   public InsertGeneratedIdentifierDelegate getInsertGeneratedIdentifierDelegate(PostInsertIdentityPersister persister, Dialect dialect, boolean isGetGeneratedKeysEnabled) throws HibernateException {
      if (isGetGeneratedKeysEnabled) {
         return new GetGeneratedKeysDelegate(persister, dialect);
      } else {
         return (InsertGeneratedIdentifierDelegate)(dialect.supportsInsertSelectIdentity() ? new InsertSelectDelegate(persister, dialect) : new BasicDelegate(persister, dialect));
      }
   }

   public static class GetGeneratedKeysDelegate extends AbstractReturningDelegate implements InsertGeneratedIdentifierDelegate {
      private final PostInsertIdentityPersister persister;
      private final Dialect dialect;

      public GetGeneratedKeysDelegate(PostInsertIdentityPersister persister, Dialect dialect) {
         super(persister);
         this.persister = persister;
         this.dialect = dialect;
      }

      public IdentifierGeneratingInsert prepareIdentifierGeneratingInsert() {
         IdentifierGeneratingInsert insert = new IdentifierGeneratingInsert(this.dialect);
         insert.addIdentityColumn(this.persister.getRootTableKeyColumnNames()[0]);
         return insert;
      }

      protected PreparedStatement prepare(String insertSQL, SessionImplementor session) throws SQLException {
         return session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(insertSQL, 1);
      }

      public Serializable executeAndExtract(PreparedStatement insert) throws SQLException {
         insert.executeUpdate();
         ResultSet rs = null;

         Serializable var3;
         try {
            rs = insert.getGeneratedKeys();
            var3 = IdentifierGeneratorHelper.getGeneratedIdentity(rs, this.persister.getRootTableKeyColumnNames()[0], this.persister.getIdentifierType());
         } finally {
            if (rs != null) {
               rs.close();
            }

         }

         return var3;
      }
   }

   public static class InsertSelectDelegate extends AbstractReturningDelegate implements InsertGeneratedIdentifierDelegate {
      private final PostInsertIdentityPersister persister;
      private final Dialect dialect;

      public InsertSelectDelegate(PostInsertIdentityPersister persister, Dialect dialect) {
         super(persister);
         this.persister = persister;
         this.dialect = dialect;
      }

      public IdentifierGeneratingInsert prepareIdentifierGeneratingInsert() {
         InsertSelectIdentityInsert insert = new InsertSelectIdentityInsert(this.dialect);
         insert.addIdentityColumn(this.persister.getRootTableKeyColumnNames()[0]);
         return insert;
      }

      protected PreparedStatement prepare(String insertSQL, SessionImplementor session) throws SQLException {
         return session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(insertSQL, 2);
      }

      public Serializable executeAndExtract(PreparedStatement insert) throws SQLException {
         if (!insert.execute()) {
            while(!insert.getMoreResults() && insert.getUpdateCount() != -1) {
            }
         }

         ResultSet rs = insert.getResultSet();

         Serializable var3;
         try {
            var3 = IdentifierGeneratorHelper.getGeneratedIdentity(rs, this.persister.getRootTableKeyColumnNames()[0], this.persister.getIdentifierType());
         } finally {
            rs.close();
         }

         return var3;
      }

      public Serializable determineGeneratedIdentifier(SessionImplementor session, Object entity) {
         throw new AssertionFailure("insert statement returns generated value");
      }
   }

   public static class BasicDelegate extends AbstractSelectingDelegate implements InsertGeneratedIdentifierDelegate {
      private final PostInsertIdentityPersister persister;
      private final Dialect dialect;

      public BasicDelegate(PostInsertIdentityPersister persister, Dialect dialect) {
         super(persister);
         this.persister = persister;
         this.dialect = dialect;
      }

      public IdentifierGeneratingInsert prepareIdentifierGeneratingInsert() {
         IdentifierGeneratingInsert insert = new IdentifierGeneratingInsert(this.dialect);
         insert.addIdentityColumn(this.persister.getRootTableKeyColumnNames()[0]);
         return insert;
      }

      protected String getSelectSQL() {
         return this.persister.getIdentitySelectString();
      }

      protected Serializable getResult(SessionImplementor session, ResultSet rs, Object object) throws SQLException {
         return IdentifierGeneratorHelper.getGeneratedIdentity(rs, this.persister.getRootTableKeyColumnNames()[0], this.persister.getIdentifierType());
      }
   }
}
