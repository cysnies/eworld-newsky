package com.earth2me.essentials.craftbukkit;

import org.bukkit.entity.Player;

public class SetExpFix {
   public static void setTotalExperience(Player player, int exp) {
      if (exp < 0) {
         throw new IllegalArgumentException("Experience is negative!");
      } else {
         player.setExp(0.0F);
         player.setLevel(0);
         player.setTotalExperience(0);
         int amount = exp;

         while(amount > 0) {
            int expToLevel = getExpAtLevel(player);
            amount -= expToLevel;
            if (amount >= 0) {
               player.giveExp(expToLevel);
            } else {
               amount += expToLevel;
               player.giveExp(amount);
               amount = 0;
            }
         }

      }
   }

   private static int getExpAtLevel(Player player) {
      return getExpAtLevel(player.getLevel());
   }

   public static int getExpAtLevel(int level) {
      if (level > 29) {
         return 62 + (level - 30) * 7;
      } else {
         return level > 15 ? 17 + (level - 15) * 3 : 17;
      }
   }

   public static int getExpToLevel(int level) {
      int currentLevel = 0;

      int exp;
      for(exp = 0; currentLevel < level; ++currentLevel) {
         exp += getExpAtLevel(currentLevel);
      }

      if (exp < 0) {
         exp = Integer.MAX_VALUE;
      }

      return exp;
   }

   public static int getTotalExperience(Player player) {
      int exp = Math.round((float)getExpAtLevel(player) * player.getExp());

      for(int currentLevel = player.getLevel(); currentLevel > 0; exp += getExpAtLevel(currentLevel)) {
         --currentLevel;
      }

      if (exp < 0) {
         exp = Integer.MAX_VALUE;
      }

      return exp;
   }

   public static int getExpUntilNextLevel(Player player) {
      int exp = Math.round((float)getExpAtLevel(player) * player.getExp());
      int nextLevel = player.getLevel();
      return getExpAtLevel(nextLevel) - exp;
   }
}
