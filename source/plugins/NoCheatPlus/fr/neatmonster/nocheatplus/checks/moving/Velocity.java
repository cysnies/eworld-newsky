package fr.neatmonster.nocheatplus.checks.moving;

import fr.neatmonster.nocheatplus.utilities.TickTask;

public class Velocity {
   public final int tick;
   public double value;
   public double sum;
   public int actCount;
   public int valCount;

   public Velocity(double value, int actCount, int valCount) {
      this.tick = TickTask.getTick();
      this.value = value;
      this.actCount = actCount;
      this.valCount = valCount;
   }

   public Velocity(int tick, double value, int actCount, int valCount) {
      this.tick = tick;
      this.value = value;
      this.actCount = actCount;
      this.valCount = valCount;
   }

   public String toString() {
      return "Velocity(tick=" + this.tick + " sum=" + this.sum + " value=" + this.value + " valid=" + this.valCount + " activate=" + this.actCount + ")";
   }
}
