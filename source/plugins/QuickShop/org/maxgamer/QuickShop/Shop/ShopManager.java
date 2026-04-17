package org.maxgamer.QuickShop.Shop;

import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Sign;
import org.maxgamer.QuickShop.QuickShop;
import org.maxgamer.QuickShop.Database.Database;
import org.maxgamer.QuickShop.Util.MsgUtil;
import org.maxgamer.QuickShop.Util.Util;

public class ShopManager {
   private QuickShop plugin;
   private HashMap actions = new HashMap(30);
   private HashMap shops = new HashMap(3);

   public ShopManager(QuickShop plugin) {
      this.plugin = plugin;
   }

   public Database getDatabase() {
      return this.plugin.getDB();
   }

   public HashMap getActions() {
      return this.actions;
   }

   public void createShop(Shop shop) {
      Location loc = shop.getLocation();
      ItemStack item = shop.getItem();

      try {
         String q = "INSERT INTO shops (owner, price, itemConfig, x, y, z, world, unlimited, type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
         this.plugin.getDB().execute(q, shop.getOwner(), shop.getPrice(), Util.serialize(item), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName(), shop.isUnlimited() ? 1 : 0, shop.getShopType().toID());
         this.addShop(loc.getWorld().getName(), shop);
      } catch (Exception e) {
         e.printStackTrace();
         System.out.println("Could not create shop! Changes will revert after a reboot!");
      }

   }

   public void loadShop(String world, Shop shop) {
      this.addShop(world, shop);
   }

   public HashMap getShops() {
      return this.shops;
   }

   public HashMap getShops(String world) {
      return (HashMap)this.shops.get(world);
   }

   public HashMap getShops(Chunk c) {
      HashMap<Location, Shop> shops = this.getShops(c.getWorld().getName(), c.getX(), c.getZ());
      return shops;
   }

   public HashMap getShops(String world, int chunkX, int chunkZ) {
      HashMap<ShopChunk, HashMap<Location, Shop>> inWorld = this.getShops(world);
      if (inWorld == null) {
         return null;
      } else {
         ShopChunk shopChunk = new ShopChunk(world, chunkX, chunkZ);
         return (HashMap)inWorld.get(shopChunk);
      }
   }

   public Shop getShop(Location loc) {
      HashMap<Location, Shop> inChunk = this.getShops(loc.getChunk());
      return inChunk == null ? null : (Shop)inChunk.get(loc);
   }

   private void addShop(String world, Shop shop) {
      HashMap<ShopChunk, HashMap<Location, Shop>> inWorld = (HashMap)this.getShops().get(world);
      if (inWorld == null) {
         inWorld = new HashMap(3);
         this.getShops().put(world, inWorld);
      }

      int x = (int)Math.floor((double)shop.getLocation().getBlockX() / (double)16.0F);
      int z = (int)Math.floor((double)shop.getLocation().getBlockZ() / (double)16.0F);
      ShopChunk shopChunk = new ShopChunk(world, x, z);
      HashMap<Location, Shop> inChunk = (HashMap)inWorld.get(shopChunk);
      if (inChunk == null) {
         inChunk = new HashMap(1);
         inWorld.put(shopChunk, inChunk);
      }

      inChunk.put(shop.getLocation(), shop);
   }

   public void removeShop(Shop shop) {
      Location loc = shop.getLocation();
      String world = loc.getWorld().getName();
      HashMap<ShopChunk, HashMap<Location, Shop>> inWorld = (HashMap)this.getShops().get(world);
      int x = (int)Math.floor((double)shop.getLocation().getBlockX() / (double)16.0F);
      int z = (int)Math.floor((double)shop.getLocation().getBlockZ() / (double)16.0F);
      ShopChunk shopChunk = new ShopChunk(world, x, z);
      HashMap<Location, Shop> inChunk = (HashMap)inWorld.get(shopChunk);
      inChunk.remove(loc);
   }

