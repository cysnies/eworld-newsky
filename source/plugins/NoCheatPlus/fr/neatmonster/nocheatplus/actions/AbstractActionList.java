package fr.neatmonster.nocheatplus.actions;

import fr.neatmonster.nocheatplus.config.ConfigFileWithActions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractActionList {
   protected static final Action[] emptyArray = new Action[0];
   public final String permissionSilent;
   private final Map actions = new HashMap();
   protected final List thresholds = new ArrayList();
   protected final ActionListFactory listFactory;

   public AbstractActionList(String permissionSilent, ActionListFactory listFactory) {
      this.listFactory = listFactory;
      this.permissionSilent = permissionSilent + ".silent";
   }

   public Action[] getActions(double violationLevel) {
      Integer result = null;

      for(Integer threshold : this.thresholds) {
         if ((double)threshold <= violationLevel) {
            result = threshold;
         }
      }

      if (result != null) {
         return (Action[])this.actions.get(result);
      } else {
         return emptyArray;
      }
   }

   public List getThresholds() {
      return this.thresholds;
   }

   public void setActions(Integer threshold, Action[] actions) {
      if (!this.thresholds.contains(threshold)) {
         this.thresholds.add(threshold);
         Collections.sort(this.thresholds);
      }

      this.actions.put(threshold, actions);
   }

   public AbstractActionList getOptimizedCopy(ConfigFileWithActions config) {
      L newList = (L)this.listFactory.getNewActionList(this.permissionSilent);

      for(Map.Entry entry : this.actions.entrySet()) {
         Integer t = (Integer)entry.getKey();
         Action<D, L>[] a = this.getOptimizedCopy(config, t, (Action[])entry.getValue());
         if (a != null && a.length > 0) {
            newList.setActions(t, a);
         }
      }

      return newList;
   }

   public Action[] getOptimizedCopy(ConfigFileWithActions config, Integer threshold, Action[] actions) {
      if (actions != null && actions.length != 0) {
         ArrayList<Action<D, L>> optimized = new ArrayList();

         for(Action action : actions) {
            Action<D, L> optAction = action.getOptimizedCopy(config, threshold);
            if (optAction != null) {
               optimized.add(optAction);
            }
         }

         if (optimized.isEmpty()) {
            return null;
         } else {
            Action<D, L>[] optActions = new Action[optimized.size()];
            optimized.toArray(optActions);
            return optActions;
         }
      } else {
         return null;
      }
   }

   public interface ActionListFactory {
      AbstractActionList getNewActionList(String var1);
   }
}
