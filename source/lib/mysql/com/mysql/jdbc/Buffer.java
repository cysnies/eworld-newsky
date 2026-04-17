package com.mysql.jdbc;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.sql.SQLException;

class Buffer {
   static final int MAX_BYTES_TO_DUMP = 512;
   static final int NO_LENGTH_LIMIT = -1;
   static final long NULL_LENGTH = -1L;
   private int bufLength = 0;
   private byte[] byteBuffer;
   private int position = 0;
   protected boolean wasMultiPacket = false;

   Buffer(byte[] buf) {
      this.byteBuffer = buf;
      this.setBufLength(buf.length);
   }

   Buffer(int size) {
      this.byteBuffer = new byte[size];
      this.setBufLength(this.byteBuffer.length);
      this.position = 4;
   }

   final void clear() {
      this.position = 4;
   }

   final void dump() {
      this.dump(this.getBufLength());
   }

   final String dump(int numBytes) {
      return StringUtils.dumpAsHex(this.getBytes(0, numBytes > this.getBufLength() ? this.getBufLength() : numBytes), numBytes > this.getBufLength() ? this.getBufLength() : numBytes);
   }

   final String dumpClampedBytes(int numBytes) {
      int numBytesToDump = numBytes < 512 ? numBytes : 512;
      String dumped = StringUtils.dumpAsHex(this.getBytes(0, numBytesToDump > this.getBufLength() ? this.getBufLength() : numBytesToDump), numBytesToDump > this.getBufLength() ? this.getBufLength() : numBytesToDump);
      return numBytesToDump < numBytes ? dumped + " ....(packet exceeds max. dump length)" : dumped;
   }

   final void dumpHeader() {
      for(int i = 0; i < 4; ++i) {
         String hexVal = Integer.toHexString(this.readByte(i) & 255);
         if (hexVal.length() == 1) {
            hexVal = "0" + hexVal;
         }

         System.out.print(hexVal + " ");
      }

   }

   final void dumpNBytes(int start, int nBytes) {
      StringBuffer asciiBuf = new StringBuffer();

      for(int i = start; i < start + nBytes && i < this.getBufLength(); ++i) {
         String hexVal = Integer.toHexString(this.readByte(i) & 255);
         if (hexVal.length() == 1) {
            hexVal = "0" + hexVal;
         }

         System.out.print(hexVal + " ");
         if (this.readByte(i) > 32 && this.readByte(i) < 127) {
            asciiBuf.append((char)this.readByte(i));
         } else {
            asciiBuf.append(".");
         }

         asciiBuf.append(" ");
      }

      System.out.println("    " + asciiBuf.toString());
   }

   final void ensureCapacity(int additionalData) throws SQLException {
      if (this.position + additionalData > this.getBufLength()) {
         if (this.position + additionalData < this.byteBuffer.length) {
            this.setBufLength(this.byteBuffer.length);
         } else {
            int newLength = (int)((double)this.byteBuffer.length * (double)1.25F);
            if (newLength < this.byteBuffer.length + additionalData) {
               newLength = this.byteBuffer.length + (int)((double)additionalData * (double)1.25F);
            }

            if (newLength < this.byteBuffer.length) {
               newLength = this.byteBuffer.length + additionalData;
            }

            byte[] newBytes = new byte[newLength];
            System.arraycopy(this.byteBuffer, 0, newBytes, 0, this.byteBuffer.length);
            this.byteBuffer = newBytes;
            this.setBufLength(this.byteBuffer.length);
         }
      }

   }

   public int fastSkipLenString() {
      long len = this.readFieldLength();
      this.position = (int)((long)this.position + len);
      return (int)len;
   }

   public void fastSkipLenByteArray() {
      long len = this.readFieldLength();
      if (len != -1L && len != 0L) {
         this.position = (int)((long)this.position + len);
      }
   }

   protected final byte[] getBufferSource() {
      return this.byteBuffer;
   }

   int getBufLength() {
      return this.bufLength;
   }

   public byte[] getByteBuffer() {
      return this.byteBuffer;
   }

   final byte[] getBytes(int len) {
      byte[] b = new byte[len];
      System.arraycopy(this.byteBuffer, this.position, b, 0, len);
      this.position += len;
      return b;
   }

   byte[] getBytes(int offset, int len) {
      byte[] dest = new byte[len];
      System.arraycopy(this.byteBuffer, offset, dest, 0, len);
      return dest;
   }

   int getCapacity() {
      return this.byteBuffer.length;
   }

   public ByteBuffer getNioBuffer() {
      throw new IllegalArgumentException(Messages.getString("ByteArrayBuffer.0"));
   }

   public int getPosition() {
      return this.position;
   }

   final boolean isLastDataPacket() {
      return this.getBufLength() < 9 && (this.byteBuffer[0] & 255) == 254;
   }

