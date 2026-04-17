package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import org.bukkit.Server;
import org.bukkit.inventory.Inventory;

public class Commandinvsee extends EssentialsCommand {
   public Commandinvsee() {
      super("invsee");
   }

   protected void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         User invUser = this.getPlayer(server, user, args, 0);
         Inventory inv;
         if (args.length > 1 && user.isAuthorized("essentials.invsee.equip")) {
            inv = server.createInventory(invUser, 9, "Equipped");
            inv.setContents(invUser.getInventory().getArmorContents());
         } else {
            inv = invUser.getInventory();
         }

         user.openInventory(inv);
         user.setInvSee(true);
      }
   }
}
