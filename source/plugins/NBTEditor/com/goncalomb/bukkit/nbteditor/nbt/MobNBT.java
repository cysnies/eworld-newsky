package com.goncalomb.bukkit.nbteditor.nbt;

import com.goncalomb.bukkit.nbteditor.nbt.attributes.AttributeContainer;
import com.goncalomb.bukkit.nbteditor.nbt.variable.BooleanVariable;
import com.goncalomb.bukkit.nbteditor.nbt.variable.FloatVariable;
import com.goncalomb.bukkit.nbteditor.nbt.variable.NBTGenericVariableContainer;
import com.goncalomb.bukkit.nbteditor.nbt.variable.ShortVariable;
import com.goncalomb.bukkit.nbteditor.nbt.variable.StringVariable;
import com.goncalomb.bukkit.reflect.NBTTagCompoundWrapper;
import com.goncalomb.bukkit.reflect.NBTTagListWrapper;
import com.goncalomb.bukkit.reflect.NBTUtils;
import java.util.Arrays;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.inventory.ItemStack;

public class MobNBT extends EntityNBT {
   private ItemStack[] _equipment;

   static {
      NBTGenericVariableContainer variables = new NBTGenericVariableContainer("Mob");
      variables.add("health", new FloatVariable("HealF", 0.0F));
      variables.add("old-health", new ShortVariable("Health", (short)0));
      variables.add("attack-time", new ShortVariable("AttackTime"));
      variables.add("hurt-time", new ShortVariable("HurtTime"));
      variables.add("death-time", new ShortVariable("DeathTime"));
      variables.add("pick-loot", new BooleanVariable("CanPickUpLoot"));
      variables.add("persistent", new BooleanVariable("PersistenceRequired"));
      variables.add("name", new StringVariable("CustomName"));
      variables.add("name-visible", new BooleanVariable("CustomNameVisible"));
      EntityNBTVariableManager.registerVariables(MobNBT.class, variables);
   }

   public void setEquipment(ItemStack hand, ItemStack feet, ItemStack legs, ItemStack chest, ItemStack head) {
      if (hand == null && feet == null && legs == null && chest == null && head == null) {
         this.clearEquipment();
      } else {
         this._equipment = new ItemStack[]{hand, feet, legs, chest, head};
         Object[] equipmentData = new Object[5];

         for(int i = 0; i < 5; ++i) {
            if (this._equipment[i] != null) {
               equipmentData[i] = NBTUtils.nbtTagCompoundFromItemStack(this._equipment[i]);
            } else {
               equipmentData[i] = new NBTTagCompoundWrapper();
            }
         }

         this._data.setList("Equipment", equipmentData);
      }
   }

   public ItemStack[] getEquipment() {
      if (this._equipment == null) {
         this._equipment = new ItemStack[5];
         if (this._data.hasKey("Equipment")) {
            Object[] equipmentData = this._data.getListAsArray("Equipment");

            for(int i = 0; i < 5; ++i) {
               this._equipment[i] = NBTUtils.itemStackFromNBTTagCompound((NBTTagCompoundWrapper)equipmentData[i]);
            }
         }
      }

      return this._equipment;
   }

   public void clearEquipment() {
      this._data.remove("Equipment");
      this._equipment = null;
   }

   public void setDropChances(float hand, float feet, float legs, float chest, float head) {
      this._data.setList("DropChances", hand, feet, legs, chest, head);
   }

   public float[] getDropChances() {
      return this._data.hasKey("DropChances") ? ArrayUtils.toPrimitive((Float[])Arrays.copyOfRange(this._data.getListAsArray("DropChances"), 0, 5, Float[].class)) : null;
   }

   public void clearDropChances() {
      this._data.remove("DropChances");
   }

   public void setEffectsFromPotion(ItemStack potion) {
      if (potion != null) {
         NBTTagListWrapper effects = NBTUtils.effectsListFromPotion(potion);
         if (effects != null) {
            this._data.set("ActiveEffects", effects);
            return;
         }
      } else {
         this._data.remove("ActiveEffects");
      }

   }

   public ItemStack getEffectsAsPotion() {
      return this._data.hasKey("ActiveEffects") ? NBTUtils.getGenericPotionFromEffectList(this._data.getList("ActiveEffects")) : null;
   }

   public AttributeContainer getAttributes() {
      return this._data.hasKey("Attributes") ? AttributeContainer.fromNBT(this._data.getList("Attributes")) : new AttributeContainer();
   }

   public void setAttributes(AttributeContainer container) {
      if (container != null && container.size() != 0) {
         this._data.setList("Attributes", container.toNBT());
      } else {
         this._data.remove("Attributes");
      }

   }
}
