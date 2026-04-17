package org.maxgamer.QuickShop.Shop;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.maxgamer.QuickShop.QuickShop;
import org.maxgamer.QuickShop.Util.MsgUtil;
import org.maxgamer.QuickShop.Util.Util;

public class ContainerShop implements Shop {
   private Location loc;
   private double price;
   private String owner;
   private ItemStack item;
   private DisplayItem displayItem;
   private boolean unlimited;
   private ShopType shopType;
   private QuickShop plugin;

   public ContainerShop clone() {
      return new ContainerShop(this);
   }

   private ContainerShop(ContainerShop s) {
      this.displayItem = s.displayItem;
      this.shopType = s.shopType;
      this.item = s.item;
      this.loc = s.loc;
      this.plugin = s.plugin;
      this.unlimited = s.unlimited;
      this.owner = s.owner;
      this.price = s.price;
   }

   public ContainerShop(Location loc, double price, ItemStack item, String owner) {
      this.loc = loc;
      this.price = price;
      this.owner = owner;
      this.item = item.clone();
      this.plugin = (QuickShop)Bukkit.getPluginManager().getPlugin("QuickShop");
      this.item.setAmount(1);
      if (this.plugin.display) {
         this.displayItem = new DisplayItem(this, this.item);
      }

      this.shopType = ShopType.SELLING;
   }

   public int getRemainingStock() {
      return this.unlimited ? 10000 : Util.countItems(this.getInventory(), this.getItem());
   }

   public int getRemainingSpace() {
      return this.unlimited ? 10000 : Util.countSpace(this.getInventory(), this.item);
   }

   public boolean matches(ItemStack item) {
      return Util.matches(this.item, item);
   }

   public ContainerShop getAttachedShop() {
      Block c = Util.getSecondHalf(this.getLocation().getBlock());
      if (c == null) {
         return null;
      } else {
         Shop shop = this.plugin.getShopManager().getShop(c.getLocation());
         return shop == null ? null : (ContainerShop)shop;
      }
   }

   public boolean isDoubleShop() {
      ContainerShop nextTo = this.getAttachedShop();
      if (nextTo == null) {
         return false;
      } else if (nextTo.matches(this.getItem())) {
         return this.getShopType() != nextTo.getShopType();
      } else {
         return false;
      }
   }

   public Location getLocation() {
      return this.loc;
   }

   public double getPrice() {
      return this.price;
   }

   public void setPrice(double price) {
      this.price = price;
   }

   public Material getMaterial() {
      return this.item.getType();
   }

   public void update() {
      int x = this.getLocation().getBlockX();
      int y = this.getLocation().getBlockY();
      int z = this.getLocation().getBlockZ();
      String world = this.getLocation().getWorld().getName();
      int unlimited = this.isUnlimited() ? 1 : 0;
      String q = "UPDATE shops SET owner = ?, itemConfig = ?, unlimited = ?, type = ?, price = ? WHERE x = ? AND y = ? and z = ? and world = ?";

      try {
         this.plugin.getDB().execute(q, this.getOwner(), Util.serialize(this.getItem()), unlimited, this.shopType.toID(), this.getPrice(), x, y, z, world);
      } catch (Exception e) {
         e.printStackTrace();
         System.out.println("Could not update shop in database! Changes will revert after a reboot!");
      }

   }

   public short getDurability() {
      return this.item.getDurability();
   }

   public Inventory getInventory() {
      InventoryHolder container = (InventoryHolder)this.loc.getBlock().getState();
      return container.getInventory();
   }

   public String getOwner() {
      return this.owner;
   }

   public Map getEnchants() {
      return this.item.getItemMeta().getEnchants();
   }

   public ItemStack getItem() {
      return this.item;
   }

   public void remove(ItemStack item, int amount) {
      if (!this.unlimited) {
         Inventory inv = this.getInventory();

         int stackSize;
         for(int remains = amount; remains > 0; remains -= stackSize) {
            stackSize = Math.min(remains, item.getMaxStackSize());
            item.setAmount(stackSize);
            inv.removeItem(new ItemStack[]{item});
         }

      }
   }

   public void add(ItemStack item, int amount) {
      if (!this.unlimited) {
         Inventory inv = this.getInventory();

         int stackSize;
         for(int remains = amount; remains > 0; remains -= stackSize) {
            stackSize = Math.min(remains, item.getMaxStackSize());
            item.setAmount(stackSize);
            inv.addItem(new ItemStack[]{item});
         }

      }
   }

