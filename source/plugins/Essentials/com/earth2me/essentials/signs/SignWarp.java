package com.earth2me.essentials.signs;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.I18n;
import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class SignWarp extends EssentialsSign {
   public SignWarp() {
      super("Warp");
   }

   protected boolean onSignCreate(EssentialsSign.ISign sign, User player, String username, IEssentials ess) throws SignException {
      this.validateTrade(sign, 3, ess);
      String warpName = sign.getLine(1);
      if (warpName.isEmpty()) {
         sign.setLine(1, "§c<Warp name>");
         throw new SignException(I18n._("invalidSignLine", 1));
      } else {
         try {
            ess.getWarps().getWarp(warpName);
         } catch (Exception ex) {
            throw new SignException(ex.getMessage(), ex);
         }

         String group = sign.getLine(2);
         if ("Everyone".equalsIgnoreCase(group) || "Everybody".equalsIgnoreCase(group)) {
            sign.setLine(2, "§2Everyone");
         }

         return true;
      }
   }

   protected boolean onSignInteract(EssentialsSign.ISign sign, User player, String username, IEssentials ess) throws SignException, ChargeException {
      String warpName = sign.getLine(1);
      String group = sign.getLine(2);
      if (!group.isEmpty() && ("§2Everyone".equals(group) || player.inGroup(group)) || group.isEmpty() && (!ess.getSettings().getPerWarpPermission() || player.isAuthorized("essentials.warps." + warpName))) {
         Trade charge = this.getTrade(sign, 3, ess);

         try {
            player.getTeleport().warp(warpName, charge, TeleportCause.PLUGIN);
            Trade.log("Sign", "Warp", "Interact", username, (Trade)null, username, charge, sign.getBlock().getLocation(), ess);
            return true;
         } catch (Exception ex) {
            throw new SignException(ex.getMessage(), ex);
         }
      } else {
         return false;
      }
   }
}
