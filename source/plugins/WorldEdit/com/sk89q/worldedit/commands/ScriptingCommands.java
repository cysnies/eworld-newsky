package com.sk89q.worldedit.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.Logging;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import java.io.File;

public class ScriptingCommands {
   private final WorldEdit we;

   public ScriptingCommands(WorldEdit we) {
      this.we = we;
   }

   @Command(
      aliases = {"cs"},
      usage = "<filename> [args...]",
      desc = "Execute a CraftScript",
      min = 1,
      max = -1
   )
   @CommandPermissions({"worldedit.scripting.execute"})
   @Logging(Logging.LogMode.ALL)
   public void execute(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      String[] scriptArgs = args.getSlice(1);
      String name = args.getString(0);
      if (!player.hasPermission("worldedit.scripting.execute." + name)) {
         player.printError("You don't have permission to use that script.");
      } else {
         session.setLastScript(name);
         File dir = this.we.getWorkingDirectoryFile(this.we.getConfiguration().scriptsDir);
         File f = this.we.getSafeOpenFile(player, dir, name, "js", "js");
         this.we.runScript(player, f, scriptArgs);
      }
   }

   @Command(
      aliases = {".s"},
      usage = "[args...]",
      desc = "Execute last CraftScript",
      min = 0,
      max = -1
   )
   @CommandPermissions({"worldedit.scripting.execute"})
   @Logging(Logging.LogMode.ALL)
   public void executeLast(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      String lastScript = session.getLastScript();
      if (!player.hasPermission("worldedit.scripting.execute." + lastScript)) {
         player.printError("You don't have permission to use that script.");
      } else if (lastScript == null) {
         player.printError("Use /cs with a script name first.");
      } else {
         String[] scriptArgs = args.getSlice(0);
         File dir = this.we.getWorkingDirectoryFile(this.we.getConfiguration().scriptsDir);
         File f = this.we.getSafeOpenFile(player, dir, lastScript, "js", "js");
         this.we.runScript(player, f, scriptArgs);
      }
   }
}