   public void sell(Player p, int amount) {
      if (amount < 0) {
         this.buy(p, -amount);
      }

      ArrayList<ItemStack> floor = new ArrayList(5);
      Inventory pInv = p.getInventory();
      int stackSize;
      if (this.isUnlimited()) {
         for(ItemStack item = this.item.clone(); amount > 0; amount -= stackSize) {
            stackSize = Math.min(amount, this.item.getMaxStackSize());
            item.setAmount(stackSize);
            pInv.addItem(new ItemStack[]{item});
         }
      } else {
         ItemStack[] chestContents = this.getInventory().getContents();

         for(int i = 0; amount > 0 && i < chestContents.length; ++i) {
            ItemStack item = chestContents[i];
            if (item != null && this.matches(item)) {
               item = item.clone();
               int stackSize = Math.min(amount, item.getAmount());
               chestContents[i].setAmount(chestContents[i].getAmount() - stackSize);
               item.setAmount(stackSize);
               floor.addAll(pInv.addItem(new ItemStack[]{item}).values());
               amount -= stackSize;
            }
         }

         this.getInventory().setContents(chestContents);
      }

      for(int i = 0; i < floor.size(); ++i) {
         p.getWorld().dropItem(p.getLocation(), (ItemStack)floor.get(i));
      }

   }

   public void buy(Player p, int amount) {
      if (amount < 0) {
         this.sell(p, -amount);
      }

      if (this.isUnlimited()) {
         ItemStack[] contents = p.getInventory().getContents();

         for(int i = 0; amount > 0 && i < contents.length; ++i) {
            ItemStack stack = contents[i];
            if (stack != null && this.matches(stack)) {
               int stackSize = Math.min(amount, stack.getAmount());
               stack.setAmount(stack.getAmount() - stackSize);
               amount -= stackSize;
            }
         }

         p.getInventory().setContents(contents);
         if (amount > 0) {
            this.plugin.getLogger().log(Level.WARNING, "Could not take all items from a players inventory on purchase! " + p.getName() + ", missing: " + amount + ", item: " + this.getDataName() + "!");
         }
      } else {
         ItemStack[] playerContents = p.getInventory().getContents();
         Inventory chestInv = this.getInventory();

         for(int i = 0; amount > 0 && i < playerContents.length; ++i) {
            ItemStack item = playerContents[i];
            if (item != null && this.matches(item)) {
               item = item.clone();
               int stackSize = Math.min(amount, item.getAmount());
               playerContents[i].setAmount(playerContents[i].getAmount() - stackSize);
               item.setAmount(stackSize);
               chestInv.addItem(new ItemStack[]{item});
               amount -= stackSize;
            }
         }

         p.getInventory().setContents(playerContents);
      }

   }

   public void setOwner(String owner) {
      this.owner = owner;
   }

   public DisplayItem getDisplayItem() {
      return this.displayItem;
   }

   public void setUnlimited(boolean unlimited) {
      this.unlimited = unlimited;
   }

   public boolean isUnlimited() {
      return this.unlimited;
   }

   public ShopType getShopType() {
      return this.shopType;
   }

   public boolean isBuying() {
      return this.shopType == ShopType.BUYING;
   }

   public boolean isSelling() {
      return this.shopType == ShopType.SELLING;
   }

   public void setShopType(ShopType shopType) {
      this.shopType = shopType;
      this.setSignText();
   }

   public void setSignText() {
      if (Util.isLoaded(this.getLocation())) {
         String[] lines = new String[4];
         lines[0] = ChatColor.RED + "[QuickShop]";
         if (this.isBuying()) {
            lines[1] = MsgUtil.getMessage("signs.buying", "" + this.getRemainingSpace());
         }

         if (this.isSelling()) {
            lines[1] = MsgUtil.getMessage("signs.selling", "" + this.getRemainingStock());
         }

         lines[2] = Util.getName(this.item);
         lines[3] = MsgUtil.getMessage("signs.price", "" + this.getPrice());
         this.setSignText(lines);
      }
   }

   public void setSignText(String[] lines) {
      if (Util.isLoaded(this.getLocation())) {
         for(Sign sign : this.getSigns()) {
            for(int i = 0; i < lines.length; ++i) {
               sign.setLine(i, lines[i]);
            }

            sign.update();
         }

      }
   }

