package com.mysql.jdbc;

import java.sql.SQLException;

public interface ConnectionProperties {
   String exposeAsXml() throws SQLException;

   boolean getAllowLoadLocalInfile();

   boolean getAllowMultiQueries();

   boolean getAllowNanAndInf();

   boolean getAllowUrlInLocalInfile();

   boolean getAlwaysSendSetIsolation();

   boolean getAutoDeserialize();

   boolean getAutoGenerateTestcaseScript();

   boolean getAutoReconnectForPools();

   int getBlobSendChunkSize();

   boolean getCacheCallableStatements();

   boolean getCachePreparedStatements();

   boolean getCacheResultSetMetadata();

   boolean getCacheServerConfiguration();

   int getCallableStatementCacheSize();

   boolean getCapitalizeTypeNames();

   String getCharacterSetResults();

   boolean getClobberStreamingResults();

   String getClobCharacterEncoding();

   String getConnectionCollation();

   int getConnectTimeout();

   boolean getContinueBatchOnError();

   boolean getCreateDatabaseIfNotExist();

   int getDefaultFetchSize();

   boolean getDontTrackOpenResources();

   boolean getDumpQueriesOnException();

   boolean getDynamicCalendars();

   boolean getElideSetAutoCommits();

   boolean getEmptyStringsConvertToZero();

   boolean getEmulateLocators();

   boolean getEmulateUnsupportedPstmts();

   boolean getEnablePacketDebug();

   String getEncoding();

   boolean getExplainSlowQueries();

   boolean getFailOverReadOnly();

   boolean getGatherPerformanceMetrics();

   boolean getHoldResultsOpenOverStatementClose();

   boolean getIgnoreNonTxTables();

   int getInitialTimeout();

   boolean getInteractiveClient();

   boolean getIsInteractiveClient();

   boolean getJdbcCompliantTruncation();

   int getLocatorFetchBufferSize();

   String getLogger();

   String getLoggerClassName();

   boolean getLogSlowQueries();

   boolean getMaintainTimeStats();

   int getMaxQuerySizeToLog();

   int getMaxReconnects();

   int getMaxRows();

   int getMetadataCacheSize();

   boolean getNoDatetimeStringSync();

   boolean getNullCatalogMeansCurrent();

   boolean getNullNamePatternMatchesAll();

   int getPacketDebugBufferSize();

   boolean getParanoid();

   boolean getPedantic();

   int getPreparedStatementCacheSize();

   int getPreparedStatementCacheSqlLimit();

   boolean getProfileSql();

   boolean getProfileSQL();

   String getPropertiesTransform();

   int getQueriesBeforeRetryMaster();

   boolean getReconnectAtTxEnd();

   boolean getRelaxAutoCommit();

   int getReportMetricsIntervalMillis();

   boolean getRequireSSL();

   boolean getRollbackOnPooledClose();

   boolean getRoundRobinLoadBalance();

   boolean getRunningCTS13();

   int getSecondsBeforeRetryMaster();

   String getServerTimezone();

   String getSessionVariables();

   int getSlowQueryThresholdMillis();

   String getSocketFactoryClassName();

   int getSocketTimeout();

   boolean getStrictFloatingPoint();

   boolean getStrictUpdates();

   boolean getTinyInt1isBit();

   boolean getTraceProtocol();

   boolean getTransformedBitIsBoolean();

   boolean getUseCompression();

   boolean getUseFastIntParsing();

   boolean getUseHostsInPrivileges();

   boolean getUseInformationSchema();

   boolean getUseLocalSessionState();

   boolean getUseOldUTF8Behavior();

   boolean getUseOnlyServerErrorMessages();

   boolean getUseReadAheadInput();

   boolean getUseServerPreparedStmts();

   boolean getUseSqlStateCodes();

   boolean getUseSSL();

   boolean getUseStreamLengthsInPrepStmts();

   boolean getUseTimezone();

   boolean getUseUltraDevWorkAround();

   boolean getUseUnbufferedInput();

   boolean getUseUnicode();

   boolean getUseUsageAdvisor();

   boolean getYearIsDateType();

   String getZeroDateTimeBehavior();

