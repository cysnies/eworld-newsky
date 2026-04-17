package com.goncalomb.bukkit.customitems.api;

import org.bukkit.inventory.ItemStack;

public interface IConsumableDetails {
   void consumeItem();

   ItemStack getItem();
}
