package org.hibernate.id.enhanced;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.jdbc.spi.SqlStatementLogger;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.IdentifierGenerationException;
import org.hibernate.id.IdentifierGeneratorHelper;
import org.hibernate.id.IntegralDataTypeHolder;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.jdbc.AbstractReturningWork;
import org.jboss.logging.Logger;

public class TableStructure implements DatabaseStructure {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, TableStructure.class.getName());
   private final String tableName;
   private final String valueColumnName;
   private final int initialValue;
   private final int incrementSize;
   private final Class numberType;
   private final String selectQuery;
   private final String updateQuery;
   private boolean applyIncrementSizeToSourceValues;
   private int accessCounter;

   public TableStructure(Dialect dialect, String tableName, String valueColumnName, int initialValue, int incrementSize, Class numberType) {
      this.tableName = tableName;
      this.initialValue = initialValue;
      this.incrementSize = incrementSize;
      this.valueColumnName = valueColumnName;
      this.numberType = numberType;
      this.selectQuery = "select " + valueColumnName + " as id_val" + " from " + dialect.appendLockHint(LockMode.PESSIMISTIC_WRITE, tableName) + dialect.getForUpdateString();
      this.updateQuery = "update " + tableName + " set " + valueColumnName + "= ?" + " where " + valueColumnName + "=?";
   }

   public String getName() {
      return this.tableName;
   }

   public int getInitialValue() {
      return this.initialValue;
   }

   public int getIncrementSize() {
      return this.incrementSize;
   }

   public int getTimesAccessed() {
      return this.accessCounter;
   }

   public void prepare(Optimizer optimizer) {
      this.applyIncrementSizeToSourceValues = optimizer.applyIncrementSizeToSourceValues();
   }

   public AccessCallback buildCallback(final SessionImplementor session) {
      return new AccessCallback() {
         public IntegralDataTypeHolder getNextValue() {
            return (IntegralDataTypeHolder)session.getTransactionCoordinator().getTransaction().createIsolationDelegate().delegateWork(new AbstractReturningWork() {
               public IntegralDataTypeHolder execute(Connection connection) throws SQLException {
                  SqlStatementLogger statementLogger = ((JdbcServices)session.getFactory().getServiceRegistry().getService(JdbcServices.class)).getSqlStatementLogger();
                  IntegralDataTypeHolder value = IdentifierGeneratorHelper.getIntegralDataTypeHolder(TableStructure.this.numberType);

                  int rows;
                  do {
                     statementLogger.logStatement(TableStructure.this.selectQuery, FormatStyle.BASIC.getFormatter());
                     PreparedStatement selectStatement = connection.prepareStatement(TableStructure.this.selectQuery);

                     try {
                        ResultSet selectRS = selectStatement.executeQuery();
                        if (!selectRS.next()) {
                           String err = "could not read a hi value - you need to populate the table: " + TableStructure.this.tableName;
                           TableStructure.LOG.error(err);
                           throw new IdentifierGenerationException(err);
                        }

                        value.initialize(selectRS, 1L);
                        selectRS.close();
                     } catch (SQLException sqle) {
                        TableStructure.LOG.error("could not read a hi value", sqle);
                        throw sqle;
                     } finally {
                        selectStatement.close();
                     }

                     statementLogger.logStatement(TableStructure.this.updateQuery, FormatStyle.BASIC.getFormatter());
                     PreparedStatement sqle = connection.prepareStatement(TableStructure.this.updateQuery);

                     try {
                        int increment = TableStructure.this.applyIncrementSizeToSourceValues ? TableStructure.this.incrementSize : 1;
                        IntegralDataTypeHolder updateValue = value.copy().add((long)increment);
                        updateValue.bind(sqle, 1);
                        value.bind(sqle, 2);
                        rows = sqle.executeUpdate();
                     } catch (SQLException e) {
                        TableStructure.LOG.unableToUpdateQueryHiValue(TableStructure.this.tableName, e);
                        throw e;
                     } finally {
                        sqle.close();
                     }
                  } while(rows == 0);

                  TableStructure.this.accessCounter++;
                  return value;
               }
            }, true);
         }
      };
   }

   public String[] sqlCreateStrings(Dialect dialect) throws HibernateException {
      return new String[]{dialect.getCreateTableString() + " " + this.tableName + " ( " + this.valueColumnName + " " + dialect.getTypeName(-5) + " )", "insert into " + this.tableName + " values ( " + this.initialValue + " )"};
   }

   public String[] sqlDropStrings(Dialect dialect) throws HibernateException {
      return new String[]{dialect.getDropTableString(this.tableName)};
   }

   public boolean isPhysicalSequence() {
      return false;
   }
}
