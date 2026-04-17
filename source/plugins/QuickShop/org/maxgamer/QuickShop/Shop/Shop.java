package org.maxgamer.QuickShop.Shop;

import java.util.List;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface Shop {
   Shop clone();

   int getRemainingStock();

   int getRemainingSpace();

   boolean matches(ItemStack var1);

   Location getLocation();

   double getPrice();

   void setPrice(double var1);

   void update();

   short getDurability();

   String getOwner();

   ItemStack getItem();

   void remove(ItemStack var1, int var2);

   void add(ItemStack var1, int var2);

   void sell(Player var1, int var2);

   void buy(Player var1, int var2);

   void setOwner(String var1);

   void setUnlimited(boolean var1);

   boolean isUnlimited();

   ShopType getShopType();

   boolean isBuying();

   boolean isSelling();

   void setShopType(ShopType var1);

   void setSignText();

   void setSignText(String[] var1);

   List getSigns();

   boolean isAttached(Block var1);

   String getDataName();

   void delete();

   void delete(boolean var1);

   boolean isValid();

   void onUnload();

   void onLoad();

   void onClick();
}
