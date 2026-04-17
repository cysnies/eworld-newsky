package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import org.bukkit.Server;

public class Commandpowertooltoggle extends EssentialsCommand {
   public Commandpowertooltoggle() {
      super("powertooltoggle");
   }

   protected void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (!user.hasPowerTools()) {
         user.sendMessage(I18n._("noPowerTools"));
      } else {
         user.sendMessage(user.togglePowerToolsEnabled() ? I18n._("powerToolsEnabled") : I18n._("powerToolsDisabled"));
      }
   }
}
