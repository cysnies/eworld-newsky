package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.MetaItemStack;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import com.earth2me.essentials.craftbukkit.InventoryWorkaround;
import java.util.Locale;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Commandgive extends EssentialsCommand {
   public Commandgive() {
      super("give");
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 2) {
         throw new NotEnoughArgumentsException();
      } else {
         ItemStack stack = this.ess.getItemDb().get(args[1]);
         String itemname = stack.getType().toString().toLowerCase(Locale.ENGLISH).replace("_", "");
         if (sender instanceof Player) {
            if (this.ess.getSettings().permissionBasedItemSpawn()) {
               if (!this.ess.getUser(sender).isAuthorized("essentials.itemspawn.item-all") && !this.ess.getUser(sender).isAuthorized("essentials.itemspawn.item-" + itemname) && !this.ess.getUser(sender).isAuthorized("essentials.itemspawn.item-" + stack.getTypeId())) {
                  throw new Exception(I18n._("cantSpawnItem", itemname));
               }
            } else if (!this.ess.getUser(sender).isAuthorized("essentials.itemspawn.exempt") && !this.ess.getUser(sender).canSpawnItem(stack.getTypeId())) {
               throw new Exception(I18n._("cantSpawnItem", itemname));
            }
         }

         User giveTo = this.getPlayer(server, args, 0);

         try {
            if (args.length > 3 && Util.isInt(args[2]) && Util.isInt(args[3])) {
               stack.setAmount(Integer.parseInt(args[2]));
               stack.setDurability(Short.parseShort(args[3]));
            } else if (args.length > 2 && Integer.parseInt(args[2]) > 0) {
               stack.setAmount(Integer.parseInt(args[2]));
            } else if (this.ess.getSettings().getDefaultStackSize() > 0) {
               stack.setAmount(this.ess.getSettings().getDefaultStackSize());
            } else if (this.ess.getSettings().getOversizedStackSize() > 0 && giveTo.isAuthorized("essentials.oversizedstacks")) {
               stack.setAmount(this.ess.getSettings().getOversizedStackSize());
            }
         } catch (NumberFormatException var10) {
            throw new NotEnoughArgumentsException();
         }

         if (args.length > 3) {
            MetaItemStack metaStack = new MetaItemStack(stack);
            boolean allowUnsafe = this.ess.getSettings().allowUnsafeEnchantments();
            if (allowUnsafe && sender instanceof Player && !this.ess.getUser(sender).isAuthorized("essentials.enchantments.allowunsafe")) {
               allowUnsafe = false;
            }

            metaStack.parseStringMeta(sender, allowUnsafe, args, Util.isInt(args[3]) ? 4 : 3, this.ess);
            stack = metaStack.getItemStack();
         }

         if (stack.getType() == Material.AIR) {
            throw new Exception(I18n._("cantSpawnItem", "Air"));
         } else {
            String itemName = stack.getType().toString().toLowerCase(Locale.ENGLISH).replace('_', ' ');
            sender.sendMessage(I18n._("giveSpawn", stack.getAmount(), itemName, giveTo.getDisplayName()));
            if (giveTo.isAuthorized("essentials.oversizedstacks")) {
               InventoryWorkaround.addOversizedItems(giveTo.getInventory(), this.ess.getSettings().getOversizedStackSize(), stack);
            } else {
               InventoryWorkaround.addItems(giveTo.getInventory(), stack);
            }

            giveTo.updateInventory();
         }
      }
   }
}
