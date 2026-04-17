package org.hibernate.engine.jdbc.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.hibernate.engine.jdbc.BinaryStream;

public class BinaryStreamImpl extends ByteArrayInputStream implements BinaryStream {
   private final int length;

   public BinaryStreamImpl(byte[] bytes) {
      super(bytes);
      this.length = bytes.length;
   }

   public InputStream getInputStream() {
      return this;
   }

   public byte[] getBytes() {
      return this.buf;
   }

   public long getLength() {
      return (long)this.length;
   }

   public void release() {
      try {
         super.close();
      } catch (IOException var2) {
      }

   }
}
