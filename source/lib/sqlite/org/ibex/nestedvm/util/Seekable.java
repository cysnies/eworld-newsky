package org.ibex.nestedvm.util;

import java.io.IOException;
import java.io.RandomAccessFile;

public abstract class Seekable {
   public abstract int read(byte[] var1, int var2, int var3) throws IOException;

   public abstract int write(byte[] var1, int var2, int var3) throws IOException;

   public abstract int length() throws IOException;

   public abstract void seek(int var1) throws IOException;

   public abstract void close() throws IOException;

   public abstract int pos() throws IOException;

   public void sync() throws IOException {
      throw new IOException("sync not implemented for " + this.getClass());
   }

   public void resize(long var1) throws IOException {
      throw new IOException("resize not implemented for " + this.getClass());
   }

   public Lock lock(long var1, long var3, boolean var5) throws IOException {
      throw new IOException("lock not implemented for " + this.getClass());
   }

   public int read() throws IOException {
      byte[] var1 = new byte[1];
      int var2 = this.read(var1, 0, 1);
      return var2 == -1 ? -1 : var1[0] & 255;
   }

   public int tryReadFully(byte[] var1, int var2, int var3) throws IOException {
      int var4;
      int var5;
      for(var4 = 0; var3 > 0; var4 += var5) {
         var5 = this.read(var1, var2, var3);
         if (var5 == -1) {
            break;
         }

         var2 += var5;
         var3 -= var5;
      }

      return var4 == 0 ? -1 : var4;
   }

   public static class ByteArray extends Seekable {
      protected byte[] data;
      protected int pos;
      private final boolean writable;

      public ByteArray(byte[] var1, boolean var2) {
         this.data = var1;
         this.pos = 0;
         this.writable = var2;
      }

      public int read(byte[] var1, int var2, int var3) {
         var3 = Math.min(var3, this.data.length - this.pos);
         if (var3 <= 0) {
            return -1;
         } else {
            System.arraycopy(this.data, this.pos, var1, var2, var3);
            this.pos += var3;
            return var3;
         }
      }

      public int write(byte[] var1, int var2, int var3) throws IOException {
         if (!this.writable) {
            throw new IOException("read-only data");
         } else {
            var3 = Math.min(var3, this.data.length - this.pos);
            if (var3 <= 0) {
               throw new IOException("no space");
            } else {
               System.arraycopy(var1, var2, this.data, this.pos, var3);
               this.pos += var3;
               return var3;
            }
         }
      }

      public int length() {
         return this.data.length;
      }

      public int pos() {
         return this.pos;
      }

      public void seek(int var1) {
         this.pos = var1;
      }

      public void close() {
      }
   }

   public static class File extends Seekable {
      private final java.io.File file;
      private final RandomAccessFile raf;

      public File(String var1) throws IOException {
         this(var1, false);
      }

      public File(String var1, boolean var2) throws IOException {
         this(new java.io.File(var1), var2, false);
      }

      public File(java.io.File var1, boolean var2, boolean var3) throws IOException {
         this.file = var1;
         String var4 = var2 ? "rw" : "r";
         this.raf = new RandomAccessFile(var1, var4);
         if (var3) {
            Platform.setFileLength(this.raf, 0);
         }

      }

      public int read(byte[] var1, int var2, int var3) throws IOException {
         return this.raf.read(var1, var2, var3);
      }

      public int write(byte[] var1, int var2, int var3) throws IOException {
         this.raf.write(var1, var2, var3);
         return var3;
      }

      public void sync() throws IOException {
         this.raf.getFD().sync();
      }

      public void seek(int var1) throws IOException {
         this.raf.seek((long)var1);
      }

      public int pos() throws IOException {
         return (int)this.raf.getFilePointer();
      }

      public int length() throws IOException {
         return (int)this.raf.length();
      }

      public void close() throws IOException {
         this.raf.close();
      }

      public void resize(long var1) throws IOException {
         Platform.setFileLength(this.raf, (int)var1);
      }

      public boolean equals(Object var1) {
         return var1 != null && var1 instanceof File && this.file.equals(((File)var1).file);
      }

      public Lock lock(long var1, long var3, boolean var5) throws IOException {
         return Platform.lockFile(this, this.raf, var1, var3, var5);
      }
   }

   public static class InputStream extends Seekable {
      private byte[] buffer = new byte[4096];
      private int bytesRead = 0;
      private boolean eof = false;
      private int pos;
      private java.io.InputStream is;

      public InputStream(java.io.InputStream var1) {
         this.is = var1;
      }

      public int read(byte[] var1, int var2, int var3) throws IOException {
         if (this.pos >= this.bytesRead && !this.eof) {
            this.readTo(this.pos + 1);
         }

         var3 = Math.min(var3, this.bytesRead - this.pos);
         if (var3 <= 0) {
            return -1;
         } else {
            System.arraycopy(this.buffer, this.pos, var1, var2, var3);
            this.pos += var3;
            return var3;
         }
      }

      private void readTo(int var1) throws IOException {
         if (var1 >= this.buffer.length) {
            byte[] var2 = new byte[Math.max(this.buffer.length + Math.min(this.buffer.length, 65536), var1)];
            System.arraycopy(this.buffer, 0, var2, 0, this.bytesRead);
            this.buffer = var2;
         }

         while(this.bytesRead < var1) {
            int var3 = this.is.read(this.buffer, this.bytesRead, this.buffer.length - this.bytesRead);
            if (var3 == -1) {
               this.eof = true;
               break;
            }

            this.bytesRead += var3;
         }

      }

      public int length() throws IOException {
         while(!this.eof) {
            this.readTo(this.bytesRead + 4096);
         }

         return this.bytesRead;
      }

      public int write(byte[] var1, int var2, int var3) throws IOException {
         throw new IOException("read-only");
      }

      public void seek(int var1) {
         this.pos = var1;
      }

      public int pos() {
         return this.pos;
      }

      public void close() throws IOException {
         this.is.close();
      }
   }

   public abstract static class Lock {
      private Object owner = null;

      public abstract Seekable seekable();

      public abstract boolean isShared();

      public abstract boolean isValid();

      public abstract void release() throws IOException;

      public abstract long position();

      public abstract long size();

      public void setOwner(Object var1) {
         this.owner = var1;
      }

      public Object getOwner() {
         return this.owner;
      }

      public final boolean contains(int var1, int var2) {
         return (long)var1 >= this.position() && this.position() + this.size() >= (long)(var1 + var2);
      }

      public final boolean contained(int var1, int var2) {
         return (long)var1 < this.position() && this.position() + this.size() < (long)(var1 + var2);
      }

      public final boolean overlaps(int var1, int var2) {
         return this.contains(var1, var2) || this.contained(var1, var2);
      }
   }
}
