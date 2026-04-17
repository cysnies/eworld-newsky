package lib.util;

import lib.Bar;

public class UtilBar {
   public static void addMsg(String msg) {
      Bar.addMsg(msg);
   }

   public static void addMsgRandom(String msg) {
      Bar.addMsgRandom(msg);
   }

   public static void removeMsg(int index) {
      Bar.removeMsg(index);
   }

   public static void removeMsg(String msg) {
      Bar.removeMsg(msg);
   }

   public static void clearMsg() {
      Bar.clearMsg();
   }
}
