package com.earth2me.essentials.spawn;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.commands.EssentialsCommand;
import com.earth2me.essentials.commands.NoChargeException;
import com.earth2me.essentials.commands.NotEnoughArgumentsException;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class Commandspawn extends EssentialsCommand {
   public Commandspawn() {
      super("spawn");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      Trade charge = new Trade(this.getName(), this.ess);
      charge.isAffordableFor(user);
      if (args.length > 0 && user.isAuthorized("essentials.spawn.others")) {
         User otherUser = this.getPlayer(server, args, 0);
         this.respawn(otherUser, charge);
         if (!otherUser.equals(user)) {
            otherUser.sendMessage(I18n._("teleportAtoB", new Object[]{user.getDisplayName(), "spawn"}));
            user.sendMessage(I18n._("teleporting", new Object[0]));
         }
      } else {
         this.respawn(user, charge);
      }

      throw new NoChargeException();
   }

   protected void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         User user = this.getPlayer(server, args, 0);
         this.respawn(user, (Trade)null);
         user.sendMessage(I18n._("teleportAtoB", new Object[]{"Console", "spawn"}));
         sender.sendMessage(I18n._("teleporting", new Object[0]));
      }
   }

   private void respawn(User user, Trade charge) throws Exception {
      SpawnStorage spawns = (SpawnStorage)this.module;
      Location spawn = spawns.getSpawn(user.getGroup());
      user.getTeleport().teleport(spawn, charge, TeleportCause.COMMAND);
   }
}
