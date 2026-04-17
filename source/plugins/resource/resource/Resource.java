package resource;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import land.Land;
import land.Pos;
import land.Range;
import landMain.LandMain;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.time.TimeEvent;
import lib.util.UtilConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class Resource implements Listener {
   private static final String FLAG = "res";
   private static final String CHECK_FROM = "from";
   private static final String CHECK_TO = "to";
   private Random r = new Random();
   private String pn;
   private HashMap resHash;
   private int interval;
   private int chance;

   public Resource(Main main) {
      this.pn = main.getPn();
      this.loadConfig0(UtilConfig.getConfig(this.pn));
      Bukkit.getPluginManager().registerEvents(this, main);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig0(e.getConfig());
      }

   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onTime(TimeEvent e) {
      if (TimeEvent.getTime() % (long)this.interval == 0L) {
         Iterator<Land> it = this.resHash.keySet().iterator();

         while(it.hasNext()) {
            Land res = (Land)it.next();
            if (res == null) {
               it.remove();
            } else {
               Land resTo = (Land)this.resHash.get(res);
               if (resTo == null) {
                  it.remove();
               } else if (this.r.nextInt(100) < this.chance) {
                  try {
                     Range range = res.getRange();
                     range.fit();
                     Range rangeTo = resTo.getRange();
                     rangeTo.fit();
                     int x = this.r.nextInt(range.getXLength());
                     int y = this.r.nextInt(range.getYLength());
                     int z = this.r.nextInt(range.getZLength());
                     Location l0 = Pos.toLoc(rangeTo.getP1());
                     Block b = l0.add((double)x, (double)y, (double)z).getBlock();
                     if (b.getTypeId() == 0) {
                        Location l = Pos.toLoc(range.getP1());
                        Block from = l.add((double)x, (double)y, (double)z).getBlock();
                        b.setTypeIdAndData(from.getTypeId(), from.getData(), false);
                     }
                  } catch (Exception var14) {
                  }
               }
            }
         }
      }

   }

   private boolean checkValidFromName(String name) {
      if (name.length() >= "from".length() + 1 && name.substring(0, "from".length()).equals("from")) {
         try {
            Integer.parseInt(name.substring("from".length(), name.length()));
            return true;
         } catch (NumberFormatException var3) {
         }
      }

      return false;
   }

   private String getToName(String name) {
      return "to" + Integer.parseInt(name.substring("from".length(), name.length()));
   }

   private void loadConfig0(FileConfiguration config) {
      this.interval = config.getInt("interval");
      this.chance = config.getInt("chance");
      this.loadRes();
   }

   private void loadRes() {
      this.resHash = new HashMap();
      HashList<Land> landList = LandMain.getLandManager().getAllLands();
      if (landList != null) {
         for(Land land : landList) {
            if (land.hasFlag("res") && this.checkValidFromName(land.getName())) {
               Land toLand = LandMain.getLandManager().getLand(this.getToName(land.getName()));
               if (toLand != null && toLand.hasFlag("res")) {
                  this.resHash.put(land, toLand);
               }
            }
         }
      }

   }
}
