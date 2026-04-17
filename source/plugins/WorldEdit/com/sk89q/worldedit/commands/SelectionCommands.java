package com.sk89q.worldedit.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandAlias;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.Logging;
import com.sk89q.worldedit.Countable;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.data.ChunkStore;
import com.sk89q.worldedit.regions.ConvexPolyhedralRegionSelector;
import com.sk89q.worldedit.regions.CuboidRegionSelector;
import com.sk89q.worldedit.regions.CylinderRegionSelector;
import com.sk89q.worldedit.regions.EllipsoidRegionSelector;
import com.sk89q.worldedit.regions.ExtendingCuboidRegionSelector;
import com.sk89q.worldedit.regions.Polygonal2DRegionSelector;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.SphereRegionSelector;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SelectionCommands {
   private final WorldEdit we;

   public SelectionCommands(WorldEdit we) {
      this.we = we;
   }

   @Command(
      aliases = {"/pos1"},
      usage = "[coordinates]",
      desc = "Set position 1",
      min = 0,
      max = 1
   )
   @Logging(Logging.LogMode.POSITION)
   @CommandPermissions({"worldedit.selection.pos"})
   public void pos1(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      Vector pos;
      if (args.argsLength() == 1) {
         if (!args.getString(0).matches("-?\\d+,-?\\d+,-?\\d+")) {
            player.printError("Invalid coordinates " + args.getString(0));
            return;
         }

         String[] coords = args.getString(0).split(",");
         pos = new Vector(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]));
      } else {
         pos = player.getBlockIn();
      }

      if (!session.getRegionSelector(player.getWorld()).selectPrimary(pos)) {
         player.printError("Position already set.");
      } else {
         session.getRegionSelector(player.getWorld()).explainPrimarySelection(player, session, pos);
      }
   }

   @Command(
      aliases = {"/pos2"},
      usage = "[coordinates]",
      desc = "Set position 2",
      min = 0,
      max = 1
   )
   @Logging(Logging.LogMode.POSITION)
   @CommandPermissions({"worldedit.selection.pos"})
   public void pos2(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      Vector pos;
      if (args.argsLength() == 1) {
         if (!args.getString(0).matches("-?\\d+,-?\\d+,-?\\d+")) {
            player.printError("Invalid coordinates " + args.getString(0));
            return;
         }

         String[] coords = args.getString(0).split(",");
         pos = new Vector(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]));
      } else {
         pos = player.getBlockIn();
      }

      if (!session.getRegionSelector(player.getWorld()).selectSecondary(pos)) {
         player.printError("Position already set.");
      } else {
         session.getRegionSelector(player.getWorld()).explainSecondarySelection(player, session, pos);
      }
   }

   @Command(
      aliases = {"/hpos1"},
      usage = "",
      desc = "Set position 1 to targeted block",
      min = 0,
      max = 0
   )
   @CommandPermissions({"worldedit.selection.hpos"})
   public void hpos1(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      Vector pos = player.getBlockTrace(300);
      if (pos != null) {
         if (!session.getRegionSelector(player.getWorld()).selectPrimary(pos)) {
            player.printError("Position already set.");
            return;
         }

         session.getRegionSelector(player.getWorld()).explainPrimarySelection(player, session, pos);
      } else {
         player.printError("No block in sight!");
      }

   }

   @Command(
      aliases = {"/hpos2"},
      usage = "",
      desc = "Set position 2 to targeted block",
      min = 0,
      max = 0
   )
   @CommandPermissions({"worldedit.selection.hpos"})
   public void hpos2(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      Vector pos = player.getBlockTrace(300);
      if (pos != null) {
         if (!session.getRegionSelector(player.getWorld()).selectSecondary(pos)) {
            player.printError("Position already set.");
            return;
         }

         session.getRegionSelector(player.getWorld()).explainSecondarySelection(player, session, pos);
      } else {
         player.printError("No block in sight!");
      }

   }

   @Command(
      aliases = {"/chunk"},
      usage = "[x,z coordinates]",
      flags = "sc",
      desc = "Set the selection to your current chunk.",
      help = "Set the selection to the chunk you are currently in.\nWith the -s flag, your current selection is expanded\nto encompass all chunks that are part of it.\n\nSpecifying coordinates will use those instead of your\ncurrent position. Use -c to specify chunk coordinates,\notherwise full coordinates will be implied.\n(for example, the coordinates 5,5 are the same as -c 0,0)",
      min = 0,
      max = 1
   )
   @Logging(Logging.LogMode.POSITION)
   @CommandPermissions({"worldedit.selection.chunk"})
   public void chunk(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      LocalWorld world = player.getWorld();
      Vector min;
      Vector max;
      if (args.hasFlag('s')) {
         Region region = session.getSelection(world);
         Vector2D min2D = ChunkStore.toChunk(region.getMinimumPoint());
         Vector2D max2D = ChunkStore.toChunk(region.getMaximumPoint());
         min = new Vector(min2D.getBlockX() * 16, 0, min2D.getBlockZ() * 16);
         max = new Vector(max2D.getBlockX() * 16 + 15, world.getMaxY(), max2D.getBlockZ() * 16 + 15);
         player.print("Chunks selected: (" + min2D.getBlockX() + ", " + min2D.getBlockZ() + ") - (" + max2D.getBlockX() + ", " + max2D.getBlockZ() + ")");
      } else {
         Vector2D min2D;
         if (args.argsLength() == 1) {
            String[] coords = args.getString(0).split(",");
            if (coords.length != 2) {
               throw new InsufficientArgumentsException("Invalid coordinates specified.");
            }

            int x = Integer.parseInt(coords[0]);
            int z = Integer.parseInt(coords[1]);
            Vector2D pos = new Vector2D(x, z);
            min2D = (Vector2D)(args.hasFlag('c') ? pos : ChunkStore.toChunk(pos.toVector()));
         } else {
            min2D = ChunkStore.toChunk(player.getBlockIn());
         }

         min = new Vector(min2D.getBlockX() * 16, 0, min2D.getBlockZ() * 16);
         max = min.add(15, world.getMaxY(), 15);
         player.print("Chunk selected: " + min2D.getBlockX() + ", " + min2D.getBlockZ());
      }

      CuboidRegionSelector selector;
      if (session.getRegionSelector(world) instanceof ExtendingCuboidRegionSelector) {
         selector = new ExtendingCuboidRegionSelector(world);
      } else {
         selector = new CuboidRegionSelector(world);
      }

      selector.selectPrimary(min);
      selector.selectSecondary(max);
      session.setRegionSelector(world, selector);
      session.dispatchCUISelection(player);
   }

   @Command(
      aliases = {"/wand"},
      usage = "",
      desc = "Get the wand object",
      min = 0,
      max = 0
   )
   @CommandPermissions({"worldedit.wand"})
   public void wand(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      player.giveItem(this.we.getConfiguration().wandItem, 1);
      player.print("Left click: select pos #1; Right click: select pos #2");
   }

   @Command(
      aliases = {"toggleeditwand"},
      usage = "",
      desc = "Toggle functionality of the edit wand",
      min = 0,
      max = 0
   )
   @CommandPermissions({"worldedit.wand.toggle"})
   public void toggleWand(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      session.setToolControl(!session.isToolControlEnabled());
      if (session.isToolControlEnabled()) {
         player.print("Edit wand enabled.");
      } else {
         player.print("Edit wand disabled.");
      }

   }

   @Command(
      aliases = {"/expand"},
      usage = "<amount> [reverse-amount] <direction>",
      desc = "Expand the selection area",
      min = 1,
      max = 3
   )
   @Logging(Logging.LogMode.REGION)
   @CommandPermissions({"worldedit.selection.expand"})
   public void expand(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      if (!args.getString(0).equalsIgnoreCase("vert") && !args.getString(0).equalsIgnoreCase("vertical")) {
         List<Vector> dirs = new ArrayList();
         int change = args.getInteger(0);
         int reverseChange = 0;
         switch (args.argsLength()) {
            case 2:
               try {
                  reverseChange = args.getInteger(1);
                  dirs.add(this.we.getDirection(player, "me"));
               } catch (NumberFormatException var15) {
                  if (args.getString(1).contains(",")) {
                     String[] split = args.getString(1).split(",");

                     for(String s : split) {
                        dirs.add(this.we.getDirection(player, s.toLowerCase()));
                     }
                  } else {
                     dirs.add(this.we.getDirection(player, args.getString(1).toLowerCase()));
                  }
               }
               break;
            case 3:
               reverseChange = args.getInteger(1);
               if (args.getString(2).contains(",")) {
                  String[] split = args.getString(2).split(",");

                  for(String s : split) {
                     dirs.add(this.we.getDirection(player, s.toLowerCase()));
                  }
               } else {
                  dirs.add(this.we.getDirection(player, args.getString(2).toLowerCase()));
               }
               break;
            default:
               dirs.add(this.we.getDirection(player, "me"));
         }

         Region region = session.getSelection(player.getWorld());
         int oldSize = region.getArea();
         if (reverseChange == 0) {
            for(Vector dir : dirs) {
               region.expand(dir.multiply(change));
            }
         } else {
            for(Vector dir : dirs) {
               region.expand(dir.multiply(change), dir.multiply(-reverseChange));
            }
         }

         session.getRegionSelector(player.getWorld()).learnChanges();
         int newSize = region.getArea();
         session.getRegionSelector(player.getWorld()).explainRegionAdjust(player, session);
         player.print("Region expanded " + (newSize - oldSize) + " blocks.");
      } else {
         Region region = session.getSelection(player.getWorld());

         try {
            int oldSize = region.getArea();
            region.expand(new Vector(0, player.getWorld().getMaxY() + 1, 0), new Vector(0, -(player.getWorld().getMaxY() + 1), 0));
            session.getRegionSelector(player.getWorld()).learnChanges();
            int newSize = region.getArea();
            session.getRegionSelector(player.getWorld()).explainRegionAdjust(player, session);
            player.print("Region expanded " + (newSize - oldSize) + " blocks [top-to-bottom].");
         } catch (RegionOperationException e) {
            player.printError(e.getMessage());
         }

      }
   }

   @Command(
      aliases = {"/contract"},
      usage = "<amount> [reverse-amount] [direction]",
      desc = "Contract the selection area",
      min = 1,
      max = 3
   )
   @Logging(Logging.LogMode.REGION)
   @CommandPermissions({"worldedit.selection.contract"})
   public void contract(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      List<Vector> dirs = new ArrayList();
      int change = args.getInteger(0);
      int reverseChange = 0;
      switch (args.argsLength()) {
         case 2:
            try {
               reverseChange = args.getInteger(1);
               dirs.add(this.we.getDirection(player, "me"));
            } catch (NumberFormatException var15) {
               if (args.getString(1).contains(",")) {
                  String[] split = args.getString(1).split(",");

                  for(String s : split) {
                     dirs.add(this.we.getDirection(player, s.toLowerCase()));
                  }
               } else {
                  dirs.add(this.we.getDirection(player, args.getString(1).toLowerCase()));
               }
            }
            break;
         case 3:
            reverseChange = args.getInteger(1);
            if (args.getString(2).contains(",")) {
               String[] split = args.getString(2).split(",");

               for(String s : split) {
                  dirs.add(this.we.getDirection(player, s.toLowerCase()));
               }
            } else {
               dirs.add(this.we.getDirection(player, args.getString(2).toLowerCase()));
            }
            break;
         default:
            dirs.add(this.we.getDirection(player, "me"));
      }

      try {
         Region region = session.getSelection(player.getWorld());
         int oldSize = region.getArea();
         if (reverseChange == 0) {
            for(Vector dir : dirs) {
               region.contract(dir.multiply(change));
            }
         } else {
            for(Vector dir : dirs) {
               region.contract(dir.multiply(change), dir.multiply(-reverseChange));
            }
         }

         session.getRegionSelector(player.getWorld()).learnChanges();
         int newSize = region.getArea();
         session.getRegionSelector(player.getWorld()).explainRegionAdjust(player, session);
         player.print("Region contracted " + (oldSize - newSize) + " blocks.");
      } catch (RegionOperationException e) {
         player.printError(e.getMessage());
      }

   }

   @Command(
      aliases = {"/shift"},
      usage = "<amount> [direction]",
      desc = "Shift the selection area",
      min = 1,
      max = 2
   )
   @Logging(Logging.LogMode.REGION)
   @CommandPermissions({"worldedit.selection.shift"})
   public void shift(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      List<Vector> dirs = new ArrayList();
      int change = args.getInteger(0);
      if (args.argsLength() == 2) {
         if (args.getString(1).contains(",")) {
            for(String s : args.getString(1).split(",")) {
               dirs.add(this.we.getDirection(player, s.toLowerCase()));
            }
         } else {
            dirs.add(this.we.getDirection(player, args.getString(1).toLowerCase()));
         }
      } else {
         dirs.add(this.we.getDirection(player, "me"));
      }

      try {
         Region region = session.getSelection(player.getWorld());

         for(Vector dir : dirs) {
            region.shift(dir.multiply(change));
         }

         session.getRegionSelector(player.getWorld()).learnChanges();
         session.getRegionSelector(player.getWorld()).explainRegionAdjust(player, session);
         player.print("Region shifted.");
      } catch (RegionOperationException e) {
         player.printError(e.getMessage());
      }

   }

   @Command(
      aliases = {"/outset"},
      usage = "<amount>",
      desc = "Outset the selection area",
      help = "Expands the selection by the given amount in all directions.\nFlags:\n  -h only expand horizontally\n  -v only expand vertically\n",
      flags = "hv",
      min = 1,
      max = 1
   )
   @Logging(Logging.LogMode.REGION)
   @CommandPermissions({"worldedit.selection.outset"})
   public void outset(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      Region region = session.getSelection(player.getWorld());
      region.expand(this.getChangesForEachDir(args));
      session.getRegionSelector(player.getWorld()).learnChanges();
      session.getRegionSelector(player.getWorld()).explainRegionAdjust(player, session);
      player.print("Region outset.");
   }

   @Command(
      aliases = {"/inset"},
      usage = "<amount>",
      desc = "Inset the selection area",
      help = "Contracts the selection by the given amount in all directions.\nFlags:\n  -h only contract horizontally\n  -v only contract vertically\n",
      flags = "hv",
      min = 1,
      max = 1
   )
   @Logging(Logging.LogMode.REGION)
   @CommandPermissions({"worldedit.selection.inset"})
   public void inset(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      Region region = session.getSelection(player.getWorld());
      region.contract(this.getChangesForEachDir(args));
      session.getRegionSelector(player.getWorld()).learnChanges();
      session.getRegionSelector(player.getWorld()).explainRegionAdjust(player, session);
      player.print("Region inset.");
   }

   private Vector[] getChangesForEachDir(CommandContext args) {
      List<Vector> changes = new ArrayList(6);
      int change = args.getInteger(0);
      if (!args.hasFlag('h')) {
         changes.add((new Vector(0, 1, 0)).multiply(change));
         changes.add((new Vector(0, -1, 0)).multiply(change));
      }

      if (!args.hasFlag('v')) {
         changes.add((new Vector(1, 0, 0)).multiply(change));
         changes.add((new Vector(-1, 0, 0)).multiply(change));
         changes.add((new Vector(0, 0, 1)).multiply(change));
         changes.add((new Vector(0, 0, -1)).multiply(change));
      }

      return (Vector[])changes.toArray(new Vector[0]);
   }

   @Command(
      aliases = {"/size"},
      flags = "c",
      usage = "",
      desc = "Get information about the selection",
      min = 0,
      max = 0
   )
   @CommandPermissions({"worldedit.selection.size"})
   public void size(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      if (args.hasFlag('c')) {
         CuboidClipboard clipboard = session.getClipboard();
         Vector size = clipboard.getSize();
         Vector offset = clipboard.getOffset();
         player.print("Size: " + size);
         player.print("Offset: " + offset);
         player.print("Cuboid distance: " + size.distance(Vector.ONE));
         player.print("# of blocks: " + (int)(size.getX() * size.getY() * size.getZ()));
      } else {
         Region region = session.getSelection(player.getWorld());
         Vector size = region.getMaximumPoint().subtract(region.getMinimumPoint()).add(1, 1, 1);
         player.print("Type: " + session.getRegionSelector(player.getWorld()).getTypeName());

         for(String line : session.getRegionSelector(player.getWorld()).getInformationLines()) {
            player.print(line);
         }

         player.print("Size: " + size);
         player.print("Cuboid distance: " + region.getMaximumPoint().distance(region.getMinimumPoint()));
         player.print("# of blocks: " + region.getArea());
      }
   }

   @Command(
      aliases = {"/count"},
      usage = "<block>",
      desc = "Counts the number of a certain type of block",
      flags = "d",
      min = 1,
      max = 1
   )
   @CommandPermissions({"worldedit.analysis.count"})
   public void count(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      boolean useData = args.hasFlag('d');
      if (args.getString(0).contains(":")) {
         useData = true;
      }

      if (useData) {
         Set<BaseBlock> searchBlocks = this.we.getBlocks(player, args.getString(0), true);
         int count = editSession.countBlocks(session.getSelection(player.getWorld()), searchBlocks);
         player.print("Counted: " + count);
      } else {
         Set<Integer> searchIDs = this.we.getBlockIDs(player, args.getString(0), true);
         int count = editSession.countBlock(session.getSelection(player.getWorld()), searchIDs);
         player.print("Counted: " + count);
      }

   }

   @Command(
      aliases = {"/distr"},
      usage = "",
      desc = "Get the distribution of blocks in the selection",
      help = "Gets the distribution of blocks in the selection.\nThe -c flag gets the distribution of your clipboard.\nThe -d flag separates blocks by data",
      flags = "cd",
      min = 0,
      max = 0
   )
   @CommandPermissions({"worldedit.analysis.distr"})
   public void distr(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      boolean useData = args.hasFlag('d');
      List<Countable<Integer>> distribution = null;
      List<Countable<BaseBlock>> distributionData = null;
      int size;
      if (args.hasFlag('c')) {
         CuboidClipboard clip = session.getClipboard();
         if (useData) {
            distributionData = clip.getBlockDistributionWithData();
         } else {
            distribution = clip.getBlockDistribution();
         }

         size = clip.getHeight() * clip.getLength() * clip.getWidth();
      } else {
         if (useData) {
            distributionData = editSession.getBlockDistributionWithData(session.getSelection(player.getWorld()));
         } else {
            distribution = editSession.getBlockDistribution(session.getSelection(player.getWorld()));
         }

         size = session.getSelection(player.getWorld()).getArea();
      }

      if ((!useData || distributionData.size() > 0) && (useData || distribution.size() > 0)) {
         player.print("# total blocks: " + size);
         if (useData) {
            for(Countable c : distributionData) {
               String name = BlockType.fromID(((BaseBlock)c.getID()).getId()).getName();
               String str = String.format("%-7s (%.3f%%) %s #%d:%d", String.valueOf(c.getAmount()), (double)c.getAmount() / (double)size * (double)100.0F, name == null ? "Unknown" : name, ((BaseBlock)c.getID()).getType(), ((BaseBlock)c.getID()).getData());
               player.print(str);
            }
         } else {
            for(Countable c : distribution) {
               BlockType block = BlockType.fromID((Integer)c.getID());
               String str = String.format("%-7s (%.3f%%) %s #%d", String.valueOf(c.getAmount()), (double)c.getAmount() / (double)size * (double)100.0F, block == null ? "Unknown" : block.getName(), c.getID());
               player.print(str);
            }
         }

      } else {
         player.printError("No blocks counted.");
      }
   }

   @Command(
      aliases = {"/sel", ";"},
      usage = "[cuboid|extend|poly|ellipsoid|sphere|cyl|convex]",
      desc = "Choose a region selector",
      min = 0,
      max = 1
   )
   public void select(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      LocalWorld world = player.getWorld();
      if (args.argsLength() == 0) {
         session.getRegionSelector(world).clear();
         session.dispatchCUISelection(player);
         player.print("Selection cleared.");
      } else {
         String typeName = args.getString(0);
         RegionSelector oldSelector = session.getRegionSelector(world);
         RegionSelector selector;
         if (typeName.equalsIgnoreCase("cuboid")) {
            selector = new CuboidRegionSelector(oldSelector);
            player.print("Cuboid: left click for point 1, right click for point 2");
         } else if (typeName.equalsIgnoreCase("extend")) {
            selector = new ExtendingCuboidRegionSelector(oldSelector);
            player.print("Cuboid: left click for a starting point, right click to extend");
         } else if (typeName.equalsIgnoreCase("poly")) {
            int maxPoints = this.we.getMaximumPolygonalPoints(player);
            selector = new Polygonal2DRegionSelector(oldSelector, maxPoints);
            player.print("2D polygon selector: Left/right click to add a point.");
            if (maxPoints > -1) {
               player.print(maxPoints + " points maximum.");
            }
         } else if (typeName.equalsIgnoreCase("ellipsoid")) {
            selector = new EllipsoidRegionSelector(oldSelector);
            player.print("Ellipsoid selector: left click=center, right click to extend");
         } else if (typeName.equalsIgnoreCase("sphere")) {
            selector = new SphereRegionSelector(oldSelector);
            player.print("Sphere selector: left click=center, right click to set radius");
         } else if (typeName.equalsIgnoreCase("cyl")) {
            selector = new CylinderRegionSelector(oldSelector);
            player.print("Cylindrical selector: Left click=center, right click to extend.");
         } else {
            if (!typeName.equalsIgnoreCase("convex") && !typeName.equalsIgnoreCase("hull") && !typeName.equalsIgnoreCase("polyhedron")) {
               player.printError("Only cuboid|extend|poly|ellipsoid|sphere|cyl|convex are accepted.");
               return;
            }

            int maxVertices = this.we.getMaximumPolyhedronPoints(player);
            selector = new ConvexPolyhedralRegionSelector(oldSelector, maxVertices);
            player.print("Convex polyhedral selector: Left click=First vertex, right click to add more.");
         }

         session.setRegionSelector(world, selector);
         session.dispatchCUISelection(player);
      }
   }

   @Command(
      aliases = {"/desel", "/deselect"},
      desc = "Deselect the current selection"
   )
   @CommandAlias({"/sel"})
   public void deselect() {
   }
}
