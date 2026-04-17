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

public class DispenserBlock extends ContainerBlock {
   public DispenserBlock() {
      super(23, 9);
   }

   public DispenserBlock(int data) {
      super(23, data, 9);
   }

   public DispenserBlock(int data, BaseItemStack[] items) {
      super(23, data, 9);
      this.setItems(items);
   }

   public String getNbtId() {
      return "Trap";
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
         if (t instanceof StringTag && ((StringTag)t).getValue().equals("Trap")) {
            List<CompoundTag> items = new ArrayList();

            for(Tag tag : ((ListTag)NBTUtils.getChildTag(values, "Items", ListTag.class)).getValue()) {
               if (!(tag instanceof CompoundTag)) {
                  throw new DataException("CompoundTag expected as child tag of Trap Items");
               }

               items.add((CompoundTag)tag);
            }

            this.setItems(this.deserializeInventory(items));
         } else {
            throw new DataException("'Trap' tile entity expected");
         }
      }
   }
}
