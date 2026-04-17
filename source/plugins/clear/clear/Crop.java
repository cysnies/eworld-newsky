package clear;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockSpreadEvent;

public class Crop implements Listener {
   private Main main;
   private Server server;
   private boolean enable;
   private HashMap ignoreWorlds;
   private int gridSize;
   private int checkInterval;
   private int ingameTipMinInterval;
   private int consoleTipMinInterval;
   private int taskId = -1;
   private ReSet reSet;
   private HashMap countHash;
   private int max;
   private boolean drop;
   private boolean reset;
   private boolean ingameTip;
   private boolean consoleTip;
   private long lastInGameTip;
   private long lastConsoleTip;

   public Crop(Main main) {
      this.main = main;
      this.server = main.getServer();
      this.reSet = new ReSet();
      YamlConfiguration config = new YamlConfiguration();

      try {
         config.load(main.getPluginPath() + File.separator + main.getPn() + File.separator + "config.yml");
         this.loadConfig(config);
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (InvalidConfigurationException e) {
         e.printStackTrace();
      }

      main.getServer().getPluginManager().registerEvents(this, main);
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onBlockGrow(BlockGrowEvent e) {
      if (!this.crop(e.getBlock().getLocation())) {
         e.setCancelled(true);
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onBlockSpread(BlockSpreadEvent e) {
      int id = e.getSource().getTypeId();
      if ((id == 39 || id == 40) && !this.crop(e.getSource().getLocation())) {
         e.setCancelled(true);
      }

   }

   public void loadConfig(YamlConfiguration config) {
      this.enable = config.getBoolean("crop.enable");
      this.ignoreWorlds = new HashMap();

      for(String s : config.getStringList("crop.ignoreWorlds")) {
         this.ignoreWorlds.put(s, true);
      }

      this.gridSize = config.getInt("crop.gridSize");
      this.checkInterval = config.getInt("crop.checkInterval");
      this.max = config.getInt("crop.max");
      this.drop = config.getBoolean("crop.drop");
      this.reset = config.getBoolean("crop.reset");
      this.ingameTip = config.getBoolean("crop.tip.ingame");
      this.consoleTip = config.getBoolean("crop.tip.console");
      this.ingameTipMinInterval = config.getInt("crop.tip.ingameTipMinInterval");
      this.consoleTipMinInterval = config.getInt("crop.tip.consoleTipMinInterval");
      if (this.taskId != -1) {
         this.main.getServer().getScheduler().cancelTask(this.taskId);
      }

      this.reSet();
   }

   private boolean crop(Location l) {
      if (!this.enable) {
         return true;
      } else {
         String worldName = l.getWorld().getName();
         if (this.ignoreWorlds.containsKey(worldName)) {
            return true;
         } else {
            int x = l.getBlockX() / this.gridSize;
            int z = l.getBlockZ() / this.gridSize;
            if (!this.countHash.containsKey(worldName)) {
               return true;
            } else {
               if (!((HashMap)this.countHash.get(worldName)).containsKey(x)) {
                  ((HashMap)this.countHash.get(worldName)).put(x, new HashMap());
               }

               if (!((HashMap)((HashMap)this.countHash.get(worldName)).get(x)).containsKey(z)) {
                  ((HashMap)((HashMap)this.countHash.get(worldName)).get(x)).put(z, 0);
               }

               ((HashMap)((HashMap)this.countHash.get(worldName)).get(x)).put(z, (Integer)((HashMap)((HashMap)this.countHash.get(worldName)).get(x)).get(z) + 1);
               if ((Integer)((HashMap)((HashMap)this.countHash.get(worldName)).get(x)).get(z) >= this.max) {
                  if (this.reset) {
                     ((HashMap)((HashMap)this.countHash.get(worldName)).get(x)).put(z, 0);
                  }

                  String handle;
                  if (this.drop) {
                     l.getBlock().breakNaturally();
                     handle = this.get(1886);
                  } else {
                     l.getBlock().setTypeId(0);
                     handle = this.get(1885);
                  }

                  String result = this.main.format("cropTip", worldName, l.getBlockX(), l.getBlockY(), l.getBlockZ(), handle);
                  long now = System.currentTimeMillis();
                  if (this.ingameTip && now - this.lastInGameTip > (long)this.ingameTipMinInterval) {
                     this.server.broadcastMessage(result);
                     this.lastInGameTip = now;
                  }

                  if (this.consoleTip && now - this.lastConsoleTip > (long)this.consoleTipMinInterval) {
                     this.server.getConsoleSender().sendMessage(result);
                     this.lastConsoleTip = now;
                  }

                  return false;
               } else {
                  return true;
               }
            }
         }
      }
   }

   private void reSet() {
      this.countHash = new HashMap();

      for(World w : this.server.getWorlds()) {
         this.countHash.put(w.getName(), new HashMap());
      }

      this.taskId = this.main.getServer().getScheduler().runTaskLater(this.main, this.reSet, (long)(this.checkInterval * 20)).getTaskId();
   }

   private String get(int id) {
      return this.main.get(id);
   }

   class ReSet implements Runnable {
      public void run() {
         Crop.this.reSet();
      }
   }
}
