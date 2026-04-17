package fr.neatmonster.nocheatplus.config;

import fr.neatmonster.nocheatplus.actions.AbstractActionFactory;
import fr.neatmonster.nocheatplus.actions.AbstractActionList;
import fr.neatmonster.nocheatplus.actions.Action;

public abstract class ConfigFileWithActions extends RawConfigFile {
   protected AbstractActionFactory factory = null;

   public abstract void regenerateActionLists();

   public AbstractActionList getOptimizedActionList(String path, String permission) {
      String value = this.getString(path);
      return this.factory.createActionList(value, permission).getOptimizedCopy(this);
   }

   public AbstractActionList getDefaultActionList(String path, String permission) {
      String value = this.getString(path);
      return this.factory.createActionList(value, permission);
   }

   public void set(String path, AbstractActionList list) {
      StringBuffer string = new StringBuffer();

      for(Integer threshold : list.getThresholds()) {
         if (threshold > 0) {
            string.append(" vl>").append(threshold);
         }

         for(Action action : list.getActions((double)threshold)) {
            string.append(" ").append(action);
         }
      }

      this.set(path, string.toString().trim());
   }
}
