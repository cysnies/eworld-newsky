package com.mysql.jdbc;

import com.mysql.jdbc.profiler.ProfilerEvent;
import com.mysql.jdbc.profiler.ProfilerEventHandler;
import com.mysql.jdbc.profiler.ProfilerEventHandlerFactory;
import com.mysql.jdbc.util.ReadAheadInputStream;
import com.mysql.jdbc.util.ResultSetUtil;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.ref.SoftReference;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.zip.Deflater;

class MysqlIO {
   private static final int UTF8_CHARSET_INDEX = 33;
   private static final String CODE_PAGE_1252 = "Cp1252";
   protected static final int NULL_LENGTH = -1;
   protected static final int COMP_HEADER_LENGTH = 3;
   protected static final int MIN_COMPRESS_LEN = 50;
   protected static final int HEADER_LENGTH = 4;
   protected static final int AUTH_411_OVERHEAD = 33;
   private static int maxBufferSize = 65535;
   private static final int CLIENT_COMPRESS = 32;
   protected static final int CLIENT_CONNECT_WITH_DB = 8;
   private static final int CLIENT_FOUND_ROWS = 2;
   private static final int CLIENT_LOCAL_FILES = 128;
   private static final int CLIENT_LONG_FLAG = 4;
   private static final int CLIENT_LONG_PASSWORD = 1;
   private static final int CLIENT_PROTOCOL_41 = 512;
   private static final int CLIENT_INTERACTIVE = 1024;
   protected static final int CLIENT_SSL = 2048;
   private static final int CLIENT_TRANSACTIONS = 8192;
   protected static final int CLIENT_RESERVED = 16384;
   protected static final int CLIENT_SECURE_CONNECTION = 32768;
   private static final int CLIENT_MULTI_QUERIES = 65536;
   private static final int CLIENT_MULTI_RESULTS = 131072;
   private static final int SERVER_STATUS_IN_TRANS = 1;
   private static final int SERVER_STATUS_AUTOCOMMIT = 2;
   static final int SERVER_MORE_RESULTS_EXISTS = 8;
   private static final int SERVER_QUERY_NO_GOOD_INDEX_USED = 16;
   private static final int SERVER_QUERY_NO_INDEX_USED = 32;
   private static final int SERVER_STATUS_CURSOR_EXISTS = 64;
   private static final String FALSE_SCRAMBLE = "xxxxxxxx";
   protected static final int MAX_QUERY_SIZE_TO_LOG = 1024;
   protected static final int MAX_QUERY_SIZE_TO_EXPLAIN = 1048576;
   protected static final int INITIAL_PACKET_SIZE = 1024;
   private static String jvmPlatformCharset = null;
   protected static final String ZERO_DATE_VALUE_MARKER = "0000-00-00";
   protected static final String ZERO_DATETIME_VALUE_MARKER = "0000-00-00 00:00:00";
   private static final int MAX_PACKET_DUMP_LENGTH = 1024;
   private boolean packetSequenceReset = false;
   protected int serverCharsetIndex;
   private Buffer reusablePacket = null;
   private Buffer sendPacket = null;
   private Buffer sharedSendPacket = null;
   protected BufferedOutputStream mysqlOutput = null;
   protected ConnectionImpl connection;
   private Deflater deflater = null;
   protected InputStream mysqlInput = null;
   private LinkedList packetDebugRingBuffer = null;
   private RowData streamingData = null;
   protected Socket mysqlConnection = null;
   private SocketFactory socketFactory = null;
   private SoftReference loadFileBufRef;
   private SoftReference splitBufRef;
   protected String host = null;
   protected String seed;
   private String serverVersion = null;
   private String socketFactoryClassName = null;
   private byte[] packetHeaderBuf = new byte[4];
   private boolean colDecimalNeedsBump = false;
   private boolean hadWarnings = false;
   private boolean has41NewNewProt = false;
   private boolean hasLongColumnInfo = false;
   private boolean isInteractiveClient = false;
   private boolean logSlowQueries = false;
   private boolean platformDbCharsetMatches = true;
   private boolean profileSql = false;
   private boolean queryBadIndexUsed = false;
   private boolean queryNoIndexUsed = false;
   private boolean use41Extensions = false;
   private boolean useCompression = false;
   private boolean useNewLargePackets = false;
   private boolean useNewUpdateCounts = false;
   private byte packetSequence = 0;
   private byte readPacketSequence = -1;
   private boolean checkPacketSequence = false;
   private byte protocolVersion = 0;
   private int maxAllowedPacket = 1048576;
   protected int maxThreeBytes = 16581375;
   protected int port = 3306;
   protected int serverCapabilities;
   private int serverMajorVersion = 0;
   private int serverMinorVersion = 0;
   private int oldServerStatus = 0;
   private int serverStatus = 0;
   private int serverSubMinorVersion = 0;
   private int warningCount = 0;
   protected long clientParam = 0L;
   protected long lastPacketSentTimeMs = 0L;
   protected long lastPacketReceivedTimeMs = 0L;
   private boolean traceProtocol = false;
   private boolean enablePacketDebug = false;
   private Calendar sessionCalendar;
   private boolean useConnectWithDb;
   private boolean needToGrabQueryFromPacket;
   private boolean autoGenerateTestcaseScript;
   private long threadId;
   private boolean useNanosForElapsedTime;
   private long slowQueryThreshold;
   private String queryTimingUnits;
   private List statementInterceptors;
   private boolean useDirectRowUnpack = true;
   private int useBufferRowSizeThreshold;
   private int commandCount = 0;
   private int statementExecutionDepth = 0;
   private boolean useAutoSlowLog;

   public MysqlIO(String host, int port, Properties props, String socketFactoryClassName, ConnectionImpl conn, int socketTimeout, int useBufferRowSizeThreshold) throws IOException, SQLException {
      this.connection = conn;
      if (this.connection.getEnablePacketDebug()) {
         this.packetDebugRingBuffer = new LinkedList();
      }

      this.useAutoSlowLog = this.connection.getAutoSlowLog();
      this.useBufferRowSizeThreshold = useBufferRowSizeThreshold;
      this.useDirectRowUnpack = this.connection.getUseDirectRowUnpack();
      this.logSlowQueries = this.connection.getLogSlowQueries();
      this.reusablePacket = new Buffer(1024);
      this.sendPacket = new Buffer(1024);
      this.port = port;
      this.host = host;
      this.socketFactoryClassName = socketFactoryClassName;
      this.socketFactory = this.createSocketFactory();
      this.mysqlConnection = this.socketFactory.connect(this.host, this.port, props);
      if (socketTimeout != 0) {
         try {
            this.mysqlConnection.setSoTimeout(socketTimeout);
         } catch (Exception var9) {
         }
      }

      this.mysqlConnection = this.socketFactory.beforeHandshake();
      if (this.connection.getUseReadAheadInput()) {
         this.mysqlInput = new ReadAheadInputStream(this.mysqlConnection.getInputStream(), 16384, this.connection.getTraceProtocol(), this.connection.getLog());
      } else if (this.connection.useUnbufferedInput()) {
         this.mysqlInput = this.mysqlConnection.getInputStream();
      } else {
         this.mysqlInput = new BufferedInputStream(this.mysqlConnection.getInputStream(), 16384);
      }

      this.mysqlOutput = new BufferedOutputStream(this.mysqlConnection.getOutputStream(), 16384);
      this.isInteractiveClient = this.connection.getInteractiveClient();
      this.profileSql = this.connection.getProfileSql();
      this.sessionCalendar = Calendar.getInstance();
      this.autoGenerateTestcaseScript = this.connection.getAutoGenerateTestcaseScript();
      this.needToGrabQueryFromPacket = this.profileSql || this.logSlowQueries || this.autoGenerateTestcaseScript;
      if (this.connection.getUseNanosForElapsedTime() && Util.nanoTimeAvailable()) {
         this.useNanosForElapsedTime = true;
         this.queryTimingUnits = Messages.getString("Nanoseconds");
      } else {
         this.queryTimingUnits = Messages.getString("Milliseconds");
      }

      if (this.connection.getLogSlowQueries()) {
         this.calculateSlowQueryThreshold();
      }

   }

   protected void initializeStatementInterceptors(String interceptorClasses, Properties props) throws SQLException {
      this.statementInterceptors = Util.loadExtensions(this.connection, props, interceptorClasses, "MysqlIo.BadStatementInterceptor");
   }

   public boolean hasLongColumnInfo() {
      return this.hasLongColumnInfo;
   }

