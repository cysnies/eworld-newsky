package com.goncalomb.bukkit.betterplugin;

import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;

public enum BetterCommandType {
   DEFAULT,
   PLAYER_ONLY,
   NO_PLAYER,
   CONSOLE_ONLY,
   BLOCK_ONLY;

   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$com$goncalomb$bukkit$betterplugin$BetterCommandType;

   public boolean isValidSender(CommandSender sender) {
      switch (this) {
         case DEFAULT:
            return true;
         case PLAYER_ONLY:
            return sender instanceof Player;
         case NO_PLAYER:
            if (!(sender instanceof ConsoleCommandSender) && !(sender instanceof RemoteConsoleCommandSender) && !(sender instanceof BlockCommandSender)) {
               return false;
            }

            return true;
         case CONSOLE_ONLY:
            if (!(sender instanceof ConsoleCommandSender) && !(sender instanceof RemoteConsoleCommandSender)) {
               return false;
            }

            return true;
         case BLOCK_ONLY:
            return sender instanceof BlockCommandSender;
         default:
            return false;
      }
   }

   public String getInvalidSenderMessage() {
      return Lang._("common.commands.invalid-sender." + this.toString());
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$com$goncalomb$bukkit$betterplugin$BetterCommandType() {
      int[] var10000 = $SWITCH_TABLE$com$goncalomb$bukkit$betterplugin$BetterCommandType;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[values().length];

         try {
            var0[BLOCK_ONLY.ordinal()] = 5;
         } catch (NoSuchFieldError var5) {
         }

         try {
            var0[CONSOLE_ONLY.ordinal()] = 4;
         } catch (NoSuchFieldError var4) {
         }

         try {
            var0[DEFAULT.ordinal()] = 1;
         } catch (NoSuchFieldError var3) {
         }

         try {
            var0[NO_PLAYER.ordinal()] = 3;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[PLAYER_ONLY.ordinal()] = 2;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$com$goncalomb$bukkit$betterplugin$BetterCommandType = var0;
         return var0;
      }
   }
}
