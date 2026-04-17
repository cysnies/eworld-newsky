package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.Mob;
import com.earth2me.essentials.SpawnMob;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import java.util.List;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;

public class Commandspawnmob extends EssentialsCommand {
   public Commandspawnmob() {
      super("spawnmob");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         String mobList = SpawnMob.mobList(user);
         throw new NotEnoughArgumentsException(I18n._("mobsAvailable", mobList));
      } else {
         List<String> mobParts = SpawnMob.mobParts(args[0]);
         List<String> mobData = SpawnMob.mobData(args[0]);
         int mobCount = 1;
         if (args.length >= 2) {
            mobCount = Integer.parseInt(args[1]);
         }

         if (args.length >= 3) {
            User target = this.getPlayer(this.ess.getServer(), user, args, 2);
            SpawnMob.spawnmob(this.ess, server, user, (User)target, mobParts, mobData, mobCount);
         } else {
            SpawnMob.spawnmob(this.ess, server, user, mobParts, mobData, mobCount);
         }
      }
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 3) {
         String mobList = Util.joinList(Mob.getMobList());
         throw new NotEnoughArgumentsException(I18n._("mobsAvailable", mobList));
      } else {
         List<String> mobParts = SpawnMob.mobParts(args[0]);
         List<String> mobData = SpawnMob.mobData(args[0]);
         int mobCount = Integer.parseInt(args[1]);
         User target = this.getPlayer(this.ess.getServer(), args, 2, true, false);
         SpawnMob.spawnmob(this.ess, server, sender, target, mobParts, mobData, mobCount);
      }
   }
}
