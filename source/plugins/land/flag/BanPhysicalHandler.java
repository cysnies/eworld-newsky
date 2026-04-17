package flag;

import java.util.HashMap;
import land.Land;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.util.Util;
import lib.util.UtilConfig;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.world.StructureGrowEvent;

public class BanPhysicalHandler implements Listener {
   private static final String FLAG_BAN_PHYSICAL = "banPhysical";
   private LandManager landManager;
   private String pn;
   private boolean use;
   private String per;
   private String tip;
   private HashMap infoHash;

   public BanPhysicalHandler(LandManager landManager) {
      this.landManager = landManager;
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      landManager.registerEvents(this);
      landManager.register("banPhysical", this.tip, this.use, false, true, this.per);
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
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBlockBurn(BlockBurnEvent e) {
      Land land = this.landManager.getHighestPriorityLand(e.getBlock().getLocation());
      if (land != null && land.hasFlag("banPhysical")) {
         Info info = (Info)this.infoHash.get(land.getFlag("banPhysical"));
         if (info != null && info.burn) {
            e.setCancelled(true);
         }
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBlockFade(BlockFadeEvent e) {
      Land land = this.landManager.getHighestPriorityLand(e.getBlock().getLocation());
      if (land != null && land.hasFlag("banPhysical")) {
         Info info = (Info)this.infoHash.get(land.getFlag("banPhysical"));
         if (info != null && info.fade) {
            e.setCancelled(true);
         }
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBlockForm(BlockFormEvent e) {
      Land land = this.landManager.getHighestPriorityLand(e.getBlock().getLocation());
      if (land != null && land.hasFlag("banPhysical")) {
         Info info = (Info)this.infoHash.get(land.getFlag("banPhysical"));
         if (info != null && info.form) {
            e.setCancelled(true);
         }
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBlockGrow(BlockGrowEvent e) {
      Land land = this.landManager.getHighestPriorityLand(e.getBlock().getLocation());
      if (land != null && land.hasFlag("banPhysical")) {
         Info info = (Info)this.infoHash.get(land.getFlag("banPhysical"));
         if (info != null && info.grow) {
            e.setCancelled(true);
         }
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBlockIgnite(BlockIgniteEvent e) {
      Land land = this.landManager.getHighestPriorityLand(e.getBlock().getLocation());
      if (land != null && land.hasFlag("banPhysical")) {
         Info info = (Info)this.infoHash.get(land.getFlag("banPhysical"));
         if (info != null && info.ignite) {
            e.setCancelled(true);
         }
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBlockSpread(BlockSpreadEvent e) {
      Land land = this.landManager.getHighestPriorityLand(e.getSource().getLocation());
      if (land != null && land.hasFlag("banPhysical")) {
         Info info = (Info)this.infoHash.get(land.getFlag("banPhysical"));
         if (info != null && info.spread) {
            e.setCancelled(true);
         }
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onLeavesDecay(LeavesDecayEvent e) {
      Land land = this.landManager.getHighestPriorityLand(e.getBlock().getLocation());
      if (land != null && land.hasFlag("banPhysical")) {
         Info info = (Info)this.infoHash.get(land.getFlag("banPhysical"));
         if (info != null && info.decay) {
            e.setCancelled(true);
         }
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onStructureGrow(StructureGrowEvent e) {
      Land land = this.landManager.getHighestPriorityLand(e.getLocation());
      if (land != null && land.hasFlag("banPhysical")) {
         Info info = (Info)this.infoHash.get(land.getFlag("banPhysical"));
         if (info != null && info.structure) {
            e.setCancelled(true);
         }
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBlockPhysics(BlockPhysicsEvent e) {
      Land land = this.landManager.getHighestPriorityLand(e.getBlock().getLocation());
      if (land != null && land.hasFlag("banPhysical")) {
         Info info = (Info)this.infoHash.get(land.getFlag("banPhysical"));
         if (info != null && info.physics) {
            e.setCancelled(true);
         }
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.use = config.getBoolean("banPhysical.use");
      this.per = config.getString("banPhysical.per");
      this.tip = Util.convert(config.getString("banPhysical.tip"));
      this.infoHash = new HashMap();
      MemorySection ms = (MemorySection)config.get("banPhysical.infos");

      for(String key : ms.getValues(false).keySet()) {
         int id = Integer.parseInt(key.substring(4));
         boolean burn = ms.getBoolean(key + ".burn");
         boolean fade = ms.getBoolean(key + ".fade");
         boolean form = ms.getBoolean(key + ".form");
         boolean grow = ms.getBoolean(key + ".grow");
         boolean ignite = ms.getBoolean(key + ".ignite");
         boolean spread = ms.getBoolean(key + ".spread");
         boolean decay = ms.getBoolean(key + ".decay");
         boolean structure = ms.getBoolean(key + ".structure");
         boolean physics = ms.getBoolean(key + ".physics");
         this.infoHash.put(id, new Info(burn, fade, form, grow, ignite, spread, decay, structure, physics));
      }

   }

   private static class Info {
      boolean burn;
      boolean fade;
      boolean form;
      boolean grow;
      boolean ignite;
      boolean spread;
      boolean decay;
      boolean structure;
      boolean physics;

      public Info(boolean burn, boolean fade, boolean form, boolean grow, boolean ignite, boolean spread, boolean decay, boolean structure, boolean physics) {
         this.burn = burn;
         this.fade = fade;
         this.form = form;
         this.grow = grow;
         this.ignite = ignite;
         this.spread = spread;
         this.decay = decay;
         this.structure = structure;
         this.physics = physics;
      }
   }
}
