package fr.neatmonster.nocheatplus.clients.motd;

import org.bukkit.entity.Player;

public class SmartMovingMOTD extends ClientMOTD {
   public String onPlayerJoin(String message, Player player, boolean allowAll) {
      if (allowAll) {
         return message;
      } else {
         String smartMoving = "";
         if (!player.hasPermission("nocheatplus.mods.smartmoving.climbing")) {
            smartMoving = smartMoving + "§0§1§0§1§2§f§f";
         }

         if (!player.hasPermission("nocheatplus.mods.smartmoving.swimming")) {
            smartMoving = smartMoving + "§0§1§3§4§f§f";
         }

         if (!player.hasPermission("nocheatplus.mods.smartmoving.crawling")) {
            smartMoving = smartMoving + "§0§1§5§f§f";
         }

         if (!player.hasPermission("nocheatplus.mods.smartmoving.sliding")) {
            smartMoving = smartMoving + "§0§1§6§f§f";
         }

         if (!player.hasPermission("nocheatplus.mods.smartmoving.jumping")) {
            smartMoving = smartMoving + "§0§1§8§9§a§b§f§f";
         }

         if (!player.hasPermission("nocheatplus.mods.smartmoving.flying")) {
            smartMoving = smartMoving + "§0§1§7§f§f";
         }

         return smartMoving.isEmpty() ? message : message + smartMoving;
      }
   }
}
