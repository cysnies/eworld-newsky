package com.comphenix.protocol.injector.packet;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class CaptureInputStream extends FilterInputStream {
   protected OutputStream out;

   public CaptureInputStream(InputStream in, OutputStream out) {
      super(in);
      this.out = out;
   }

   public int read() throws IOException {
      int value = super.read();
      if (value >= 0) {
         this.out.write(value);
      }

      return value;
   }

   public void close() throws IOException {
      super.close();
      this.out.close();
   }

   public int read(byte[] b) throws IOException {
      int count = super.read(b);
      if (count > 0) {
         this.out.write(b, 0, count);
      }

      return count;
   }

   public int read(byte[] b, int off, int len) throws IOException {
      int count = super.read(b, off, len);
      if (count > 0) {
         this.out.write(b, off, count);
      }

      return count;
   }

   public OutputStream getOutputStream() {
      return this.out;
   }
}
