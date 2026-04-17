package com.earth2me.essentials.signs;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.I18n;
import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import java.util.Locale;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class SignGameMode extends EssentialsSign {
   public SignGameMode() {
      super("GameMode");
   }

   protected boolean onSignCreate(EssentialsSign.ISign sign, User player, String username, IEssentials ess) throws SignException {
      String gamemode = sign.getLine(1);
      if (gamemode.isEmpty()) {
         sign.setLine(1, "Survival");
      }

      this.validateTrade(sign, 2, ess);
      return true;
   }

   protected boolean onSignInteract(EssentialsSign.ISign sign, User player, String username, IEssentials ess) throws SignException, ChargeException {
      Trade charge = this.getTrade(sign, 2, ess);
      String mode = sign.getLine(1).trim();
      if (mode.isEmpty()) {
         throw new SignException(I18n._("invalidSignLine", 2));
      } else {
         charge.isAffordableFor(player);
         this.performSetMode(mode.toLowerCase(Locale.ENGLISH), player);
         player.sendMessage(I18n._("gameMode", I18n._(player.getGameMode().toString().toLowerCase(Locale.ENGLISH)), player.getDisplayName()));
         Trade.log("Sign", "gameMode", "Interact", username, (Trade)null, username, charge, sign.getBlock().getLocation(), ess);
         charge.charge(player);
         return true;
      }
   }

   private void performSetMode(String mode, Player player) throws SignException {
      if (!mode.contains("survi") && !mode.equalsIgnoreCase("0")) {
         if (!mode.contains("creat") && !mode.equalsIgnoreCase("1")) {
            if (!mode.contains("advent") && !mode.equalsIgnoreCase("2")) {
               throw new SignException(I18n._("invalidSignLine", 2));
            }

            player.setGameMode(GameMode.ADVENTURE);
         } else {
            player.setGameMode(GameMode.CREATIVE);
         }
      } else {
         player.setGameMode(GameMode.SURVIVAL);
      }

   }
}
