package com.mysql.jdbc;

import com.mysql.jdbc.log.Log;
import com.mysql.jdbc.log.LogFactory;
import com.mysql.jdbc.log.NullLogger;
import com.mysql.jdbc.profiler.ProfilerEvent;
import com.mysql.jdbc.profiler.ProfilerEventHandler;
import com.mysql.jdbc.profiler.ProfilerEventHandlerFactory;
import com.mysql.jdbc.util.LRUCache;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TreeMap;

public class ConnectionImpl extends ConnectionPropertiesImpl implements Connection {
   private static final String JDBC_LOCAL_CHARACTER_SET_RESULTS = "jdbc.local.character_set_results";
   private static final Object CHARSET_CONVERTER_NOT_AVAILABLE_MARKER = new Object();
   public static Map charsetMap;
   protected static final String DEFAULT_LOGGER_CLASS = "com.mysql.jdbc.log.StandardLogger";
   private static final int HISTOGRAM_BUCKETS = 20;
   private static final String LOGGER_INSTANCE_NAME = "MySQL";
   private static Map mapTransIsolationNameToValue = null;
   private static final Log NULL_LOGGER = new NullLogger("MySQL");
   private static Map roundRobinStatsMap;
   private static final Map serverCollationByUrl = new HashMap();
   private static final Map serverConfigByUrl = new HashMap();
   private long queryTimeCount;
   private double queryTimeSum;
   private double queryTimeSumSquares;
   private double queryTimeMean;
   private static Timer cancelTimer;
   private List connectionLifecycleInterceptors;
   private static final Constructor JDBC_4_CONNECTION_CTOR;
   private boolean autoCommit = true;
   private Map cachedPreparedStatementParams;
   private String characterSetMetadata = null;
   private String characterSetResultsOnServer = null;
   private Map charsetConverterMap = new HashMap(CharsetMapping.getNumberOfCharsetsConfigured());
   private Map charsetToNumBytesMap;
   private long connectionCreationTimeMillis = 0L;
   private long connectionId;
   private String database = null;
   private java.sql.DatabaseMetaData dbmd = null;
   private TimeZone defaultTimeZone;
   private ProfilerEventHandler eventSink;
   private boolean executingFailoverReconnect = false;
   private boolean failedOver = false;
   private Throwable forceClosedReason;
   private Throwable forcedClosedLocation;
   private boolean hasIsolationLevels = false;
   private boolean hasQuotedIdentifiers = false;
   private String host = null;
   private List hostList = null;
   private int hostListSize = 0;
   private String[] indexToCharsetMapping;
   private MysqlIO io;
   private boolean isClientTzUTC;
   private boolean isClosed;
   private boolean isInGlobalTx;
   private boolean isRunningOnJDK13;
   private int isolationLevel;
   private boolean isServerTzUTC;
   private long lastQueryFinishedTime;
   private Log log;
   private long longestQueryTimeMs;
   private boolean lowerCaseTableNames;
   private long masterFailTimeMillis;
   private int maxAllowedPacket;
   private long maximumNumberTablesAccessed;
   private boolean maxRowsChanged;
   private long metricsLastReportedMs;
   private long minimumNumberTablesAccessed;
   private final Object mutex;
   private String myURL;
   private boolean needsPing;
   private int netBufferLength;
   private boolean noBackslashEscapes;
   private long numberOfPreparedExecutes;
   private long numberOfPrepares;
   private long numberOfQueriesIssued;
   private long numberOfResultSetsCreated;
   private long[] numTablesMetricsHistBreakpoints;
   private int[] numTablesMetricsHistCounts;
   private long[] oldHistBreakpoints;
   private int[] oldHistCounts;
   private Map openStatements;
   private LRUCache parsedCallableStatementCache;
   private boolean parserKnowsUnicode;
   private String password;
   private long[] perfMetricsHistBreakpoints;
   private int[] perfMetricsHistCounts;
   private Throwable pointOfOrigin;
   private int port;
   private boolean preferSlaveDuringFailover;
   protected Properties props;
   private long queriesIssuedFailedOver;
   private boolean readInfoMsg;
   private boolean readOnly;
   protected LRUCache resultSetMetadataCache;
   private TimeZone serverTimezoneTZ;
   private Map serverVariables;
   private long shortestQueryTimeMs;
   private Map statementsUsingMaxRows;
   private double totalQueryTimeMs;
   private boolean transactionsSupported;
   private Map typeMap;
   private boolean useAnsiQuotes;
   private String user;
   private boolean useServerPreparedStmts;
   private LRUCache serverSideStatementCheckCache;
   private LRUCache serverSideStatementCache;
   private Calendar sessionCalendar;
   private Calendar utcCalendar;
   private String origHostToConnectTo;
   private int origPortToConnectTo;
   private String origDatabaseToConnectTo;
   private String errorMessageEncoding;
   private boolean usePlatformCharsetConverters;
   private boolean hasTriedMasterFlag;
   private String statementComment;
   private boolean usingCachedConfig;
   // $FF: synthetic field
   static Class class$java$lang$String;
   // $FF: synthetic field
   static Class class$java$util$Timer;
   // $FF: synthetic field
   static Class class$java$util$Properties;
   // $FF: synthetic field
   static Class class$java$lang$Throwable;
   // $FF: synthetic field
   static Class class$java$sql$Blob;

   protected static SQLException appendMessageToException(SQLException sqlEx, String messageToAppend) {
      String origMessage = sqlEx.getMessage();
      String sqlState = sqlEx.getSQLState();
      int vendorErrorCode = sqlEx.getErrorCode();
      StringBuffer messageBuf = new StringBuffer(origMessage.length() + messageToAppend.length());
      messageBuf.append(origMessage);
      messageBuf.append(messageToAppend);
      SQLException sqlExceptionWithNewMessage = SQLError.createSQLException(messageBuf.toString(), sqlState, vendorErrorCode);

      try {
         Method getStackTraceMethod = null;
         Method setStackTraceMethod = null;
         Object theStackTraceAsObject = null;
         Class stackTraceElementClass = Class.forName("java.lang.StackTraceElement");
         Class stackTraceElementArrayClass = Array.newInstance(stackTraceElementClass, new int[]{0}).getClass();
         getStackTraceMethod = (class$java$lang$Throwable == null ? (class$java$lang$Throwable = class$("java.lang.Throwable")) : class$java$lang$Throwable).getMethod("getStackTrace");
         setStackTraceMethod = (class$java$lang$Throwable == null ? (class$java$lang$Throwable = class$("java.lang.Throwable")) : class$java$lang$Throwable).getMethod("setStackTrace", stackTraceElementArrayClass);
         if (getStackTraceMethod != null && setStackTraceMethod != null) {
            theStackTraceAsObject = getStackTraceMethod.invoke(sqlEx);
            setStackTraceMethod.invoke(sqlExceptionWithNewMessage, theStackTraceAsObject);
         }
      } catch (NoClassDefFoundError var12) {
      } catch (NoSuchMethodException var13) {
      } catch (Throwable var14) {
      }

      return sqlExceptionWithNewMessage;
   }

   protected static Timer getCancelTimer() {
      return cancelTimer;
   }

   protected static Connection getInstance(String hostToConnectTo, int portToConnectTo, Properties info, String databaseToConnectTo, String url) throws SQLException {
      return (Connection)(!Util.isJdbc4() ? new ConnectionImpl(hostToConnectTo, portToConnectTo, info, databaseToConnectTo, url) : (Connection)Util.handleNewInstance(JDBC_4_CONNECTION_CTOR, new Object[]{hostToConnectTo, Constants.integerValueOf(portToConnectTo), info, databaseToConnectTo, url}));
   }

   private static synchronized int getNextRoundRobinHostIndex(String url, List hostList) {
      int indexRange = hostList.size() - 1;
      int index = (int)(Math.random() * (double)indexRange);
      return index;
   }

   private static boolean nullSafeCompare(String s1, String s2) {
      if (s1 == null && s2 == null) {
         return true;
      } else {
         return s1 == null && s2 != null ? false : s1.equals(s2);
      }
   }

   protected ConnectionImpl() {
      this.indexToCharsetMapping = CharsetMapping.INDEX_TO_CHARSET;
      this.io = null;
      this.isClientTzUTC = false;
      this.isClosed = true;
      this.isInGlobalTx = false;
      this.isRunningOnJDK13 = false;
      this.isolationLevel = 2;
      this.isServerTzUTC = false;
      this.lastQueryFinishedTime = 0L;
      this.log = NULL_LOGGER;
      this.longestQueryTimeMs = 0L;
      this.lowerCaseTableNames = false;
      this.masterFailTimeMillis = 0L;
      this.maxAllowedPacket = 65536;
      this.maximumNumberTablesAccessed = 0L;
      this.maxRowsChanged = false;
      this.minimumNumberTablesAccessed = Long.MAX_VALUE;
      this.mutex = new Object();
      this.myURL = null;
      this.needsPing = false;
      this.netBufferLength = 16384;
      this.noBackslashEscapes = false;
      this.numberOfPreparedExecutes = 0L;
      this.numberOfPrepares = 0L;
      this.numberOfQueriesIssued = 0L;
      this.numberOfResultSetsCreated = 0L;
      this.oldHistBreakpoints = null;
      this.oldHistCounts = null;
      this.parserKnowsUnicode = false;
      this.password = null;
      this.port = 3306;
      this.preferSlaveDuringFailover = false;
      this.props = null;
      this.queriesIssuedFailedOver = 0L;
      this.readInfoMsg = false;
      this.readOnly = false;
      this.serverTimezoneTZ = null;
      this.serverVariables = null;
      this.shortestQueryTimeMs = Long.MAX_VALUE;
      this.totalQueryTimeMs = (double)0.0F;
      this.transactionsSupported = false;
      this.useAnsiQuotes = false;
      this.user = null;
      this.useServerPreparedStmts = false;
      this.errorMessageEncoding = "Cp1252";
      this.hasTriedMasterFlag = false;
      this.statementComment = null;
      this.usingCachedConfig = false;
   }

   protected ConnectionImpl(String hostToConnectTo, int portToConnectTo, Properties info, String databaseToConnectTo, String url) throws SQLException {
      this.indexToCharsetMapping = CharsetMapping.INDEX_TO_CHARSET;
      this.io = null;
      this.isClientTzUTC = false;
      this.isClosed = true;
      this.isInGlobalTx = false;
      this.isRunningOnJDK13 = false;
      this.isolationLevel = 2;
      this.isServerTzUTC = false;
      this.lastQueryFinishedTime = 0L;
      this.log = NULL_LOGGER;
      this.longestQueryTimeMs = 0L;
      this.lowerCaseTableNames = false;
      this.masterFailTimeMillis = 0L;
      this.maxAllowedPacket = 65536;
      this.maximumNumberTablesAccessed = 0L;
      this.maxRowsChanged = false;
      this.minimumNumberTablesAccessed = Long.MAX_VALUE;
      this.mutex = new Object();
      this.myURL = null;
      this.needsPing = false;
      this.netBufferLength = 16384;
      this.noBackslashEscapes = false;
      this.numberOfPreparedExecutes = 0L;
      this.numberOfPrepares = 0L;
      this.numberOfQueriesIssued = 0L;
      this.numberOfResultSetsCreated = 0L;
      this.oldHistBreakpoints = null;
      this.oldHistCounts = null;
      this.parserKnowsUnicode = false;
      this.password = null;
      this.port = 3306;
      this.preferSlaveDuringFailover = false;
      this.props = null;
      this.queriesIssuedFailedOver = 0L;
      this.readInfoMsg = false;
      this.readOnly = false;
      this.serverTimezoneTZ = null;
      this.serverVariables = null;
      this.shortestQueryTimeMs = Long.MAX_VALUE;
      this.totalQueryTimeMs = (double)0.0F;
      this.transactionsSupported = false;
      this.useAnsiQuotes = false;
      this.user = null;
      this.useServerPreparedStmts = false;
      this.errorMessageEncoding = "Cp1252";
      this.hasTriedMasterFlag = false;
      this.statementComment = null;
      this.usingCachedConfig = false;
      this.charsetToNumBytesMap = new HashMap();
      this.connectionCreationTimeMillis = System.currentTimeMillis();
      this.pointOfOrigin = new Throwable();
      this.origHostToConnectTo = hostToConnectTo;
      this.origPortToConnectTo = portToConnectTo;
      this.origDatabaseToConnectTo = databaseToConnectTo;

      try {
         (class$java$sql$Blob == null ? (class$java$sql$Blob = class$("java.sql.Blob")) : class$java$sql$Blob).getMethod("truncate", Long.TYPE);
         this.isRunningOnJDK13 = false;
      } catch (NoSuchMethodException var9) {
         this.isRunningOnJDK13 = true;
      }

      this.sessionCalendar = new GregorianCalendar();
      this.utcCalendar = new GregorianCalendar();
      this.utcCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
      this.log = LogFactory.getLogger(this.getLogger(), "MySQL");
      this.defaultTimeZone = Util.getDefaultTimeZone();
      if ("GMT".equalsIgnoreCase(this.defaultTimeZone.getID())) {
         this.isClientTzUTC = true;
      } else {
         this.isClientTzUTC = false;
      }

      this.openStatements = new HashMap();
      this.serverVariables = new HashMap();
      this.hostList = new ArrayList();
      if (hostToConnectTo == null) {
         this.host = "localhost";
         this.hostList.add(this.host);
      } else if (hostToConnectTo.indexOf(44) != -1) {
         StringTokenizer hostTokenizer = new StringTokenizer(hostToConnectTo, ",", false);

         while(hostTokenizer.hasMoreTokens()) {
            this.hostList.add(hostTokenizer.nextToken().trim());
         }
      } else {
         this.host = hostToConnectTo;
         this.hostList.add(this.host);
      }

      this.hostListSize = this.hostList.size();
      this.port = portToConnectTo;
      if (databaseToConnectTo == null) {
         databaseToConnectTo = "";
      }

      this.database = databaseToConnectTo;
      this.myURL = url;
      this.user = info.getProperty("user");
      this.password = info.getProperty("password");
      if (this.user == null || this.user.equals("")) {
         this.user = "";
      }

      if (this.password == null) {
         this.password = "";
      }

      this.props = info;
      this.initializeDriverProperties(info);

      try {
         this.dbmd = this.getMetaData(false, false);
         this.createNewIO(false);
      } catch (SQLException ex) {
         this.cleanup(ex);
         throw ex;
      } catch (Exception ex) {
         this.cleanup(ex);
         StringBuffer mesg = new StringBuffer(128);
         if (this.getParanoid()) {
            mesg.append("Cannot connect to MySQL server on ");
            mesg.append(this.host);
            mesg.append(":");
            mesg.append(this.port);
            mesg.append(".\n\n");
            mesg.append("Make sure that there is a MySQL server ");
            mesg.append("running on the machine/port you are trying ");
            mesg.append("to connect to and that the machine this software is running on ");
            mesg.append("is able to connect to this host/port (i.e. not firewalled). ");
            mesg.append("Also make sure that the server has not been started with the --skip-networking ");
            mesg.append("flag.\n\n");
         } else {
            mesg.append("Unable to connect to database.");
         }

         SQLException sqlEx = SQLError.createSQLException(mesg.toString(), "08S01");
         sqlEx.initCause(ex);
         throw sqlEx;
      }
   }

