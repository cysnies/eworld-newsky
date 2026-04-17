package com.earth2me.essentials.signs;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;

public class SignBuy extends EssentialsSign {
   public SignBuy() {
      super("Buy");
   }

   protected boolean onSignCreate(EssentialsSign.ISign sign, User player, String username, IEssentials ess) throws SignException {
      this.validateTrade(sign, 1, 2, player, ess);
      this.validateTrade(sign, 3, ess);
      return true;
   }

   protected boolean onSignInteract(EssentialsSign.ISign sign, User player, String username, IEssentials ess) throws SignException, ChargeException {
      Trade items = this.getTrade(sign, 1, 2, player, ess);
      Trade charge = this.getTrade(sign, 3, ess);
      charge.isAffordableFor(player);
      if (!items.pay(player, false)) {
         throw new ChargeException("Inventory full");
      } else {
         charge.charge(player);
         Trade.log("Sign", "Buy", "Interact", username, charge, username, items, sign.getBlock().getLocation(), ess);
         return true;
      }
   }
}
