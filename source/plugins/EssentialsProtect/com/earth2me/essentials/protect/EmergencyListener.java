package com.earth2me.essentials.protect;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class EmergencyListener implements Listener {
   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onBlockBurn(BlockBurnEvent event) {
      event.setCancelled(true);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onBlockIgnite(BlockIgniteEvent event) {
      event.setCancelled(true);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onBlockFromTo(BlockFromToEvent event) {
      event.setCancelled(true);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onBlockBreak(BlockBreakEvent event) {
      event.setCancelled(true);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onPlayerJoin(PlayerJoinEvent event) {
      event.getPlayer().sendMessage("Essentials Protect is in emergency mode. Check your log for errors.");
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onEntityExplode(EntityExplodeEvent event) {
      event.setCancelled(true);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onEntityDamage(EntityDamageEvent event) {
      event.setCancelled(true);
   }
}