   protected boolean isDataAvailable() throws SQLException {
      try {
         return this.mysqlInput.available() > 0;
      } catch (IOException ioEx) {
         throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ioEx);
      }
   }

   protected long getLastPacketSentTimeMs() {
      return this.lastPacketSentTimeMs;
   }

   protected long getLastPacketReceivedTimeMs() {
      return this.lastPacketReceivedTimeMs;
   }

   protected ResultSetImpl getResultSet(StatementImpl callingStatement, long columnCount, int maxRows, int resultSetType, int resultSetConcurrency, boolean streamResults, String catalog, boolean isBinaryEncoded, Field[] metadataFromCache) throws SQLException {
      Field[] fields = null;
      if (metadataFromCache == null) {
         fields = new Field[(int)columnCount];

         for(int i = 0; (long)i < columnCount; ++i) {
            Buffer fieldPacket = null;
            fieldPacket = this.readPacket();
            fields[i] = this.unpackField(fieldPacket, false);
         }
      } else {
         for(int i = 0; (long)i < columnCount; ++i) {
            this.skipPacket();
         }
      }

      Buffer packet = this.reuseAndReadPacket(this.reusablePacket);
      this.readServerStatusForResultSets(packet);
      if (this.connection.versionMeetsMinimum(5, 0, 2) && this.connection.getUseCursorFetch() && isBinaryEncoded && callingStatement != null && callingStatement.getFetchSize() != 0 && callingStatement.getResultSetType() == 1003) {
         ServerPreparedStatement prepStmt = (ServerPreparedStatement)callingStatement;
         boolean usingCursor = true;
         if (this.connection.versionMeetsMinimum(5, 0, 5)) {
            usingCursor = (this.serverStatus & 64) != 0;
         }

         if (usingCursor) {
            RowData rows = new RowDataCursor(this, prepStmt, fields);
            ResultSetImpl rs = this.buildResultSetWithRows(callingStatement, catalog, fields, rows, resultSetType, resultSetConcurrency, isBinaryEncoded);
            if (usingCursor) {
               rs.setFetchSize(callingStatement.getFetchSize());
            }

            return rs;
         }
      }

      RowData rowData = null;
      if (!streamResults) {
         rowData = this.readSingleRowSet(columnCount, maxRows, resultSetConcurrency, isBinaryEncoded, metadataFromCache == null ? fields : metadataFromCache);
      } else {
         rowData = new RowDataDynamic(this, (int)columnCount, metadataFromCache == null ? fields : metadataFromCache, isBinaryEncoded);
         this.streamingData = rowData;
      }

      ResultSetImpl rs = this.buildResultSetWithRows(callingStatement, catalog, metadataFromCache == null ? fields : metadataFromCache, rowData, resultSetType, resultSetConcurrency, isBinaryEncoded);
      return rs;
   }

   protected final void forceClose() {
      try {
         if (this.mysqlInput != null) {
            this.mysqlInput.close();
         }
      } catch (IOException var4) {
         this.mysqlInput = null;
      }

      try {
         if (this.mysqlOutput != null) {
            this.mysqlOutput.close();
         }
      } catch (IOException var3) {
         this.mysqlOutput = null;
      }

      try {
         if (this.mysqlConnection != null) {
            this.mysqlConnection.close();
         }
      } catch (IOException var2) {
         this.mysqlConnection = null;
      }

   }

   protected final void skipPacket() throws SQLException {
      try {
         int lengthRead = this.readFully(this.mysqlInput, this.packetHeaderBuf, 0, 4);
         if (lengthRead < 4) {
            this.forceClose();
            throw new IOException(Messages.getString("MysqlIO.1"));
         } else {
            int packetLength = (this.packetHeaderBuf[0] & 255) + ((this.packetHeaderBuf[1] & 255) << 8) + ((this.packetHeaderBuf[2] & 255) << 16);
            if (this.traceProtocol) {
               StringBuffer traceMessageBuf = new StringBuffer();
               traceMessageBuf.append(Messages.getString("MysqlIO.2"));
               traceMessageBuf.append(packetLength);
               traceMessageBuf.append(Messages.getString("MysqlIO.3"));
               traceMessageBuf.append(StringUtils.dumpAsHex(this.packetHeaderBuf, 4));
               this.connection.getLog().logTrace(traceMessageBuf.toString());
            }

            byte multiPacketSeq = this.packetHeaderBuf[3];
            if (!this.packetSequenceReset) {
               if (this.enablePacketDebug && this.checkPacketSequence) {
                  this.checkPacketSequencing(multiPacketSeq);
               }
            } else {
               this.packetSequenceReset = false;
            }

            this.readPacketSequence = multiPacketSeq;
            this.skipFully(this.mysqlInput, (long)packetLength);
         }
      } catch (IOException ioEx) {
         throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ioEx);
      } catch (OutOfMemoryError var10) {
         OutOfMemoryError oom = var10;

         try {
            this.connection.realClose(false, false, true, oom);
         } finally {
            ;
         }

         throw var10;
      }
   }

   protected final Buffer readPacket() throws SQLException {
      try {
         int lengthRead = this.readFully(this.mysqlInput, this.packetHeaderBuf, 0, 4);
         if (lengthRead < 4) {
            this.forceClose();
            throw new IOException(Messages.getString("MysqlIO.1"));
         } else {
            int packetLength = (this.packetHeaderBuf[0] & 255) + ((this.packetHeaderBuf[1] & 255) << 8) + ((this.packetHeaderBuf[2] & 255) << 16);
            if (packetLength > this.maxAllowedPacket) {
               throw new PacketTooBigException((long)packetLength, (long)this.maxAllowedPacket);
            } else {
               if (this.traceProtocol) {
                  StringBuffer traceMessageBuf = new StringBuffer();
                  traceMessageBuf.append(Messages.getString("MysqlIO.2"));
                  traceMessageBuf.append(packetLength);
                  traceMessageBuf.append(Messages.getString("MysqlIO.3"));
                  traceMessageBuf.append(StringUtils.dumpAsHex(this.packetHeaderBuf, 4));
                  this.connection.getLog().logTrace(traceMessageBuf.toString());
               }

               byte multiPacketSeq = this.packetHeaderBuf[3];
               if (!this.packetSequenceReset) {
                  if (this.enablePacketDebug && this.checkPacketSequence) {
                     this.checkPacketSequencing(multiPacketSeq);
                  }
               } else {
                  this.packetSequenceReset = false;
               }

               this.readPacketSequence = multiPacketSeq;
               byte[] buffer = new byte[packetLength + 1];
               int numBytesRead = this.readFully(this.mysqlInput, buffer, 0, packetLength);
               if (numBytesRead != packetLength) {
                  throw new IOException("Short read, expected " + packetLength + " bytes, only read " + numBytesRead);
               } else {
                  buffer[packetLength] = 0;
                  Buffer packet = new Buffer(buffer);
                  packet.setBufLength(packetLength + 1);
                  if (this.traceProtocol) {
                     StringBuffer traceMessageBuf = new StringBuffer();
                     traceMessageBuf.append(Messages.getString("MysqlIO.4"));
                     traceMessageBuf.append(getPacketDumpToLog(packet, packetLength));
                     this.connection.getLog().logTrace(traceMessageBuf.toString());
                  }

                  if (this.enablePacketDebug) {
                     this.enqueuePacketForDebugging(false, false, 0, this.packetHeaderBuf, packet);
                  }

                  if (this.connection.getMaintainTimeStats()) {
                     this.lastPacketReceivedTimeMs = System.currentTimeMillis();
                  }

                  return packet;
               }
            }
         }
      } catch (IOException ioEx) {
         throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ioEx);
      } catch (OutOfMemoryError var14) {
         OutOfMemoryError oom = var14;

         try {
            this.connection.realClose(false, false, true, oom);
         } finally {
            ;
         }

         throw var14;
      }
   }

   protected final Field unpackField(Buffer packet, boolean extractDefaultValues) throws SQLException {
      if (this.use41Extensions) {
         if (this.has41NewNewProt) {
            int catalogNameStart = packet.getPosition() + 1;
            int catalogNameLength = packet.fastSkipLenString();
            this.adjustStartForFieldLength(catalogNameStart, catalogNameLength);
         }

         int databaseNameStart = packet.getPosition() + 1;
         int databaseNameLength = packet.fastSkipLenString();
         databaseNameStart = this.adjustStartForFieldLength(databaseNameStart, databaseNameLength);
         int tableNameStart = packet.getPosition() + 1;
         int tableNameLength = packet.fastSkipLenString();
         tableNameStart = this.adjustStartForFieldLength(tableNameStart, tableNameLength);
         int originalTableNameStart = packet.getPosition() + 1;
         int originalTableNameLength = packet.fastSkipLenString();
         originalTableNameStart = this.adjustStartForFieldLength(originalTableNameStart, originalTableNameLength);
         int nameStart = packet.getPosition() + 1;
         int nameLength = packet.fastSkipLenString();
         nameStart = this.adjustStartForFieldLength(nameStart, nameLength);
         int originalColumnNameStart = packet.getPosition() + 1;
         int originalColumnNameLength = packet.fastSkipLenString();
         originalColumnNameStart = this.adjustStartForFieldLength(originalColumnNameStart, originalColumnNameLength);
         packet.readByte();
         short charSetNumber = (short)packet.readInt();
         long colLength = 0L;
         if (this.has41NewNewProt) {
            colLength = packet.readLong();
         } else {
            colLength = (long)packet.readLongInt();
         }

         int colType = packet.readByte() & 255;
         short colFlag = 0;
         if (this.hasLongColumnInfo) {
            colFlag = (short)packet.readInt();
         } else {
            colFlag = (short)(packet.readByte() & 255);
         }

         int colDecimals = packet.readByte() & 255;
         int defaultValueStart = -1;
         int defaultValueLength = -1;
         if (extractDefaultValues) {
            defaultValueStart = packet.getPosition() + 1;
            defaultValueLength = packet.fastSkipLenString();
         }

         Field field = new Field(this.connection, packet.getByteBuffer(), databaseNameStart, databaseNameLength, tableNameStart, tableNameLength, originalTableNameStart, originalTableNameLength, nameStart, nameLength, originalColumnNameStart, originalColumnNameLength, colLength, colType, colFlag, colDecimals, defaultValueStart, defaultValueLength, charSetNumber);
         return field;
      } else {
         int tableNameStart = packet.getPosition() + 1;
         int tableNameLength = packet.fastSkipLenString();
         tableNameStart = this.adjustStartForFieldLength(tableNameStart, tableNameLength);
         int nameStart = packet.getPosition() + 1;
         int nameLength = packet.fastSkipLenString();
         nameStart = this.adjustStartForFieldLength(nameStart, nameLength);
         int colLength = packet.readnBytes();
         int colType = packet.readnBytes();
         packet.readByte();
         short colFlag = 0;
         if (this.hasLongColumnInfo) {
            colFlag = (short)packet.readInt();
         } else {
            colFlag = (short)(packet.readByte() & 255);
         }

         int colDecimals = packet.readByte() & 255;
         if (this.colDecimalNeedsBump) {
            ++colDecimals;
         }

         Field field = new Field(this.connection, packet.getByteBuffer(), nameStart, nameLength, tableNameStart, tableNameLength, colLength, colType, colFlag, colDecimals);
         return field;
      }
   }

   private int adjustStartForFieldLength(int nameStart, int nameLength) {
      if (nameLength < 251) {
         return nameStart;
      } else if (nameLength >= 251 && nameLength < 65536) {
         return nameStart + 2;
      } else {
         return nameLength >= 65536 && nameLength < 16777216 ? nameStart + 3 : nameStart + 8;
      }
   }

   protected boolean isSetNeededForAutoCommitMode(boolean autoCommitFlag) {
      if (this.use41Extensions && this.connection.getElideSetAutoCommits()) {
         boolean autoCommitModeOnServer = (this.serverStatus & 2) != 0;
         if (!autoCommitFlag && this.versionMeetsMinimum(5, 0, 0)) {
            boolean inTransactionOnServer = (this.serverStatus & 1) != 0;
            return !inTransactionOnServer;
         } else {
            return autoCommitModeOnServer != autoCommitFlag;
         }
      } else {
         return true;
      }
   }

   protected boolean inTransactionOnServer() {
      return (this.serverStatus & 1) != 0;
   }

   protected void changeUser(String userName, String password, String database) throws SQLException {
      this.packetSequence = -1;
      int passwordLength = 16;
      int userLength = userName != null ? userName.length() : 0;
      int databaseLength = database != null ? database.length() : 0;
      int packLength = (userLength + passwordLength + databaseLength) * 2 + 7 + 4 + 33;
      if ((this.serverCapabilities & '耀') != 0) {
         Buffer changeUserPacket = new Buffer(packLength + 1);
         changeUserPacket.writeByte((byte)17);
         if (this.versionMeetsMinimum(4, 1, 1)) {
            this.secureAuth411(changeUserPacket, packLength, userName, password, database, false);
         } else {
            this.secureAuth(changeUserPacket, packLength, userName, password, database, false);
         }
      } else {
         Buffer packet = new Buffer(packLength);
         packet.writeByte((byte)17);
         packet.writeString(userName);
         if (this.protocolVersion > 9) {
            packet.writeString(Util.newCrypt(password, this.seed));
         } else {
            packet.writeString(Util.oldCrypt(password, this.seed));
         }

         boolean localUseConnectWithDb = this.useConnectWithDb && database != null && database.length() > 0;
         if (localUseConnectWithDb) {
            packet.writeString(database);
         }

         this.send(packet, packet.getPosition());
         this.checkErrorPacket();
         if (!localUseConnectWithDb) {
            this.changeDatabaseTo(database);
         }
      }

   }

   protected Buffer checkErrorPacket() throws SQLException {
      return this.checkErrorPacket(-1);
   }

   protected void checkForCharsetMismatch() {
      if (this.connection.getUseUnicode() && this.connection.getEncoding() != null) {
         String encodingToCheck = jvmPlatformCharset;
         if (encodingToCheck == null) {
            encodingToCheck = System.getProperty("file.encoding");
         }

         if (encodingToCheck == null) {
            this.platformDbCharsetMatches = false;
         } else {
            this.platformDbCharsetMatches = encodingToCheck.equals(this.connection.getEncoding());
         }
      }

   }

   protected void clearInputStream() throws SQLException {
      try {
         for(int len = this.mysqlInput.available(); len > 0; len = this.mysqlInput.available()) {
            this.mysqlInput.skip((long)len);
         }

      } catch (IOException ioEx) {
         throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ioEx);
      }
   }

   protected void resetReadPacketSequence() {
      this.readPacketSequence = 0;
   }

   protected void dumpPacketRingBuffer() throws SQLException {
      if (this.packetDebugRingBuffer != null && this.connection.getEnablePacketDebug()) {
         StringBuffer dumpBuffer = new StringBuffer();
         dumpBuffer.append("Last " + this.packetDebugRingBuffer.size() + " packets received from server, from oldest->newest:\n");
         dumpBuffer.append("\n");
         Iterator ringBufIter = this.packetDebugRingBuffer.iterator();

         while(ringBufIter.hasNext()) {
            dumpBuffer.append((StringBuffer)ringBufIter.next());
            dumpBuffer.append("\n");
         }

         this.connection.getLog().logTrace(dumpBuffer.toString());
      }

   }

   protected void explainSlowQuery(byte[] querySQL, String truncatedQuery) throws SQLException {
      if (StringUtils.startsWithIgnoreCaseAndWs(truncatedQuery, "SELECT")) {
         PreparedStatement stmt = null;
         ResultSet rs = null;

         try {
            stmt = (PreparedStatement)this.connection.clientPrepareStatement("EXPLAIN ?");
            stmt.setBytesNoEscapeNoQuotes(1, querySQL);
            rs = stmt.executeQuery();
            StringBuffer explainResults = new StringBuffer(Messages.getString("MysqlIO.8") + truncatedQuery + Messages.getString("MysqlIO.9"));
            ResultSetUtil.appendResultSetSlashGStyle(explainResults, rs);
            this.connection.getLog().logWarn(explainResults.toString());
         } catch (SQLException var10) {
         } finally {
            if (rs != null) {
               rs.close();
            }

            if (stmt != null) {
               stmt.close();
            }

         }
      }

   }

   static int getMaxBuf() {
      return maxBufferSize;
   }

   final int getServerMajorVersion() {
      return this.serverMajorVersion;
   }

   final int getServerMinorVersion() {
      return this.serverMinorVersion;
   }

   final int getServerSubMinorVersion() {
      return this.serverSubMinorVersion;
   }

   String getServerVersion() {
      return this.serverVersion;
   }

   void doHandshake(String user, String password, String database) throws SQLException {
      this.checkPacketSequence = false;
      this.readPacketSequence = 0;
      Buffer buf = this.readPacket();
      this.protocolVersion = buf.readByte();
      if (this.protocolVersion == -1) {
         try {
            this.mysqlConnection.close();
         } catch (Exception var12) {
         }

         int errno = 2000;
         errno = buf.readInt();
         String serverErrorMessage = buf.readString("ASCII");
         StringBuffer errorBuf = new StringBuffer(Messages.getString("MysqlIO.10"));
         errorBuf.append(serverErrorMessage);
         errorBuf.append("\"");
         String xOpen = SQLError.mysqlToSqlState(errno, this.connection.getUseSqlStateCodes());
         throw SQLError.createSQLException(SQLError.get(xOpen) + ", " + errorBuf.toString(), xOpen, errno);
      } else {
         this.serverVersion = buf.readString("ASCII");
         int point = this.serverVersion.indexOf(46);
         if (point != -1) {
            try {
               int n = Integer.parseInt(this.serverVersion.substring(0, point));
               this.serverMajorVersion = n;
            } catch (NumberFormatException var16) {
            }

            String remaining = this.serverVersion.substring(point + 1, this.serverVersion.length());
            point = remaining.indexOf(46);
            if (point != -1) {
               try {
                  int n = Integer.parseInt(remaining.substring(0, point));
                  this.serverMinorVersion = n;
               } catch (NumberFormatException var15) {
               }

               remaining = remaining.substring(point + 1, remaining.length());

               int pos;
               for(pos = 0; pos < remaining.length() && remaining.charAt(pos) >= '0' && remaining.charAt(pos) <= '9'; ++pos) {
               }

               try {
                  int n = Integer.parseInt(remaining.substring(0, pos));
                  this.serverSubMinorVersion = n;
               } catch (NumberFormatException var14) {
               }
            }
         }

         if (this.versionMeetsMinimum(4, 0, 8)) {
            this.maxThreeBytes = 16777215;
            this.useNewLargePackets = true;
         } else {
            this.maxThreeBytes = 16581375;
            this.useNewLargePackets = false;
         }

         this.colDecimalNeedsBump = this.versionMeetsMinimum(3, 23, 0);
         this.colDecimalNeedsBump = !this.versionMeetsMinimum(3, 23, 15);
         this.useNewUpdateCounts = this.versionMeetsMinimum(3, 22, 5);
         this.threadId = buf.readLong();
         this.seed = buf.readString("ASCII");
         this.serverCapabilities = 0;
         if (buf.getPosition() < buf.getBufLength()) {
            this.serverCapabilities = buf.readInt();
         }

         if (this.versionMeetsMinimum(4, 1, 1)) {
            int position = buf.getPosition();
            this.serverCharsetIndex = buf.readByte() & 255;
            this.serverStatus = buf.readInt();
            this.checkTransactionState(0);
            buf.setPosition(position + 16);
            String seedPart2 = buf.readString("ASCII");
            StringBuffer newSeed = new StringBuffer(20);
            newSeed.append(this.seed);
            newSeed.append(seedPart2);
            this.seed = newSeed.toString();
         }

         if ((this.serverCapabilities & 32) != 0 && this.connection.getUseCompression()) {
            this.clientParam |= 32L;
         }

         this.useConnectWithDb = database != null && database.length() > 0 && !this.connection.getCreateDatabaseIfNotExist();
         if (this.useConnectWithDb) {
            this.clientParam |= 8L;
         }

         if ((this.serverCapabilities & 2048) == 0 && this.connection.getUseSSL()) {
            if (this.connection.getRequireSSL()) {
               this.connection.close();
               this.forceClose();
               throw SQLError.createSQLException(Messages.getString("MysqlIO.15"), "08001");
            }

            this.connection.setUseSSL(false);
         }

         if ((this.serverCapabilities & 4) != 0) {
            this.clientParam |= 4L;
            this.hasLongColumnInfo = true;
         }

         this.clientParam |= 2L;
         if (this.connection.getAllowLoadLocalInfile()) {
            this.clientParam |= 128L;
         }

         if (this.isInteractiveClient) {
            this.clientParam |= 1024L;
         }

         if (this.protocolVersion > 9) {
            this.clientParam |= 1L;
         } else {
            this.clientParam &= -2L;
         }

         if (this.versionMeetsMinimum(4, 1, 0)) {
            if (this.versionMeetsMinimum(4, 1, 1)) {
               this.clientParam |= 512L;
               this.has41NewNewProt = true;
               this.clientParam |= 8192L;
               this.clientParam |= 131072L;
               if (this.connection.getAllowMultiQueries()) {
                  this.clientParam |= 65536L;
               }
            } else {
               this.clientParam |= 16384L;
               this.has41NewNewProt = false;
            }

            this.use41Extensions = true;
         }

         int passwordLength = 16;
         int userLength = user != null ? user.length() : 0;
         int databaseLength = database != null ? database.length() : 0;
         int packLength = (userLength + passwordLength + databaseLength) * 2 + 7 + 4 + 33;
         Buffer packet = null;
         if (!this.connection.getUseSSL()) {
            if ((this.serverCapabilities & '耀') != 0) {
               this.clientParam |= 32768L;
               if (this.versionMeetsMinimum(4, 1, 1)) {
                  this.secureAuth411((Buffer)null, packLength, user, password, database, true);
               } else {
                  this.secureAuth((Buffer)null, packLength, user, password, database, true);
               }
            } else {
               packet = new Buffer(packLength);
               if ((this.clientParam & 16384L) != 0L) {
                  if (this.versionMeetsMinimum(4, 1, 1)) {
                     packet.writeLong(this.clientParam);
                     packet.writeLong((long)this.maxThreeBytes);
                     packet.writeByte((byte)8);
                     packet.writeBytesNoNull(new byte[23]);
                  } else {
                     packet.writeLong(this.clientParam);
                     packet.writeLong((long)this.maxThreeBytes);
                  }
               } else {
                  packet.writeInt((int)this.clientParam);
                  packet.writeLongInt(this.maxThreeBytes);
               }

               packet.writeString(user, "Cp1252", this.connection);
               if (this.protocolVersion > 9) {
                  packet.writeString(Util.newCrypt(password, this.seed), "Cp1252", this.connection);
               } else {
                  packet.writeString(Util.oldCrypt(password, this.seed), "Cp1252", this.connection);
               }

               if (this.useConnectWithDb) {
                  packet.writeString(database, "Cp1252", this.connection);
               }

               this.send(packet, packet.getPosition());
            }
         } else {
            this.negotiateSSLConnection(user, password, database, packLength);
         }

         if (!this.versionMeetsMinimum(4, 1, 1)) {
            this.checkErrorPacket();
         }

         if ((this.serverCapabilities & 32) != 0 && this.connection.getUseCompression()) {
            this.deflater = new Deflater();
            this.useCompression = true;
            this.mysqlInput = new CompressedInputStream(this.connection, this.mysqlInput);
         }

         if (!this.useConnectWithDb) {
            this.changeDatabaseTo(database);
         }

         try {
            this.mysqlConnection = this.socketFactory.afterHandshake();
         } catch (IOException ioEx) {
            throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ioEx);
         }
      }
   }

   private void changeDatabaseTo(String database) throws SQLException {
      if (database != null && database.length() != 0) {
         try {
            this.sendCommand(2, database, (Buffer)null, false, (String)null);
         } catch (Exception ex) {
            if (!this.connection.getCreateDatabaseIfNotExist()) {
               throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ex);
            }

            this.sendCommand(3, "CREATE DATABASE IF NOT EXISTS " + database, (Buffer)null, false, (String)null);
            this.sendCommand(2, database, (Buffer)null, false, (String)null);
         }

      }
   }

   final ResultSetRow nextRow(Field[] fields, int columnCount, boolean isBinaryEncoded, int resultSetConcurrency, boolean useBufferRowIfPossible, boolean useBufferRowExplicit, boolean canReuseRowPacketForBufferRow, Buffer existingRowPacket) throws SQLException {
      if (this.useDirectRowUnpack && existingRowPacket == null && !isBinaryEncoded && !useBufferRowIfPossible && !useBufferRowExplicit) {
         return this.nextRowFast(fields, columnCount, isBinaryEncoded, resultSetConcurrency, useBufferRowIfPossible, useBufferRowExplicit, canReuseRowPacketForBufferRow);
      } else {
         Buffer rowPacket = null;
         if (existingRowPacket == null) {
            rowPacket = this.checkErrorPacket();
            if (!useBufferRowExplicit && useBufferRowIfPossible && rowPacket.getBufLength() > this.useBufferRowSizeThreshold) {
               useBufferRowExplicit = true;
            }
         } else {
            rowPacket = existingRowPacket;
            this.checkErrorPacket(existingRowPacket);
         }

         if (!isBinaryEncoded) {
            rowPacket.setPosition(rowPacket.getPosition() - 1);
            if (rowPacket.isLastDataPacket()) {
               this.readServerStatusForResultSets(rowPacket);
               return null;
            } else if (resultSetConcurrency == 1008 || !useBufferRowIfPossible && !useBufferRowExplicit) {
               byte[][] rowData = new byte[columnCount][];

               for(int i = 0; i < columnCount; ++i) {
                  rowData[i] = rowPacket.readLenByteArray(0);
               }

               return new ByteArrayRow(rowData);
            } else {
               if (!canReuseRowPacketForBufferRow) {
                  this.reusablePacket = new Buffer(rowPacket.getBufLength());
               }

               return new BufferRow(rowPacket, fields, false);
            }
         } else if (rowPacket.isLastDataPacket()) {
            rowPacket.setPosition(rowPacket.getPosition() - 1);
            this.readServerStatusForResultSets(rowPacket);
            return null;
         } else if (resultSetConcurrency != 1008 && (useBufferRowIfPossible || useBufferRowExplicit)) {
            if (!canReuseRowPacketForBufferRow) {
               this.reusablePacket = new Buffer(rowPacket.getBufLength());
            }

            return new BufferRow(rowPacket, fields, true);
         } else {
            return this.unpackBinaryResultSetRow(fields, rowPacket, resultSetConcurrency);
         }
      }
   }

   final ResultSetRow nextRowFast(Field[] fields, int columnCount, boolean isBinaryEncoded, int resultSetConcurrency, boolean useBufferRowIfPossible, boolean useBufferRowExplicit, boolean canReuseRowPacket) throws SQLException {
      try {
         int lengthRead = this.readFully(this.mysqlInput, this.packetHeaderBuf, 0, 4);
         if (lengthRead < 4) {
            this.forceClose();
            throw new RuntimeException(Messages.getString("MysqlIO.43"));
         } else {
            int packetLength = (this.packetHeaderBuf[0] & 255) + ((this.packetHeaderBuf[1] & 255) << 8) + ((this.packetHeaderBuf[2] & 255) << 16);
            if (packetLength == this.maxThreeBytes) {
               this.reuseAndReadPacket(this.reusablePacket, packetLength);
               return this.nextRow(fields, columnCount, isBinaryEncoded, resultSetConcurrency, useBufferRowIfPossible, useBufferRowExplicit, canReuseRowPacket, this.reusablePacket);
            } else if (packetLength > this.useBufferRowSizeThreshold) {
               this.reuseAndReadPacket(this.reusablePacket, packetLength);
               return this.nextRow(fields, columnCount, isBinaryEncoded, resultSetConcurrency, true, true, false, this.reusablePacket);
            } else {
               int remaining = packetLength;
               boolean firstTime = true;
               byte[][] rowData = (byte[][])null;

               for(int i = 0; i < columnCount; ++i) {
                  int sw = this.mysqlInput.read() & 255;
                  --remaining;
                  if (firstTime) {
                     if (sw == 255) {
                        Buffer errorPacket = new Buffer(packetLength + 4);
                        errorPacket.setPosition(0);
                        errorPacket.writeByte(this.packetHeaderBuf[0]);
                        errorPacket.writeByte(this.packetHeaderBuf[1]);
                        errorPacket.writeByte(this.packetHeaderBuf[2]);
                        errorPacket.writeByte((byte)1);
                        errorPacket.writeByte((byte)sw);
                        this.readFully(this.mysqlInput, errorPacket.getByteBuffer(), 5, packetLength - 1);
                        errorPacket.setPosition(4);
                        this.checkErrorPacket(errorPacket);
                     }

                     if (sw == 254 && packetLength < 9) {
                        if (this.use41Extensions) {
                           this.warningCount = this.mysqlInput.read() & 255 | (this.mysqlInput.read() & 255) << 8;
                           remaining -= 2;
                           if (this.warningCount > 0) {
                              this.hadWarnings = true;
                           }

                           this.oldServerStatus = this.serverStatus;
                           this.serverStatus = this.mysqlInput.read() & 255 | (this.mysqlInput.read() & 255) << 8;
                           this.checkTransactionState(this.oldServerStatus);
                           remaining -= 2;
                           if (remaining > 0) {
                              this.skipFully(this.mysqlInput, (long)remaining);
                           }
                        }

                        return null;
                     }

                     rowData = new byte[columnCount][];
                     firstTime = false;
                  }

                  int len = 0;
                  switch (sw) {
                     case 251:
                        len = -1;
                        break;
                     case 252:
                        len = this.mysqlInput.read() & 255 | (this.mysqlInput.read() & 255) << 8;
                        remaining -= 2;
                        break;
                     case 253:
                        len = this.mysqlInput.read() & 255 | (this.mysqlInput.read() & 255) << 8 | (this.mysqlInput.read() & 255) << 16;
                        remaining -= 3;
                        break;
                     case 254:
                        len = (int)((long)(this.mysqlInput.read() & 255) | (long)(this.mysqlInput.read() & 255) << 8 | (long)(this.mysqlInput.read() & 255) << 16 | (long)(this.mysqlInput.read() & 255) << 24 | (long)(this.mysqlInput.read() & 255) << 32 | (long)(this.mysqlInput.read() & 255) << 40 | (long)(this.mysqlInput.read() & 255) << 48 | (long)(this.mysqlInput.read() & 255) << 56);
                        remaining -= 8;
                        break;
                     default:
                        len = sw;
                  }

                  if (len == -1) {
                     rowData[i] = null;
                  } else if (len == 0) {
                     rowData[i] = Constants.EMPTY_BYTE_ARRAY;
                  } else {
                     rowData[i] = new byte[len];
                     int bytesRead = this.readFully(this.mysqlInput, rowData[i], 0, len);
                     if (bytesRead != len) {
                        throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, new IOException(Messages.getString("MysqlIO.43")));
                     }

                     remaining -= bytesRead;
                  }
               }

               if (remaining > 0) {
                  this.skipFully(this.mysqlInput, (long)remaining);
               }

               return new ByteArrayRow(rowData);
            }
         }
      } catch (IOException ioEx) {
         throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ioEx);
      }
   }

   final void quit() throws SQLException {
      Buffer packet = new Buffer(6);
      this.packetSequence = -1;
      packet.writeByte((byte)1);
      this.send(packet, packet.getPosition());
      this.forceClose();
   }

   Buffer getSharedSendPacket() {
      if (this.sharedSendPacket == null) {
         this.sharedSendPacket = new Buffer(1024);
      }

      return this.sharedSendPacket;
   }

   void closeStreamer(RowData streamer) throws SQLException {
      if (this.streamingData == null) {
         throw SQLError.createSQLException(Messages.getString("MysqlIO.17") + streamer + Messages.getString("MysqlIO.18"));
      } else if (streamer != this.streamingData) {
         throw SQLError.createSQLException(Messages.getString("MysqlIO.19") + streamer + Messages.getString("MysqlIO.20") + Messages.getString("MysqlIO.21") + Messages.getString("MysqlIO.22"));
      } else {
         this.streamingData = null;
      }
   }

   boolean tackOnMoreStreamingResults(ResultSetImpl addingTo) throws SQLException {
      if ((this.serverStatus & 8) == 0) {
         return false;
      } else {
         boolean moreRowSetsExist = true;
         ResultSetImpl currentResultSet = addingTo;
         boolean firstTime = true;

         while(moreRowSetsExist && (firstTime || !currentResultSet.reallyResult())) {
            firstTime = false;
            Buffer fieldPacket = this.checkErrorPacket();
            fieldPacket.setPosition(0);
            java.sql.Statement owningStatement = addingTo.getStatement();
            int maxRows = owningStatement.getMaxRows();
            ResultSetImpl newResultSet = this.readResultsForQueryOrUpdate((StatementImpl)owningStatement, maxRows, owningStatement.getResultSetType(), owningStatement.getResultSetConcurrency(), true, owningStatement.getConnection().getCatalog(), fieldPacket, addingTo.isBinaryEncoded, -1L, (Field[])null);
            currentResultSet.setNextResultSet(newResultSet);
            currentResultSet = newResultSet;
            moreRowSetsExist = (this.serverStatus & 8) != 0;
            if (!newResultSet.reallyResult() && !moreRowSetsExist) {
               return false;
            }
         }

         return true;
      }
   }

   ResultSetImpl readAllResults(StatementImpl callingStatement, int maxRows, int resultSetType, int resultSetConcurrency, boolean streamResults, String catalog, Buffer resultPacket, boolean isBinaryEncoded, long preSentColumnCount, Field[] metadataFromCache) throws SQLException {
      resultPacket.setPosition(resultPacket.getPosition() - 1);
      ResultSetImpl topLevelResultSet = this.readResultsForQueryOrUpdate(callingStatement, maxRows, resultSetType, resultSetConcurrency, streamResults, catalog, resultPacket, isBinaryEncoded, preSentColumnCount, metadataFromCache);
      ResultSetImpl currentResultSet = topLevelResultSet;
      boolean checkForMoreResults = (this.clientParam & 131072L) != 0L;
      boolean serverHasMoreResults = (this.serverStatus & 8) != 0;
      if (serverHasMoreResults && streamResults) {
         if (topLevelResultSet.getUpdateCount() != -1L) {
            this.tackOnMoreStreamingResults(topLevelResultSet);
         }

         this.reclaimLargeReusablePacket();
         return topLevelResultSet;
      } else {
         for(boolean moreRowSetsExist = checkForMoreResults & serverHasMoreResults; moreRowSetsExist; moreRowSetsExist = (this.serverStatus & 8) != 0) {
            Buffer fieldPacket = this.checkErrorPacket();
            fieldPacket.setPosition(0);
            ResultSetImpl newResultSet = this.readResultsForQueryOrUpdate(callingStatement, maxRows, resultSetType, resultSetConcurrency, streamResults, catalog, fieldPacket, isBinaryEncoded, preSentColumnCount, metadataFromCache);
            currentResultSet.setNextResultSet(newResultSet);
            currentResultSet = newResultSet;
         }

         if (!streamResults) {
            this.clearInputStream();
         }

         this.reclaimLargeReusablePacket();
         return topLevelResultSet;
      }
   }

   void resetMaxBuf() {
      this.maxAllowedPacket = this.connection.getMaxAllowedPacket();
   }

   final Buffer sendCommand(int command, String extraData, Buffer queryPacket, boolean skipCheck, String extraDataCharEncoding) throws SQLException {
      ++this.commandCount;
      this.enablePacketDebug = this.connection.getEnablePacketDebug();
      this.traceProtocol = this.connection.getTraceProtocol();
      this.readPacketSequence = 0;

      try {
         this.checkForOutstandingStreamingData();
         this.oldServerStatus = this.serverStatus;
         this.serverStatus = 0;
         this.hadWarnings = false;
         this.warningCount = 0;
         this.queryNoIndexUsed = false;
         this.queryBadIndexUsed = false;
         if (this.useCompression) {
            int bytesLeft = this.mysqlInput.available();
            if (bytesLeft > 0) {
               this.mysqlInput.skip((long)bytesLeft);
            }
         }

         try {
            this.clearInputStream();
            if (queryPacket == null) {
               int packLength = 8 + (extraData != null ? extraData.length() : 0) + 2;
               if (this.sendPacket == null) {
                  this.sendPacket = new Buffer(packLength);
               }

               this.packetSequence = -1;
               this.readPacketSequence = 0;
               this.checkPacketSequence = true;
               this.sendPacket.clear();
               this.sendPacket.writeByte((byte)command);
               if (command != 2 && command != 5 && command != 6 && command != 3 && command != 22) {
                  if (command == 12) {
                     long id = Long.parseLong(extraData);
                     this.sendPacket.writeLong(id);
                  }
               } else if (extraDataCharEncoding == null) {
                  this.sendPacket.writeStringNoNull(extraData);
               } else {
                  this.sendPacket.writeStringNoNull(extraData, extraDataCharEncoding, this.connection.getServerCharacterEncoding(), this.connection.parserKnowsUnicode(), this.connection);
               }

               this.send(this.sendPacket, this.sendPacket.getPosition());
            } else {
               this.packetSequence = -1;
               this.send(queryPacket, queryPacket.getPosition());
            }
         } catch (SQLException sqlEx) {
            throw sqlEx;
         } catch (Exception ex) {
            throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ex);
         }

         Buffer returnPacket = null;
         if (!skipCheck) {
            if (command == 23 || command == 26) {
               this.readPacketSequence = 0;
               this.packetSequenceReset = true;
            }

            returnPacket = this.checkErrorPacket(command);
         }

         return returnPacket;
      } catch (IOException ioEx) {
         throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ioEx);
      }
   }

   final ResultSetInternalMethods sqlQueryDirect(StatementImpl callingStatement, String query, String characterEncoding, Buffer queryPacket, int maxRows, int resultSetType, int resultSetConcurrency, boolean streamResults, String catalog, Field[] cachedMetadata) throws Exception {
      ++this.statementExecutionDepth;

      try {
         if (this.statementInterceptors != null) {
            ResultSetInternalMethods interceptedResults = this.invokeStatementInterceptorsPre(query, callingStatement);
            if (interceptedResults != null) {
               ResultSetInternalMethods var12 = interceptedResults;
               return var12;
            }
         }

         long queryStartTime = 0L;
         long queryEndTime = 0L;
         if (query != null) {
            int packLength = 5 + query.length() * 2 + 2;
            String statementComment = this.connection.getStatementComment();
            byte[] commentAsBytes = null;
            if (statementComment != null) {
               commentAsBytes = StringUtils.getBytes((String)statementComment, (SingleByteCharsetConverter)null, characterEncoding, this.connection.getServerCharacterEncoding(), this.connection.parserKnowsUnicode());
               packLength += commentAsBytes.length;
               packLength += 6;
            }

            if (this.sendPacket == null) {
               this.sendPacket = new Buffer(packLength);
            } else {
               this.sendPacket.clear();
            }

            this.sendPacket.writeByte((byte)3);
            if (commentAsBytes != null) {
               this.sendPacket.writeBytesNoNull(Constants.SLASH_STAR_SPACE_AS_BYTES);
               this.sendPacket.writeBytesNoNull(commentAsBytes);
               this.sendPacket.writeBytesNoNull(Constants.SPACE_STAR_SLASH_SPACE_AS_BYTES);
            }

            if (characterEncoding != null) {
               if (this.platformDbCharsetMatches) {
                  this.sendPacket.writeStringNoNull(query, characterEncoding, this.connection.getServerCharacterEncoding(), this.connection.parserKnowsUnicode(), this.connection);
               } else if (StringUtils.startsWithIgnoreCaseAndWs(query, "LOAD DATA")) {
                  this.sendPacket.writeBytesNoNull(query.getBytes());
               } else {
                  this.sendPacket.writeStringNoNull(query, characterEncoding, this.connection.getServerCharacterEncoding(), this.connection.parserKnowsUnicode(), this.connection);
               }
            } else {
               this.sendPacket.writeStringNoNull(query);
            }

            queryPacket = this.sendPacket;
         }

         byte[] queryBuf = null;
         int oldPacketPosition = 0;
         if (this.needToGrabQueryFromPacket) {
            queryBuf = queryPacket.getByteBuffer();
            oldPacketPosition = queryPacket.getPosition();
            queryStartTime = this.getCurrentTimeNanosOrMillis();
         }

         Buffer resultPacket = this.sendCommand(3, (String)null, queryPacket, false, (String)null);
         long fetchBeginTime = 0L;
         long fetchEndTime = 0L;
         String profileQueryToLog = null;
         boolean queryWasSlow = false;
         if (this.profileSql || this.logSlowQueries) {
            queryEndTime = System.currentTimeMillis();
            boolean shouldExtractQuery = false;
            if (this.profileSql) {
               shouldExtractQuery = true;
            } else if (this.logSlowQueries) {
               long queryTime = queryEndTime - queryStartTime;
               boolean logSlow = false;
               if (this.useAutoSlowLog) {
                  logSlow = queryTime > (long)this.connection.getSlowQueryThresholdMillis();
               } else {
                  logSlow = this.connection.isAbonormallyLongQuery(queryTime);
                  this.connection.reportQueryTime(queryTime);
               }

               if (logSlow) {
                  shouldExtractQuery = true;
                  queryWasSlow = true;
               }
            }

            if (shouldExtractQuery) {
               boolean truncated = false;
               int extractPosition = oldPacketPosition;
               if (oldPacketPosition > this.connection.getMaxQuerySizeToLog()) {
                  extractPosition = this.connection.getMaxQuerySizeToLog() + 5;
                  truncated = true;
               }

               profileQueryToLog = new String(queryBuf, 5, extractPosition - 5);
               if (truncated) {
                  profileQueryToLog = profileQueryToLog + Messages.getString("MysqlIO.25");
               }
            }

            fetchBeginTime = queryEndTime;
         }

         if (this.autoGenerateTestcaseScript) {
            String testcaseQuery = null;
            if (query != null) {
               testcaseQuery = query;
            } else {
               testcaseQuery = new String(queryBuf, 5, oldPacketPosition - 5);
            }

            StringBuffer debugBuf = new StringBuffer(testcaseQuery.length() + 32);
            this.connection.generateConnectionCommentBlock(debugBuf);
            debugBuf.append(testcaseQuery);
            debugBuf.append(';');
            this.connection.dumpTestcaseQuery(debugBuf.toString());
         }

         ResultSetInternalMethods rs = this.readAllResults(callingStatement, maxRows, resultSetType, resultSetConcurrency, streamResults, catalog, resultPacket, false, -1L, cachedMetadata);
         if (queryWasSlow) {
            StringBuffer mesgBuf = new StringBuffer(48 + profileQueryToLog.length());
            mesgBuf.append(Messages.getString("MysqlIO.SlowQuery", new Object[]{new Long(this.slowQueryThreshold), this.queryTimingUnits, new Long(queryEndTime - queryStartTime)}));
            mesgBuf.append(profileQueryToLog);
            ProfilerEventHandler eventSink = ProfilerEventHandlerFactory.getInstance(this.connection);
            eventSink.consumeEvent(new ProfilerEvent((byte)6, "", catalog, this.connection.getId(), callingStatement != null ? callingStatement.getId() : 999, ((ResultSetImpl)rs).resultId, System.currentTimeMillis(), (long)((int)(queryEndTime - queryStartTime)), this.queryTimingUnits, (String)null, new Throwable(), mesgBuf.toString()));
            if (this.connection.getExplainSlowQueries()) {
               if (oldPacketPosition < 1048576) {
                  this.explainSlowQuery(queryPacket.getBytes(5, oldPacketPosition - 5), profileQueryToLog);
               } else {
                  this.connection.getLog().logWarn(Messages.getString("MysqlIO.28") + 1048576 + Messages.getString("MysqlIO.29"));
               }
            }
         }

         if (this.logSlowQueries) {
            ProfilerEventHandler eventSink = ProfilerEventHandlerFactory.getInstance(this.connection);
            if (this.queryBadIndexUsed) {
               eventSink.consumeEvent(new ProfilerEvent((byte)6, "", catalog, this.connection.getId(), callingStatement != null ? callingStatement.getId() : 999, ((ResultSetImpl)rs).resultId, System.currentTimeMillis(), queryEndTime - queryStartTime, this.queryTimingUnits, (String)null, new Throwable(), Messages.getString("MysqlIO.33") + profileQueryToLog));
            }

            if (this.queryNoIndexUsed) {
               eventSink.consumeEvent(new ProfilerEvent((byte)6, "", catalog, this.connection.getId(), callingStatement != null ? callingStatement.getId() : 999, ((ResultSetImpl)rs).resultId, System.currentTimeMillis(), queryEndTime - queryStartTime, this.queryTimingUnits, (String)null, new Throwable(), Messages.getString("MysqlIO.35") + profileQueryToLog));
            }
         }

         if (this.profileSql) {
            fetchEndTime = this.getCurrentTimeNanosOrMillis();
            ProfilerEventHandler eventSink = ProfilerEventHandlerFactory.getInstance(this.connection);
            eventSink.consumeEvent(new ProfilerEvent((byte)3, "", catalog, this.connection.getId(), callingStatement != null ? callingStatement.getId() : 999, ((ResultSetImpl)rs).resultId, System.currentTimeMillis(), queryEndTime - queryStartTime, this.queryTimingUnits, (String)null, new Throwable(), profileQueryToLog));
            eventSink.consumeEvent(new ProfilerEvent((byte)5, "", catalog, this.connection.getId(), callingStatement != null ? callingStatement.getId() : 999, ((ResultSetImpl)rs).resultId, System.currentTimeMillis(), fetchEndTime - fetchBeginTime, this.queryTimingUnits, (String)null, new Throwable(), (String)null));
         }

         if (this.hadWarnings) {
            this.scanForAndThrowDataTruncation();
         }

         if (this.statementInterceptors != null) {
            ResultSetInternalMethods interceptedResults = this.invokeStatementInterceptorsPost(query, callingStatement, rs);
            if (interceptedResults != null) {
               rs = interceptedResults;
            }
         }

         Object var46 = rs;
         return (ResultSetInternalMethods)var46;
      } finally {
         --this.statementExecutionDepth;
      }
   }

   private ResultSetInternalMethods invokeStatementInterceptorsPre(String sql, Statement interceptedStatement) throws SQLException {
      ResultSetInternalMethods previousResultSet = null;

      for(StatementInterceptor interceptor : this.statementInterceptors) {
         boolean executeTopLevelOnly = interceptor.executeTopLevelOnly();
         boolean shouldExecute = executeTopLevelOnly && this.statementExecutionDepth == 1 || !executeTopLevelOnly;
         if (shouldExecute) {
            String sqlToInterceptor = sql;
            if (interceptedStatement instanceof PreparedStatement) {
               sqlToInterceptor = ((PreparedStatement)interceptedStatement).asSql();
            }

            ResultSetInternalMethods interceptedResultSet = interceptor.preProcess(sqlToInterceptor, interceptedStatement, this.connection);
            if (interceptedResultSet != null) {
               previousResultSet = interceptedResultSet;
            }
         }
      }

      return previousResultSet;
   }

   private ResultSetInternalMethods invokeStatementInterceptorsPost(String sql, Statement interceptedStatement, ResultSetInternalMethods originalResultSet) throws SQLException {
      for(StatementInterceptor interceptor : this.statementInterceptors) {
         boolean executeTopLevelOnly = interceptor.executeTopLevelOnly();
         boolean shouldExecute = executeTopLevelOnly && this.statementExecutionDepth == 1 || !executeTopLevelOnly;
         if (shouldExecute) {
            String sqlToInterceptor = sql;
            if (interceptedStatement instanceof PreparedStatement) {
               sqlToInterceptor = ((PreparedStatement)interceptedStatement).asSql();
            }

            ResultSetInternalMethods interceptedResultSet = interceptor.postProcess(sqlToInterceptor, interceptedStatement, originalResultSet, this.connection);
            if (interceptedResultSet != null) {
               originalResultSet = interceptedResultSet;
            }
         }
      }

      return originalResultSet;
   }

   private void calculateSlowQueryThreshold() {
      this.slowQueryThreshold = (long)this.connection.getSlowQueryThresholdMillis();
      if (this.connection.getUseNanosForElapsedTime()) {
         long nanosThreshold = this.connection.getSlowQueryThresholdNanos();
         if (nanosThreshold != 0L) {
            this.slowQueryThreshold = nanosThreshold;
         } else {
            this.slowQueryThreshold *= 1000000L;
         }
      }

   }

   protected long getCurrentTimeNanosOrMillis() {
      return this.useNanosForElapsedTime ? Util.getCurrentTimeNanosOrMillis() : System.currentTimeMillis();
   }

   String getHost() {
      return this.host;
   }

   boolean isVersion(int major, int minor, int subminor) {
      return major == this.getServerMajorVersion() && minor == this.getServerMinorVersion() && subminor == this.getServerSubMinorVersion();
   }

   boolean versionMeetsMinimum(int major, int minor, int subminor) {
      if (this.getServerMajorVersion() >= major) {
         if (this.getServerMajorVersion() == major) {
            if (this.getServerMinorVersion() >= minor) {
               if (this.getServerMinorVersion() == minor) {
                  return this.getServerSubMinorVersion() >= subminor;
               } else {
                  return true;
               }
            } else {
               return false;
            }
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   private static final String getPacketDumpToLog(Buffer packetToDump, int packetLength) {
      if (packetLength < 1024) {
         return packetToDump.dump(packetLength);
      } else {
         StringBuffer packetDumpBuf = new StringBuffer(4096);
         packetDumpBuf.append(packetToDump.dump(1024));
         packetDumpBuf.append(Messages.getString("MysqlIO.36"));
         packetDumpBuf.append(1024);
         packetDumpBuf.append(Messages.getString("MysqlIO.37"));
         return packetDumpBuf.toString();
      }
   }

   private final int readFully(InputStream in, byte[] b, int off, int len) throws IOException {
      if (len < 0) {
         throw new IndexOutOfBoundsException();
      } else {
         int n;
         int count;
         for(n = 0; n < len; n += count) {
            count = in.read(b, off + n, len - n);
            if (count < 0) {
               throw new EOFException(Messages.getString("MysqlIO.EOF", new Object[]{new Integer(len), new Integer(n)}));
            }
         }

         return n;
      }
   }

   private final long skipFully(InputStream in, long len) throws IOException {
      if (len < 0L) {
         throw new IOException("Negative skip length not allowed");
      } else {
         long n;
         long count;
         for(n = 0L; n < len; n += count) {
            count = in.skip(len - n);
            if (count < 0L) {
               throw new EOFException(Messages.getString("MysqlIO.EOF", new Object[]{new Long(len), new Long(n)}));
            }
         }

         return n;
      }
   }

   protected final ResultSetImpl readResultsForQueryOrUpdate(StatementImpl callingStatement, int maxRows, int resultSetType, int resultSetConcurrency, boolean streamResults, String catalog, Buffer resultPacket, boolean isBinaryEncoded, long preSentColumnCount, Field[] metadataFromCache) throws SQLException {
      long columnCount = resultPacket.readFieldLength();
      if (columnCount == 0L) {
         return this.buildResultSetWithUpdates(callingStatement, resultPacket);
      } else if (columnCount == -1L) {
         String charEncoding = null;
         if (this.connection.getUseUnicode()) {
            charEncoding = this.connection.getEncoding();
         }

         String fileName = null;
         if (this.platformDbCharsetMatches) {
            fileName = charEncoding != null ? resultPacket.readString(charEncoding) : resultPacket.readString();
         } else {
            fileName = resultPacket.readString();
         }

         return this.sendFileToServer(callingStatement, fileName);
      } else {
         ResultSetImpl results = this.getResultSet(callingStatement, columnCount, maxRows, resultSetType, resultSetConcurrency, streamResults, catalog, isBinaryEncoded, metadataFromCache);
         return results;
      }
   }

   private int alignPacketSize(int a, int l) {
      return a + l - 1 & ~(l - 1);
   }

   private ResultSetImpl buildResultSetWithRows(StatementImpl callingStatement, String catalog, Field[] fields, RowData rows, int resultSetType, int resultSetConcurrency, boolean isBinaryEncoded) throws SQLException {
      ResultSetImpl rs = null;
      switch (resultSetConcurrency) {
         case 1007:
            rs = ResultSetImpl.getInstance(catalog, fields, rows, this.connection, callingStatement, false);
            if (isBinaryEncoded) {
               rs.setBinaryEncoded();
            }
            break;
         case 1008:
            rs = ResultSetImpl.getInstance(catalog, fields, rows, this.connection, callingStatement, true);
            break;
         default:
            return ResultSetImpl.getInstance(catalog, fields, rows, this.connection, callingStatement, false);
      }

      rs.setResultSetType(resultSetType);
      rs.setResultSetConcurrency(resultSetConcurrency);
      return rs;
   }

   private ResultSetImpl buildResultSetWithUpdates(StatementImpl callingStatement, Buffer resultPacket) throws SQLException {
      long updateCount = -1L;
      long updateID = -1L;
      String info = null;

      try {
         if (this.useNewUpdateCounts) {
            updateCount = resultPacket.newReadLength();
            updateID = resultPacket.newReadLength();
         } else {
            updateCount = resultPacket.readLength();
            updateID = resultPacket.readLength();
         }

         if (this.use41Extensions) {
            this.serverStatus = resultPacket.readInt();
            this.checkTransactionState(this.oldServerStatus);
            this.warningCount = resultPacket.readInt();
            if (this.warningCount > 0) {
               this.hadWarnings = true;
            }

            resultPacket.readByte();
            if (this.profileSql) {
               this.queryNoIndexUsed = (this.serverStatus & 16) != 0;
               this.queryBadIndexUsed = (this.serverStatus & 32) != 0;
            }
         }

         if (this.connection.isReadInfoMsgEnabled()) {
            info = resultPacket.readString(this.connection.getErrorMessageEncoding());
         }
      } catch (Exception ex) {
         SQLException sqlEx = SQLError.createSQLException(SQLError.get("S1000"), "S1000", -1);
         sqlEx.initCause(ex);
         throw sqlEx;
      }

      ResultSetInternalMethods updateRs = ResultSetImpl.getInstance(updateCount, updateID, this.connection, callingStatement);
      if (info != null) {
         ((ResultSetImpl)updateRs).setServerInfo(info);
      }

      return (ResultSetImpl)updateRs;
   }

   private void checkForOutstandingStreamingData() throws SQLException {
      if (this.streamingData != null) {
         boolean shouldClobber = this.connection.getClobberStreamingResults();
         if (!shouldClobber) {
            throw SQLError.createSQLException(Messages.getString("MysqlIO.39") + this.streamingData + Messages.getString("MysqlIO.40") + Messages.getString("MysqlIO.41") + Messages.getString("MysqlIO.42"));
         }

         this.streamingData.getOwner().realClose(false);
         this.clearInputStream();
      }

   }

   private Buffer compressPacket(Buffer packet, int offset, int packetLen, int headerLength) throws SQLException {
      packet.writeLongInt(packetLen - headerLength);
      packet.writeByte((byte)0);
      int lengthToWrite = 0;
      int compressedLength = 0;
      byte[] bytesToCompress = packet.getByteBuffer();
      byte[] compressedBytes = null;
      int offsetWrite = 0;
      if (packetLen < 50) {
         lengthToWrite = packetLen;
         compressedBytes = packet.getByteBuffer();
         compressedLength = 0;
         offsetWrite = offset;
      } else {
         compressedBytes = new byte[bytesToCompress.length * 2];
         this.deflater.reset();
         this.deflater.setInput(bytesToCompress, offset, packetLen);
         this.deflater.finish();
         int compLen = this.deflater.deflate(compressedBytes);
         if (compLen > packetLen) {
            lengthToWrite = packetLen;
            compressedBytes = packet.getByteBuffer();
            compressedLength = 0;
            offsetWrite = offset;
         } else {
            lengthToWrite = compLen;
            headerLength += 3;
            compressedLength = packetLen;
         }
      }

      Buffer compressedPacket = new Buffer(packetLen + headerLength);
      compressedPacket.setPosition(0);
      compressedPacket.writeLongInt(lengthToWrite);
      compressedPacket.writeByte(this.packetSequence);
      compressedPacket.writeLongInt(compressedLength);
      compressedPacket.writeBytesNoNull(compressedBytes, offsetWrite, lengthToWrite);
      return compressedPacket;
   }

   private final void readServerStatusForResultSets(Buffer rowPacket) throws SQLException {
      if (this.use41Extensions) {
         rowPacket.readByte();
         this.warningCount = rowPacket.readInt();
         if (this.warningCount > 0) {
            this.hadWarnings = true;
         }

         this.oldServerStatus = this.serverStatus;
         this.serverStatus = rowPacket.readInt();
         this.checkTransactionState(this.oldServerStatus);
         if (this.profileSql) {
            this.queryNoIndexUsed = (this.serverStatus & 16) != 0;
            this.queryBadIndexUsed = (this.serverStatus & 32) != 0;
         }
      }

   }

   private SocketFactory createSocketFactory() throws SQLException {
      try {
         if (this.socketFactoryClassName == null) {
            throw SQLError.createSQLException(Messages.getString("MysqlIO.75"), "08001");
         } else {
            return (SocketFactory)Class.forName(this.socketFactoryClassName).newInstance();
         }
      } catch (Exception ex) {
         SQLException sqlEx = SQLError.createSQLException(Messages.getString("MysqlIO.76") + this.socketFactoryClassName + Messages.getString("MysqlIO.77"), "08001");
         sqlEx.initCause(ex);
         throw sqlEx;
      }
   }

   private void enqueuePacketForDebugging(boolean isPacketBeingSent, boolean isPacketReused, int sendLength, byte[] header, Buffer packet) throws SQLException {
      if (this.packetDebugRingBuffer.size() + 1 > this.connection.getPacketDebugBufferSize()) {
         this.packetDebugRingBuffer.removeFirst();
      }

      StringBuffer packetDump = null;
      if (!isPacketBeingSent) {
         int bytesToDump = Math.min(1024, packet.getBufLength());
         Buffer packetToDump = new Buffer(4 + bytesToDump);
         packetToDump.setPosition(0);
         packetToDump.writeBytesNoNull(header);
         packetToDump.writeBytesNoNull(packet.getBytes(0, bytesToDump));
         String packetPayload = packetToDump.dump(bytesToDump);
         packetDump = new StringBuffer(96 + packetPayload.length());
         packetDump.append("Server ");
         if (isPacketReused) {
            packetDump.append("(re-used)");
         } else {
            packetDump.append("(new)");
         }

         packetDump.append(" ");
         packetDump.append(packet.toSuperString());
         packetDump.append(" --------------------> Client\n");
         packetDump.append("\nPacket payload:\n\n");
         packetDump.append(packetPayload);
         if (bytesToDump == 1024) {
            packetDump.append("\nNote: Packet of " + packet.getBufLength() + " bytes truncated to " + 1024 + " bytes.\n");
         }
      } else {
         int bytesToDump = Math.min(1024, sendLength);
         String packetPayload = packet.dump(bytesToDump);
         packetDump = new StringBuffer(68 + packetPayload.length());
         packetDump.append("Client ");
         packetDump.append(packet.toSuperString());
         packetDump.append("--------------------> Server\n");
         packetDump.append("\nPacket payload:\n\n");
         packetDump.append(packetPayload);
         if (bytesToDump == 1024) {
            packetDump.append("\nNote: Packet of " + sendLength + " bytes truncated to " + 1024 + " bytes.\n");
         }
      }

      this.packetDebugRingBuffer.addLast(packetDump);
   }

   private RowData readSingleRowSet(long columnCount, int maxRows, int resultSetConcurrency, boolean isBinaryEncoded, Field[] fields) throws SQLException {
      ArrayList rows = new ArrayList();
      boolean useBufferRowExplicit = useBufferRowExplicit(fields);
      ResultSetRow row = this.nextRow(fields, (int)columnCount, isBinaryEncoded, resultSetConcurrency, false, useBufferRowExplicit, false, (Buffer)null);
      int rowCount = 0;
      if (row != null) {
         rows.add(row);
         rowCount = 1;
      }

      while(row != null) {
         row = this.nextRow(fields, (int)columnCount, isBinaryEncoded, resultSetConcurrency, false, useBufferRowExplicit, false, (Buffer)null);
         if (row != null && (maxRows == -1 || rowCount < maxRows)) {
            rows.add(row);
            ++rowCount;
         }
      }

      RowData rowData = new RowDataStatic(rows);
      return rowData;
   }

   public static boolean useBufferRowExplicit(Field[] param0) {
      // $FF: Couldn't be decompiled
   }

   private void reclaimLargeReusablePacket() {
      if (this.reusablePacket != null && this.reusablePacket.getCapacity() > 1048576) {
         this.reusablePacket = new Buffer(1024);
      }

   }

   private final Buffer reuseAndReadPacket(Buffer reuse) throws SQLException {
      return this.reuseAndReadPacket(reuse, -1);
   }

   private final Buffer reuseAndReadPacket(Buffer reuse, int existingPacketLength) throws SQLException {
      try {
         reuse.setWasMultiPacket(false);
         int packetLength = 0;
         if (existingPacketLength == -1) {
            int lengthRead = this.readFully(this.mysqlInput, this.packetHeaderBuf, 0, 4);
            if (lengthRead < 4) {
               this.forceClose();
               throw new IOException(Messages.getString("MysqlIO.43"));
            }

            packetLength = (this.packetHeaderBuf[0] & 255) + ((this.packetHeaderBuf[1] & 255) << 8) + ((this.packetHeaderBuf[2] & 255) << 16);
         } else {
            packetLength = existingPacketLength;
         }

         if (this.traceProtocol) {
            StringBuffer traceMessageBuf = new StringBuffer();
            traceMessageBuf.append(Messages.getString("MysqlIO.44"));
            traceMessageBuf.append(packetLength);
            traceMessageBuf.append(Messages.getString("MysqlIO.45"));
            traceMessageBuf.append(StringUtils.dumpAsHex(this.packetHeaderBuf, 4));
            this.connection.getLog().logTrace(traceMessageBuf.toString());
         }

         byte multiPacketSeq = this.packetHeaderBuf[3];
         if (!this.packetSequenceReset) {
            if (this.enablePacketDebug && this.checkPacketSequence) {
               this.checkPacketSequencing(multiPacketSeq);
            }
         } else {
            this.packetSequenceReset = false;
         }

         this.readPacketSequence = multiPacketSeq;
         reuse.setPosition(0);
         if (reuse.getByteBuffer().length <= packetLength) {
            reuse.setByteBuffer(new byte[packetLength + 1]);
         }

         reuse.setBufLength(packetLength);
         int numBytesRead = this.readFully(this.mysqlInput, reuse.getByteBuffer(), 0, packetLength);
         if (numBytesRead != packetLength) {
            throw new IOException("Short read, expected " + packetLength + " bytes, only read " + numBytesRead);
         } else {
            if (this.traceProtocol) {
               StringBuffer traceMessageBuf = new StringBuffer();
               traceMessageBuf.append(Messages.getString("MysqlIO.46"));
               traceMessageBuf.append(getPacketDumpToLog(reuse, packetLength));
               this.connection.getLog().logTrace(traceMessageBuf.toString());
            }

            if (this.enablePacketDebug) {
               this.enqueuePacketForDebugging(false, true, 0, this.packetHeaderBuf, reuse);
            }

            boolean isMultiPacket = false;
            if (packetLength == this.maxThreeBytes) {
               reuse.setPosition(this.maxThreeBytes);
               isMultiPacket = true;
               packetLength = this.readRemainingMultiPackets(reuse, multiPacketSeq, packetLength);
            }

            if (!isMultiPacket) {
               reuse.getByteBuffer()[packetLength] = 0;
            }

            if (this.connection.getMaintainTimeStats()) {
               this.lastPacketReceivedTimeMs = System.currentTimeMillis();
            }

            return reuse;
         }
      } catch (IOException ioEx) {
         throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ioEx);
      } catch (OutOfMemoryError var22) {
         OutOfMemoryError oom = var22;

         label216:
         try {
            this.clearInputStream();
         } finally {
            break label216;
         }

         try {
            this.connection.realClose(false, false, true, oom);
         } finally {
            ;
         }

         throw var22;
      }
   }

   private int readRemainingMultiPackets(Buffer reuse, byte multiPacketSeq, int packetEndPoint) throws IOException, SQLException {
      int lengthRead = this.readFully(this.mysqlInput, this.packetHeaderBuf, 0, 4);
      if (lengthRead < 4) {
         this.forceClose();
         throw new IOException(Messages.getString("MysqlIO.47"));
      } else {
         int packetLength = (this.packetHeaderBuf[0] & 255) + ((this.packetHeaderBuf[1] & 255) << 8) + ((this.packetHeaderBuf[2] & 255) << 16);
         Buffer multiPacket = new Buffer(packetLength);
         boolean firstMultiPkt = true;

         while(true) {
            if (!firstMultiPkt) {
               lengthRead = this.readFully(this.mysqlInput, this.packetHeaderBuf, 0, 4);
               if (lengthRead < 4) {
                  this.forceClose();
                  throw new IOException(Messages.getString("MysqlIO.48"));
               }

               packetLength = (this.packetHeaderBuf[0] & 255) + ((this.packetHeaderBuf[1] & 255) << 8) + ((this.packetHeaderBuf[2] & 255) << 16);
            } else {
               firstMultiPkt = false;
            }

            if (!this.useNewLargePackets && packetLength == 1) {
               this.clearInputStream();
               break;
            }

            if (packetLength < this.maxThreeBytes) {
               byte newPacketSeq = this.packetHeaderBuf[3];
               if (newPacketSeq != multiPacketSeq + 1) {
                  throw new IOException(Messages.getString("MysqlIO.49"));
               }

               multiPacket.setPosition(0);
               multiPacket.setBufLength(packetLength);
               byte[] byteBuf = multiPacket.getByteBuffer();
               int bytesRead = this.readFully(this.mysqlInput, byteBuf, 0, packetLength);
               if (bytesRead != packetLength) {
                  throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, SQLError.createSQLException(Messages.getString("MysqlIO.50") + packetLength + Messages.getString("MysqlIO.51") + bytesRead + "."));
               }

               reuse.writeBytesNoNull(byteBuf, 0, packetLength);
               packetEndPoint += packetLength;
               break;
            }

            byte newPacketSeq = this.packetHeaderBuf[3];
            if (newPacketSeq != multiPacketSeq + 1) {
               throw new IOException(Messages.getString("MysqlIO.53"));
            }

            multiPacketSeq = newPacketSeq;
            multiPacket.setPosition(0);
            multiPacket.setBufLength(packetLength);
            byte[] byteBuf = multiPacket.getByteBuffer();
            int bytesRead = this.readFully(this.mysqlInput, byteBuf, 0, packetLength);
            if (bytesRead != packetLength) {
               throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, SQLError.createSQLException(Messages.getString("MysqlIO.54") + packetLength + Messages.getString("MysqlIO.55") + bytesRead + "."));
            }

            reuse.writeBytesNoNull(byteBuf, 0, packetLength);
            packetEndPoint += packetLength;
         }

         reuse.setPosition(0);
         reuse.setWasMultiPacket(true);
         return packetLength;
      }
   }

   private void checkPacketSequencing(byte multiPacketSeq) throws SQLException {
      if (multiPacketSeq == -128 && this.readPacketSequence != 127) {
         throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, new IOException("Packets out of order, expected packet # -128, but received packet # " + multiPacketSeq));
      } else if (this.readPacketSequence == -1 && multiPacketSeq != 0) {
         throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, new IOException("Packets out of order, expected packet # -1, but received packet # " + multiPacketSeq));
      } else if (multiPacketSeq != -128 && this.readPacketSequence != -1 && multiPacketSeq != this.readPacketSequence + 1) {
         throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, new IOException("Packets out of order, expected packet # " + (this.readPacketSequence + 1) + ", but received packet # " + multiPacketSeq));
      }
   }

   void enableMultiQueries() throws SQLException {
      Buffer buf = this.getSharedSendPacket();
      buf.clear();
      buf.writeByte((byte)27);
      buf.writeInt(0);
      this.sendCommand(27, (String)null, buf, false, (String)null);
   }

   void disableMultiQueries() throws SQLException {
      Buffer buf = this.getSharedSendPacket();
      buf.clear();
      buf.writeByte((byte)27);
      buf.writeInt(1);
      this.sendCommand(27, (String)null, buf, false, (String)null);
   }

   private final void send(Buffer packet, int packetLen) throws SQLException {
      try {
         if (packetLen > this.maxAllowedPacket) {
            throw new PacketTooBigException((long)packetLen, (long)this.maxAllowedPacket);
         } else {
            if (this.serverMajorVersion >= 4 && packetLen >= this.maxThreeBytes) {
               this.sendSplitPackets(packet);
            } else {
               ++this.packetSequence;
               Buffer packetToSend = packet;
               packet.setPosition(0);
               if (this.useCompression) {
                  int originalPacketLen = packetLen;
                  packetToSend = this.compressPacket(packet, 0, packetLen, 4);
                  packetLen = packetToSend.getPosition();
                  if (this.traceProtocol) {
                     StringBuffer traceMessageBuf = new StringBuffer();
                     traceMessageBuf.append(Messages.getString("MysqlIO.57"));
                     traceMessageBuf.append(getPacketDumpToLog(packetToSend, packetLen));
                     traceMessageBuf.append(Messages.getString("MysqlIO.58"));
                     traceMessageBuf.append(getPacketDumpToLog(packet, originalPacketLen));
                     this.connection.getLog().logTrace(traceMessageBuf.toString());
                  }
               } else {
                  packet.writeLongInt(packetLen - 4);
                  packet.writeByte(this.packetSequence);
                  if (this.traceProtocol) {
                     StringBuffer traceMessageBuf = new StringBuffer();
                     traceMessageBuf.append(Messages.getString("MysqlIO.59"));
                     traceMessageBuf.append(packet.dump(packetLen));
                     this.connection.getLog().logTrace(traceMessageBuf.toString());
                  }
               }

               this.mysqlOutput.write(packetToSend.getByteBuffer(), 0, packetLen);
               this.mysqlOutput.flush();
            }

            if (this.enablePacketDebug) {
               this.enqueuePacketForDebugging(true, false, packetLen + 5, this.packetHeaderBuf, packet);
            }

            if (packet == this.sharedSendPacket) {
               this.reclaimLargeSharedSendPacket();
            }

            if (this.connection.getMaintainTimeStats()) {
               this.lastPacketSentTimeMs = System.currentTimeMillis();
            }

         }
      } catch (IOException ioEx) {
         throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ioEx);
      }
   }

   private final ResultSetImpl sendFileToServer(StatementImpl callingStatement, String fileName) throws SQLException {
      Buffer filePacket = this.loadFileBufRef == null ? null : (Buffer)this.loadFileBufRef.get();
      int bigPacketLength = Math.min(this.connection.getMaxAllowedPacket() - 12, this.alignPacketSize(this.connection.getMaxAllowedPacket() - 16, 4096) - 12);
      int oneMeg = 1048576;
      int smallerPacketSizeAligned = Math.min(oneMeg - 12, this.alignPacketSize(oneMeg - 16, 4096) - 12);
      int packetLength = Math.min(smallerPacketSizeAligned, bigPacketLength);
      if (filePacket == null) {
         try {
            filePacket = new Buffer(packetLength + 4);
            this.loadFileBufRef = new SoftReference(filePacket);
         } catch (OutOfMemoryError var24) {
            throw SQLError.createSQLException("Could not allocate packet of " + packetLength + " bytes required for LOAD DATA LOCAL INFILE operation." + " Try increasing max heap allocation for JVM or decreasing server variable " + "'max_allowed_packet'", "S1001");
         }
      }

      filePacket.clear();
      this.send(filePacket, 0);
      byte[] fileBuf = new byte[packetLength];
      BufferedInputStream fileIn = null;

      try {
         if (!this.connection.getAllowLoadLocalInfile()) {
            throw SQLError.createSQLException(Messages.getString("MysqlIO.LoadDataLocalNotAllowed"), "S1000");
         }

         InputStream hookedStream = null;
         if (callingStatement != null) {
            hookedStream = callingStatement.getLocalInfileInputStream();
         }

         if (hookedStream != null) {
            fileIn = new BufferedInputStream(hookedStream);
         } else if (!this.connection.getAllowUrlInLocalInfile()) {
            fileIn = new BufferedInputStream(new FileInputStream(fileName));
         } else if (fileName.indexOf(58) != -1) {
            try {
               URL urlFromFileName = new URL(fileName);
               fileIn = new BufferedInputStream(urlFromFileName.openStream());
            } catch (MalformedURLException var23) {
               fileIn = new BufferedInputStream(new FileInputStream(fileName));
            }
         } else {
            fileIn = new BufferedInputStream(new FileInputStream(fileName));
         }

         int bytesRead = 0;

         while((bytesRead = fileIn.read(fileBuf)) != -1) {
            filePacket.clear();
            filePacket.writeBytesNoNull(fileBuf, 0, bytesRead);
            this.send(filePacket, filePacket.getPosition());
         }
      } catch (IOException ioEx) {
         StringBuffer messageBuf = new StringBuffer(Messages.getString("MysqlIO.60"));
         if (!this.connection.getParanoid()) {
            messageBuf.append("'");
            if (fileName != null) {
               messageBuf.append(fileName);
            }

            messageBuf.append("'");
         }

         messageBuf.append(Messages.getString("MysqlIO.63"));
         if (!this.connection.getParanoid()) {
            messageBuf.append(Messages.getString("MysqlIO.64"));
            messageBuf.append(Util.stackTraceToString(ioEx));
         }

         throw SQLError.createSQLException(messageBuf.toString(), "S1009");
      } finally {
         if (fileIn != null) {
            try {
               fileIn.close();
            } catch (Exception ex) {
               SQLException sqlEx = SQLError.createSQLException(Messages.getString("MysqlIO.65"), "S1000");
               sqlEx.initCause(ex);
               throw sqlEx;
            }

            BufferedInputStream var27 = null;
         } else {
            filePacket.clear();
            this.send(filePacket, filePacket.getPosition());
            this.checkErrorPacket();
         }

      }

      filePacket.clear();
      this.send(filePacket, filePacket.getPosition());
      Buffer resultPacket = this.checkErrorPacket();
      return this.buildResultSetWithUpdates(callingStatement, resultPacket);
   }

   private Buffer checkErrorPacket(int command) throws SQLException {
      int statusCode = 0;
      Buffer resultPacket = null;
      this.serverStatus = 0;

      try {
         resultPacket = this.reuseAndReadPacket(this.reusablePacket);
      } catch (SQLException sqlEx) {
         throw sqlEx;
      } catch (Exception fallThru) {
         throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, fallThru);
      }

      this.checkErrorPacket(resultPacket);
      return resultPacket;
   }

   private void checkErrorPacket(Buffer resultPacket) throws SQLException {
      int statusCode = resultPacket.readByte();
      if (statusCode == -1) {
         int errno = 2000;
         if (this.protocolVersion > 9) {
            errno = resultPacket.readInt();
            String xOpen = null;
            String serverErrorMessage = resultPacket.readString(this.connection.getErrorMessageEncoding());
            if (serverErrorMessage.charAt(0) == '#') {
               if (serverErrorMessage.length() > 6) {
                  xOpen = serverErrorMessage.substring(1, 6);
                  serverErrorMessage = serverErrorMessage.substring(6);
                  if (xOpen.equals("HY000")) {
                     xOpen = SQLError.mysqlToSqlState(errno, this.connection.getUseSqlStateCodes());
                  }
               } else {
                  xOpen = SQLError.mysqlToSqlState(errno, this.connection.getUseSqlStateCodes());
               }
            } else {
               xOpen = SQLError.mysqlToSqlState(errno, this.connection.getUseSqlStateCodes());
            }

            this.clearInputStream();
            StringBuffer errorBuf = new StringBuffer();
            String xOpenErrorMessage = SQLError.get(xOpen);
            if (!this.connection.getUseOnlyServerErrorMessages() && xOpenErrorMessage != null) {
               errorBuf.append(xOpenErrorMessage);
               errorBuf.append(Messages.getString("MysqlIO.68"));
            }

            errorBuf.append(serverErrorMessage);
            if (!this.connection.getUseOnlyServerErrorMessages() && xOpenErrorMessage != null) {
               errorBuf.append("\"");
            }

            this.appendInnodbStatusInformation(xOpen, errorBuf);
            if (xOpen != null && xOpen.startsWith("22")) {
               throw new MysqlDataTruncation(errorBuf.toString(), 0, true, false, 0, 0);
            } else {
               throw SQLError.createSQLException(errorBuf.toString(), xOpen, errno);
            }
         } else {
            String serverErrorMessage = resultPacket.readString(this.connection.getErrorMessageEncoding());
            this.clearInputStream();
            if (serverErrorMessage.indexOf(Messages.getString("MysqlIO.70")) != -1) {
               throw SQLError.createSQLException(SQLError.get("S0022") + ", " + serverErrorMessage, "S0022", -1);
            } else {
               StringBuffer errorBuf = new StringBuffer(Messages.getString("MysqlIO.72"));
               errorBuf.append(serverErrorMessage);
               errorBuf.append("\"");
               throw SQLError.createSQLException(SQLError.get("S1000") + ", " + errorBuf.toString(), "S1000", -1);
            }
         }
      }
   }

   private void appendInnodbStatusInformation(String xOpen, StringBuffer errorBuf) throws SQLException {
      if (this.connection.getIncludeInnodbStatusInDeadlockExceptions() && xOpen != null && (xOpen.startsWith("40") || xOpen.startsWith("41")) && this.streamingData == null) {
         ResultSet rs = null;

         try {
            rs = this.sqlQueryDirect((StatementImpl)null, "SHOW ENGINE INNODB STATUS", this.connection.getEncoding(), (Buffer)null, -1, 1003, 1007, false, this.connection.getCatalog(), (Field[])null);
            if (rs.next()) {
               errorBuf.append("\n\n");
               errorBuf.append(rs.getString(1));
            } else {
               errorBuf.append(Messages.getString("MysqlIO.NoInnoDBStatusFound"));
            }
         } catch (Exception ex) {
            errorBuf.append(Messages.getString("MysqlIO.InnoDBStatusFailed"));
            errorBuf.append("\n\n");
            errorBuf.append(Util.stackTraceToString(ex));
         } finally {
            if (rs != null) {
               rs.close();
            }

         }
      }

   }

   private final void sendSplitPackets(Buffer packet) throws SQLException {
      try {
         Buffer headerPacket = this.splitBufRef == null ? null : (Buffer)this.splitBufRef.get();
         if (headerPacket == null) {
            headerPacket = new Buffer(this.maxThreeBytes + 4);
            this.splitBufRef = new SoftReference(headerPacket);
         }

         int len = packet.getPosition();
         int splitSize = this.maxThreeBytes;
         int originalPacketPos = 4;
         byte[] origPacketBytes = packet.getByteBuffer();

         byte[] headerPacketBytes;
         for(headerPacketBytes = headerPacket.getByteBuffer(); len >= this.maxThreeBytes; len -= splitSize) {
            ++this.packetSequence;
            headerPacket.setPosition(0);
            headerPacket.writeLongInt(splitSize);
            headerPacket.writeByte(this.packetSequence);
            System.arraycopy(origPacketBytes, originalPacketPos, headerPacketBytes, 4, splitSize);
            int packetLen = splitSize + 4;
            if (!this.useCompression) {
               this.mysqlOutput.write(headerPacketBytes, 0, splitSize + 4);
               this.mysqlOutput.flush();
            } else {
               headerPacket.setPosition(0);
               Buffer packetToSend = this.compressPacket(headerPacket, 4, splitSize, 4);
               packetLen = packetToSend.getPosition();
               this.mysqlOutput.write(packetToSend.getByteBuffer(), 0, packetLen);
               this.mysqlOutput.flush();
            }

            originalPacketPos += splitSize;
         }

         headerPacket.clear();
         headerPacket.setPosition(0);
         headerPacket.writeLongInt(len - 4);
         ++this.packetSequence;
         headerPacket.writeByte(this.packetSequence);
         if (len != 0) {
            System.arraycopy(origPacketBytes, originalPacketPos, headerPacketBytes, 4, len - 4);
         }

         int packetLen = len - 4;
         if (!this.useCompression) {
            this.mysqlOutput.write(headerPacket.getByteBuffer(), 0, len);
            this.mysqlOutput.flush();
         } else {
            headerPacket.setPosition(0);
            Buffer packetToSend = this.compressPacket(headerPacket, 4, packetLen, 4);
            packetLen = packetToSend.getPosition();
            this.mysqlOutput.write(packetToSend.getByteBuffer(), 0, packetLen);
            this.mysqlOutput.flush();
         }

      } catch (IOException ioEx) {
         throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ioEx);
      }
   }

   private void reclaimLargeSharedSendPacket() {
      if (this.sharedSendPacket != null && this.sharedSendPacket.getCapacity() > 1048576) {
         this.sharedSendPacket = new Buffer(1024);
      }

   }

   boolean hadWarnings() {
      return this.hadWarnings;
   }

   void scanForAndThrowDataTruncation() throws SQLException {
      if (this.streamingData == null && this.versionMeetsMinimum(4, 1, 0) && this.connection.getJdbcCompliantTruncation() && this.warningCount > 0) {
         SQLError.convertShowWarningsToSQLWarnings(this.connection, this.warningCount, true);
      }

   }

   private void secureAuth(Buffer packet, int packLength, String user, String password, String database, boolean writeClientParams) throws SQLException {
      if (packet == null) {
         packet = new Buffer(packLength);
      }

      if (writeClientParams) {
         if (this.use41Extensions) {
            if (this.versionMeetsMinimum(4, 1, 1)) {
               packet.writeLong(this.clientParam);
               packet.writeLong((long)this.maxThreeBytes);
               packet.writeByte((byte)8);
               packet.writeBytesNoNull(new byte[23]);
            } else {
               packet.writeLong(this.clientParam);
               packet.writeLong((long)this.maxThreeBytes);
            }
         } else {
            packet.writeInt((int)this.clientParam);
            packet.writeLongInt(this.maxThreeBytes);
         }
      }

      packet.writeString(user, "Cp1252", this.connection);
      if (password.length() != 0) {
         packet.writeString("xxxxxxxx", "Cp1252", this.connection);
      } else {
         packet.writeString("", "Cp1252", this.connection);
      }

      if (this.useConnectWithDb) {
         packet.writeString(database, "Cp1252", this.connection);
      }

      this.send(packet, packet.getPosition());
      if (password.length() > 0) {
         Buffer b = this.readPacket();
         b.setPosition(0);
         byte[] replyAsBytes = b.getByteBuffer();
         if (replyAsBytes.length == 25 && replyAsBytes[0] != 0) {
            if (replyAsBytes[0] != 42) {
               try {
                  byte[] buff = Security.passwordHashStage1(password);
                  byte[] passwordHash = new byte[buff.length];
                  System.arraycopy(buff, 0, passwordHash, 0, buff.length);
                  passwordHash = Security.passwordHashStage2(passwordHash, replyAsBytes);
                  byte[] packetDataAfterSalt = new byte[replyAsBytes.length - 5];
                  System.arraycopy(replyAsBytes, 4, packetDataAfterSalt, 0, replyAsBytes.length - 5);
                  byte[] mysqlScrambleBuff = new byte[20];
                  Security.passwordCrypt(packetDataAfterSalt, mysqlScrambleBuff, passwordHash, 20);
                  Security.passwordCrypt(mysqlScrambleBuff, buff, buff, 20);
                  Buffer packet2 = new Buffer(25);
                  packet2.writeBytesNoNull(buff);
                  ++this.packetSequence;
                  this.send(packet2, 24);
               } catch (NoSuchAlgorithmException var15) {
                  throw SQLError.createSQLException(Messages.getString("MysqlIO.91") + Messages.getString("MysqlIO.92"), "S1000");
               }
            } else {
               try {
                  byte[] passwordHash = Security.createKeyFromOldPassword(password);
                  byte[] netReadPos4 = new byte[replyAsBytes.length - 5];
                  System.arraycopy(replyAsBytes, 4, netReadPos4, 0, replyAsBytes.length - 5);
                  byte[] mysqlScrambleBuff = new byte[20];
                  Security.passwordCrypt(netReadPos4, mysqlScrambleBuff, passwordHash, 20);
                  String scrambledPassword = Util.scramble(new String(mysqlScrambleBuff), password);
                  Buffer packet2 = new Buffer(packLength);
                  packet2.writeString(scrambledPassword, "Cp1252", this.connection);
                  ++this.packetSequence;
                  this.send(packet2, 24);
               } catch (NoSuchAlgorithmException var14) {
                  throw SQLError.createSQLException(Messages.getString("MysqlIO.93") + Messages.getString("MysqlIO.94"), "S1000");
               }
            }
         }
      }

   }

   void secureAuth411(Buffer packet, int packLength, String user, String password, String database, boolean writeClientParams) throws SQLException {
      if (packet == null) {
         packet = new Buffer(packLength);
      }

      if (writeClientParams) {
         if (this.use41Extensions) {
            if (this.versionMeetsMinimum(4, 1, 1)) {
               packet.writeLong(this.clientParam);
               packet.writeLong((long)this.maxThreeBytes);
               packet.writeByte((byte)33);
               packet.writeBytesNoNull(new byte[23]);
            } else {
               packet.writeLong(this.clientParam);
               packet.writeLong((long)this.maxThreeBytes);
            }
         } else {
            packet.writeInt((int)this.clientParam);
            packet.writeLongInt(this.maxThreeBytes);
         }
      }

      packet.writeString(user, "utf-8", this.connection);
      if (password.length() != 0) {
         packet.writeByte((byte)20);

         try {
            packet.writeBytesNoNull(Security.scramble411(password, this.seed));
         } catch (NoSuchAlgorithmException var10) {
            throw SQLError.createSQLException(Messages.getString("MysqlIO.95") + Messages.getString("MysqlIO.96"), "S1000");
         }
      } else {
         packet.writeByte((byte)0);
      }

      if (this.useConnectWithDb) {
         packet.writeString(database, "utf-8", this.connection);
      }

      this.send(packet, packet.getPosition());
      byte var10002 = this.packetSequence;
      this.packetSequence = (byte)(var10002 + 1);
      byte savePacketSequence = var10002;
      Buffer reply = this.checkErrorPacket();
      if (reply.isLastDataPacket()) {
         ++savePacketSequence;
         this.packetSequence = savePacketSequence;
         packet.clear();
         String seed323 = this.seed.substring(0, 8);
         packet.writeString(Util.newCrypt(password, seed323));
         this.send(packet, packet.getPosition());
         this.checkErrorPacket();
      }

   }

   private final ResultSetRow unpackBinaryResultSetRow(Field[] fields, Buffer binaryData, int resultSetConcurrency) throws SQLException {
      int numFields = fields.length;
      byte[][] unpackedRowData = new byte[numFields][];
      int nullCount = (numFields + 9) / 8;
      byte[] nullBitMask = new byte[nullCount];

      for(int i = 0; i < nullCount; ++i) {
         nullBitMask[i] = binaryData.readByte();
      }

      int nullMaskPos = 0;
      int bit = 4;

      for(int i = 0; i < numFields; ++i) {
         if ((nullBitMask[nullMaskPos] & bit) != 0) {
            unpackedRowData[i] = null;
         } else if (resultSetConcurrency != 1008) {
            this.extractNativeEncodedColumn(binaryData, fields, i, unpackedRowData);
         } else {
            this.unpackNativeEncodedColumn(binaryData, fields, i, unpackedRowData);
         }

         if (((bit <<= 1) & 255) == 0) {
            bit = 1;
            ++nullMaskPos;
         }
      }

      return new ByteArrayRow(unpackedRowData);
   }

   private final void extractNativeEncodedColumn(Buffer binaryData, Field[] fields, int columnIndex, byte[][] unpackedRowData) throws SQLException {
      Field curField = fields[columnIndex];
      switch (curField.getMysqlType()) {
         case 0:
         case 15:
         case 246:
         case 249:
         case 250:
         case 251:
         case 252:
         case 253:
         case 254:
         case 255:
            unpackedRowData[columnIndex] = binaryData.readLenByteArray(0);
            break;
         case 1:
            unpackedRowData[columnIndex] = new byte[]{binaryData.readByte()};
            break;
         case 2:
         case 13:
            unpackedRowData[columnIndex] = binaryData.getBytes(2);
            break;
         case 3:
         case 9:
            unpackedRowData[columnIndex] = binaryData.getBytes(4);
            break;
         case 4:
            unpackedRowData[columnIndex] = binaryData.getBytes(4);
            break;
         case 5:
            unpackedRowData[columnIndex] = binaryData.getBytes(8);
         case 6:
            break;
         case 7:
         case 12:
            int length = (int)binaryData.readFieldLength();
            unpackedRowData[columnIndex] = binaryData.getBytes(length);
            break;
         case 8:
            unpackedRowData[columnIndex] = binaryData.getBytes(8);
            break;
         case 10:
            int length = (int)binaryData.readFieldLength();
            unpackedRowData[columnIndex] = binaryData.getBytes(length);
            break;
         case 11:
            int length = (int)binaryData.readFieldLength();
            unpackedRowData[columnIndex] = binaryData.getBytes(length);
            break;
         case 16:
            unpackedRowData[columnIndex] = binaryData.readLenByteArray(0);
            break;
         default:
            throw SQLError.createSQLException(Messages.getString("MysqlIO.97") + curField.getMysqlType() + Messages.getString("MysqlIO.98") + columnIndex + Messages.getString("MysqlIO.99") + fields.length + Messages.getString("MysqlIO.100"), "S1000");
      }

   }

   private final void unpackNativeEncodedColumn(Buffer binaryData, Field[] fields, int columnIndex, byte[][] unpackedRowData) throws SQLException {
      Field curField = fields[columnIndex];
      switch (curField.getMysqlType()) {
         case 0:
         case 15:
         case 16:
         case 246:
         case 249:
         case 250:
         case 251:
         case 252:
         case 253:
         case 254:
            unpackedRowData[columnIndex] = binaryData.readLenByteArray(0);
            break;
         case 1:
            byte tinyVal = binaryData.readByte();
            if (!curField.isUnsigned()) {
               unpackedRowData[columnIndex] = String.valueOf(tinyVal).getBytes();
            } else {
               short unsignedTinyVal = (short)(tinyVal & 255);
               unpackedRowData[columnIndex] = String.valueOf(unsignedTinyVal).getBytes();
            }
            break;
         case 2:
         case 13:
            short shortVal = (short)binaryData.readInt();
            if (!curField.isUnsigned()) {
               unpackedRowData[columnIndex] = String.valueOf(shortVal).getBytes();
            } else {
               int unsignedShortVal = shortVal & '\uffff';
               unpackedRowData[columnIndex] = String.valueOf(unsignedShortVal).getBytes();
            }
            break;
         case 3:
         case 9:
            int intVal = (int)binaryData.readLong();
            if (!curField.isUnsigned()) {
               unpackedRowData[columnIndex] = String.valueOf(intVal).getBytes();
            } else {
               long longVal = (long)intVal & 4294967295L;
               unpackedRowData[columnIndex] = String.valueOf(longVal).getBytes();
            }
            break;
         case 4:
            float floatVal = Float.intBitsToFloat(binaryData.readIntAsLong());
            unpackedRowData[columnIndex] = String.valueOf(floatVal).getBytes();
            break;
         case 5:
            double doubleVal = Double.longBitsToDouble(binaryData.readLongLong());
            unpackedRowData[columnIndex] = String.valueOf(doubleVal).getBytes();
         case 6:
            break;
         case 7:
         case 12:
            int length = (int)binaryData.readFieldLength();
            int year = 0;
            int month = 0;
            int day = 0;
            int hour = 0;
            int minute = 0;
            int seconds = 0;
            int nanos = 0;
            if (length != 0) {
               year = binaryData.readInt();
               month = binaryData.readByte();
               day = binaryData.readByte();
               if (length > 4) {
                  hour = binaryData.readByte();
                  minute = binaryData.readByte();
                  seconds = binaryData.readByte();
               }
            }

            if (year == 0 && month == 0 && day == 0) {
               if ("convertToNull".equals(this.connection.getZeroDateTimeBehavior())) {
                  unpackedRowData[columnIndex] = null;
                  break;
               }

               if ("exception".equals(this.connection.getZeroDateTimeBehavior())) {
                  throw SQLError.createSQLException("Value '0000-00-00' can not be represented as java.sql.Timestamp", "S1009");
               }

               year = 1;
               month = 1;
               day = 1;
            }

            int stringLength = 19;
            byte[] nanosAsBytes = Integer.toString(nanos).getBytes();
            stringLength += 1 + nanosAsBytes.length;
            byte[] datetimeAsBytes = new byte[stringLength];
            datetimeAsBytes[0] = (byte)Character.forDigit(year / 1000, 10);
            int after1000 = year % 1000;
            datetimeAsBytes[1] = (byte)Character.forDigit(after1000 / 100, 10);
            int after100 = after1000 % 100;
            datetimeAsBytes[2] = (byte)Character.forDigit(after100 / 10, 10);
            datetimeAsBytes[3] = (byte)Character.forDigit(after100 % 10, 10);
            datetimeAsBytes[4] = 45;
            datetimeAsBytes[5] = (byte)Character.forDigit(month / 10, 10);
            datetimeAsBytes[6] = (byte)Character.forDigit(month % 10, 10);
            datetimeAsBytes[7] = 45;
            datetimeAsBytes[8] = (byte)Character.forDigit(day / 10, 10);
            datetimeAsBytes[9] = (byte)Character.forDigit(day % 10, 10);
            datetimeAsBytes[10] = 32;
            datetimeAsBytes[11] = (byte)Character.forDigit(hour / 10, 10);
            datetimeAsBytes[12] = (byte)Character.forDigit(hour % 10, 10);
            datetimeAsBytes[13] = 58;
            datetimeAsBytes[14] = (byte)Character.forDigit(minute / 10, 10);
            datetimeAsBytes[15] = (byte)Character.forDigit(minute % 10, 10);
            datetimeAsBytes[16] = 58;
            datetimeAsBytes[17] = (byte)Character.forDigit(seconds / 10, 10);
            datetimeAsBytes[18] = (byte)Character.forDigit(seconds % 10, 10);
            datetimeAsBytes[19] = 46;
            int nanosOffset = 20;

            for(int j = 0; j < nanosAsBytes.length; ++j) {
               datetimeAsBytes[nanosOffset + j] = nanosAsBytes[j];
            }

            unpackedRowData[columnIndex] = datetimeAsBytes;
            break;
         case 8:
            long longVal = binaryData.readLongLong();
            if (!curField.isUnsigned()) {
               unpackedRowData[columnIndex] = String.valueOf(longVal).getBytes();
            } else {
               BigInteger asBigInteger = ResultSetImpl.convertLongToUlong(longVal);
               unpackedRowData[columnIndex] = asBigInteger.toString().getBytes();
            }
            break;
         case 10:
            int length = (int)binaryData.readFieldLength();
            int year = 0;
            int month = 0;
            int day = 0;
            int hour = 0;
            int minute = 0;
            int seconds = 0;
            if (length != 0) {
               year = binaryData.readInt();
               month = binaryData.readByte();
               day = binaryData.readByte();
            }

            if (year == 0 && month == 0 && day == 0) {
               if ("convertToNull".equals(this.connection.getZeroDateTimeBehavior())) {
                  unpackedRowData[columnIndex] = null;
                  break;
               }

               if ("exception".equals(this.connection.getZeroDateTimeBehavior())) {
                  throw SQLError.createSQLException("Value '0000-00-00' can not be represented as java.sql.Date", "S1009");
               }

               year = 1;
               month = 1;
               day = 1;
            }

            byte[] dateAsBytes = new byte[10];
            dateAsBytes[0] = (byte)Character.forDigit(year / 1000, 10);
            int after1000 = year % 1000;
            dateAsBytes[1] = (byte)Character.forDigit(after1000 / 100, 10);
            int after100 = after1000 % 100;
            dateAsBytes[2] = (byte)Character.forDigit(after100 / 10, 10);
            dateAsBytes[3] = (byte)Character.forDigit(after100 % 10, 10);
            dateAsBytes[4] = 45;
            dateAsBytes[5] = (byte)Character.forDigit(month / 10, 10);
            dateAsBytes[6] = (byte)Character.forDigit(month % 10, 10);
            dateAsBytes[7] = 45;
            dateAsBytes[8] = (byte)Character.forDigit(day / 10, 10);
            dateAsBytes[9] = (byte)Character.forDigit(day % 10, 10);
            unpackedRowData[columnIndex] = dateAsBytes;
            break;
         case 11:
            int length = (int)binaryData.readFieldLength();
            int hour = 0;
            int minute = 0;
            int seconds = 0;
            if (length != 0) {
               binaryData.readByte();
               binaryData.readLong();
               hour = binaryData.readByte();
               minute = binaryData.readByte();
               seconds = binaryData.readByte();
               if (length > 8) {
                  binaryData.readLong();
               }
            }

            byte[] timeAsBytes = new byte[]{(byte)Character.forDigit(hour / 10, 10), (byte)Character.forDigit(hour % 10, 10), 58, (byte)Character.forDigit(minute / 10, 10), (byte)Character.forDigit(minute % 10, 10), 58, (byte)Character.forDigit(seconds / 10, 10), (byte)Character.forDigit(seconds % 10, 10)};
            unpackedRowData[columnIndex] = timeAsBytes;
            break;
         default:
            throw SQLError.createSQLException(Messages.getString("MysqlIO.97") + curField.getMysqlType() + Messages.getString("MysqlIO.98") + columnIndex + Messages.getString("MysqlIO.99") + fields.length + Messages.getString("MysqlIO.100"), "S1000");
      }

   }

   private void negotiateSSLConnection(String user, String password, String database, int packLength) throws SQLException {
      if (!ExportControlled.enabled()) {
         throw new ConnectionFeatureNotAvailableException(this.connection, this.lastPacketSentTimeMs, (Exception)null);
      } else {
         boolean doSecureAuth = false;
         if ((this.serverCapabilities & '耀') != 0) {
            this.clientParam |= 32768L;
            doSecureAuth = true;
         }

         this.clientParam |= 2048L;
         Buffer packet = new Buffer(packLength);
         if (this.use41Extensions) {
            packet.writeLong(this.clientParam);
         } else {
            packet.writeInt((int)this.clientParam);
         }

         this.send(packet, packet.getPosition());
         ExportControlled.transformSocketToSSLSocket(this);
         packet.clear();
         if (doSecureAuth) {
            if (this.versionMeetsMinimum(4, 1, 1)) {
               this.secureAuth411((Buffer)null, packLength, user, password, database, true);
            } else {
               this.secureAuth411((Buffer)null, packLength, user, password, database, true);
            }
         } else {
            if (this.use41Extensions) {
               packet.writeLong(this.clientParam);
               packet.writeLong((long)this.maxThreeBytes);
            } else {
               packet.writeInt((int)this.clientParam);
               packet.writeLongInt(this.maxThreeBytes);
            }

            packet.writeString(user);
            if (this.protocolVersion > 9) {
               packet.writeString(Util.newCrypt(password, this.seed));
            } else {
               packet.writeString(Util.oldCrypt(password, this.seed));
            }

            if ((this.serverCapabilities & 8) != 0 && database != null && database.length() > 0) {
               packet.writeString(database);
            }

            this.send(packet, packet.getPosition());
         }

      }
   }

   protected int getServerStatus() {
      return this.serverStatus;
   }

   protected List fetchRowsViaCursor(List fetchedRows, long statementId, Field[] columnTypes, int fetchSize, boolean useBufferRowExplicit) throws SQLException {
      if (fetchedRows == null) {
         fetchedRows = new ArrayList(fetchSize);
      } else {
         fetchedRows.clear();
      }

      this.sharedSendPacket.clear();
      this.sharedSendPacket.writeByte((byte)28);
      this.sharedSendPacket.writeLong(statementId);
      this.sharedSendPacket.writeLong((long)fetchSize);
      this.sendCommand(28, (String)null, this.sharedSendPacket, true, (String)null);
      ResultSetRow row = null;

      while((row = this.nextRow(columnTypes, columnTypes.length, true, 1007, false, useBufferRowExplicit, false, (Buffer)null)) != null) {
         fetchedRows.add(row);
      }

      return fetchedRows;
   }

   protected long getThreadId() {
      return this.threadId;
   }

   protected boolean useNanosForElapsedTime() {
      return this.useNanosForElapsedTime;
   }

   protected long getSlowQueryThreshold() {
      return this.slowQueryThreshold;
   }

   protected String getQueryTimingUnits() {
      return this.queryTimingUnits;
   }

   protected int getCommandCount() {
      return this.commandCount;
   }

   private void checkTransactionState(int oldStatus) throws SQLException {
      boolean previouslyInTrans = (oldStatus & 1) != 0;
      boolean currentlyInTrans = (this.serverStatus & 1) != 0;
      if (previouslyInTrans && !currentlyInTrans) {
         this.connection.transactionCompleted();
      } else if (!previouslyInTrans && currentlyInTrans) {
         this.connection.transactionBegun();
      }

   }

   static {
      OutputStreamWriter outWriter = null;

      try {
         outWriter = new OutputStreamWriter(new ByteArrayOutputStream());
         jvmPlatformCharset = outWriter.getEncoding();
      } finally {
         try {
            if (outWriter != null) {
               outWriter.close();
            }
         } catch (IOException var7) {
         }

      }

   }
}
