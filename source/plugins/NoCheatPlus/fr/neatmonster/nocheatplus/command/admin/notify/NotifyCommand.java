package fr.neatmonster.nocheatplus.command.admin.notify;

import fr.neatmonster.nocheatplus.command.AbstractCommand;
import fr.neatmonster.nocheatplus.command.BaseCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class NotifyCommand extends BaseCommand {
   public NotifyCommand(JavaPlugin plugin) {
      super(plugin, "notify", "nocheatplus.admin.notify", new String[]{"alert", "alerts"});
      this.addSubCommands(new AbstractCommand[]{new NotifyOffCommand(plugin), new NotifyOnCommand(plugin)});
   }
}
