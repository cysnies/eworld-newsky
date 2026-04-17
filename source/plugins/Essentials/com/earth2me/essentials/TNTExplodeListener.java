package com.earth2me.essentials;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class TNTExplodeListener implements Listener, Runnable {
   private final transient IEssentials ess;
   private transient boolean enabled = false;
   private transient int timer = -1;

   public TNTExplodeListener(IEssentials ess) {
      this.ess = ess;
   }

   public void enable() {
      if (!this.enabled) {
         this.enabled = true;
         this.timer = this.ess.scheduleSyncDelayedTask(this, 200L);
      } else {
         if (this.timer != -1) {
            this.ess.getScheduler().cancelTask(this.timer);
            this.timer = this.ess.scheduleSyncDelayedTask(this, 200L);
         }

      }
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onEntityExplode(EntityExplodeEvent event) {
      if (this.enabled) {
         if (!(event.getEntity() instanceof LivingEntity)) {
            if (event.blockList().size() >= 1) {
               event.setCancelled(true);
               event.getLocation().getWorld().createExplosion(event.getLocation(), 0.0F);
            }
         }
      }
   }

   public void run() {
      this.enabled = false;
   }
}