   void setAllowLoadLocalInfile(boolean var1);

   void setAllowMultiQueries(boolean var1);

   void setAllowNanAndInf(boolean var1);

   void setAllowUrlInLocalInfile(boolean var1);

   void setAlwaysSendSetIsolation(boolean var1);

   void setAutoDeserialize(boolean var1);

   void setAutoGenerateTestcaseScript(boolean var1);

   void setAutoReconnect(boolean var1);

   void setAutoReconnectForConnectionPools(boolean var1);

   void setAutoReconnectForPools(boolean var1);

   void setBlobSendChunkSize(String var1) throws SQLException;

   void setCacheCallableStatements(boolean var1);

   void setCachePreparedStatements(boolean var1);

   void setCacheResultSetMetadata(boolean var1);

   void setCacheServerConfiguration(boolean var1);

   void setCallableStatementCacheSize(int var1);

   void setCapitalizeDBMDTypes(boolean var1);

   void setCapitalizeTypeNames(boolean var1);

   void setCharacterEncoding(String var1);

   void setCharacterSetResults(String var1);

   void setClobberStreamingResults(boolean var1);

   void setClobCharacterEncoding(String var1);

   void setConnectionCollation(String var1);

   void setConnectTimeout(int var1);

   void setContinueBatchOnError(boolean var1);

   void setCreateDatabaseIfNotExist(boolean var1);

   void setDefaultFetchSize(int var1);

   void setDetectServerPreparedStmts(boolean var1);

   void setDontTrackOpenResources(boolean var1);

   void setDumpQueriesOnException(boolean var1);

   void setDynamicCalendars(boolean var1);

   void setElideSetAutoCommits(boolean var1);

   void setEmptyStringsConvertToZero(boolean var1);

   void setEmulateLocators(boolean var1);

   void setEmulateUnsupportedPstmts(boolean var1);

   void setEnablePacketDebug(boolean var1);

   void setEncoding(String var1);

   void setExplainSlowQueries(boolean var1);

   void setFailOverReadOnly(boolean var1);

   void setGatherPerformanceMetrics(boolean var1);

   void setHoldResultsOpenOverStatementClose(boolean var1);

   void setIgnoreNonTxTables(boolean var1);

   void setInitialTimeout(int var1);

   void setIsInteractiveClient(boolean var1);

   void setJdbcCompliantTruncation(boolean var1);

   void setLocatorFetchBufferSize(String var1) throws SQLException;

   void setLogger(String var1);

   void setLoggerClassName(String var1);

   void setLogSlowQueries(boolean var1);

   void setMaintainTimeStats(boolean var1);

   void setMaxQuerySizeToLog(int var1);

   void setMaxReconnects(int var1);

   void setMaxRows(int var1);

   void setMetadataCacheSize(int var1);

   void setNoDatetimeStringSync(boolean var1);

   void setNullCatalogMeansCurrent(boolean var1);

   void setNullNamePatternMatchesAll(boolean var1);

   void setPacketDebugBufferSize(int var1);

   void setParanoid(boolean var1);

   void setPedantic(boolean var1);

   void setPreparedStatementCacheSize(int var1);

   void setPreparedStatementCacheSqlLimit(int var1);

   void setProfileSql(boolean var1);

   void setProfileSQL(boolean var1);

   void setPropertiesTransform(String var1);

   void setQueriesBeforeRetryMaster(int var1);

   void setReconnectAtTxEnd(boolean var1);

   void setRelaxAutoCommit(boolean var1);

   void setReportMetricsIntervalMillis(int var1);

   void setRequireSSL(boolean var1);

   void setRetainStatementAfterResultSetClose(boolean var1);

   void setRollbackOnPooledClose(boolean var1);

   void setRoundRobinLoadBalance(boolean var1);

   void setRunningCTS13(boolean var1);

   void setSecondsBeforeRetryMaster(int var1);

   void setServerTimezone(String var1);

   void setSessionVariables(String var1);

   void setSlowQueryThresholdMillis(int var1);

   void setSocketFactoryClassName(String var1);

   void setSocketTimeout(int var1);

   void setStrictFloatingPoint(boolean var1);

   void setStrictUpdates(boolean var1);

