package org.hibernate.engine.jdbc.internal;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.hibernate.MultiTenancyStrategy;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.LobCreationContext;
import org.hibernate.engine.jdbc.LobCreator;
import org.hibernate.engine.jdbc.spi.ExtractedDatabaseMetaData;
import org.hibernate.engine.jdbc.spi.JdbcConnectionAccess;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.jdbc.spi.ResultSetWrapper;
import org.hibernate.engine.jdbc.spi.SchemaNameResolver;
import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;
import org.hibernate.engine.jdbc.spi.SqlStatementLogger;
import org.hibernate.exception.internal.SQLExceptionTypeDelegate;
import org.hibernate.exception.internal.SQLStateConversionDelegate;
import org.hibernate.exception.internal.StandardSQLExceptionConverter;
import org.hibernate.exception.spi.SQLExceptionConverter;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.service.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.hibernate.service.jdbc.dialect.spi.DialectFactory;
import org.hibernate.service.spi.Configurable;
import org.hibernate.service.spi.ServiceRegistryAwareService;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.jboss.logging.Logger;

public class JdbcServicesImpl implements JdbcServices, ServiceRegistryAwareService, Configurable {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, JdbcServicesImpl.class.getName());
   private ServiceRegistryImplementor serviceRegistry;
   private Dialect dialect;
   private ConnectionProvider connectionProvider;
   private SqlStatementLogger sqlStatementLogger;
   private SqlExceptionHelper sqlExceptionHelper;
   private ExtractedDatabaseMetaData extractedMetaDataSupport;
   private LobCreatorBuilder lobCreatorBuilder;
   public static final String SCHEMA_NAME_RESOLVER = "hibernate.schema_name_resolver";

   public void injectServices(ServiceRegistryImplementor serviceRegistry) {
      this.serviceRegistry = serviceRegistry;
   }

   public void configure(Map configValues) {
      JdbcConnectionAccess jdbcConnectionAccess = this.buildJdbcConnectionAccess(configValues);
      DialectFactory dialectFactory = (DialectFactory)this.serviceRegistry.getService(DialectFactory.class);
      Dialect dialect = null;
      LobCreatorBuilder lobCreatorBuilder = null;
      boolean metaSupportsScrollable = false;
      boolean metaSupportsGetGeneratedKeys = false;
      boolean metaSupportsBatchUpdates = false;
      boolean metaReportsDDLCausesTxnCommit = false;
      boolean metaReportsDDLInTxnSupported = true;
      String extraKeywordsString = "";
      int sqlStateType = -1;
      boolean lobLocatorUpdateCopy = false;
      String catalogName = null;
      String schemaName = null;
      LinkedHashSet<TypeInfo> typeInfoSet = new LinkedHashSet();
      boolean useJdbcMetadata = ConfigurationHelper.getBoolean("hibernate.temp.use_jdbc_metadata_defaults", configValues, true);
      if (useJdbcMetadata) {
         try {
            Connection connection = jdbcConnectionAccess.obtainConnection();

            try {
               DatabaseMetaData meta = connection.getMetaData();
               if (LOG.isDebugEnabled()) {
                  LOG.debugf("Database ->\n       name : %s\n    version : %s\n      major : %s\n      minor : %s", new Object[]{meta.getDatabaseProductName(), meta.getDatabaseProductVersion(), meta.getDatabaseMajorVersion(), meta.getDatabaseMinorVersion()});
                  LOG.debugf("Driver ->\n       name : %s\n    version : %s\n      major : %s\n      minor : %s", new Object[]{meta.getDriverName(), meta.getDriverVersion(), meta.getDriverMajorVersion(), meta.getDriverMinorVersion()});
                  LOG.debugf("JDBC version : %s.%s", meta.getJDBCMajorVersion(), meta.getJDBCMinorVersion());
               }

               metaSupportsScrollable = meta.supportsResultSetType(1004);
               metaSupportsBatchUpdates = meta.supportsBatchUpdates();
               metaReportsDDLCausesTxnCommit = meta.dataDefinitionCausesTransactionCommit();
               metaReportsDDLInTxnSupported = !meta.dataDefinitionIgnoredInTransactions();
               metaSupportsGetGeneratedKeys = meta.supportsGetGeneratedKeys();
               extraKeywordsString = meta.getSQLKeywords();
               sqlStateType = meta.getSQLStateType();
               lobLocatorUpdateCopy = meta.locatorsUpdateCopy();
               typeInfoSet.addAll(TypeInfoExtracter.extractTypeInfo(meta));
               dialect = dialectFactory.buildDialect(configValues, connection);
               catalogName = connection.getCatalog();
               SchemaNameResolver schemaNameResolver = this.determineExplicitSchemaNameResolver(configValues);
               if (schemaNameResolver == null) {
               }

               if (schemaNameResolver != null) {
                  schemaName = schemaNameResolver.resolveSchemaName(connection);
               }

               lobCreatorBuilder = new LobCreatorBuilder(configValues, connection);
            } catch (SQLException sqle) {
               LOG.unableToObtainConnectionMetadata(sqle.getMessage());
            } finally {
               if (connection != null) {
                  jdbcConnectionAccess.releaseConnection(connection);
               }

            }
         } catch (SQLException sqle) {
            LOG.unableToObtainConnectionToQueryMetadata(sqle.getMessage());
            dialect = dialectFactory.buildDialect(configValues, (Connection)null);
         } catch (UnsupportedOperationException var29) {
            dialect = dialectFactory.buildDialect(configValues, (Connection)null);
         }
      } else {
         dialect = dialectFactory.buildDialect(configValues, (Connection)null);
      }

      boolean showSQL = ConfigurationHelper.getBoolean("hibernate.show_sql", configValues, false);
      boolean formatSQL = ConfigurationHelper.getBoolean("hibernate.format_sql", configValues, false);
      this.dialect = dialect;
      this.lobCreatorBuilder = lobCreatorBuilder == null ? new LobCreatorBuilder(configValues, (Connection)null) : lobCreatorBuilder;
      this.sqlStatementLogger = new SqlStatementLogger(showSQL, formatSQL);
      this.extractedMetaDataSupport = new ExtractedDatabaseMetaDataImpl(metaSupportsScrollable, metaSupportsGetGeneratedKeys, metaSupportsBatchUpdates, metaReportsDDLInTxnSupported, metaReportsDDLCausesTxnCommit, this.parseKeywords(extraKeywordsString), this.parseSQLStateType(sqlStateType), lobLocatorUpdateCopy, schemaName, catalogName, typeInfoSet);
      SQLExceptionConverter sqlExceptionConverter = dialect.buildSQLExceptionConverter();
      if (sqlExceptionConverter == null) {
         StandardSQLExceptionConverter converter = new StandardSQLExceptionConverter();
         sqlExceptionConverter = converter;
         converter.addDelegate(dialect.buildSQLExceptionConversionDelegate());
         converter.addDelegate(new SQLExceptionTypeDelegate(dialect));
         converter.addDelegate(new SQLStateConversionDelegate(dialect));
      }

      this.sqlExceptionHelper = new SqlExceptionHelper(sqlExceptionConverter);
   }

   private JdbcConnectionAccess buildJdbcConnectionAccess(Map configValues) {
      MultiTenancyStrategy multiTenancyStrategy = MultiTenancyStrategy.determineMultiTenancyStrategy(configValues);
      if (MultiTenancyStrategy.NONE == multiTenancyStrategy) {
         this.connectionProvider = (ConnectionProvider)this.serviceRegistry.getService(ConnectionProvider.class);
         return new ConnectionProviderJdbcConnectionAccess(this.connectionProvider);
      } else {
         this.connectionProvider = null;
         MultiTenantConnectionProvider multiTenantConnectionProvider = (MultiTenantConnectionProvider)this.serviceRegistry.getService(MultiTenantConnectionProvider.class);
         return new MultiTenantConnectionProviderJdbcConnectionAccess(multiTenantConnectionProvider);
      }
   }

   private SchemaNameResolver determineExplicitSchemaNameResolver(Map configValues) {
      Object setting = configValues.get("hibernate.schema_name_resolver");
      if (SchemaNameResolver.class.isInstance(setting)) {
         return (SchemaNameResolver)setting;
      } else {
         String resolverClassName = (String)setting;
         if (resolverClassName != null) {
            try {
               Class resolverClass = ReflectHelper.classForName(resolverClassName, this.getClass());
               return (SchemaNameResolver)ReflectHelper.getDefaultConstructor(resolverClass).newInstance();
            } catch (ClassNotFoundException e) {
               LOG.unableToLocateConfiguredSchemaNameResolver(resolverClassName, e.toString());
            } catch (InvocationTargetException e) {
               LOG.unableToInstantiateConfiguredSchemaNameResolver(resolverClassName, e.getTargetException().toString());
            } catch (Exception e) {
               LOG.unableToInstantiateConfiguredSchemaNameResolver(resolverClassName, e.toString());
            }
         }

         return null;
      }
   }

   private Set parseKeywords(String extraKeywordsString) {
      Set<String> keywordSet = new HashSet();
      keywordSet.addAll(Arrays.asList(extraKeywordsString.split(",")));
      return keywordSet;
   }

   private ExtractedDatabaseMetaData.SQLStateType parseSQLStateType(int sqlStateType) {
      switch (sqlStateType) {
         case 1:
            return ExtractedDatabaseMetaData.SQLStateType.XOpen;
         case 2:
            return ExtractedDatabaseMetaData.SQLStateType.SQL99;
         default:
            return ExtractedDatabaseMetaData.SQLStateType.UNKOWN;
      }
   }

   public ConnectionProvider getConnectionProvider() {
      return this.connectionProvider;
   }

   public SqlStatementLogger getSqlStatementLogger() {
      return this.sqlStatementLogger;
   }

   public SqlExceptionHelper getSqlExceptionHelper() {
      return this.sqlExceptionHelper;
   }

   public Dialect getDialect() {
      return this.dialect;
   }

   public ExtractedDatabaseMetaData getExtractedMetaDataSupport() {
      return this.extractedMetaDataSupport;
   }

   public LobCreator getLobCreator(LobCreationContext lobCreationContext) {
      return this.lobCreatorBuilder.buildLobCreator(lobCreationContext);
   }

   public ResultSetWrapper getResultSetWrapper() {
      return ResultSetWrapperImpl.INSTANCE;
   }

   private static class ConnectionProviderJdbcConnectionAccess implements JdbcConnectionAccess {
      private final ConnectionProvider connectionProvider;

      public ConnectionProviderJdbcConnectionAccess(ConnectionProvider connectionProvider) {
         this.connectionProvider = connectionProvider;
      }

      public Connection obtainConnection() throws SQLException {
         return this.connectionProvider.getConnection();
      }

      public void releaseConnection(Connection connection) throws SQLException {
         this.connectionProvider.closeConnection(connection);
      }

      public boolean supportsAggressiveRelease() {
         return this.connectionProvider.supportsAggressiveRelease();
      }
   }

   private static class MultiTenantConnectionProviderJdbcConnectionAccess implements JdbcConnectionAccess {
      private final MultiTenantConnectionProvider connectionProvider;

      public MultiTenantConnectionProviderJdbcConnectionAccess(MultiTenantConnectionProvider connectionProvider) {
         this.connectionProvider = connectionProvider;
      }

      public Connection obtainConnection() throws SQLException {
         return this.connectionProvider.getAnyConnection();
      }

      public void releaseConnection(Connection connection) throws SQLException {
         this.connectionProvider.releaseAnyConnection(connection);
      }

      public boolean supportsAggressiveRelease() {
         return this.connectionProvider.supportsAggressiveRelease();
      }
   }

   private static class ExtractedDatabaseMetaDataImpl implements ExtractedDatabaseMetaData {
      private final boolean supportsScrollableResults;
      private final boolean supportsGetGeneratedKeys;
      private final boolean supportsBatchUpdates;
      private final boolean supportsDataDefinitionInTransaction;
      private final boolean doesDataDefinitionCauseTransactionCommit;
      private final Set extraKeywords;
      private final ExtractedDatabaseMetaData.SQLStateType sqlStateType;
      private final boolean lobLocatorUpdateCopy;
      private final String connectionSchemaName;
      private final String connectionCatalogName;
      private final LinkedHashSet typeInfoSet;

      private ExtractedDatabaseMetaDataImpl(boolean supportsScrollableResults, boolean supportsGetGeneratedKeys, boolean supportsBatchUpdates, boolean supportsDataDefinitionInTransaction, boolean doesDataDefinitionCauseTransactionCommit, Set extraKeywords, ExtractedDatabaseMetaData.SQLStateType sqlStateType, boolean lobLocatorUpdateCopy, String connectionSchemaName, String connectionCatalogName, LinkedHashSet typeInfoSet) {
         this.supportsScrollableResults = supportsScrollableResults;
         this.supportsGetGeneratedKeys = supportsGetGeneratedKeys;
         this.supportsBatchUpdates = supportsBatchUpdates;
         this.supportsDataDefinitionInTransaction = supportsDataDefinitionInTransaction;
         this.doesDataDefinitionCauseTransactionCommit = doesDataDefinitionCauseTransactionCommit;
         this.extraKeywords = extraKeywords;
         this.sqlStateType = sqlStateType;
         this.lobLocatorUpdateCopy = lobLocatorUpdateCopy;
         this.connectionSchemaName = connectionSchemaName;
         this.connectionCatalogName = connectionCatalogName;
         this.typeInfoSet = typeInfoSet;
      }

      public boolean supportsScrollableResults() {
         return this.supportsScrollableResults;
      }

      public boolean supportsGetGeneratedKeys() {
         return this.supportsGetGeneratedKeys;
      }

      public boolean supportsBatchUpdates() {
         return this.supportsBatchUpdates;
      }

      public boolean supportsDataDefinitionInTransaction() {
         return this.supportsDataDefinitionInTransaction;
      }

      public boolean doesDataDefinitionCauseTransactionCommit() {
         return this.doesDataDefinitionCauseTransactionCommit;
      }

      public Set getExtraKeywords() {
         return this.extraKeywords;
      }

      public ExtractedDatabaseMetaData.SQLStateType getSqlStateType() {
         return this.sqlStateType;
      }

      public boolean doesLobLocatorUpdateCopy() {
         return this.lobLocatorUpdateCopy;
      }

      public String getConnectionSchemaName() {
         return this.connectionSchemaName;
      }

      public String getConnectionCatalogName() {
         return this.connectionCatalogName;
      }

      public LinkedHashSet getTypeInfoSet() {
         return this.typeInfoSet;
      }
   }
}