   private void addToHistogram(int[] histogramCounts, long[] histogramBreakpoints, long value, int numberOfTimes, long currentLowerBound, long currentUpperBound) {
      if (histogramCounts == null) {
         this.createInitialHistogram(histogramBreakpoints, currentLowerBound, currentUpperBound);
      } else {
         for(int i = 0; i < 20; ++i) {
            if (histogramBreakpoints[i] >= value) {
               histogramCounts[i] += numberOfTimes;
               break;
            }
         }
      }

   }

   private void addToPerformanceHistogram(long value, int numberOfTimes) {
      this.checkAndCreatePerformanceHistogram();
      this.addToHistogram(this.perfMetricsHistCounts, this.perfMetricsHistBreakpoints, value, numberOfTimes, this.shortestQueryTimeMs == Long.MAX_VALUE ? 0L : this.shortestQueryTimeMs, this.longestQueryTimeMs);
   }

   private void addToTablesAccessedHistogram(long value, int numberOfTimes) {
      this.checkAndCreateTablesAccessedHistogram();
      this.addToHistogram(this.numTablesMetricsHistCounts, this.numTablesMetricsHistBreakpoints, value, numberOfTimes, this.minimumNumberTablesAccessed == Long.MAX_VALUE ? 0L : this.minimumNumberTablesAccessed, this.maximumNumberTablesAccessed);
   }

   private void buildCollationMapping() throws SQLException {
      if (this.versionMeetsMinimum(4, 1, 0)) {
         TreeMap sortedCollationMap = null;
         if (this.getCacheServerConfiguration()) {
            synchronized(serverConfigByUrl) {
               sortedCollationMap = (TreeMap)serverCollationByUrl.get(this.getURL());
            }
         }

         java.sql.Statement stmt = null;
         ResultSet results = null;

         try {
            if (sortedCollationMap == null) {
               sortedCollationMap = new TreeMap();
               stmt = this.getMetadataSafeStatement();
               results = stmt.executeQuery("SHOW COLLATION");

               while(results.next()) {
                  String charsetName = results.getString(2);
                  Integer charsetIndex = Constants.integerValueOf(results.getInt(3));
                  sortedCollationMap.put(charsetIndex, charsetName);
               }

               if (this.getCacheServerConfiguration()) {
                  synchronized(serverConfigByUrl) {
                     serverCollationByUrl.put(this.getURL(), sortedCollationMap);
                  }
               }
            }

            int highestIndex = (Integer)sortedCollationMap.lastKey();
            if (CharsetMapping.INDEX_TO_CHARSET.length > highestIndex) {
               highestIndex = CharsetMapping.INDEX_TO_CHARSET.length;
            }

            this.indexToCharsetMapping = new String[highestIndex + 1];

            for(int i = 0; i < CharsetMapping.INDEX_TO_CHARSET.length; ++i) {
               this.indexToCharsetMapping[i] = CharsetMapping.INDEX_TO_CHARSET[i];
            }

            for(Map.Entry indexEntry : sortedCollationMap.entrySet()) {
               String mysqlCharsetName = (String)indexEntry.getValue();
               this.indexToCharsetMapping[(Integer)indexEntry.getKey()] = CharsetMapping.getJavaEncodingForMysqlEncoding(mysqlCharsetName, this);
            }
         } catch (SQLException e) {
            throw e;
         } finally {
            if (results != null) {
               try {
                  results.close();
               } catch (SQLException var20) {
               }
            }

            if (stmt != null) {
               try {
                  stmt.close();
               } catch (SQLException var19) {
               }
            }

         }
      } else {
         this.indexToCharsetMapping = CharsetMapping.INDEX_TO_CHARSET;
      }

   }

   private boolean canHandleAsServerPreparedStatement(String sql) throws SQLException {
      if (sql != null && sql.length() != 0) {
         if (!this.useServerPreparedStmts) {
            return false;
         } else if (this.getCachePreparedStatements()) {
            synchronized(this.serverSideStatementCheckCache) {
               Boolean flag = (Boolean)this.serverSideStatementCheckCache.get(sql);
               if (flag != null) {
                  return flag;
               } else {
                  boolean canHandle = this.canHandleAsServerPreparedStatementNoCache(sql);
                  if (sql.length() < this.getPreparedStatementCacheSqlLimit()) {
                     this.serverSideStatementCheckCache.put(sql, canHandle ? Boolean.TRUE : Boolean.FALSE);
                  }

                  return canHandle;
               }
            }
         } else {
            return this.canHandleAsServerPreparedStatementNoCache(sql);
         }
      } else {
         return true;
      }
   }

   private boolean canHandleAsServerPreparedStatementNoCache(String sql) throws SQLException {
      if (StringUtils.startsWithIgnoreCaseAndNonAlphaNumeric(sql, "CALL")) {
         return false;
      } else {
         boolean canHandleAsStatement = true;
         if (!this.versionMeetsMinimum(5, 0, 7) && (StringUtils.startsWithIgnoreCaseAndNonAlphaNumeric(sql, "SELECT") || StringUtils.startsWithIgnoreCaseAndNonAlphaNumeric(sql, "DELETE") || StringUtils.startsWithIgnoreCaseAndNonAlphaNumeric(sql, "INSERT") || StringUtils.startsWithIgnoreCaseAndNonAlphaNumeric(sql, "UPDATE") || StringUtils.startsWithIgnoreCaseAndNonAlphaNumeric(sql, "REPLACE"))) {
            int currentPos = 0;
            int statementLength = sql.length();
            int lastPosToLook = statementLength - 7;
            boolean allowBackslashEscapes = !this.noBackslashEscapes;
            char quoteChar = (char)(this.useAnsiQuotes ? 34 : 39);
            boolean foundLimitWithPlaceholder = false;

            while(currentPos < lastPosToLook) {
               int limitStart = StringUtils.indexOfIgnoreCaseRespectQuotes(currentPos, sql, "LIMIT ", quoteChar, allowBackslashEscapes);
               if (limitStart == -1) {
                  break;
               }

               for(currentPos = limitStart + 7; currentPos < statementLength; ++currentPos) {
                  char c = sql.charAt(currentPos);
                  if (!Character.isDigit(c) && !Character.isWhitespace(c) && c != ',' && c != '?') {
                     break;
                  }

                  if (c == '?') {
                     foundLimitWithPlaceholder = true;
                     break;
                  }
               }
            }

            canHandleAsStatement = !foundLimitWithPlaceholder;
         } else if (StringUtils.startsWithIgnoreCaseAndWs(sql, "CREATE TABLE")) {
            canHandleAsStatement = false;
         } else if (StringUtils.startsWithIgnoreCaseAndWs(sql, "DO")) {
            canHandleAsStatement = false;
         } else if (StringUtils.startsWithIgnoreCaseAndWs(sql, "SET")) {
            canHandleAsStatement = false;
         }

         return canHandleAsStatement;
      }
   }

   public void changeUser(String userName, String newPassword) throws SQLException {
      if (userName == null || userName.equals("")) {
         userName = "";
      }

      if (newPassword == null) {
         newPassword = "";
      }

      this.io.changeUser(userName, newPassword, this.database);
      this.user = userName;
      this.password = newPassword;
      if (this.versionMeetsMinimum(4, 1, 0)) {
         this.configureClientCharacterSet(true);
      }

      this.setupServerForTruncationChecks();
   }

   private boolean characterSetNamesMatches(String mysqlEncodingName) {
      return mysqlEncodingName != null && mysqlEncodingName.equalsIgnoreCase((String)this.serverVariables.get("character_set_client")) && mysqlEncodingName.equalsIgnoreCase((String)this.serverVariables.get("character_set_connection"));
   }

   private void checkAndCreatePerformanceHistogram() {
      if (this.perfMetricsHistCounts == null) {
         this.perfMetricsHistCounts = new int[20];
      }

      if (this.perfMetricsHistBreakpoints == null) {
         this.perfMetricsHistBreakpoints = new long[20];
      }

   }

   private void checkAndCreateTablesAccessedHistogram() {
      if (this.numTablesMetricsHistCounts == null) {
         this.numTablesMetricsHistCounts = new int[20];
      }

      if (this.numTablesMetricsHistBreakpoints == null) {
         this.numTablesMetricsHistBreakpoints = new long[20];
      }

   }

   protected void checkClosed() throws SQLException {
      if (this.isClosed) {
         StringBuffer messageBuf = new StringBuffer("No operations allowed after connection closed.");
         if (this.forcedClosedLocation != null || this.forceClosedReason != null) {
            messageBuf.append("Connection was implicitly closed ");
         }

         if (this.forcedClosedLocation != null) {
            messageBuf.append("\n\n at (stack trace):\n");
            messageBuf.append(Util.stackTraceToString(this.forcedClosedLocation));
         }

         if (this.forceClosedReason != null) {
            if (this.forcedClosedLocation != null) {
               messageBuf.append("\n\nDue ");
            } else {
               messageBuf.append("due ");
            }

            messageBuf.append("to underlying exception/error:\n");
            messageBuf.append(Util.stackTraceToString(this.forceClosedReason));
         }

         throw SQLError.createSQLException(messageBuf.toString(), "08003");
      }
   }

   private void checkServerEncoding() throws SQLException {
      if (!this.getUseUnicode() || this.getEncoding() == null) {
         String serverEncoding = (String)this.serverVariables.get("character_set");
         if (serverEncoding == null) {
            serverEncoding = (String)this.serverVariables.get("character_set_server");
         }

         String mappedServerEncoding = null;
         if (serverEncoding != null) {
            mappedServerEncoding = CharsetMapping.getJavaEncodingForMysqlEncoding(serverEncoding.toUpperCase(Locale.ENGLISH), this);
         }

         if (!this.getUseUnicode() && mappedServerEncoding != null) {
            SingleByteCharsetConverter converter = this.getCharsetConverter(mappedServerEncoding);
            if (converter != null) {
               this.setUseUnicode(true);
               this.setEncoding(mappedServerEncoding);
               return;
            }
         }

         if (serverEncoding != null) {
            if (mappedServerEncoding == null && Character.isLowerCase(serverEncoding.charAt(0))) {
               char[] ach = serverEncoding.toCharArray();
               ach[0] = Character.toUpperCase(serverEncoding.charAt(0));
               this.setEncoding(new String(ach));
            }

            if (mappedServerEncoding == null) {
               throw SQLError.createSQLException("Unknown character encoding on server '" + serverEncoding + "', use 'characterEncoding=' property " + " to provide correct mapping", "01S00");
            }

            try {
               "abc".getBytes(mappedServerEncoding);
               this.setEncoding(mappedServerEncoding);
               this.setUseUnicode(true);
            } catch (UnsupportedEncodingException var4) {
               throw SQLError.createSQLException("The driver can not map the character encoding '" + this.getEncoding() + "' that your server is using " + "to a character encoding your JVM understands. You " + "can specify this mapping manually by adding \"useUnicode=true\" " + "as well as \"characterEncoding=[an_encoding_your_jvm_understands]\" " + "to your JDBC URL.", "0S100");
            }
         }

      }
   }

   private void checkTransactionIsolationLevel() throws SQLException {
      String txIsolationName = null;
      if (this.versionMeetsMinimum(4, 0, 3)) {
         txIsolationName = "tx_isolation";
      } else {
         txIsolationName = "transaction_isolation";
      }

      String s = (String)this.serverVariables.get(txIsolationName);
      if (s != null) {
         Integer intTI = (Integer)mapTransIsolationNameToValue.get(s);
         if (intTI != null) {
            this.isolationLevel = intTI;
         }
      }

   }

   protected void abortInternal() throws SQLException {
      if (this.io != null) {
         try {
            this.io.forceClose();
         } catch (Throwable var2) {
         }

         this.io = null;
      }

      this.isClosed = true;
   }

   private void cleanup(Throwable whyCleanedUp) {
      try {
         if (this.io != null && !this.isClosed()) {
            this.realClose(false, false, false, whyCleanedUp);
         } else if (this.io != null) {
            this.io.forceClose();
         }
      } catch (SQLException var3) {
      }

      this.isClosed = true;
   }

   public void clearHasTriedMaster() {
      this.hasTriedMasterFlag = false;
   }

   public void clearWarnings() throws SQLException {
   }

   public java.sql.PreparedStatement clientPrepareStatement(String sql) throws SQLException {
      return this.clientPrepareStatement(sql, 1005, 1007);
   }

   public java.sql.PreparedStatement clientPrepareStatement(String sql, int autoGenKeyIndex) throws SQLException {
      java.sql.PreparedStatement pStmt = this.clientPrepareStatement(sql);
      ((PreparedStatement)pStmt).setRetrieveGeneratedKeys(autoGenKeyIndex == 1);
      return pStmt;
   }

