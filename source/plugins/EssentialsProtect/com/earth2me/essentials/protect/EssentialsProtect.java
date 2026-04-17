package com.earth2me.essentials.protect;

import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class EssentialsProtect extends JavaPlugin implements IProtect {
   private static final Logger LOGGER = Logger.getLogger("Minecraft");
   private final Map settingsBoolean = new EnumMap(ProtectConfig.class);
   private final Map settingsString = new EnumMap(ProtectConfig.class);
   private final Map settingsList = new EnumMap(ProtectConfig.class);
   private EssentialsConnect ess = null;

   public void onEnable() {
      PluginManager pm = this.getServer().getPluginManager();
      Plugin essPlugin = pm.getPlugin("Essentials");
      if (essPlugin != null && essPlugin.isEnabled()) {
         this.ess = new EssentialsConnect(essPlugin, this);
         EssentialsProtectBlockListener blockListener = new EssentialsProtectBlockListener(this);
         pm.registerEvents(blockListener, this);
         EssentialsProtectEntityListener entityListener = new EssentialsProtectEntityListener(this);
         pm.registerEvents(entityListener, this);
         EssentialsProtectWeatherListener weatherListener = new EssentialsProtectWeatherListener(this);
         pm.registerEvents(weatherListener, this);
      } else {
         this.enableEmergencyMode(pm);
      }
   }

   private void enableEmergencyMode(PluginManager pm) {
      EmergencyListener emListener = new EmergencyListener();
      pm.registerEvents(emListener, this);

      for(Player player : this.getServer().getOnlinePlayers()) {
         player.sendMessage("Essentials Protect is in emergency mode. Check your log for errors.");
      }

      LOGGER.log(Level.SEVERE, "Essentials not installed or failed to load. Essenials Protect is in emergency mode now.");
   }

   public EssentialsConnect getEssentialsConnect() {
      return this.ess;
   }

   public Map getSettingsBoolean() {
      return this.settingsBoolean;
   }

   public Map getSettingsString() {
      return this.settingsString;
   }

   public Map getSettingsList() {
      return this.settingsList;
   }

   public boolean getSettingBool(ProtectConfig protectConfig) {
      Boolean bool = (Boolean)this.settingsBoolean.get(protectConfig);
      return bool == null ? protectConfig.getDefaultValueBoolean() : bool;
   }

   public String getSettingString(ProtectConfig protectConfig) {
      String str = (String)this.settingsString.get(protectConfig);
      return str == null ? protectConfig.getDefaultValueString() : str;
   }
}
