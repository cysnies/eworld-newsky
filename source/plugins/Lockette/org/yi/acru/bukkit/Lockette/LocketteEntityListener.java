package org.yi.acru.bukkit.Lockette;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.PluginManager;

public class LocketteEntityListener implements Listener {
   private static Lockette plugin;

   public LocketteEntityListener(Lockette instance) {
      plugin = instance;
   }

   protected void registerEvents() {
      PluginManager pm = plugin.getServer().getPluginManager();
      pm.registerEvents(this, plugin);
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onEntityExplode(EntityExplodeEvent event) {
      if (!event.isCancelled()) {
         for(int x = 0; x < event.blockList().size(); ++x) {
            if (Lockette.explosionProtectionAll) {
               Block block = (Block)event.blockList().get(x);
               if (Lockette.isProtected(block)) {
                  event.blockList().remove(x);
                  --x;
               } else if (block.getTypeId() == Material.CHEST.getId() || block.getTypeId() == Material.DISPENSER.getId() || block.getTypeId() == Material.FURNACE.getId() || block.getTypeId() == Material.BURNING_FURNACE.getId() || block.getTypeId() == Material.BREWING_STAND.getId()) {
                  event.blockList().remove(x);
                  --x;
               }
            }
         }

      }
   }
}
