package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

public class Commandsudo extends EssentialsCommand {
   private static final Logger LOGGER = Logger.getLogger("Minecraft");

   public Commandsudo() {
      super("sudo");
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 2) {
         throw new NotEnoughArgumentsException();
      } else {
         final User user = this.getPlayer(server, args, 0, true, false);
         if (args[1].toLowerCase(Locale.ENGLISH).startsWith("c:")) {
            if (user.isAuthorized("essentials.sudo.exempt") && sender instanceof Player) {
               throw new Exception(I18n._("sudoExempt"));
            } else {
               user.chat(getFinalArg(args, 1).substring(2));
            }
         } else {
            final String command = args[1];
            final String[] arguments = new String[args.length - 2];
            if (arguments.length > 0) {
               System.arraycopy(args, 2, arguments, 0, args.length - 2);
            }

            if (user.isAuthorized("essentials.sudo.exempt") && sender instanceof Player) {
               throw new Exception(I18n._("sudoExempt"));
            } else {
               sender.sendMessage(I18n._("sudoRun", user.getDisplayName(), command, getFinalArg(arguments, 0)));
               final PluginCommand execCommand = this.ess.getServer().getPluginCommand(command);
               if (execCommand != null) {
                  this.ess.scheduleSyncDelayedTask(new Runnable() {
                     public void run() {
                        Commandsudo.LOGGER.log(Level.INFO, String.format("[Sudo] %s issued server command: /%s %s", user.getName(), command, EssentialsCommand.getFinalArg(arguments, 0)));
                        execCommand.execute(user.getBase(), command, arguments);
                     }
                  });
               } else {
                  sender.sendMessage(I18n._("errorCallingCommand", command));
               }

            }
         }
      }
   }
}
