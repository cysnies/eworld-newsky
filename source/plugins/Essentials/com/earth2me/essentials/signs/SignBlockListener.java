package com.earth2me.essentials.signs;

import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;

public class SignBlockListener implements Listener {
   private final transient IEssentials ess;
   private static final Logger LOGGER = Logger.getLogger("Minecraft");
   private static final int WALL_SIGN;
   private static final int SIGN_POST;

   public SignBlockListener(IEssentials ess) {
      this.ess = ess;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBlockBreak(BlockBreakEvent event) {
      if (this.ess.getSettings().areSignsDisabled()) {
         event.getHandlers().unregister(this);
      } else {
         if (this.protectSignsAndBlocks(event.getBlock(), event.getPlayer())) {
            event.setCancelled(true);
         }

      }
   }

   public boolean protectSignsAndBlocks(Block block, Player player) {
      if (EssentialsSign.checkIfBlockBreaksSigns(block)) {
         LOGGER.log(Level.INFO, "Prevented that a block was broken next to a sign.");
         return true;
      } else {
         int mat = block.getTypeId();
         if (mat == SIGN_POST || mat == WALL_SIGN) {
            Sign csign = (Sign)block.getState();

            for(EssentialsSign sign : this.ess.getSettings().enabledSigns()) {
               if (csign.getLine(0).equalsIgnoreCase(sign.getSuccessName()) && !sign.onSignBreak(block, player, this.ess)) {
                  return true;
               }
            }
         }

         for(EssentialsSign sign : this.ess.getSettings().enabledSigns()) {
            if (sign.areHeavyEventRequired() && sign.getBlocks().contains(block.getType()) && !sign.onBlockBreak(block, player, this.ess)) {
               LOGGER.log(Level.INFO, "A block was protected by a sign.");
               return true;
            }
         }

         return false;
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onSignChange2(SignChangeEvent event) {
      if (this.ess.getSettings().areSignsDisabled()) {
         event.getHandlers().unregister(this);
      } else {
         User user = this.ess.getUser(event.getPlayer());

         for(int i = 0; i < 4; ++i) {
            event.setLine(i, Util.formatString(user, "essentials.signs", event.getLine(i)));
         }

         String topLine = event.getLine(0);

         for(Signs signs : Signs.values()) {
            EssentialsSign sign = signs.getSign();
            if (topLine.equalsIgnoreCase(sign.getSuccessName())) {
               event.setLine(0, Util.stripFormat(topLine));
            }
         }

      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onSignChange(SignChangeEvent event) {
      if (this.ess.getSettings().areSignsDisabled()) {
         event.getHandlers().unregister(this);
      } else {
         for(EssentialsSign sign : this.ess.getSettings().enabledSigns()) {
            if (event.getLine(0).equalsIgnoreCase(sign.getSuccessName())) {
               event.setCancelled(true);
               return;
            }

            if (event.getLine(0).equalsIgnoreCase(sign.getTemplateName()) && !sign.onSignCreate(event, this.ess)) {
               event.setCancelled(true);
               return;
            }
         }

      }
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onBlockPlace(BlockPlaceEvent event) {
      if (this.ess.getSettings().areSignsDisabled()) {
         event.getHandlers().unregister(this);
      } else {
         Block against = event.getBlockAgainst();
         if ((against.getTypeId() == WALL_SIGN || against.getTypeId() == SIGN_POST) && EssentialsSign.isValidSign(new EssentialsSign.BlockSign(against))) {
            event.setCancelled(true);
         } else {
            Block block = event.getBlock();
            if (block.getTypeId() != WALL_SIGN && block.getTypeId() != SIGN_POST) {
               for(EssentialsSign sign : this.ess.getSettings().enabledSigns()) {
                  if (sign.areHeavyEventRequired() && sign.getBlocks().contains(block.getType()) && !sign.onBlockPlace(block, event.getPlayer(), this.ess)) {
                     event.setCancelled(true);
                     return;
                  }
               }

            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onBlockBurn(BlockBurnEvent event) {
      if (this.ess.getSettings().areSignsDisabled()) {
         event.getHandlers().unregister(this);
      } else {
         Block block = event.getBlock();
         if ((block.getTypeId() != WALL_SIGN && block.getTypeId() != SIGN_POST || !EssentialsSign.isValidSign(new EssentialsSign.BlockSign(block))) && !EssentialsSign.checkIfBlockBreaksSigns(block)) {
            for(EssentialsSign sign : this.ess.getSettings().enabledSigns()) {
               if (sign.areHeavyEventRequired() && sign.getBlocks().contains(block.getType()) && !sign.onBlockBurn(block, this.ess)) {
                  event.setCancelled(true);
                  return;
               }
            }

         } else {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onBlockIgnite(BlockIgniteEvent event) {
      if (this.ess.getSettings().areSignsDisabled()) {
         event.getHandlers().unregister(this);
      } else {
         Block block = event.getBlock();
         if ((block.getTypeId() != WALL_SIGN && block.getTypeId() != SIGN_POST || !EssentialsSign.isValidSign(new EssentialsSign.BlockSign(block))) && !EssentialsSign.checkIfBlockBreaksSigns(block)) {
            for(EssentialsSign sign : this.ess.getSettings().enabledSigns()) {
               if (sign.areHeavyEventRequired() && sign.getBlocks().contains(block.getType()) && !sign.onBlockIgnite(block, this.ess)) {
                  event.setCancelled(true);
                  return;
               }
            }

         } else {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onBlockPistonExtend(BlockPistonExtendEvent event) {
      if (this.ess.getSettings().areSignsDisabled()) {
         event.getHandlers().unregister(this);
      } else {
         for(Block block : event.getBlocks()) {
            if ((block.getTypeId() == WALL_SIGN || block.getTypeId() == SIGN_POST) && EssentialsSign.isValidSign(new EssentialsSign.BlockSign(block)) || EssentialsSign.checkIfBlockBreaksSigns(block)) {
               event.setCancelled(true);
               return;
            }

            for(EssentialsSign sign : this.ess.getSettings().enabledSigns()) {
               if (sign.areHeavyEventRequired() && sign.getBlocks().contains(block.getType()) && !sign.onBlockPush(block, this.ess)) {
                  event.setCancelled(true);
                  return;
               }
            }
         }

      }
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onBlockPistonRetract(BlockPistonRetractEvent event) {
      if (this.ess.getSettings().areSignsDisabled()) {
         event.getHandlers().unregister(this);
      } else {
         if (event.isSticky()) {
            Block block = event.getBlock();
            if ((block.getTypeId() == WALL_SIGN || block.getTypeId() == SIGN_POST) && EssentialsSign.isValidSign(new EssentialsSign.BlockSign(block)) || EssentialsSign.checkIfBlockBreaksSigns(block)) {
               event.setCancelled(true);
               return;
            }

            for(EssentialsSign sign : this.ess.getSettings().enabledSigns()) {
               if (sign.areHeavyEventRequired() && sign.getBlocks().contains(block.getType()) && !sign.onBlockPush(block, this.ess)) {
                  event.setCancelled(true);
                  return;
               }
            }
         }

      }
   }

   static {
      WALL_SIGN = Material.WALL_SIGN.getId();
      SIGN_POST = Material.SIGN_POST.getId();
   }
}
