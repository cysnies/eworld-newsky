package org.hibernate.cfg;

import java.util.Map;
import org.hibernate.ConnectionReleaseMode;
import org.hibernate.EntityMode;
import org.hibernate.MultiTenancyStrategy;
import org.hibernate.cache.spi.QueryCacheFactory;
import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.hql.spi.MultiTableBulkIdStrategy;
import org.hibernate.hql.spi.QueryTranslatorFactory;
import org.hibernate.service.jta.platform.spi.JtaPlatform;
import org.hibernate.tuple.entity.EntityTuplizerFactory;

public final class Settings {
   private Integer maximumFetchDepth;
   private Map querySubstitutions;
   private int jdbcBatchSize;
   private int defaultBatchFetchSize;
   private boolean scrollableResultSetsEnabled;
   private boolean getGeneratedKeysEnabled;
   private String defaultSchemaName;
   private String defaultCatalogName;
   private Integer jdbcFetchSize;
   private String sessionFactoryName;
   private boolean sessionFactoryNameAlsoJndiName;
   private boolean autoCreateSchema;
   private boolean autoDropSchema;
   private boolean autoUpdateSchema;
   private boolean autoValidateSchema;
   private boolean queryCacheEnabled;
   private boolean structuredCacheEntriesEnabled;
   private boolean secondLevelCacheEnabled;
   private String cacheRegionPrefix;
   private boolean minimalPutsEnabled;
   private boolean commentsEnabled;
   private boolean statisticsEnabled;
   private boolean jdbcBatchVersionedData;
   private boolean identifierRollbackEnabled;
   private boolean flushBeforeCompletionEnabled;
   private boolean autoCloseSessionEnabled;
   private ConnectionReleaseMode connectionReleaseMode;
   private RegionFactory regionFactory;
   private QueryCacheFactory queryCacheFactory;
   private QueryTranslatorFactory queryTranslatorFactory;
   private boolean wrapResultSetsEnabled;
   private boolean orderUpdatesEnabled;
   private boolean orderInsertsEnabled;
   private EntityMode defaultEntityMode;
   private boolean dataDefinitionImplicitCommit;
   private boolean dataDefinitionInTransactionSupported;
   private boolean strictJPAQLCompliance;
   private boolean namedQueryStartupCheckingEnabled;
   private EntityTuplizerFactory entityTuplizerFactory;
   private boolean checkNullability;
   private boolean initializeLazyStateOutsideTransactions;
   private String importFiles;
   private MultiTenancyStrategy multiTenancyStrategy;
   private JtaPlatform jtaPlatform;
   private MultiTableBulkIdStrategy multiTableBulkIdStrategy;

   Settings() {
   }

   public String getImportFiles() {
      return this.importFiles;
   }

   public void setImportFiles(String importFiles) {
      this.importFiles = importFiles;
   }

   public String getDefaultSchemaName() {
      return this.defaultSchemaName;
   }

   public String getDefaultCatalogName() {
      return this.defaultCatalogName;
   }

   public int getJdbcBatchSize() {
      return this.jdbcBatchSize;
   }

   public int getDefaultBatchFetchSize() {
      return this.defaultBatchFetchSize;
   }

   public Map getQuerySubstitutions() {
      return this.querySubstitutions;
   }

   public boolean isIdentifierRollbackEnabled() {
      return this.identifierRollbackEnabled;
   }

   public boolean isScrollableResultSetsEnabled() {
      return this.scrollableResultSetsEnabled;
   }

   public boolean isGetGeneratedKeysEnabled() {
      return this.getGeneratedKeysEnabled;
   }

   public boolean isMinimalPutsEnabled() {
      return this.minimalPutsEnabled;
   }

   public Integer getJdbcFetchSize() {
      return this.jdbcFetchSize;
   }

   public String getSessionFactoryName() {
      return this.sessionFactoryName;
   }

   public boolean isSessionFactoryNameAlsoJndiName() {
      return this.sessionFactoryNameAlsoJndiName;
   }

   public boolean isAutoCreateSchema() {
      return this.autoCreateSchema;
   }

   public boolean isAutoDropSchema() {
      return this.autoDropSchema;
   }

   public boolean isAutoUpdateSchema() {
      return this.autoUpdateSchema;
   }

   public Integer getMaximumFetchDepth() {
      return this.maximumFetchDepth;
   }

   public RegionFactory getRegionFactory() {
      return this.regionFactory;
   }

   public boolean isQueryCacheEnabled() {
      return this.queryCacheEnabled;
   }

