package fr.neatmonster.nocheatplus.checks;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.actions.ActionList;
import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.components.MCAccessHolder;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import fr.neatmonster.nocheatplus.hooks.NCPHookManager;
import fr.neatmonster.nocheatplus.logging.LogUtil;
import fr.neatmonster.nocheatplus.metrics.MetricsData;
import fr.neatmonster.nocheatplus.players.DataManager;
import fr.neatmonster.nocheatplus.players.ExecutionHistory;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;

public abstract class Check implements MCAccessHolder {
   protected static Map histories = new HashMap();
   protected final CheckType type;
   protected MCAccess mcAccess;

   protected static ExecutionHistory getHistory(Player player) {
      if (!histories.containsKey(player.getName())) {
         histories.put(player.getName(), new ExecutionHistory());
      }

      return (ExecutionHistory)histories.get(player.getName());
   }

   public Check(CheckType type) {
      this.type = type;
      this.mcAccess = NCPAPIProvider.getNoCheatPlusAPI().getMCAccess();
      ViolationHistory.checkTypeMap.put(this.getClass().getName(), type);
      DataManager.registerExecutionHistory(type, histories);
   }

   public boolean executeActions(Player player, double vL, double addedVL, ActionList actions, boolean isMainThread) {
      return this.executeActions(new ViolationData(this, player, vL, addedVL, actions), isMainThread);
   }

   protected boolean executeActions(Player player, double vL, double addedVL, ActionList actions) {
      return this.executeActions(new ViolationData(this, player, vL, addedVL, actions), true);
   }

   protected boolean executeActions(ViolationData violationData) {
      return this.executeActions(violationData, true);
   }

   protected boolean executeActions(ViolationData violationData, boolean isMainThread) {
      if (NCPHookManager.shouldCancelVLProcessing(violationData)) {
         return false;
      } else {
         MetricsData.addFailed(violationData.check.type);
         boolean hasCancel = violationData.hasCancel();
         if (isMainThread) {
            return violationData.executeActions();
         } else {
            TickTask.requestActionsExecution(violationData);
            return hasCancel;
         }
      }
   }

   protected Map getParameterMap(ViolationData violationData) {
      Map<ParameterName, String> params = new HashMap();
      return params;
   }

   public CheckType getType() {
      return this.type;
   }

   public boolean isEnabled(Player player) {
      try {
         if (!this.type.isEnabled(player) || player.hasPermission(this.type.getPermission())) {
            return false;
         }
      } catch (Exception e) {
         LogUtil.logSevere((Throwable)e);
      }

      return !NCPExemptionManager.isExempted(player, this.type);
   }

   public void setMCAccess(MCAccess mcAccess) {
      this.mcAccess = mcAccess;
   }

   public MCAccess getMCAccess() {
      return this.mcAccess;
   }
}
