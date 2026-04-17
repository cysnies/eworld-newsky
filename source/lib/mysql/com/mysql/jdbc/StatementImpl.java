package com.mysql.jdbc;

import com.mysql.jdbc.exceptions.MySQLStatementCancelledException;
import com.mysql.jdbc.exceptions.MySQLTimeoutException;
import com.mysql.jdbc.profiler.ProfilerEvent;
import com.mysql.jdbc.profiler.ProfilerEventHandler;
import com.mysql.jdbc.profiler.ProfilerEventHandlerFactory;
import java.io.InputStream;
import java.sql.BatchUpdateException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimerTask;

public class StatementImpl implements Statement {
   protected static final String PING_MARKER = "/* ping */";
   protected Object cancelTimeoutMutex = new Object();
   protected static int statementCounter = 1;
   public static final byte USES_VARIABLES_FALSE = 0;
   public static final byte USES_VARIABLES_TRUE = 1;
   public static final byte USES_VARIABLES_UNKNOWN = -1;
   protected boolean wasCancelled = false;
   protected boolean wasCancelledByTimeout = false;
   protected List batchedArgs;
   protected SingleByteCharsetConverter charConverter = null;
   protected String charEncoding = null;
   protected ConnectionImpl connection = null;
   protected long connectionId = 0L;
   protected String currentCatalog = null;
   protected boolean doEscapeProcessing = true;
   protected ProfilerEventHandler eventSink = null;
   private int fetchSize = 0;
   protected boolean isClosed = false;
   protected long lastInsertId = -1L;
   protected int maxFieldSize = MysqlIO.getMaxBuf();
   protected int maxRows = -1;
   protected boolean maxRowsChanged = false;
   protected List openResults = new ArrayList();
   protected boolean pedantic = false;
   protected Throwable pointOfOrigin;
   protected boolean profileSQL = false;
   protected ResultSetInternalMethods results = null;
   protected int resultSetConcurrency = 0;
   protected int resultSetType = 0;
   protected int statementId;
   protected int timeoutInMillis = 0;
   protected long updateCount = -1L;
   protected boolean useUsageAdvisor = false;
   protected SQLWarning warningChain = null;
   protected boolean holdResultsOpenOverClose = false;
   protected ArrayList batchedGeneratedKeys = null;
   protected boolean retrieveGeneratedKeys = false;
   protected boolean continueBatchOnError = false;
   protected PingTarget pingTarget = null;
   protected boolean useLegacyDatetimeCode;
   private int originalResultSetType = 0;
   private int originalFetchSize = 0;
   private boolean isPoolable = true;
   private InputStream localInfileInputStream;

   public StatementImpl(ConnectionImpl c, String catalog) throws SQLException {
      if (c != null && !c.isClosed()) {
         this.connection = c;
         this.connectionId = this.connection.getId();
         this.currentCatalog = catalog;
         this.pedantic = this.connection.getPedantic();
         this.continueBatchOnError = this.connection.getContinueBatchOnError();
         this.useLegacyDatetimeCode = this.connection.getUseLegacyDatetimeCode();
         if (!this.connection.getDontTrackOpenResources()) {
            this.connection.registerStatement(this);
         }

         if (this.connection != null) {
            this.maxFieldSize = this.connection.getMaxAllowedPacket();
            int defaultFetchSize = this.connection.getDefaultFetchSize();
            if (defaultFetchSize != 0) {
               this.setFetchSize(defaultFetchSize);
            }
         }

         if (this.connection.getUseUnicode()) {
            this.charEncoding = this.connection.getEncoding();
            this.charConverter = this.connection.getCharsetConverter(this.charEncoding);
         }

         boolean profiling = this.connection.getProfileSql() || this.connection.getUseUsageAdvisor();
         if (this.connection.getAutoGenerateTestcaseScript() || profiling) {
            this.statementId = statementCounter++;
         }

         if (profiling) {
            this.pointOfOrigin = new Throwable();
            this.profileSQL = this.connection.getProfileSql();
            this.useUsageAdvisor = this.connection.getUseUsageAdvisor();
            this.eventSink = ProfilerEventHandlerFactory.getInstance(this.connection);
         }

         int maxRowsConn = this.connection.getMaxRows();
         if (maxRowsConn != -1) {
            this.setMaxRows(maxRowsConn);
         }

      } else {
         throw SQLError.createSQLException(Messages.getString("Statement.0"), "08003");
      }
   }

   public synchronized void addBatch(String sql) throws SQLException {
      if (this.batchedArgs == null) {
         this.batchedArgs = new ArrayList();
      }

      if (sql != null) {
         this.batchedArgs.add(sql);
      }

   }

   public void cancel() throws SQLException {
      if (!this.isClosed && this.connection != null && this.connection.versionMeetsMinimum(5, 0, 0)) {
         Connection cancelConn = null;
         java.sql.Statement cancelStmt = null;

         try {
            cancelConn = this.connection.duplicate();
            cancelStmt = cancelConn.createStatement();
            cancelStmt.execute("KILL QUERY " + this.connection.getIO().getThreadId());
            this.wasCancelled = true;
         } finally {
            if (cancelStmt != null) {
               cancelStmt.close();
            }

            if (cancelConn != null) {
               cancelConn.close();
            }

         }
      }

   }

   protected void checkClosed() throws SQLException {
      if (this.isClosed) {
         throw SQLError.createSQLException(Messages.getString("Statement.49"), "08003");
      }
   }

   protected void checkForDml(String sql, char firstStatementChar) throws SQLException {
      if (firstStatementChar == 'I' || firstStatementChar == 'U' || firstStatementChar == 'D' || firstStatementChar == 'A' || firstStatementChar == 'C') {
         String noCommentSql = StringUtils.stripComments(sql, "'\"", "'\"", true, false, true, true);
         if (StringUtils.startsWithIgnoreCaseAndWs(noCommentSql, "INSERT") || StringUtils.startsWithIgnoreCaseAndWs(noCommentSql, "UPDATE") || StringUtils.startsWithIgnoreCaseAndWs(noCommentSql, "DELETE") || StringUtils.startsWithIgnoreCaseAndWs(noCommentSql, "DROP") || StringUtils.startsWithIgnoreCaseAndWs(noCommentSql, "CREATE") || StringUtils.startsWithIgnoreCaseAndWs(noCommentSql, "ALTER")) {
            throw SQLError.createSQLException(Messages.getString("Statement.57"), "S1009");
         }
      }

   }

   protected void checkNullOrEmptyQuery(String sql) throws SQLException {
      if (sql == null) {
         throw SQLError.createSQLException(Messages.getString("Statement.59"), "S1009");
      } else if (sql.length() == 0) {
         throw SQLError.createSQLException(Messages.getString("Statement.61"), "S1009");
      }
   }

