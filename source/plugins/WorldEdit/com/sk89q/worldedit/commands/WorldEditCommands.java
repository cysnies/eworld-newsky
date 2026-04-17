package com.sk89q.worldedit.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.Console;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class WorldEditCommands {
   private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
   private final WorldEdit we;

   public WorldEditCommands(WorldEdit we) {
      this.we = we;
   }

   @Command(
      aliases = {"version", "ver"},
      usage = "",
      desc = "Get WorldEdit version",
      min = 0,
      max = 0
   )
   @Console
   public void version(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      player.print("WorldEdit version " + WorldEdit.getVersion());
      player.print("http://www.sk89q.com/projects/worldedit/");
   }

   @Command(
      aliases = {"reload"},
      usage = "",
      desc = "Reload WorldEdit",
      min = 0,
      max = 0
   )
   @CommandPermissions({"worldedit.reload"})
   @Console
   public void reload(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      this.we.getServer().reload();
      player.print("Configuration reloaded!");
   }

   @Command(
      aliases = {"cui"},
      usage = "",
      desc = "Complete CUI handshake",
      min = 0,
      max = 0
   )
   public void cui(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      session.setCUISupport(true);
      session.dispatchCUISetup(player);
   }

   @Command(
      aliases = {"tz"},
      usage = "[timezone]",
      desc = "Set your timezone",
      min = 1,
      max = 1
   )
   @Console
   public void tz(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      TimeZone tz = TimeZone.getTimeZone(args.getString(0));
      session.setTimezone(tz);
      player.print("Timezone set for this session to: " + tz.getDisplayName());
      player.print("The current time in that timezone is: " + dateFormat.format(Calendar.getInstance(tz).getTime()));
   }

   @Command(
      aliases = {"help"},
      usage = "[<command>]",
      desc = "Displays help for the given command or lists all commands.",
      min = 0,
      max = -1
   )
   @CommandPermissions({"worldedit.help"})
   @Console
   public void help(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      UtilityCommands.help(args, this.we, session, player, editSession);
   }
}
