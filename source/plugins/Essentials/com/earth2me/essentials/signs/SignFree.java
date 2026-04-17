package com.earth2me.essentials.signs;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SignFree extends EssentialsSign {
   public SignFree() {
      super("Free");
   }

   protected boolean onSignCreate(EssentialsSign.ISign sign, User player, String username, IEssentials ess) throws SignException {
      try {
         this.getItemStack(sign.getLine(1), 1, ess);
         return true;
      } catch (SignException ex) {
         sign.setLine(1, "§c<item>");
         throw new SignException(ex.getMessage(), ex);
      }
   }

   protected boolean onSignInteract(EssentialsSign.ISign sign, User player, String username, IEssentials ess) throws SignException {
      ItemStack item = this.getItemStack(sign.getLine(1), 1, ess);
      if (item.getType() == Material.AIR) {
         throw new SignException(I18n._("cantSpawnItem", "Air"));
      } else {
         item.setAmount(item.getType().getMaxStackSize());
         Inventory invent = ess.getServer().createInventory(player, 36);

         for(int i = 0; i < 36; ++i) {
            invent.addItem(new ItemStack[]{item});
         }

         player.openInventory(invent);
         Trade.log("Sign", "Free", "Interact", username, (Trade)null, username, new Trade(item, ess), sign.getBlock().getLocation(), ess);
         return true;
      }
   }
}
