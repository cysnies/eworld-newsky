package lib.util;

import lib.Lib;
import lib.Msg;
import org.bukkit.entity.Player;

public class UtilScoreboard {
   private static Msg Msg;

   public static void init(Lib lib) {
      Msg = lib.getMsg();
   }

   public static void setPrefix(Player p, String prefix) {
      Msg.setPrefix(p, prefix);
   }

   public static void setSuffix(Player p, String suffix) {
      Msg.setSuffix(p, suffix);
   }

   public static void show(String show, String from, int last) {
      Msg.show(show, from, last);
   }

   public static void setDisplaySideBar(Player p, boolean display) {
      Msg.setDisplaySideBar(p, display);
   }

   public static boolean isDisplaySideBar(Player p) {
      return Msg.isDisplaySideBar(p);
   }

   public static String getPrefix(String name) {
      return Msg.getPrefix(name);
   }

   public static String getSuffix(String name) {
      return Msg.getSuffix(name);
   }
}
