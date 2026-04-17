package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import org.bukkit.Server;

public class Commandenderchest extends EssentialsCommand {
   public Commandenderchest() {
      super("enderchest");
   }

   protected void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length > 0 && user.isAuthorized("essentials.enderchest.others")) {
         User invUser = this.getPlayer(server, user, args, 0);
         user.openInventory(invUser.getEnderChest());
         user.setEnderSee(true);
      } else {
         user.openInventory(user.getEnderChest());
         user.setEnderSee(false);
      }

   }
}
