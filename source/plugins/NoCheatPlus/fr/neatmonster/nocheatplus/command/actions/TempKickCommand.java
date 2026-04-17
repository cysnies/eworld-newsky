package fr.neatmonster.nocheatplus.command.actions;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.command.AbstractCommand;
import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.logging.LogUtil;
import fr.neatmonster.nocheatplus.players.DataManager;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class TempKickCommand extends BaseCommand {
   public TempKickCommand(JavaPlugin plugin) {
      super(plugin, "tempkick", "nocheatplus.admin.tempkick", new String[]{"tkick", "tempban", "tban"});
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (args.length < 3) {
         return false;
      } else {
         long base = 60000L;
         String name = args[1];
         long duration = -1L;

         try {
            duration = (long)Integer.parseInt(args[2]);
         } catch (NumberFormatException var13) {
         }

         if (duration <= 0L) {
            return false;
         } else {
            long finalDuration = duration * base;
            String reason;
            if (args.length > 3) {
               reason = AbstractCommand.join(args, 3);
            } else {
               reason = "";
            }

            this.tempKick(sender, name, finalDuration, reason);
            return true;
         }
      }
   }

   protected void tempKick(CommandSender sender, String name, long duration, String reason) {
      Player player = DataManager.getPlayer(name);
      NCPAPIProvider.getNoCheatPlusAPI().denyLogin(name, duration);
      if (player != null) {
         player.kickPlayer(reason);
         LogUtil.logInfo("[NoCheatPlus] (" + sender.getName() + ") Kicked " + player.getName() + " for " + duration / 60000L + " minutes: " + reason);
      }
   }

   public List onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      return null;
   }
}