   void setTinyInt1isBit(boolean var1);

   void setTraceProtocol(boolean var1);

   void setTransformedBitIsBoolean(boolean var1);

   void setUseCompression(boolean var1);

   void setUseFastIntParsing(boolean var1);

   void setUseHostsInPrivileges(boolean var1);

   void setUseInformationSchema(boolean var1);

   void setUseLocalSessionState(boolean var1);

   void setUseOldUTF8Behavior(boolean var1);

   void setUseOnlyServerErrorMessages(boolean var1);

   void setUseReadAheadInput(boolean var1);

   void setUseServerPreparedStmts(boolean var1);

   void setUseSqlStateCodes(boolean var1);

   void setUseSSL(boolean var1);

   void setUseStreamLengthsInPrepStmts(boolean var1);

   void setUseTimezone(boolean var1);

   void setUseUltraDevWorkAround(boolean var1);

   void setUseUnbufferedInput(boolean var1);

   void setUseUnicode(boolean var1);

   void setUseUsageAdvisor(boolean var1);

   void setYearIsDateType(boolean var1);

   void setZeroDateTimeBehavior(String var1);

   boolean useUnbufferedInput();

   boolean getUseCursorFetch();

   void setUseCursorFetch(boolean var1);

   boolean getOverrideSupportsIntegrityEnhancementFacility();

   void setOverrideSupportsIntegrityEnhancementFacility(boolean var1);

   boolean getNoTimezoneConversionForTimeType();

   void setNoTimezoneConversionForTimeType(boolean var1);

   boolean getUseJDBCCompliantTimezoneShift();

   void setUseJDBCCompliantTimezoneShift(boolean var1);

   boolean getAutoClosePStmtStreams();

   void setAutoClosePStmtStreams(boolean var1);

   boolean getProcessEscapeCodesForPrepStmts();

   void setProcessEscapeCodesForPrepStmts(boolean var1);

   boolean getUseGmtMillisForDatetimes();

   void setUseGmtMillisForDatetimes(boolean var1);

   boolean getDumpMetadataOnColumnNotFound();

   void setDumpMetadataOnColumnNotFound(boolean var1);

   String getResourceId();

   void setResourceId(String var1);

   boolean getRewriteBatchedStatements();

   void setRewriteBatchedStatements(boolean var1);

   boolean getJdbcCompliantTruncationForReads();

   void setJdbcCompliantTruncationForReads(boolean var1);

   boolean getUseJvmCharsetConverters();

   void setUseJvmCharsetConverters(boolean var1);

   boolean getPinGlobalTxToPhysicalConnection();

   void setPinGlobalTxToPhysicalConnection(boolean var1);

   void setGatherPerfMetrics(boolean var1);

   boolean getGatherPerfMetrics();

   void setUltraDevHack(boolean var1);

   boolean getUltraDevHack();

   void setInteractiveClient(boolean var1);

   void setSocketFactory(String var1);

   String getSocketFactory();

   void setUseServerPrepStmts(boolean var1);

   boolean getUseServerPrepStmts();

   void setCacheCallableStmts(boolean var1);

   boolean getCacheCallableStmts();

   void setCachePrepStmts(boolean var1);

   boolean getCachePrepStmts();

   void setCallableStmtCacheSize(int var1);

   int getCallableStmtCacheSize();

   void setPrepStmtCacheSize(int var1);

   int getPrepStmtCacheSize();

   void setPrepStmtCacheSqlLimit(int var1);

   int getPrepStmtCacheSqlLimit();

   boolean getNoAccessToProcedureBodies();

   void setNoAccessToProcedureBodies(boolean var1);

   boolean getUseOldAliasMetadataBehavior();

   void setUseOldAliasMetadataBehavior(boolean var1);

   String getClientCertificateKeyStorePassword();

   void setClientCertificateKeyStorePassword(String var1);

   String getClientCertificateKeyStoreType();

   void setClientCertificateKeyStoreType(String var1);

   String getClientCertificateKeyStoreUrl();

   void setClientCertificateKeyStoreUrl(String var1);

   String getTrustCertificateKeyStorePassword();

   void setTrustCertificateKeyStorePassword(String var1);

