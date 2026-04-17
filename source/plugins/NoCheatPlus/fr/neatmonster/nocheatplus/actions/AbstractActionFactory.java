package fr.neatmonster.nocheatplus.actions;

import fr.neatmonster.nocheatplus.actions.types.CommandAction;
import fr.neatmonster.nocheatplus.actions.types.DummyAction;
import fr.neatmonster.nocheatplus.logging.LogUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractActionFactory {
   protected static final Map lib = new HashMap();
   protected final AbstractActionList.ActionListFactory listFactory;

   public AbstractActionFactory(Map library, AbstractActionList.ActionListFactory listFactory) {
      this.listFactory = listFactory;
      lib.putAll(library);
   }

   public abstract Action createAction(String var1);

   public AbstractActionList createActionList(String definition, String permission) {
      L list = (L)this.listFactory.getNewActionList(permission);
      if (definition == null) {
         return list;
      } else {
         boolean first = true;

         for(String s : definition.split("vl>")) {
            s = s.trim();
            if (s.length() == 0) {
               first = false;
            } else {
               try {
                  Integer vl;
                  String def;
                  if (first) {
                     first = false;
                     vl = 0;
                     def = s;
                  } else {
                     String[] listEntry = s.split("\\s+", 2);
                     vl = Integer.parseInt(listEntry[0]);
                     def = listEntry[1];
                  }

                  list.setActions(vl, this.createActions(def.split("\\s+")));
               } catch (Exception var12) {
                  LogUtil.logWarning("[NoCheatPlus] Couldn't parse action definition 'vl:" + s + "'.");
               }
            }
         }

         return list;
      }
   }

   public Action[] createActions(String... definitions) {
      List<Action<D, L>> actions = new ArrayList();

      for(String def : definitions) {
         if (def.length() != 0) {
            try {
               actions.add(this.createAction(def));
            } catch (IllegalArgumentException e) {
               LogUtil.logWarning("[NoCheatPlus] Failed to create action: " + e.getMessage());
               actions.add(new DummyAction(def));
            }
         }
      }

      return (Action[])actions.toArray(new Action[actions.size()]);
   }

   protected Action parseCmdAction(String definition) {
      String[] parts = definition.split(":");
      String name = parts[0];
      Object command = lib.get(parts[0]);
      int delay = 0;
      int repeat = 0;
      if (command == null) {
         throw new IllegalArgumentException("NoCheatPlus doesn't know command '" + name + "'. Have you forgotten to define it?");
      } else {
         if (parts.length > 1) {
            try {
               delay = Integer.parseInt(parts[1]);
               repeat = Integer.parseInt(parts[2]);
            } catch (Exception var8) {
               LogUtil.logWarning("[NoCheatPlus] Couldn't parse details of command '" + definition + "', will use default values instead.");
               delay = 0;
               repeat = 0;
            }
         }

         return new CommandAction(name, delay, repeat, command.toString());
      }
   }
}
