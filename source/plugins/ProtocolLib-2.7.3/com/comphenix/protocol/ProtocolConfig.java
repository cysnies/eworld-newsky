package com.comphenix.protocol;

import com.comphenix.protocol.injector.PacketFilterManager;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

class ProtocolConfig {
   private static final String LAST_UPDATE_FILE = "lastupdate";
   private static final String SECTION_GLOBAL = "global";
   private static final String SECTION_AUTOUPDATER = "auto updater";
   private static final String METRICS_ENABLED = "metrics";
   private static final String IGNORE_VERSION_CHECK = "ignore version check";
   private static final String BACKGROUND_COMPILER_ENABLED = "background compiler";
   private static final String DEBUG_MODE_ENABLED = "debug";
   private static final String DETAILED_ERROR = "detailed error";
   private static final String INJECTION_METHOD = "injection method";
   private static final String SCRIPT_ENGINE_NAME = "script engine";
   private static final String SUPPRESSED_REPORTS = "suppressed reports";
   private static final String UPDATER_NOTIFY = "notify";
   private static final String UPDATER_DOWNLAD = "download";
   private static final String UPDATER_DELAY = "delay";
   private static final long DEFAULT_UPDATER_DELAY = 43200L;
   private Plugin plugin;
   private Configuration config;
   private boolean loadingSections;
   private ConfigurationSection global;
   private ConfigurationSection updater;
   private long lastUpdateTime;
   private boolean configChanged;
   private boolean valuesChanged;
   private int modCount;

   public ProtocolConfig(Plugin plugin) {
      this(plugin, plugin.getConfig());
   }

   public ProtocolConfig(Plugin plugin, Configuration config) {
      this.plugin = plugin;
      this.reloadConfig();
   }

   public void reloadConfig() {
      this.configChanged = false;
      this.valuesChanged = false;
      ++this.modCount;
      this.config = this.plugin.getConfig();
      this.lastUpdateTime = this.loadLastUpdate();
      this.loadSections(!this.loadingSections);
   }

   private long loadLastUpdate() {
      File dataFile = this.getLastUpdateFile();
      if (dataFile.exists()) {
         try {
            return Long.parseLong(Files.toString(dataFile, Charsets.UTF_8));
         } catch (NumberFormatException e) {
            throw new RuntimeException("Cannot parse " + dataFile + " as a number.", e);
         } catch (IOException e) {
            throw new RuntimeException("Cannot read " + dataFile, e);
         }
      } else {
         return 0L;
      }
   }

   private void saveLastUpdate(long value) {
      File dataFile = this.getLastUpdateFile();
      dataFile.getParentFile().mkdirs();
      if (dataFile.exists()) {
         dataFile.delete();
      }

      try {
         Files.write(Long.toString(value), dataFile, Charsets.UTF_8);
      } catch (IOException e) {
         throw new RuntimeException("Cannot write " + dataFile, e);
      }
   }

   private File getLastUpdateFile() {
      return new File(this.plugin.getDataFolder(), "lastupdate");
   }

   private void loadSections(boolean copyDefaults) {
      if (this.config != null) {
         this.global = this.config.getConfigurationSection("global");
      }

      if (this.global != null) {
         this.updater = this.global.getConfigurationSection("auto updater");
      }

      if (copyDefaults && (!this.getFile().exists() || this.global == null || this.updater == null)) {
         this.loadingSections = true;
         if (this.config != null) {
            this.config.options().copyDefaults(true);
         }

         this.plugin.saveDefaultConfig();
         this.plugin.reloadConfig();
         this.loadingSections = false;
         System.out.println("[ProtocolLib] Created default configuration.");
      }

   }

   private void setConfig(ConfigurationSection section, String path, Object value) {
      this.configChanged = true;
      section.set(path, value);
   }

   public File getFile() {
      return new File(this.plugin.getDataFolder(), "config.yml");
   }

   public boolean isDetailedErrorReporting() {
      return this.global.getBoolean("detailed error", false);
   }

   public void setDetailedErrorReporting(boolean value) {
      this.global.set("detailed error", value);
   }

   public boolean isAutoNotify() {
      return this.updater.getBoolean("notify", true);
   }

   public void setAutoNotify(boolean value) {
      this.setConfig(this.updater, "notify", value);
      ++this.modCount;
   }

   public boolean isAutoDownload() {
      return this.updater != null && this.updater.getBoolean("download", true);
   }

   public void setAutoDownload(boolean value) {
      this.setConfig(this.updater, "download", value);
      ++this.modCount;
   }

   public boolean isDebug() {
      return this.global.getBoolean("debug", false);
   }

   public void setDebug(boolean value) {
      this.setConfig(this.global, "debug", value);
      ++this.modCount;
   }

   public ImmutableList getSuppressedReports() {
      return ImmutableList.copyOf(this.global.getStringList("suppressed reports"));
   }

   public void setSuppressedReports(List reports) {
      this.global.set("suppressed reports", Lists.newArrayList(reports));
      ++this.modCount;
   }

   public long getAutoDelay() {
      return Math.max((long)this.updater.getInt("delay", 0), 43200L);
   }

   public void setAutoDelay(long delaySeconds) {
      if (delaySeconds < 43200L) {
         delaySeconds = 43200L;
      }

      this.setConfig(this.updater, "delay", delaySeconds);
      ++this.modCount;
   }

   public String getIgnoreVersionCheck() {
      return this.global.getString("ignore version check", "");
   }

   public void setIgnoreVersionCheck(String ignoreVersion) {
      this.setConfig(this.global, "ignore version check", ignoreVersion);
      ++this.modCount;
   }

   public boolean isMetricsEnabled() {
      return this.global.getBoolean("metrics", true);
   }

   public void setMetricsEnabled(boolean enabled) {
      this.setConfig(this.global, "metrics", enabled);
      ++this.modCount;
   }

   public boolean isBackgroundCompilerEnabled() {
      return this.global.getBoolean("background compiler", true);
   }

   public void setBackgroundCompilerEnabled(boolean enabled) {
      this.setConfig(this.global, "background compiler", enabled);
      ++this.modCount;
   }

   public long getAutoLastTime() {
      return this.lastUpdateTime;
   }

   public void setAutoLastTime(long lastTimeSeconds) {
      this.valuesChanged = true;
      this.lastUpdateTime = lastTimeSeconds;
   }

   public String getScriptEngineName() {
      return this.global.getString("script engine", "JavaScript");
   }

   public void setScriptEngineName(String name) {
      this.setConfig(this.global, "script engine", name);
      ++this.modCount;
   }

   public PacketFilterManager.PlayerInjectHooks getDefaultMethod() {
      return PacketFilterManager.PlayerInjectHooks.NETWORK_SERVER_OBJECT;
   }

   public PacketFilterManager.PlayerInjectHooks getInjectionMethod() throws IllegalArgumentException {
      String text = this.global.getString("injection method");
      PacketFilterManager.PlayerInjectHooks hook = this.getDefaultMethod();
      if (text != null) {
         hook = PacketFilterManager.PlayerInjectHooks.valueOf(text.toUpperCase().replace(" ", "_"));
      }

      return hook;
   }

   public void setInjectionMethod(PacketFilterManager.PlayerInjectHooks hook) {
      this.setConfig(this.global, "injection method", hook.name());
      ++this.modCount;
   }

   public int getModificationCount() {
      return this.modCount;
   }

   public void saveAll() {
      if (this.valuesChanged) {
         this.saveLastUpdate(this.lastUpdateTime);
      }

      if (this.configChanged) {
         this.plugin.saveConfig();
      }

      this.valuesChanged = false;
      this.configChanged = false;
   }
}
