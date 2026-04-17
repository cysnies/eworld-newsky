package fr.neatmonster.nocheatplus.command.admin;

import fr.neatmonster.nocheatplus.checks.ViolationHistory;
import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.players.DataManager;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class InfoCommand extends BaseCommand {
   public InfoCommand(JavaPlugin plugin) {
      super(plugin, "info", "nocheatplus.admin.info");
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (args.length != 2) {
         return false;
      } else {
         this.handleInfoCommand(sender, args[1]);
         return true;
      }
   }

   private void handleInfoCommand(CommandSender sender, String playerName) {
      Player player = DataManager.getPlayer(playerName);
      if (player != null) {
         playerName = player.getName();
      }

      ViolationHistory history = ViolationHistory.getHistory(playerName, false);
      boolean known = player != null || history != null;
      if (history == null) {
         sender.sendMessage(TAG + "No entries for " + playerName + "'s violations... " + (known ? "" : "(exact spelling?)") + ".");
      } else {
         DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
         ViolationHistory.ViolationLevel[] violations = history.getViolationLevels();
         if (violations.length > 0) {
            sender.sendMessage(TAG + "Displaying " + playerName + "'s violations...");
            String c = sender instanceof Player ? ChatColor.GRAY.toString() : "";

            for(ViolationHistory.ViolationLevel violationLevel : violations) {
               long time = violationLevel.time;
               String[] parts = violationLevel.check.split("\\.");
               String check = parts[parts.length - 1].toLowerCase();
               String parent = parts[parts.length - 2].toLowerCase();
               long sumVL = Math.round(violationLevel.sumVL);
               long maxVL = Math.round(violationLevel.maxVL);
               long avVl = Math.round(violationLevel.sumVL / (double)violationLevel.nVL);
               sender.sendMessage(TAG + "[" + dateFormat.format(new Date(time)) + "] " + parent + "." + check + " VL " + sumVL + c + "  (n" + violationLevel.nVL + "a" + avVl + "m" + maxVL + ")");
            }
         } else {
            sender.sendMessage(TAG + "Displaying " + playerName + "'s violations... nothing to display.");
         }

      }
   }

   public List onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      return null;
   }
}
