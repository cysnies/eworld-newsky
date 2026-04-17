package com.sk89q.worldedit;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.minecraft.util.commands.CommandUsageException;
import com.sk89q.minecraft.util.commands.CommandsManager;
import com.sk89q.minecraft.util.commands.Console;
import com.sk89q.minecraft.util.commands.Logging;
import com.sk89q.minecraft.util.commands.MissingNestedCommandException;
import com.sk89q.minecraft.util.commands.SimpleInjector;
import com.sk89q.minecraft.util.commands.UnhandledCommandException;
import com.sk89q.minecraft.util.commands.WrappedCommandException;
import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.blocks.ClothColor;
import com.sk89q.worldedit.blocks.ItemType;
import com.sk89q.worldedit.blocks.MobSpawnerBlock;
import com.sk89q.worldedit.blocks.NoteBlock;
import com.sk89q.worldedit.blocks.SignBlock;
import com.sk89q.worldedit.blocks.SkullBlock;
import com.sk89q.worldedit.commands.BiomeCommands;
import com.sk89q.worldedit.commands.ChunkCommands;
import com.sk89q.worldedit.commands.ClipboardCommands;
import com.sk89q.worldedit.commands.GeneralCommands;
import com.sk89q.worldedit.commands.GenerationCommands;
import com.sk89q.worldedit.commands.HistoryCommands;
import com.sk89q.worldedit.commands.InsufficientArgumentsException;
import com.sk89q.worldedit.commands.NavigationCommands;
import com.sk89q.worldedit.commands.RegionCommands;
import com.sk89q.worldedit.commands.ScriptingCommands;
import com.sk89q.worldedit.commands.SelectionCommands;
import com.sk89q.worldedit.commands.SnapshotUtilCommands;
import com.sk89q.worldedit.commands.ToolCommands;
import com.sk89q.worldedit.commands.ToolUtilCommands;
import com.sk89q.worldedit.commands.UtilityCommands;
import com.sk89q.worldedit.masks.BiomeTypeMask;
import com.sk89q.worldedit.masks.BlockMask;
import com.sk89q.worldedit.masks.CombinedMask;
import com.sk89q.worldedit.masks.DynamicRegionMask;
import com.sk89q.worldedit.masks.ExistingBlockMask;
import com.sk89q.worldedit.masks.InvertedMask;
import com.sk89q.worldedit.masks.Mask;
import com.sk89q.worldedit.masks.RandomMask;
import com.sk89q.worldedit.masks.RegionMask;
import com.sk89q.worldedit.masks.UnderOverlayMask;
import com.sk89q.worldedit.patterns.BlockChance;
import com.sk89q.worldedit.patterns.ClipboardPattern;
import com.sk89q.worldedit.patterns.RandomFillPattern;
import com.sk89q.worldedit.patterns.SingleBlockPattern;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.scripting.CraftScriptContext;
import com.sk89q.worldedit.scripting.CraftScriptEngine;
import com.sk89q.worldedit.scripting.RhinoCraftScriptEngine;
import com.sk89q.worldedit.tools.BlockTool;
import com.sk89q.worldedit.tools.DoubleActionBlockTool;
import com.sk89q.worldedit.tools.DoubleActionTraceTool;
import com.sk89q.worldedit.tools.Tool;
import com.sk89q.worldedit.tools.TraceTool;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.script.ScriptException;

public class WorldEdit {
   public static final Logger logger = Logger.getLogger("Minecraft.WorldEdit");
   public final Logger commandLogger = Logger.getLogger("Minecraft.WorldEdit.CommandLogger");
   private static WorldEdit instance;
   private static String version;
   private final ServerInterface server;
   private final LocalConfiguration config;
   private final CommandsManager commands;
   private EditSessionFactory editSessionFactory = new EditSessionFactory();
   private final HashMap sessions = new HashMap();
   private static final Pattern numberFormatExceptionPattern;

   public WorldEdit(ServerInterface server, final LocalConfiguration config) {
      instance = this;
      this.server = server;
      this.config = config;
      if (!config.logFile.equals("")) {
         try {
            FileHandler logFileHandler = new FileHandler((new File(config.getWorkingDirectory(), config.logFile)).getAbsolutePath(), true);
            logFileHandler.setFormatter(new LogFormat());
            this.commandLogger.addHandler(logFileHandler);
         } catch (IOException e) {
            logger.log(Level.WARNING, "Could not use command log file " + config.logFile + ": " + e.getMessage());
         }
      }

      this.commands = new CommandsManager() {
         protected void checkPermission(LocalPlayer player, Method method) throws CommandException {
            if (!player.isPlayer() && !method.isAnnotationPresent(Console.class)) {
               throw new UnhandledCommandException();
            } else {
               super.checkPermission(player, method);
            }
         }

         public boolean hasPermission(LocalPlayer player, String perm) {
            return player.hasPermission(perm);
         }

         public void invokeMethod(Method parent, String[] args, LocalPlayer player, Method method, Object instance, Object[] methodArgs, int level) throws CommandException {
            if (config.logCommands) {
               Logging loggingAnnotation = (Logging)method.getAnnotation(Logging.class);
               Logging.LogMode logMode;
               if (loggingAnnotation == null) {
                  logMode = null;
               } else {
                  logMode = loggingAnnotation.value();
               }

               String msg = "WorldEdit: " + player.getName();
               if (player.isPlayer()) {
                  msg = msg + " (in \"" + player.getWorld().getName() + "\")";
               }

               msg = msg + ": " + StringUtil.joinString(args, " ");
               if (logMode != null && player.isPlayer()) {
                  Vector position = player.getPosition();
                  LocalSession session = WorldEdit.this.getSession(player);
                  switch (logMode) {
                     case PLACEMENT:
                        try {
                           position = session.getPlacementPosition(player);
                        } catch (IncompleteRegionException var15) {
                           break;
                        }
                     case POSITION:
                        msg = msg + " - Position: " + position;
                        break;
                     case ALL:
                        msg = msg + " - Position: " + position;
                     case ORIENTATION_REGION:
                        msg = msg + " - Orientation: " + player.getCardinalDirection().name();
                     case REGION:
                        try {
                           msg = msg + " - Region: " + session.getSelection(player.getWorld());
                        } catch (IncompleteRegionException var14) {
                        }
                  }
               }

               WorldEdit.this.commandLogger.info(msg);
            }

            super.invokeMethod(parent, args, player, method, instance, methodArgs, level);
         }
      };
      this.commands.setInjector(new SimpleInjector(new Object[]{this}));
      this.reg(BiomeCommands.class);
      this.reg(ChunkCommands.class);
      this.reg(ClipboardCommands.class);
      this.reg(GeneralCommands.class);
      this.reg(GenerationCommands.class);
      this.reg(HistoryCommands.class);
      this.reg(NavigationCommands.class);
      this.reg(RegionCommands.class);
      this.reg(ScriptingCommands.class);
      this.reg(SelectionCommands.class);
      this.reg(SnapshotUtilCommands.class);
      this.reg(ToolUtilCommands.class);
      this.reg(ToolCommands.class);
      this.reg(UtilityCommands.class);
   }