   public java.sql.PreparedStatement clientPrepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
      return this.clientPrepareStatement(sql, resultSetType, resultSetConcurrency, true);
   }

   protected java.sql.PreparedStatement clientPrepareStatement(String sql, int resultSetType, int resultSetConcurrency, boolean processEscapeCodesIfNeeded) throws SQLException {
      this.checkClosed();
      String nativeSql = processEscapeCodesIfNeeded && this.getProcessEscapeCodesForPrepStmts() ? this.nativeSQL(sql) : sql;
      PreparedStatement pStmt = null;
      if (this.getCachePreparedStatements()) {
         synchronized(this.cachedPreparedStatementParams) {
            PreparedStatement.ParseInfo pStmtInfo = (PreparedStatement.ParseInfo)this.cachedPreparedStatementParams.get(nativeSql);
            if (pStmtInfo == null) {
               pStmt = PreparedStatement.getInstance(this, nativeSql, this.database);
               PreparedStatement.ParseInfo parseInfo = pStmt.getParseInfo();
               if (parseInfo.statementLength < this.getPreparedStatementCacheSqlLimit()) {
                  if (this.cachedPreparedStatementParams.size() >= this.getPreparedStatementCacheSize()) {
                     Iterator oldestIter = this.cachedPreparedStatementParams.keySet().iterator();
                     long lruTime = Long.MAX_VALUE;
                     String oldestSql = null;

                     while(oldestIter.hasNext()) {
                        String sqlKey = (String)oldestIter.next();
                        PreparedStatement.ParseInfo lruInfo = (PreparedStatement.ParseInfo)this.cachedPreparedStatementParams.get(sqlKey);
                        if (lruInfo.lastUsed < lruTime) {
                           lruTime = lruInfo.lastUsed;
                           oldestSql = sqlKey;
                        }
                     }

                     if (oldestSql != null) {
                        this.cachedPreparedStatementParams.remove(oldestSql);
                     }
                  }

                  this.cachedPreparedStatementParams.put(nativeSql, pStmt.getParseInfo());
               }
            } else {
               pStmtInfo.lastUsed = System.currentTimeMillis();
               pStmt = new PreparedStatement(this, nativeSql, this.database, pStmtInfo);
            }
         }
      } else {
         pStmt = PreparedStatement.getInstance(this, nativeSql, this.database);
      }

      pStmt.setResultSetType(resultSetType);
      pStmt.setResultSetConcurrency(resultSetConcurrency);
      return pStmt;
   }

   public java.sql.PreparedStatement clientPrepareStatement(String sql, int[] autoGenKeyIndexes) throws SQLException {
      PreparedStatement pStmt = (PreparedStatement)this.clientPrepareStatement(sql);
      pStmt.setRetrieveGeneratedKeys(autoGenKeyIndexes != null && autoGenKeyIndexes.length > 0);
      return pStmt;
   }

   public java.sql.PreparedStatement clientPrepareStatement(String sql, String[] autoGenKeyColNames) throws SQLException {
      PreparedStatement pStmt = (PreparedStatement)this.clientPrepareStatement(sql);
      pStmt.setRetrieveGeneratedKeys(autoGenKeyColNames != null && autoGenKeyColNames.length > 0);
      return pStmt;
   }

   public java.sql.PreparedStatement clientPrepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
      return this.clientPrepareStatement(sql, resultSetType, resultSetConcurrency, true);
   }

   public synchronized void close() throws SQLException {
      if (this.connectionLifecycleInterceptors != null) {
         (new IterateBlock(this.connectionLifecycleInterceptors.iterator()) {
            void forEach(Object each) throws SQLException {
               ((ConnectionLifecycleInterceptor)each).close();
            }
         }).doForAll();
      }

      this.realClose(true, true, false, (Throwable)null);
   }

   private void closeAllOpenStatements() throws SQLException {
      SQLException postponedException = null;
      if (this.openStatements != null) {
         List currentlyOpenStatements = new ArrayList();
         Iterator iter = this.openStatements.keySet().iterator();

         while(iter.hasNext()) {
            currentlyOpenStatements.add(iter.next());
         }

         int numStmts = currentlyOpenStatements.size();

         for(int i = 0; i < numStmts; ++i) {
            StatementImpl stmt = (StatementImpl)currentlyOpenStatements.get(i);

            try {
               stmt.realClose(false, true);
            } catch (SQLException sqlEx) {
               postponedException = sqlEx;
            }
         }

         if (postponedException != null) {
            throw postponedException;
         }
      }

   }

   private void closeStatement(java.sql.Statement stmt) {
      if (stmt != null) {
         try {
            stmt.close();
         } catch (SQLException var3) {
         }

         java.sql.Statement var4 = null;
      }

   }

   public void commit() throws SQLException {
      synchronized(this.getMutex()) {
         this.checkClosed();

         try {
            if (this.connectionLifecycleInterceptors != null) {
               IterateBlock iter = new IterateBlock(this.connectionLifecycleInterceptors.iterator()) {
                  void forEach(Object each) throws SQLException {
                     if (!((ConnectionLifecycleInterceptor)each).commit()) {
                        this.stopIterating = true;
                     }

                  }
               };
               iter.doForAll();
               if (!iter.fullIteration()) {
                  return;
               }
            }

            if (this.autoCommit && !this.getRelaxAutoCommit()) {
               throw SQLError.createSQLException("Can't call commit when autocommit=true");
            } else if (this.transactionsSupported) {
               if (!this.getUseLocalSessionState() || !this.versionMeetsMinimum(5, 0, 0) || this.io.inTransactionOnServer()) {
                  this.execSQL((StatementImpl)null, "commit", -1, (Buffer)null, 1003, 1007, false, this.database, (Field[])null, false);
               }
            }
         } catch (SQLException sqlException) {
            if ("08S01".equals(sqlException.getSQLState())) {
               throw SQLError.createSQLException("Communications link failure during commit(). Transaction resolution unknown.", "08007");
            } else {
               throw sqlException;
            }
         } finally {
            this.needsPing = this.getReconnectAtTxEnd();
         }
      }
   }

   private void configureCharsetProperties() throws SQLException {
      if (this.getEncoding() != null) {
         try {
            String testString = "abc";
            testString.getBytes(this.getEncoding());
         } catch (UnsupportedEncodingException var5) {
            String oldEncoding = this.getEncoding();
            this.setEncoding(CharsetMapping.getJavaEncodingForMysqlEncoding(oldEncoding, this));
            if (this.getEncoding() == null) {
               throw SQLError.createSQLException("Java does not support the MySQL character encoding  encoding '" + oldEncoding + "'.", "01S00");
            }

            try {
               String testString = "abc";
               testString.getBytes(this.getEncoding());
            } catch (UnsupportedEncodingException var4) {
               throw SQLError.createSQLException("Unsupported character encoding '" + this.getEncoding() + "'.", "01S00");
            }
         }
      }

   }

   private boolean configureClientCharacterSet(boolean dontCheckServerMatch) throws SQLException {
      String realJavaEncoding = this.getEncoding();
      boolean characterSetAlreadyConfigured = false;

      try {
         if (this.versionMeetsMinimum(4, 1, 0)) {
            characterSetAlreadyConfigured = true;
            this.setUseUnicode(true);
            this.configureCharsetProperties();
            realJavaEncoding = this.getEncoding();

            try {
               if (this.props != null && this.props.getProperty("com.mysql.jdbc.faultInjection.serverCharsetIndex") != null) {
                  this.io.serverCharsetIndex = Integer.parseInt(this.props.getProperty("com.mysql.jdbc.faultInjection.serverCharsetIndex"));
               }

               String serverEncodingToSet = CharsetMapping.INDEX_TO_CHARSET[this.io.serverCharsetIndex];
               if (serverEncodingToSet == null || serverEncodingToSet.length() == 0) {
                  if (realJavaEncoding == null) {
                     throw SQLError.createSQLException("Unknown initial character set index '" + this.io.serverCharsetIndex + "' received from server. Initial client character set can be forced via the 'characterEncoding' property.", "S1000");
                  }

                  this.setEncoding(realJavaEncoding);
               }

               if (this.versionMeetsMinimum(4, 1, 0) && "ISO8859_1".equalsIgnoreCase(serverEncodingToSet)) {
                  serverEncodingToSet = "Cp1252";
               }

               this.setEncoding(serverEncodingToSet);
            } catch (ArrayIndexOutOfBoundsException var12) {
               if (realJavaEncoding == null) {
                  throw SQLError.createSQLException("Unknown initial character set index '" + this.io.serverCharsetIndex + "' received from server. Initial client character set can be forced via the 'characterEncoding' property.", "S1000");
               }

               this.setEncoding(realJavaEncoding);
            }

            if (this.getEncoding() == null) {
               this.setEncoding("ISO8859_1");
            }

            if (this.getUseUnicode()) {
               if (realJavaEncoding != null) {
                  if (!realJavaEncoding.equalsIgnoreCase("UTF-8") && !realJavaEncoding.equalsIgnoreCase("UTF8")) {
                     String mysqlEncodingName = CharsetMapping.getMysqlEncodingForJavaEncoding(realJavaEncoding.toUpperCase(Locale.ENGLISH), this);
                     if (mysqlEncodingName != null && (dontCheckServerMatch || !this.characterSetNamesMatches(mysqlEncodingName))) {
                        this.execSQL((StatementImpl)null, "SET NAMES " + mysqlEncodingName, -1, (Buffer)null, 1003, 1007, false, this.database, (Field[])null, false);
                     }

                     this.setEncoding(realJavaEncoding);
                  } else {
                     if (!this.getUseOldUTF8Behavior() && (dontCheckServerMatch || !this.characterSetNamesMatches("utf8"))) {
                        this.execSQL((StatementImpl)null, "SET NAMES utf8", -1, (Buffer)null, 1003, 1007, false, this.database, (Field[])null, false);
                     }

                     this.setEncoding(realJavaEncoding);
                  }
               } else if (this.getEncoding() != null) {
                  String mysqlEncodingName = CharsetMapping.getMysqlEncodingForJavaEncoding(this.getEncoding().toUpperCase(Locale.ENGLISH), this);
                  if (dontCheckServerMatch || !this.characterSetNamesMatches(mysqlEncodingName)) {
                     this.execSQL((StatementImpl)null, "SET NAMES " + mysqlEncodingName, -1, (Buffer)null, 1003, 1007, false, this.database, (Field[])null, false);
                  }

                  realJavaEncoding = this.getEncoding();
               }
            }

            String onServer = null;
            boolean isNullOnServer = false;
            if (this.serverVariables != null) {
               onServer = (String)this.serverVariables.get("character_set_results");
               isNullOnServer = onServer == null || "NULL".equalsIgnoreCase(onServer) || onServer.length() == 0;
            }

            if (this.getCharacterSetResults() == null) {
               if (!isNullOnServer) {
                  this.execSQL((StatementImpl)null, "SET character_set_results = NULL", -1, (Buffer)null, 1003, 1007, false, this.database, (Field[])null, false);
                  if (!this.usingCachedConfig) {
                     this.serverVariables.put("jdbc.local.character_set_results", (Object)null);
                  }
               } else if (!this.usingCachedConfig) {
                  this.serverVariables.put("jdbc.local.character_set_results", onServer);
               }
            } else {
               String charsetResults = this.getCharacterSetResults();
               String mysqlEncodingName = null;
               if (!"UTF-8".equalsIgnoreCase(charsetResults) && !"UTF8".equalsIgnoreCase(charsetResults)) {
                  mysqlEncodingName = CharsetMapping.getMysqlEncodingForJavaEncoding(charsetResults.toUpperCase(Locale.ENGLISH), this);
               } else {
                  mysqlEncodingName = "utf8";
               }

               if (!mysqlEncodingName.equalsIgnoreCase((String)this.serverVariables.get("character_set_results"))) {
                  StringBuffer setBuf = new StringBuffer("SET character_set_results = ".length() + mysqlEncodingName.length());
                  setBuf.append("SET character_set_results = ").append(mysqlEncodingName);
                  this.execSQL((StatementImpl)null, setBuf.toString(), -1, (Buffer)null, 1003, 1007, false, this.database, (Field[])null, false);
                  if (!this.usingCachedConfig) {
                     this.serverVariables.put("jdbc.local.character_set_results", mysqlEncodingName);
                  }
               } else if (!this.usingCachedConfig) {
                  this.serverVariables.put("jdbc.local.character_set_results", onServer);
               }
            }

            if (this.getConnectionCollation() != null) {
               StringBuffer setBuf = new StringBuffer("SET collation_connection = ".length() + this.getConnectionCollation().length());
               setBuf.append("SET collation_connection = ").append(this.getConnectionCollation());
               this.execSQL((StatementImpl)null, setBuf.toString(), -1, (Buffer)null, 1003, 1007, false, this.database, (Field[])null, false);
            }
         } else {
            realJavaEncoding = this.getEncoding();
         }
      } finally {
         this.setEncoding(realJavaEncoding);
      }

      return characterSetAlreadyConfigured;
   }

   private void configureTimezone() throws SQLException {
      String configuredTimeZoneOnServer = (String)this.serverVariables.get("timezone");
      if (configuredTimeZoneOnServer == null) {
         configuredTimeZoneOnServer = (String)this.serverVariables.get("time_zone");
         if ("SYSTEM".equalsIgnoreCase(configuredTimeZoneOnServer)) {
            configuredTimeZoneOnServer = (String)this.serverVariables.get("system_time_zone");
         }
      }

      String canoncicalTimezone = this.getServerTimezone();
      if ((this.getUseTimezone() || !this.getUseLegacyDatetimeCode()) && configuredTimeZoneOnServer != null) {
         if (canoncicalTimezone == null || StringUtils.isEmptyOrWhitespaceOnly(canoncicalTimezone)) {
            try {
               canoncicalTimezone = TimeUtil.getCanoncialTimezone(configuredTimeZoneOnServer);
               if (canoncicalTimezone == null) {
                  throw SQLError.createSQLException("Can't map timezone '" + configuredTimeZoneOnServer + "' to " + " canonical timezone.", "S1009");
               }
            } catch (IllegalArgumentException iae) {
               throw SQLError.createSQLException(iae.getMessage(), "S1000");
            }
         }
      } else {
         canoncicalTimezone = this.getServerTimezone();
      }

      if (canoncicalTimezone != null && canoncicalTimezone.length() > 0) {
         this.serverTimezoneTZ = TimeZone.getTimeZone(canoncicalTimezone);
         if (!canoncicalTimezone.equalsIgnoreCase("GMT") && this.serverTimezoneTZ.getID().equals("GMT")) {
            throw SQLError.createSQLException("No timezone mapping entry for '" + canoncicalTimezone + "'", "S1009");
         }

         if ("GMT".equalsIgnoreCase(this.serverTimezoneTZ.getID())) {
            this.isServerTzUTC = true;
         } else {
            this.isServerTzUTC = false;
         }
      }

   }

   private void createInitialHistogram(long[] breakpoints, long lowerBound, long upperBound) {
      double bucketSize = ((double)upperBound - (double)lowerBound) / (double)20.0F * (double)1.25F;
      if (bucketSize < (double)1.0F) {
         bucketSize = (double)1.0F;
      }

      for(int i = 0; i < 20; ++i) {
         breakpoints[i] = lowerBound;
         lowerBound = (long)((double)lowerBound + bucketSize);
      }

   }

   protected void createNewIO(boolean isForReconnect) throws SQLException {
      synchronized(this.mutex) {
         Properties mergedProps = this.exposeAsProperties(this.props);
         long queriesIssuedFailedOverCopy = this.queriesIssuedFailedOver;
         this.queriesIssuedFailedOver = 0L;

         try {
            if (!this.getHighAvailability() && !this.failedOver) {
               boolean connectionGood = false;
               Exception connectionNotEstablishedBecause = null;
               int hostIndex = 0;
               if (this.getRoundRobinLoadBalance()) {
                  hostIndex = getNextRoundRobinHostIndex(this.getURL(), this.hostList);
               }

               for(; hostIndex < this.hostListSize; ++hostIndex) {
                  if (hostIndex == 0) {
                     this.hasTriedMasterFlag = true;
                  }

                  try {
                     String newHostPortPair = (String)this.hostList.get(hostIndex);
                     int newPort = 3306;
                     String[] hostPortPair = NonRegisteringDriver.parseHostPortPair(newHostPortPair);
                     String newHost = hostPortPair[0];
                     if (newHost == null || StringUtils.isEmptyOrWhitespaceOnly(newHost)) {
                        newHost = "localhost";
                     }

                     if (hostPortPair[1] != null) {
                        try {
                           newPort = Integer.parseInt(hostPortPair[1]);
                        } catch (NumberFormatException var32) {
                           throw SQLError.createSQLException("Illegal connection port value '" + hostPortPair[1] + "'", "01S00");
                        }
                     }

                     this.io = new MysqlIO(newHost, newPort, mergedProps, this.getSocketFactoryClassName(), this, this.getSocketTimeout(), this.largeRowSizeThreshold.getValueAsInt());
                     this.io.doHandshake(this.user, this.password, this.database);
                     this.connectionId = this.io.getThreadId();
                     this.isClosed = false;
                     boolean oldAutoCommit = this.getAutoCommit();
                     int oldIsolationLevel = this.isolationLevel;
                     boolean oldReadOnly = this.isReadOnly();
                     String oldCatalog = this.getCatalog();
                     this.initializePropsFromServer();
                     if (isForReconnect) {
                        this.setAutoCommit(oldAutoCommit);
                        if (this.hasIsolationLevels) {
                           this.setTransactionIsolation(oldIsolationLevel);
                        }

                        this.setCatalog(oldCatalog);
                     }

                     if (hostIndex != 0) {
                        this.setFailedOverState();
                        queriesIssuedFailedOverCopy = 0L;
                     } else {
                        this.failedOver = false;
                        queriesIssuedFailedOverCopy = 0L;
                        if (this.hostListSize > 1) {
                           this.setReadOnlyInternal(false);
                        } else {
                           this.setReadOnlyInternal(oldReadOnly);
                        }
                     }

                     connectionGood = true;
                     break;
                  } catch (Exception var33) {
                     if (this.io != null) {
                        this.io.forceClose();
                     }

                     connectionNotEstablishedBecause = var33;
                     connectionGood = false;
                     if (var33 instanceof SQLException) {
                        SQLException sqlEx = (SQLException)var33;
                        String sqlState = sqlEx.getSQLState();
                        if (sqlState == null || !sqlState.equals("08S01")) {
                           throw sqlEx;
                        }
                     }

                     if (this.getRoundRobinLoadBalance()) {
                        hostIndex = getNextRoundRobinHostIndex(this.getURL(), this.hostList) - 1;
                     } else if (this.hostListSize - 1 == hostIndex) {
                        throw SQLError.createCommunicationsException(this, this.io != null ? this.io.getLastPacketSentTimeMs() : 0L, this.io != null ? this.io.getLastPacketReceivedTimeMs() : 0L, var33);
                     }
                  }
               }

               if (!connectionGood) {
                  SQLException chainedEx = SQLError.createSQLException(Messages.getString("Connection.UnableToConnect"), "08001");
                  chainedEx.initCause(connectionNotEstablishedBecause);
                  throw chainedEx;
               }
            } else {
               double timeout = (double)this.getInitialTimeout();
               boolean connectionGood = false;
               Exception connectionException = null;
               int hostIndex = 0;
               if (this.getRoundRobinLoadBalance()) {
                  hostIndex = getNextRoundRobinHostIndex(this.getURL(), this.hostList);
               }

               for(; hostIndex < this.hostListSize && !connectionGood; ++hostIndex) {
                  if (hostIndex == 0) {
                     this.hasTriedMasterFlag = true;
                  }

                  if (this.preferSlaveDuringFailover && hostIndex == 0) {
                     ++hostIndex;
                  }

                  for(int attemptCount = 0; attemptCount < this.getMaxReconnects() && !connectionGood; ++attemptCount) {
                     try {
                        if (this.io != null) {
                           this.io.forceClose();
                        }

                        String newHostPortPair = (String)this.hostList.get(hostIndex);
                        int newPort = 3306;
                        String[] hostPortPair = NonRegisteringDriver.parseHostPortPair(newHostPortPair);
                        String newHost = hostPortPair[0];
                        if (newHost == null || StringUtils.isEmptyOrWhitespaceOnly(newHost)) {
                           newHost = "localhost";
                        }

                        if (hostPortPair[1] != null) {
                           try {
                              newPort = Integer.parseInt(hostPortPair[1]);
                           } catch (NumberFormatException var31) {
                              throw SQLError.createSQLException("Illegal connection port value '" + hostPortPair[1] + "'", "01S00");
                           }
                        }

                        this.io = new MysqlIO(newHost, newPort, mergedProps, this.getSocketFactoryClassName(), this, this.getSocketTimeout(), this.largeRowSizeThreshold.getValueAsInt());
                        this.io.doHandshake(this.user, this.password, this.database);
                        this.pingInternal(false);
                        this.connectionId = this.io.getThreadId();
                        this.isClosed = false;
                        boolean oldAutoCommit = this.getAutoCommit();
                        int oldIsolationLevel = this.isolationLevel;
                        boolean oldReadOnly = this.isReadOnly();
                        String oldCatalog = this.getCatalog();
                        this.initializePropsFromServer();
                        if (isForReconnect) {
                           this.setAutoCommit(oldAutoCommit);
                           if (this.hasIsolationLevels) {
                              this.setTransactionIsolation(oldIsolationLevel);
                           }

                           this.setCatalog(oldCatalog);
                        }

                        connectionGood = true;
                        if (hostIndex != 0) {
                           this.setFailedOverState();
                           queriesIssuedFailedOverCopy = 0L;
                        } else {
                           this.failedOver = false;
                           queriesIssuedFailedOverCopy = 0L;
                           if (this.hostListSize > 1) {
                              this.setReadOnlyInternal(false);
                           } else {
                              this.setReadOnlyInternal(oldReadOnly);
                           }
                        }
                        break;
                     } catch (Exception EEE) {
                        connectionException = EEE;
                        connectionGood = false;
                        if (this.getRoundRobinLoadBalance()) {
                           hostIndex = getNextRoundRobinHostIndex(this.getURL(), this.hostList) - 1;
                        }

                        if (connectionGood) {
                           break;
                        }

                        if (attemptCount > 0) {
                           try {
                              Thread.sleep((long)timeout * 1000L);
                           } catch (InterruptedException var30) {
                           }
                        }
                     }
                  }
               }

               if (!connectionGood) {
                  SQLException chainedEx = SQLError.createSQLException(Messages.getString("Connection.UnableToConnectWithRetries", new Object[]{new Integer(this.getMaxReconnects())}), "08001");
                  chainedEx.initCause(connectionException);
                  throw chainedEx;
               }
            }

            if (this.getParanoid() && !this.getHighAvailability() && this.hostListSize <= 1) {
               this.password = null;
               this.user = null;
            }

            if (isForReconnect) {
               Iterator statementIter = this.openStatements.values().iterator();
               Stack serverPreparedStatements = null;

               while(statementIter.hasNext()) {
                  Object statementObj = statementIter.next();
                  if (statementObj instanceof ServerPreparedStatement) {
                     if (serverPreparedStatements == null) {
                        serverPreparedStatements = new Stack();
                     }

                     serverPreparedStatements.add(statementObj);
                  }
               }

               if (serverPreparedStatements != null) {
                  while(!serverPreparedStatements.isEmpty()) {
                     ((ServerPreparedStatement)serverPreparedStatements.pop()).rePrepare();
                  }
               }
            }
         } finally {
            this.queriesIssuedFailedOver = queriesIssuedFailedOverCopy;
            if (this.io != null && this.getStatementInterceptors() != null) {
               this.io.initializeStatementInterceptors(this.getStatementInterceptors(), mergedProps);
            }

         }

      }
   }

   private void createPreparedStatementCaches() {
      int cacheSize = this.getPreparedStatementCacheSize();
      this.cachedPreparedStatementParams = new HashMap(cacheSize);
      if (this.getUseServerPreparedStmts()) {
         this.serverSideStatementCheckCache = new LRUCache(cacheSize);
         this.serverSideStatementCache = new LRUCache(cacheSize) {
            protected boolean removeEldestEntry(Map.Entry eldest) {
               if (this.maxElements <= 1) {
                  return false;
               } else {
                  boolean removeIt = super.removeEldestEntry(eldest);
                  if (removeIt) {
                     ServerPreparedStatement ps = (ServerPreparedStatement)eldest.getValue();
                     ps.isCached = false;
                     ps.setClosed(false);

                     try {
                        ps.close();
                     } catch (SQLException var5) {
                     }
                  }

                  return removeIt;
               }
            }
         };
      }

   }

   public java.sql.Statement createStatement() throws SQLException {
      return this.createStatement(1003, 1007);
   }

   public java.sql.Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
      this.checkClosed();
      StatementImpl stmt = new StatementImpl(this, this.database);
      stmt.setResultSetType(resultSetType);
      stmt.setResultSetConcurrency(resultSetConcurrency);
      return stmt;
   }

   public java.sql.Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
      if (this.getPedantic() && resultSetHoldability != 1) {
         throw SQLError.createSQLException("HOLD_CUSRORS_OVER_COMMIT is only supported holdability level", "S1009");
      } else {
         return this.createStatement(resultSetType, resultSetConcurrency);
      }
   }

   protected void dumpTestcaseQuery(String query) {
      System.err.println(query);
   }

   protected Connection duplicate() throws SQLException {
      return new ConnectionImpl(this.origHostToConnectTo, this.origPortToConnectTo, this.props, this.origDatabaseToConnectTo, this.myURL);
   }

   ResultSetInternalMethods execSQL(StatementImpl callingStatement, String sql, int maxRows, Buffer packet, int resultSetType, int resultSetConcurrency, boolean streamResults, String catalog, Field[] cachedMetadata) throws SQLException {
      return this.execSQL(callingStatement, sql, maxRows, packet, resultSetType, resultSetConcurrency, streamResults, catalog, cachedMetadata, false);
   }

   ResultSetInternalMethods execSQL(StatementImpl callingStatement, String sql, int maxRows, Buffer packet, int resultSetType, int resultSetConcurrency, boolean streamResults, String catalog, Field[] cachedMetadata, boolean isBatch) throws SQLException {
      synchronized(this.mutex) {
         long queryStartTime = 0L;
         int endOfQueryPacketPosition = 0;
         if (packet != null) {
            endOfQueryPacketPosition = packet.getPosition();
         }

         if (this.getGatherPerformanceMetrics()) {
            queryStartTime = System.currentTimeMillis();
         }

         this.lastQueryFinishedTime = 0L;
         if (this.failedOver && this.autoCommit && !isBatch && this.shouldFallBack() && !this.executingFailoverReconnect) {
            try {
               this.executingFailoverReconnect = true;
               this.createNewIO(true);
               String connectedHost = this.io.getHost();
               if (connectedHost != null && this.hostList.get(0).equals(connectedHost)) {
                  this.failedOver = false;
                  this.queriesIssuedFailedOver = 0L;
                  this.setReadOnlyInternal(false);
               }
            } finally {
               this.executingFailoverReconnect = false;
            }
         }

         if ((this.getHighAvailability() || this.failedOver) && (this.autoCommit || this.getAutoReconnectForPools()) && this.needsPing && !isBatch) {
            try {
               this.pingInternal(false);
               this.needsPing = false;
            } catch (Exception var35) {
               this.createNewIO(true);
            }
         }

         ResultSetInternalMethods var46;
         try {
            if (packet != null) {
               ResultSetInternalMethods var43 = this.io.sqlQueryDirect(callingStatement, (String)null, (String)null, packet, maxRows, resultSetType, resultSetConcurrency, streamResults, catalog, cachedMetadata);
               return var43;
            }

            String encoding = null;
            if (this.getUseUnicode()) {
               encoding = this.getEncoding();
            }

            var46 = this.io.sqlQueryDirect(callingStatement, sql, encoding, (Buffer)null, maxRows, resultSetType, resultSetConcurrency, streamResults, catalog, cachedMetadata);
         } catch (SQLException var37) {
            SQLException sqlE = var37;
            if (this.getDumpQueriesOnException()) {
               String extractedSql = this.extractSqlFromPacket(sql, packet, endOfQueryPacketPosition);
               StringBuffer messageBuf = new StringBuffer(extractedSql.length() + 32);
               messageBuf.append("\n\nQuery being executed when exception was thrown:\n\n");
               messageBuf.append(extractedSql);
               sqlE = appendMessageToException(var37, messageBuf.toString());
            }

            if (!this.getHighAvailability() && !this.failedOver) {
               String sqlState = sqlE.getSQLState();
               if (sqlState != null && sqlState.equals("08S01")) {
                  this.cleanup(sqlE);
               }
            } else {
               this.needsPing = true;
            }

            throw sqlE;
         } catch (Exception var38) {
            if (!this.getHighAvailability() && !this.failedOver) {
               if (var38 instanceof IOException) {
                  this.cleanup(var38);
               }
            } else {
               this.needsPing = true;
            }

            SQLException sqlEx = SQLError.createSQLException(Messages.getString("Connection.UnexpectedException"), "S1000");
            sqlEx.initCause(var38);
            throw sqlEx;
         } finally {
            if (this.getMaintainTimeStats()) {
               this.lastQueryFinishedTime = System.currentTimeMillis();
            }

            if (this.failedOver) {
               ++this.queriesIssuedFailedOver;
            }

            if (this.getGatherPerformanceMetrics()) {
               long queryTime = System.currentTimeMillis() - queryStartTime;
               this.registerQueryExecutionTime(queryTime);
            }

         }

         return var46;
      }
   }

   protected String extractSqlFromPacket(String possibleSqlQuery, Buffer queryPacket, int endOfQueryPacketPosition) throws SQLException {
      String extractedSql = null;
      if (possibleSqlQuery != null) {
         if (possibleSqlQuery.length() > this.getMaxQuerySizeToLog()) {
            StringBuffer truncatedQueryBuf = new StringBuffer(possibleSqlQuery.substring(0, this.getMaxQuerySizeToLog()));
            truncatedQueryBuf.append(Messages.getString("MysqlIO.25"));
            extractedSql = truncatedQueryBuf.toString();
         } else {
            extractedSql = possibleSqlQuery;
         }
      }

      if (extractedSql == null) {
         int extractPosition = endOfQueryPacketPosition;
         boolean truncated = false;
         if (endOfQueryPacketPosition > this.getMaxQuerySizeToLog()) {
            extractPosition = this.getMaxQuerySizeToLog();
            truncated = true;
         }

         extractedSql = new String(queryPacket.getByteBuffer(), 5, extractPosition - 5);
         if (truncated) {
            extractedSql = extractedSql + Messages.getString("MysqlIO.25");
         }
      }

      return extractedSql;
   }

   protected void finalize() throws Throwable {
      this.cleanup((Throwable)null);
      super.finalize();
   }

   protected StringBuffer generateConnectionCommentBlock(StringBuffer buf) {
      buf.append("/* conn id ");
      buf.append(this.getId());
      buf.append(" */ ");
      return buf;
   }

   public int getActiveStatementCount() {
      if (this.openStatements != null) {
         synchronized(this.openStatements) {
            return this.openStatements.size();
         }
      } else {
         return 0;
      }
   }

   public boolean getAutoCommit() throws SQLException {
      return this.autoCommit;
   }

   protected Calendar getCalendarInstanceForSessionOrNew() {
      return this.getDynamicCalendars() ? Calendar.getInstance() : this.getSessionLockedCalendar();
   }

   public String getCatalog() throws SQLException {
      return this.database;
   }

   protected String getCharacterSetMetadata() {
      return this.characterSetMetadata;
   }

   SingleByteCharsetConverter getCharsetConverter(String javaEncodingName) throws SQLException {
      if (javaEncodingName == null) {
         return null;
      } else if (this.usePlatformCharsetConverters) {
         return null;
      } else {
         SingleByteCharsetConverter converter = null;
         synchronized(this.charsetConverterMap) {
            Object asObject = this.charsetConverterMap.get(javaEncodingName);
            if (asObject == CHARSET_CONVERTER_NOT_AVAILABLE_MARKER) {
               return null;
            } else {
               converter = (SingleByteCharsetConverter)asObject;
               if (converter == null) {
                  try {
                     converter = SingleByteCharsetConverter.getInstance(javaEncodingName, this);
                     if (converter == null) {
                        this.charsetConverterMap.put(javaEncodingName, CHARSET_CONVERTER_NOT_AVAILABLE_MARKER);
                     } else {
                        this.charsetConverterMap.put(javaEncodingName, converter);
                     }
                  } catch (UnsupportedEncodingException var7) {
                     this.charsetConverterMap.put(javaEncodingName, CHARSET_CONVERTER_NOT_AVAILABLE_MARKER);
                     converter = null;
                  }
               }

               return converter;
            }
         }
      }
   }

   protected String getCharsetNameForIndex(int charsetIndex) throws SQLException {
      String charsetName = null;
      if (this.getUseOldUTF8Behavior()) {
         return this.getEncoding();
      } else {
         if (charsetIndex != -1) {
            try {
               charsetName = this.indexToCharsetMapping[charsetIndex];
               if (("sjis".equalsIgnoreCase(charsetName) || "MS932".equalsIgnoreCase(charsetName)) && CharsetMapping.isAliasForSjis(this.getEncoding())) {
                  charsetName = this.getEncoding();
               }
            } catch (ArrayIndexOutOfBoundsException var4) {
               throw SQLError.createSQLException("Unknown character set index for field '" + charsetIndex + "' received from server.", "S1000");
            }

            if (charsetName == null) {
               charsetName = this.getEncoding();
            }
         } else {
            charsetName = this.getEncoding();
         }

         return charsetName;
      }
   }

   protected TimeZone getDefaultTimeZone() {
      return this.defaultTimeZone;
   }

   protected String getErrorMessageEncoding() {
      return this.errorMessageEncoding;
   }

   public int getHoldability() throws SQLException {
      return 2;
   }

   long getId() {
      return this.connectionId;
   }

   public long getIdleFor() {
      if (this.lastQueryFinishedTime == 0L) {
         return 0L;
      } else {
         long now = System.currentTimeMillis();
         long idleTime = now - this.lastQueryFinishedTime;
         return idleTime;
      }
   }

   protected MysqlIO getIO() throws SQLException {
      if (this.io != null && !this.isClosed) {
         return this.io;
      } else {
         throw SQLError.createSQLException("Operation not allowed on closed connection", "08003");
      }
   }

   public Log getLog() throws SQLException {
      return this.log;
   }

   int getMaxAllowedPacket() {
      return this.maxAllowedPacket;
   }

   protected int getMaxBytesPerChar(String javaCharsetName) throws SQLException {
      String charset = CharsetMapping.getMysqlEncodingForJavaEncoding(javaCharsetName, this);
      if (!this.versionMeetsMinimum(4, 1, 0)) {
         return 1;
      } else {
         Map mapToCheck = null;
         if (!this.getUseDynamicCharsetInfo()) {
            mapToCheck = CharsetMapping.STATIC_CHARSET_TO_NUM_BYTES_MAP;
         } else {
            mapToCheck = this.charsetToNumBytesMap;
            synchronized(this.charsetToNumBytesMap) {
               if (this.charsetToNumBytesMap.isEmpty()) {
                  java.sql.Statement stmt = null;
                  ResultSet rs = null;

                  try {
                     stmt = this.getMetadataSafeStatement();
                     rs = stmt.executeQuery("SHOW CHARACTER SET");

                     while(rs.next()) {
                        this.charsetToNumBytesMap.put(rs.getString("Charset"), Constants.integerValueOf(rs.getInt("Maxlen")));
                     }

                     rs.close();
                     rs = null;
                     stmt.close();
                     stmt = null;
                  } finally {
                     if (rs != null) {
                        rs.close();
                        ResultSet var18 = null;
                     }

                     if (stmt != null) {
                        stmt.close();
                        java.sql.Statement var16 = null;
                     }

                  }
               }
            }
         }

         Integer mbPerChar = (Integer)mapToCheck.get(charset);
         return mbPerChar != null ? mbPerChar : 1;
      }
   }

   public java.sql.DatabaseMetaData getMetaData() throws SQLException {
      return this.getMetaData(true, true);
   }

   private java.sql.DatabaseMetaData getMetaData(boolean checkClosed, boolean checkForInfoSchema) throws SQLException {
      if (checkClosed) {
         this.checkClosed();
      }

      return DatabaseMetaData.getInstance(this, this.database, checkForInfoSchema);
   }

   protected java.sql.Statement getMetadataSafeStatement() throws SQLException {
      java.sql.Statement stmt = this.createStatement();
      if (stmt.getMaxRows() != 0) {
         stmt.setMaxRows(0);
      }

      stmt.setEscapeProcessing(false);
      if (stmt.getFetchSize() != 0) {
         stmt.setFetchSize(0);
      }

      return stmt;
   }

   Object getMutex() throws SQLException {
      if (this.io == null) {
         throw SQLError.createSQLException("Connection.close() has already been called. Invalid operation in this state.", "08003");
      } else {
         this.reportMetricsIfNeeded();
         return this.mutex;
      }
   }

   int getNetBufferLength() {
      return this.netBufferLength;
   }

   public String getServerCharacterEncoding() {
      return this.io.versionMeetsMinimum(4, 1, 0) ? (String)this.serverVariables.get("character_set_server") : (String)this.serverVariables.get("character_set");
   }

   int getServerMajorVersion() {
      return this.io.getServerMajorVersion();
   }

   int getServerMinorVersion() {
      return this.io.getServerMinorVersion();
   }

   int getServerSubMinorVersion() {
      return this.io.getServerSubMinorVersion();
   }

   public TimeZone getServerTimezoneTZ() {
      return this.serverTimezoneTZ;
   }

   String getServerVariable(String variableName) {
      return this.serverVariables != null ? (String)this.serverVariables.get(variableName) : null;
   }

   String getServerVersion() {
      return this.io.getServerVersion();
   }

   protected Calendar getSessionLockedCalendar() {
      return this.sessionCalendar;
   }

   public int getTransactionIsolation() throws SQLException {
      if (this.hasIsolationLevels && !this.getUseLocalSessionState()) {
         java.sql.Statement stmt = null;
         ResultSet rs = null;

         try {
            stmt = this.getMetadataSafeStatement();
            String query = null;
            int offset = 0;
            if (this.versionMeetsMinimum(4, 0, 3)) {
               query = "SELECT @@session.tx_isolation";
               offset = 1;
            } else {
               query = "SHOW VARIABLES LIKE 'transaction_isolation'";
               offset = 2;
            }

            rs = stmt.executeQuery(query);
            if (!rs.next()) {
               throw SQLError.createSQLException("Could not retrieve transaction isolation level from server", "S1000");
            } else {
               String s = rs.getString(offset);
               if (s != null) {
                  Integer intTI = (Integer)mapTransIsolationNameToValue.get(s);
                  if (intTI != null) {
                     int var7 = intTI;
                     return var7;
                  }
               }

               throw SQLError.createSQLException("Could not map transaction isolation '" + s + " to a valid JDBC level.", "S1000");
            }
         } finally {
            if (rs != null) {
               try {
                  rs.close();
               } catch (Exception var17) {
               }

               ResultSet var20 = null;
            }

            if (stmt != null) {
               try {
                  stmt.close();
               } catch (Exception var16) {
               }

               java.sql.Statement var19 = null;
            }

         }
      } else {
         return this.isolationLevel;
      }
   }

   public synchronized Map getTypeMap() throws SQLException {
      if (this.typeMap == null) {
         this.typeMap = new HashMap();
      }

      return this.typeMap;
   }

   String getURL() {
      return this.myURL;
   }

   String getUser() {
      return this.user;
   }

   protected Calendar getUtcCalendar() {
      return this.utcCalendar;
   }

   public SQLWarning getWarnings() throws SQLException {
      return null;
   }

   public boolean hasSameProperties(Connection c) {
      return this.props.equals(((ConnectionImpl)c).props);
   }

   public boolean hasTriedMaster() {
      return this.hasTriedMasterFlag;
   }

   protected void incrementNumberOfPreparedExecutes() {
      if (this.getGatherPerformanceMetrics()) {
         ++this.numberOfPreparedExecutes;
         ++this.numberOfQueriesIssued;
      }

   }

   protected void incrementNumberOfPrepares() {
      if (this.getGatherPerformanceMetrics()) {
         ++this.numberOfPrepares;
      }

   }

   protected void incrementNumberOfResultSetsCreated() {
      if (this.getGatherPerformanceMetrics()) {
         ++this.numberOfResultSetsCreated;
      }

   }

   private void initializeDriverProperties(Properties info) throws SQLException {
      this.initializeProperties(info);
      this.usePlatformCharsetConverters = this.getUseJvmCharsetConverters();
      this.log = LogFactory.getLogger(this.getLogger(), "MySQL");
      if (this.getProfileSql() || this.getUseUsageAdvisor()) {
         this.eventSink = ProfilerEventHandlerFactory.getInstance(this);
      }

      if (this.getCachePreparedStatements()) {
         this.createPreparedStatementCaches();
      }

      if (this.getNoDatetimeStringSync() && this.getUseTimezone()) {
         throw SQLError.createSQLException("Can't enable noDatetimeSync and useTimezone configuration properties at the same time", "01S00");
      } else {
         if (this.getCacheCallableStatements()) {
            this.parsedCallableStatementCache = new LRUCache(this.getCallableStatementCacheSize());
         }

         if (this.getAllowMultiQueries()) {
            this.setCacheResultSetMetadata(false);
         }

         if (this.getCacheResultSetMetadata()) {
            this.resultSetMetadataCache = new LRUCache(this.getMetadataCacheSize());
         }

      }
   }

   private void initializePropsFromServer() throws SQLException {
      String connectionInterceptorClasses = this.getConnectionLifecycleInterceptors();
      this.connectionLifecycleInterceptors = null;
      if (connectionInterceptorClasses != null) {
         this.connectionLifecycleInterceptors = Util.loadExtensions(this, this.props, connectionInterceptorClasses, "Connection.badLifecycleInterceptor");
         Iterator iter = this.connectionLifecycleInterceptors.iterator();
         (new IterateBlock(iter) {
            void forEach(Object each) throws SQLException {
               ((ConnectionLifecycleInterceptor)each).init(ConnectionImpl.this, ConnectionImpl.this.props);
            }
         }).doForAll();
      }

      this.setSessionVariables();
      if (!this.versionMeetsMinimum(4, 1, 0)) {
         this.setTransformedBitIsBoolean(false);
      }

      this.parserKnowsUnicode = this.versionMeetsMinimum(4, 1, 0);
      if (this.getUseServerPreparedStmts() && this.versionMeetsMinimum(4, 1, 0)) {
         this.useServerPreparedStmts = true;
         if (this.versionMeetsMinimum(5, 0, 0) && !this.versionMeetsMinimum(5, 0, 3)) {
            this.useServerPreparedStmts = false;
         }
      }

      this.serverVariables.clear();
      if (this.versionMeetsMinimum(3, 21, 22)) {
         this.loadServerVariables();
         this.buildCollationMapping();
         LicenseConfiguration.checkLicenseType(this.serverVariables);
         String lowerCaseTables = (String)this.serverVariables.get("lower_case_table_names");
         this.lowerCaseTableNames = "on".equalsIgnoreCase(lowerCaseTables) || "1".equalsIgnoreCase(lowerCaseTables) || "2".equalsIgnoreCase(lowerCaseTables);
         this.configureTimezone();
         if (this.serverVariables.containsKey("max_allowed_packet")) {
            this.maxAllowedPacket = this.getServerVariableAsInt("max_allowed_packet", 1048576);
            int preferredBlobSendChunkSize = this.getBlobSendChunkSize();
            int allowedBlobSendChunkSize = Math.min(preferredBlobSendChunkSize, this.maxAllowedPacket) - 8192 - 11;
            this.setBlobSendChunkSize(String.valueOf(allowedBlobSendChunkSize));
         }

         if (this.serverVariables.containsKey("net_buffer_length")) {
            this.netBufferLength = this.getServerVariableAsInt("net_buffer_length", 16384);
         }

         this.checkTransactionIsolationLevel();
         if (!this.versionMeetsMinimum(4, 1, 0)) {
            this.checkServerEncoding();
         }

         this.io.checkForCharsetMismatch();
         if (this.serverVariables.containsKey("sql_mode")) {
            int sqlMode = 0;
            String sqlModeAsString = (String)this.serverVariables.get("sql_mode");

            try {
               sqlMode = Integer.parseInt(sqlModeAsString);
            } catch (NumberFormatException var6) {
               sqlMode = 0;
               if (sqlModeAsString != null) {
                  if (sqlModeAsString.indexOf("ANSI_QUOTES") != -1) {
                     sqlMode |= 4;
                  }

                  if (sqlModeAsString.indexOf("NO_BACKSLASH_ESCAPES") != -1) {
                     this.noBackslashEscapes = true;
                  }
               }
            }

            if ((sqlMode & 4) > 0) {
               this.useAnsiQuotes = true;
            } else {
               this.useAnsiQuotes = false;
            }
         }
      }

      this.errorMessageEncoding = CharsetMapping.getCharacterEncodingForErrorMessages(this);
      boolean overrideDefaultAutocommit = this.isAutoCommitNonDefaultOnServer();
      this.configureClientCharacterSet(false);
      if (this.versionMeetsMinimum(3, 23, 15)) {
         this.transactionsSupported = true;
         if (!overrideDefaultAutocommit) {
            this.setAutoCommit(true);
         }
      } else {
         this.transactionsSupported = false;
      }

      if (this.versionMeetsMinimum(3, 23, 36)) {
         this.hasIsolationLevels = true;
      } else {
         this.hasIsolationLevels = false;
      }

      this.hasQuotedIdentifiers = this.versionMeetsMinimum(3, 23, 6);
      this.io.resetMaxBuf();
      if (this.io.versionMeetsMinimum(4, 1, 0)) {
         String characterSetResultsOnServerMysql = (String)this.serverVariables.get("jdbc.local.character_set_results");
         if (characterSetResultsOnServerMysql != null && !StringUtils.startsWithIgnoreCaseAndWs(characterSetResultsOnServerMysql, "NULL") && characterSetResultsOnServerMysql.length() != 0) {
            this.characterSetResultsOnServer = CharsetMapping.getJavaEncodingForMysqlEncoding(characterSetResultsOnServerMysql, this);
            this.characterSetMetadata = this.characterSetResultsOnServer;
         } else {
            String defaultMetadataCharsetMysql = (String)this.serverVariables.get("character_set_system");
            String defaultMetadataCharset = null;
            if (defaultMetadataCharsetMysql != null) {
               defaultMetadataCharset = CharsetMapping.getJavaEncodingForMysqlEncoding(defaultMetadataCharsetMysql, this);
            } else {
               defaultMetadataCharset = "UTF-8";
            }

            this.characterSetMetadata = defaultMetadataCharset;
         }
      } else {
         this.characterSetMetadata = this.getEncoding();
      }

      if (this.versionMeetsMinimum(4, 1, 0) && !this.versionMeetsMinimum(4, 1, 10) && this.getAllowMultiQueries() && "ON".equalsIgnoreCase((String)this.serverVariables.get("query_cache_type")) && !"0".equalsIgnoreCase((String)this.serverVariables.get("query_cache_size"))) {
         this.setAllowMultiQueries(false);
      }

      this.setupServerForTruncationChecks();
   }

   private int getServerVariableAsInt(String variableName, int fallbackValue) throws SQLException {
      try {
         return Integer.parseInt((String)this.serverVariables.get(variableName));
      } catch (NumberFormatException var4) {
         this.getLog().logWarn(Messages.getString("Connection.BadValueInServerVariables", new Object[]{variableName, this.serverVariables.get(variableName), new Integer(fallbackValue)}));
         return fallbackValue;
      }
   }

   private boolean isAutoCommitNonDefaultOnServer() throws SQLException {
      boolean overrideDefaultAutocommit = false;
      String initConnectValue = (String)this.serverVariables.get("init_connect");
      if (this.versionMeetsMinimum(4, 1, 2) && initConnectValue != null && initConnectValue.length() > 0) {
         if (!this.getElideSetAutoCommits()) {
            ResultSet rs = null;
            java.sql.Statement stmt = null;

            try {
               stmt = this.getMetadataSafeStatement();
               rs = stmt.executeQuery("SELECT @@session.autocommit");
               if (rs.next()) {
                  this.autoCommit = rs.getBoolean(1);
                  if (!this.autoCommit) {
                     overrideDefaultAutocommit = true;
                  }
               }
            } finally {
               if (rs != null) {
                  try {
                     rs.close();
                  } catch (SQLException var14) {
                  }
               }

               if (stmt != null) {
                  try {
                     stmt.close();
                  } catch (SQLException var13) {
                  }
               }

            }
         } else if (this.getIO().isSetNeededForAutoCommitMode(true)) {
            this.autoCommit = false;
            overrideDefaultAutocommit = true;
         }
      }

      return overrideDefaultAutocommit;
   }

   protected boolean isClientTzUTC() {
      return this.isClientTzUTC;
   }

   public boolean isClosed() {
      return this.isClosed;
   }

   protected boolean isCursorFetchEnabled() throws SQLException {
      return this.versionMeetsMinimum(5, 0, 2) && this.getUseCursorFetch();
   }

   public boolean isInGlobalTx() {
      return this.isInGlobalTx;
   }

   public synchronized boolean isMasterConnection() {
      return !this.failedOver;
   }

   public boolean isNoBackslashEscapesSet() {
      return this.noBackslashEscapes;
   }

   boolean isReadInfoMsgEnabled() {
      return this.readInfoMsg;
   }

   public boolean isReadOnly() throws SQLException {
      return this.readOnly;
   }

   protected boolean isRunningOnJDK13() {
      return this.isRunningOnJDK13;
   }

   public synchronized boolean isSameResource(Connection otherConnection) {
      if (otherConnection == null) {
         return false;
      } else {
         boolean directCompare = true;
         String otherHost = ((ConnectionImpl)otherConnection).origHostToConnectTo;
         String otherOrigDatabase = ((ConnectionImpl)otherConnection).origDatabaseToConnectTo;
         String otherCurrentCatalog = ((ConnectionImpl)otherConnection).database;
         if (!nullSafeCompare(otherHost, this.origHostToConnectTo)) {
            directCompare = false;
         } else if (otherHost != null && otherHost.indexOf(44) == -1 && otherHost.indexOf(58) == -1) {
            directCompare = ((ConnectionImpl)otherConnection).origPortToConnectTo == this.origPortToConnectTo;
         }

         if (directCompare) {
            if (!nullSafeCompare(otherOrigDatabase, this.origDatabaseToConnectTo)) {
               directCompare = false;
               directCompare = false;
            } else if (!nullSafeCompare(otherCurrentCatalog, this.database)) {
               directCompare = false;
            }
         }

         if (directCompare) {
            return true;
         } else {
            String otherResourceId = ((ConnectionImpl)otherConnection).getResourceId();
            String myResourceId = this.getResourceId();
            if (otherResourceId != null || myResourceId != null) {
               directCompare = nullSafeCompare(otherResourceId, myResourceId);
               if (directCompare) {
                  return true;
               }
            }

            return false;
         }
      }
   }

   protected boolean isServerTzUTC() {
      return this.isServerTzUTC;
   }

   private void loadServerVariables() throws SQLException {
      if (this.getCacheServerConfiguration()) {
         synchronized(serverConfigByUrl) {
            Map cachedVariableMap = (Map)serverConfigByUrl.get(this.getURL());
            if (cachedVariableMap != null) {
               this.serverVariables = cachedVariableMap;
               this.usingCachedConfig = true;
               return;
            }
         }
      }

      java.sql.Statement stmt = null;
      ResultSet results = null;

      try {
         stmt = this.getMetadataSafeStatement();
         String version = this.dbmd.getDriverVersion();
         if (version != null && version.indexOf(42) != -1) {
            StringBuffer buf = new StringBuffer(version.length() + 10);

            for(int i = 0; i < version.length(); ++i) {
               char c = version.charAt(i);
               if (c == '*') {
                  buf.append("[star]");
               } else {
                  buf.append(c);
               }
            }

            version = buf.toString();
         }

         String versionComment = !this.getParanoid() && version != null ? "/* " + version + " */" : "";
         String query = versionComment + "SHOW VARIABLES";
         if (this.versionMeetsMinimum(5, 0, 3)) {
            query = versionComment + "SHOW VARIABLES WHERE Variable_name ='language'" + " OR Variable_name = 'net_write_timeout'" + " OR Variable_name = 'interactive_timeout'" + " OR Variable_name = 'wait_timeout'" + " OR Variable_name = 'character_set_client'" + " OR Variable_name = 'character_set_connection'" + " OR Variable_name = 'character_set'" + " OR Variable_name = 'character_set_server'" + " OR Variable_name = 'tx_isolation'" + " OR Variable_name = 'transaction_isolation'" + " OR Variable_name = 'character_set_results'" + " OR Variable_name = 'timezone'" + " OR Variable_name = 'time_zone'" + " OR Variable_name = 'system_time_zone'" + " OR Variable_name = 'lower_case_table_names'" + " OR Variable_name = 'max_allowed_packet'" + " OR Variable_name = 'net_buffer_length'" + " OR Variable_name = 'sql_mode'" + " OR Variable_name = 'query_cache_type'" + " OR Variable_name = 'query_cache_size'" + " OR Variable_name = 'init_connect'";
         }

         results = stmt.executeQuery(query);

         while(results.next()) {
            this.serverVariables.put(results.getString(1), results.getString(2));
         }

         if (this.getCacheServerConfiguration()) {
            synchronized(serverConfigByUrl) {
               serverConfigByUrl.put(this.getURL(), this.serverVariables);
            }
         }
      } catch (SQLException e) {
         throw e;
      } finally {
         if (results != null) {
            try {
               results.close();
            } catch (SQLException var20) {
            }
         }

         if (stmt != null) {
            try {
               stmt.close();
            } catch (SQLException var19) {
            }
         }

      }

   }

   public boolean lowerCaseTableNames() {
      return this.lowerCaseTableNames;
   }

   void maxRowsChanged(StatementImpl stmt) {
      synchronized(this.mutex) {
         if (this.statementsUsingMaxRows == null) {
            this.statementsUsingMaxRows = new HashMap();
         }

         this.statementsUsingMaxRows.put(stmt, stmt);
         this.maxRowsChanged = true;
      }
   }

   public String nativeSQL(String sql) throws SQLException {
      if (sql == null) {
         return null;
      } else {
         Object escapedSqlResult = EscapeProcessor.escapeSQL(sql, this.serverSupportsConvertFn(), this);
         return escapedSqlResult instanceof String ? (String)escapedSqlResult : ((EscapeProcessorResult)escapedSqlResult).escapedSql;
      }
   }

   private CallableStatement parseCallableStatement(String sql) throws SQLException {
      Object escapedSqlResult = EscapeProcessor.escapeSQL(sql, this.serverSupportsConvertFn(), this);
      boolean isFunctionCall = false;
      String parsedSql = null;
      if (escapedSqlResult instanceof EscapeProcessorResult) {
         parsedSql = ((EscapeProcessorResult)escapedSqlResult).escapedSql;
         isFunctionCall = ((EscapeProcessorResult)escapedSqlResult).callingStoredFunction;
      } else {
         parsedSql = (String)escapedSqlResult;
         isFunctionCall = false;
      }

      return CallableStatement.getInstance(this, parsedSql, this.database, isFunctionCall);
   }

   public boolean parserKnowsUnicode() {
      return this.parserKnowsUnicode;
   }

   public void ping() throws SQLException {
      this.pingInternal(true);
   }

   protected void pingInternal(boolean checkForClosedConnection) throws SQLException {
      if (checkForClosedConnection) {
         this.checkClosed();
      }

      long pingMillisLifetime = (long)this.getSelfDestructOnPingSecondsLifetime();
      int pingMaxOperations = this.getSelfDestructOnPingMaxOperations();
      if ((pingMillisLifetime <= 0L || System.currentTimeMillis() - this.connectionCreationTimeMillis <= pingMillisLifetime) && (pingMaxOperations <= 0 || pingMaxOperations > this.io.getCommandCount())) {
         this.io.sendCommand(14, (String)null, (Buffer)null, false, (String)null);
      } else {
         this.close();
         throw SQLError.createSQLException(Messages.getString("Connection.exceededConnectionLifetime"), "08S01");
      }
   }

   public java.sql.CallableStatement prepareCall(String sql) throws SQLException {
      return this.prepareCall(sql, 1003, 1007);
   }

   public java.sql.CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
      if (this.versionMeetsMinimum(5, 0, 0)) {
         CallableStatement cStmt = null;
         if (!this.getCacheCallableStatements()) {
            cStmt = this.parseCallableStatement(sql);
         } else {
            synchronized(this.parsedCallableStatementCache) {
               CompoundCacheKey key = new CompoundCacheKey(this.getCatalog(), sql);
               CallableStatement.CallableStatementParamInfo cachedParamInfo = (CallableStatement.CallableStatementParamInfo)this.parsedCallableStatementCache.get(key);
               if (cachedParamInfo != null) {
                  cStmt = CallableStatement.getInstance(this, cachedParamInfo);
               } else {
                  cStmt = this.parseCallableStatement(sql);
                  cachedParamInfo = cStmt.paramInfo;
                  this.parsedCallableStatementCache.put(key, cachedParamInfo);
               }
            }
         }

         cStmt.setResultSetType(resultSetType);
         cStmt.setResultSetConcurrency(resultSetConcurrency);
         return cStmt;
      } else {
         throw SQLError.createSQLException("Callable statements not supported.", "S1C00");
      }
   }

   public java.sql.CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
      if (this.getPedantic() && resultSetHoldability != 1) {
         throw SQLError.createSQLException("HOLD_CUSRORS_OVER_COMMIT is only supported holdability level", "S1009");
      } else {
         CallableStatement cStmt = (CallableStatement)this.prepareCall(sql, resultSetType, resultSetConcurrency);
         return cStmt;
      }
   }

   public java.sql.PreparedStatement prepareStatement(String sql) throws SQLException {
      return this.prepareStatement(sql, 1003, 1007);
   }

   public java.sql.PreparedStatement prepareStatement(String sql, int autoGenKeyIndex) throws SQLException {
      java.sql.PreparedStatement pStmt = this.prepareStatement(sql);
      ((PreparedStatement)pStmt).setRetrieveGeneratedKeys(autoGenKeyIndex == 1);
      return pStmt;
   }

   public java.sql.PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
      this.checkClosed();
      PreparedStatement pStmt = null;
      boolean canServerPrepare = true;
      String nativeSql = this.getProcessEscapeCodesForPrepStmts() ? this.nativeSQL(sql) : sql;
      if (this.useServerPreparedStmts && this.getEmulateUnsupportedPstmts()) {
         canServerPrepare = this.canHandleAsServerPreparedStatement(nativeSql);
      }

      if (this.useServerPreparedStmts && canServerPrepare) {
         if (this.getCachePreparedStatements()) {
            synchronized(this.serverSideStatementCache) {
               pStmt = (ServerPreparedStatement)this.serverSideStatementCache.remove(sql);
               if (pStmt != null) {
                  ((ServerPreparedStatement)pStmt).setClosed(false);
                  pStmt.clearParameters();
               }

               if (pStmt == null) {
                  try {
                     pStmt = ServerPreparedStatement.getInstance(this, nativeSql, this.database, resultSetType, resultSetConcurrency);
                     if (sql.length() < this.getPreparedStatementCacheSqlLimit()) {
                        ((ServerPreparedStatement)pStmt).isCached = true;
                     }

                     pStmt.setResultSetType(resultSetType);
                     pStmt.setResultSetConcurrency(resultSetConcurrency);
                  } catch (SQLException sqlEx) {
                     if (!this.getEmulateUnsupportedPstmts()) {
                        throw sqlEx;
                     }

                     pStmt = (PreparedStatement)this.clientPrepareStatement(nativeSql, resultSetType, resultSetConcurrency, false);
                     if (sql.length() < this.getPreparedStatementCacheSqlLimit()) {
                        this.serverSideStatementCheckCache.put(sql, Boolean.FALSE);
                     }
                  }
               }
            }
         } else {
            try {
               pStmt = ServerPreparedStatement.getInstance(this, nativeSql, this.database, resultSetType, resultSetConcurrency);
               pStmt.setResultSetType(resultSetType);
               pStmt.setResultSetConcurrency(resultSetConcurrency);
            } catch (SQLException sqlEx) {
               if (!this.getEmulateUnsupportedPstmts()) {
                  throw sqlEx;
               }

               pStmt = (PreparedStatement)this.clientPrepareStatement(nativeSql, resultSetType, resultSetConcurrency, false);
            }
         }
      } else {
         pStmt = (PreparedStatement)this.clientPrepareStatement(nativeSql, resultSetType, resultSetConcurrency, false);
      }

      return pStmt;
   }

   public java.sql.PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
      if (this.getPedantic() && resultSetHoldability != 1) {
         throw SQLError.createSQLException("HOLD_CUSRORS_OVER_COMMIT is only supported holdability level", "S1009");
      } else {
         return this.prepareStatement(sql, resultSetType, resultSetConcurrency);
      }
   }

   public java.sql.PreparedStatement prepareStatement(String sql, int[] autoGenKeyIndexes) throws SQLException {
      java.sql.PreparedStatement pStmt = this.prepareStatement(sql);
      ((PreparedStatement)pStmt).setRetrieveGeneratedKeys(autoGenKeyIndexes != null && autoGenKeyIndexes.length > 0);
      return pStmt;
   }

   public java.sql.PreparedStatement prepareStatement(String sql, String[] autoGenKeyColNames) throws SQLException {
      java.sql.PreparedStatement pStmt = this.prepareStatement(sql);
      ((PreparedStatement)pStmt).setRetrieveGeneratedKeys(autoGenKeyColNames != null && autoGenKeyColNames.length > 0);
      return pStmt;
   }

   protected void realClose(boolean calledExplicitly, boolean issueRollback, boolean skipLocalTeardown, Throwable reason) throws SQLException {
      SQLException sqlEx = null;
      if (!this.isClosed()) {
         this.forceClosedReason = reason;

         try {
            if (!skipLocalTeardown) {
               if (!this.getAutoCommit() && issueRollback) {
                  try {
                     this.rollback();
                  } catch (SQLException ex) {
                     sqlEx = ex;
                  }
               }

               this.reportMetrics();
               if (this.getUseUsageAdvisor()) {
                  if (!calledExplicitly) {
                     String message = "Connection implicitly closed by Driver. You should call Connection.close() from your code to free resources more efficiently and avoid resource leaks.";
                     this.eventSink.consumeEvent(new ProfilerEvent((byte)0, "", this.getCatalog(), this.getId(), -1, -1, System.currentTimeMillis(), 0L, Constants.MILLIS_I18N, (String)null, this.pointOfOrigin, message));
                  }

                  long connectionLifeTime = System.currentTimeMillis() - this.connectionCreationTimeMillis;
                  if (connectionLifeTime < 500L) {
                     String message = "Connection lifetime of < .5 seconds. You might be un-necessarily creating short-lived connections and should investigate connection pooling to be more efficient.";
                     this.eventSink.consumeEvent(new ProfilerEvent((byte)0, "", this.getCatalog(), this.getId(), -1, -1, System.currentTimeMillis(), 0L, Constants.MILLIS_I18N, (String)null, this.pointOfOrigin, message));
                  }
               }

               try {
                  this.closeAllOpenStatements();
               } catch (SQLException ex) {
                  sqlEx = ex;
               }

               if (this.io != null) {
                  try {
                     this.io.quit();
                  } catch (Exception var15) {
                  }
               }
            } else {
               this.io.forceClose();
            }
         } finally {
            this.openStatements = null;
            this.io = null;
            ProfilerEventHandlerFactory.removeInstance(this);
            this.isClosed = true;
         }

         if (sqlEx != null) {
            throw sqlEx;
         }
      }
   }

   protected void recachePreparedStatement(ServerPreparedStatement pstmt) throws SQLException {
      if (pstmt.isPoolable()) {
         synchronized(this.serverSideStatementCache) {
            this.serverSideStatementCache.put(pstmt.originalSql, pstmt);
         }
      }

   }

   protected void registerQueryExecutionTime(long queryTimeMs) {
      if (queryTimeMs > this.longestQueryTimeMs) {
         this.longestQueryTimeMs = queryTimeMs;
         this.repartitionPerformanceHistogram();
      }

      this.addToPerformanceHistogram(queryTimeMs, 1);
      if (queryTimeMs < this.shortestQueryTimeMs) {
         this.shortestQueryTimeMs = queryTimeMs == 0L ? 1L : queryTimeMs;
      }

      ++this.numberOfQueriesIssued;
      this.totalQueryTimeMs += (double)queryTimeMs;
   }

   void registerStatement(StatementImpl stmt) {
      synchronized(this.openStatements) {
         this.openStatements.put(stmt, stmt);
      }
   }

   public void releaseSavepoint(Savepoint arg0) throws SQLException {
   }

   private void repartitionHistogram(int[] histCounts, long[] histBreakpoints, long currentLowerBound, long currentUpperBound) {
      if (this.oldHistCounts == null) {
         this.oldHistCounts = new int[histCounts.length];
         this.oldHistBreakpoints = new long[histBreakpoints.length];
      }

      System.arraycopy(histCounts, 0, this.oldHistCounts, 0, histCounts.length);
      System.arraycopy(histBreakpoints, 0, this.oldHistBreakpoints, 0, histBreakpoints.length);
      this.createInitialHistogram(histBreakpoints, currentLowerBound, currentUpperBound);

      for(int i = 0; i < 20; ++i) {
         this.addToHistogram(histCounts, histBreakpoints, this.oldHistBreakpoints[i], this.oldHistCounts[i], currentLowerBound, currentUpperBound);
      }

   }

   private void repartitionPerformanceHistogram() {
      this.checkAndCreatePerformanceHistogram();
      this.repartitionHistogram(this.perfMetricsHistCounts, this.perfMetricsHistBreakpoints, this.shortestQueryTimeMs == Long.MAX_VALUE ? 0L : this.shortestQueryTimeMs, this.longestQueryTimeMs);
   }

   private void repartitionTablesAccessedHistogram() {
      this.checkAndCreateTablesAccessedHistogram();
      this.repartitionHistogram(this.numTablesMetricsHistCounts, this.numTablesMetricsHistBreakpoints, this.minimumNumberTablesAccessed == Long.MAX_VALUE ? 0L : this.minimumNumberTablesAccessed, this.maximumNumberTablesAccessed);
   }

   private void reportMetrics() {
      if (this.getGatherPerformanceMetrics()) {
         StringBuffer logMessage = new StringBuffer(256);
         logMessage.append("** Performance Metrics Report **\n");
         logMessage.append("\nLongest reported query: " + this.longestQueryTimeMs + " ms");
         logMessage.append("\nShortest reported query: " + this.shortestQueryTimeMs + " ms");
         logMessage.append("\nAverage query execution time: " + this.totalQueryTimeMs / (double)this.numberOfQueriesIssued + " ms");
         logMessage.append("\nNumber of statements executed: " + this.numberOfQueriesIssued);
         logMessage.append("\nNumber of result sets created: " + this.numberOfResultSetsCreated);
         logMessage.append("\nNumber of statements prepared: " + this.numberOfPrepares);
         logMessage.append("\nNumber of prepared statement executions: " + this.numberOfPreparedExecutes);
         if (this.perfMetricsHistBreakpoints != null) {
            logMessage.append("\n\n\tTiming Histogram:\n");
            int maxNumPoints = 20;
            int highestCount = Integer.MIN_VALUE;

            for(int i = 0; i < 20; ++i) {
               if (this.perfMetricsHistCounts[i] > highestCount) {
                  highestCount = this.perfMetricsHistCounts[i];
               }
            }

            if (highestCount == 0) {
               highestCount = 1;
            }

            for(int i = 0; i < 19; ++i) {
               if (i == 0) {
                  logMessage.append("\n\tless than " + this.perfMetricsHistBreakpoints[i + 1] + " ms: \t" + this.perfMetricsHistCounts[i]);
               } else {
                  logMessage.append("\n\tbetween " + this.perfMetricsHistBreakpoints[i] + " and " + this.perfMetricsHistBreakpoints[i + 1] + " ms: \t" + this.perfMetricsHistCounts[i]);
               }

               logMessage.append("\t");
               int numPointsToGraph = (int)((double)maxNumPoints * ((double)this.perfMetricsHistCounts[i] / (double)highestCount));

               for(int j = 0; j < numPointsToGraph; ++j) {
                  logMessage.append("*");
               }

               if (this.longestQueryTimeMs < (long)this.perfMetricsHistCounts[i + 1]) {
                  break;
               }
            }

            if (this.perfMetricsHistBreakpoints[18] < this.longestQueryTimeMs) {
               logMessage.append("\n\tbetween ");
               logMessage.append(this.perfMetricsHistBreakpoints[18]);
               logMessage.append(" and ");
               logMessage.append(this.perfMetricsHistBreakpoints[19]);
               logMessage.append(" ms: \t");
               logMessage.append(this.perfMetricsHistCounts[19]);
            }
         }

         if (this.numTablesMetricsHistBreakpoints != null) {
            logMessage.append("\n\n\tTable Join Histogram:\n");
            int maxNumPoints = 20;
            int highestCount = Integer.MIN_VALUE;

            for(int i = 0; i < 20; ++i) {
               if (this.numTablesMetricsHistCounts[i] > highestCount) {
                  highestCount = this.numTablesMetricsHistCounts[i];
               }
            }

            if (highestCount == 0) {
               highestCount = 1;
            }

            for(int i = 0; i < 19; ++i) {
               if (i == 0) {
                  logMessage.append("\n\t" + this.numTablesMetricsHistBreakpoints[i + 1] + " tables or less: \t\t" + this.numTablesMetricsHistCounts[i]);
               } else {
                  logMessage.append("\n\tbetween " + this.numTablesMetricsHistBreakpoints[i] + " and " + this.numTablesMetricsHistBreakpoints[i + 1] + " tables: \t" + this.numTablesMetricsHistCounts[i]);
               }

               logMessage.append("\t");
               int numPointsToGraph = (int)((double)maxNumPoints * ((double)this.numTablesMetricsHistCounts[i] / (double)highestCount));

               for(int j = 0; j < numPointsToGraph; ++j) {
                  logMessage.append("*");
               }

               if (this.maximumNumberTablesAccessed < this.numTablesMetricsHistBreakpoints[i + 1]) {
                  break;
               }
            }

            if (this.numTablesMetricsHistBreakpoints[18] < this.maximumNumberTablesAccessed) {
               logMessage.append("\n\tbetween ");
               logMessage.append(this.numTablesMetricsHistBreakpoints[18]);
               logMessage.append(" and ");
               logMessage.append(this.numTablesMetricsHistBreakpoints[19]);
               logMessage.append(" tables: ");
               logMessage.append(this.numTablesMetricsHistCounts[19]);
            }
         }

         this.log.logInfo(logMessage);
         this.metricsLastReportedMs = System.currentTimeMillis();
      }

   }

   private void reportMetricsIfNeeded() {
      if (this.getGatherPerformanceMetrics() && System.currentTimeMillis() - this.metricsLastReportedMs > (long)this.getReportMetricsIntervalMillis()) {
         this.reportMetrics();
      }

   }

   protected void reportNumberOfTablesAccessed(int numTablesAccessed) {
      if ((long)numTablesAccessed < this.minimumNumberTablesAccessed) {
         this.minimumNumberTablesAccessed = (long)numTablesAccessed;
      }

      if ((long)numTablesAccessed > this.maximumNumberTablesAccessed) {
         this.maximumNumberTablesAccessed = (long)numTablesAccessed;
         this.repartitionTablesAccessedHistogram();
      }

      this.addToTablesAccessedHistogram((long)numTablesAccessed, 1);
   }

   public void resetServerState() throws SQLException {
      if (!this.getParanoid() && this.io != null && this.versionMeetsMinimum(4, 0, 6)) {
         this.changeUser(this.user, this.password);
      }

   }

   public void rollback() throws SQLException {
      synchronized(this.getMutex()) {
         this.checkClosed();

         try {
            if (this.connectionLifecycleInterceptors != null) {
               IterateBlock iter = new IterateBlock(this.connectionLifecycleInterceptors.iterator()) {
                  void forEach(Object each) throws SQLException {
                     if (!((ConnectionLifecycleInterceptor)each).rollback()) {
                        this.stopIterating = true;
                     }

                  }
               };
               iter.doForAll();
               if (!iter.fullIteration()) {
                  return;
               }
            }

            if (this.autoCommit && !this.getRelaxAutoCommit()) {
               throw SQLError.createSQLException("Can't call rollback when autocommit=true", "08003");
            }

            if (!this.transactionsSupported) {
               return;
            }

            try {
               this.rollbackNoChecks();
            } catch (SQLException sqlEx) {
               if (this.getIgnoreNonTxTables() && sqlEx.getErrorCode() != 1196) {
                  throw sqlEx;
               }
            }
         } catch (SQLException sqlException) {
            if ("08S01".equals(sqlException.getSQLState())) {
               throw SQLError.createSQLException("Communications link failure during rollback(). Transaction resolution unknown.", "08007");
            }

            throw sqlException;
         } finally {
            this.needsPing = this.getReconnectAtTxEnd();
         }

      }
   }

   public void rollback(final Savepoint savepoint) throws SQLException {
      if (!this.versionMeetsMinimum(4, 0, 14) && !this.versionMeetsMinimum(4, 1, 1)) {
         throw SQLError.notImplemented();
      } else {
         synchronized(this.getMutex()) {
            this.checkClosed();

            try {
               if (this.connectionLifecycleInterceptors != null) {
                  IterateBlock iter = new IterateBlock(this.connectionLifecycleInterceptors.iterator()) {
                     void forEach(Object each) throws SQLException {
                        if (!((ConnectionLifecycleInterceptor)each).rollback(savepoint)) {
                           this.stopIterating = true;
                        }

                     }
                  };
                  iter.doForAll();
                  if (!iter.fullIteration()) {
                     return;
                  }
               }

               StringBuffer rollbackQuery = new StringBuffer("ROLLBACK TO SAVEPOINT ");
               rollbackQuery.append('`');
               rollbackQuery.append(savepoint.getSavepointName());
               rollbackQuery.append('`');
               java.sql.Statement stmt = null;

               try {
                  stmt = this.getMetadataSafeStatement();
                  stmt.executeUpdate(rollbackQuery.toString());
               } catch (SQLException var20) {
                  int errno = var20.getErrorCode();
                  if (errno == 1181) {
                     String msg = var20.getMessage();
                     if (msg != null) {
                        int indexOfError153 = msg.indexOf("153");
                        if (indexOfError153 != -1) {
                           throw SQLError.createSQLException("Savepoint '" + savepoint.getSavepointName() + "' does not exist", "S1009", errno);
                        }
                     }
                  }

                  if (this.getIgnoreNonTxTables() && var20.getErrorCode() != 1196) {
                     throw var20;
                  } else if ("08S01".equals(var20.getSQLState())) {
                     throw SQLError.createSQLException("Communications link failure during rollback(). Transaction resolution unknown.", "08007");
                  } else {
                     throw var20;
                  }
               } finally {
                  this.closeStatement(stmt);
               }
            } finally {
               this.needsPing = this.getReconnectAtTxEnd();
            }
         }
      }
   }

   private void rollbackNoChecks() throws SQLException {
      if (!this.getUseLocalSessionState() || !this.versionMeetsMinimum(5, 0, 0) || this.io.inTransactionOnServer()) {
         this.execSQL((StatementImpl)null, "rollback", -1, (Buffer)null, 1003, 1007, false, this.database, (Field[])null, false);
      }
   }

   public java.sql.PreparedStatement serverPrepareStatement(String sql) throws SQLException {
      String nativeSql = this.getProcessEscapeCodesForPrepStmts() ? this.nativeSQL(sql) : sql;
      return ServerPreparedStatement.getInstance(this, nativeSql, this.getCatalog(), 1005, 1007);
   }

   public java.sql.PreparedStatement serverPrepareStatement(String sql, int autoGenKeyIndex) throws SQLException {
      String nativeSql = this.getProcessEscapeCodesForPrepStmts() ? this.nativeSQL(sql) : sql;
      PreparedStatement pStmt = ServerPreparedStatement.getInstance(this, nativeSql, this.getCatalog(), 1005, 1007);
      pStmt.setRetrieveGeneratedKeys(autoGenKeyIndex == 1);
      return pStmt;
   }

   public java.sql.PreparedStatement serverPrepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
      String nativeSql = this.getProcessEscapeCodesForPrepStmts() ? this.nativeSQL(sql) : sql;
      return ServerPreparedStatement.getInstance(this, nativeSql, this.getCatalog(), resultSetType, resultSetConcurrency);
   }

   public java.sql.PreparedStatement serverPrepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
      if (this.getPedantic() && resultSetHoldability != 1) {
         throw SQLError.createSQLException("HOLD_CUSRORS_OVER_COMMIT is only supported holdability level", "S1009");
      } else {
         return this.serverPrepareStatement(sql, resultSetType, resultSetConcurrency);
      }
   }

   public java.sql.PreparedStatement serverPrepareStatement(String sql, int[] autoGenKeyIndexes) throws SQLException {
      PreparedStatement pStmt = (PreparedStatement)this.serverPrepareStatement(sql);
      pStmt.setRetrieveGeneratedKeys(autoGenKeyIndexes != null && autoGenKeyIndexes.length > 0);
      return pStmt;
   }

   public java.sql.PreparedStatement serverPrepareStatement(String sql, String[] autoGenKeyColNames) throws SQLException {
      PreparedStatement pStmt = (PreparedStatement)this.serverPrepareStatement(sql);
      pStmt.setRetrieveGeneratedKeys(autoGenKeyColNames != null && autoGenKeyColNames.length > 0);
      return pStmt;
   }

   protected boolean serverSupportsConvertFn() throws SQLException {
      return this.versionMeetsMinimum(4, 0, 2);
   }

   public void setAutoCommit(final boolean autoCommitFlag) throws SQLException {
      synchronized(this.getMutex()) {
         this.checkClosed();
         if (this.connectionLifecycleInterceptors != null) {
            IterateBlock iter = new IterateBlock(this.connectionLifecycleInterceptors.iterator()) {
               void forEach(Object each) throws SQLException {
                  if (!((ConnectionLifecycleInterceptor)each).setAutoCommit(autoCommitFlag)) {
                     this.stopIterating = true;
                  }

               }
            };
            iter.doForAll();
            if (!iter.fullIteration()) {
               return;
            }
         }

         if (this.getAutoReconnectForPools()) {
            this.setHighAvailability(true);
         }

         try {
            if (this.transactionsSupported) {
               boolean needsSetOnServer = true;
               if (this.getUseLocalSessionState() && this.autoCommit == autoCommitFlag) {
                  needsSetOnServer = false;
               } else if (!this.getHighAvailability()) {
                  needsSetOnServer = this.getIO().isSetNeededForAutoCommitMode(autoCommitFlag);
               }

               this.autoCommit = autoCommitFlag;
               if (needsSetOnServer) {
                  this.execSQL((StatementImpl)null, autoCommitFlag ? "SET autocommit=1" : "SET autocommit=0", -1, (Buffer)null, 1003, 1007, false, this.database, (Field[])null, false);
               }
            } else {
               if (!autoCommitFlag && !this.getRelaxAutoCommit()) {
                  throw SQLError.createSQLException("MySQL Versions Older than 3.23.15 do not support transactions", "08003");
               }

               this.autoCommit = autoCommitFlag;
            }
         } finally {
            if (this.getAutoReconnectForPools()) {
               this.setHighAvailability(false);
            }

         }

      }
   }

   public void setCatalog(final String catalog) throws SQLException {
      synchronized(this.getMutex()) {
         this.checkClosed();
         if (catalog == null) {
            throw SQLError.createSQLException("Catalog can not be null", "S1009");
         } else {
            if (this.connectionLifecycleInterceptors != null) {
               IterateBlock iter = new IterateBlock(this.connectionLifecycleInterceptors.iterator()) {
                  void forEach(Object each) throws SQLException {
                     if (!((ConnectionLifecycleInterceptor)each).setCatalog(catalog)) {
                        this.stopIterating = true;
                     }

                  }
               };
               iter.doForAll();
               if (!iter.fullIteration()) {
                  return;
               }
            }

            if (this.getUseLocalSessionState()) {
               if (this.lowerCaseTableNames) {
                  if (this.database.equalsIgnoreCase(catalog)) {
                     return;
                  }
               } else if (this.database.equals(catalog)) {
                  return;
               }
            }

            String quotedId = this.dbmd.getIdentifierQuoteString();
            if (quotedId == null || quotedId.equals(" ")) {
               quotedId = "";
            }

            StringBuffer query = new StringBuffer("USE ");
            query.append(quotedId);
            query.append(catalog);
            query.append(quotedId);
            this.execSQL((StatementImpl)null, query.toString(), -1, (Buffer)null, 1003, 1007, false, this.database, (Field[])null, false);
            this.database = catalog;
         }
      }
   }

   public synchronized void setFailedOver(boolean flag) {
      if (!flag || !this.getRoundRobinLoadBalance()) {
         this.failedOver = flag;
      }
   }

   private void setFailedOverState() throws SQLException {
      if (!this.getRoundRobinLoadBalance()) {
         if (this.getFailOverReadOnly()) {
            this.setReadOnlyInternal(true);
         }

         this.queriesIssuedFailedOver = 0L;
         this.failedOver = true;
         this.masterFailTimeMillis = System.currentTimeMillis();
      }
   }

   public void setHoldability(int arg0) throws SQLException {
   }

   public void setInGlobalTx(boolean flag) {
      this.isInGlobalTx = flag;
   }

   public void setPreferSlaveDuringFailover(boolean flag) {
      this.preferSlaveDuringFailover = flag;
   }

   void setReadInfoMsgEnabled(boolean flag) {
      this.readInfoMsg = flag;
   }

   public void setReadOnly(boolean readOnlyFlag) throws SQLException {
      this.checkClosed();
      if (!this.failedOver || !this.getFailOverReadOnly() || readOnlyFlag) {
         this.setReadOnlyInternal(readOnlyFlag);
      }
   }

   protected void setReadOnlyInternal(boolean readOnlyFlag) throws SQLException {
      this.readOnly = readOnlyFlag;
   }

   public Savepoint setSavepoint() throws SQLException {
      MysqlSavepoint savepoint = new MysqlSavepoint();
      this.setSavepoint(savepoint);
      return savepoint;
   }

   private void setSavepoint(MysqlSavepoint savepoint) throws SQLException {
      if (!this.versionMeetsMinimum(4, 0, 14) && !this.versionMeetsMinimum(4, 1, 1)) {
         throw SQLError.notImplemented();
      } else {
         synchronized(this.getMutex()) {
            this.checkClosed();
            StringBuffer savePointQuery = new StringBuffer("SAVEPOINT ");
            savePointQuery.append('`');
            savePointQuery.append(savepoint.getSavepointName());
            savePointQuery.append('`');
            java.sql.Statement stmt = null;

            try {
               stmt = this.getMetadataSafeStatement();
               stmt.executeUpdate(savePointQuery.toString());
            } finally {
               this.closeStatement(stmt);
            }

         }
      }
   }

   public synchronized Savepoint setSavepoint(String name) throws SQLException {
      MysqlSavepoint savepoint = new MysqlSavepoint(name);
      this.setSavepoint(savepoint);
      return savepoint;
   }

   private void setSessionVariables() throws SQLException {
      if (this.versionMeetsMinimum(4, 0, 0) && this.getSessionVariables() != null) {
         List variablesToSet = StringUtils.split(this.getSessionVariables(), ",", "\"'", "\"'", false);
         int numVariablesToSet = variablesToSet.size();
         java.sql.Statement stmt = null;

         try {
            stmt = this.getMetadataSafeStatement();

            for(int i = 0; i < numVariablesToSet; ++i) {
               String variableValuePair = (String)variablesToSet.get(i);
               if (variableValuePair.startsWith("@")) {
                  stmt.executeUpdate("SET " + variableValuePair);
               } else {
                  stmt.executeUpdate("SET SESSION " + variableValuePair);
               }
            }
         } finally {
            if (stmt != null) {
               stmt.close();
            }

         }
      }

   }

   public synchronized void setTransactionIsolation(int level) throws SQLException {
      this.checkClosed();
      if (this.hasIsolationLevels) {
         String sql = null;
         boolean shouldSendSet = false;
         if (this.getAlwaysSendSetIsolation()) {
            shouldSendSet = true;
         } else if (level != this.isolationLevel) {
            shouldSendSet = true;
         }

         if (this.getUseLocalSessionState()) {
            shouldSendSet = this.isolationLevel != level;
         }

         if (shouldSendSet) {
            switch (level) {
               case 0:
                  throw SQLError.createSQLException("Transaction isolation level NONE not supported by MySQL");
               case 1:
                  sql = "SET SESSION TRANSACTION ISOLATION LEVEL READ UNCOMMITTED";
                  break;
               case 2:
                  sql = "SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED";
                  break;
               case 3:
               case 5:
               case 6:
               case 7:
               default:
                  throw SQLError.createSQLException("Unsupported transaction isolation level '" + level + "'", "S1C00");
               case 4:
                  sql = "SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ";
                  break;
               case 8:
                  sql = "SET SESSION TRANSACTION ISOLATION LEVEL SERIALIZABLE";
            }

            this.execSQL((StatementImpl)null, sql, -1, (Buffer)null, 1003, 1007, false, this.database, (Field[])null, false);
            this.isolationLevel = level;
         }

      } else {
         throw SQLError.createSQLException("Transaction Isolation Levels are not supported on MySQL versions older than 3.23.36.", "S1C00");
      }
   }

   public synchronized void setTypeMap(Map map) throws SQLException {
      this.typeMap = map;
   }

   private void setupServerForTruncationChecks() throws SQLException {
      if (this.getJdbcCompliantTruncation() && this.versionMeetsMinimum(5, 0, 2)) {
         String currentSqlMode = (String)this.serverVariables.get("sql_mode");
         boolean strictTransTablesIsSet = StringUtils.indexOfIgnoreCase(currentSqlMode, "STRICT_TRANS_TABLES") != -1;
         if (currentSqlMode != null && currentSqlMode.length() != 0 && strictTransTablesIsSet) {
            if (strictTransTablesIsSet) {
               this.setJdbcCompliantTruncation(false);
            }
         } else {
            StringBuffer commandBuf = new StringBuffer("SET sql_mode='");
            if (currentSqlMode != null && currentSqlMode.length() > 0) {
               commandBuf.append(currentSqlMode);
               commandBuf.append(",");
            }

            commandBuf.append("STRICT_TRANS_TABLES'");
            this.execSQL((StatementImpl)null, commandBuf.toString(), -1, (Buffer)null, 1003, 1007, false, this.database, (Field[])null, false);
            this.setJdbcCompliantTruncation(false);
         }
      }

   }

   private boolean shouldFallBack() {
      long secondsSinceFailedOver = (System.currentTimeMillis() - this.masterFailTimeMillis) / 1000L;
      boolean tryFallback = secondsSinceFailedOver >= (long)this.getSecondsBeforeRetryMaster() || this.queriesIssuedFailedOver >= (long)this.getQueriesBeforeRetryMaster();
      return tryFallback;
   }

   public void shutdownServer() throws SQLException {
      try {
         this.io.sendCommand(8, (String)null, (Buffer)null, false, (String)null);
      } catch (Exception ex) {
         SQLException sqlEx = SQLError.createSQLException(Messages.getString("Connection.UnhandledExceptionDuringShutdown"), "S1000");
         sqlEx.initCause(ex);
         throw sqlEx;
      }
   }

   public boolean supportsIsolationLevel() {
      return this.hasIsolationLevels;
   }

   public boolean supportsQuotedIdentifiers() {
      return this.hasQuotedIdentifiers;
   }

   public boolean supportsTransactions() {
      return this.transactionsSupported;
   }

   void unregisterStatement(StatementImpl stmt) {
      if (this.openStatements != null) {
         synchronized(this.openStatements) {
            this.openStatements.remove(stmt);
         }
      }

   }

   void unsetMaxRows(StatementImpl stmt) throws SQLException {
      synchronized(this.mutex) {
         if (this.statementsUsingMaxRows != null) {
            Object found = this.statementsUsingMaxRows.remove(stmt);
            if (found != null && this.statementsUsingMaxRows.size() == 0) {
               this.execSQL((StatementImpl)null, "SET OPTION SQL_SELECT_LIMIT=DEFAULT", -1, (Buffer)null, 1003, 1007, false, this.database, (Field[])null, false);
               this.maxRowsChanged = false;
            }
         }

      }
   }

   boolean useAnsiQuotedIdentifiers() {
      return this.useAnsiQuotes;
   }

   boolean useMaxRows() {
      synchronized(this.mutex) {
         return this.maxRowsChanged;
      }
   }

   public boolean versionMeetsMinimum(int major, int minor, int subminor) throws SQLException {
      this.checkClosed();
      return this.io.versionMeetsMinimum(major, minor, subminor);
   }

   protected CachedResultSetMetaData getCachedMetaData(String sql) {
      if (this.resultSetMetadataCache != null) {
         synchronized(this.resultSetMetadataCache) {
            return (CachedResultSetMetaData)this.resultSetMetadataCache.get(sql);
         }
      } else {
         return null;
      }
   }

   protected void initializeResultsMetadataFromCache(String sql, CachedResultSetMetaData cachedMetaData, ResultSetInternalMethods resultSet) throws SQLException {
      if (cachedMetaData == null) {
         cachedMetaData = new CachedResultSetMetaData();
         resultSet.buildIndexMapping();
         resultSet.initializeWithMetadata();
         if (resultSet instanceof UpdatableResultSet) {
            ((UpdatableResultSet)resultSet).checkUpdatability();
         }

         resultSet.populateCachedMetaData(cachedMetaData);
         this.resultSetMetadataCache.put(sql, cachedMetaData);
      } else {
         resultSet.initializeFromCachedMetaData(cachedMetaData);
         resultSet.initializeWithMetadata();
         if (resultSet instanceof UpdatableResultSet) {
            ((UpdatableResultSet)resultSet).checkUpdatability();
         }
      }

   }

   public String getStatementComment() {
      return this.statementComment;
   }

   public void setStatementComment(String comment) {
      this.statementComment = comment;
   }

   public synchronized void reportQueryTime(long millisOrNanos) {
      ++this.queryTimeCount;
      this.queryTimeSum += (double)millisOrNanos;
      this.queryTimeSumSquares += (double)(millisOrNanos * millisOrNanos);
      this.queryTimeMean = (this.queryTimeMean * (double)(this.queryTimeCount - 1L) + (double)millisOrNanos) / (double)this.queryTimeCount;
   }

   public synchronized boolean isAbonormallyLongQuery(long millisOrNanos) {
      if (this.queryTimeCount < 15L) {
         return false;
      } else {
         double stddev = Math.sqrt((this.queryTimeSumSquares - this.queryTimeSum * this.queryTimeSum / (double)this.queryTimeCount) / (double)(this.queryTimeCount - 1L));
         return (double)millisOrNanos > this.queryTimeMean + (double)5.0F * stddev;
      }
   }

   public void initializeExtension(Extension ex) throws SQLException {
      ex.init(this, this.props);
   }

   protected void transactionBegun() throws SQLException {
      if (this.connectionLifecycleInterceptors != null) {
         IterateBlock iter = new IterateBlock(this.connectionLifecycleInterceptors.iterator()) {
            void forEach(Object each) throws SQLException {
               ((ConnectionLifecycleInterceptor)each).transactionBegun();
            }
         };
         iter.doForAll();
      }

   }

   protected void transactionCompleted() throws SQLException {
      if (this.connectionLifecycleInterceptors != null) {
         IterateBlock iter = new IterateBlock(this.connectionLifecycleInterceptors.iterator()) {
            void forEach(Object each) throws SQLException {
               ((ConnectionLifecycleInterceptor)each).transactionCompleted();
            }
         };
         iter.doForAll();
      }

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
      mapTransIsolationNameToValue = new HashMap(8);
      mapTransIsolationNameToValue.put("READ-UNCOMMITED", Constants.integerValueOf(1));
      mapTransIsolationNameToValue.put("READ-UNCOMMITTED", Constants.integerValueOf(1));
      mapTransIsolationNameToValue.put("READ-COMMITTED", Constants.integerValueOf(2));
      mapTransIsolationNameToValue.put("REPEATABLE-READ", Constants.integerValueOf(4));
      mapTransIsolationNameToValue.put("SERIALIZABLE", Constants.integerValueOf(8));
      boolean createdNamedTimer = false;

      try {
         Constructor ctr = (class$java$util$Timer == null ? (class$java$util$Timer = class$("java.util.Timer")) : class$java$util$Timer).getConstructor(class$java$lang$String == null ? (class$java$lang$String = class$("java.lang.String")) : class$java$lang$String, Boolean.TYPE);
         cancelTimer = (Timer)ctr.newInstance("MySQL Statement Cancellation Timer", Boolean.TRUE);
         createdNamedTimer = true;
      } catch (Throwable var5) {
         createdNamedTimer = false;
      }

      if (!createdNamedTimer) {
         cancelTimer = new Timer(true);
      }

      if (Util.isJdbc4()) {
         try {
            JDBC_4_CONNECTION_CTOR = Class.forName("com.mysql.jdbc.JDBC4Connection").getConstructor(class$java$lang$String == null ? (class$java$lang$String = class$("java.lang.String")) : class$java$lang$String, Integer.TYPE, class$java$util$Properties == null ? (class$java$util$Properties = class$("java.util.Properties")) : class$java$util$Properties, class$java$lang$String == null ? (class$java$lang$String = class$("java.lang.String")) : class$java$lang$String, class$java$lang$String == null ? (class$java$lang$String = class$("java.lang.String")) : class$java$lang$String);
         } catch (SecurityException e) {
            throw new RuntimeException(e);
         } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
         } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
         }
      } else {
         JDBC_4_CONNECTION_CTOR = null;
      }

   }

   class CompoundCacheKey {
      String componentOne;
      String componentTwo;
      int hashCode;

      CompoundCacheKey(String partOne, String partTwo) {
         this.componentOne = partOne;
         this.componentTwo = partTwo;
         this.hashCode = ((this.componentOne != null ? this.componentOne : "") + this.componentTwo).hashCode();
      }

      public boolean equals(Object obj) {
         if (obj instanceof CompoundCacheKey) {
            CompoundCacheKey another = (CompoundCacheKey)obj;
            boolean firstPartEqual = false;
            if (this.componentOne == null) {
               firstPartEqual = another.componentOne == null;
            } else {
               firstPartEqual = this.componentOne.equals(another.componentOne);
            }

            return firstPartEqual && this.componentTwo.equals(another.componentTwo);
         } else {
            return false;
         }
      }

      public int hashCode() {
         return this.hashCode;
      }
   }
}
