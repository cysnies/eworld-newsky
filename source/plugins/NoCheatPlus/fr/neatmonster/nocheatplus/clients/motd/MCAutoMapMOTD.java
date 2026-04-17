package fr.neatmonster.nocheatplus.clients.motd;

import org.bukkit.entity.Player;

public class MCAutoMapMOTD extends ClientMOTD {
   public String onPlayerJoin(String message, Player player, boolean allowAll) {
      if (allowAll) {
         return message;
      } else {
         String mcAutoMap = "";
         if (!player.hasPermission("nocheatplus.mods.minecraftautomap.ores")) {
            mcAutoMap = mcAutoMap + "§0§0§1§f§e";
         }

         if (!player.hasPermission("nocheatplus.mods.minecraftautomap.cave")) {
            mcAutoMap = mcAutoMap + "§0§0§2§f§e";
         }

         if (!player.hasPermission("nocheatplus.mods.minecraftautomap.radar")) {
            mcAutoMap = mcAutoMap + "§0§0§3§4§5§6§7§8§f§e";
         }

         return mcAutoMap.isEmpty() ? message : message + mcAutoMap;
      }
   }
}