   public List getSigns() {
      ArrayList<Sign> signs = new ArrayList(1);
      if (this.getLocation().getWorld() == null) {
         return signs;
      } else {
         Block[] blocks = new Block[]{this.loc.getBlock().getRelative(1, 0, 0), this.loc.getBlock().getRelative(-1, 0, 0), this.loc.getBlock().getRelative(0, 0, 1), this.loc.getBlock().getRelative(0, 0, -1)};

         for(Block b : blocks) {
            if (b.getType() == Material.WALL_SIGN && this.isAttached(b)) {
               Sign sign = (Sign)b.getState();
               if (sign.getLine(0).contains("[QuickShop")) {
                  signs.add(sign);
               } else {
                  boolean text = false;

                  String[] var12;
                  for(String s : var12 = sign.getLines()) {
                     if (!s.isEmpty()) {
                        text = true;
                        break;
                     }
                  }

                  if (!text) {
                     signs.add(sign);
                  }
               }
            }
         }

         return signs;
      }
   }

   public boolean isAttached(Block b) {
      if (b.getType() != Material.WALL_SIGN) {
         (new IllegalArgumentException(b + " Is not a sign!")).printStackTrace();
      }

      return this.getLocation().getBlock().equals(Util.getAttached(b));
   }

   public String getDataName() {
      return Util.getName(this.getItem());
   }

   public void delete() {
      this.delete(true);
   }

   public void delete(boolean fromMemory) {
      if (this.getDisplayItem() != null) {
         this.getDisplayItem().remove();
      }

      for(Sign s : this.getSigns()) {
         s.getBlock().setType(Material.AIR);
      }

      int x = this.getLocation().getBlockX();
      int y = this.getLocation().getBlockY();
      int z = this.getLocation().getBlockZ();
      String world = this.getLocation().getWorld().getName();
      this.plugin.getDB().execute("DELETE FROM shops WHERE x = '" + x + "' AND y = '" + y + "' AND z = '" + z + "' AND world = '" + world + "'");
      if (this.plugin.getConfig().getBoolean("shop.refund")) {
         this.plugin.getEcon().deposit(this.getOwner(), this.plugin.getConfig().getDouble("shop.cost"));
      }

      if (fromMemory) {
         this.plugin.getShopManager().removeShop(this);
      }

   }

   public boolean isValid() {
      this.checkDisplay();
      return Util.canBeShop(this.getLocation().getBlock());
   }

   private void checkDisplay() {
      if (this.plugin.display) {
         if (this.getLocation().getWorld() != null) {
            boolean trans = Util.isTransparent(this.getLocation().clone().add((double)0.5F, 1.2, (double)0.5F).getBlock().getType());
            if (trans && this.getDisplayItem() == null) {
               this.displayItem = new DisplayItem(this, this.getItem());
               this.getDisplayItem().spawn();
            }

            if (this.getDisplayItem() != null) {
               if (!trans) {
                  this.getDisplayItem().remove();
                  this.displayItem = null;
                  return;
               }

               DisplayItem disItem = this.getDisplayItem();
               Location dispLoc = disItem.getDisplayLocation();
               if (dispLoc.getBlock() != null && dispLoc.getBlock().getType() == Material.WATER) {
                  disItem.remove();
                  return;
               }

               if (disItem.getItem() == null) {
                  disItem.spawn();
                  return;
               }

               Item item = disItem.getItem();
               if (item.getTicksLived() <= 5000 && item.isValid() && !item.isDead()) {
                  if (item.getLocation().distanceSquared(dispLoc) > (double)1.0F) {
                     item.teleport(dispLoc, TeleportCause.PLUGIN);
                  }
               } else {
                  disItem.respawn();
               }
            }

         }
      }
   }

   public void onUnload() {
      if (this.getDisplayItem() != null) {
         this.getDisplayItem().remove();
         this.displayItem = null;
      }

   }

   public void onLoad() {
      this.checkDisplay();
   }

   public void onClick() {
      this.setSignText();
   }

   public String toString() {
      StringBuilder sb = new StringBuilder("Shop " + (this.loc.getWorld() == null ? "unloaded world" : this.loc.getWorld().getName()) + "(" + this.loc.getBlockX() + ", " + this.loc.getBlockY() + ", " + this.loc.getBlockZ() + ")");
      sb.append(" Owner: " + this.getOwner());
      if (this.isUnlimited()) {
         sb.append(" Unlimited: true");
      }

      sb.append(" Price: " + this.getPrice());
      sb.append("Item: " + this.getItem().toString());
      return sb.toString();
   }
}
