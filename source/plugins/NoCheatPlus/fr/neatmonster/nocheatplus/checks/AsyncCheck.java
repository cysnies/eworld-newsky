package fr.neatmonster.nocheatplus.checks;

import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import fr.neatmonster.nocheatplus.logging.LogUtil;
import org.bukkit.entity.Player;

public abstract class AsyncCheck extends Check {
   public AsyncCheck(CheckType type) {
      super(type);
   }

   public boolean isEnabled(Player player) {
      try {
         if (!this.type.isEnabled(player) || this.type.hasCachedPermission(player)) {
            return false;
         }
      } catch (Exception e) {
         LogUtil.scheduleLogSevere((Throwable)e);
      }

      return !NCPExemptionManager.isExempted(player, this.type);
   }
}
