package org.hibernate.cfg;

import java.io.Serializable;
import java.util.Map;
import java.util.Properties;
import org.hibernate.ConnectionReleaseMode;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.MultiTenancyStrategy;
import org.hibernate.cache.internal.NoCachingRegionFactory;
import org.hibernate.cache.internal.RegionFactoryInitiator;
import org.hibernate.cache.internal.StandardQueryCacheFactory;
import org.hibernate.cache.spi.QueryCacheFactory;
import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.spi.ExtractedDatabaseMetaData;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.transaction.spi.TransactionFactory;
import org.hibernate.hql.spi.MultiTableBulkIdStrategy;
import org.hibernate.hql.spi.PersistentTableBulkIdStrategy;
import org.hibernate.hql.spi.QueryTranslatorFactory;
import org.hibernate.hql.spi.TemporaryTableBulkIdStrategy;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.classloading.spi.ClassLoaderService;
import org.hibernate.service.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.hibernate.service.jta.platform.spi.JtaPlatform;
import org.hibernate.tuple.entity.EntityTuplizerFactory;
import org.jboss.logging.Logger;

public class SettingsFactory implements Serializable {
   private static final long serialVersionUID = -1194386144994524825L;
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, SettingsFactory.class.getName());
   public static final String DEF_CACHE_REG_FACTORY = NoCachingRegionFactory.class.getName();

   public Settings buildSettings(Properties props, ServiceRegistry serviceRegistry) {
      boolean debugEnabled = LOG.isDebugEnabled();
      JdbcServices jdbcServices = (JdbcServices)serviceRegistry.getService(JdbcServices.class);
      Settings settings = new Settings();
      String sessionFactoryName = props.getProperty("hibernate.session_factory_name");
      settings.setSessionFactoryName(sessionFactoryName);
      settings.setSessionFactoryNameAlsoJndiName(ConfigurationHelper.getBoolean("hibernate.session_factory_name_is_jndi", props, true));
      ExtractedDatabaseMetaData meta = jdbcServices.getExtractedMetaDataSupport();
      settings.setDataDefinitionImplicitCommit(meta.doesDataDefinitionCauseTransactionCommit());
      settings.setDataDefinitionInTransactionSupported(meta.supportsDataDefinitionInTransaction());
      Properties properties = new Properties();
      properties.putAll(jdbcServices.getDialect().getDefaultProperties());
      properties.putAll(props);
      settings.setJtaPlatform((JtaPlatform)serviceRegistry.getService(JtaPlatform.class));
      MultiTableBulkIdStrategy multiTableBulkIdStrategy = this.getMultiTableBulkIdStrategy(properties, jdbcServices.getDialect(), (ClassLoaderService)serviceRegistry.getService(ClassLoaderService.class));
      settings.setMultiTableBulkIdStrategy(multiTableBulkIdStrategy);
      boolean flushBeforeCompletion = ConfigurationHelper.getBoolean("hibernate.transaction.flush_before_completion", properties);
      if (debugEnabled) {
         LOG.debugf("Automatic flush during beforeCompletion(): %s", enabledDisabled(flushBeforeCompletion));
      }

      settings.setFlushBeforeCompletionEnabled(flushBeforeCompletion);
      boolean autoCloseSession = ConfigurationHelper.getBoolean("hibernate.transaction.auto_close_session", properties);
      if (debugEnabled) {
         LOG.debugf("Automatic session close at end of transaction: %s", enabledDisabled(autoCloseSession));
      }

      settings.setAutoCloseSessionEnabled(autoCloseSession);
      int batchSize = ConfigurationHelper.getInt("hibernate.jdbc.batch_size", properties, 0);
      if (!meta.supportsBatchUpdates()) {
         batchSize = 0;
      }

      if (batchSize > 0 && debugEnabled) {
         LOG.debugf("JDBC batch size: %s", batchSize);
      }

      settings.setJdbcBatchSize(batchSize);
      boolean jdbcBatchVersionedData = ConfigurationHelper.getBoolean("hibernate.jdbc.batch_versioned_data", properties, false);
      if (batchSize > 0 && debugEnabled) {
         LOG.debugf("JDBC batch updates for versioned data: %s", enabledDisabled(jdbcBatchVersionedData));
      }

      settings.setJdbcBatchVersionedData(jdbcBatchVersionedData);
      boolean useScrollableResultSets = ConfigurationHelper.getBoolean("hibernate.jdbc.use_scrollable_resultset", properties, meta.supportsScrollableResults());
      if (debugEnabled) {
         LOG.debugf("Scrollable result sets: %s", enabledDisabled(useScrollableResultSets));
      }

      settings.setScrollableResultSetsEnabled(useScrollableResultSets);
      boolean wrapResultSets = ConfigurationHelper.getBoolean("hibernate.jdbc.wrap_result_sets", properties, false);
      if (debugEnabled) {
         LOG.debugf("Wrap result sets: %s", enabledDisabled(wrapResultSets));
      }

      settings.setWrapResultSetsEnabled(wrapResultSets);
      boolean useGetGeneratedKeys = ConfigurationHelper.getBoolean("hibernate.jdbc.use_get_generated_keys", properties, meta.supportsGetGeneratedKeys());
      if (debugEnabled) {
         LOG.debugf("JDBC3 getGeneratedKeys(): %s", enabledDisabled(useGetGeneratedKeys));
      }

      settings.setGetGeneratedKeysEnabled(useGetGeneratedKeys);
      Integer statementFetchSize = ConfigurationHelper.getInteger("hibernate.jdbc.fetch_size", properties);
      if (statementFetchSize != null && debugEnabled) {
         LOG.debugf("JDBC result set fetch size: %s", statementFetchSize);
      }

      settings.setJdbcFetchSize(statementFetchSize);
      MultiTenancyStrategy multiTenancyStrategy = MultiTenancyStrategy.determineMultiTenancyStrategy(properties);
      if (debugEnabled) {
         LOG.debugf("multi-tenancy strategy : %s", multiTenancyStrategy);
      }

      settings.setMultiTenancyStrategy(multiTenancyStrategy);
      String releaseModeName = ConfigurationHelper.getString("hibernate.connection.release_mode", properties, "auto");
      if (debugEnabled) {
         LOG.debugf("Connection release mode: %s", releaseModeName);
      }

      ConnectionReleaseMode releaseMode;
      if ("auto".equals(releaseModeName)) {
         releaseMode = ((TransactionFactory)serviceRegistry.getService(TransactionFactory.class)).getDefaultReleaseMode();
      } else {
         releaseMode = ConnectionReleaseMode.parse(releaseModeName);
         if (releaseMode == ConnectionReleaseMode.AFTER_STATEMENT) {
            boolean supportsAgrressiveRelease = multiTenancyStrategy.requiresMultiTenantConnectionProvider() ? ((MultiTenantConnectionProvider)serviceRegistry.getService(MultiTenantConnectionProvider.class)).supportsAggressiveRelease() : ((ConnectionProvider)serviceRegistry.getService(ConnectionProvider.class)).supportsAggressiveRelease();
            if (!supportsAgrressiveRelease) {
               LOG.unsupportedAfterStatement();
               releaseMode = ConnectionReleaseMode.AFTER_TRANSACTION;
            }
         }
      }

      settings.setConnectionReleaseMode(releaseMode);
      String defaultSchema = properties.getProperty("hibernate.default_schema");
      String defaultCatalog = properties.getProperty("hibernate.default_catalog");
      if (defaultSchema != null && debugEnabled) {
         LOG.debugf("Default schema: %s", defaultSchema);
      }

      if (defaultCatalog != null && debugEnabled) {
         LOG.debugf("Default catalog: %s", defaultCatalog);
      }

      settings.setDefaultSchemaName(defaultSchema);
      settings.setDefaultCatalogName(defaultCatalog);
      Integer maxFetchDepth = ConfigurationHelper.getInteger("hibernate.max_fetch_depth", properties);
      if (maxFetchDepth != null) {
         LOG.debugf("Maximum outer join fetch depth: %s", maxFetchDepth);
      }

      settings.setMaximumFetchDepth(maxFetchDepth);
      int batchFetchSize = ConfigurationHelper.getInt("hibernate.default_batch_fetch_size", properties, 1);
      if (debugEnabled) {
         LOG.debugf("Default batch fetch size: %s", batchFetchSize);
      }

      settings.setDefaultBatchFetchSize(batchFetchSize);
      boolean comments = ConfigurationHelper.getBoolean("hibernate.use_sql_comments", properties);
      if (debugEnabled) {
         LOG.debugf("Generate SQL with comments: %s", enabledDisabled(comments));
      }

      settings.setCommentsEnabled(comments);
      boolean orderUpdates = ConfigurationHelper.getBoolean("hibernate.order_updates", properties);
      if (debugEnabled) {
         LOG.debugf("Order SQL updates by primary key: %s", enabledDisabled(orderUpdates));
      }

      settings.setOrderUpdatesEnabled(orderUpdates);
      boolean orderInserts = ConfigurationHelper.getBoolean("hibernate.order_inserts", properties);
      if (debugEnabled) {
         LOG.debugf("Order SQL inserts for batching: %s", enabledDisabled(orderInserts));
      }

      settings.setOrderInsertsEnabled(orderInserts);
      settings.setQueryTranslatorFactory(this.createQueryTranslatorFactory(properties, serviceRegistry));
      Map querySubstitutions = ConfigurationHelper.toMap("hibernate.query.substitutions", " ,=;:\n\t\r\f", properties);
      if (debugEnabled) {
         LOG.debugf("Query language substitutions: %s", querySubstitutions);
      }

      settings.setQuerySubstitutions(querySubstitutions);
      boolean jpaqlCompliance = ConfigurationHelper.getBoolean("hibernate.query.jpaql_strict_compliance", properties, false);
      if (debugEnabled) {
         LOG.debugf("JPA-QL strict compliance: %s", enabledDisabled(jpaqlCompliance));
      }

      settings.setStrictJPAQLCompliance(jpaqlCompliance);
      boolean useSecondLevelCache = ConfigurationHelper.getBoolean("hibernate.cache.use_second_level_cache", properties, true);
      if (debugEnabled) {
         LOG.debugf("Second-level cache: %s", enabledDisabled(useSecondLevelCache));
      }

      settings.setSecondLevelCacheEnabled(useSecondLevelCache);
      boolean useQueryCache = ConfigurationHelper.getBoolean("hibernate.cache.use_query_cache", properties);
      if (debugEnabled) {
         LOG.debugf("Query cache: %s", enabledDisabled(useQueryCache));
      }

      settings.setQueryCacheEnabled(useQueryCache);
      if (useQueryCache) {
         settings.setQueryCacheFactory(this.createQueryCacheFactory(properties, serviceRegistry));
      }

      settings.setRegionFactory(createRegionFactory(properties, useSecondLevelCache || useQueryCache, serviceRegistry));
      boolean useMinimalPuts = ConfigurationHelper.getBoolean("hibernate.cache.use_minimal_puts", properties, settings.getRegionFactory().isMinimalPutsEnabledByDefault());
      if (debugEnabled) {
         LOG.debugf("Optimize cache for minimal puts: %s", enabledDisabled(useMinimalPuts));
      }

      settings.setMinimalPutsEnabled(useMinimalPuts);
      String prefix = properties.getProperty("hibernate.cache.region_prefix");
      if (StringHelper.isEmpty(prefix)) {
         prefix = null;
      }

      if (prefix != null && debugEnabled) {
         LOG.debugf("Cache region prefix: %s", prefix);
      }

      settings.setCacheRegionPrefix(prefix);
      boolean useStructuredCacheEntries = ConfigurationHelper.getBoolean("hibernate.cache.use_structured_entries", properties, false);
      if (debugEnabled) {
         LOG.debugf("Structured second-level cache entries: %s", enabledDisabled(useStructuredCacheEntries));
      }

      settings.setStructuredCacheEntriesEnabled(useStructuredCacheEntries);
      boolean useStatistics = ConfigurationHelper.getBoolean("hibernate.generate_statistics", properties);
      if (debugEnabled) {
         LOG.debugf("Statistics: %s", enabledDisabled(useStatistics));
      }

      settings.setStatisticsEnabled(useStatistics);
      boolean useIdentifierRollback = ConfigurationHelper.getBoolean("hibernate.use_identifier_rollback", properties);
      if (debugEnabled) {
         LOG.debugf("Deleted entity synthetic identifier rollback: %s", enabledDisabled(useIdentifierRollback));
      }

      settings.setIdentifierRollbackEnabled(useIdentifierRollback);
      String autoSchemaExport = properties.getProperty("hibernate.hbm2ddl.auto");
      if ("validate".equals(autoSchemaExport)) {
         settings.setAutoValidateSchema(true);
      }

      if ("update".equals(autoSchemaExport)) {
         settings.setAutoUpdateSchema(true);
      }

      if ("create".equals(autoSchemaExport)) {
         settings.setAutoCreateSchema(true);
      }

      if ("create-drop".equals(autoSchemaExport)) {
         settings.setAutoCreateSchema(true);
         settings.setAutoDropSchema(true);
      }

      settings.setImportFiles(properties.getProperty("hibernate.hbm2ddl.import_files"));
      EntityMode defaultEntityMode = EntityMode.parse(properties.getProperty("hibernate.default_entity_mode"));
      if (debugEnabled) {
         LOG.debugf("Default entity-mode: %s", defaultEntityMode);
      }

      settings.setDefaultEntityMode(defaultEntityMode);
      boolean namedQueryChecking = ConfigurationHelper.getBoolean("hibernate.query.startup_check", properties, true);
      if (debugEnabled) {
         LOG.debugf("Named query checking : %s", enabledDisabled(namedQueryChecking));
      }

      settings.setNamedQueryStartupCheckingEnabled(namedQueryChecking);
      boolean checkNullability = ConfigurationHelper.getBoolean("hibernate.check_nullability", properties, true);
      if (debugEnabled) {
         LOG.debugf("Check Nullability in Core (should be disabled when Bean Validation is on): %s", enabledDisabled(checkNullability));
      }

      settings.setCheckNullability(checkNullability);
      settings.setEntityTuplizerFactory(new EntityTuplizerFactory());
      boolean initializeLazyStateOutsideTransactionsEnabled = ConfigurationHelper.getBoolean("hibernate.enable_lazy_load_no_trans", properties, false);
      if (debugEnabled) {
         LOG.debugf("Allow initialization of lazy state outside session : : %s", enabledDisabled(initializeLazyStateOutsideTransactionsEnabled));
      }

      settings.setInitializeLazyStateOutsideTransactions(initializeLazyStateOutsideTransactionsEnabled);
      return settings;
   }

   private MultiTableBulkIdStrategy getMultiTableBulkIdStrategy(Properties properties, Dialect dialect, ClassLoaderService classLoaderService) {
      Object setting = properties.get("hibernate.hql.bulk_id_strategy");
      if (setting != null) {
         if (MultiTableBulkIdStrategy.class.isInstance(setting)) {
            return (MultiTableBulkIdStrategy)setting;
         } else {
            Class strategyClass;
            if (Class.class.isInstance(setting)) {
               strategyClass = (Class)setting;
            } else {
               String settingStr = setting.toString();
               if ("persistent".equals(settingStr)) {
                  return new PersistentTableBulkIdStrategy();
               }

               if ("temporary".equals(settingStr)) {
                  return TemporaryTableBulkIdStrategy.INSTANCE;
               }

               strategyClass = classLoaderService.classForName(settingStr);
            }

            try {
               return (MultiTableBulkIdStrategy)strategyClass.newInstance();
            } catch (Exception e) {
               throw new HibernateException("Unable to interpret MultiTableBulkIdStrategy setting [" + setting + "]", e);
            }
         }
      } else {
         return (MultiTableBulkIdStrategy)(dialect.supportsTemporaryTables() ? TemporaryTableBulkIdStrategy.INSTANCE : new PersistentTableBulkIdStrategy());
      }
   }

   private static String enabledDisabled(boolean value) {
      return value ? "enabled" : "disabled";
   }

   protected QueryCacheFactory createQueryCacheFactory(Properties properties, ServiceRegistry serviceRegistry) {
      String queryCacheFactoryClassName = ConfigurationHelper.getString("hibernate.cache.query_cache_factory", properties, StandardQueryCacheFactory.class.getName());
      LOG.debugf("Query cache factory: %s", queryCacheFactoryClassName);

      try {
         return (QueryCacheFactory)((ClassLoaderService)serviceRegistry.getService(ClassLoaderService.class)).classForName(queryCacheFactoryClassName).newInstance();
      } catch (Exception e) {
         throw new HibernateException("could not instantiate QueryCacheFactory: " + queryCacheFactoryClassName, e);
      }
   }

   private static RegionFactory createRegionFactory(Properties properties, boolean cachingEnabled, ServiceRegistry serviceRegistry) {
      String regionFactoryClassName = RegionFactoryInitiator.mapLegacyNames(ConfigurationHelper.getString("hibernate.cache.region.factory_class", properties, (String)null));
      if (regionFactoryClassName == null || !cachingEnabled) {
         regionFactoryClassName = DEF_CACHE_REG_FACTORY;
      }

      LOG.debugf("Cache region factory : %s", regionFactoryClassName);

      try {
         try {
            return (RegionFactory)((ClassLoaderService)serviceRegistry.getService(ClassLoaderService.class)).classForName(regionFactoryClassName).getConstructor(Properties.class).newInstance(properties);
         } catch (NoSuchMethodException var5) {
            LOG.debugf("%s did not provide constructor accepting java.util.Properties; attempting no-arg constructor.", regionFactoryClassName);
            return (RegionFactory)((ClassLoaderService)serviceRegistry.getService(ClassLoaderService.class)).classForName(regionFactoryClassName).newInstance();
         }
      } catch (Exception e) {
         throw new HibernateException("could not instantiate RegionFactory [" + regionFactoryClassName + "]", e);
      }
   }

   public static RegionFactory createRegionFactory(Properties properties, boolean cachingEnabled) {
      String regionFactoryClassName = RegionFactoryInitiator.mapLegacyNames(ConfigurationHelper.getString("hibernate.cache.region.factory_class", properties, (String)null));
      if (regionFactoryClassName == null) {
         regionFactoryClassName = DEF_CACHE_REG_FACTORY;
      }

      LOG.debugf("Cache region factory : %s", regionFactoryClassName);

      try {
         try {
            return (RegionFactory)ReflectHelper.classForName(regionFactoryClassName).getConstructor(Properties.class).newInstance(properties);
         } catch (NoSuchMethodException var4) {
            LOG.debugf("%s did not provide constructor accepting java.util.Properties; attempting no-arg constructor.", regionFactoryClassName);
            return (RegionFactory)ReflectHelper.classForName(regionFactoryClassName).newInstance();
         }
      } catch (Exception e) {
         throw new HibernateException("could not instantiate RegionFactory [" + regionFactoryClassName + "]", e);
      }
   }

   protected QueryTranslatorFactory createQueryTranslatorFactory(Properties properties, ServiceRegistry serviceRegistry) {
      String className = ConfigurationHelper.getString("hibernate.query.factory_class", properties, "org.hibernate.hql.internal.ast.ASTQueryTranslatorFactory");
      LOG.debugf("Query translator: %s", className);

      try {
         return (QueryTranslatorFactory)((ClassLoaderService)serviceRegistry.getService(ClassLoaderService.class)).classForName(className).newInstance();
      } catch (Exception e) {
         throw new HibernateException("could not instantiate QueryTranslatorFactory: " + className, e);
      }
   }
}
