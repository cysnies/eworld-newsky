package org.hibernate.id;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.MappingException;
import org.hibernate.cfg.ObjectNameNormalizer;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.jdbc.spi.SqlStatementLogger;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.enhanced.AccessCallback;
import org.hibernate.id.enhanced.OptimizerFactory;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.jdbc.AbstractReturningWork;
import org.hibernate.jdbc.WorkExecutorVisitable;
import org.hibernate.mapping.Table;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public class MultipleHiLoPerTableGenerator implements PersistentIdentifierGenerator, Configurable {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, MultipleHiLoPerTableGenerator.class.getName());
   public static final String ID_TABLE = "table";
   public static final String PK_COLUMN_NAME = "primary_key_column";
   public static final String PK_VALUE_NAME = "primary_key_value";
   public static final String VALUE_COLUMN_NAME = "value_column";
   public static final String PK_LENGTH_NAME = "primary_key_length";
   private static final int DEFAULT_PK_LENGTH = 255;
   public static final String DEFAULT_TABLE = "hibernate_sequences";
   private static final String DEFAULT_PK_COLUMN = "sequence_name";
   private static final String DEFAULT_VALUE_COLUMN = "sequence_next_hi_value";
   private String tableName;
   private String pkColumnName;
   private String valueColumnName;
   private String query;
   private String insert;
   private String update;
   public static final String MAX_LO = "max_lo";
   private int maxLo;
   private OptimizerFactory.LegacyHiLoAlgorithmOptimizer hiloOptimizer;
   private Class returnClass;
   private int keySize;

   public String[] sqlCreateStrings(Dialect dialect) throws HibernateException {
      return new String[]{dialect.getCreateTableString() + ' ' + this.tableName + " ( " + this.pkColumnName + ' ' + dialect.getTypeName(12, (long)this.keySize, 0, 0) + ",  " + this.valueColumnName + ' ' + dialect.getTypeName(4) + " ) "};
   }

   public String[] sqlDropStrings(Dialect dialect) throws HibernateException {
      return new String[]{dialect.getDropTableString(this.tableName)};
   }

   public Object generatorKey() {
      return this.tableName;
   }

   public synchronized Serializable generate(final SessionImplementor session, Object obj) {
      final WorkExecutorVisitable<IntegralDataTypeHolder> work = new AbstractReturningWork() {
         public IntegralDataTypeHolder execute(Connection connection) throws SQLException {
            IntegralDataTypeHolder value = IdentifierGeneratorHelper.getIntegralDataTypeHolder(MultipleHiLoPerTableGenerator.this.returnClass);
            SqlStatementLogger statementLogger = ((JdbcServices)session.getFactory().getServiceRegistry().getService(JdbcServices.class)).getSqlStatementLogger();

            int rows;
            do {
               statementLogger.logStatement(MultipleHiLoPerTableGenerator.this.query, FormatStyle.BASIC.getFormatter());
               PreparedStatement qps = connection.prepareStatement(MultipleHiLoPerTableGenerator.this.query);
               PreparedStatement ips = null;

               try {
                  ResultSet rs = qps.executeQuery();
                  boolean isInitialized = rs.next();
                  if (!isInitialized) {
                     value.initialize(0L);
                     statementLogger.logStatement(MultipleHiLoPerTableGenerator.this.insert, FormatStyle.BASIC.getFormatter());
                     ips = connection.prepareStatement(MultipleHiLoPerTableGenerator.this.insert);
                     value.bind(ips, 1);
                     ips.execute();
                  } else {
                     value.initialize(rs, 0L);
                  }

                  rs.close();
               } catch (SQLException sqle) {
                  MultipleHiLoPerTableGenerator.LOG.unableToReadOrInitHiValue(sqle);
                  throw sqle;
               } finally {
                  if (ips != null) {
                     ips.close();
                  }

                  qps.close();
               }

               statementLogger.logStatement(MultipleHiLoPerTableGenerator.this.update, FormatStyle.BASIC.getFormatter());
               PreparedStatement sqle = connection.prepareStatement(MultipleHiLoPerTableGenerator.this.update);

               try {
                  value.copy().increment().bind(sqle, 1);
                  value.bind(sqle, 2);
                  rows = sqle.executeUpdate();
               } catch (SQLException sqle) {
                  MultipleHiLoPerTableGenerator.LOG.error(MultipleHiLoPerTableGenerator.LOG.unableToUpdateHiValue(MultipleHiLoPerTableGenerator.this.tableName), sqle);
                  throw sqle;
               } finally {
                  sqle.close();
               }
            } while(rows == 0);

            return value;
         }
      };
      if (this.maxLo >= 1) {
         return this.hiloOptimizer.generate(new AccessCallback() {
            public IntegralDataTypeHolder getNextValue() {
               return (IntegralDataTypeHolder)session.getTransactionCoordinator().getTransaction().createIsolationDelegate().delegateWork(work, true);
            }
         });
      } else {
         IntegralDataTypeHolder value;
         for(value = null; value == null || value.lt(1L); value = (IntegralDataTypeHolder)session.getTransactionCoordinator().getTransaction().createIsolationDelegate().delegateWork(work, true)) {
         }

         return value.makeValue();
      }
   }

   public void configure(Type type, Properties params, Dialect dialect) throws MappingException {
      ObjectNameNormalizer normalizer = (ObjectNameNormalizer)params.get("identifier_normalizer");
      this.tableName = normalizer.normalizeIdentifierQuoting(ConfigurationHelper.getString("table", params, "hibernate_sequences"));
      if (this.tableName.indexOf(46) < 0) {
         this.tableName = dialect.quote(this.tableName);
         String schemaName = dialect.quote(normalizer.normalizeIdentifierQuoting(params.getProperty("schema")));
         String catalogName = dialect.quote(normalizer.normalizeIdentifierQuoting(params.getProperty("catalog")));
         this.tableName = Table.qualify(catalogName, schemaName, this.tableName);
      }

      this.pkColumnName = dialect.quote(normalizer.normalizeIdentifierQuoting(ConfigurationHelper.getString("primary_key_column", params, "sequence_name")));
      this.valueColumnName = dialect.quote(normalizer.normalizeIdentifierQuoting(ConfigurationHelper.getString("value_column", params, "sequence_next_hi_value")));
      this.keySize = ConfigurationHelper.getInt("primary_key_length", params, 255);
      String keyValue = ConfigurationHelper.getString("primary_key_value", params, params.getProperty("target_table"));
      this.query = "select " + this.valueColumnName + " from " + dialect.appendLockHint(LockMode.PESSIMISTIC_WRITE, this.tableName) + " where " + this.pkColumnName + " = '" + keyValue + "'" + dialect.getForUpdateString();
      this.update = "update " + this.tableName + " set " + this.valueColumnName + " = ? where " + this.valueColumnName + " = ? and " + this.pkColumnName + " = '" + keyValue + "'";
      this.insert = "insert into " + this.tableName + "(" + this.pkColumnName + ", " + this.valueColumnName + ") " + "values('" + keyValue + "', ?)";
      this.maxLo = ConfigurationHelper.getInt("max_lo", params, 32767);
      this.returnClass = type.getReturnedClass();
      if (this.maxLo >= 1) {
         this.hiloOptimizer = new OptimizerFactory.LegacyHiLoAlgorithmOptimizer(this.returnClass, this.maxLo);
      }

   }
}
