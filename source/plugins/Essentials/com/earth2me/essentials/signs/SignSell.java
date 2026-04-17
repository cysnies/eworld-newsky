package com.earth2me.essentials.signs;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;

public class SignSell extends EssentialsSign {
   public SignSell() {
      super("Sell");
   }

   protected boolean onSignCreate(EssentialsSign.ISign sign, User player, String username, IEssentials ess) throws SignException {
      this.validateTrade(sign, 1, 2, player, ess);
      this.validateTrade(sign, 3, ess);
      return true;
   }

   protected boolean onSignInteract(EssentialsSign.ISign sign, User player, String username, IEssentials ess) throws SignException, ChargeException {
      Trade charge = this.getTrade(sign, 1, 2, player, ess);
      Trade money = this.getTrade(sign, 3, ess);
      charge.isAffordableFor(player);
      money.pay(player);
      charge.charge(player);
      Trade.log("Sign", "Sell", "Interact", username, charge, username, money, sign.getBlock().getLocation(), ess);
      return true;
   }
}
