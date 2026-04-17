package fr.neatmonster.nocheatplus.command.actions;

import fr.neatmonster.nocheatplus.command.AbstractCommand;
import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.players.DataManager;
import fr.neatmonster.nocheatplus.utilities.ColorUtil;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class TellCommand extends BaseCommand {
   public TellCommand(JavaPlugin plugin) {
      super(plugin, "tell", "nocheatplus.admin.tell");
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (args.length < 3) {
         return false;
      } else {
         String name = args[1].trim();
         String message = AbstractCommand.join(args, 2);
         this.tell(name, message);
         return true;
      }
   }

   private void tell(String name, String message) {
      Player player = DataManager.getPlayer(name);
      if (player != null) {
         player.sendMessage(ColorUtil.replaceColors(message));
      }

   }

   public List onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      return null;
   }
}
