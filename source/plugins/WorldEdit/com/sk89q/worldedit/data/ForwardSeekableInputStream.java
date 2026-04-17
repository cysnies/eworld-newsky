package com.sk89q.worldedit.data;

import java.io.IOException;
import java.io.InputStream;

public class ForwardSeekableInputStream extends InputStream {
   protected InputStream parent;
   protected long position = 0L;

   public ForwardSeekableInputStream(InputStream parent) {
      this.parent = parent;
   }

   public int read() throws IOException {
      int ret = this.parent.read();
      ++this.position;
      return ret;
   }

   public int available() throws IOException {
      return this.parent.available();
   }

   public void close() throws IOException {
      this.parent.close();
   }

   public synchronized void mark(int readlimit) {
      this.parent.mark(readlimit);
   }

   public boolean markSupported() {
      return this.parent.markSupported();
   }

   public int read(byte[] b, int off, int len) throws IOException {
      int read = super.read(b, off, len);
      this.position += (long)read;
      return read;
   }

   public int read(byte[] b) throws IOException {
      int read = this.parent.read(b);
      this.position += (long)read;
      return read;
   }

   public synchronized void reset() throws IOException {
      this.parent.reset();
   }

   public long skip(long n) throws IOException {
      long skipped = this.parent.skip(n);
      this.position += skipped;
      return skipped;
   }

   public void seek(long n) throws IOException {
      long diff = n - this.position;
      if (diff < 0L) {
         throw new IOException("Can't seek backwards");
      } else if (diff != 0L) {
         if (this.skip(diff) < diff) {
            throw new IOException("Failed to seek " + diff + " bytes");
         }
      }
   }
}
