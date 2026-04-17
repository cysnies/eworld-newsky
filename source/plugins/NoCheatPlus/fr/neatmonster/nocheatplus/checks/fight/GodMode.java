package fr.neatmonster.nocheatplus.checks.fight;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.compat.BridgeHealth;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class GodMode extends Check {
   public GodMode() {
      super(CheckType.FIGHT_GODMODE);
   }

   public boolean check(Player player, double damage, FightData data) {
      int tick = TickTask.getTick();
      int noDamageTicks = Math.max(0, player.getNoDamageTicks());
      int invulnerabilityTicks = this.mcAccess.getInvulnerableTicks(player);
      boolean legit = false;
      boolean set = false;
      boolean resetAcc = false;
      boolean resetAll = false;
      int dTick = tick - data.lastDamageTick;
      int dNDT = data.lastNoDamageTicks - noDamageTicks;
      int delta = dTick - dNDT;
      double health = BridgeHealth.getHealth(player);
      if (data.godModeHealth > health) {
         data.godModeHealthDecreaseTick = tick;
         resetAcc = true;
         set = true;
         legit = true;
      }

      if (invulnerabilityTicks > 0 && noDamageTicks != invulnerabilityTicks || tick < data.lastDamageTick) {
         resetAcc = true;
         set = true;
         legit = true;
      }

      if (20 + data.godModeAcc < dTick || dTick > 40) {
         resetAcc = true;
         legit = true;
         set = true;
      }

      if (delta <= 0 || data.lastNoDamageTicks <= player.getMaximumNoDamageTicks() / 2 || dTick > data.lastNoDamageTicks || damage > BridgeHealth.getLastDamage(player) || damage == (double)0.0F) {
         set = true;
         legit = true;
      }

      if (dTick == 1 && noDamageTicks < 19) {
         set = true;
      }

      if (delta == 1) {
         legit = true;
      }

      data.godModeHealth = health;
      if (resetAcc || resetAll) {
         data.godModeAcc = 0;
      }

      if (legit) {
         data.godModeVL *= 0.97;
      }

      if (resetAll) {
         data.lastNoDamageTicks = 0;
         data.lastDamageTick = 0;
         return false;
      } else if (set) {
         data.lastNoDamageTicks = noDamageTicks;
         data.lastDamageTick = tick;
         return false;
      } else if (legit) {
         return false;
      } else {
         if (tick < data.godModeHealthDecreaseTick) {
            data.godModeHealthDecreaseTick = 0;
         } else {
            int dht = tick - data.godModeHealthDecreaseTick;
            if (dht <= 20) {
               return false;
            }
         }

         FightConfig cc = FightConfig.getConfig(player);
         long now = System.currentTimeMillis();
         long maxAge = cc.godModeLagMaxAge;
         long keepAlive = this.mcAccess.getKeepAliveTime(player);
         if (keepAlive > now || keepAlive == Long.MIN_VALUE) {
            keepAlive = CheckUtils.guessKeepAliveTime(player, now, maxAge);
         }

         if ((double)keepAlive != Double.MIN_VALUE && now - keepAlive > cc.godModeLagMinAge && now - keepAlive < maxAge) {
            return false;
         } else {
            data.godModeAcc += delta;
            boolean cancel = false;
            if (data.godModeAcc > 2) {
               data.godModeVL += (double)delta;
               if (this.executeActions(player, data.godModeVL, (double)delta, FightConfig.getConfig(player).godModeActions)) {
                  cancel = true;
               } else {
                  cancel = false;
               }
            } else {
               cancel = false;
            }

            data.lastNoDamageTicks = noDamageTicks;
            data.lastDamageTick = tick;
            return cancel;
         }
      }
   }

   public void death(final Player player) {
      if (BridgeHealth.getHealth(player) <= (double)0.0F && player.isDead()) {
         try {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("NoCheatPlus"), new Runnable() {
               public void run() {
                  try {
                     if (GodMode.this.mcAccess.shouldBeZombie(player)) {
                        GodMode.this.mcAccess.setDead(player, 19);
                     }
                  } catch (Exception var2) {
                  }

               }
            }, 30L);
         } catch (Exception var3) {
         }
      }

   }
}
