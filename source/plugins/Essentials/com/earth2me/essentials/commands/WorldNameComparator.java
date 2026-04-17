package com.earth2me.essentials.commands;

import java.util.Comparator;
import org.bukkit.World;

class WorldNameComparator implements Comparator {
   public int compare(World a, World b) {
      return a.getName().compareTo(b.getName());
   }
}
