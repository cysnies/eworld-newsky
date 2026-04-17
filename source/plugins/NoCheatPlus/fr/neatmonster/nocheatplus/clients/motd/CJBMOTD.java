package fr.neatmonster.nocheatplus.clients.motd;

import org.bukkit.entity.Player;

public class CJBMOTD extends ClientMOTD {
   public String onPlayerJoin(String message, Player player, boolean allowAll) {
      if (allowAll) {
         return message;
      } else {
         String cjb = "";
         if (!player.hasPermission("nocheatplus.mods.cjb.fly")) {
            cjb = cjb + "§3 §9 §2 §0 §0 §1";
         }

         if (!player.hasPermission("nocheatplus.mods.cjb.xray")) {
            cjb = cjb + "§3 §9 §2 §0 §0 §2";
         }

         if (!player.hasPermission("nocheatplus.mods.cjb.radar")) {
            cjb = cjb + "§3 §9 §2 §0 §0 §3";
         }

         return cjb.isEmpty() ? message : message + cjb;
      }
   }
}
