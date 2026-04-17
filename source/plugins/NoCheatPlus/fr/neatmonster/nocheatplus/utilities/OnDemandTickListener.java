package fr.neatmonster.nocheatplus.utilities;

import fr.neatmonster.nocheatplus.components.TickListener;

public abstract class OnDemandTickListener implements TickListener {
   protected boolean isRegistered = false;

   public abstract boolean delegateTick(int var1, long var2);

   public void onTick(int tick, long timeLast) {
      if (this.isRegistered) {
         if (!this.delegateTick(tick, timeLast)) {
            this.unRegister();
         }

      }
   }

   public OnDemandTickListener register() {
      return this.register(false);
   }

   public OnDemandTickListener register(boolean force) {
      if (force || !this.isRegistered) {
         TickTask.addTickListener(this);
      }

      return this;
   }

   public OnDemandTickListener unRegister() {
      return this.unRegister(false);
   }

   public OnDemandTickListener unRegister(boolean force) {
      if (force || this.isRegistered) {
         TickTask.removeTickListener(this);
      }

      return this;
   }

   public void setRegistered(boolean registered) {
      this.isRegistered = registered;
   }

   public boolean isRegistered() {
      return this.isRegistered;
   }
}
