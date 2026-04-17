package fr.neatmonster.nocheatplus.command.actions;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.command.BaseCommand;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class UnKickCommand extends BaseCommand {
   public UnKickCommand(JavaPlugin plugin) {
      super(plugin, "unkick", "nocheatplus.admin.unkick");
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (args.length != 2) {
         return false;
      } else {
         if (args[1].trim().equals("*")) {
            sender.sendMessage(TAG + "Removed " + NCPAPIProvider.getNoCheatPlusAPI().allowLoginAll() + " players from the 'deny-login' list.");
         } else if (NCPAPIProvider.getNoCheatPlusAPI().allowLogin(args[1])) {
            sender.sendMessage(TAG + "Allow to login again: " + args[1].trim());
         } else {
            sender.sendMessage(TAG + "Was not denied to login: " + args[1].trim());
         }

         return true;
      }
   }

   public List onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      return null;
   }
}