   public synchronized void clearBatch() throws SQLException {
      if (this.batchedArgs != null) {
         this.batchedArgs.clear();
      }

   }

   public void clearWarnings() throws SQLException {
      this.warningChain = null;
   }

   public synchronized void close() throws SQLException {
      this.realClose(true, true);
   }

   protected void closeAllOpenResults() {
      if (this.openResults != null) {
         for(ResultSetInternalMethods element : this.openResults) {
            try {
               element.realClose(false);
            } catch (SQLException sqlEx) {
               AssertionFailedException.shouldNotHappen(sqlEx);
            }
         }

         this.openResults.clear();
      }

   }

   private ResultSetInternalMethods createResultSetUsingServerFetch(String sql) throws SQLException {
      java.sql.PreparedStatement pStmt = this.connection.prepareStatement(sql, this.resultSetType, this.resultSetConcurrency);
      pStmt.setFetchSize(this.fetchSize);
      if (this.maxRows > -1) {
         pStmt.setMaxRows(this.maxRows);
      }

      pStmt.execute();
      ResultSetInternalMethods rs = ((StatementImpl)pStmt).getResultSetInternal();
      rs.setStatementUsedForFetchingRows((PreparedStatement)pStmt);
      this.results = rs;
      return rs;
   }

   protected boolean createStreamingResultSet() {
      return this.resultSetType == 1003 && this.resultSetConcurrency == 1007 && this.fetchSize == Integer.MIN_VALUE;
   }

   public void enableStreamingResults() throws SQLException {
      this.originalResultSetType = this.resultSetType;
      this.originalFetchSize = this.fetchSize;
      this.setFetchSize(Integer.MIN_VALUE);
      this.setResultSetType(1003);
   }

   public void disableStreamingResults() throws SQLException {
      if (this.fetchSize == Integer.MIN_VALUE && this.resultSetType == 1003) {
         this.setFetchSize(this.originalFetchSize);
         this.setResultSetType(this.originalResultSetType);
      }

   }

   public boolean execute(String sql) throws SQLException {
      this.checkClosed();
      ConnectionImpl locallyScopedConn = this.connection;
      synchronized(locallyScopedConn.getMutex()) {
         this.resetCancelledState();
         this.checkNullOrEmptyQuery(sql);
         this.checkClosed();
         char firstNonWsChar = StringUtils.firstAlphaCharUc(sql, this.findStartOfStatement(sql));
         boolean isSelect = true;
         if (firstNonWsChar != 'S') {
            isSelect = false;
            if (locallyScopedConn.isReadOnly()) {
               throw SQLError.createSQLException(Messages.getString("Statement.27") + Messages.getString("Statement.28"), "S1009");
            }
         }

         boolean doStreaming = this.createStreamingResultSet();
         if (doStreaming && this.connection.getNetTimeoutForStreamingResults() > 0) {
            this.executeSimpleNonQuery(locallyScopedConn, "SET net_write_timeout=" + this.connection.getNetTimeoutForStreamingResults());
         }

         if (this.doEscapeProcessing) {
            Object escapedSqlResult = EscapeProcessor.escapeSQL(sql, locallyScopedConn.serverSupportsConvertFn(), locallyScopedConn);
            if (escapedSqlResult instanceof String) {
               sql = (String)escapedSqlResult;
            } else {
               sql = ((EscapeProcessorResult)escapedSqlResult).escapedSql;
            }
         }

         if (this.results != null && !locallyScopedConn.getHoldResultsOpenOverStatementClose()) {
            this.results.realClose(false);
         }

         if (sql.charAt(0) == '/' && sql.startsWith("/* ping */")) {
            this.doPingInstead();
            return true;
         } else {
            CachedResultSetMetaData cachedMetaData = null;
            ResultSetInternalMethods rs = null;
            this.batchedGeneratedKeys = null;
            if (this.useServerFetch()) {
               rs = this.createResultSetUsingServerFetch(sql);
            } else {
               CancelTask timeoutTask = null;
               String oldCatalog = null;

               try {
                  if (locallyScopedConn.getEnableQueryTimeouts() && this.timeoutInMillis != 0 && locallyScopedConn.versionMeetsMinimum(5, 0, 0)) {
                     timeoutTask = new CancelTask(this);
                     ConnectionImpl.getCancelTimer().schedule(timeoutTask, (long)this.timeoutInMillis);
                  }

                  if (!locallyScopedConn.getCatalog().equals(this.currentCatalog)) {
                     oldCatalog = locallyScopedConn.getCatalog();
                     locallyScopedConn.setCatalog(this.currentCatalog);
                  }

                  Field[] cachedFields = null;
                  if (locallyScopedConn.getCacheResultSetMetadata()) {
                     cachedMetaData = locallyScopedConn.getCachedMetaData(sql);
                     if (cachedMetaData != null) {
                        cachedFields = cachedMetaData.fields;
                     }
                  }

                  if (locallyScopedConn.useMaxRows()) {
                     int rowLimit = -1;
                     if (isSelect) {
                        if (StringUtils.indexOfIgnoreCase(sql, "LIMIT") != -1) {
                           rowLimit = this.maxRows;
                        } else if (this.maxRows <= 0) {
                           this.executeSimpleNonQuery(locallyScopedConn, "SET OPTION SQL_SELECT_LIMIT=DEFAULT");
                        } else {
                           this.executeSimpleNonQuery(locallyScopedConn, "SET OPTION SQL_SELECT_LIMIT=" + this.maxRows);
                        }
                     } else {
                        this.executeSimpleNonQuery(locallyScopedConn, "SET OPTION SQL_SELECT_LIMIT=DEFAULT");
                     }

                     rs = locallyScopedConn.execSQL(this, sql, rowLimit, (Buffer)null, this.resultSetType, this.resultSetConcurrency, doStreaming, this.currentCatalog, cachedFields);
                  } else {
                     rs = locallyScopedConn.execSQL(this, sql, -1, (Buffer)null, this.resultSetType, this.resultSetConcurrency, doStreaming, this.currentCatalog, cachedFields);
                  }

                  if (timeoutTask != null) {
                     if (timeoutTask.caughtWhileCancelling != null) {
                        throw timeoutTask.caughtWhileCancelling;
                     }

                     timeoutTask.cancel();
                     timeoutTask = null;
                  }

                  synchronized(this.cancelTimeoutMutex) {
                     if (this.wasCancelled) {
                        SQLException cause = null;
                        Object var26;
                        if (this.wasCancelledByTimeout) {
                           var26 = new MySQLTimeoutException();
                        } else {
                           var26 = new MySQLStatementCancelledException();
                        }

                        this.resetCancelledState();
                        throw var26;
                     }
                  }
               } finally {
                  if (timeoutTask != null) {
                     timeoutTask.cancel();
                  }

                  if (oldCatalog != null) {
                     locallyScopedConn.setCatalog(oldCatalog);
                  }

               }
            }

            if (rs != null) {
               this.lastInsertId = rs.getUpdateID();
               this.results = rs;
               rs.setFirstCharOfQuery(firstNonWsChar);
               if (rs.reallyResult()) {
                  if (cachedMetaData != null) {
                     locallyScopedConn.initializeResultsMetadataFromCache(sql, cachedMetaData, this.results);
                  } else if (this.connection.getCacheResultSetMetadata()) {
                     locallyScopedConn.initializeResultsMetadataFromCache(sql, (CachedResultSetMetaData)null, this.results);
                  }
               }
            }

            return rs != null && rs.reallyResult();
         }
      }
   }