   private void reg(Class clazz) {
      this.server.onCommandRegistration(this.commands.registerAndReturn(clazz), this.commands);
   }

   public static WorldEdit getInstance() {
      return instance;
   }

   public LocalSession getSession(String player) {
      return (LocalSession)this.sessions.get(player);
   }

   public LocalSession getSession(LocalPlayer player) {
      synchronized(this.sessions) {
         LocalSession session;
         if (this.sessions.containsKey(player.getName())) {
            session = (LocalSession)this.sessions.get(player.getName());
         } else {
            session = new LocalSession(this.config);
            session.setBlockChangeLimit(this.config.defaultChangeLimit);
            this.sessions.put(player.getName(), session);
         }

         int currentChangeLimit = session.getBlockChangeLimit();
         if (!player.hasPermission("worldedit.limit.unrestricted") && this.config.maxChangeLimit > -1) {
            if (this.config.defaultChangeLimit < 0) {
               if (currentChangeLimit < 0 || currentChangeLimit > this.config.maxChangeLimit) {
                  session.setBlockChangeLimit(this.config.maxChangeLimit);
               }
            } else {
               int maxChangeLimit = this.config.maxChangeLimit;
               if (currentChangeLimit == -1 || currentChangeLimit > maxChangeLimit) {
                  session.setBlockChangeLimit(maxChangeLimit);
               }
            }
         }

         session.setUseInventory(this.config.useInventory && (!this.config.useInventoryOverride || !player.hasPermission("worldedit.inventory.unrestricted") && (!this.config.useInventoryCreativeOverride || !player.hasCreativeMode())));
         return session;
      }
   }

   public boolean hasSession(LocalPlayer player) {
      synchronized(this.sessions) {
         return this.sessions.containsKey(player.getName());
      }
   }

   public BaseBlock getBlock(LocalPlayer player, String arg, boolean allAllowed) throws UnknownItemException, DisallowedItemException {
      return this.getBlock(player, arg, allAllowed, false);
   }

