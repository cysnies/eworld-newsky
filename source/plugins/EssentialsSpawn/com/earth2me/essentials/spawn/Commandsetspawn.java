package com.earth2me.essentials.spawn;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.commands.EssentialsCommand;
import org.bukkit.Server;

public class Commandsetspawn extends EssentialsCommand {
   public Commandsetspawn() {
      super("setspawn");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      String group = args.length > 0 ? getFinalArg(args, 0) : "default";
      ((SpawnStorage)this.module).setSpawn(user.getLocation(), group);
      user.sendMessage(I18n._("spawnSet", new Object[]{group}));
   }
}
