package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import java.util.Locale;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;

public class Commandmore extends EssentialsCommand {
   public Commandmore() {
      super("more");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      ItemStack stack = user.getItemInHand();
      if (stack == null) {
         throw new Exception(I18n._("cantSpawnItem", "Air"));
      } else if (stack.getAmount() >= (user.isAuthorized("essentials.oversizedstacks") ? this.ess.getSettings().getOversizedStackSize() : stack.getMaxStackSize())) {
         throw new Exception(I18n._("fullStack"));
      } else {
         String itemname = stack.getType().toString().toLowerCase(Locale.ENGLISH).replace("_", "");
         if (this.ess.getSettings().permissionBasedItemSpawn()) {
            if (!user.isAuthorized("essentials.itemspawn.item-all") && !user.isAuthorized("essentials.itemspawn.item-" + itemname) && !user.isAuthorized("essentials.itemspawn.item-" + stack.getTypeId())) {
               throw new Exception(I18n._("cantSpawnItem", itemname));
            }
         } else if (!user.isAuthorized("essentials.itemspawn.exempt") && !user.canSpawnItem(stack.getTypeId())) {
            throw new Exception(I18n._("cantSpawnItem", itemname));
         }

         if (user.isAuthorized("essentials.oversizedstacks")) {
            stack.setAmount(this.ess.getSettings().getOversizedStackSize());
         } else {
            stack.setAmount(stack.getMaxStackSize());
         }

         user.updateInventory();
      }
   }
}
