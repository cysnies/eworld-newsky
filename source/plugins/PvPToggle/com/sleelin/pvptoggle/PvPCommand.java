package com.sleelin.pvptoggle;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class PvPCommand {
   protected PvPToggle plugin;
   protected CommandSender sender;
   protected Player player;
   protected Command command;
   protected String label;
   protected String[] args;
   protected String helpHeader;

   public PvPCommand(PvPToggle plugin, CommandSender sender, Command command, String label, String[] args) {
      this.plugin = plugin;
      this.sender = sender;
      if (sender instanceof Player) {
         this.player = (Player)sender;
      }

      this.command = command;
      this.label = label;
      this.args = args;
      this.helpHeader = ChatColor.RED + "-----------------[ " + ChatColor.GOLD + "PvPToggle v" + plugin.getDescription().getVersion() + ChatColor.RED + " ]-----------------";
   }

   public PvPCommand(PvPToggle plugin, CommandSender sender) {
      this.helpHeader = ChatColor.RED + "-----------------[ " + ChatColor.GOLD + "PvPToggle v" + plugin.getDescription().getVersion() + ChatColor.RED + " ]-----------------";
      this.plugin = plugin;
      this.sendUsage(sender);
   }

   public PvPCommand exec() {
      this.processCommand();
      return this;
   }

   protected abstract boolean processCommand();

   protected abstract void sendUsage(CommandSender var1);

   protected Player getPlayer(CommandSender sender, String player, boolean notify) {
      List<Player> found = new ArrayList();
      Player[] players = this.plugin.getServer().getOnlinePlayers();

      for(Player search : players) {
         if (search.getDisplayName().toLowerCase().contains(player.toLowerCase()) || search.getName().toLowerCase().contains(player.toLowerCase())) {
            found.add(search);
         }
      }

      if (found.size() == 1) {
         return (Player)found.get(0);
      } else {
         if (found.size() > 1) {
            if (notify) {
               sender.sendMessage("Found " + found.size() + " online players matching that partial name:");
            }

            for(Player p : found) {
               if (notify) {
                  sender.sendMessage("- " + p.getDisplayName());
               }
            }
         } else if (notify) {
            PvPLocalisation.display(sender, "", "", "", PvPLocalisation.Strings.PLAYER_NOT_FOUND);
         }

         return null;
      }
   }

   protected static Boolean checkNewValue(String string) {
      Boolean enable = null;
      if (!string.equalsIgnoreCase("on") && !string.equalsIgnoreCase("enable")) {
         if (string.equalsIgnoreCase("off") || string.equalsIgnoreCase("disable")) {
            enable = false;
         }
      } else {
         enable = true;
      }

      return enable;
   }

   protected String isWorld(CommandSender sender, String worldname, boolean notify) {
      String output = this.plugin.checkWorldName(worldname);
      if (output == null && notify) {
         PvPLocalisation.display(sender, (String)null, (String)null, (String)null, PvPLocalisation.Strings.WORLD_NOT_FOUND);
      }

      return output;
   }

   protected String isWorld(CommandSender sender, String worldname) {
      return this.isWorld(sender, worldname, true);
   }

   protected void toggleGlobalStatus(boolean newval) {
      this.plugin.toggleGlobalStatus(newval);
   }

   protected void setWorldStatus(String targetworld, boolean newval) {
      this.plugin.setWorldStatus(targetworld, newval);
   }
}
