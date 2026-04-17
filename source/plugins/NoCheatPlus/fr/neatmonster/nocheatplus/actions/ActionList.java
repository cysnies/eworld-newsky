package fr.neatmonster.nocheatplus.actions;

public class ActionList extends AbstractActionList {
   public static final AbstractActionList.ActionListFactory listFactory = new AbstractActionList.ActionListFactory() {
      public ActionList getNewActionList(String permissionSilent) {
         return new ActionList(permissionSilent);
      }
   };

   public ActionList(String permissionSilent) {
      super(permissionSilent, listFactory);
   }
}
