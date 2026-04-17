package lib.util;

import lib.Costs;
import lib.Lib;
import lib.types.InvalidTypeException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class UtilCosts {
   private static Costs costs;

   public static void init(Lib lib) {
      costs = lib.getCosts();
   }

   public static void reloadCosts(String plugin, YamlConfiguration config) {
      costs.reloadCosts(plugin, config);
   }

   public static boolean cost(Player p, String plugin, String type, boolean force) throws InvalidTypeException {
      return costs.cost(p, plugin, type, force);
   }

   public static boolean cost(Player p, String plugin, String type, boolean force, boolean tip) throws InvalidTypeException {
      return costs.cost(p, plugin, type, force);
   }
}
