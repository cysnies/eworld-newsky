package com.earth2me.essentials;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;

public final class Console implements IReplyTo {
   private static Console instance = new Console();
   private CommandSender replyTo;
   public static final String NAME = "Console";

   private Console() {
   }

   public static CommandSender getCommandSender(Server server) throws Exception {
      return server.getConsoleSender();
   }

   public void setReplyTo(CommandSender user) {
      this.replyTo = user;
   }

   public CommandSender getReplyTo() {
      return this.replyTo;
   }

   public static Console getConsoleReplyTo() {
      return instance;
   }
}
