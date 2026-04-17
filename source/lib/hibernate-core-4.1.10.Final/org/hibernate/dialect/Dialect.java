package org.hibernate.dialect;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.MappingException;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.function.CastFunction;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.dialect.function.StandardAnsiSqlAggregationFunctions;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.dialect.lock.LockingStrategy;
import org.hibernate.dialect.lock.OptimisticForceIncrementLockingStrategy;
import org.hibernate.dialect.lock.OptimisticLockingStrategy;
import org.hibernate.dialect.lock.PessimisticForceIncrementLockingStrategy;
import org.hibernate.dialect.lock.PessimisticReadSelectLockingStrategy;
import org.hibernate.dialect.lock.PessimisticWriteSelectLockingStrategy;
import org.hibernate.dialect.lock.SelectLockingStrategy;
import org.hibernate.dialect.pagination.LegacyLimitHandler;
import org.hibernate.dialect.pagination.LimitHandler;
import org.hibernate.dialect.unique.DefaultUniqueDelegate;
import org.hibernate.dialect.unique.UniqueDelegate;
import org.hibernate.engine.jdbc.LobCreator;
import org.hibernate.engine.spi.RowSelection;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.exception.spi.ConversionContext;
import org.hibernate.exception.spi.SQLExceptionConversionDelegate;
import org.hibernate.exception.spi.SQLExceptionConverter;
import org.hibernate.exception.spi.ViolatedConstraintNameExtracter;
import org.hibernate.id.IdentityGenerator;
import org.hibernate.id.SequenceGenerator;
import org.hibernate.id.TableHiLoGenerator;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.internal.util.io.StreamCopier;
import org.hibernate.persister.entity.Lockable;
import org.hibernate.sql.ANSICaseFragment;
import org.hibernate.sql.ANSIJoinFragment;
import org.hibernate.sql.CaseFragment;
import org.hibernate.sql.ForUpdateFragment;
import org.hibernate.sql.JoinFragment;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.descriptor.sql.ClobTypeDescriptor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;
import org.jboss.logging.Logger;

