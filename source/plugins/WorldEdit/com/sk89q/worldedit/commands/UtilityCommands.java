package com.sk89q.worldedit.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.CommandsManager;
import com.sk89q.minecraft.util.commands.Console;
import com.sk89q.minecraft.util.commands.Logging;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EntityType;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.patterns.SingleBlockPattern;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class UtilityCommands {
   private final WorldEdit we;

   public UtilityCommands(WorldEdit we) {
      this.we = we;
   }

   @Command(
      aliases = {"/fill"},
      usage = "<block> <radius> [depth]",
      desc = "Fill a hole",
      min = 2,
      max = 3
   )
   @CommandPermissions({"worldedit.fill"})
   @Logging(Logging.LogMode.PLACEMENT)
   public void fill(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      Pattern pattern = this.we.getBlockPattern(player, args.getString(0));
      double radius = Math.max((double)1.0F, args.getDouble(1));
      this.we.checkMaxRadius(radius);
      int depth = args.argsLength() > 2 ? Math.max(1, args.getInteger(2)) : 1;
      Vector pos = session.getPlacementPosition(player);
      int affected = 0;
      if (pattern instanceof SingleBlockPattern) {
         affected = editSession.fillXZ(pos, ((SingleBlockPattern)pattern).getBlock(), radius, depth, false);
      } else {
         affected = editSession.fillXZ(pos, pattern, radius, depth, false);
      }

      player.print(affected + " block(s) have been created.");
   }

   @Command(
      aliases = {"/fillr"},
      usage = "<block> <radius> [depth]",
      desc = "Fill a hole recursively",
      min = 2,
      max = 3
   )
   @CommandPermissions({"worldedit.fill.recursive"})
   @Logging(Logging.LogMode.PLACEMENT)
   public void fillr(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      Pattern pattern = this.we.getBlockPattern(player, args.getString(0));
      double radius = Math.max((double)1.0F, args.getDouble(1));
      this.we.checkMaxRadius(radius);
      int depth = args.argsLength() > 2 ? Math.max(1, args.getInteger(2)) : 1;
      Vector pos = session.getPlacementPosition(player);
      int affected = 0;
      if (pattern instanceof SingleBlockPattern) {
         affected = editSession.fillXZ(pos, ((SingleBlockPattern)pattern).getBlock(), radius, depth, true);
      } else {
         affected = editSession.fillXZ(pos, pattern, radius, depth, true);
      }

      player.print(affected + " block(s) have been created.");
   }

   @Command(
      aliases = {"/drain"},
      usage = "<radius>",
      desc = "Drain a pool",
      min = 1,
      max = 1
   )
   @CommandPermissions({"worldedit.drain"})
   @Logging(Logging.LogMode.PLACEMENT)
   public void drain(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      double radius = Math.max((double)0.0F, args.getDouble(0));
      this.we.checkMaxRadius(radius);
      int affected = editSession.drainArea(session.getPlacementPosition(player), radius);
      player.print(affected + " block(s) have been changed.");
   }

   @Command(
      aliases = {"/fixlava", "fixlava"},
      usage = "<radius>",
      desc = "Fix lava to be stationary",
      min = 1,
      max = 1
   )
   @CommandPermissions({"worldedit.fixlava"})
   @Logging(Logging.LogMode.PLACEMENT)
   public void fixLava(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      double radius = Math.max((double)0.0F, args.getDouble(0));
      this.we.checkMaxRadius(radius);
      int affected = editSession.fixLiquid(session.getPlacementPosition(player), radius, 10, 11);
      player.print(affected + " block(s) have been changed.");
   }

   @Command(
      aliases = {"/fixwater", "fixwater"},
      usage = "<radius>",
      desc = "Fix water to be stationary",
      min = 1,
      max = 1
   )
   @CommandPermissions({"worldedit.fixwater"})
   @Logging(Logging.LogMode.PLACEMENT)
   public void fixWater(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      double radius = Math.max((double)0.0F, args.getDouble(0));
      this.we.checkMaxRadius(radius);
      int affected = editSession.fixLiquid(session.getPlacementPosition(player), radius, 8, 9);
      player.print(affected + " block(s) have been changed.");
   }

   @Command(
      aliases = {"/removeabove", "removeabove"},
      usage = "[size] [height]",
      desc = "Remove blocks above your head.",
      min = 0,
      max = 2
   )
   @CommandPermissions({"worldedit.removeabove"})
   @Logging(Logging.LogMode.PLACEMENT)
   public void removeAbove(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      int size = args.argsLength() > 0 ? Math.max(1, args.getInteger(0)) : 1;
      this.we.checkMaxRadius((double)size);
      LocalWorld world = player.getWorld();
      int height = args.argsLength() > 1 ? Math.min(world.getMaxY() + 1, args.getInteger(1) + 2) : world.getMaxY() + 1;
      int affected = editSession.removeAbove(session.getPlacementPosition(player), size, height);
      player.print(affected + " block(s) have been removed.");
   }

   @Command(
      aliases = {"/removebelow", "removebelow"},
      usage = "[size] [height]",
      desc = "Remove blocks below you.",
      min = 0,
      max = 2
   )
   @CommandPermissions({"worldedit.removebelow"})
   @Logging(Logging.LogMode.PLACEMENT)
   public void removeBelow(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      int size = args.argsLength() > 0 ? Math.max(1, args.getInteger(0)) : 1;
      this.we.checkMaxRadius((double)size);
      LocalWorld world = player.getWorld();
      int height = args.argsLength() > 1 ? Math.min(world.getMaxY() + 1, args.getInteger(1) + 2) : world.getMaxY() + 1;
      int affected = editSession.removeBelow(session.getPlacementPosition(player), size, height);
      player.print(affected + " block(s) have been removed.");
   }

   @Command(
      aliases = {"/removenear", "removenear"},
      usage = "<block> [size]",
      desc = "Remove blocks near you.",
      min = 1,
      max = 2
   )
   @CommandPermissions({"worldedit.removenear"})
   @Logging(Logging.LogMode.PLACEMENT)
   public void removeNear(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      BaseBlock block = this.we.getBlock(player, args.getString(0), true);
      int size = Math.max(1, args.getInteger(1, 50));
      this.we.checkMaxRadius((double)size);
      int affected = editSession.removeNear(session.getPlacementPosition(player), block.getType(), size);
      player.print(affected + " block(s) have been removed.");
   }

   @Command(
      aliases = {"/replacenear", "replacenear"},
      usage = "<size> <from-id> <to-id>",
      desc = "Replace nearby blocks",
      flags = "f",
      min = 3,
      max = 3
   )
   @CommandPermissions({"worldedit.replacenear"})
   @Logging(Logging.LogMode.PLACEMENT)
   public void replaceNear(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      int size = Math.max(1, args.getInteger(0));
      Set<BaseBlock> from;
      Pattern to;
      if (args.argsLength() == 2) {
         from = null;
         to = this.we.getBlockPattern(player, args.getString(1));
      } else {
         from = this.we.getBlocks(player, args.getString(1), true, !args.hasFlag('f'));
         to = this.we.getBlockPattern(player, args.getString(2));
      }

      Vector base = session.getPlacementPosition(player);
      Vector min = base.subtract(size, size, size);
      Vector max = base.add(size, size, size);
      Region region = new CuboidRegion(player.getWorld(), min, max);
      int affected;
      if (to instanceof SingleBlockPattern) {
         affected = editSession.replaceBlocks(region, from, ((SingleBlockPattern)to).getBlock());
      } else {
         affected = editSession.replaceBlocks(region, from, to);
      }

      player.print(affected + " block(s) have been replaced.");
   }

   @Command(
      aliases = {"/snow", "snow"},
      usage = "[radius]",
      desc = "Simulates snow",
      min = 0,
      max = 1
   )
   @CommandPermissions({"worldedit.snow"})
   @Logging(Logging.LogMode.PLACEMENT)
   public void snow(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      double size = args.argsLength() > 0 ? Math.max((double)1.0F, args.getDouble(0)) : (double)10.0F;
      int affected = editSession.simulateSnow(session.getPlacementPosition(player), size);
      player.print(affected + " surfaces covered. Let it snow~");
   }

   @Command(
      aliases = {"/thaw", "thaw"},
      usage = "[radius]",
      desc = "Thaws the area",
      min = 0,
      max = 1
   )
   @CommandPermissions({"worldedit.thaw"})
   @Logging(Logging.LogMode.PLACEMENT)
   public void thaw(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      double size = args.argsLength() > 0 ? Math.max((double)1.0F, args.getDouble(0)) : (double)10.0F;
      int affected = editSession.thaw(session.getPlacementPosition(player), size);
      player.print(affected + " surfaces thawed.");
   }

   @Command(
      aliases = {"/green", "green"},
      usage = "[radius]",
      desc = "Greens the area",
      min = 0,
      max = 1
   )
   @CommandPermissions({"worldedit.green"})
   @Logging(Logging.LogMode.PLACEMENT)
   public void green(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      double size = args.argsLength() > 0 ? Math.max((double)1.0F, args.getDouble(0)) : (double)10.0F;
      int affected = editSession.green(session.getPlacementPosition(player), size);
      player.print(affected + " surfaces greened.");
   }

   @Command(
      aliases = {"/ex", "/ext", "/extinguish", "ex", "ext", "extinguish"},
      usage = "[radius]",
      desc = "Extinguish nearby fire",
      min = 0,
      max = 1
   )
   @CommandPermissions({"worldedit.extinguish"})
   @Logging(Logging.LogMode.PLACEMENT)
   public void extinguish(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      LocalConfiguration config = this.we.getConfiguration();
      int defaultRadius = config.maxRadius != -1 ? Math.min(40, config.maxRadius) : 40;
      int size = args.argsLength() > 0 ? Math.max(1, args.getInteger(0)) : defaultRadius;
      this.we.checkMaxRadius((double)size);
      int affected = editSession.removeNear(session.getPlacementPosition(player), 51, size);
      player.print(affected + " block(s) have been removed.");
   }

   @Command(
      aliases = {"butcher"},
      usage = "[radius]",
      flags = "plangbf",
      desc = "Kill all or nearby mobs",
      help = "Kills nearby mobs, based on radius, if none is given uses default in configuration.\nFlags:  -p also kills pets.\n  -n also kills NPCs.\n  -g also kills Golems.\n  -a also kills animals.\n  -b also kills ambient mobs.\n  -f compounds all previous flags.\n  -l strikes lightning on each killed mob.",
      min = 0,
      max = 1
   )
   @CommandPermissions({"worldedit.butcher"})
   @Logging(Logging.LogMode.PLACEMENT)
   @Console
   public void butcher(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      LocalConfiguration config = this.we.getConfiguration();
      int radius = config.butcherDefaultRadius;
      if (args.argsLength() > 0) {
         radius = args.getInteger(0);
         if (config.butcherMaxRadius != -1) {
            if (radius == -1) {
               radius = config.butcherMaxRadius;
            } else {
               radius = Math.min(radius, config.butcherMaxRadius);
            }
         }
      }

      FlagContainer flags = new FlagContainer(player);
      flags.or(31, args.hasFlag('f'));
      flags.or(1, args.hasFlag('p'), "worldedit.butcher.pets");
      flags.or(2, args.hasFlag('n'), "worldedit.butcher.npcs");
      flags.or(8, args.hasFlag('g'), "worldedit.butcher.golems");
      flags.or(4, args.hasFlag('a'), "worldedit.butcher.animals");
      flags.or(16, args.hasFlag('b'), "worldedit.butcher.ambient");
      flags.or(1048576, args.hasFlag('l'), "worldedit.butcher.lightning");
      int killed;
      if (player.isPlayer()) {
         killed = player.getWorld().killMobs(session.getPlacementPosition(player), (double)radius, flags.flags);
      } else {
         killed = 0;

         for(LocalWorld world : this.we.getServer().getWorlds()) {
            killed += world.killMobs(new Vector(), (double)radius, flags.flags);
         }
      }

      if (radius < 0) {
         player.print("Killed " + killed + " mobs.");
      } else {
         player.print("Killed " + killed + " mobs in a radius of " + radius + ".");
      }

   }

   @Command(
      aliases = {"remove", "rem", "rement"},
      usage = "<type> <radius>",
      desc = "Remove all entities of a type",
      min = 2,
      max = 2
   )
   @CommandPermissions({"worldedit.remove"})
   @Logging(Logging.LogMode.PLACEMENT)
   @Console
   public void remove(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      String typeStr = args.getString(0);
      int radius = args.getInteger(1);
      if (radius < -1) {
         player.printError("Use -1 to remove all entities in loaded chunks");
      } else {
         EntityType type = null;
         if (typeStr.matches("all")) {
            type = EntityType.ALL;
         } else if (typeStr.matches("projectiles?|arrows?")) {
            type = EntityType.PROJECTILES;
         } else if (!typeStr.matches("items?") && !typeStr.matches("drops?")) {
            if (typeStr.matches("falling(blocks?|sand|gravel)")) {
               type = EntityType.FALLING_BLOCKS;
            } else if (!typeStr.matches("paintings?") && !typeStr.matches("art")) {
               if (typeStr.matches("(item)frames?")) {
                  type = EntityType.ITEM_FRAMES;
               } else if (typeStr.matches("boats?")) {
                  type = EntityType.BOATS;
               } else if (!typeStr.matches("minecarts?") && !typeStr.matches("carts?")) {
                  if (typeStr.matches("tnt")) {
                     type = EntityType.TNT;
                  } else {
                     if (!typeStr.matches("xp")) {
                        player.printError("Acceptable types: projectiles, items, paintings, itemframes, boats, minecarts, tnt, xp, or all");
                        return;
                     }

                     type = EntityType.XP_ORBS;
                  }
               } else {
                  type = EntityType.MINECARTS;
               }
            } else {
               type = EntityType.PAINTINGS;
            }
         } else {
            type = EntityType.ITEMS;
         }

         int removed = 0;
         if (player.isPlayer()) {
            Vector origin = session.getPlacementPosition(player);
            removed = player.getWorld().removeEntities(type, origin, radius);
         } else {
            for(LocalWorld world : this.we.getServer().getWorlds()) {
               removed += world.removeEntities(type, new Vector(), radius);
            }
         }

         player.print("Marked " + removed + " entit(ies) for removal.");
      }
   }

   @Command(
      aliases = {"/help"},
      usage = "[<command>]",
      desc = "Displays help for the given command or lists all commands.",
      min = 0,
      max = -1
   )
   @Console
   @CommandPermissions({"worldedit.help"})
   public void help(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
      help(args, this.we, session, player, editSession);
   }

   public static void help(CommandContext args, WorldEdit we, LocalSession session, LocalPlayer player, EditSession editSession) {
      CommandsManager<LocalPlayer> commandsManager = we.getCommandsManager();
      if (args.argsLength() == 0) {
         SortedSet<String> commands = new TreeSet(new Comparator() {
            public int compare(String o1, String o2) {
               int ret = o1.replaceAll("/", "").compareToIgnoreCase(o2.replaceAll("/", ""));
               return ret == 0 ? o1.compareToIgnoreCase(o2) : ret;
            }
         });
         commands.addAll(commandsManager.getCommands().keySet());
         StringBuilder sb = new StringBuilder();
         boolean first = true;

         for(String command : commands) {
            if (!first) {
               sb.append(", ");
            }

            sb.append('/');
            sb.append(command);
            first = false;
         }

         player.print(sb.toString());
      } else {
         String command = args.getJoinedStrings(0).replaceAll("/", "");
         String helpMessage = (String)commandsManager.getHelpMessages().get(command);
         if (helpMessage == null) {
            player.printError("Unknown command '" + command + "'.");
         } else {
            player.print(helpMessage);
         }
      }
   }

   public static class FlagContainer {
      private final LocalPlayer player;
      public int flags = 0;

      public FlagContainer(LocalPlayer player) {
         this.player = player;
      }

      public void or(int flag, boolean on) {
         if (on) {
            this.flags |= flag;
         }

      }

      public void or(int flag, boolean on, String permission) {
         this.or(flag, on);
         if ((this.flags & flag) != 0 && !this.player.hasPermission(permission)) {
            this.flags &= ~flag;
         }

      }
   }
}
