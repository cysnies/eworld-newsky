package fr.neatmonster.nocheatplus.command.admin.exemption;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.command.CommandUtil;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import fr.neatmonster.nocheatplus.players.DataManager;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class UnexemptCommand extends BaseCommand {
   public UnexemptCommand(JavaPlugin plugin) {
      super(plugin, "unexempt", "nocheatplus.admin.unexempt");
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (args.length >= 2 && args.length <= 3) {
         String playerName = args[1];
         CheckType checkType;
         if (args.length == 3) {
            try {
               checkType = CheckType.valueOf(args[2].toUpperCase().replace('-', '_').replace('.', '_'));
            } catch (Exception var8) {
               sender.sendMessage(TAG + "Could not interpret: " + args[2]);
               sender.sendMessage(TAG + "Check type should be one of: " + StringUtil.join(Arrays.asList(CheckType.values()), " | "));
               return true;
            }
         } else {
            checkType = CheckType.ALL;
         }

         if (playerName.equals("*")) {
            NCPExemptionManager.clear();
            sender.sendMessage(TAG + "Nobody will be exempted from: " + checkType);
            return true;
         } else {
            Player player = DataManager.getPlayer(playerName);
            if (player != null) {
               playerName = player.getName();
            }

            NCPExemptionManager.unexempt(playerName, checkType);
            sender.sendMessage(TAG + "Player " + playerName + " will not be exempted from: " + checkType);
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
