package com.sk89q.bukkit.util;

import org.bukkit.command.CommandMap;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class FallbackRegistrationListener implements Listener {
   private final CommandMap commandRegistration;

   public FallbackRegistrationListener(CommandMap commandRegistration) {
      this.commandRegistration = commandRegistration;
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
      if (this.commandRegistration.dispatch(event.getPlayer(), event.getMessage())) {
         event.setCancelled(true);
      }

   }
}