   final long newReadLength() {
      int sw = this.byteBuffer[this.position++] & 255;
      switch (sw) {
         case 251:
            return 0L;
         case 252:
            return (long)this.readInt();
         case 253:
            return (long)this.readLongInt();
         case 254:
            return this.readLongLong();
         default:
            return (long)sw;
      }
   }

   final byte readByte() {
      return this.byteBuffer[this.position++];
   }

   final byte readByte(int readAt) {
      return this.byteBuffer[readAt];
   }

   final long readFieldLength() {
      int sw = this.byteBuffer[this.position++] & 255;
      switch (sw) {
         case 251:
            return -1L;
         case 252:
            return (long)this.readInt();
         case 253:
            return (long)this.readLongInt();
         case 254:
            return this.readLongLong();
         default:
            return (long)sw;
      }
   }

   final int readInt() {
      byte[] b = this.byteBuffer;
      return b[this.position++] & 255 | (b[this.position++] & 255) << 8;
   }

   final int readIntAsLong() {
      byte[] b = this.byteBuffer;
      return b[this.position++] & 255 | (b[this.position++] & 255) << 8 | (b[this.position++] & 255) << 16 | (b[this.position++] & 255) << 24;
   }

   final byte[] readLenByteArray(int offset) {
      long len = this.readFieldLength();
      if (len == -1L) {
         return null;
      } else if (len == 0L) {
         return Constants.EMPTY_BYTE_ARRAY;
      } else {
         this.position += offset;
         return this.getBytes((int)len);
      }
   }

   final long readLength() {
      int sw = this.byteBuffer[this.position++] & 255;
      switch (sw) {
         case 251:
            return 0L;
         case 252:
            return (long)this.readInt();
         case 253:
            return (long)this.readLongInt();
         case 254:
            return this.readLong();
         default:
            return (long)sw;
      }
   }

   final long readLong() {
      byte[] b = this.byteBuffer;
      return (long)b[this.position++] & 255L | ((long)b[this.position++] & 255L) << 8 | (long)(b[this.position++] & 255) << 16 | (long)(b[this.position++] & 255) << 24;
   }

   final int readLongInt() {
      byte[] b = this.byteBuffer;
      return b[this.position++] & 255 | (b[this.position++] & 255) << 8 | (b[this.position++] & 255) << 16;
   }

   final long readLongLong() {
      byte[] b = this.byteBuffer;
      return (long)(b[this.position++] & 255) | (long)(b[this.position++] & 255) << 8 | (long)(b[this.position++] & 255) << 16 | (long)(b[this.position++] & 255) << 24 | (long)(b[this.position++] & 255) << 32 | (long)(b[this.position++] & 255) << 40 | (long)(b[this.position++] & 255) << 48 | (long)(b[this.position++] & 255) << 56;
   }

   final int readnBytes() {
      int sw = this.byteBuffer[this.position++] & 255;
      switch (sw) {
         case 1:
            return this.byteBuffer[this.position++] & 255;
         case 2:
            return this.readInt();
         case 3:
            return this.readLongInt();
         case 4:
            return (int)this.readLong();
         default:
            return 255;
      }
   }

   final String readString() {
      int i = this.position;
      int len = 0;

      for(int maxLen = this.getBufLength(); i < maxLen && this.byteBuffer[i] != 0; ++i) {
         ++len;
      }

      String s = new String(this.byteBuffer, this.position, len);
      this.position += len + 1;
      return s;
   }

   final String readString(String encoding) throws SQLException {
      int i = this.position;
      int len = 0;

      for(int maxLen = this.getBufLength(); i < maxLen && this.byteBuffer[i] != 0; ++i) {
         ++len;
      }

      String var5;
      try {
         var5 = new String(this.byteBuffer, this.position, len, encoding);
      } catch (UnsupportedEncodingException var9) {
         throw SQLError.createSQLException(Messages.getString("ByteArrayBuffer.1") + encoding + "'", "S1009");
      } finally {
         this.position += len + 1;
      }

      return var5;
   }

   void setBufLength(int bufLengthToSet) {
      this.bufLength = bufLengthToSet;
   }

   public void setByteBuffer(byte[] byteBufferToSet) {
      this.byteBuffer = byteBufferToSet;
   }

   public void setPosition(int positionToSet) {
      this.position = positionToSet;
   }

   public void setWasMultiPacket(boolean flag) {
      this.wasMultiPacket = flag;
   }

   public String toString() {
      return this.dumpClampedBytes(this.getPosition());
   }

   public String toSuperString() {
      return super.toString();
   }

   public boolean wasMultiPacket() {
      return this.wasMultiPacket;
   }

   final void writeByte(byte b) throws SQLException {
      this.ensureCapacity(1);
      this.byteBuffer[this.position++] = b;
   }

   final void writeBytesNoNull(byte[] bytes) throws SQLException {
      int len = bytes.length;
      this.ensureCapacity(len);
      System.arraycopy(bytes, 0, this.byteBuffer, this.position, len);
      this.position += len;
   }

