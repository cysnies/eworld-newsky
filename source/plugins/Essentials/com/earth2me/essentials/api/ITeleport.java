package com.earth2me.essentials.api;

import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent;

public interface ITeleport {
   void now(Location var1, boolean var2, PlayerTeleportEvent.TeleportCause var3) throws Exception;
}
