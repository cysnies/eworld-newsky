package org.hibernate.engine.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Blob;
import java.sql.SQLException;
import org.hibernate.engine.jdbc.internal.BinaryStreamImpl;
import org.hibernate.type.descriptor.java.DataHelper;

public class BlobProxy implements InvocationHandler {
   private static final Class[] PROXY_INTERFACES = new Class[]{Blob.class, BlobImplementer.class};
   private BinaryStream binaryStream;
   private boolean needsReset = false;

   private BlobProxy(byte[] bytes) {
      this.binaryStream = new BinaryStreamImpl(bytes);
   }

   private BlobProxy(InputStream stream, long length) {
      this.binaryStream = new StreamBackedBinaryStream(stream, length);
   }

   private long getLength() {
      return this.binaryStream.getLength();
   }

   private InputStream getStream() throws SQLException {
      InputStream stream = this.binaryStream.getInputStream();

      try {
         if (this.needsReset) {
            stream.reset();
         }
      } catch (IOException var3) {
         throw new SQLException("could not reset reader");
      }

      this.needsReset = true;
      return stream;
   }

   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      String methodName = method.getName();
      int argCount = method.getParameterTypes().length;
      if ("length".equals(methodName) && argCount == 0) {
         return this.getLength();
      } else if ("getUnderlyingStream".equals(methodName)) {
         return this.binaryStream;
      } else {
         if ("getBinaryStream".equals(methodName)) {
            if (argCount == 0) {
               return this.getStream();
            }

            if (argCount == 2) {
               long start = (Long)args[0];
               if (start < 1L) {
                  throw new SQLException("Start position 1-based; must be 1 or more.");
               }

               if (start > this.getLength()) {
                  throw new SQLException("Start position [" + start + "] cannot exceed overall CLOB length [" + this.getLength() + "]");
               }

               int length = (Integer)args[1];
               if (length < 0) {
                  throw new SQLException("Length must be great-than-or-equal to zero.");
               }

               return DataHelper.subStream(this.getStream(), start - 1L, length);
            }
         }

         if ("getBytes".equals(methodName) && argCount == 2) {
            long start = (Long)args[0];
            if (start < 1L) {
               throw new SQLException("Start position 1-based; must be 1 or more.");
            } else {
               int length = (Integer)args[1];
               if (length < 0) {
                  throw new SQLException("Length must be great-than-or-equal to zero.");
               } else {
                  return DataHelper.extractBytes(this.getStream(), start - 1L, length);
               }
            }
         } else if ("free".equals(methodName) && argCount == 0) {
            this.binaryStream.release();
            return null;
         } else if ("toString".equals(methodName) && argCount == 0) {
            return this.toString();
         } else if ("equals".equals(methodName) && argCount == 1) {
            return proxy == args[0];
         } else if ("hashCode".equals(methodName) && argCount == 0) {
            return this.hashCode();
         } else {
            throw new UnsupportedOperationException("Blob may not be manipulated from creating session");
         }
      }
   }

   public static Blob generateProxy(byte[] bytes) {
      return (Blob)Proxy.newProxyInstance(getProxyClassLoader(), PROXY_INTERFACES, new BlobProxy(bytes));
   }

   public static Blob generateProxy(InputStream stream, long length) {
      return (Blob)Proxy.newProxyInstance(getProxyClassLoader(), PROXY_INTERFACES, new BlobProxy(stream, length));
   }

   private static ClassLoader getProxyClassLoader() {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      if (cl == null) {
         cl = BlobImplementer.class.getClassLoader();
      }

      return cl;
   }

   private static class StreamBackedBinaryStream implements BinaryStream {
      private final InputStream stream;
      private final long length;
      private byte[] bytes;

      private StreamBackedBinaryStream(InputStream stream, long length) {
         this.stream = stream;
         this.length = length;
      }

      public InputStream getInputStream() {
         return this.stream;
      }

      public byte[] getBytes() {
         if (this.bytes == null) {
            this.bytes = DataHelper.extractBytes(this.stream);
         }

         return this.bytes;
      }

      public long getLength() {
         return (long)((int)this.length);
      }

      public void release() {
         try {
            this.stream.close();
         } catch (IOException var2) {
         }

      }
   }
}
