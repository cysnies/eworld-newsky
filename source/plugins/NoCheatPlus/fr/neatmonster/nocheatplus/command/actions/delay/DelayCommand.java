package fr.neatmonster.nocheatplus.command.actions.delay;

import fr.neatmonster.nocheatplus.command.AbstractCommand;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class DelayCommand extends DelayableCommand {
   public DelayCommand(JavaPlugin plugin) {
      super(plugin, "delay", "nocheatplus.admin.delay", 1, 0, true);
   }

   public boolean execute(CommandSender sender, Command command, String label, String[] alteredArgs, long delay) {
      if (alteredArgs.length < 2) {
         return false;
      } else {
         final String cmd = AbstractCommand.join(alteredArgs, 1);
         this.schedule(new Runnable() {
            public void run() {
               Server server = Bukkit.getServer();
               server.dispatchCommand(server.getConsoleSender(), cmd);
            }
         }, delay);
         return true;
      }
   }
}
