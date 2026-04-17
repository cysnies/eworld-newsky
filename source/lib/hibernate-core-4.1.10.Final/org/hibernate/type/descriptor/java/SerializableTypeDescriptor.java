package org.hibernate.type.descriptor.java;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.Blob;
import java.sql.SQLException;
import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.BinaryStream;
import org.hibernate.engine.jdbc.internal.BinaryStreamImpl;
import org.hibernate.internal.util.SerializationHelper;
import org.hibernate.type.descriptor.WrapperOptions;

public class SerializableTypeDescriptor extends AbstractTypeDescriptor {
   public SerializableTypeDescriptor(Class type) {
      super(type, Serializable.class.equals(type) ? SerializableTypeDescriptor.SerializableMutabilityPlan.INSTANCE : new SerializableMutabilityPlan(type));
   }

   public String toString(Serializable value) {
      return PrimitiveByteArrayTypeDescriptor.INSTANCE.toString(this.toBytes(value));
   }

   public Serializable fromString(String string) {
      return this.fromBytes(PrimitiveByteArrayTypeDescriptor.INSTANCE.fromString(string));
   }

   public boolean areEqual(Serializable one, Serializable another) {
      if (one == another) {
         return true;
      } else if (one != null && another != null) {
         return one.equals(another) || PrimitiveByteArrayTypeDescriptor.INSTANCE.areEqual(this.toBytes(one), this.toBytes(another));
      } else {
         return false;
      }
   }

   public int extractHashCode(Serializable value) {
      return PrimitiveByteArrayTypeDescriptor.INSTANCE.extractHashCode(this.toBytes(value));
   }

   public Object unwrap(Serializable value, Class type, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (byte[].class.isAssignableFrom(type)) {
         return this.toBytes(value);
      } else if (InputStream.class.isAssignableFrom(type)) {
         return new ByteArrayInputStream(this.toBytes(value));
      } else if (BinaryStream.class.isAssignableFrom(type)) {
         return new BinaryStreamImpl(this.toBytes(value));
      } else if (Blob.class.isAssignableFrom(type)) {
         return options.getLobCreator().createBlob(this.toBytes(value));
      } else {
         throw this.unknownUnwrap(type);
      }
   }

   public Serializable wrap(Object value, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (byte[].class.isInstance(value)) {
         return this.fromBytes((byte[])value);
      } else if (InputStream.class.isInstance(value)) {
         return this.fromBytes(DataHelper.extractBytes((InputStream)value));
      } else if (Blob.class.isInstance(value)) {
         try {
            return this.fromBytes(DataHelper.extractBytes(((Blob)value).getBinaryStream()));
         } catch (SQLException e) {
            throw new HibernateException(e);
         }
      } else {
         throw this.unknownWrap(value.getClass());
      }
   }

   protected byte[] toBytes(Serializable value) {
      return SerializationHelper.serialize(value);
   }

   protected Serializable fromBytes(byte[] bytes) {
      return (Serializable)SerializationHelper.deserialize(bytes, this.getJavaTypeClass().getClassLoader());
   }

   public static class SerializableMutabilityPlan extends MutableMutabilityPlan {
      private final Class type;
      public static final SerializableMutabilityPlan INSTANCE = new SerializableMutabilityPlan(Serializable.class);

      public SerializableMutabilityPlan(Class type) {
         this.type = type;
      }

      public Serializable deepCopyNotNull(Serializable value) {
         return (Serializable)SerializationHelper.clone(value);
      }
   }
}