   public boolean isCommentsEnabled() {
      return this.commentsEnabled;
   }

   public boolean isSecondLevelCacheEnabled() {
      return this.secondLevelCacheEnabled;
   }

   public String getCacheRegionPrefix() {
      return this.cacheRegionPrefix;
   }

   public QueryCacheFactory getQueryCacheFactory() {
      return this.queryCacheFactory;
   }

   public boolean isStatisticsEnabled() {
      return this.statisticsEnabled;
   }

   public boolean isJdbcBatchVersionedData() {
      return this.jdbcBatchVersionedData;
   }

   public boolean isFlushBeforeCompletionEnabled() {
      return this.flushBeforeCompletionEnabled;
   }

   public boolean isAutoCloseSessionEnabled() {
      return this.autoCloseSessionEnabled;
   }

   public ConnectionReleaseMode getConnectionReleaseMode() {
      return this.connectionReleaseMode;
   }

   public QueryTranslatorFactory getQueryTranslatorFactory() {
      return this.queryTranslatorFactory;
   }

   public boolean isWrapResultSetsEnabled() {
      return this.wrapResultSetsEnabled;
   }

   public boolean isOrderUpdatesEnabled() {
      return this.orderUpdatesEnabled;
   }

   public boolean isOrderInsertsEnabled() {
      return this.orderInsertsEnabled;
   }

   public boolean isStructuredCacheEntriesEnabled() {
      return this.structuredCacheEntriesEnabled;
   }

   public EntityMode getDefaultEntityMode() {
      return this.defaultEntityMode;
   }

   public boolean isAutoValidateSchema() {
      return this.autoValidateSchema;
   }

   public boolean isDataDefinitionImplicitCommit() {
      return this.dataDefinitionImplicitCommit;
   }

   public boolean isDataDefinitionInTransactionSupported() {
      return this.dataDefinitionInTransactionSupported;
   }

   public boolean isStrictJPAQLCompliance() {
      return this.strictJPAQLCompliance;
   }

   public boolean isNamedQueryStartupCheckingEnabled() {
      return this.namedQueryStartupCheckingEnabled;
   }

   public EntityTuplizerFactory getEntityTuplizerFactory() {
      return this.entityTuplizerFactory;
   }

   void setDefaultSchemaName(String string) {
      this.defaultSchemaName = string;
   }

   void setDefaultCatalogName(String string) {
      this.defaultCatalogName = string;
   }

   void setJdbcBatchSize(int i) {
      this.jdbcBatchSize = i;
   }

   void setDefaultBatchFetchSize(int i) {
      this.defaultBatchFetchSize = i;
   }

   void setQuerySubstitutions(Map map) {
      this.querySubstitutions = map;
   }

   void setIdentifierRollbackEnabled(boolean b) {
      this.identifierRollbackEnabled = b;
   }

   void setMinimalPutsEnabled(boolean b) {
      this.minimalPutsEnabled = b;
   }

   void setScrollableResultSetsEnabled(boolean b) {
      this.scrollableResultSetsEnabled = b;
   }

   void setGetGeneratedKeysEnabled(boolean b) {
      this.getGeneratedKeysEnabled = b;
   }

   void setJdbcFetchSize(Integer integer) {
      this.jdbcFetchSize = integer;
   }

   void setSessionFactoryName(String string) {
      this.sessionFactoryName = string;
   }

   void setSessionFactoryNameAlsoJndiName(boolean sessionFactoryNameAlsoJndiName) {
      this.sessionFactoryNameAlsoJndiName = sessionFactoryNameAlsoJndiName;
   }

   void setAutoCreateSchema(boolean b) {
      this.autoCreateSchema = b;
   }

   void setAutoDropSchema(boolean b) {
      this.autoDropSchema = b;
   }

   void setAutoUpdateSchema(boolean b) {
      this.autoUpdateSchema = b;
   }

   void setMaximumFetchDepth(Integer i) {
      this.maximumFetchDepth = i;
   }

   void setRegionFactory(RegionFactory regionFactory) {
      this.regionFactory = regionFactory;
   }

   void setQueryCacheEnabled(boolean b) {
      this.queryCacheEnabled = b;
   }

   void setCommentsEnabled(boolean commentsEnabled) {
      this.commentsEnabled = commentsEnabled;
   }

   void setSecondLevelCacheEnabled(boolean secondLevelCacheEnabled) {
      this.secondLevelCacheEnabled = secondLevelCacheEnabled;
   }