   protected synchronized void resetCancelledState() {
      if (this.cancelTimeoutMutex != null) {
         synchronized(this.cancelTimeoutMutex) {
            this.wasCancelled = false;
            this.wasCancelledByTimeout = false;
         }
      }
   }

   public boolean execute(String sql, int returnGeneratedKeys) throws SQLException {
      if (returnGeneratedKeys == 1) {
         this.checkClosed();
         ConnectionImpl locallyScopedConn = this.connection;
         synchronized(locallyScopedConn.getMutex()) {
            boolean readInfoMsgState = this.connection.isReadInfoMsgEnabled();
            locallyScopedConn.setReadInfoMsgEnabled(true);

            boolean var6;
            try {
               var6 = this.execute(sql);
            } finally {
               locallyScopedConn.setReadInfoMsgEnabled(readInfoMsgState);
            }

            return var6;
         }
      } else {
         return this.execute(sql);
      }
   }

   public boolean execute(String sql, int[] generatedKeyIndices) throws SQLException {
      if (generatedKeyIndices != null && generatedKeyIndices.length > 0) {
         this.checkClosed();
         ConnectionImpl locallyScopedConn = this.connection;
         synchronized(locallyScopedConn.getMutex()) {
            boolean readInfoMsgState = locallyScopedConn.isReadInfoMsgEnabled();
            locallyScopedConn.setReadInfoMsgEnabled(true);

            boolean var6;
            try {
               var6 = this.execute(sql);
            } finally {
               locallyScopedConn.setReadInfoMsgEnabled(readInfoMsgState);
            }

            return var6;
         }
      } else {
         return this.execute(sql);
      }
   }

   public boolean execute(String sql, String[] generatedKeyNames) throws SQLException {
      if (generatedKeyNames != null && generatedKeyNames.length > 0) {
         this.checkClosed();
         ConnectionImpl locallyScopedConn = this.connection;
         synchronized(locallyScopedConn.getMutex()) {
            boolean readInfoMsgState = this.connection.isReadInfoMsgEnabled();
            locallyScopedConn.setReadInfoMsgEnabled(true);

            boolean var6;
            try {
               var6 = this.execute(sql);
            } finally {
               locallyScopedConn.setReadInfoMsgEnabled(readInfoMsgState);
            }

            return var6;
         }
      } else {
         return this.execute(sql);
      }
   }

   public synchronized int[] executeBatch() throws SQLException {
      this.checkClosed();
      ConnectionImpl locallyScopedConn = this.connection;
      if (locallyScopedConn.isReadOnly()) {
         throw SQLError.createSQLException(Messages.getString("Statement.34") + Messages.getString("Statement.35"), "S1009");
      } else {
         if (this.results != null && !locallyScopedConn.getHoldResultsOpenOverStatementClose()) {
            this.results.realClose(false);
         }

         synchronized(locallyScopedConn.getMutex()) {
            if (this.batchedArgs != null && this.batchedArgs.size() != 0) {
               int individualStatementTimeout = this.timeoutInMillis;
               this.timeoutInMillis = 0;
               CancelTask timeoutTask = null;

               int[] var21;
               try {
                  this.resetCancelledState();
                  this.retrieveGeneratedKeys = true;
                  int[] updateCounts = null;
                  if (this.batchedArgs != null) {
                     int nbrCommands = this.batchedArgs.size();
                     this.batchedGeneratedKeys = new ArrayList(this.batchedArgs.size());
                     boolean multiQueriesEnabled = locallyScopedConn.getAllowMultiQueries();
                     if (locallyScopedConn.versionMeetsMinimum(4, 1, 1) && (multiQueriesEnabled || locallyScopedConn.getRewriteBatchedStatements() && nbrCommands > 4)) {
                        int[] var8 = this.executeBatchUsingMultiQueries(multiQueriesEnabled, nbrCommands, individualStatementTimeout);
                        return var8;
                     }

                     if (locallyScopedConn.getEnableQueryTimeouts() && individualStatementTimeout != 0 && locallyScopedConn.versionMeetsMinimum(5, 0, 0)) {
                        timeoutTask = new CancelTask(this);
                        ConnectionImpl.getCancelTimer().schedule(timeoutTask, (long)individualStatementTimeout);
                     }

                     updateCounts = new int[nbrCommands];

                     for(int i = 0; i < nbrCommands; ++i) {
                        updateCounts[i] = -3;
                     }

                     SQLException sqlEx = null;
                     int commandIndex = 0;

                     for(int var24 = 0; var24 < nbrCommands; ++var24) {
                        try {
                           updateCounts[var24] = this.executeUpdate((String)this.batchedArgs.get(var24), true);
                           this.getBatchedGeneratedKeys();
                        } catch (SQLException var18) {
                           updateCounts[var24] = -3;
                           if (!this.continueBatchOnError || var18 instanceof MySQLTimeoutException || var18 instanceof MySQLStatementCancelledException) {
                              int[] newUpdateCounts = new int[var24];
                              System.arraycopy(updateCounts, 0, newUpdateCounts, 0, var24);
                              throw new BatchUpdateException(var18.getMessage(), var18.getSQLState(), var18.getErrorCode(), newUpdateCounts);
                           }

                           sqlEx = var18;
                        }
                     }

                     if (sqlEx != null) {
                        throw new BatchUpdateException(sqlEx.getMessage(), sqlEx.getSQLState(), sqlEx.getErrorCode(), updateCounts);
                     }
                  }

                  if (timeoutTask != null) {
                     if (timeoutTask.caughtWhileCancelling != null) {
                        throw timeoutTask.caughtWhileCancelling;
                     }

                     timeoutTask.cancel();
                     timeoutTask = null;
                  }

                  var21 = updateCounts != null ? updateCounts : new int[0];
               } finally {
                  if (timeoutTask != null) {
                     timeoutTask.cancel();
                  }

                  this.resetCancelledState();
                  this.timeoutInMillis = individualStatementTimeout;
                  this.retrieveGeneratedKeys = false;
                  this.clearBatch();
               }

               return var21;
            } else {
               return new int[0];
            }
         }
      }
   }

