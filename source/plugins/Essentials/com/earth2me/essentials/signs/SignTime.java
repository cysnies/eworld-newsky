package com.earth2me.essentials.signs;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.I18n;
import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;

public class SignTime extends EssentialsSign {
   public SignTime() {
      super("Time");
   }

   protected boolean onSignCreate(EssentialsSign.ISign sign, User player, String username, IEssentials ess) throws SignException {
      this.validateTrade(sign, 2, ess);
      String timeString = sign.getLine(1);
      if ("Day".equalsIgnoreCase(timeString)) {
         sign.setLine(1, "§2Day");
         return true;
      } else if ("Night".equalsIgnoreCase(timeString)) {
         sign.setLine(1, "§2Night");
         return true;
      } else {
         throw new SignException(I18n._("onlyDayNight"));
      }
   }

   protected boolean onSignInteract(EssentialsSign.ISign sign, User player, String username, IEssentials ess) throws SignException, ChargeException {
      Trade charge = this.getTrade(sign, 2, ess);
      charge.isAffordableFor(player);
      String timeString = sign.getLine(1);
      long time = player.getWorld().getTime();
      time -= time % 24000L;
      if ("§2Day".equalsIgnoreCase(timeString)) {
         player.getWorld().setTime(time + 24000L);
         charge.charge(player);
         Trade.log("Sign", "TimeDay", "Interact", username, (Trade)null, username, charge, sign.getBlock().getLocation(), ess);
         return true;
      } else if ("§2Night".equalsIgnoreCase(timeString)) {
         player.getWorld().setTime(time + 37700L);
         charge.charge(player);
         Trade.log("Sign", "TimeNight", "Interact", username, (Trade)null, username, charge, sign.getBlock().getLocation(), ess);
         return true;
      } else {
         throw new SignException(I18n._("onlyDayNight"));
      }
   }
}
