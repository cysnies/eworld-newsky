package com.sk89q.worldedit.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.Logging;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.HeightMap;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.expression.ExpressionException;
import com.sk89q.worldedit.filtering.GaussianKernel;
import com.sk89q.worldedit.filtering.HeightMapFilter;
import com.sk89q.worldedit.masks.Mask;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.patterns.SingleBlockPattern;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;
import java.util.Set;

public class RegionCommands {
   private final WorldEdit we;

   public RegionCommands(WorldEdit we) {
      this.we = we;
   }

   @Command(
      aliases = {"/set"},
      usage = "<block>",
      desc = "Set all the blocks inside the selection to a block",
      min = 1,
      max = 1
   )
   @CommandPermissions({"worldedit.region.set"})
   @Logging(Logging.LogMode.REGION)
   public void set(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      Pattern pattern = this.we.getBlockPattern(player, args.getString(0));
      int affected;
      if (pattern instanceof SingleBlockPattern) {
         affected = editSession.setBlocks(session.getSelection(player.getWorld()), ((SingleBlockPattern)pattern).getBlock());
      } else {
         affected = editSession.setBlocks(session.getSelection(player.getWorld()), pattern);
      }

      player.print(affected + " block(s) have been changed.");
   }

   @Command(
      aliases = {"/replace", "/re", "/rep"},
      usage = "[from-block] <to-block>",
      desc = "Replace all blocks in the selection with another",
      flags = "f",
      min = 1,
      max = 2
   )
   @CommandPermissions({"worldedit.region.replace"})
   @Logging(Logging.LogMode.REGION)
   public void replace(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      Set<BaseBlock> from;
      Pattern to;
      if (args.argsLength() == 1) {
         from = null;
         to = this.we.getBlockPattern(player, args.getString(0));
      } else {
         from = this.we.getBlocks(player, args.getString(0), true, !args.hasFlag('f'));
         to = this.we.getBlockPattern(player, args.getString(1));
      }

      int affected = 0;
      if (to instanceof SingleBlockPattern) {
         affected = editSession.replaceBlocks(session.getSelection(player.getWorld()), from, ((SingleBlockPattern)to).getBlock());
      } else {
         affected = editSession.replaceBlocks(session.getSelection(player.getWorld()), from, to);
      }

      player.print(affected + " block(s) have been replaced.");
   }

   @Command(
      aliases = {"/overlay"},
      usage = "<block>",
      desc = "Set a block on top of blocks in the region",
      min = 1,
      max = 1
   )
   @CommandPermissions({"worldedit.region.overlay"})
   @Logging(Logging.LogMode.REGION)
   public void overlay(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      Pattern pat = this.we.getBlockPattern(player, args.getString(0));
      Region region = session.getSelection(player.getWorld());
      int affected = 0;
      if (pat instanceof SingleBlockPattern) {
         affected = editSession.overlayCuboidBlocks(region, ((SingleBlockPattern)pat).getBlock());
      } else {
         affected = editSession.overlayCuboidBlocks(region, pat);
      }

      player.print(affected + " block(s) have been overlayed.");
   }

   @Command(
      aliases = {"/center", "/middle"},
      usage = "<block>",
      desc = "Set the center block(s)",
      min = 1,
      max = 1
   )
   @Logging(Logging.LogMode.REGION)
   @CommandPermissions({"worldedit.region.center"})
   public void center(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      Pattern pattern = this.we.getBlockPattern(player, args.getString(0));
      Region region = session.getSelection(player.getWorld());
      int affected = editSession.center(region, pattern);
      player.print("Center set (" + affected + " blocks changed)");
   }

   @Command(
      aliases = {"/naturalize"},
      usage = "",
      desc = "3 layers of dirt on top then rock below",
      min = 0,
      max = 0
   )
   @CommandPermissions({"worldedit.region.naturalize"})
   @Logging(Logging.LogMode.REGION)
   public void naturalize(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      Region region = session.getSelection(player.getWorld());
      int affected = editSession.naturalizeCuboidBlocks(region);
      player.print(affected + " block(s) have been naturalized.");
   }