   public BaseBlock getBlock(LocalPlayer player, String arg, boolean allAllowed, boolean allowNoData) throws UnknownItemException, DisallowedItemException {
      arg = arg.replace("_", " ");
      arg = arg.replace(";", "|");
      String[] blockAndExtraData = arg.split("\\|");
      String[] typeAndData = blockAndExtraData[0].split(":", 2);
      String testID = typeAndData[0];
      int blockId = -1;
      int data = -1;

      BlockType blockType;
      try {
         blockId = Integer.parseInt(testID);
         blockType = BlockType.fromID(blockId);
      } catch (NumberFormatException var20) {
         blockType = BlockType.lookup(testID);
         if (blockType == null) {
            int t = this.server.resolveItem(testID);
            if (t > 0) {
               blockType = BlockType.fromID(t);
               blockId = t;
            }
         }
      }

      if (blockId == -1 && blockType == null) {
         ClothColor col = ClothColor.lookup(testID);
         if (col == null) {
            throw new UnknownItemException(arg);
         }

         blockType = BlockType.CLOTH;
         data = col.getID();
      }

      if (blockId == -1) {
         blockId = blockType.getID();
      }

      if (!player.getWorld().isValidBlockType(blockId)) {
         throw new UnknownItemException(arg);
      } else {
         if (data == -1) {
            try {
               data = typeAndData.length > 1 && typeAndData[1].length() > 0 ? Integer.parseInt(typeAndData[1]) : (allowNoData ? -1 : 0);
               if (data > 15 && !this.config.allowExtraDataValues || data < 0 && (!allAllowed || data != -1)) {
                  data = 0;
               }
            } catch (NumberFormatException var19) {
               if (blockType == null) {
                  throw new InvalidItemException(arg, "Unknown data value '" + typeAndData[1] + "'");
               }

               label151:
               switch (blockType) {
                  case CLOTH:
                     ClothColor col = ClothColor.lookup(typeAndData[1]);
                     if (col == null) {
                        throw new InvalidItemException(arg, "Unknown cloth color '" + typeAndData[1] + "'");
                     }

                     data = col.getID();
                     break;
                  case STEP:
                  case DOUBLE_STEP:
                     BlockType dataType = BlockType.lookup(typeAndData[1]);
                     if (dataType == null) {
                        throw new InvalidItemException(arg, "Unknown step type '" + typeAndData[1] + "'");
                     }

                     switch (dataType) {
                        case STONE:
                           data = 0;
                           break label151;
                        case SANDSTONE:
                           data = 1;
                           break label151;
                        case WOOD:
                           data = 2;
                           break label151;
                        case COBBLESTONE:
                           data = 3;
                           break label151;
                        case BRICK:
                           data = 4;
                           break label151;
                        case STONE_BRICK:
                           data = 5;
                           break label151;
                        case NETHER_BRICK:
                           data = 6;
                           break label151;
                        case QUARTZ_BLOCK:
                           data = 7;
                           break label151;
                        default:
                           throw new InvalidItemException(arg, "Invalid step type '" + typeAndData[1] + "'");
                     }
                  default:
                     throw new InvalidItemException(arg, "Unknown data value '" + typeAndData[1] + "'");
               }
            }
         }

         if (!allAllowed && !player.hasPermission("worldedit.anyblock") && this.config.disallowedBlocks.contains(blockId)) {
            throw new DisallowedItemException(arg);
         } else if (blockType == null) {
            return new BaseBlock(blockId, data);
         } else {
            switch (blockType) {
               case SIGN_POST:
               case WALL_SIGN:
                  String[] text = new String[4];
                  text[0] = blockAndExtraData.length > 1 ? blockAndExtraData[1] : "";
                  text[1] = blockAndExtraData.length > 2 ? blockAndExtraData[2] : "";
                  text[2] = blockAndExtraData.length > 3 ? blockAndExtraData[3] : "";
                  text[3] = blockAndExtraData.length > 4 ? blockAndExtraData[4] : "";
                  return new SignBlock(blockType.getID(), data, text);
               case MOB_SPAWNER:
                  if (blockAndExtraData.length <= 1) {
                     return new MobSpawnerBlock(data, MobType.PIG.getName());
                  } else {
                     String mobName = blockAndExtraData[1];

                     for(MobType mobType : MobType.values()) {
                        if (mobType.getName().toLowerCase().equals(mobName.toLowerCase())) {
                           mobName = mobType.getName();
                           break;
                        }
                     }

                     if (!this.server.isValidMobType(mobName)) {
                        throw new InvalidItemException(arg, "Unknown mob type '" + mobName + "'");
                     } else {
                        return new MobSpawnerBlock(data, mobName);
                     }
                  }
               case NOTE_BLOCK:
                  if (blockAndExtraData.length > 1) {
                     byte note = Byte.parseByte(blockAndExtraData[1]);
                     if (note >= 0 && note <= 24) {
                        return new NoteBlock(data, note);
                     }

                     throw new InvalidItemException(arg, "Out of range note value: '" + blockAndExtraData[1] + "'");
                  }

                  return new NoteBlock(data, (byte)0);
               case HEAD:
                  if (blockAndExtraData.length > 1) {
                     byte rot = 0;
                     String type = "";

                     try {
                        rot = Byte.parseByte(blockAndExtraData[1]);
                     } catch (NumberFormatException var18) {
                        type = blockAndExtraData[1];
                        if (blockAndExtraData.length > 2) {
                           try {
                              rot = Byte.parseByte(blockAndExtraData[2]);
                           } catch (NumberFormatException var17) {
                              throw new InvalidItemException(arg, "Second part of skull metadata should be a number.");
                           }
                        }
                     }

                     byte skullType = 0;
                     if (!type.isEmpty()) {
                        if (type.equalsIgnoreCase("skeleton")) {
                           skullType = 0;
                        } else if (type.equalsIgnoreCase("wither")) {
                           skullType = 1;
                        } else if (type.equalsIgnoreCase("zombie")) {
                           skullType = 2;
                        } else if (type.equalsIgnoreCase("creeper")) {
                           skullType = 4;
                        } else {
                           skullType = 3;
                        }
                     }

                     if (skullType == 3) {
                        return new SkullBlock(data, rot, type.replace(" ", "_"));
                     }

                     return new SkullBlock(data, skullType, rot);
                  }

                  return new SkullBlock(data);
               default:
                  return new BaseBlock(blockId, data);
            }
         }
      }
   }

   public BaseBlock getBlock(LocalPlayer player, String id) throws UnknownItemException, DisallowedItemException {
      return this.getBlock(player, id, false);
   }

   public Set getBlocks(LocalPlayer player, String list, boolean allAllowed, boolean allowNoData) throws DisallowedItemException, UnknownItemException {
      String[] items = list.split(",");
      Set<BaseBlock> blocks = new HashSet();

      for(String id : items) {
         blocks.add(this.getBlock(player, id, allAllowed, allowNoData));
      }

      return blocks;
   }

   public Set getBlocks(LocalPlayer player, String list, boolean allAllowed) throws DisallowedItemException, UnknownItemException {
      return this.getBlocks(player, list, allAllowed, false);
   }

   public Set getBlocks(LocalPlayer player, String list) throws DisallowedItemException, UnknownItemException {
      return this.getBlocks(player, list, false);
   }

