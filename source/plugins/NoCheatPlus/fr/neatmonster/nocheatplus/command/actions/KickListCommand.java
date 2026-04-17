package fr.neatmonster.nocheatplus.command.actions;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import java.util.Arrays;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class KickListCommand extends BaseCommand {
   public KickListCommand(JavaPlugin plugin) {
      super(plugin, "kicklist", "nocheatplus.admin.kicklist");
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      String[] kicked = NCPAPIProvider.getNoCheatPlusAPI().getLoginDeniedPlayers();
      if (kicked.length < 100) {
         Arrays.sort(kicked);
      }

      sender.sendMessage(TAG + "Temporarily kicked players:");
      sender.sendMessage(StringUtil.join(Arrays.asList(kicked), " "));
      return true;
   }
}
