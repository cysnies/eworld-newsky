package fr.neatmonster.nocheatplus.command.actions;

import fr.neatmonster.nocheatplus.command.AbstractCommand;
import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.logging.LogUtil;
import fr.neatmonster.nocheatplus.players.DataManager;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class KickCommand extends BaseCommand {
   public KickCommand(JavaPlugin plugin) {
      super(plugin, "kick", "nocheatplus.admin.kick");
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (args.length < 2) {
         return false;
      } else {
         String name = args[1];
         String reason;
         if (args.length > 2) {
            reason = AbstractCommand.join(args, 2);
         } else {
            reason = "";
         }

         this.kick(sender, name, reason);
         return true;
      }
   }

   void kick(CommandSender sender, String name, String reason) {
      Player player = DataManager.getPlayer(name);
      if (player != null) {
         player.kickPlayer(reason);
         LogUtil.logInfo("[NoCheatPlus] (" + sender.getName() + ") Kicked " + player.getName() + " : " + reason);
      }
   }

   public List onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      return null;
   }
}
