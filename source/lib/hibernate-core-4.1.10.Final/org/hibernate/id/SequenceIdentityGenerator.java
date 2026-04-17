package org.hibernate.id;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.insert.AbstractReturningDelegate;
import org.hibernate.id.insert.IdentifierGeneratingInsert;
import org.hibernate.id.insert.InsertGeneratedIdentifierDelegate;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.sql.Insert;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public class SequenceIdentityGenerator extends SequenceGenerator implements PostInsertIdentifierGenerator {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, SequenceIdentityGenerator.class.getName());

   public Serializable generate(SessionImplementor s, Object obj) {
      return IdentifierGeneratorHelper.POST_INSERT_INDICATOR;
   }

   public InsertGeneratedIdentifierDelegate getInsertGeneratedIdentifierDelegate(PostInsertIdentityPersister persister, Dialect dialect, boolean isGetGeneratedKeysEnabled) throws HibernateException {
      return new Delegate(persister, dialect, this.getSequenceName());
   }

   public void configure(Type type, Properties params, Dialect dialect) throws MappingException {
      super.configure(type, params, dialect);
   }

   public static class Delegate extends AbstractReturningDelegate {
      private final Dialect dialect;
      private final String sequenceNextValFragment;
      private final String[] keyColumns;

      public Delegate(PostInsertIdentityPersister persister, Dialect dialect, String sequenceName) {
         super(persister);
         this.dialect = dialect;
         this.sequenceNextValFragment = dialect.getSelectSequenceNextValString(sequenceName);
         this.keyColumns = this.getPersister().getRootTableKeyColumnNames();
         if (this.keyColumns.length > 1) {
            throw new HibernateException("sequence-identity generator cannot be used with with multi-column keys");
         }
      }

      public IdentifierGeneratingInsert prepareIdentifierGeneratingInsert() {
         NoCommentsInsert insert = new NoCommentsInsert(this.dialect);
         insert.addColumn(this.getPersister().getRootTableKeyColumnNames()[0], this.sequenceNextValFragment);
         return insert;
      }

      protected PreparedStatement prepare(String insertSQL, SessionImplementor session) throws SQLException {
         return session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(insertSQL, this.keyColumns);
      }

      protected Serializable executeAndExtract(PreparedStatement insert) throws SQLException {
         insert.executeUpdate();
         return IdentifierGeneratorHelper.getGeneratedIdentity(insert.getGeneratedKeys(), this.getPersister().getRootTableKeyColumnNames()[0], this.getPersister().getIdentifierType());
      }
   }

   public static class NoCommentsInsert extends IdentifierGeneratingInsert {
      public NoCommentsInsert(Dialect dialect) {
         super(dialect);
      }

      public Insert setComment(String comment) {
         SequenceIdentityGenerator.LOG.disallowingInsertStatementComment();
         return this;
      }
   }
}
