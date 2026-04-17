package org.maxgamer.QuickShop.Listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.maxgamer.QuickShop.QuickShop;
import org.maxgamer.QuickShop.Shop.Shop;
import org.maxgamer.QuickShop.Util.MsgUtil;
import org.maxgamer.QuickShop.Util.Util;

public class LockListener implements Listener {
   private QuickShop plugin;

   public LockListener(QuickShop plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onClick(PlayerInteractEvent e) {
      Block b = e.getClickedBlock();
      Player p = e.getPlayer();
      if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
         if (Util.canBeShop(b)) {
            Shop shop = this.plugin.getShopManager().getShop(b.getLocation());
            if (shop == null) {
               b = Util.getSecondHalf(b);
               if (b == null) {
                  return;
               }

               shop = this.plugin.getShopManager().getShop(b.getLocation());
               if (shop == null) {
                  return;
               }
            }

            if (!shop.getOwner().equalsIgnoreCase(p.getName())) {
               if (p.hasPermission("quickshop.other.open")) {
                  p.sendMessage(MsgUtil.getMessage("bypassing-lock"));
               } else {
                  p.sendMessage(MsgUtil.getMessage("that-is-locked"));
                  e.setCancelled(true);
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onPlace(BlockPlaceEvent e) {
      Block b = e.getBlock();

      try {
         if (b.getType() != Material.HOPPER) {
            return;
         }
      } catch (NoSuchFieldError var6) {
         return;
      }

      Block c = e.getBlockAgainst();
      if (Util.canBeShop(c)) {
         Player p = e.getPlayer();
         Shop shop = this.plugin.getShopManager().getShop(c.getLocation());
         if (shop == null) {
            c = Util.getSecondHalf(c);
            if (c == null) {
               return;
            }

            shop = this.plugin.getShopManager().getShop(c.getLocation());
            if (shop == null) {
               return;
            }
         }

         if (!p.getName().equalsIgnoreCase(shop.getOwner())) {
            if (p.hasPermission("quickshop.other.open")) {
               p.sendMessage(MsgUtil.getMessage("bypassing-lock"));
            } else {
               p.sendMessage(MsgUtil.getMessage("that-is-locked"));
               e.setCancelled(true);
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onBreak(BlockBreakEvent e) {
      Block b = e.getBlock();
      Player p = e.getPlayer();
      if (Util.canBeShop(b)) {
         Shop shop = this.plugin.getShopManager().getShop(b.getLocation());
         if (shop == null) {
            return;
         }

         if (!shop.getOwner().equalsIgnoreCase(p.getName()) && !p.hasPermission("quickshop.other.destroy")) {
            e.setCancelled(true);
            p.sendMessage(MsgUtil.getMessage("no-permission"));
            return;
         }
      } else if (b.getType() == Material.WALL_SIGN) {
         b = Util.getAttached(b);
         if (b == null) {
            return;
         }

         Shop shop = this.plugin.getShopManager().getShop(b.getLocation());
         if (shop == null) {
            return;
         }

         if (!shop.getOwner().equalsIgnoreCase(p.getName()) && !p.hasPermission("quickshop.other.destroy")) {
            e.setCancelled(true);
            p.sendMessage(MsgUtil.getMessage("no-permission"));
            return;
         }
      }

   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onExplode(EntityExplodeEvent e) {
      if (!e.isCancelled()) {
         for(int i = 0; i < e.blockList().size(); ++i) {
            Block b = (Block)e.blockList().get(i);
            Shop shop = this.plugin.getShopManager().getShop(b.getLocation());
            if (shop != null) {
               e.blockList().remove(b);
            }
         }

      }
   }
}
