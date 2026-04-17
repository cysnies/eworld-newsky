package com.earth2me.essentials.signs;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.I18n;
import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;

public class SignHeal extends EssentialsSign {
   public SignHeal() {
      super("Heal");
   }

   protected boolean onSignCreate(EssentialsSign.ISign sign, User player, String username, IEssentials ess) throws SignException {
      this.validateTrade(sign, 1, ess);
      return true;
   }

   protected boolean onSignInteract(EssentialsSign.ISign sign, User player, String username, IEssentials ess) throws SignException, ChargeException {
      if (player.getHealth() == 0) {
         throw new SignException(I18n._("healDead"));
      } else {
         Trade charge = this.getTrade(sign, 1, ess);
         charge.isAffordableFor(player);
         player.setHealth(20);
         player.setFoodLevel(20);
         player.setFireTicks(0);
         player.sendMessage(I18n._("youAreHealed"));
         charge.charge(player);
         Trade.log("Sign", "Heal", "Interact", username, (Trade)null, username, charge, sign.getBlock().getLocation(), ess);
         return true;
      }
   }
}
