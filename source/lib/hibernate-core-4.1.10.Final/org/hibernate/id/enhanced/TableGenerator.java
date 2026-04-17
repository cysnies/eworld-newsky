package org.hibernate.id.enhanced;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.MappingException;
import org.hibernate.cfg.ObjectNameNormalizer;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.jdbc.spi.SqlStatementLogger;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGeneratorHelper;
import org.hibernate.id.IntegralDataTypeHolder;
import org.hibernate.id.PersistentIdentifierGenerator;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.jdbc.AbstractReturningWork;
import org.hibernate.mapping.Table;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public class TableGenerator implements PersistentIdentifierGenerator, Configurable {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, TableGenerator.class.getName());
   public static final String CONFIG_PREFER_SEGMENT_PER_ENTITY = "prefer_entity_table_as_segment_value";
   public static final String TABLE_PARAM = "table_name";
   public static final String DEF_TABLE = "hibernate_sequences";
   public static final String VALUE_COLUMN_PARAM = "value_column_name";
   public static final String DEF_VALUE_COLUMN = "next_val";
   public static final String SEGMENT_COLUMN_PARAM = "segment_column_name";
   public static final String DEF_SEGMENT_COLUMN = "sequence_name";
   public static final String SEGMENT_VALUE_PARAM = "segment_value";
   public static final String DEF_SEGMENT_VALUE = "default";
   public static final String SEGMENT_LENGTH_PARAM = "segment_value_length";
   public static final int DEF_SEGMENT_LENGTH = 255;
   public static final String INITIAL_PARAM = "initial_value";
   public static final int DEFAULT_INITIAL_VALUE = 1;
   public static final String INCREMENT_PARAM = "increment_size";
   public static final int DEFAULT_INCREMENT_SIZE = 1;
   public static final String OPT_PARAM = "optimizer";
   private Type identifierType;
   private String tableName;
   private String segmentColumnName;
   private String segmentValue;
   private int segmentValueLength;
   private String valueColumnName;
   private int initialValue;
   private int incrementSize;
   private String selectQuery;
   private String insertQuery;
   private String updateQuery;
   private Optimizer optimizer;
   private long accessCount = 0L;

   public Object generatorKey() {
      return this.tableName;
   }

   public final Type getIdentifierType() {
      return this.identifierType;
   }

   public final String getTableName() {
      return this.tableName;
   }

   public final String getSegmentColumnName() {
      return this.segmentColumnName;
   }

   public final String getSegmentValue() {
      return this.segmentValue;
   }

   public final int getSegmentValueLength() {
      return this.segmentValueLength;
   }

   public final String getValueColumnName() {
      return this.valueColumnName;
   }

   public final int getInitialValue() {
      return this.initialValue;
   }

   public final int getIncrementSize() {
      return this.incrementSize;
   }

   public final Optimizer getOptimizer() {
      return this.optimizer;
   }

   public final long getTableAccessCount() {
      return this.accessCount;
   }

   public void configure(Type type, Properties params, Dialect dialect) throws MappingException {
      this.identifierType = type;
      this.tableName = this.determineGeneratorTableName(params, dialect);
      this.segmentColumnName = this.determineSegmentColumnName(params, dialect);
      this.valueColumnName = this.determineValueColumnName(params, dialect);
      this.segmentValue = this.determineSegmentValue(params);
      this.segmentValueLength = this.determineSegmentColumnSize(params);
      this.initialValue = this.determineInitialValue(params);
      this.incrementSize = this.determineIncrementSize(params);
      this.selectQuery = this.buildSelectQuery(dialect);
      this.updateQuery = this.buildUpdateQuery();
      this.insertQuery = this.buildInsertQuery();
      String defaultPooledOptimizerStrategy = ConfigurationHelper.getBoolean("hibernate.id.optimizer.pooled.prefer_lo", params, false) ? OptimizerFactory.StandardOptimizerDescriptor.POOLED_LO.getExternalName() : OptimizerFactory.StandardOptimizerDescriptor.POOLED.getExternalName();
      String defaultOptimizerStrategy = this.incrementSize <= 1 ? OptimizerFactory.StandardOptimizerDescriptor.NONE.getExternalName() : defaultPooledOptimizerStrategy;
      String optimizationStrategy = ConfigurationHelper.getString("optimizer", params, defaultOptimizerStrategy);
      this.optimizer = OptimizerFactory.buildOptimizer(optimizationStrategy, this.identifierType.getReturnedClass(), this.incrementSize, (long)ConfigurationHelper.getInt("initial_value", params, -1));
   }

   protected String determineGeneratorTableName(Properties params, Dialect dialect) {
      String name = ConfigurationHelper.getString("table_name", params, "hibernate_sequences");
      boolean isGivenNameUnqualified = name.indexOf(46) < 0;
      if (isGivenNameUnqualified) {
         ObjectNameNormalizer normalizer = (ObjectNameNormalizer)params.get("identifier_normalizer");
         name = normalizer.normalizeIdentifierQuoting(name);
         String schemaName = normalizer.normalizeIdentifierQuoting(params.getProperty("schema"));
         String catalogName = normalizer.normalizeIdentifierQuoting(params.getProperty("catalog"));
         name = Table.qualify(dialect.quote(catalogName), dialect.quote(schemaName), dialect.quote(name));
      }

      return name;
   }

   protected String determineSegmentColumnName(Properties params, Dialect dialect) {
      ObjectNameNormalizer normalizer = (ObjectNameNormalizer)params.get("identifier_normalizer");
      String name = ConfigurationHelper.getString("segment_column_name", params, "sequence_name");
      return dialect.quote(normalizer.normalizeIdentifierQuoting(name));
   }

   protected String determineValueColumnName(Properties params, Dialect dialect) {
      ObjectNameNormalizer normalizer = (ObjectNameNormalizer)params.get("identifier_normalizer");
      String name = ConfigurationHelper.getString("value_column_name", params, "next_val");
      return dialect.quote(normalizer.normalizeIdentifierQuoting(name));
   }

   protected String determineSegmentValue(Properties params) {
      String segmentValue = params.getProperty("segment_value");
      if (StringHelper.isEmpty(segmentValue)) {
         segmentValue = this.determineDefaultSegmentValue(params);
      }

      return segmentValue;
   }

   protected String determineDefaultSegmentValue(Properties params) {
      boolean preferSegmentPerEntity = ConfigurationHelper.getBoolean("prefer_entity_table_as_segment_value", params, false);
      String defaultToUse = preferSegmentPerEntity ? params.getProperty("target_table") : "default";
      LOG.usingDefaultIdGeneratorSegmentValue(this.tableName, this.segmentColumnName, defaultToUse);
      return defaultToUse;
   }

   protected int determineSegmentColumnSize(Properties params) {
      return ConfigurationHelper.getInt("segment_value_length", params, 255);
   }

   protected int determineInitialValue(Properties params) {
      return ConfigurationHelper.getInt("initial_value", params, 1);
   }

   protected int determineIncrementSize(Properties params) {
      return ConfigurationHelper.getInt("increment_size", params, 1);
   }

   protected String buildSelectQuery(Dialect dialect) {
      String alias = "tbl";
      String query = "select " + StringHelper.qualify("tbl", this.valueColumnName) + " from " + this.tableName + ' ' + "tbl" + " where " + StringHelper.qualify("tbl", this.segmentColumnName) + "=?";
      LockOptions lockOptions = new LockOptions(LockMode.PESSIMISTIC_WRITE);
      lockOptions.setAliasSpecificLockMode("tbl", LockMode.PESSIMISTIC_WRITE);
      Map updateTargetColumnsMap = Collections.singletonMap("tbl", new String[]{this.valueColumnName});
      return dialect.applyLocksToSql(query, lockOptions, updateTargetColumnsMap);
   }

   protected String buildUpdateQuery() {
      return "update " + this.tableName + " set " + this.valueColumnName + "=? " + " where " + this.valueColumnName + "=? and " + this.segmentColumnName + "=?";
   }

   protected String buildInsertQuery() {
      return "insert into " + this.tableName + " (" + this.segmentColumnName + ", " + this.valueColumnName + ") " + " values (?,?)";
   }

   public synchronized Serializable generate(final SessionImplementor session, Object obj) {
      final SqlStatementLogger statementLogger = ((JdbcServices)session.getFactory().getServiceRegistry().getService(JdbcServices.class)).getSqlStatementLogger();
      return this.optimizer.generate(new AccessCallback() {
         public IntegralDataTypeHolder getNextValue() {
            return (IntegralDataTypeHolder)session.getTransactionCoordinator().getTransaction().createIsolationDelegate().delegateWork(new AbstractReturningWork() {
               public IntegralDataTypeHolder execute(Connection connection) throws SQLException {
                  IntegralDataTypeHolder value = IdentifierGeneratorHelper.getIntegralDataTypeHolder(TableGenerator.this.identifierType.getReturnedClass());

                  int rows;
                  do {
                     statementLogger.logStatement(TableGenerator.this.selectQuery, FormatStyle.BASIC.getFormatter());
                     PreparedStatement selectPS = connection.prepareStatement(TableGenerator.this.selectQuery);

                     try {
                        selectPS.setString(1, TableGenerator.this.segmentValue);
                        ResultSet selectRS = selectPS.executeQuery();
                        if (!selectRS.next()) {
                           value.initialize((long)TableGenerator.this.initialValue);
                           PreparedStatement insertPS = null;

                           try {
                              statementLogger.logStatement(TableGenerator.this.insertQuery, FormatStyle.BASIC.getFormatter());
                              insertPS = connection.prepareStatement(TableGenerator.this.insertQuery);
                              insertPS.setString(1, TableGenerator.this.segmentValue);
                              value.bind(insertPS, 2);
                              insertPS.execute();
                           } finally {
                              if (insertPS != null) {
                                 insertPS.close();
                              }

                           }
                        } else {
                           value.initialize(selectRS, 1L);
                        }

                        selectRS.close();
                     } catch (SQLException e) {
                        TableGenerator.LOG.unableToReadOrInitHiValue(e);
                        throw e;
                     } finally {
                        selectPS.close();
                     }

                     statementLogger.logStatement(TableGenerator.this.updateQuery, FormatStyle.BASIC.getFormatter());
                     PreparedStatement e = connection.prepareStatement(TableGenerator.this.updateQuery);

                     try {
                        IntegralDataTypeHolder updateValue = value.copy();
                        if (TableGenerator.this.optimizer.applyIncrementSizeToSourceValues()) {
                           updateValue.add((long)TableGenerator.this.incrementSize);
                        } else {
                           updateValue.increment();
                        }

                        updateValue.bind(e, 1);
                        value.bind(e, 2);
                        e.setString(3, TableGenerator.this.segmentValue);
                        rows = e.executeUpdate();
                     } catch (SQLException e) {
                        TableGenerator.LOG.unableToUpdateQueryHiValue(TableGenerator.this.tableName, e);
                        throw e;
                     } finally {
                        e.close();
                     }
                  } while(rows == false);

                  TableGenerator.this.accessCount++;
                  return value;
               }
            }, true);
         }
      });
   }

   public String[] sqlCreateStrings(Dialect dialect) throws HibernateException {
      return new String[]{dialect.getCreateTableString() + ' ' + this.tableName + " ( " + this.segmentColumnName + ' ' + dialect.getTypeName(12, (long)this.segmentValueLength, 0, 0) + " not null " + ",  " + this.valueColumnName + ' ' + dialect.getTypeName(-5) + ", primary key ( " + this.segmentColumnName + " ) ) "};
   }

   public String[] sqlDropStrings(Dialect dialect) throws HibernateException {
      return new String[]{dialect.getDropTableString(this.tableName)};
   }
}
