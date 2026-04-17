package com.earth2me.essentials.antibuild;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.User;
import java.util.logging.Level;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class EssentialsAntiBuildListener implements Listener {
   private final transient IAntiBuild prot;
   private final transient IEssentials ess;

   public EssentialsAntiBuildListener(IAntiBuild parent) {
      this.prot = parent;
      this.ess = this.prot.getEssentialsConnect().getEssentials();
   }

   private boolean metaPermCheck(User user, String action, Block block) {
      if (block == null) {
         if (this.ess.getSettings().isDebug()) {
            this.ess.getLogger().log(Level.INFO, "AntiBuild permission check failed, invalid block.");
         }

         return false;
      } else {
         return this.metaPermCheck(user, action, block.getTypeId(), (short)block.getData());
      }
   }

   private boolean metaPermCheck(User user, String action, int blockId) {
      String blockPerm = "essentials.build." + action + "." + blockId;
      return user.isAuthorized(blockPerm);
   }

   private boolean metaPermCheck(User user, String action, int blockId, short data) {
      String blockPerm = "essentials.build." + action + "." + blockId;
      String dataPerm = blockPerm + ":" + data;
      if (user.isPermissionSet(dataPerm)) {
         return user.isAuthorized(dataPerm);
      } else {
         if (this.ess.getSettings().isDebug()) {
            this.ess.getLogger().log(Level.INFO, "DataValue perm on " + user.getName() + " is not directly set: " + dataPerm);
         }

         return user.isAuthorized(blockPerm);
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBlockPlace(BlockPlaceEvent event) {
      User user = this.ess.getUser(event.getPlayer());
      Block block = event.getBlockPlaced();
      int typeId = block.getTypeId();
      Material type = block.getType();
      if (this.prot.getSettingBool(AntiBuildConfig.disable_build) && !user.canBuild() && !user.isAuthorized("essentials.build") && !this.metaPermCheck(user, "place", block)) {
         if (this.ess.getSettings().warnOnBuildDisallow()) {
            user.sendMessage(I18n._("antiBuildPlace", new Object[]{type.toString()}));
         }

         event.setCancelled(true);
      } else if (this.prot.checkProtectionItems(AntiBuildConfig.blacklist_placement, typeId) && !user.isAuthorized("essentials.protect.exemptplacement")) {
         if (this.ess.getSettings().warnOnBuildDisallow()) {
            user.sendMessage(I18n._("antiBuildPlace", new Object[]{type.toString()}));
         }

         event.setCancelled(true);
      } else {
         if (this.prot.checkProtectionItems(AntiBuildConfig.alert_on_placement, typeId) && !user.isAuthorized("essentials.protect.alerts.notrigger")) {
            this.prot.getEssentialsConnect().alert(user, type.toString(), I18n._("alertPlaced", new Object[0]));
         }

      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBlockBreak(BlockBreakEvent event) {
      User user = this.ess.getUser(event.getPlayer());
      Block block = event.getBlock();
      int typeId = block.getTypeId();
      Material type = block.getType();
      if (this.prot.getSettingBool(AntiBuildConfig.disable_build) && !user.canBuild() && !user.isAuthorized("essentials.build") && !this.metaPermCheck(user, "break", block)) {
         if (this.ess.getSettings().warnOnBuildDisallow()) {
            user.sendMessage(I18n._("antiBuildBreak", new Object[]{type.toString()}));
         }

         event.setCancelled(true);
      } else if (this.prot.checkProtectionItems(AntiBuildConfig.blacklist_break, typeId) && !user.isAuthorized("essentials.protect.exemptbreak")) {
         if (this.ess.getSettings().warnOnBuildDisallow()) {
            user.sendMessage(I18n._("antiBuildBreak", new Object[]{type.toString()}));
         }

         event.setCancelled(true);
      } else {
         if (this.prot.checkProtectionItems(AntiBuildConfig.alert_on_break, typeId) && !user.isAuthorized("essentials.protect.alerts.notrigger")) {
            this.prot.getEssentialsConnect().alert(user, type.toString(), I18n._("alertBroke", new Object[0]));
         }

      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onHangingBreak(HangingBreakByEntityEvent event) {
      Entity entity = event.getRemover();
      if (entity instanceof Player) {
         User user = this.ess.getUser(entity);
         EntityType type = event.getEntity().getType();
         boolean warn = this.ess.getSettings().warnOnBuildDisallow();
         if (this.prot.getSettingBool(AntiBuildConfig.disable_build) && !user.canBuild() && !user.isAuthorized("essentials.build")) {
            if (type == EntityType.PAINTING && !this.metaPermCheck(user, "break", Material.PAINTING.getId())) {
               if (warn) {
                  user.sendMessage(I18n._("antiBuildBreak", new Object[]{Material.PAINTING.toString()}));
               }

               event.setCancelled(true);
            } else if (type == EntityType.ITEM_FRAME && !this.metaPermCheck(user, "break", Material.ITEM_FRAME.getId())) {
               if (warn) {
                  user.sendMessage(I18n._("antiBuildBreak", new Object[]{Material.ITEM_FRAME.toString()}));
               }

               event.setCancelled(true);
            }
         }
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBlockPistonExtend(BlockPistonExtendEvent event) {
      for(Block block : event.getBlocks()) {
         if (this.prot.checkProtectionItems(AntiBuildConfig.blacklist_piston, block.getTypeId())) {
            event.setCancelled(true);
            return;
         }
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBlockPistonRetract(BlockPistonRetractEvent event) {
      if (event.isSticky()) {
         Block block = event.getRetractLocation().getBlock();
         if (this.prot.checkProtectionItems(AntiBuildConfig.blacklist_piston, block.getTypeId())) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onPlayerInteract(PlayerInteractEvent event) {
      User user = this.ess.getUser(event.getPlayer());
      ItemStack item = event.getItem();
      if (item != null && this.prot.checkProtectionItems(AntiBuildConfig.blacklist_usage, item.getTypeId()) && !user.isAuthorized("essentials.protect.exemptusage")) {
         if (this.ess.getSettings().warnOnBuildDisallow()) {
            user.sendMessage(I18n._("antiBuildUse", new Object[]{item.getType().toString()}));
         }

         event.setCancelled(true);
      } else {
         if (item != null && this.prot.checkProtectionItems(AntiBuildConfig.alert_on_use, item.getTypeId()) && !user.isAuthorized("essentials.protect.alerts.notrigger")) {
            this.prot.getEssentialsConnect().alert(user, item.getType().toString(), I18n._("alertUsed", new Object[0]));
         }

         if (this.prot.getSettingBool(AntiBuildConfig.disable_use) && !user.canBuild() && !user.isAuthorized("essentials.build")) {
            if (event.hasItem() && !this.metaPermCheck(user, "interact", item.getTypeId(), item.getDurability())) {
               event.setCancelled(true);
               if (this.ess.getSettings().warnOnBuildDisallow()) {
                  user.sendMessage(I18n._("antiBuildUse", new Object[]{item.getType().toString()}));
               }

               return;
            }

            if (event.hasBlock() && !this.metaPermCheck(user, "interact", event.getClickedBlock())) {
               event.setCancelled(true);
               if (this.ess.getSettings().warnOnBuildDisallow()) {
                  user.sendMessage(I18n._("antiBuildInteract", new Object[]{event.getClickedBlock().getType().toString()}));
               }
            }
         }

      }
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onCraftItemEvent(CraftItemEvent event) {
      HumanEntity entity = event.getWhoClicked();
      if (entity instanceof Player) {
         User user = this.ess.getUser(entity);
         ItemStack item = event.getRecipe().getResult();
         if (this.prot.getSettingBool(AntiBuildConfig.disable_use) && !user.canBuild() && !user.isAuthorized("essentials.build") && !this.metaPermCheck(user, "craft", item.getTypeId(), item.getDurability())) {
            event.setCancelled(true);
            if (this.ess.getSettings().warnOnBuildDisallow()) {
               user.sendMessage(I18n._("antiBuildCraft", new Object[]{item.getType().toString()}));
            }
         }
      }

   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onPlayerPickupItem(PlayerPickupItemEvent event) {
      User user = this.ess.getUser(event.getPlayer());
      ItemStack item = event.getItem().getItemStack();
      if (this.prot.getSettingBool(AntiBuildConfig.disable_use) && !user.canBuild() && !user.isAuthorized("essentials.build") && !this.metaPermCheck(user, "pickup", item.getTypeId(), item.getDurability())) {
         event.setCancelled(true);
         event.getItem().setPickupDelay(50);
      }

   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onPlayerDropItem(PlayerDropItemEvent event) {
      User user = this.ess.getUser(event.getPlayer());
      ItemStack item = event.getItemDrop().getItemStack();
      if (this.prot.getSettingBool(AntiBuildConfig.disable_use) && !user.canBuild() && !user.isAuthorized("essentials.build") && !this.metaPermCheck(user, "drop", item.getTypeId(), item.getDurability())) {
         event.setCancelled(true);
         user.updateInventory();
         if (this.ess.getSettings().warnOnBuildDisallow()) {
            user.sendMessage(I18n._("antiBuildDrop", new Object[]{item.getType().toString()}));
         }
      }

   }
}
