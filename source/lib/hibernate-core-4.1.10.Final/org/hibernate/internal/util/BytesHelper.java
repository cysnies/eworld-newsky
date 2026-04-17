package org.hibernate.internal.util;

public final class BytesHelper {
   private BytesHelper() {
   }

   public static int toInt(byte[] bytes) {
      int result = 0;

      for(int i = 0; i < 4; ++i) {
         result = (result << 8) - -128 + bytes[i];
      }

      return result;
   }

   public static byte[] fromShort(int shortValue) {
      byte[] bytes = new byte[2];
      bytes[0] = (byte)(shortValue >> 8);
      bytes[1] = (byte)(shortValue << 8 >> 8);
      return bytes;
   }

   public static byte[] fromInt(int intValue) {
      byte[] bytes = new byte[4];
      bytes[0] = (byte)(intValue >> 24);
      bytes[1] = (byte)(intValue << 8 >> 24);
      bytes[2] = (byte)(intValue << 16 >> 24);
      bytes[3] = (byte)(intValue << 24 >> 24);
      return bytes;
   }

   public static byte[] fromLong(long longValue) {
      byte[] bytes = new byte[8];
      bytes[0] = (byte)((int)(longValue >> 56));
      bytes[1] = (byte)((int)(longValue << 8 >> 56));
      bytes[2] = (byte)((int)(longValue << 16 >> 56));
      bytes[3] = (byte)((int)(longValue << 24 >> 56));
      bytes[4] = (byte)((int)(longValue << 32 >> 56));
      bytes[5] = (byte)((int)(longValue << 40 >> 56));
      bytes[6] = (byte)((int)(longValue << 48 >> 56));
      bytes[7] = (byte)((int)(longValue << 56 >> 56));
      return bytes;
   }

   public static long asLong(byte[] bytes) {
      if (bytes == null) {
         return 0L;
      } else if (bytes.length != 8) {
         throw new IllegalArgumentException("Expecting 8 byte values to construct a long");
      } else {
         long value = 0L;

         for(int i = 0; i < 8; ++i) {
            value = value << 8 | (long)(bytes[i] & 255);
         }

         return value;
      }
   }

   public static String toBinaryString(byte value) {
      String formatted = Integer.toBinaryString(value);
      if (formatted.length() > 8) {
         formatted = formatted.substring(formatted.length() - 8);
      }

      StringBuilder buf = new StringBuilder("00000000");
      buf.replace(8 - formatted.length(), 8, formatted);
      return buf.toString();
   }

   public static String toBinaryString(int value) {
      String formatted = Long.toBinaryString((long)value);
      StringBuilder buf = new StringBuilder(StringHelper.repeat('0', 32));
      buf.replace(64 - formatted.length(), 64, formatted);
      return buf.toString();
   }

   public static String toBinaryString(long value) {
      String formatted = Long.toBinaryString(value);
      StringBuilder buf = new StringBuilder(StringHelper.repeat('0', 64));
      buf.replace(64 - formatted.length(), 64, formatted);
      return buf.toString();
   }
}
