package fr.neatmonster.nocheatplus.command.actions;

import fr.neatmonster.nocheatplus.command.AbstractCommand;
import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.logging.LogUtil;
import fr.neatmonster.nocheatplus.players.DataManager;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class BanCommand extends BaseCommand {
   public BanCommand(JavaPlugin plugin) {
      super(plugin, "ban", "nocheatplus.admin.ban");
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

         this.ban(sender, name, reason);
         return true;
      }
   }

   void ban(CommandSender sender, String name, String reason) {
      Player player = DataManager.getPlayer(name);
      if (player != null) {
         player.kickPlayer(reason);
      }

      OfflinePlayer offlinePlayer = Bukkit.getServer().getOfflinePlayer(name);
      offlinePlayer.setBanned(true);
      LogUtil.logInfo("[NoCheatPlus] (" + sender.getName() + ") Banned " + offlinePlayer.getName() + " : " + reason);
   }

   public List onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      return null;
   }
}
