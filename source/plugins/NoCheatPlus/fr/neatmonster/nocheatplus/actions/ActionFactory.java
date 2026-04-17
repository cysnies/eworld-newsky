package fr.neatmonster.nocheatplus.actions;

import fr.neatmonster.nocheatplus.actions.types.CancelAction;
import fr.neatmonster.nocheatplus.actions.types.LogAction;
import fr.neatmonster.nocheatplus.logging.LogUtil;
import java.util.Map;

public class ActionFactory extends AbstractActionFactory {
   public ActionFactory(Map library) {
      super(library, ActionList.listFactory);
   }

   public Action createAction(String actionDefinition) {
      actionDefinition = actionDefinition.toLowerCase();
      if (actionDefinition.equals("cancel")) {
         return new CancelAction();
      } else if (actionDefinition.startsWith("cmd:")) {
         return this.parseCmdAction(actionDefinition.split(":", 2)[1]);
      } else if (actionDefinition.startsWith("log:")) {
         return this.parseLogAction(actionDefinition.split(":", 2)[1]);
      } else {
         throw new IllegalArgumentException("NoCheatPlus doesn't understand action '" + actionDefinition + "' at all");
      }
   }

   protected Action parseLogAction(String definition) {
      String[] parts = definition.split(":");
      String name = parts[0];
      Object message = lib.get(parts[0]);
      int delay = 0;
      int repeat = 1;
      boolean toConsole = true;
      boolean toFile = true;
      boolean toChat = true;
      if (message == null) {
         throw new IllegalArgumentException("NoCheatPlus doesn't know log message '" + name + "'. Have you forgotten to define it?");
      } else {
         try {
            delay = Integer.parseInt(parts[1]);
            repeat = Integer.parseInt(parts[2]);
            toConsole = parts[3].contains("c");
            toChat = parts[3].contains("i");
            toFile = parts[3].contains("f");
         } catch (Exception e) {
            LogUtil.logWarning("[NoCheatPlus] Couldn't parse details of log action '" + definition + "', will use default values instead.");
            LogUtil.logWarning((Throwable)e);
            delay = 0;
            repeat = 1;
            toConsole = true;
            toFile = true;
            toChat = true;
         }

         return new LogAction(name, delay, repeat, toChat, toConsole, toFile, message.toString());
      }
   }
}
