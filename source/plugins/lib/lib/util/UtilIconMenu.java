package lib.util;

import java.util.List;
import lib.IconMenu;
import lib.Lib;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class UtilIconMenu {
   private static IconMenu IconMenu;

   public static void init(Lib lib) {
      IconMenu = lib.getIcon();
   }

   public static IconMenu.Info register(String name, int size, boolean emptyDestroy, IconMenu.OptionClickEventHandler handler) {
      return IconMenu.register(name, size, emptyDestroy, handler);
   }

   public static void unregister(IconMenu.Info info) {
      IconMenu.unregister(info);
   }

   public static void open(Player p, IconMenu.Info info, String title, Inventory handle) {
      IconMenu.open(p, info, title, handle);
   }

   public static IconMenu.Info getInfo(int id) {
      return IconMenu.getInfo(id);
   }

   public static void openSession(Player p, String name, List lore, IconMenu.Session session, int timeLimit) {
      IconMenu.openSession(p, name, lore, session, timeLimit);
   }
}
