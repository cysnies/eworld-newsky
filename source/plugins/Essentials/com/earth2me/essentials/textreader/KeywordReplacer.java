package com.earth2me.essentials.textreader;

import com.earth2me.essentials.DescParseTickFormat;
import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class KeywordReplacer implements IText {
   private final transient IText input;
   private final transient List replaced;
   private final transient IEssentials ess;

   public KeywordReplacer(IText input, CommandSender sender, IEssentials ess) {
      this.input = input;
      this.replaced = new ArrayList(this.input.getLines().size());
      this.ess = ess;
      this.replaceKeywords(sender);
   }

   private void replaceKeywords(CommandSender sender) {
      String displayName;
      String ipAddress;
      String balance;
      String mails;
      String world;
      String worldTime12;
      String worldTime24;
      String worldDate;
      if (sender instanceof Player) {
         User user = this.ess.getUser(sender);
         user.setDisplayNick();
         displayName = user.getDisplayName();
         String userName = user.getName();
         ipAddress = user.getAddress() != null && user.getAddress().getAddress() != null ? user.getAddress().getAddress().toString() : "";
         if (user.getAddress() == null) {
            String var10000 = "";
         } else {
            user.getAddress().toString();
         }

         balance = Util.displayCurrency(user.getMoney(), this.ess);
         mails = Integer.toString(user.getMails().size());
         world = user.getLocation() != null && user.getLocation().getWorld() != null ? user.getLocation().getWorld().getName() : "";
         worldTime12 = DescParseTickFormat.format12(user.getWorld() == null ? 0L : user.getWorld().getTime());
         worldTime24 = DescParseTickFormat.format24(user.getWorld() == null ? 0L : user.getWorld().getTime());
         worldDate = DateFormat.getDateInstance(2, this.ess.getI18n().getCurrentLocale()).format(DescParseTickFormat.ticksToDate(user.getWorld() == null ? 0L : user.getWorld().getFullTime()));
      } else {
         worldDate = "";
         worldTime24 = "";
         worldTime12 = "";
         world = "";
         mails = "";
         balance = "";
         ipAddress = "";
         displayName = "";
      }

      int playerHidden = 0;

      for(Player p : this.ess.getServer().getOnlinePlayers()) {
         if (this.ess.getUser(p).isHidden()) {
            ++playerHidden;
         }
      }

      String online = Integer.toString(this.ess.getServer().getOnlinePlayers().length - playerHidden);
      String unique = Integer.toString(this.ess.getUserMap().getUniqueUsers());
      StringBuilder worldsBuilder = new StringBuilder();

      for(World w : this.ess.getServer().getWorlds()) {
         if (worldsBuilder.length() > 0) {
            worldsBuilder.append(", ");
         }

         worldsBuilder.append(w.getName());
      }

      String worlds = worldsBuilder.toString();
      StringBuilder playerlistBuilder = new StringBuilder();

      for(Player p : this.ess.getServer().getOnlinePlayers()) {
         if (!this.ess.getUser(p).isHidden()) {
            if (playerlistBuilder.length() > 0) {
               playerlistBuilder.append(", ");
            }

            playerlistBuilder.append(p.getDisplayName());
         }
      }

      String playerlist = playerlistBuilder.toString();
      StringBuilder pluginlistBuilder = new StringBuilder();

      for(Plugin p : this.ess.getServer().getPluginManager().getPlugins()) {
         if (pluginlistBuilder.length() > 0) {
            pluginlistBuilder.append(", ");
         }

         pluginlistBuilder.append(p.getDescription().getName());
      }

      String plugins = pluginlistBuilder.toString();
      String date = DateFormat.getDateInstance(2, this.ess.getI18n().getCurrentLocale()).format(new Date());
      String time = DateFormat.getTimeInstance(2, this.ess.getI18n().getCurrentLocale()).format(new Date());
      String version = this.ess.getServer().getVersion();

      for(int i = 0; i < this.input.getLines().size(); ++i) {
         String line = (String)this.input.getLines().get(i);
         line = line.replace("{PLAYER}", displayName);
         line = line.replace("{DISPLAYNAME}", displayName);
         line = line.replace("{USERNAME}", displayName);
         line = line.replace("{IP}", ipAddress);
         line = line.replace("{ADDRESS}", ipAddress);
         line = line.replace("{BALANCE}", balance);
         line = line.replace("{MAILS}", mails);
         line = line.replace("{WORLD}", world);
         line = line.replace("{ONLINE}", online);
         line = line.replace("{UNIQUE}", unique);
         line = line.replace("{WORLDS}", worlds);
         line = line.replace("{PLAYERLIST}", playerlist);
         line = line.replace("{TIME}", time);
         line = line.replace("{DATE}", date);
         line = line.replace("{WORLDTIME12}", worldTime12);
         line = line.replace("{WORLDTIME24}", worldTime24);
         line = line.replace("{WORLDDATE}", worldDate);
         line = line.replace("{PLUGINS}", plugins);
         line = line.replace("{VERSION}", version);
         this.replaced.add(line);
      }

   }

   public List getLines() {
      return this.replaced;
   }

   public List getChapters() {
      return this.input.getChapters();
   }

   public Map getBookmarks() {
      return this.input.getBookmarks();
   }
}
