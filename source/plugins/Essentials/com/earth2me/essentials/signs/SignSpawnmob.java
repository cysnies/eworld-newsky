package com.earth2me.essentials.signs;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.SpawnMob;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import java.util.List;

public class SignSpawnmob extends EssentialsSign {
   public SignSpawnmob() {
      super("Spawnmob");
   }

   protected boolean onSignCreate(EssentialsSign.ISign sign, User player, String username, IEssentials ess) throws SignException, ChargeException {
      this.validateInteger(sign, 1);
      this.validateTrade(sign, 3, ess);
      return true;
   }

   protected boolean onSignInteract(EssentialsSign.ISign sign, User player, String username, IEssentials ess) throws SignException, ChargeException {
      Trade charge = this.getTrade(sign, 3, ess);
      charge.isAffordableFor(player);

      try {
         List<String> mobParts = SpawnMob.mobParts(sign.getLine(2));
         List<String> mobData = SpawnMob.mobData(sign.getLine(2));
         SpawnMob.spawnmob(ess, ess.getServer(), player, (User)player, mobParts, mobData, Integer.parseInt(sign.getLine(1)));
      } catch (Exception ex) {
         throw new SignException(ex.getMessage(), ex);
      }

      charge.charge(player);
      Trade.log("Sign", "Spawnmob", "Interact", username, (Trade)null, username, charge, sign.getBlock().getLocation(), ess);
      return true;
   }
}
