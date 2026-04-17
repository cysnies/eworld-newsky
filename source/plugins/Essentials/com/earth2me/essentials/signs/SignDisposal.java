package com.earth2me.essentials.signs;

import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.User;

public class SignDisposal extends EssentialsSign {
   public SignDisposal() {
      super("Disposal");
   }

   protected boolean onSignInteract(EssentialsSign.ISign sign, User player, String username, IEssentials ess) {
      player.getBase().openInventory(ess.getServer().createInventory(player, 36));
      return true;
   }
}
