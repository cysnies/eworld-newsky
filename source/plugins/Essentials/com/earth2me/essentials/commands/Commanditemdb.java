package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Commanditemdb extends EssentialsCommand {
   public Commanditemdb() {
      super("itemdb");
   }

   protected void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      ItemStack itemStack = null;
      boolean itemHeld = false;
      if (args.length < 1) {
         if (sender instanceof Player) {
            itemHeld = true;
            itemStack = ((Player)sender).getItemInHand();
         }

         if (itemStack == null) {
            throw new NotEnoughArgumentsException();
         }
      } else {
         itemStack = this.ess.getItemDb().get(args[0]);
      }

      sender.sendMessage(I18n._("itemType", itemStack.getType().toString(), itemStack.getTypeId() + ":" + Integer.toString(itemStack.getDurability())));
      if (itemHeld && itemStack.getType() != Material.AIR) {
         int maxuses = itemStack.getType().getMaxDurability();
         int durability = maxuses + 1 - itemStack.getDurability();
         if (maxuses != 0) {
            sender.sendMessage(I18n._("durability", Integer.toString(durability)));
         }
      }

      String itemNameList = this.ess.getItemDb().names(itemStack);
      if (itemNameList != null) {
         sender.sendMessage(I18n._("itemNames", this.ess.getItemDb().names(itemStack)));
      }

   }
}
