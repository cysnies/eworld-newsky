package org.maxgamer.QuickShop.Command;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.BlockIterator;
import org.maxgamer.QuickShop.QuickShop;
import org.maxgamer.QuickShop.Database.Database;
import org.maxgamer.QuickShop.Database.MySQLCore;
import org.maxgamer.QuickShop.Database.SQLiteCore;
import org.maxgamer.QuickShop.Shop.ContainerShop;
import org.maxgamer.QuickShop.Shop.Shop;
import org.maxgamer.QuickShop.Shop.ShopChunk;
import org.maxgamer.QuickShop.Shop.ShopType;
import org.maxgamer.QuickShop.Util.MsgUtil;

public class QS implements CommandExecutor {
   QuickShop plugin;

   public QS(QuickShop plugin) {
      this.plugin = plugin;
   }

   private void setUnlimited(CommandSender sender) {
      if (sender instanceof Player && sender.hasPermission("quickshop.unlimited")) {
         BlockIterator bIt = new BlockIterator((Player)sender, 10);

         while(bIt.hasNext()) {
            Block b = bIt.next();
            Shop shop = this.plugin.getShopManager().getShop(b.getLocation());
            if (shop != null) {
               shop.setUnlimited(!shop.isUnlimited());
               shop.update();
               sender.sendMessage(MsgUtil.getMessage("command.toggle-unlimited", shop.isUnlimited() ? "unlimited" : "limited"));
               return;
            }
         }

         sender.sendMessage(MsgUtil.getMessage("not-looking-at-shop"));
      } else {
         sender.sendMessage(MsgUtil.getMessage("no-permission"));
      }
   }

   private void remove(CommandSender sender, String[] args) {
      if (!(sender instanceof Player)) {
         sender.sendMessage(ChatColor.RED + "Only players may use that command.");
      } else if (!sender.hasPermission("quickshop.delete")) {
         sender.sendMessage(ChatColor.RED + "You do not have permission to use that command. Try break the shop instead?");
      } else {
         Player p = (Player)sender;
         BlockIterator bIt = new BlockIterator(p, 10);

         while(bIt.hasNext()) {
            Block b = bIt.next();
            Shop shop = this.plugin.getShopManager().getShop(b.getLocation());
            if (shop != null) {
               if (shop.getOwner().equalsIgnoreCase(p.getName())) {
                  shop.delete();
                  sender.sendMessage(ChatColor.GREEN + "Success. Deleted shop.");
               } else {
                  p.sendMessage(ChatColor.RED + "That's not your shop!");
               }

               return;
            }
         }

         p.sendMessage(ChatColor.RED + "No shop found!");
      }
   }

   private void export(CommandSender sender, String[] args) {
      if (args.length < 2) {
         sender.sendMessage(ChatColor.RED + "Usage: /qs export mysql|sqlite");
      } else {
         String type = args[1].toLowerCase();
         if (type.startsWith("mysql")) {
            if (this.plugin.getDB().getCore() instanceof MySQLCore) {
               sender.sendMessage(ChatColor.RED + "Database is already MySQL");
            } else {
               ConfigurationSection cfg = this.plugin.getConfig().getConfigurationSection("database");
               String host = cfg.getString("host");
               String port = cfg.getString("port");
               String user = cfg.getString("user");
               String pass = cfg.getString("password");
               String name = cfg.getString("database");
               MySQLCore core = new MySQLCore(host, user, pass, name, port);

               try {
                  Database target = new Database(core);
                  QuickShop.instance.getDB().copyTo(target);
                  sender.sendMessage(ChatColor.GREEN + "Success - Exported to MySQL " + user + "@" + host + "." + name);
               } catch (Exception e) {
                  e.printStackTrace();
                  sender.sendMessage(ChatColor.RED + "Failed to export to MySQL " + user + "@" + host + "." + name + ChatColor.DARK_RED + " Reason: " + e.getMessage());
               }

            }
         } else if (!type.startsWith("sql") && !type.contains("file")) {
            sender.sendMessage(ChatColor.RED + "No target given. Usage: /qs export mysql|sqlite");
         } else if (this.plugin.getDB().getCore() instanceof SQLiteCore) {
            sender.sendMessage(ChatColor.RED + "Database is already SQLite");
         } else {
            File file = new File(this.plugin.getDataFolder(), "shops.db");
            if (file.exists() && !file.delete()) {
               sender.sendMessage(ChatColor.RED + "Warning: Failed to delete old shops.db file. This may cause errors.");
            }

            SQLiteCore core = new SQLiteCore(file);

            try {
               Database target = new Database(core);
               QuickShop.instance.getDB().copyTo(target);
               sender.sendMessage(ChatColor.GREEN + "Success - Exported to SQLite: " + file.toString());
            } catch (Exception e) {
               e.printStackTrace();
               sender.sendMessage(ChatColor.RED + "Failed to export to SQLite: " + file.toString() + " Reason: " + e.getMessage());
            }

         }
      }
   }

