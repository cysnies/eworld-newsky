package com.wimbli.WorldBorder;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkLoadEvent;

public class WBListener implements Listener {
   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onPlayerTeleport(PlayerTeleportEvent event) {
      if (Config.KnockBack() != (double)0.0F) {
         if (Config.Debug()) {
            Config.LogWarn("Teleport cause: " + event.getCause().toString());
         }

         Location newLoc = BorderCheckTask.checkPlayer(event.getPlayer(), event.getTo(), true, true);
         if (newLoc != null) {
            event.setTo(newLoc);
         }

      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onPlayerPortal(PlayerPortalEvent event) {
      if (Config.KnockBack() != (double)0.0F && Config.portalRedirection()) {
         Location newLoc = BorderCheckTask.checkPlayer(event.getPlayer(), event.getTo(), true, false);
         if (newLoc != null) {
            event.setTo(newLoc);
         }

      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onChunkLoad(ChunkLoadEvent event) {
      if (!Config.isBorderTimerRunning()) {
         Config.LogWarn("Border-checking task was not running! Something on your server apparently killed it. It will now be restarted.");
         Config.StartBorderTimer();
      }
   }
}
