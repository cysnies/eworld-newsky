package death;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import lib.config.ReloadConfigEvent;
import lib.hashList.ChanceHashList;
import lib.hashList.ChanceHashListImpl;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilNames;
import lib.util.UtilPer;
import org.bukkit.Server;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerQuitEvent;

public class DeathMessage implements Listener {
   private static final String CHECKPER1 = "per.death.check1";
   private static final String CHECKPER2 = "per.death.check2";
   private static final String LIB = "lib";
   private Death death;
   private Server server;
   private String pn;
   private int dur;
   private String per_death_msg;
   private boolean displayName;
   private boolean customName;
   private int killTime;
   private HashMap killMsgHash;
   private int max;
   private HashMap lastKillHash = new HashMap();
   private HashMap killHash = new HashMap();
   private ChanceHashList explosion;
   private ChanceHashList contact;
   private ChanceHashList drowning;
   private ChanceHashList entityAttack;
   private ChanceHashList fall;
   private ChanceHashList fallingBlock;
   private ChanceHashList fire;
   private ChanceHashList lava;
   private ChanceHashList lighting;
   private ChanceHashList poison;
   private ChanceHashList starvation;
   private ChanceHashList suicide;
   private ChanceHashList voidDeath;
   private ChanceHashList other;

   public DeathMessage(Death death) {
      this.death = death;
      this.server = death.getServer();
      this.pn = death.getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      death.getPm().registerEvents(this, death);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.death.getPn())) {
         this.loadConfig(e.getConfig());
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onPlayerDeath(PlayerDeathEvent e) {
      e.setDeathMessage("");
      String msg = this.die(e.getEntity());

      Player[] var6;
      for(Player p : var6 = this.server.getOnlinePlayers()) {
         switch (this.getMode(p)) {
            case 0:
               p.sendMessage(msg);
               break;
            case 1:
               Util.sendItemMessage(p, msg, this.dur);
         }
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerQuit(PlayerQuitEvent e) {
      this.lastKillHash.remove(e.getPlayer());
   }

   public String die(Player p) {
      String result = "";
      Player damager = p.getKiller();
      if (damager != null && damager.isOnline() && !damager.equals(p) && p.getLastDamage() > 0.1) {
         long now = System.currentTimeMillis();
         if (!this.killHash.containsKey(damager)) {
            this.killHash.put(damager, 0);
         }

         int amount = (Integer)this.killHash.get(damager) + 1;
         this.killHash.put(damager, amount);
         if (this.lastKillHash.containsKey(damager) && now - (Long)this.lastKillHash.get(damager) < (long)this.killTime) {
            String tip;
            if (amount <= this.max) {
               tip = (String)this.killMsgHash.get(amount);
            } else {
               tip = (String)this.killMsgHash.get(this.max);
            }

            String name;
            String name2;
            if (this.displayName) {
               name = damager.getDisplayName();
               name2 = p.getDisplayName();
            } else {
               name = damager.getName();
               name2 = p.getName();
            }

            if (tip != null) {
               result = tip.replace("{0}", name).replace("{1}", name2);
            }
         } else {
            this.killHash.put(damager, 1);
         }

         this.lastKillHash.put(damager, now);
      }

      if (result.isEmpty()) {
         EntityDamageEvent lastDamage = p.getLastDamageCause();
         if (lastDamage != null) {
            EntityDamageEvent.DamageCause damageCause = lastDamage.getCause();
            if (!damageCause.equals(DamageCause.BLOCK_EXPLOSION) && !damageCause.equals(DamageCause.ENTITY_EXPLOSION)) {
               if (damageCause.equals(DamageCause.CONTACT)) {
                  result = (String)this.contact.getRandom();
               } else if (damageCause.equals(DamageCause.DROWNING)) {
                  result = (String)this.drowning.getRandom();
               } else if (damageCause.equals(DamageCause.FALL)) {
                  result = (String)this.fall.getRandom();
               } else if (damageCause.equals(DamageCause.FALLING_BLOCK)) {
                  result = (String)this.fallingBlock.getRandom();
               } else if (!damageCause.equals(DamageCause.FIRE) && !damageCause.equals(DamageCause.FIRE_TICK)) {
                  if (damageCause.equals(DamageCause.LAVA)) {
                     result = (String)this.lava.getRandom();
                  } else if (damageCause.equals(DamageCause.LIGHTNING)) {
                     result = (String)this.lighting.getRandom();
                  } else if (damageCause.equals(DamageCause.POISON)) {
                     result = (String)this.poison.getRandom();
                  } else if (damageCause.equals(DamageCause.STARVATION)) {
                     result = (String)this.starvation.getRandom();
                  } else if (damageCause.equals(DamageCause.SUICIDE)) {
                     result = (String)this.suicide.getRandom();
                  } else if (damageCause.equals(DamageCause.VOID)) {
                     result = (String)this.voidDeath.getRandom();
                  } else if (!damageCause.equals(DamageCause.ENTITY_ATTACK) && !damageCause.equals(DamageCause.PROJECTILE)) {
                     result = (String)this.other.getRandom();
                  } else if (lastDamage instanceof EntityDamageByEntityEvent) {
                     EntityDamageByEntityEvent entityDamageByEntity = (EntityDamageByEntityEvent)lastDamage;
                     Entity killer = entityDamageByEntity.getDamager();
                     String killerName = "";
                     if (killer == null) {
                        killerName = this.get(165);
                     } else {
                        if (killer instanceof Projectile) {
                           killer = ((Projectile)killer).getShooter();
                        }

                        killerName = UtilNames.getEntityName(killer, this.customName, true);
                        if (killerName.isEmpty()) {
                           killerName = this.get(165);
                        }
                     }

                     result = ((String)this.entityAttack.getRandom()).replace("{k}", killerName);
                  } else {
                     result = (String)this.other.getRandom();
                  }
               } else {
                  result = (String)this.fire.getRandom();
               }
            } else {
               result = (String)this.explosion.getRandom();
            }
         } else {
            result = (String)this.other.getRandom();
         }

         if (this.displayName) {
            result = result.replace("{p}", p.getDisplayName());
         } else {
            result = result.replace("{p}", p.getName());
         }
      }

      return result;
   }

   public int getMode(Player p) {
      if (UtilPer.hasPer(p, "per.death.check1")) {
         return 1;
      } else {
         return UtilPer.hasPer(p, "per.death.check2") ? 2 : 0;
      }
   }

   public String getModeShow(Player p) {
      switch (this.getMode(p)) {
         case 0:
            return this.get(155);
         case 1:
            return this.get(150);
         case 2:
            return this.get(160);
         default:
            return "";
      }
   }

   public void toggleDeathMsg(Player p) {
      if (UtilPer.checkPer(p, this.per_death_msg)) {
         int mode = this.getMode(p) + 1;
         if (mode >= 3) {
            mode = 0;
         }

         this.setMode(p, mode);
         switch (mode) {
            case 0:
               p.sendMessage(UtilFormat.format("lib", "success", new Object[]{this.get(137)}));
               break;
            case 1:
               p.sendMessage(UtilFormat.format("lib", "success", new Object[]{this.get(135)}));
               break;
            case 2:
               p.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(140)}));
         }

      }
   }

   private void setMode(Player p, int mode) {
      switch (mode) {
         case 0:
            UtilPer.remove(p, "per.death.check1");
            UtilPer.remove(p, "per.death.check2");
            break;
         case 1:
            UtilPer.add(p, "per.death.check1");
            UtilPer.remove(p, "per.death.check2");
            break;
         case 2:
            UtilPer.remove(p, "per.death.check1");
            UtilPer.add(p, "per.death.check2");
      }

   }

   private void loadConfig(YamlConfiguration config) {
      try {
         this.dur = config.getInt("dur");
         this.per_death_msg = config.getString("per_death_msg");
         this.displayName = config.getBoolean("displayName");
         this.customName = config.getBoolean("customName");
         this.killTime = config.getInt("killTime") * 1000;
         String deathMsgPath = config.getString("deathMsg");
         YamlConfiguration deathMsgConfig = new YamlConfiguration();
         deathMsgConfig.load((new File(deathMsgPath)).getCanonicalPath());
         this.explosion = new ChanceHashListImpl();
         this.contact = new ChanceHashListImpl();
         this.drowning = new ChanceHashListImpl();
         this.entityAttack = new ChanceHashListImpl();
         this.fall = new ChanceHashListImpl();
         this.fallingBlock = new ChanceHashListImpl();
         this.fire = new ChanceHashListImpl();
         this.lava = new ChanceHashListImpl();
         this.lighting = new ChanceHashListImpl();
         this.poison = new ChanceHashListImpl();
         this.starvation = new ChanceHashListImpl();
         this.suicide = new ChanceHashListImpl();
         this.voidDeath = new ChanceHashListImpl();
         this.other = new ChanceHashListImpl();

         for(String s : deathMsgConfig.getStringList("deathMessage.explosion")) {
            this.explosion.add(Util.convert(s));
         }

         for(String s : deathMsgConfig.getStringList("deathMessage.contact")) {
            this.contact.add(Util.convert(s));
         }

         for(String s : deathMsgConfig.getStringList("deathMessage.drowning")) {
            this.drowning.add(Util.convert(s));
         }

         for(String s : deathMsgConfig.getStringList("deathMessage.entityAttack")) {
            this.entityAttack.add(Util.convert(s));
         }

         for(String s : deathMsgConfig.getStringList("deathMessage.fall")) {
            this.fall.add(Util.convert(s));
         }

         for(String s : deathMsgConfig.getStringList("deathMessage.fallingBlock")) {
            this.fallingBlock.add(Util.convert(s));
         }

         for(String s : deathMsgConfig.getStringList("deathMessage.fire")) {
            this.fire.add(Util.convert(s));
         }

         for(String s : deathMsgConfig.getStringList("deathMessage.lava")) {
            this.lava.add(Util.convert(s));
         }

         for(String s : deathMsgConfig.getStringList("deathMessage.lighting")) {
            this.lighting.add(Util.convert(s));
         }

         for(String s : deathMsgConfig.getStringList("deathMessage.poison")) {
            this.poison.add(Util.convert(s));
         }

         for(String s : deathMsgConfig.getStringList("deathMessage.starvation")) {
            this.starvation.add(Util.convert(s));
         }

         for(String s : deathMsgConfig.getStringList("deathMessage.suicide")) {
            this.suicide.add(Util.convert(s));
         }

         for(String s : deathMsgConfig.getStringList("deathMessage.voidDeath")) {
            this.voidDeath.add(Util.convert(s));
         }

         for(String s : deathMsgConfig.getStringList("deathMessage.other")) {
            this.other.add(Util.convert(s));
         }

         this.killMsgHash = new HashMap();

         for(String s : deathMsgConfig.getStringList("killMsg")) {
            String[] ss = s.split(" ");
            int amount = Integer.parseInt(ss[0]);
            String tip = Util.convert(Util.combine(ss, " ", 1, ss.length));
            this.killMsgHash.put(amount, tip);
            if (amount > this.max) {
               this.max = amount;
            }
         }
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (InvalidConfigurationException e) {
         e.printStackTrace();
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
