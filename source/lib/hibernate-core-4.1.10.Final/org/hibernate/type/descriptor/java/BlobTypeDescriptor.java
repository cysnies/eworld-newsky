package org.hibernate.type.descriptor.java;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Comparator;
import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.BinaryStream;
import org.hibernate.engine.jdbc.BlobImplementer;
import org.hibernate.engine.jdbc.BlobProxy;
import org.hibernate.engine.jdbc.WrappedBlob;
import org.hibernate.engine.jdbc.internal.BinaryStreamImpl;
import org.hibernate.type.descriptor.WrapperOptions;

public class BlobTypeDescriptor extends AbstractTypeDescriptor {
   public static final BlobTypeDescriptor INSTANCE = new BlobTypeDescriptor();

   public BlobTypeDescriptor() {
      super(Blob.class, BlobTypeDescriptor.BlobMutabilityPlan.INSTANCE);
   }

   public String toString(Blob value) {
      byte[] bytes;
      try {
         bytes = DataHelper.extractBytes(value.getBinaryStream());
      } catch (SQLException e) {
         throw new HibernateException("Unable to access blob stream", e);
      }

      return PrimitiveByteArrayTypeDescriptor.INSTANCE.toString(bytes);
   }

   public Blob fromString(String string) {
      return BlobProxy.generateProxy(PrimitiveByteArrayTypeDescriptor.INSTANCE.fromString(string));
   }

   public Comparator getComparator() {
      return IncomparableComparator.INSTANCE;
   }

   public int extractHashCode(Blob value) {
      return System.identityHashCode(value);
   }

   public boolean areEqual(Blob one, Blob another) {
      return one == another;
   }

   public Object unwrap(Blob value, Class type, WrapperOptions options) {
      if (value == null) {
         return null;
      } else {
         try {
            if (BinaryStream.class.isAssignableFrom(type)) {
               if (BlobImplementer.class.isInstance(value)) {
                  return ((BlobImplementer)value).getUnderlyingStream();
               }

               return new BinaryStreamImpl(DataHelper.extractBytes(value.getBinaryStream()));
            }

            if (byte[].class.isAssignableFrom(type)) {
               if (BlobImplementer.class.isInstance(value)) {
                  return ((BlobImplementer)value).getUnderlyingStream().getBytes();
               }

               return DataHelper.extractBytes(value.getBinaryStream());
            }

            if (Blob.class.isAssignableFrom(type)) {
               Blob blob = WrappedBlob.class.isInstance(value) ? ((WrappedBlob)value).getWrappedBlob() : value;
               return blob;
            }
         } catch (SQLException e) {
            throw new HibernateException("Unable to access blob stream", e);
         }

         throw this.unknownUnwrap(type);
      }
   }

   public Blob wrap(Object value, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (Blob.class.isAssignableFrom(value.getClass())) {
         return options.getLobCreator().wrap((Blob)value);
      } else if (byte[].class.isAssignableFrom(value.getClass())) {
         return options.getLobCreator().createBlob((byte[])value);
      } else if (InputStream.class.isAssignableFrom(value.getClass())) {
         InputStream inputStream = (InputStream)value;

         try {
            return options.getLobCreator().createBlob(inputStream, (long)inputStream.available());
         } catch (IOException var5) {
            throw this.unknownWrap(value.getClass());
         }
      } else {
         throw this.unknownWrap(value.getClass());
      }
   }

   public static class BlobMutabilityPlan implements MutabilityPlan {
      public static final BlobMutabilityPlan INSTANCE = new BlobMutabilityPlan();

      public boolean isMutable() {
         return false;
      }

      public Blob deepCopy(Blob value) {
         return value;
      }

      public Serializable disassemble(Blob value) {
         throw new UnsupportedOperationException("Blobs are not cacheable");
      }

      public Blob assemble(Serializable cached) {
         throw new UnsupportedOperationException("Blobs are not cacheable");
      }
   }
}
