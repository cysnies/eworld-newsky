package fr.neatmonster.nocheatplus.checks.combined;

import fr.neatmonster.nocheatplus.checks.CheckListener;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;

public class CombinedListener extends CheckListener {
   protected final Improbable improbable = (Improbable)this.addCheck(new Improbable());
   protected final MunchHausen munchHausen = (MunchHausen)this.addCheck(new MunchHausen());

   public CombinedListener() {
      super(CheckType.COMBINED);
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      CombinedData data = CombinedData.getData(player);
      CombinedConfig cc = CombinedConfig.getConfig(player);
      if (cc.invulnerableCheck && (cc.invulnerableTriggerAlways || cc.invulnerableTriggerFallDistance && player.getFallDistance() > 0.0F)) {
         int ticks = cc.invulnerableInitialTicksJoin >= 0 ? cc.invulnerableInitialTicksJoin : this.mcAccess.getInvulnerableTicks(player);
         data.invulnerableTick = TickTask.getTick() + ticks;
         this.mcAccess.setInvulnerableTicks(player, 0);
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onEntityDamage(EntityDamageEvent event) {
      Entity entity = event.getEntity();
      if (entity instanceof Player) {
         Player player = (Player)entity;
         CombinedConfig cc = CombinedConfig.getConfig(player);
         if (cc.invulnerableCheck) {
            EntityDamageEvent.DamageCause cause = event.getCause();
            if (!cc.invulnerableIgnore.contains(cause)) {
               Integer modifier = (Integer)cc.invulnerableModifiers.get(cause);
               if (modifier == null) {
                  modifier = cc.invulnerableModifierDefault;
               }

               CombinedData data = CombinedData.getData(player);
               if (TickTask.getTick() < data.invulnerableTick + modifier) {
                  event.setCancelled(true);
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onPlayerToggleSprintHighest(PlayerToggleSprintEvent event) {
      if (event.isCancelled() && event.isSprinting()) {
         event.setCancelled(false);
      }

      Improbable.feed(event.getPlayer(), 0.35F, System.currentTimeMillis());
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
      Improbable.feed(event.getPlayer(), 0.35F, System.currentTimeMillis());
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerFish(PlayerFishEvent event) {
      Player player = event.getPlayer();
      if (this.munchHausen.isEnabled(player) && this.munchHausen.checkFish(player, event.getCaught(), event.getState())) {
         event.setCancelled(true);
      }

   }
}
