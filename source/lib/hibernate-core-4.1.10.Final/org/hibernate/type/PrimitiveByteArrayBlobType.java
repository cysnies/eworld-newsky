package org.hibernate.type;

/** @deprecated */
@Deprecated
public class PrimitiveByteArrayBlobType extends ByteArrayBlobType {
   public Class getReturnedClass() {
      return byte[].class;
   }

   protected Object wrap(byte[] bytes) {
      return bytes;
   }

   protected byte[] unWrap(Object bytes) {
      return (byte[])bytes;
   }
}
