package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commandlist extends EssentialsCommand {
   public Commandlist() {
      super("list");
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      boolean showHidden = true;
      if (sender instanceof Player) {
         showHidden = this.ess.getUser(sender).isAuthorized("essentials.list.hidden");
      }

      sender.sendMessage(this.listSummary(server, showHidden));
      Map<String, List<User>> playerList = this.getPlayerLists(server, showHidden);
      if (args.length > 0) {
         sender.sendMessage(this.listGroupUsers(playerList, args[0].toLowerCase()));
      } else {
         this.sendGroupedList(sender, commandLabel, playerList);
      }

   }

   private String listSummary(Server server, boolean showHidden) {
      int playerHidden = 0;

      for(Player onlinePlayer : server.getOnlinePlayers()) {
         if (this.ess.getUser(onlinePlayer).isHidden()) {
            ++playerHidden;
         }
      }

      String online;
      if (showHidden && playerHidden > 0) {
         online = I18n._("listAmountHidden", server.getOnlinePlayers().length - playerHidden, playerHidden, server.getMaxPlayers());
      } else {
         online = I18n._("listAmount", server.getOnlinePlayers().length - playerHidden, server.getMaxPlayers());
      }

      return online;
   }

   private Map getPlayerLists(Server server, boolean showHidden) {
      Map<String, List<User>> playerList = new HashMap();

      for(Player onlinePlayer : server.getOnlinePlayers()) {
         User onlineUser = this.ess.getUser(onlinePlayer);
         if (!onlineUser.isHidden() || showHidden) {
            String group = Util.stripFormat(onlineUser.getGroup().toLowerCase());
            List<User> list = (List)playerList.get(group);
            if (list == null) {
               list = new ArrayList();
               playerList.put(group, list);
            }

            list.add(onlineUser);
         }
      }

      return playerList;
   }

   private String listGroupUsers(Map playerList, String groupName) throws Exception {
      List<User> users = this.getMergedList(playerList, groupName);
      List<User> groupUsers = (List)playerList.get(groupName);
      if (groupUsers != null && !groupUsers.isEmpty()) {
         users.addAll(groupUsers);
      }

      if (users != null && !users.isEmpty()) {
         return this.outputFormat(groupName, this.listUsers(users));
      } else {
         throw new Exception(I18n._("groupDoesNotExist"));
      }
   }

   private List getMergedList(Map playerList, String groupName) {
      Set<String> configGroups = this.ess.getSettings().getListGroupConfig().keySet();
      List<User> users = new ArrayList();

      for(String configGroup : configGroups) {
         if (configGroup.equalsIgnoreCase(groupName)) {
            String[] groupValues = this.ess.getSettings().getListGroupConfig().get(configGroup).toString().trim().split(" ");

            for(String groupValue : groupValues) {
               if (groupValue != null && !groupValue.equals("")) {
                  List<User> u = (List)playerList.get(groupValue.trim());
                  if (u != null && !u.isEmpty()) {
                     playerList.remove(groupValue);
                     users.addAll(u);
                  }
               }
            }
         }
      }

      return users;
   }

   private void sendGroupedList(CommandSender sender, String commandLabel, Map playerList) {
      Set<String> configGroups = this.ess.getSettings().getListGroupConfig().keySet();
      List<String> asterisk = new ArrayList();

      for(String oConfigGroup : configGroups) {
         String groupValue = this.ess.getSettings().getListGroupConfig().get(oConfigGroup).toString().trim();
         String configGroup = oConfigGroup.toLowerCase();
         if (groupValue.equals("*")) {
            asterisk.add(oConfigGroup);
         } else if (groupValue.equalsIgnoreCase("hidden")) {
            playerList.remove(groupValue);
         } else {
            List<User> outputUserList = new ArrayList();
            List<User> matchedList = (List)playerList.get(configGroup);
            if (Util.isInt(groupValue) && matchedList != null && !matchedList.isEmpty()) {
               playerList.remove(configGroup);
               outputUserList.addAll(matchedList);
               int limit = Integer.parseInt(groupValue);
               if (matchedList.size() > limit) {
                  sender.sendMessage(this.outputFormat(oConfigGroup, I18n._("groupNumber", matchedList.size(), commandLabel, Util.stripFormat(configGroup))));
               } else {
                  sender.sendMessage(this.outputFormat(oConfigGroup, this.listUsers(outputUserList)));
               }
            } else {
               outputUserList = this.getMergedList(playerList, configGroup);
               if (outputUserList != null && !outputUserList.isEmpty()) {
                  sender.sendMessage(this.outputFormat(oConfigGroup, this.listUsers(outputUserList)));
               }
            }
         }
      }

      String[] onlineGroups = (String[])playerList.keySet().toArray(new String[0]);
      Arrays.sort(onlineGroups, String.CASE_INSENSITIVE_ORDER);
      if (!asterisk.isEmpty()) {
         List<User> asteriskUsers = new ArrayList();

         for(String onlineGroup : onlineGroups) {
            asteriskUsers.addAll((Collection)playerList.get(onlineGroup));
         }

         for(String key : asterisk) {
            playerList.put(key, asteriskUsers);
         }

         onlineGroups = (String[])asterisk.toArray(new String[0]);
      }

      for(String onlineGroup : onlineGroups) {
         List<User> users = (List)playerList.get(onlineGroup);
         String groupName = asterisk.isEmpty() ? ((User)users.get(0)).getGroup() : onlineGroup;
         if (this.ess.getPermissionsHandler().getName().equals("ConfigPermissions")) {
            groupName = I18n._("connectedPlayers");
         }

         if (users != null && !users.isEmpty()) {
            sender.sendMessage(this.outputFormat(groupName, this.listUsers(users)));
         }
      }

   }

   private String listUsers(List users) {
      StringBuilder groupString = new StringBuilder();
      Collections.sort(users);
      boolean needComma = false;

      for(User user : users) {
         if (needComma) {
            groupString.append(", ");
         }

         needComma = true;
         if (user.isAfk()) {
            groupString.append(I18n._("listAfkTag"));
         }

         if (user.isHidden()) {
            groupString.append(I18n._("listHiddenTag"));
         }

         user.setDisplayNick();
         groupString.append(user.getDisplayName());
         groupString.append("§f");
      }

      return groupString.toString();
   }

   private String outputFormat(String group, String message) {
      StringBuilder outputString = new StringBuilder();
      outputString.append(I18n._("listGroupTag", Util.replaceFormat(group)));
      outputString.append(message);
      outputString.setCharAt(0, Character.toTitleCase(outputString.charAt(0)));
      return outputString.toString();
   }
}
