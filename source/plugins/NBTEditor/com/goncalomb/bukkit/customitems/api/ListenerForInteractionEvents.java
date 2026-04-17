package com.goncalomb.bukkit.customitems.api;

import java.util.Arrays;
import java.util.HashSet;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

final class ListenerForInteractionEvents extends CustomItemListener {
   private static HashSet _interationMaterials;

   static {
      _interationMaterials = new HashSet(Arrays.asList(Material.WORKBENCH, Material.CHEST, Material.ENDER_CHEST, Material.BREWING_STAND, Material.ENCHANTMENT_TABLE));
   }

   public boolean put(CustomItem customItem) {
      try {
         return !this.isOverriden(customItem, "onLeftClick", new Class[]{PlayerInteractEvent.class, PlayerDetails.class}) && !this.isOverriden(customItem, "onRightClick", new Class[]{PlayerInteractEvent.class, PlayerDetails.class}) && !this.isOverriden(customItem, "onAttack", new Class[]{EntityDamageByEntityEvent.class, PlayerDetails.class}) && !this.isOverriden(customItem, "onInteractEntity", new Class[]{PlayerInteractEntityEvent.class, PlayerDetails.class}) ? false : super.put(customItem);
      } catch (NoSuchMethodException e) {
         throw new Error(e);
      }
   }

   @EventHandler
   private void playerInteract(PlayerInteractEvent event) {
      Action action = event.getAction();
      if (action != Action.PHYSICAL) {
         if (action == Action.RIGHT_CLICK_BLOCK && _interationMaterials.contains(event.getClickedBlock().getType())) {
            return;
         }

         CustomItem customItem = this.get(event.getItem());
         if (customItem != null) {
            if (this.verifyCustomItem(customItem, event.getPlayer(), false)) {
               if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
                  if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
                     customItem.onLeftClick(event, new PlayerDetails(event));
                  }
               } else {
                  customItem.onRightClick(event, new PlayerDetails(event));
               }
            } else {
               event.setCancelled(true);
            }
         }
      }

   }

   @EventHandler
   private void playerInteractEntity(PlayerInteractEntityEvent event) {
      Player player = event.getPlayer();
      ItemStack item = player.getItemInHand();
      CustomItem customItem = this.get(item);
      if (customItem != null && this.verifyCustomItem(customItem, event.getPlayer(), false)) {
         event.setCancelled(true);
         customItem.onInteractEntity(event, new PlayerDetails(item, event.getPlayer()));
      }

   }

   @EventHandler
   private void entityDamageByEntity(EntityDamageByEntityEvent event) {
      Entity damager = event.getDamager();
      if (damager instanceof Player) {
         Player player = (Player)damager;
         ItemStack item = player.getItemInHand();
         CustomItem customItem = this.get(item);
         if (customItem != null && this.verifyCustomItem(customItem, player, true)) {
            customItem.onAttack(event, new PlayerDetails(item, player));
         }
      }

   }
}
