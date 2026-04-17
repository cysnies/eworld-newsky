package org.hibernate.type.descriptor.java;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Arrays;
import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.BinaryStream;
import org.hibernate.engine.jdbc.internal.BinaryStreamImpl;
import org.hibernate.type.descriptor.WrapperOptions;

public class PrimitiveByteArrayTypeDescriptor extends AbstractTypeDescriptor {
   public static final PrimitiveByteArrayTypeDescriptor INSTANCE = new PrimitiveByteArrayTypeDescriptor();

   public PrimitiveByteArrayTypeDescriptor() {
      super(byte[].class, ArrayMutabilityPlan.INSTANCE);
   }

   public boolean areEqual(byte[] one, byte[] another) {
      return one == another || one != null && another != null && Arrays.equals(one, another);
   }

   public int extractHashCode(byte[] bytes) {
      int hashCode = 1;

      for(byte aByte : bytes) {
         hashCode = 31 * hashCode + aByte;
      }

      return hashCode;
   }

   public String toString(byte[] bytes) {
      StringBuilder buf = new StringBuilder(bytes.length * 2);

      for(byte aByte : bytes) {
         String hexStr = Integer.toHexString(aByte - -128);
         if (hexStr.length() == 1) {
            buf.append('0');
         }

         buf.append(hexStr);
      }

      return buf.toString();
   }

   public byte[] fromString(String string) {
      if (string == null) {
         return null;
      } else if (string.length() % 2 != 0) {
         throw new IllegalArgumentException("The string is not a valid string representation of a binary content.");
      } else {
         byte[] bytes = new byte[string.length() / 2];

         for(int i = 0; i < bytes.length; ++i) {
            String hexStr = string.substring(i * 2, (i + 1) * 2);
            bytes[i] = (byte)(Integer.parseInt(hexStr, 16) + -128);
         }

         return bytes;
      }
   }

   public Object unwrap(byte[] value, Class type, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (byte[].class.isAssignableFrom(type)) {
         return value;
      } else if (InputStream.class.isAssignableFrom(type)) {
         return new ByteArrayInputStream(value);
      } else if (BinaryStream.class.isAssignableFrom(type)) {
         return new BinaryStreamImpl(value);
      } else if (Blob.class.isAssignableFrom(type)) {
         return options.getLobCreator().createBlob(value);
      } else {
         throw this.unknownUnwrap(type);
      }
   }

   public byte[] wrap(Object value, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (byte[].class.isInstance(value)) {
         return (byte[])value;
      } else if (InputStream.class.isInstance(value)) {
         return DataHelper.extractBytes((InputStream)value);
      } else if (!Blob.class.isInstance(value) && !DataHelper.isNClob(value.getClass())) {
         throw this.unknownWrap(value.getClass());
      } else {
         try {
            return DataHelper.extractBytes(((Blob)value).getBinaryStream());
         } catch (SQLException e) {
            throw new HibernateException("Unable to access lob stream", e);
         }
      }
   }
}
