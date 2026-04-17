package com.earth2me.essentials.signs;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.textreader.IText;
import com.earth2me.essentials.textreader.KeywordReplacer;
import com.earth2me.essentials.textreader.TextInput;
import com.earth2me.essentials.textreader.TextPager;
import java.io.IOException;

public class SignInfo extends EssentialsSign {
   public SignInfo() {
      super("Info");
   }

   protected boolean onSignCreate(EssentialsSign.ISign sign, User player, String username, IEssentials ess) throws SignException {
      this.validateTrade(sign, 3, ess);
      return true;
   }

   protected boolean onSignInteract(EssentialsSign.ISign sign, User player, String username, IEssentials ess) throws SignException, ChargeException {
      Trade charge = this.getTrade(sign, 3, ess);
      charge.isAffordableFor(player);
      String chapter = sign.getLine(1);
      String page = sign.getLine(2);

      try {
         IText input = new TextInput(player, "info", true, ess);
         IText output = new KeywordReplacer(input, player, ess);
         TextPager pager = new TextPager(output);
         pager.showPage(chapter, page, (String)null, player);
      } catch (IOException ex) {
         throw new SignException(ex.getMessage(), ex);
      }

      charge.charge(player);
      Trade.log("Sign", "Info", "Interact", username, (Trade)null, username, charge, sign.getBlock().getLocation(), ess);
      return true;
   }
}
