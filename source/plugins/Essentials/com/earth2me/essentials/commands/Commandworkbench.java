package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import org.bukkit.Location;
import org.bukkit.Server;

public class Commandworkbench extends EssentialsCommand {
   public Commandworkbench() {
      super("workbench");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      user.openWorkbench((Location)null, true);
   }
}
