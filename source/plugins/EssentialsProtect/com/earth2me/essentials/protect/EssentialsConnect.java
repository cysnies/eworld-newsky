package com.earth2me.essentials.protect;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.IConf;
import com.earth2me.essentials.IEssentials;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.plugin.Plugin;

public class EssentialsConnect {
   private static final Logger LOGGER = Logger.getLogger("Minecraft");
   private final IEssentials ess;
   private final IProtect protect;

   public EssentialsConnect(Plugin essPlugin, Plugin essProtect) {
      if (!essProtect.getDescription().getVersion().equals(essPlugin.getDescription().getVersion())) {
         LOGGER.log(Level.WARNING, I18n._("versionMismatchAll", new Object[0]));
      }

      this.ess = (IEssentials)essPlugin;
      this.protect = (IProtect)essProtect;
      ProtectReloader pr = new ProtectReloader();
      pr.reloadConfig();
      this.ess.addReloadListener(pr);
   }

   public IEssentials getEssentials() {
      return this.ess;
   }

   private class ProtectReloader implements IConf {
      private ProtectReloader() {
      }

      public void reloadConfig() {
         for(ProtectConfig protectConfig : ProtectConfig.values()) {
            if (protectConfig.isList()) {
               EssentialsConnect.this.protect.getSettingsList().put(protectConfig, EssentialsConnect.this.ess.getSettings().getProtectList(protectConfig.getConfigName()));
            } else if (protectConfig.isString()) {
               EssentialsConnect.this.protect.getSettingsString().put(protectConfig, EssentialsConnect.this.ess.getSettings().getProtectString(protectConfig.getConfigName()));
            } else {
               EssentialsConnect.this.protect.getSettingsBoolean().put(protectConfig, EssentialsConnect.this.ess.getSettings().getProtectBoolean(protectConfig.getConfigName(), protectConfig.getDefaultValueBoolean()));
            }
         }

      }
   }
}
