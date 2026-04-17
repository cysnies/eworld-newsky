package fr.neatmonster.nocheatplus.clients.motd;

import org.bukkit.entity.Player;

public class ZombeMOTD extends ClientMOTD {
   public String onPlayerJoin(String message, Player player, boolean allowAll) {
      String zombe = "";
      if (allowAll || player.hasPermission("nocheatplus.mods.zombe.noclip")) {
         zombe = zombe + "§f §f §4 §0 §9 §6";
      }

      if (!allowAll) {
         if (!player.hasPermission("nocheatplus.mods.zombe.fly")) {
            zombe = zombe + "§f §f §1 §0 §2 §4";
         }

         if (!player.hasPermission("nocheatplus.mods.zombe.cheat")) {
            zombe = zombe + "§f §f §2 §0 §4 §8";
         }
      }

      return zombe.isEmpty() ? message : message + zombe;
   }
}
