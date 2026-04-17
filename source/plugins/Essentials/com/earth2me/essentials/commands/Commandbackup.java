package com.earth2me.essentials.commands;

import com.earth2me.essentials.Backup;
import com.earth2me.essentials.I18n;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;

public class Commandbackup extends EssentialsCommand {
   public Commandbackup() {
      super("backup");
   }

   protected void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      Backup backup = this.ess.getBackup();
      if (backup == null) {
         throw new Exception(I18n._("backupDisabled"));
      } else {
         String command = this.ess.getSettings().getBackupCommand();
         if (command != null && !"".equals(command) && !"save-all".equalsIgnoreCase(command)) {
            backup.run();
            sender.sendMessage(I18n._("backupStarted"));
         } else {
            throw new Exception(I18n._("backupDisabled"));
         }
      }
   }
}
