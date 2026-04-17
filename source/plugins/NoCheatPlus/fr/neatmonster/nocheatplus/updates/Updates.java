package fr.neatmonster.nocheatplus.updates;

import fr.neatmonster.nocheatplus.config.ConfigFile;

public class Updates {
   public static boolean isConfigOutdated(int neededVersion, ConfigFile config) {
      try {
         int configurationVersion = Integer.parseInt(config.options().header().split("-b")[1].split("\\.")[0]);
         if (neededVersion > configurationVersion) {
            return true;
         }
      } catch (Exception var3) {
      }

      return false;
   }

   public static boolean checkForUpdates(String versionString, int updateTimeout) {
      boolean updateAvailable = false;
      return updateAvailable;
   }
}
