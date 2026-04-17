package lib.util;

import java.util.List;
import lib.Lib;
import lib.Potions;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;

public class UtilPotions {
   private static Potions Potions;

   public static void init(Lib lib) {
      Potions = lib.getPotions();
   }

   public static boolean reloadPotions(String plugin, YamlConfiguration config) {
      return Potions.reloadPotions(plugin, config);
   }

   public static List addPotions(CommandSender sender, String plugin, String type, LivingEntity le, boolean random, boolean force, boolean all) {
      return Potions.addPotions(sender, plugin, type, le, random, force, all);
   }

   public static boolean isExsit(String plugin, String type) {
      return Potions.isExsit(plugin, type);
   }
}
