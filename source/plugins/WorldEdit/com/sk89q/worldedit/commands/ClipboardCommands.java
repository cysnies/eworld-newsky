package com.sk89q.worldedit.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.Logging;
import com.sk89q.minecraft.util.commands.NestedCommand;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalEntity;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;

public class ClipboardCommands {
   private final WorldEdit we;

   public ClipboardCommands(WorldEdit we) {
      this.we = we;
   }

   @Command(
      aliases = {"/copy"},
      flags = "e",
      desc = "Copy the selection to the clipboard",
      help = "Copy the selection to the clipboard\nFlags:\n  -e controls whether entities are copied\nWARNING: Pasting entities cannot yet be undone!",
      min = 0,
      max = 0
   )
   @CommandPermissions({"worldedit.clipboard.copy"})
   public void copy(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      Region region = session.getSelection(player.getWorld());
      Vector min = region.getMinimumPoint();
      Vector max = region.getMaximumPoint();
      Vector pos = session.getPlacementPosition(player);
      CuboidClipboard clipboard = new CuboidClipboard(max.subtract(min).add(Vector.ONE), min, min.subtract(pos));
      if (region instanceof CuboidRegion) {
         clipboard.copy(editSession);
      } else {
         clipboard.copy(editSession, region);
      }

      if (args.hasFlag('e')) {
         for(LocalEntity entity : player.getWorld().getEntities(region)) {
            clipboard.storeEntity(entity);
         }
      }

      session.setClipboard(clipboard);
      player.print("Block(s) copied.");
   }

   @Command(
      aliases = {"/cut"},
      usage = "[leave-id]",
      desc = "Cut the selection to the clipboard",
      help = "Copy the selection to the clipboard\nFlags:\n  -e controls whether entities are copied\nWARNING: Cutting and pasting entities cannot yet be undone!",
      flags = "e",
      min = 0,
      max = 1
   )
   @CommandPermissions({"worldedit.clipboard.cut"})
   @Logging(Logging.LogMode.REGION)
   public void cut(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      BaseBlock block = new BaseBlock(0);
      LocalWorld world = player.getWorld();
      if (args.argsLength() > 0) {
         block = this.we.getBlock(player, args.getString(0));
      }

      Region region = session.getSelection(world);
      Vector min = region.getMinimumPoint();
      Vector max = region.getMaximumPoint();
      Vector pos = session.getPlacementPosition(player);
      CuboidClipboard clipboard = new CuboidClipboard(max.subtract(min).add(Vector.ONE), min, min.subtract(pos));
      if (region instanceof CuboidRegion) {
         clipboard.copy(editSession);
      } else {
         clipboard.copy(editSession, region);
      }

      if (args.hasFlag('e')) {
         LocalEntity[] entities = world.getEntities(region);

         for(LocalEntity entity : entities) {
            clipboard.storeEntity(entity);
         }

         world.killEntities(entities);
      }

      session.setClipboard(clipboard);
      editSession.setBlocks(region, block);
      player.print("Block(s) cut.");
   }

   @Command(
      aliases = {"/paste"},
      usage = "",
      flags = "ao",
      desc = "Paste the clipboard's contents",
      help = "Pastes the clipboard's contents.\nFlags:\n  -a skips air blocks\n  -o pastes at the original position",
      min = 0,
      max = 0
   )
   @CommandPermissions({"worldedit.clipboard.paste"})
   @Logging(Logging.LogMode.PLACEMENT)
   public void paste(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      boolean atOrigin = args.hasFlag('o');
      boolean pasteNoAir = args.hasFlag('a');
      if (atOrigin) {
         Vector pos = session.getClipboard().getOrigin();
         session.getClipboard().place(editSession, pos, pasteNoAir);
         session.getClipboard().pasteEntities(pos);
         player.findFreePosition();
         player.print("Pasted to copy origin. Undo with //undo");
      } else {
         Vector pos = session.getPlacementPosition(player);
         session.getClipboard().paste(editSession, pos, pasteNoAir, true);
         player.findFreePosition();
         player.print("Pasted relative to you. Undo with //undo");
      }

   }

   @Command(
      aliases = {"/rotate"},
      usage = "<angle-in-degrees>",
      desc = "Rotate the contents of the clipboard",
      min = 1,
      max = 1
   )
   @CommandPermissions({"worldedit.clipboard.rotate"})
   public void rotate(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      int angle = args.getInteger(0);
      if (angle % 90 == 0) {
         CuboidClipboard clipboard = session.getClipboard();
         clipboard.rotate2D(angle);
         player.print("Clipboard rotated by " + angle + " degrees.");
      } else {
         player.printError("Angles must be divisible by 90 degrees.");
      }

   }

   @Command(
      aliases = {"/flip"},
      usage = "[dir]",
      flags = "p",
      desc = "Flip the contents of the clipboard.",
      help = "Flips the contents of the clipboard.\nThe -p flag flips the selection around the player,\ninstead of the selections center.",
      min = 0,
      max = 1
   )
   @CommandPermissions({"worldedit.clipboard.flip"})
   public void flip(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      CuboidClipboard.FlipDirection dir = this.we.getFlipDirection(player, args.argsLength() > 0 ? args.getString(0).toLowerCase() : "me");
      CuboidClipboard clipboard = session.getClipboard();
      clipboard.flip(dir, args.hasFlag('p'));
      player.print("Clipboard flipped.");
   }

   /** @deprecated */
   @Command(
      aliases = {"/load"},
      usage = "<filename>",
      desc = "Load a schematic into your clipboard",
      min = 0,
      max = 1
   )
   @Deprecated
   @CommandPermissions({"worldedit.clipboard.load"})
   public void load(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      player.printError("This command is no longer used. See //schematic load.");
   }

   /** @deprecated */
   @Command(
      aliases = {"/save"},
      usage = "<filename>",
      desc = "Save a schematic into your clipboard",
      min = 0,
      max = 1
   )
   @Deprecated
   @CommandPermissions({"worldedit.clipboard.save"})
   public void save(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      player.printError("This command is no longer used. See //schematic save.");
   }

   @Command(
      aliases = {"/schematic", "/schem"},
      desc = "Schematic-related commands"
   )
   @NestedCommand({SchematicCommands.class})
   public void schematic() {
   }

   @Command(
      aliases = {"clearclipboard"},
      usage = "",
      desc = "Clear your clipboard",
      min = 0,
      max = 0
   )
   @CommandPermissions({"worldedit.clipboard.clear"})
   public void clearClipboard(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      session.setClipboard((CuboidClipboard)null);
      player.print("Clipboard cleared.");
   }
}
