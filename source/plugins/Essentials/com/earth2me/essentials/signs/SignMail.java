package com.earth2me.essentials.signs;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.User;
import java.util.List;

public class SignMail extends EssentialsSign {
   public SignMail() {
      super("Mail");
   }

   protected boolean onSignInteract(EssentialsSign.ISign sign, User player, String username, IEssentials ess) throws SignException {
      List<String> mail = player.getMails();
      if (mail.isEmpty()) {
         player.sendMessage(I18n._("noNewMail"));
         return false;
      } else {
         for(String s : mail) {
            player.sendMessage(s);
         }

         player.sendMessage(I18n._("markMailAsRead"));
         return true;
      }
   }
}
