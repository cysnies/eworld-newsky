package com.mysql.jdbc;

import com.mysql.jdbc.profiler.ProfilerEvent;
import com.mysql.jdbc.profiler.ProfilerEventHandler;
import com.mysql.jdbc.profiler.ProfilerEventHandlerFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Array;
import java.sql.Date;
import java.sql.Ref;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.TreeMap;

public class ResultSetImpl implements ResultSetInternalMethods {
   private static final Constructor JDBC_4_RS_4_ARG_CTOR;
   private static final Constructor JDBC_4_RS_6_ARG_CTOR;
   private static final Constructor JDBC_4_UPD_RS_6_ARG_CTOR;
   protected static final double MIN_DIFF_PREC;
   protected static final double MAX_DIFF_PREC;
   protected static int resultCounter;
   protected String catalog = null;
   protected Map columnNameToIndex = null;
   protected boolean[] columnUsed = null;
   protected ConnectionImpl connection;
   protected long connectionId = 0L;
   protected int currentRow = -1;
   TimeZone defaultTimeZone;
   protected boolean doingUpdates = false;
   protected ProfilerEventHandler eventSink = null;
   Calendar fastDateCal = null;
   protected int fetchDirection = 1000;
   protected int fetchSize = 0;
   protected Field[] fields;
   protected char firstCharOfQuery;
   protected Map fullColumnNameToIndex = null;
   protected boolean hasBuiltIndexMapping = false;
   protected boolean isBinaryEncoded = false;
   protected boolean isClosed = false;
   protected ResultSetInternalMethods nextResultSet = null;
   protected boolean onInsertRow = false;
   protected StatementImpl owningStatement;
   protected Throwable pointOfOrigin;
   protected boolean profileSql = false;
   protected boolean reallyResult = false;
   protected int resultId;
   protected int resultSetConcurrency = 0;
   protected int resultSetType = 0;
   protected RowData rowData;
   protected String serverInfo = null;
   PreparedStatement statementUsedForFetchingRows;
   protected ResultSetRow thisRow = null;
   protected long updateCount;
   protected long updateId = -1L;
   private boolean useStrictFloatingPoint = false;
   protected boolean useUsageAdvisor = false;
   protected SQLWarning warningChain = null;
   protected boolean wasNullFlag = false;
   protected java.sql.Statement wrapperStatement;
   protected boolean retainOwningStatement;
   protected Calendar gmtCalendar = null;
   protected boolean useFastDateParsing = false;
   private boolean padCharsWithSpace = false;
   private boolean jdbcCompliantTruncationForReads;
   private boolean useFastIntParsing = true;
   protected static final char[] EMPTY_SPACE;
   private boolean onValidRow = false;
   private String invalidRowReason = null;
   protected boolean useLegacyDatetimeCode;
   private TimeZone serverTimeZoneTz;
   // $FF: synthetic field
   static Class class$com$mysql$jdbc$ConnectionImpl;
   // $FF: synthetic field
   static Class class$com$mysql$jdbc$StatementImpl;
   // $FF: synthetic field
   static Class class$java$lang$String;
   // $FF: synthetic field
   static Class array$Lcom$mysql$jdbc$Field;
   // $FF: synthetic field
   static Class class$com$mysql$jdbc$RowData;

   protected static BigInteger convertLongToUlong(long longVal) {
      byte[] asBytes = new byte[8];
      asBytes[7] = (byte)((int)(longVal & 255L));
      asBytes[6] = (byte)((int)(longVal >>> 8));
      asBytes[5] = (byte)((int)(longVal >>> 16));
      asBytes[4] = (byte)((int)(longVal >>> 24));
      asBytes[3] = (byte)((int)(longVal >>> 32));
      asBytes[2] = (byte)((int)(longVal >>> 40));
      asBytes[1] = (byte)((int)(longVal >>> 48));
      asBytes[0] = (byte)((int)(longVal >>> 56));
      return new BigInteger(1, asBytes);
   }

   protected static ResultSetImpl getInstance(long updateCount, long updateID, ConnectionImpl conn, StatementImpl creatorStmt) throws SQLException {
      return !Util.isJdbc4() ? new ResultSetImpl(updateCount, updateID, conn, creatorStmt) : (ResultSetImpl)Util.handleNewInstance(JDBC_4_RS_4_ARG_CTOR, new Object[]{Constants.longValueOf(updateCount), Constants.longValueOf(updateID), conn, creatorStmt});
   }

   protected static ResultSetImpl getInstance(String catalog, Field[] fields, RowData tuples, ConnectionImpl conn, StatementImpl creatorStmt, boolean isUpdatable) throws SQLException {
      if (!Util.isJdbc4()) {
         return (ResultSetImpl)(!isUpdatable ? new ResultSetImpl(catalog, fields, tuples, conn, creatorStmt) : new UpdatableResultSet(catalog, fields, tuples, conn, creatorStmt));
      } else {
         return !isUpdatable ? (ResultSetImpl)Util.handleNewInstance(JDBC_4_RS_6_ARG_CTOR, new Object[]{catalog, fields, tuples, conn, creatorStmt}) : (ResultSetImpl)Util.handleNewInstance(JDBC_4_UPD_RS_6_ARG_CTOR, new Object[]{catalog, fields, tuples, conn, creatorStmt});
      }
   }

   public ResultSetImpl(long updateCount, long updateID, ConnectionImpl conn, StatementImpl creatorStmt) {
      this.updateCount = updateCount;
      this.updateId = updateID;
      this.reallyResult = false;
      this.fields = new Field[0];
      this.connection = conn;
      this.owningStatement = creatorStmt;
      this.retainOwningStatement = false;
      if (this.connection != null) {
         this.retainOwningStatement = this.connection.getRetainStatementAfterResultSetClose();
         this.connectionId = this.connection.getId();
         this.serverTimeZoneTz = this.connection.getServerTimezoneTZ();
      }

      this.useLegacyDatetimeCode = this.connection.getUseLegacyDatetimeCode();
   }

   public ResultSetImpl(String catalog, Field[] fields, RowData tuples, ConnectionImpl conn, StatementImpl creatorStmt) throws SQLException {
      this.connection = conn;
      this.retainOwningStatement = false;
      if (this.connection != null) {
         this.useStrictFloatingPoint = this.connection.getStrictFloatingPoint();
         this.setDefaultTimeZone(this.connection.getDefaultTimeZone());
         this.connectionId = this.connection.getId();
         this.useFastDateParsing = this.connection.getUseFastDateParsing();
         this.profileSql = this.connection.getProfileSql();
         this.retainOwningStatement = this.connection.getRetainStatementAfterResultSetClose();
         this.jdbcCompliantTruncationForReads = this.connection.getJdbcCompliantTruncationForReads();
         this.useFastIntParsing = this.connection.getUseFastIntParsing();
         this.serverTimeZoneTz = this.connection.getServerTimezoneTZ();
      }

      this.owningStatement = creatorStmt;
      this.catalog = catalog;
      this.fields = fields;
      this.rowData = tuples;
      this.updateCount = (long)this.rowData.size();
      this.reallyResult = true;
      if (this.rowData.size() > 0) {
         if (this.updateCount == 1L && this.thisRow == null) {
            this.rowData.close();
            this.updateCount = -1L;
         }
      } else {
         this.thisRow = null;
      }

      this.rowData.setOwner(this);
      if (this.fields != null) {
         this.initializeWithMetadata();
      }

      this.useLegacyDatetimeCode = this.connection.getUseLegacyDatetimeCode();
   }

   public void initializeWithMetadata() throws SQLException {
      this.rowData.setMetadata(this.fields);
      if (this.profileSql || this.connection.getUseUsageAdvisor()) {
         this.columnUsed = new boolean[this.fields.length];
         this.pointOfOrigin = new Throwable();
         this.resultId = resultCounter++;
         this.useUsageAdvisor = this.connection.getUseUsageAdvisor();
         this.eventSink = ProfilerEventHandlerFactory.getInstance(this.connection);
      }

      if (this.connection.getGatherPerformanceMetrics()) {
         this.connection.incrementNumberOfResultSetsCreated();
         Map tableNamesMap = new HashMap();

         for(int i = 0; i < this.fields.length; ++i) {
            Field f = this.fields[i];
            String tableName = f.getOriginalTableName();
            if (tableName == null) {
               tableName = f.getTableName();
            }

            if (tableName != null) {
               if (this.connection.lowerCaseTableNames()) {
                  tableName = tableName.toLowerCase();
               }

               tableNamesMap.put(tableName, (Object)null);
            }
         }

         this.connection.reportNumberOfTablesAccessed(tableNamesMap.size());
      }

   }

   private synchronized void createCalendarIfNeeded() {
      if (this.fastDateCal == null) {
         this.fastDateCal = new GregorianCalendar(Locale.US);
         this.fastDateCal.setTimeZone(this.getDefaultTimeZone());
      }

   }

   public boolean absolute(int row) throws SQLException {
      this.checkClosed();
      boolean b;
      if (this.rowData.size() == 0) {
         b = false;
      } else {
         if (row == 0) {
            throw SQLError.createSQLException(Messages.getString("ResultSet.Cannot_absolute_position_to_row_0_110"), "S1009");
         }

         if (this.onInsertRow) {
            this.onInsertRow = false;
         }

         if (this.doingUpdates) {
            this.doingUpdates = false;
         }

         if (this.thisRow != null) {
            this.thisRow.closeOpenStreams();
         }

         if (row == 1) {
            b = this.first();
         } else if (row == -1) {
            b = this.last();
         } else if (row > this.rowData.size()) {
            this.afterLast();
            b = false;
         } else if (row < 0) {
            int newRowPosition = this.rowData.size() + row + 1;
            if (newRowPosition <= 0) {
               this.beforeFirst();
               b = false;
            } else {
               b = this.absolute(newRowPosition);
            }
         } else {
            --row;
            this.rowData.setCurrentRow(row);
            this.thisRow = this.rowData.getAt(row);
            b = true;
         }
      }

      this.setRowPositionValidity();
      return b;
   }

   public void afterLast() throws SQLException {
      this.checkClosed();
      if (this.onInsertRow) {
         this.onInsertRow = false;
      }

      if (this.doingUpdates) {
         this.doingUpdates = false;
      }

      if (this.thisRow != null) {
         this.thisRow.closeOpenStreams();
      }

      if (this.rowData.size() != 0) {
         this.rowData.afterLast();
         this.thisRow = null;
      }

      this.setRowPositionValidity();
   }

   public void beforeFirst() throws SQLException {
      this.checkClosed();
      if (this.onInsertRow) {
         this.onInsertRow = false;
      }

      if (this.doingUpdates) {
         this.doingUpdates = false;
      }

      if (this.rowData.size() != 0) {
         if (this.thisRow != null) {
            this.thisRow.closeOpenStreams();
         }

         this.rowData.beforeFirst();
         this.thisRow = null;
         this.setRowPositionValidity();
      }
   }

   public void buildIndexMapping() throws SQLException {
      int numFields = this.fields.length;
      this.columnNameToIndex = new TreeMap(String.CASE_INSENSITIVE_ORDER);
      this.fullColumnNameToIndex = new TreeMap(String.CASE_INSENSITIVE_ORDER);

      for(int i = numFields - 1; i >= 0; --i) {
         Integer index = Constants.integerValueOf(i);
         String columnName = this.fields[i].getName();
         String fullColumnName = this.fields[i].getFullName();
         if (columnName != null) {
            this.columnNameToIndex.put(columnName, index);
         }

         if (fullColumnName != null) {
            this.fullColumnNameToIndex.put(fullColumnName, index);
         }
      }

      this.hasBuiltIndexMapping = true;
   }

   public void cancelRowUpdates() throws SQLException {
      throw new NotUpdatable();
   }

   protected final void checkClosed() throws SQLException {
      if (this.isClosed) {
         throw SQLError.createSQLException(Messages.getString("ResultSet.Operation_not_allowed_after_ResultSet_closed_144"), "S1000");
      }
   }

   protected final void checkColumnBounds(int columnIndex) throws SQLException {
      if (columnIndex < 1) {
         throw SQLError.createSQLException(Messages.getString("ResultSet.Column_Index_out_of_range_low", new Object[]{Constants.integerValueOf(columnIndex), Constants.integerValueOf(this.fields.length)}), "S1009");
      } else if (columnIndex > this.fields.length) {
         throw SQLError.createSQLException(Messages.getString("ResultSet.Column_Index_out_of_range_high", new Object[]{Constants.integerValueOf(columnIndex), Constants.integerValueOf(this.fields.length)}), "S1009");
      } else {
         if (this.profileSql || this.useUsageAdvisor) {
            this.columnUsed[columnIndex - 1] = true;
         }

      }
   }

   protected void checkRowPos() throws SQLException {
      this.checkClosed();
      if (!this.onValidRow) {
         throw SQLError.createSQLException(this.invalidRowReason, "S1000");
      }
   }

   private void setRowPositionValidity() throws SQLException {
      if (!this.rowData.isDynamic() && this.rowData.size() == 0) {
         this.invalidRowReason = Messages.getString("ResultSet.Illegal_operation_on_empty_result_set");
         this.onValidRow = false;
      } else if (this.rowData.isBeforeFirst()) {
         this.invalidRowReason = Messages.getString("ResultSet.Before_start_of_result_set_146");
         this.onValidRow = false;
      } else if (this.rowData.isAfterLast()) {
         this.invalidRowReason = Messages.getString("ResultSet.After_end_of_result_set_148");
         this.onValidRow = false;
      } else {
         this.onValidRow = true;
         this.invalidRowReason = null;
      }

   }

   public void clearNextResult() {
      this.nextResultSet = null;
   }

   public void clearWarnings() throws SQLException {
      this.warningChain = null;
   }

   public void close() throws SQLException {
      this.realClose(true);
   }

   private int convertToZeroWithEmptyCheck() throws SQLException {
      if (this.connection.getEmptyStringsConvertToZero()) {
         return 0;
      } else {
         throw SQLError.createSQLException("Can't convert empty string ('') to numeric", "22018");
      }
   }

   private String convertToZeroLiteralStringWithEmptyCheck() throws SQLException {
      if (this.connection.getEmptyStringsConvertToZero()) {
         return "0";
      } else {
         throw SQLError.createSQLException("Can't convert empty string ('') to numeric", "22018");
      }
   }

   public ResultSetInternalMethods copy() throws SQLException {
      ResultSetInternalMethods rs = getInstance(this.catalog, this.fields, this.rowData, this.connection, this.owningStatement, false);
      return rs;
   }

   public void redefineFieldsForDBMD(Field[] f) {
      this.fields = f;

      for(int i = 0; i < this.fields.length; ++i) {
         this.fields[i].setUseOldNameMetadata(true);
         this.fields[i].setConnection(this.connection);
      }

   }

   public void populateCachedMetaData(CachedResultSetMetaData cachedMetaData) throws SQLException {
      cachedMetaData.fields = this.fields;
      cachedMetaData.columnNameToIndex = this.columnNameToIndex;
      cachedMetaData.fullColumnNameToIndex = this.fullColumnNameToIndex;
      cachedMetaData.metadata = this.getMetaData();
   }

   public void initializeFromCachedMetaData(CachedResultSetMetaData cachedMetaData) {
      this.fields = cachedMetaData.fields;
      this.columnNameToIndex = cachedMetaData.columnNameToIndex;
      this.fullColumnNameToIndex = cachedMetaData.fullColumnNameToIndex;
      this.hasBuiltIndexMapping = true;
   }

   public void deleteRow() throws SQLException {
      throw new NotUpdatable();
   }

   private String extractStringFromNativeColumn(int columnIndex, int mysqlType) throws SQLException {
      int columnIndexMinusOne = columnIndex - 1;
      this.wasNullFlag = false;
      if (this.thisRow.isNull(columnIndexMinusOne)) {
         this.wasNullFlag = true;
         return null;
      } else {
         this.wasNullFlag = false;
         String encoding = this.fields[columnIndexMinusOne].getCharacterSet();
         return this.thisRow.getString(columnIndex - 1, encoding, this.connection);
      }
   }

   protected synchronized Date fastDateCreate(Calendar cal, int year, int month, int day) {
      if (this.useLegacyDatetimeCode) {
         return TimeUtil.fastDateCreate(year, month, day, cal);
      } else {
         if (cal == null) {
            this.createCalendarIfNeeded();
            cal = this.fastDateCal;
         }

         boolean useGmtMillis = this.connection.getUseGmtMillisForDatetimes();
         return TimeUtil.fastDateCreate(useGmtMillis, useGmtMillis ? this.getGmtCalendar() : cal, cal, year, month, day);
      }
   }

   protected synchronized Time fastTimeCreate(Calendar cal, int hour, int minute, int second) throws SQLException {
      if (!this.useLegacyDatetimeCode) {
         return TimeUtil.fastTimeCreate(hour, minute, second, cal);
      } else {
         if (cal == null) {
            this.createCalendarIfNeeded();
            cal = this.fastDateCal;
         }

         return TimeUtil.fastTimeCreate(cal, hour, minute, second);
      }
   }

   protected synchronized Timestamp fastTimestampCreate(Calendar cal, int year, int month, int day, int hour, int minute, int seconds, int secondsPart) {
      if (!this.useLegacyDatetimeCode) {
         return TimeUtil.fastTimestampCreate(cal.getTimeZone(), year, month, day, hour, minute, seconds, secondsPart);
      } else {
         if (cal == null) {
            this.createCalendarIfNeeded();
            cal = this.fastDateCal;
         }

         boolean useGmtMillis = this.connection.getUseGmtMillisForDatetimes();
         return TimeUtil.fastTimestampCreate(useGmtMillis, useGmtMillis ? this.getGmtCalendar() : null, cal, year, month, day, hour, minute, seconds, secondsPart);
      }
   }

   public synchronized int findColumn(String columnName) throws SQLException {
      if (!this.hasBuiltIndexMapping) {
         this.buildIndexMapping();
      }

      Integer index = (Integer)this.columnNameToIndex.get(columnName);
      if (index == null) {
         index = (Integer)this.fullColumnNameToIndex.get(columnName);
      }

      if (index != null) {
         return index + 1;
      } else {
         for(int i = 0; i < this.fields.length; ++i) {
            if (this.fields[i].getName().equalsIgnoreCase(columnName)) {
               return i + 1;
            }

            if (this.fields[i].getFullName().equalsIgnoreCase(columnName)) {
               return i + 1;
            }
         }

         throw SQLError.createSQLException(Messages.getString("ResultSet.Column____112") + columnName + Messages.getString("ResultSet.___not_found._113"), "S0022");
      }
   }

   public boolean first() throws SQLException {
      this.checkClosed();
      boolean b = true;
      if (this.rowData.isEmpty()) {
         b = false;
      } else {
         if (this.onInsertRow) {
            this.onInsertRow = false;
         }

         if (this.doingUpdates) {
            this.doingUpdates = false;
         }

         this.rowData.beforeFirst();
         this.thisRow = this.rowData.next();
      }

      this.setRowPositionValidity();
      return b;
   }

   public Array getArray(int i) throws SQLException {
      this.checkColumnBounds(i);
      throw SQLError.notImplemented();
   }

   public Array getArray(String colName) throws SQLException {
      return this.getArray(this.findColumn(colName));
   }

   public InputStream getAsciiStream(int columnIndex) throws SQLException {
      this.checkRowPos();
      return !this.isBinaryEncoded ? this.getBinaryStream(columnIndex) : this.getNativeBinaryStream(columnIndex);
   }

   public InputStream getAsciiStream(String columnName) throws SQLException {
      return this.getAsciiStream(this.findColumn(columnName));
   }