   String getTrustCertificateKeyStoreType();

   void setTrustCertificateKeyStoreType(String var1);

   String getTrustCertificateKeyStoreUrl();

   void setTrustCertificateKeyStoreUrl(String var1);

   boolean getUseSSPSCompatibleTimezoneShift();

   void setUseSSPSCompatibleTimezoneShift(boolean var1);

   boolean getTreatUtilDateAsTimestamp();

   void setTreatUtilDateAsTimestamp(boolean var1);

   boolean getUseFastDateParsing();

   void setUseFastDateParsing(boolean var1);

   String getLocalSocketAddress();

   void setLocalSocketAddress(String var1);

   void setUseConfigs(String var1);

   String getUseConfigs();

   boolean getGenerateSimpleParameterMetadata();

   void setGenerateSimpleParameterMetadata(boolean var1);

   boolean getLogXaCommands();

   void setLogXaCommands(boolean var1);

   int getResultSetSizeThreshold();

   void setResultSetSizeThreshold(int var1);

   int getNetTimeoutForStreamingResults();

   void setNetTimeoutForStreamingResults(int var1);

   boolean getEnableQueryTimeouts();

   void setEnableQueryTimeouts(boolean var1);

   boolean getPadCharsWithSpace();

   void setPadCharsWithSpace(boolean var1);

   boolean getUseDynamicCharsetInfo();

   void setUseDynamicCharsetInfo(boolean var1);

   String getClientInfoProvider();

   void setClientInfoProvider(String var1);

   boolean getPopulateInsertRowWithDefaultValues();

   void setPopulateInsertRowWithDefaultValues(boolean var1);

   String getLoadBalanceStrategy();

   void setLoadBalanceStrategy(String var1);

   boolean getTcpNoDelay();

   void setTcpNoDelay(boolean var1);

   boolean getTcpKeepAlive();

   void setTcpKeepAlive(boolean var1);

   int getTcpRcvBuf();

   void setTcpRcvBuf(int var1);

   int getTcpSndBuf();

   void setTcpSndBuf(int var1);

   int getTcpTrafficClass();

   void setTcpTrafficClass(int var1);

   boolean getUseNanosForElapsedTime();

   void setUseNanosForElapsedTime(boolean var1);

   long getSlowQueryThresholdNanos();

   void setSlowQueryThresholdNanos(long var1);

   String getStatementInterceptors();

   void setStatementInterceptors(String var1);

   boolean getUseDirectRowUnpack();

   void setUseDirectRowUnpack(boolean var1);

   String getLargeRowSizeThreshold();

   void setLargeRowSizeThreshold(String var1);

   boolean getUseBlobToStoreUTF8OutsideBMP();

   void setUseBlobToStoreUTF8OutsideBMP(boolean var1);

   String getUtf8OutsideBmpExcludedColumnNamePattern();

   void setUtf8OutsideBmpExcludedColumnNamePattern(String var1);

   String getUtf8OutsideBmpIncludedColumnNamePattern();

   void setUtf8OutsideBmpIncludedColumnNamePattern(String var1);

   boolean getIncludeInnodbStatusInDeadlockExceptions();

   void setIncludeInnodbStatusInDeadlockExceptions(boolean var1);

   boolean getBlobsAreStrings();

   void setBlobsAreStrings(boolean var1);

   boolean getFunctionsNeverReturnBlobs();

   void setFunctionsNeverReturnBlobs(boolean var1);

   boolean getAutoSlowLog();

   void setAutoSlowLog(boolean var1);

   String getConnectionLifecycleInterceptors();

   void setConnectionLifecycleInterceptors(String var1);

   String getProfilerEventHandler();

   void setProfilerEventHandler(String var1);

   boolean getVerifyServerCertificate();

   void setVerifyServerCertificate(boolean var1);

   boolean getUseLegacyDatetimeCode();

   void setUseLegacyDatetimeCode(boolean var1);

   int getSelfDestructOnPingSecondsLifetime();

   void setSelfDestructOnPingSecondsLifetime(int var1);

   int getSelfDestructOnPingMaxOperations();

   void setSelfDestructOnPingMaxOperations(int var1);
}
