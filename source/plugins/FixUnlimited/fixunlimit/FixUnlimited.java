package fixunlimit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class FixUnlimited extends JavaPlugin implements Listener {
   public void onEnable() {
      Bukkit.getPluginManager().registerEvents(this, this);
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onInventoryClick(InventoryClickEvent e) {
      if (e.getWhoClicked() instanceof Player) {
         Player p = (Player)e.getWhoClicked();
         ItemStack is = e.getCurrentItem();
         if (is != null && is.getTypeId() != 0 && is.getAmount() <= 0) {
            e.setCancelled(true);
            e.setCurrentItem((ItemStack)null);
            p.updateInventory();
         }
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onBlockDispense(BlockDispenseEvent e) {
      ItemStack is = e.getItem();
      if (is != null && is.getTypeId() != 0 && is.getAmount() <= 0) {
         e.setItem((ItemStack)null);
         e.setCancelled(true);
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
      if (e.getRightClicked().getType().getTypeId() == 43) {
         e.setCancelled(true);
         e.getPlayer().sendMessage("§c禁止!");
      }

   }
}
