package com.mysql.jdbc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

public class BufferRow extends ResultSetRow {
   private Buffer rowFromServer;
   private int homePosition = 0;
   private int preNullBitmaskHomePosition = 0;
   private int lastRequestedIndex = -1;
   private int lastRequestedPos;
   private Field[] metadata;
   private boolean isBinaryEncoded;
   private boolean[] isNull;
   private List openStreams;

   public BufferRow(Buffer buf, Field[] fields, boolean isBinaryEncoded) throws SQLException {
      this.rowFromServer = buf;
      this.metadata = fields;
      this.isBinaryEncoded = isBinaryEncoded;
      this.homePosition = this.rowFromServer.getPosition();
      this.preNullBitmaskHomePosition = this.homePosition;
      if (fields != null) {
         this.setMetadata(fields);
      }

   }

   public synchronized void closeOpenStreams() {
      if (this.openStreams != null) {
         Iterator iter = this.openStreams.iterator();

         while(iter.hasNext()) {
            try {
               ((InputStream)iter.next()).close();
            } catch (IOException var3) {
            }
         }

         this.openStreams.clear();
      }

   }

   private int findAndSeekToOffset(int index) throws SQLException {
      if (this.isBinaryEncoded) {
         return this.findAndSeekToOffsetForBinaryEncoding(index);
      } else if (index == 0) {
         this.lastRequestedIndex = 0;
         this.lastRequestedPos = this.homePosition;
         this.rowFromServer.setPosition(this.homePosition);
         return 0;
      } else if (index == this.lastRequestedIndex) {
         this.rowFromServer.setPosition(this.lastRequestedPos);
         return this.lastRequestedPos;
      } else {
         int startingIndex = 0;
         if (index > this.lastRequestedIndex) {
            if (this.lastRequestedIndex >= 0) {
               startingIndex = this.lastRequestedIndex;
            } else {
               startingIndex = 0;
            }

            this.rowFromServer.setPosition(this.lastRequestedPos);
         } else {
            this.rowFromServer.setPosition(this.homePosition);
         }

         for(int i = startingIndex; i < index; ++i) {
            this.rowFromServer.fastSkipLenByteArray();
         }

         this.lastRequestedIndex = index;
         this.lastRequestedPos = this.rowFromServer.getPosition();
         return this.lastRequestedPos;
      }
   }

   private int findAndSeekToOffsetForBinaryEncoding(int index) throws SQLException {
      if (index == 0) {
         this.lastRequestedIndex = 0;
         this.lastRequestedPos = this.homePosition;
         this.rowFromServer.setPosition(this.homePosition);
         return 0;
      } else if (index == this.lastRequestedIndex) {
         this.rowFromServer.setPosition(this.lastRequestedPos);
         return this.lastRequestedPos;
      } else {
         int startingIndex = 0;
         if (index > this.lastRequestedIndex) {
            if (this.lastRequestedIndex >= 0) {
               startingIndex = this.lastRequestedIndex;
            } else {
               startingIndex = 0;
               this.lastRequestedPos = this.homePosition;
            }

            this.rowFromServer.setPosition(this.lastRequestedPos);
         } else {
            this.rowFromServer.setPosition(this.homePosition);
         }

         for(int i = startingIndex; i < index; ++i) {
            if (!this.isNull[i]) {
               int curPosition = this.rowFromServer.getPosition();
               switch (this.metadata[i].getMysqlType()) {
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
                  case 255:
                     this.rowFromServer.fastSkipLenByteArray();
                     break;
                  case 1:
                     this.rowFromServer.setPosition(curPosition + 1);
                     break;
                  case 2:
                  case 13:
                     this.rowFromServer.setPosition(curPosition + 2);
                     break;
                  case 3:
                  case 9:
                     this.rowFromServer.setPosition(curPosition + 4);
                     break;
                  case 4:
                     this.rowFromServer.setPosition(curPosition + 4);
                     break;
                  case 5:
                     this.rowFromServer.setPosition(curPosition + 8);
                  case 6:
                     break;
                  case 7:
                  case 12:
                     this.rowFromServer.fastSkipLenByteArray();
                     break;
                  case 8:
                     this.rowFromServer.setPosition(curPosition + 8);
                     break;
                  case 10:
                     this.rowFromServer.fastSkipLenByteArray();
                     break;
                  case 11:
                     this.rowFromServer.fastSkipLenByteArray();
                     break;
                  default:
                     throw SQLError.createSQLException(Messages.getString("MysqlIO.97") + this.metadata[i].getMysqlType() + Messages.getString("MysqlIO.98") + (i + 1) + Messages.getString("MysqlIO.99") + this.metadata.length + Messages.getString("MysqlIO.100"), "S1000");
               }
            }
         }

         this.lastRequestedIndex = index;
         this.lastRequestedPos = this.rowFromServer.getPosition();
         return this.lastRequestedPos;
      }
   }

