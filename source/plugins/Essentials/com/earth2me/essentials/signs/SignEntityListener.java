package com.earth2me.essentials.signs;

import com.earth2me.essentials.IEssentials;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class SignEntityListener implements Listener {
   private final transient IEssentials ess;

   public SignEntityListener(IEssentials ess) {
      this.ess = ess;
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onEntityExplode(EntityExplodeEvent event) {
      if (this.ess.getSettings().areSignsDisabled()) {
         event.getHandlers().unregister(this);
      } else {
         for(Block block : event.blockList()) {
            if ((block.getTypeId() == Material.WALL_SIGN.getId() || block.getTypeId() == Material.SIGN_POST.getId()) && EssentialsSign.isValidSign(new EssentialsSign.BlockSign(block)) || EssentialsSign.checkIfBlockBreaksSigns(block)) {
               event.setCancelled(true);
               return;
            }

            for(EssentialsSign sign : this.ess.getSettings().enabledSigns()) {
               if (sign.areHeavyEventRequired() && sign.getBlocks().contains(block.getType())) {
                  event.setCancelled(!sign.onBlockExplode(block, this.ess));
                  return;
               }
            }
         }

      }
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onEntityChangeBlock(EntityChangeBlockEvent event) {
      if (this.ess.getSettings().areSignsDisabled()) {
         event.getHandlers().unregister(this);
      } else {
         Block block = event.getBlock();
         if ((block.getTypeId() != Material.WALL_SIGN.getId() && block.getTypeId() != Material.SIGN_POST.getId() || !EssentialsSign.isValidSign(new EssentialsSign.BlockSign(block))) && !EssentialsSign.checkIfBlockBreaksSigns(block)) {
            for(EssentialsSign sign : this.ess.getSettings().enabledSigns()) {
               if (sign.areHeavyEventRequired() && sign.getBlocks().contains(block.getType()) && !sign.onBlockBreak(block, this.ess)) {
                  event.setCancelled(true);
                  return;
               }
            }

         } else {
            event.setCancelled(true);
         }
      }
   }
}