   public com.sk89q.worldedit.patterns.Pattern getBlockPattern(LocalPlayer player, String patternString) throws UnknownItemException, DisallowedItemException {
      String[] items = patternString.split(",");
      if (patternString.charAt(0) == '#') {
         if (!patternString.equals("#clipboard") && !patternString.equals("#copy")) {
            throw new UnknownItemException(patternString);
         } else {
            LocalSession session = this.getSession(player);

            CuboidClipboard clipboard;
            try {
               clipboard = session.getClipboard();
            } catch (EmptyClipboardException var13) {
               player.printError("Copy a selection first with //copy.");
               throw new UnknownItemException("#clipboard");
            }

            return new ClipboardPattern(clipboard);
         }
      } else if (items.length == 1) {
         return new SingleBlockPattern(this.getBlock(player, items[0]));
      } else {
         List<BlockChance> blockChances = new ArrayList();

         for(String s : items) {
            BaseBlock block;
            double chance;
            if (s.matches("[0-9]+(\\.[0-9]*)?%.*")) {
               String[] p = s.split("%");
               chance = Double.parseDouble(p[0]);
               block = this.getBlock(player, p[1]);
            } else {
               chance = (double)1.0F;
               block = this.getBlock(player, s);
            }

            blockChances.add(new BlockChance(block, chance));
         }

         return new RandomFillPattern(blockChances);
      }
   }

   public Mask getBlockMask(LocalPlayer player, LocalSession session, String maskString) throws WorldEditException {
      List<Mask> masks = new ArrayList();

      for(String component : maskString.split(" ")) {
         if (component.length() != 0) {
            Mask current = this.getBlockMaskComponent(player, session, masks, component);
            masks.add(current);
         }
      }

      switch (masks.size()) {
         case 0:
            return null;
         case 1:
            return (Mask)masks.get(0);
         default:
            return new CombinedMask(masks);
      }
   }

   private Mask getBlockMaskComponent(LocalPlayer player, LocalSession session, List masks, String component) throws WorldEditException {
      char firstChar = component.charAt(0);
      switch (firstChar) {
         case '!':
            if (component.length() > 1) {
               return new InvertedMask(this.getBlockMaskComponent(player, session, masks, component.substring(1)));
            }
         default:
            return new BlockMask(this.getBlocks(player, component, true, true));
         case '#':
            if (component.equalsIgnoreCase("#existing")) {
               return new ExistingBlockMask();
            } else {
               if (!component.equalsIgnoreCase("#dregion") && !component.equalsIgnoreCase("#dselection") && !component.equalsIgnoreCase("#dsel")) {
                  if (!component.equalsIgnoreCase("#selection") && !component.equalsIgnoreCase("#region") && !component.equalsIgnoreCase("#sel")) {
                     throw new UnknownItemException(component);
                  }

                  return new RegionMask(session.getSelection(player.getWorld()));
               }

               return new DynamicRegionMask();
            }
         case '$':
            Set<BiomeType> biomes = new HashSet();
            String[] biomesList = component.substring(1).split(",");

            for(String biomeName : biomesList) {
               BiomeType biome = this.server.getBiomes().get(biomeName);
               biomes.add(biome);
            }

            return new BiomeTypeMask(biomes);
         case '%':
            int i = Integer.parseInt(component.substring(1));
            return new RandomMask((double)i / (double)100.0F);
         case '<':
         case '>':
            Mask submask;
            if (component.length() > 1) {
               submask = this.getBlockMaskComponent(player, session, masks, component.substring(1));
            } else {
               submask = new ExistingBlockMask();
            }

            return new UnderOverlayMask(submask, firstChar == '>');
      }
   }

   public Set getBlockIDs(LocalPlayer player, String list, boolean allBlocksAllowed) throws UnknownItemException, DisallowedItemException {
      String[] items = list.split(",");
      Set<Integer> blocks = new HashSet();

      for(String s : items) {
         blocks.add(this.getBlock(player, s, allBlocksAllowed).getType());
      }

      return blocks;
   }

   public File getSafeSaveFile(LocalPlayer player, File dir, String filename, String defaultExt, String... extensions) throws FilenameException {
      return this.getSafeFile(player, dir, filename, defaultExt, extensions, true);
   }

   public File getSafeOpenFile(LocalPlayer player, File dir, String filename, String defaultExt, String... extensions) throws FilenameException {
      return this.getSafeFile(player, dir, filename, defaultExt, extensions, false);
   }

   private File getSafeFile(LocalPlayer player, File dir, String filename, String defaultExt, String[] extensions, boolean isSave) throws FilenameException {
      if (extensions != null && extensions.length == 1 && extensions[0] == null) {
         extensions = null;
      }

      File f;
      if (filename.equals("#")) {
         if (isSave) {
            f = player.openFileSaveDialog(extensions);
         } else {
            f = player.openFileOpenDialog(extensions);
         }

         if (f == null) {
            throw new FileSelectionAbortedException("No file selected");
         }
      } else {
         if (defaultExt != null && filename.lastIndexOf(46) == -1) {
            filename = filename + "." + defaultExt;
         }

         if (!filename.matches("^[A-Za-z0-9_\\- \\./\\\\'\\$@~!%\\^\\*\\(\\)\\[\\]\\+\\{\\},\\?]+\\.[A-Za-z0-9]+$")) {
            throw new InvalidFilenameException(filename, "Invalid characters or extension missing");
         }

         f = new File(dir, filename);
      }

      try {
         String filePath = f.getCanonicalPath();
         String dirPath = dir.getCanonicalPath();
         if (!filePath.substring(0, dirPath.length()).equals(dirPath) && !this.config.allowSymlinks) {
            throw new FilenameResolutionException(filename, "Path is outside allowable root");
         } else {
            return f;
         }
      } catch (IOException var10) {
         throw new FilenameResolutionException(filename, "Failed to resolve path");
      }
   }

   public int getMaximumPolygonalPoints(LocalPlayer player) {
      if (!player.hasPermission("worldedit.limit.unrestricted") && this.config.maxPolygonalPoints >= 0) {
         return this.config.defaultMaxPolygonalPoints < 0 ? this.config.maxPolygonalPoints : Math.min(this.config.defaultMaxPolygonalPoints, this.config.maxPolygonalPoints);
      } else {
         return this.config.defaultMaxPolygonalPoints;
      }
   }

