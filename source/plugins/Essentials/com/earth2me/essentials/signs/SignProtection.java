package com.earth2me.essentials.signs;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.I18n;
import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

/** @deprecated */
@Deprecated
public class SignProtection extends EssentialsSign {
   private final transient Set protectedBlocks = EnumSet.noneOf(Material.class);

   public SignProtection() {
      super("Protection");
      this.protectedBlocks.add(Material.CHEST);
      this.protectedBlocks.add(Material.BURNING_FURNACE);
      this.protectedBlocks.add(Material.FURNACE);
      this.protectedBlocks.add(Material.DISPENSER);
   }

   protected boolean onSignCreate(EssentialsSign.ISign sign, User player, String username, IEssentials ess) throws SignException, ChargeException {
      sign.setLine(3, "§4" + username);
      if (this.hasAdjacentBlock(sign.getBlock())) {
         SignProtectionState state = this.isBlockProtected(sign.getBlock(), player, username, true);
         if (state == SignProtection.SignProtectionState.NOSIGN || state == SignProtection.SignProtectionState.OWNER || player.isAuthorized("essentials.signs.protection.override")) {
            sign.setLine(3, "§1" + username);
            return true;
         }
      }

      player.sendMessage(I18n._("signProtectInvalidLocation"));
      return false;
   }

   protected boolean onSignBreak(EssentialsSign.ISign sign, User player, String username, IEssentials ess) throws SignException {
      SignProtectionState state = this.checkProtectionSign(sign, player, username);
      return state == SignProtection.SignProtectionState.OWNER;
   }

   public boolean hasAdjacentBlock(Block block, Block... ignoredBlocks) {
      Block[] faces = this.getAdjacentBlocks(block);

      for(Block b : faces) {
         for(Block ignoredBlock : ignoredBlocks) {
            if (b.getLocation().equals(ignoredBlock.getLocation())) {
            }
         }

         if (this.protectedBlocks.contains(b.getType())) {
            return true;
         }
      }

      return false;
   }

   private void checkIfSignsAreBroken(Block block, User player, String username, IEssentials ess) {
      Map<Location, SignProtectionState> signs = this.getConnectedSigns(block, player, username, false);

      for(Map.Entry entry : signs.entrySet()) {
         if (entry.getValue() != SignProtection.SignProtectionState.NOSIGN) {
            Block sign = ((Location)entry.getKey()).getBlock();
            if (!this.hasAdjacentBlock(sign, block)) {
               block.setType(Material.AIR);
               Trade trade = new Trade(new ItemStack(Material.SIGN, 1), ess);
               trade.pay(player);
            }
         }
      }

   }

   private Map getConnectedSigns(Block block, User user, String username, boolean secure) {
      Map<Location, SignProtectionState> signs = new HashMap();
      this.getConnectedSigns(block, signs, user, username, secure ? 4 : 2);
      return signs;
   }

   private void getConnectedSigns(Block block, Map signs, User user, String username, int depth) {
      Block[] faces = this.getAdjacentBlocks(block);

      for(Block b : faces) {
         Location loc = b.getLocation();
         if (!signs.containsKey(loc)) {
            SignProtectionState check = this.checkProtectionSign(b, user, username);
            signs.put(loc, check);
            if (this.protectedBlocks.contains(b.getType()) && depth > 0) {
               this.getConnectedSigns(b, signs, user, username, depth - 1);
            }
         }
      }

   }

   private SignProtectionState checkProtectionSign(Block block, User user, String username) {
      if (block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN) {
         EssentialsSign.BlockSign sign = new EssentialsSign.BlockSign(block);
         if (sign.getLine(0).equals(this.getSuccessName())) {
            return this.checkProtectionSign((EssentialsSign.ISign)sign, user, username);
         }
      }

      return SignProtection.SignProtectionState.NOSIGN;
   }

   private SignProtectionState checkProtectionSign(EssentialsSign.ISign sign, User user, String username) {
      if (user != null && username != null) {
         if (user.isAuthorized("essentials.signs.protection.override")) {
            return SignProtection.SignProtectionState.OWNER;
         } else if (Util.stripFormat(sign.getLine(3)).equalsIgnoreCase(username)) {
            return SignProtection.SignProtectionState.OWNER;
         } else {
            for(int i = 1; i <= 2; ++i) {
               String line = sign.getLine(i);
               if (line.startsWith("(") && line.endsWith(")") && user.inGroup(line.substring(1, line.length() - 1))) {
                  return SignProtection.SignProtectionState.ALLOWED;
               }

               if (line.equalsIgnoreCase(username)) {
                  return SignProtection.SignProtectionState.ALLOWED;
               }
            }

            return SignProtection.SignProtectionState.NOT_ALLOWED;
         }
      } else {
         return SignProtection.SignProtectionState.NOT_ALLOWED;
      }
   }

   private Block[] getAdjacentBlocks(Block block) {
      return new Block[]{block.getRelative(BlockFace.NORTH), block.getRelative(BlockFace.SOUTH), block.getRelative(BlockFace.EAST), block.getRelative(BlockFace.WEST), block.getRelative(BlockFace.DOWN), block.getRelative(BlockFace.UP)};
   }

