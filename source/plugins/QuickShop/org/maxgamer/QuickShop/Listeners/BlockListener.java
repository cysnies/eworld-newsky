package org.maxgamer.QuickShop.Listeners;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.InventoryHolder;
import org.maxgamer.QuickShop.QuickShop;
import org.maxgamer.QuickShop.Shop.Info;
import org.maxgamer.QuickShop.Shop.Shop;
import org.maxgamer.QuickShop.Shop.ShopAction;
import org.maxgamer.QuickShop.Util.MsgUtil;
import org.maxgamer.QuickShop.Util.Util;

public class BlockListener implements Listener {
   private QuickShop plugin;

   public BlockListener(QuickShop plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public void onPlace(BlockPlaceEvent e) {
      if (!e.isCancelled()) {
         BlockState bs = e.getBlock().getState();
         if (bs instanceof DoubleChest) {
            Block b = e.getBlock();
            Player p = e.getPlayer();
            Block chest = Util.getSecondHalf(b);
            if (chest != null && this.plugin.getShopManager().getShop(chest.getLocation()) != null && !p.hasPermission("quickshop.create.double")) {
               e.setCancelled(true);
               p.sendMessage(MsgUtil.getMessage("no-double-chests"));
            }

         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBreak(BlockBreakEvent e) {
      Block b = e.getBlock();
      Player p = e.getPlayer();
      if (b.getState() instanceof InventoryHolder) {
         Shop shop = this.plugin.getShopManager().getShop(b.getLocation());
         if (shop == null) {
            return;
         }

         if (p.getGameMode() == GameMode.CREATIVE && !p.getName().equalsIgnoreCase(shop.getOwner())) {
            e.setCancelled(true);
            p.sendMessage(MsgUtil.getMessage("no-creative-break"));
            return;
         }

         Info action = (Info)this.plugin.getShopManager().getActions().get(p.getName());
         if (action != null) {
            action.setAction(ShopAction.CANCELLED);
         }

         shop.delete();
         p.sendMessage(MsgUtil.getMessage("success-removed-shop"));
      } else if (b.getType() == Material.WALL_SIGN) {
         Shop shop = this.getShopNextTo(b.getLocation());
         if (shop == null) {
            return;
         }

         if (p.getGameMode() == GameMode.CREATIVE && !p.getName().equalsIgnoreCase(shop.getOwner())) {
            e.setCancelled(true);
            p.sendMessage(MsgUtil.getMessage("no-creative-break"));
            return;
         }

         if (e.isCancelled()) {
            return;
         }

         e.setCancelled(true);
         b.setType(Material.AIR);
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onExplode(EntityExplodeEvent e) {
      if (!e.isCancelled()) {
         for(int i = 0; i < e.blockList().size(); ++i) {
            Block b = (Block)e.blockList().get(i);
            Shop shop = this.plugin.getShopManager().getShop(b.getLocation());
            if (shop != null) {
               shop.delete();
            }
         }

      }
   }

   private Shop getShopNextTo(Location loc) {
      Block b = Util.getAttached(loc.getBlock());
      return b == null ? null : this.plugin.getShopManager().getShop(b.getLocation());
   }
}
