package org.maxgamer.QuickShop.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.maxgamer.QuickShop.QuickShop;
import org.maxgamer.QuickShop.Database.Database;
import org.maxgamer.QuickShop.Shop.ContainerShop;
import org.maxgamer.QuickShop.Shop.Shop;
import org.maxgamer.QuickShop.Shop.ShopChunk;
import org.maxgamer.QuickShop.Shop.ShopManager;
import org.maxgamer.QuickShop.Shop.ShopType;

public class Converter {
   public static int convert() {
      Database database = QuickShop.instance.getDB();

      try {
         if (database.hasColumn("shops", "itemString")) {
            try {
               convertDatabase_2_9();
               convertDatabase_3_4();
               convertDatabase_3_8();
               return 1;
            } catch (Exception e) {
               e.printStackTrace();
               return -1;
            }
         }
      } catch (SQLException e) {
         e.printStackTrace();
         return -1;
      }

      try {
         Connection con = database.getConnection();
         PreparedStatement ps = con.prepareStatement("SELECT * FROM shops");
         ResultSet rs = ps.executeQuery();
         String colType = rs.getMetaData().getColumnTypeName(3);
         if (rs.next()) {
            ps.close();

            try {
               rs.getString("item");
               if (!colType.equalsIgnoreCase("BLOB")) {
                  System.out.println("Item column type: " + colType + ", converting to BLOB.");

                  try {
                     convertDatabase_3_4();
                     convertDatabase_3_8();
                     return 1;
                  } catch (Exception e) {
                     e.printStackTrace();
                     return -1;
                  }
               }
            } catch (SQLException var9) {
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
         return -1;
      }

      try {
         if (database.hasColumn("shops", "item")) {
            convertDatabase_3_8();
            return 1;
         } else {
            return 0;
         }
      } catch (Exception e) {
         e.printStackTrace();
         return -1;
      }
   }

   public static void convertDatabase_3_8() throws Exception {
      Database database = QuickShop.instance.getDB();
      ShopManager shopManager = QuickShop.instance.getShopManager();
      Connection con = database.getConnection();
      System.out.println("Converting shops to 3.8 format...");
      PreparedStatement ps = con.prepareStatement("SELECT * FROM shops");
      ResultSet rs = ps.executeQuery();
      int shops = 0;
      System.out.println("Loading shops...");

      while(rs.next()) {
         int x = rs.getInt("x");
         int y = rs.getInt("y");
         int z = rs.getInt("z");
         String worldName = rs.getString("world");

         try {
            World world = Bukkit.getWorld(worldName);
            ItemStack item = Util.getItemStack(rs.getBytes("item"));
            String owner = rs.getString("owner");
            double price = rs.getDouble("price");
            Location loc = new Location(world, (double)x, (double)y, (double)z);
            int type = rs.getInt("type");
            Shop shop = new ContainerShop(loc, price, item, owner);
            shop.setUnlimited(rs.getBoolean("unlimited"));
            shop.setShopType(ShopType.fromID(type));
            shopManager.loadShop(rs.getString("world"), shop);
            ++shops;
         } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading a shop! Coords: " + worldName + " (" + x + ", " + y + ", " + z + ") - Skipping it...");
         }
      }

      ps.close();
      rs.close();
      System.out.println("Loading complete. Backing up and deleting shops table...");
      File existing = new File(QuickShop.instance.getDataFolder(), "shops.db");
      File backup = new File(existing.getAbsolutePath() + ".3.7.bak");
      InputStream in = new FileInputStream(existing);
      OutputStream out = new FileOutputStream(backup);
      byte[] buf = new byte[1024];

      int len;
      while((len = in.read(buf)) > 0) {
         out.write(buf, 0, len);
      }

      in.close();
      out.close();
      ps = con.prepareStatement("DELETE FROM shops");
      ps.execute();
      ps.close();
      con.close();
      con = database.getConnection();
      ps = con.prepareStatement("DROP TABLE shops");
      ps.execute();
      ps.close();
      Statement st = database.getConnection().createStatement();
      String createTable = "CREATE TABLE shops (owner  TEXT(20) NOT NULL, price  double(32, 2) NOT NULL, itemConfig  BLOB NOT NULL, x  INTEGER(32) NOT NULL, y  INTEGER(32) NOT NULL, z  INTEGER(32) NOT NULL, world VARCHAR(32) NOT NULL, unlimited  boolean, type  boolean, PRIMARY KEY (x, y, z, world) );";
      st.execute(createTable);

      for(Map.Entry worlds : shopManager.getShops().entrySet()) {
         String world = (String)worlds.getKey();

         for(Map.Entry chunks : ((HashMap)worlds.getValue()).entrySet()) {
            for(Shop shop : ((HashMap)chunks.getValue()).values()) {
               ps = con.prepareStatement("INSERT INTO shops (owner, price, itemConfig, x, y, z, world, unlimited, type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
               ps.setString(1, shop.getOwner());
               ps.setDouble(2, shop.getPrice());
               ps.setString(3, Util.serialize(shop.getItem()));
               ps.setInt(4, shop.getLocation().getBlockX());
               ps.setInt(5, shop.getLocation().getBlockY());
               ps.setInt(6, shop.getLocation().getBlockZ());
               ps.setString(7, world);
               ps.setInt(8, shop.isUnlimited() ? 1 : 0);
               ps.setInt(9, ShopType.toID(shop.getShopType()));
               ps.execute();
               ps.close();
               --shops;
               if (shops % 10 == 0) {
                  System.out.println("Remaining: " + shops + " shops.");
               }
            }
         }
      }

      System.out.println("Conversion complete.");
   }

   public static void convertDatabase_2_9() throws Exception {
      Database database = QuickShop.instance.getDB();
      ShopManager shopManager = QuickShop.instance.getShopManager();
      Connection con = database.getConnection();
      System.out.println("Converting shops to 2.9 format...");
      PreparedStatement ps = con.prepareStatement("SELECT * FROM shops");
      ResultSet rs = ps.executeQuery();
      int shops = 0;
      System.out.println("Loading shops...");

      while(rs.next()) {
         int x = rs.getInt("x");
         int y = rs.getInt("y");
         int z = rs.getInt("z");
         String worldName = rs.getString("world");

         try {
            World world = Bukkit.getWorld(worldName);
            ItemStack item = Util.makeItem(rs.getString("itemString"));
            String owner = rs.getString("owner");
            double price = rs.getDouble("price");
            Location loc = new Location(world, (double)x, (double)y, (double)z);
            int type = rs.getInt("type");
            Shop shop = new ContainerShop(loc, price, item, owner);
            shop.setUnlimited(rs.getBoolean("unlimited"));
            shop.setShopType(ShopType.fromID(type));
            shopManager.loadShop(rs.getString("world"), shop);
            ++shops;
         } catch (Exception var21) {
            System.out.println("Error loading a shop! Coords: " + worldName + " (" + x + ", " + y + ", " + z + ") - Skipping it...");
         }
      }

      ps.close();
      rs.close();
      System.out.println("Loading complete. Backing up and deleting shops table...");
      File existing = new File(QuickShop.instance.getDataFolder(), "shops.db");
      File backup = new File(existing.getAbsolutePath() + ".bak");
      InputStream in = new FileInputStream(existing);
      OutputStream out = new FileOutputStream(backup);
      byte[] buf = new byte[1024];

      int len;
      while((len = in.read(buf)) > 0) {
         out.write(buf, 0, len);
      }

      in.close();
      out.close();
      ps = con.prepareStatement("DELETE FROM shops");
      ps.execute();
      ps.close();
      con.close();
      con = database.getConnection();
      ps = con.prepareStatement("DROP TABLE shops");
      ps.execute();
      ps.close();
      Statement st = database.getConnection().createStatement();
      String createTable = "CREATE TABLE shops (owner  TEXT(20) NOT NULL, price  double(32, 2) NOT NULL, item  BLOB NOT NULL, x  INTEGER(32) NOT NULL, y  INTEGER(32) NOT NULL, z  INTEGER(32) NOT NULL, world VARCHAR(32) NOT NULL, unlimited  boolean, type  boolean, PRIMARY KEY (x, y, z, world) );";
      st.execute(createTable);

      for(Map.Entry worlds : shopManager.getShops().entrySet()) {
         String world = (String)worlds.getKey();

         for(Map.Entry chunks : ((HashMap)worlds.getValue()).entrySet()) {
            for(Shop shop : ((HashMap)chunks.getValue()).values()) {
               ps = con.prepareStatement("INSERT INTO shops (owner, price, item, x, y, z, world, unlimited, type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
               ps.setString(1, shop.getOwner());
               ps.setDouble(2, shop.getPrice());
               ps.setString(3, Util.getNBTString(shop.getItem()));
               ps.setInt(4, shop.getLocation().getBlockX());
               ps.setInt(5, shop.getLocation().getBlockY());
               ps.setInt(6, shop.getLocation().getBlockZ());
               ps.setString(7, world);
               ps.setInt(8, shop.isUnlimited() ? 1 : 0);
               ps.setInt(9, ShopType.toID(shop.getShopType()));
               ps.execute();
               ps.close();
               --shops;
               if (shops % 10 == 0) {
                  System.out.println("Remaining: " + shops + " shops.");
               }
            }
         }
      }

      System.out.println("Conversion complete.");
   }

   public static void convertDatabase_3_4() throws Exception {
      Database database = QuickShop.instance.getDB();
      ShopManager shopManager = QuickShop.instance.getShopManager();
      Connection con = database.getConnection();
      System.out.println("Converting shops to 3.4 format...");
      PreparedStatement ps = con.prepareStatement("SELECT * FROM shops");
      ResultSet rs = ps.executeQuery();
      int shops = 0;
      System.out.println("Loading shops...");

      while(rs.next()) {
         int x = rs.getInt("x");
         int y = rs.getInt("y");
         int z = rs.getInt("z");
         String worldName = rs.getString("world");

         try {
            World world = Bukkit.getWorld(worldName);
            ItemStack item = Util.getItemStack(rs.getString("item"));
            String owner = rs.getString("owner");
            double price = rs.getDouble("price");
            Location loc = new Location(world, (double)x, (double)y, (double)z);
            int type = rs.getInt("type");
            Shop shop = new ContainerShop(loc, price, item, owner);
            shop.setUnlimited(rs.getBoolean("unlimited"));
            shop.setShopType(ShopType.fromID(type));
            shopManager.loadShop(rs.getString("world"), shop);
            ++shops;
         } catch (Exception var21) {
            System.out.println("Error loading a shop! Coords: " + worldName + " (" + x + ", " + y + ", " + z + ") - Skipping it...");
         }
      }

      ps.close();
      rs.close();
      System.out.println("Loading complete. Backing up and deleting shops table...");
      File existing = new File(QuickShop.instance.getDataFolder(), "shops.db");
      File backup = new File(existing.getAbsolutePath() + ".bak2");
      InputStream in = new FileInputStream(existing);
      OutputStream out = new FileOutputStream(backup);
      byte[] buf = new byte[1024];

      int len;
      while((len = in.read(buf)) > 0) {
         out.write(buf, 0, len);
      }

      in.close();
      out.close();
      ps = con.prepareStatement("DELETE FROM shops");
      ps.execute();
      ps.close();
      con.close();
      con = database.getConnection();
      ps = con.prepareStatement("DROP TABLE shops");
      ps.execute();
      ps.close();
      Statement st = database.getConnection().createStatement();
      String createTable = "CREATE TABLE shops (owner  TEXT(20) NOT NULL, price  double(32, 2) NOT NULL, item  BLOB NOT NULL, x  INTEGER(32) NOT NULL, y  INTEGER(32) NOT NULL, z  INTEGER(32) NOT NULL, world VARCHAR(32) NOT NULL, unlimited  boolean, type  boolean, PRIMARY KEY (x, y, z, world) );";
      st.execute(createTable);

      for(Map.Entry worlds : shopManager.getShops().entrySet()) {
         String world = (String)worlds.getKey();

         for(Map.Entry chunks : ((HashMap)worlds.getValue()).entrySet()) {
            for(Shop shop : ((HashMap)chunks.getValue()).values()) {
               ps = con.prepareStatement("INSERT INTO shops (owner, price, item, x, y, z, world, unlimited, type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
               ps.setString(1, shop.getOwner());
               ps.setDouble(2, shop.getPrice());
               ps.setBytes(3, Util.getNBTBytes(shop.getItem()));
               ps.setInt(4, shop.getLocation().getBlockX());
               ps.setInt(5, shop.getLocation().getBlockY());
               ps.setInt(6, shop.getLocation().getBlockZ());
               ps.setString(7, world);
               ps.setInt(8, shop.isUnlimited() ? 1 : 0);
               ps.setInt(9, ShopType.toID(shop.getShopType()));
               ps.execute();
               ps.close();
               --shops;
               if (shops % 10 == 0) {
                  System.out.println("Remaining: " + shops + " shops.");
               }
            }
         }
      }

      System.out.println("Conversion complete.");
   }
}
