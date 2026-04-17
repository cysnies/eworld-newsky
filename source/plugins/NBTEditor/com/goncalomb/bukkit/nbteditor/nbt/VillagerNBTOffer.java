package com.goncalomb.bukkit.nbteditor.nbt;

import com.goncalomb.bukkit.reflect.NBTTagCompoundWrapper;
import com.goncalomb.bukkit.reflect.NBTUtils;
import org.bukkit.inventory.ItemStack;

public final class VillagerNBTOffer {
   private ItemStack _buyA;
   private ItemStack _buyB;
   private ItemStack _sell;
   private int _maxUses;
   private int _uses;

   public VillagerNBTOffer(ItemStack buyA, ItemStack buyB, ItemStack sell) {
      this(buyA, buyB, sell, 7);
   }

   public VillagerNBTOffer(ItemStack buyA, ItemStack buyB, ItemStack sell, int maxUses) {
      this(buyA, buyB, sell, maxUses, 0);
   }

   public VillagerNBTOffer(ItemStack buyA, ItemStack buyB, ItemStack sell, int maxUses, int uses) {
      this._buyA = buyA;
      this._buyB = buyB;
      this._sell = sell;
      this._maxUses = maxUses;
      this._uses = uses;
   }

   VillagerNBTOffer(NBTTagCompoundWrapper offer) {
      this._buyA = NBTUtils.itemStackFromNBTTagCompound(offer.getCompound("buy"));
      if (offer.hasKey("buyB")) {
         this._buyB = NBTUtils.itemStackFromNBTTagCompound(offer.getCompound("buyB"));
      } else {
         this._buyB = null;
      }

      this._sell = NBTUtils.itemStackFromNBTTagCompound(offer.getCompound("sell"));
      this._maxUses = offer.getInt("maxUses");
      this._uses = offer.getInt("uses");
   }

   NBTTagCompoundWrapper getCompound() {
      NBTTagCompoundWrapper offer = new NBTTagCompoundWrapper();
      offer.setCompound("buy", NBTUtils.nbtTagCompoundFromItemStack(this._buyA));
      if (this._buyB != null) {
         offer.setCompound("buyB", NBTUtils.nbtTagCompoundFromItemStack(this._buyB));
      }

      offer.setCompound("sell", NBTUtils.nbtTagCompoundFromItemStack(this._sell));
      offer.setInt("maxUses", this._maxUses);
      offer.setInt("uses", this._uses);
      return offer;
   }

   public ItemStack getBuyA() {
      return this._buyA;
   }

   public ItemStack getBuyB() {
      return this._buyB;
   }

   public ItemStack getSell() {
      return this._sell;
   }

   public int getMaxUses() {
      return this._maxUses;
   }

   public int getUses() {
      return this._uses;
   }
}
