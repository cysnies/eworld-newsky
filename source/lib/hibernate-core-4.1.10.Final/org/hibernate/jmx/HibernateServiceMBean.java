package org.hibernate.jmx;

import org.hibernate.HibernateException;

/** @deprecated */
@Deprecated
public interface HibernateServiceMBean {
   String getMapResources();

   void setMapResources(String var1);

   void addMapResource(String var1);

   void setProperty(String var1, String var2);

   String getProperty(String var1);

   String getPropertyList();

   String getDatasource();

   void setDatasource(String var1);

   String getUserName();

   void setUserName(String var1);

   String getPassword();

   void setPassword(String var1);

   String getDialect();

   void setDialect(String var1);

   String getJndiName();

   void setJndiName(String var1);

   String getTransactionStrategy();

   void setTransactionStrategy(String var1);

   String getUserTransactionName();

   void setUserTransactionName(String var1);

   String getJtaPlatformName();

   void setJtaPlatformName(String var1);

   String getShowSqlEnabled();

   void setShowSqlEnabled(String var1);

   String getMaximumFetchDepth();

   void setMaximumFetchDepth(String var1);

   String getJdbcBatchSize();

   void setJdbcBatchSize(String var1);

   String getJdbcFetchSize();

   void setJdbcFetchSize(String var1);

   String getQuerySubstitutions();

   void setQuerySubstitutions(String var1);

   String getDefaultSchema();

   void setDefaultSchema(String var1);

   String getDefaultCatalog();

   void setDefaultCatalog(String var1);

   String getJdbcScrollableResultSetEnabled();

   void setJdbcScrollableResultSetEnabled(String var1);

   String getGetGeneratedKeysEnabled();

   void setGetGeneratedKeysEnabled(String var1);

   String getCacheRegionFactory();

   void setCacheRegionFactory(String var1);

   String getCacheProviderConfig();

   void setCacheProviderConfig(String var1);

   String getQueryCacheEnabled();

   void setQueryCacheEnabled(String var1);

   String getSecondLevelCacheEnabled();

   void setSecondLevelCacheEnabled(String var1);

   String getCacheRegionPrefix();

   void setCacheRegionPrefix(String var1);

   String getMinimalPutsEnabled();

   void setMinimalPutsEnabled(String var1);

   String getCommentsEnabled();

   void setCommentsEnabled(String var1);

   String getBatchVersionedDataEnabled();

   void setBatchVersionedDataEnabled(String var1);

   void setFlushBeforeCompletionEnabled(String var1);

   String getFlushBeforeCompletionEnabled();

   void setAutoCloseSessionEnabled(String var1);

   String getAutoCloseSessionEnabled();

   void createSchema() throws HibernateException;

   void dropSchema() throws HibernateException;

   void start() throws HibernateException;

   void stop();
}