   @Command(
      aliases = {"/walls"},
      usage = "<block>",
      desc = "Build the four sides of the selection",
      min = 1,
      max = 1
   )
   @CommandPermissions({"worldedit.region.walls"})
   @Logging(Logging.LogMode.REGION)
   public void walls(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      Pattern pattern = this.we.getBlockPattern(player, args.getString(0));
      int affected;
      if (pattern instanceof SingleBlockPattern) {
         affected = editSession.makeCuboidWalls(session.getSelection(player.getWorld()), ((SingleBlockPattern)pattern).getBlock());
      } else {
         affected = editSession.makeCuboidWalls(session.getSelection(player.getWorld()), pattern);
      }

      player.print(affected + " block(s) have been changed.");
   }

   @Command(
      aliases = {"/faces", "/outline"},
      usage = "<block>",
      desc = "Build the walls, ceiling, and floor of a selection",
      min = 1,
      max = 1
   )
   @CommandPermissions({"worldedit.region.faces"})
   @Logging(Logging.LogMode.REGION)
   public void faces(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      Pattern pattern = this.we.getBlockPattern(player, args.getString(0));
      int affected;
      if (pattern instanceof SingleBlockPattern) {
         affected = editSession.makeCuboidFaces(session.getSelection(player.getWorld()), ((SingleBlockPattern)pattern).getBlock());
      } else {
         affected = editSession.makeCuboidFaces(session.getSelection(player.getWorld()), pattern);
      }

      player.print(affected + " block(s) have been changed.");
   }

   @Command(
      aliases = {"/smooth"},
      usage = "[iterations]",
      flags = "n",
      desc = "Smooth the elevation in the selection",
      help = "Smooths the elevation in the selection.\nThe -n flag makes it only consider naturally occuring blocks.",
      min = 0,
      max = 1
   )
   @CommandPermissions({"worldedit.region.smooth"})
   @Logging(Logging.LogMode.REGION)
   public void smooth(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      int iterations = 1;
      if (args.argsLength() > 0) {
         iterations = args.getInteger(0);
      }

      HeightMap heightMap = new HeightMap(editSession, session.getSelection(player.getWorld()), args.hasFlag('n'));
      HeightMapFilter filter = new HeightMapFilter(new GaussianKernel(5, (double)1.0F));
      int affected = heightMap.applyFilter(filter, iterations);
      player.print("Terrain's height map smoothed. " + affected + " block(s) changed.");
   }

   @Command(
      aliases = {"/move"},
      usage = "[count] [direction] [leave-id]",
      flags = "s",
      desc = "Move the contents of the selection",
      help = "Moves the contents of the selection.\nThe -s flag shifts the selection to the target location.\nOptionally fills the old location with <leave-id>.",
      min = 0,
      max = 3
   )
   @CommandPermissions({"worldedit.region.move"})
   @Logging(Logging.LogMode.ORIENTATION_REGION)
   public void move(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      int count = args.argsLength() > 0 ? Math.max(1, args.getInteger(0)) : 1;
      Vector dir = this.we.getDirection(player, args.argsLength() > 1 ? args.getString(1).toLowerCase() : "me");
      BaseBlock replace;
      if (args.argsLength() > 2) {
         replace = this.we.getBlock(player, args.getString(2));
      } else {
         replace = new BaseBlock(0);
      }

      int affected = editSession.moveRegion(session.getSelection(player.getWorld()), dir, count, true, replace);
      if (args.hasFlag('s')) {
         try {
            Region region = session.getSelection(player.getWorld());
            region.shift(dir.multiply(count));
            session.getRegionSelector(player.getWorld()).learnChanges();
            session.getRegionSelector(player.getWorld()).explainRegionAdjust(player, session);
         } catch (RegionOperationException e) {
            player.printError(e.getMessage());
         }
      }

      player.print(affected + " blocks moved.");
   }

