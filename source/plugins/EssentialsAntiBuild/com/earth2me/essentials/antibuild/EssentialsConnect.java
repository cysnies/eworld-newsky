package com.earth2me.essentials.antibuild;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.IConf;
import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.User;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class EssentialsConnect {
   private static final Logger LOGGER = Logger.getLogger("Minecraft");
   private final transient IEssentials ess;
   private final transient IAntiBuild protect;

   public EssentialsConnect(Plugin essPlugin, Plugin essProtect) {
      if (!essProtect.getDescription().getVersion().equals(essPlugin.getDescription().getVersion())) {
         LOGGER.log(Level.WARNING, I18n._("versionMismatchAll", new Object[0]));
      }

      this.ess = (IEssentials)essPlugin;
      this.protect = (IAntiBuild)essProtect;
      AntiBuildReloader pr = new AntiBuildReloader();
      pr.reloadConfig();
      this.ess.addReloadListener(pr);
   }

   public void onDisable() {
   }

   public IEssentials getEssentials() {
      return this.ess;
   }

   public void alert(User user, String item, String type) {
      Location loc = user.getLocation();
      String warnMessage = I18n._("alertFormat", new Object[]{user.getName(), type, item, loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ()});
      LOGGER.log(Level.WARNING, warnMessage);

      for(Player p : this.ess.getServer().getOnlinePlayers()) {
         User alertUser = this.ess.getUser(p);
         if (alertUser.isAuthorized("essentials.protect.alerts")) {
            alertUser.sendMessage(warnMessage);
         }
      }

   }

   private class AntiBuildReloader implements IConf {
      private AntiBuildReloader() {
      }

      public void reloadConfig() {
         for(AntiBuildConfig protectConfig : AntiBuildConfig.values()) {
            if (protectConfig.isList()) {
               EssentialsConnect.this.protect.getSettingsList().put(protectConfig, EssentialsConnect.this.ess.getSettings().getProtectList(protectConfig.getConfigName()));
            } else {
               EssentialsConnect.this.protect.getSettingsBoolean().put(protectConfig, EssentialsConnect.this.ess.getSettings().getProtectBoolean(protectConfig.getConfigName(), protectConfig.getDefaultValueBoolean()));
            }
         }

      }
   }
}
