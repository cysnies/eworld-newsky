package com.mysql.jdbc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;

public class Blob implements java.sql.Blob, OutputStreamWatcher {
   private byte[] binaryData = null;
   private boolean isClosed = false;

   Blob() {
      this.setBinaryData(Constants.EMPTY_BYTE_ARRAY);
   }

   Blob(byte[] data) {
      this.setBinaryData(data);
   }

   Blob(byte[] data, ResultSetInternalMethods creatorResultSetToSet, int columnIndexToSet) {
      this.setBinaryData(data);
   }

   private synchronized byte[] getBinaryData() {
      return this.binaryData;
   }

   public synchronized InputStream getBinaryStream() throws SQLException {
      this.checkClosed();
      return new ByteArrayInputStream(this.getBinaryData());
   }

   public synchronized byte[] getBytes(long pos, int length) throws SQLException {
      this.checkClosed();
      if (pos < 1L) {
         throw SQLError.createSQLException(Messages.getString("Blob.2"), "S1009");
      } else {
         --pos;
         if (pos > (long)this.binaryData.length) {
            throw SQLError.createSQLException("\"pos\" argument can not be larger than the BLOB's length.", "S1009");
         } else if (pos + (long)length > (long)this.binaryData.length) {
            throw SQLError.createSQLException("\"pos\" + \"length\" arguments can not be larger than the BLOB's length.", "S1009");
         } else {
            byte[] newData = new byte[length];
            System.arraycopy(this.getBinaryData(), (int)pos, newData, 0, length);
            return newData;
         }
      }
   }

   public synchronized long length() throws SQLException {
      this.checkClosed();
      return (long)this.getBinaryData().length;
   }

   public synchronized long position(byte[] pattern, long start) throws SQLException {
      throw SQLError.createSQLException("Not implemented");
   }

   public synchronized long position(java.sql.Blob pattern, long start) throws SQLException {
      this.checkClosed();
      return this.position(pattern.getBytes(0L, (int)pattern.length()), start);
   }

   private synchronized void setBinaryData(byte[] newBinaryData) {
      this.binaryData = newBinaryData;
   }

   public synchronized OutputStream setBinaryStream(long indexToWriteAt) throws SQLException {
      this.checkClosed();
      if (indexToWriteAt < 1L) {
         throw SQLError.createSQLException(Messages.getString("Blob.0"), "S1009");
      } else {
         WatchableOutputStream bytesOut = new WatchableOutputStream();
         bytesOut.setWatcher(this);
         if (indexToWriteAt > 0L) {
            bytesOut.write(this.binaryData, 0, (int)(indexToWriteAt - 1L));
         }

         return bytesOut;
      }
   }

   public synchronized int setBytes(long writeAt, byte[] bytes) throws SQLException {
      this.checkClosed();
      return this.setBytes(writeAt, bytes, 0, bytes.length);
   }

   public synchronized int setBytes(long writeAt, byte[] bytes, int offset, int length) throws SQLException {
      this.checkClosed();
      OutputStream bytesOut = this.setBinaryStream(writeAt);

      try {
         bytesOut.write(bytes, offset, length);
      } catch (IOException ioEx) {
         SQLException sqlEx = SQLError.createSQLException(Messages.getString("Blob.1"), "S1000");
         sqlEx.initCause(ioEx);
         throw sqlEx;
      } finally {
         try {
            bytesOut.close();
         } catch (IOException var15) {
         }

      }

      return length;
   }

   public synchronized void streamClosed(byte[] byteData) {
      this.binaryData = byteData;
   }

   public synchronized void streamClosed(WatchableOutputStream out) {
      int streamSize = out.size();
      if (streamSize < this.binaryData.length) {
         out.write(this.binaryData, streamSize, this.binaryData.length - streamSize);
      }

      this.binaryData = out.toByteArray();
   }

   public synchronized void truncate(long len) throws SQLException {
      this.checkClosed();
      if (len < 0L) {
         throw SQLError.createSQLException("\"len\" argument can not be < 1.", "S1009");
      } else if (len > (long)this.binaryData.length) {
         throw SQLError.createSQLException("\"len\" argument can not be larger than the BLOB's length.", "S1009");
      } else {
         byte[] newData = new byte[(int)len];
         System.arraycopy(this.getBinaryData(), 0, newData, 0, (int)len);
         this.binaryData = newData;
      }
   }

   public synchronized void free() throws SQLException {
      this.binaryData = null;
      this.isClosed = true;
   }

   public synchronized InputStream getBinaryStream(long pos, long length) throws SQLException {
      this.checkClosed();
      if (pos < 1L) {
         throw SQLError.createSQLException("\"pos\" argument can not be < 1.", "S1009");
      } else {
         --pos;
         if (pos > (long)this.binaryData.length) {
            throw SQLError.createSQLException("\"pos\" argument can not be larger than the BLOB's length.", "S1009");
         } else if (pos + length > (long)this.binaryData.length) {
            throw SQLError.createSQLException("\"pos\" + \"length\" arguments can not be larger than the BLOB's length.", "S1009");
         } else {
            return new ByteArrayInputStream(this.getBinaryData(), (int)pos, (int)length);
         }
      }
   }

   private synchronized void checkClosed() throws SQLException {
      if (this.isClosed) {
         throw SQLError.createSQLException("Invalid operation on closed BLOB", "S1009");
      }
   }
}