public abstract class Dialect implements ConversionContext {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, Dialect.class.getName());
   public static final String DEFAULT_BATCH_SIZE = "15";
   public static final String NO_BATCH = "0";
   public static final String QUOTE = "`\"[";
   public static final String CLOSED_QUOTE = "`\"]";
   private final TypeNames typeNames = new TypeNames();
   private final TypeNames hibernateTypeNames = new TypeNames();
   private final Properties properties = new Properties();
   private final Map sqlFunctions = new HashMap();
   private final Set sqlKeywords = new HashSet();
   private final UniqueDelegate uniqueDelegate;
   protected static final LobMergeStrategy LEGACY_LOB_MERGE_STRATEGY = new LobMergeStrategy() {
      public Blob mergeBlob(Blob original, Blob target, SessionImplementor session) {
         return target;
      }

      public Clob mergeClob(Clob original, Clob target, SessionImplementor session) {
         return target;
      }

      public NClob mergeNClob(NClob original, NClob target, SessionImplementor session) {
         return target;
      }
   };
   protected static final LobMergeStrategy STREAM_XFER_LOB_MERGE_STRATEGY = new LobMergeStrategy() {
      public Blob mergeBlob(Blob original, Blob target, SessionImplementor session) {
         if (original != target) {
            try {
               OutputStream connectedStream = target.setBinaryStream(1L);
               InputStream detachedStream = original.getBinaryStream();
               StreamCopier.copy(detachedStream, connectedStream);
               return target;
            } catch (SQLException e) {
               throw session.getFactory().getSQLExceptionHelper().convert(e, "unable to merge BLOB data");
            }
         } else {
            return Dialect.NEW_LOCATOR_LOB_MERGE_STRATEGY.mergeBlob(original, target, session);
         }
      }

      public Clob mergeClob(Clob original, Clob target, SessionImplementor session) {
         if (original != target) {
            try {
               OutputStream connectedStream = target.setAsciiStream(1L);
               InputStream detachedStream = original.getAsciiStream();
               StreamCopier.copy(detachedStream, connectedStream);
               return target;
            } catch (SQLException e) {
               throw session.getFactory().getSQLExceptionHelper().convert(e, "unable to merge CLOB data");
            }
         } else {
            return Dialect.NEW_LOCATOR_LOB_MERGE_STRATEGY.mergeClob(original, target, session);
         }
      }

      public NClob mergeNClob(NClob original, NClob target, SessionImplementor session) {
         if (original != target) {
            try {
               OutputStream connectedStream = target.setAsciiStream(1L);
               InputStream detachedStream = original.getAsciiStream();
               StreamCopier.copy(detachedStream, connectedStream);
               return target;
            } catch (SQLException e) {
               throw session.getFactory().getSQLExceptionHelper().convert(e, "unable to merge NCLOB data");
            }
         } else {
            return Dialect.NEW_LOCATOR_LOB_MERGE_STRATEGY.mergeNClob(original, target, session);
         }
      }
   };
   protected static final LobMergeStrategy NEW_LOCATOR_LOB_MERGE_STRATEGY = new LobMergeStrategy() {
      public Blob mergeBlob(Blob original, Blob target, SessionImplementor session) {
         if (original == null && target == null) {
            return null;
         } else {
            try {
               LobCreator lobCreator = session.getFactory().getJdbcServices().getLobCreator(session);
               return original == null ? lobCreator.createBlob(ArrayHelper.EMPTY_BYTE_ARRAY) : lobCreator.createBlob(original.getBinaryStream(), original.length());
            } catch (SQLException e) {
               throw session.getFactory().getSQLExceptionHelper().convert(e, "unable to merge BLOB data");
            }
         }
      }

      public Clob mergeClob(Clob original, Clob target, SessionImplementor session) {
         if (original == null && target == null) {
            return null;
         } else {
            try {
               LobCreator lobCreator = session.getFactory().getJdbcServices().getLobCreator(session);
               return original == null ? lobCreator.createClob("") : lobCreator.createClob(original.getCharacterStream(), original.length());
            } catch (SQLException e) {
               throw session.getFactory().getSQLExceptionHelper().convert(e, "unable to merge CLOB data");
            }
         }
      }

      public NClob mergeNClob(NClob original, NClob target, SessionImplementor session) {
         if (original == null && target == null) {
            return null;
         } else {
            try {
               LobCreator lobCreator = session.getFactory().getJdbcServices().getLobCreator(session);
               return original == null ? lobCreator.createNClob("") : lobCreator.createNClob(original.getCharacterStream(), original.length());
            } catch (SQLException e) {
               throw session.getFactory().getSQLExceptionHelper().convert(e, "unable to merge NCLOB data");
            }
         }
      }
   };
   private static final ViolatedConstraintNameExtracter EXTRACTER = new ViolatedConstraintNameExtracter() {
      public String extractConstraintName(SQLException sqle) {
         return null;
      }
   };

   protected Dialect() {
      LOG.usingDialect(this);
      StandardAnsiSqlAggregationFunctions.primeFunctionMap(this.sqlFunctions);
      this.registerFunction("substring", new SQLFunctionTemplate(StandardBasicTypes.STRING, "substring(?1, ?2, ?3)"));
      this.registerFunction("locate", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "locate(?1, ?2, ?3)"));
      this.registerFunction("trim", new SQLFunctionTemplate(StandardBasicTypes.STRING, "trim(?1 ?2 ?3 ?4)"));
      this.registerFunction("length", new StandardSQLFunction("length", StandardBasicTypes.INTEGER));
      this.registerFunction("bit_length", new StandardSQLFunction("bit_length", StandardBasicTypes.INTEGER));
      this.registerFunction("coalesce", new StandardSQLFunction("coalesce"));
      this.registerFunction("nullif", new StandardSQLFunction("nullif"));
      this.registerFunction("abs", new StandardSQLFunction("abs"));
      this.registerFunction("mod", new StandardSQLFunction("mod", StandardBasicTypes.INTEGER));
      this.registerFunction("sqrt", new StandardSQLFunction("sqrt", StandardBasicTypes.DOUBLE));
      this.registerFunction("upper", new StandardSQLFunction("upper"));
      this.registerFunction("lower", new StandardSQLFunction("lower"));
      this.registerFunction("cast", new CastFunction());
      this.registerFunction("extract", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "extract(?1 ?2 ?3)"));
      this.registerFunction("second", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "extract(second from ?1)"));
      this.registerFunction("minute", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "extract(minute from ?1)"));
      this.registerFunction("hour", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "extract(hour from ?1)"));
      this.registerFunction("day", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "extract(day from ?1)"));
      this.registerFunction("month", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "extract(month from ?1)"));
      this.registerFunction("year", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "extract(year from ?1)"));
      this.registerFunction("str", new SQLFunctionTemplate(StandardBasicTypes.STRING, "cast(?1 as char)"));
      this.registerColumnType(-7, "bit");
      this.registerColumnType(16, "boolean");
      this.registerColumnType(-6, "tinyint");
      this.registerColumnType(5, "smallint");
      this.registerColumnType(4, "integer");
      this.registerColumnType(-5, "bigint");
      this.registerColumnType(6, "float($p)");
      this.registerColumnType(8, "double precision");
      this.registerColumnType(2, "numeric($p,$s)");
      this.registerColumnType(7, "real");
      this.registerColumnType(91, "date");
      this.registerColumnType(92, "time");
      this.registerColumnType(93, "timestamp");
      this.registerColumnType(-3, "bit varying($l)");
      this.registerColumnType(-4, "bit varying($l)");
      this.registerColumnType(2004, "blob");
      this.registerColumnType(1, "char($l)");
      this.registerColumnType(12, "varchar($l)");
      this.registerColumnType(-1, "varchar($l)");
      this.registerColumnType(2005, "clob");
      this.registerColumnType(-15, "nchar($l)");
      this.registerColumnType(-9, "nvarchar($l)");
      this.registerColumnType(-16, "nvarchar($l)");
      this.registerColumnType(2011, "nclob");
      this.registerHibernateType(-5, StandardBasicTypes.BIG_INTEGER.getName());
      this.registerHibernateType(-2, StandardBasicTypes.BINARY.getName());
      this.registerHibernateType(-7, StandardBasicTypes.BOOLEAN.getName());
      this.registerHibernateType(16, StandardBasicTypes.BOOLEAN.getName());
      this.registerHibernateType(1, StandardBasicTypes.CHARACTER.getName());
      this.registerHibernateType(1, 1L, StandardBasicTypes.CHARACTER.getName());
      this.registerHibernateType(1, 255L, StandardBasicTypes.STRING.getName());
      this.registerHibernateType(91, StandardBasicTypes.DATE.getName());
      this.registerHibernateType(8, StandardBasicTypes.DOUBLE.getName());
      this.registerHibernateType(6, StandardBasicTypes.FLOAT.getName());
      this.registerHibernateType(4, StandardBasicTypes.INTEGER.getName());
      this.registerHibernateType(5, StandardBasicTypes.SHORT.getName());
      this.registerHibernateType(-6, StandardBasicTypes.BYTE.getName());
      this.registerHibernateType(92, StandardBasicTypes.TIME.getName());
      this.registerHibernateType(93, StandardBasicTypes.TIMESTAMP.getName());
      this.registerHibernateType(12, StandardBasicTypes.STRING.getName());
      this.registerHibernateType(-3, StandardBasicTypes.BINARY.getName());
      this.registerHibernateType(-1, StandardBasicTypes.TEXT.getName());
      this.registerHibernateType(-4, StandardBasicTypes.IMAGE.getName());
      this.registerHibernateType(2, StandardBasicTypes.BIG_DECIMAL.getName());
      this.registerHibernateType(3, StandardBasicTypes.BIG_DECIMAL.getName());
      this.registerHibernateType(2004, StandardBasicTypes.BLOB.getName());
      this.registerHibernateType(2005, StandardBasicTypes.CLOB.getName());
      this.registerHibernateType(7, StandardBasicTypes.FLOAT.getName());
      this.uniqueDelegate = new DefaultUniqueDelegate(this);
   }

   public static Dialect getDialect() throws HibernateException {
      String dialectName = Environment.getProperties().getProperty("hibernate.dialect");
      return instantiateDialect(dialectName);
   }

   public static Dialect getDialect(Properties props) throws HibernateException {
      String dialectName = props.getProperty("hibernate.dialect");
      return dialectName == null ? getDialect() : instantiateDialect(dialectName);
   }

   private static Dialect instantiateDialect(String dialectName) throws HibernateException {
      if (dialectName == null) {
         throw new HibernateException("The dialect was not set. Set the property hibernate.dialect.");
      } else {
         try {
            return (Dialect)ReflectHelper.classForName(dialectName).newInstance();
         } catch (ClassNotFoundException var2) {
            throw new HibernateException("Dialect class not found: " + dialectName);
         } catch (Exception e) {
            throw new HibernateException("Could not instantiate given dialect class: " + dialectName, e);
         }
      }
   }

   public final Properties getDefaultProperties() {
      return this.properties;
   }

   public String toString() {
      return this.getClass().getName();
   }

   public String getTypeName(int code) throws HibernateException {
      String result = this.typeNames.get(code);
      if (result == null) {
         throw new HibernateException("No default type mapping for (java.sql.Types) " + code);
      } else {
         return result;
      }
   }

   public String getTypeName(int code, long length, int precision, int scale) throws HibernateException {
      String result = this.typeNames.get(code, length, precision, scale);
      if (result == null) {
         throw new HibernateException(String.format("No type mapping for java.sql.Types code: %s, length: %s", code, length));
      } else {
         return result;
      }
   }

   public String getCastTypeName(int code) {
      return this.getTypeName(code, 255L, 19, 2);
   }

   public String cast(String value, int jdbcTypeCode, int length, int precision, int scale) {
      return jdbcTypeCode == 1 ? "cast(" + value + " as char(" + length + "))" : "cast(" + value + "as " + this.getTypeName(jdbcTypeCode, (long)length, precision, scale) + ")";
   }

   public String cast(String value, int jdbcTypeCode, int length) {
      return this.cast(value, jdbcTypeCode, length, 19, 2);
   }

   public String cast(String value, int jdbcTypeCode, int precision, int scale) {
      return this.cast(value, jdbcTypeCode, 255, precision, scale);
   }

   protected void registerColumnType(int code, long capacity, String name) {
      this.typeNames.put(code, capacity, name);
   }

   protected void registerColumnType(int code, String name) {
      this.typeNames.put(code, name);
   }

   public SqlTypeDescriptor remapSqlTypeDescriptor(SqlTypeDescriptor sqlTypeDescriptor) {
      if (sqlTypeDescriptor == null) {
         throw new IllegalArgumentException("sqlTypeDescriptor is null");
      } else if (!sqlTypeDescriptor.canBeRemapped()) {
         return sqlTypeDescriptor;
      } else {
         SqlTypeDescriptor overridden = this.getSqlTypeDescriptorOverride(sqlTypeDescriptor.getSqlType());
         return overridden == null ? sqlTypeDescriptor : overridden;
      }
   }

   protected SqlTypeDescriptor getSqlTypeDescriptorOverride(int sqlCode) {
      SqlTypeDescriptor descriptor;
      switch (sqlCode) {
         case 2005:
            descriptor = this.useInputStreamToInsertBlob() ? ClobTypeDescriptor.STREAM_BINDING : null;
            break;
         default:
            descriptor = null;
      }

      return descriptor;
   }

   public LobMergeStrategy getLobMergeStrategy() {
      return NEW_LOCATOR_LOB_MERGE_STRATEGY;
   }

   public String getHibernateTypeName(int code) throws HibernateException {
      String result = this.hibernateTypeNames.get(code);
      if (result == null) {
         throw new HibernateException("No Hibernate type mapping for java.sql.Types code: " + code);
      } else {
         return result;
      }
   }

   public String getHibernateTypeName(int code, int length, int precision, int scale) throws HibernateException {
      String result = this.hibernateTypeNames.get(code, (long)length, precision, scale);
      if (result == null) {
         throw new HibernateException("No Hibernate type mapping for java.sql.Types code: " + code + ", length: " + length);
      } else {
         return result;
      }
   }

   protected void registerHibernateType(int code, long capacity, String name) {
      this.hibernateTypeNames.put(code, capacity, name);
   }

   protected void registerHibernateType(int code, String name) {
      this.hibernateTypeNames.put(code, name);
   }

   protected void registerFunction(String name, SQLFunction function) {
      this.sqlFunctions.put(name.toLowerCase(), function);
   }

   public final Map getFunctions() {
      return this.sqlFunctions;
   }

   protected void registerKeyword(String word) {
      this.sqlKeywords.add(word);
   }

   public Set getKeywords() {
      return this.sqlKeywords;
   }

   public Class getNativeIdentifierGeneratorClass() {
      if (this.supportsIdentityColumns()) {
         return IdentityGenerator.class;
      } else {
         return this.supportsSequences() ? SequenceGenerator.class : TableHiLoGenerator.class;
      }
   }

   public boolean supportsIdentityColumns() {
      return false;
   }

   public boolean supportsInsertSelectIdentity() {
      return false;
   }

   public boolean hasDataTypeInIdentityColumn() {
      return true;
   }

   public String appendIdentitySelectToInsert(String insertString) {
      return insertString;
   }

   public String getIdentitySelectString(String table, String column, int type) throws MappingException {
      return this.getIdentitySelectString();
   }

   protected String getIdentitySelectString() throws MappingException {
      throw new MappingException(this.getClass().getName() + " does not support identity key generation");
   }

   public String getIdentityColumnString(int type) throws MappingException {
      return this.getIdentityColumnString();
   }

   protected String getIdentityColumnString() throws MappingException {
      throw new MappingException(this.getClass().getName() + " does not support identity key generation");
   }

   public String getIdentityInsertString() {
      return null;
   }

   public boolean supportsSequences() {
      return false;
   }

   public boolean supportsPooledSequences() {
      return false;
   }

   public String getSequenceNextValString(String sequenceName) throws MappingException {
      throw new MappingException(this.getClass().getName() + " does not support sequences");
   }

   public String getSelectSequenceNextValString(String sequenceName) throws MappingException {
      throw new MappingException(this.getClass().getName() + " does not support sequences");
   }

   /** @deprecated */
   @Deprecated
   public String[] getCreateSequenceStrings(String sequenceName) throws MappingException {
      return new String[]{this.getCreateSequenceString(sequenceName)};
   }

   public String[] getCreateSequenceStrings(String sequenceName, int initialValue, int incrementSize) throws MappingException {
      return new String[]{this.getCreateSequenceString(sequenceName, initialValue, incrementSize)};
   }

   protected String getCreateSequenceString(String sequenceName) throws MappingException {
      throw new MappingException(this.getClass().getName() + " does not support sequences");
   }

   protected String getCreateSequenceString(String sequenceName, int initialValue, int incrementSize) throws MappingException {
      if (this.supportsPooledSequences()) {
         return this.getCreateSequenceString(sequenceName) + " start with " + initialValue + " increment by " + incrementSize;
      } else {
         throw new MappingException(this.getClass().getName() + " does not support pooled sequences");
      }
   }

   public String[] getDropSequenceStrings(String sequenceName) throws MappingException {
      return new String[]{this.getDropSequenceString(sequenceName)};
   }

   protected String getDropSequenceString(String sequenceName) throws MappingException {
      throw new MappingException(this.getClass().getName() + " does not support sequences");
   }

   public String getQuerySequencesString() {
      return null;
   }

   public String getSelectGUIDString() {
      throw new UnsupportedOperationException(this.getClass().getName() + " does not support GUIDs");
   }

   /** @deprecated */
   @Deprecated
   public boolean supportsLimit() {
      return false;
   }

   /** @deprecated */
   @Deprecated
   public boolean supportsLimitOffset() {
      return this.supportsLimit();
   }

   /** @deprecated */
   @Deprecated
   public boolean supportsVariableLimit() {
      return this.supportsLimit();
   }

   /** @deprecated */
   @Deprecated
   public boolean bindLimitParametersInReverseOrder() {
      return false;
   }

   /** @deprecated */
   @Deprecated
   public boolean bindLimitParametersFirst() {
      return false;
   }

   /** @deprecated */
   @Deprecated
   public boolean useMaxForLimit() {
      return false;
   }

   /** @deprecated */
   @Deprecated
   public boolean forceLimitUsage() {
      return false;
   }

   /** @deprecated */
   @Deprecated
   public String getLimitString(String query, int offset, int limit) {
      return this.getLimitString(query, offset > 0 || this.forceLimitUsage());
   }

   /** @deprecated */
   @Deprecated
   protected String getLimitString(String query, boolean hasOffset) {
      throw new UnsupportedOperationException("Paged queries not supported by " + this.getClass().getName());
   }

   /** @deprecated */
   @Deprecated
   public int convertToFirstRowValue(int zeroBasedFirstResult) {
      return zeroBasedFirstResult;
   }

   public LimitHandler buildLimitHandler(String sql, RowSelection selection) {
      return new LegacyLimitHandler(this, sql, selection);
   }

   public boolean supportsLockTimeouts() {
      return true;
   }

   public boolean isLockTimeoutParameterized() {
      return false;
   }

   public LockingStrategy getLockingStrategy(Lockable lockable, LockMode lockMode) {
      switch (lockMode) {
         case PESSIMISTIC_FORCE_INCREMENT:
            return new PessimisticForceIncrementLockingStrategy(lockable, lockMode);
         case PESSIMISTIC_WRITE:
            return new PessimisticWriteSelectLockingStrategy(lockable, lockMode);
         case PESSIMISTIC_READ:
            return new PessimisticReadSelectLockingStrategy(lockable, lockMode);
         case OPTIMISTIC:
            return new OptimisticLockingStrategy(lockable, lockMode);
         case OPTIMISTIC_FORCE_INCREMENT:
            return new OptimisticForceIncrementLockingStrategy(lockable, lockMode);
         default:
            return new SelectLockingStrategy(lockable, lockMode);
      }
   }

   public String getForUpdateString(LockOptions lockOptions) {
      LockMode lockMode = lockOptions.getLockMode();
      return this.getForUpdateString(lockMode, lockOptions.getTimeOut());
   }

   private String getForUpdateString(LockMode lockMode, int timeout) {
      switch (lockMode) {
         case PESSIMISTIC_FORCE_INCREMENT:
         case UPGRADE_NOWAIT:
         case FORCE:
            return this.getForUpdateNowaitString();
         case PESSIMISTIC_WRITE:
            return this.getWriteLockString(timeout);
         case PESSIMISTIC_READ:
            return this.getReadLockString(timeout);
         case OPTIMISTIC:
         case OPTIMISTIC_FORCE_INCREMENT:
         default:
            return "";
         case UPGRADE:
            return this.getForUpdateString();
      }
   }

   public String getForUpdateString(LockMode lockMode) {
      return this.getForUpdateString(lockMode, -1);
   }

   public String getForUpdateString() {
      return " for update";
   }

   public String getWriteLockString(int timeout) {
      return this.getForUpdateString();
   }

   public String getReadLockString(int timeout) {
      return this.getForUpdateString();
   }

   public boolean forUpdateOfColumns() {
      return false;
   }

   public boolean supportsOuterJoinForUpdate() {
      return true;
   }

   public String getForUpdateString(String aliases) {
      return this.getForUpdateString();
   }

   public String getForUpdateString(String aliases, LockOptions lockOptions) {
      LockMode lockMode = lockOptions.getLockMode();
      Iterator<Map.Entry<String, LockMode>> itr = lockOptions.getAliasLockIterator();

      while(itr.hasNext()) {
         Map.Entry<String, LockMode> entry = (Map.Entry)itr.next();
         LockMode lm = (LockMode)entry.getValue();
         if (lm.greaterThan(lockMode)) {
            lockMode = lm;
         }
      }

      lockOptions.setLockMode(lockMode);
      return this.getForUpdateString(lockOptions);
   }

   public String getForUpdateNowaitString() {
      return this.getForUpdateString();
   }

   public String getForUpdateNowaitString(String aliases) {
      return this.getForUpdateString(aliases);
   }

   /** @deprecated */
   @Deprecated
   public String appendLockHint(LockMode mode, String tableName) {
      return this.appendLockHint(new LockOptions(mode), tableName);
   }

   public String appendLockHint(LockOptions lockOptions, String tableName) {
      return tableName;
   }

   public String applyLocksToSql(String sql, LockOptions aliasedLockOptions, Map keyColumnNames) {
      return sql + (new ForUpdateFragment(this, aliasedLockOptions, keyColumnNames)).toFragmentString();
   }

   public String getCreateTableString() {
      return "create table";
   }

   public String getCreateMultisetTableString() {
      return this.getCreateTableString();
   }

   public boolean supportsTemporaryTables() {
      return false;
   }

   public String generateTemporaryTableName(String baseTableName) {
      return "HT_" + baseTableName;
   }

   public String getCreateTemporaryTableString() {
      return "create table";
   }

   public String getCreateTemporaryTablePostfix() {
      return "";
   }

   public String getDropTemporaryTableString() {
      return "drop table";
   }

   public Boolean performTemporaryTableDDLInIsolation() {
      return null;
   }

   public boolean dropTemporaryTableAfterUse() {
      return true;
   }

   public int registerResultSetOutParameter(CallableStatement statement, int position) throws SQLException {
      throw new UnsupportedOperationException(this.getClass().getName() + " does not support resultsets via stored procedures");
   }

   public ResultSet getResultSet(CallableStatement statement) throws SQLException {
      throw new UnsupportedOperationException(this.getClass().getName() + " does not support resultsets via stored procedures");
   }

   public boolean supportsCurrentTimestampSelection() {
      return false;
   }

   public boolean isCurrentTimestampSelectStringCallable() {
      throw new UnsupportedOperationException("Database not known to define a current timestamp function");
   }

   public String getCurrentTimestampSelectString() {
      throw new UnsupportedOperationException("Database not known to define a current timestamp function");
   }

   public String getCurrentTimestampSQLFunctionName() {
      return "current_timestamp";
   }

   /** @deprecated */
   @Deprecated
   public SQLExceptionConverter buildSQLExceptionConverter() {
      return null;
   }

   public SQLExceptionConversionDelegate buildSQLExceptionConversionDelegate() {
      return null;
   }

   public ViolatedConstraintNameExtracter getViolatedConstraintNameExtracter() {
      return EXTRACTER;
   }

   public String getSelectClauseNullString(int sqlType) {
      return "null";
   }

   public boolean supportsUnionAll() {
      return false;
   }

   public JoinFragment createOuterJoinFragment() {
      return new ANSIJoinFragment();
   }

   public CaseFragment createCaseFragment() {
      return new ANSICaseFragment();
   }

   public String getNoColumnsInsertString() {
      return "values ( )";
   }

   public String getLowercaseFunction() {
      return "lower";
   }

   public String getCaseInsensitiveLike() {
      return "like";
   }

   public boolean supportsCaseInsensitiveLike() {
      return false;
   }

   public String transformSelectString(String select) {
      return select;
   }

   public int getMaxAliasLength() {
      return 10;
   }

   public String toBooleanValueString(boolean bool) {
      return bool ? "1" : "0";
   }

   public char openQuote() {
      return '"';
   }

   public char closeQuote() {
      return '"';
   }

   public final String quote(String name) {
      if (name == null) {
         return null;
      } else {
         return name.charAt(0) == '`' ? this.openQuote() + name.substring(1, name.length() - 1) + this.closeQuote() : name;
      }
   }

   public boolean hasAlterTable() {
      return true;
   }

   public boolean dropConstraints() {
      return true;
   }

   public boolean qualifyIndexName() {
      return true;
   }

   public String getAddColumnString() {
      throw new UnsupportedOperationException("No add column syntax supported by " + this.getClass().getName());
   }

   public String getDropForeignKeyString() {
      return " drop constraint ";
   }

   public String getTableTypeString() {
      return "";
   }

   public String getAddForeignKeyConstraintString(String constraintName, String[] foreignKey, String referencedTable, String[] primaryKey, boolean referencesPrimaryKey) {
      StringBuilder res = new StringBuilder(30);
      res.append(" add constraint ").append(constraintName).append(" foreign key (").append(StringHelper.join(", ", foreignKey)).append(") references ").append(referencedTable);
      if (!referencesPrimaryKey) {
         res.append(" (").append(StringHelper.join(", ", primaryKey)).append(')');
      }

      return res.toString();
   }

   public String getAddPrimaryKeyConstraintString(String constraintName) {
      return " add constraint " + constraintName + " primary key ";
   }

   public boolean hasSelfReferentialForeignKeyBug() {
      return false;
   }

   public String getNullColumnString() {
      return "";
   }

   public boolean supportsCommentOn() {
      return false;
   }

   public String getTableComment(String comment) {
      return "";
   }

   public String getColumnComment(String comment) {
      return "";
   }

   public boolean supportsIfExistsBeforeTableName() {
      return false;
   }

   public boolean supportsIfExistsAfterTableName() {
      return false;
   }

   public String getDropTableString(String tableName) {
      StringBuilder buf = new StringBuilder("drop table ");
      if (this.supportsIfExistsBeforeTableName()) {
         buf.append("if exists ");
      }

      buf.append(tableName).append(this.getCascadeConstraintsString());
      if (this.supportsIfExistsAfterTableName()) {
         buf.append(" if exists");
      }

      return buf.toString();
   }

   public boolean supportsColumnCheck() {
      return true;
   }

   public boolean supportsTableCheck() {
      return true;
   }

   public boolean supportsCascadeDelete() {
      return true;
   }

   public String getCascadeConstraintsString() {
      return "";
   }

   public String getCrossJoinSeparator() {
      return " cross join ";
   }

   public ColumnAliasExtractor getColumnAliasExtractor() {
      return ColumnAliasExtractor.COLUMN_LABEL_EXTRACTOR;
   }

   public boolean supportsEmptyInList() {
      return true;
   }

   public boolean areStringComparisonsCaseInsensitive() {
      return false;
   }

   public boolean supportsRowValueConstructorSyntax() {
      return false;
   }

   public boolean supportsRowValueConstructorSyntaxInInList() {
      return false;
   }

   public boolean useInputStreamToInsertBlob() {
      return true;
   }

   public boolean supportsParametersInInsertSelect() {
      return true;
   }

   public boolean replaceResultVariableInOrderByClauseWithPosition() {
      return false;
   }

   public boolean requiresCastingOfParametersInSelectClause() {
      return false;
   }

   public boolean supportsResultSetPositionQueryMethodsOnForwardOnlyCursor() {
      return true;
   }

   public boolean supportsCircularCascadeDeleteConstraints() {
      return true;
   }

   public boolean supportsSubselectAsInPredicateLHS() {
      return true;
   }

   public boolean supportsExpectedLobUsagePattern() {
      return true;
   }

   public boolean supportsLobValueChangePropogation() {
      return true;
   }

   public boolean supportsUnboundedLobLocatorMaterialization() {
      return true;
   }

   public boolean supportsSubqueryOnMutatingTable() {
      return true;
   }

   public boolean supportsExistsInSelect() {
      return true;
   }

   public boolean doesReadCommittedCauseWritersToBlockReaders() {
      return false;
   }

   public boolean doesRepeatableReadCauseReadersToBlockWriters() {
      return false;
   }

   public boolean supportsBindAsCallableArgument() {
      return true;
   }

   public boolean supportsTupleCounts() {
      return false;
   }

   public boolean supportsTupleDistinctCounts() {
      return true;
   }

   public int getInExpressionCountLimit() {
      return 0;
   }

   public boolean forceLobAsLastValue() {
      return false;
   }

   public boolean useFollowOnLocking() {
      return false;
   }

   public UniqueDelegate getUniqueDelegate() {
      return this.uniqueDelegate;
   }

   public String getNotExpression(String expression) {
      return "not " + expression;
   }

   /** @deprecated */
   @Deprecated
   public boolean supportsUnique() {
      return true;
   }

   /** @deprecated */
   @Deprecated
   public boolean supportsUniqueConstraintInCreateAlterTable() {
      return true;
   }

   /** @deprecated */
   @Deprecated
   public String getAddUniqueConstraintString(String constraintName) {
      return " add constraint " + constraintName + " unique ";
   }

   /** @deprecated */
   @Deprecated
   public boolean supportsNotNullUnique() {
      return true;
   }
}
