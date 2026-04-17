package com.sk89q.worldedit.bukkit.entity;

import com.sk89q.worldedit.Location;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import java.util.UUID;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;

public class BukkitExpOrb extends BukkitEntity {
   private final int amount;

   public BukkitExpOrb(Location loc, UUID entityId, int amount) {
      super(loc, EntityType.EXPERIENCE_ORB, entityId);
      this.amount = amount;
   }

   public boolean spawn(Location weLoc) {
      org.bukkit.Location loc = BukkitUtil.toLocation(weLoc);
      ExperienceOrb orb = (ExperienceOrb)loc.getWorld().spawn(loc, ExperienceOrb.class);
      if (orb != null) {
         orb.setExperience(this.amount);
         return true;
      } else {
         return false;
      }
   }
}
