package fr.neatmonster.nocheatplus.command.admin;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationHistory;
import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.command.CommandUtil;
import fr.neatmonster.nocheatplus.players.DataManager;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class RemovePlayerCommand extends BaseCommand {
   public RemovePlayerCommand(JavaPlugin plugin) {
      super(plugin, "removeplayer", "nocheatplus.admin.removeplayer", new String[]{"remove"});
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (args.length >= 2 && args.length <= 3) {
         String playerName = args[1];
         CheckType checkType;
         if (args.length == 3) {
            try {
               checkType = CheckType.valueOf(args[2].toUpperCase().replace('-', '_').replace('.', '_'));
            } catch (Exception var12) {
               sender.sendMessage(TAG + "Could not interpret: " + args[2]);
               sender.sendMessage(TAG + "Check type should be one of: " + StringUtil.join(Arrays.asList(CheckType.values()), " | "));
               return true;
            }
         } else {
            checkType = CheckType.ALL;
         }

         if (playerName.equals("*")) {
            DataManager.clearData(checkType);
            sender.sendMessage(TAG + "Removed all data and history: " + checkType);
            return true;
         } else {
            Player player = DataManager.getPlayer(playerName);
            if (player != null) {
               playerName = player.getName();
            }

            ViolationHistory hist = ViolationHistory.getHistory(playerName, false);
            boolean histRemoved = false;
            if (hist != null) {
               histRemoved = hist.remove(checkType);
               if (checkType == CheckType.ALL) {
                  histRemoved = true;
                  ViolationHistory.removeHistory(playerName);
               }
            }

            if (DataManager.removeExecutionHistory(checkType, playerName)) {
               histRemoved = true;
            }

            boolean dataRemoved = DataManager.removeData(playerName, checkType);
            if (!dataRemoved && !histRemoved) {
               sender.sendMessage(TAG + "Nothing found (" + checkType + ", exact spelling): " + playerName);
            } else {
               String which;
               if (dataRemoved && histRemoved) {
                  which = "data and history";
               } else if (dataRemoved) {
                  which = "data";
               } else {
                  which = "history";
               }

               sender.sendMessage(TAG + "Removed " + which + " (" + checkType + "): " + playerName);
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public List onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      return args.length == 3 ? CommandUtil.getCheckTypeTabMatches(args[2]) : null;
   }
}