   public int getMaximumPolyhedronPoints(LocalPlayer player) {
      if (!player.hasPermission("worldedit.limit.unrestricted") && this.config.maxPolyhedronPoints >= 0) {
         return this.config.defaultMaxPolyhedronPoints < 0 ? this.config.maxPolyhedronPoints : Math.min(this.config.defaultMaxPolyhedronPoints, this.config.maxPolyhedronPoints);
      } else {
         return this.config.defaultMaxPolyhedronPoints;
      }
   }

   public void checkMaxRadius(double radius) throws MaxRadiusException {
      if (this.config.maxRadius > 0 && radius > (double)this.config.maxRadius) {
         throw new MaxRadiusException();
      }
   }

   public void checkMaxBrushRadius(double radius) throws MaxBrushRadiusException {
      if (this.config.maxBrushRadius > 0 && radius > (double)this.config.maxBrushRadius) {
         throw new MaxBrushRadiusException();
      }
   }

   public File getWorkingDirectoryFile(String path) {
      File f = new File(path);
      return f.isAbsolute() ? f : new File(this.config.getWorkingDirectory(), path);
   }

   public static int divisorMod(int a, int n) {
      return (int)((double)a - (double)n * Math.floor(Math.floor((double)a) / (double)n));
   }

   public Vector getDirection(LocalPlayer player, String dirStr) throws UnknownDirectionException {
      dirStr = dirStr.toLowerCase();
      PlayerDirection dir = this.getPlayerDirection(player, dirStr);
      switch (dir) {
         case WEST:
         case EAST:
         case SOUTH:
         case NORTH:
         case UP:
         case DOWN:
            return dir.vector();
         default:
            throw new UnknownDirectionException(dir.name());
      }
   }

   private PlayerDirection getPlayerDirection(LocalPlayer player, String dirStr) throws UnknownDirectionException {
      PlayerDirection dir;
      switch (dirStr.charAt(0)) {
         case 'b':
            dir = player.getCardinalDirection(180);
            break;
         case 'c':
         case 'g':
         case 'h':
         case 'i':
         case 'j':
         case 'k':
         case 'o':
         case 'p':
         case 'q':
         case 't':
         case 'v':
         default:
            throw new UnknownDirectionException(dirStr);
         case 'd':
            dir = PlayerDirection.DOWN;
            break;
         case 'e':
            dir = PlayerDirection.EAST;
            break;
         case 'f':
         case 'm':
            dir = player.getCardinalDirection(0);
            break;
         case 'l':
            dir = player.getCardinalDirection(-90);
            break;
         case 'n':
            if (dirStr.indexOf(119) > 0) {
               return PlayerDirection.NORTH_WEST;
            }

            if (dirStr.indexOf(101) > 0) {
               return PlayerDirection.NORTH_EAST;
            }

            dir = PlayerDirection.NORTH;
            break;
         case 'r':
            dir = player.getCardinalDirection(90);
            break;
         case 's':
            if (dirStr.indexOf(119) > 0) {
               return PlayerDirection.SOUTH_WEST;
            }

            if (dirStr.indexOf(101) > 0) {
               return PlayerDirection.SOUTH_EAST;
            }

            dir = PlayerDirection.SOUTH;
            break;
         case 'u':
            dir = PlayerDirection.UP;
            break;
         case 'w':
            dir = PlayerDirection.WEST;
      }

      return dir;
   }

   public Vector getDiagonalDirection(LocalPlayer player, String dirStr) throws UnknownDirectionException {
      return this.getPlayerDirection(player, dirStr.toLowerCase()).vector();
   }

   public CuboidClipboard.FlipDirection getFlipDirection(LocalPlayer player, String dirStr) throws UnknownDirectionException {
      PlayerDirection dir = this.getPlayerDirection(player, dirStr);
      switch (dir) {
         case WEST:
         case EAST:
            return CuboidClipboard.FlipDirection.WEST_EAST;
         case SOUTH:
         case NORTH:
            return CuboidClipboard.FlipDirection.NORTH_SOUTH;
         case UP:
         case DOWN:
            return CuboidClipboard.FlipDirection.UP_DOWN;
         default:
            throw new UnknownDirectionException(dir.name());
      }
   }

   public void removeSession(LocalPlayer player) {
      synchronized(this.sessions) {
         this.sessions.remove(player.getName());
      }
   }

   public void clearSessions() {
      synchronized(this.sessions) {
         this.sessions.clear();
      }
   }

   public void flushBlockBag(LocalPlayer player, EditSession editSession) {
      BlockBag blockBag = editSession.getBlockBag();
      if (blockBag != null) {
         blockBag.flushChanges();
      }

      Map<Integer, Integer> missingBlocks = editSession.popMissingBlocks();
      if (missingBlocks.size() > 0) {
         StringBuilder str = new StringBuilder();
         str.append("Missing these blocks: ");
         int size = missingBlocks.size();
         int i = 0;

         for(Integer id : missingBlocks.keySet()) {
            BlockType type = BlockType.fromID(id);
            str.append(type != null ? type.getName() + " (" + id + ")" : id.toString());
            str.append(" [Amt: " + missingBlocks.get(id) + "]");
            ++i;
            if (i != size) {
               str.append(", ");
            }
         }

         player.printError(str.toString());
      }

   }

   public Map getCommands() {
      return this.commands.getCommands();
   }

   public CommandsManager getCommandsManager() {
      return this.commands;
   }

