package com.earth2me.essentials;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.bukkit.Material;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.inventory.ItemStack;

public class EssentialsEntityListener implements Listener {
   private static final Logger LOGGER = Logger.getLogger("Minecraft");
   private final IEssentials ess;
   private static final transient Pattern powertoolPlayer = Pattern.compile("\\{player\\}");

   public EssentialsEntityListener(IEssentials ess) {
      this.ess = ess;
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onEntityDamage(EntityDamageByEntityEvent event) {
      Entity eAttack = event.getDamager();
      Entity eDefend = event.getEntity();
      if (eAttack instanceof Player) {
         User attacker = this.ess.getUser(eAttack);
         if (eDefend instanceof Player) {
            this.onPlayerVsPlayerDamage(event, (Player)eDefend, attacker);
         } else if (eDefend instanceof Ageable) {
            ItemStack hand = attacker.getItemInHand();
            if (hand != null && hand.getType() == Material.MILK_BUCKET) {
               ((Ageable)eDefend).setBaby();
               hand.setType(Material.BUCKET);
               attacker.setItemInHand(hand);
               attacker.updateInventory();
               event.setCancelled(true);
            }
         }

         attacker.updateActivity(true);
      } else if (eAttack instanceof Projectile && eDefend instanceof Player) {
         Entity shooter = ((Projectile)event.getDamager()).getShooter();
         if (shooter instanceof Player) {
            User attacker = this.ess.getUser(shooter);
            this.onPlayerVsPlayerDamage(event, (Player)eDefend, attacker);
            attacker.updateActivity(true);
         }
      }

   }

   private void onPlayerVsPlayerDamage(EntityDamageByEntityEvent event, Player defender, User attacker) {
      if (this.ess.getSettings().getLoginAttackDelay() > 0L && System.currentTimeMillis() < attacker.getLastLogin() + this.ess.getSettings().getLoginAttackDelay() && !attacker.isAuthorized("essentials.pvpdelay.exempt")) {
         event.setCancelled(true);
      }

      if (!defender.equals(attacker.getBase()) && (attacker.hasInvulnerabilityAfterTeleport() || this.ess.getUser(defender).hasInvulnerabilityAfterTeleport())) {
         event.setCancelled(true);
      }

      if (attacker.isGodModeEnabled() && !attacker.isAuthorized("essentials.god.pvp")) {
         event.setCancelled(true);
      }

      if (attacker.isHidden() && !attacker.isAuthorized("essentials.vanish.pvp")) {
         event.setCancelled(true);
      }

      this.onPlayerVsPlayerPowertool(event, defender, attacker);
   }

   private void onPlayerVsPlayerPowertool(EntityDamageByEntityEvent event, Player defender, final User attacker) {
      List<String> commandList = attacker.getPowertool(attacker.getItemInHand());
      if (commandList != null && !commandList.isEmpty()) {
         for(String tempCommand : commandList) {
            final String command = powertoolPlayer.matcher(tempCommand).replaceAll(defender.getName());
            if (command != null && !command.isEmpty() && !command.equals(tempCommand)) {
               this.ess.scheduleSyncDelayedTask(new Runnable() {
                  public void run() {
                     attacker.getServer().dispatchCommand(attacker.getBase(), command);
                     EssentialsEntityListener.LOGGER.log(Level.INFO, String.format("[PT] %s issued server command: /%s", attacker.getName(), command));
                  }
               });
               event.setCancelled(true);
               return;
            }
         }
      }

   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onEntityDamage(EntityDamageEvent event) {
      if (event.getEntity() instanceof Player && this.ess.getUser(event.getEntity()).isGodModeEnabled()) {
         Player player = (Player)event.getEntity();
         player.setFireTicks(0);
         player.setRemainingAir(player.getMaximumAir());
         event.setCancelled(true);
      }

   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onEntityCombust(EntityCombustEvent event) {
      if (event.getEntity() instanceof Player && this.ess.getUser(event.getEntity()).isGodModeEnabled()) {
         event.setCancelled(true);
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerDeathEvent(PlayerDeathEvent event) {
      User user = this.ess.getUser(event.getEntity());
      if (user.isAuthorized("essentials.back.ondeath") && !this.ess.getSettings().isCommandDisabled("back")) {
         user.setLastLocation();
         user.sendMessage(I18n._("backAfterDeath"));
      }

      if (!this.ess.getSettings().areDeathMessagesEnabled()) {
         event.setDeathMessage("");
      }

   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onPlayerDeathExpEvent(PlayerDeathEvent event) {
      User user = this.ess.getUser(event.getEntity());
      if (user.isAuthorized("essentials.keepxp")) {
         event.setKeepLevel(true);
         event.setDroppedExp(0);
      }

   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onFoodLevelChange(FoodLevelChangeEvent event) {
      if (event.getEntity() instanceof Player && this.ess.getUser(event.getEntity()).isGodModeEnabled()) {
         Player player = (Player)event.getEntity();
         player.setFoodLevel(20);
         player.setSaturation(10.0F);
         event.setCancelled(true);
      }

   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onEntityRegainHealth(EntityRegainHealthEvent event) {
      if (event.getRegainReason() == RegainReason.SATIATED && event.getEntity() instanceof Player && this.ess.getUser(event.getEntity()).isAfk() && this.ess.getSettings().getFreezeAfkPlayers()) {
         event.setCancelled(true);
      }

   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onPotionSplashEvent(PotionSplashEvent event) {
      for(LivingEntity entity : event.getAffectedEntities()) {
         if (entity instanceof Player && this.ess.getUser(entity).isGodModeEnabled()) {
            event.setIntensity(entity, (double)0.0F);
         }
      }

   }
}
