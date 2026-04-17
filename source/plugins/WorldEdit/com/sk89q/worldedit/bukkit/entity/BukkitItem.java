package com.sk89q.worldedit.bukkit.entity;

import com.sk89q.worldedit.Location;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import java.util.UUID;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class BukkitItem extends BukkitEntity {
   private final ItemStack stack;

   public BukkitItem(Location loc, ItemStack stack, UUID entityId) {
      super(loc, EntityType.DROPPED_ITEM, entityId);
      this.stack = stack;
   }

   public boolean spawn(Location weLoc) {
      org.bukkit.Location loc = BukkitUtil.toLocation(weLoc);
      return loc.getWorld().dropItem(loc, this.stack) != null;
   }
}
