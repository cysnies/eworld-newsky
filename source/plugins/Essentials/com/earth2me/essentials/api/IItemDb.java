package com.earth2me.essentials.api;

import org.bukkit.inventory.ItemStack;

public interface IItemDb {
   ItemStack get(String var1, int var2) throws Exception;

   ItemStack get(String var1) throws Exception;
}
