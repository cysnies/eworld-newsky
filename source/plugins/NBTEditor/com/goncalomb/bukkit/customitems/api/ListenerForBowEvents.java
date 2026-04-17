package com.goncalomb.bukkit.customitems.api;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

final class ListenerForBowEvents extends CustomItemListener {
   private CustomItemManager _manager;

   public ListenerForBowEvents(CustomItemManager manager) {
      this._manager = manager;
   }

   public boolean put(CustomBow customItem) {
      try {
         return !this.isOverriden(customItem, CustomBow.class, "onShootBow", new Class[]{EntityShootBowEvent.class, DelayedPlayerDetails.class}) && !this.isOverriden(customItem, CustomBow.class, "onProjectileHit", new Class[]{ProjectileHitEvent.class, DelayedPlayerDetails.class}) && !this.isOverriden(customItem, CustomBow.class, "onProjectileDamageEntity", new Class[]{EntityDamageByEntityEvent.class, DelayedPlayerDetails.class}) ? false : super.put(customItem);
      } catch (NoSuchMethodException e) {
         throw new Error(e);
      }
   }

   @EventHandler
   private void entityShootBow(EntityShootBowEvent event) {
      if (event.getEntity() instanceof Player) {
         Player player = (Player)event.getEntity();
         CustomBow customBow = (CustomBow)this.get(event.getBow());
         if (this.verifyCustomItem(customBow, player, false)) {
            DelayedPlayerDetails details = new DelayedPlayerDetails(event.getBow(), player);
            customBow.onShootBow(event, details);
            if (!event.isCancelled() && event.getProjectile() instanceof Projectile) {
               details.lock();
               event.getProjectile().setMetadata("CustomItem-bow", new FixedMetadataValue(this._manager._plugin, new Object[]{customBow, details}));
            }
         }
      }

   }

   @EventHandler
   private void entityDamageByEntity(EntityDamageByEntityEvent event) {
      Entity damager = event.getDamager();
      if (damager.hasMetadata("CustomItem-bow")) {
         Object[] data = ((MetadataValue)damager.getMetadata("CustomItem-bow").get(0)).value();
         ((CustomBow)data[0]).onProjectileDamageEntity(event, (DelayedPlayerDetails)data[1]);
      }

   }

   @EventHandler
   private void projectileHit(ProjectileHitEvent event) {
      Projectile projectile = event.getEntity();
      if (projectile.hasMetadata("CustomItem-bow")) {
         Object[] data = ((MetadataValue)projectile.getMetadata("CustomItem-bow").get(0)).value();
         ((CustomBow)data[0]).onProjectileHit(event, (DelayedPlayerDetails)data[1]);
      }

   }
}
