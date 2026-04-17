package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class Commandtop extends EssentialsCommand {
   public Commandtop() {
      super("top");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      int topX = user.getLocation().getBlockX();
      int topZ = user.getLocation().getBlockZ();
      float pitch = user.getLocation().getPitch();
      float yaw = user.getLocation().getYaw();
      Location location = new Location(user.getWorld(), (double)topX, (double)user.getWorld().getMaxHeight(), (double)topZ, yaw, pitch);
      user.getTeleport().teleport(location, new Trade(this.getName(), this.ess), TeleportCause.COMMAND);
      user.sendMessage(I18n._("teleportTop"));
   }
}
