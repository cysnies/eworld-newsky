package com.goncalomb.bukkit.nbteditor.nbt;

import com.goncalomb.bukkit.EntityTypeMap;
import com.goncalomb.bukkit.reflect.NBTTagCompoundWrapper;
import org.bukkit.entity.EntityType;

public class SpawnerEntityNBT {
   private int _weight;
   private EntityNBT _entityNbt;

   public static boolean isValidCreature(String name) {
      return EntityType.fromName(name).isAlive();
   }

   public SpawnerEntityNBT(EntityType entityType) {
      this((EntityType)entityType, 1);
   }

   public SpawnerEntityNBT(EntityType entityType, int weight) {
      this._weight = weight;
      this._entityNbt = EntityNBT.fromEntityType(entityType);
   }

   public SpawnerEntityNBT(EntityNBT entityNbt) {
      this((EntityNBT)entityNbt, 1);
   }

   public SpawnerEntityNBT(EntityNBT entityNbt, int weight) {
      this._weight = weight;
      this._entityNbt = entityNbt;
   }

   public int getWeight() {
      return this._weight;
   }

   public EntityNBT getEntityNBT() {
      return this._entityNbt;
   }

   public EntityType getEntityType() {
      return this._entityNbt.getEntityType();
   }

   public SpawnerEntityNBT clone() {
      return new SpawnerEntityNBT(this._entityNbt.clone(), this._weight);
   }

   NBTTagCompoundWrapper buildTagCompound() {
      NBTTagCompoundWrapper data = new NBTTagCompoundWrapper();
      data.setInt("Weight", this._weight);
      data.setString("Type", EntityTypeMap.getName(this._entityNbt.getEntityType()));
      data.setCompound("Properties", this._entityNbt._data);
      return data;
   }
}
