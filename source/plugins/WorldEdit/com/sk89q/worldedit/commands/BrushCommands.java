package com.sk89q.worldedit.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.masks.BlockMask;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.patterns.SingleBlockPattern;
import com.sk89q.worldedit.tools.BrushTool;
import com.sk89q.worldedit.tools.brushes.ButcherBrush;
import com.sk89q.worldedit.tools.brushes.ClipboardBrush;
import com.sk89q.worldedit.tools.brushes.CylinderBrush;
import com.sk89q.worldedit.tools.brushes.GravityBrush;
import com.sk89q.worldedit.tools.brushes.HollowCylinderBrush;
import com.sk89q.worldedit.tools.brushes.HollowSphereBrush;
import com.sk89q.worldedit.tools.brushes.SmoothBrush;
import com.sk89q.worldedit.tools.brushes.SphereBrush;

public class BrushCommands {
   private final WorldEdit we;

   public BrushCommands(WorldEdit we) {
      this.we = we;
   }

   @Command(
      aliases = {"sphere", "s"},
      usage = "<block> [radius]",
      flags = "h",
      desc = "Choose the sphere brush",
      help = "Chooses the sphere brush.\nThe -h flag creates hollow spheres instead.",
      min = 1,
      max = 2
   )
   @CommandPermissions({"worldedit.brush.sphere"})
   public void sphereBrush(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      double radius = args.argsLength() > 1 ? args.getDouble(1) : (double)2.0F;
      this.we.checkMaxBrushRadius(radius);
      BrushTool tool = session.getBrushTool(player.getItemInHand());
      Pattern fill = this.we.getBlockPattern(player, args.getString(0));
      tool.setFill(fill);
      tool.setSize(radius);
      if (args.hasFlag('h')) {
         tool.setBrush(new HollowSphereBrush(), "worldedit.brush.sphere");
      } else {
         tool.setBrush(new SphereBrush(), "worldedit.brush.sphere");
      }

      player.print(String.format("Sphere brush shape equipped (%.0f).", radius));
   }

   @Command(
      aliases = {"cylinder", "cyl", "c"},
      usage = "<block> [radius] [height]",
      flags = "h",
      desc = "Choose the cylinder brush",
      help = "Chooses the cylinder brush.\nThe -h flag creates hollow cylinders instead.",
      min = 1,
      max = 3
   )
   @CommandPermissions({"worldedit.brush.cylinder"})
   public void cylinderBrush(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      double radius = args.argsLength() > 1 ? args.getDouble(1) : (double)2.0F;
      this.we.checkMaxBrushRadius(radius);
      int height = args.argsLength() > 2 ? args.getInteger(2) : 1;
      this.we.checkMaxBrushRadius((double)height);
      BrushTool tool = session.getBrushTool(player.getItemInHand());
      Pattern fill = this.we.getBlockPattern(player, args.getString(0));
      tool.setFill(fill);
      tool.setSize(radius);
      if (args.hasFlag('h')) {
         tool.setBrush(new HollowCylinderBrush(height), "worldedit.brush.cylinder");
      } else {
         tool.setBrush(new CylinderBrush(height), "worldedit.brush.cylinder");
      }

      player.print(String.format("Cylinder brush shape equipped (%.0f by %d).", radius, height));
   }

   @Command(
      aliases = {"clipboard", "copy"},
      usage = "",
      flags = "a",
      desc = "Choose the clipboard brush",
      help = "Chooses the clipboard brush.\nThe -a flag makes it not paste air.",
      min = 0,
      max = 0
   )
   @CommandPermissions({"worldedit.brush.clipboard"})
   public void clipboardBrush(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      CuboidClipboard clipboard = session.getClipboard();
      if (clipboard == null) {
         player.printError("Copy something first.");
      } else {
         Vector size = clipboard.getSize();
         this.we.checkMaxBrushRadius((double)size.getBlockX());
         this.we.checkMaxBrushRadius((double)size.getBlockY());
         this.we.checkMaxBrushRadius((double)size.getBlockZ());
         BrushTool tool = session.getBrushTool(player.getItemInHand());
         tool.setBrush(new ClipboardBrush(clipboard, args.hasFlag('a')), "worldedit.brush.clipboard");
         player.print("Clipboard brush shape equipped.");
      }
   }

