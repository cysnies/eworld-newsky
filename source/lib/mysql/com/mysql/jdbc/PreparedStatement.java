package com.mysql.jdbc;

import com.mysql.jdbc.exceptions.MySQLStatementCancelledException;
import com.mysql.jdbc.exceptions.MySQLTimeoutException;
import com.mysql.jdbc.profiler.ProfilerEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.sql.Array;
import java.sql.BatchUpdateException;
import java.sql.Date;
import java.sql.ParameterMetaData;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class PreparedStatement extends StatementImpl implements java.sql.PreparedStatement {
   private static final Constructor JDBC_4_PSTMT_2_ARG_CTOR;
   private static final Constructor JDBC_4_PSTMT_3_ARG_CTOR;
   private static final Constructor JDBC_4_PSTMT_4_ARG_CTOR;
   private static final byte[] HEX_DIGITS;
   protected boolean batchHasPlainStatements = false;
   private java.sql.DatabaseMetaData dbmd = null;
   protected char firstCharOfStmt = 0;
   protected boolean hasLimitClause = false;
   protected boolean isLoadDataQuery = false;
   private boolean[] isNull = null;
   private boolean[] isStream = null;
   protected int numberOfExecutions = 0;
   protected String originalSql = null;
   protected int parameterCount;
   protected MysqlParameterMetadata parameterMetaData;
   private InputStream[] parameterStreams = null;
   private byte[][] parameterValues = (byte[][])null;
   protected int[] parameterTypes = null;
   private ParseInfo parseInfo;
   private java.sql.ResultSetMetaData pstmtResultMetaData;
   private byte[][] staticSqlStrings = (byte[][])null;
   private byte[] streamConvertBuf = new byte[4096];
   private int[] streamLengths = null;
   private SimpleDateFormat tsdf = null;
   protected boolean useTrueBoolean = false;
   protected boolean usingAnsiMode;
   protected String batchedValuesClause;
   private int statementAfterCommentsPos;
   private boolean hasCheckedForRewrite = false;
   private boolean canRewrite = false;
   private boolean doPingInstead;
   private SimpleDateFormat ddf;
   private SimpleDateFormat tdf;
   // $FF: synthetic field
   static Class class$com$mysql$jdbc$ConnectionImpl;
   // $FF: synthetic field
   static Class class$java$lang$String;
   // $FF: synthetic field
   static Class class$com$mysql$jdbc$PreparedStatement$ParseInfo;

   protected static int readFully(Reader reader, char[] buf, int length) throws IOException {
      int numCharsRead;
      int count;
      for(numCharsRead = 0; numCharsRead < length; numCharsRead += count) {
         count = reader.read(buf, numCharsRead, length - numCharsRead);
         if (count < 0) {
            break;
         }
      }

      return numCharsRead;
   }

   protected static PreparedStatement getInstance(ConnectionImpl conn, String catalog) throws SQLException {
      return !Util.isJdbc4() ? new PreparedStatement(conn, catalog) : (PreparedStatement)Util.handleNewInstance(JDBC_4_PSTMT_2_ARG_CTOR, new Object[]{conn, catalog});
   }

   protected static PreparedStatement getInstance(ConnectionImpl conn, String sql, String catalog) throws SQLException {
      return !Util.isJdbc4() ? new PreparedStatement(conn, sql, catalog) : (PreparedStatement)Util.handleNewInstance(JDBC_4_PSTMT_3_ARG_CTOR, new Object[]{conn, sql, catalog});
   }

   protected static PreparedStatement getInstance(ConnectionImpl conn, String sql, String catalog, ParseInfo cachedParseInfo) throws SQLException {
      return !Util.isJdbc4() ? new PreparedStatement(conn, sql, catalog, cachedParseInfo) : (PreparedStatement)Util.handleNewInstance(JDBC_4_PSTMT_4_ARG_CTOR, new Object[]{conn, sql, catalog, cachedParseInfo});
   }

   public PreparedStatement(ConnectionImpl conn, String catalog) throws SQLException {
      super(conn, catalog);
   }

   public PreparedStatement(ConnectionImpl conn, String sql, String catalog) throws SQLException {
      super(conn, catalog);
      if (sql == null) {
         throw SQLError.createSQLException(Messages.getString("PreparedStatement.0"), "S1009");
      } else {
         this.originalSql = sql;
         if (this.originalSql.startsWith("/* ping */")) {
            this.doPingInstead = true;
         } else {
            this.doPingInstead = false;
         }

         this.dbmd = this.connection.getMetaData();
         this.useTrueBoolean = this.connection.versionMeetsMinimum(3, 21, 23);
         this.parseInfo = new ParseInfo(sql, this.connection, this.dbmd, this.charEncoding, this.charConverter);
         this.initializeFromParseInfo();
      }
   }

   public PreparedStatement(ConnectionImpl conn, String sql, String catalog, ParseInfo cachedParseInfo) throws SQLException {
      super(conn, catalog);
      if (sql == null) {
         throw SQLError.createSQLException(Messages.getString("PreparedStatement.1"), "S1009");
      } else {
         this.originalSql = sql;
         this.dbmd = this.connection.getMetaData();
         this.useTrueBoolean = this.connection.versionMeetsMinimum(3, 21, 23);
         this.parseInfo = cachedParseInfo;
         this.usingAnsiMode = !this.connection.useAnsiQuotedIdentifiers();
         this.initializeFromParseInfo();
      }
   }

   public void addBatch() throws SQLException {
      if (this.batchedArgs == null) {
         this.batchedArgs = new ArrayList();
      }

      this.batchedArgs.add(new BatchParams(this.parameterValues, this.parameterStreams, this.isStream, this.streamLengths, this.isNull));
   }

   public synchronized void addBatch(String sql) throws SQLException {
      this.batchHasPlainStatements = true;
      super.addBatch(sql);
   }

   protected String asSql() throws SQLException {
      return this.asSql(false);
   }

   protected String asSql(boolean quoteStreamsAndUnknowns) throws SQLException {
      if (this.isClosed) {
         return "statement has been closed, no further internal information available";
      } else {
         StringBuffer buf = new StringBuffer();

         try {
            for(int i = 0; i < this.parameterCount; ++i) {
               if (this.charEncoding != null) {
                  buf.append(new String(this.staticSqlStrings[i], this.charEncoding));
               } else {
                  buf.append(new String(this.staticSqlStrings[i]));
               }

               if (this.parameterValues[i] == null && !this.isStream[i]) {
                  if (quoteStreamsAndUnknowns) {
                     buf.append("'");
                  }

                  buf.append("** NOT SPECIFIED **");
                  if (quoteStreamsAndUnknowns) {
                     buf.append("'");
                  }
               } else if (this.isStream[i]) {
                  if (quoteStreamsAndUnknowns) {
                     buf.append("'");
                  }

                  buf.append("** STREAM DATA **");
                  if (quoteStreamsAndUnknowns) {
                     buf.append("'");
                  }
               } else if (this.charConverter != null) {
                  buf.append(this.charConverter.toString(this.parameterValues[i]));
               } else if (this.charEncoding != null) {
                  buf.append(new String(this.parameterValues[i], this.charEncoding));
               } else {
                  buf.append(StringUtils.toAsciiString(this.parameterValues[i]));
               }
            }

            if (this.charEncoding != null) {
               buf.append(new String(this.staticSqlStrings[this.parameterCount], this.charEncoding));
            } else {
               buf.append(StringUtils.toAsciiString(this.staticSqlStrings[this.parameterCount]));
            }
         } catch (UnsupportedEncodingException var4) {
            throw new RuntimeException(Messages.getString("PreparedStatement.32") + this.charEncoding + Messages.getString("PreparedStatement.33"));
         }

         return buf.toString();
      }
   }

   public synchronized void clearBatch() throws SQLException {
      this.batchHasPlainStatements = false;
      super.clearBatch();
   }

   public synchronized void clearParameters() throws SQLException {
      this.checkClosed();

      for(int i = 0; i < this.parameterValues.length; ++i) {
         this.parameterValues[i] = null;
         this.parameterStreams[i] = null;
         this.isStream[i] = false;
         this.isNull[i] = false;
         this.parameterTypes[i] = 0;
      }

   }

   public synchronized void close() throws SQLException {
      this.realClose(true, true);
   }

   private final void escapeblockFast(byte[] buf, Buffer packet, int size) throws SQLException {
      int lastwritten = 0;

      for(int i = 0; i < size; ++i) {
         byte b = buf[i];
         if (b == 0) {
            if (i > lastwritten) {
               packet.writeBytesNoNull(buf, lastwritten, i - lastwritten);
            }

            packet.writeByte((byte)92);
            packet.writeByte((byte)48);
            lastwritten = i + 1;
         } else if (b == 92 || b == 39 || !this.usingAnsiMode && b == 34) {
            if (i > lastwritten) {
               packet.writeBytesNoNull(buf, lastwritten, i - lastwritten);
            }

            packet.writeByte((byte)92);
            lastwritten = i;
         }
      }

      if (lastwritten < size) {
         packet.writeBytesNoNull(buf, lastwritten, size - lastwritten);
      }

   }

   private final void escapeblockFast(byte[] buf, ByteArrayOutputStream bytesOut, int size) {
      int lastwritten = 0;

      for(int i = 0; i < size; ++i) {
         byte b = buf[i];
         if (b == 0) {
            if (i > lastwritten) {
               bytesOut.write(buf, lastwritten, i - lastwritten);
            }

            bytesOut.write(92);
            bytesOut.write(48);
            lastwritten = i + 1;
         } else if (b == 92 || b == 39 || !this.usingAnsiMode && b == 34) {
            if (i > lastwritten) {
               bytesOut.write(buf, lastwritten, i - lastwritten);
            }

            bytesOut.write(92);
            lastwritten = i;
         }
      }

      if (lastwritten < size) {
         bytesOut.write(buf, lastwritten, size - lastwritten);
      }

   }

   public boolean execute() throws SQLException {
      this.checkClosed();
      ConnectionImpl locallyScopedConn = this.connection;
      if (locallyScopedConn.isReadOnly() && this.firstCharOfStmt != 'S') {
         throw SQLError.createSQLException(Messages.getString("PreparedStatement.20") + Messages.getString("PreparedStatement.21"), "S1009");
      } else {
         ResultSetInternalMethods rs = null;
         CachedResultSetMetaData cachedMetadata = null;
         synchronized(locallyScopedConn.getMutex()) {
            boolean doStreaming = this.createStreamingResultSet();
            this.clearWarnings();
            if (doStreaming && this.connection.getNetTimeoutForStreamingResults() > 0) {
               this.executeSimpleNonQuery(locallyScopedConn, "SET net_write_timeout=" + this.connection.getNetTimeoutForStreamingResults());
            }

            this.batchedGeneratedKeys = null;
            Buffer sendPacket = this.fillSendPacket();
            String oldCatalog = null;
            if (!locallyScopedConn.getCatalog().equals(this.currentCatalog)) {
               oldCatalog = locallyScopedConn.getCatalog();
               locallyScopedConn.setCatalog(this.currentCatalog);
            }

            if (locallyScopedConn.getCacheResultSetMetadata()) {
               cachedMetadata = locallyScopedConn.getCachedMetaData(this.originalSql);
            }

            Field[] metadataFromCache = null;
            if (cachedMetadata != null) {
               metadataFromCache = cachedMetadata.fields;
            }

            boolean oldInfoMsgState = false;
            if (this.retrieveGeneratedKeys) {
               oldInfoMsgState = locallyScopedConn.isReadInfoMsgEnabled();
               locallyScopedConn.setReadInfoMsgEnabled(true);
            }

            if (locallyScopedConn.useMaxRows()) {
               int rowLimit = -1;
               if (this.firstCharOfStmt == 'S') {
                  if (this.hasLimitClause) {
                     rowLimit = this.maxRows;
                  } else if (this.maxRows <= 0) {
                     this.executeSimpleNonQuery(locallyScopedConn, "SET OPTION SQL_SELECT_LIMIT=DEFAULT");
                  } else {
                     this.executeSimpleNonQuery(locallyScopedConn, "SET OPTION SQL_SELECT_LIMIT=" + this.maxRows);
                  }
               } else {
                  this.executeSimpleNonQuery(locallyScopedConn, "SET OPTION SQL_SELECT_LIMIT=DEFAULT");
               }

               rs = this.executeInternal(rowLimit, sendPacket, doStreaming, this.firstCharOfStmt == 'S', metadataFromCache, false);
            } else {
               rs = this.executeInternal(-1, sendPacket, doStreaming, this.firstCharOfStmt == 'S', metadataFromCache, false);
            }

            if (cachedMetadata != null) {
               locallyScopedConn.initializeResultsMetadataFromCache(this.originalSql, cachedMetadata, this.results);
            } else if (rs.reallyResult() && locallyScopedConn.getCacheResultSetMetadata()) {
               locallyScopedConn.initializeResultsMetadataFromCache(this.originalSql, (CachedResultSetMetaData)null, rs);
            }

            if (this.retrieveGeneratedKeys) {
               locallyScopedConn.setReadInfoMsgEnabled(oldInfoMsgState);
               rs.setFirstCharOfQuery(this.firstCharOfStmt);
            }

            if (oldCatalog != null) {
               locallyScopedConn.setCatalog(oldCatalog);
            }

            if (rs != null) {
               this.lastInsertId = rs.getUpdateID();
               this.results = rs;
            }
         }

         return rs != null && rs.reallyResult();
      }
   }

   public int[] executeBatch() throws SQLException {
      this.checkClosed();
      if (this.connection.isReadOnly()) {
         throw new SQLException(Messages.getString("PreparedStatement.25") + Messages.getString("PreparedStatement.26"), "S1009");
      } else {
         synchronized(this.connection.getMutex()) {
            if (this.batchedArgs != null && this.batchedArgs.size() != 0) {
               int batchTimeout = this.timeoutInMillis;
               this.timeoutInMillis = 0;
               this.resetCancelledState();

               try {
                  this.clearWarnings();
                  if (!this.batchHasPlainStatements && this.connection.getRewriteBatchedStatements()) {
                     if (this.canRewriteAsMultivalueInsertStatement()) {
                        int[] var11 = this.executeBatchedInserts(batchTimeout);
                        return var11;
                     }

                     if (this.connection.versionMeetsMinimum(4, 1, 0) && !this.batchHasPlainStatements && this.batchedArgs != null && this.batchedArgs.size() > 3) {
                        int[] var10 = this.executePreparedBatchAsMultiStatement(batchTimeout);
                        return var10;
                     }
                  }

                  int[] var3 = this.executeBatchSerially(batchTimeout);
                  return var3;
               } finally {
                  this.clearBatch();
               }
            } else {
               return new int[0];
            }
         }
      }
   }

   public synchronized boolean canRewriteAsMultivalueInsertStatement() {
      if (!this.hasCheckedForRewrite) {
         this.canRewrite = StringUtils.startsWithIgnoreCaseAndWs(this.originalSql, "INSERT", this.statementAfterCommentsPos) && StringUtils.indexOfIgnoreCaseRespectMarker(this.statementAfterCommentsPos, this.originalSql, "SELECT", "\"'`", "\"'`", false) == -1 && StringUtils.indexOfIgnoreCaseRespectMarker(this.statementAfterCommentsPos, this.originalSql, "UPDATE", "\"'`", "\"'`", false) == -1;
         this.hasCheckedForRewrite = true;
      }

      return this.canRewrite;
   }

   protected int[] executePreparedBatchAsMultiStatement(int batchTimeout) throws SQLException {
      synchronized(this.connection.getMutex()) {
         if (this.batchedValuesClause == null) {
            this.batchedValuesClause = this.originalSql + ";";
         }

         ConnectionImpl locallyScopedConn = this.connection;
         boolean multiQueriesEnabled = locallyScopedConn.getAllowMultiQueries();
         StatementImpl.CancelTask timeoutTask = null;

         int numberArgsToExecute;
         try {
            this.clearWarnings();
            int numBatchedArgs = this.batchedArgs.size();
            if (this.retrieveGeneratedKeys) {
               this.batchedGeneratedKeys = new ArrayList(numBatchedArgs);
            }

            int numValuesPerBatch = this.computeBatchSize(numBatchedArgs);
            if (numBatchedArgs < numValuesPerBatch) {
               numValuesPerBatch = numBatchedArgs;
            }

            java.sql.PreparedStatement batchedStatement = null;
            int batchedParamIndex = 1;
            int numberToExecuteAsMultiValue = 0;
            int batchCounter = 0;
            int updateCountCounter = 0;
            int[] updateCounts = new int[numBatchedArgs];
            SQLException sqlEx = null;

            try {
               if (!multiQueriesEnabled) {
                  locallyScopedConn.getIO().enableMultiQueries();
               }

               if (this.retrieveGeneratedKeys) {
                  batchedStatement = locallyScopedConn.prepareStatement(this.generateMultiStatementForBatch(numValuesPerBatch), 1);
               } else {
                  batchedStatement = locallyScopedConn.prepareStatement(this.generateMultiStatementForBatch(numValuesPerBatch));
               }

               if (locallyScopedConn.getEnableQueryTimeouts() && batchTimeout != 0 && locallyScopedConn.versionMeetsMinimum(5, 0, 0)) {
                  timeoutTask = new StatementImpl.CancelTask((StatementImpl)batchedStatement);
                  ConnectionImpl.getCancelTimer().schedule(timeoutTask, (long)batchTimeout);
               }

               if (numBatchedArgs < numValuesPerBatch) {
                  numberToExecuteAsMultiValue = numBatchedArgs;
               } else {
                  numberToExecuteAsMultiValue = numBatchedArgs / numValuesPerBatch;
               }

               numberArgsToExecute = numberToExecuteAsMultiValue * numValuesPerBatch;

               for(int i = 0; i < numberArgsToExecute; ++i) {
                  if (i != 0 && i % numValuesPerBatch == 0) {
                     try {
                        batchedStatement.execute();
                     } catch (SQLException ex) {
                        sqlEx = this.handleExceptionForBatch(batchCounter, numValuesPerBatch, updateCounts, ex);
                     }

                     updateCountCounter = this.processMultiCountsAndKeys((StatementImpl)batchedStatement, updateCountCounter, updateCounts);
                     batchedStatement.clearParameters();
                     batchedParamIndex = 1;
                  }

                  batchedParamIndex = this.setOneBatchedParameterSet(batchedStatement, batchedParamIndex, this.batchedArgs.get(batchCounter++));
               }

               try {
                  batchedStatement.execute();
               } catch (SQLException ex) {
                  sqlEx = this.handleExceptionForBatch(batchCounter - 1, numValuesPerBatch, updateCounts, ex);
               }

               updateCountCounter = this.processMultiCountsAndKeys((StatementImpl)batchedStatement, updateCountCounter, updateCounts);
               batchedStatement.clearParameters();
               numValuesPerBatch = numBatchedArgs - batchCounter;
            } finally {
               if (batchedStatement != null) {
                  batchedStatement.close();
               }

            }

            try {
               if (numValuesPerBatch > 0) {
                  if (this.retrieveGeneratedKeys) {
                     batchedStatement = locallyScopedConn.prepareStatement(this.generateMultiStatementForBatch(numValuesPerBatch), 1);
                  } else {
                     batchedStatement = locallyScopedConn.prepareStatement(this.generateMultiStatementForBatch(numValuesPerBatch));
                  }

                  if (timeoutTask != null) {
                     timeoutTask.toCancel = (StatementImpl)batchedStatement;
                  }

                  for(int var52 = 1; batchCounter < numBatchedArgs; var52 = this.setOneBatchedParameterSet(batchedStatement, var52, this.batchedArgs.get(batchCounter++))) {
                  }

                  try {
                     batchedStatement.execute();
                  } catch (SQLException var44) {
                     numberArgsToExecute = (int)var44;
                     sqlEx = this.handleExceptionForBatch(batchCounter - 1, numValuesPerBatch, updateCounts, var44);
                  }

                  this.processMultiCountsAndKeys((StatementImpl)batchedStatement, updateCountCounter, updateCounts);
                  batchedStatement.clearParameters();
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

               numberArgsToExecute = (int)updateCounts;
            } finally {
               if (batchedStatement != null) {
                  batchedStatement.close();
               }

            }
         } finally {
            if (timeoutTask != null) {
               timeoutTask.cancel();
            }

            this.resetCancelledState();
            if (!multiQueriesEnabled) {
               locallyScopedConn.getIO().disableMultiQueries();
            }

            this.clearBatch();
         }

         return (int[])numberArgsToExecute;
      }
   }

   private String generateMultiStatementForBatch(int numBatches) {
      StringBuffer newStatementSql = new StringBuffer((this.originalSql.length() + 1) * numBatches);
      newStatementSql.append(this.originalSql);

      for(int i = 0; i < numBatches - 1; ++i) {
         newStatementSql.append(';');
         newStatementSql.append(this.originalSql);
      }

      return newStatementSql.toString();
   }

   protected int[] executeBatchedInserts(int batchTimeout) throws SQLException {
      String valuesClause = this.extractValuesClause();
      Connection locallyScopedConn = this.connection;
      if (valuesClause == null) {
         return this.executeBatchSerially(batchTimeout);
      } else {
         int numBatchedArgs = this.batchedArgs.size();
         if (this.retrieveGeneratedKeys) {
            this.batchedGeneratedKeys = new ArrayList(numBatchedArgs);
         }

         int numValuesPerBatch = this.computeBatchSize(numBatchedArgs);
         if (numBatchedArgs < numValuesPerBatch) {
            numValuesPerBatch = numBatchedArgs;
         }

         java.sql.PreparedStatement batchedStatement = null;
         int batchedParamIndex = 1;
         int updateCountRunningTotal = 0;
         int numberToExecuteAsMultiValue = 0;
         int batchCounter = 0;
         StatementImpl.CancelTask timeoutTask = null;
         SQLException sqlEx = null;
         int[] updateCounts = new int[numBatchedArgs];

         for(int i = 0; i < this.batchedArgs.size(); ++i) {
            updateCounts[i] = 1;
         }

         int numberArgsToExecute;
         try {
            try {
               if (this.retrieveGeneratedKeys) {
                  batchedStatement = locallyScopedConn.prepareStatement(this.generateBatchedInsertSQL(valuesClause, numValuesPerBatch), 1);
               } else {
                  batchedStatement = locallyScopedConn.prepareStatement(this.generateBatchedInsertSQL(valuesClause, numValuesPerBatch));
               }

               if (this.connection.getEnableQueryTimeouts() && batchTimeout != 0 && this.connection.versionMeetsMinimum(5, 0, 0)) {
                  timeoutTask = new StatementImpl.CancelTask((StatementImpl)batchedStatement);
                  ConnectionImpl.getCancelTimer().schedule(timeoutTask, (long)batchTimeout);
               }

               if (numBatchedArgs < numValuesPerBatch) {
                  numberToExecuteAsMultiValue = numBatchedArgs;
               } else {
                  numberToExecuteAsMultiValue = numBatchedArgs / numValuesPerBatch;
               }

               numberArgsToExecute = numberToExecuteAsMultiValue * numValuesPerBatch;

               for(int i = 0; i < numberArgsToExecute; ++i) {
                  if (i != 0 && i % numValuesPerBatch == 0) {
                     try {
                        updateCountRunningTotal += batchedStatement.executeUpdate();
                     } catch (SQLException ex) {
                        sqlEx = this.handleExceptionForBatch(batchCounter - 1, numValuesPerBatch, updateCounts, ex);
                     }

                     this.getBatchedGeneratedKeys(batchedStatement);
                     batchedStatement.clearParameters();
                     batchedParamIndex = 1;
                  }

                  batchedParamIndex = this.setOneBatchedParameterSet(batchedStatement, batchedParamIndex, this.batchedArgs.get(batchCounter++));
               }

               try {
                  updateCountRunningTotal += batchedStatement.executeUpdate();
               } catch (SQLException ex) {
                  sqlEx = this.handleExceptionForBatch(batchCounter - 1, numValuesPerBatch, updateCounts, ex);
               }

               this.getBatchedGeneratedKeys(batchedStatement);
               numValuesPerBatch = numBatchedArgs - batchCounter;
            } finally {
               if (batchedStatement != null) {
                  batchedStatement.close();
               }

            }

            try {
               if (numValuesPerBatch > 0) {
                  if (this.retrieveGeneratedKeys) {
                     batchedStatement = locallyScopedConn.prepareStatement(this.generateBatchedInsertSQL(valuesClause, numValuesPerBatch), 1);
                  } else {
                     batchedStatement = locallyScopedConn.prepareStatement(this.generateBatchedInsertSQL(valuesClause, numValuesPerBatch));
                  }

                  if (timeoutTask != null) {
                     timeoutTask.toCancel = (StatementImpl)batchedStatement;
                  }

                  for(int var46 = 1; batchCounter < numBatchedArgs; var46 = this.setOneBatchedParameterSet(batchedStatement, var46, this.batchedArgs.get(batchCounter++))) {
                  }

                  try {
                     int var10000 = updateCountRunningTotal + batchedStatement.executeUpdate();
                  } catch (SQLException var39) {
                     numberArgsToExecute = (int)var39;
                     sqlEx = this.handleExceptionForBatch(batchCounter - 1, numValuesPerBatch, updateCounts, var39);
                  }

                  this.getBatchedGeneratedKeys(batchedStatement);
               }

               if (sqlEx != null) {
                  throw new BatchUpdateException(sqlEx.getMessage(), sqlEx.getSQLState(), sqlEx.getErrorCode(), updateCounts);
               }

               numberArgsToExecute = (int)updateCounts;
            } finally {
               if (batchedStatement != null) {
                  batchedStatement.close();
               }

            }
         } finally {
            if (timeoutTask != null) {
               timeoutTask.cancel();
            }

            this.resetCancelledState();
         }

         return (int[])numberArgsToExecute;
      }
   }

   protected int computeBatchSize(int numBatchedArgs) {
      long[] combinedValues = this.computeMaxParameterSetSizeAndBatchSize(numBatchedArgs);
      long maxSizeOfParameterSet = combinedValues[0];
      long sizeOfEntireBatch = combinedValues[1];
      int maxAllowedPacket = this.connection.getMaxAllowedPacket();
      return sizeOfEntireBatch < (long)(maxAllowedPacket - this.originalSql.length()) ? numBatchedArgs : (int)Math.max(1L, (long)(maxAllowedPacket - this.originalSql.length()) / maxSizeOfParameterSet);
   }

   protected long[] computeMaxParameterSetSizeAndBatchSize(int numBatchedArgs) {
      long sizeOfEntireBatch = 0L;
      long maxSizeOfParameterSet = 0L;

      for(int i = 0; i < numBatchedArgs; ++i) {
         BatchParams paramArg = (BatchParams)this.batchedArgs.get(i);
         boolean[] isNullBatch = paramArg.isNull;
         boolean[] isStreamBatch = paramArg.isStream;
         long sizeOfParameterSet = 0L;

         for(int j = 0; j < isNullBatch.length; ++j) {
            if (!isNullBatch[j]) {
               if (isStreamBatch[j]) {
                  int streamLength = paramArg.streamLengths[j];
                  if (streamLength != -1) {
                     sizeOfParameterSet += (long)(streamLength * 2);
                  } else {
                     int paramLength = paramArg.parameterStrings[j].length;
                     sizeOfParameterSet += (long)paramLength;
                  }
               } else {
                  sizeOfParameterSet += (long)paramArg.parameterStrings[j].length;
               }
            } else {
               sizeOfParameterSet += 4L;
            }
         }

         if (this.batchedValuesClause != null) {
            sizeOfParameterSet += (long)(this.batchedValuesClause.length() + 1);
         }

         sizeOfEntireBatch += sizeOfParameterSet;
         if (sizeOfParameterSet > maxSizeOfParameterSet) {
            maxSizeOfParameterSet = sizeOfParameterSet;
         }
      }

      return new long[]{maxSizeOfParameterSet, sizeOfEntireBatch};
   }

   protected int[] executeBatchSerially(int batchTimeout) throws SQLException {
      Connection locallyScopedConn = this.connection;
      if (locallyScopedConn == null) {
         this.checkClosed();
      }

      int[] updateCounts = null;
      if (this.batchedArgs != null) {
         int nbrCommands = this.batchedArgs.size();
         updateCounts = new int[nbrCommands];

         for(int i = 0; i < nbrCommands; ++i) {
            updateCounts[i] = -3;
         }

         SQLException sqlEx = null;
         int commandIndex = 0;
         StatementImpl.CancelTask timeoutTask = null;

         try {
            if (this.connection.getEnableQueryTimeouts() && batchTimeout != 0 && this.connection.versionMeetsMinimum(5, 0, 0)) {
               timeoutTask = new StatementImpl.CancelTask(this);
               ConnectionImpl.getCancelTimer().schedule(timeoutTask, (long)batchTimeout);
            }

            if (this.retrieveGeneratedKeys) {
               this.batchedGeneratedKeys = new ArrayList(nbrCommands);
            }

            for(int var24 = 0; var24 < nbrCommands; ++var24) {
               Object arg = this.batchedArgs.get(var24);
               if (arg instanceof String) {
                  updateCounts[var24] = this.executeUpdate((String)arg);
               } else {
                  BatchParams paramArg = (BatchParams)arg;

                  try {
                     updateCounts[var24] = this.executeUpdate(paramArg.parameterStrings, paramArg.parameterStreams, paramArg.isStream, paramArg.streamLengths, paramArg.isNull, true);
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
                  } catch (SQLException var21) {
                     updateCounts[var24] = -3;
                     if (!this.continueBatchOnError || var21 instanceof MySQLTimeoutException || var21 instanceof MySQLStatementCancelledException) {
                        int[] newUpdateCounts = new int[var24];
                        System.arraycopy(updateCounts, 0, newUpdateCounts, 0, var24);
                        throw new BatchUpdateException(var21.getMessage(), var21.getSQLState(), var21.getErrorCode(), newUpdateCounts);
                     }

                     sqlEx = var21;
                  }
               }
            }

            if (sqlEx != null) {
               throw new BatchUpdateException(sqlEx.getMessage(), sqlEx.getSQLState(), sqlEx.getErrorCode(), updateCounts);
            }
         } finally {
            if (timeoutTask != null) {
               timeoutTask.cancel();
            }

            this.resetCancelledState();
         }
      }

      return updateCounts != null ? updateCounts : new int[0];
   }

   protected ResultSetInternalMethods executeInternal(int maxRowsToRetrieve, Buffer sendPacket, boolean createStreamingResultSet, boolean queryIsSelectOnly, Field[] metadataFromCache, boolean isBatch) throws SQLException {
      try {
         this.resetCancelledState();
         ConnectionImpl locallyScopedConnection = this.connection;
         ++this.numberOfExecutions;
         if (this.doPingInstead) {
            this.doPingInstead();
            return this.results;
         } else {
            StatementImpl.CancelTask timeoutTask = null;

            ResultSetInternalMethods rs;
            try {
               if (locallyScopedConnection.getEnableQueryTimeouts() && this.timeoutInMillis != 0 && locallyScopedConnection.versionMeetsMinimum(5, 0, 0)) {
                  timeoutTask = new StatementImpl.CancelTask(this);
                  ConnectionImpl.getCancelTimer().schedule(timeoutTask, (long)this.timeoutInMillis);
               }

               rs = locallyScopedConnection.execSQL(this, (String)null, maxRowsToRetrieve, sendPacket, this.resultSetType, this.resultSetConcurrency, createStreamingResultSet, this.currentCatalog, metadataFromCache, isBatch);
               if (timeoutTask != null) {
                  timeoutTask.cancel();
                  if (timeoutTask.caughtWhileCancelling != null) {
                     throw timeoutTask.caughtWhileCancelling;
                  }

                  timeoutTask = null;
               }

               synchronized(this.cancelTimeoutMutex) {
                  if (this.wasCancelled) {
                     SQLException cause = null;
                     Object var20;
                     if (this.wasCancelledByTimeout) {
                        var20 = new MySQLTimeoutException();
                     } else {
                        var20 = new MySQLStatementCancelledException();
                     }

                     this.resetCancelledState();
                     throw var20;
                  }
               }
            } finally {
               if (timeoutTask != null) {
                  timeoutTask.cancel();
               }

            }

            return rs;
         }
      } catch (NullPointerException npe) {
         this.checkClosed();
         throw npe;
      }
   }

   public ResultSet executeQuery() throws SQLException {
      this.checkClosed();
      ConnectionImpl locallyScopedConn = this.connection;
      this.checkForDml(this.originalSql, this.firstCharOfStmt);
      CachedResultSetMetaData cachedMetadata = null;
      synchronized(locallyScopedConn.getMutex()) {
         this.clearWarnings();
         boolean doStreaming = this.createStreamingResultSet();
         this.batchedGeneratedKeys = null;
         if (doStreaming && this.connection.getNetTimeoutForStreamingResults() > 0) {
            locallyScopedConn.execSQL(this, "SET net_write_timeout=" + this.connection.getNetTimeoutForStreamingResults(), -1, (Buffer)null, 1003, 1007, false, this.currentCatalog, (Field[])null, false);
         }

         Buffer sendPacket = this.fillSendPacket();
         if (this.results != null && !this.connection.getHoldResultsOpenOverStatementClose() && !this.holdResultsOpenOverClose) {
            this.results.realClose(false);
         }

         String oldCatalog = null;
         if (!locallyScopedConn.getCatalog().equals(this.currentCatalog)) {
            oldCatalog = locallyScopedConn.getCatalog();
            locallyScopedConn.setCatalog(this.currentCatalog);
         }

         if (locallyScopedConn.getCacheResultSetMetadata()) {
            cachedMetadata = locallyScopedConn.getCachedMetaData(this.originalSql);
         }

         Field[] metadataFromCache = null;
         if (cachedMetadata != null) {
            metadataFromCache = cachedMetadata.fields;
         }

         if (locallyScopedConn.useMaxRows()) {
            if (this.hasLimitClause) {
               this.results = this.executeInternal(this.maxRows, sendPacket, this.createStreamingResultSet(), true, metadataFromCache, false);
            } else {
               if (this.maxRows <= 0) {
                  this.executeSimpleNonQuery(locallyScopedConn, "SET OPTION SQL_SELECT_LIMIT=DEFAULT");
               } else {
                  this.executeSimpleNonQuery(locallyScopedConn, "SET OPTION SQL_SELECT_LIMIT=" + this.maxRows);
               }

               this.results = this.executeInternal(-1, sendPacket, doStreaming, true, metadataFromCache, false);
               if (oldCatalog != null) {
                  this.connection.setCatalog(oldCatalog);
               }
            }
         } else {
            this.results = this.executeInternal(-1, sendPacket, doStreaming, true, metadataFromCache, false);
         }

         if (oldCatalog != null) {
            locallyScopedConn.setCatalog(oldCatalog);
         }

         if (cachedMetadata != null) {
            locallyScopedConn.initializeResultsMetadataFromCache(this.originalSql, cachedMetadata, this.results);
         } else if (locallyScopedConn.getCacheResultSetMetadata()) {
            locallyScopedConn.initializeResultsMetadataFromCache(this.originalSql, (CachedResultSetMetaData)null, this.results);
         }
      }

      this.lastInsertId = this.results.getUpdateID();
      return this.results;
   }

   public int executeUpdate() throws SQLException {
      return this.executeUpdate(true, false);
   }

   protected int executeUpdate(boolean clearBatchedGeneratedKeysAndWarnings, boolean isBatch) throws SQLException {
      if (clearBatchedGeneratedKeysAndWarnings) {
         this.clearWarnings();
         this.batchedGeneratedKeys = null;
      }

      return this.executeUpdate(this.parameterValues, this.parameterStreams, this.isStream, this.streamLengths, this.isNull, isBatch);
   }

   protected int executeUpdate(byte[][] batchedParameterStrings, InputStream[] batchedParameterStreams, boolean[] batchedIsStream, int[] batchedStreamLengths, boolean[] batchedIsNull, boolean isReallyBatch) throws SQLException {
      this.checkClosed();
      ConnectionImpl locallyScopedConn = this.connection;
      if (locallyScopedConn.isReadOnly()) {
         throw SQLError.createSQLException(Messages.getString("PreparedStatement.34") + Messages.getString("PreparedStatement.35"), "S1009");
      } else if (this.firstCharOfStmt == 'S' && this.isSelectQuery()) {
         throw SQLError.createSQLException(Messages.getString("PreparedStatement.37"), "01S03");
      } else {
         if (this.results != null && !locallyScopedConn.getHoldResultsOpenOverStatementClose()) {
            this.results.realClose(false);
         }

         ResultSetInternalMethods rs = null;
         synchronized(locallyScopedConn.getMutex()) {
            Buffer sendPacket = this.fillSendPacket(batchedParameterStrings, batchedParameterStreams, batchedIsStream, batchedStreamLengths);
            String oldCatalog = null;
            if (!locallyScopedConn.getCatalog().equals(this.currentCatalog)) {
               oldCatalog = locallyScopedConn.getCatalog();
               locallyScopedConn.setCatalog(this.currentCatalog);
            }

            if (locallyScopedConn.useMaxRows()) {
               this.executeSimpleNonQuery(locallyScopedConn, "SET OPTION SQL_SELECT_LIMIT=DEFAULT");
            }

            boolean oldInfoMsgState = false;
            if (this.retrieveGeneratedKeys) {
               oldInfoMsgState = locallyScopedConn.isReadInfoMsgEnabled();
               locallyScopedConn.setReadInfoMsgEnabled(true);
            }

            rs = this.executeInternal(-1, sendPacket, false, false, (Field[])null, isReallyBatch);
            if (this.retrieveGeneratedKeys) {
               locallyScopedConn.setReadInfoMsgEnabled(oldInfoMsgState);
               rs.setFirstCharOfQuery(this.firstCharOfStmt);
            }

            if (oldCatalog != null) {
               locallyScopedConn.setCatalog(oldCatalog);
            }
         }

         this.results = rs;
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
   }

   private String extractValuesClause() throws SQLException {
      if (this.batchedValuesClause == null) {
         String quoteCharStr = this.connection.getMetaData().getIdentifierQuoteString();
         int indexOfValues = -1;
         if (quoteCharStr.length() > 0) {
            indexOfValues = StringUtils.indexOfIgnoreCaseRespectQuotes(this.statementAfterCommentsPos, this.originalSql, "VALUES ", quoteCharStr.charAt(0), false);
         } else {
            indexOfValues = StringUtils.indexOfIgnoreCase(this.statementAfterCommentsPos, this.originalSql, "VALUES ");
         }

         if (indexOfValues == -1) {
            return null;
         }

         int indexOfFirstParen = this.originalSql.indexOf(40, indexOfValues + 7);
         if (indexOfFirstParen == -1) {
            return null;
         }

         int indexOfLastParen = this.originalSql.lastIndexOf(41);
         if (indexOfLastParen == -1) {
            return null;
         }

         this.batchedValuesClause = this.originalSql.substring(indexOfFirstParen, indexOfLastParen + 1);
      }

      return this.batchedValuesClause;
   }

   protected Buffer fillSendPacket() throws SQLException {
      return this.fillSendPacket(this.parameterValues, this.parameterStreams, this.isStream, this.streamLengths);
   }

   protected Buffer fillSendPacket(byte[][] batchedParameterStrings, InputStream[] batchedParameterStreams, boolean[] batchedIsStream, int[] batchedStreamLengths) throws SQLException {
      Buffer sendPacket = this.connection.getIO().getSharedSendPacket();
      sendPacket.clear();
      sendPacket.writeByte((byte)3);
      boolean useStreamLengths = this.connection.getUseStreamLengthsInPrepStmts();
      int ensurePacketSize = 0;
      String statementComment = this.connection.getStatementComment();
      byte[] commentAsBytes = null;
      if (statementComment != null) {
         if (this.charConverter != null) {
            commentAsBytes = this.charConverter.toBytes(statementComment);
         } else {
            commentAsBytes = StringUtils.getBytes(statementComment, this.charConverter, this.charEncoding, this.connection.getServerCharacterEncoding(), this.connection.parserKnowsUnicode());
         }

         ensurePacketSize += commentAsBytes.length;
         ensurePacketSize += 6;
      }

      for(int i = 0; i < batchedParameterStrings.length; ++i) {
         if (batchedIsStream[i] && useStreamLengths) {
            ensurePacketSize += batchedStreamLengths[i];
         }
      }

      if (ensurePacketSize != 0) {
         sendPacket.ensureCapacity(ensurePacketSize);
      }

      if (commentAsBytes != null) {
         sendPacket.writeBytesNoNull(Constants.SLASH_STAR_SPACE_AS_BYTES);
         sendPacket.writeBytesNoNull(commentAsBytes);
         sendPacket.writeBytesNoNull(Constants.SPACE_STAR_SLASH_SPACE_AS_BYTES);
      }

      for(int i = 0; i < batchedParameterStrings.length; ++i) {
         if (batchedParameterStrings[i] == null && batchedParameterStreams[i] == null) {
            throw SQLError.createSQLException(Messages.getString("PreparedStatement.40") + (i + 1), "07001");
         }

         sendPacket.writeBytesNoNull(this.staticSqlStrings[i]);
         if (batchedIsStream[i]) {
            this.streamToBytes(sendPacket, batchedParameterStreams[i], true, batchedStreamLengths[i], useStreamLengths);
         } else {
            sendPacket.writeBytesNoNull(batchedParameterStrings[i]);
         }
      }

      sendPacket.writeBytesNoNull(this.staticSqlStrings[batchedParameterStrings.length]);
      return sendPacket;
   }

   private String generateBatchedInsertSQL(String valuesClause, int numBatches) {
      StringBuffer newStatementSql = new StringBuffer(this.originalSql.length() + numBatches * (valuesClause.length() + 1));
      newStatementSql.append(this.originalSql);

      for(int i = 0; i < numBatches - 1; ++i) {
         newStatementSql.append(',');
         newStatementSql.append(valuesClause);
      }

      return newStatementSql.toString();
   }

   public byte[] getBytesRepresentation(int parameterIndex) throws SQLException {
      if (this.isStream[parameterIndex]) {
         return this.streamToBytes(this.parameterStreams[parameterIndex], false, this.streamLengths[parameterIndex], this.connection.getUseStreamLengthsInPrepStmts());
      } else {
         byte[] parameterVal = this.parameterValues[parameterIndex];
         if (parameterVal == null) {
            return null;
         } else if (parameterVal[0] == 39 && parameterVal[parameterVal.length - 1] == 39) {
            byte[] valNoQuotes = new byte[parameterVal.length - 2];
            System.arraycopy(parameterVal, 1, valNoQuotes, 0, parameterVal.length - 2);
            return valNoQuotes;
         } else {
            return parameterVal;
         }
      }
   }

   private final String getDateTimePattern(String dt, boolean toTime) throws Exception {
      int dtLength = dt != null ? dt.length() : 0;
      if (dtLength >= 8 && dtLength <= 10) {
         int dashCount = 0;
         boolean isDateOnly = true;

         for(int i = 0; i < dtLength; ++i) {
            char c = dt.charAt(i);
            if (!Character.isDigit(c) && c != '-') {
               isDateOnly = false;
               break;
            }

            if (c == '-') {
               ++dashCount;
            }
         }

         if (isDateOnly && dashCount == 2) {
            return "yyyy-MM-dd";
         }
      }

      boolean colonsOnly = true;

      for(int i = 0; i < dtLength; ++i) {
         char c = dt.charAt(i);
         if (!Character.isDigit(c) && c != ':') {
            colonsOnly = false;
            break;
         }
      }

      if (colonsOnly) {
         return "HH:mm:ss";
      } else {
         StringReader reader = new StringReader(dt + " ");
         ArrayList vec = new ArrayList();
         ArrayList vecRemovelist = new ArrayList();
         Object[] nv = new Object[]{Constants.characterValueOf('y'), new StringBuffer(), Constants.integerValueOf(0)};
         vec.add(nv);
         if (toTime) {
            nv = new Object[]{Constants.characterValueOf('h'), new StringBuffer(), Constants.integerValueOf(0)};
            vec.add(nv);
         }

         int z;
         while((z = reader.read()) != -1) {
            char separator = (char)z;
            int maxvecs = vec.size();

            for(int count = 0; count < maxvecs; ++count) {
               Object[] v = vec.get(count);
               int n = (Integer)v[2];
               char c = this.getSuccessor((Character)v[0], n);
               if (!Character.isLetterOrDigit(separator)) {
                  if (c == (Character)v[0] && c != 'S') {
                     vecRemovelist.add(v);
                  } else {
                     ((StringBuffer)v[1]).append(separator);
                     if (c == 'X' || c == 'Y') {
                        v[2] = Constants.integerValueOf(4);
                     }
                  }
               } else {
                  if (c == 'X') {
                     c = 'y';
                     nv = new Object[]{Constants.characterValueOf('M'), (new StringBuffer(((StringBuffer)v[1]).toString())).append('M'), Constants.integerValueOf(1)};
                     vec.add(nv);
                  } else if (c == 'Y') {
                     c = 'M';
                     nv = new Object[]{Constants.characterValueOf('d'), (new StringBuffer(((StringBuffer)v[1]).toString())).append('d'), Constants.integerValueOf(1)};
                     vec.add(nv);
                  }

                  ((StringBuffer)v[1]).append(c);
                  if (c == (Character)v[0]) {
                     v[2] = Constants.integerValueOf(n + 1);
                  } else {
                     v[0] = Constants.characterValueOf(c);
                     v[2] = Constants.integerValueOf(1);
                  }
               }
            }

            int size = vecRemovelist.size();

            for(int i = 0; i < size; ++i) {
               Object[] v = vecRemovelist.get(i);
               vec.remove(v);
            }

            vecRemovelist.clear();
         }

         int size = vec.size();

         for(int i = 0; i < size; ++i) {
            Object[] v = vec.get(i);
            char c = (Character)v[0];
            int n = (Integer)v[2];
            boolean bk = this.getSuccessor(c, n) != c;
            boolean atEnd = (c == 's' || c == 'm' || c == 'h' && toTime) && bk;
            boolean finishesAtDate = bk && c == 'd' && !toTime;
            boolean containsEnd = ((StringBuffer)v[1]).toString().indexOf(87) != -1;
            if (!atEnd && !finishesAtDate || containsEnd) {
               vecRemovelist.add(v);
            }
         }

         size = vecRemovelist.size();

         for(int i = 0; i < size; ++i) {
            vec.remove(vecRemovelist.get(i));
         }

         vecRemovelist.clear();
         Object[] v = vec.get(0);
         StringBuffer format = (StringBuffer)v[1];
         format.setLength(format.length() - 1);
         return format.toString();
      }
   }

   public java.sql.ResultSetMetaData getMetaData() throws SQLException {
      if (!this.isSelectQuery()) {
         return null;
      } else {
         PreparedStatement mdStmt = null;
         ResultSet mdRs = null;
         if (this.pstmtResultMetaData == null) {
            try {
               mdStmt = new PreparedStatement(this.connection, this.originalSql, this.currentCatalog, this.parseInfo);
               mdStmt.setMaxRows(0);
               int paramCount = this.parameterValues.length;

               for(int i = 1; i <= paramCount; ++i) {
                  mdStmt.setString(i, "");
               }

               boolean hadResults = mdStmt.execute();
               if (hadResults) {
                  mdRs = mdStmt.getResultSet();
                  this.pstmtResultMetaData = mdRs.getMetaData();
               } else {
                  this.pstmtResultMetaData = new ResultSetMetaData(new Field[0], this.connection.getUseOldAliasMetadataBehavior());
               }
            } finally {
               SQLException sqlExRethrow = null;
               if (mdRs != null) {
                  try {
                     mdRs.close();
                  } catch (SQLException sqlEx) {
                     sqlExRethrow = sqlEx;
                  }

                  ResultSet var18 = null;
               }

               if (mdStmt != null) {
                  try {
                     mdStmt.close();
                  } catch (SQLException sqlEx) {
                     sqlExRethrow = sqlEx;
                  }

                  PreparedStatement var17 = null;
               }

               if (sqlExRethrow != null) {
                  throw sqlExRethrow;
               }

            }
         }

         return this.pstmtResultMetaData;
      }
   }

   protected boolean isSelectQuery() {
      return StringUtils.startsWithIgnoreCaseAndWs(StringUtils.stripComments(this.originalSql, "'\"", "'\"", true, false, true, true), "SELECT");
   }

   public ParameterMetaData getParameterMetaData() throws SQLException {
      if (this.parameterMetaData == null) {
         if (this.connection.getGenerateSimpleParameterMetadata()) {
            this.parameterMetaData = new MysqlParameterMetadata(this.parameterCount);
         } else {
            this.parameterMetaData = new MysqlParameterMetadata((Field[])null, this.parameterCount);
         }
      }

      return this.parameterMetaData;
   }

   ParseInfo getParseInfo() {
      return this.parseInfo;
   }

   private final char getSuccessor(char c, int n) {
      return (char)(c == 'y' && n == 2 ? 'X' : (c == 'y' && n < 4 ? 'y' : (c == 'y' ? 'M' : (c == 'M' && n == 2 ? 'Y' : (c == 'M' && n < 3 ? 'M' : (c == 'M' ? 'd' : (c == 'd' && n < 2 ? 'd' : (c == 'd' ? 'H' : (c == 'H' && n < 2 ? 'H' : (c == 'H' ? 'm' : (c == 'm' && n < 2 ? 'm' : (c == 'm' ? 's' : (c == 's' && n < 2 ? 's' : 'W')))))))))))));
   }

   private final void hexEscapeBlock(byte[] buf, Buffer packet, int size) throws SQLException {
      for(int i = 0; i < size; ++i) {
         byte b = buf[i];
         int lowBits = (b & 255) / 16;
         int highBits = (b & 255) % 16;
         packet.writeByte(HEX_DIGITS[lowBits]);
         packet.writeByte(HEX_DIGITS[highBits]);
      }

   }

   private void initializeFromParseInfo() throws SQLException {
      this.staticSqlStrings = this.parseInfo.staticSql;
      this.hasLimitClause = this.parseInfo.foundLimitClause;
      this.isLoadDataQuery = this.parseInfo.foundLoadData;
      this.firstCharOfStmt = this.parseInfo.firstStmtChar;
      this.parameterCount = this.staticSqlStrings.length - 1;
      this.parameterValues = new byte[this.parameterCount][];
      this.parameterStreams = new InputStream[this.parameterCount];
      this.isStream = new boolean[this.parameterCount];
      this.streamLengths = new int[this.parameterCount];
      this.isNull = new boolean[this.parameterCount];
      this.parameterTypes = new int[this.parameterCount];
      this.clearParameters();

      for(int j = 0; j < this.parameterCount; ++j) {
         this.isStream[j] = false;
      }

      this.statementAfterCommentsPos = this.parseInfo.statementStartPos;
   }

   boolean isNull(int paramIndex) {
      return this.isNull[paramIndex];
   }

   private final int readblock(InputStream i, byte[] b) throws SQLException {
      try {
         return i.read(b);
      } catch (Throwable ex) {
         SQLException sqlEx = SQLError.createSQLException(Messages.getString("PreparedStatement.56") + ex.getClass().getName(), "S1000");
         sqlEx.initCause(ex);
         throw sqlEx;
      }
   }

   private final int readblock(InputStream i, byte[] b, int length) throws SQLException {
      try {
         int lengthToRead = length;
         if (length > b.length) {
            lengthToRead = b.length;
         }

         return i.read(b, 0, lengthToRead);
      } catch (Throwable ex) {
         SQLException sqlEx = SQLError.createSQLException(Messages.getString("PreparedStatement.56") + ex.getClass().getName(), "S1000");
         sqlEx.initCause(ex);
         throw sqlEx;
      }
   }

   protected void realClose(boolean calledExplicitly, boolean closeOpenResults) throws SQLException {
      if (this.useUsageAdvisor && this.numberOfExecutions <= 1) {
         String message = Messages.getString("PreparedStatement.43");
         this.eventSink.consumeEvent(new ProfilerEvent((byte)0, "", this.currentCatalog, this.connectionId, this.getId(), -1, System.currentTimeMillis(), 0L, Constants.MILLIS_I18N, (String)null, this.pointOfOrigin, message));
      }

      super.realClose(calledExplicitly, closeOpenResults);
      this.dbmd = null;
      this.originalSql = null;
      this.staticSqlStrings = (byte[][])null;
      this.parameterValues = (byte[][])null;
      this.parameterStreams = null;
      this.isStream = null;
      this.streamLengths = null;
      this.isNull = null;
      this.streamConvertBuf = null;
      this.parameterTypes = null;
   }

   public void setArray(int i, Array x) throws SQLException {
      throw SQLError.notImplemented();
   }

   public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
      if (x == null) {
         this.setNull(parameterIndex, 12);
      } else {
         this.setBinaryStream(parameterIndex, x, length);
      }

   }

   public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
      if (x == null) {
         this.setNull(parameterIndex, 3);
      } else {
         this.setInternal(parameterIndex, StringUtils.fixDecimalExponent(StringUtils.consistentToString(x)));
         this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 3;
      }

   }

   public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
      if (x == null) {
         this.setNull(parameterIndex, -2);
      } else {
         int parameterIndexOffset = this.getParameterIndexOffset();
         if (parameterIndex < 1 || parameterIndex > this.staticSqlStrings.length) {
            throw SQLError.createSQLException(Messages.getString("PreparedStatement.2") + parameterIndex + Messages.getString("PreparedStatement.3") + this.staticSqlStrings.length + Messages.getString("PreparedStatement.4"), "S1009");
         }

         if (parameterIndexOffset == -1 && parameterIndex == 1) {
            throw SQLError.createSQLException("Can't set IN parameter for return value of stored function call.", "S1009");
         }

         this.parameterStreams[parameterIndex - 1 + parameterIndexOffset] = x;
         this.isStream[parameterIndex - 1 + parameterIndexOffset] = true;
         this.streamLengths[parameterIndex - 1 + parameterIndexOffset] = length;
         this.isNull[parameterIndex - 1 + parameterIndexOffset] = false;
         this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 2004;
      }

   }

   public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
      this.setBinaryStream(parameterIndex, inputStream, (int)length);
   }

   public void setBlob(int i, java.sql.Blob x) throws SQLException {
      if (x == null) {
         this.setNull(i, 2004);
      } else {
         ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
         bytesOut.write(39);
         this.escapeblockFast(x.getBytes(1L, (int)x.length()), bytesOut, (int)x.length());
         bytesOut.write(39);
         this.setInternal(i, bytesOut.toByteArray());
         this.parameterTypes[i - 1 + this.getParameterIndexOffset()] = 2004;
      }

   }

   public void setBoolean(int parameterIndex, boolean x) throws SQLException {
      if (this.useTrueBoolean) {
         this.setInternal(parameterIndex, x ? "1" : "0");
      } else {
         this.setInternal(parameterIndex, x ? "'t'" : "'f'");
         this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 16;
      }

   }

   public void setByte(int parameterIndex, byte x) throws SQLException {
      this.setInternal(parameterIndex, String.valueOf(x));
      this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = -6;
   }

   public void setBytes(int parameterIndex, byte[] x) throws SQLException {
      this.setBytes(parameterIndex, x, true, true);
      if (x != null) {
         this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = -2;
      }

   }

   protected void setBytes(int parameterIndex, byte[] x, boolean checkForIntroducer, boolean escapeForMBChars) throws SQLException {
      if (x == null) {
         this.setNull(parameterIndex, -2);
      } else {
         String connectionEncoding = this.connection.getEncoding();
         if (this.connection.isNoBackslashEscapesSet() || escapeForMBChars && this.connection.getUseUnicode() && connectionEncoding != null && CharsetMapping.isMultibyteCharset(connectionEncoding)) {
            ByteArrayOutputStream bOut = new ByteArrayOutputStream(x.length * 2 + 3);
            bOut.write(120);
            bOut.write(39);

            for(int i = 0; i < x.length; ++i) {
               int lowBits = (x[i] & 255) / 16;
               int highBits = (x[i] & 255) % 16;
               bOut.write(HEX_DIGITS[lowBits]);
               bOut.write(HEX_DIGITS[highBits]);
            }

            bOut.write(39);
            this.setInternal(parameterIndex, bOut.toByteArray());
            return;
         }

         int numBytes = x.length;
         int pad = 2;
         boolean needsIntroducer = checkForIntroducer && this.connection.versionMeetsMinimum(4, 1, 0);
         if (needsIntroducer) {
            pad += 7;
         }

         ByteArrayOutputStream bOut = new ByteArrayOutputStream(numBytes + pad);
         if (needsIntroducer) {
            bOut.write(95);
            bOut.write(98);
            bOut.write(105);
            bOut.write(110);
            bOut.write(97);
            bOut.write(114);
            bOut.write(121);
         }

         bOut.write(39);

         for(int i = 0; i < numBytes; ++i) {
            byte b = x[i];
            switch (b) {
               case 0:
                  bOut.write(92);
                  bOut.write(48);
                  break;
               case 10:
                  bOut.write(92);
                  bOut.write(110);
                  break;
               case 13:
                  bOut.write(92);
                  bOut.write(114);
                  break;
               case 26:
                  bOut.write(92);
                  bOut.write(90);
                  break;
               case 34:
                  bOut.write(92);
                  bOut.write(34);
                  break;
               case 39:
                  bOut.write(92);
                  bOut.write(39);
                  break;
               case 92:
                  bOut.write(92);
                  bOut.write(92);
                  break;
               default:
                  bOut.write(b);
            }
         }

         bOut.write(39);
         this.setInternal(parameterIndex, bOut.toByteArray());
      }

   }

   protected void setBytesNoEscape(int parameterIndex, byte[] parameterAsBytes) throws SQLException {
      byte[] parameterWithQuotes = new byte[parameterAsBytes.length + 2];
      parameterWithQuotes[0] = 39;
      System.arraycopy(parameterAsBytes, 0, parameterWithQuotes, 1, parameterAsBytes.length);
      parameterWithQuotes[parameterAsBytes.length + 1] = 39;
      this.setInternal(parameterIndex, parameterWithQuotes);
   }

   protected void setBytesNoEscapeNoQuotes(int parameterIndex, byte[] parameterAsBytes) throws SQLException {
      this.setInternal(parameterIndex, parameterAsBytes);
   }

   public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
      try {
         if (reader == null) {
            this.setNull(parameterIndex, -1);
         } else {
            char[] c = null;
            int len = 0;
            boolean useLength = this.connection.getUseStreamLengthsInPrepStmts();
            String forcedEncoding = this.connection.getClobCharacterEncoding();
            if (useLength && length != -1) {
               c = new char[length];
               int numCharsRead = readFully(reader, c, length);
               if (forcedEncoding == null) {
                  this.setString(parameterIndex, new String(c, 0, numCharsRead));
               } else {
                  try {
                     this.setBytes(parameterIndex, (new String(c, 0, numCharsRead)).getBytes(forcedEncoding));
                  } catch (UnsupportedEncodingException var11) {
                     throw SQLError.createSQLException("Unsupported character encoding " + forcedEncoding, "S1009");
                  }
               }
            } else {
               c = new char[4096];
               StringBuffer buf = new StringBuffer();

               while((len = reader.read(c)) != -1) {
                  buf.append(c, 0, len);
               }

               if (forcedEncoding == null) {
                  this.setString(parameterIndex, buf.toString());
               } else {
                  try {
                     this.setBytes(parameterIndex, buf.toString().getBytes(forcedEncoding));
                  } catch (UnsupportedEncodingException var10) {
                     throw SQLError.createSQLException("Unsupported character encoding " + forcedEncoding, "S1009");
                  }
               }
            }

            this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 2005;
         }

      } catch (IOException ioEx) {
         throw SQLError.createSQLException(ioEx.toString(), "S1000");
      }
   }

   public void setClob(int i, java.sql.Clob x) throws SQLException {
      if (x == null) {
         this.setNull(i, 2005);
      } else {
         String forcedEncoding = this.connection.getClobCharacterEncoding();
         if (forcedEncoding == null) {
            this.setString(i, x.getSubString(1L, (int)x.length()));
         } else {
            try {
               this.setBytes(i, x.getSubString(1L, (int)x.length()).getBytes(forcedEncoding));
            } catch (UnsupportedEncodingException var5) {
               throw SQLError.createSQLException("Unsupported character encoding " + forcedEncoding, "S1009");
            }
         }

         this.parameterTypes[i - 1 + this.getParameterIndexOffset()] = 2005;
      }

   }

   public void setDate(int parameterIndex, Date x) throws SQLException {
      this.setDate(parameterIndex, x, (Calendar)null);
   }

   public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
      if (x == null) {
         this.setNull(parameterIndex, 91);
      } else {
         this.checkClosed();
         if (!this.useLegacyDatetimeCode) {
            this.newSetDateInternal(parameterIndex, x, cal);
         } else {
            SimpleDateFormat dateFormatter = new SimpleDateFormat("''yyyy-MM-dd''", Locale.US);
            this.setInternal(parameterIndex, dateFormatter.format(x));
            this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 91;
         }
      }

   }

   public void setDouble(int parameterIndex, double x) throws SQLException {
      if (this.connection.getAllowNanAndInf() || x != Double.POSITIVE_INFINITY && x != Double.NEGATIVE_INFINITY && !Double.isNaN(x)) {
         this.setInternal(parameterIndex, StringUtils.fixDecimalExponent(String.valueOf(x)));
         this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 8;
      } else {
         throw SQLError.createSQLException("'" + x + "' is not a valid numeric or approximate numeric value", "S1009");
      }
   }

   public void setFloat(int parameterIndex, float x) throws SQLException {
      this.setInternal(parameterIndex, StringUtils.fixDecimalExponent(String.valueOf(x)));
      this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 6;
   }

   public void setInt(int parameterIndex, int x) throws SQLException {
      this.setInternal(parameterIndex, String.valueOf(x));
      this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 4;
   }

   protected final void setInternal(int paramIndex, byte[] val) throws SQLException {
      if (this.isClosed) {
         throw SQLError.createSQLException(Messages.getString("PreparedStatement.48"), "S1009");
      } else {
         int parameterIndexOffset = this.getParameterIndexOffset();
         this.checkBounds(paramIndex, parameterIndexOffset);
         this.isStream[paramIndex - 1 + parameterIndexOffset] = false;
         this.isNull[paramIndex - 1 + parameterIndexOffset] = false;
         this.parameterStreams[paramIndex - 1 + parameterIndexOffset] = null;
         this.parameterValues[paramIndex - 1 + parameterIndexOffset] = val;
      }
   }

   private void checkBounds(int paramIndex, int parameterIndexOffset) throws SQLException {
      if (paramIndex < 1) {
         throw SQLError.createSQLException(Messages.getString("PreparedStatement.49") + paramIndex + Messages.getString("PreparedStatement.50"), "S1009");
      } else if (paramIndex > this.parameterCount) {
         throw SQLError.createSQLException(Messages.getString("PreparedStatement.51") + paramIndex + Messages.getString("PreparedStatement.52") + this.parameterValues.length + Messages.getString("PreparedStatement.53"), "S1009");
      } else if (parameterIndexOffset == -1 && paramIndex == 1) {
         throw SQLError.createSQLException("Can't set IN parameter for return value of stored function call.", "S1009");
      }
   }

   protected final void setInternal(int paramIndex, String val) throws SQLException {
      this.checkClosed();
      byte[] parameterAsBytes = null;
      if (this.charConverter != null) {
         parameterAsBytes = this.charConverter.toBytes(val);
      } else {
         parameterAsBytes = StringUtils.getBytes(val, this.charConverter, this.charEncoding, this.connection.getServerCharacterEncoding(), this.connection.parserKnowsUnicode());
      }

      this.setInternal(paramIndex, parameterAsBytes);
   }

   public void setLong(int parameterIndex, long x) throws SQLException {
      this.setInternal(parameterIndex, String.valueOf(x));
      this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = -5;
   }

   public void setNull(int parameterIndex, int sqlType) throws SQLException {
      this.setInternal(parameterIndex, "null");
      this.isNull[parameterIndex - 1 + this.getParameterIndexOffset()] = true;
      this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 0;
   }

   public void setNull(int parameterIndex, int sqlType, String arg) throws SQLException {
      this.setNull(parameterIndex, sqlType);
      this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 0;
   }

   private void setNumericObject(int parameterIndex, Object parameterObj, int targetSqlType, int scale) throws SQLException {
      Number parameterAsNum;
      if (parameterObj instanceof Boolean) {
         parameterAsNum = (Boolean)parameterObj ? Constants.integerValueOf(1) : Constants.integerValueOf(0);
      } else if (parameterObj instanceof String) {
         switch (targetSqlType) {
            case -7:
               if (!"1".equals((String)parameterObj) && !"0".equals((String)parameterObj)) {
                  boolean parameterAsBoolean = "true".equalsIgnoreCase((String)parameterObj);
                  parameterAsNum = parameterAsBoolean ? Constants.integerValueOf(1) : Constants.integerValueOf(0);
               } else {
                  parameterAsNum = Integer.valueOf((String)parameterObj);
               }
               break;
            case -6:
            case 4:
            case 5:
               parameterAsNum = Integer.valueOf((String)parameterObj);
               break;
            case -5:
               parameterAsNum = Long.valueOf((String)parameterObj);
               break;
            case -4:
            case -3:
            case -2:
            case -1:
            case 0:
            case 1:
            case 2:
            case 3:
            default:
               parameterAsNum = new BigDecimal((String)parameterObj);
               break;
            case 6:
            case 8:
               parameterAsNum = Double.valueOf((String)parameterObj);
               break;
            case 7:
               parameterAsNum = Float.valueOf((String)parameterObj);
         }
      } else {
         parameterAsNum = (Number)parameterObj;
      }

      switch (targetSqlType) {
         case -7:
         case -6:
         case 4:
         case 5:
            this.setInt(parameterIndex, parameterAsNum.intValue());
            break;
         case -5:
            this.setLong(parameterIndex, parameterAsNum.longValue());
         case -4:
         case -3:
         case -2:
         case -1:
         case 0:
         case 1:
         default:
            break;
         case 2:
         case 3:
            if (parameterAsNum instanceof BigDecimal) {
               BigDecimal scaledBigDecimal = null;

               try {
                  scaledBigDecimal = ((BigDecimal)parameterAsNum).setScale(scale);
               } catch (ArithmeticException var10) {
                  try {
                     scaledBigDecimal = ((BigDecimal)parameterAsNum).setScale(scale, 4);
                  } catch (ArithmeticException var9) {
                     throw SQLError.createSQLException("Can't set scale of '" + scale + "' for DECIMAL argument '" + parameterAsNum + "'", "S1009");
                  }
               }

               this.setBigDecimal(parameterIndex, scaledBigDecimal);
            } else if (parameterAsNum instanceof BigInteger) {
               this.setBigDecimal(parameterIndex, new BigDecimal((BigInteger)parameterAsNum, scale));
            } else {
               this.setBigDecimal(parameterIndex, new BigDecimal(parameterAsNum.doubleValue()));
            }
            break;
         case 6:
         case 8:
            this.setDouble(parameterIndex, parameterAsNum.doubleValue());
            break;
         case 7:
            this.setFloat(parameterIndex, parameterAsNum.floatValue());
      }

   }

   public void setObject(int parameterIndex, Object parameterObj) throws SQLException {
      if (parameterObj == null) {
         this.setNull(parameterIndex, 1111);
      } else if (parameterObj instanceof Byte) {
         this.setInt(parameterIndex, ((Byte)parameterObj).intValue());
      } else if (parameterObj instanceof String) {
         this.setString(parameterIndex, (String)parameterObj);
      } else if (parameterObj instanceof BigDecimal) {
         this.setBigDecimal(parameterIndex, (BigDecimal)parameterObj);
      } else if (parameterObj instanceof Short) {
         this.setShort(parameterIndex, (Short)parameterObj);
      } else if (parameterObj instanceof Integer) {
         this.setInt(parameterIndex, (Integer)parameterObj);
      } else if (parameterObj instanceof Long) {
         this.setLong(parameterIndex, (Long)parameterObj);
      } else if (parameterObj instanceof Float) {
         this.setFloat(parameterIndex, (Float)parameterObj);
      } else if (parameterObj instanceof Double) {
         this.setDouble(parameterIndex, (Double)parameterObj);
      } else if (parameterObj instanceof byte[]) {
         this.setBytes(parameterIndex, (byte[])parameterObj);
      } else if (parameterObj instanceof Date) {
         this.setDate(parameterIndex, (Date)parameterObj);
      } else if (parameterObj instanceof Time) {
         this.setTime(parameterIndex, (Time)parameterObj);
      } else if (parameterObj instanceof Timestamp) {
         this.setTimestamp(parameterIndex, (Timestamp)parameterObj);
      } else if (parameterObj instanceof Boolean) {
         this.setBoolean(parameterIndex, (Boolean)parameterObj);
      } else if (parameterObj instanceof InputStream) {
         this.setBinaryStream(parameterIndex, (InputStream)parameterObj, -1);
      } else if (parameterObj instanceof java.sql.Blob) {
         this.setBlob(parameterIndex, (java.sql.Blob)parameterObj);
      } else if (parameterObj instanceof java.sql.Clob) {
         this.setClob(parameterIndex, (java.sql.Clob)parameterObj);
      } else if (this.connection.getTreatUtilDateAsTimestamp() && parameterObj instanceof java.util.Date) {
         this.setTimestamp(parameterIndex, new Timestamp(((java.util.Date)parameterObj).getTime()));
      } else if (parameterObj instanceof BigInteger) {
         this.setString(parameterIndex, parameterObj.toString());
      } else {
         this.setSerializableObject(parameterIndex, parameterObj);
      }

   }

   public void setObject(int parameterIndex, Object parameterObj, int targetSqlType) throws SQLException {
      if (!(parameterObj instanceof BigDecimal)) {
         this.setObject(parameterIndex, parameterObj, targetSqlType, 0);
      } else {
         this.setObject(parameterIndex, parameterObj, targetSqlType, ((BigDecimal)parameterObj).scale());
      }

   }

   public void setObject(int parameterIndex, Object parameterObj, int targetSqlType, int scale) throws SQLException {
      if (parameterObj == null) {
         this.setNull(parameterIndex, 1111);
      } else {
         try {
            switch (targetSqlType) {
               case -7:
               case -6:
               case -5:
               case 2:
               case 3:
               case 4:
               case 5:
               case 6:
               case 7:
               case 8:
                  this.setNumericObject(parameterIndex, parameterObj, targetSqlType, scale);
                  break;
               case -4:
               case -3:
               case -2:
               case 2004:
                  if (parameterObj instanceof byte[]) {
                     this.setBytes(parameterIndex, (byte[])parameterObj);
                  } else if (parameterObj instanceof java.sql.Blob) {
                     this.setBlob(parameterIndex, (java.sql.Blob)parameterObj);
                  } else {
                     this.setBytes(parameterIndex, StringUtils.getBytes(parameterObj.toString(), this.charConverter, this.charEncoding, this.connection.getServerCharacterEncoding(), this.connection.parserKnowsUnicode()));
                  }
                  break;
               case -1:
               case 1:
               case 12:
                  if (parameterObj instanceof BigDecimal) {
                     this.setString(parameterIndex, StringUtils.fixDecimalExponent(StringUtils.consistentToString((BigDecimal)parameterObj)));
                  } else {
                     this.setString(parameterIndex, parameterObj.toString());
                  }
                  break;
               case 16:
                  if (parameterObj instanceof Boolean) {
                     this.setBoolean(parameterIndex, (Boolean)parameterObj);
                  } else if (parameterObj instanceof String) {
                     this.setBoolean(parameterIndex, "true".equalsIgnoreCase((String)parameterObj) || !"0".equalsIgnoreCase((String)parameterObj));
                  } else {
                     if (!(parameterObj instanceof Number)) {
                        throw SQLError.createSQLException("No conversion from " + parameterObj.getClass().getName() + " to Types.BOOLEAN possible.", "S1009");
                     }

                     int intValue = ((Number)parameterObj).intValue();
                     this.setBoolean(parameterIndex, intValue != 0);
                  }
                  break;
               case 91:
               case 93:
                  java.util.Date parameterAsDate;
                  if (parameterObj instanceof String) {
                     ParsePosition pp = new ParsePosition(0);
                     DateFormat sdf = new SimpleDateFormat(this.getDateTimePattern((String)parameterObj, false), Locale.US);
                     parameterAsDate = sdf.parse((String)parameterObj, pp);
                  } else {
                     parameterAsDate = (java.util.Date)parameterObj;
                  }

                  switch (targetSqlType) {
                     case 91:
                        if (parameterAsDate instanceof Date) {
                           this.setDate(parameterIndex, (Date)parameterAsDate);
                        } else {
                           this.setDate(parameterIndex, new Date(parameterAsDate.getTime()));
                        }

                        return;
                     case 93:
                        if (parameterAsDate instanceof Timestamp) {
                           this.setTimestamp(parameterIndex, (Timestamp)parameterAsDate);
                        } else {
                           this.setTimestamp(parameterIndex, new Timestamp(parameterAsDate.getTime()));
                        }

                        return;
                     default:
                        return;
                  }
               case 92:
                  if (parameterObj instanceof String) {
                     DateFormat sdf = new SimpleDateFormat(this.getDateTimePattern((String)parameterObj, true), Locale.US);
                     this.setTime(parameterIndex, new Time(sdf.parse((String)parameterObj).getTime()));
                  } else if (parameterObj instanceof Timestamp) {
                     Timestamp xT = (Timestamp)parameterObj;
                     this.setTime(parameterIndex, new Time(xT.getTime()));
                  } else {
                     this.setTime(parameterIndex, (Time)parameterObj);
                  }
                  break;
               case 1111:
                  this.setSerializableObject(parameterIndex, parameterObj);
                  break;
               case 2005:
                  if (parameterObj instanceof java.sql.Clob) {
                     this.setClob(parameterIndex, (java.sql.Clob)parameterObj);
                  } else {
                     this.setString(parameterIndex, parameterObj.toString());
                  }
                  break;
               default:
                  throw SQLError.createSQLException(Messages.getString("PreparedStatement.16"), "S1000");
            }
         } catch (Exception ex) {
            if (ex instanceof SQLException) {
               throw (SQLException)ex;
            }

            SQLException sqlEx = SQLError.createSQLException(Messages.getString("PreparedStatement.17") + parameterObj.getClass().toString() + Messages.getString("PreparedStatement.18") + ex.getClass().getName() + Messages.getString("PreparedStatement.19") + ex.getMessage(), "S1000");
            sqlEx.initCause(ex);
            throw sqlEx;
         }
      }

   }

   protected int setOneBatchedParameterSet(java.sql.PreparedStatement batchedStatement, int batchedParamIndex, Object paramSet) throws SQLException {
      BatchParams paramArg = (BatchParams)paramSet;
      boolean[] isNullBatch = paramArg.isNull;
      boolean[] isStreamBatch = paramArg.isStream;

      for(int j = 0; j < isNullBatch.length; ++j) {
         if (isNullBatch[j]) {
            batchedStatement.setNull(batchedParamIndex++, 0);
         } else if (isStreamBatch[j]) {
            batchedStatement.setBinaryStream(batchedParamIndex++, paramArg.parameterStreams[j], paramArg.streamLengths[j]);
         } else {
            ((PreparedStatement)batchedStatement).setBytesNoEscapeNoQuotes(batchedParamIndex++, paramArg.parameterStrings[j]);
         }
      }

      return batchedParamIndex;
   }

   public void setRef(int i, Ref x) throws SQLException {
      throw SQLError.notImplemented();
   }

   void setResultSetConcurrency(int concurrencyFlag) {
      this.resultSetConcurrency = concurrencyFlag;
   }

   void setResultSetType(int typeFlag) {
      this.resultSetType = typeFlag;
   }

   protected void setRetrieveGeneratedKeys(boolean retrieveGeneratedKeys) {
      this.retrieveGeneratedKeys = retrieveGeneratedKeys;
   }

   private final void setSerializableObject(int parameterIndex, Object parameterObj) throws SQLException {
      try {
         ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
         ObjectOutputStream objectOut = new ObjectOutputStream(bytesOut);
         objectOut.writeObject(parameterObj);
         objectOut.flush();
         objectOut.close();
         bytesOut.flush();
         bytesOut.close();
         byte[] buf = bytesOut.toByteArray();
         ByteArrayInputStream bytesIn = new ByteArrayInputStream(buf);
         this.setBinaryStream(parameterIndex, bytesIn, buf.length);
         this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 2000;
      } catch (Exception ex) {
         SQLException sqlEx = SQLError.createSQLException(Messages.getString("PreparedStatement.54") + ex.getClass().getName(), "S1009");
         sqlEx.initCause(ex);
         throw sqlEx;
      }
   }

   public void setShort(int parameterIndex, short x) throws SQLException {
      this.setInternal(parameterIndex, String.valueOf(x));
      this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 5;
   }

   public void setString(int parameterIndex, String x) throws SQLException {
      if (x == null) {
         this.setNull(parameterIndex, 1);
      } else {
         this.checkClosed();
         int stringLength = x.length();
         if (this.connection.isNoBackslashEscapesSet()) {
            boolean needsHexEscape = this.isEscapeNeededForString(x, stringLength);
            if (!needsHexEscape) {
               byte[] parameterAsBytes = null;
               StringBuffer quotedString = new StringBuffer(x.length() + 2);
               quotedString.append('\'');
               quotedString.append(x);
               quotedString.append('\'');
               if (!this.isLoadDataQuery) {
                  parameterAsBytes = StringUtils.getBytes(quotedString.toString(), this.charConverter, this.charEncoding, this.connection.getServerCharacterEncoding(), this.connection.parserKnowsUnicode());
               } else {
                  parameterAsBytes = quotedString.toString().getBytes();
               }

               this.setInternal(parameterIndex, parameterAsBytes);
            } else {
               byte[] parameterAsBytes = null;
               if (!this.isLoadDataQuery) {
                  parameterAsBytes = StringUtils.getBytes(x, this.charConverter, this.charEncoding, this.connection.getServerCharacterEncoding(), this.connection.parserKnowsUnicode());
               } else {
                  parameterAsBytes = x.getBytes();
               }

               this.setBytes(parameterIndex, parameterAsBytes);
            }

            return;
         }

         String parameterAsString = x;
         boolean needsQuoted = true;
         if (this.isLoadDataQuery || this.isEscapeNeededForString(x, stringLength)) {
            needsQuoted = false;
            StringBuffer buf = new StringBuffer((int)((double)x.length() * 1.1));
            buf.append('\'');

            for(int i = 0; i < stringLength; ++i) {
               char c = x.charAt(i);
               switch (c) {
                  case '\u0000':
                     buf.append('\\');
                     buf.append('0');
                     break;
                  case '\n':
                     buf.append('\\');
                     buf.append('n');
                     break;
                  case '\r':
                     buf.append('\\');
                     buf.append('r');
                     break;
                  case '\u001a':
                     buf.append('\\');
                     buf.append('Z');
                     break;
                  case '"':
                     if (this.usingAnsiMode) {
                        buf.append('\\');
                     }

                     buf.append('"');
                     break;
                  case '\'':
                     buf.append('\\');
                     buf.append('\'');
                     break;
                  case '\\':
                     buf.append('\\');
                     buf.append('\\');
                     break;
                  default:
                     buf.append(c);
               }
            }

            buf.append('\'');
            parameterAsString = buf.toString();
         }

         byte[] parameterAsBytes = null;
         if (!this.isLoadDataQuery) {
            if (needsQuoted) {
               parameterAsBytes = StringUtils.getBytesWrapped(parameterAsString, '\'', '\'', this.charConverter, this.charEncoding, this.connection.getServerCharacterEncoding(), this.connection.parserKnowsUnicode());
            } else {
               parameterAsBytes = StringUtils.getBytes(parameterAsString, this.charConverter, this.charEncoding, this.connection.getServerCharacterEncoding(), this.connection.parserKnowsUnicode());
            }
         } else {
            parameterAsBytes = parameterAsString.getBytes();
         }

         this.setInternal(parameterIndex, parameterAsBytes);
         this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 12;
      }

   }

   private boolean isEscapeNeededForString(String x, int stringLength) {
      boolean needsHexEscape = false;

      for(int i = 0; i < stringLength; ++i) {
         char c = x.charAt(i);
         switch (c) {
            case '\u0000':
               needsHexEscape = true;
               break;
            case '\n':
               needsHexEscape = true;
               break;
            case '\r':
               needsHexEscape = true;
               break;
            case '\u001a':
               needsHexEscape = true;
               break;
            case '"':
               needsHexEscape = true;
               break;
            case '\'':
               needsHexEscape = true;
               break;
            case '\\':
               needsHexEscape = true;
         }

         if (needsHexEscape) {
            break;
         }
      }

      return needsHexEscape;
   }

   public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
      this.setTimeInternal(parameterIndex, x, cal, cal.getTimeZone(), true);
   }

   public void setTime(int parameterIndex, Time x) throws SQLException {
      this.setTimeInternal(parameterIndex, x, (Calendar)null, Util.getDefaultTimeZone(), false);
   }

   private void setTimeInternal(int parameterIndex, Time x, Calendar targetCalendar, TimeZone tz, boolean rollForward) throws SQLException {
      if (x == null) {
         this.setNull(parameterIndex, 92);
      } else {
         this.checkClosed();
         if (!this.useLegacyDatetimeCode) {
            this.newSetTimeInternal(parameterIndex, x, targetCalendar);
         } else {
            Calendar sessionCalendar = this.getCalendarInstanceForSessionOrNew();
            synchronized(sessionCalendar) {
               x = TimeUtil.changeTimezone(this.connection, sessionCalendar, targetCalendar, x, tz, this.connection.getServerTimezoneTZ(), rollForward);
            }

            this.setInternal(parameterIndex, "'" + x.toString() + "'");
         }

         this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 92;
      }

   }

   public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
      this.setTimestampInternal(parameterIndex, x, cal, cal.getTimeZone(), true);
   }

   public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
      this.setTimestampInternal(parameterIndex, x, (Calendar)null, Util.getDefaultTimeZone(), false);
   }

   private void setTimestampInternal(int parameterIndex, Timestamp x, Calendar targetCalendar, TimeZone tz, boolean rollForward) throws SQLException {
      if (x == null) {
         this.setNull(parameterIndex, 93);
      } else {
         this.checkClosed();
         if (!this.useLegacyDatetimeCode) {
            this.newSetTimestampInternal(parameterIndex, x, targetCalendar);
         } else {
            String timestampString = null;
            Calendar sessionCalendar = this.connection.getUseJDBCCompliantTimezoneShift() ? this.connection.getUtcCalendar() : this.getCalendarInstanceForSessionOrNew();
            synchronized(sessionCalendar) {
               x = TimeUtil.changeTimezone(this.connection, sessionCalendar, targetCalendar, x, tz, this.connection.getServerTimezoneTZ(), rollForward);
            }

            if (this.connection.getUseSSPSCompatibleTimezoneShift()) {
               this.doSSPSCompatibleTimezoneShift(parameterIndex, x, sessionCalendar);
            } else {
               synchronized(this) {
                  if (this.tsdf == null) {
                     this.tsdf = new SimpleDateFormat("''yyyy-MM-dd HH:mm:ss''", Locale.US);
                  }

                  timestampString = this.tsdf.format(x);
                  this.setInternal(parameterIndex, timestampString);
               }
            }
         }

         this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 93;
      }

   }

   private synchronized void newSetTimestampInternal(int parameterIndex, Timestamp x, Calendar targetCalendar) throws SQLException {
      if (this.tsdf == null) {
         this.tsdf = new SimpleDateFormat("''yyyy-MM-dd HH:mm:ss''", Locale.US);
      }

      String timestampString = null;
      if (targetCalendar != null) {
         targetCalendar.setTime(x);
         this.tsdf.setTimeZone(targetCalendar.getTimeZone());
         timestampString = this.tsdf.format(x);
      } else {
         this.tsdf.setTimeZone(this.connection.getServerTimezoneTZ());
         timestampString = this.tsdf.format(x);
      }

      this.setInternal(parameterIndex, timestampString);
   }

   private synchronized void newSetTimeInternal(int parameterIndex, Time x, Calendar targetCalendar) throws SQLException {
      if (this.tdf == null) {
         this.tdf = new SimpleDateFormat("''HH:mm:ss''", Locale.US);
      }

      String timeString = null;
      if (targetCalendar != null) {
         targetCalendar.setTime(x);
         this.tdf.setTimeZone(targetCalendar.getTimeZone());
         timeString = this.tdf.format(x);
      } else {
         this.tdf.setTimeZone(this.connection.getServerTimezoneTZ());
         timeString = this.tdf.format(x);
      }

      this.setInternal(parameterIndex, timeString);
   }

   private synchronized void newSetDateInternal(int parameterIndex, Date x, Calendar targetCalendar) throws SQLException {
      if (this.ddf == null) {
         this.ddf = new SimpleDateFormat("''yyyy-MM-dd''", Locale.US);
      }

      String timeString = null;
      if (targetCalendar != null) {
         targetCalendar.setTime(x);
         this.ddf.setTimeZone(targetCalendar.getTimeZone());
         timeString = this.ddf.format(x);
      } else {
         this.ddf.setTimeZone(this.connection.getServerTimezoneTZ());
         timeString = this.ddf.format(x);
      }

      this.setInternal(parameterIndex, timeString);
   }

   private void doSSPSCompatibleTimezoneShift(int parameterIndex, Timestamp x, Calendar sessionCalendar) throws SQLException {
      Calendar sessionCalendar2 = this.connection.getUseJDBCCompliantTimezoneShift() ? this.connection.getUtcCalendar() : this.getCalendarInstanceForSessionOrNew();
      synchronized(sessionCalendar2) {
         java.util.Date oldTime = sessionCalendar2.getTime();

         try {
            sessionCalendar2.setTime(x);
            int year = sessionCalendar2.get(1);
            int month = sessionCalendar2.get(2) + 1;
            int date = sessionCalendar2.get(5);
            int hour = sessionCalendar2.get(11);
            int minute = sessionCalendar2.get(12);
            int seconds = sessionCalendar2.get(13);
            StringBuffer tsBuf = new StringBuffer();
            tsBuf.append('\'');
            tsBuf.append(year);
            tsBuf.append("-");
            if (month < 10) {
               tsBuf.append('0');
            }

            tsBuf.append(month);
            tsBuf.append('-');
            if (date < 10) {
               tsBuf.append('0');
            }

            tsBuf.append(date);
            tsBuf.append(' ');
            if (hour < 10) {
               tsBuf.append('0');
            }

            tsBuf.append(hour);
            tsBuf.append(':');
            if (minute < 10) {
               tsBuf.append('0');
            }

            tsBuf.append(minute);
            tsBuf.append(':');
            if (seconds < 10) {
               tsBuf.append('0');
            }

            tsBuf.append(seconds);
            tsBuf.append('\'');
            this.setInternal(parameterIndex, tsBuf.toString());
         } finally {
            sessionCalendar.setTime(oldTime);
         }

      }
   }

   /** @deprecated */
   public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
      if (x == null) {
         this.setNull(parameterIndex, 12);
      } else {
         this.setBinaryStream(parameterIndex, x, length);
         this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 2005;
      }

   }

   public void setURL(int parameterIndex, URL arg) throws SQLException {
      if (arg != null) {
         this.setString(parameterIndex, arg.toString());
         this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 70;
      } else {
         this.setNull(parameterIndex, 1);
      }

   }

   private final void streamToBytes(Buffer packet, InputStream in, boolean escape, int streamLength, boolean useLength) throws SQLException {
      try {
         String connectionEncoding = this.connection.getEncoding();
         boolean hexEscape = false;
         if (this.connection.isNoBackslashEscapesSet() || this.connection.getUseUnicode() && connectionEncoding != null && CharsetMapping.isMultibyteCharset(connectionEncoding) && !this.connection.parserKnowsUnicode()) {
            hexEscape = true;
         }

         if (streamLength == -1) {
            useLength = false;
         }

         int bc = -1;
         if (useLength) {
            bc = this.readblock(in, this.streamConvertBuf, streamLength);
         } else {
            bc = this.readblock(in, this.streamConvertBuf);
         }

         int lengthLeftToRead = streamLength - bc;
         if (hexEscape) {
            packet.writeStringNoNull("x");
         } else if (this.connection.getIO().versionMeetsMinimum(4, 1, 0)) {
            packet.writeStringNoNull("_binary");
         }

         if (escape) {
            packet.writeByte((byte)39);
         }

         while(bc > 0) {
            if (hexEscape) {
               this.hexEscapeBlock(this.streamConvertBuf, packet, bc);
            } else if (escape) {
               this.escapeblockFast(this.streamConvertBuf, packet, bc);
            } else {
               packet.writeBytesNoNull(this.streamConvertBuf, 0, bc);
            }

            if (useLength) {
               bc = this.readblock(in, this.streamConvertBuf, lengthLeftToRead);
               if (bc > 0) {
                  lengthLeftToRead -= bc;
               }
            } else {
               bc = this.readblock(in, this.streamConvertBuf);
            }
         }

         if (escape) {
            packet.writeByte((byte)39);
         }
      } finally {
         if (this.connection.getAutoClosePStmtStreams()) {
            try {
               in.close();
            } catch (IOException var16) {
            }

            InputStream var18 = null;
         }

      }

   }

   private final byte[] streamToBytes(InputStream in, boolean escape, int streamLength, boolean useLength) throws SQLException {
      byte[] var8;
      try {
         if (streamLength == -1) {
            useLength = false;
         }

         ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
         int bc = -1;
         if (useLength) {
            bc = this.readblock(in, this.streamConvertBuf, streamLength);
         } else {
            bc = this.readblock(in, this.streamConvertBuf);
         }

         int lengthLeftToRead = streamLength - bc;
         if (escape) {
            if (this.connection.versionMeetsMinimum(4, 1, 0)) {
               bytesOut.write(95);
               bytesOut.write(98);
               bytesOut.write(105);
               bytesOut.write(110);
               bytesOut.write(97);
               bytesOut.write(114);
               bytesOut.write(121);
            }

            bytesOut.write(39);
         }

         while(bc > 0) {
            if (escape) {
               this.escapeblockFast(this.streamConvertBuf, bytesOut, bc);
            } else {
               bytesOut.write(this.streamConvertBuf, 0, bc);
            }

            if (useLength) {
               bc = this.readblock(in, this.streamConvertBuf, lengthLeftToRead);
               if (bc > 0) {
                  lengthLeftToRead -= bc;
               }
            } else {
               bc = this.readblock(in, this.streamConvertBuf);
            }
         }

         if (escape) {
            bytesOut.write(39);
         }

         var8 = bytesOut.toByteArray();
      } finally {
         if (this.connection.getAutoClosePStmtStreams()) {
            try {
               in.close();
            } catch (IOException var15) {
            }

            InputStream var17 = null;
         }

      }

      return var8;
   }

   public String toString() {
      StringBuffer buf = new StringBuffer();
      buf.append(super.toString());
      buf.append(": ");

      try {
         buf.append(this.asSql());
      } catch (SQLException sqlEx) {
         buf.append("EXCEPTION: " + sqlEx.toString());
      }

      return buf.toString();
   }

   public synchronized boolean isClosed() throws SQLException {
      return this.isClosed;
   }

   protected int getParameterIndexOffset() {
      return 0;
   }

   public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
      this.setAsciiStream(parameterIndex, x, -1);
   }

   public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
      this.setAsciiStream(parameterIndex, x, (int)length);
      this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 2005;
   }

   public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
      this.setBinaryStream(parameterIndex, x, -1);
   }

   public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
      this.setBinaryStream(parameterIndex, x, (int)length);
   }

   public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
      this.setBinaryStream(parameterIndex, inputStream);
   }

   public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
      this.setCharacterStream(parameterIndex, reader, -1);
   }

   public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
      this.setCharacterStream(parameterIndex, reader, (int)length);
   }

   public void setClob(int parameterIndex, Reader reader) throws SQLException {
      this.setCharacterStream(parameterIndex, reader);
   }

   public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
      this.setCharacterStream(parameterIndex, reader, length);
   }

   public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
      this.setNCharacterStream(parameterIndex, value, -1L);
   }

   public void setNString(int parameterIndex, String x) throws SQLException {
      if (!this.charEncoding.equalsIgnoreCase("UTF-8") && !this.charEncoding.equalsIgnoreCase("utf8")) {
         if (x == null) {
            this.setNull(parameterIndex, 1);
         } else {
            int stringLength = x.length();
            StringBuffer buf = new StringBuffer((int)((double)x.length() * 1.1 + (double)4.0F));
            buf.append("_utf8");
            buf.append('\'');

            for(int i = 0; i < stringLength; ++i) {
               char c = x.charAt(i);
               switch (c) {
                  case '\u0000':
                     buf.append('\\');
                     buf.append('0');
                     break;
                  case '\n':
                     buf.append('\\');
                     buf.append('n');
                     break;
                  case '\r':
                     buf.append('\\');
                     buf.append('r');
                     break;
                  case '\u001a':
                     buf.append('\\');
                     buf.append('Z');
                     break;
                  case '"':
                     if (this.usingAnsiMode) {
                        buf.append('\\');
                     }

                     buf.append('"');
                     break;
                  case '\'':
                     buf.append('\\');
                     buf.append('\'');
                     break;
                  case '\\':
                     buf.append('\\');
                     buf.append('\\');
                     break;
                  default:
                     buf.append(c);
               }
            }

            buf.append('\'');
            String parameterAsString = buf.toString();
            byte[] parameterAsBytes = null;
            if (!this.isLoadDataQuery) {
               parameterAsBytes = StringUtils.getBytes(parameterAsString, this.connection.getCharsetConverter("UTF-8"), "UTF-8", this.connection.getServerCharacterEncoding(), this.connection.parserKnowsUnicode());
            } else {
               parameterAsBytes = parameterAsString.getBytes();
            }

            this.setInternal(parameterIndex, parameterAsBytes);
            this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = -9;
         }

      } else {
         this.setString(parameterIndex, x);
      }
   }

   public void setNCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
      try {
         if (reader == null) {
            this.setNull(parameterIndex, -1);
         } else {
            char[] c = null;
            int len = 0;
            boolean useLength = this.connection.getUseStreamLengthsInPrepStmts();
            if (useLength && length != -1L) {
               c = new char[(int)length];
               int numCharsRead = readFully(reader, c, (int)length);
               this.setNString(parameterIndex, new String(c, 0, numCharsRead));
            } else {
               c = new char[4096];
               StringBuffer buf = new StringBuffer();

               while((len = reader.read(c)) != -1) {
                  buf.append(c, 0, len);
               }

               this.setNString(parameterIndex, buf.toString());
            }

            this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 2011;
         }

      } catch (IOException ioEx) {
         throw SQLError.createSQLException(ioEx.toString(), "S1000");
      }
   }

   public void setNClob(int parameterIndex, Reader reader) throws SQLException {
      this.setNCharacterStream(parameterIndex, reader);
   }

   public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
      if (reader == null) {
         this.setNull(parameterIndex, -1);
      } else {
         this.setNCharacterStream(parameterIndex, reader, length);
      }

   }

   public ParameterBindings getParameterBindings() throws SQLException {
      return new EmulatedPreparedStatementBindings();
   }

   public String getPreparedSql() {
      return this.originalSql;
   }

   // $FF: synthetic method
   static Class class$(String x0) {
      try {
         return Class.forName(x0);
      } catch (ClassNotFoundException x1) {
         throw new NoClassDefFoundError(x1.getMessage());
      }
   }

   static {
      if (Util.isJdbc4()) {
         try {
            JDBC_4_PSTMT_2_ARG_CTOR = Class.forName("com.mysql.jdbc.JDBC4PreparedStatement").getConstructor(class$com$mysql$jdbc$ConnectionImpl == null ? (class$com$mysql$jdbc$ConnectionImpl = class$("com.mysql.jdbc.ConnectionImpl")) : class$com$mysql$jdbc$ConnectionImpl, class$java$lang$String == null ? (class$java$lang$String = class$("java.lang.String")) : class$java$lang$String);
            JDBC_4_PSTMT_3_ARG_CTOR = Class.forName("com.mysql.jdbc.JDBC4PreparedStatement").getConstructor(class$com$mysql$jdbc$ConnectionImpl == null ? (class$com$mysql$jdbc$ConnectionImpl = class$("com.mysql.jdbc.ConnectionImpl")) : class$com$mysql$jdbc$ConnectionImpl, class$java$lang$String == null ? (class$java$lang$String = class$("java.lang.String")) : class$java$lang$String, class$java$lang$String == null ? (class$java$lang$String = class$("java.lang.String")) : class$java$lang$String);
            JDBC_4_PSTMT_4_ARG_CTOR = Class.forName("com.mysql.jdbc.JDBC4PreparedStatement").getConstructor(class$com$mysql$jdbc$ConnectionImpl == null ? (class$com$mysql$jdbc$ConnectionImpl = class$("com.mysql.jdbc.ConnectionImpl")) : class$com$mysql$jdbc$ConnectionImpl, class$java$lang$String == null ? (class$java$lang$String = class$("java.lang.String")) : class$java$lang$String, class$java$lang$String == null ? (class$java$lang$String = class$("java.lang.String")) : class$java$lang$String, class$com$mysql$jdbc$PreparedStatement$ParseInfo == null ? (class$com$mysql$jdbc$PreparedStatement$ParseInfo = class$("com.mysql.jdbc.PreparedStatement$ParseInfo")) : class$com$mysql$jdbc$PreparedStatement$ParseInfo);
         } catch (SecurityException e) {
            throw new RuntimeException(e);
         } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
         } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
         }
      } else {
         JDBC_4_PSTMT_2_ARG_CTOR = null;
         JDBC_4_PSTMT_3_ARG_CTOR = null;
         JDBC_4_PSTMT_4_ARG_CTOR = null;
      }

      HEX_DIGITS = new byte[]{48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70};
   }

   class BatchParams {
      boolean[] isNull = null;
      boolean[] isStream = null;
      InputStream[] parameterStreams = null;
      byte[][] parameterStrings = (byte[][])null;
      int[] streamLengths = null;

      BatchParams(byte[][] strings, InputStream[] streams, boolean[] isStreamFlags, int[] lengths, boolean[] isNullFlags) {
         this.parameterStrings = new byte[strings.length][];
         this.parameterStreams = new InputStream[streams.length];
         this.isStream = new boolean[isStreamFlags.length];
         this.streamLengths = new int[lengths.length];
         this.isNull = new boolean[isNullFlags.length];
         System.arraycopy(strings, 0, this.parameterStrings, 0, strings.length);
         System.arraycopy(streams, 0, this.parameterStreams, 0, streams.length);
         System.arraycopy(isStreamFlags, 0, this.isStream, 0, isStreamFlags.length);
         System.arraycopy(lengths, 0, this.streamLengths, 0, lengths.length);
         System.arraycopy(isNullFlags, 0, this.isNull, 0, isNullFlags.length);
      }
   }

   class EndPoint {
      int begin;
      int end;

      EndPoint(int b, int e) {
         this.begin = b;
         this.end = e;
      }
   }

   class ParseInfo {
      char firstStmtChar = 0;
      boolean foundLimitClause = false;
      boolean foundLoadData = false;
      long lastUsed = 0L;
      int statementLength = 0;
      int statementStartPos = 0;
      byte[][] staticSql = (byte[][])null;

      public ParseInfo(String sql, ConnectionImpl conn, java.sql.DatabaseMetaData dbmd, String encoding, SingleByteCharsetConverter converter) throws SQLException {
         try {
            if (sql == null) {
               throw SQLError.createSQLException(Messages.getString("PreparedStatement.61"), "S1009");
            } else {
               this.lastUsed = System.currentTimeMillis();
               String quotedIdentifierString = dbmd.getIdentifierQuoteString();
               char quotedIdentifierChar = 0;
               if (quotedIdentifierString != null && !quotedIdentifierString.equals(" ") && quotedIdentifierString.length() > 0) {
                  quotedIdentifierChar = quotedIdentifierString.charAt(0);
               }

               this.statementLength = sql.length();
               ArrayList endpointList = new ArrayList();
               boolean inQuotes = false;
               char quoteChar = 0;
               boolean inQuotedId = false;
               int lastParmEnd = 0;
               int stopLookingForLimitClause = this.statementLength - 5;
               this.foundLimitClause = false;
               boolean noBackslashEscapes = PreparedStatement.this.connection.isNoBackslashEscapesSet();
               this.statementStartPos = PreparedStatement.this.findStartOfStatement(sql);

               label245:
               for(int i = this.statementStartPos; i < this.statementLength; ++i) {
                  char c = sql.charAt(i);
                  if (this.firstStmtChar == 0 && Character.isLetter(c)) {
                     this.firstStmtChar = Character.toUpperCase(c);
                  }

                  if (!noBackslashEscapes && c == '\\' && i < this.statementLength - 1) {
                     ++i;
                  } else {
                     if (!inQuotes && quotedIdentifierChar != 0 && c == quotedIdentifierChar) {
                        inQuotedId = !inQuotedId;
                     } else if (!inQuotedId) {
                        if (inQuotes) {
                           if ((c == '\'' || c == '"') && c == quoteChar) {
                              if (i < this.statementLength - 1 && sql.charAt(i + 1) == quoteChar) {
                                 ++i;
                                 continue;
                              }

                              inQuotes = !inQuotes;
                              quoteChar = 0;
                           } else if ((c == '\'' || c == '"') && c == quoteChar) {
                              inQuotes = !inQuotes;
                              quoteChar = 0;
                           }
                        } else {
                           if (c == '#' || c == '-' && i + 1 < this.statementLength && sql.charAt(i + 1) == '-') {
                              for(int endOfStmt = this.statementLength - 1; i < endOfStmt; ++i) {
                                 c = sql.charAt(i);
                                 if (c == '\r' || c == '\n') {
                                    continue label245;
                                 }
                              }
                              continue;
                           }

                           if (c == '/' && i + 1 < this.statementLength) {
                              char cNext = sql.charAt(i + 1);
                              if (cNext == '*') {
                                 i += 2;

                                 for(int j = i; j < this.statementLength; ++j) {
                                    ++i;
                                    cNext = sql.charAt(j);
                                    if (cNext == '*' && j + 1 < this.statementLength && sql.charAt(j + 1) == '/') {
                                       ++i;
                                       if (i < this.statementLength) {
                                          c = sql.charAt(i);
                                       }
                                       break;
                                    }
                                 }
                              }
                           } else if (c == '\'' || c == '"') {
                              inQuotes = true;
                              quoteChar = c;
                           }
                        }
                     }

                     if (c == '?' && !inQuotes && !inQuotedId) {
                        endpointList.add(new int[]{lastParmEnd, i});
                        lastParmEnd = i + 1;
                     }

                     if (!inQuotes && i < stopLookingForLimitClause && (c == 'L' || c == 'l')) {
                        char posI1 = sql.charAt(i + 1);
                        if (posI1 == 'I' || posI1 == 'i') {
                           char posM = sql.charAt(i + 2);
                           if (posM == 'M' || posM == 'm') {
                              char posI2 = sql.charAt(i + 3);
                              if (posI2 == 'I' || posI2 == 'i') {
                                 char posT = sql.charAt(i + 4);
                                 if (posT == 'T' || posT == 't') {
                                    this.foundLimitClause = true;
                                 }
                              }
                           }
                        }
                     }
                  }
               }

               if (this.firstStmtChar == 'L') {
                  if (StringUtils.startsWithIgnoreCaseAndWs(sql, "LOAD DATA")) {
                     this.foundLoadData = true;
                  } else {
                     this.foundLoadData = false;
                  }
               } else {
                  this.foundLoadData = false;
               }

               endpointList.add(new int[]{lastParmEnd, this.statementLength});
               this.staticSql = new byte[endpointList.size()][];
               char[] asCharArray = sql.toCharArray();

               for(int var26 = 0; var26 < this.staticSql.length; ++var26) {
                  int[] ep = (int[])endpointList.get(var26);
                  int end = ep[1];
                  int begin = ep[0];
                  int len = end - begin;
                  if (this.foundLoadData) {
                     String temp = new String(asCharArray, begin, len);
                     this.staticSql[var26] = temp.getBytes();
                  } else if (encoding != null) {
                     if (converter != null) {
                        this.staticSql[var26] = StringUtils.getBytes(sql, converter, encoding, PreparedStatement.this.connection.getServerCharacterEncoding(), begin, len, PreparedStatement.this.connection.parserKnowsUnicode());
                     } else {
                        String temp = new String(asCharArray, begin, len);
                        this.staticSql[var26] = StringUtils.getBytes(temp, encoding, PreparedStatement.this.connection.getServerCharacterEncoding(), PreparedStatement.this.connection.parserKnowsUnicode(), conn);
                     }
                  } else {
                     byte[] buf = new byte[len];

                     for(int j = 0; j < len; ++j) {
                        buf[j] = (byte)sql.charAt(begin + j);
                     }

                     this.staticSql[var26] = buf;
                  }
               }

            }
         } catch (StringIndexOutOfBoundsException oobEx) {
            SQLException sqlEx = new SQLException("Parse error for " + sql);
            sqlEx.initCause(oobEx);
            throw sqlEx;
         }
      }
   }

   class EmulatedPreparedStatementBindings implements ParameterBindings {
      private ResultSetImpl bindingsAsRs;
      private boolean[] parameterIsNull;

      public EmulatedPreparedStatementBindings() throws SQLException {
         List rows = new ArrayList();
         this.parameterIsNull = new boolean[PreparedStatement.this.parameterCount];
         System.arraycopy(PreparedStatement.this.isNull, 0, this.parameterIsNull, 0, PreparedStatement.this.parameterCount);
         byte[][] rowData = new byte[PreparedStatement.this.parameterCount][];
         Field[] typeMetadata = new Field[PreparedStatement.this.parameterCount];

         for(int i = 0; i < PreparedStatement.this.parameterCount; ++i) {
            rowData[i] = PreparedStatement.this.getBytesRepresentation(i);
            int charsetIndex = 0;
            if (PreparedStatement.this.parameterTypes[i] != -2 && PreparedStatement.this.parameterTypes[i] != 2004) {
               String mysqlEncodingName = CharsetMapping.getMysqlEncodingForJavaEncoding(PreparedStatement.this.connection.getEncoding(), PreparedStatement.this.connection);
               charsetIndex = CharsetMapping.getCharsetIndexForMysqlEncodingName(mysqlEncodingName);
            } else {
               charsetIndex = 63;
            }

            Field parameterMetadata = new Field((String)null, "parameter_" + (i + 1), charsetIndex, PreparedStatement.this.parameterTypes[i], rowData[i].length);
            parameterMetadata.setConnection(PreparedStatement.this.connection);
            typeMetadata[i] = parameterMetadata;
         }

         rows.add(new ByteArrayRow(rowData));
         this.bindingsAsRs = new ResultSetImpl(PreparedStatement.this.connection.getCatalog(), typeMetadata, new RowDataStatic(rows), PreparedStatement.this.connection, (StatementImpl)null);
         this.bindingsAsRs.next();
      }

      public Array getArray(int parameterIndex) throws SQLException {
         return this.bindingsAsRs.getArray(parameterIndex);
      }

      public InputStream getAsciiStream(int parameterIndex) throws SQLException {
         return this.bindingsAsRs.getAsciiStream(parameterIndex);
      }

      public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
         return this.bindingsAsRs.getBigDecimal(parameterIndex);
      }

      public InputStream getBinaryStream(int parameterIndex) throws SQLException {
         return this.bindingsAsRs.getBinaryStream(parameterIndex);
      }

      public java.sql.Blob getBlob(int parameterIndex) throws SQLException {
         return this.bindingsAsRs.getBlob(parameterIndex);
      }

      public boolean getBoolean(int parameterIndex) throws SQLException {
         return this.bindingsAsRs.getBoolean(parameterIndex);
      }

      public byte getByte(int parameterIndex) throws SQLException {
         return this.bindingsAsRs.getByte(parameterIndex);
      }

      public byte[] getBytes(int parameterIndex) throws SQLException {
         return this.bindingsAsRs.getBytes(parameterIndex);
      }

      public Reader getCharacterStream(int parameterIndex) throws SQLException {
         return this.bindingsAsRs.getCharacterStream(parameterIndex);
      }

      public java.sql.Clob getClob(int parameterIndex) throws SQLException {
         return this.bindingsAsRs.getClob(parameterIndex);
      }

      public Date getDate(int parameterIndex) throws SQLException {
         return this.bindingsAsRs.getDate(parameterIndex);
      }

      public double getDouble(int parameterIndex) throws SQLException {
         return this.bindingsAsRs.getDouble(parameterIndex);
      }

      public float getFloat(int parameterIndex) throws SQLException {
         return this.bindingsAsRs.getFloat(parameterIndex);
      }

      public int getInt(int parameterIndex) throws SQLException {
         return this.bindingsAsRs.getInt(parameterIndex);
      }

      public long getLong(int parameterIndex) throws SQLException {
         return this.bindingsAsRs.getLong(parameterIndex);
      }

      public Reader getNCharacterStream(int parameterIndex) throws SQLException {
         return this.bindingsAsRs.getCharacterStream(parameterIndex);
      }

      public Reader getNClob(int parameterIndex) throws SQLException {
         return this.bindingsAsRs.getCharacterStream(parameterIndex);
      }

      public Object getObject(int parameterIndex) throws SQLException {
         PreparedStatement.this.checkBounds(parameterIndex, 0);
         if (this.parameterIsNull[parameterIndex - 1]) {
            return null;
         } else {
            switch (PreparedStatement.this.parameterTypes[parameterIndex - 1]) {
               case -6:
                  return new Byte(this.getByte(parameterIndex));
               case -5:
                  return new Long(this.getLong(parameterIndex));
               case -4:
               case -3:
               case -2:
               case -1:
               case 0:
               case 1:
               case 2:
               case 3:
               case 7:
               default:
                  return this.bindingsAsRs.getObject(parameterIndex);
               case 4:
                  return new Integer(this.getInt(parameterIndex));
               case 5:
                  return new Short(this.getShort(parameterIndex));
               case 6:
                  return new Float(this.getFloat(parameterIndex));
               case 8:
                  return new Double(this.getDouble(parameterIndex));
            }
         }
      }

      public Ref getRef(int parameterIndex) throws SQLException {
         return this.bindingsAsRs.getRef(parameterIndex);
      }

      public short getShort(int parameterIndex) throws SQLException {
         return this.bindingsAsRs.getShort(parameterIndex);
      }

      public String getString(int parameterIndex) throws SQLException {
         return this.bindingsAsRs.getString(parameterIndex);
      }

      public Time getTime(int parameterIndex) throws SQLException {
         return this.bindingsAsRs.getTime(parameterIndex);
      }

      public Timestamp getTimestamp(int parameterIndex) throws SQLException {
         return this.bindingsAsRs.getTimestamp(parameterIndex);
      }

      public URL getURL(int parameterIndex) throws SQLException {
         return this.bindingsAsRs.getURL(parameterIndex);
      }

      public boolean isNull(int parameterIndex) throws SQLException {
         PreparedStatement.this.checkBounds(parameterIndex, 0);
         return this.parameterIsNull[parameterIndex - 1];
      }
   }
}
