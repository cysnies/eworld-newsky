package com.sleelin.pvptoggle.listeners;

import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sleelin.pvptoggle.PvPLocalisation;
import com.sleelin.pvptoggle.PvPToggle;
import com.sleelin.pvptoggle.handlers.RegionHandler;
import java.util.HashMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class RegionListener implements Listener {
   public static PvPToggle plugin;
   private static HashMap playerstatus = new HashMap();

   public RegionListener(PvPToggle instance) {
      plugin = instance;
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onPlayerMove(PlayerMoveEvent event) {
      if ((Boolean)plugin.getGlobalSetting("worldguard")) {
         Player player = event.getPlayer();
         WorldGuardPlugin worldGuard = (WorldGuardPlugin)plugin.getServer().getPluginManager().getPlugin("WorldGuard");
         Boolean inRegion = false;

         label44:
         for(ProtectedRegion region : worldGuard.getRegionManager(player.getWorld()).getApplicableRegions(BukkitUtil.toVector(player.getLocation().getBlock()))) {
            if (RegionHandler.isApplicableRegion(player.getWorld().getName(), region.getId())) {
               if (playerstatus.containsKey(player.getName()) && ((String[])playerstatus.get(player.getName()))[1].equalsIgnoreCase(region.getId())) {
                  inRegion = true;
                  break;
               }

               for(Flag flag : region.getFlags().keySet()) {
                  if (flag.getName().equals("pvp")) {
                     if (region.getFlag(flag).equals(State.ALLOW)) {
                        plugin.setPlayerStatus(player, player.getWorld().getName(), true);
                        playerstatus.put(player.getName(), new String[]{PvPLocalisation.Strings.PVP_ENABLED.toString(), region.getId()});
                        PvPLocalisation.display(player, region.getId(), (String)null, PvPLocalisation.Strings.PVP_ENABLED.toString(), PvPLocalisation.Strings.WORLDGUARD_REGION_ENTERED);
                     } else if (region.getFlag(flag).equals(State.DENY)) {
                        plugin.setPlayerStatus(player, player.getWorld().getName(), false);
                        playerstatus.put(player.getName(), new String[]{PvPLocalisation.Strings.PVP_DENIED.toString(), region.getId()});
                        PvPLocalisation.display(player, region.getId(), (String)null, PvPLocalisation.Strings.PVP_DENIED.toString(), PvPLocalisation.Strings.WORLDGUARD_REGION_ENTERED);
                     }

                     inRegion = true;
                     break label44;
                  }
               }
            }
         }

         if (!inRegion && playerstatus.containsKey(player.getName())) {
            PvPLocalisation.display(player, ((String[])playerstatus.get(player.getName()))[1], (String)null, ((String[])playerstatus.get(player.getName()))[0], PvPLocalisation.Strings.WORLDGUARD_REGION_EXIT);
            playerstatus.remove(player.getName());
         }
      }

   }

   public boolean WorldGuardRegionCheck(Player player, String target) {
      if ((Boolean)plugin.getGlobalSetting("worldguard") && playerstatus.containsKey(player.getName())) {
         if (((String[])playerstatus.get(player.getName()))[0].equals(PvPLocalisation.Strings.PVP_ENABLED.toString())) {
            PvPLocalisation.display(player, target, (String)null, PvPLocalisation.Strings.PVP_FORCED.toString(), PvPLocalisation.Strings.WORLDGUARD_TOGGLE_DENIED);
         } else if (((String[])playerstatus.get(player.getName()))[0].equals(PvPLocalisation.Strings.PVP_DENIED.toString())) {
            PvPLocalisation.display(player, target, (String)null, PvPLocalisation.Strings.PVP_DENIED.toString(), PvPLocalisation.Strings.WORLDGUARD_TOGGLE_DENIED);
         }

         return false;
      } else {
         return true;
      }
   }
}
