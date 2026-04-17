package lib.realDamage;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class RealDamageEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private EntityDamageByEntityEvent entityDamageByEntityEvent;
   private LivingEntity damager;
   private LivingEntity victim;
   private double damage;
   private boolean projectile;

   public RealDamageEvent(EntityDamageByEntityEvent entityDamageByEntityEvent, LivingEntity damager, LivingEntity le, double damage, boolean projectile) {
      this.entityDamageByEntityEvent = entityDamageByEntityEvent;
      this.damager = damager;
      this.victim = le;
      if (damage >= (double)0.0F) {
         this.damage = damage;
      }

      this.projectile = projectile;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public double getDamage() {
      return this.damage;
   }

   public EntityDamageByEntityEvent getEntityDamageByEntityEvent() {
      return this.entityDamageByEntityEvent;
   }

   public LivingEntity getVictim() {
      return this.victim;
   }

   public void setDamage(double damage) {
      if (damage >= (double)0.0F) {
         this.damage = damage;
      }

   }

   public boolean isProjectile() {
      return this.projectile;
   }

   public LivingEntity getDamager() {
      return this.damager;
   }
}
