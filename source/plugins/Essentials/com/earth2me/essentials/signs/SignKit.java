package com.earth2me.essentials.signs;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.I18n;
import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.Kit;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.commands.NoChargeException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SignKit extends EssentialsSign {
   public SignKit() {
      super("Kit");
   }

   protected boolean onSignCreate(EssentialsSign.ISign sign, User player, String username, IEssentials ess) throws SignException {
      this.validateTrade(sign, 3, ess);
      String kitName = sign.getLine(1).toLowerCase(Locale.ENGLISH).trim();
      if (kitName.isEmpty()) {
         sign.setLine(1, "§dKit name!");
         return false;
      } else {
         try {
            ess.getSettings().getKit(kitName);
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
      String kitName = sign.getLine(1).toLowerCase(Locale.ENGLISH).trim();
      String group = sign.getLine(2);
      if (!group.isEmpty() && ("§2Everyone".equals(group) || player.inGroup(group)) || group.isEmpty() && player.isAuthorized("essentials.kits." + kitName)) {
         Trade charge = this.getTrade(sign, 3, ess);
         charge.isAffordableFor(player);

         try {
            Map<String, Object> kit = ess.getSettings().getKit(kitName);
            Kit.checkTime(player, kitName, kit);
            List<String> items = Kit.getItems(player, kit);
            Kit.expandItems(ess, player, items);
            charge.charge(player);
            Trade.log("Sign", "Kit", "Interact", username, (Trade)null, username, charge, sign.getBlock().getLocation(), ess);
            return true;
         } catch (NoChargeException var10) {
            return false;
         } catch (Exception ex) {
            throw new SignException(ex.getMessage(), ex);
         }
      } else {
         throw new SignException(I18n._("noKitPermission", "essentials.kits." + kitName));
      }
   }
}
