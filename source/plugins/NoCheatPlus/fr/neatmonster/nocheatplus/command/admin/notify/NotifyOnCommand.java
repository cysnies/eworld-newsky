package fr.neatmonster.nocheatplus.command.admin.notify;

import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.players.DataManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class NotifyOnCommand extends BaseCommand {
   public NotifyOnCommand(JavaPlugin plugin) {
      super(plugin, "on", (String)null, new String[]{"1", "+"});
   }

   public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
      if (args.length != 2) {
         return false;
      } else if (!(sender instanceof Player)) {
         sender.sendMessage("[NoCheatPlus] Toggling notifications is only available for online players.");
         return true;
      } else {
         DataManager.getPlayerData(sender.getName(), true).setNotifyOff(false);
         sender.sendMessage(ChatColor.RED + "NCP: " + ChatColor.WHITE + "Notifications are now turned " + ChatColor.YELLOW + "on" + ChatColor.WHITE + ".");
         return true;
      }
   }
}
