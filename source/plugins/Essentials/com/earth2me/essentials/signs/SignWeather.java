package com.earth2me.essentials.signs;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.I18n;
import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;

public class SignWeather extends EssentialsSign {
   public SignWeather() {
      super("Weather");
   }

   protected boolean onSignCreate(EssentialsSign.ISign sign, User player, String username, IEssentials ess) throws SignException {
      this.validateTrade(sign, 2, ess);
      String timeString = sign.getLine(1);
      if ("Sun".equalsIgnoreCase(timeString)) {
         sign.setLine(1, "§2Sun");
         return true;
      } else if ("Storm".equalsIgnoreCase(timeString)) {
         sign.setLine(1, "§2Storm");
         return true;
      } else {
         sign.setLine(1, "§c<sun|storm>");
         throw new SignException(I18n._("onlySunStorm"));
      }
   }

   protected boolean onSignInteract(EssentialsSign.ISign sign, User player, String username, IEssentials ess) throws SignException, ChargeException {
      Trade charge = this.getTrade(sign, 2, ess);
      charge.isAffordableFor(player);
      String weatherString = sign.getLine(1);
      if ("§2Sun".equalsIgnoreCase(weatherString)) {
         player.getWorld().setStorm(false);
         charge.charge(player);
         Trade.log("Sign", "WeatherSun", "Interact", username, (Trade)null, username, charge, sign.getBlock().getLocation(), ess);
         return true;
      } else if ("§2Storm".equalsIgnoreCase(weatherString)) {
         player.getWorld().setStorm(true);
         charge.charge(player);
         Trade.log("Sign", "WeatherStorm", "Interact", username, (Trade)null, username, charge, sign.getBlock().getLocation(), ess);
         return true;
      } else {
         throw new SignException(I18n._("onlySunStorm"));
      }
   }
}
