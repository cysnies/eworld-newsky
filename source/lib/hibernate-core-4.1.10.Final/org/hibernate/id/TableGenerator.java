package org.hibernate.id;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.cfg.ObjectNameNormalizer;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.jdbc.spi.SqlStatementLogger;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.jdbc.AbstractReturningWork;
import org.hibernate.mapping.Table;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

/** @deprecated */
@Deprecated
public class TableGenerator implements PersistentIdentifierGenerator, Configurable {
   public static final String COLUMN = "column";
   public static final String DEFAULT_COLUMN_NAME = "next_hi";
   public static final String TABLE = "table";
   public static final String DEFAULT_TABLE_NAME = "hibernate_unique_key";
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, TableGenerator.class.getName());
   private Type identifierType;
   private String tableName;
   private String columnName;
   private String query;
   private String update;

   public void configure(Type type, Properties params, Dialect dialect) {
      this.identifierType = type;
      ObjectNameNormalizer normalizer = (ObjectNameNormalizer)params.get("identifier_normalizer");
      this.tableName = ConfigurationHelper.getString("table", params, "hibernate_unique_key");
      if (this.tableName.indexOf(46) < 0) {
         String schemaName = normalizer.normalizeIdentifierQuoting(params.getProperty("schema"));
         String catalogName = normalizer.normalizeIdentifierQuoting(params.getProperty("catalog"));
         this.tableName = Table.qualify(dialect.quote(catalogName), dialect.quote(schemaName), dialect.quote(this.tableName));
      }

      this.columnName = dialect.quote(normalizer.normalizeIdentifierQuoting(ConfigurationHelper.getString("column", params, "next_hi")));
      this.query = "select " + this.columnName + " from " + dialect.appendLockHint(LockMode.PESSIMISTIC_WRITE, this.tableName) + dialect.getForUpdateString();
      this.update = "update " + this.tableName + " set " + this.columnName + " = ? where " + this.columnName + " = ?";
   }

   public synchronized Serializable generate(SessionImplementor session, Object object) {
      return this.generateHolder(session).makeValue();
   }

   protected IntegralDataTypeHolder generateHolder(SessionImplementor session) {
      final SqlStatementLogger statementLogger = ((JdbcServices)session.getFactory().getServiceRegistry().getService(JdbcServices.class)).getSqlStatementLogger();
      return (IntegralDataTypeHolder)session.getTransactionCoordinator().getTransaction().createIsolationDelegate().delegateWork(new AbstractReturningWork() {
         public IntegralDataTypeHolder execute(Connection connection) throws SQLException {
            IntegralDataTypeHolder value = TableGenerator.this.buildHolder();

            int rows;
            do {
               statementLogger.logStatement(TableGenerator.this.query, FormatStyle.BASIC.getFormatter());
               PreparedStatement qps = connection.prepareStatement(TableGenerator.this.query);

               try {
                  ResultSet rs = qps.executeQuery();
                  if (!rs.next()) {
                     String err = "could not read a hi value - you need to populate the table: " + TableGenerator.this.tableName;
                     TableGenerator.LOG.error(err);
                     throw new IdentifierGenerationException(err);
                  }

                  value.initialize(rs, 1L);
                  rs.close();
               } catch (SQLException e) {
                  TableGenerator.LOG.error("Could not read a hi value", e);
                  throw e;
               } finally {
                  qps.close();
               }

               statementLogger.logStatement(TableGenerator.this.update, FormatStyle.BASIC.getFormatter());
               PreparedStatement e = connection.prepareStatement(TableGenerator.this.update);

               try {
                  value.copy().increment().bind(e, 1);
                  value.bind(e, 2);
                  rows = e.executeUpdate();
               } catch (SQLException sqle) {
                  TableGenerator.LOG.error(TableGenerator.LOG.unableToUpdateHiValue(TableGenerator.this.tableName), sqle);
                  throw sqle;
               } finally {
                  e.close();
               }
            } while(rows == 0);

            return value;
         }
      }, true);
   }

   public String[] sqlCreateStrings(Dialect dialect) throws HibernateException {
      return new String[]{dialect.getCreateTableString() + " " + this.tableName + " ( " + this.columnName + " " + dialect.getTypeName(4) + " )", "insert into " + this.tableName + " values ( 0 )"};
   }

   public String[] sqlDropStrings(Dialect dialect) {
      return new String[]{dialect.getDropTableString(this.tableName)};
   }

   public Object generatorKey() {
      return this.tableName;
   }

   protected IntegralDataTypeHolder buildHolder() {
      return IdentifierGeneratorHelper.getIntegralDataTypeHolder(this.identifierType.getReturnedClass());
   }
}
