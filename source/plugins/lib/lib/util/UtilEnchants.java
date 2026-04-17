package lib.util;

import lib.Enchants;
import lib.Lib;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

public class UtilEnchants {
   private static Enchants Enchants;

   public static void init(Lib lib) {
      Enchants = lib.getEnchants();
   }

   public static boolean addEnchant(CommandSender sender, String plugin, String type, ItemStack is, boolean replace, boolean force, boolean all) {
      return Enchants.addEnchant(sender, plugin, type, is, replace, force, all);
   }

   public static void reloadEnchants(String plugin, YamlConfiguration config) {
      Enchants.reloadEnchants(plugin, config);
   }
}
