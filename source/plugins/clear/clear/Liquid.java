package clear;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class Liquid implements Listener {
   private static final int CHECK_INTERVAL = 200;
   private Main main;
   private String pn;
   private ServerManager serverManager;
   private boolean enable;
   private int checkInterval;
   private int goodLimit;
   private int goodCancel;
   private int fineLimit;
   private int fineCancel;
   private int badLimit;
   private int badCancel;
   private int unknownLimit;
   private int unknownCancel;
   private HashMap liquidBlockList;
   private HashMap itemsList;
   private int limit;
   private int cancel;
   private int count;

   public Liquid(Main main) {
      this.main = main;
      this.pn = main.getPn();
      this.serverManager = main.getServerManager();
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
      this.check();
      main.getServer().getScheduler().scheduleSyncRepeatingTask(main, new Runnable() {
         public void run() {
            Liquid.this.check();
         }
      }, 200L, 200L);
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBlockFromTo(BlockFromToEvent e) {
      if (this.enable && this.liquidBlockList.containsKey(e.getBlock().getTypeId()) && !this.flow()) {
         e.setCancelled(true);
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onPlayerInteract(PlayerInteractEvent e) {
      if (e.hasItem() && this.itemsList.containsKey(e.getItem().getTypeId()) && this.count >= this.limit) {
         e.setCancelled(true);
         e.getPlayer().sendMessage(this.main.format("fail", this.get(2000)));
      }

   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onTime(TimeEvent0 e) {
      if (TimeEvent0.getTime() % (long)this.checkInterval == 0L) {
         this.count = 0;
      }

   }

   public void loadConfig(YamlConfiguration config) {
      this.enable = config.getBoolean("liquid.enable");
      this.checkInterval = config.getInt("liquid.checkInterval");
      this.goodLimit = config.getInt("liquid.times.good.limit");
      this.goodCancel = config.getInt("liquid.times.good.cancel");
      this.fineLimit = config.getInt("liquid.times.fine.limit");
      this.fineCancel = config.getInt("liquid.times.fine.cancel");
      this.badLimit = config.getInt("liquid.times.bad.limit");
      this.badCancel = config.getInt("liquid.times.bad.cancel");
      this.unknownLimit = config.getInt("liquid.times.unknown.limit");
      this.unknownCancel = config.getInt("liquid.times.unknown.cancel");
      this.liquidBlockList = new HashMap();

      for(int id : config.getIntegerList("liquid.liquidBlock")) {
         this.liquidBlockList.put(id, true);
      }

      this.itemsList = new HashMap();

      for(int id : config.getIntegerList("liquid.items")) {
         this.itemsList.put(id, true);
      }

   }

   private boolean flow() {
      ++this.count;
      return this.count < this.cancel;
   }

   private void check() {
      switch (this.serverManager.getServerStatus()) {
         case 0:
            this.limit = this.goodLimit;
            this.cancel = this.goodCancel;
            break;
         case 1:
            this.limit = this.fineLimit;
            this.cancel = this.fineCancel;
            break;
         case 2:
            this.limit = this.badLimit;
            this.cancel = this.badCancel;
            break;
         case 3:
            this.limit = this.unknownLimit;
            this.cancel = this.unknownCancel;
      }

   }

   private String get(int id) {
      return this.main.format(this.pn, id);
   }
}