   private int[] executeBatchUsingMultiQueries(boolean multiQueriesEnabled, int nbrCommands, int individualStatementTimeout) throws SQLException {
      ConnectionImpl locallyScopedConn = this.connection;
      if (!multiQueriesEnabled) {
         locallyScopedConn.getIO().enableMultiQueries();
      }

      java.sql.Statement batchStmt = null;
      CancelTask timeoutTask = null;

      int[] ex;
      try {
         int[] updateCounts = new int[nbrCommands];

         for(int i = 0; i < nbrCommands; ++i) {
            updateCounts[i] = -3;
         }

         int commandIndex = 0;
         StringBuffer queryBuf = new StringBuffer();
         batchStmt = locallyScopedConn.createStatement();
         if (locallyScopedConn.getEnableQueryTimeouts() && individualStatementTimeout != 0 && locallyScopedConn.versionMeetsMinimum(5, 0, 0)) {
            timeoutTask = new CancelTask((StatementImpl)batchStmt);
            ConnectionImpl.getCancelTimer().schedule(timeoutTask, (long)individualStatementTimeout);
         }

         int counter = 0;
         int numberOfBytesPerChar = 1;
         String connectionEncoding = locallyScopedConn.getEncoding();
         if (StringUtils.startsWithIgnoreCase(connectionEncoding, "utf")) {
            numberOfBytesPerChar = 3;
         } else if (CharsetMapping.isMultibyteCharset(connectionEncoding)) {
            numberOfBytesPerChar = 2;
         }

         int escapeAdjust = 1;
         if (this.doEscapeProcessing) {
            escapeAdjust = 2;
         }

         SQLException sqlEx = null;
         int argumentSetsInBatchSoFar = 0;

         for(commandIndex = 0; commandIndex < nbrCommands; ++commandIndex) {
            String nextQuery = (String)this.batchedArgs.get(commandIndex);
            if (((queryBuf.length() + nextQuery.length()) * numberOfBytesPerChar + 1 + 4) * escapeAdjust + 32 > this.connection.getMaxAllowedPacket()) {
               try {
                  batchStmt.execute(queryBuf.toString());
               } catch (SQLException ex) {
                  sqlEx = this.handleExceptionForBatch(commandIndex, argumentSetsInBatchSoFar, updateCounts, ex);
               }

               counter = this.processMultiCountsAndKeys((StatementImpl)batchStmt, counter, updateCounts);
               queryBuf = new StringBuffer();
               argumentSetsInBatchSoFar = 0;
            }

            queryBuf.append(nextQuery);
            queryBuf.append(";");
            ++argumentSetsInBatchSoFar;
         }

         if (queryBuf.length() > 0) {
            try {
               batchStmt.execute(queryBuf.toString());
            } catch (SQLException ex) {
               sqlEx = this.handleExceptionForBatch(commandIndex - 1, argumentSetsInBatchSoFar, updateCounts, ex);
            }

            this.processMultiCountsAndKeys((StatementImpl)batchStmt, counter, updateCounts);
         }

         if (timeoutTask != null) {
            if (timeoutTask.caughtWhileCancelling != null) {
               throw timeoutTask.caughtWhileCancelling;
            }

            timeoutTask.cancel();
            timeoutTask = null;
         }

         if (sqlEx != null) {
            throw new BatchUpdateException(sqlEx.getMessage(), sqlEx.getSQLState(), sqlEx.getErrorCode(), updateCounts);
         }

         ex = updateCounts != null ? updateCounts : new int[0];
      } finally {
         if (timeoutTask != null) {
            timeoutTask.cancel();
         }

         this.resetCancelledState();

         try {
            if (batchStmt != null) {
               batchStmt.close();
            }
         } finally {
            if (!multiQueriesEnabled) {
               locallyScopedConn.getIO().disableMultiQueries();
            }

         }

      }

      return ex;
   }

   protected int processMultiCountsAndKeys(StatementImpl batchedStatement, int updateCountCounter, int[] updateCounts) throws SQLException {
      updateCounts[updateCountCounter++] = batchedStatement.getUpdateCount();
      boolean doGenKeys = this.batchedGeneratedKeys != null;
      byte[][] row = (byte[][])null;
      if (doGenKeys) {
         long generatedKey = batchedStatement.getLastInsertID();
         row = new byte[][]{Long.toString(generatedKey).getBytes()};
         this.batchedGeneratedKeys.add(new ByteArrayRow(row));
      }

      while(batchedStatement.getMoreResults() || batchedStatement.getUpdateCount() != -1) {
         updateCounts[updateCountCounter++] = batchedStatement.getUpdateCount();
         if (doGenKeys) {
            long generatedKey = batchedStatement.getLastInsertID();
            row = new byte[][]{Long.toString(generatedKey).getBytes()};
            this.batchedGeneratedKeys.add(new ByteArrayRow(row));
         }
      }

      return updateCountCounter;
   }

   protected SQLException handleExceptionForBatch(int endOfBatchIndex, int numValuesPerBatch, int[] updateCounts, SQLException ex) throws BatchUpdateException {
      for(int j = endOfBatchIndex; j > endOfBatchIndex - numValuesPerBatch; --j) {
         updateCounts[j] = -3;
      }

      if (this.continueBatchOnError && !(ex instanceof MySQLTimeoutException) && !(ex instanceof MySQLStatementCancelledException)) {
         return ex;
      } else {
         int[] newUpdateCounts = new int[endOfBatchIndex];
         System.arraycopy(updateCounts, 0, newUpdateCounts, 0, endOfBatchIndex);
         throw new BatchUpdateException(ex.getMessage(), ex.getSQLState(), ex.getErrorCode(), newUpdateCounts);
      }
   }

