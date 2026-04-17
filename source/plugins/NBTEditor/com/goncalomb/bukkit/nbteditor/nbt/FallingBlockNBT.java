package com.goncalomb.bukkit.nbteditor.nbt;

import com.goncalomb.bukkit.nbteditor.nbt.variable.BlockVariable;
import com.goncalomb.bukkit.nbteditor.nbt.variable.BooleanVariable;
import com.goncalomb.bukkit.nbteditor.nbt.variable.ByteVariable;
import com.goncalomb.bukkit.nbteditor.nbt.variable.FloatVariable;
import com.goncalomb.bukkit.nbteditor.nbt.variable.IntegerVariable;
import com.goncalomb.bukkit.nbteditor.nbt.variable.NBTGenericVariableContainer;
import com.goncalomb.bukkit.reflect.NBTTagCompoundWrapper;
import com.goncalomb.bukkit.reflect.NBTUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

public class FallingBlockNBT extends EntityNBT {
   static {
      NBTGenericVariableContainer variables = new NBTGenericVariableContainer("FallingBlock");
      variables.add("block", new BlockVariable("TileID", "Data"));
      variables.add("time", new ByteVariable("Time", (byte)0));
      variables.add("drop-item", new BooleanVariable("DropItem"));
      variables.add("hurt-entities", new BooleanVariable("HurtEntities"));
      variables.add("fall-hurt-amount", new FloatVariable("FallHurtAmount", 0.0F));
      variables.add("fall-hurt-max", new IntegerVariable("FallHurtMax", 0));
      EntityNBTVariableManager.registerVariables(FallingBlockNBT.class, variables);
   }

   public void copyFromTileEntity(Block block) {
      this._data.setInt("TileID", block.getTypeId());
      this._data.setByte("Data", block.getData());
      NBTTagCompoundWrapper tileEntityData = NBTUtils.getTileEntityNBTTagCompound(block);
      if (tileEntityData != null) {
         this._data.setCompound("TileEntityData", tileEntityData);
      } else {
         this._data.remove("TileEntityData");
      }

   }

   public boolean hasTileEntityData() {
      return this._data.hasKey("TileEntityData");
   }

   public Entity spawn(Location location) {
      int blockId = this._data.hasKey("TileID") ? this._data.getInt("TileID") : Material.SAND.getId();
      byte blockData = this._data.hasKey("Data") ? this._data.getByte("Data") : 0;
      Entity entity = location.getWorld().spawnFallingBlock(location, blockId, blockData);
      NBTUtils.setEntityNBTTagCompound(entity, this._data);
      return entity;
   }
}
