package org.hibernate.id;

import java.net.InetAddress;
import org.hibernate.internal.util.BytesHelper;

public abstract class AbstractUUIDGenerator implements IdentifierGenerator {
   private static final int IP;
   private static short counter;
   private static final int JVM;

   protected int getJVM() {
      return JVM;
   }

   protected short getCount() {
      synchronized(AbstractUUIDGenerator.class) {
         if (counter < 0) {
            counter = 0;
         }

         short var10000 = counter;
         counter = (short)(var10000 + 1);
         return var10000;
      }
   }

   protected int getIP() {
      return IP;
   }

   protected short getHiTime() {
      return (short)((int)(System.currentTimeMillis() >>> 32));
   }

   protected int getLoTime() {
      return (int)System.currentTimeMillis();
   }

   static {
      int ipadd;
      try {
         ipadd = BytesHelper.toInt(InetAddress.getLocalHost().getAddress());
      } catch (Exception var2) {
         ipadd = 0;
      }

      IP = ipadd;
      counter = 0;
      JVM = (int)(System.currentTimeMillis() >>> 8);
   }
}
