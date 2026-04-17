package org.maxgamer.QuickShop.Listeners;

import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;
import org.maxgamer.QuickShop.QuickShop;
import org.maxgamer.QuickShop.Shop.Info;
import org.maxgamer.QuickShop.Shop.Shop;
import org.maxgamer.QuickShop.Shop.ShopAction;
import org.maxgamer.QuickShop.Util.MsgUtil;
import org.maxgamer.QuickShop.Util.Util;

public class PlayerListener implements Listener {
   private QuickShop plugin;

   public PlayerListener(QuickShop plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public void onClick(PlayerInteractEvent e) {
      if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
         Block b = e.getClickedBlock();
         if (Util.canBeShop(b) || b.getType() == Material.WALL_SIGN) {
            Player p = e.getPlayer();
            if (!this.plugin.sneak || p.isSneaking()) {
               Location loc = b.getLocation();
               ItemStack item = e.getItem();
               Shop shop = this.plugin.getShopManager().getShop(loc);
               if (shop == null && b.getType() == Material.WALL_SIGN) {
                  Block attached = Util.getAttached(b);
                  if (attached != null) {
                     shop = this.plugin.getShopManager().getShop(attached.getLocation());
                  }
               }

               if (shop == null || !p.hasPermission("quickshop.use") || this.plugin.sneak && !p.isSneaking()) {
                  if (shop == null && item != null && item.getType() != Material.AIR && p.hasPermission("quickshop.create.sell") && Util.canBeShop(b) && p.getGameMode() != GameMode.CREATIVE && (!this.plugin.sneak || p.isSneaking())) {
                     if (!this.plugin.getShopManager().canBuildShop(p, b, e.getBlockFace())) {
                        return;
                     }

                     if (Util.getSecondHalf(b) != null && !p.hasPermission("quickshop.create.double")) {
                        p.sendMessage(MsgUtil.getMessage("no-double-chests"));
                        return;
                     }

                     if (Util.isBlacklisted(item.getType()) && !p.hasPermission("quickshop.bypass." + item.getTypeId())) {
                        p.sendMessage(MsgUtil.getMessage("blacklisted-item"));
                        return;
                     }

                     Block last = null;
                     Location from = p.getLocation().clone();
                     from.setY((double)b.getY());
                     from.setPitch(0.0F);

                     Block n;
                     for(BlockIterator bIt = new BlockIterator(from, (double)0.0F, 7); bIt.hasNext(); last = n) {
                        n = bIt.next();
                        if (n.equals(b)) {
                           break;
                        }
                     }

                     Info info = new Info(b.getLocation(), ShopAction.CREATE, e.getItem(), last);
                     this.plugin.getShopManager().getActions().put(p.getName(), info);
                     p.sendMessage(MsgUtil.getMessage("how-much-to-trade-for", Util.getName(info.getItem())));
                  }

               } else {
                  shop.onClick();
                  MsgUtil.sendShopInfo(p, shop);
                  if (shop.isSelling()) {
                     p.sendMessage(MsgUtil.getMessage("how-many-buy"));
                  } else {
                     int items = Util.countItems(p.getInventory(), shop.getItem());
                     p.sendMessage(MsgUtil.getMessage("how-many-sell", "" + items));
                  }

                  HashMap<String, Info> actions = this.plugin.getShopManager().getActions();
                  Info info = new Info(shop.getLocation(), ShopAction.BUY, (ItemStack)null, (Block)null, shop);
                  actions.put(p.getName(), info);
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onMove(PlayerMoveEvent e) {
      if (!e.isCancelled()) {
         Info info = (Info)this.plugin.getShopManager().getActions().get(e.getPlayer().getName());
         if (info != null) {
            Player p = e.getPlayer();
            Location loc1 = info.getLocation();
            Location loc2 = p.getLocation();
            if (loc1.getWorld() != loc2.getWorld() || loc1.distanceSquared(loc2) > (double)25.0F) {
               if (info.getAction() == ShopAction.CREATE) {
                  p.sendMessage(MsgUtil.getMessage("shop-creation-cancelled"));
               } else if (info.getAction() == ShopAction.BUY) {
                  p.sendMessage(MsgUtil.getMessage("shop-purchase-cancelled"));
               }

               this.plugin.getShopManager().getActions().remove(p.getName());
               return;
            }
         }

      }
   }

   @EventHandler
   public void onTeleport(PlayerTeleportEvent e) {
      PlayerMoveEvent me = new PlayerMoveEvent(e.getPlayer(), e.getFrom(), e.getTo());
      this.onMove(me);
   }

   @EventHandler
   public void onJoin(final PlayerJoinEvent e) {
      Bukkit.getScheduler().runTaskLater(QuickShop.instance, new Runnable() {
         public void run() {
            MsgUtil.flush(e.getPlayer());
         }
      }, 60L);
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent e) {
      this.plugin.getShopManager().getActions().remove(e.getPlayer().getName());
   }

   @EventHandler
   public void onPlayerPickup(PlayerPickupItemEvent e) {
      ItemStack stack = e.getItem().getItemStack();

      try {
         if (stack.getItemMeta().getDisplayName().startsWith(ChatColor.RED + "QuickShop ")) {
            e.setCancelled(true);
         }
      } catch (NullPointerException var4) {
      }

   }
}
