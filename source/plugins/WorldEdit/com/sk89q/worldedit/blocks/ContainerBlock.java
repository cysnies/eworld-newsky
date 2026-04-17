package com.sk89q.worldedit.blocks;

import com.sk89q.jnbt.ByteTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.NBTUtils;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.data.DataException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ContainerBlock extends BaseBlock implements TileEntityBlock {
   private BaseItemStack[] items;

   public ContainerBlock(int type, int inventorySize) {
      super(type);
      this.items = new BaseItemStack[inventorySize];
   }

   public ContainerBlock(int type, int data, int inventorySize) {
      super(type, data);
      this.items = new BaseItemStack[inventorySize];
   }

   public BaseItemStack[] getItems() {
      return this.items;
   }

   public void setItems(BaseItemStack[] items) {
      this.items = items;
   }

   public boolean hasNbtData() {
      return true;
   }

   public Map serializeItem(BaseItemStack item) {
      Map<String, Tag> data = new HashMap();
      data.put("id", new ShortTag("id", (short)item.getType()));
      data.put("Damage", new ShortTag("Damage", item.getData()));
      data.put("Count", new ByteTag("Count", (byte)item.getAmount()));
      if (item.getEnchantments().size() > 0) {
         List<CompoundTag> enchantmentList = new ArrayList();

         for(Map.Entry entry : item.getEnchantments().entrySet()) {
            Map<String, Tag> enchantment = new HashMap();
            enchantment.put("id", new ShortTag("id", ((Integer)entry.getKey()).shortValue()));
            enchantment.put("lvl", new ShortTag("lvl", ((Integer)entry.getValue()).shortValue()));
            enchantmentList.add(new CompoundTag((String)null, enchantment));
         }

         Map<String, Tag> auxData = new HashMap();
         auxData.put("ench", new ListTag("ench", CompoundTag.class, enchantmentList));
         data.put("tag", new CompoundTag("tag", auxData));
      }

      return data;
   }

   public BaseItemStack deserializeItem(Map data) throws DataException {
      short id = ((ShortTag)NBTUtils.getChildTag(data, "id", ShortTag.class)).getValue();
      short damage = ((ShortTag)NBTUtils.getChildTag(data, "Damage", ShortTag.class)).getValue();
      byte count = ((ByteTag)NBTUtils.getChildTag(data, "Count", ByteTag.class)).getValue();
      BaseItemStack stack = new BaseItemStack(id, count, damage);
      if (data.containsKey("tag")) {
         Map<String, Tag> auxData = ((CompoundTag)NBTUtils.getChildTag(data, "tag", CompoundTag.class)).getValue();
         ListTag ench = (ListTag)auxData.get("ench");

         for(Tag e : ench.getValue()) {
            Map<String, Tag> vars = ((CompoundTag)e).getValue();
            short enchId = ((ShortTag)NBTUtils.getChildTag(vars, "id", ShortTag.class)).getValue();
            short enchLevel = ((ShortTag)NBTUtils.getChildTag(vars, "lvl", ShortTag.class)).getValue();
            stack.getEnchantments().put(Integer.valueOf(enchId), Integer.valueOf(enchLevel));
         }
      }

      return stack;
   }

   public BaseItemStack[] deserializeInventory(List items) throws DataException {
      BaseItemStack[] stacks = new BaseItemStack[items.size()];

      for(CompoundTag tag : items) {
         Map<String, Tag> item = tag.getValue();
         BaseItemStack stack = this.deserializeItem(item);
         byte slot = ((ByteTag)NBTUtils.getChildTag(item, "Slot", ByteTag.class)).getValue();
         if (slot >= 0 && slot < stacks.length) {
            stacks[slot] = stack;
         }
      }

      return stacks;
   }

   public List serializeInventory(BaseItemStack[] items) {
      List<CompoundTag> tags = new ArrayList();

      for(int i = 0; i < items.length; ++i) {
         if (items[i] != null) {
            Map<String, Tag> tagData = this.serializeItem(items[i]);
            tagData.put("Slot", new ByteTag("Slot", (byte)i));
            tags.add(new CompoundTag("", tagData));
         }
      }

      return tags;
   }
}