   public void clear() {
      if (this.plugin.display) {
         for(World world : Bukkit.getWorlds()) {
            Chunk[] var6;
            for(Chunk chunk : var6 = world.getLoadedChunks()) {
               HashMap<Location, Shop> inChunk = this.getShops(chunk);
               if (inChunk != null) {
                  for(Shop shop : inChunk.values()) {
                     shop.onUnload();
                  }
               }
            }
         }
      }

      this.actions.clear();
      this.shops.clear();
   }

   public boolean canBuildShop(Player p, Block b, BlockFace bf) {
      if (this.plugin.limit) {
         int owned = 0;
         Iterator<Shop> it = this.getShopIterator();

         while(it.hasNext()) {
            if (((Shop)it.next()).getOwner().equalsIgnoreCase(p.getName())) {
               ++owned;
            }
         }

         int max = this.plugin.getShopLimit(p);
         if (owned + 1 > max) {
            p.sendMessage(ChatColor.RED + "You have already created a maximum of " + owned + "/" + max + " shops!");
            return false;
         }
      }

      PlayerInteractEvent pie = new PlayerInteractEvent(p, Action.RIGHT_CLICK_BLOCK, new ItemStack(Material.AIR), b, bf);
      Bukkit.getPluginManager().callEvent(pie);
      pie.getPlayer().closeInventory();
      if (pie.isCancelled()) {
         return false;
      } else {
         ShopPreCreateEvent spce = new ShopPreCreateEvent(p, b.getLocation());
         Bukkit.getPluginManager().callEvent(spce);
         return !spce.isCancelled();
      }
   }

