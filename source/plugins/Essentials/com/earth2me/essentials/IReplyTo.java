package com.earth2me.essentials;

import org.bukkit.command.CommandSender;

public interface IReplyTo {
   void setReplyTo(CommandSender var1);

   CommandSender getReplyTo();
}
