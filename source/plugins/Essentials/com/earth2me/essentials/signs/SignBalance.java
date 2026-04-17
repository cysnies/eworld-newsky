package com.earth2me.essentials.signs;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;

public class SignBalance extends EssentialsSign {
   public SignBalance() {
      super("Balance");
   }

   protected boolean onSignInteract(EssentialsSign.ISign sign, User player, String username, IEssentials ess) throws SignException {
      player.sendMessage(I18n._("balance", Util.displayCurrency(player.getMoney(), ess)));
      return true;
   }
}
