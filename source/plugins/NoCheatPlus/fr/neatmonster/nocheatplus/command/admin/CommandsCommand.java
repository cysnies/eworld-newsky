package fr.neatmonster.nocheatplus.command.admin;

import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import java.util.Arrays;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandsCommand extends BaseCommand {
   final String[] moreCommands = new String[]{"/<command> ban [delay=(ticks)] (player) [(reason)...]: ban player", "/<command> kick [delay=(ticks)] (player) [(reason)...]: kick player", "/<command> tempkick [delay=(ticks)] (player) (minutes) [(reason)...]", "/<command> unkick (player): Allow a player to login again.", "/<command> kicklist: Show temporarily kicked players.", "/<command> tell [delay=(ticks)] (player) (message)...: tell a message", "/<command> delay [delay=(ticks)] (command)...: delay a command"};
   final String allCommands;

   public CommandsCommand(JavaPlugin plugin) {
      super(plugin, "commands", "nocheatplus.admin.commands", new String[]{"cmds"});

      for(int i = 0; i < this.moreCommands.length; ++i) {
         this.moreCommands[i] = this.moreCommands[i].replace("<command>", "ncp");
      }

      String all = TAG + "All commands info:\n";
      Command cmd = plugin.getCommand("nocheatplus");
      if (cmd != null) {
         all = all + cmd.getUsage().replace("<command>", "ncp") + "Auxiliary commands (actions):\n";
      }

      all = all + StringUtil.join(Arrays.asList(this.moreCommands), "\n");
      this.allCommands = all;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      sender.sendMessage(this.allCommands);
      return true;
   }
}
