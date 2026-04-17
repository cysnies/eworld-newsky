package org.hibernate.type.descriptor.java;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.BinaryStream;
import org.hibernate.engine.jdbc.internal.BinaryStreamImpl;
import org.hibernate.type.descriptor.WrapperOptions;

public class ByteArrayTypeDescriptor extends AbstractTypeDescriptor {
   public static final ByteArrayTypeDescriptor INSTANCE = new ByteArrayTypeDescriptor();

   public ByteArrayTypeDescriptor() {
      super(Byte[].class, ArrayMutabilityPlan.INSTANCE);
   }

   public String toString(Byte[] bytes) {
      StringBuilder buf = new StringBuilder();

      for(Byte aByte : bytes) {
         String hexStr = Integer.toHexString(aByte - -128);
         if (hexStr.length() == 1) {
            buf.append('0');
         }

         buf.append(hexStr);
      }

      return buf.toString();
   }

   public Byte[] fromString(String string) {
      if (string == null) {
         return null;
      } else if (string.length() % 2 != 0) {
         throw new IllegalArgumentException("The string is not a valid string representation of a binary content.");
      } else {
         Byte[] bytes = new Byte[string.length() / 2];

         for(int i = 0; i < bytes.length; ++i) {
            String hexStr = string.substring(i * 2, (i + 1) * 2);
            bytes[i] = (byte)(Integer.parseInt(hexStr, 16) + -128);
         }

         return bytes;
      }
   }

   public Object unwrap(Byte[] value, Class type, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (Byte[].class.isAssignableFrom(type)) {
         return value;
      } else if (byte[].class.isAssignableFrom(type)) {
         return this.unwrapBytes(value);
      } else if (InputStream.class.isAssignableFrom(type)) {
         return new ByteArrayInputStream(this.unwrapBytes(value));
      } else if (BinaryStream.class.isAssignableFrom(type)) {
         return new BinaryStreamImpl(this.unwrapBytes(value));
      } else if (Blob.class.isAssignableFrom(type)) {
         return options.getLobCreator().createBlob(this.unwrapBytes(value));
      } else {
         throw this.unknownUnwrap(type);
      }
   }

   public Byte[] wrap(Object value, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (Byte[].class.isInstance(value)) {
         return (Byte[])value;
      } else if (byte[].class.isInstance(value)) {
         return this.wrapBytes((byte[])value);
      } else if (InputStream.class.isInstance(value)) {
         return this.wrapBytes(DataHelper.extractBytes((InputStream)value));
      } else if (!Blob.class.isInstance(value) && !DataHelper.isNClob(value.getClass())) {
         throw this.unknownWrap(value.getClass());
      } else {
         try {
            return this.wrapBytes(DataHelper.extractBytes(((Blob)value).getBinaryStream()));
         } catch (SQLException e) {
            throw new HibernateException("Unable to access lob stream", e);
         }
      }
   }

   private Byte[] wrapBytes(byte[] bytes) {
      if (bytes == null) {
         return null;
      } else {
         Byte[] result = new Byte[bytes.length];

         for(int i = 0; i < bytes.length; ++i) {
            result[i] = bytes[i];
         }

         return result;
      }
   }

   private byte[] unwrapBytes(Byte[] bytes) {
      if (bytes == null) {
         return null;
      } else {
         byte[] result = new byte[bytes.length];

         for(int i = 0; i < bytes.length; ++i) {
            result[i] = bytes[i];
         }

         return result;
      }
   }
}