   public SignProtectionState isBlockProtected(Block block, User user, String username, boolean secure) {
      Map<Location, SignProtectionState> signs = this.getConnectedSigns(block, user, username, secure);
      SignProtectionState retstate = SignProtection.SignProtectionState.NOSIGN;

      for(SignProtectionState state : signs.values()) {
         if (state == SignProtection.SignProtectionState.ALLOWED) {
            retstate = state;
         } else if (state == SignProtection.SignProtectionState.NOT_ALLOWED && retstate != SignProtection.SignProtectionState.ALLOWED) {
            retstate = state;
         }
      }

      if (!secure || retstate == SignProtection.SignProtectionState.NOSIGN) {
         for(SignProtectionState state : signs.values()) {
            if (state == SignProtection.SignProtectionState.OWNER) {
               return state;
            }
         }
      }

      return retstate;
   }

   public boolean isBlockProtected(Block block) {
      Block[] faces = this.getAdjacentBlocks(block);

      for(Block b : faces) {
         if (b.getType() == Material.SIGN_POST || b.getType() == Material.WALL_SIGN) {
            Sign sign = (Sign)b.getState();
            if (sign.getLine(0).equalsIgnoreCase("§1[Protection]")) {
               return true;
            }
         }

         if (this.protectedBlocks.contains(b.getType())) {
            Block[] faceChest = this.getAdjacentBlocks(b);

            for(Block a : faceChest) {
               if (a.getType() == Material.SIGN_POST || a.getType() == Material.WALL_SIGN) {
                  Sign sign = (Sign)a.getState();
                  if (sign.getLine(0).equalsIgnoreCase("§1[Protection]")) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   public Set getBlocks() {
      return this.protectedBlocks;
   }

   public boolean areHeavyEventRequired() {
      return true;
   }

   protected boolean onBlockPlace(Block block, User player, String username, IEssentials ess) throws SignException {
      for(Block adjBlock : this.getAdjacentBlocks(block)) {
         SignProtectionState state = this.isBlockProtected(adjBlock, player, username, true);
         if ((state == SignProtection.SignProtectionState.ALLOWED || state == SignProtection.SignProtectionState.NOT_ALLOWED) && !player.isAuthorized("essentials.signs.protection.override")) {
            player.sendMessage(I18n._("noPlacePermission", block.getType().toString().toLowerCase(Locale.ENGLISH)));
            return false;
         }
      }

      return true;
   }

   protected boolean onBlockInteract(Block block, User player, String username, IEssentials ess) throws SignException {
      SignProtectionState state = this.isBlockProtected(block, player, username, false);
      if (state != SignProtection.SignProtectionState.OWNER && state != SignProtection.SignProtectionState.NOSIGN && state != SignProtection.SignProtectionState.ALLOWED) {
         if (state == SignProtection.SignProtectionState.NOT_ALLOWED && player.isAuthorized("essentials.signs.protection.override")) {
            return true;
         } else {
            player.sendMessage(I18n._("noAccessPermission", block.getType().toString().toLowerCase(Locale.ENGLISH)));
            return false;
         }
      } else {
         return true;
      }
   }

   protected boolean onBlockBreak(Block block, User player, String username, IEssentials ess) throws SignException {
      SignProtectionState state = this.isBlockProtected(block, player, username, false);
      if (state != SignProtection.SignProtectionState.OWNER && state != SignProtection.SignProtectionState.NOSIGN) {
         if ((state == SignProtection.SignProtectionState.ALLOWED || state == SignProtection.SignProtectionState.NOT_ALLOWED) && player.isAuthorized("essentials.signs.protection.override")) {
            this.checkIfSignsAreBroken(block, player, username, ess);
            return true;
         } else {
            player.sendMessage(I18n._("noDestroyPermission", block.getType().toString().toLowerCase(Locale.ENGLISH)));
            return false;
         }
      } else {
         this.checkIfSignsAreBroken(block, player, username, ess);
         return true;
      }
   }

   public boolean onBlockBreak(Block block, IEssentials ess) {
      SignProtectionState state = this.isBlockProtected(block, (User)null, (String)null, false);
      return state == SignProtection.SignProtectionState.NOSIGN;
   }

   public boolean onBlockExplode(Block block, IEssentials ess) {
      SignProtectionState state = this.isBlockProtected(block, (User)null, (String)null, false);
      return state == SignProtection.SignProtectionState.NOSIGN;
   }

   public boolean onBlockBurn(Block block, IEssentials ess) {
      SignProtectionState state = this.isBlockProtected(block, (User)null, (String)null, false);
      return state == SignProtection.SignProtectionState.NOSIGN;
   }

   public boolean onBlockIgnite(Block block, IEssentials ess) {
      SignProtectionState state = this.isBlockProtected(block, (User)null, (String)null, false);
      return state == SignProtection.SignProtectionState.NOSIGN;
   }

   public boolean onBlockPush(Block block, IEssentials ess) {
      SignProtectionState state = this.isBlockProtected(block, (User)null, (String)null, false);
      return state == SignProtection.SignProtectionState.NOSIGN;
   }

   public static enum SignProtectionState {
      NOT_ALLOWED,
      ALLOWED,
      NOSIGN,
      OWNER;
   }
}
