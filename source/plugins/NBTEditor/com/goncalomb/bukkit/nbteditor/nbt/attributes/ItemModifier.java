package com.goncalomb.bukkit.nbteditor.nbt.attributes;

import com.goncalomb.bukkit.reflect.NBTTagCompoundWrapper;
import com.goncalomb.bukkit.reflect.NBTTagListWrapper;
import com.goncalomb.bukkit.reflect.NBTUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.inventory.ItemStack;

public final class ItemModifier extends Modifier {
   private AttributeType _attributeType;

   public static ItemModifier fromNBT(NBTTagCompoundWrapper data) {
      return new ItemModifier(AttributeType.getByInternalName(data.getString("AttributeName")), data.getString("Name"), data.getDouble("Amount"), data.getInt("Operation"), new UUID(data.getLong("UUIDMost"), data.getLong("UUIDLeast")));
   }

   public static List getItemStackModifiers(ItemStack item) {
      NBTTagCompoundWrapper tag = NBTUtils.getItemStackTag(item);
      if (!tag.hasKey("AttributeModifiers")) {
         return new ArrayList();
      } else {
         Object[] modifiersData = tag.getListAsArray("AttributeModifiers");
         List<ItemModifier> modifiers = new ArrayList(modifiersData.length);

         for(Object data : modifiersData) {
            modifiers.add(fromNBT((NBTTagCompoundWrapper)data));
         }

         return modifiers;
      }
   }

   public static void setItemStackModifiers(ItemStack item, List modifiers) {
      NBTTagListWrapper modifiersData = new NBTTagListWrapper();

      for(ItemModifier modifier : modifiers) {
         modifiersData.add(modifier.toNBT());
      }

      NBTTagCompoundWrapper tag = NBTUtils.getItemStackTag(item);
      tag.setList("AttributeModifiers", modifiersData);
      NBTUtils.setItemStackTag(item, tag);
   }

   public ItemModifier(AttributeType attributeType, String name, double amount, int operation) {
      super(name, amount, operation);
      this._attributeType = attributeType;
   }

   public ItemModifier(AttributeType attributeType, String name, double amount, int operation, UUID uuid) {
      super(name, amount, operation, uuid);
      this._attributeType = attributeType;
   }

   public AttributeType getAttributeType() {
      return this._attributeType;
   }

   public NBTTagCompoundWrapper toNBT() {
      NBTTagCompoundWrapper data = super.toNBT();
      data.setString("AttributeName", this._attributeType._internalName);
      return data;
   }
}
