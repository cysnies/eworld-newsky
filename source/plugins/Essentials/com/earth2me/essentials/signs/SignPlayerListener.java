package com.earth2me.essentials.signs;

import com.earth2me.essentials.IEssentials;
import java.util.HashSet;
import java.util.logging.Level;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignPlayerListener implements Listener {
   private final transient IEssentials ess;

   public SignPlayerListener(IEssentials ess) {
      this.ess = ess;
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onPlayerInteract(PlayerInteractEvent event) {
      if (this.ess.getSettings().areSignsDisabled()) {
         event.getHandlers().unregister(this);
      } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
         Block block;
         if (event.isCancelled() && event.getAction() == Action.RIGHT_CLICK_AIR) {
            Block targetBlock = null;

            try {
               targetBlock = event.getPlayer().getTargetBlock((HashSet)null, 5);
            } catch (IllegalStateException ex) {
               if (this.ess.getSettings().isDebug()) {
                  this.ess.getLogger().log(Level.WARNING, ex.getMessage(), ex);
               }
            }

            block = targetBlock;
         } else {
            block = event.getClickedBlock();
         }

         if (block != null) {
            int mat = block.getTypeId();
            if (mat != Material.SIGN_POST.getId() && mat != Material.WALL_SIGN.getId()) {
               for(EssentialsSign sign : this.ess.getSettings().enabledSigns()) {
                  if (sign.areHeavyEventRequired() && sign.getBlocks().contains(block.getType()) && !sign.onBlockInteract(block, event.getPlayer(), this.ess)) {
                     event.setCancelled(true);
                     return;
                  }
               }
            } else {
               String csign = ((Sign)block.getState()).getLine(0);

               for(EssentialsSign sign : this.ess.getSettings().enabledSigns()) {
                  if (csign.equalsIgnoreCase(sign.getSuccessName())) {
                     sign.onSignInteract(block, event.getPlayer(), this.ess);
                     event.setCancelled(true);
                     return;
                  }
               }
            }

         }
      }
   }
}
