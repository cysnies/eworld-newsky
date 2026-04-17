package com.mysql.jdbc;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;

public class ByteArrayRow extends ResultSetRow {
   byte[][] internalRowData;

   public ByteArrayRow(byte[][] internalRowData) {
      this.internalRowData = internalRowData;
   }

   public byte[] getColumnValue(int index) throws SQLException {
      return this.internalRowData[index];
   }

   public void setColumnValue(int index, byte[] value) throws SQLException {
      this.internalRowData[index] = value;
   }

   public String getString(int index, String encoding, ConnectionImpl conn) throws SQLException {
      byte[] columnData = this.internalRowData[index];
      return columnData == null ? null : this.getString(encoding, conn, columnData, 0, columnData.length);
   }

   public boolean isNull(int index) throws SQLException {
      return this.internalRowData[index] == null;
   }

   public boolean isFloatingPointNumber(int index) throws SQLException {
      byte[] numAsBytes = this.internalRowData[index];
      if (this.internalRowData[index] != null && this.internalRowData[index].length != 0) {
         for(int i = 0; i < numAsBytes.length; ++i) {
            if ((char)numAsBytes[i] == 'e' || (char)numAsBytes[i] == 'E') {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public long length(int index) throws SQLException {
      return this.internalRowData[index] == null ? 0L : (long)this.internalRowData[index].length;
   }

   public int getInt(int columnIndex) {
      return this.internalRowData[columnIndex] == null ? 0 : StringUtils.getInt(this.internalRowData[columnIndex]);
   }

   public long getLong(int columnIndex) {
      return this.internalRowData[columnIndex] == null ? 0L : StringUtils.getLong(this.internalRowData[columnIndex]);
   }

   public Timestamp getTimestampFast(int columnIndex, Calendar targetCalendar, TimeZone tz, boolean rollForward, ConnectionImpl conn, ResultSetImpl rs) throws SQLException {
      byte[] columnValue = this.internalRowData[columnIndex];
      return columnValue == null ? null : this.getTimestampFast(columnIndex, this.internalRowData[columnIndex], 0, columnValue.length, targetCalendar, tz, rollForward, conn, rs);
   }

   public double getNativeDouble(int columnIndex) throws SQLException {
      return this.internalRowData[columnIndex] == null ? (double)0.0F : this.getNativeDouble(this.internalRowData[columnIndex], 0);
   }

   public float getNativeFloat(int columnIndex) throws SQLException {
      return this.internalRowData[columnIndex] == null ? 0.0F : this.getNativeFloat(this.internalRowData[columnIndex], 0);
   }

   public int getNativeInt(int columnIndex) throws SQLException {
      return this.internalRowData[columnIndex] == null ? 0 : this.getNativeInt(this.internalRowData[columnIndex], 0);
   }

   public long getNativeLong(int columnIndex) throws SQLException {
      return this.internalRowData[columnIndex] == null ? 0L : this.getNativeLong(this.internalRowData[columnIndex], 0);
   }

   public short getNativeShort(int columnIndex) throws SQLException {
      return this.internalRowData[columnIndex] == null ? 0 : this.getNativeShort(this.internalRowData[columnIndex], 0);
   }

   public Timestamp getNativeTimestamp(int columnIndex, Calendar targetCalendar, TimeZone tz, boolean rollForward, ConnectionImpl conn, ResultSetImpl rs) throws SQLException {
      byte[] bits = this.internalRowData[columnIndex];
      return bits == null ? null : this.getNativeTimestamp(bits, 0, bits.length, targetCalendar, tz, rollForward, conn, rs);
   }

   public void closeOpenStreams() {
   }

   public InputStream getBinaryInputStream(int columnIndex) throws SQLException {
      return this.internalRowData[columnIndex] == null ? null : new ByteArrayInputStream(this.internalRowData[columnIndex]);
   }

   public Reader getReader(int columnIndex) throws SQLException {
      InputStream stream = this.getBinaryInputStream(columnIndex);
      if (stream == null) {
         return null;
      } else {
         try {
            return new InputStreamReader(stream, this.metadata[columnIndex].getCharacterSet());
         } catch (UnsupportedEncodingException e) {
            SQLException sqlEx = SQLError.createSQLException("");
            sqlEx.initCause(e);
            throw sqlEx;
         }
      }
   }

   public Time getTimeFast(int columnIndex, Calendar targetCalendar, TimeZone tz, boolean rollForward, ConnectionImpl conn, ResultSetImpl rs) throws SQLException {
      byte[] columnValue = this.internalRowData[columnIndex];
      return columnValue == null ? null : this.getTimeFast(columnIndex, this.internalRowData[columnIndex], 0, columnValue.length, targetCalendar, tz, rollForward, conn, rs);
   }

   public Date getDateFast(int columnIndex, ConnectionImpl conn, ResultSetImpl rs, Calendar targetCalendar) throws SQLException {
      byte[] columnValue = this.internalRowData[columnIndex];
      return columnValue == null ? null : this.getDateFast(columnIndex, this.internalRowData[columnIndex], 0, columnValue.length, conn, rs, targetCalendar);
   }

   public Object getNativeDateTimeValue(int columnIndex, Calendar targetCalendar, int jdbcType, int mysqlType, TimeZone tz, boolean rollForward, ConnectionImpl conn, ResultSetImpl rs) throws SQLException {
      byte[] columnValue = this.internalRowData[columnIndex];
      return columnValue == null ? null : this.getNativeDateTimeValue(columnIndex, columnValue, 0, columnValue.length, targetCalendar, jdbcType, mysqlType, tz, rollForward, conn, rs);
   }

   public Date getNativeDate(int columnIndex, ConnectionImpl conn, ResultSetImpl rs) throws SQLException {
      byte[] columnValue = this.internalRowData[columnIndex];
      return columnValue == null ? null : this.getNativeDate(columnIndex, columnValue, 0, columnValue.length, conn, rs);
   }

   public Time getNativeTime(int columnIndex, Calendar targetCalendar, TimeZone tz, boolean rollForward, ConnectionImpl conn, ResultSetImpl rs) throws SQLException {
      byte[] columnValue = this.internalRowData[columnIndex];
      return columnValue == null ? null : this.getNativeTime(columnIndex, columnValue, 0, columnValue.length, targetCalendar, tz, rollForward, conn, rs);
   }
}