   public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
      if (!this.isBinaryEncoded) {
         String stringVal = this.getString(columnIndex);
         if (stringVal != null) {
            if (stringVal.length() == 0) {
               BigDecimal val = new BigDecimal(this.convertToZeroLiteralStringWithEmptyCheck());
               return val;
            } else {
               try {
                  BigDecimal val = new BigDecimal(stringVal);
                  return val;
               } catch (NumberFormatException var5) {
                  throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[]{stringVal, Constants.integerValueOf(columnIndex)}), "S1009");
               }
            }
         } else {
            return null;
         }
      } else {
         return this.getNativeBigDecimal(columnIndex);
      }
   }

   /** @deprecated */
   public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
      if (!this.isBinaryEncoded) {
         String stringVal = this.getString(columnIndex);
         if (stringVal == null) {
            return null;
         } else if (stringVal.length() == 0) {
            BigDecimal val = new BigDecimal(this.convertToZeroLiteralStringWithEmptyCheck());

            try {
               return val.setScale(scale);
            } catch (ArithmeticException var10) {
               try {
                  return val.setScale(scale, 4);
               } catch (ArithmeticException var9) {
                  throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[]{stringVal, new Integer(columnIndex)}), "S1009");
               }
            }
         } else {
            BigDecimal val;
            try {
               val = new BigDecimal(stringVal);
            } catch (NumberFormatException var12) {
               if (this.fields[columnIndex - 1].getMysqlType() != 16) {
                  throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[]{Constants.integerValueOf(columnIndex), stringVal}), "S1009");
               }

               long valueAsLong = this.getNumericRepresentationOfSQLBitType(columnIndex);
               val = new BigDecimal((double)valueAsLong);
            }

            try {
               return val.setScale(scale);
            } catch (ArithmeticException var11) {
               try {
                  return val.setScale(scale, 4);
               } catch (ArithmeticException var8) {
                  throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[]{Constants.integerValueOf(columnIndex), stringVal}), "S1009");
               }
            }
         }
      } else {
         return this.getNativeBigDecimal(columnIndex, scale);
      }
   }

   public BigDecimal getBigDecimal(String columnName) throws SQLException {
      return this.getBigDecimal(this.findColumn(columnName));
   }

   /** @deprecated */
   public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException {
      return this.getBigDecimal(this.findColumn(columnName), scale);
   }

   private final BigDecimal getBigDecimalFromString(String stringVal, int columnIndex, int scale) throws SQLException {
      if (stringVal == null) {
         return null;
      } else if (stringVal.length() == 0) {
         BigDecimal bdVal = new BigDecimal(this.convertToZeroLiteralStringWithEmptyCheck());

         try {
            return bdVal.setScale(scale);
         } catch (ArithmeticException var14) {
            try {
               return bdVal.setScale(scale, 4);
            } catch (ArithmeticException var13) {
               throw new SQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[]{stringVal, Constants.integerValueOf(columnIndex)}), "S1009");
            }
         }
      } else {
         try {
            try {
               return (new BigDecimal(stringVal)).setScale(scale);
            } catch (ArithmeticException var15) {
               try {
                  return (new BigDecimal(stringVal)).setScale(scale, 4);
               } catch (ArithmeticException var12) {
                  throw new SQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[]{stringVal, Constants.integerValueOf(columnIndex)}), "S1009");
               }
            }
         } catch (NumberFormatException var16) {
            if (this.fields[columnIndex - 1].getMysqlType() == 16) {
               long valueAsLong = this.getNumericRepresentationOfSQLBitType(columnIndex);

               try {
                  return (new BigDecimal((double)valueAsLong)).setScale(scale);
               } catch (ArithmeticException var11) {
                  try {
                     return (new BigDecimal((double)valueAsLong)).setScale(scale, 4);
                  } catch (ArithmeticException var10) {
                     throw new SQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[]{stringVal, Constants.integerValueOf(columnIndex)}), "S1009");
                  }
               }
            } else if (this.fields[columnIndex - 1].getMysqlType() == 1 && this.connection.getTinyInt1isBit() && this.fields[columnIndex - 1].getLength() == 1L) {
               return (new BigDecimal(stringVal.equalsIgnoreCase("true") ? (double)1.0F : (double)0.0F)).setScale(scale);
            } else {
               throw new SQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[]{stringVal, Constants.integerValueOf(columnIndex)}), "S1009");
            }
         }
      }
   }

   public InputStream getBinaryStream(int columnIndex) throws SQLException {
      this.checkRowPos();
      if (!this.isBinaryEncoded) {
         this.checkColumnBounds(columnIndex);
         int columnIndexMinusOne = columnIndex - 1;
         if (this.thisRow.isNull(columnIndexMinusOne)) {
            this.wasNullFlag = true;
            return null;
         } else {
            this.wasNullFlag = false;
            return this.thisRow.getBinaryInputStream(columnIndexMinusOne);
         }
      } else {
         return this.getNativeBinaryStream(columnIndex);
      }
   }

   public InputStream getBinaryStream(String columnName) throws SQLException {
      return this.getBinaryStream(this.findColumn(columnName));
   }

   public java.sql.Blob getBlob(int columnIndex) throws SQLException {
      if (!this.isBinaryEncoded) {
         this.checkRowPos();
         this.checkColumnBounds(columnIndex);
         int columnIndexMinusOne = columnIndex - 1;
         if (this.thisRow.isNull(columnIndexMinusOne)) {
            this.wasNullFlag = true;
         } else {
            this.wasNullFlag = false;
         }

         if (this.wasNullFlag) {
            return null;
         } else {
            return (java.sql.Blob)(!this.connection.getEmulateLocators() ? new Blob(this.thisRow.getColumnValue(columnIndexMinusOne)) : new BlobFromLocator(this, columnIndex));
         }
      } else {
         return this.getNativeBlob(columnIndex);
      }
   }

   public java.sql.Blob getBlob(String colName) throws SQLException {
      return this.getBlob(this.findColumn(colName));
   }

   public boolean getBoolean(int columnIndex) throws SQLException {
      this.checkColumnBounds(columnIndex);
      int columnIndexMinusOne = columnIndex - 1;
      Field field = this.fields[columnIndexMinusOne];
      if (field.getMysqlType() == 16) {
         return this.byteArrayToBoolean(columnIndexMinusOne);
      } else {
         this.wasNullFlag = false;
         int sqlType = field.getSQLType();
         switch (sqlType) {
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
            case 16:
               long boolVal = this.getLong(columnIndex, false);
               return boolVal == -1L || boolVal > 0L;
            case -4:
            case -3:
            case -2:
            case -1:
            case 0:
            case 1:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            default:
               if (this.connection.getPedantic()) {
                  switch (sqlType) {
                     case -4:
                     case -3:
                     case -2:
                     case 70:
                     case 91:
                     case 92:
                     case 93:
                     case 2000:
                     case 2002:
                     case 2003:
                     case 2004:
                     case 2005:
                     case 2006:
                        throw SQLError.createSQLException("Required type conversion not allowed", "22018");
                  }
               }

               if (sqlType != -2 && sqlType != -3 && sqlType != -4 && sqlType != 2004) {
                  if (this.useUsageAdvisor) {
                     this.issueConversionViaParsingWarning("getBoolean()", columnIndex, this.thisRow.getColumnValue(columnIndexMinusOne), this.fields[columnIndex], new int[]{16, 5, 1, 2, 3, 8, 4});
                  }

                  String stringVal = this.getString(columnIndex);
                  return this.getBooleanFromString(stringVal, columnIndex);
               } else {
                  return this.byteArrayToBoolean(columnIndexMinusOne);
               }
         }
      }
   }

   private boolean byteArrayToBoolean(int columnIndexMinusOne) throws SQLException {
      Object value = this.thisRow.getColumnValue(columnIndexMinusOne);
      if (value == null) {
         this.wasNullFlag = true;
         return false;
      } else {
         this.wasNullFlag = false;
         if (((byte[])value).length == 0) {
            return false;
         } else {
            byte boolVal = ((byte[])value)[0];
            if (boolVal == 49) {
               return true;
            } else if (boolVal == 48) {
               return false;
            } else {
               return boolVal == -1 || boolVal > 0;
            }
         }
      }
   }

   public boolean getBoolean(String columnName) throws SQLException {
      return this.getBoolean(this.findColumn(columnName));
   }

   private final boolean getBooleanFromString(String stringVal, int columnIndex) throws SQLException {
      if (stringVal != null && stringVal.length() > 0) {
         int c = Character.toLowerCase(stringVal.charAt(0));
         return c == 116 || c == 121 || c == 49 || stringVal.equals("-1");
      } else {
         return false;
      }
   }

   public byte getByte(int columnIndex) throws SQLException {
      if (!this.isBinaryEncoded) {
         String stringVal = this.getString(columnIndex);
         return !this.wasNullFlag && stringVal != null ? this.getByteFromString(stringVal, columnIndex) : 0;
      } else {
         return this.getNativeByte(columnIndex);
      }
   }

   public byte getByte(String columnName) throws SQLException {
      return this.getByte(this.findColumn(columnName));
   }

   private final byte getByteFromString(String stringVal, int columnIndex) throws SQLException {
      if (stringVal != null && stringVal.length() == 0) {
         return (byte)this.convertToZeroWithEmptyCheck();
      } else if (stringVal == null) {
         return 0;
      } else {
         stringVal = stringVal.trim();

         try {
            int decimalIndex = stringVal.indexOf(".");
            if (decimalIndex != -1) {
               double valueAsDouble = Double.parseDouble(stringVal);
               if (this.jdbcCompliantTruncationForReads && (valueAsDouble < (double)-128.0F || valueAsDouble > (double)127.0F)) {
                  this.throwRangeException(stringVal, columnIndex, -6);
               }

               return (byte)((int)valueAsDouble);
            } else {
               long valueAsLong = Long.parseLong(stringVal);
               if (this.jdbcCompliantTruncationForReads && (valueAsLong < -128L || valueAsLong > 127L)) {
                  this.throwRangeException(String.valueOf(valueAsLong), columnIndex, -6);
               }

               return (byte)((int)valueAsLong);
            }
         } catch (NumberFormatException var6) {
            throw SQLError.createSQLException(Messages.getString("ResultSet.Value____173") + stringVal + Messages.getString("ResultSet.___is_out_of_range_[-127,127]_174"), "S1009");
         }
      }
   }

   public byte[] getBytes(int columnIndex) throws SQLException {
      return this.getBytes(columnIndex, false);
   }

   protected byte[] getBytes(int columnIndex, boolean noConversion) throws SQLException {
      if (!this.isBinaryEncoded) {
         this.checkRowPos();
         this.checkColumnBounds(columnIndex);
         int columnIndexMinusOne = columnIndex - 1;
         if (this.thisRow.isNull(columnIndexMinusOne)) {
            this.wasNullFlag = true;
         } else {
            this.wasNullFlag = false;
         }

         return this.wasNullFlag ? null : this.thisRow.getColumnValue(columnIndexMinusOne);
      } else {
         return this.getNativeBytes(columnIndex, noConversion);
      }
   }

   public byte[] getBytes(String columnName) throws SQLException {
      return this.getBytes(this.findColumn(columnName));
   }

   private final byte[] getBytesFromString(String stringVal, int columnIndex) throws SQLException {
      return stringVal != null ? StringUtils.getBytes(stringVal, this.connection.getEncoding(), this.connection.getServerCharacterEncoding(), this.connection.parserKnowsUnicode(), this.connection) : null;
   }

   protected Calendar getCalendarInstanceForSessionOrNew() {
      return (Calendar)(this.connection != null ? this.connection.getCalendarInstanceForSessionOrNew() : new GregorianCalendar());
   }

   public Reader getCharacterStream(int columnIndex) throws SQLException {
      if (!this.isBinaryEncoded) {
         this.checkColumnBounds(columnIndex);
         int columnIndexMinusOne = columnIndex - 1;
         if (this.thisRow.isNull(columnIndexMinusOne)) {
            this.wasNullFlag = true;
            return null;
         } else {
            this.wasNullFlag = false;
            return this.thisRow.getReader(columnIndexMinusOne);
         }
      } else {
         return this.getNativeCharacterStream(columnIndex);
      }
   }

   public Reader getCharacterStream(String columnName) throws SQLException {
      return this.getCharacterStream(this.findColumn(columnName));
   }

   private final Reader getCharacterStreamFromString(String stringVal, int columnIndex) throws SQLException {
      return stringVal != null ? new StringReader(stringVal) : null;
   }

   public java.sql.Clob getClob(int i) throws SQLException {
      if (!this.isBinaryEncoded) {
         String asString = this.getStringForClob(i);
         return asString == null ? null : new Clob(asString);
      } else {
         return this.getNativeClob(i);
      }
   }

   public java.sql.Clob getClob(String colName) throws SQLException {
      return this.getClob(this.findColumn(colName));
   }

   private final java.sql.Clob getClobFromString(String stringVal, int columnIndex) throws SQLException {
      return new Clob(stringVal);
   }

   public int getConcurrency() throws SQLException {
      return 1007;
   }

   public String getCursorName() throws SQLException {
      throw SQLError.createSQLException(Messages.getString("ResultSet.Positioned_Update_not_supported"), "S1C00");
   }

   public Date getDate(int columnIndex) throws SQLException {
      return this.getDate(columnIndex, (Calendar)null);
   }

   public Date getDate(int columnIndex, Calendar cal) throws SQLException {
      if (this.isBinaryEncoded) {
         return this.getNativeDate(columnIndex, cal != null ? cal.getTimeZone() : this.getDefaultTimeZone());
      } else if (!this.useFastDateParsing) {
         String stringVal = this.getStringInternal(columnIndex, false);
         return stringVal == null ? null : this.getDateFromString(stringVal, columnIndex, cal);
      } else {
         this.checkColumnBounds(columnIndex);
         int columnIndexMinusOne = columnIndex - 1;
         if (this.thisRow.isNull(columnIndexMinusOne)) {
            this.wasNullFlag = true;
            return null;
         } else {
            this.wasNullFlag = false;
            return this.thisRow.getDateFast(columnIndexMinusOne, this.connection, this, cal);
         }
      }
   }

   public Date getDate(String columnName) throws SQLException {
      return this.getDate(this.findColumn(columnName));
   }

   public Date getDate(String columnName, Calendar cal) throws SQLException {
      return this.getDate(this.findColumn(columnName), cal);
   }

   private final Date getDateFromString(String stringVal, int columnIndex, Calendar targetCalendar) throws SQLException {
      int year = 0;
      int month = 0;
      int day = 0;

      try {
         this.wasNullFlag = false;
         if (stringVal == null) {
            this.wasNullFlag = true;
            return null;
         } else {
            stringVal = stringVal.trim();
            if (!stringVal.equals("0") && !stringVal.equals("0000-00-00") && !stringVal.equals("0000-00-00 00:00:00") && !stringVal.equals("00000000000000") && !stringVal.equals("0")) {
               if (this.fields[columnIndex - 1].getMysqlType() == 7) {
                  switch (stringVal.length()) {
                     case 2:
                        year = Integer.parseInt(stringVal.substring(0, 2));
                        if (year <= 69) {
                           year += 100;
                        }

                        return this.fastDateCreate(targetCalendar, year + 1900, 1, 1);
                     case 3:
                     case 5:
                     case 7:
                     case 9:
                     case 11:
                     case 13:
                     case 15:
                     case 16:
                     case 17:
                     case 18:
                     case 20:
                     default:
                        throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_Date", new Object[]{stringVal, Constants.integerValueOf(columnIndex)}), "S1009");
                     case 4:
                        year = Integer.parseInt(stringVal.substring(0, 4));
                        if (year <= 69) {
                           year += 100;
                        }

                        month = Integer.parseInt(stringVal.substring(2, 4));
                        return this.fastDateCreate(targetCalendar, year + 1900, month, 1);
                     case 6:
                     case 10:
                     case 12:
                        year = Integer.parseInt(stringVal.substring(0, 2));
                        if (year <= 69) {
                           year += 100;
                        }

                        month = Integer.parseInt(stringVal.substring(2, 4));
                        day = Integer.parseInt(stringVal.substring(4, 6));
                        return this.fastDateCreate(targetCalendar, year + 1900, month, day);
                     case 8:
                     case 14:
                        year = Integer.parseInt(stringVal.substring(0, 4));
                        month = Integer.parseInt(stringVal.substring(4, 6));
                        day = Integer.parseInt(stringVal.substring(6, 8));
                        return this.fastDateCreate(targetCalendar, year, month, day);
                     case 19:
                     case 21:
                        year = Integer.parseInt(stringVal.substring(0, 4));
                        month = Integer.parseInt(stringVal.substring(5, 7));
                        day = Integer.parseInt(stringVal.substring(8, 10));
                        return this.fastDateCreate(targetCalendar, year, month, day);
                  }
               } else if (this.fields[columnIndex - 1].getMysqlType() != 13) {
                  if (this.fields[columnIndex - 1].getMysqlType() == 11) {
                     return this.fastDateCreate(targetCalendar, 1970, 1, 1);
                  } else if (stringVal.length() < 10) {
                     if (stringVal.length() == 8) {
                        return this.fastDateCreate(targetCalendar, 1970, 1, 1);
                     } else {
                        throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_Date", new Object[]{stringVal, Constants.integerValueOf(columnIndex)}), "S1009");
                     }
                  } else {
                     if (stringVal.length() != 18) {
                        year = Integer.parseInt(stringVal.substring(0, 4));
                        month = Integer.parseInt(stringVal.substring(5, 7));
                        day = Integer.parseInt(stringVal.substring(8, 10));
                     } else {
                        StringTokenizer st = new StringTokenizer(stringVal, "- ");
                        year = Integer.parseInt(st.nextToken());
                        month = Integer.parseInt(st.nextToken());
                        day = Integer.parseInt(st.nextToken());
                     }

                     return this.fastDateCreate(targetCalendar, year, month, day);
                  }
               } else {
                  if (stringVal.length() != 2 && stringVal.length() != 1) {
                     year = Integer.parseInt(stringVal.substring(0, 4));
                  } else {
                     year = Integer.parseInt(stringVal);
                     if (year <= 69) {
                        year += 100;
                     }

                     year += 1900;
                  }

                  return this.fastDateCreate(targetCalendar, year, 1, 1);
               }
            } else if ("convertToNull".equals(this.connection.getZeroDateTimeBehavior())) {
               this.wasNullFlag = true;
               return null;
            } else if ("exception".equals(this.connection.getZeroDateTimeBehavior())) {
               throw SQLError.createSQLException("Value '" + stringVal + "' can not be represented as java.sql.Date", "S1009");
            } else {
               return this.fastDateCreate(targetCalendar, 1, 1, 1);
            }
         }
      } catch (SQLException sqlEx) {
         throw sqlEx;
      } catch (Exception e) {
         SQLException sqlEx = SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_Date", new Object[]{stringVal, Constants.integerValueOf(columnIndex)}), "S1009");
         sqlEx.initCause(e);
         throw sqlEx;
      }
   }

   private TimeZone getDefaultTimeZone() {
      return !this.useLegacyDatetimeCode && this.connection != null ? this.serverTimeZoneTz : this.connection.getDefaultTimeZone();
   }

   public double getDouble(int columnIndex) throws SQLException {
      return !this.isBinaryEncoded ? this.getDoubleInternal(columnIndex) : this.getNativeDouble(columnIndex);
   }

   public double getDouble(String columnName) throws SQLException {
      return this.getDouble(this.findColumn(columnName));
   }

   private final double getDoubleFromString(String stringVal, int columnIndex) throws SQLException {
      return this.getDoubleInternal(stringVal, columnIndex);
   }

   protected double getDoubleInternal(int colIndex) throws SQLException {
      return this.getDoubleInternal(this.getString(colIndex), colIndex);
   }

   protected double getDoubleInternal(String stringVal, int colIndex) throws SQLException {
      try {
         if (stringVal == null) {
            return (double)0.0F;
         } else if (stringVal.length() == 0) {
            return (double)this.convertToZeroWithEmptyCheck();
         } else {
            double d = Double.parseDouble(stringVal);
            if (this.useStrictFloatingPoint) {
               if (d == (double)(float)Integer.MAX_VALUE) {
                  d = (double)Integer.MAX_VALUE;
               } else if (d == 1.0000000036275E-15) {
                  d = 1.0E-15;
               } else if (d == 9.999999869911E14) {
                  d = 9.99999999999999E14;
               } else if (d == 1.4012984643248E-45) {
                  d = 1.4E-45;
               } else if (d == 1.4013E-45) {
                  d = 1.4E-45;
               } else if (d == 3.4028234663853E37) {
                  d = 3.4028235E37;
               } else if (d == -2.14748E9) {
                  d = (double)Integer.MIN_VALUE;
               } else if (d == 3.40282E37) {
                  d = 3.4028235E37;
               }
            }

            return d;
         }
      } catch (NumberFormatException var6) {
         if (this.fields[colIndex - 1].getMysqlType() == 16) {
            long valueAsLong = this.getNumericRepresentationOfSQLBitType(colIndex);
            return (double)valueAsLong;
         } else {
            throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_number", new Object[]{stringVal, Constants.integerValueOf(colIndex)}), "S1009");
         }
      }
   }

   public int getFetchDirection() throws SQLException {
      return this.fetchDirection;
   }

   public int getFetchSize() throws SQLException {
      return this.fetchSize;
   }

   public char getFirstCharOfQuery() {
      return this.firstCharOfQuery;
   }

   public float getFloat(int columnIndex) throws SQLException {
      if (!this.isBinaryEncoded) {
         String val = null;
         val = this.getString(columnIndex);
         return this.getFloatFromString(val, columnIndex);
      } else {
         return this.getNativeFloat(columnIndex);
      }
   }

   public float getFloat(String columnName) throws SQLException {
      return this.getFloat(this.findColumn(columnName));
   }

   private final float getFloatFromString(String val, int columnIndex) throws SQLException {
      try {
         if (val != null) {
            if (val.length() == 0) {
               return (float)this.convertToZeroWithEmptyCheck();
            } else {
               float f = Float.parseFloat(val);
               if (this.jdbcCompliantTruncationForReads && (f == Float.MIN_VALUE || f == Float.MAX_VALUE)) {
                  double valAsDouble = Double.parseDouble(val);
                  if (valAsDouble < (double)Float.MIN_VALUE - MIN_DIFF_PREC || valAsDouble > (double)Float.MAX_VALUE - MAX_DIFF_PREC) {
                     this.throwRangeException(String.valueOf(valAsDouble), columnIndex, 6);
                  }
               }

               return f;
            }
         } else {
            return 0.0F;
         }
      } catch (NumberFormatException var7) {
         try {
            Double valueAsDouble = new Double(val);
            float valueAsFloat = valueAsDouble.floatValue();
            if (this.jdbcCompliantTruncationForReads && (this.jdbcCompliantTruncationForReads && valueAsFloat == Float.NEGATIVE_INFINITY || valueAsFloat == Float.POSITIVE_INFINITY)) {
               this.throwRangeException(valueAsDouble.toString(), columnIndex, 6);
            }

            return valueAsFloat;
         } catch (NumberFormatException var6) {
            throw SQLError.createSQLException(Messages.getString("ResultSet.Invalid_value_for_getFloat()_-____200") + val + Messages.getString("ResultSet.___in_column__201") + columnIndex, "S1009");
         }
      }
   }

   public int getInt(int columnIndex) throws SQLException {
      this.checkRowPos();
      if (this.isBinaryEncoded) {
         return this.getNativeInt(columnIndex);
      } else {
         int columnIndexMinusOne = columnIndex - 1;
         if (this.useFastIntParsing) {
            this.checkColumnBounds(columnIndex);
            if (this.thisRow.isNull(columnIndexMinusOne)) {
               this.wasNullFlag = true;
            } else {
               this.wasNullFlag = false;
            }

            if (this.wasNullFlag) {
               return 0;
            }

            if (this.thisRow.length(columnIndexMinusOne) == 0L) {
               return this.convertToZeroWithEmptyCheck();
            }

            boolean needsFullParse = this.thisRow.isFloatingPointNumber(columnIndexMinusOne);
            if (!needsFullParse) {
               try {
                  return this.getIntWithOverflowCheck(columnIndexMinusOne);
               } catch (NumberFormatException var10) {
                  try {
                     return this.parseIntAsDouble(columnIndex, this.thisRow.getString(columnIndexMinusOne, this.fields[columnIndexMinusOne].getCharacterSet(), this.connection));
                  } catch (NumberFormatException var9) {
                     if (this.fields[columnIndexMinusOne].getMysqlType() == 16) {
                        long valueAsLong = this.getNumericRepresentationOfSQLBitType(columnIndex);
                        if (this.connection.getJdbcCompliantTruncationForReads() && (valueAsLong < -2147483648L || valueAsLong > 2147483647L)) {
                           this.throwRangeException(String.valueOf(valueAsLong), columnIndex, 4);
                        }

                        return (int)valueAsLong;
                     }

                     throw SQLError.createSQLException(Messages.getString("ResultSet.Invalid_value_for_getInt()_-____74") + this.thisRow.getString(columnIndexMinusOne, this.fields[columnIndexMinusOne].getCharacterSet(), this.connection) + "'", "S1009");
                  }
               }
            }
         }

         String val = null;

         try {
            val = this.getString(columnIndex);
            if (val != null) {
               if (val.length() == 0) {
                  return this.convertToZeroWithEmptyCheck();
               } else if (val.indexOf("e") == -1 && val.indexOf("E") == -1 && val.indexOf(".") == -1) {
                  int intVal = Integer.parseInt(val);
                  this.checkForIntegerTruncation(columnIndex, (byte[])null, val, intVal);
                  return intVal;
               } else {
                  int intVal = this.parseIntAsDouble(columnIndex, val);
                  this.checkForIntegerTruncation(columnIndex, (byte[])null, val, intVal);
                  return intVal;
               }
            } else {
               return 0;
            }
         } catch (NumberFormatException var8) {
            try {
               return this.parseIntAsDouble(columnIndex, val);
            } catch (NumberFormatException var7) {
               if (this.fields[columnIndexMinusOne].getMysqlType() == 16) {
                  long valueAsLong = this.getNumericRepresentationOfSQLBitType(columnIndex);
                  if (this.jdbcCompliantTruncationForReads && (valueAsLong < -2147483648L || valueAsLong > 2147483647L)) {
                     this.throwRangeException(String.valueOf(valueAsLong), columnIndex, 4);
                  }

                  return (int)valueAsLong;
               } else {
                  throw SQLError.createSQLException(Messages.getString("ResultSet.Invalid_value_for_getInt()_-____74") + val + "'", "S1009");
               }
            }
         }
      }
   }

   public int getInt(String columnName) throws SQLException {
      return this.getInt(this.findColumn(columnName));
   }

   private final int getIntFromString(String val, int columnIndex) throws SQLException {
      try {
         if (val != null) {
            if (val.length() == 0) {
               return this.convertToZeroWithEmptyCheck();
            } else if (val.indexOf("e") == -1 && val.indexOf("E") == -1 && val.indexOf(".") == -1) {
               val = val.trim();
               int valueAsInt = Integer.parseInt(val);
               if (this.jdbcCompliantTruncationForReads && (valueAsInt == Integer.MIN_VALUE || valueAsInt == Integer.MAX_VALUE)) {
                  long valueAsLong = Long.parseLong(val);
                  if (valueAsLong < -2147483648L || valueAsLong > 2147483647L) {
                     this.throwRangeException(String.valueOf(valueAsLong), columnIndex, 4);
                  }
               }

               return valueAsInt;
            } else {
               double valueAsDouble = Double.parseDouble(val);
               if (this.jdbcCompliantTruncationForReads && (valueAsDouble < (double)Integer.MIN_VALUE || valueAsDouble > (double)Integer.MAX_VALUE)) {
                  this.throwRangeException(String.valueOf(valueAsDouble), columnIndex, 4);
               }

               return (int)valueAsDouble;
            }
         } else {
            return 0;
         }
      } catch (NumberFormatException var7) {
         try {
            double valueAsDouble = Double.parseDouble(val);
            if (this.jdbcCompliantTruncationForReads && (valueAsDouble < (double)Integer.MIN_VALUE || valueAsDouble > (double)Integer.MAX_VALUE)) {
               this.throwRangeException(String.valueOf(valueAsDouble), columnIndex, 4);
            }

            return (int)valueAsDouble;
         } catch (NumberFormatException var6) {
            throw SQLError.createSQLException(Messages.getString("ResultSet.Invalid_value_for_getInt()_-____206") + val + Messages.getString("ResultSet.___in_column__207") + columnIndex, "S1009");
         }
      }
   }

   public long getLong(int columnIndex) throws SQLException {
      return this.getLong(columnIndex, true);
   }

   private long getLong(int columnIndex, boolean overflowCheck) throws SQLException {
      if (this.isBinaryEncoded) {
         return this.getNativeLong(columnIndex, overflowCheck, true);
      } else {
         this.checkRowPos();
         int columnIndexMinusOne = columnIndex - 1;
         if (this.useFastIntParsing) {
            this.checkColumnBounds(columnIndex);
            if (this.thisRow.isNull(columnIndexMinusOne)) {
               this.wasNullFlag = true;
            } else {
               this.wasNullFlag = false;
            }

            if (this.wasNullFlag) {
               return 0L;
            }

            if (this.thisRow.length(columnIndexMinusOne) == 0L) {
               return (long)this.convertToZeroWithEmptyCheck();
            }

            boolean needsFullParse = this.thisRow.isFloatingPointNumber(columnIndexMinusOne);
            if (!needsFullParse) {
               try {
                  return this.getLongWithOverflowCheck(columnIndexMinusOne, overflowCheck);
               } catch (NumberFormatException var10) {
                  try {
                     return this.parseLongAsDouble(columnIndex, this.thisRow.getString(columnIndexMinusOne, this.fields[columnIndexMinusOne].getCharacterSet(), this.connection));
                  } catch (NumberFormatException var9) {
                     if (this.fields[columnIndexMinusOne].getMysqlType() == 16) {
                        return this.getNumericRepresentationOfSQLBitType(columnIndex);
                     }

                     throw SQLError.createSQLException(Messages.getString("ResultSet.Invalid_value_for_getLong()_-____79") + this.thisRow.getString(columnIndexMinusOne, this.fields[columnIndexMinusOne].getCharacterSet(), this.connection) + "'", "S1009");
                  }
               }
            }
         }

         String val = null;

         try {
            val = this.getString(columnIndex);
            if (val != null) {
               if (val.length() == 0) {
                  return (long)this.convertToZeroWithEmptyCheck();
               } else {
                  return val.indexOf("e") == -1 && val.indexOf("E") == -1 ? this.parseLongWithOverflowCheck(columnIndex, (byte[])null, val, overflowCheck) : this.parseLongAsDouble(columnIndex, val);
               }
            } else {
               return 0L;
            }
         } catch (NumberFormatException var8) {
            try {
               return this.parseLongAsDouble(columnIndex, val);
            } catch (NumberFormatException var7) {
               throw SQLError.createSQLException(Messages.getString("ResultSet.Invalid_value_for_getLong()_-____79") + val + "'", "S1009");
            }
         }
      }
   }

   public long getLong(String columnName) throws SQLException {
      return this.getLong(this.findColumn(columnName));
   }

   private final long getLongFromString(String val, int columnIndex) throws SQLException {
      try {
         if (val != null) {
            if (val.length() == 0) {
               return (long)this.convertToZeroWithEmptyCheck();
            } else {
               return val.indexOf("e") == -1 && val.indexOf("E") == -1 ? this.parseLongWithOverflowCheck(columnIndex, (byte[])null, val, true) : this.parseLongAsDouble(columnIndex, val);
            }
         } else {
            return 0L;
         }
      } catch (NumberFormatException var6) {
         try {
            return this.parseLongAsDouble(columnIndex, val);
         } catch (NumberFormatException var5) {
            throw SQLError.createSQLException(Messages.getString("ResultSet.Invalid_value_for_getLong()_-____211") + val + Messages.getString("ResultSet.___in_column__212") + columnIndex, "S1009");
         }
      }
   }

   public java.sql.ResultSetMetaData getMetaData() throws SQLException {
      this.checkClosed();
      return new ResultSetMetaData(this.fields, this.connection.getUseOldAliasMetadataBehavior());
   }

   protected Array getNativeArray(int i) throws SQLException {
      throw SQLError.notImplemented();
   }

   protected InputStream getNativeAsciiStream(int columnIndex) throws SQLException {
      this.checkRowPos();
      return this.getNativeBinaryStream(columnIndex);
   }

   protected BigDecimal getNativeBigDecimal(int columnIndex) throws SQLException {
      this.checkColumnBounds(columnIndex);
      int scale = this.fields[columnIndex - 1].getDecimals();
      return this.getNativeBigDecimal(columnIndex, scale);
   }

   protected BigDecimal getNativeBigDecimal(int columnIndex, int scale) throws SQLException {
      this.checkColumnBounds(columnIndex);
      String stringVal = null;
      Field f = this.fields[columnIndex - 1];
      Object value = this.thisRow.getColumnValue(columnIndex - 1);
      if (value == null) {
         this.wasNullFlag = true;
         return null;
      } else {
         this.wasNullFlag = false;
         switch (f.getSQLType()) {
            case 2:
            case 3:
               stringVal = StringUtils.toAsciiString((byte[])value);
               break;
            default:
               stringVal = this.getNativeString(columnIndex);
         }

         return this.getBigDecimalFromString(stringVal, columnIndex, scale);
      }
   }

   protected InputStream getNativeBinaryStream(int columnIndex) throws SQLException {
      this.checkRowPos();
      int columnIndexMinusOne = columnIndex - 1;
      if (this.thisRow.isNull(columnIndexMinusOne)) {
         this.wasNullFlag = true;
         return null;
      } else {
         this.wasNullFlag = false;
         switch (this.fields[columnIndexMinusOne].getSQLType()) {
            case -7:
            case -4:
            case -3:
            case -2:
            case 2004:
               return this.thisRow.getBinaryInputStream(columnIndexMinusOne);
            default:
               byte[] b = this.getNativeBytes(columnIndex, false);
               return b != null ? new ByteArrayInputStream(b) : null;
         }
      }
   }

   protected java.sql.Blob getNativeBlob(int columnIndex) throws SQLException {
      this.checkRowPos();
      this.checkColumnBounds(columnIndex);
      Object value = this.thisRow.getColumnValue(columnIndex - 1);
      if (value == null) {
         this.wasNullFlag = true;
      } else {
         this.wasNullFlag = false;
      }

      if (this.wasNullFlag) {
         return null;
      } else {
         int mysqlType = this.fields[columnIndex - 1].getMysqlType();
         byte[] dataAsBytes = null;
         switch (mysqlType) {
            case 249:
            case 250:
            case 251:
            case 252:
               dataAsBytes = (byte[])value;
               break;
            default:
               dataAsBytes = this.getNativeBytes(columnIndex, false);
         }

         return (java.sql.Blob)(!this.connection.getEmulateLocators() ? new Blob(dataAsBytes) : new BlobFromLocator(this, columnIndex));
      }
   }

   public static boolean arraysEqual(byte[] left, byte[] right) {
      if (left == null) {
         return right == null;
      } else if (right == null) {
         return false;
      } else if (left.length != right.length) {
         return false;
      } else {
         for(int i = 0; i < left.length; ++i) {
            if (left[i] != right[i]) {
               return false;
            }
         }

         return true;
      }
   }

   protected byte getNativeByte(int columnIndex) throws SQLException {
      return this.getNativeByte(columnIndex, true);
   }

   protected byte getNativeByte(int columnIndex, boolean overflowCheck) throws SQLException {
      this.checkRowPos();
      this.checkColumnBounds(columnIndex);
      Object value = this.thisRow.getColumnValue(columnIndex - 1);
      if (value == null) {
         this.wasNullFlag = true;
         return 0;
      } else {
         if (value == null) {
            this.wasNullFlag = true;
         } else {
            this.wasNullFlag = false;
         }

         if (this.wasNullFlag) {
            return 0;
         } else {
            --columnIndex;
            Field field = this.fields[columnIndex];
            switch (field.getMysqlType()) {
               case 1:
                  byte valueAsByte = ((byte[])value)[0];
                  if (!field.isUnsigned()) {
                     return valueAsByte;
                  }

                  short valueAsShort = valueAsByte >= 0 ? (short)valueAsByte : (short)(valueAsByte + 256);
                  if (overflowCheck && this.jdbcCompliantTruncationForReads && valueAsShort > 127) {
                     this.throwRangeException(String.valueOf(valueAsShort), columnIndex + 1, -6);
                  }

                  return (byte)valueAsShort;
               case 2:
               case 13:
                  short valueAsShort = this.getNativeShort(columnIndex + 1);
                  if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsShort < -128 || valueAsShort > 127)) {
                     this.throwRangeException(String.valueOf(valueAsShort), columnIndex + 1, -6);
                  }

                  return (byte)valueAsShort;
               case 3:
               case 9:
                  int valueAsInt = this.getNativeInt(columnIndex + 1, false);
                  if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsInt < -128 || valueAsInt > 127)) {
                     this.throwRangeException(String.valueOf(valueAsInt), columnIndex + 1, -6);
                  }

                  return (byte)valueAsInt;
               case 4:
                  float valueAsFloat = this.getNativeFloat(columnIndex + 1);
                  if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsFloat < -128.0F || valueAsFloat > 127.0F)) {
                     this.throwRangeException(String.valueOf(valueAsFloat), columnIndex + 1, -6);
                  }

                  return (byte)((int)valueAsFloat);
               case 5:
                  double valueAsDouble = this.getNativeDouble(columnIndex + 1);
                  if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsDouble < (double)-128.0F || valueAsDouble > (double)127.0F)) {
                     this.throwRangeException(String.valueOf(valueAsDouble), columnIndex + 1, -6);
                  }

                  return (byte)((int)valueAsDouble);
               case 6:
               case 7:
               case 10:
               case 11:
               case 12:
               case 14:
               case 15:
               default:
                  if (this.useUsageAdvisor) {
                     this.issueConversionViaParsingWarning("getByte()", columnIndex, this.thisRow.getColumnValue(columnIndex - 1), this.fields[columnIndex], new int[]{5, 1, 2, 3, 8, 4});
                  }

                  return this.getByteFromString(this.getNativeString(columnIndex + 1), columnIndex + 1);
               case 8:
                  long valueAsLong = this.getNativeLong(columnIndex + 1, false, true);
                  if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsLong < -128L || valueAsLong > 127L)) {
                     this.throwRangeException(String.valueOf(valueAsLong), columnIndex + 1, -6);
                  }

                  return (byte)((int)valueAsLong);
               case 16:
                  long valueAsLong = this.getNumericRepresentationOfSQLBitType(columnIndex + 1);
                  if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsLong < -128L || valueAsLong > 127L)) {
                     this.throwRangeException(String.valueOf(valueAsLong), columnIndex + 1, -6);
                  }

                  return (byte)((int)valueAsLong);
            }
         }
      }
   }

   protected byte[] getNativeBytes(int columnIndex, boolean noConversion) throws SQLException {
      this.checkRowPos();
      this.checkColumnBounds(columnIndex);
      Object value = this.thisRow.getColumnValue(columnIndex - 1);
      if (value == null) {
         this.wasNullFlag = true;
      } else {
         this.wasNullFlag = false;
      }

      if (this.wasNullFlag) {
         return null;
      } else {
         Field field = this.fields[columnIndex - 1];
         int mysqlType = field.getMysqlType();
         if (noConversion) {
            mysqlType = 252;
         }

         switch (mysqlType) {
            case 15:
            case 253:
            case 254:
               if (value instanceof byte[]) {
                  return (byte[])value;
               }
            default:
               int sqlType = field.getSQLType();
               if (sqlType != -3 && sqlType != -2) {
                  return this.getBytesFromString(this.getNativeString(columnIndex), columnIndex);
               }

               return (byte[])value;
            case 16:
            case 249:
            case 250:
            case 251:
            case 252:
               return (byte[])value;
         }
      }
   }

   protected Reader getNativeCharacterStream(int columnIndex) throws SQLException {
      int columnIndexMinusOne = columnIndex - 1;
      switch (this.fields[columnIndexMinusOne].getSQLType()) {
         case -1:
         case 1:
         case 12:
         case 2005:
            if (this.thisRow.isNull(columnIndexMinusOne)) {
               this.wasNullFlag = true;
               return null;
            }

            this.wasNullFlag = false;
            return this.thisRow.getReader(columnIndexMinusOne);
         default:
            String asString = null;
            asString = this.getStringForClob(columnIndex);
            return asString == null ? null : this.getCharacterStreamFromString(asString, columnIndex);
      }
   }

   protected java.sql.Clob getNativeClob(int columnIndex) throws SQLException {
      String stringVal = this.getStringForClob(columnIndex);
      return stringVal == null ? null : this.getClobFromString(stringVal, columnIndex);
   }

   private String getNativeConvertToString(int columnIndex, Field field) throws SQLException {
      int sqlType = field.getSQLType();
      int mysqlType = field.getMysqlType();
      switch (sqlType) {
         case -7:
            return String.valueOf(this.getNumericRepresentationOfSQLBitType(columnIndex));
         case -6:
            byte tinyintVal = this.getNativeByte(columnIndex, false);
            if (this.wasNullFlag) {
               return null;
            } else {
               if (field.isUnsigned() && tinyintVal < 0) {
                  short unsignedTinyVal = (short)(tinyintVal & 255);
                  return String.valueOf(unsignedTinyVal);
               }

               return String.valueOf(tinyintVal);
            }
         case -5:
            if (!field.isUnsigned()) {
               long longVal = this.getNativeLong(columnIndex, false, true);
               if (this.wasNullFlag) {
                  return null;
               }

               return String.valueOf(longVal);
            } else {
               long longVal = this.getNativeLong(columnIndex, false, false);
               if (this.wasNullFlag) {
                  return null;
               }

               return String.valueOf(convertLongToUlong(longVal));
            }
         case -4:
         case -3:
         case -2:
            if (!field.isBlob()) {
               return this.extractStringFromNativeColumn(columnIndex, mysqlType);
            } else if (!field.isBinary()) {
               return this.extractStringFromNativeColumn(columnIndex, mysqlType);
            } else {
               byte[] data = this.getBytes(columnIndex);
               Object obj = data;
               if (data != null && data.length >= 2) {
                  if (data[0] == -84 && data[1] == -19) {
                     try {
                        ByteArrayInputStream bytesIn = new ByteArrayInputStream(data);
                        ObjectInputStream objIn = new ObjectInputStream(bytesIn);
                        obj = objIn.readObject();
                        objIn.close();
                        bytesIn.close();
                     } catch (ClassNotFoundException cnfe) {
                        throw SQLError.createSQLException(Messages.getString("ResultSet.Class_not_found___91") + cnfe.toString() + Messages.getString("ResultSet._while_reading_serialized_object_92"));
                     } catch (IOException var22) {
                        obj = data;
                     }
                  }

                  return obj.toString();
               }

               return this.extractStringFromNativeColumn(columnIndex, mysqlType);
            }
         case -1:
         case 1:
         case 12:
            return this.extractStringFromNativeColumn(columnIndex, mysqlType);
         case 2:
         case 3:
            String stringVal = StringUtils.toAsciiString(this.thisRow.getColumnValue(columnIndex - 1));
            if (stringVal != null) {
               this.wasNullFlag = false;
               if (stringVal.length() == 0) {
                  BigDecimal val = new BigDecimal((double)0.0F);
                  return val.toString();
               }

               BigDecimal val;
               try {
                  val = new BigDecimal(stringVal);
               } catch (NumberFormatException var20) {
                  throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[]{stringVal, Constants.integerValueOf(columnIndex)}), "S1009");
               }

               return val.toString();
            }

            this.wasNullFlag = true;
            return null;
         case 4:
            int intVal = this.getNativeInt(columnIndex, false);
            if (this.wasNullFlag) {
               return null;
            } else {
               if (field.isUnsigned() && intVal < 0 && field.getMysqlType() != 9) {
                  long longVal = (long)intVal & 4294967295L;
                  return String.valueOf(longVal);
               }

               return String.valueOf(intVal);
            }
         case 5:
            int intVal = this.getNativeInt(columnIndex, false);
            if (this.wasNullFlag) {
               return null;
            } else {
               if (field.isUnsigned() && intVal < 0) {
                  intVal &= 65535;
                  return String.valueOf(intVal);
               }

               return String.valueOf(intVal);
            }
         case 6:
         case 8:
            double doubleVal = this.getNativeDouble(columnIndex);
            if (this.wasNullFlag) {
               return null;
            }

            return String.valueOf(doubleVal);
         case 7:
            float floatVal = this.getNativeFloat(columnIndex);
            if (this.wasNullFlag) {
               return null;
            }

            return String.valueOf(floatVal);
         case 16:
            boolean booleanVal = this.getBoolean(columnIndex);
            if (this.wasNullFlag) {
               return null;
            }

            return String.valueOf(booleanVal);
         case 91:
            if (mysqlType == 13) {
               short shortVal = this.getNativeShort(columnIndex);
               if (!this.connection.getYearIsDateType()) {
                  if (this.wasNullFlag) {
                     return null;
                  }

                  return String.valueOf(shortVal);
               }

               if (field.getLength() == 2L) {
                  if (shortVal <= 69) {
                     shortVal = (short)(shortVal + 100);
                  }

                  shortVal = (short)(shortVal + 1900);
               }

               return this.fastDateCreate((Calendar)null, shortVal, 1, 1).toString();
            } else {
               Date dt = this.getNativeDate(columnIndex);
               if (dt == null) {
                  return null;
               }

               return String.valueOf(dt);
            }
         case 92:
            Time tm = this.getNativeTime(columnIndex, (Calendar)null, this.defaultTimeZone, false);
            if (tm == null) {
               return null;
            }

            return String.valueOf(tm);
         case 93:
            Timestamp tstamp = this.getNativeTimestamp(columnIndex, (Calendar)null, this.defaultTimeZone, false);
            if (tstamp == null) {
               return null;
            } else {
               String result = String.valueOf(tstamp);
               if (!this.connection.getNoDatetimeStringSync()) {
                  return result;
               } else if (result.endsWith(".0")) {
                  return result.substring(0, result.length() - 2);
               }
            }
         default:
            return this.extractStringFromNativeColumn(columnIndex, mysqlType);
      }
   }

   protected Date getNativeDate(int columnIndex) throws SQLException {
      return this.getNativeDate(columnIndex, (TimeZone)null);
   }

   protected Date getNativeDate(int columnIndex, TimeZone tz) throws SQLException {
      this.checkRowPos();
      this.checkColumnBounds(columnIndex);
      int columnIndexMinusOne = columnIndex - 1;
      int mysqlType = this.fields[columnIndexMinusOne].getMysqlType();
      Date dateToReturn = null;
      if (mysqlType == 10) {
         dateToReturn = this.thisRow.getNativeDate(columnIndexMinusOne, this.connection, this);
      } else {
         boolean rollForward = tz != null && !tz.equals(this.getDefaultTimeZone());
         dateToReturn = (Date)this.thisRow.getNativeDateTimeValue(columnIndexMinusOne, (Calendar)null, 91, mysqlType, tz, rollForward, this.connection, this);
      }

      if (dateToReturn == null) {
         this.wasNullFlag = true;
         return null;
      } else {
         this.wasNullFlag = false;
         return dateToReturn;
      }
   }

   Date getNativeDateViaParseConversion(int columnIndex) throws SQLException {
      if (this.useUsageAdvisor) {
         this.issueConversionViaParsingWarning("getDate()", columnIndex, this.thisRow.getColumnValue(columnIndex - 1), this.fields[columnIndex - 1], new int[]{10});
      }

      String stringVal = this.getNativeString(columnIndex);
      return this.getDateFromString(stringVal, columnIndex, (Calendar)null);
   }

   protected double getNativeDouble(int columnIndex) throws SQLException {
      this.checkRowPos();
      this.checkColumnBounds(columnIndex);
      --columnIndex;
      if (this.thisRow.isNull(columnIndex)) {
         this.wasNullFlag = true;
         return (double)0.0F;
      } else {
         this.wasNullFlag = false;
         Field f = this.fields[columnIndex];
         switch (f.getMysqlType()) {
            case 1:
               if (!f.isUnsigned()) {
                  return (double)this.getNativeByte(columnIndex + 1);
               }

               return (double)this.getNativeShort(columnIndex + 1);
            case 2:
            case 13:
               if (!f.isUnsigned()) {
                  return (double)this.getNativeShort(columnIndex + 1);
               }

               return (double)this.getNativeInt(columnIndex + 1);
            case 3:
            case 9:
               if (!f.isUnsigned()) {
                  return (double)this.getNativeInt(columnIndex + 1);
               }

               return (double)this.getNativeLong(columnIndex + 1);
            case 4:
               return (double)this.getNativeFloat(columnIndex + 1);
            case 5:
               return this.thisRow.getNativeDouble(columnIndex);
            case 6:
            case 7:
            case 10:
            case 11:
            case 12:
            case 14:
            case 15:
            default:
               String stringVal = this.getNativeString(columnIndex + 1);
               if (this.useUsageAdvisor) {
                  this.issueConversionViaParsingWarning("getDouble()", columnIndex, stringVal, this.fields[columnIndex], new int[]{5, 1, 2, 3, 8, 4});
               }

               return this.getDoubleFromString(stringVal, columnIndex + 1);
            case 8:
               long valueAsLong = this.getNativeLong(columnIndex + 1);
               if (!f.isUnsigned()) {
                  return (double)valueAsLong;
               }

               BigInteger asBigInt = convertLongToUlong(valueAsLong);
               return asBigInt.doubleValue();
            case 16:
               return (double)this.getNumericRepresentationOfSQLBitType(columnIndex + 1);
         }
      }
   }

   protected float getNativeFloat(int columnIndex) throws SQLException {
      this.checkRowPos();
      this.checkColumnBounds(columnIndex);
      --columnIndex;
      if (this.thisRow.isNull(columnIndex)) {
         this.wasNullFlag = true;
         return 0.0F;
      } else {
         this.wasNullFlag = false;
         Field f = this.fields[columnIndex];
         switch (f.getMysqlType()) {
            case 1:
               if (!f.isUnsigned()) {
                  return (float)this.getNativeByte(columnIndex + 1);
               }

               return (float)this.getNativeShort(columnIndex + 1);
            case 2:
            case 13:
               if (!f.isUnsigned()) {
                  return (float)this.getNativeShort(columnIndex + 1);
               }

               return (float)this.getNativeInt(columnIndex + 1);
            case 3:
            case 9:
               if (!f.isUnsigned()) {
                  return (float)this.getNativeInt(columnIndex + 1);
               }

               return (float)this.getNativeLong(columnIndex + 1);
            case 4:
               return this.thisRow.getNativeFloat(columnIndex);
            case 5:
               Double valueAsDouble = new Double(this.getNativeDouble(columnIndex + 1));
               float valueAsFloat = valueAsDouble.floatValue();
               if (this.jdbcCompliantTruncationForReads && valueAsFloat == Float.NEGATIVE_INFINITY || valueAsFloat == Float.POSITIVE_INFINITY) {
                  this.throwRangeException(valueAsDouble.toString(), columnIndex + 1, 6);
               }

               return (float)this.getNativeDouble(columnIndex + 1);
            case 6:
            case 7:
            case 10:
            case 11:
            case 12:
            case 14:
            case 15:
            default:
               String stringVal = this.getNativeString(columnIndex + 1);
               if (this.useUsageAdvisor) {
                  this.issueConversionViaParsingWarning("getFloat()", columnIndex, stringVal, this.fields[columnIndex], new int[]{5, 1, 2, 3, 8, 4});
               }

               return this.getFloatFromString(stringVal, columnIndex + 1);
            case 8:
               long valueAsLong = this.getNativeLong(columnIndex + 1);
               if (!f.isUnsigned()) {
                  return (float)valueAsLong;
               }

               BigInteger asBigInt = convertLongToUlong(valueAsLong);
               return asBigInt.floatValue();
            case 16:
               long valueAsLong = this.getNumericRepresentationOfSQLBitType(columnIndex + 1);
               return (float)valueAsLong;
         }
      }
   }

   protected int getNativeInt(int columnIndex) throws SQLException {
      return this.getNativeInt(columnIndex, true);
   }

   protected int getNativeInt(int columnIndex, boolean overflowCheck) throws SQLException {
      this.checkRowPos();
      this.checkColumnBounds(columnIndex);
      --columnIndex;
      if (this.thisRow.isNull(columnIndex)) {
         this.wasNullFlag = true;
         return 0;
      } else {
         this.wasNullFlag = false;
         Field f = this.fields[columnIndex];
         switch (f.getMysqlType()) {
            case 1:
               byte tinyintVal = this.getNativeByte(columnIndex + 1, false);
               if (f.isUnsigned() && tinyintVal < 0) {
                  return tinyintVal + 256;
               }

               return tinyintVal;
            case 2:
            case 13:
               short asShort = this.getNativeShort(columnIndex + 1, false);
               if (f.isUnsigned() && asShort < 0) {
                  return asShort + 65536;
               }

               return asShort;
            case 3:
            case 9:
               int valueAsInt = this.thisRow.getNativeInt(columnIndex);
               if (!f.isUnsigned()) {
                  return valueAsInt;
               }

               long valueAsLong = valueAsInt >= 0 ? (long)valueAsInt : (long)valueAsInt + 4294967296L;
               if (overflowCheck && this.jdbcCompliantTruncationForReads && valueAsLong > 2147483647L) {
                  this.throwRangeException(String.valueOf(valueAsLong), columnIndex + 1, 4);
               }

               return (int)valueAsLong;
            case 4:
               double valueAsDouble = (double)this.getNativeFloat(columnIndex + 1);
               if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsDouble < (double)Integer.MIN_VALUE || valueAsDouble > (double)Integer.MAX_VALUE)) {
                  this.throwRangeException(String.valueOf(valueAsDouble), columnIndex + 1, 4);
               }

               return (int)valueAsDouble;
            case 5:
               double valueAsDouble = this.getNativeDouble(columnIndex + 1);
               if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsDouble < (double)Integer.MIN_VALUE || valueAsDouble > (double)Integer.MAX_VALUE)) {
                  this.throwRangeException(String.valueOf(valueAsDouble), columnIndex + 1, 4);
               }

               return (int)valueAsDouble;
            case 6:
            case 7:
            case 10:
            case 11:
            case 12:
            case 14:
            case 15:
            default:
               String stringVal = this.getNativeString(columnIndex + 1);
               if (this.useUsageAdvisor) {
                  this.issueConversionViaParsingWarning("getInt()", columnIndex, stringVal, this.fields[columnIndex], new int[]{5, 1, 2, 3, 8, 4});
               }

               return this.getIntFromString(stringVal, columnIndex + 1);
            case 8:
               long valueAsLong = this.getNativeLong(columnIndex + 1, false, true);
               if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsLong < -2147483648L || valueAsLong > 2147483647L)) {
                  this.throwRangeException(String.valueOf(valueAsLong), columnIndex + 1, 4);
               }

               return (int)valueAsLong;
            case 16:
               long valueAsLong = this.getNumericRepresentationOfSQLBitType(columnIndex + 1);
               if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsLong < -2147483648L || valueAsLong > 2147483647L)) {
                  this.throwRangeException(String.valueOf(valueAsLong), columnIndex + 1, 4);
               }

               return (short)((int)valueAsLong);
         }
      }
   }

   protected long getNativeLong(int columnIndex) throws SQLException {
      return this.getNativeLong(columnIndex, true, true);
   }

   protected long getNativeLong(int columnIndex, boolean overflowCheck, boolean expandUnsignedLong) throws SQLException {
      this.checkRowPos();
      this.checkColumnBounds(columnIndex);
      --columnIndex;
      if (this.thisRow.isNull(columnIndex)) {
         this.wasNullFlag = true;
         return 0L;
      } else {
         this.wasNullFlag = false;
         Field f = this.fields[columnIndex];
         switch (f.getMysqlType()) {
            case 1:
               if (!f.isUnsigned()) {
                  return (long)this.getNativeByte(columnIndex + 1);
               }

               return (long)this.getNativeInt(columnIndex + 1);
            case 2:
               if (!f.isUnsigned()) {
                  return (long)this.getNativeShort(columnIndex + 1);
               }

               return (long)this.getNativeInt(columnIndex + 1, false);
            case 3:
            case 9:
               int asInt = this.getNativeInt(columnIndex + 1, false);
               if (f.isUnsigned() && asInt < 0) {
                  return (long)asInt + 4294967296L;
               }

               return (long)asInt;
            case 4:
               double valueAsDouble = (double)this.getNativeFloat(columnIndex + 1);
               if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsDouble < (double)Long.MIN_VALUE || valueAsDouble > (double)Long.MAX_VALUE)) {
                  this.throwRangeException(String.valueOf(valueAsDouble), columnIndex + 1, -5);
               }

               return (long)valueAsDouble;
            case 5:
               double valueAsDouble = this.getNativeDouble(columnIndex + 1);
               if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsDouble < (double)Long.MIN_VALUE || valueAsDouble > (double)Long.MAX_VALUE)) {
                  this.throwRangeException(String.valueOf(valueAsDouble), columnIndex + 1, -5);
               }

               return (long)valueAsDouble;
            case 6:
            case 7:
            case 10:
            case 11:
            case 12:
            case 14:
            case 15:
            default:
               String stringVal = this.getNativeString(columnIndex + 1);
               if (this.useUsageAdvisor) {
                  this.issueConversionViaParsingWarning("getLong()", columnIndex, stringVal, this.fields[columnIndex], new int[]{5, 1, 2, 3, 8, 4});
               }

               return this.getLongFromString(stringVal, columnIndex + 1);
            case 8:
               long valueAsLong = this.thisRow.getNativeLong(columnIndex);
               if (f.isUnsigned() && expandUnsignedLong) {
                  BigInteger asBigInt = convertLongToUlong(valueAsLong);
                  if (overflowCheck && this.jdbcCompliantTruncationForReads && (asBigInt.compareTo(new BigInteger(String.valueOf(Long.MAX_VALUE))) > 0 || asBigInt.compareTo(new BigInteger(String.valueOf(Long.MIN_VALUE))) < 0)) {
                     this.throwRangeException(asBigInt.toString(), columnIndex + 1, -5);
                  }

                  return this.getLongFromString(asBigInt.toString(), columnIndex + 1);
               }

               return valueAsLong;
            case 13:
               return (long)this.getNativeShort(columnIndex + 1);
            case 16:
               return this.getNumericRepresentationOfSQLBitType(columnIndex + 1);
         }
      }
   }

   protected Ref getNativeRef(int i) throws SQLException {
      throw SQLError.notImplemented();
   }

   protected short getNativeShort(int columnIndex) throws SQLException {
      return this.getNativeShort(columnIndex, true);
   }

   protected short getNativeShort(int columnIndex, boolean overflowCheck) throws SQLException {
      this.checkRowPos();
      this.checkColumnBounds(columnIndex);
      --columnIndex;
      if (this.thisRow.isNull(columnIndex)) {
         this.wasNullFlag = true;
         return 0;
      } else {
         this.wasNullFlag = false;
         Field f = this.fields[columnIndex];
         switch (f.getMysqlType()) {
            case 1:
               byte tinyintVal = this.getNativeByte(columnIndex + 1, false);
               if (f.isUnsigned() && tinyintVal < 0) {
                  return (short)(tinyintVal + 256);
               }

               return (short)tinyintVal;
            case 2:
            case 13:
               short asShort = this.thisRow.getNativeShort(columnIndex);
               if (!f.isUnsigned()) {
                  return asShort;
               }

               int valueAsInt = asShort & '\uffff';
               if (overflowCheck && this.jdbcCompliantTruncationForReads && valueAsInt > 32767) {
                  this.throwRangeException(String.valueOf(valueAsInt), columnIndex + 1, 5);
               }

               return (short)valueAsInt;
            case 3:
            case 9:
               if (f.isUnsigned()) {
                  long valueAsLong = this.getNativeLong(columnIndex + 1, false, true);
                  if (overflowCheck && this.jdbcCompliantTruncationForReads && valueAsLong > 32767L) {
                     this.throwRangeException(String.valueOf(valueAsLong), columnIndex + 1, 5);
                  }

                  return (short)((int)valueAsLong);
               }

               int valueAsInt = this.getNativeInt(columnIndex + 1, false);
               if (overflowCheck && this.jdbcCompliantTruncationForReads && valueAsInt > 32767 || valueAsInt < -32768) {
                  this.throwRangeException(String.valueOf(valueAsInt), columnIndex + 1, 5);
               }

               return (short)valueAsInt;
            case 4:
               float valueAsFloat = this.getNativeFloat(columnIndex + 1);
               if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsFloat < -32768.0F || valueAsFloat > 32767.0F)) {
                  this.throwRangeException(String.valueOf(valueAsFloat), columnIndex + 1, 5);
               }

               return (short)((int)valueAsFloat);
            case 5:
               double valueAsDouble = this.getNativeDouble(columnIndex + 1);
               if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsDouble < (double)-32768.0F || valueAsDouble > (double)32767.0F)) {
                  this.throwRangeException(String.valueOf(valueAsDouble), columnIndex + 1, 5);
               }

               return (short)((int)valueAsDouble);
            case 6:
            case 7:
            case 10:
            case 11:
            case 12:
            default:
               String stringVal = this.getNativeString(columnIndex + 1);
               if (this.useUsageAdvisor) {
                  this.issueConversionViaParsingWarning("getShort()", columnIndex, stringVal, this.fields[columnIndex], new int[]{5, 1, 2, 3, 8, 4});
               }

               return this.getShortFromString(stringVal, columnIndex + 1);
            case 8:
               long valueAsLong = this.getNativeLong(columnIndex + 1, false, false);
               if (!f.isUnsigned()) {
                  if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsLong < -32768L || valueAsLong > 32767L)) {
                     this.throwRangeException(String.valueOf(valueAsLong), columnIndex + 1, 5);
                  }

                  return (short)((int)valueAsLong);
               } else {
                  BigInteger asBigInt = convertLongToUlong(valueAsLong);
                  if (overflowCheck && this.jdbcCompliantTruncationForReads && (asBigInt.compareTo(new BigInteger(String.valueOf(32767))) > 0 || asBigInt.compareTo(new BigInteger(String.valueOf(-32768))) < 0)) {
                     this.throwRangeException(asBigInt.toString(), columnIndex + 1, 5);
                  }

                  return (short)this.getIntFromString(asBigInt.toString(), columnIndex + 1);
               }
         }
      }
   }

   protected String getNativeString(int columnIndex) throws SQLException {
      this.checkRowPos();
      this.checkColumnBounds(columnIndex);
      if (this.fields == null) {
         throw SQLError.createSQLException(Messages.getString("ResultSet.Query_generated_no_fields_for_ResultSet_133"), "S1002");
      } else if (this.thisRow.isNull(columnIndex - 1)) {
         this.wasNullFlag = true;
         return null;
      } else {
         this.wasNullFlag = false;
         String stringVal = null;
         Field field = this.fields[columnIndex - 1];
         stringVal = this.getNativeConvertToString(columnIndex, field);
         if (field.isZeroFill() && stringVal != null) {
            int origLength = stringVal.length();
            StringBuffer zeroFillBuf = new StringBuffer(origLength);
            long numZeros = field.getLength() - (long)origLength;

            for(long i = 0L; i < numZeros; ++i) {
               zeroFillBuf.append('0');
            }

            zeroFillBuf.append(stringVal);
            stringVal = zeroFillBuf.toString();
         }

         return stringVal;
      }
   }

   private Time getNativeTime(int columnIndex, Calendar targetCalendar, TimeZone tz, boolean rollForward) throws SQLException {
      this.checkRowPos();
      this.checkColumnBounds(columnIndex);
      int columnIndexMinusOne = columnIndex - 1;
      int mysqlType = this.fields[columnIndexMinusOne].getMysqlType();
      Time timeVal = null;
      if (mysqlType == 11) {
         timeVal = this.thisRow.getNativeTime(columnIndexMinusOne, targetCalendar, tz, rollForward, this.connection, this);
      } else {
         timeVal = (Time)this.thisRow.getNativeDateTimeValue(columnIndexMinusOne, (Calendar)null, 92, mysqlType, tz, rollForward, this.connection, this);
      }

      if (timeVal == null) {
         this.wasNullFlag = true;
         return null;
      } else {
         this.wasNullFlag = false;
         return timeVal;
      }
   }

   Time getNativeTimeViaParseConversion(int columnIndex, Calendar targetCalendar, TimeZone tz, boolean rollForward) throws SQLException {
      if (this.useUsageAdvisor) {
         this.issueConversionViaParsingWarning("getTime()", columnIndex, this.thisRow.getColumnValue(columnIndex - 1), this.fields[columnIndex - 1], new int[]{11});
      }

      String strTime = this.getNativeString(columnIndex);
      return this.getTimeFromString(strTime, targetCalendar, columnIndex, tz, rollForward);
   }

   private Timestamp getNativeTimestamp(int columnIndex, Calendar targetCalendar, TimeZone tz, boolean rollForward) throws SQLException {
      this.checkRowPos();
      this.checkColumnBounds(columnIndex);
      int columnIndexMinusOne = columnIndex - 1;
      Timestamp tsVal = null;
      int mysqlType = this.fields[columnIndexMinusOne].getMysqlType();
      switch (mysqlType) {
         case 7:
         case 12:
            tsVal = this.thisRow.getNativeTimestamp(columnIndexMinusOne, targetCalendar, tz, rollForward, this.connection, this);
            break;
         default:
            tsVal = (Timestamp)this.thisRow.getNativeDateTimeValue(columnIndexMinusOne, (Calendar)null, 93, mysqlType, tz, rollForward, this.connection, this);
      }

      if (tsVal == null) {
         this.wasNullFlag = true;
         return null;
      } else {
         this.wasNullFlag = false;
         return tsVal;
      }
   }

   Timestamp getNativeTimestampViaParseConversion(int columnIndex, Calendar targetCalendar, TimeZone tz, boolean rollForward) throws SQLException {
      if (this.useUsageAdvisor) {
         this.issueConversionViaParsingWarning("getTimestamp()", columnIndex, this.thisRow.getColumnValue(columnIndex - 1), this.fields[columnIndex - 1], new int[]{7, 12});
      }

      String strTimestamp = this.getNativeString(columnIndex);
      return this.getTimestampFromString(columnIndex, targetCalendar, strTimestamp, tz, rollForward);
   }

   protected InputStream getNativeUnicodeStream(int columnIndex) throws SQLException {
      this.checkRowPos();
      return this.getBinaryStream(columnIndex);
   }

   protected URL getNativeURL(int colIndex) throws SQLException {
      String val = this.getString(colIndex);
      if (val == null) {
         return null;
      } else {
         try {
            return new URL(val);
         } catch (MalformedURLException var4) {
            throw SQLError.createSQLException(Messages.getString("ResultSet.Malformed_URL____141") + val + "'", "S1009");
         }
      }
   }

   public ResultSetInternalMethods getNextResultSet() {
      return this.nextResultSet;
   }

   public Object getObject(int columnIndex) throws SQLException {
      this.checkRowPos();
      this.checkColumnBounds(columnIndex);
      int columnIndexMinusOne = columnIndex - 1;
      if (this.thisRow.isNull(columnIndexMinusOne)) {
         this.wasNullFlag = true;
         return null;
      } else {
         this.wasNullFlag = false;
         Field field = this.fields[columnIndexMinusOne];
         switch (field.getSQLType()) {
            case -7:
            case 16:
               if (field.getMysqlType() == 16 && !field.isSingleBit()) {
                  return this.getBytes(columnIndex);
               }

               return this.getBoolean(columnIndex);
            case -6:
               if (!field.isUnsigned()) {
                  return Constants.integerValueOf(this.getByte(columnIndex));
               }

               return Constants.integerValueOf(this.getInt(columnIndex));
            case -5:
               if (!field.isUnsigned()) {
                  return Constants.longValueOf(this.getLong(columnIndex));
               } else {
                  String stringVal = this.getString(columnIndex);
                  if (stringVal == null) {
                     return null;
                  } else {
                     try {
                        return new BigInteger(stringVal);
                     } catch (NumberFormatException var13) {
                        throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_BigInteger", new Object[]{Constants.integerValueOf(columnIndex), stringVal}), "S1009");
                     }
                  }
               }
            case -4:
            case -3:
            case -2:
               if (field.getMysqlType() == 255) {
                  return this.getBytes(columnIndex);
               } else if (!field.isBinary() && !field.isBlob()) {
                  return this.getBytes(columnIndex);
               } else {
                  byte[] data = this.getBytes(columnIndex);
                  if (!this.connection.getAutoDeserialize()) {
                     return data;
                  } else {
                     Object obj = data;
                     if (data != null && data.length >= 2) {
                        if (data[0] != -84 || data[1] != -19) {
                           return this.getString(columnIndex);
                        }

                        try {
                           ByteArrayInputStream bytesIn = new ByteArrayInputStream(data);
                           ObjectInputStream objIn = new ObjectInputStream(bytesIn);
                           obj = objIn.readObject();
                           objIn.close();
                           bytesIn.close();
                        } catch (ClassNotFoundException cnfe) {
                           throw SQLError.createSQLException(Messages.getString("ResultSet.Class_not_found___91") + cnfe.toString() + Messages.getString("ResultSet._while_reading_serialized_object_92"));
                        } catch (IOException var12) {
                           obj = data;
                        }
                     }

                     return obj;
                  }
               }
            case -1:
               if (!field.isOpaqueBinary()) {
                  return this.getStringForClob(columnIndex);
               }

               return this.getBytes(columnIndex);
            case 1:
            case 12:
               if (!field.isOpaqueBinary()) {
                  return this.getString(columnIndex);
               }

               return this.getBytes(columnIndex);
            case 2:
            case 3:
               String stringVal = this.getString(columnIndex);
               if (stringVal != null) {
                  if (stringVal.length() == 0) {
                     BigDecimal val = new BigDecimal((double)0.0F);
                     return val;
                  }

                  try {
                     BigDecimal val = new BigDecimal(stringVal);
                     return val;
                  } catch (NumberFormatException var10) {
                     throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[]{stringVal, new Integer(columnIndex)}), "S1009");
                  }
               }

               return null;
            case 4:
               if (field.isUnsigned() && field.getMysqlType() != 9) {
                  return Constants.longValueOf(this.getLong(columnIndex));
               }

               return Constants.integerValueOf(this.getInt(columnIndex));
            case 5:
               return Constants.integerValueOf(this.getInt(columnIndex));
            case 6:
            case 8:
               return new Double(this.getDouble(columnIndex));
            case 7:
               return new Float(this.getFloat(columnIndex));
            case 91:
               if (field.getMysqlType() == 13 && !this.connection.getYearIsDateType()) {
                  return Constants.shortValueOf(this.getShort(columnIndex));
               }

               return this.getDate(columnIndex);
            case 92:
               return this.getTime(columnIndex);
            case 93:
               return this.getTimestamp(columnIndex);
            default:
               return this.getString(columnIndex);
         }
      }
   }

   public Object getObject(int i, Map map) throws SQLException {
      return this.getObject(i);
   }

   public Object getObject(String columnName) throws SQLException {
      return this.getObject(this.findColumn(columnName));
   }

   public Object getObject(String colName, Map map) throws SQLException {
      return this.getObject(this.findColumn(colName), map);
   }

   public Object getObjectStoredProc(int columnIndex, int desiredSqlType) throws SQLException {
      this.checkRowPos();
      this.checkColumnBounds(columnIndex);
      Object value = this.thisRow.getColumnValue(columnIndex - 1);
      if (value == null) {
         this.wasNullFlag = true;
         return null;
      } else {
         this.wasNullFlag = false;
         Field field = this.fields[columnIndex - 1];
         switch (desiredSqlType) {
            case -7:
            case 16:
               return this.getBoolean(columnIndex);
            case -6:
               return Constants.integerValueOf(this.getInt(columnIndex));
            case -5:
               if (field.isUnsigned()) {
                  return this.getBigDecimal(columnIndex);
               }

               return Constants.longValueOf(this.getLong(columnIndex));
            case -4:
            case -3:
            case -2:
               return this.getBytes(columnIndex);
            case -1:
               return this.getStringForClob(columnIndex);
            case 1:
            case 12:
               return this.getString(columnIndex);
            case 2:
            case 3:
               String stringVal = this.getString(columnIndex);
               if (stringVal != null) {
                  if (stringVal.length() == 0) {
                     BigDecimal val = new BigDecimal((double)0.0F);
                     return val;
                  }

                  try {
                     BigDecimal val = new BigDecimal(stringVal);
                     return val;
                  } catch (NumberFormatException var8) {
                     throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[]{stringVal, new Integer(columnIndex)}), "S1009");
                  }
               }

               return null;
            case 4:
               if (field.isUnsigned() && field.getMysqlType() != 9) {
                  return Constants.longValueOf(this.getLong(columnIndex));
               }

               return Constants.integerValueOf(this.getInt(columnIndex));
            case 5:
               return Constants.integerValueOf(this.getInt(columnIndex));
            case 6:
               if (!this.connection.getRunningCTS13()) {
                  return new Double((double)this.getFloat(columnIndex));
               }

               return new Float(this.getFloat(columnIndex));
            case 7:
               return new Float(this.getFloat(columnIndex));
            case 8:
               return new Double(this.getDouble(columnIndex));
            case 91:
               if (field.getMysqlType() == 13 && !this.connection.getYearIsDateType()) {
                  return Constants.shortValueOf(this.getShort(columnIndex));
               }

               return this.getDate(columnIndex);
            case 92:
               return this.getTime(columnIndex);
            case 93:
               return this.getTimestamp(columnIndex);
            default:
               return this.getString(columnIndex);
         }
      }
   }

   public Object getObjectStoredProc(int i, Map map, int desiredSqlType) throws SQLException {
      return this.getObjectStoredProc(i, desiredSqlType);
   }

   public Object getObjectStoredProc(String columnName, int desiredSqlType) throws SQLException {
      return this.getObjectStoredProc(this.findColumn(columnName), desiredSqlType);
   }

   public Object getObjectStoredProc(String colName, Map map, int desiredSqlType) throws SQLException {
      return this.getObjectStoredProc(this.findColumn(colName), map, desiredSqlType);
   }

   public Ref getRef(int i) throws SQLException {
      this.checkColumnBounds(i);
      throw SQLError.notImplemented();
   }

   public Ref getRef(String colName) throws SQLException {
      return this.getRef(this.findColumn(colName));
   }

   public int getRow() throws SQLException {
      this.checkClosed();
      int currentRowNumber = this.rowData.getCurrentRowNumber();
      int row = 0;
      if (!this.rowData.isDynamic()) {
         if (currentRowNumber >= 0 && !this.rowData.isAfterLast() && !this.rowData.isEmpty()) {
            row = currentRowNumber + 1;
         } else {
            row = 0;
         }
      } else {
         row = currentRowNumber + 1;
      }

      return row;
   }

   public String getServerInfo() {
      return this.serverInfo;
   }

   private long getNumericRepresentationOfSQLBitType(int columnIndex) throws SQLException {
      Object value = this.thisRow.getColumnValue(columnIndex - 1);
      if (!this.fields[columnIndex - 1].isSingleBit() && ((byte[])value).length != 1) {
         byte[] asBytes = (byte[])value;
         int shift = 0;
         long[] steps = new long[asBytes.length];

         for(int i = asBytes.length - 1; i >= 0; --i) {
            steps[i] = (long)(asBytes[i] & 255) << shift;
            shift += 8;
         }

         long valueAsLong = 0L;

         for(int i = 0; i < asBytes.length; ++i) {
            valueAsLong |= steps[i];
         }

         return valueAsLong;
      } else {
         return (long)((byte[])value)[0];
      }
   }

   public short getShort(int columnIndex) throws SQLException {
      if (this.isBinaryEncoded) {
         return this.getNativeShort(columnIndex);
      } else {
         this.checkRowPos();
         if (this.useFastIntParsing) {
            this.checkColumnBounds(columnIndex);
            Object value = this.thisRow.getColumnValue(columnIndex - 1);
            if (value == null) {
               this.wasNullFlag = true;
            } else {
               this.wasNullFlag = false;
            }

            if (this.wasNullFlag) {
               return 0;
            }

            byte[] shortAsBytes = (byte[])value;
            if (shortAsBytes.length == 0) {
               return (short)this.convertToZeroWithEmptyCheck();
            }

            boolean needsFullParse = false;

            for(int i = 0; i < shortAsBytes.length; ++i) {
               if ((char)shortAsBytes[i] == 'e' || (char)shortAsBytes[i] == 'E') {
                  needsFullParse = true;
                  break;
               }
            }

            if (!needsFullParse) {
               try {
                  return this.parseShortWithOverflowCheck(columnIndex, shortAsBytes, (String)null);
               } catch (NumberFormatException var11) {
                  try {
                     return this.parseShortAsDouble(columnIndex, new String(shortAsBytes));
                  } catch (NumberFormatException var10) {
                     if (this.fields[columnIndex - 1].getMysqlType() == 16) {
                        long valueAsLong = this.getNumericRepresentationOfSQLBitType(columnIndex);
                        if (this.jdbcCompliantTruncationForReads && (valueAsLong < -32768L || valueAsLong > 32767L)) {
                           this.throwRangeException(String.valueOf(valueAsLong), columnIndex, 5);
                        }

                        return (short)((int)valueAsLong);
                     }

                     throw SQLError.createSQLException(Messages.getString("ResultSet.Invalid_value_for_getShort()_-____96") + new String(shortAsBytes) + "'", "S1009");
                  }
               }
            }
         }

         String val = null;

         try {
            val = this.getString(columnIndex);
            if (val != null) {
               if (val.length() == 0) {
                  return (short)this.convertToZeroWithEmptyCheck();
               } else {
                  return val.indexOf("e") == -1 && val.indexOf("E") == -1 && val.indexOf(".") == -1 ? this.parseShortWithOverflowCheck(columnIndex, (byte[])null, val) : this.parseShortAsDouble(columnIndex, val);
               }
            } else {
               return 0;
            }
         } catch (NumberFormatException var9) {
            try {
               return this.parseShortAsDouble(columnIndex, val);
            } catch (NumberFormatException var8) {
               if (this.fields[columnIndex - 1].getMysqlType() == 16) {
                  long valueAsLong = this.getNumericRepresentationOfSQLBitType(columnIndex);
                  if (this.jdbcCompliantTruncationForReads && (valueAsLong < -32768L || valueAsLong > 32767L)) {
                     this.throwRangeException(String.valueOf(valueAsLong), columnIndex, 5);
                  }

                  return (short)((int)valueAsLong);
               } else {
                  throw SQLError.createSQLException(Messages.getString("ResultSet.Invalid_value_for_getShort()_-____96") + val + "'", "S1009");
               }
            }
         }
      }
   }

   public short getShort(String columnName) throws SQLException {
      return this.getShort(this.findColumn(columnName));
   }

   private final short getShortFromString(String val, int columnIndex) throws SQLException {
      try {
         if (val != null) {
            if (val.length() == 0) {
               return (short)this.convertToZeroWithEmptyCheck();
            } else {
               return val.indexOf("e") == -1 && val.indexOf("E") == -1 && val.indexOf(".") == -1 ? this.parseShortWithOverflowCheck(columnIndex, (byte[])null, val) : this.parseShortAsDouble(columnIndex, val);
            }
         } else {
            return 0;
         }
      } catch (NumberFormatException var6) {
         try {
            return this.parseShortAsDouble(columnIndex, val);
         } catch (NumberFormatException var5) {
            throw SQLError.createSQLException(Messages.getString("ResultSet.Invalid_value_for_getShort()_-____217") + val + Messages.getString("ResultSet.___in_column__218") + columnIndex, "S1009");
         }
      }
   }

   public java.sql.Statement getStatement() throws SQLException {
      if (this.isClosed && !this.retainOwningStatement) {
         throw SQLError.createSQLException("Operation not allowed on closed ResultSet. Statements can be retained over result set closure by setting the connection property \"retainStatementAfterResultSetClose\" to \"true\".", "S1000");
      } else {
         return (java.sql.Statement)(this.wrapperStatement != null ? this.wrapperStatement : this.owningStatement);
      }
   }

   public String getString(int columnIndex) throws SQLException {
      String stringVal = this.getStringInternal(columnIndex, true);
      if (this.padCharsWithSpace) {
         Field f = this.fields[columnIndex - 1];
         if (f.getMysqlType() == 254) {
            int fieldLength = (int)f.getLength() / f.getMaxBytesPerCharacter();
            int currentLength = stringVal.length();
            if (currentLength < fieldLength) {
               StringBuffer paddedBuf = new StringBuffer(fieldLength);
               paddedBuf.append(stringVal);
               int difference = fieldLength - currentLength;
               paddedBuf.append(EMPTY_SPACE, 0, difference);
               stringVal = paddedBuf.toString();
            }
         }
      }

      return stringVal;
   }

   public String getString(String columnName) throws SQLException {
      return this.getString(this.findColumn(columnName));
   }

   private String getStringForClob(int columnIndex) throws SQLException {
      String asString = null;
      String forcedEncoding = this.connection.getClobCharacterEncoding();
      if (forcedEncoding == null) {
         if (!this.isBinaryEncoded) {
            asString = this.getString(columnIndex);
         } else {
            asString = this.getNativeString(columnIndex);
         }
      } else {
         try {
            byte[] asBytes = null;
            if (!this.isBinaryEncoded) {
               asBytes = this.getBytes(columnIndex);
            } else {
               asBytes = this.getNativeBytes(columnIndex, true);
            }

            if (asBytes != null) {
               asString = new String(asBytes, forcedEncoding);
            }
         } catch (UnsupportedEncodingException var5) {
            throw SQLError.createSQLException("Unsupported character encoding " + forcedEncoding, "S1009");
         }
      }

      return asString;
   }

   protected String getStringInternal(int columnIndex, boolean checkDateTypes) throws SQLException {
      if (!this.isBinaryEncoded) {
         this.checkRowPos();
         this.checkColumnBounds(columnIndex);
         if (this.fields == null) {
            throw SQLError.createSQLException(Messages.getString("ResultSet.Query_generated_no_fields_for_ResultSet_99"), "S1002");
         } else {
            int internalColumnIndex = columnIndex - 1;
            if (this.thisRow.isNull(internalColumnIndex)) {
               this.wasNullFlag = true;
               return null;
            } else {
               this.wasNullFlag = false;
               Field metadata = this.fields[internalColumnIndex];
               String stringVal = null;
               if (metadata.getMysqlType() == 16) {
                  if (metadata.isSingleBit()) {
                     byte[] value = this.thisRow.getColumnValue(internalColumnIndex);
                     return value.length == 0 ? String.valueOf(this.convertToZeroWithEmptyCheck()) : String.valueOf(value[0]);
                  } else {
                     return String.valueOf(this.getNumericRepresentationOfSQLBitType(columnIndex));
                  }
               } else {
                  String encoding = metadata.getCharacterSet();
                  stringVal = this.thisRow.getString(internalColumnIndex, encoding, this.connection);
                  if (metadata.getMysqlType() == 13) {
                     if (!this.connection.getYearIsDateType()) {
                        return stringVal;
                     } else {
                        Date dt = this.getDateFromString(stringVal, columnIndex, (Calendar)null);
                        if (dt == null) {
                           this.wasNullFlag = true;
                           return null;
                        } else {
                           this.wasNullFlag = false;
                           return dt.toString();
                        }
                     }
                  } else {
                     if (checkDateTypes && !this.connection.getNoDatetimeStringSync()) {
                        switch (metadata.getSQLType()) {
                           case 91:
                              Date dt = this.getDateFromString(stringVal, columnIndex, (Calendar)null);
                              if (dt == null) {
                                 this.wasNullFlag = true;
                                 return null;
                              }

                              this.wasNullFlag = false;
                              return dt.toString();
                           case 92:
                              Time tm = this.getTimeFromString(stringVal, (Calendar)null, columnIndex, this.getDefaultTimeZone(), false);
                              if (tm == null) {
                                 this.wasNullFlag = true;
                                 return null;
                              }

                              this.wasNullFlag = false;
                              return tm.toString();
                           case 93:
                              Timestamp ts = this.getTimestampFromString(columnIndex, (Calendar)null, stringVal, this.getDefaultTimeZone(), false);
                              if (ts == null) {
                                 this.wasNullFlag = true;
                                 return null;
                              }

                              this.wasNullFlag = false;
                              return ts.toString();
                        }
                     }

                     return stringVal;
                  }
               }
            }
         }
      } else {
         return this.getNativeString(columnIndex);
      }
   }

   public Time getTime(int columnIndex) throws SQLException {
      return this.getTimeInternal(columnIndex, (Calendar)null, this.getDefaultTimeZone(), false);
   }

   public Time getTime(int columnIndex, Calendar cal) throws SQLException {
      return this.getTimeInternal(columnIndex, cal, cal.getTimeZone(), true);
   }

   public Time getTime(String columnName) throws SQLException {
      return this.getTime(this.findColumn(columnName));
   }

   public Time getTime(String columnName, Calendar cal) throws SQLException {
      return this.getTime(this.findColumn(columnName), cal);
   }

   private Time getTimeFromString(String timeAsString, Calendar targetCalendar, int columnIndex, TimeZone tz, boolean rollForward) throws SQLException {
      int hr = 0;
      int min = 0;
      int sec = 0;

      try {
         if (timeAsString == null) {
            this.wasNullFlag = true;
            return null;
         } else {
            timeAsString = timeAsString.trim();
            if (!timeAsString.equals("0") && !timeAsString.equals("0000-00-00") && !timeAsString.equals("0000-00-00 00:00:00") && !timeAsString.equals("00000000000000")) {
               this.wasNullFlag = false;
               Field timeColField = this.fields[columnIndex - 1];
               if (timeColField.getMysqlType() == 7) {
                  int length = timeAsString.length();
                  switch (length) {
                     case 10:
                        hr = Integer.parseInt(timeAsString.substring(6, 8));
                        min = Integer.parseInt(timeAsString.substring(8, 10));
                        sec = 0;
                        break;
                     case 11:
                     case 13:
                     case 15:
                     case 16:
                     case 17:
                     case 18:
                     default:
                        throw SQLError.createSQLException(Messages.getString("ResultSet.Timestamp_too_small_to_convert_to_Time_value_in_column__257") + columnIndex + "(" + this.fields[columnIndex - 1] + ").", "S1009");
                     case 12:
                     case 14:
                        hr = Integer.parseInt(timeAsString.substring(length - 6, length - 4));
                        min = Integer.parseInt(timeAsString.substring(length - 4, length - 2));
                        sec = Integer.parseInt(timeAsString.substring(length - 2, length));
                        break;
                     case 19:
                        hr = Integer.parseInt(timeAsString.substring(length - 8, length - 6));
                        min = Integer.parseInt(timeAsString.substring(length - 5, length - 3));
                        sec = Integer.parseInt(timeAsString.substring(length - 2, length));
                  }

                  SQLWarning precisionLost = new SQLWarning(Messages.getString("ResultSet.Precision_lost_converting_TIMESTAMP_to_Time_with_getTime()_on_column__261") + columnIndex + "(" + this.fields[columnIndex - 1] + ").");
                  if (this.warningChain == null) {
                     this.warningChain = precisionLost;
                  } else {
                     this.warningChain.setNextWarning(precisionLost);
                  }
               } else if (timeColField.getMysqlType() == 12) {
                  hr = Integer.parseInt(timeAsString.substring(11, 13));
                  min = Integer.parseInt(timeAsString.substring(14, 16));
                  sec = Integer.parseInt(timeAsString.substring(17, 19));
                  SQLWarning precisionLost = new SQLWarning(Messages.getString("ResultSet.Precision_lost_converting_DATETIME_to_Time_with_getTime()_on_column__264") + columnIndex + "(" + this.fields[columnIndex - 1] + ").");
                  if (this.warningChain == null) {
                     this.warningChain = precisionLost;
                  } else {
                     this.warningChain.setNextWarning(precisionLost);
                  }
               } else {
                  if (timeColField.getMysqlType() == 10) {
                     return this.fastTimeCreate(targetCalendar, 0, 0, 0);
                  }

                  if (timeAsString.length() != 5 && timeAsString.length() != 8) {
                     throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_Time____267") + timeAsString + Messages.getString("ResultSet.___in_column__268") + columnIndex, "S1009");
                  }

                  hr = Integer.parseInt(timeAsString.substring(0, 2));
                  min = Integer.parseInt(timeAsString.substring(3, 5));
                  sec = timeAsString.length() == 5 ? 0 : Integer.parseInt(timeAsString.substring(6));
               }

               Calendar sessionCalendar = this.getCalendarInstanceForSessionOrNew();
               synchronized(sessionCalendar) {
                  return TimeUtil.changeTimezone(this.connection, sessionCalendar, targetCalendar, this.fastTimeCreate(sessionCalendar, hr, min, sec), this.connection.getServerTimezoneTZ(), tz, rollForward);
               }
            } else if ("convertToNull".equals(this.connection.getZeroDateTimeBehavior())) {
               this.wasNullFlag = true;
               return null;
            } else if ("exception".equals(this.connection.getZeroDateTimeBehavior())) {
               throw SQLError.createSQLException("Value '" + timeAsString + "' can not be represented as java.sql.Time", "S1009");
            } else {
               return this.fastTimeCreate(targetCalendar, 0, 0, 0);
            }
         }
      } catch (Exception ex) {
         SQLException sqlEx = SQLError.createSQLException(ex.toString(), "S1009");
         sqlEx.initCause(ex);
         throw sqlEx;
      }
   }

   private Time getTimeInternal(int columnIndex, Calendar targetCalendar, TimeZone tz, boolean rollForward) throws SQLException {
      if (this.isBinaryEncoded) {
         return this.getNativeTime(columnIndex, targetCalendar, tz, rollForward);
      } else if (!this.useFastDateParsing) {
         String timeAsString = this.getStringInternal(columnIndex, false);
         return this.getTimeFromString(timeAsString, targetCalendar, columnIndex, tz, rollForward);
      } else {
         this.checkColumnBounds(columnIndex);
         int columnIndexMinusOne = columnIndex - 1;
         if (this.thisRow.isNull(columnIndexMinusOne)) {
            this.wasNullFlag = true;
            return null;
         } else {
            this.wasNullFlag = false;
            return this.thisRow.getTimeFast(columnIndexMinusOne, targetCalendar, tz, rollForward, this.connection, this);
         }
      }
   }

   public Timestamp getTimestamp(int columnIndex) throws SQLException {
      return this.getTimestampInternal(columnIndex, (Calendar)null, this.getDefaultTimeZone(), false);
   }

   public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
      return this.getTimestampInternal(columnIndex, cal, cal.getTimeZone(), true);
   }

   public Timestamp getTimestamp(String columnName) throws SQLException {
      return this.getTimestamp(this.findColumn(columnName));
   }

   public Timestamp getTimestamp(String columnName, Calendar cal) throws SQLException {
      return this.getTimestamp(this.findColumn(columnName), cal);
   }

   private Timestamp getTimestampFromString(int columnIndex, Calendar targetCalendar, String timestampValue, TimeZone tz, boolean rollForward) throws SQLException {
      try {
         this.wasNullFlag = false;
         if (timestampValue == null) {
            this.wasNullFlag = true;
            return null;
         } else {
            timestampValue = timestampValue.trim();
            int length = timestampValue.length();
            Calendar sessionCalendar = this.connection.getUseJDBCCompliantTimezoneShift() ? this.connection.getUtcCalendar() : this.getCalendarInstanceForSessionOrNew();
            synchronized(sessionCalendar) {
               if (length <= 0 || timestampValue.charAt(0) != '0' || !timestampValue.equals("0000-00-00") && !timestampValue.equals("0000-00-00 00:00:00") && !timestampValue.equals("00000000000000") && !timestampValue.equals("0")) {
                  if (this.fields[columnIndex - 1].getMysqlType() == 13) {
                     return !this.useLegacyDatetimeCode ? TimeUtil.fastTimestampCreate(tz, Integer.parseInt(timestampValue.substring(0, 4)), 1, 1, 0, 0, 0, 0) : TimeUtil.changeTimezone(this.connection, sessionCalendar, targetCalendar, this.fastTimestampCreate(sessionCalendar, Integer.parseInt(timestampValue.substring(0, 4)), 1, 1, 0, 0, 0, 0), this.connection.getServerTimezoneTZ(), tz, rollForward);
                  } else {
                     if (timestampValue.endsWith(".")) {
                        timestampValue = timestampValue.substring(0, timestampValue.length() - 1);
                     }

                     int year = 0;
                     int month = 0;
                     int day = 0;
                     int hour = 0;
                     int minutes = 0;
                     int seconds = 0;
                     int nanos = 0;
                     switch (length) {
                        case 2:
                           year = Integer.parseInt(timestampValue.substring(0, 2));
                           if (year <= 69) {
                              year += 100;
                           }

                           year += 1900;
                           month = 1;
                           day = 1;
                           break;
                        case 3:
                        case 5:
                        case 7:
                        case 9:
                        case 11:
                        case 13:
                        case 15:
                        case 16:
                        case 17:
                        case 18:
                        default:
                           throw new SQLException("Bad format for Timestamp '" + timestampValue + "' in column " + columnIndex + ".", "S1009");
                        case 4:
                           year = Integer.parseInt(timestampValue.substring(0, 2));
                           if (year <= 69) {
                              year += 100;
                           }

                           year += 1900;
                           month = Integer.parseInt(timestampValue.substring(2, 4));
                           day = 1;
                           break;
                        case 6:
                           year = Integer.parseInt(timestampValue.substring(0, 2));
                           if (year <= 69) {
                              year += 100;
                           }

                           year += 1900;
                           month = Integer.parseInt(timestampValue.substring(2, 4));
                           day = Integer.parseInt(timestampValue.substring(4, 6));
                           break;
                        case 8:
                           if (timestampValue.indexOf(":") != -1) {
                              hour = Integer.parseInt(timestampValue.substring(0, 2));
                              minutes = Integer.parseInt(timestampValue.substring(3, 5));
                              seconds = Integer.parseInt(timestampValue.substring(6, 8));
                              year = 1970;
                              month = 1;
                              day = 1;
                           } else {
                              year = Integer.parseInt(timestampValue.substring(0, 4));
                              month = Integer.parseInt(timestampValue.substring(4, 6));
                              day = Integer.parseInt(timestampValue.substring(6, 8));
                              year -= 1900;
                              --month;
                           }
                           break;
                        case 10:
                           if (this.fields[columnIndex - 1].getMysqlType() != 10 && timestampValue.indexOf("-") == -1) {
                              year = Integer.parseInt(timestampValue.substring(0, 2));
                              if (year <= 69) {
                                 year += 100;
                              }

                              month = Integer.parseInt(timestampValue.substring(2, 4));
                              day = Integer.parseInt(timestampValue.substring(4, 6));
                              hour = Integer.parseInt(timestampValue.substring(6, 8));
                              minutes = Integer.parseInt(timestampValue.substring(8, 10));
                              year += 1900;
                           } else {
                              year = Integer.parseInt(timestampValue.substring(0, 4));
                              month = Integer.parseInt(timestampValue.substring(5, 7));
                              day = Integer.parseInt(timestampValue.substring(8, 10));
                              hour = 0;
                              minutes = 0;
                           }
                           break;
                        case 12:
                           year = Integer.parseInt(timestampValue.substring(0, 2));
                           if (year <= 69) {
                              year += 100;
                           }

                           year += 1900;
                           month = Integer.parseInt(timestampValue.substring(2, 4));
                           day = Integer.parseInt(timestampValue.substring(4, 6));
                           hour = Integer.parseInt(timestampValue.substring(6, 8));
                           minutes = Integer.parseInt(timestampValue.substring(8, 10));
                           seconds = Integer.parseInt(timestampValue.substring(10, 12));
                           break;
                        case 14:
                           year = Integer.parseInt(timestampValue.substring(0, 4));
                           month = Integer.parseInt(timestampValue.substring(4, 6));
                           day = Integer.parseInt(timestampValue.substring(6, 8));
                           hour = Integer.parseInt(timestampValue.substring(8, 10));
                           minutes = Integer.parseInt(timestampValue.substring(10, 12));
                           seconds = Integer.parseInt(timestampValue.substring(12, 14));
                           break;
                        case 19:
                        case 20:
                        case 21:
                        case 22:
                        case 23:
                        case 24:
                        case 25:
                        case 26:
                           year = Integer.parseInt(timestampValue.substring(0, 4));
                           month = Integer.parseInt(timestampValue.substring(5, 7));
                           day = Integer.parseInt(timestampValue.substring(8, 10));
                           hour = Integer.parseInt(timestampValue.substring(11, 13));
                           minutes = Integer.parseInt(timestampValue.substring(14, 16));
                           seconds = Integer.parseInt(timestampValue.substring(17, 19));
                           nanos = 0;
                           if (length > 19) {
                              int decimalIndex = timestampValue.lastIndexOf(46);
                              if (decimalIndex != -1) {
                                 if (decimalIndex + 2 > timestampValue.length()) {
                                    throw new IllegalArgumentException();
                                 }

                                 nanos = Integer.parseInt(timestampValue.substring(decimalIndex + 1));
                              }
                           }
                     }

                     return !this.useLegacyDatetimeCode ? TimeUtil.fastTimestampCreate(tz, year, month, day, hour, minutes, seconds, nanos) : TimeUtil.changeTimezone(this.connection, sessionCalendar, targetCalendar, this.fastTimestampCreate(sessionCalendar, year, month, day, hour, minutes, seconds, nanos), this.connection.getServerTimezoneTZ(), tz, rollForward);
                  }
               } else if ("convertToNull".equals(this.connection.getZeroDateTimeBehavior())) {
                  this.wasNullFlag = true;
                  return null;
               } else if ("exception".equals(this.connection.getZeroDateTimeBehavior())) {
                  throw SQLError.createSQLException("Value '" + timestampValue + "' can not be represented as java.sql.Timestamp", "S1009");
               } else {
                  return this.fastTimestampCreate((Calendar)null, 1, 1, 1, 0, 0, 0, 0);
               }
            }
         }
      } catch (Exception e) {
         SQLException sqlEx = SQLError.createSQLException("Cannot convert value '" + timestampValue + "' from column " + columnIndex + " to TIMESTAMP.", "S1009");
         sqlEx.initCause(e);
         throw sqlEx;
      }
   }

   private Timestamp getTimestampFromBytes(int columnIndex, Calendar targetCalendar, byte[] timestampAsBytes, TimeZone tz, boolean rollForward) throws SQLException {
      this.checkColumnBounds(columnIndex);

      try {
         this.wasNullFlag = false;
         if (timestampAsBytes == null) {
            this.wasNullFlag = true;
            return null;
         } else {
            int length = timestampAsBytes.length;
            Calendar sessionCalendar = this.connection.getUseJDBCCompliantTimezoneShift() ? this.connection.getUtcCalendar() : this.getCalendarInstanceForSessionOrNew();
            synchronized(sessionCalendar) {
               boolean allZeroTimestamp = true;
               boolean onlyTimePresent = StringUtils.indexOf(timestampAsBytes, ':') != -1;

               for(int i = 0; i < length; ++i) {
                  byte b = timestampAsBytes[i];
                  if (b == 32 || b == 45 || b == 47) {
                     onlyTimePresent = false;
                  }

                  if (b != 48 && b != 32 && b != 58 && b != 45 && b != 47 && b != 46) {
                     allZeroTimestamp = false;
                     break;
                  }
               }

               if (!onlyTimePresent && allZeroTimestamp) {
                  if ("convertToNull".equals(this.connection.getZeroDateTimeBehavior())) {
                     this.wasNullFlag = true;
                     return null;
                  } else if ("exception".equals(this.connection.getZeroDateTimeBehavior())) {
                     throw SQLError.createSQLException("Value '" + timestampAsBytes + "' can not be represented as java.sql.Timestamp", "S1009");
                  } else {
                     return !this.useLegacyDatetimeCode ? TimeUtil.fastTimestampCreate(tz, 1, 1, 1, 0, 0, 0, 0) : this.fastTimestampCreate((Calendar)null, 1, 1, 1, 0, 0, 0, 0);
                  }
               } else if (this.fields[columnIndex - 1].getMysqlType() == 13) {
                  return !this.useLegacyDatetimeCode ? TimeUtil.fastTimestampCreate(tz, StringUtils.getInt(timestampAsBytes, 0, 4), 1, 1, 0, 0, 0, 0) : TimeUtil.changeTimezone(this.connection, sessionCalendar, targetCalendar, this.fastTimestampCreate(sessionCalendar, StringUtils.getInt(timestampAsBytes, 0, 4), 1, 1, 0, 0, 0, 0), this.connection.getServerTimezoneTZ(), tz, rollForward);
               } else {
                  if (timestampAsBytes[length - 1] == 46) {
                     --length;
                  }

                  int year = 0;
                  int month = 0;
                  int day = 0;
                  int hour = 0;
                  int minutes = 0;
                  int seconds = 0;
                  int nanos = 0;
                  switch (length) {
                     case 2:
                        year = StringUtils.getInt(timestampAsBytes, 0, 2);
                        if (year <= 69) {
                           year += 100;
                        }

                        year += 1900;
                        month = 1;
                        day = 1;
                        break;
                     case 3:
                     case 5:
                     case 7:
                     case 9:
                     case 11:
                     case 13:
                     case 15:
                     case 16:
                     case 17:
                     case 18:
                     default:
                        throw new SQLException("Bad format for Timestamp '" + new String(timestampAsBytes) + "' in column " + columnIndex + ".", "S1009");
                     case 4:
                        year = StringUtils.getInt(timestampAsBytes, 0, 2);
                        if (year <= 69) {
                           year += 100;
                        }

                        year += 1900;
                        month = StringUtils.getInt(timestampAsBytes, 2, 4);
                        day = 1;
                        break;
                     case 6:
                        year = StringUtils.getInt(timestampAsBytes, 0, 2);
                        if (year <= 69) {
                           year += 100;
                        }

                        year += 1900;
                        month = StringUtils.getInt(timestampAsBytes, 2, 4);
                        day = StringUtils.getInt(timestampAsBytes, 4, 6);
                        break;
                     case 8:
                        if (StringUtils.indexOf(timestampAsBytes, ':') != -1) {
                           hour = StringUtils.getInt(timestampAsBytes, 0, 2);
                           minutes = StringUtils.getInt(timestampAsBytes, 3, 5);
                           seconds = StringUtils.getInt(timestampAsBytes, 6, 8);
                           year = 1970;
                           month = 1;
                           day = 1;
                        } else {
                           year = StringUtils.getInt(timestampAsBytes, 0, 4);
                           month = StringUtils.getInt(timestampAsBytes, 4, 6);
                           day = StringUtils.getInt(timestampAsBytes, 6, 8);
                           year -= 1900;
                           --month;
                        }
                        break;
                     case 10:
                        if (this.fields[columnIndex - 1].getMysqlType() != 10 && StringUtils.indexOf(timestampAsBytes, '-') == -1) {
                           year = StringUtils.getInt(timestampAsBytes, 0, 2);
                           if (year <= 69) {
                              year += 100;
                           }

                           month = StringUtils.getInt(timestampAsBytes, 2, 4);
                           day = StringUtils.getInt(timestampAsBytes, 4, 6);
                           hour = StringUtils.getInt(timestampAsBytes, 6, 8);
                           minutes = StringUtils.getInt(timestampAsBytes, 8, 10);
                           year += 1900;
                        } else {
                           year = StringUtils.getInt(timestampAsBytes, 0, 4);
                           month = StringUtils.getInt(timestampAsBytes, 5, 7);
                           day = StringUtils.getInt(timestampAsBytes, 8, 10);
                           hour = 0;
                           minutes = 0;
                        }
                        break;
                     case 12:
                        year = StringUtils.getInt(timestampAsBytes, 0, 2);
                        if (year <= 69) {
                           year += 100;
                        }

                        year += 1900;
                        month = StringUtils.getInt(timestampAsBytes, 2, 4);
                        day = StringUtils.getInt(timestampAsBytes, 4, 6);
                        hour = StringUtils.getInt(timestampAsBytes, 6, 8);
                        minutes = StringUtils.getInt(timestampAsBytes, 8, 10);
                        seconds = StringUtils.getInt(timestampAsBytes, 10, 12);
                        break;
                     case 14:
                        year = StringUtils.getInt(timestampAsBytes, 0, 4);
                        month = StringUtils.getInt(timestampAsBytes, 4, 6);
                        day = StringUtils.getInt(timestampAsBytes, 6, 8);
                        hour = StringUtils.getInt(timestampAsBytes, 8, 10);
                        minutes = StringUtils.getInt(timestampAsBytes, 10, 12);
                        seconds = StringUtils.getInt(timestampAsBytes, 12, 14);
                        break;
                     case 19:
                     case 20:
                     case 21:
                     case 22:
                     case 23:
                     case 24:
                     case 25:
                     case 26:
                        year = StringUtils.getInt(timestampAsBytes, 0, 4);
                        month = StringUtils.getInt(timestampAsBytes, 5, 7);
                        day = StringUtils.getInt(timestampAsBytes, 8, 10);
                        hour = StringUtils.getInt(timestampAsBytes, 11, 13);
                        minutes = StringUtils.getInt(timestampAsBytes, 14, 16);
                        seconds = StringUtils.getInt(timestampAsBytes, 17, 19);
                        nanos = 0;
                        if (length > 19) {
                           int decimalIndex = StringUtils.lastIndexOf(timestampAsBytes, '.');
                           if (decimalIndex != -1) {
                              if (decimalIndex + 2 > length) {
                                 throw new IllegalArgumentException();
                              }

                              nanos = StringUtils.getInt(timestampAsBytes, decimalIndex + 1, length);
                           }
                        }
                  }

                  return !this.useLegacyDatetimeCode ? TimeUtil.fastTimestampCreate(tz, year, month, day, hour, minutes, seconds, nanos) : TimeUtil.changeTimezone(this.connection, sessionCalendar, targetCalendar, this.fastTimestampCreate(sessionCalendar, year, month, day, hour, minutes, seconds, nanos), this.connection.getServerTimezoneTZ(), tz, rollForward);
               }
            }
         }
      } catch (Exception e) {
         SQLException sqlEx = SQLError.createSQLException("Cannot convert value '" + new String(timestampAsBytes) + "' from column " + columnIndex + " to TIMESTAMP.", "S1009");
         sqlEx.initCause(e);
         throw sqlEx;
      }
   }

   private Timestamp getTimestampInternal(int columnIndex, Calendar targetCalendar, TimeZone tz, boolean rollForward) throws SQLException {
      if (this.isBinaryEncoded) {
         return this.getNativeTimestamp(columnIndex, targetCalendar, tz, rollForward);
      } else {
         Timestamp tsVal = null;
         if (!this.useFastDateParsing) {
            String timestampValue = this.getStringInternal(columnIndex, false);
            tsVal = this.getTimestampFromString(columnIndex, targetCalendar, timestampValue, tz, rollForward);
         } else {
            this.checkClosed();
            this.checkRowPos();
            this.checkColumnBounds(columnIndex);
            tsVal = this.thisRow.getTimestampFast(columnIndex - 1, targetCalendar, tz, rollForward, this.connection, this);
         }

         if (tsVal == null) {
            this.wasNullFlag = true;
         } else {
            this.wasNullFlag = false;
         }

         return tsVal;
      }
   }

   public int getType() throws SQLException {
      return this.resultSetType;
   }

   /** @deprecated */
   public InputStream getUnicodeStream(int columnIndex) throws SQLException {
      if (!this.isBinaryEncoded) {
         this.checkRowPos();
         return this.getBinaryStream(columnIndex);
      } else {
         return this.getNativeBinaryStream(columnIndex);
      }
   }

   /** @deprecated */
   public InputStream getUnicodeStream(String columnName) throws SQLException {
      return this.getUnicodeStream(this.findColumn(columnName));
   }

   public long getUpdateCount() {
      return this.updateCount;
   }

   public long getUpdateID() {
      return this.updateId;
   }

   public URL getURL(int colIndex) throws SQLException {
      String val = this.getString(colIndex);
      if (val == null) {
         return null;
      } else {
         try {
            return new URL(val);
         } catch (MalformedURLException var4) {
            throw SQLError.createSQLException(Messages.getString("ResultSet.Malformed_URL____104") + val + "'", "S1009");
         }
      }
   }

   public URL getURL(String colName) throws SQLException {
      String val = this.getString(colName);
      if (val == null) {
         return null;
      } else {
         try {
            return new URL(val);
         } catch (MalformedURLException var4) {
            throw SQLError.createSQLException(Messages.getString("ResultSet.Malformed_URL____107") + val + "'", "S1009");
         }
      }
   }

   public SQLWarning getWarnings() throws SQLException {
      return this.warningChain;
   }

   public void insertRow() throws SQLException {
      throw new NotUpdatable();
   }

   public boolean isAfterLast() throws SQLException {
      this.checkClosed();
      boolean b = this.rowData.isAfterLast();
      return b;
   }

   public boolean isBeforeFirst() throws SQLException {
      this.checkClosed();
      return this.rowData.isBeforeFirst();
   }

   public boolean isFirst() throws SQLException {
      this.checkClosed();
      return this.rowData.isFirst();
   }

   public boolean isLast() throws SQLException {
      this.checkClosed();
      return this.rowData.isLast();
   }

   private void issueConversionViaParsingWarning(String methodName, int columnIndex, Object value, Field fieldInfo, int[] typesWithNoParseConversion) throws SQLException {
      StringBuffer originalQueryBuf = new StringBuffer();
      if (this.owningStatement != null && this.owningStatement instanceof PreparedStatement) {
         originalQueryBuf.append(Messages.getString("ResultSet.CostlyConversionCreatedFromQuery"));
         originalQueryBuf.append(((PreparedStatement)this.owningStatement).originalSql);
         originalQueryBuf.append("\n\n");
      } else {
         originalQueryBuf.append(".");
      }

      StringBuffer convertibleTypesBuf = new StringBuffer();

      for(int i = 0; i < typesWithNoParseConversion.length; ++i) {
         convertibleTypesBuf.append(MysqlDefs.typeToName(typesWithNoParseConversion[i]));
         convertibleTypesBuf.append("\n");
      }

      String message = Messages.getString("ResultSet.CostlyConversion", new Object[]{methodName, new Integer(columnIndex + 1), fieldInfo.getOriginalName(), fieldInfo.getOriginalTableName(), originalQueryBuf.toString(), value != null ? value.getClass().getName() : ResultSetMetaData.getClassNameForJavaType(fieldInfo.getSQLType(), fieldInfo.isUnsigned(), fieldInfo.getMysqlType(), fieldInfo.isBinary() || fieldInfo.isBlob(), fieldInfo.isOpaqueBinary()), MysqlDefs.typeToName(fieldInfo.getMysqlType()), convertibleTypesBuf.toString()});
      this.eventSink.consumeEvent(new ProfilerEvent((byte)0, "", this.owningStatement == null ? "N/A" : this.owningStatement.currentCatalog, this.connectionId, this.owningStatement == null ? -1 : this.owningStatement.getId(), this.resultId, System.currentTimeMillis(), 0L, Constants.MILLIS_I18N, (String)null, this.pointOfOrigin, message));
   }

   public boolean last() throws SQLException {
      this.checkClosed();
      boolean b = true;
      if (this.rowData.size() == 0) {
         b = false;
      } else {
         if (this.onInsertRow) {
            this.onInsertRow = false;
         }

         if (this.doingUpdates) {
            this.doingUpdates = false;
         }

         if (this.thisRow != null) {
            this.thisRow.closeOpenStreams();
         }

         this.rowData.beforeLast();
         this.thisRow = this.rowData.next();
      }

      this.setRowPositionValidity();
      return b;
   }

   public void moveToCurrentRow() throws SQLException {
      throw new NotUpdatable();
   }

   public void moveToInsertRow() throws SQLException {
      throw new NotUpdatable();
   }

   public boolean next() throws SQLException {
      this.checkClosed();
      if (this.onInsertRow) {
         this.onInsertRow = false;
      }

      if (this.doingUpdates) {
         this.doingUpdates = false;
      }

      if (!this.reallyResult()) {
         throw SQLError.createSQLException(Messages.getString("ResultSet.ResultSet_is_from_UPDATE._No_Data_115"), "S1000");
      } else {
         if (this.thisRow != null) {
            this.thisRow.closeOpenStreams();
         }

         boolean b;
         if (this.rowData.size() == 0) {
            b = false;
         } else {
            this.thisRow = this.rowData.next();
            if (this.thisRow == null) {
               b = false;
            } else {
               this.clearWarnings();
               b = true;
            }
         }

         this.setRowPositionValidity();
         return b;
      }
   }

   private int parseIntAsDouble(int columnIndex, String val) throws NumberFormatException, SQLException {
      if (val == null) {
         return 0;
      } else {
         double valueAsDouble = Double.parseDouble(val);
         if (this.jdbcCompliantTruncationForReads && (valueAsDouble < (double)Integer.MIN_VALUE || valueAsDouble > (double)Integer.MAX_VALUE)) {
            this.throwRangeException(String.valueOf(valueAsDouble), columnIndex, 4);
         }

         return (int)valueAsDouble;
      }
   }

   private int getIntWithOverflowCheck(int columnIndex) throws SQLException {
      int intValue = this.thisRow.getInt(columnIndex);
      this.checkForIntegerTruncation(columnIndex + 1, (byte[])null, this.thisRow.getString(columnIndex, this.fields[columnIndex].getCharacterSet(), this.connection), intValue);
      return intValue;
   }

   private void checkForIntegerTruncation(int columnIndex, byte[] valueAsBytes, String valueAsString, int intValue) throws SQLException {
      if (this.jdbcCompliantTruncationForReads && (intValue == Integer.MIN_VALUE || intValue == Integer.MAX_VALUE)) {
         long valueAsLong = Long.parseLong(valueAsString == null ? new String(valueAsBytes) : valueAsString);
         if (valueAsLong < -2147483648L || valueAsLong > 2147483647L) {
            this.throwRangeException(valueAsString == null ? new String(valueAsBytes) : valueAsString, columnIndex, 4);
         }
      }

   }

   private long parseLongAsDouble(int columnIndex, String val) throws NumberFormatException, SQLException {
      if (val == null) {
         return 0L;
      } else {
         double valueAsDouble = Double.parseDouble(val);
         if (this.jdbcCompliantTruncationForReads && (valueAsDouble < (double)Long.MIN_VALUE || valueAsDouble > (double)Long.MAX_VALUE)) {
            this.throwRangeException(val, columnIndex, -5);
         }

         return (long)valueAsDouble;
      }
   }

   private long getLongWithOverflowCheck(int columnIndex, boolean doOverflowCheck) throws SQLException {
      long longValue = this.thisRow.getLong(columnIndex);
      if (doOverflowCheck) {
         this.checkForLongTruncation(columnIndex + 1, (byte[])null, this.thisRow.getString(columnIndex, this.fields[columnIndex].getCharacterSet(), this.connection), longValue);
      }

      return longValue;
   }

   private long parseLongWithOverflowCheck(int columnIndex, byte[] valueAsBytes, String valueAsString, boolean doCheck) throws NumberFormatException, SQLException {
      long longValue = 0L;
      if (valueAsBytes == null && valueAsString == null) {
         return 0L;
      } else {
         if (valueAsBytes != null) {
            longValue = StringUtils.getLong(valueAsBytes);
         } else {
            valueAsString = valueAsString.trim();
            longValue = Long.parseLong(valueAsString);
         }

         if (doCheck && this.jdbcCompliantTruncationForReads) {
            this.checkForLongTruncation(columnIndex, valueAsBytes, valueAsString, longValue);
         }

         return longValue;
      }
   }

   private void checkForLongTruncation(int columnIndex, byte[] valueAsBytes, String valueAsString, long longValue) throws SQLException {
      if (longValue == Long.MIN_VALUE || longValue == Long.MAX_VALUE) {
         double valueAsDouble = Double.parseDouble(valueAsString == null ? new String(valueAsBytes) : valueAsString);
         if (valueAsDouble < (double)Long.MIN_VALUE || valueAsDouble > (double)Long.MAX_VALUE) {
            this.throwRangeException(valueAsString == null ? new String(valueAsBytes) : valueAsString, columnIndex, -5);
         }
      }

   }

   private short parseShortAsDouble(int columnIndex, String val) throws NumberFormatException, SQLException {
      if (val == null) {
         return 0;
      } else {
         double valueAsDouble = Double.parseDouble(val);
         if (this.jdbcCompliantTruncationForReads && (valueAsDouble < (double)-32768.0F || valueAsDouble > (double)32767.0F)) {
            this.throwRangeException(String.valueOf(valueAsDouble), columnIndex, 5);
         }

         return (short)((int)valueAsDouble);
      }
   }

   private short parseShortWithOverflowCheck(int columnIndex, byte[] valueAsBytes, String valueAsString) throws NumberFormatException, SQLException {
      short shortValue = 0;
      if (valueAsBytes == null && valueAsString == null) {
         return 0;
      } else {
         if (valueAsBytes != null) {
            shortValue = StringUtils.getShort(valueAsBytes);
         } else {
            valueAsString = valueAsString.trim();
            shortValue = Short.parseShort(valueAsString);
         }

         if (this.jdbcCompliantTruncationForReads && (shortValue == Short.MIN_VALUE || shortValue == 32767)) {
            long valueAsLong = Long.parseLong(valueAsString == null ? new String(valueAsBytes) : valueAsString);
            if (valueAsLong < -32768L || valueAsLong > 32767L) {
               this.throwRangeException(valueAsString == null ? new String(valueAsBytes) : valueAsString, columnIndex, 5);
            }
         }

         return shortValue;
      }
   }

   public boolean prev() throws SQLException {
      this.checkClosed();
      int rowIndex = this.rowData.getCurrentRowNumber();
      if (this.thisRow != null) {
         this.thisRow.closeOpenStreams();
      }

      boolean b = true;
      if (rowIndex - 1 >= 0) {
         --rowIndex;
         this.rowData.setCurrentRow(rowIndex);
         this.thisRow = this.rowData.getAt(rowIndex);
         b = true;
      } else if (rowIndex - 1 == -1) {
         --rowIndex;
         this.rowData.setCurrentRow(rowIndex);
         this.thisRow = null;
         b = false;
      } else {
         b = false;
      }

      this.setRowPositionValidity();
      return b;
   }

   public boolean previous() throws SQLException {
      if (this.onInsertRow) {
         this.onInsertRow = false;
      }

      if (this.doingUpdates) {
         this.doingUpdates = false;
      }

      return this.prev();
   }

   public void realClose(boolean calledExplicitly) throws SQLException {
      if (!this.isClosed) {
         try {
            if (this.useUsageAdvisor) {
               if (!calledExplicitly) {
                  this.eventSink.consumeEvent(new ProfilerEvent((byte)0, "", this.owningStatement == null ? "N/A" : this.owningStatement.currentCatalog, this.connectionId, this.owningStatement == null ? -1 : this.owningStatement.getId(), this.resultId, System.currentTimeMillis(), 0L, Constants.MILLIS_I18N, (String)null, this.pointOfOrigin, Messages.getString("ResultSet.ResultSet_implicitly_closed_by_driver")));
               }

               if (this.rowData instanceof RowDataStatic) {
                  if (this.rowData.size() > this.connection.getResultSetSizeThreshold()) {
                     this.eventSink.consumeEvent(new ProfilerEvent((byte)0, "", this.owningStatement == null ? Messages.getString("ResultSet.N/A_159") : this.owningStatement.currentCatalog, this.connectionId, this.owningStatement == null ? -1 : this.owningStatement.getId(), this.resultId, System.currentTimeMillis(), 0L, Constants.MILLIS_I18N, (String)null, this.pointOfOrigin, Messages.getString("ResultSet.Too_Large_Result_Set", new Object[]{new Integer(this.rowData.size()), new Integer(this.connection.getResultSetSizeThreshold())})));
                  }

                  if (!this.isLast() && !this.isAfterLast() && this.rowData.size() != 0) {
                     this.eventSink.consumeEvent(new ProfilerEvent((byte)0, "", this.owningStatement == null ? Messages.getString("ResultSet.N/A_159") : this.owningStatement.currentCatalog, this.connectionId, this.owningStatement == null ? -1 : this.owningStatement.getId(), this.resultId, System.currentTimeMillis(), 0L, Constants.MILLIS_I18N, (String)null, this.pointOfOrigin, Messages.getString("ResultSet.Possible_incomplete_traversal_of_result_set", new Object[]{new Integer(this.getRow()), new Integer(this.rowData.size())})));
                  }
               }

               if (this.columnUsed.length > 0 && !this.rowData.wasEmpty()) {
                  StringBuffer buf = new StringBuffer(Messages.getString("ResultSet.The_following_columns_were_never_referenced"));
                  boolean issueWarn = false;

                  for(int i = 0; i < this.columnUsed.length; ++i) {
                     if (!this.columnUsed[i]) {
                        if (!issueWarn) {
                           issueWarn = true;
                        } else {
                           buf.append(", ");
                        }

                        buf.append(this.fields[i].getFullName());
                     }
                  }

                  if (issueWarn) {
                     this.eventSink.consumeEvent(new ProfilerEvent((byte)0, "", this.owningStatement == null ? "N/A" : this.owningStatement.currentCatalog, this.connectionId, this.owningStatement == null ? -1 : this.owningStatement.getId(), 0, System.currentTimeMillis(), 0L, Constants.MILLIS_I18N, (String)null, this.pointOfOrigin, buf.toString()));
                  }
               }
            }
         } finally {
            SQLException exceptionDuringClose = null;
            if (this.rowData != null) {
               try {
                  this.rowData.close();
               } catch (SQLException sqlEx) {
                  exceptionDuringClose = sqlEx;
               }
            }

            if (this.statementUsedForFetchingRows != null) {
               try {
                  this.statementUsedForFetchingRows.realClose(true, false);
               } catch (SQLException sqlEx) {
                  if (exceptionDuringClose != null) {
                     exceptionDuringClose.setNextException(sqlEx);
                  } else {
                     exceptionDuringClose = sqlEx;
                  }
               }
            }

            this.rowData = null;
            this.defaultTimeZone = null;
            this.fields = null;
            this.columnNameToIndex = null;
            this.fullColumnNameToIndex = null;
            this.eventSink = null;
            this.warningChain = null;
            if (!this.retainOwningStatement) {
               this.owningStatement = null;
            }

            this.catalog = null;
            this.serverInfo = null;
            this.thisRow = null;
            this.fastDateCal = null;
            this.connection = null;
            this.isClosed = true;
            if (exceptionDuringClose != null) {
               throw exceptionDuringClose;
            }

         }

      }
   }

   public boolean reallyResult() {
      return this.rowData != null ? true : this.reallyResult;
   }

   public void refreshRow() throws SQLException {
      throw new NotUpdatable();
   }

   public boolean relative(int rows) throws SQLException {
      this.checkClosed();
      if (this.rowData.size() == 0) {
         this.setRowPositionValidity();
         return false;
      } else {
         if (this.thisRow != null) {
            this.thisRow.closeOpenStreams();
         }

         this.rowData.moveRowRelative(rows);
         this.thisRow = this.rowData.getAt(this.rowData.getCurrentRowNumber());
         this.setRowPositionValidity();
         return !this.rowData.isAfterLast() && !this.rowData.isBeforeFirst();
      }
   }

   public boolean rowDeleted() throws SQLException {
      throw SQLError.notImplemented();
   }

   public boolean rowInserted() throws SQLException {
      throw SQLError.notImplemented();
   }

   public boolean rowUpdated() throws SQLException {
      throw SQLError.notImplemented();
   }

   protected void setBinaryEncoded() {
      this.isBinaryEncoded = true;
   }

   private void setDefaultTimeZone(TimeZone defaultTimeZone) {
      this.defaultTimeZone = defaultTimeZone;
   }

   public void setFetchDirection(int direction) throws SQLException {
      if (direction != 1000 && direction != 1001 && direction != 1002) {
         throw SQLError.createSQLException(Messages.getString("ResultSet.Illegal_value_for_fetch_direction_64"), "S1009");
      } else {
         this.fetchDirection = direction;
      }
   }

   public void setFetchSize(int rows) throws SQLException {
      if (rows < 0) {
         throw SQLError.createSQLException(Messages.getString("ResultSet.Value_must_be_between_0_and_getMaxRows()_66"), "S1009");
      } else {
         this.fetchSize = rows;
      }
   }

   public void setFirstCharOfQuery(char c) {
      this.firstCharOfQuery = c;
   }

   protected void setNextResultSet(ResultSetInternalMethods nextResultSet) {
      this.nextResultSet = nextResultSet;
   }

   public void setOwningStatement(StatementImpl owningStatement) {
      this.owningStatement = owningStatement;
   }

   protected void setResultSetConcurrency(int concurrencyFlag) {
      this.resultSetConcurrency = concurrencyFlag;
   }

   protected void setResultSetType(int typeFlag) {
      this.resultSetType = typeFlag;
   }

   protected void setServerInfo(String info) {
      this.serverInfo = info;
   }

   public void setStatementUsedForFetchingRows(PreparedStatement stmt) {
      this.statementUsedForFetchingRows = stmt;
   }

   public void setWrapperStatement(java.sql.Statement wrapperStatement) {
      this.wrapperStatement = wrapperStatement;
   }

   private void throwRangeException(String valueAsString, int columnIndex, int jdbcType) throws SQLException {
      String datatype = null;
      switch (jdbcType) {
         case -6:
            datatype = "TINYINT";
            break;
         case -5:
            datatype = "BIGINT";
            break;
         case -4:
         case -3:
         case -2:
         case -1:
         case 0:
         case 1:
         case 2:
         default:
            datatype = " (JDBC type '" + jdbcType + "')";
            break;
         case 3:
            datatype = "DECIMAL";
            break;
         case 4:
            datatype = "INTEGER";
            break;
         case 5:
            datatype = "SMALLINT";
            break;
         case 6:
            datatype = "FLOAT";
            break;
         case 7:
            datatype = "REAL";
            break;
         case 8:
            datatype = "DOUBLE";
      }

      throw SQLError.createSQLException("'" + valueAsString + "' in column '" + columnIndex + "' is outside valid range for the datatype " + datatype + ".", "22003");
   }

   public String toString() {
      return this.reallyResult ? super.toString() : "Result set representing update count of " + this.updateCount;
   }

   public void updateArray(int arg0, Array arg1) throws SQLException {
      throw SQLError.notImplemented();
   }

   public void updateArray(String arg0, Array arg1) throws SQLException {
      throw SQLError.notImplemented();
   }

   public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
      throw new NotUpdatable();
   }

   public void updateAsciiStream(String columnName, InputStream x, int length) throws SQLException {
      this.updateAsciiStream(this.findColumn(columnName), x, length);
   }

   public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
      throw new NotUpdatable();
   }

   public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException {
      this.updateBigDecimal(this.findColumn(columnName), x);
   }

   public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
      throw new NotUpdatable();
   }

   public void updateBinaryStream(String columnName, InputStream x, int length) throws SQLException {
      this.updateBinaryStream(this.findColumn(columnName), x, length);
   }

   public void updateBlob(int arg0, java.sql.Blob arg1) throws SQLException {
      throw new NotUpdatable();
   }

   public void updateBlob(String arg0, java.sql.Blob arg1) throws SQLException {
      throw new NotUpdatable();
   }

   public void updateBoolean(int columnIndex, boolean x) throws SQLException {
      throw new NotUpdatable();
   }

   public void updateBoolean(String columnName, boolean x) throws SQLException {
      this.updateBoolean(this.findColumn(columnName), x);
   }

   public void updateByte(int columnIndex, byte x) throws SQLException {
      throw new NotUpdatable();
   }

   public void updateByte(String columnName, byte x) throws SQLException {
      this.updateByte(this.findColumn(columnName), x);
   }

   public void updateBytes(int columnIndex, byte[] x) throws SQLException {
      throw new NotUpdatable();
   }

   public void updateBytes(String columnName, byte[] x) throws SQLException {
      this.updateBytes(this.findColumn(columnName), x);
   }

   public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
      throw new NotUpdatable();
   }

   public void updateCharacterStream(String columnName, Reader reader, int length) throws SQLException {
      this.updateCharacterStream(this.findColumn(columnName), reader, length);
   }

   public void updateClob(int arg0, java.sql.Clob arg1) throws SQLException {
      throw SQLError.notImplemented();
   }

   public void updateClob(String columnName, java.sql.Clob clob) throws SQLException {
      this.updateClob(this.findColumn(columnName), clob);
   }

   public void updateDate(int columnIndex, Date x) throws SQLException {
      throw new NotUpdatable();
   }

   public void updateDate(String columnName, Date x) throws SQLException {
      this.updateDate(this.findColumn(columnName), x);
   }

   public void updateDouble(int columnIndex, double x) throws SQLException {
      throw new NotUpdatable();
   }

   public void updateDouble(String columnName, double x) throws SQLException {
      this.updateDouble(this.findColumn(columnName), x);
   }

   public void updateFloat(int columnIndex, float x) throws SQLException {
      throw new NotUpdatable();
   }

   public void updateFloat(String columnName, float x) throws SQLException {
      this.updateFloat(this.findColumn(columnName), x);
   }

   public void updateInt(int columnIndex, int x) throws SQLException {
      throw new NotUpdatable();
   }

   public void updateInt(String columnName, int x) throws SQLException {
      this.updateInt(this.findColumn(columnName), x);
   }

   public void updateLong(int columnIndex, long x) throws SQLException {
      throw new NotUpdatable();
   }

   public void updateLong(String columnName, long x) throws SQLException {
      this.updateLong(this.findColumn(columnName), x);
   }

   public void updateNull(int columnIndex) throws SQLException {
      throw new NotUpdatable();
   }

   public void updateNull(String columnName) throws SQLException {
      this.updateNull(this.findColumn(columnName));
   }

   public void updateObject(int columnIndex, Object x) throws SQLException {
      throw new NotUpdatable();
   }

   public void updateObject(int columnIndex, Object x, int scale) throws SQLException {
      throw new NotUpdatable();
   }

   public void updateObject(String columnName, Object x) throws SQLException {
      this.updateObject(this.findColumn(columnName), x);
   }

   public void updateObject(String columnName, Object x, int scale) throws SQLException {
      this.updateObject(this.findColumn(columnName), x);
   }

   public void updateRef(int arg0, Ref arg1) throws SQLException {
      throw SQLError.notImplemented();
   }

   public void updateRef(String arg0, Ref arg1) throws SQLException {
      throw SQLError.notImplemented();
   }

   public void updateRow() throws SQLException {
      throw new NotUpdatable();
   }

   public void updateShort(int columnIndex, short x) throws SQLException {
      throw new NotUpdatable();
   }

   public void updateShort(String columnName, short x) throws SQLException {
      this.updateShort(this.findColumn(columnName), x);
   }

   public void updateString(int columnIndex, String x) throws SQLException {
      throw new NotUpdatable();
   }

   public void updateString(String columnName, String x) throws SQLException {
      this.updateString(this.findColumn(columnName), x);
   }

   public void updateTime(int columnIndex, Time x) throws SQLException {
      throw new NotUpdatable();
   }

   public void updateTime(String columnName, Time x) throws SQLException {
      this.updateTime(this.findColumn(columnName), x);
   }

   public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
      throw new NotUpdatable();
   }

   public void updateTimestamp(String columnName, Timestamp x) throws SQLException {
      this.updateTimestamp(this.findColumn(columnName), x);
   }

   public boolean wasNull() throws SQLException {
      return this.wasNullFlag;
   }

   protected Calendar getGmtCalendar() {
      if (this.gmtCalendar == null) {
         this.gmtCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
      }

      return this.gmtCalendar;
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
            JDBC_4_RS_4_ARG_CTOR = Class.forName("com.mysql.jdbc.JDBC4ResultSet").getConstructor(Long.TYPE, Long.TYPE, class$com$mysql$jdbc$ConnectionImpl == null ? (class$com$mysql$jdbc$ConnectionImpl = class$("com.mysql.jdbc.ConnectionImpl")) : class$com$mysql$jdbc$ConnectionImpl, class$com$mysql$jdbc$StatementImpl == null ? (class$com$mysql$jdbc$StatementImpl = class$("com.mysql.jdbc.StatementImpl")) : class$com$mysql$jdbc$StatementImpl);
            JDBC_4_RS_6_ARG_CTOR = Class.forName("com.mysql.jdbc.JDBC4ResultSet").getConstructor(class$java$lang$String == null ? (class$java$lang$String = class$("java.lang.String")) : class$java$lang$String, array$Lcom$mysql$jdbc$Field == null ? (array$Lcom$mysql$jdbc$Field = class$("[Lcom.mysql.jdbc.Field;")) : array$Lcom$mysql$jdbc$Field, class$com$mysql$jdbc$RowData == null ? (class$com$mysql$jdbc$RowData = class$("com.mysql.jdbc.RowData")) : class$com$mysql$jdbc$RowData, class$com$mysql$jdbc$ConnectionImpl == null ? (class$com$mysql$jdbc$ConnectionImpl = class$("com.mysql.jdbc.ConnectionImpl")) : class$com$mysql$jdbc$ConnectionImpl, class$com$mysql$jdbc$StatementImpl == null ? (class$com$mysql$jdbc$StatementImpl = class$("com.mysql.jdbc.StatementImpl")) : class$com$mysql$jdbc$StatementImpl);
            JDBC_4_UPD_RS_6_ARG_CTOR = Class.forName("com.mysql.jdbc.JDBC4UpdatableResultSet").getConstructor(class$java$lang$String == null ? (class$java$lang$String = class$("java.lang.String")) : class$java$lang$String, array$Lcom$mysql$jdbc$Field == null ? (array$Lcom$mysql$jdbc$Field = class$("[Lcom.mysql.jdbc.Field;")) : array$Lcom$mysql$jdbc$Field, class$com$mysql$jdbc$RowData == null ? (class$com$mysql$jdbc$RowData = class$("com.mysql.jdbc.RowData")) : class$com$mysql$jdbc$RowData, class$com$mysql$jdbc$ConnectionImpl == null ? (class$com$mysql$jdbc$ConnectionImpl = class$("com.mysql.jdbc.ConnectionImpl")) : class$com$mysql$jdbc$ConnectionImpl, class$com$mysql$jdbc$StatementImpl == null ? (class$com$mysql$jdbc$StatementImpl = class$("com.mysql.jdbc.StatementImpl")) : class$com$mysql$jdbc$StatementImpl);
         } catch (SecurityException e) {
            throw new RuntimeException(e);
         } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
         } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
         }
      } else {
         JDBC_4_RS_4_ARG_CTOR = null;
         JDBC_4_RS_6_ARG_CTOR = null;
         JDBC_4_UPD_RS_6_ARG_CTOR = null;
      }

      MIN_DIFF_PREC = (double)Float.parseFloat(Float.toString(Float.MIN_VALUE)) - Double.parseDouble(Float.toString(Float.MIN_VALUE));
      MAX_DIFF_PREC = (double)Float.parseFloat(Float.toString(Float.MAX_VALUE)) - Double.parseDouble(Float.toString(Float.MAX_VALUE));
      resultCounter = 1;
      EMPTY_SPACE = new char[255];

      for(int i = 0; i < EMPTY_SPACE.length; ++i) {
         EMPTY_SPACE[i] = ' ';
      }

   }
}
