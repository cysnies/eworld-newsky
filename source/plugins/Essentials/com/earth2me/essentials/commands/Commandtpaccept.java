package com.earth2me.essentials.commands;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.I18n;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class Commandtpaccept extends EssentialsCommand {
   public Commandtpaccept() {
      super("tpaccept");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      User target = user.getTeleportRequest();
      if (target != null && target.isOnline()) {
         if (!user.isTpRequestHere() || (target.isAuthorized("essentials.tpahere") || target.isAuthorized("essentials.tpaall")) && (user.getWorld() == target.getWorld() || !this.ess.getSettings().isWorldTeleportPermissions() || user.isAuthorized("essentials.worlds." + user.getWorld().getName()))) {
            if (user.isTpRequestHere() || target.isAuthorized("essentials.tpa") && (user.getWorld() == target.getWorld() || !this.ess.getSettings().isWorldTeleportPermissions() || user.isAuthorized("essentials.worlds." + target.getWorld().getName()))) {
               if (args.length > 0 && !target.getName().contains(args[0])) {
                  throw new Exception(I18n._("noPendingRequest"));
               } else {
                  long timeout = this.ess.getSettings().getTpaAcceptCancellation();
                  if (timeout != 0L && (System.currentTimeMillis() - user.getTeleportRequestTime()) / 1000L > timeout) {
                     user.requestTeleport((User)null, false);
                     throw new Exception(I18n._("requestTimedOut"));
                  } else {
                     Trade charge = new Trade(this.getName(), this.ess);
                     user.sendMessage(I18n._("requestAccepted"));
                     target.sendMessage(I18n._("requestAcceptedFrom", user.getDisplayName()));

                     try {
                        if (user.isTpRequestHere()) {
                           target.getTeleport().teleportToMe(user, charge, TeleportCause.COMMAND);
                        } else {
                           target.getTeleport().teleport((Player)user, charge, TeleportCause.COMMAND);
                        }
                     } catch (ChargeException ex) {
                        user.sendMessage(I18n._("pendingTeleportCancelled"));
                        this.ess.showError(target, ex, commandLabel);
                     }

                     user.requestTeleport((User)null, false);
                     throw new NoChargeException();
                  }
               }
            } else {
               throw new Exception(I18n._("noPendingRequest"));
            }
         } else {
            throw new Exception(I18n._("noPendingRequest"));
         }
      } else {
         throw new Exception(I18n._("noPendingRequest"));
      }
   }
}
