package landHandler;

import java.util.HashMap;
import land.Range;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilPer;
import lib.util.UtilSpeed;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class ShowHandler implements Listener {
   private static final String SHOW = "showLandRange";
   private LandManager landManager;
   private String pn;
   private String per;
   private int id;
   private int maxShowBlocks;
   private int interval;
   private HashMap showHash;

   public ShowHandler(LandManager landManager) {
      this.landManager = landManager;
      this.pn = landManager.getLandMain().getPn();
      this.showHash = new HashMap();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      landManager.registerEvents(this);
      UtilSpeed.register(this.pn, "showLandRange");
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerQuit(PlayerQuitEvent e) {
      this.showHash.remove(e.getPlayer());
   }

   public Range getShowRange(Player p) {
      return (Range)this.showHash.get(p);
   }

   public void checkCancelShow(Player p) {
      Range range = (Range)this.showHash.get(p);
      if (range != null) {
         this.showHash.remove(p);
         World w = Bukkit.getServer().getWorld(range.getP1().getWorld());
         if (w != null) {
            this.show(p, w, range, true);
            p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(225)}));
         }
      }
   }

   public boolean showOn(Player p) {
      if (!UtilSpeed.check(p, this.pn, "showLandRange", this.interval)) {
         return false;
      } else {
         Range range = this.landManager.getSelectHandler().getRange(p);
         if (range == null) {
            p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(105)}));
            return false;
         } else {
            World w = Bukkit.getServer().getWorld(range.getP1().getWorld());
            if (this.showHash.containsKey(p)) {
               this.showOff(p, false);
            }

            this.showHash.put(p, range);
            this.show(p, w, range, false);
            p.sendMessage(UtilFormat.format((String)null, "success", new Object[]{this.get(220)}));
            return true;
         }
      }
   }

   public boolean show(Player p, World w, Range range, boolean show) {
      if (!UtilPer.checkPer(p, this.per)) {
         return false;
      } else {
         Range r = range.clone();
         r.fit();
         int x1 = r.getP1().getX();
         int x2 = r.getP2().getX();
         int y1 = r.getP1().getY();
         int y2 = r.getP2().getY();
         int z1 = r.getP1().getZ();
         int z2 = r.getP2().getZ();
         int totalLength = r.getTotalLength(false);
         int showInterval = 1;
         if (totalLength > this.maxShowBlocks) {
            showInterval = totalLength / this.maxShowBlocks;
            if (totalLength % this.maxShowBlocks != 0) {
               ++showInterval;
            }
         }

         this.showXEdge(p, w, x1, x2, y1, z1, showInterval, show);
         this.showXEdge(p, w, x1, x2, y2, z1, showInterval, show);
         this.showXEdge(p, w, x1, x2, y1, z2, showInterval, show);
         this.showXEdge(p, w, x1, x2, y2, z2, showInterval, show);
         this.showYEdge(p, w, x1, y1, y2, z1, showInterval, show);
         this.showYEdge(p, w, x2, y1, y2, z1, showInterval, show);
         this.showYEdge(p, w, x1, y1, y2, z2, showInterval, show);
         this.showYEdge(p, w, x2, y1, y2, z2, showInterval, show);
         this.showZEdge(p, w, x1, y1, z1, z2, showInterval, show);
         this.showZEdge(p, w, x2, y1, z1, z2, showInterval, show);
         this.showZEdge(p, w, x1, y2, z1, z2, showInterval, show);
         this.showZEdge(p, w, x2, y2, z1, z2, showInterval, show);
         this.showPoint(p, w, x1, y1, z1, 1, show);
         this.showPoint(p, w, x2, y1, z1, 1, show);
         this.showPoint(p, w, x1, y2, z1, 1, show);
         this.showPoint(p, w, x1, y1, z2, 1, show);
         this.showPoint(p, w, x2, y2, z1, 1, show);
         this.showPoint(p, w, x1, y2, z2, 1, show);
         this.showPoint(p, w, x2, y1, z2, 1, show);
         this.showPoint(p, w, x2, y2, z2, 1, show);
         return true;
      }
   }

   private void loadConfig(YamlConfiguration config) {
      this.maxShowBlocks = config.getInt("show.maxShowBlocks");
      this.interval = config.getInt("show.interval");
      this.per = config.getString("show.per");
      this.id = config.getInt("show.id");
   }

   private void showOff(Player p, boolean tip) {
      Range range = (Range)this.showHash.get(p);
      if (range == null) {
         p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(215)}));
      } else {
         World w = Bukkit.getServer().getWorld(range.getP1().getWorld());
         this.showHash.remove(p);
         this.show(p, w, range, true);
         if (tip) {
            p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(225)}));
         }

      }
   }

   private void showXEdge(Player p, World w, int x1, int x2, int y, int z, int showInterval, boolean real) {
      for(int x = x1; x <= x2; x += showInterval) {
         if (real) {
            p.sendBlockChange(new Location(w, (double)x, (double)y, (double)z), w.getBlockTypeIdAt(x, y, z), w.getBlockAt(x, y, z).getData());
         } else {
            p.sendBlockChange(new Location(w, (double)x, (double)y, (double)z), this.id, (byte)0);
         }
      }

   }

   private void showYEdge(Player p, World w, int x, int y1, int y2, int z, int showInterval, boolean real) {
      for(int y = y1; y <= y2; y += showInterval) {
         if (real) {
            p.sendBlockChange(new Location(w, (double)x, (double)y, (double)z), w.getBlockTypeIdAt(x, y, z), w.getBlockAt(x, y, z).getData());
         } else {
            p.sendBlockChange(new Location(w, (double)x, (double)y, (double)z), this.id, (byte)0);
         }
      }

   }

   private void showZEdge(Player p, World w, int x, int y, int z1, int z2, int showInterval, boolean real) {
      for(int z = z1; z <= z2; z += showInterval) {
         if (real) {
            p.sendBlockChange(new Location(w, (double)x, (double)y, (double)z), w.getBlockTypeIdAt(x, y, z), w.getBlockAt(x, y, z).getData());
         } else {
            p.sendBlockChange(new Location(w, (double)x, (double)y, (double)z), this.id, (byte)0);
         }
      }

   }

   private void showPoint(Player p, World w, int x, int y, int z, int showInterval, boolean real) {
      if (real) {
         p.sendBlockChange(new Location(w, (double)x, (double)y, (double)z), w.getBlockTypeIdAt(x, y, z), w.getBlockAt(x, y, z).getData());
      } else {
         p.sendBlockChange(new Location(w, (double)x, (double)y, (double)z), this.id, (byte)0);
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   public String getPer() {
      return this.per;
   }
}