   public ResultSet executeQuery(String sql) throws SQLException {
      this.checkClosed();
      ConnectionImpl locallyScopedConn = this.connection;
      synchronized(locallyScopedConn.getMutex()) {
         this.resetCancelledState();
         this.checkNullOrEmptyQuery(sql);
         boolean doStreaming = this.createStreamingResultSet();
         if (doStreaming && this.connection.getNetTimeoutForStreamingResults() > 0) {
            this.executeSimpleNonQuery(locallyScopedConn, "SET net_write_timeout=" + this.connection.getNetTimeoutForStreamingResults());
         }

         if (this.doEscapeProcessing) {
            Object escapedSqlResult = EscapeProcessor.escapeSQL(sql, locallyScopedConn.serverSupportsConvertFn(), this.connection);
            if (escapedSqlResult instanceof String) {
               sql = (String)escapedSqlResult;
            } else {
               sql = ((EscapeProcessorResult)escapedSqlResult).escapedSql;
            }
         }

         char firstStatementChar = StringUtils.firstNonWsCharUc(sql, this.findStartOfStatement(sql));
         if (sql.charAt(0) == '/' && sql.startsWith("/* ping */")) {
            this.doPingInstead();
            return this.results;
         } else {
            this.checkForDml(sql, firstStatementChar);
            if (this.results != null && !locallyScopedConn.getHoldResultsOpenOverStatementClose()) {
               this.results.realClose(false);
            }

            CachedResultSetMetaData cachedMetaData = null;
            if (this.useServerFetch()) {
               this.results = this.createResultSetUsingServerFetch(sql);
               return this.results;
            } else {
               CancelTask timeoutTask = null;
               String oldCatalog = null;

               try {
                  if (locallyScopedConn.getEnableQueryTimeouts() && this.timeoutInMillis != 0 && locallyScopedConn.versionMeetsMinimum(5, 0, 0)) {
                     timeoutTask = new CancelTask(this);
                     ConnectionImpl.getCancelTimer().schedule(timeoutTask, (long)this.timeoutInMillis);
                  }

                  if (!locallyScopedConn.getCatalog().equals(this.currentCatalog)) {
                     oldCatalog = locallyScopedConn.getCatalog();
                     locallyScopedConn.setCatalog(this.currentCatalog);
                  }

                  Field[] cachedFields = null;
                  if (locallyScopedConn.getCacheResultSetMetadata()) {
                     cachedMetaData = locallyScopedConn.getCachedMetaData(sql);
                     if (cachedMetaData != null) {
                        cachedFields = cachedMetaData.fields;
                     }
                  }

                  if (locallyScopedConn.useMaxRows()) {
                     if (StringUtils.indexOfIgnoreCase(sql, "LIMIT") != -1) {
                        this.results = locallyScopedConn.execSQL(this, sql, this.maxRows, (Buffer)null, this.resultSetType, this.resultSetConcurrency, doStreaming, this.currentCatalog, cachedFields);
                     } else {
                        if (this.maxRows <= 0) {
                           this.executeSimpleNonQuery(locallyScopedConn, "SET OPTION SQL_SELECT_LIMIT=DEFAULT");
                        } else {
                           this.executeSimpleNonQuery(locallyScopedConn, "SET OPTION SQL_SELECT_LIMIT=" + this.maxRows);
                        }

                        this.results = locallyScopedConn.execSQL(this, sql, -1, (Buffer)null, this.resultSetType, this.resultSetConcurrency, doStreaming, this.currentCatalog, cachedFields);
                        if (oldCatalog != null) {
                           locallyScopedConn.setCatalog(oldCatalog);
                        }
                     }
                  } else {
                     this.results = locallyScopedConn.execSQL(this, sql, -1, (Buffer)null, this.resultSetType, this.resultSetConcurrency, doStreaming, this.currentCatalog, cachedFields);
                  }

                  if (timeoutTask != null) {
                     if (timeoutTask.caughtWhileCancelling != null) {
                        throw timeoutTask.caughtWhileCancelling;
                     }

                     timeoutTask.cancel();
                     timeoutTask = null;
                  }

                  synchronized(this.cancelTimeoutMutex) {
                     if (this.wasCancelled) {
                        SQLException cause = null;
                        Object var23;
                        if (this.wasCancelledByTimeout) {
                           var23 = new MySQLTimeoutException();
                        } else {
                           var23 = new MySQLStatementCancelledException();
                        }

                        this.resetCancelledState();
                        throw var23;
                     }
                  }
               } finally {
                  if (timeoutTask != null) {
                     timeoutTask.cancel();
                  }

                  if (oldCatalog != null) {
                     locallyScopedConn.setCatalog(oldCatalog);
                  }

               }

               this.lastInsertId = this.results.getUpdateID();
               if (cachedMetaData != null) {
                  locallyScopedConn.initializeResultsMetadataFromCache(sql, cachedMetaData, this.results);
               } else if (this.connection.getCacheResultSetMetadata()) {
                  locallyScopedConn.initializeResultsMetadataFromCache(sql, (CachedResultSetMetaData)null, this.results);
               }

               return this.results;
            }
         }
      }
   }

   protected void doPingInstead() throws SQLException {
      if (this.pingTarget != null) {
         this.pingTarget.doPing();
      } else {
         this.connection.ping();
      }

      ResultSetInternalMethods fakeSelectOneResultSet = this.generatePingResultSet();
      this.results = fakeSelectOneResultSet;
   }

   protected ResultSetInternalMethods generatePingResultSet() throws SQLException {
      Field[] fields = new Field[]{new Field((String)null, "1", -5, 1)};
      ArrayList rows = new ArrayList();
      byte[] colVal = new byte[]{49};
      rows.add(new ByteArrayRow(new byte[][]{colVal}));
      return (ResultSetInternalMethods)DatabaseMetaData.buildResultSet(fields, rows, this.connection);
   }

   protected void executeSimpleNonQuery(ConnectionImpl c, String nonQuery) throws SQLException {
      c.execSQL(this, nonQuery, -1, (Buffer)null, 1003, 1007, false, this.currentCatalog, (Field[])null, false).close();
   }

   public int executeUpdate(String sql) throws SQLException {
      return this.executeUpdate(sql, false);
   }

