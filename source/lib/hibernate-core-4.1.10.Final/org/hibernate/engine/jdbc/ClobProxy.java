package org.hibernate.engine.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Clob;
import java.sql.SQLException;
import org.hibernate.engine.jdbc.internal.CharacterStreamImpl;
import org.hibernate.type.descriptor.java.DataHelper;

public class ClobProxy implements InvocationHandler {
   private static final Class[] PROXY_INTERFACES = new Class[]{Clob.class, ClobImplementer.class};
   private final CharacterStream characterStream;
   private boolean needsReset = false;

   protected ClobProxy(String string) {
      this.characterStream = new CharacterStreamImpl(string);
   }

   protected ClobProxy(Reader reader, long length) {
      this.characterStream = new CharacterStreamImpl(reader, length);
   }

   protected long getLength() {
      return this.characterStream.getLength();
   }

   protected InputStream getAsciiStream() throws SQLException {
      this.resetIfNeeded();
      return new ReaderInputStream(this.characterStream.asReader());
   }

   protected Reader getCharacterStream() throws SQLException {
      this.resetIfNeeded();
      return this.characterStream.asReader();
   }

   protected String getSubString(long start, int length) {
      String string = this.characterStream.asString();
      int endIndex = Math.min((int)start + length, string.length());
      return string.substring((int)start, endIndex);
   }

   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      String methodName = method.getName();
      int argCount = method.getParameterTypes().length;
      if ("length".equals(methodName) && argCount == 0) {
         return this.getLength();
      } else if ("getUnderlyingStream".equals(methodName)) {
         return this.characterStream;
      } else if ("getAsciiStream".equals(methodName) && argCount == 0) {
         return this.getAsciiStream();
      } else {
         if ("getCharacterStream".equals(methodName)) {
            if (argCount == 0) {
               return this.getCharacterStream();
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

               return DataHelper.subStream(this.getCharacterStream(), start - 1L, length);
            }
         }

         if ("getSubString".equals(methodName) && argCount == 2) {
            long start = (Long)args[0];
            if (start < 1L) {
               throw new SQLException("Start position 1-based; must be 1 or more.");
            } else if (start > this.getLength()) {
               throw new SQLException("Start position [" + start + "] cannot exceed overall CLOB length [" + this.getLength() + "]");
            } else {
               int length = (Integer)args[1];
               if (length < 0) {
                  throw new SQLException("Length must be great-than-or-equal to zero.");
               } else {
                  return this.getSubString(start - 1L, length);
               }
            }
         } else if ("free".equals(methodName) && argCount == 0) {
            this.characterStream.release();
            return null;
         } else if ("toString".equals(methodName) && argCount == 0) {
            return this.toString();
         } else if ("equals".equals(methodName) && argCount == 1) {
            return proxy == args[0];
         } else if ("hashCode".equals(methodName) && argCount == 0) {
            return this.hashCode();
         } else {
            throw new UnsupportedOperationException("Clob may not be manipulated from creating session");
         }
      }
   }

   protected void resetIfNeeded() throws SQLException {
      try {
         if (this.needsReset) {
            this.characterStream.asReader().reset();
         }
      } catch (IOException ioe) {
         throw new SQLException("could not reset reader", ioe);
      }

      this.needsReset = true;
   }

   public static Clob generateProxy(String string) {
      return (Clob)Proxy.newProxyInstance(getProxyClassLoader(), PROXY_INTERFACES, new ClobProxy(string));
   }

   public static Clob generateProxy(Reader reader, long length) {
      return (Clob)Proxy.newProxyInstance(getProxyClassLoader(), PROXY_INTERFACES, new ClobProxy(reader, length));
   }

   protected static ClassLoader getProxyClassLoader() {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      if (cl == null) {
         cl = ClobImplementer.class.getClassLoader();
      }

      return cl;
   }
}
