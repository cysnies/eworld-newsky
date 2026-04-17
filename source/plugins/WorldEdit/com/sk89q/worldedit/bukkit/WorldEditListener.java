package com.sk89q.worldedit.bukkit;

import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldVector;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class WorldEditListener implements Listener {
   private WorldEditPlugin plugin;
   private boolean ignoreLeftClickAir = false;

   public WorldEditListener(WorldEditPlugin plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      this.plugin.getWorldEdit().markExpire(this.plugin.wrapPlayer(event.getPlayer()));
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onGamemode(PlayerGameModeChangeEvent event) {
      WorldEdit.getInstance().getSession((LocalPlayer)this.plugin.wrapPlayer(event.getPlayer()));
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
      String[] split = event.getMessage().split(" ");
      if (split.length > 0) {
         split = this.plugin.getWorldEdit().commandDetection(split);
         split[0] = "/" + split[0];
      }

      String newMessage = StringUtil.joinString(split, " ");
      if (!newMessage.equals(event.getMessage())) {
         event.setMessage(newMessage);
         this.plugin.getServer().getPluginManager().callEvent(event);
         if (!event.isCancelled()) {
            if (event.getMessage().length() > 0) {
               this.plugin.getServer().dispatchCommand(event.getPlayer(), event.getMessage().substring(1));
            }

            event.setCancelled(true);
         }
      }

   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      if (event.useItemInHand() != Result.DENY) {
         LocalPlayer player = this.plugin.wrapPlayer(event.getPlayer());
         LocalWorld world = player.getWorld();
         WorldEdit we = this.plugin.getWorldEdit();
         Action action = event.getAction();
         if (action == Action.LEFT_CLICK_BLOCK) {
            Block clickedBlock = event.getClickedBlock();
            WorldVector pos = new WorldVector(world, clickedBlock.getX(), clickedBlock.getY(), clickedBlock.getZ());
            if (we.handleBlockLeftClick(player, pos)) {
               event.setCancelled(true);
            }

            if (we.handleArmSwing(player)) {
               event.setCancelled(true);
            }

            if (!this.ignoreLeftClickAir) {
               int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                  public void run() {
                     WorldEditListener.this.ignoreLeftClickAir = false;
                  }
               }, 2L);
               if (taskId != -1) {
                  this.ignoreLeftClickAir = true;
               }
            }
         } else if (action == Action.LEFT_CLICK_AIR) {
            if (this.ignoreLeftClickAir) {
               return;
            }

            if (we.handleArmSwing(player)) {
               event.setCancelled(true);
            }
         } else if (action == Action.RIGHT_CLICK_BLOCK) {
            Block clickedBlock = event.getClickedBlock();
            WorldVector pos = new WorldVector(world, clickedBlock.getX(), clickedBlock.getY(), clickedBlock.getZ());
            if (we.handleBlockRightClick(player, pos)) {
               event.setCancelled(true);
            }

            if (we.handleRightClick(player)) {
               event.setCancelled(true);
            }
         } else if (action == Action.RIGHT_CLICK_AIR && we.handleRightClick(player)) {
            event.setCancelled(true);
         }

      }
   }
}
