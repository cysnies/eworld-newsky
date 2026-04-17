package com.earth2me.essentials.antibuild;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class EssentialsAntiBuild extends JavaPlugin implements IAntiBuild {
   private static final Logger LOGGER = Logger.getLogger("Minecraft");
   private final transient Map settingsBoolean = new EnumMap(AntiBuildConfig.class);
   private final transient Map settingsList = new EnumMap(AntiBuildConfig.class);
   private transient EssentialsConnect ess = null;

   public void onEnable() {
      PluginManager pm = this.getServer().getPluginManager();
      Plugin essPlugin = pm.getPlugin("Essentials");
      if (essPlugin != null && essPlugin.isEnabled()) {
         this.ess = new EssentialsConnect(essPlugin, this);
         EssentialsAntiBuildListener blockListener = new EssentialsAntiBuildListener(this);
         pm.registerEvents(blockListener, this);
      }
   }

   public boolean checkProtectionItems(AntiBuildConfig list, int id) {
      List<Integer> itemList = (List)this.settingsList.get(list);
      return itemList != null && !itemList.isEmpty() && itemList.contains(id);
   }

   public EssentialsConnect getEssentialsConnect() {
      return this.ess;
   }

   public Map getSettingsBoolean() {
      return this.settingsBoolean;
   }

   public Map getSettingsList() {
      return this.settingsList;
   }

   public boolean getSettingBool(AntiBuildConfig protectConfig) {
      Boolean bool = (Boolean)this.settingsBoolean.get(protectConfig);
      return bool == null ? protectConfig.getDefaultValueBoolean() : bool;
   }
}
