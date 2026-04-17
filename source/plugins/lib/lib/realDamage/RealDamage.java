package lib.realDamage;

import lib.Lib;
import lib.config.ReloadConfigEvent;
import lib.util.Util;
import lib.util.UtilFormat;
import lib.util.UtilNames;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;

public class RealDamage implements Listener {
   private Lib lib;
   private Server server;
   private PluginManager pm;
   private String pn;
   private BukkitScheduler scheduler;
   private boolean debug;
   private double ignoreDamage;
   private double ignoreSetDamage;

   public RealDamage(Lib lib) {
      this.lib = lib;
      this.server = lib.getServer();
      this.pm = this.server.getPluginManager();
      this.pn = lib.getPn();
      this.scheduler = this.server.getScheduler();
      this.loadConfig(lib.getCon().getConfig(this.pn));
      this.pm.registerEvents(this, lib);
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
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
      try {
         if (e.getEntity() instanceof LivingEntity) {
            Entity damager = e.getDamager();
            boolean projectile = false;
            if (damager instanceof Projectile) {
               damager = ((Projectile)damager).getShooter();
               projectile = true;
            }

            if (damager instanceof LivingEntity) {
               LivingEntity trueDamager = (LivingEntity)damager;
               LivingEntity victim = (LivingEntity)e.getEntity();
               if (trueDamager == null || victim == null) {
                  return;
               }

               Check c = new Check(e, trueDamager, victim, victim.getHealth(), projectile);
               this.scheduler.scheduleSyncDelayedTask(this.lib, c);
            }
         }
      } catch (Exception var7) {
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.debug = config.getBoolean("realDamage.debug");
      this.ignoreDamage = config.getDouble("realDamage.ignoreDamage");
      if (this.ignoreDamage < (double)0.0F) {
         this.ignoreDamage = (double)0.0F;
      }

      this.ignoreSetDamage = config.getDouble("realDamage.ignoreSetDamage");
      if (this.ignoreSetDamage < (double)0.0F) {
         this.ignoreSetDamage = (double)0.0F;
      }

   }

   private class Check implements Runnable {
      private EntityDamageByEntityEvent e;
      private LivingEntity damager;
      private LivingEntity victim;
      private double health;
      private boolean projectile;

      public Check(EntityDamageByEntityEvent e, LivingEntity damager, LivingEntity victim, double health, boolean projectile) {
         this.e = e;
         this.damager = damager;
         this.victim = victim;
         this.health = health;
         this.projectile = projectile;
      }

      public void run() {
         double damage = this.health - this.victim.getHealth();
         if (damage >= RealDamage.this.ignoreDamage) {
            RealDamageEvent realDamageEvent = new RealDamageEvent(this.e, this.damager, this.victim, damage, this.projectile);
            RealDamage.this.pm.callEvent(realDamageEvent);
            if (RealDamage.this.debug) {
               String msg = UtilFormat.format(RealDamage.this.pn, "preDamage", UtilNames.getEntityName(this.damager), UtilNames.getEntityName(this.victim), damage, realDamageEvent.getDamage());
               Util.sendConsoleMessage(msg);
               if (this.damager instanceof Player) {
                  ((Player)this.damager).sendMessage(msg);
               }

               if (this.victim instanceof Player) {
                  ((Player)this.victim).sendMessage(msg);
               }
            }

            double diff = realDamageEvent.getDamage() - damage;
            if (Math.abs(diff) < RealDamage.this.ignoreSetDamage) {
               return;
            }

            if (diff > (double)0.0F) {
               this.victim.setLastDamage((double)0.0F);
               this.victim.damage(diff);
            } else {
               this.victim.setHealth(Math.min(this.victim.getMaxHealth(), this.victim.getHealth() - diff));
            }
         }

      }
   }
}
