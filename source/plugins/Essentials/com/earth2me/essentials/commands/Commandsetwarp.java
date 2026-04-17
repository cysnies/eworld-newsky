package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import com.earth2me.essentials.Warps;
import org.bukkit.Location;
import org.bukkit.Server;

public class Commandsetwarp extends EssentialsCommand {
   public Commandsetwarp() {
      super("setwarp");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else if (!Util.isInt(args[0]) && !args[0].isEmpty()) {
         Location loc = user.getLocation();
         Warps warps = this.ess.getWarps();
         Location warpLoc = null;

         try {
            warpLoc = warps.getWarp(args[0]);
         } catch (Exception var9) {
         }

         if (warpLoc != null && !user.isAuthorized("essentials.warp.overwrite." + Util.safeString(args[0]))) {
            throw new Exception(I18n._("warpOverwrite"));
         } else {
            warps.setWarp(args[0], loc);
            user.sendMessage(I18n._("warpSet", args[0]));
         }
      } else {
         throw new NoSuchFieldException(I18n._("invalidWarpName"));
      }
   }
}
