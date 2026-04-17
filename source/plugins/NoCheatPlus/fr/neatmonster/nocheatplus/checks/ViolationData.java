package fr.neatmonster.nocheatplus.checks;

import fr.neatmonster.nocheatplus.actions.Action;
import fr.neatmonster.nocheatplus.actions.ActionData;
import fr.neatmonster.nocheatplus.actions.ActionList;
import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.actions.types.CancelAction;
import fr.neatmonster.nocheatplus.checks.access.IViolationInfo;
import fr.neatmonster.nocheatplus.logging.LogUtil;
import java.util.Map;
import org.bukkit.entity.Player;

public class ViolationData implements IViolationInfo, ActionData {
   public final ActionList actions;
   public final Action[] applicableActions;
   public final double addedVL;
   public final Check check;
   public final Player player;
   public final double vL;
   private final Map parameters;

   public ViolationData(Check check, Player player, double vL, double addedVL, ActionList actions) {
      this.check = check;
      this.player = player;
      this.vL = vL;
      this.addedVL = addedVL;
      this.actions = actions;
      this.applicableActions = actions.getActions(vL);
      boolean needsParameters = false;

      for(int i = 0; i < this.applicableActions.length; ++i) {
         if (this.applicableActions[i].needsParameters()) {
            needsParameters = true;
            break;
         }
      }

      this.parameters = needsParameters ? check.getParameterMap(this) : null;
   }

   public Action[] getActions() {
      return this.applicableActions;
   }

   public boolean executeActions() {
      try {
         ViolationHistory.getHistory(this.player).log(this.check.getClass().getName(), this.addedVL);
         long time = System.currentTimeMillis() / 1000L;
         boolean cancel = false;

         for(Action action : this.getActions()) {
            if (Check.getHistory(this.player).executeAction(this, action, time) && action.execute(this)) {
               cancel = true;
            }
         }

         return cancel;
      } catch (Exception e) {
         LogUtil.logSevere((Throwable)e);
         return true;
      }
   }

   public boolean hasCancel() {
      for(Action action : this.applicableActions) {
         if (action instanceof CancelAction) {
            return true;
         }
      }

      return false;
   }

   public String getParameter(ParameterName parameterName) {
      if (parameterName == null) {
         return "<???>";
      } else {
         switch (parameterName) {
            case CHECK:
               return this.check.getClass().getSimpleName();
            case PLAYER:
               return this.player.getName();
            case VIOLATIONS:
               return String.valueOf(Math.round(this.vL));
            default:
               if (this.parameters == null) {
                  return "<?" + parameterName + ">";
               } else {
                  String value = (String)this.parameters.get(parameterName);
                  return value == null ? "<?" + parameterName + ">" : value;
               }
         }
      }
   }

   public void setParameter(ParameterName parameterName, String value) {
      if (this.parameters != null) {
         this.parameters.put(parameterName, value);
      }

   }

   public boolean needsParameters() {
      return this.parameters != null;
   }

   public double getAddedVl() {
      return this.addedVL;
   }

   public double getTotalVl() {
      return this.vL;
   }

   public String getPermissionSilent() {
      return this.actions.permissionSilent;
   }

   public ActionList getActionList() {
      return this.actions;
   }
}
