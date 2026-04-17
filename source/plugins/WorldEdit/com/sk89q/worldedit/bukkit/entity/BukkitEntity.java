package com.sk89q.worldedit.bukkit.entity;

import com.sk89q.worldedit.LocalEntity;
import com.sk89q.worldedit.Location;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import java.util.UUID;
import org.bukkit.entity.EntityType;

public class BukkitEntity extends LocalEntity {
   private final EntityType type;
   private final UUID entityId;

   public BukkitEntity(Location loc, EntityType type, UUID entityId) {
      super(loc);
      this.type = type;
      this.entityId = entityId;
   }

   public UUID getEntityId() {
      return this.entityId;
   }

   public boolean spawn(Location weLoc) {
      org.bukkit.Location loc = BukkitUtil.toLocation(weLoc);
      return loc.getWorld().spawn(loc, this.type.getEntityClass()) != null;
   }
}