   public synchronized InputStream getBinaryInputStream(int columnIndex) throws SQLException {
      if (this.isBinaryEncoded && this.isNull(columnIndex)) {
         return null;
      } else {
         this.findAndSeekToOffset(columnIndex);
         long length = this.rowFromServer.readFieldLength();
         int offset = this.rowFromServer.getPosition();
         if (length == -1L) {
            return null;
         } else {
            InputStream stream = new ByteArrayInputStream(this.rowFromServer.getByteBuffer(), offset, (int)length);
            if (this.openStreams == null) {
               this.openStreams = new LinkedList();
            }

            return stream;
         }
      }
   }

   public byte[] getColumnValue(int index) throws SQLException {
      this.findAndSeekToOffset(index);
      if (!this.isBinaryEncoded) {
         return this.rowFromServer.readLenByteArray(0);
      } else if (this.isNull[index]) {
         return null;
      } else {
         switch (this.metadata[index].getMysqlType()) {
            case 0:
            case 7:
            case 10:
            case 11:
            case 12:
            case 15:
            case 16:
            case 246:
            case 249:
            case 250:
            case 251:
            case 252:
            case 253:
            case 254:
            case 255:
               return this.rowFromServer.readLenByteArray(0);
            case 1:
               return new byte[]{this.rowFromServer.readByte()};
            case 2:
            case 13:
               return this.rowFromServer.getBytes(2);
            case 3:
            case 9:
               return this.rowFromServer.getBytes(4);
            case 4:
               return this.rowFromServer.getBytes(4);
            case 5:
               return this.rowFromServer.getBytes(8);
            case 6:
               return null;
            case 8:
               return this.rowFromServer.getBytes(8);
            default:
               throw SQLError.createSQLException(Messages.getString("MysqlIO.97") + this.metadata[index].getMysqlType() + Messages.getString("MysqlIO.98") + (index + 1) + Messages.getString("MysqlIO.99") + this.metadata.length + Messages.getString("MysqlIO.100"), "S1000");
         }
      }
   }

   public int getInt(int columnIndex) throws SQLException {
      this.findAndSeekToOffset(columnIndex);
      long length = this.rowFromServer.readFieldLength();
      int offset = this.rowFromServer.getPosition();
      return length == -1L ? 0 : StringUtils.getInt(this.rowFromServer.getByteBuffer(), offset, offset + (int)length);
   }

   public long getLong(int columnIndex) throws SQLException {
      this.findAndSeekToOffset(columnIndex);
      long length = this.rowFromServer.readFieldLength();
      int offset = this.rowFromServer.getPosition();
      return length == -1L ? 0L : StringUtils.getLong(this.rowFromServer.getByteBuffer(), offset, offset + (int)length);
   }

   public double getNativeDouble(int columnIndex) throws SQLException {
      if (this.isNull(columnIndex)) {
         return (double)0.0F;
      } else {
         this.findAndSeekToOffset(columnIndex);
         int offset = this.rowFromServer.getPosition();
         return this.getNativeDouble(this.rowFromServer.getByteBuffer(), offset);
      }
   }

   public float getNativeFloat(int columnIndex) throws SQLException {
      if (this.isNull(columnIndex)) {
         return 0.0F;
      } else {
         this.findAndSeekToOffset(columnIndex);
         int offset = this.rowFromServer.getPosition();
         return this.getNativeFloat(this.rowFromServer.getByteBuffer(), offset);
      }
   }

