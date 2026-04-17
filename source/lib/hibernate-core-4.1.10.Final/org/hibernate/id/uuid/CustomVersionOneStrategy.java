package org.hibernate.id.uuid;

import java.util.UUID;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.UUIDGenerationStrategy;
import org.hibernate.internal.util.BytesHelper;

public class CustomVersionOneStrategy implements UUIDGenerationStrategy {
   private final long mostSignificantBits;

   public int getGeneratedVersion() {
      return 1;
   }

   public CustomVersionOneStrategy() {
      byte[] hiBits = new byte[8];
      System.arraycopy(Helper.getAddressBytes(), 0, hiBits, 0, 4);
      System.arraycopy(Helper.getJvmIdentifierBytes(), 0, hiBits, 4, 4);
      hiBits[6] = (byte)(hiBits[6] & 15);
      hiBits[6] = (byte)(hiBits[6] | 16);
      this.mostSignificantBits = BytesHelper.asLong(hiBits);
   }

   public UUID generateUUID(SessionImplementor session) {
      long leastSignificantBits = generateLeastSignificantBits(System.currentTimeMillis());
      return new UUID(this.mostSignificantBits, leastSignificantBits);
   }

   public long getMostSignificantBits() {
      return this.mostSignificantBits;
   }

   public static long generateLeastSignificantBits(long seed) {
      byte[] loBits = new byte[8];
      short hiTime = (short)((int)(seed >>> 32));
      int loTime = (int)seed;
      System.arraycopy(BytesHelper.fromShort(hiTime), 0, loBits, 0, 2);
      System.arraycopy(BytesHelper.fromInt(loTime), 0, loBits, 2, 4);
      System.arraycopy(Helper.getCountBytes(), 0, loBits, 6, 2);
      loBits[0] = (byte)(loBits[0] & 63);
      loBits[0] = (byte)(loBits[0] | 128);
      return BytesHelper.asLong(loBits);
   }

   public static void main(String[] args) {
      CustomVersionOneStrategy strategy = new CustomVersionOneStrategy();

      for(int i = 0; i < 1000; ++i) {
         System.out.println("Generation # " + i + " ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
         byte[] loBits = new byte[8];
         long sysTime = System.currentTimeMillis();
         short hiTime = (short)((int)(System.currentTimeMillis() >>> 32));
         int loTime = (int)sysTime;
         System.arraycopy(BytesHelper.fromShort(hiTime), 0, loBits, 0, 2);
         System.arraycopy(BytesHelper.fromInt(loTime), 0, loBits, 2, 4);
         System.arraycopy(Helper.getCountBytes(), 0, loBits, 6, 2);
         System.out.println("    before bit setting ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
         System.out.println("       loBits[0] : " + BytesHelper.toBinaryString(loBits[0]));
         System.out.println("             lsb : " + BytesHelper.toBinaryString(BytesHelper.asLong(loBits)));
         System.out.println("    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
         loBits[0] = (byte)(loBits[0] & 63);
         loBits[0] = (byte)(loBits[0] | 128);
         System.out.println("    after bit setting ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
         System.out.println("       loBits[0] : " + BytesHelper.toBinaryString(loBits[0]));
         long leastSignificantBits = BytesHelper.asLong(loBits);
         System.out.println("             lsb : " + BytesHelper.toBinaryString(leastSignificantBits));
         System.out.println("    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
         UUID uuid = new UUID(strategy.mostSignificantBits, leastSignificantBits);
         System.out.println("  uuid : " + uuid.toString());
         System.out.println("  variant : " + uuid.variant());
         System.out.println("  version : " + uuid.version());
         if (uuid.variant() != 2) {
            throw new RuntimeException("bad variant");
         }

         System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
      }

   }
}
