package fr.neatmonster.nocheatplus.actions.types;

import fr.neatmonster.nocheatplus.actions.Action;
import fr.neatmonster.nocheatplus.actions.ActionData;
import fr.neatmonster.nocheatplus.config.ConfigFileWithActions;

public class DummyAction extends Action {
   protected final String definition;

   public DummyAction(String definition) {
      super("dummyAction", 0, 0);
      this.definition = definition;
   }

   public boolean execute(ActionData violationData) {
      return false;
   }

   public String toString() {
      return this.definition;
   }

   public Action getOptimizedCopy(ConfigFileWithActions config, Integer threshold) {
      return null;
   }
}
