package org.hibernate.id.enhanced;

import java.io.Serializable;
import java.util.Properties;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.cfg.ObjectNameNormalizer;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.BulkInsertionCapableIdentifierGenerator;
import org.hibernate.id.Configurable;
import org.hibernate.id.PersistentIdentifierGenerator;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.mapping.Table;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public class SequenceStyleGenerator implements PersistentIdentifierGenerator, BulkInsertionCapableIdentifierGenerator, Configurable {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, SequenceStyleGenerator.class.getName());
   public static final String SEQUENCE_PARAM = "sequence_name";
   public static final String DEF_SEQUENCE_NAME = "hibernate_sequence";
   public static final String INITIAL_PARAM = "initial_value";
   public static final int DEFAULT_INITIAL_VALUE = 1;
   public static final String INCREMENT_PARAM = "increment_size";
   public static final int DEFAULT_INCREMENT_SIZE = 1;
   public static final String OPT_PARAM = "optimizer";
   public static final String FORCE_TBL_PARAM = "force_table_use";
   public static final String CONFIG_PREFER_SEQUENCE_PER_ENTITY = "prefer_sequence_per_entity";
   public static final String CONFIG_SEQUENCE_PER_ENTITY_SUFFIX = "sequence_per_entity_suffix";
   public static final String DEF_SEQUENCE_SUFFIX = "_SEQ";
   public static final String VALUE_COLUMN_PARAM = "value_column";
   public static final String DEF_VALUE_COLUMN = "next_val";
   private DatabaseStructure databaseStructure;
   private Optimizer optimizer;
   private Type identifierType;

   public DatabaseStructure getDatabaseStructure() {
      return this.databaseStructure;
   }

   public Optimizer getOptimizer() {
      return this.optimizer;
   }

   public Type getIdentifierType() {
      return this.identifierType;
   }

   public void configure(Type type, Properties params, Dialect dialect) throws MappingException {
      this.identifierType = type;
      boolean forceTableUse = ConfigurationHelper.getBoolean("force_table_use", params, false);
      String sequenceName = this.determineSequenceName(params, dialect);
      int initialValue = this.determineInitialValue(params);
      int incrementSize = this.determineIncrementSize(params);
      String optimizationStrategy = this.determineOptimizationStrategy(params, incrementSize);
      incrementSize = this.determineAdjustedIncrementSize(optimizationStrategy, incrementSize);
      if (dialect.supportsSequences() && !forceTableUse && !dialect.supportsPooledSequences() && OptimizerFactory.isPooledOptimizer(optimizationStrategy)) {
         forceTableUse = true;
         LOG.forcingTableUse();
      }

      this.databaseStructure = this.buildDatabaseStructure(type, params, dialect, forceTableUse, sequenceName, initialValue, incrementSize);
      this.optimizer = OptimizerFactory.buildOptimizer(optimizationStrategy, this.identifierType.getReturnedClass(), incrementSize, (long)ConfigurationHelper.getInt("initial_value", params, -1));
      this.databaseStructure.prepare(this.optimizer);
   }

   protected String determineSequenceName(Properties params, Dialect dialect) {
      String sequencePerEntitySuffix = ConfigurationHelper.getString("sequence_per_entity_suffix", params, "_SEQ");
      String sequenceName = ConfigurationHelper.getBoolean("prefer_sequence_per_entity", params, false) ? params.getProperty("jpa_entity_name") + sequencePerEntitySuffix : "hibernate_sequence";
      ObjectNameNormalizer normalizer = (ObjectNameNormalizer)params.get("identifier_normalizer");
      sequenceName = ConfigurationHelper.getString("sequence_name", params, sequenceName);
      if (sequenceName.indexOf(46) < 0) {
         sequenceName = normalizer.normalizeIdentifierQuoting(sequenceName);
         String schemaName = params.getProperty("schema");
         String catalogName = params.getProperty("catalog");
         sequenceName = Table.qualify(dialect.quote(catalogName), dialect.quote(schemaName), dialect.quote(sequenceName));
      }

      return sequenceName;
   }

   protected String determineValueColumnName(Properties params, Dialect dialect) {
      ObjectNameNormalizer normalizer = (ObjectNameNormalizer)params.get("identifier_normalizer");
      String name = ConfigurationHelper.getString("value_column", params, "next_val");
      return dialect.quote(normalizer.normalizeIdentifierQuoting(name));
   }

   protected int determineInitialValue(Properties params) {
      return ConfigurationHelper.getInt("initial_value", params, 1);
   }

   protected int determineIncrementSize(Properties params) {
      return ConfigurationHelper.getInt("increment_size", params, 1);
   }

   protected String determineOptimizationStrategy(Properties params, int incrementSize) {
      String defaultPooledOptimizerStrategy = ConfigurationHelper.getBoolean("hibernate.id.optimizer.pooled.prefer_lo", params, false) ? OptimizerFactory.StandardOptimizerDescriptor.POOLED_LO.getExternalName() : OptimizerFactory.StandardOptimizerDescriptor.POOLED.getExternalName();
      String defaultOptimizerStrategy = incrementSize <= 1 ? OptimizerFactory.StandardOptimizerDescriptor.NONE.getExternalName() : defaultPooledOptimizerStrategy;
      return ConfigurationHelper.getString("optimizer", params, defaultOptimizerStrategy);
   }

   protected int determineAdjustedIncrementSize(String optimizationStrategy, int incrementSize) {
      if (incrementSize > 1 && OptimizerFactory.StandardOptimizerDescriptor.NONE.getExternalName().equals(optimizationStrategy)) {
         LOG.honoringOptimizerSetting(OptimizerFactory.StandardOptimizerDescriptor.NONE.getExternalName(), "increment_size", incrementSize);
         incrementSize = 1;
      }

      return incrementSize;
   }

   protected DatabaseStructure buildDatabaseStructure(Type type, Properties params, Dialect dialect, boolean forceTableUse, String sequenceName, int initialValue, int incrementSize) {
      boolean useSequence = dialect.supportsSequences() && !forceTableUse;
      if (useSequence) {
         return new SequenceStructure(dialect, sequenceName, initialValue, incrementSize, type.getReturnedClass());
      } else {
         String valueColumnName = this.determineValueColumnName(params, dialect);
         return new TableStructure(dialect, sequenceName, valueColumnName, initialValue, incrementSize, type.getReturnedClass());
      }
   }

   public Serializable generate(SessionImplementor session, Object object) throws HibernateException {
      return this.optimizer.generate(this.databaseStructure.buildCallback(session));
   }

   public Object generatorKey() {
      return this.databaseStructure.getName();
   }

   public String[] sqlCreateStrings(Dialect dialect) throws HibernateException {
      return this.databaseStructure.sqlCreateStrings(dialect);
   }

   public String[] sqlDropStrings(Dialect dialect) throws HibernateException {
      return this.databaseStructure.sqlDropStrings(dialect);
   }

   public boolean supportsBulkInsertionIdentifierGeneration() {
      return OptimizerFactory.NoopOptimizer.class.isInstance(this.getOptimizer()) && this.getDatabaseStructure().isPhysicalSequence();
   }

   public String determineBulkInsertionIdentifierGenerationSelectFragment(Dialect dialect) {
      return dialect.getSelectSequenceNextValString(this.getDatabaseStructure().getName());
   }
}