   /** @deprecated */
   @Deprecated
   public void handleDisconnect(LocalPlayer player) {
      this.forgetPlayer(player);
   }

   public void markExpire(LocalPlayer player) {
      synchronized(this.sessions) {
         LocalSession session = (LocalSession)this.sessions.get(player.getName());
         if (session != null) {
            session.update();
         }

      }
   }

   public void forgetPlayer(LocalPlayer player) {
      this.removeSession(player);
   }

   public void flushExpiredSessions(SessionCheck checker) {
      synchronized(this.sessions) {
         Iterator<Map.Entry<String, LocalSession>> it = this.sessions.entrySet().iterator();

         while(it.hasNext()) {
            Map.Entry<String, LocalSession> entry = (Map.Entry)it.next();
            if (((LocalSession)entry.getValue()).hasExpired() && !checker.isOnlinePlayer((String)entry.getKey())) {
               it.remove();
            }
         }

      }
   }

   public boolean handleArmSwing(LocalPlayer player) {
      if (player.getItemInHand() == this.config.navigationWand) {
         if (this.config.navigationWandMaxDistance <= 0) {
            return false;
         } else if (!player.hasPermission("worldedit.navigation.jumpto.tool")) {
            return false;
         } else {
            WorldVector pos = player.getSolidBlockTrace(this.config.navigationWandMaxDistance);
            if (pos != null) {
               player.findFreePosition(pos);
            } else {
               player.printError("No block in sight (or too far)!");
            }

            return true;
         }
      } else {
         LocalSession session = this.getSession(player);
         Tool tool = session.getTool(player.getItemInHand());
         if (tool != null && tool instanceof DoubleActionTraceTool && tool.canUse(player)) {
            ((DoubleActionTraceTool)tool).actSecondary(this.server, this.config, player, session);
            return true;
         } else {
            return false;
         }
      }
   }

   public boolean handleRightClick(LocalPlayer player) {
      if (player.getItemInHand() == this.config.navigationWand) {
         if (this.config.navigationWandMaxDistance <= 0) {
            return false;
         } else if (!player.hasPermission("worldedit.navigation.thru.tool")) {
            return false;
         } else {
            if (!player.passThroughForwardWall(40)) {
               player.printError("Nothing to pass through!");
            }

            return true;
         }
      } else {
         LocalSession session = this.getSession(player);
         Tool tool = session.getTool(player.getItemInHand());
         if (tool != null && tool instanceof TraceTool && tool.canUse(player)) {
            ((TraceTool)tool).actPrimary(this.server, this.config, player, session);
            return true;
         } else {
            return false;
         }
      }
   }

   public boolean handleBlockRightClick(LocalPlayer player, WorldVector clicked) {
      LocalSession session = this.getSession(player);
      if (player.getItemInHand() == this.config.wandItem) {
         if (!session.isToolControlEnabled()) {
            return false;
         } else if (!player.hasPermission("worldedit.selection.pos")) {
            return false;
         } else {
            RegionSelector selector = session.getRegionSelector(player.getWorld());
            if (selector.selectSecondary(clicked)) {
               selector.explainSecondarySelection(player, session, clicked);
            }

            return true;
         }
      } else {
         Tool tool = session.getTool(player.getItemInHand());
         if (tool != null && tool instanceof BlockTool && tool.canUse(player)) {
            ((BlockTool)tool).actPrimary(this.server, this.config, player, session, clicked);
            return true;
         } else {
            return false;
         }
      }
   }

   public boolean handleBlockLeftClick(LocalPlayer player, WorldVector clicked) {
      LocalSession session = this.getSession(player);
      if (player.getItemInHand() == this.config.wandItem) {
         if (!session.isToolControlEnabled()) {
            return false;
         } else if (!player.hasPermission("worldedit.selection.pos")) {
            return false;
         } else {
            RegionSelector selector = session.getRegionSelector(player.getWorld());
            if (selector.selectPrimary(clicked)) {
               selector.explainPrimarySelection(player, session, clicked);
            }

            return true;
         }
      } else {
         if (player.isHoldingPickAxe() && session.hasSuperPickAxe()) {
            BlockTool superPickaxe = session.getSuperPickaxe();
            if (superPickaxe != null && superPickaxe.canUse(player)) {
               return superPickaxe.actPrimary(this.server, this.config, player, session, clicked);
            }
         }

         Tool tool = session.getTool(player.getItemInHand());
         if (tool != null && tool instanceof DoubleActionBlockTool && tool.canUse(player)) {
            ((DoubleActionBlockTool)tool).actSecondary(this.server, this.config, player, session, clicked);
            return true;
         } else {
            return false;
         }
      }
   }

