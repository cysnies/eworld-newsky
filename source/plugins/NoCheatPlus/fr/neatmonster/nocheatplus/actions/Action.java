package fr.neatmonster.nocheatplus.actions;

import fr.neatmonster.nocheatplus.config.ConfigFileWithActions;

public abstract class Action {
   public final String name;
   public final int delay;
   public final int repeat;

   public Action(String name, int delay, int repeat) {
      this.name = name;
      this.delay = delay;
      this.repeat = repeat;
   }

   public abstract boolean execute(ActionData var1);

   public boolean needsParameters() {
      return false;
   }

   public boolean executesAlways() {
      return this.delay == 0 && this.repeat == 0;
   }

   public Action getOptimizedCopy(ConfigFileWithActions config, Integer threshold) {
      return this;
   }
}