   @Command(
      aliases = {"/stack"},
      usage = "[count] [direction]",
      flags = "sa",
      desc = "Repeat the contents of the selection",
      help = "Repeats the contents of the selection.\nFlags:\n  -s shifts the selection to the last stacked copy\n  -a skips air blocks",
      min = 0,
      max = 2
   )
   @CommandPermissions({"worldedit.region.stack"})
   @Logging(Logging.LogMode.ORIENTATION_REGION)
   public void stack(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      int count = args.argsLength() > 0 ? Math.max(1, args.getInteger(0)) : 1;
      Vector dir = this.we.getDiagonalDirection(player, args.argsLength() > 1 ? args.getString(1).toLowerCase() : "me");
      int affected = editSession.stackCuboidRegion(session.getSelection(player.getWorld()), dir, count, !args.hasFlag('a'));
      if (args.hasFlag('s')) {
         try {
            Region region = session.getSelection(player.getWorld());
            Vector size = region.getMaximumPoint().subtract(region.getMinimumPoint());
            Vector shiftVector = dir.multiply((double)count * (Math.abs(dir.dot(size)) + (double)1.0F));
            region.shift(shiftVector);
            session.getRegionSelector(player.getWorld()).learnChanges();
            session.getRegionSelector(player.getWorld()).explainRegionAdjust(player, session);
         } catch (RegionOperationException e) {
            player.printError(e.getMessage());
         }
      }

      player.print(affected + " blocks changed. Undo with //undo");
   }

   @Command(
      aliases = {"/regen"},
      usage = "",
      desc = "Regenerates the contents of the selection",
      help = "Regenerates the contents of the current selection.\nThis command might affect things outside the selection,\nif they are within the same chunk.",
      min = 0,
      max = 0
   )
   @CommandPermissions({"worldedit.regen"})
   @Logging(Logging.LogMode.REGION)
   public void regenerateChunk(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      Region region = session.getSelection(player.getWorld());
      Mask mask = session.getMask();
      session.setMask((Mask)null);
      player.getWorld().regenerate(region, editSession);
      session.setMask(mask);
      player.print("Region regenerated.");
   }

   @Command(
      aliases = {"/deform"},
      usage = "<expression>",
      desc = "Deforms a selected region with an expression",
      help = "Deforms a selected region with an expression\nThe expression is executed for each block and is expected\nto modify the variables x, y and z to point to a new block\nto fetch. See also tinyurl.com/wesyntax.",
      flags = "ro",
      min = 1,
      max = -1
   )
   @CommandPermissions({"worldedit.region.deform"})
   @Logging(Logging.LogMode.ALL)
   public void deform(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      Region region = session.getSelection(player.getWorld());
      String expression = args.getJoinedStrings(0);
      Vector zero;
      Vector unit;
      if (args.hasFlag('r')) {
         zero = Vector.ZERO;
         unit = Vector.ONE;
      } else if (args.hasFlag('o')) {
         zero = session.getPlacementPosition(player);
         unit = Vector.ONE;
      } else {
         Vector min = region.getMinimumPoint();
         Vector max = region.getMaximumPoint();
         zero = max.add(min).multiply((double)0.5F);
         unit = max.subtract(zero);
         if (unit.getX() == (double)0.0F) {
            unit = unit.setX((double)1.0F);
         }

         if (unit.getY() == (double)0.0F) {
            unit = unit.setY((double)1.0F);
         }

         if (unit.getZ() == (double)0.0F) {
            unit = unit.setZ((double)1.0F);
         }
      }

      try {
         int affected = editSession.deformRegion(region, zero, unit, expression);
         player.findFreePosition();
         player.print(affected + " block(s) have been deformed.");
      } catch (ExpressionException e) {
         player.printError(e.getMessage());
      }

   }

   @Command(
      aliases = {"/hollow"},
      usage = "[<thickness>[ <block>]]",
      desc = "Hollows out the object contained in this selection",
      help = "Hollows out the object contained in this selection.\nOptionally fills the hollowed out part with the given block.\nThickness is measured in manhattan distance.",
      min = 0,
      max = 2
   )
   @CommandPermissions({"worldedit.region.hollow"})
   @Logging(Logging.LogMode.REGION)
   public void hollow(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      int thickness = args.argsLength() >= 1 ? Math.max(1, args.getInteger(0)) : 1;
      Pattern pattern = (Pattern)(args.argsLength() >= 2 ? this.we.getBlockPattern(player, args.getString(1)) : new SingleBlockPattern(new BaseBlock(0)));
      int affected = editSession.hollowOutRegion(session.getSelection(player.getWorld()), thickness, pattern);
      player.print(affected + " block(s) have been changed.");
   }
}
