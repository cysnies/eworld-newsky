package com.earth2me.essentials.antibuild;

public enum AntiBuildConfig {
   disable_build("protect.disable.build", true),
   disable_use("protect.disable.use", true),
   alert_on_placement("protect.alert.on-placement"),
   alert_on_use("protect.alert.on-use"),
   alert_on_break("protect.alert.on-break"),
   blacklist_placement("protect.blacklist.placement"),
   blacklist_usage("protect.blacklist.usage"),
   blacklist_break("protect.blacklist.break"),
   blacklist_piston("protect.blacklist.piston");

   private final String configName;
   private final String defValueString;
   private final boolean defValueBoolean;
   private final boolean isList;
   private final boolean isString;

   private AntiBuildConfig(String configName) {
      this(configName, (String)null, false, true, false);
   }

   private AntiBuildConfig(String configName, boolean defValueBoolean) {
      this(configName, (String)null, defValueBoolean, false, false);
   }

   private AntiBuildConfig(String configName, String defValueString, boolean defValueBoolean, boolean isList, boolean isString) {
      this.configName = configName;
      this.defValueString = defValueString;
      this.defValueBoolean = defValueBoolean;
      this.isList = isList;
      this.isString = isString;
   }

   public String getConfigName() {
      return this.configName;
   }

   public String getDefaultValueString() {
      return this.defValueString;
   }

   public boolean getDefaultValueBoolean() {
      return this.defValueBoolean;
   }

   public boolean isString() {
      return this.isString;
   }

   public boolean isList() {
      return this.isList;
   }
}
