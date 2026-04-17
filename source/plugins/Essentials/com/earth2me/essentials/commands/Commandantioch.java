package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.TNTPrimed;

public class Commandantioch extends EssentialsCommand {
   public Commandantioch() {
      super("antioch");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length > 0) {
         this.ess.broadcastMessage(user, "...lobbest thou thy Holy Hand Grenade of Antioch towards thy foe,");
         this.ess.broadcastMessage(user, "who being naughty in My sight, shall snuff it.");
      }

      Location loc = Util.getTarget(user);
      loc.getWorld().spawn(loc, TNTPrimed.class);
   }
}
