package org.hibernate.type;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;
import org.hibernate.HibernateException;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.spi.SessionImplementor;

/** @deprecated */
public abstract class AbstractBynaryType extends MutableType implements VersionType, Comparator {
   protected abstract Object toExternalFormat(byte[] var1);

   protected abstract byte[] toInternalFormat(Object var1);

   public void set(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
      byte[] internalValue = this.toInternalFormat(value);
      if (Environment.useStreamsForBinary()) {
         st.setBinaryStream(index, new ByteArrayInputStream(internalValue), internalValue.length);
      } else {
         st.setBytes(index, internalValue);
      }

   }

   public Object get(ResultSet rs, String name) throws HibernateException, SQLException {
      if (!Environment.useStreamsForBinary()) {
         return this.toExternalFormat(rs.getBytes(name));
      } else {
         InputStream inputStream = rs.getBinaryStream(name);
         if (inputStream == null) {
            return this.toExternalFormat((byte[])null);
         } else {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(2048);
            byte[] buffer = new byte[2048];

            try {
               while(true) {
                  int amountRead = inputStream.read(buffer);
                  if (amountRead == -1) {
                     inputStream.close();
                     outputStream.close();
                     return this.toExternalFormat(outputStream.toByteArray());
                  }

                  outputStream.write(buffer, 0, amountRead);
               }
            } catch (IOException ioe) {
               throw new HibernateException("IOException occurred reading a binary value", ioe);
            }
         }
      }
   }

   public int sqlType() {
      return -3;
   }

   public Object seed(SessionImplementor session) {
      return null;
   }

   public Object next(Object current, SessionImplementor session) {
      return current;
   }

   public Comparator getComparator() {
      return this;
   }

   public boolean isEqual(Object x, Object y) {
      return x == y || x != null && y != null && Arrays.equals(this.toInternalFormat(x), this.toInternalFormat(y));
   }

   public int getHashCode(Object x) {
      byte[] bytes = this.toInternalFormat(x);
      int hashCode = 1;

      for(int j = 0; j < bytes.length; ++j) {
         hashCode = 31 * hashCode + bytes[j];
      }

      return hashCode;
   }

   public int compare(Object x, Object y) {
      byte[] xbytes = this.toInternalFormat(x);
      byte[] ybytes = this.toInternalFormat(y);
      if (xbytes.length < ybytes.length) {
         return -1;
      } else if (xbytes.length > ybytes.length) {
         return 1;
      } else {
         for(int i = 0; i < xbytes.length; ++i) {
            if (xbytes[i] < ybytes[i]) {
               return -1;
            }

            if (xbytes[i] > ybytes[i]) {
               return 1;
            }
         }

         return 0;
      }
   }

   public abstract String getName();

   public String toString(Object val) {
      byte[] bytes = this.toInternalFormat(val);
      StringBuilder buf = new StringBuilder();

      for(int i = 0; i < bytes.length; ++i) {
         String hexStr = Integer.toHexString(bytes[i] - -128);
         if (hexStr.length() == 1) {
            buf.append('0');
         }

         buf.append(hexStr);
      }

      return buf.toString();
   }

   public Object deepCopyNotNull(Object value) {
      byte[] bytes = this.toInternalFormat(value);
      byte[] result = new byte[bytes.length];
      System.arraycopy(bytes, 0, result, 0, bytes.length);
      return this.toExternalFormat(result);
   }

   public Object fromStringValue(String xml) throws HibernateException {
      if (xml == null) {
         return null;
      } else if (xml.length() % 2 != 0) {
         throw new IllegalArgumentException("The string is not a valid xml representation of a binary content.");
      } else {
         byte[] bytes = new byte[xml.length() / 2];

         for(int i = 0; i < bytes.length; ++i) {
            String hexStr = xml.substring(i * 2, (i + 1) * 2);
            bytes[i] = (byte)(Integer.parseInt(hexStr, 16) + -128);
         }

         return this.toExternalFormat(bytes);
      }
   }
}