   public int getNativeInt(int columnIndex) throws SQLException {
      if (this.isNull(columnIndex)) {
         return 0;
      } else {
         this.findAndSeekToOffset(columnIndex);
         int offset = this.rowFromServer.getPosition();
         return this.getNativeInt(this.rowFromServer.getByteBuffer(), offset);
      }
   }

   public long getNativeLong(int columnIndex) throws SQLException {
      if (this.isNull(columnIndex)) {
         return 0L;
      } else {
         this.findAndSeekToOffset(columnIndex);
         int offset = this.rowFromServer.getPosition();
         return this.getNativeLong(this.rowFromServer.getByteBuffer(), offset);
      }
   }

   public short getNativeShort(int columnIndex) throws SQLException {
      if (this.isNull(columnIndex)) {
         return 0;
      } else {
         this.findAndSeekToOffset(columnIndex);
         int offset = this.rowFromServer.getPosition();
         return this.getNativeShort(this.rowFromServer.getByteBuffer(), offset);
      }
   }

   public Timestamp getNativeTimestamp(int columnIndex, Calendar targetCalendar, TimeZone tz, boolean rollForward, ConnectionImpl conn, ResultSetImpl rs) throws SQLException {
      if (this.isNull(columnIndex)) {
         return null;
      } else {
         this.findAndSeekToOffset(columnIndex);
         long length = this.rowFromServer.readFieldLength();
         int offset = this.rowFromServer.getPosition();
         return this.getNativeTimestamp(this.rowFromServer.getByteBuffer(), offset, (int)length, targetCalendar, tz, rollForward, conn, rs);
      }
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

   public String getString(int columnIndex, String encoding, ConnectionImpl conn) throws SQLException {
      if (this.isBinaryEncoded && this.isNull(columnIndex)) {
         return null;
      } else {
         this.findAndSeekToOffset(columnIndex);
         long length = this.rowFromServer.readFieldLength();
         if (length == -1L) {
            return null;
         } else if (length == 0L) {
            return "";
         } else {
            int offset = this.rowFromServer.getPosition();
            return this.getString(encoding, conn, this.rowFromServer.getByteBuffer(), offset, (int)length);
         }
      }
   }

   public Time getTimeFast(int columnIndex, Calendar targetCalendar, TimeZone tz, boolean rollForward, ConnectionImpl conn, ResultSetImpl rs) throws SQLException {
      if (this.isNull(columnIndex)) {
         return null;
      } else {
         this.findAndSeekToOffset(columnIndex);
         long length = this.rowFromServer.readFieldLength();
         int offset = this.rowFromServer.getPosition();
         return this.getTimeFast(columnIndex, this.rowFromServer.getByteBuffer(), offset, (int)length, targetCalendar, tz, rollForward, conn, rs);
      }
   }

   public Timestamp getTimestampFast(int columnIndex, Calendar targetCalendar, TimeZone tz, boolean rollForward, ConnectionImpl conn, ResultSetImpl rs) throws SQLException {
      if (this.isNull(columnIndex)) {
         return null;
      } else {
         this.findAndSeekToOffset(columnIndex);
         long length = this.rowFromServer.readFieldLength();
         int offset = this.rowFromServer.getPosition();
         return this.getTimestampFast(columnIndex, this.rowFromServer.getByteBuffer(), offset, (int)length, targetCalendar, tz, rollForward, conn, rs);
      }
   }

   public boolean isFloatingPointNumber(int index) throws SQLException {
      if (this.isBinaryEncoded) {
         switch (this.metadata[index].getSQLType()) {
            case 2:
            case 3:
            case 6:
            case 8:
               return true;
            case 4:
            case 5:
            case 7:
            default:
               return false;
         }
      } else {
         this.findAndSeekToOffset(index);
         long length = this.rowFromServer.readFieldLength();
         if (length == -1L) {
            return false;
         } else if (length == 0L) {
            return false;
         } else {
            for(int i = 0; i < (int)length; ++i) {
               char c = (char)this.rowFromServer.readByte();
               if (c == 'e' || c == 'E') {
                  return true;
               }
            }

            return false;
         }
      }
   }

   public boolean isNull(int index) throws SQLException {
      if (!this.isBinaryEncoded) {
         this.findAndSeekToOffset(index);
         return this.rowFromServer.readFieldLength() == -1L;
      } else {
         return this.isNull[index];
      }
   }

   public long length(int index) throws SQLException {
      this.findAndSeekToOffset(index);
      long length = this.rowFromServer.readFieldLength();
      return length == -1L ? 0L : length;
   }

   public void setColumnValue(int index, byte[] value) throws SQLException {
      throw new OperationNotSupportedException();
   }

   public ResultSetRow setMetadata(Field[] f) throws SQLException {
      super.setMetadata(f);
      if (this.isBinaryEncoded) {
         this.setupIsNullBitmask();
      }

      return this;
   }

   private void setupIsNullBitmask() throws SQLException {
      if (this.isNull == null) {
         this.rowFromServer.setPosition(this.preNullBitmaskHomePosition);
         int nullCount = (this.metadata.length + 9) / 8;
         byte[] nullBitMask = new byte[nullCount];

         for(int i = 0; i < nullCount; ++i) {
            nullBitMask[i] = this.rowFromServer.readByte();
         }

         this.homePosition = this.rowFromServer.getPosition();
         this.isNull = new boolean[this.metadata.length];
         int nullMaskPos = 0;
         int bit = 4;

         for(int i = 0; i < this.metadata.length; ++i) {
            this.isNull[i] = (nullBitMask[nullMaskPos] & bit) != 0;
            if (((bit <<= 1) & 255) == 0) {
               bit = 1;
               ++nullMaskPos;
            }
         }

      }
   }

   public Date getDateFast(int columnIndex, ConnectionImpl conn, ResultSetImpl rs, Calendar targetCalendar) throws SQLException {
      if (this.isNull(columnIndex)) {
         return null;
      } else {
         this.findAndSeekToOffset(columnIndex);
         long length = this.rowFromServer.readFieldLength();
         int offset = this.rowFromServer.getPosition();
         return this.getDateFast(columnIndex, this.rowFromServer.getByteBuffer(), offset, (int)length, conn, rs, targetCalendar);
      }
   }

   public Date getNativeDate(int columnIndex, ConnectionImpl conn, ResultSetImpl rs) throws SQLException {
      if (this.isNull(columnIndex)) {
         return null;
      } else {
         this.findAndSeekToOffset(columnIndex);
         long length = this.rowFromServer.readFieldLength();
         int offset = this.rowFromServer.getPosition();
         return this.getNativeDate(columnIndex, this.rowFromServer.getByteBuffer(), offset, (int)length, conn, rs);
      }
   }

   public Object getNativeDateTimeValue(int columnIndex, Calendar targetCalendar, int jdbcType, int mysqlType, TimeZone tz, boolean rollForward, ConnectionImpl conn, ResultSetImpl rs) throws SQLException {
      if (this.isNull(columnIndex)) {
         return null;
      } else {
         this.findAndSeekToOffset(columnIndex);
         long length = this.rowFromServer.readFieldLength();
         int offset = this.rowFromServer.getPosition();
         return this.getNativeDateTimeValue(columnIndex, this.rowFromServer.getByteBuffer(), offset, (int)length, targetCalendar, jdbcType, mysqlType, tz, rollForward, conn, rs);
      }
   }

   public Time getNativeTime(int columnIndex, Calendar targetCalendar, TimeZone tz, boolean rollForward, ConnectionImpl conn, ResultSetImpl rs) throws SQLException {
      if (this.isNull(columnIndex)) {
         return null;
      } else {
         this.findAndSeekToOffset(columnIndex);
         long length = this.rowFromServer.readFieldLength();
         int offset = this.rowFromServer.getPosition();
         return this.getNativeTime(columnIndex, this.rowFromServer.getByteBuffer(), offset, (int)length, targetCalendar, tz, rollForward, conn, rs);
      }
   }
}
