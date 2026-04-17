package com.earth2me.essentials;

import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class EssentialsBlockListener implements Listener {
   private final transient IEssentials ess;

   public EssentialsBlockListener(IEssentials ess) {
      this.ess = ess;
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onBlockPlace(BlockPlaceEvent event) {
      final ItemStack is = Util.convertBlockToItem(event.getBlockPlaced());
      if (is != null) {
         final User user = this.ess.getUser(event.getPlayer());
         if (user.hasUnlimited(is) && user.getGameMode() == GameMode.SURVIVAL) {
            this.ess.scheduleSyncDelayedTask(new Runnable() {
               public void run() {
                  user.getInventory().addItem(new ItemStack[]{is});
                  user.updateInventory();
               }
            });
         }

      }
   }
}
