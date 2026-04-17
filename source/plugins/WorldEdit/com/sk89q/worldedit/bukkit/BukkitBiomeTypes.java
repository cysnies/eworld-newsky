package com.sk89q.worldedit.bukkit;

import com.sk89q.worldedit.BiomeType;
import com.sk89q.worldedit.BiomeTypes;
import com.sk89q.worldedit.UnknownBiomeTypeException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class BukkitBiomeTypes implements BiomeTypes {
   public boolean has(String name) {
      try {
         BukkitBiomeType.valueOf(name.toUpperCase(Locale.ENGLISH));
         return true;
      } catch (IllegalArgumentException var3) {
         return false;
      }
   }

   public BiomeType get(String name) throws UnknownBiomeTypeException {
      try {
         return BukkitBiomeType.valueOf(name.toUpperCase(Locale.ENGLISH));
      } catch (IllegalArgumentException var3) {
         throw new UnknownBiomeTypeException(name);
      }
   }

   public List all() {
      return Arrays.asList(BukkitBiomeType.values());
   }
}
