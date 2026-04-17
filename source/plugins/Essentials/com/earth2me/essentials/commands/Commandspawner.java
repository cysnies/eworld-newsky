package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.Mob;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import java.util.Locale;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.CreatureSpawner;

public class Commandspawner extends EssentialsCommand {
   public Commandspawner() {
      super("spawner");
   }

   protected void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length >= 1 && args[0].length() >= 2) {
         Location target = Util.getTarget(user);
         if (target != null && target.getBlock().getType() == Material.MOB_SPAWNER) {
            String name = args[0];
            int delay = 0;
            Mob mob = null;
            mob = Mob.fromName(name);
            if (mob == null) {
               throw new Exception(I18n._("invalidMob"));
            } else if (this.ess.getSettings().getProtectPreventSpawn(mob.getType().toString().toLowerCase(Locale.ENGLISH))) {
               throw new Exception(I18n._("disabledToSpawnMob"));
            } else if (!user.isAuthorized("essentials.spawner." + mob.name.toLowerCase(Locale.ENGLISH))) {
               throw new Exception(I18n._("noPermToSpawnMob"));
            } else {
               if (args.length > 1 && Util.isInt(args[1])) {
                  delay = Integer.parseInt(args[1]);
               }

               Trade charge = new Trade("spawner-" + mob.name.toLowerCase(Locale.ENGLISH), this.ess);
               charge.isAffordableFor(user);

               try {
                  CreatureSpawner spawner = (CreatureSpawner)target.getBlock().getState();
                  spawner.setSpawnedType(mob.getType());
                  spawner.setDelay(delay);
               } catch (Throwable ex) {
                  throw new Exception(I18n._("mobSpawnError"), ex);
               }

               charge.charge(user);
               user.sendMessage(I18n._("setSpawner", mob.name));
            }
         } else {
            throw new Exception(I18n._("mobSpawnTarget"));
         }
      } else {
         throw new NotEnoughArgumentsException(I18n._("mobsAvailable", Util.joinList(Mob.getMobList())));
      }
   }
}
