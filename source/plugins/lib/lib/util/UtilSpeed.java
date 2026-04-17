package lib.util;

import lib.Lib;
import lib.Speed;
import org.bukkit.entity.Player;

public class UtilSpeed {
   private static Speed Speed;

   public static void init(Lib lib) {
      Speed = lib.getSpeed();
   }

   public static void register(String plugin, String type) {
      Speed.register(plugin, type);
   }

   public static boolean check(Player p, String plugin, String type, int limit) {
      return Speed.check(p, plugin, type, limit);
   }

   public static boolean check(Player p, String plugin, String type, int limit, boolean tip) {
      return Speed.check(p, plugin, type, limit, tip);
   }
}
