package fr.neatmonster.nocheatplus.checks.fight;

import fr.neatmonster.nocheatplus.checks.CheckListener;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.combined.Combined;
import fr.neatmonster.nocheatplus.checks.combined.Improbable;
import fr.neatmonster.nocheatplus.checks.inventory.Items;
import fr.neatmonster.nocheatplus.checks.moving.MovingConfig;
import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.checks.moving.MovingListener;
import fr.neatmonster.nocheatplus.compat.BridgeHealth;
import fr.neatmonster.nocheatplus.components.JoinLeaveListener;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import fr.neatmonster.nocheatplus.utilities.TrigUtil;
import fr.neatmonster.nocheatplus.utilities.build.BuildParameters;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class FightListener extends CheckListener implements JoinLeaveListener {
   private final Angle angle = (Angle)this.addCheck(new Angle());
   private final Critical critical = (Critical)this.addCheck(new Critical());
   private final Direction direction = (Direction)this.addCheck(new Direction());
   private final FastHeal fastHeal = (FastHeal)this.addCheck(new FastHeal());
   private final GodMode godMode = (GodMode)this.addCheck(new GodMode());
   private final Knockback knockback = (Knockback)this.addCheck(new Knockback());
   private final NoSwing noSwing = (NoSwing)this.addCheck(new NoSwing());
   private final Reach reach = (Reach)this.addCheck(new Reach());
   private final SelfHit selfHit = (SelfHit)this.addCheck(new SelfHit());
   private final Speed speed = (Speed)this.addCheck(new Speed());

   public FightListener() {
      super(CheckType.FIGHT);
   }

   private boolean handleNormalDamage(Player player, Entity damaged, double damage) {
      FightConfig cc = FightConfig.getConfig(player);
      FightData data = FightData.getData(player);
      ItemStack stack = player.getItemInHand();
      if (Items.checkIllegalEnchantments(player, stack)) {
         return true;
      } else {
         boolean cancelled = false;
         String worldName = player.getWorld().getName();
         int tick = TickTask.getTick();
         long now = System.currentTimeMillis();
         boolean worldChanged = !worldName.equals(data.lastWorld);
         Location loc = player.getLocation();
         Location targetLoc = damaged.getLocation();
         double normalizedMove;
         if (data.lastAttackedX != (double)Integer.MAX_VALUE && tick >= data.lastAttackTick && !worldChanged && tick - data.lastAttackTick <= 20) {
            int tickAge = tick - data.lastAttackTick;
            double targetMove = TrigUtil.distance(data.lastAttackedX, data.lastAttackedZ, targetLoc.getX(), targetLoc.getZ());
            long msAge = (long)(50.0F * TickTask.getLag(50L * (long)tickAge) * (float)tickAge);
            normalizedMove = msAge == 0L ? targetMove : targetMove * Math.min((double)20.0F, (double)1000.0F / (double)msAge);
         } else {
            int tickAge = 0;
            double targetMove = (double)0.0F;
            normalizedMove = (double)0.0F;
            long msAge = 0L;
         }

         if (damaged instanceof Player) {
            Player damagedPlayer = (Player)damaged;
            if (cc.debug && damagedPlayer.hasPermission("nocheatplus.admin.debug")) {
               damagedPlayer.sendMessage("Attacked by " + player.getName() + ": inv=" + this.mcAccess.getInvulnerableTicks(damagedPlayer) + " ndt=" + damagedPlayer.getNoDamageTicks());
            }

            if (this.selfHit.isEnabled(player) && this.selfHit.check(player, damagedPlayer, data, cc)) {
               cancelled = true;
            }
         }

         if (cc.cancelDead) {
            if (damaged.isDead()) {
               cancelled = true;
            }

            if (player.isDead() && data.damageTakenByEntityTick != (long)TickTask.getTick()) {
               cancelled = true;
            }
         }

         if (damage <= (double)4.0F && (long)tick == data.damageTakenByEntityTick && data.thornsId != Integer.MIN_VALUE && data.thornsId == damaged.getEntityId()) {
            data.thornsId = Integer.MIN_VALUE;
            return cancelled;
         } else {
            data.thornsId = Integer.MIN_VALUE;
            if (!cancelled && this.speed.isEnabled(player)) {
               if (this.speed.check(player, now)) {
                  cancelled = true;
                  if (data.speedVL > (double)50.0F) {
                     Improbable.check(player, 2.0F, now, "fight.speed");
                  } else {
                     Improbable.feed(player, 2.0F, now);
                  }
               } else if (normalizedMove > (double)2.0F && Improbable.check(player, 1.0F, now, "fight.speed")) {
                  cancelled = true;
               }
            }

            if (this.angle.isEnabled(player)) {
               if (Combined.checkYawRate(player, loc.getYaw(), now, worldName, cc.yawRateCheck)) {
                  cancelled = true;
               }

               if (this.angle.check(player, worldChanged)) {
                  cancelled = true;
               }
            }

            if (!cancelled && this.critical.isEnabled(player) && this.critical.check(player)) {
               cancelled = true;
            }

            if (!cancelled && this.knockback.isEnabled(player) && this.knockback.check(player)) {
               cancelled = true;
            }

            if (!cancelled && this.noSwing.isEnabled(player) && this.noSwing.check(player)) {
               cancelled = true;
            }

            if (!cancelled && player.isBlocking() && !player.hasPermission("nocheatplus.checks.moving.survivalfly.blocking")) {
               cancelled = true;
            }

            if (!cancelled && this.reach.isEnabled(player) && this.reach.check(player, damaged)) {
               cancelled = true;
            }

            if (!cancelled && this.direction.isEnabled(player) && this.direction.check(player, damaged)) {
               cancelled = true;
            }

            data.lastWorld = worldName;
            data.lastAttackTick = tick;
            data.lastAttackedX = targetLoc.getX();
            data.lastAttackedY = targetLoc.getY();
            data.lastAttackedZ = targetLoc.getZ();
            if (!cancelled && player.isSprinting() && TrigUtil.distance(loc.getX(), loc.getZ(), targetLoc.getX(), targetLoc.getZ()) < (double)3.0F) {
               MovingData mData = MovingData.getData(player);
               if (mData.fromX != Double.MAX_VALUE) {
                  double hDist = TrigUtil.distance(loc.getX(), loc.getZ(), mData.fromX, mData.fromZ);
                  if (hDist >= 0.23 && mData.sfHorizontalBuffer > (double)0.5F && MovingListener.shouldCheckSurvivalFly(player, mData, MovingConfig.getConfig(player))) {
                     mData.sfHBufExtra = 7;
                     if (cc.debug && BuildParameters.debugLevel > 0) {
                        System.out.println(player.getName() + " attacks, hDist to last from: " + hDist + " | targetdist=" + TrigUtil.distance(loc.getX(), loc.getZ(), targetLoc.getX(), targetLoc.getZ()) + " | sprinting=" + player.isSprinting() + " | food=" + player.getFoodLevel() + " | hbuf=" + mData.sfHorizontalBuffer);
                     }
                  }
               }
            }

            return cancelled;
         }
      }
   }

   public static final boolean hasThorns(Player player) {
      PlayerInventory inv = player.getInventory();
      ItemStack[] contents = inv.getArmorContents();

      for(int i = 0; i < contents.length; ++i) {
         ItemStack stack = contents[i];
         if (stack != null && stack.getEnchantmentLevel(Enchantment.THORNS) > 0) {
            return true;
         }
      }

      return false;
   }

   @EventHandler(
      ignoreCancelled = true,
      priority = EventPriority.LOWEST
   )
   public void onEntityDamage(EntityDamageEvent event) {
      Entity damaged = event.getEntity();
      Player damagedPlayer = damaged instanceof Player ? (Player)damaged : null;
      FightData damagedData = damagedPlayer == null ? null : FightData.getData(damagedPlayer);
      boolean damagedIsDead = damaged.isDead();
      if (damagedPlayer != null && !damagedIsDead) {
         if (!damagedPlayer.isDead() && this.godMode.isEnabled(damagedPlayer) && this.godMode.check(damagedPlayer, BridgeHealth.getDamage(event), damagedData)) {
            damagedPlayer.setNoDamageTicks(0);
         }

         if (BridgeHealth.getHealth(damagedPlayer) == BridgeHealth.getMaxHealth(damagedPlayer)) {
            if (damagedData.fastHealBuffer < 0L) {
               damagedData.fastHealBuffer /= 2L;
            }

            damagedData.fastHealRefTime = System.currentTimeMillis();
         }
      }

      if (event instanceof EntityDamageByEntityEvent) {
         EntityDamageByEntityEvent e = (EntityDamageByEntityEvent)event;
         Entity damager = e.getDamager();
         if (damagedPlayer != null && !damagedIsDead) {
            FightData.getData(damagedPlayer).damageTakenByEntityTick = (long)TickTask.getTick();
            if (hasThorns(damagedPlayer)) {
               damagedData.thornsId = damager.getEntityId();
            } else {
               damagedData.thornsId = Integer.MIN_VALUE;
            }
         }

         if (damager instanceof Player) {
            Player player = (Player)damager;
            if (e.getCause() == DamageCause.ENTITY_ATTACK && this.handleNormalDamage(player, damaged, BridgeHealth.getDamage(e))) {
               e.setCancelled(true);
            }
         }
      }

   }

   @EventHandler(
      ignoreCancelled = true,
      priority = EventPriority.LOWEST
   )
   public void onEntityDamageMonitor(EntityDamageEvent event) {
      Entity damaged = event.getEntity();
      if (damaged instanceof Player) {
         Player player = (Player)damaged;
         FightData data = FightData.getData(player);
         int ndt = player.getNoDamageTicks();
         if (data.lastDamageTick == TickTask.getTick() && data.lastNoDamageTicks != ndt) {
            data.lastNoDamageTicks = ndt;
         }
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   protected void onEntityDeathEvent(EntityDeathEvent event) {
      Entity entity = event.getEntity();
      if (entity instanceof Player) {
         Player player = (Player)entity;
         if (this.godMode.isEnabled(player)) {
            this.godMode.death(player);
         }
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   protected void onPlayerAnimation(PlayerAnimationEvent event) {
      FightData.getData(event.getPlayer()).noSwingArmSwung = true;
   }

   @EventHandler(
      ignoreCancelled = true,
      priority = EventPriority.MONITOR
   )
   public void onPlayerToggleSprint(PlayerToggleSprintEvent event) {
      if (event.isSprinting()) {
         FightData.getData(event.getPlayer()).knockbackSprintTime = System.currentTimeMillis();
      }

   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onEntityRegainHealthLow(EntityRegainHealthEvent event) {
      Entity entity = event.getEntity();
      if (entity instanceof Player) {
         Player player = (Player)entity;
         if (event.getRegainReason() == RegainReason.SATIATED) {
            if (this.fastHeal.isEnabled(player) && this.fastHeal.check(player)) {
               event.setCancelled(true);
            }

         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onEntityRegainHealth(EntityRegainHealthEvent event) {
      Entity entity = event.getEntity();
      if (entity instanceof Player) {
         Player player = (Player)entity;
         FightData data = FightData.getData(player);
         data.regainHealthTime = System.currentTimeMillis();
         double health = Math.min(BridgeHealth.getHealth(player) + BridgeHealth.getAmount(event), BridgeHealth.getMaxHealth(player));
         data.godModeHealth = Math.max(data.godModeHealth, health);
      }
   }

   public void playerJoins(Player player) {
   }

   public void playerLeaves(Player player) {
      FightData data = FightData.getData(player);
      data.angleHits.clear();
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
      FightData data = FightData.getData(event.getPlayer());
      data.angleHits.clear();
   }
}