   protected int executeUpdate(String sql, boolean isBatch) throws SQLException {
      this.checkClosed();
      ConnectionImpl locallyScopedConn = this.connection;
      char firstStatementChar = StringUtils.firstAlphaCharUc(sql, this.findStartOfStatement(sql));
      ResultSetInternalMethods rs = null;
      synchronized(locallyScopedConn.getMutex()) {
         this.resetCancelledState();
         this.checkNullOrEmptyQuery(sql);
         if (this.doEscapeProcessing) {
            Object escapedSqlResult = EscapeProcessor.escapeSQL(sql, this.connection.serverSupportsConvertFn(), this.connection);
            if (escapedSqlResult instanceof String) {
               sql = (String)escapedSqlResult;
            } else {
               sql = ((EscapeProcessorResult)escapedSqlResult).escapedSql;
            }
         }

         if (locallyScopedConn.isReadOnly()) {
            throw SQLError.createSQLException(Messages.getString("Statement.42") + Messages.getString("Statement.43"), "S1009");
         }

         if (StringUtils.startsWithIgnoreCaseAndWs(sql, "select")) {
            throw SQLError.createSQLException(Messages.getString("Statement.46"), "01S03");
         }

         if (this.results != null && !locallyScopedConn.getHoldResultsOpenOverStatementClose()) {
            this.results.realClose(false);
         }

         CancelTask timeoutTask = null;
         String oldCatalog = null;

         try {
            if (locallyScopedConn.getEnableQueryTimeouts() && this.timeoutInMillis != 0 && locallyScopedConn.versionMeetsMinimum(5, 0, 0)) {
               timeoutTask = new CancelTask(this);
               ConnectionImpl.getCancelTimer().schedule(timeoutTask, (long)this.timeoutInMillis);
            }

            if (!locallyScopedConn.getCatalog().equals(this.currentCatalog)) {
               oldCatalog = locallyScopedConn.getCatalog();
               locallyScopedConn.setCatalog(this.currentCatalog);
            }

            if (locallyScopedConn.useMaxRows()) {
               this.executeSimpleNonQuery(locallyScopedConn, "SET OPTION SQL_SELECT_LIMIT=DEFAULT");
            }

            rs = locallyScopedConn.execSQL(this, sql, -1, (Buffer)null, 1003, 1007, false, this.currentCatalog, (Field[])null, isBatch);
            if (timeoutTask != null) {
               if (timeoutTask.caughtWhileCancelling != null) {
                  throw timeoutTask.caughtWhileCancelling;
               }

               timeoutTask.cancel();
               timeoutTask = null;
            }

            synchronized(this.cancelTimeoutMutex) {
               if (this.wasCancelled) {
                  SQLException cause = null;
                  Object var24;
                  if (this.wasCancelledByTimeout) {
                     var24 = new MySQLTimeoutException();
                  } else {
                     var24 = new MySQLStatementCancelledException();
                  }

                  this.resetCancelledState();
                  throw var24;
               }
            }
         } finally {
            if (timeoutTask != null) {
               timeoutTask.cancel();
            }

            if (oldCatalog != null) {
               locallyScopedConn.setCatalog(oldCatalog);
            }

         }
      }

      this.results = rs;
      rs.setFirstCharOfQuery(firstStatementChar);
      this.updateCount = rs.getUpdateCount();
      int truncatedUpdateCount = 0;
      if (this.updateCount > 2147483647L) {
         truncatedUpdateCount = Integer.MAX_VALUE;
      } else {
         truncatedUpdateCount = (int)this.updateCount;
      }

      this.lastInsertId = rs.getUpdateID();
      return truncatedUpdateCount;
   }

   public int executeUpdate(String sql, int returnGeneratedKeys) throws SQLException {
      if (returnGeneratedKeys == 1) {
         this.checkClosed();
         ConnectionImpl locallyScopedConn = this.connection;
         synchronized(locallyScopedConn.getMutex()) {
            boolean readInfoMsgState = locallyScopedConn.isReadInfoMsgEnabled();
            locallyScopedConn.setReadInfoMsgEnabled(true);

            int var6;
            try {
               var6 = this.executeUpdate(sql);
            } finally {
               locallyScopedConn.setReadInfoMsgEnabled(readInfoMsgState);
            }

            return var6;
         }
      } else {
         return this.executeUpdate(sql);
      }
   }

   public int executeUpdate(String sql, int[] generatedKeyIndices) throws SQLException {
      if (generatedKeyIndices != null && generatedKeyIndices.length > 0) {
         this.checkClosed();
         ConnectionImpl locallyScopedConn = this.connection;
         synchronized(locallyScopedConn.getMutex()) {
            boolean readInfoMsgState = locallyScopedConn.isReadInfoMsgEnabled();
            locallyScopedConn.setReadInfoMsgEnabled(true);

            int var6;
            try {
               var6 = this.executeUpdate(sql);
            } finally {
               locallyScopedConn.setReadInfoMsgEnabled(readInfoMsgState);
            }

            return var6;
         }
      } else {
         return this.executeUpdate(sql);
      }
   }

   public int executeUpdate(String sql, String[] generatedKeyNames) throws SQLException {
      if (generatedKeyNames != null && generatedKeyNames.length > 0) {
         this.checkClosed();
         ConnectionImpl locallyScopedConn = this.connection;
         synchronized(locallyScopedConn.getMutex()) {
            boolean readInfoMsgState = this.connection.isReadInfoMsgEnabled();
            locallyScopedConn.setReadInfoMsgEnabled(true);

            int var6;
            try {
               var6 = this.executeUpdate(sql);
            } finally {
               locallyScopedConn.setReadInfoMsgEnabled(readInfoMsgState);
            }

            return var6;
         }
      } else {
         return this.executeUpdate(sql);
      }
   }

   protected Calendar getCalendarInstanceForSessionOrNew() {
      return (Calendar)(this.connection != null ? this.connection.getCalendarInstanceForSessionOrNew() : new GregorianCalendar());
   }

   public java.sql.Connection getConnection() throws SQLException {
      return this.connection;
   }

   public int getFetchDirection() throws SQLException {
      return 1000;
   }

   public int getFetchSize() throws SQLException {
      return this.fetchSize;
   }

   public synchronized ResultSet getGeneratedKeys() throws SQLException {
      if (this.batchedGeneratedKeys == null) {
         return this.getGeneratedKeysInternal();
      } else {
         Field[] fields = new Field[1];
         fields[0] = new Field("", "GENERATED_KEY", -5, 17);
         fields[0].setConnection(this.connection);
         return ResultSetImpl.getInstance(this.currentCatalog, fields, new RowDataStatic(this.batchedGeneratedKeys), this.connection, this, false);
      }
   }

   protected ResultSet getGeneratedKeysInternal() throws SQLException {
      Field[] fields = new Field[]{new Field("", "GENERATED_KEY", -5, 17)};
      fields[0].setConnection(this.connection);
      fields[0].setUseOldNameMetadata(true);
      ArrayList rowSet = new ArrayList();
      long beginAt = this.getLastInsertID();
      int numKeys = this.getUpdateCount();
      if (this.results != null) {
         String serverInfo = this.results.getServerInfo();
         if (numKeys > 0 && this.results.getFirstCharOfQuery() == 'R' && serverInfo != null && serverInfo.length() > 0) {
            numKeys = this.getRecordCountFromInfo(serverInfo);
         }

         if (beginAt > 0L && numKeys > 0) {
            for(int i = 0; i < numKeys; ++i) {
               byte[][] row = new byte[][]{Long.toString(beginAt++).getBytes()};
               rowSet.add(new ByteArrayRow(row));
            }
         }
      }

      ResultSetImpl gkRs = ResultSetImpl.getInstance(this.currentCatalog, fields, new RowDataStatic(rowSet), this.connection, this, false);
      this.openResults.add(gkRs);
      return gkRs;
   }