   public boolean handleCommand(LocalPlayer player, String[] split) {
      try {
         split = this.commandDetection(split);
         if (!this.commands.hasCommand(split[0])) {
            return false;
         }

         LocalSession session = this.getSession(player);
         EditSession editSession = session.createEditSession(player);
         editSession.enableQueue();
         session.tellVersion(player);
         long start = System.currentTimeMillis();
         boolean var44 = false;

         label311: {
            boolean var8;
            label330: {
               label331: {
                  label332: {
                     label333: {
                        try {
                           var44 = true;
                           this.commands.execute(split, player, session, player, editSession);
                           var44 = false;
                           break label311;
                        } catch (CommandPermissionsException var45) {
                           player.printError("You don't have permission to do this.");
                           var44 = false;
                        } catch (MissingNestedCommandException e) {
                           player.printError(e.getUsage());
                           var44 = false;
                           break label333;
                        } catch (CommandUsageException e) {
                           player.printError(e.getMessage());
                           player.printError(e.getUsage());
                           var44 = false;
                           break label332;
                        } catch (PlayerNeededException e) {
                           player.printError(e.getMessage());
                           var44 = false;
                           break label331;
                        } catch (WrappedCommandException e) {
                           throw e.getCause();
                        } catch (UnhandledCommandException var50) {
                           player.printError("Command could not be handled; invalid sender!");
                           var8 = false;
                           var44 = false;
                           break label330;
                        } finally {
                           if (var44) {
                              session.remember(editSession);
                              editSession.flushQueue();
                              if (this.config.profile) {
                                 long time = System.currentTimeMillis() - start;
                                 int changed = editSession.getBlockChangeCount();
                                 if (time > 0L) {
                                    double throughput = (double)changed / ((double)time / (double)1000.0F);
                                    player.printDebug((double)time / (double)1000.0F + "s elapsed (history: " + changed + " changed; " + Math.round(throughput) + " blocks/sec).");
                                 } else {
                                    player.printDebug((double)time / (double)1000.0F + "s elapsed.");
                                 }
                              }

                              this.flushBlockBag(player, editSession);
                           }
                        }

                        session.remember(editSession);
                        editSession.flushQueue();
                        if (this.config.profile) {
                           long time = System.currentTimeMillis() - start;
                           int changed = editSession.getBlockChangeCount();
                           if (time > 0L) {
                              double throughput = (double)changed / ((double)time / (double)1000.0F);
                              player.printDebug((double)time / (double)1000.0F + "s elapsed (history: " + changed + " changed; " + Math.round(throughput) + " blocks/sec).");
                           } else {
                              player.printDebug((double)time / (double)1000.0F + "s elapsed.");
                           }
                        }

                        this.flushBlockBag(player, editSession);
                        return true;
                     }

                     session.remember(editSession);
                     editSession.flushQueue();
                     if (this.config.profile) {
                        long time = System.currentTimeMillis() - start;
                        int changed = editSession.getBlockChangeCount();
                        if (time > 0L) {
                           double throughput = (double)changed / ((double)time / (double)1000.0F);
                           player.printDebug((double)time / (double)1000.0F + "s elapsed (history: " + changed + " changed; " + Math.round(throughput) + " blocks/sec).");
                        } else {
                           player.printDebug((double)time / (double)1000.0F + "s elapsed.");
                        }
                     }

                     this.flushBlockBag(player, editSession);
                     return true;
                  }

                  session.remember(editSession);
                  editSession.flushQueue();
                  if (this.config.profile) {
                     long time = System.currentTimeMillis() - start;
                     int changed = editSession.getBlockChangeCount();
                     if (time > 0L) {
                        double throughput = (double)changed / ((double)time / (double)1000.0F);
                        player.printDebug((double)time / (double)1000.0F + "s elapsed (history: " + changed + " changed; " + Math.round(throughput) + " blocks/sec).");
                     } else {
                        player.printDebug((double)time / (double)1000.0F + "s elapsed.");
                     }
                  }

                  this.flushBlockBag(player, editSession);
                  return true;
               }

               session.remember(editSession);
               editSession.flushQueue();
               if (this.config.profile) {
                  long time = System.currentTimeMillis() - start;
                  int changed = editSession.getBlockChangeCount();
                  if (time > 0L) {
                     double throughput = (double)changed / ((double)time / (double)1000.0F);
                     player.printDebug((double)time / (double)1000.0F + "s elapsed (history: " + changed + " changed; " + Math.round(throughput) + " blocks/sec).");
                  } else {
                     player.printDebug((double)time / (double)1000.0F + "s elapsed.");
                  }
               }

               this.flushBlockBag(player, editSession);
               return true;
            }

            session.remember(editSession);
            editSession.flushQueue();
            if (this.config.profile) {
               long time = System.currentTimeMillis() - start;
               int changed = editSession.getBlockChangeCount();
               if (time > 0L) {
                  double throughput = (double)changed / ((double)time / (double)1000.0F);
                  player.printDebug((double)time / (double)1000.0F + "s elapsed (history: " + changed + " changed; " + Math.round(throughput) + " blocks/sec).");
               } else {
                  player.printDebug((double)time / (double)1000.0F + "s elapsed.");
               }
            }

            this.flushBlockBag(player, editSession);
            return var8;
         }

         session.remember(editSession);
         editSession.flushQueue();
         if (this.config.profile) {
            long time = System.currentTimeMillis() - start;
            int changed = editSession.getBlockChangeCount();
            if (time > 0L) {
               double throughput = (double)changed / ((double)time / (double)1000.0F);
               player.printDebug((double)time / (double)1000.0F + "s elapsed (history: " + changed + " changed; " + Math.round(throughput) + " blocks/sec).");
            } else {
               player.printDebug((double)time / (double)1000.0F + "s elapsed.");
            }
         }

         this.flushBlockBag(player, editSession);
      } catch (NumberFormatException e) {
         Matcher matcher = numberFormatExceptionPattern.matcher(e.getMessage());
         if (matcher.matches()) {
            player.printError("Number expected; string \"" + matcher.group(1) + "\" given.");
         } else {
            player.printError("Number expected; string given.");
         }
      } catch (IncompleteRegionException var53) {
         player.printError("Make a region selection first.");
      } catch (UnknownItemException e) {
         player.printError("Block name '" + e.getID() + "' was not recognized.");
      } catch (InvalidItemException e) {
         player.printError(e.getMessage());
      } catch (DisallowedItemException e) {
         player.printError("Block '" + e.getID() + "' not allowed (see WorldEdit configuration).");
      } catch (MaxChangedBlocksException e) {
         player.printError("Max blocks changed in an operation reached (" + e.getBlockLimit() + ").");
      } catch (MaxBrushRadiusException var58) {
         player.printError("Maximum allowed brush size: " + this.config.maxBrushRadius);
      } catch (MaxRadiusException var59) {
         player.printError("Maximum allowed size: " + this.config.maxRadius);
      } catch (UnknownDirectionException e) {
         player.printError("Unknown direction: " + e.getDirection());
      } catch (InsufficientArgumentsException e) {
         player.printError(e.getMessage());
      } catch (EmptyClipboardException var62) {
         player.printError("Your clipboard is empty. Use //copy first.");
      } catch (InvalidFilenameException e) {
         player.printError("Filename '" + e.getFilename() + "' invalid: " + e.getMessage());
      } catch (FilenameResolutionException e) {
         player.printError("File '" + e.getFilename() + "' resolution error: " + e.getMessage());
      } catch (InvalidToolBindException e) {
         player.printError("Can't bind tool to " + ItemType.toHeldName(e.getItemId()) + ": " + e.getMessage());
      } catch (FileSelectionAbortedException var66) {
         player.printError("File selection aborted.");
      } catch (WorldEditException e) {
         player.printError(e.getMessage());
      } catch (Throwable excp) {
         player.printError("Please report this error: [See console]");
         player.printRaw(excp.getClass().getName() + ": " + excp.getMessage());
         excp.printStackTrace();
      }

      return true;
   }