   private void setOwner(CommandSender sender, String[] args) {
      if (sender instanceof Player && sender.hasPermission("quickshop.setowner")) {
         if (args.length < 2) {
            sender.sendMessage(MsgUtil.getMessage("command.no-owner-given"));
         } else {
            BlockIterator bIt = new BlockIterator((Player)sender, 10);

            while(bIt.hasNext()) {
               Block b = bIt.next();
               Shop shop = this.plugin.getShopManager().getShop(b.getLocation());
               if (shop != null) {
                  shop.setOwner(args[1]);
                  shop.update();
                  sender.sendMessage(MsgUtil.getMessage("command.new-owner", shop.getOwner()));
                  return;
               }
            }

            sender.sendMessage(MsgUtil.getMessage("not-looking-at-shop"));
         }
      } else {
         sender.sendMessage(MsgUtil.getMessage("no-permission"));
      }
   }

   private void refill(CommandSender sender, String[] args) {
      if (sender instanceof Player && sender.hasPermission("quickshop.refill")) {
         if (args.length < 2) {
            sender.sendMessage(MsgUtil.getMessage("command.no-amount-given"));
         } else {
            int add;
            try {
               add = Integer.parseInt(args[1]);
            } catch (NumberFormatException var7) {
               sender.sendMessage(MsgUtil.getMessage("thats-not-a-number"));
               return;
            }

            BlockIterator bIt = new BlockIterator((Player)sender, 10);

            while(bIt.hasNext()) {
               Block b = bIt.next();
               Shop shop = this.plugin.getShopManager().getShop(b.getLocation());
               if (shop != null) {
                  shop.add(shop.getItem(), add);
                  sender.sendMessage(MsgUtil.getMessage("refill-success"));
                  return;
               }
            }

            sender.sendMessage(MsgUtil.getMessage("not-looking-at-shop"));
         }
      } else {
         sender.sendMessage(MsgUtil.getMessage("no-permission"));
      }
   }

   private void empty(CommandSender sender, String[] args) {
      if (sender instanceof Player && sender.hasPermission("quickshop.refill")) {
         BlockIterator bIt = new BlockIterator((Player)sender, 10);

         while(bIt.hasNext()) {
            Block b = bIt.next();
            Shop shop = this.plugin.getShopManager().getShop(b.getLocation());
            if (shop != null) {
               if (shop instanceof ContainerShop) {
                  ContainerShop cs = (ContainerShop)shop;
                  cs.getInventory().clear();
                  sender.sendMessage(MsgUtil.getMessage("empty-success"));
                  return;
               }

               sender.sendMessage(MsgUtil.getMessage("not-looking-at-shop"));
               return;
            }
         }

         sender.sendMessage(MsgUtil.getMessage("not-looking-at-shop"));
      } else {
         sender.sendMessage(MsgUtil.getMessage("no-permission"));
      }
   }

   private void find(CommandSender sender, String[] args) {
      if (sender instanceof Player && sender.hasPermission("quickshop.find")) {
         if (args.length < 2) {
            sender.sendMessage(MsgUtil.getMessage("command.no-type-given"));
         } else {
            StringBuilder sb = new StringBuilder(args[1]);

            for(int i = 2; i < args.length; ++i) {
               sb.append(" " + args[i]);
            }

            String lookFor = sb.toString();
            lookFor = lookFor.toLowerCase();
            Player p = (Player)sender;
            Location loc = p.getEyeLocation().clone();
            double minDistance = (double)this.plugin.getConfig().getInt("shop.find-distance");
            double minDistanceSquared = minDistance * minDistance;
            int chunkRadius = (int)minDistance / 16 + 1;
            Shop closest = null;
            Chunk c = loc.getChunk();

            for(int x = -chunkRadius + c.getX(); x < chunkRadius + c.getX(); ++x) {
               for(int z = -chunkRadius + c.getZ(); z < chunkRadius + c.getZ(); ++z) {
                  Chunk d = c.getWorld().getChunkAt(x, z);
                  HashMap<Location, Shop> inChunk = this.plugin.getShopManager().getShops(d);
                  if (inChunk != null) {
                     for(Shop shop : inChunk.values()) {
                        if (shop.getDataName().toLowerCase().contains(lookFor) && shop.getLocation().distanceSquared(loc) < minDistanceSquared) {
                           closest = shop;
                           minDistanceSquared = shop.getLocation().distanceSquared(loc);
                        }
                     }
                  }
               }
            }

            if (closest == null) {
               sender.sendMessage(MsgUtil.getMessage("no-nearby-shop", args[1]));
            } else {
               Location lookat = closest.getLocation().clone().add((double)0.5F, (double)0.5F, (double)0.5F);
               p.teleport(this.lookAt(loc, lookat).add((double)0.0F, -1.62, (double)0.0F), TeleportCause.UNKNOWN);
               p.sendMessage(MsgUtil.getMessage("nearby-shop-this-way", "" + (int)Math.floor(Math.sqrt(minDistanceSquared))));
            }
         }
      } else {
         sender.sendMessage(MsgUtil.getMessage("no-permission"));
      }
   }