   protected int getId() {
      return this.statementId;
   }

   public long getLastInsertID() {
      return this.lastInsertId;
   }

   public long getLongUpdateCount() {
      if (this.results == null) {
         return -1L;
      } else {
         return this.results.reallyResult() ? -1L : this.updateCount;
      }
   }

   public int getMaxFieldSize() throws SQLException {
      return this.maxFieldSize;
   }

   public int getMaxRows() throws SQLException {
      return this.maxRows <= 0 ? 0 : this.maxRows;
   }

   public boolean getMoreResults() throws SQLException {
      return this.getMoreResults(1);
   }

   public boolean getMoreResults(int current) throws SQLException {
      if (this.results == null) {
         return false;
      } else {
         boolean streamingMode = this.createStreamingResultSet();
         if (streamingMode && this.results.reallyResult()) {
            while(this.results.next()) {
            }
         }

         ResultSetInternalMethods nextResultSet = this.results.getNextResultSet();
         switch (current) {
            case 1:
               if (this.results != null) {
                  if (!streamingMode) {
                     this.results.close();
                  }

                  this.results.clearNextResult();
               }
               break;
            case 2:
               if (!this.connection.getDontTrackOpenResources()) {
                  this.openResults.add(this.results);
               }

               this.results.clearNextResult();
               break;
            case 3:
               if (this.results != null) {
                  if (!streamingMode) {
                     this.results.close();
                  }

                  this.results.clearNextResult();
               }

               this.closeAllOpenResults();
               break;
            default:
               throw SQLError.createSQLException(Messages.getString("Statement.19"), "S1009");
         }

         this.results = nextResultSet;
         if (this.results == null) {
            this.updateCount = -1L;
            this.lastInsertId = -1L;
         } else if (this.results.reallyResult()) {
            this.updateCount = -1L;
            this.lastInsertId = -1L;
         } else {
            this.updateCount = this.results.getUpdateCount();
            this.lastInsertId = this.results.getUpdateID();
         }

         return this.results != null && this.results.reallyResult();
      }
   }

   public int getQueryTimeout() throws SQLException {
      return this.timeoutInMillis / 1000;
   }

   private int getRecordCountFromInfo(String serverInfo) {
      StringBuffer recordsBuf = new StringBuffer();
      int recordsCount = 0;
      int duplicatesCount = 0;
      char c = '\u0000';
      int length = serverInfo.length();

      int i;
      for(i = 0; i < length; ++i) {
         c = serverInfo.charAt(i);
         if (Character.isDigit(c)) {
            break;
         }
      }

      recordsBuf.append(c);
      ++i;

      while(i < length) {
         c = serverInfo.charAt(i);
         if (!Character.isDigit(c)) {
            break;
         }

         recordsBuf.append(c);
         ++i;
      }

      recordsCount = Integer.parseInt(recordsBuf.toString());

      StringBuffer duplicatesBuf;
      for(duplicatesBuf = new StringBuffer(); i < length; ++i) {
         c = serverInfo.charAt(i);
         if (Character.isDigit(c)) {
            break;
         }
      }

      duplicatesBuf.append(c);
      ++i;

      while(i < length) {
         c = serverInfo.charAt(i);
         if (!Character.isDigit(c)) {
            break;
         }

         duplicatesBuf.append(c);
         ++i;
      }

      duplicatesCount = Integer.parseInt(duplicatesBuf.toString());
      return recordsCount - duplicatesCount;
   }

   public ResultSet getResultSet() throws SQLException {
      return this.results != null && this.results.reallyResult() ? this.results : null;
   }

   public int getResultSetConcurrency() throws SQLException {
      return this.resultSetConcurrency;
   }

   public int getResultSetHoldability() throws SQLException {
      return 1;
   }

   protected ResultSetInternalMethods getResultSetInternal() {
      return this.results;
   }

   public int getResultSetType() throws SQLException {
      return this.resultSetType;
   }

   public int getUpdateCount() throws SQLException {
      if (this.results == null) {
         return -1;
      } else if (this.results.reallyResult()) {
         return -1;
      } else {
         int truncatedUpdateCount = 0;
         if (this.results.getUpdateCount() > 2147483647L) {
            truncatedUpdateCount = Integer.MAX_VALUE;
         } else {
            truncatedUpdateCount = (int)this.results.getUpdateCount();
         }

         return truncatedUpdateCount;
      }
   }

   public SQLWarning getWarnings() throws SQLException {
      this.checkClosed();
      if (this.connection != null && !this.connection.isClosed() && this.connection.versionMeetsMinimum(4, 1, 0)) {
         SQLWarning pendingWarningsFromServer = SQLError.convertShowWarningsToSQLWarnings(this.connection);
         if (this.warningChain != null) {
            this.warningChain.setNextWarning(pendingWarningsFromServer);
         } else {
            this.warningChain = pendingWarningsFromServer;
         }

         return this.warningChain;
      } else {
         return this.warningChain;
      }
   }

   protected void realClose(boolean calledExplicitly, boolean closeOpenResults) throws SQLException {
      if (!this.isClosed) {
         if (this.useUsageAdvisor && !calledExplicitly) {
            String message = Messages.getString("Statement.63") + Messages.getString("Statement.64");
            this.eventSink.consumeEvent(new ProfilerEvent((byte)0, "", this.currentCatalog, this.connectionId, this.getId(), -1, System.currentTimeMillis(), 0L, Constants.MILLIS_I18N, (String)null, this.pointOfOrigin, message));
         }

         if (closeOpenResults) {
            closeOpenResults = !this.holdResultsOpenOverClose;
         }

         if (closeOpenResults) {
            if (this.results != null) {
               try {
                  this.results.close();
               } catch (Exception var4) {
               }
            }

            this.closeAllOpenResults();
         }

         if (this.connection != null) {
            if (this.maxRowsChanged) {
               this.connection.unsetMaxRows(this);
            }

            if (!this.connection.getDontTrackOpenResources()) {
               this.connection.unregisterStatement(this);
            }
         }

         this.isClosed = true;
         this.results = null;
         this.connection = null;
         this.warningChain = null;
         this.openResults = null;
         this.batchedGeneratedKeys = null;
         this.localInfileInputStream = null;
         this.pingTarget = null;
      }
   }

   public void setCursorName(String name) throws SQLException {
   }

   public void setEscapeProcessing(boolean enable) throws SQLException {
      this.doEscapeProcessing = enable;
   }

   public void setFetchDirection(int direction) throws SQLException {
      switch (direction) {
         case 1000:
         case 1001:
         case 1002:
            return;
         default:
            throw SQLError.createSQLException(Messages.getString("Statement.5"), "S1009");
      }
   }

