package org.yi.acru.bukkit.Lockette;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.plugin.PluginManager;

public class LocketteWorldListener implements Listener {
   private static Lockette plugin;

   public LocketteWorldListener(Lockette instance) {
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
   public void onStructureGrow(StructureGrowEvent event) {
      if (!event.isCancelled()) {
         List<BlockState> blockList = event.getBlocks();
         int count = blockList.size();

         for(int x = 0; x < count; ++x) {
            Block block = ((BlockState)blockList.get(x)).getBlock();
            if (Lockette.isProtected(block)) {
               event.setCancelled(true);
               return;
            }

            if (Lockette.explosionProtectionAll && (block.getTypeId() == Material.CHEST.getId() || block.getTypeId() == Material.DISPENSER.getId() || block.getTypeId() == Material.FURNACE.getId() || block.getTypeId() == Material.BURNING_FURNACE.getId() || block.getTypeId() == Material.BREWING_STAND.getId())) {
               event.setCancelled(true);
               return;
            }
         }

      }
   }
}
