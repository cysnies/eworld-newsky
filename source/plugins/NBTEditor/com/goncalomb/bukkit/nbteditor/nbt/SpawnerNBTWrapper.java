package com.goncalomb.bukkit.nbteditor.nbt;

import com.goncalomb.bukkit.EntityTypeMap;
import com.goncalomb.bukkit.nbteditor.nbt.variable.NBTGenericVariableContainer;
import com.goncalomb.bukkit.nbteditor.nbt.variable.NBTVariable;
import com.goncalomb.bukkit.nbteditor.nbt.variable.NBTVariableContainer;
import com.goncalomb.bukkit.nbteditor.nbt.variable.ShortVariable;
import com.goncalomb.bukkit.reflect.NBTTagCompoundWrapper;
import com.goncalomb.bukkit.reflect.NBTTagListWrapper;
import com.goncalomb.bukkit.reflect.NBTUtils;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;

public final class SpawnerNBTWrapper {
   private static NBTGenericVariableContainer _variables = new NBTGenericVariableContainer("Spawner");
   private Block _spawnerBlock;
   private NBTTagCompoundWrapper _data;
   private List _entities;

   static {
      _variables.add("count", new ShortVariable("SpawnCount", (short)0));
      _variables.add("range", new ShortVariable("SpawnRange", (short)0));
      _variables.add("delay", new ShortVariable("Delay", (short)0));
      _variables.add("min-delay", new ShortVariable("MinSpawnDelay", (short)0));
      _variables.add("max-delay", new ShortVariable("MaxSpawnDelay", (short)0));
      _variables.add("max-entities", new ShortVariable("MaxNearbyEntities", (short)0));
      _variables.add("player-range", new ShortVariable("RequiredPlayerRange", (short)0));
   }

   public SpawnerNBTWrapper(Block block) {
      this._spawnerBlock = block;
      this._data = NBTUtils.getTileEntityNBTTagCompound(block);
      if (this._data.hasKey("SpawnPotentials")) {
         NBTTagListWrapper spawnPotentials = this._data.getList("SpawnPotentials");
         int l = spawnPotentials.size();
         this._entities = new ArrayList(l);

         for(int i = 0; i < l; ++i) {
            NBTTagCompoundWrapper potential = (NBTTagCompoundWrapper)spawnPotentials.get(i);
            EntityType entityType = EntityTypeMap.getByName(potential.getString("Type"));
            if (entityType != null) {
               EntityNBT entityNbt;
               if (potential.hasKey("Properties")) {
                  entityNbt = EntityNBT.fromEntityType(entityType, potential.getCompound("Properties"));
               } else {
                  entityNbt = EntityNBT.fromEntityType(entityType);
               }

               this._entities.add(new SpawnerEntityNBT(entityNbt, potential.getInt("Weight")));
            }
         }

         this._data.remove("SpawnPotentials");
      } else {
         this._entities = new ArrayList();
      }

   }

   public void addEntity(SpawnerEntityNBT spawnerEntityNbt) {
      this._data.setString("EntityId", EntityTypeMap.getName(spawnerEntityNbt.getEntityType()));
      this._data.setCompound("SpawnData", spawnerEntityNbt.getEntityNBT()._data.clone());
      this._entities.add(spawnerEntityNbt);
   }

   public void clearEntities() {
      this._entities.clear();
   }

   public void cloneFrom(SpawnerNBTWrapper other) {
      NBTTagCompoundWrapper clone = other._data.clone();
      clone.remove("id");
      clone.remove("x");
      clone.remove("y");
      clone.remove("z");
      this._data.merge(clone);
      this._entities = new ArrayList(other._entities.size());

      for(SpawnerEntityNBT spawnerEntityNBT : other._entities) {
         this._entities.add(spawnerEntityNBT.clone());
      }

   }

   public List getEntities() {
      return this._entities;
   }

   public void removeEntity(int index) {
      this._entities.remove(index);
   }

   public EntityType getCurrentEntity() {
      return EntityTypeMap.getByName(this._data.getString("EntityId"));
   }

   public Location getLocation() {
      return this._spawnerBlock.getLocation();
   }

   public NBTVariableContainer getVariables() {
      return _variables.boundToData(this._data);
   }

   public NBTVariable getVariable(String name) {
      return _variables.getVariable(name, this._data);
   }

   public void save() {
      if (this._entities.size() > 0) {
         NBTTagListWrapper spawnPotentials = new NBTTagListWrapper();

         for(SpawnerEntityNBT spawnerEntityNbt : this._entities) {
            spawnPotentials.add(spawnerEntityNbt.buildTagCompound());
         }

         this._data.set("SpawnPotentials", spawnPotentials);
      } else {
         this._data.setString("EntityId", "Pig");
         this._data.remove("SpawnData");
         this._data.remove("SpawnPotentials");
      }

      NBTUtils.setTileEntityNBTTagCompound(this._spawnerBlock, this._data);
   }
}