   private void setBuy(CommandSender sender) {
      if (sender instanceof Player && sender.hasPermission("quickshop.create.buy")) {
         BlockIterator bIt = new BlockIterator((Player)sender, 10);

         while(bIt.hasNext()) {
            Block b = bIt.next();
            Shop shop = this.plugin.getShopManager().getShop(b.getLocation());
            if (shop != null && shop.getOwner().equalsIgnoreCase(((Player)sender).getName())) {
               shop.setShopType(ShopType.BUYING);
               shop.setSignText();
               shop.update();
               sender.sendMessage(MsgUtil.getMessage("command.now-buying", shop.getDataName()));
               return;
            }
         }

         sender.sendMessage(MsgUtil.getMessage("not-looking-at-shop"));
      } else {
         sender.sendMessage(MsgUtil.getMessage("no-permission"));
      }
   }

   private void setSell(CommandSender sender) {
      if (sender instanceof Player && sender.hasPermission("quickshop.create.sell")) {
         BlockIterator bIt = new BlockIterator((Player)sender, 10);

         while(bIt.hasNext()) {
            Block b = bIt.next();
            Shop shop = this.plugin.getShopManager().getShop(b.getLocation());
            if (shop != null && shop.getOwner().equalsIgnoreCase(((Player)sender).getName())) {
               shop.setShopType(ShopType.SELLING);
               shop.setSignText();
               shop.update();
               sender.sendMessage(MsgUtil.getMessage("command.now-selling", shop.getDataName()));
               return;
            }
         }

         sender.sendMessage(MsgUtil.getMessage("not-looking-at-shop"));
      } else {
         sender.sendMessage(MsgUtil.getMessage("no-permission"));
      }
   }

   private void setPrice(CommandSender sender, String[] args) {
      if (sender instanceof Player && sender.hasPermission("quickshop.create.changeprice")) {
         if (args.length < 2) {
            sender.sendMessage(MsgUtil.getMessage("no-price-given"));
         } else {
            double price;
            try {
               price = Double.parseDouble(args[1]);
            } catch (NumberFormatException var12) {
               sender.sendMessage(MsgUtil.getMessage("thats-not-a-number"));
               return;
            }

            if (price < 0.01) {
               sender.sendMessage(MsgUtil.getMessage("price-too-cheap"));
            } else {
               double fee = (double)0.0F;
               if (this.plugin.priceChangeRequiresFee) {
                  fee = this.plugin.getConfig().getDouble("shop.fee-for-price-change");
                  if (fee > (double)0.0F && this.plugin.getEcon().getBalance(sender.getName()) < fee) {
                     sender.sendMessage(MsgUtil.getMessage("you-cant-afford-to-change-price", this.plugin.getEcon().format(fee)));
                     return;
                  }
               }

               BlockIterator bIt = new BlockIterator((Player)sender, 10);

               while(bIt.hasNext()) {
                  Block b = bIt.next();
                  Shop shop = this.plugin.getShopManager().getShop(b.getLocation());
                  if (shop != null && (shop.getOwner().equalsIgnoreCase(((Player)sender).getName()) || sender.hasPermission("quickshop.other.price"))) {
                     if (shop.getPrice() == price) {
                        sender.sendMessage(MsgUtil.getMessage("no-price-change"));
                        return;
                     } else {
                        if (fee > (double)0.0F) {
                           if (!this.plugin.getEcon().withdraw(sender.getName(), fee)) {
                              sender.sendMessage(MsgUtil.getMessage("you-cant-afford-to-change-price", this.plugin.getEcon().format(fee)));
                              return;
                           }

                           sender.sendMessage(MsgUtil.getMessage("fee-charged-for-price-change", this.plugin.getEcon().format(fee)));
                           this.plugin.getEcon().deposit(this.plugin.getConfig().getString("tax-account"), fee);
                        }

                        shop.setPrice(price);
                        shop.setSignText();
                        shop.update();
                        sender.sendMessage(MsgUtil.getMessage("price-is-now", this.plugin.getEcon().format(shop.getPrice())));
                        if (shop instanceof ContainerShop) {
                           ContainerShop cs = (ContainerShop)shop;
                           if (cs.isDoubleShop()) {
                              Shop nextTo = cs.getAttachedShop();
                              if (cs.isSelling()) {
                                 if (cs.getPrice() < nextTo.getPrice()) {
                                    sender.sendMessage(MsgUtil.getMessage("buying-more-than-selling"));
                                 }
                              } else if (cs.getPrice() > nextTo.getPrice()) {
                                 sender.sendMessage(MsgUtil.getMessage("buying-more-than-selling"));
                              }
                           }
                        }

                        return;
                     }
                  }
               }

               sender.sendMessage(MsgUtil.getMessage("not-looking-at-shop"));
            }
         }
      } else {
         sender.sendMessage(MsgUtil.getMessage("no-permission"));
      }
   }

