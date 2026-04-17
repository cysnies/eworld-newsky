package fr.neatmonster.nocheatplus.checks.access;

import fr.neatmonster.nocheatplus.config.ConfigFile;

public abstract class ACheckConfig implements ICheckConfig {
   public boolean debug = false;
   public final boolean lag;

   public ACheckConfig(ConfigFile config, String pathPrefix) {
      this.debug = config.getBoolean(pathPrefix + "debug", config.getBoolean("checks.debug", false));
      this.lag = config.getBoolean(pathPrefix + "lag", true) && config.getBoolean("miscellaneous.lag", true);
   }

   public String[] getCachePermissions() {
      return null;
   }

   public boolean getDebug() {
      return this.debug;
   }

   public void setDebug(boolean debug) {
      this.debug = debug;
   }
}
