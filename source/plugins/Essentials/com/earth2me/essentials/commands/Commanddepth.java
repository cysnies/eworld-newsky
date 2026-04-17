package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import org.bukkit.Server;

public class Commanddepth extends EssentialsCommand {
   public Commanddepth() {
      super("depth");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      int depth = user.getLocation().getBlockY() - 63;
      if (depth > 0) {
         user.sendMessage(I18n._("depthAboveSea", depth));
      } else if (depth < 0) {
         user.sendMessage(I18n._("depthBelowSea", -depth));
      } else {
         user.sendMessage(I18n._("depth"));
      }

   }
}
