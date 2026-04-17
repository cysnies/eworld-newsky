package com.goncalomb.bukkit.betterplugin;

import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

final class InternalCommand extends Command {
   private BetterCommand _command;

   public InternalCommand(BetterCommand command, String name) {
      super(name);
      this._command = command;
   }

   public boolean execute(CommandSender sender, String label, String[] args) {
      if (this._command._plugin != null && this._command._plugin.isEnabled()) {
         if (!this.testPermission(sender)) {
            return true;
         }

         this._command.execute(sender, label, args);
      } else {
         sender.sendMessage("Nop!");
      }

      return true;
   }

   public List tabComplete(CommandSender sender, String alias, String[] args) {
      return this._command._plugin != null && this._command._plugin.isEnabled() && this.testPermissionSilent(sender) ? this._command.tabComplete(sender, args) : null;
   }
}
