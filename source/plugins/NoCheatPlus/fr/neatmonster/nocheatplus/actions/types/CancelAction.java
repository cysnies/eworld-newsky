package fr.neatmonster.nocheatplus.actions.types;

import fr.neatmonster.nocheatplus.actions.Action;
import fr.neatmonster.nocheatplus.actions.ActionData;

public class CancelAction extends Action {
   public CancelAction() {
      super("cancel", 0, 0);
   }

   public boolean execute(ActionData data) {
      return true;
   }

   public String toString() {
      return "cancel";
   }
}
