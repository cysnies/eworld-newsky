package fr.neatmonster.nocheatplus.hooks;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.access.IViolationInfo;
import org.bukkit.entity.Player;

public abstract class AbstractNCPHook implements NCPHook {
   /** @deprecated */
   public boolean onCheckFailure(CheckType checkType, Player player) {
      return false;
   }

   public boolean onCheckFailure(CheckType checkType, Player player, IViolationInfo info) {
      return this.onCheckFailure(checkType, player);
   }
}
