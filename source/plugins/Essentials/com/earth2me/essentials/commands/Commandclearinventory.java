package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import java.util.List;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Commandclearinventory extends EssentialsCommand {
   public Commandclearinventory() {
      super("clearinventory");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length > 0 && user.isAuthorized("essentials.clearinventory.others")) {
         if (args[0].contentEquals("*") && user.isAuthorized("essentials.clearinventory.all")) {
            this.cleanInventoryAll(server, user, args);
         } else if (args[0].trim().length() < 2) {
            this.cleanInventorySelf(server, user, args);
         } else {
            this.cleanInventoryOthers(server, user, args);
         }
      } else {
         this.cleanInventorySelf(server, user, args);
      }

   }

   protected void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length > 0) {
         if (args[0].contentEquals("*")) {
            this.cleanInventoryAll(server, sender, args);
         } else {
            if (args[0].trim().length() < 2) {
               throw new Exception(I18n._("playerNotFound"));
            }

            this.cleanInventoryOthers(server, sender, args);
         }

      } else {
         throw new NotEnoughArgumentsException();
      }
   }

   private void cleanInventoryAll(Server server, CommandSender sender, String[] args) throws Exception {
      if (args.length <= 1) {
         throw new NotEnoughArgumentsException();
      } else {
         for(Player onlinePlayer : server.getOnlinePlayers()) {
            this.clearInventory(onlinePlayer, args[1]);
         }

         sender.sendMessage(I18n._("inventoryClearedAll"));
      }
   }

   private void cleanInventoryOthers(Server server, CommandSender sender, String[] args) throws Exception {
      List<Player> online = server.matchPlayer(args[0]);
      if (!online.isEmpty()) {
         for(Player p : online) {
            if (args.length > 1) {
               this.clearInventory(p, args[1]);
            } else {
               p.getInventory().clear();
            }

            sender.sendMessage(I18n._("inventoryClearedOthers", p.getDisplayName()));
         }

      } else {
         throw new Exception(I18n._("playerNotFound"));
      }
   }

   private void cleanInventorySelf(Server server, User user, String[] args) throws Exception {
      if (args.length > 0) {
         this.clearInventory(user, args[0]);
      } else {
         user.getInventory().clear();
      }

      user.sendMessage(I18n._("inventoryCleared"));
   }

   private void clearInventory(Player player, String arg) throws Exception {
      if (arg.equalsIgnoreCase("*")) {
         player.getInventory().clear();
      } else if (arg.equalsIgnoreCase("**")) {
         player.getInventory().clear();
         player.getInventory().setArmorContents((ItemStack[])null);
      } else {
         String[] split = arg.split(":");
         ItemStack item = this.ess.getItemDb().get(split[0]);
         int type = item.getTypeId();
         if (split.length > 1 && Util.isInt(split[1])) {
            player.getInventory().clear(type, Integer.parseInt(split[1]));
         } else if (split.length > 1 && split[1].equalsIgnoreCase("*")) {
            player.getInventory().clear(type, -1);
         } else if (Util.isInt(split[0])) {
            player.getInventory().clear(type, -1);
         } else {
            player.getInventory().clear(type, item.getDurability());
         }
      }

   }
}
