package com.sk89q.bukkit.util;

import com.sk89q.minecraft.util.commands.CommandsManager;
import com.sk89q.wepif.PermissionsResolverManager;
import com.sk89q.wepif.WEPIFRuntimeException;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.help.HelpTopic;
import org.bukkit.help.HelpTopicFactory;

public class DynamicPluginCommandHelpTopic extends HelpTopic {
   private final DynamicPluginCommand cmd;

   public DynamicPluginCommandHelpTopic(DynamicPluginCommand cmd) {
      this.cmd = cmd;
      this.name = "/" + cmd.getName();
      String fullTextTemp = null;
      StringBuilder fullText = new StringBuilder();
      if (cmd.getRegisteredWith() instanceof CommandsManager) {
         Map<String, String> helpText = ((CommandsManager)cmd.getRegisteredWith()).getHelpMessages();
         String lookupName = cmd.getName().replaceAll("/", "");
         if (helpText.containsKey(lookupName)) {
            fullTextTemp = (String)helpText.get(lookupName);
         }

         helpText = ((CommandsManager)cmd.getRegisteredWith()).getCommands();
         if (helpText.containsKey(cmd.getName())) {
            String shortText = (String)helpText.get(cmd.getName());
            if (fullTextTemp == null) {
               fullTextTemp = this.name + " " + shortText;
            }

            this.shortText = shortText;
         }
      } else {
         this.shortText = cmd.getDescription();
      }

      String[] split = fullTextTemp == null ? new String[2] : fullTextTemp.split("\n", 2);
      fullText.append(ChatColor.BOLD).append(ChatColor.GOLD).append("Usage: ").append(ChatColor.WHITE);
      fullText.append(split[0]).append("\n");
      if (cmd.getAliases().size() > 0) {
         fullText.append(ChatColor.BOLD).append(ChatColor.GOLD).append("Aliases: ").append(ChatColor.WHITE);
         boolean first = true;

         for(String alias : cmd.getAliases()) {
            if (!first) {
               fullText.append(", ");
            }

            fullText.append(alias);
            first = false;
         }

         fullText.append("\n");
      }

      if (split.length > 1) {
         fullText.append(split[1]);
      }

      this.fullText = fullText.toString();
   }

   public boolean canSee(CommandSender player) {
      if (this.cmd.getPermissions() != null && this.cmd.getPermissions().length > 0) {
         if (this.cmd.getRegisteredWith() instanceof CommandsManager) {
            try {
               for(String perm : this.cmd.getPermissions()) {
                  if (((CommandsManager)this.cmd.getRegisteredWith()).hasPermission((Object)player, (String)perm)) {
                     return true;
                  }
               }
            } catch (Throwable var7) {
            }
         }

         if (player instanceof OfflinePlayer) {
            try {
               for(String perm : this.cmd.getPermissions()) {
                  if (PermissionsResolverManager.getInstance().hasPermission((OfflinePlayer)player, perm)) {
                     return true;
                  }
               }
            } catch (WEPIFRuntimeException var6) {
            }
         }

         for(String perm : this.cmd.getPermissions()) {
            if (player.hasPermission(perm)) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   public String getFullText(CommandSender forWho) {
      return this.fullText != null && this.fullText.length() != 0 ? this.fullText : this.getShortText();
   }

   public static class Factory implements HelpTopicFactory {
      public HelpTopic createTopic(DynamicPluginCommand command) {
         return new DynamicPluginCommandHelpTopic(command);
      }
   }
}
