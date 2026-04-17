package com.sk89q.worldedit.blocks;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.NBTUtils;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.data.DataException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChestBlock extends ContainerBlock {
   public ChestBlock() {
      super(54, 27);
   }

   public ChestBlock(int data) {
      super(54, data, 27);
   }

   public ChestBlock(int data, BaseItemStack[] items) {
      super(54, data, 27);
      this.setItems(items);
   }

   public String getNbtId() {
      return "Chest";
   }

   public CompoundTag getNbtData() {
      Map<String, Tag> values = new HashMap();
      values.put("Items", new ListTag("Items", CompoundTag.class, this.serializeInventory(this.getItems())));
      return new CompoundTag(this.getNbtId(), values);
   }

   public void setNbtData(CompoundTag rootTag) throws DataException {
      if (rootTag != null) {
         Map<String, Tag> values = rootTag.getValue();
         Tag t = (Tag)values.get("id");
         if (t instanceof StringTag && ((StringTag)t).getValue().equals("Chest")) {
            List<CompoundTag> items = new ArrayList();

            for(Tag tag : ((ListTag)NBTUtils.getChildTag(values, "Items", ListTag.class)).getValue()) {
               if (!(tag instanceof CompoundTag)) {
                  throw new DataException("CompoundTag expected as child tag of Chest's Items");
               }

               items.add((CompoundTag)tag);
            }

            this.setItems(this.deserializeInventory(items));
         } else {
            throw new DataException("'Chest' tile entity expected");
         }
      }
   }
}
