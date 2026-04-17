package org.anjocaido.groupmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.anjocaido.groupmanager.utils.Tasks;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.reader.UnicodeReader;

public class GMConfiguration {
   private boolean allowCommandBlocks = false;
   private boolean opOverride = true;
   private boolean toggleValidate = true;
   private Integer saveInterval = 10;
   private Integer backupDuration = 24;
   private String loggerLevel = "OFF";
   private Map mirrorsMap;
   private GroupManager plugin;
   private Map GMconfig;

   public GMConfiguration(GroupManager plugin) {
      this.plugin = plugin;
      this.allowCommandBlocks = false;
      this.opOverride = true;
      this.toggleValidate = true;
      this.saveInterval = 10;
      this.backupDuration = 24;
      this.loggerLevel = "OFF";
      this.load();
   }

   public void load() {
      if (!this.plugin.getDataFolder().exists()) {
         this.plugin.getDataFolder().mkdirs();
      }

      File configFile = new File(this.plugin.getDataFolder(), "config.yml");
      if (!configFile.exists()) {
         try {
            Tasks.copy(this.plugin.getResourceAsStream("config.yml"), configFile);
         } catch (IOException ex) {
            GroupManager.logger.log(Level.SEVERE, "Error creating a new config.yml", ex);
         }
      }

      Yaml configYAML = new Yaml(new SafeConstructor());

      try {
         FileInputStream configInputStream = new FileInputStream(configFile);
         this.GMconfig = (Map)configYAML.load(new UnicodeReader(configInputStream));
         configInputStream.close();
      } catch (Exception ex) {
         throw new IllegalArgumentException("The following file couldn't pass on Parser.\n" + configFile.getPath(), ex);
      }

      try {
         Map<String, Object> config = this.getElement("config", this.getElement("settings", this.GMconfig));

         try {
            this.allowCommandBlocks = (Boolean)config.get("allow_commandblocks");
         } catch (Exception ex) {
            GroupManager.logger.log(Level.SEVERE, "Missing or corrupt 'allow_commandblocks' node. Using default settings", ex);
         }

         try {
            this.opOverride = (Boolean)config.get("opOverrides");
         } catch (Exception ex) {
            GroupManager.logger.log(Level.SEVERE, "Missing or corrupt 'opOverrides' node. Using default settings", ex);
         }

         try {
            this.toggleValidate = (Boolean)config.get("validate_toggle");
         } catch (Exception ex) {
            GroupManager.logger.log(Level.SEVERE, "Missing or corrupt 'validate_toggle' node. Using default settings", ex);
         }

         try {
            Map<String, Object> save = this.getElement("save", this.getElement("data", this.getElement("settings", this.GMconfig)));

            try {
               this.saveInterval = (Integer)save.get("minutes");
            } catch (Exception ex) {
               GroupManager.logger.log(Level.SEVERE, "Missing or corrupt 'minutes' node. Using default setting", ex);
            }

            try {
               this.backupDuration = (Integer)save.get("hours");
            } catch (Exception ex) {
               GroupManager.logger.log(Level.SEVERE, "Missing or corrupt 'hours' node. Using default setting", ex);
            }
         } catch (Exception ex) {
            GroupManager.logger.log(Level.SEVERE, "Missing or corrupt 'data' node. Using default settings", ex);
         }

         Object level = ((Map)this.getElement("settings", this.GMconfig).get("logging")).get("level");
         if (level instanceof String) {
            this.loggerLevel = (String)level;
         }

         this.mirrorsMap = (Map)((Map)this.GMconfig.get("settings")).get("mirrors");
      } catch (Exception ex) {
         GroupManager.logger.log(Level.SEVERE, "There are errors in your config.yml. Using default settings", ex);
         this.mirrorsMap = new HashMap();
      }

      this.adjustLoggerLevel();
      this.plugin.setValidateOnlinePlayer(this.isToggleValidate());
   }

   private Map getElement(String element, Map map) {
      if (!map.containsKey(element)) {
         throw new IllegalArgumentException("The config.yml has no '" + element + ".\n");
      } else {
         return (Map)map.get(element);
      }
   }

   public boolean isAllowCommandBlocks() {
      return this.allowCommandBlocks;
   }

   public boolean isOpOverride() {
      return this.opOverride;
   }

   public boolean isToggleValidate() {
      return this.toggleValidate;
   }

   public Integer getSaveInterval() {
      return this.saveInterval;
   }

   public Integer getBackupDuration() {
      return this.backupDuration;
   }

   public void adjustLoggerLevel() {
      try {
         GroupManager.logger.setLevel(Level.parse(this.loggerLevel));
      } catch (Exception var2) {
         GroupManager.logger.setLevel(Level.INFO);
      }
   }

   public Map getMirrorsMap() {
      return !this.mirrorsMap.isEmpty() ? this.mirrorsMap : null;
   }
}
