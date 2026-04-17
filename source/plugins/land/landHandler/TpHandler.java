package landHandler;

import event.LandRemoveEvent;
import event.RangeChangeEvent;
import java.util.HashMap;
import land.Land;
import land.LandSpawn;
import land.Pos;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilPer;
import lib.util.UtilSpeed;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class TpHandler implements Listener {
   private static final String SPEED_TP = "tpInterval";
   private LandManager landManager;
   private Server server;
   private String pn;
   private HashMap spawnHash;
   private String per_land_admin;
   private String perTp;
   private String perSet;
   private String noDelayPer;
   private int delay;
   private int interval;

   public TpHandler(LandManager landManager) {
      this.landManager = landManager;
      this.server = landManager.getServer();
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      landManager.registerEvents(this);
      UtilSpeed.register(this.pn, "tpInterval");
      this.loadData();
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
   public void onLandRemove(LandRemoveEvent e) {
      LandSpawn landSpawn = (LandSpawn)this.spawnHash.get(e.getRemovedLand());
      if (landSpawn != null) {
         this.landManager.removeLandSpawn(landSpawn);
         this.spawnHash.remove(e.getRemovedLand());
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onRangeChange(RangeChangeEvent e) {
      LandSpawn landSpawn = (LandSpawn)this.spawnHash.get(e.getLand());
      if (landSpawn != null) {
         Pos pos = landSpawn.getSpawn();
         if (!e.getLand().getRange().checkPos(pos)) {
            landSpawn.setSpawn(e.getLand().getRange().getCenter());
            this.landManager.addLandSpawn(landSpawn);
            String owner = e.getLand().getOwner();
            if (this.server.getPlayer(owner) != null) {
               this.server.getPlayer(owner).sendMessage(UtilFormat.format(this.pn, "landSpawnUpdate", new Object[]{e.getLand().getName()}));
            }
         }

      }
   }

   public Location getSpawnLoc(String name) {
      Land land = this.landManager.getLand((CommandSender)null, name);
      if (land == null) {
         return null;
      } else {
         LandSpawn landSpawn = (LandSpawn)this.spawnHash.get(land);
         if (landSpawn == null) {
            return null;
         } else {
            Pos pos = landSpawn.getSpawn();
            Location l = Pos.toLoc(pos);
            l.setYaw(landSpawn.getYaw());
            l.setPitch(landSpawn.getPitch());
            return l;
         }
      }
   }

   public boolean tp(Player p, String name) {
      if (!UtilPer.checkPer(p, this.perTp)) {
         return false;
      } else if (!UtilSpeed.check(p, this.pn, "tpInterval", this.interval)) {
         return false;
      } else {
         Land land = this.landManager.getLand(p, name);
         if (land == null) {
            return false;
         } else {
            LandSpawn landSpawn = (LandSpawn)this.spawnHash.get(land);
            if (landSpawn == null) {
               p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(330)}));
               return false;
            } else if (!this.landManager.getBanTpHandler().checkTp(p, land)) {
               return false;
            } else {
               Pos pos = landSpawn.getSpawn();
               Location l = Pos.toLoc(pos);
               l.getChunk().load(true);
               l.setYaw(landSpawn.getYaw());
               l.setPitch(landSpawn.getPitch());
               long delay = (long)this.delay;
               String vip = "§m";
               if (UtilPer.hasPer(p, this.noDelayPer)) {
                  delay = 0L;
                  vip = "";
               }

               TpDelay tpr = new TpDelay(p, l);
               this.server.getScheduler().scheduleSyncDelayedTask(this.landManager.getLandMain(), tpr, delay * 20L);
               p.sendMessage(UtilFormat.format(this.pn, "landShow6", new Object[]{delay, vip}));
               return true;
            }
         }
      }
   }

   public boolean setTp(Player p, String name) {
      if (!UtilPer.checkPer(p, this.perSet)) {
         return false;
      } else {
         Land land = this.landManager.getLand(p, name);
         if (land == null) {
            return false;
         } else if (!land.getOwner().equals(p.getName()) && !UtilPer.hasPer(p, this.per_land_admin)) {
            p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(740)}));
            return false;
         } else if (land.isFix()) {
            p.sendMessage(this.get(1220));
            return false;
         } else if (!p.isOp() && land.getType() == 2) {
            p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(1870)}));
            return false;
         } else {
            for(Land checkLand : this.landManager.getLands(p.getLocation())) {
               if (land.equals(checkLand)) {
                  this.setTp(land, p.getLocation());
                  p.sendMessage(UtilFormat.format((String)null, "success", new Object[]{this.get(345)}));
                  return true;
               }
            }

            p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(340)}));
            return false;
         }
      }
   }

   public void setTp(Land land, Location spawn) {
      LandSpawn landSpawn = new LandSpawn(land.getId(), Pos.getPos(spawn), spawn.getYaw(), spawn.getPitch());
      this.spawnHash.put(land, landSpawn);
      this.landManager.addLandSpawn(landSpawn);
   }

   private void loadConfig(YamlConfiguration config) {
      this.per_land_admin = config.getString("per_land_admin");
      this.perTp = config.getString("tp.perTp");
      this.perSet = config.getString("tp.perSet");
      this.noDelayPer = config.getString("tp.noDelayPer");
      this.delay = config.getInt("tp.delay");
      this.interval = config.getInt("tp.interval");
   }

   private void loadData() {
      this.spawnHash = new HashMap();

      for(LandSpawn landSpawn : this.landManager.getAllLandSpawns()) {
         Land land = this.landManager.getLand(landSpawn.getLandId());
         if (land != null) {
            this.spawnHash.put(land, landSpawn);
         }
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   class TpDelay implements Runnable {
      private Player p;
      private Location l;
      private Location tarLoc;

      public TpDelay(Player p, Location tarLoc) {
         this.p = p;
         this.l = p.getLocation().clone();
         this.tarLoc = tarLoc;
      }

      public void run() {
         if (this.p != null && this.p.isOnline() && !this.p.isDead()) {
            if (this.l.getWorld().equals(this.p.getWorld()) && !(this.l.distance(this.p.getLocation()) > (double)1.0F)) {
               if (this.tarLoc.getChunk().load(true)) {
                  Util.tp(this.p, this.tarLoc, false, true);
                  this.p.sendMessage(UtilFormat.format((String)null, "success", new Object[]{TpHandler.this.get(335)}));
               } else {
                  this.p.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{TpHandler.this.get(337)}));
               }

            } else {
               this.p.sendMessage(TpHandler.this.get(710));
            }
         }
      }
   }
}
