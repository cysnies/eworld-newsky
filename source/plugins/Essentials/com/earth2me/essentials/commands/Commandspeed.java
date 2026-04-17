package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commandspeed extends EssentialsCommand {
   public Commandspeed() {
      super("speed");
   }

   protected void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 2) {
         throw new NotEnoughArgumentsException();
      } else {
         boolean isFly = this.isFlyMode(args[0]);
         float speed = this.getMoveSpeed(args[1]);
         this.speedOtherPlayers(server, sender, isFly, true, speed, args[2]);
      }
   }

   protected void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         boolean isBypass = user.isAuthorized("essentials.speed.bypass");
         boolean isFly;
         float speed;
         if (args.length == 1) {
            isFly = this.flyPermCheck(user, user.isFlying());
            speed = this.getMoveSpeed(args[0]);
         } else {
            isFly = this.flyPermCheck(user, this.isFlyMode(args[0]));
            speed = this.getMoveSpeed(args[1]);
            if (args.length > 2 && user.isAuthorized("essentials.speed.others")) {
               this.speedOtherPlayers(server, user, isFly, isBypass, speed, args[2]);
               return;
            }
         }

         if (isFly) {
            user.setFlySpeed(this.getRealMoveSpeed(speed, isFly, isBypass));
            user.sendMessage(I18n._("moveSpeed", I18n._("flying"), speed, user.getDisplayName()));
         } else {
            user.setWalkSpeed(this.getRealMoveSpeed(speed, isFly, isBypass));
            user.sendMessage(I18n._("moveSpeed", I18n._("walking"), speed, user.getDisplayName()));
         }

      }
   }

   private void speedOtherPlayers(Server server, CommandSender sender, boolean isFly, boolean isBypass, float speed, String name) throws NotEnoughArgumentsException {
      boolean skipHidden = sender instanceof Player && !this.ess.getUser(sender).isAuthorized("essentials.vanish.interact");
      boolean foundUser = false;

      for(Player matchPlayer : server.matchPlayer(name)) {
         User player = this.ess.getUser(matchPlayer);
         if (!skipHidden || !player.isHidden()) {
            foundUser = true;
            if (isFly) {
               matchPlayer.setFlySpeed(this.getRealMoveSpeed(speed, isFly, isBypass));
               sender.sendMessage(I18n._("moveSpeed", I18n._("flying"), speed, matchPlayer.getDisplayName()));
            } else {
               matchPlayer.setWalkSpeed(this.getRealMoveSpeed(speed, isFly, isBypass));
               sender.sendMessage(I18n._("moveSpeed", I18n._("walking"), speed, matchPlayer.getDisplayName()));
            }
         }
      }

      if (!foundUser) {
         throw new NotEnoughArgumentsException(I18n._("playerNotFound"));
      }
   }

   private Boolean flyPermCheck(User user, boolean input) throws Exception {
      boolean canFly = user.isAuthorized("essentials.speed.fly");
      boolean canWalk = user.isAuthorized("essentials.speed.walk");
      if ((!input || !canFly) && (input || !canWalk) && (canFly || canWalk)) {
         return canWalk ? false : true;
      } else {
         return input;
      }
   }

   private boolean isFlyMode(String modeString) throws NotEnoughArgumentsException {
      boolean isFlyMode;
      if (!modeString.contains("fly") && !modeString.equalsIgnoreCase("f")) {
         if (!modeString.contains("walk") && !modeString.contains("run") && !modeString.equalsIgnoreCase("w") && !modeString.equalsIgnoreCase("r")) {
            throw new NotEnoughArgumentsException();
         }

         isFlyMode = false;
      } else {
         isFlyMode = true;
      }

      return isFlyMode;
   }

   private float getMoveSpeed(String moveSpeed) throws NotEnoughArgumentsException {
      try {
         float userSpeed = Float.parseFloat(moveSpeed);
         if (userSpeed > 10.0F) {
            userSpeed = 10.0F;
         } else if (userSpeed < 0.0F) {
            userSpeed = 0.0F;
         }

         return userSpeed;
      } catch (NumberFormatException var4) {
         throw new NotEnoughArgumentsException();
      }
   }

   private float getRealMoveSpeed(float userSpeed, boolean isFly, boolean isBypass) {
      float defaultSpeed = isFly ? 0.1F : 0.2F;
      float maxSpeed = 1.0F;
      if (!isBypass) {
         maxSpeed = (float)(isFly ? this.ess.getSettings().getMaxFlySpeed() : this.ess.getSettings().getMaxWalkSpeed());
      }

      if (userSpeed < 1.0F) {
         return defaultSpeed * userSpeed;
      } else {
         float ratio = (userSpeed - 1.0F) / 9.0F * (maxSpeed - defaultSpeed);
         return ratio + defaultSpeed;
      }
   }
}