   @Command(
      aliases = {"smooth"},
      usage = "[size] [iterations]",
      flags = "n",
      desc = "Choose the terrain softener brush",
      help = "Chooses the terrain softener brush.\nThe -n flag makes it only consider naturally occuring blocks.",
      min = 0,
      max = 2
   )
   @CommandPermissions({"worldedit.brush.smooth"})
   public void smoothBrush(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      double radius = args.argsLength() > 0 ? args.getDouble(0) : (double)2.0F;
      this.we.checkMaxBrushRadius(radius);
      int iterations = args.argsLength() > 1 ? args.getInteger(1) : 4;
      BrushTool tool = session.getBrushTool(player.getItemInHand());
      tool.setSize(radius);
      tool.setBrush(new SmoothBrush(iterations, args.hasFlag('n')), "worldedit.brush.smooth");
      player.print(String.format("Smooth brush equipped (%.0f x %dx, using " + (args.hasFlag('n') ? "natural blocks only" : "any block") + ").", radius, iterations));
   }

   @Command(
      aliases = {"ex", "extinguish"},
      usage = "[radius]",
      desc = "Shortcut fire extinguisher brush",
      min = 0,
      max = 1
   )
   @CommandPermissions({"worldedit.brush.ex"})
   public void extinguishBrush(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      double radius = args.argsLength() > 1 ? args.getDouble(1) : (double)5.0F;
      this.we.checkMaxBrushRadius(radius);
      BrushTool tool = session.getBrushTool(player.getItemInHand());
      Pattern fill = new SingleBlockPattern(new BaseBlock(0));
      tool.setFill(fill);
      tool.setSize(radius);
      tool.setMask(new BlockMask(new BaseBlock(51)));
      tool.setBrush(new SphereBrush(), "worldedit.brush.ex");
      player.print(String.format("Extinguisher equipped (%.0f).", radius));
   }

   @Command(
      aliases = {"gravity", "grav"},
      usage = "[radius]",
      flags = "h",
      desc = "Gravity brush",
      help = "This brush simulates the affect of gravity.\nThe -h flag makes it affect blocks starting at the world's max y, instead of the clicked block's y + radius.",
      min = 0,
      max = 1
   )
   @CommandPermissions({"worldedit.brush.gravity"})
   public void gravityBrush(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      double radius = args.argsLength() > 0 ? args.getDouble(0) : (double)5.0F;
      this.we.checkMaxBrushRadius(radius);
      BrushTool tool = session.getBrushTool(player.getItemInHand());
      tool.setSize(radius);
      tool.setBrush(new GravityBrush(args.hasFlag('h')), "worldedit.brush.gravity");
      player.print(String.format("Gravity brush equipped (%.0f).", radius));
   }

   @Command(
      aliases = {"butcher", "kill"},
      usage = "[radius] [command flags]",
      desc = "Butcher brush",
      help = "Kills nearby mobs within the specified radius.\nAny number of 'flags' that the //butcher command uses\nmay be specified as an argument",
      min = 0,
      max = 2
   )
   @CommandPermissions({"worldedit.brush.butcher"})
   public void butcherBrush(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      LocalConfiguration config = this.we.getConfiguration();
      double radius = args.argsLength() > 0 ? args.getDouble(0) : (double)5.0F;
      double maxRadius = (double)config.maxBrushRadius;
      if (player.hasPermission("worldedit.butcher")) {
         maxRadius = (double)Math.max(config.maxBrushRadius, config.butcherMaxRadius);
      }

      if (radius > maxRadius) {
         player.printError("Maximum allowed brush radius: " + maxRadius);
      } else {
         UtilityCommands.FlagContainer flags = new UtilityCommands.FlagContainer(player);
         if (args.argsLength() == 2) {
            String flagString = args.getString(1);
            flags.or(31, flagString.contains("f"));
            flags.or(1, flagString.contains("p"), "worldedit.butcher.pets");
            flags.or(2, flagString.contains("n"), "worldedit.butcher.npcs");
            flags.or(8, flagString.contains("g"), "worldedit.butcher.golems");
            flags.or(4, flagString.contains("a"), "worldedit.butcher.animals");
            flags.or(16, flagString.contains("b"), "worldedit.butcher.ambient");
            flags.or(1048576, flagString.contains("l"), "worldedit.butcher.lightning");
         }

         BrushTool tool = session.getBrushTool(player.getItemInHand());
         tool.setSize(radius);
         tool.setBrush(new ButcherBrush(flags.flags), "worldedit.brush.butcher");
         player.print(String.format("Butcher brush equipped (%.0f).", radius));
      }
   }
}
