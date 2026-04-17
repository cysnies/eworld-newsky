package flag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import land.Land;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.time.TimeEvent;
import lib.util.UtilConfig;
import org.bukkit.Bukkit;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PotionHandler implements Listener {
   private static final String FLAG_POTION = "potion";
   private LandManager landManager;
   private String pn;
   private boolean use;
   private String per;
   private String tip;
   private int interval;
   private HashMap infoHash;

   public PotionHandler(LandManager landManager) {
      this.landManager = landManager;
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(landManager.getLandMain().getPn()));
      landManager.registerEvents(this);
      landManager.register("potion", this.tip, this.use, true, true, this.per);
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
      priority = EventPriority.LOW
   )
   public void onTime(TimeEvent e) {
      if (TimeEvent.getTime() % (long)this.interval == 0L) {
         Player[] var5;
         for(Player p : var5 = Bukkit.getServer().getOnlinePlayers()) {
            Land land = this.landManager.getHighestPriorityLand(p.getLocation());
            if (land != null && land.hasFlag("potion") && !land.hasPer("potion", p.getName())) {
               this.check(p, land.getFlag("potion"));
            }
         }
      }

   }

   private void check(Player p, int flag) {
      List<PotionInfo> list = (List)this.infoHash.get(flag);
      if (list != null) {
         for(PotionInfo pi : list) {
            boolean result = false;

            for(PotionEffect pe : p.getActivePotionEffects()) {
               if (pe.getType().equals(pi.type) && pe.getDuration() > 25) {
                  result = true;
                  break;
               }
            }

            if (!result) {
               p.addPotionEffect(new PotionEffect(pi.type, pi.last, pi.level, false), true);
            }
         }
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.use = config.getBoolean("potion.use");
      this.per = config.getString("potion.per");
      this.tip = config.getString("potion.tip");
      this.interval = config.getInt("potion.interval");
      this.infoHash = new HashMap();
      MemorySection ms = (MemorySection)config.get("potion.infos");

      for(String s : ms.getValues(false).keySet()) {
         List<PotionInfo> list = new ArrayList();
         int id = Integer.parseInt(s.substring(4, s.length()));

         for(String check : ms.getStringList(s)) {
            String[] args = check.split(" ");
            String name = args[0];
            int last = Integer.parseInt(args[1]);
            int level = Integer.parseInt(args[2]);
            PotionInfo pi = new PotionInfo(PotionEffectType.getByName(name), last, level);
            list.add(pi);
         }

         this.infoHash.put(id, list);
      }

   }

   private class PotionInfo {
      private PotionEffectType type;
      private int last;
      private int level;

      public PotionInfo(PotionEffectType type, int last, int level) {
         this.type = type;
         this.last = last;
         this.level = level;
      }
   }
}
