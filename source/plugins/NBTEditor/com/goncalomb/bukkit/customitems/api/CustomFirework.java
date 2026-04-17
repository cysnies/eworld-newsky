package com.goncalomb.bukkit.customitems.api;

import com.goncalomb.bukkit.UtilsMc;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.scheduler.BukkitTask;

public abstract class CustomFirework extends CustomItem {
   protected CustomFirework(String slug, String name) {
      super(slug, name, new MaterialData(Material.FIREWORK));
   }

   public final void onRightClick(PlayerInteractEvent event, PlayerDetails details) {
      if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
         details.consumeItem();
         Location loc = event.getClickedBlock().getLocation();
         this.fire(loc.add(UtilsMc.faceToDelta(event.getBlockFace())), details, (Object)null);
         event.setCancelled(true);
      }

   }

   public void onDispense(BlockDispenseEvent event, DispenserDetails details) {
      event.setCancelled(true);
   }

   protected final Firework fire(Location location, IConsumableDetails details, Object userObject) {
      final Firework firework = (Firework)location.getWorld().spawnEntity(location, EntityType.FIREWORK);
      FireworkMeta meta = firework.getFireworkMeta();
      final FireworkPlayerDetails fDetails = FireworkPlayerDetails.fromConsumableDetails(details, firework, userObject);
      this.onFire(fDetails, meta);
      firework.setFireworkMeta(meta);
      final BukkitTask[] task = new BukkitTask[1];
      task[0] = Bukkit.getScheduler().runTaskTimer(this.getPlugin(), new Runnable() {
         public void run() {
            if (firework.isDead()) {
               CustomFirework.this.onExplode(fDetails);
               task[0].cancel();
            }

            firework.setTicksLived(Integer.MAX_VALUE);
         }
      }, (long)(10 * (1 + meta.getPower())), 2L);
      return firework;
   }

   public void onFire(FireworkPlayerDetails details, FireworkMeta meta) {
   }

   public void onExplode(FireworkPlayerDetails details) {
   }
}
