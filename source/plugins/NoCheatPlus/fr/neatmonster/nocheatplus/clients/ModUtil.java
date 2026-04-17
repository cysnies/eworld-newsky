package fr.neatmonster.nocheatplus.clients;

import fr.neatmonster.nocheatplus.clients.motd.CJBMOTD;
import fr.neatmonster.nocheatplus.clients.motd.ClientMOTD;
import fr.neatmonster.nocheatplus.clients.motd.MCAutoMapMOTD;
import fr.neatmonster.nocheatplus.clients.motd.ReiMOTD;
import fr.neatmonster.nocheatplus.clients.motd.SmartMovingMOTD;
import fr.neatmonster.nocheatplus.clients.motd.ZombeMOTD;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import org.bukkit.entity.Player;

public class ModUtil {
   private static final ClientMOTD[] motdS = new ClientMOTD[]{new ReiMOTD(), new ZombeMOTD(), new SmartMovingMOTD(), new CJBMOTD(), new MCAutoMapMOTD()};

   public static void motdOnJoin(Player player) {
      ConfigFile config = ConfigManager.getConfigFile();
      if (config.getBoolean("protection.clients.motd.active")) {
         boolean allowAll = config.getBoolean("protection.clients.motd.allowall");
         String message = "";

         for(int i = 0; i < motdS.length; ++i) {
            message = motdS[i].onPlayerJoin(message, player, allowAll);
         }

         if (!message.isEmpty()) {
            player.sendMessage(message);
         }

      }
   }
}
