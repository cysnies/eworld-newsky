package com.sk89q.bukkit.util;

import com.sk89q.util.ReflectionUtil;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;

public class CommandRegistration {
   protected final Plugin plugin;
   protected final CommandExecutor executor;
   private CommandMap fallbackCommands;

   public CommandRegistration(Plugin plugin) {
      this(plugin, plugin);
   }

   public CommandRegistration(Plugin plugin, CommandExecutor executor) {
      this.plugin = plugin;
      this.executor = executor;
   }

   public boolean register(List registered) {
      CommandMap commandMap = this.getCommandMap();
      if (registered != null && commandMap != null) {
         for(CommandInfo command : registered) {
            DynamicPluginCommand cmd = new DynamicPluginCommand(command.getAliases(), command.getDesc(), "/" + command.getAliases()[0] + " " + command.getUsage(), this.executor, command.getRegisteredWith(), this.plugin);
            cmd.setPermissions(command.getPermissions());
            commandMap.register(this.plugin.getDescription().getName(), cmd);
         }

         return true;
      } else {
         return false;
      }
   }

   public CommandMap getCommandMap() {
      CommandMap commandMap = (CommandMap)ReflectionUtil.getField(this.plugin.getServer().getPluginManager(), "commandMap");
      if (commandMap == null) {
         if (this.fallbackCommands != null) {
            commandMap = this.fallbackCommands;
         } else {
            Bukkit.getServer().getLogger().severe(this.plugin.getDescription().getName() + ": Could not retrieve server CommandMap, using fallback instead!");
            this.fallbackCommands = commandMap = new SimpleCommandMap(Bukkit.getServer());
            Bukkit.getServer().getPluginManager().registerEvents(new FallbackRegistrationListener(this.fallbackCommands), this.plugin);
         }
      }

      return commandMap;
   }

   public boolean unregisterCommands() {
      CommandMap commandMap = this.getCommandMap();
      List<String> toRemove = new ArrayList();
      Map<String, Command> knownCommands = (Map)ReflectionUtil.getField(commandMap, "knownCommands");
      Set<String> aliases = (Set)ReflectionUtil.getField(commandMap, "aliases");
      if (knownCommands != null && aliases != null) {
         Iterator<Command> i = knownCommands.values().iterator();

         while(i.hasNext()) {
            Command cmd = (Command)i.next();
            if (cmd instanceof DynamicPluginCommand && ((DynamicPluginCommand)cmd).getOwner().equals(this.executor)) {
               i.remove();

               for(String alias : cmd.getAliases()) {
                  Command aliasCmd = (Command)knownCommands.get(alias);
                  if (cmd.equals(aliasCmd)) {
                     aliases.remove(alias);
                     toRemove.add(alias);
                  }
               }
            }
         }

         for(String string : toRemove) {
            knownCommands.remove(string);
         }

         return true;
      } else {
         return false;
      }
   }

   static {
      Bukkit.getServer().getHelpMap().registerHelpTopicFactory(DynamicPluginCommand.class, new DynamicPluginCommandHelpTopic.Factory());
   }
}
