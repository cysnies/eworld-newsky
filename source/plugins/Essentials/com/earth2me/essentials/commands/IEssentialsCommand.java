package com.earth2me.essentials.commands;

import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.IEssentialsModule;
import com.earth2me.essentials.User;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public interface IEssentialsCommand {
   String getName();

   void run(Server var1, User var2, String var3, Command var4, String[] var5) throws Exception;

   void run(Server var1, CommandSender var2, String var3, Command var4, String[] var5) throws Exception;

   void setEssentials(IEssentials var1);

   void setEssentialsModule(IEssentialsModule var1);
}
