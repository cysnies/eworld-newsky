package lib.util;

import java.util.HashMap;
import lib.Lib;
import lib.Rewards;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

public class UtilRewards {
   private static Rewards Rewards;

   public static void init(Lib lib) {
      Rewards = lib.getRewards();
   }

   public static void reloadRewards(String plugin, YamlConfiguration config) {
      Rewards.reloadRewards(plugin, config);
   }

   public static boolean addRewards(CommandSender sender, String tar, String plugin, String type) {
      return Rewards.addRewards(sender, tar, plugin, type);
   }

   public static boolean addRewards(String plugin, String type, String tar, int money, int exp, int level, String tip, HashMap itemsHash, boolean force) {
      return Rewards.addRewards(plugin, type, tar, money, exp, level, tip, itemsHash, force);
   }

   public static Rewards.RewardsInfo getRewardsInfo(String plugin, String type) {
      return Rewards.getRewardsInfo(plugin, type);
   }

   public static boolean showList(CommandSender sender, String tar, int page) {
      return Rewards.showList(sender, tar, page);
   }
}