   private void clean(CommandSender sender) {
      if (sender.hasPermission("quickshop.clean")) {
         sender.sendMessage(MsgUtil.getMessage("command.cleaning"));
         Iterator<Shop> shIt = this.plugin.getShopManager().getShopIterator();
         int i = 0;

         while(shIt.hasNext()) {
            Shop shop = (Shop)shIt.next();
            if (shop.getLocation().getWorld() != null && shop.isSelling() && shop.getRemainingStock() == 0 && shop instanceof ContainerShop) {
               ContainerShop cs = (ContainerShop)shop;
               if (!cs.isDoubleShop()) {
                  shIt.remove();
                  ++i;
               }
            }
         }

         MsgUtil.clean();
         sender.sendMessage(MsgUtil.getMessage("command.cleaned", "" + i));
      } else {
         sender.sendMessage(MsgUtil.getMessage("no-permission"));
      }
   }

   private void reload(CommandSender sender) {
      if (sender.hasPermission("quickshop.reload")) {
         sender.sendMessage(MsgUtil.getMessage("command.reloading"));
         Bukkit.getPluginManager().disablePlugin(this.plugin);
         Bukkit.getPluginManager().enablePlugin(this.plugin);
      } else {
         sender.sendMessage(MsgUtil.getMessage("no-permission"));
      }
   }

