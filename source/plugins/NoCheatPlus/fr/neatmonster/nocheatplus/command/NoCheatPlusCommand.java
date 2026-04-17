package fr.neatmonster.nocheatplus.command;

import fr.neatmonster.nocheatplus.command.actions.BanCommand;
import fr.neatmonster.nocheatplus.command.actions.KickCommand;
import fr.neatmonster.nocheatplus.command.actions.KickListCommand;
import fr.neatmonster.nocheatplus.command.actions.TellCommand;
import fr.neatmonster.nocheatplus.command.actions.TempKickCommand;
import fr.neatmonster.nocheatplus.command.actions.UnKickCommand;
import fr.neatmonster.nocheatplus.command.actions.delay.DelayCommand;
import fr.neatmonster.nocheatplus.command.admin.CommandsCommand;
import fr.neatmonster.nocheatplus.command.admin.InfoCommand;
import fr.neatmonster.nocheatplus.command.admin.LagCommand;
import fr.neatmonster.nocheatplus.command.admin.NCPVersionCommand;
import fr.neatmonster.nocheatplus.command.admin.ReloadCommand;
import fr.neatmonster.nocheatplus.command.admin.RemovePlayerCommand;
import fr.neatmonster.nocheatplus.command.admin.exemption.ExemptCommand;
import fr.neatmonster.nocheatplus.command.admin.exemption.ExemptionsCommand;
import fr.neatmonster.nocheatplus.command.admin.exemption.UnexemptCommand;
import fr.neatmonster.nocheatplus.command.admin.notify.NotifyCommand;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.utilities.ColorUtil;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class NoCheatPlusCommand extends BaseCommand {
   private Set rootLabels = new LinkedHashSet();

   public NoCheatPlusCommand(JavaPlugin plugin, Collection notifyReload) {
      super(plugin, "nocheatplus", (String)null, new String[]{"ncp"});

      for(BaseCommand cmd : new BaseCommand[]{new BanCommand(plugin), new CommandsCommand(plugin), new DelayCommand(plugin), new ExemptCommand(plugin), new ExemptionsCommand(plugin), new InfoCommand(plugin), new KickCommand(plugin), new KickListCommand(plugin), new LagCommand(plugin), new NCPVersionCommand(plugin), new NotifyCommand(plugin), new ReloadCommand(plugin, notifyReload), new RemovePlayerCommand(plugin), new TellCommand(plugin), new TempKickCommand(plugin), new UnexemptCommand(plugin), new UnKickCommand(plugin)}) {
         this.addSubCommands(new AbstractCommand[]{cmd});
         this.rootLabels.add(cmd.label);
      }

   }

   public Collection getAllSubCommandPermissions() {
      Set<String> set = new LinkedHashSet(this.rootLabels.size());

      for(String label : this.rootLabels) {
         set.add(((AbstractCommand)this.subCommands.get(label)).permission);
      }

      return set;
   }

   public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
      if (!command.getName().equalsIgnoreCase("nocheatplus")) {
         return false;
      } else if (sender.hasPermission("feature.command.nocheatplus")) {
         if (args.length > 0) {
            AbstractCommand<?> subCommand = (AbstractCommand)this.subCommands.get(args[0].trim().toLowerCase());
            if (subCommand != null && sender.hasPermission(subCommand.permission)) {
               return subCommand.onCommand(sender, command, commandLabel, args);
            }
         }

         return false;
      } else {
         ConfigFile config = ConfigManager.getConfigFile();
         if (config.getBoolean("protection.plugins.hide.active")) {
            sender.sendMessage(ColorUtil.replaceColors(config.getString("protection.plugins.hide.messages.unknowncommand")));
            return true;
         } else {
            return false;
         }
      }
   }

   public static class NCPReloadEvent extends Event {
      private static final HandlerList handlers = new HandlerList();

      public static HandlerList getHandlerList() {
         return handlers;
      }

      public HandlerList getHandlers() {
         return handlers;
      }
   }
}