   void setCacheRegionPrefix(String cacheRegionPrefix) {
      this.cacheRegionPrefix = cacheRegionPrefix;
   }

   void setQueryCacheFactory(QueryCacheFactory queryCacheFactory) {
      this.queryCacheFactory = queryCacheFactory;
   }

   void setStatisticsEnabled(boolean statisticsEnabled) {
      this.statisticsEnabled = statisticsEnabled;
   }

   void setJdbcBatchVersionedData(boolean jdbcBatchVersionedData) {
      this.jdbcBatchVersionedData = jdbcBatchVersionedData;
   }

   void setFlushBeforeCompletionEnabled(boolean flushBeforeCompletionEnabled) {
      this.flushBeforeCompletionEnabled = flushBeforeCompletionEnabled;
   }

   void setAutoCloseSessionEnabled(boolean autoCloseSessionEnabled) {
      this.autoCloseSessionEnabled = autoCloseSessionEnabled;
   }

   void setConnectionReleaseMode(ConnectionReleaseMode connectionReleaseMode) {
      this.connectionReleaseMode = connectionReleaseMode;
   }

   void setQueryTranslatorFactory(QueryTranslatorFactory queryTranslatorFactory) {
      this.queryTranslatorFactory = queryTranslatorFactory;
   }

   void setWrapResultSetsEnabled(boolean wrapResultSetsEnabled) {
      this.wrapResultSetsEnabled = wrapResultSetsEnabled;
   }

   void setOrderUpdatesEnabled(boolean orderUpdatesEnabled) {
      this.orderUpdatesEnabled = orderUpdatesEnabled;
   }

   void setOrderInsertsEnabled(boolean orderInsertsEnabled) {
      this.orderInsertsEnabled = orderInsertsEnabled;
   }

   void setStructuredCacheEntriesEnabled(boolean structuredCacheEntriesEnabled) {
      this.structuredCacheEntriesEnabled = structuredCacheEntriesEnabled;
   }

   void setDefaultEntityMode(EntityMode defaultEntityMode) {
      this.defaultEntityMode = defaultEntityMode;
   }

   void setAutoValidateSchema(boolean autoValidateSchema) {
      this.autoValidateSchema = autoValidateSchema;
   }

   void setDataDefinitionImplicitCommit(boolean dataDefinitionImplicitCommit) {
      this.dataDefinitionImplicitCommit = dataDefinitionImplicitCommit;
   }

   void setDataDefinitionInTransactionSupported(boolean dataDefinitionInTransactionSupported) {
      this.dataDefinitionInTransactionSupported = dataDefinitionInTransactionSupported;
   }

   void setStrictJPAQLCompliance(boolean strictJPAQLCompliance) {
      this.strictJPAQLCompliance = strictJPAQLCompliance;
   }

   void setNamedQueryStartupCheckingEnabled(boolean namedQueryStartupCheckingEnabled) {
      this.namedQueryStartupCheckingEnabled = namedQueryStartupCheckingEnabled;
   }

   void setEntityTuplizerFactory(EntityTuplizerFactory entityTuplizerFactory) {
      this.entityTuplizerFactory = entityTuplizerFactory;
   }

   public boolean isCheckNullability() {
      return this.checkNullability;
   }

   public void setCheckNullability(boolean checkNullability) {
      this.checkNullability = checkNullability;
   }

   public JtaPlatform getJtaPlatform() {
      return this.jtaPlatform;
   }

   void setJtaPlatform(JtaPlatform jtaPlatform) {
      this.jtaPlatform = jtaPlatform;
   }

   public MultiTenancyStrategy getMultiTenancyStrategy() {
      return this.multiTenancyStrategy;
   }

   void setMultiTenancyStrategy(MultiTenancyStrategy multiTenancyStrategy) {
      this.multiTenancyStrategy = multiTenancyStrategy;
   }

   public boolean isInitializeLazyStateOutsideTransactionsEnabled() {
      return this.initializeLazyStateOutsideTransactions;
   }

   void setInitializeLazyStateOutsideTransactions(boolean initializeLazyStateOutsideTransactions) {
      this.initializeLazyStateOutsideTransactions = initializeLazyStateOutsideTransactions;
   }

   public MultiTableBulkIdStrategy getMultiTableBulkIdStrategy() {
      return this.multiTableBulkIdStrategy;
   }

   void setMultiTableBulkIdStrategy(MultiTableBulkIdStrategy multiTableBulkIdStrategy) {
      this.multiTableBulkIdStrategy = multiTableBulkIdStrategy;
   }
}
