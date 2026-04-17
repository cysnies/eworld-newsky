package fr.neatmonster.nocheatplus.command;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.logging.LogUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandUtil {
   public static CommandMap getCommandMap() {
      try {
         return NCPAPIProvider.getNoCheatPlusAPI().getMCAccess().getCommandMap();
      } catch (Throwable t) {
         LogUtil.logSevere(t);
         return null;
      }
   }

   public static Collection getCommands() {
      CommandMap commandMap = getCommandMap();
      if (commandMap != null && commandMap instanceof SimpleCommandMap) {
         return ((SimpleCommandMap)commandMap).getCommands();
      } else {
         Collection<Command> commands = new LinkedHashSet(100);

         for(Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (plugin instanceof JavaPlugin) {
               JavaPlugin javaPlugin = (JavaPlugin)plugin;
               Map<String, Map<String, Object>> map = javaPlugin.getDescription().getCommands();
               if (map != null) {
                  for(String label : map.keySet()) {
                     Command command = javaPlugin.getCommand(label);
                     if (command != null) {
                        commands.add(command);
                     }
                  }
               }
            }
         }

         return commands;
      }
   }

   public static String getCommandLabel(String alias, boolean strict) {
      Command command = getCommand(alias);
      if (command == null) {
         return strict ? null : alias.trim().toLowerCase();
      } else {
         return command.getLabel().trim().toLowerCase();
      }
   }

   public static Command getCommand(String alias) {
      String lcAlias = alias.trim().toLowerCase();
      CommandMap map = getCommandMap();
      return map != null ? map.getCommand(lcAlias) : null;
   }

   public static List getCheckTypeTabMatches(String input) {
      String ref = input.toUpperCase().replace('-', '_').replace('.', '_');
      List<String> res = new ArrayList();

      for(CheckType checkType : CheckType.values()) {
         String name = checkType.name();
         if (name.startsWith(ref)) {
            res.add(name);
         }
      }

      if (ref.indexOf(95) == -1) {
         for(CheckType checkType : CheckType.values()) {
            String name = checkType.name();
            String[] split = name.split("_", 2);
            if (split.length > 1 && split[1].startsWith(ref)) {
               res.add(name);
            }
         }
      }

      if (!res.isEmpty()) {
         Collections.sort(res);
         return res;
      } else {
         return null;
      }
   }
}
