package org.hibernate.type.descriptor.java;

import java.io.Serializable;
import java.util.UUID;
import org.hibernate.internal.util.BytesHelper;
import org.hibernate.type.descriptor.WrapperOptions;

public class UUIDTypeDescriptor extends AbstractTypeDescriptor {
   public static final UUIDTypeDescriptor INSTANCE = new UUIDTypeDescriptor();

   public UUIDTypeDescriptor() {
      super(UUID.class);
   }

   public String toString(UUID value) {
      return UUIDTypeDescriptor.ToStringTransformer.INSTANCE.transform(value);
   }

   public UUID fromString(String string) {
      return UUIDTypeDescriptor.ToStringTransformer.INSTANCE.parse(string);
   }

   public Object unwrap(UUID value, Class type, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (UUID.class.isAssignableFrom(type)) {
         return UUIDTypeDescriptor.PassThroughTransformer.INSTANCE.transform(value);
      } else if (String.class.isAssignableFrom(type)) {
         return UUIDTypeDescriptor.ToStringTransformer.INSTANCE.transform(value);
      } else if (byte[].class.isAssignableFrom(type)) {
         return UUIDTypeDescriptor.ToBytesTransformer.INSTANCE.transform(value);
      } else {
         throw this.unknownUnwrap(type);
      }
   }

   public UUID wrap(Object value, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (UUID.class.isInstance(value)) {
         return UUIDTypeDescriptor.PassThroughTransformer.INSTANCE.parse(value);
      } else if (String.class.isInstance(value)) {
         return UUIDTypeDescriptor.ToStringTransformer.INSTANCE.parse(value);
      } else if (byte[].class.isInstance(value)) {
         return UUIDTypeDescriptor.ToBytesTransformer.INSTANCE.parse(value);
      } else {
         throw this.unknownWrap(value.getClass());
      }
   }

   public static class PassThroughTransformer implements ValueTransformer {
      public static final PassThroughTransformer INSTANCE = new PassThroughTransformer();

      public UUID transform(UUID uuid) {
         return uuid;
      }

      public UUID parse(Object value) {
         return (UUID)value;
      }
   }

   public static class ToStringTransformer implements ValueTransformer {
      public static final ToStringTransformer INSTANCE = new ToStringTransformer();

      public String transform(UUID uuid) {
         return uuid.toString();
      }

      public UUID parse(Object value) {
         return UUID.fromString((String)value);
      }
   }

   public static class ToBytesTransformer implements ValueTransformer {
      public static final ToBytesTransformer INSTANCE = new ToBytesTransformer();

      public byte[] transform(UUID uuid) {
         byte[] bytes = new byte[16];
         System.arraycopy(BytesHelper.fromLong(uuid.getMostSignificantBits()), 0, bytes, 0, 8);
         System.arraycopy(BytesHelper.fromLong(uuid.getLeastSignificantBits()), 0, bytes, 8, 8);
         return bytes;
      }

      public UUID parse(Object value) {
         byte[] msb = new byte[8];
         byte[] lsb = new byte[8];
         System.arraycopy(value, 0, msb, 0, 8);
         System.arraycopy(value, 8, lsb, 0, 8);
         return new UUID(BytesHelper.asLong(msb), BytesHelper.asLong(lsb));
      }
   }

   public interface ValueTransformer {
      Serializable transform(UUID var1);

      UUID parse(Object var1);
   }
}
