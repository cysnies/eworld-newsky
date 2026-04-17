package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.Util;
import java.lang.management.ManagementFactory;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

public class Commandgc extends EssentialsCommand {
   public Commandgc() {
      super("gc");
   }

   protected void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      float tps = this.ess.getTimer().getAverageTPS();
      ChatColor color;
      if (tps >= 18.0F) {
         color = ChatColor.GREEN;
      } else if (tps >= 15.0F) {
         color = ChatColor.YELLOW;
      } else {
         color = ChatColor.RED;
      }

      sender.sendMessage(I18n._("uptime", Util.formatDateDiff(ManagementFactory.getRuntimeMXBean().getStartTime())));
      sender.sendMessage(I18n._("tps", "" + color + tps));
      sender.sendMessage(I18n._("gcmax", Runtime.getRuntime().maxMemory() / 1024L / 1024L));
      sender.sendMessage(I18n._("gctotal", Runtime.getRuntime().totalMemory() / 1024L / 1024L));
      sender.sendMessage(I18n._("gcfree", Runtime.getRuntime().freeMemory() / 1024L / 1024L));

      for(World w : server.getWorlds()) {
         String worldType = "World";
         switch (w.getEnvironment()) {
            case NETHER:
               worldType = "Nether";
               break;
            case THE_END:
               worldType = "The End";
         }

         sender.sendMessage(I18n._("gcWorld", worldType, w.getName(), w.getLoadedChunks().length, w.getEntities().size()));
      }

   }
}
