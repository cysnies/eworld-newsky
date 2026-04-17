package org.hibernate.type.descriptor.sql;

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.hibernate.engine.jdbc.BinaryStream;
import org.hibernate.type.descriptor.ValueExtractor;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;

public abstract class BlobTypeDescriptor implements SqlTypeDescriptor {
   public static final BlobTypeDescriptor DEFAULT = new BlobTypeDescriptor() {
      public BasicBinder getBlobBinder(final JavaTypeDescriptor javaTypeDescriptor) {
         return new BasicBinder(javaTypeDescriptor, this) {
            protected void doBind(PreparedStatement st, Object value, int index, WrapperOptions options) throws SQLException {
               BlobTypeDescriptor descriptor = BlobTypeDescriptor.BLOB_BINDING;
               if (byte[].class.isInstance(value)) {
                  descriptor = BlobTypeDescriptor.PRIMITIVE_ARRAY_BINDING;
               } else if (options.useStreamForLobBinding()) {
                  descriptor = BlobTypeDescriptor.STREAM_BINDING;
               }

               descriptor.getBlobBinder(javaTypeDescriptor).doBind(st, value, index, options);
            }
         };
      }
   };
   public static final BlobTypeDescriptor PRIMITIVE_ARRAY_BINDING = new BlobTypeDescriptor() {
      public BasicBinder getBlobBinder(final JavaTypeDescriptor javaTypeDescriptor) {
         return new BasicBinder(javaTypeDescriptor, this) {
            public void doBind(PreparedStatement st, Object value, int index, WrapperOptions options) throws SQLException {
               st.setBytes(index, (byte[])javaTypeDescriptor.unwrap(value, byte[].class, options));
            }
         };
      }
   };
   public static final BlobTypeDescriptor BLOB_BINDING = new BlobTypeDescriptor() {
      public BasicBinder getBlobBinder(final JavaTypeDescriptor javaTypeDescriptor) {
         return new BasicBinder(javaTypeDescriptor, this) {
            protected void doBind(PreparedStatement st, Object value, int index, WrapperOptions options) throws SQLException {
               st.setBlob(index, (Blob)javaTypeDescriptor.unwrap(value, Blob.class, options));
            }
         };
      }
   };
   public static final BlobTypeDescriptor STREAM_BINDING = new BlobTypeDescriptor() {
      public BasicBinder getBlobBinder(final JavaTypeDescriptor javaTypeDescriptor) {
         return new BasicBinder(javaTypeDescriptor, this) {
            protected void doBind(PreparedStatement st, Object value, int index, WrapperOptions options) throws SQLException {
               BinaryStream binaryStream = (BinaryStream)javaTypeDescriptor.unwrap(value, BinaryStream.class, options);
               st.setBinaryStream(index, binaryStream.getInputStream(), binaryStream.getLength());
            }
         };
      }
   };

   private BlobTypeDescriptor() {
   }

   public int getSqlType() {
      return 2004;
   }

   public boolean canBeRemapped() {
      return true;
   }

   public ValueExtractor getExtractor(final JavaTypeDescriptor javaTypeDescriptor) {
      return new BasicExtractor(javaTypeDescriptor, this) {
         protected Object doExtract(ResultSet rs, String name, WrapperOptions options) throws SQLException {
            return javaTypeDescriptor.wrap(rs.getBlob(name), options);
         }
      };
   }

   protected abstract BasicBinder getBlobBinder(JavaTypeDescriptor var1);

   public BasicBinder getBinder(JavaTypeDescriptor javaTypeDescriptor) {
      return this.getBlobBinder(javaTypeDescriptor);
   }
}
