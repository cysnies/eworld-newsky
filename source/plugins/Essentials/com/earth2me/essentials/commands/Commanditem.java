package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.MetaItemStack;
import com.earth2me.essentials.User;
import com.earth2me.essentials.craftbukkit.InventoryWorkaround;
import java.util.Locale;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;

public class Commanditem extends EssentialsCommand {
   public Commanditem() {
      super("item");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         ItemStack stack = this.ess.getItemDb().get(args[0]);
         String itemname = stack.getType().toString().toLowerCase(Locale.ENGLISH).replace("_", "");
         if (this.ess.getSettings().permissionBasedItemSpawn()) {
            if (!user.isAuthorized("essentials.itemspawn.item-all") && !user.isAuthorized("essentials.itemspawn.item-" + itemname) && !user.isAuthorized("essentials.itemspawn.item-" + stack.getTypeId())) {
               throw new Exception(I18n._("cantSpawnItem", itemname));
            }
         } else if (!user.isAuthorized("essentials.itemspawn.exempt") && !user.canSpawnItem(stack.getTypeId())) {
            throw new Exception(I18n._("cantSpawnItem", itemname));
         }

         try {
            if (args.length > 1 && Integer.parseInt(args[1]) > 0) {
               stack.setAmount(Integer.parseInt(args[1]));
            } else if (this.ess.getSettings().getDefaultStackSize() > 0) {
               stack.setAmount(this.ess.getSettings().getDefaultStackSize());
            } else if (this.ess.getSettings().getOversizedStackSize() > 0 && user.isAuthorized("essentials.oversizedstacks")) {
               stack.setAmount(this.ess.getSettings().getOversizedStackSize());
            }
         } catch (NumberFormatException var9) {
            throw new NotEnoughArgumentsException();
         }

         if (args.length > 2) {
            MetaItemStack metaStack = new MetaItemStack(stack);
            boolean allowUnsafe = this.ess.getSettings().allowUnsafeEnchantments() && user.isAuthorized("essentials.enchantments.allowunsafe");
            metaStack.parseStringMeta(user, allowUnsafe, args, 2, this.ess);
            stack = metaStack.getItemStack();
         }

         if (stack.getType() == Material.AIR) {
            throw new Exception(I18n._("cantSpawnItem", "Air"));
         } else {
            String displayName = stack.getType().toString().toLowerCase(Locale.ENGLISH).replace('_', ' ');
            user.sendMessage(I18n._("itemSpawn", stack.getAmount(), displayName));
            if (user.isAuthorized("essentials.oversizedstacks")) {
               InventoryWorkaround.addOversizedItems(user.getInventory(), this.ess.getSettings().getOversizedStackSize(), stack);
            } else {
               InventoryWorkaround.addItems(user.getInventory(), stack);
            }

            user.updateInventory();
         }
      }
   }
}