   public void handleChat(final Player p, String msg) {
      final String message = ChatColor.stripColor(msg);
      Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
         public void run() {
            HashMap<String, Info> actions = ShopManager.this.getActions();
            Info info = (Info)actions.remove(p.getName());
            if (info != null) {
               if (info.getLocation().getWorld() != p.getLocation().getWorld()) {
                  p.sendMessage(MsgUtil.getMessage("shop-creation-cancelled"));
               } else if (info.getLocation().distanceSquared(p.getLocation()) > (double)25.0F) {
                  p.sendMessage(MsgUtil.getMessage("shop-creation-cancelled"));
               } else {
                  if (info.getAction() == ShopAction.CREATE) {
                     try {
                        if (ShopManager.this.plugin.getShopManager().getShop(info.getLocation()) != null) {
                           p.sendMessage(MsgUtil.getMessage("shop-already-owned"));
                           return;
                        }

                        if (Util.getSecondHalf(info.getLocation().getBlock()) != null && !p.hasPermission("quickshop.create.double")) {
                           p.sendMessage(MsgUtil.getMessage("no-double-chests"));
                           return;
                        }

                        if (!Util.canBeShop(info.getLocation().getBlock())) {
                           p.sendMessage(MsgUtil.getMessage("chest-was-removed"));
                           return;
                        }

                        double price;
                        if (ShopManager.this.plugin.getConfig().getBoolean("whole-number-prices-only")) {
                           price = (double)Integer.parseInt(message);
                        } else {
                           price = Double.parseDouble(message);
                        }

                        if (price < 0.01) {
                           p.sendMessage(MsgUtil.getMessage("price-too-cheap"));
                           return;
                        }

                        double tax = ShopManager.this.plugin.getConfig().getDouble("shop.cost");
                        if (tax != (double)0.0F && ShopManager.this.plugin.getEcon().getBalance(p.getName()) < tax) {
                           p.sendMessage(MsgUtil.getMessage("you-cant-afford-a-new-shop", ShopManager.this.format(tax)));
                           return;
                        }

                        try {
                           Chest c = (Chest)info.getLocation().getBlock().getState();

                           for(HumanEntity he : c.getInventory().getViewers()) {
                              he.closeInventory();
                           }
                        } catch (Exception var14) {
                        }

                        Shop shop = new ContainerShop(info.getLocation(), price, info.getItem(), p.getName());
                        shop.onLoad();
                        ShopCreateEvent e = new ShopCreateEvent(shop, p);
                        Bukkit.getPluginManager().callEvent(e);
                        if (e.isCancelled()) {
                           shop.onUnload();
                           return;
                        }

                        if (tax != (double)0.0F) {
                           if (!ShopManager.this.plugin.getEcon().withdraw(p.getName(), tax)) {
                              p.sendMessage(MsgUtil.getMessage("you-cant-afford-a-new-shop", ShopManager.this.format(tax)));
                              shop.onUnload();
                              return;
                           }

                           ShopManager.this.plugin.getEcon().deposit(ShopManager.this.plugin.getConfig().getString("tax-account"), tax);
                        }

                        ShopManager.this.createShop(shop);
                        Location loc = shop.getLocation();
                        ShopManager.this.plugin.log(p.getName() + " created a " + shop.getDataName() + " shop at (" + loc.getWorld().getName() + " - " + loc.getX() + "," + loc.getY() + "," + loc.getZ() + ")");
                        if (!ShopManager.this.plugin.getConfig().getBoolean("shop.lock") && !ShopManager.this.plugin.warnings.contains(p.getName())) {
                           p.sendMessage(MsgUtil.getMessage("shops-arent-locked"));
                           ShopManager.this.plugin.warnings.add(p.getName());
                        }

                        if (info.getSignBlock() != null && info.getSignBlock().getType() == Material.AIR && ShopManager.this.plugin.getConfig().getBoolean("shop.auto-sign")) {
                           BlockState bs = info.getSignBlock().getState();
                           BlockFace bf = info.getLocation().getBlock().getFace(info.getSignBlock());
                           bs.setType(Material.WALL_SIGN);
                           Sign sign = (Sign)bs.getData();
                           sign.setFacingDirection(bf);
                           bs.update(true);
                           shop.setSignText();
                        }

                        if (shop instanceof ContainerShop) {
                           ContainerShop cs = (ContainerShop)shop;
                           if (cs.isDoubleShop()) {
                              Shop nextTo = cs.getAttachedShop();
                              if (nextTo.getPrice() > shop.getPrice()) {
                                 p.sendMessage(MsgUtil.getMessage("buying-more-than-selling"));
                              }
                           }
                        }
                     } catch (NumberFormatException var15) {
                        p.sendMessage(MsgUtil.getMessage("shop-creation-cancelled"));
                        return;
                     }
                  } else {
                     if (info.getAction() != ShopAction.BUY) {
                        return;
                     }

                     int amount = 0;

                     try {
                        amount = Integer.parseInt(message);
                     } catch (NumberFormatException var13) {
                        p.sendMessage(MsgUtil.getMessage("shop-purchase-cancelled"));
                        return;
                     }

                     Shop shop = ShopManager.this.plugin.getShopManager().getShop(info.getLocation());
                     if (shop == null || !Util.canBeShop(info.getLocation().getBlock())) {
                        p.sendMessage(MsgUtil.getMessage("chest-was-removed"));
                        return;
                     }

                     if (info.hasChanged(shop)) {
                        p.sendMessage(MsgUtil.getMessage("shop-has-changed"));
                        return;
                     }

                     if (shop.isSelling()) {
                        int stock = shop.getRemainingStock();
                        if (stock < amount) {
                           p.sendMessage(MsgUtil.getMessage("shop-stock-too-low", "" + shop.getRemainingStock(), shop.getDataName()));
                           return;
                        }

                        if (amount == 0) {
                           MsgUtil.sendPurchaseSuccess(p, shop, amount);
                           return;
                        }

                        if (amount < 0) {
                           p.sendMessage(MsgUtil.getMessage("negative-amount"));
                           return;
                        }

                        int pSpace = Util.countSpace(p.getInventory(), shop.getItem());
                        if (amount > pSpace) {
                           p.sendMessage(MsgUtil.getMessage("not-enough-space", "" + pSpace));
                           return;
                        }

                        ShopPurchaseEvent e = new ShopPurchaseEvent(shop, p, amount);
                        Bukkit.getPluginManager().callEvent(e);
                        if (e.isCancelled()) {
                           return;
                        }

                        if (!p.getName().equalsIgnoreCase(shop.getOwner())) {
                           if (ShopManager.this.plugin.getEcon().getBalance(p.getName()) < (double)amount * shop.getPrice()) {
                              p.sendMessage(MsgUtil.getMessage("you-cant-afford-to-buy", ShopManager.this.format((double)amount * shop.getPrice()), ShopManager.this.format(ShopManager.this.plugin.getEcon().getBalance(p.getName()))));
                              return;
                           }

                           double tax = ShopManager.this.plugin.getConfig().getDouble("tax");
                           double total = (double)amount * shop.getPrice();
                           if (!ShopManager.this.plugin.getEcon().withdraw(p.getName(), total)) {
                              p.sendMessage(MsgUtil.getMessage("you-cant-afford-to-buy", ShopManager.this.format((double)amount * shop.getPrice()), ShopManager.this.format(ShopManager.this.plugin.getEcon().getBalance(p.getName()))));
                              return;
                           }

                           if (!shop.isUnlimited() || ShopManager.this.plugin.getConfig().getBoolean("shop.pay-unlimited-shop-owners")) {
                              ShopManager.this.plugin.getEcon().deposit(shop.getOwner(), total * ((double)1.0F - tax));
                              if (tax != (double)0.0F) {
                                 ShopManager.this.plugin.getEcon().deposit(ShopManager.this.plugin.getConfig().getString("tax-account"), total * tax);
                              }
                           }

                           if (ShopManager.this.plugin.getConfig().getBoolean("show-tax")) {
                              String msg = MsgUtil.getMessage("player-bought-from-your-store-tax", p.getName(), "" + amount, shop.getDataName(), Util.format(tax * total));
                              if (stock == amount) {
                                 msg = msg + "\n" + MsgUtil.getMessage("shop-out-of-stock", "" + shop.getLocation().getBlockX(), "" + shop.getLocation().getBlockY(), "" + shop.getLocation().getBlockZ(), shop.getDataName());
                              }

                              MsgUtil.send(shop.getOwner(), msg);
                           } else {
                              String msg = MsgUtil.getMessage("player-bought-from-your-store", p.getName(), "" + amount, shop.getDataName());
                              if (stock == amount) {
                                 msg = msg + "\n" + MsgUtil.getMessage("shop-out-of-stock", "" + shop.getLocation().getBlockX(), "" + shop.getLocation().getBlockY(), "" + shop.getLocation().getBlockZ(), shop.getDataName());
                              }

                              MsgUtil.send(shop.getOwner(), msg);
                           }
                        }

                        shop.sell(p, amount);
                        MsgUtil.sendPurchaseSuccess(p, shop, amount);
                        ShopManager.this.plugin.log(p.getName() + " bought " + amount + " for " + shop.getPrice() * (double)amount + " from " + shop.toString());
                     } else if (shop.isBuying()) {
                        int space = shop.getRemainingSpace();
                        if (space < amount) {
                           p.sendMessage(MsgUtil.getMessage("shop-has-no-space", "" + space, shop.getDataName()));
                           return;
                        }

                        int count = Util.countItems(p.getInventory(), shop.getItem());
                        if (amount > count) {
                           p.sendMessage(MsgUtil.getMessage("you-dont-have-that-many-items", "" + count, shop.getDataName()));
                           return;
                        }

                        if (amount == 0) {
                           MsgUtil.sendPurchaseSuccess(p, shop, amount);
                           return;
                        }

                        if (amount < 0) {
                           p.sendMessage(MsgUtil.getMessage("negative-amount"));
                           return;
                        }

                        if (!p.getName().equalsIgnoreCase(shop.getOwner())) {
                           double tax = ShopManager.this.plugin.getConfig().getDouble("tax");
                           double total = (double)amount * shop.getPrice();
                           if (!shop.isUnlimited() || ShopManager.this.plugin.getConfig().getBoolean("shop.pay-unlimited-shop-owners")) {
                              if (ShopManager.this.plugin.getEcon().getBalance(shop.getOwner()) < (double)amount * shop.getPrice()) {
                                 p.sendMessage(MsgUtil.getMessage("the-owner-cant-afford-to-buy-from-you", ShopManager.this.format((double)amount * shop.getPrice()), ShopManager.this.format(ShopManager.this.plugin.getEcon().getBalance(shop.getOwner()))));
                                 return;
                              }

                              if (!ShopManager.this.plugin.getEcon().withdraw(shop.getOwner(), total)) {
                                 p.sendMessage(MsgUtil.getMessage("the-owner-cant-afford-to-buy-from-you", ShopManager.this.format((double)amount * shop.getPrice()), ShopManager.this.format(ShopManager.this.plugin.getEcon().getBalance(shop.getOwner()))));
                                 return;
                              }

                              if (tax != (double)0.0F) {
                                 ShopManager.this.plugin.getEcon().deposit(ShopManager.this.plugin.getConfig().getString("tax-account"), total * tax);
                              }
                           }

                           ShopManager.this.plugin.getEcon().deposit(p.getName(), total * ((double)1.0F - tax));
                           String msg = MsgUtil.getMessage("player-sold-to-your-store", p.getName(), "" + amount, shop.getDataName());
                           if (space == amount) {
                              msg = msg + "\n" + MsgUtil.getMessage("shop-out-of-space", "" + shop.getLocation().getBlockX(), "" + shop.getLocation().getBlockY(), "" + shop.getLocation().getBlockZ());
                           }

                           MsgUtil.send(shop.getOwner(), msg);
                        }

                        shop.buy(p, amount);
                        MsgUtil.sendSellSuccess(p, shop, amount);
                        ShopManager.this.plugin.log(p.getName() + " sold " + amount + " for " + shop.getPrice() * (double)amount + " to " + shop.toString());
                     }

                     shop.setSignText();
                  }

               }
            }
         }
      });
   }

   public Iterator getShopIterator() {
      return new ShopIterator();
   }

   public String format(double d) {
      return this.plugin.getEcon().format(d);
   }

   public class ShopIterator implements Iterator {
      private Iterator shops;
      private Iterator chunks;
      private Iterator worlds = ShopManager.this.getShops().values().iterator();
      private Shop current;

      public boolean hasNext() {
         if (this.shops != null && this.shops.hasNext()) {
            return true;
         } else if (this.chunks != null && this.chunks.hasNext()) {
            this.shops = ((HashMap)this.chunks.next()).values().iterator();
            return this.hasNext();
         } else if (!this.worlds.hasNext()) {
            return false;
         } else {
            this.chunks = ((HashMap)this.worlds.next()).values().iterator();
            return this.hasNext();
         }
      }

      public Shop next() {
         if (this.shops == null || !this.shops.hasNext()) {
            if (this.chunks == null || !this.chunks.hasNext()) {
               if (!this.worlds.hasNext()) {
                  throw new NoSuchElementException("No more shops to iterate over!");
               }

               this.chunks = ((HashMap)this.worlds.next()).values().iterator();
            }

            this.shops = ((HashMap)this.chunks.next()).values().iterator();
         }

         if (!this.shops.hasNext()) {
            return this.next();
         } else {
            this.current = (Shop)this.shops.next();
            return this.current;
         }
      }

      public void remove() {
         this.current.delete(false);
         this.shops.remove();
      }
   }
}