   public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
      if (args.length <= 0) {
         this.sendHelp(sender);
         return true;
      } else {
         String subArg = args[0].toLowerCase();
         if (subArg.equals("unlimited")) {
            this.setUnlimited(sender);
            return true;
         } else if (subArg.equals("setowner")) {
            this.setOwner(sender, args);
            return true;
         } else if (subArg.equals("find")) {
            this.find(sender, args);
            return true;
         } else if (subArg.startsWith("buy")) {
            this.setBuy(sender);
            return true;
         } else if (subArg.startsWith("sell")) {
            this.setSell(sender);
            return true;
         } else if (subArg.startsWith("price")) {
            this.setPrice(sender, args);
            return true;
         } else {
            if (subArg.equals("remove")) {
               this.remove(sender, args);
            } else {
               if (subArg.equals("refill")) {
                  this.refill(sender, args);
                  return true;
               }

               if (subArg.equals("empty")) {
                  this.empty(sender, args);
                  return true;
               }

               if (subArg.equals("clean")) {
                  this.clean(sender);
                  return true;
               }

               if (subArg.equals("reload")) {
                  this.reload(sender);
                  return true;
               }

               if (subArg.equals("export")) {
                  this.export(sender, args);
                  return true;
               }

               if (subArg.equals("info")) {
                  if (!sender.hasPermission("quickshop.info")) {
                     sender.sendMessage(MsgUtil.getMessage("no-permission"));
                     return true;
                  }

                  int worlds = 0;
                  int chunks = 0;
                  int doubles = 0;
                  int selling = 0;
                  int buying = 0;
                  int nostock = 0;

                  for(HashMap inWorld : this.plugin.getShopManager().getShops().values()) {
                     ++worlds;

                     for(HashMap inChunk : inWorld.values()) {
                        ++chunks;

                        for(Shop shop : inChunk.values()) {
                           if (shop.isBuying()) {
                              ++buying;
                           } else if (shop.isSelling()) {
                              ++selling;
                           }

                           if (shop instanceof ContainerShop && ((ContainerShop)shop).isDoubleShop()) {
                              ++doubles;
                           } else if (shop.isSelling() && shop.getRemainingStock() == 0) {
                              ++nostock;
                           }
                        }
                     }
                  }

                  sender.sendMessage(ChatColor.RED + "QuickShop Statistics...");
                  sender.sendMessage("" + ChatColor.GREEN + (buying + selling) + " shops in " + chunks + " chunks spread over " + worlds + " worlds.");
                  sender.sendMessage("" + ChatColor.GREEN + doubles + " double shops. ");
                  sender.sendMessage("" + ChatColor.GREEN + nostock + " selling shops (excluding doubles) which will be removed by /qs clean.");
                  return true;
               }
            }

            this.sendHelp(sender);
            return true;
         }
      }
   }

   public Location lookAt(Location loc, Location lookat) {
      loc = loc.clone();
      double dx = lookat.getX() - loc.getX();
      double dy = lookat.getY() - loc.getY();
      double dz = lookat.getZ() - loc.getZ();
      if (dx != (double)0.0F) {
         if (dx < (double)0.0F) {
            loc.setYaw(((float)Math.PI * 1.5F));
         } else {
            loc.setYaw(((float)Math.PI / 2F));
         }

         loc.setYaw(loc.getYaw() - (float)Math.atan(dz / dx));
      } else if (dz < (double)0.0F) {
         loc.setYaw((float)Math.PI);
      }

      double dxz = Math.sqrt(Math.pow(dx, (double)2.0F) + Math.pow(dz, (double)2.0F));
      float pitch = (float)(-Math.atan(dy / dxz));
      loc.setYaw(-loc.getYaw() * 180.0F / (float)Math.PI + 360.0F);
      loc.setPitch(pitch * 180.0F / (float)Math.PI);
      return loc;
   }

   public void sendHelp(CommandSender s) {
      s.sendMessage(MsgUtil.getMessage("command.description.title"));
      if (s.hasPermission("quickshop.unlimited")) {
         s.sendMessage(ChatColor.GREEN + "/qs unlimited" + ChatColor.YELLOW + " - " + MsgUtil.getMessage("command.description.unlimited"));
      }

      if (s.hasPermission("quickshop.setowner")) {
         s.sendMessage(ChatColor.GREEN + "/qs setowner <player>" + ChatColor.YELLOW + " - " + MsgUtil.getMessage("command.description.setowner"));
      }

      if (s.hasPermission("quickshop.create.buy")) {
         s.sendMessage(ChatColor.GREEN + "/qs buy" + ChatColor.YELLOW + " - " + MsgUtil.getMessage("command.description.buy"));
      }

      if (s.hasPermission("quickshop.create.sell")) {
         s.sendMessage(ChatColor.GREEN + "/qs sell" + ChatColor.YELLOW + " - " + MsgUtil.getMessage("command.description.sell"));
      }

      if (s.hasPermission("quickshop.create.changeprice")) {
         s.sendMessage(ChatColor.GREEN + "/qs price" + ChatColor.YELLOW + " - " + MsgUtil.getMessage("command.description.price"));
      }

      if (s.hasPermission("quickshop.clean")) {
         s.sendMessage(ChatColor.GREEN + "/qs clean" + ChatColor.YELLOW + " - " + MsgUtil.getMessage("command.description.clean"));
      }

      if (s.hasPermission("quickshop.find")) {
         s.sendMessage(ChatColor.GREEN + "/qs find <item>" + ChatColor.YELLOW + " - " + MsgUtil.getMessage("command.description.find"));
      }

      if (s.hasPermission("quickshop.refill")) {
         s.sendMessage(ChatColor.GREEN + "/qs refill <amount>" + ChatColor.YELLOW + " - " + MsgUtil.getMessage("command.description.refill"));
      }

      if (s.hasPermission("quickshop.empty")) {
         s.sendMessage(ChatColor.GREEN + "/qs empty" + ChatColor.YELLOW + " - " + MsgUtil.getMessage("command.description.empty"));
      }

      if (s.hasPermission("quickshop.export")) {
         s.sendMessage(ChatColor.GREEN + "/qs export mysql|sqlite" + ChatColor.YELLOW + " - Exports the database to SQLite or MySQL");
      }

   }
}
