package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class Commandkill extends EssentialsCommand {
   public Commandkill() {
      super("kill");
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else if (args[0].trim().length() < 2) {
         throw new NotEnoughArgumentsException("You need to specify a player to kill.");
      } else {
         for(Player matchPlayer : server.matchPlayer(args[0])) {
            EntityDamageEvent ede = new EntityDamageEvent(matchPlayer, sender instanceof Player && ((Player)sender).getName().equals(matchPlayer.getName()) ? DamageCause.SUICIDE : DamageCause.CUSTOM, 32767);
            server.getPluginManager().callEvent(ede);
            if (!ede.isCancelled() || !(sender instanceof Player) || this.ess.getUser(sender).isAuthorized("essentials.kill.force")) {
               matchPlayer.damage(32767);
               if (matchPlayer.getHealth() > 0) {
                  matchPlayer.setHealth(0);
               }

               sender.sendMessage(I18n._("kill", matchPlayer.getDisplayName()));
            }
         }

      }
   }
}