   public String[] commandDetection(String[] split) {
      split[0] = split[0].substring(1);
      if (split[0].matches("^[^/].*\\.js$")) {
         String[] newSplit = new String[split.length + 1];
         System.arraycopy(split, 0, newSplit, 1, split.length);
         newSplit[0] = "cs";
         newSplit[1] = newSplit[1];
         split = newSplit;
      }

      String searchCmd = split[0].toLowerCase();
      if (!this.commands.hasCommand(searchCmd)) {
         if (this.config.noDoubleSlash && this.commands.hasCommand("/" + searchCmd)) {
            split[0] = "/" + split[0];
         } else if (split[0].length() >= 2 && split[0].charAt(0) == '/' && this.commands.hasCommand(searchCmd.substring(1))) {
            split[0] = split[0].substring(1);
         }
      }

      return split;
   }

   public void runScript(LocalPlayer player, File f, String[] args) throws WorldEditException {
      String filename = f.getPath();
      int index = filename.lastIndexOf(".");
      String ext = filename.substring(index + 1, filename.length());
      if (!ext.equalsIgnoreCase("js")) {
         player.printError("Only .js scripts are currently supported");
      } else {
         String script;
         try {
            InputStream file;
            if (!f.exists()) {
               file = WorldEdit.class.getResourceAsStream("craftscripts/" + filename);
               if (file == null) {
                  player.printError("Script does not exist: " + filename);
                  return;
               }
            } else {
               file = new FileInputStream(f);
            }

            DataInputStream in = new DataInputStream(file);
            byte[] data = new byte[in.available()];
            in.readFully(data);
            in.close();
            script = new String(data, 0, data.length, "utf-8");
         } catch (IOException e) {
            player.printError("Script read error: " + e.getMessage());
            return;
         }

         LocalSession session = this.getSession(player);
         CraftScriptContext scriptContext = new CraftScriptContext(this, this.server, this.config, session, player, args);
         CraftScriptEngine engine = null;

         try {
            engine = new RhinoCraftScriptEngine();
         } catch (NoClassDefFoundError var28) {
            player.printError("Failed to find an installed script engine.");
            player.printError("Please see http://wiki.sk89q.com/wiki/WorldEdit/Installation");
            return;
         }

         engine.setTimeLimit(this.config.scriptTimeout);
         Map<String, Object> vars = new HashMap();
         vars.put("argv", args);
         vars.put("context", scriptContext);
         vars.put("player", player);

         try {
            engine.evaluate(script, filename, vars);
         } catch (ScriptException e) {
            player.printError("Failed to execute:");
            player.printRaw(e.getMessage());
            e.printStackTrace();
         } catch (NumberFormatException e) {
            throw e;
         } catch (WorldEditException e) {
            throw e;
         } catch (Throwable e) {
            player.printError("Failed to execute (see console):");
            player.printRaw(e.getClass().getCanonicalName());
            e.printStackTrace();
         } finally {
            for(EditSession editSession : scriptContext.getEditSessions()) {
               editSession.flushQueue();
               session.remember(editSession);
            }

         }

      }
   }

   public LocalConfiguration getConfiguration() {
      return this.config;
   }

   public ServerInterface getServer() {
      return this.server;
   }

   public EditSessionFactory getEditSessionFactory() {
      return this.editSessionFactory;
   }

   public void setEditSessionFactory(EditSessionFactory factory) {
      if (factory == null) {
         throw new IllegalArgumentException("New EditSessionFactory may not be null");
      } else {
         logger.info("Accepted EditSessionFactory of type " + factory.getClass().getName() + " from " + factory.getClass().getPackage().getName());
         this.editSessionFactory = factory;
      }
   }

   public static String getVersion() {
      if (version != null) {
         return version;
      } else {
         Package p = WorldEdit.class.getPackage();
         if (p == null) {
            p = Package.getPackage("com.sk89q.worldedit");
         }

         if (p == null) {
            version = "(unknown)";
         } else {
            version = p.getImplementationVersion();
            if (version == null) {
               version = "(unknown)";
            }
         }

         return version;
      }
   }

   public static void setVersion(String version) {
      WorldEdit.version = version;
   }

   static {
      getVersion();
      numberFormatExceptionPattern = Pattern.compile("^For input string: \"(.*)\"$");
   }
}
