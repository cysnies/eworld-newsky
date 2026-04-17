package fr.neatmonster.nocheatplus.clients.motd;

import org.bukkit.entity.Player;

public class ReiMOTD extends ClientMOTD {
   public String onPlayerJoin(String message, Player player, boolean allowAll) {
      String rei = "";
      if (allowAll || player.hasPermission("nocheatplus.mods.rei.cave")) {
         rei = rei + "§1";
      }

      if (!allowAll && !player.hasPermission("nocheatplus.mods.rei.radar")) {
         if (allowAll || player.hasPermission("nocheatplus.mods.rei.radar.player")) {
            rei = rei + "§2";
         }

         if (allowAll || player.hasPermission("nocheatplus.mods.rei.radar.animal")) {
            rei = rei + "§3";
         }

         if (allowAll || player.hasPermission("nocheatplus.mods.rei.radar.mob")) {
            rei = rei + "§4";
         }

         if (allowAll || player.hasPermission("nocheatplus.mods.rei.radar.slime")) {
            rei = rei + "§5";
         }

         if (allowAll || player.hasPermission("nocheatplus.mods.rei.radar.squid")) {
            rei = rei + "§6";
         }

         if (allowAll || player.hasPermission("nocheatplus.mods.rei.radar.other")) {
            rei = rei + "§7";
         }
      } else {
         rei = rei + "§2§3§4§5§6§7";
      }

      if (rei.isEmpty()) {
         return message;
      } else {
         rei = "§0§0" + rei + "§e§f";
         return message + rei;
      }
   }
}
