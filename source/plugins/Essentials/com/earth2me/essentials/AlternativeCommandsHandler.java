package com.earth2me.essentials;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.PluginCommandYamlParser;
import org.bukkit.plugin.Plugin;

public class AlternativeCommandsHandler {
   private static final Logger LOGGER = Logger.getLogger("Minecraft");
   private final transient Map altcommands = new HashMap();
   private final transient Map disabledList = new HashMap();
   private final transient IEssentials ess;

   public AlternativeCommandsHandler(IEssentials ess) {
      this.ess = ess;

      for(Plugin plugin : ess.getServer().getPluginManager().getPlugins()) {
         if (plugin.isEnabled()) {
            this.addPlugin(plugin);
         }
      }

   }

   public final void addPlugin(Plugin plugin) {
      if (!plugin.getDescription().getMain().contains("com.earth2me.essentials")) {
         List<Command> commands = PluginCommandYamlParser.parse(plugin);
         String pluginName = plugin.getDescription().getName().toLowerCase(Locale.ENGLISH);

         for(Command command : commands) {
            PluginCommand pc = (PluginCommand)command;
            List<String> labels = new ArrayList(pc.getAliases());
            labels.add(pc.getName());
            PluginCommand reg = this.ess.getServer().getPluginCommand(pluginName + ":" + pc.getName().toLowerCase(Locale.ENGLISH));
            if (reg == null) {
               reg = this.ess.getServer().getPluginCommand(pc.getName().toLowerCase(Locale.ENGLISH));
            }

            if (reg != null && reg.getPlugin().equals(plugin)) {
               for(String label : labels) {
                  List<PluginCommand> plugincommands = (List)this.altcommands.get(label.toLowerCase(Locale.ENGLISH));
                  if (plugincommands == null) {
                     plugincommands = new ArrayList();
                     this.altcommands.put(label.toLowerCase(Locale.ENGLISH), plugincommands);
                  }

                  boolean found = false;

                  for(PluginCommand pc2 : plugincommands) {
                     if (pc2.getPlugin().equals(plugin)) {
                        found = true;
                     }
                  }

                  if (!found) {
                     plugincommands.add(reg);
                  }
               }
            }
         }

      }
   }

   public void removePlugin(Plugin plugin) {
      Iterator<Map.Entry<String, List<PluginCommand>>> iterator = this.altcommands.entrySet().iterator();

      while(iterator.hasNext()) {
         Map.Entry<String, List<PluginCommand>> entry = (Map.Entry)iterator.next();
         Iterator<PluginCommand> pcIterator = ((List)entry.getValue()).iterator();

         while(pcIterator.hasNext()) {
            PluginCommand pc = (PluginCommand)pcIterator.next();
            if (pc.getPlugin() == null || pc.getPlugin().equals(plugin)) {
               pcIterator.remove();
            }
         }

         if (((List)entry.getValue()).isEmpty()) {
            iterator.remove();
         }
      }

   }

   public PluginCommand getAlternative(String label) {
      List<PluginCommand> commands = (List)this.altcommands.get(label);
      if (commands != null && !commands.isEmpty()) {
         if (commands.size() == 1) {
            return (PluginCommand)commands.get(0);
         } else {
            for(PluginCommand command : commands) {
               if (command.getName().equalsIgnoreCase(label)) {
                  return command;
               }
            }

            return (PluginCommand)commands.get(0);
         }
      } else {
         return null;
      }
   }

   public void executed(String label, PluginCommand pc) {
      String altString = pc.getPlugin().getName() + ":" + pc.getLabel();
      if (this.ess.getSettings().isDebug()) {
         LOGGER.log(Level.INFO, "Essentials: Alternative command " + label + " found, using " + altString);
      }

      this.disabledList.put(label, altString);
   }

   public Map disabledCommands() {
      return this.disabledList;
   }
}
