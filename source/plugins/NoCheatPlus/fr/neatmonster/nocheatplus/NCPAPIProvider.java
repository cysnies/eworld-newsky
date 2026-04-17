package fr.neatmonster.nocheatplus;

import fr.neatmonster.nocheatplus.components.NoCheatPlusAPI;

public class NCPAPIProvider {
   private static NoCheatPlusAPI noCheatPlusAPI = null;

   public static NoCheatPlusAPI getNoCheatPlusAPI() {
      return noCheatPlusAPI;
   }

   protected static void setNoCheatPlusAPI(NoCheatPlusAPI noCheatPlusAPI) {
      NCPAPIProvider.noCheatPlusAPI = noCheatPlusAPI;
   }
}
