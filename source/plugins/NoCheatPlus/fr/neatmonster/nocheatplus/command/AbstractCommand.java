package fr.neatmonster.nocheatplus.command;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

public abstract class AbstractCommand implements TabExecutor {
   public static final List noTabChoices = Collections.unmodifiableList(new LinkedList());
   protected final Object access;
   public final String label;
   public final String permission;
   protected final Map subCommands;
   protected int subCommandIndex;
   protected final String[] aliases;

   public static String join(String[] args, int startIndex) {
      return join(args, startIndex, " ");
   }

   public static String join(String[] args, int startIndex, String sep) {
      StringBuilder b = new StringBuilder(100);
      if (startIndex < args.length) {
         b.append(args[startIndex]);
      }

      for(int i = startIndex + 1; i < args.length; ++i) {
         b.append(sep);
         b.append(args[i]);
      }

      return b.toString();
   }

   public AbstractCommand(Object access, String label, String permission) {
      this(access, label, permission, (String[])null);
   }

   public AbstractCommand(Object access, String label, String permission, String[] aliases) {
      this.subCommands = new LinkedHashMap();
      this.subCommandIndex = -1;
      this.access = access;
      this.label = label;
      this.permission = permission;
      this.aliases = aliases;
   }

   public void addSubCommands(AbstractCommand... commands) {
      for(AbstractCommand subCommand : commands) {
         this.subCommands.put(subCommand.label, subCommand);
         if (subCommand.subCommandIndex == -1) {
            subCommand.subCommandIndex = Math.max(0, this.subCommandIndex) + 1;
         }

         if (subCommand.aliases != null) {
            for(String alias : subCommand.aliases) {
               if (!this.subCommands.containsKey(alias)) {
                  this.subCommands.put(alias, subCommand);
               }
            }
         }
      }

   }

   public List onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      Set<String> choices = new LinkedHashSet(this.subCommands.size());
      int len = args.length;
      int subCommandIndex = Math.max(0, this.subCommandIndex);
      if (len != subCommandIndex && len != subCommandIndex + 1) {
         if (len > subCommandIndex + 1) {
            String arg = args[subCommandIndex].trim().toLowerCase();
            AbstractCommand<?> subCommand = (AbstractCommand)this.subCommands.get(arg);
            if (subCommand != null && (subCommand.permission == null || sender.hasPermission(subCommand.permission))) {
               return subCommand.onTabComplete(sender, command, alias, args);
            }
         }
      } else {
         String arg = len == subCommandIndex ? "" : args[subCommandIndex].trim().toLowerCase();

         for(AbstractCommand cmd : this.subCommands.values()) {
            if (cmd.label.startsWith(arg) && (cmd.permission == null || sender.hasPermission(cmd.permission))) {
               choices.add(cmd.label);
            }
         }
      }

      if (choices.isEmpty()) {
         return noTabChoices;
      } else {
         return new LinkedList(choices);
      }
   }

   public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
      int len = args.length;
      int subCommandIndex = Math.max(0, this.subCommandIndex);
      if (len > subCommandIndex) {
         String arg = args[subCommandIndex].trim().toLowerCase();
         AbstractCommand<?> subCommand = (AbstractCommand)this.subCommands.get(arg);
         if (subCommand != null) {
            if (subCommand.permission != null && !sender.hasPermission(subCommand.permission)) {
               sender.sendMessage(ChatColor.DARK_RED + "You don't have permission.");
               return true;
            }

            return subCommand.onCommand(sender, command, alias, args);
         }
      }

      return false;
   }
}
