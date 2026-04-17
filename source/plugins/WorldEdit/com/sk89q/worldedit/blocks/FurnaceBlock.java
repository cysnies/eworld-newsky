package com.sk89q.worldedit.blocks;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.NBTUtils;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.data.DataException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FurnaceBlock extends ContainerBlock {
   private short burnTime;
   private short cookTime;

   public FurnaceBlock(int type) {
      super(type, 2);
   }

   public FurnaceBlock(int type, int data) {
      super(type, data, 2);
   }

   public FurnaceBlock(int type, int data, BaseItemStack[] items) {
      super(type, data, 2);
      this.setItems(items);
   }

   public short getBurnTime() {
      return this.burnTime;
   }

   public void setBurnTime(short burnTime) {
      this.burnTime = burnTime;
   }

   public short getCookTime() {
      return this.cookTime;
   }

   public void setCookTime(short cookTime) {
      this.cookTime = cookTime;
   }

   public String getNbtId() {
      return "Furnace";
   }

   public CompoundTag getNbtData() {
      Map<String, Tag> values = new HashMap();
      values.put("Items", new ListTag("Items", CompoundTag.class, this.serializeInventory(this.getItems())));
      values.put("BurnTime", new ShortTag("BurnTime", this.burnTime));
      values.put("CookTime", new ShortTag("CookTime", this.cookTime));
      return new CompoundTag(this.getNbtId(), values);
   }

   public void setNbtData(CompoundTag rootTag) throws DataException {
      if (rootTag != null) {
         Map<String, Tag> values = rootTag.getValue();
         Tag t = (Tag)values.get("id");
         if (t instanceof StringTag && ((StringTag)t).getValue().equals("Furnace")) {
            ListTag items = (ListTag)NBTUtils.getChildTag(values, "Items", ListTag.class);
            List<CompoundTag> compound = new ArrayList();

            for(Tag tag : items.getValue()) {
               if (!(tag instanceof CompoundTag)) {
                  throw new DataException("CompoundTag expected as child tag of Furnace Items");
               }

               compound.add((CompoundTag)tag);
            }

            this.setItems(this.deserializeInventory(compound));
            t = (Tag)values.get("BurnTime");
            if (t instanceof ShortTag) {
               this.burnTime = ((ShortTag)t).getValue();
            }

            t = (Tag)values.get("CookTime");
            if (t instanceof ShortTag) {
               this.cookTime = ((ShortTag)t).getValue();
            }

         } else {
            throw new DataException("'Furnace' tile entity expected");
         }
      }
   }
}
