package org.hibernate.cfg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.hibernate.internal.util.config.ConfigurationHelper;

public abstract class ExternalSessionFactoryConfig {
   private String mapResources;
   private String dialect;
   private String defaultSchema;
   private String defaultCatalog;
   private String maximumFetchDepth;
   private String jdbcFetchSize;
   private String jdbcBatchSize;
   private String batchVersionedDataEnabled;
   private String jdbcScrollableResultSetEnabled;
   private String getGeneratedKeysEnabled;
   private String streamsForBinaryEnabled;
   private String reflectionOptimizationEnabled;
   private String querySubstitutions;
   private String showSqlEnabled;
   private String commentsEnabled;
   private String cacheRegionFactory;
   private String cacheProviderConfig;
   private String cacheRegionPrefix;
   private String secondLevelCacheEnabled;
   private String minimalPutsEnabled;
   private String queryCacheEnabled;
   private Map additionalProperties;
   private Set excludedPropertyNames = new HashSet();

   protected Set getExcludedPropertyNames() {
      return this.excludedPropertyNames;
   }

   public final String getMapResources() {
      return this.mapResources;
   }

   public final void setMapResources(String mapResources) {
      this.mapResources = mapResources;
   }

   public void addMapResource(String mapResource) {
      if (this.mapResources != null && this.mapResources.length() != 0) {
         this.mapResources = this.mapResources + ", " + mapResource.trim();
      } else {
         this.mapResources = mapResource.trim();
      }

   }

   public final String getDialect() {
      return this.dialect;
   }

   public final void setDialect(String dialect) {
      this.dialect = dialect;
   }

   public final String getDefaultSchema() {
      return this.defaultSchema;
   }

   public final void setDefaultSchema(String defaultSchema) {
      this.defaultSchema = defaultSchema;
   }

   public final String getDefaultCatalog() {
      return this.defaultCatalog;
   }

   public final void setDefaultCatalog(String defaultCatalog) {
      this.defaultCatalog = defaultCatalog;
   }

   public final String getMaximumFetchDepth() {
      return this.maximumFetchDepth;
   }

   public final void setMaximumFetchDepth(String maximumFetchDepth) {
      this.verifyInt(maximumFetchDepth);
      this.maximumFetchDepth = maximumFetchDepth;
   }

   public final String getJdbcFetchSize() {
      return this.jdbcFetchSize;
   }

   public final void setJdbcFetchSize(String jdbcFetchSize) {
      this.verifyInt(jdbcFetchSize);
      this.jdbcFetchSize = jdbcFetchSize;
   }

   public final String getJdbcBatchSize() {
      return this.jdbcBatchSize;
   }

   public final void setJdbcBatchSize(String jdbcBatchSize) {
      this.verifyInt(jdbcBatchSize);
      this.jdbcBatchSize = jdbcBatchSize;
   }

   public final String getBatchVersionedDataEnabled() {
      return this.batchVersionedDataEnabled;
   }

   public final void setBatchVersionedDataEnabled(String batchVersionedDataEnabled) {
      this.batchVersionedDataEnabled = batchVersionedDataEnabled;
   }

   public final String getJdbcScrollableResultSetEnabled() {
      return this.jdbcScrollableResultSetEnabled;
   }

   public final void setJdbcScrollableResultSetEnabled(String jdbcScrollableResultSetEnabled) {
      this.jdbcScrollableResultSetEnabled = jdbcScrollableResultSetEnabled;
   }

   public final String getGetGeneratedKeysEnabled() {
      return this.getGeneratedKeysEnabled;
   }

   public final void setGetGeneratedKeysEnabled(String getGeneratedKeysEnabled) {
      this.getGeneratedKeysEnabled = getGeneratedKeysEnabled;
   }

   public final String getStreamsForBinaryEnabled() {
      return this.streamsForBinaryEnabled;
   }

   public final void setStreamsForBinaryEnabled(String streamsForBinaryEnabled) {
      this.streamsForBinaryEnabled = streamsForBinaryEnabled;
   }

   public final String getReflectionOptimizationEnabled() {
      return this.reflectionOptimizationEnabled;
   }

   public final void setReflectionOptimizationEnabled(String reflectionOptimizationEnabled) {
      this.reflectionOptimizationEnabled = reflectionOptimizationEnabled;
   }

   public final String getQuerySubstitutions() {
      return this.querySubstitutions;
   }

   public final void setQuerySubstitutions(String querySubstitutions) {
      this.querySubstitutions = querySubstitutions;
   }

   public final String getShowSqlEnabled() {
      return this.showSqlEnabled;
   }

   public final void setShowSqlEnabled(String showSqlEnabled) {
      this.showSqlEnabled = showSqlEnabled;
   }

   public final String getCommentsEnabled() {
      return this.commentsEnabled;
   }

   public final void setCommentsEnabled(String commentsEnabled) {
      this.commentsEnabled = commentsEnabled;
   }

   public final String getSecondLevelCacheEnabled() {
      return this.secondLevelCacheEnabled;
   }

