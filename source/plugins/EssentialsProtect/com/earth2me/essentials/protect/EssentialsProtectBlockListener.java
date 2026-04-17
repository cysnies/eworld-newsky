package com.earth2me.essentials.protect;

import com.earth2me.essentials.IEssentials;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;

public class EssentialsProtectBlockListener implements Listener {
   private final IProtect prot;
   private final IEssentials ess;

   public EssentialsProtectBlockListener(IProtect parent) {
      this.prot = parent;
      this.ess = this.prot.getEssentialsConnect().getEssentials();
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBlockIgnite(BlockIgniteEvent event) {
      if (event.getBlock().getType() != Material.OBSIDIAN && event.getBlock().getRelative(BlockFace.DOWN).getType() != Material.OBSIDIAN) {
         if (event.getCause().equals(IgniteCause.SPREAD)) {
            event.setCancelled(this.prot.getSettingBool(ProtectConfig.prevent_fire_spread));
         } else if (event.getCause().equals(IgniteCause.FLINT_AND_STEEL)) {
            event.setCancelled(this.prot.getSettingBool(ProtectConfig.prevent_flint_fire));
         } else if (event.getCause().equals(IgniteCause.LAVA)) {
            event.setCancelled(this.prot.getSettingBool(ProtectConfig.prevent_lava_fire_spread));
         } else {
            if (event.getCause().equals(IgniteCause.LIGHTNING)) {
               event.setCancelled(this.prot.getSettingBool(ProtectConfig.prevent_lightning_fire_spread));
            }

         }
      } else {
         event.setCancelled(this.prot.getSettingBool(ProtectConfig.prevent_portal_creation));
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBlockFromTo(BlockFromToEvent event) {
      Block block = event.getBlock();
      if (block.getType() != Material.WATER && block.getType() != Material.STATIONARY_WATER) {
         if (block.getType() != Material.LAVA && block.getType() != Material.STATIONARY_LAVA) {
            if (block.getType() == Material.AIR) {
               event.setCancelled(this.prot.getSettingBool(ProtectConfig.prevent_water_bucket_flow));
            }

         } else {
            event.setCancelled(this.prot.getSettingBool(ProtectConfig.prevent_lava_flow));
         }
      } else {
         event.setCancelled(this.prot.getSettingBool(ProtectConfig.prevent_water_flow));
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBlockBurn(BlockBurnEvent event) {
      if (this.prot.getSettingBool(ProtectConfig.prevent_fire_spread)) {
         event.setCancelled(true);
      }

   }
}