   public void setFetchSize(int rows) throws SQLException {
      if ((rows >= 0 || rows == Integer.MIN_VALUE) && (this.maxRows == 0 || this.maxRows == -1 || rows <= this.getMaxRows())) {
         this.fetchSize = rows;
      } else {
         throw SQLError.createSQLException(Messages.getString("Statement.7"), "S1009");
      }
   }

   protected void setHoldResultsOpenOverClose(boolean holdResultsOpenOverClose) {
      this.holdResultsOpenOverClose = holdResultsOpenOverClose;
   }

   public void setMaxFieldSize(int max) throws SQLException {
      if (max < 0) {
         throw SQLError.createSQLException(Messages.getString("Statement.11"), "S1009");
      } else {
         int maxBuf = this.connection != null ? this.connection.getMaxAllowedPacket() : MysqlIO.getMaxBuf();
         if (max > maxBuf) {
            throw SQLError.createSQLException(Messages.getString("Statement.13", new Object[]{Constants.longValueOf((long)maxBuf)}), "S1009");
         } else {
            this.maxFieldSize = max;
         }
      }
   }

   public void setMaxRows(int max) throws SQLException {
      if (max <= 50000000 && max >= 0) {
         if (max == 0) {
            max = -1;
         }

         this.maxRows = max;
         this.maxRowsChanged = true;
         if (this.maxRows == -1) {
            this.connection.unsetMaxRows(this);
            this.maxRowsChanged = false;
         } else {
            this.connection.maxRowsChanged(this);
         }

      } else {
         throw SQLError.createSQLException(Messages.getString("Statement.15") + max + " > " + 50000000 + ".", "S1009");
      }
   }

   public void setQueryTimeout(int seconds) throws SQLException {
      if (seconds < 0) {
         throw SQLError.createSQLException(Messages.getString("Statement.21"), "S1009");
      } else {
         this.timeoutInMillis = seconds * 1000;
      }
   }

   void setResultSetConcurrency(int concurrencyFlag) {
      this.resultSetConcurrency = concurrencyFlag;
   }

   void setResultSetType(int typeFlag) {
      this.resultSetType = typeFlag;
   }

   protected void getBatchedGeneratedKeys(java.sql.Statement batchedStatement) throws SQLException {
      if (this.retrieveGeneratedKeys) {
         ResultSet rs = null;

         try {
            rs = batchedStatement.getGeneratedKeys();

            while(rs.next()) {
               this.batchedGeneratedKeys.add(new ByteArrayRow(new byte[][]{rs.getBytes(1)}));
            }
         } finally {
            if (rs != null) {
               rs.close();
            }

         }
      }

   }

   protected void getBatchedGeneratedKeys() throws SQLException {
      if (this.retrieveGeneratedKeys) {
         ResultSet rs = null;

         try {
            rs = this.getGeneratedKeysInternal();

            while(rs.next()) {
               this.batchedGeneratedKeys.add(new ByteArrayRow(new byte[][]{rs.getBytes(1)}));
            }
         } finally {
            if (rs != null) {
               rs.close();
            }

         }
      }

   }

   private boolean useServerFetch() throws SQLException {
      return this.connection.isCursorFetchEnabled() && this.fetchSize > 0 && this.resultSetConcurrency == 1007 && this.resultSetType == 1003;
   }

   public synchronized boolean isClosed() throws SQLException {
      return this.isClosed;
   }

   public boolean isPoolable() throws SQLException {
      return this.isPoolable;
   }

   public void setPoolable(boolean poolable) throws SQLException {
      this.isPoolable = poolable;
   }

   public boolean isWrapperFor(Class iface) throws SQLException {
      this.checkClosed();
      return iface.isInstance(this);
   }

   public Object unwrap(Class iface) throws SQLException {
      try {
         return Util.cast(iface, this);
      } catch (ClassCastException var3) {
         throw SQLError.createSQLException("Unable to unwrap to " + iface.toString(), "S1009");
      }
   }

   protected int findStartOfStatement(String sql) {
      int statementStartPos = 0;
      if (StringUtils.startsWithIgnoreCaseAndWs(sql, "/*")) {
         statementStartPos = sql.indexOf("*/");
         if (statementStartPos == -1) {
            statementStartPos = 0;
         } else {
            statementStartPos += 2;
         }
      } else if (StringUtils.startsWithIgnoreCaseAndWs(sql, "--") || StringUtils.startsWithIgnoreCaseAndWs(sql, "#")) {
         statementStartPos = sql.indexOf(10);
         if (statementStartPos == -1) {
            statementStartPos = sql.indexOf(13);
            if (statementStartPos == -1) {
               statementStartPos = 0;
            }
         }
      }

      return statementStartPos;
   }

   public synchronized InputStream getLocalInfileInputStream() {
      return this.localInfileInputStream;
   }

   public synchronized void setLocalInfileInputStream(InputStream stream) {
      this.localInfileInputStream = stream;
   }

   public synchronized void setPingTarget(PingTarget pingTarget) {
      this.pingTarget = pingTarget;
   }

   class CancelTask extends TimerTask {
      long connectionId = 0L;
      SQLException caughtWhileCancelling = null;
      StatementImpl toCancel;

      CancelTask(StatementImpl cancellee) throws SQLException {
         this.connectionId = StatementImpl.this.connection.getIO().getThreadId();
         this.toCancel = cancellee;
      }

      public void run() {
         Thread cancelThread = new Thread() {
            public void run() {
               Connection cancelConn = null;
               java.sql.Statement cancelStmt = null;

               try {
                  synchronized(StatementImpl.this.cancelTimeoutMutex) {
                     cancelConn = StatementImpl.this.connection.duplicate();
                     cancelStmt = cancelConn.createStatement();
                     cancelStmt.execute("KILL QUERY " + CancelTask.this.connectionId);
                     CancelTask.this.toCancel.wasCancelled = true;
                     CancelTask.this.toCancel.wasCancelledByTimeout = true;
                  }
               } catch (SQLException sqlEx) {
                  CancelTask.this.caughtWhileCancelling = sqlEx;
               } catch (NullPointerException var24) {
               } finally {
                  if (cancelStmt != null) {
                     try {
                        cancelStmt.close();
                     } catch (SQLException sqlEx) {
                        throw new RuntimeException(sqlEx.toString());
                     }
                  }

                  if (cancelConn != null) {
                     try {
                        cancelConn.close();
                     } catch (SQLException sqlEx) {
                        throw new RuntimeException(sqlEx.toString());
                     }
                  }

               }

            }
         };
         cancelThread.start();
      }
   }
}