   public final void setSecondLevelCacheEnabled(String secondLevelCacheEnabled) {
      this.secondLevelCacheEnabled = secondLevelCacheEnabled;
   }

   public final String getCacheRegionFactory() {
      return this.cacheRegionFactory;
   }

   public final void setCacheRegionFactory(String cacheRegionFactory) {
      this.cacheRegionFactory = cacheRegionFactory;
   }

   public String getCacheProviderConfig() {
      return this.cacheProviderConfig;
   }

   public void setCacheProviderConfig(String cacheProviderConfig) {
      this.cacheProviderConfig = cacheProviderConfig;
   }

   public final String getCacheRegionPrefix() {
      return this.cacheRegionPrefix;
   }

   public final void setCacheRegionPrefix(String cacheRegionPrefix) {
      this.cacheRegionPrefix = cacheRegionPrefix;
   }

   public final String getMinimalPutsEnabled() {
      return this.minimalPutsEnabled;
   }

   public final void setMinimalPutsEnabled(String minimalPutsEnabled) {
      this.minimalPutsEnabled = minimalPutsEnabled;
   }

   public final String getQueryCacheEnabled() {
      return this.queryCacheEnabled;
   }

   public final void setQueryCacheEnabled(String queryCacheEnabled) {
      this.queryCacheEnabled = queryCacheEnabled;
   }

   public final void addAdditionalProperty(String name, String value) {
      if (!this.getExcludedPropertyNames().contains(name)) {
         if (this.additionalProperties == null) {
            this.additionalProperties = new HashMap();
         }

         this.additionalProperties.put(name, value);
      }

   }

   protected final Configuration buildConfiguration() {
      Configuration cfg = (new Configuration()).setProperties(this.buildProperties());
      String[] mappingFiles = ConfigurationHelper.toStringArray(this.mapResources, " ,\n\t\r\f");

      for(int i = 0; i < mappingFiles.length; ++i) {
         cfg.addResource(mappingFiles[i]);
      }

      return cfg;
   }

   protected final Properties buildProperties() {
      Properties props = new Properties();
      this.setUnlessNull(props, "hibernate.dialect", this.dialect);
      this.setUnlessNull(props, "hibernate.default_schema", this.defaultSchema);
      this.setUnlessNull(props, "hibernate.default_catalog", this.defaultCatalog);
      this.setUnlessNull(props, "hibernate.max_fetch_depth", this.maximumFetchDepth);
      this.setUnlessNull(props, "hibernate.jdbc.fetch_size", this.jdbcFetchSize);
      this.setUnlessNull(props, "hibernate.jdbc.batch_size", this.jdbcBatchSize);
      this.setUnlessNull(props, "hibernate.jdbc.batch_versioned_data", this.batchVersionedDataEnabled);
      this.setUnlessNull(props, "hibernate.jdbc.use_scrollable_resultset", this.jdbcScrollableResultSetEnabled);
      this.setUnlessNull(props, "hibernate.jdbc.use_get_generated_keys", this.getGeneratedKeysEnabled);
      this.setUnlessNull(props, "hibernate.jdbc.use_streams_for_binary", this.streamsForBinaryEnabled);
      this.setUnlessNull(props, "hibernate.bytecode.use_reflection_optimizer", this.reflectionOptimizationEnabled);
      this.setUnlessNull(props, "hibernate.query.substitutions", this.querySubstitutions);
      this.setUnlessNull(props, "hibernate.show_sql", this.showSqlEnabled);
      this.setUnlessNull(props, "hibernate.use_sql_comments", this.commentsEnabled);
      this.setUnlessNull(props, "hibernate.cache.region.factory_class", this.cacheRegionFactory);
      this.setUnlessNull(props, "hibernate.cache.provider_configuration_file_resource_path", this.cacheProviderConfig);
      this.setUnlessNull(props, "hibernate.cache.region_prefix", this.cacheRegionPrefix);
      this.setUnlessNull(props, "hibernate.cache.use_minimal_puts", this.minimalPutsEnabled);
      this.setUnlessNull(props, "hibernate.cache.use_second_level_cache", this.secondLevelCacheEnabled);
      this.setUnlessNull(props, "hibernate.cache.use_query_cache", this.queryCacheEnabled);
      Map extraProperties = this.getExtraProperties();
      if (extraProperties != null) {
         this.addAll(props, extraProperties);
      }

      if (this.additionalProperties != null) {
         this.addAll(props, this.additionalProperties);
      }

      return props;
   }

   protected void addAll(Properties target, Map source) {
      for(Map.Entry entry : source.entrySet()) {
         String propertyName = (String)entry.getKey();
         String propertyValue = (String)entry.getValue();
         if (propertyName != null && propertyValue != null && !target.keySet().contains(propertyName) && !this.getExcludedPropertyNames().contains(propertyName)) {
            target.put(propertyName, propertyValue);
         }
      }

   }

   protected Map getExtraProperties() {
      return null;
   }

   private void setUnlessNull(Properties props, String key, String value) {
      if (value != null) {
         props.setProperty(key, value);
      }

   }

   private void verifyInt(String value) {
      if (value != null) {
         Integer.parseInt(value);
      }

   }
}
