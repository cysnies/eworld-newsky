package fr.neatmonster.nocheatplus.checks.access;

import fr.neatmonster.nocheatplus.config.ConfigFile;

public abstract class AsyncCheckConfig extends ACheckConfig {
   protected String[] cachePermissions;

   public AsyncCheckConfig(ConfigFile config, String pathPrefix, String[] cachePermissions) {
      super(config, pathPrefix);
      this.cachePermissions = cachePermissions;
   }

   public String[] getCachePermissions() {
      return this.cachePermissions;
   }
}
