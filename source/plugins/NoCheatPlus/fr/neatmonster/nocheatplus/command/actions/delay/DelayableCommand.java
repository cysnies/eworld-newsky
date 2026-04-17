package fr.neatmonster.nocheatplus.command.actions.delay;

import fr.neatmonster.nocheatplus.command.BaseCommand;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class DelayableCommand extends BaseCommand {
   protected final int delayIndex;
   protected final boolean mustHaveDelay;
   protected final int delayPreset;

   public static long parseDelay(String[] args, int index) {
      return parseDelay(args, index, -1);
   }

   public static long parseDelay(String[] args, int index, int preset) {
      if (args.length <= index) {
         return (long)preset;
      } else {
         String arg = args[index].trim().toLowerCase();
         if (!arg.startsWith("delay=")) {
            return (long)preset;
         } else if (arg.length() < 7) {
            return -1L;
         } else {
            try {
               long res = Long.parseLong(arg.substring(6));
               return res < 0L ? -1L : res;
            } catch (NumberFormatException var6) {
               return -1L;
            }
         }
      }
   }

   public DelayableCommand(JavaPlugin plugin, String label, String permission) {
      this(plugin, label, permission, 1);
   }

   public DelayableCommand(JavaPlugin plugin, String label, String permission, int delayIndex) {
      this(plugin, label, permission, delayIndex, -1, false);
   }

   public DelayableCommand(JavaPlugin plugin, String label, String permission, int delayIndex, int delayPreset, boolean mustHaveDelay) {
      this(plugin, label, permission, (String[])null, delayIndex, delayPreset, mustHaveDelay);
   }

   public DelayableCommand(JavaPlugin plugin, String label, String permission, String[] aliases, int delayIndex, int delayPreset, boolean mustHaveDelay) {
      super(plugin, label, permission, aliases);
      this.delayIndex = delayIndex;
      this.mustHaveDelay = mustHaveDelay;
      this.delayPreset = delayPreset;
   }

   public abstract boolean execute(CommandSender var1, Command var2, String var3, String[] var4, long var5);

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      long delay = parseDelay(args, this.delayIndex, this.delayPreset);
      String[] alteredArgs;
      if (args.length <= this.delayIndex) {
         if (this.mustHaveDelay) {
            return false;
         }

         alteredArgs = args;
      } else {
         boolean hasDef = args[this.delayIndex].startsWith("delay=") && delay != -1L;
         alteredArgs = new String[args.length + (hasDef ? -1 : 0)];
         if (alteredArgs.length > 0) {
            int increment = 0;

            for(int i = 0; i < args.length; ++i) {
               if (i == this.delayIndex && hasDef) {
                  increment = -1;
               } else {
                  alteredArgs[i + increment] = args[i];
               }
            }
         }
      }

      return this.execute(sender, command, label, alteredArgs, delay);
   }

   protected void schedule(Runnable runnable, long delay) {
      if (delay < 0L) {
         runnable.run();
      } else if (delay == 0L) {
         Bukkit.getScheduler().scheduleSyncDelayedTask((Plugin)this.access, runnable);
      } else {
         Bukkit.getScheduler().scheduleSyncDelayedTask((Plugin)this.access, runnable, delay);
      }

   }

   public List onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      return null;
   }
}