   final void writeBytesNoNull(byte[] bytes, int offset, int length) throws SQLException {
      this.ensureCapacity(length);
      System.arraycopy(bytes, offset, this.byteBuffer, this.position, length);
      this.position += length;
   }

   final void writeDouble(double d) throws SQLException {
      long l = Double.doubleToLongBits(d);
      this.writeLongLong(l);
   }

   final void writeFieldLength(long length) throws SQLException {
      if (length < 251L) {
         this.writeByte((byte)((int)length));
      } else if (length < 65536L) {
         this.ensureCapacity(3);
         this.writeByte((byte)-4);
         this.writeInt((int)length);
      } else if (length < 16777216L) {
         this.ensureCapacity(4);
         this.writeByte((byte)-3);
         this.writeLongInt((int)length);
      } else {
         this.ensureCapacity(9);
         this.writeByte((byte)-2);
         this.writeLongLong(length);
      }

   }

   final void writeFloat(float f) throws SQLException {
      this.ensureCapacity(4);
      int i = Float.floatToIntBits(f);
      byte[] b = this.byteBuffer;
      b[this.position++] = (byte)(i & 255);
      b[this.position++] = (byte)(i >>> 8);
      b[this.position++] = (byte)(i >>> 16);
      b[this.position++] = (byte)(i >>> 24);
   }

   final void writeInt(int i) throws SQLException {
      this.ensureCapacity(2);
      byte[] b = this.byteBuffer;
      b[this.position++] = (byte)(i & 255);
      b[this.position++] = (byte)(i >>> 8);
   }

   final void writeLenBytes(byte[] b) throws SQLException {
      int len = b.length;
      this.ensureCapacity(len + 9);
      this.writeFieldLength((long)len);
      System.arraycopy(b, 0, this.byteBuffer, this.position, len);
      this.position += len;
   }

   final void writeLenString(String s, String encoding, String serverEncoding, SingleByteCharsetConverter converter, boolean parserKnowsUnicode, ConnectionImpl conn) throws UnsupportedEncodingException, SQLException {
      byte[] b = null;
      if (converter != null) {
         b = converter.toBytes(s);
      } else {
         b = StringUtils.getBytes(s, encoding, serverEncoding, parserKnowsUnicode, conn);
      }

      int len = b.length;
      this.ensureCapacity(len + 9);
      this.writeFieldLength((long)len);
      System.arraycopy(b, 0, this.byteBuffer, this.position, len);
      this.position += len;
   }

   final void writeLong(long i) throws SQLException {
      this.ensureCapacity(4);
      byte[] b = this.byteBuffer;
      b[this.position++] = (byte)((int)(i & 255L));
      b[this.position++] = (byte)((int)(i >>> 8));
      b[this.position++] = (byte)((int)(i >>> 16));
      b[this.position++] = (byte)((int)(i >>> 24));
   }

   final void writeLongInt(int i) throws SQLException {
      this.ensureCapacity(3);
      byte[] b = this.byteBuffer;
      b[this.position++] = (byte)(i & 255);
      b[this.position++] = (byte)(i >>> 8);
      b[this.position++] = (byte)(i >>> 16);
   }

   final void writeLongLong(long i) throws SQLException {
      this.ensureCapacity(8);
      byte[] b = this.byteBuffer;
      b[this.position++] = (byte)((int)(i & 255L));
      b[this.position++] = (byte)((int)(i >>> 8));
      b[this.position++] = (byte)((int)(i >>> 16));
      b[this.position++] = (byte)((int)(i >>> 24));
      b[this.position++] = (byte)((int)(i >>> 32));
      b[this.position++] = (byte)((int)(i >>> 40));
      b[this.position++] = (byte)((int)(i >>> 48));
      b[this.position++] = (byte)((int)(i >>> 56));
   }

   final void writeString(String s) throws SQLException {
      this.ensureCapacity(s.length() * 2 + 1);
      this.writeStringNoNull(s);
      this.byteBuffer[this.position++] = 0;
   }

   final void writeString(String s, String encoding, ConnectionImpl conn) throws SQLException {
      this.ensureCapacity(s.length() * 2 + 1);

      try {
         this.writeStringNoNull(s, encoding, encoding, false, conn);
      } catch (UnsupportedEncodingException ue) {
         throw new SQLException(ue.toString(), "S1000");
      }

      this.byteBuffer[this.position++] = 0;
   }

   final void writeStringNoNull(String s) throws SQLException {
      int len = s.length();
      this.ensureCapacity(len * 2);
      System.arraycopy(s.getBytes(), 0, this.byteBuffer, this.position, len);
      this.position += len;
   }

   final void writeStringNoNull(String s, String encoding, String serverEncoding, boolean parserKnowsUnicode, ConnectionImpl conn) throws UnsupportedEncodingException, SQLException {
      byte[] b = StringUtils.getBytes(s, encoding, serverEncoding, parserKnowsUnicode, conn);
      int len = b.length;
      this.ensureCapacity(len);
      System.arraycopy(b, 0, this.byteBuffer, this.position, len);
      this.position += len;
   }
}
