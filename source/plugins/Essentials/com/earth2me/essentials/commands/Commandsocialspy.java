package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import org.bukkit.Server;

public class Commandsocialspy extends EssentialsCommand {
   public Commandsocialspy() {
      super("socialspy");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      user.sendMessage("§7SocialSpy " + (user.toggleSocialSpy() ? I18n._("enabled") : I18n._("disabled")));
   }
}
