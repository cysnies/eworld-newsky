package lib.util;

import lib.Lib;
import lib.Per;
import org.bukkit.entity.Player;

public class UtilPer {
   private static String Pn;
   private static Per Per;

   public static void init(Lib lib) {
      Pn = lib.getPn();
      Per = lib.getPer();
   }

   public static boolean checkPer(Player p, String per) {
      if (!Per.has(p, per)) {
         try {
            p.sendMessage(UtilFormat.format(Pn, "noPer", per));
         } catch (Exception var3) {
         }

         return false;
      } else {
         return true;
      }
   }

   public static boolean add(Player p, String per) {
      return Per.add(p, per);
   }

   public static boolean add(String name, String per) {
      return Per.add(name, per);
   }

   public static boolean remove(Player p, String per) {
      return Per.remove(p, per);
   }

   public static boolean inGroup(Player p, String group) {
      return Per.inGroup(p, group);
   }

   public static boolean addGroup(Player p, String group) {
      return Per.addGroup(p, group);
   }

   public static boolean hasPer(Player p, String per) {
      return Per.has(p, per);
   }

   public static boolean hasPer(String name, String per) {
      return Per.has(name, per);
   }
}
