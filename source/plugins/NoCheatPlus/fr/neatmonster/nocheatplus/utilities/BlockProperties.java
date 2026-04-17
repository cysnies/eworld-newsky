package fr.neatmonster.nocheatplus.utilities;

import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.compat.blocks.BlockPropertiesSetup;
import fr.neatmonster.nocheatplus.compat.blocks.init.vanilla.VanillaBlocksFactory;
import fr.neatmonster.nocheatplus.config.RawConfigFile;
import fr.neatmonster.nocheatplus.config.WorldConfigProvider;
import fr.neatmonster.nocheatplus.logging.LogUtil;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.InputMismatchException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

public class BlockProperties {
   protected static final int maxBlocks = 4096;
   protected static final BlockProps[] blocks = new BlockProps[4096];
   protected static Map tools = new LinkedHashMap(50, 0.5F);
   public static final long indestructible = Long.MAX_VALUE;
   public static final ToolProps noTool;
   public static final ToolProps woodSword;
   public static final ToolProps woodSpade;
   public static final ToolProps woodPickaxe;
   public static final ToolProps woodAxe;
   public static final ToolProps stonePickaxe;
   public static final ToolProps ironPickaxe;
   public static final ToolProps diamondPickaxe;
   public static final long[] instantTimes;
   public static final long[] leafTimes;
   public static long[] glassTimes;
   public static final long[] gravelTimes;
   public static long[] railsTimes;
   public static final long[] woodTimes;
   public static final long[] ironTimes;
   public static final long[] diamondTimes;
   private static final long[] indestructibleTimes;
   public static final BlockProps instantType;
   public static final BlockProps glassType;
   public static final BlockProps gravelType;
   public static final BlockProps stoneType;
   public static final BlockProps woodType;
   public static final BlockProps brickType;
   public static final BlockProps coalType;
   public static final BlockProps ironType;
   public static final BlockProps diamondType;
   public static final BlockProps goldBlockType;
   public static final BlockProps ironBlockType;
   public static final BlockProps diamondBlockType;
   public static final BlockProps hugeMushroomType;
   public static final BlockProps leafType;
   public static final BlockProps sandType;
   public static final BlockProps leverType;
   public static final BlockProps sandStoneType;
   public static final BlockProps pumpkinType;
   public static final BlockProps chestType;
   public static final BlockProps woodDoorType;
   public static final BlockProps dispenserType;
   public static final BlockProps ironDoorType;
   private static final BlockProps indestructibleType;
   private static BlockProps defaultBlockProps;
   protected static final Material[] instantMat;
   private static BlockCache blockCache;
   private static PlayerLocation pLoc;
   protected static final long[] blockFlags;
   public static final long F_STAIRS = 1L;
   public static final long F_LIQUID = 2L;
   public static final long F_SOLID = 4L;
   public static final long F_IGN_PASSABLE = 8L;
   public static final long F_WATER = 16L;
   public static final long F_LAVA = 32L;
   public static final long F_HEIGHT150 = 64L;
   public static final long F_GROUND = 128L;
   public static final long F_HEIGHT100 = 256L;
   public static final long F_CLIMBABLE = 512L;
   public static final long F_VARIABLE = 1024L;
   public static final long F_XZ100 = 2048L;
   public static final long F_GROUND_HEIGHT = 4096L;
   public static final long F_HEIGHT_8SIM_DEC = 8192L;
   public static final long F_HEIGHT_8SIM_INC = 16384L;
   public static final long F_HEIGHT_8_INC = 32768L;
   public static final long F_RAILS = 65536L;
   private static final Map flagNameMap;
   private static final Map nameFlagMap;
   protected static float breakPenaltyInWater;
   protected static float breakPenaltyOffGround;

   public static void init(MCAccess mcAccess, WorldConfigProvider worldConfigProvider) {
      blockCache = mcAccess.getBlockCache((World)null);
      pLoc = new PlayerLocation(mcAccess, (BlockCache)null);

      try {
         initTools(mcAccess, worldConfigProvider);
         initBlocks(mcAccess, worldConfigProvider);

         try {
            (new VanillaBlocksFactory()).setupBlockProperties(worldConfigProvider);
         } catch (Throwable t) {
            LogUtil.logSevere("[NoCheatPlus] Could not initialize vanilla blocks: " + t.getClass().getSimpleName() + " - " + t.getMessage());
            LogUtil.logSevere(t);
         }

         if (mcAccess instanceof BlockPropertiesSetup) {
            try {
               ((BlockPropertiesSetup)mcAccess).setupBlockProperties(worldConfigProvider);
            } catch (Throwable t) {
               LogUtil.logSevere("[NoCheatPlus] McAccess.setupBlockProperties (" + mcAccess.getClass().getSimpleName() + ") could not execute properly: " + t.getClass().getSimpleName() + " - " + t.getMessage());
               LogUtil.logSevere(t);
            }
         }
      } catch (Throwable t) {
         LogUtil.logSevere(t);
      }

   }

   private static void initTools(MCAccess mcAccess, WorldConfigProvider worldConfigProvider) {
      tools.clear();
      tools.put(268, new ToolProps(BlockProperties.ToolType.SWORD, BlockProperties.MaterialBase.WOOD));
      tools.put(269, new ToolProps(BlockProperties.ToolType.SPADE, BlockProperties.MaterialBase.WOOD));
      tools.put(270, new ToolProps(BlockProperties.ToolType.PICKAXE, BlockProperties.MaterialBase.WOOD));
      tools.put(271, new ToolProps(BlockProperties.ToolType.AXE, BlockProperties.MaterialBase.WOOD));
      tools.put(272, new ToolProps(BlockProperties.ToolType.SWORD, BlockProperties.MaterialBase.STONE));
      tools.put(273, new ToolProps(BlockProperties.ToolType.SPADE, BlockProperties.MaterialBase.STONE));
      tools.put(274, new ToolProps(BlockProperties.ToolType.PICKAXE, BlockProperties.MaterialBase.STONE));
      tools.put(275, new ToolProps(BlockProperties.ToolType.AXE, BlockProperties.MaterialBase.STONE));
      tools.put(256, new ToolProps(BlockProperties.ToolType.SPADE, BlockProperties.MaterialBase.IRON));
      tools.put(257, new ToolProps(BlockProperties.ToolType.PICKAXE, BlockProperties.MaterialBase.IRON));
      tools.put(258, new ToolProps(BlockProperties.ToolType.AXE, BlockProperties.MaterialBase.IRON));
      tools.put(267, new ToolProps(BlockProperties.ToolType.SWORD, BlockProperties.MaterialBase.IRON));
      tools.put(276, new ToolProps(BlockProperties.ToolType.SWORD, BlockProperties.MaterialBase.DIAMOND));
      tools.put(277, new ToolProps(BlockProperties.ToolType.SPADE, BlockProperties.MaterialBase.DIAMOND));
      tools.put(278, new ToolProps(BlockProperties.ToolType.PICKAXE, BlockProperties.MaterialBase.DIAMOND));
      tools.put(279, new ToolProps(BlockProperties.ToolType.AXE, BlockProperties.MaterialBase.DIAMOND));
      tools.put(283, new ToolProps(BlockProperties.ToolType.SWORD, BlockProperties.MaterialBase.GOLD));
      tools.put(284, new ToolProps(BlockProperties.ToolType.SPADE, BlockProperties.MaterialBase.GOLD));
      tools.put(285, new ToolProps(BlockProperties.ToolType.PICKAXE, BlockProperties.MaterialBase.GOLD));
      tools.put(286, new ToolProps(BlockProperties.ToolType.AXE, BlockProperties.MaterialBase.GOLD));
      tools.put(359, new ToolProps(BlockProperties.ToolType.SHEARS, BlockProperties.MaterialBase.NONE));
   }

   private static void initBlocks(MCAccess mcAccess, WorldConfigProvider worldConfigProvider) {
      Arrays.fill(blocks, (Object)null);

      for(int i = 0; i < 4096; ++i) {
         blockFlags[i] = 0L;
         if (mcAccess.isBlockLiquid(i).decide()) {
            long[] var10000 = blockFlags;
            var10000[i] |= 2L;
            if (mcAccess.isBlockSolid(i).decide()) {
               var10000 = blockFlags;
               var10000[i] |= 4L;
            }
         } else if (mcAccess.isBlockSolid(i).decide()) {
            long[] var120 = blockFlags;
            var120[i] |= 132L;
         }
      }

      for(Material mat : new Material[]{Material.NETHER_BRICK_STAIRS, Material.COBBLESTONE_STAIRS, Material.SMOOTH_STAIRS, Material.BRICK_STAIRS, Material.SANDSTONE_STAIRS, Material.WOOD_STAIRS, Material.SPRUCE_WOOD_STAIRS, Material.BIRCH_WOOD_STAIRS, Material.JUNGLE_WOOD_STAIRS}) {
         long[] var121 = blockFlags;
         int var10001 = mat.getId();
         var121[var10001] |= 6529L;
      }

      for(Material mat : new Material[]{Material.STEP, Material.WOOD_STEP}) {
         long[] var122 = blockFlags;
         int var139 = mat.getId();
         var122[var139] |= 2176L;
      }

      for(Material mat : new Material[]{Material.RAILS, Material.DETECTOR_RAIL, Material.POWERED_RAIL}) {
         long[] var123 = blockFlags;
         int var140 = mat.getId();
         var123[var140] |= 65536L;
      }

      for(Material mat : new Material[]{Material.STATIONARY_WATER, Material.WATER}) {
         long[] var124 = blockFlags;
         int var141 = mat.getId();
         var124[var141] |= 8210L;
      }

      for(Material mat : new Material[]{Material.LAVA, Material.STATIONARY_LAVA}) {
         long[] var125 = blockFlags;
         int var142 = mat.getId();
         var125[var142] |= 8226L;
      }

      long[] var126 = blockFlags;
      int var143 = Material.SNOW.getId();
      var126[var143] |= 16384L;

      for(Material mat : new Material[]{Material.FENCE, Material.FENCE_GATE, Material.NETHER_FENCE}) {
         var126 = blockFlags;
         var143 = mat.getId();
         var126[var143] |= 64L;
      }

      for(Material mat : new Material[]{Material.VINE, Material.LADDER}) {
         var126 = blockFlags;
         var143 = mat.getId();
         var126[var143] |= 512L;
      }

      for(Material mat : new Material[]{Material.WATER_LILY, Material.LADDER, Material.DIODE_BLOCK_OFF, Material.DIODE_BLOCK_ON, Material.COCOA, Material.SNOW, Material.BREWING_STAND, Material.PISTON_MOVING_PIECE, Material.PISTON_EXTENSION, Material.STEP, Material.WOOD_STEP}) {
         var126 = blockFlags;
         var143 = mat.getId();
         var126[var143] |= 128L;
      }

      for(Material mat : new Material[]{Material.BREWING_STAND, Material.PISTON_EXTENSION}) {
         var126 = blockFlags;
         var143 = mat.getId();
         var126[var143] |= 256L;
      }

      for(Material mat : new Material[]{Material.PISTON_EXTENSION}) {
         var126 = blockFlags;
         var143 = mat.getId();
         var126[var143] |= 2048L;
      }

      for(Material mat : new Material[]{Material.WALL_SIGN, Material.SIGN_POST}) {
         var126 = blockFlags;
         var143 = mat.getId();
         var126[var143] &= -133L;
      }

      for(Material mat : new Material[]{Material.WOOD_PLATE, Material.STONE_PLATE, Material.WALL_SIGN, Material.SIGN_POST, Material.DIODE_BLOCK_ON, Material.DIODE_BLOCK_OFF, Material.BREWING_STAND, Material.LADDER, Material.CAKE_BLOCK}) {
         var126 = blockFlags;
         var143 = mat.getId();
         var126[var143] |= 8L;
      }

      for(Material mat : new Material[]{Material.FENCE, Material.FENCE_GATE, Material.COBBLE_WALL, Material.NETHER_FENCE, Material.IRON_FENCE, Material.THIN_GLASS}) {
         var126 = blockFlags;
         var143 = mat.getId();
         var126[var143] |= 1024L;
      }

      for(Material mat : new Material[]{Material.PISTON_EXTENSION, Material.BREWING_STAND, Material.CAKE_BLOCK}) {
         var126 = blockFlags;
         var143 = mat.getId();
         var126[var143] |= 4096L;
      }

      for(Material mat : instantMat) {
         blocks[mat.getId()] = instantType;
      }

      for(Material mat : new Material[]{Material.LEAVES, Material.BED_BLOCK}) {
         blocks[mat.getId()] = leafType;
      }

      for(Material mat : new Material[]{Material.HUGE_MUSHROOM_1, Material.HUGE_MUSHROOM_2, Material.VINE, Material.COCOA}) {
         blocks[mat.getId()] = hugeMushroomType;
      }

      blocks[Material.SNOW.getId()] = new BlockProps(getToolProps(Material.WOOD_SPADE), 0.1F, secToMs((double)0.5F, 0.1, 0.05, 0.05, 0.05, 0.05));
      blocks[Material.SNOW_BLOCK.getId()] = new BlockProps(getToolProps(Material.WOOD_SPADE), 0.1F, secToMs((double)1.0F, 0.15, 0.1, 0.05, 0.05, 0.05));

      for(Material mat : new Material[]{Material.REDSTONE_LAMP_ON, Material.REDSTONE_LAMP_OFF, Material.GLOWSTONE, Material.GLASS}) {
         blocks[mat.getId()] = glassType;
      }

      blocks[102] = glassType;
      blocks[Material.NETHERRACK.getId()] = new BlockProps(woodPickaxe, 0.4F, secToMs((double)2.0F, 0.3, 0.15, 0.1, 0.1, 0.05));
      blocks[Material.LADDER.getId()] = new BlockProps(noTool, 0.4F, secToMs(0.6), 2.5F);
      blocks[Material.CACTUS.getId()] = new BlockProps(noTool, 0.4F, secToMs(0.6));
      blocks[Material.WOOD_PLATE.getId()] = new BlockProps(woodAxe, 0.5F, secToMs((double)0.75F, 0.4, 0.2, 0.15, 0.1, 0.1));
      blocks[Material.STONE_PLATE.getId()] = new BlockProps(woodPickaxe, 0.5F, secToMs((double)2.5F, 0.4, 0.2, 0.15, 0.1, 0.07));
      blocks[Material.SAND.getId()] = sandType;
      blocks[Material.SOUL_SAND.getId()] = sandType;

      for(Material mat : new Material[]{Material.LEVER, Material.PISTON_BASE, Material.PISTON_EXTENSION, Material.PISTON_STICKY_BASE, Material.STONE_BUTTON, Material.PISTON_MOVING_PIECE}) {
         blocks[mat.getId()] = leverType;
      }

      blocks[Material.ICE.getId()] = new BlockProps(woodPickaxe, 0.5F, secToMs(0.7, 0.35, 0.18, 0.12, 0.09, 0.06));
      blocks[Material.DIRT.getId()] = sandType;
      blocks[Material.CAKE_BLOCK.getId()] = leverType;
      blocks[Material.BREWING_STAND.getId()] = new BlockProps(woodPickaxe, 0.5F, secToMs((double)2.5F, 0.4, 0.2, 0.15, 0.1, 0.1));
      blocks[Material.SPONGE.getId()] = new BlockProps(noTool, 0.6F, secToMs(0.9));

      for(Material mat : new Material[]{Material.MYCEL, Material.GRAVEL, Material.GRASS, Material.SOIL, Material.CLAY}) {
         blocks[mat.getId()] = gravelType;
      }

      for(Material mat : new Material[]{Material.RAILS, Material.POWERED_RAIL, Material.DETECTOR_RAIL}) {
         blocks[mat.getId()] = new BlockProps(woodPickaxe, 0.7F, railsTimes);
      }

      blocks[Material.MONSTER_EGGS.getId()] = new BlockProps(noTool, 0.75F, secToMs(1.15));
      blocks[Material.WOOL.getId()] = new BlockProps(noTool, 0.8F, secToMs(1.2), 3.0F);
      blocks[Material.SANDSTONE.getId()] = sandStoneType;
      blocks[Material.SANDSTONE_STAIRS.getId()] = sandStoneType;

      for(Material mat : new Material[]{Material.STONE, Material.SMOOTH_BRICK, Material.SMOOTH_STAIRS}) {
         blocks[mat.getId()] = stoneType;
      }

      blocks[Material.NOTE_BLOCK.getId()] = new BlockProps(woodAxe, 0.8F, secToMs(1.2, 0.6, 0.3, 0.2, 0.15, 0.1));
      blocks[Material.WALL_SIGN.getId()] = pumpkinType;
      blocks[Material.SIGN_POST.getId()] = pumpkinType;
      blocks[Material.PUMPKIN.getId()] = pumpkinType;
      blocks[Material.JACK_O_LANTERN.getId()] = pumpkinType;
      blocks[Material.MELON_BLOCK.getId()] = new BlockProps(noTool, 1.0F, secToMs(1.45), 3.0F);
      blocks[Material.BOOKSHELF.getId()] = new BlockProps(woodAxe, 1.5F, secToMs((double)2.25F, 1.15, 0.6, 0.4, 0.3, 0.2));

      for(Material mat : new Material[]{Material.WOOD_STAIRS, Material.WOOD, Material.WOOD_STEP, Material.LOG, Material.FENCE, Material.FENCE_GATE, Material.JUKEBOX, Material.JUNGLE_WOOD_STAIRS, Material.SPRUCE_WOOD_STAIRS, Material.BIRCH_WOOD_STAIRS, Material.WOOD_DOUBLE_STEP}) {
         blocks[mat.getId()] = woodType;
      }

      for(Material mat : new Material[]{Material.COBBLESTONE_STAIRS, Material.COBBLESTONE, Material.NETHER_BRICK, Material.NETHER_BRICK_STAIRS, Material.NETHER_FENCE, Material.CAULDRON, Material.BRICK, Material.BRICK_STAIRS, Material.MOSSY_COBBLESTONE, Material.BRICK, Material.BRICK_STAIRS, Material.STEP, Material.DOUBLE_STEP}) {
         blocks[mat.getId()] = brickType;
      }

      blocks[Material.WORKBENCH.getId()] = chestType;
      blocks[Material.CHEST.getId()] = chestType;
      blocks[Material.WOODEN_DOOR.getId()] = woodDoorType;
      blocks[Material.TRAP_DOOR.getId()] = woodDoorType;

      for(Material mat : new Material[]{Material.ENDER_STONE, Material.DRAGON_EGG, Material.COAL_ORE}) {
         blocks[mat.getId()] = coalType;
      }

      for(Material mat : new Material[]{Material.LAPIS_ORE, Material.LAPIS_BLOCK, Material.IRON_ORE}) {
         blocks[mat.getId()] = ironType;
      }

      for(Material mat : new Material[]{Material.REDSTONE_ORE, Material.GLOWING_REDSTONE_ORE, Material.EMERALD_ORE, Material.GOLD_ORE, Material.DIAMOND_ORE}) {
         blocks[mat.getId()] = diamondType;
      }

      blocks[Material.GOLD_BLOCK.getId()] = goldBlockType;
      blocks[Material.FURNACE.getId()] = dispenserType;
      blocks[Material.BURNING_FURNACE.getId()] = dispenserType;
      blocks[Material.DISPENSER.getId()] = dispenserType;
      blocks[Material.WEB.getId()] = new BlockProps(woodSword, 4.0F, secToMs((double)20.0F, 0.4, 0.4, 0.4, 0.4, 0.4));

      for(Material mat : new Material[]{Material.MOB_SPAWNER, Material.IRON_DOOR_BLOCK, Material.IRON_FENCE, Material.ENCHANTMENT_TABLE, Material.EMERALD_BLOCK}) {
         blocks[mat.getId()] = ironDoorType;
      }

      blocks[Material.IRON_BLOCK.getId()] = ironBlockType;
      blocks[Material.DIAMOND_BLOCK.getId()] = diamondBlockType;
      blocks[Material.ENDER_CHEST.getId()] = new BlockProps(woodPickaxe, 22.5F);
      blocks[Material.OBSIDIAN.getId()] = new BlockProps(diamondPickaxe, 50.0F, secToMs((double)250.0F, (double)250.0F, (double)250.0F, (double)250.0F, 9.4, (double)250.0F));
      blocks[Material.BEACON.getId()] = new BlockProps(noTool, 25.0F, secToMs(4.45));
      blocks[Material.COBBLE_WALL.getId()] = brickType;
      var126 = blockFlags;
      var143 = Material.COBBLE_WALL.getId();
      var126[var143] |= 64L;
      blocks[Material.WOOD_BUTTON.getId()] = leverType;
      blocks[Material.SKULL.getId()] = new BlockProps(noTool, 8.5F, secToMs(1.45));
      var126 = blockFlags;
      var143 = Material.SKULL.getId();
      var126[var143] |= 128L;
      blocks[Material.ANVIL.getId()] = new BlockProps(woodPickaxe, 5.0F);
      var126 = blockFlags;
      var143 = Material.FLOWER_POT.getId();
      var126[var143] |= 128L;

      for(Material mat : new Material[]{Material.AIR, Material.ENDER_PORTAL, Material.ENDER_PORTAL_FRAME, Material.PORTAL, Material.LAVA, Material.WATER, Material.BEDROCK, Material.STATIONARY_LAVA, Material.STATIONARY_WATER, Material.LOCKED_CHEST}) {
         blocks[mat.getId()] = indestructibleType;
      }

   }

   public static void dumpBlocks(boolean all) {
      List<String> missing = new LinkedList();
      if (all) {
         LogUtil.logInfo("[NoCheatPlus] Dump block properties for fastbreak check:");
         LogUtil.logInfo("--- Present entries -------------------------------");
      }

      List<String> tags = new ArrayList();

      for(int i = 0; i < blocks.length; ++i) {
         String mat;
         try {
            Material temp = Material.getMaterial(i);
            if (!temp.isBlock()) {
               continue;
            }

            mat = temp.toString();
         } catch (Exception var6) {
            mat = "?";
         }

         tags.clear();
         addFlagNames(blockFlags[i], tags);
         String tagsJoined = tags.isEmpty() ? "" : " / " + StringUtil.join(tags, "+");
         if (blocks[i] == null) {
            if (!mat.equals("?")) {
               missing.add("* MISSING " + i + "(" + mat + tagsJoined + ") ");
            }
         } else if (all) {
            LogUtil.logInfo(i + ": (" + mat + tagsJoined + ") " + blocks[i].toString());
         }
      }

      if (!missing.isEmpty()) {
         Bukkit.getLogger().warning("[NoCheatPlus] The block breaking data is incomplete, default to allow instant breaking:");
         LogUtil.logWarning("--- Missing entries -------------------------------");

         for(String spec : missing) {
            LogUtil.logWarning(spec);
         }
      }

   }

   public static void addFlagNames(long flags, Collection tags) {
      String tag = (String)flagNameMap.get(flags);
      if (tag != null) {
         tags.add(tag);
      } else {
         for(Long flag : flagNameMap.keySet()) {
            if ((flags & flag) != 0L) {
               tags.add(flagNameMap.get(flag));
            }
         }

      }
   }

   public static Collection getFlagNames(Long flags) {
      ArrayList<String> tags = new ArrayList(flagNameMap.size());
      if (flags == null) {
         return tags;
      } else {
         addFlagNames(flags, tags);
         return tags;
      }
   }

   public static long parseFlag(String input) {
      String ucInput = input.trim().toUpperCase();
      Long flag = (Long)nameFlagMap.get(ucInput);
      if (flag != null) {
         return flag;
      } else {
         try {
            Long altFlag = Long.parseLong(input);
            return altFlag;
         } catch (NumberFormatException var4) {
            throw new InputMismatchException();
         }
      }
   }

   public static long[] secToMs(double s1, double s2, double s3, double s4, double s5, double s6) {
      return new long[]{(long)(s1 * (double)1000.0F), (long)(s2 * (double)1000.0F), (long)(s3 * (double)1000.0F), (long)(s4 * (double)1000.0F), (long)(s5 * (double)1000.0F), (long)(s6 * (double)1000.0F)};
   }

   public static long[] secToMs(double s1) {
      long v = (long)(s1 * (double)1000.0F);
      return new long[]{v, v, v, v, v, v};
   }

   public static ToolProps getToolProps(ItemStack stack) {
      return stack == null ? noTool : getToolProps(stack.getTypeId());
   }

   public static ToolProps getToolProps(Material mat) {
      return mat == null ? noTool : getToolProps(mat.getId());
   }

   public static ToolProps getToolProps(Integer id) {
      ToolProps props = (ToolProps)tools.get(id);
      return props == null ? noTool : props;
   }

   public static BlockProps getBlockProps(ItemStack stack) {
      return stack == null ? defaultBlockProps : getBlockProps(stack.getTypeId());
   }

   public static BlockProps getBlockProps(Material mat) {
      return mat == null ? defaultBlockProps : getBlockProps(mat.getId());
   }

   public static BlockProps getBlockProps(int blockId) {
      return blockId >= 0 && blockId < blocks.length && blocks[blockId] != null ? blocks[blockId] : defaultBlockProps;
   }

   public static long getBreakingDuration(int blockId, Player player) {
      return getBreakingDuration(blockId, player.getItemInHand(), player.getInventory().getHelmet(), player, player.getLocation());
   }

   public static long getBreakingDuration(int blockId, ItemStack itemInHand, ItemStack helmet, Player player, Location location) {
      blockCache.setAccess(location.getWorld());
      pLoc.setBlockCache(blockCache);
      pLoc.set(location, player, 0.3);
      boolean onGround = pLoc.isOnGround();
      int bx = pLoc.getBlockX();
      int bz = pLoc.getBlockZ();
      double y = pLoc.getY() + player.getEyeHeight();
      int by = Location.locToBlock(y);
      int headId = blockCache.getTypeId(bx, by, bz);
      long headFlags = blockFlags[headId];
      boolean inWater;
      if ((headFlags & 16L) == 0L) {
         inWater = false;
      } else {
         int data8 = (blockCache.getData(bx, by, bz) & 15) % 8;
         double level;
         if ((data8 & 8) != 0) {
            level = (double)1.0F;
         } else {
            level = (double)1.0F - (double)0.125F * ((double)1.0F + (double)data8);
         }

         inWater = y - (double)by < level;
      }

      blockCache.cleanup();
      pLoc.cleanup();
      double haste = PotionUtil.getPotionEffectAmplifier(player, PotionEffectType.FAST_DIGGING);
      return getBreakingDuration(blockId, itemInHand, onGround, inWater, helmet != null && helmet.containsEnchantment(Enchantment.WATER_WORKER), haste == Double.NEGATIVE_INFINITY ? 0 : 1 + (int)haste);
   }

   public static long getBreakingDuration(int blockId, ItemStack itemInHand, boolean onGround, boolean inWater, boolean aquaAffinity, int haste) {
      if (itemInHand == null) {
         return getBreakingDuration(blockId, getBlockProps(blockId), noTool, onGround, inWater, aquaAffinity, 0);
      } else {
         int efficiency = 0;
         if (itemInHand.containsEnchantment(Enchantment.DIG_SPEED)) {
            efficiency = itemInHand.getEnchantmentLevel(Enchantment.DIG_SPEED);
         }

         return getBreakingDuration(blockId, getBlockProps(blockId), getToolProps(itemInHand.getTypeId()), onGround, inWater, aquaAffinity, efficiency, haste);
      }
   }

   public static long getBreakingDuration(int blockId, BlockProps blockProps, ToolProps toolProps, boolean onGround, boolean inWater, boolean aquaAffinity, int efficiency, int haste) {
      long dur = getBreakingDuration(blockId, blockProps, toolProps, onGround, inWater, aquaAffinity, efficiency);
      return haste > 0 ? (long)(Math.pow(0.8, (double)haste) * (double)dur) : dur;
   }

   public static long getBreakingDuration(int blockId, BlockProps blockProps, ToolProps toolProps, boolean onGround, boolean inWater, boolean aquaAffinity, int efficiency) {
      if (efficiency > 0) {
         if (blockId == Material.LEAVES.getId() || blockProps == glassType) {
            return efficiency == 1 ? 100L : 0L;
         }

         if (blockId == Material.MELON_BLOCK.getId()) {
            return 450L / (long)Math.pow((double)2.0F, (double)(efficiency - 1));
         }

         if (blockProps == chestType) {
            return (long)((double)blockProps.breakingTimes[0] / (double)5.0F / (double)efficiency);
         }
      }

      boolean isValidTool = isValidTool(blockId, blockProps, toolProps, efficiency);
      long duration;
      if (isValidTool) {
         duration = blockProps.breakingTimes[toolProps.materialBase.index];
         if (efficiency > 0) {
            duration = (long)((float)duration / blockProps.efficiencyMod);
         }
      } else {
         duration = blockProps.breakingTimes[0];
         if (toolProps.toolType == BlockProperties.ToolType.SWORD) {
            duration = (long)((float)duration / 1.5F);
         }
      }

      if (toolProps.toolType == BlockProperties.ToolType.SHEARS) {
         if (blockId == Material.WEB.getId()) {
            duration = 400L;
            isValidTool = true;
         } else if (blockId == Material.WOOL.getId()) {
            duration = 240L;
            isValidTool = true;
         } else if (blockId == Material.LEAVES.getId()) {
            duration = 20L;
            isValidTool = true;
         } else if (blockId == Material.VINE.getId()) {
            duration = 300L;
            isValidTool = true;
         }
      } else if (blockId == Material.VINE.getId() && toolProps.toolType == BlockProperties.ToolType.AXE) {
         isValidTool = true;
         if (toolProps.materialBase != BlockProperties.MaterialBase.WOOD && toolProps.materialBase != BlockProperties.MaterialBase.STONE) {
            duration = 0L;
         } else {
            duration = 100L;
         }
      }

      if (isValidTool || blockProps.tool.toolType == BlockProperties.ToolType.NONE) {
         float mult = 1.0F;
         if (inWater && !aquaAffinity) {
            mult *= breakPenaltyInWater;
         }

         if (!onGround) {
            mult *= breakPenaltyOffGround;
         }

         duration = (long)(mult * (float)duration);
         if (efficiency > 0) {
            if (blockId == Material.WOODEN_DOOR.getId() && toolProps.toolType != BlockProperties.ToolType.AXE) {
               switch (efficiency) {
                  case 1:
                     return (long)(mult * 1500.0F);
                  case 2:
                     return (long)(mult * 750.0F);
                  case 3:
                     return (long)(mult * 450.0F);
                  case 4:
                     return (long)(mult * 250.0F);
                  case 5:
                     return (long)(mult * 150.0F);
               }
            }

            for(int i = 0; i < efficiency; ++i) {
               duration = (long)((double)duration / 1.33);
            }

            if (toolProps.materialBase == BlockProperties.MaterialBase.WOOD) {
               if (toolProps.toolType == BlockProperties.ToolType.PICKAXE && (blockProps == ironDoorType || blockProps == dispenserType)) {
                  if (blockProps == dispenserType) {
                     duration = (long)((double)duration / (double)1.5F - (double)((efficiency - 1) * 60));
                  } else if (blockProps == ironDoorType) {
                     duration = (long)((double)duration / (double)1.5F - (double)((efficiency - 1) * 100));
                  }
               } else if (blockId == Material.LOG.getId()) {
                  duration -= efficiency >= 4 ? 250L : 400L;
               } else if (blockProps.tool.toolType == toolProps.toolType) {
                  duration -= 250L;
               } else {
                  duration -= (long)(efficiency * 30);
               }
            } else if (toolProps.materialBase == BlockProperties.MaterialBase.STONE && blockId == Material.LOG.getId()) {
               duration -= 100L;
            }
         }
      }

      return Math.max(0L, duration);
   }

   public static boolean isValidTool(int blockId, BlockProps blockProps, ToolProps toolProps, int efficiency) {
      boolean isValidTool = blockProps.tool.toolType == toolProps.toolType;
      if (!isValidTool && efficiency > 0) {
         if (blockId == Material.SNOW.getId()) {
            return toolProps.toolType == BlockProperties.ToolType.SPADE;
         }

         if (blockId == Material.WOOL.getId()) {
            return true;
         }

         if (blockId == Material.WOODEN_DOOR.getId()) {
            return true;
         }

         if (blockProps.hardness <= 2.0F && (blockProps.tool.toolType == BlockProperties.ToolType.AXE || blockProps.tool.toolType == BlockProperties.ToolType.SPADE || (double)blockProps.hardness < 0.8 && blockId != Material.NETHERRACK.getId() && blockId != Material.SNOW.getId() && blockId != Material.SNOW_BLOCK.getId() && blockId != Material.STONE_PLATE.getId())) {
            return true;
         }
      }

      return isValidTool;
   }

   public static void setToolProps(int itemId, ToolProps toolProps) {
      if (toolProps == null) {
         throw new NullPointerException("ToolProps must not be null");
      } else {
         toolProps.validate();
         tools.put(itemId, toolProps);
      }
   }

   public static void setBlockProps(int blockId, BlockProps blockProps) {
      if (blockProps == null) {
         throw new NullPointerException("BlockProps must not be null");
      } else {
         blockProps.validate();
         if (blockId >= 0 && blockId < blocks.length) {
            blocks[blockId] = blockProps;
         } else {
            throw new IllegalArgumentException("The blockId is outside of supported range: " + blockId);
         }
      }
   }

   public static boolean isValidTool(int blockId, ItemStack itemInHand) {
      BlockProps blockProps = getBlockProps(blockId);
      ToolProps toolProps = getToolProps(itemInHand);
      int efficiency = itemInHand == null ? 0 : itemInHand.getEnchantmentLevel(Enchantment.DIG_SPEED);
      return isValidTool(blockId, blockProps, toolProps, efficiency);
   }

   public static BlockProps getDefaultBlockProps() {
      return defaultBlockProps;
   }

   public static void setDefaultBlockProps(BlockProps blockProps) {
      blockProps.validate();
      defaultBlockProps = blockProps;
   }

   /** @deprecated */
   public static boolean isInWater(int blockId) {
      return blockId == Material.STATIONARY_WATER.getId() || blockId == Material.STATIONARY_LAVA.getId();
   }

   public static boolean isOnGround(Player player, Location location, double yOnGround) {
      blockCache.setAccess(location.getWorld());
      pLoc.setBlockCache(blockCache);
      pLoc.set(location, player, yOnGround);
      boolean onGround = pLoc.isOnGround();
      blockCache.cleanup();
      pLoc.cleanup();
      return onGround;
   }

   public static boolean isOnGroundOrResetCond(Player player, Location location, double yOnGround) {
      blockCache.setAccess(location.getWorld());
      pLoc.setBlockCache(blockCache);
      pLoc.set(location, player, yOnGround);
      boolean res = pLoc.isOnGround() || pLoc.isResetCond();
      blockCache.cleanup();
      pLoc.cleanup();
      return res;
   }

   /** @deprecated */
   public static final long getBLockFlags(int id) {
      return blockFlags[id];
   }

   public static final long getBlockFlags(int id) {
      return blockFlags[id];
   }

   public static final void setBlockFlags(int id, long flags) {
      blockFlags[id] = flags;
   }

   public static final boolean canClimbUp(BlockCache cache, int x, int y, int z) {
      int id = cache.getTypeId(x, y, z);
      if ((blockFlags[id] & 512L) == 0L) {
         return false;
      } else if (id == Material.LADDER.getId()) {
         return true;
      } else if ((blockFlags[cache.getTypeId(x + 1, y, z)] & 4L) != 0L) {
         return true;
      } else if ((blockFlags[cache.getTypeId(x - 1, y, z)] & 4L) != 0L) {
         return true;
      } else if ((blockFlags[cache.getTypeId(x, y, z + 1)] & 4L) != 0L) {
         return true;
      } else {
         return (blockFlags[cache.getTypeId(x, y, z - 1)] & 4L) != 0L;
      }
   }

   public static final boolean isClimbable(int id) {
      return (blockFlags[id] & 512L) != 0L;
   }

   public static final boolean isStairs(int id) {
      return (blockFlags[id] & 1L) != 0L;
   }

   public static final boolean isLiquid(int id) {
      return (blockFlags[id] & 2L) != 0L;
   }

   public static final boolean isSolid(int id) {
      return (blockFlags[id] & 4L) != 0L;
   }

   public static final boolean isGround(int id) {
      return (blockFlags[id] & 128L) != 0L;
   }

   public static final boolean isPassable(int id) {
      long flags = blockFlags[id];
      if ((flags & 10L) != 0L) {
         return true;
      } else {
         return (flags & 4L) == 0L;
      }
   }

   public static final boolean isRails(int id) {
      return (blockFlags[id] & 65536L) != 0L;
   }

   public static final boolean isPassable(BlockCache access, double x, double y, double z, int id) {
      if (isPassable(id)) {
         return true;
      } else {
         int bx = Location.locToBlock(x);
         int by = Location.locToBlock(y);
         int bz = Location.locToBlock(z);
         double[] bounds = access.getBounds(bx, by, bz);
         if (bounds != null && collidesBlock(access, x, y, z, x, y, z, bx, by, bz, id, bounds, blockFlags[id])) {
            double fx = x - (double)bx;
            double fy = y - (double)by;
            double fz = z - (double)bz;
            return isPassableWorkaround(access, bx, by, bz, fx, fy, fz, id, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F);
         } else {
            return true;
         }
      }
   }

   public static final boolean isPassableH150(BlockCache access, double x, double y, double z) {
      int by = Location.locToBlock(y) - 1;
      double fy = y - (double)by;
      if (fy >= (double)1.5F) {
         return true;
      } else {
         int bx = Location.locToBlock(x);
         int bz = Location.locToBlock(z);
         int belowId = access.getTypeId(bx, by, bz);
         long belowFlags = blockFlags[belowId];
         if ((belowFlags & 64L) != 0L && !isPassable(belowId)) {
            double[] belowBounds = access.getBounds(bx, by, bz);
            if (belowBounds == null) {
               return true;
            } else if (!collidesBlock(access, x, y, z, x, y, z, bx, by, bz, belowId, belowBounds, belowFlags)) {
               return true;
            } else {
               double fx = x - (double)bx;
               double fz = z - (double)bz;
               return isPassableWorkaround(access, bx, by, bz, fx, fy, fz, belowId, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F);
            }
         } else {
            return true;
         }
      }
   }

   public static final boolean isPassableExact(BlockCache access, double x, double y, double z, int id) {
      return isPassable(access, x, y, z, id) && isPassableH150(access, x, y, z);
   }

   public static final boolean isPassableExact(BlockCache access, Location loc) {
      return isPassableExact(access, loc.getX(), loc.getY(), loc.getZ(), access.getTypeId(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
   }

   public static final boolean isPassableWorkaround(BlockCache access, int bx, int by, int bz, double fx, double fy, double fz, int id, double dX, double dY, double dZ, double dT) {
      long flags = blockFlags[id];
      if ((flags & 1L) != 0L) {
         if ((access.getData(bx, by, bz) & 4) != 0) {
            if (Math.max(fy, fy + dY * dT) < (double)0.5F) {
               return true;
            }
         } else if (Math.min(fy, fy + dY * dT) >= (double)0.5F) {
            return true;
         }
      } else if (id == Material.SOUL_SAND.getId()) {
         if (Math.min(fy, fy + dY * dT) >= (double)0.875F) {
            return true;
         }
      } else if (id != Material.IRON_FENCE.getId() && id != Material.THIN_GLASS.getId()) {
         if (id != Material.FENCE.getId() && id != Material.NETHER_FENCE.getId()) {
            if (id == Material.FENCE_GATE.getId()) {
               if ((access.getData(bx, by, bz) & 4) != 0) {
                  return true;
               }
            } else if (id == Material.CAKE_BLOCK.getId()) {
               if (Math.min(fy, fy + dY * dT) >= (double)0.4375F) {
                  return true;
               }
            } else if (id == Material.CAULDRON.getId()) {
               if (Math.min(fy, fy + dY * dT) >= (double)0.3125F) {
                  return isInsideCenter(fx, fz, dX, dZ, dT, (double)0.125F);
               }
            } else {
               if (id == Material.CACTUS.getId()) {
                  if (Math.min(fy, fy + dY * dT) >= (double)0.9375F) {
                     return true;
                  }

                  return !collidesCenter(fx, fz, dX, dZ, dT, (double)0.0625F);
               }

               if (id == Material.PISTON_EXTENSION.getId() && Math.min(fy, fy + dY * dT) >= (double)0.625F) {
                  return true;
               }
            }
         } else if (!collidesFence(fx, fz, dX, dZ, dT, 0.425)) {
            return true;
         }
      } else if (!collidesFence(fx, fz, dX, dZ, dT, 0.05)) {
         return true;
      }

      return false;
   }

   public static boolean collidesFence(double fx, double fz, double dX, double dZ, double dT, double d) {
      double dFx = (double)0.5F - fx;
      double dFz = (double)0.5F - fz;
      if (Math.abs(dFx) > 0.05 && Math.abs(dFz) > d) {
         double dFx2 = (double)0.5F - (fx + dX * dT);
         double dFz2 = (double)0.5F - (fz + dZ * dT);
         if (Math.abs(dFx2) > 0.05 && Math.abs(dFz2) > d && dFx * dFx2 > (double)0.0F && dFz * dFz2 > (double)0.0F) {
            return false;
         }
      }

      return true;
   }

   public static final boolean collidesCenter(double fx, double fz, double dX, double dZ, double dT, double inset) {
      double high = (double)1.0F - inset;
      double xEnd = fx + dX * dT;
      if (xEnd < inset && fx < inset) {
         return false;
      } else if (xEnd >= high && fx >= high) {
         return false;
      } else {
         double zEnd = fz + dZ * dT;
         if (zEnd < inset && fz < inset) {
            return false;
         } else {
            return !(zEnd >= high) || !(fz >= high);
         }
      }
   }

   public static final boolean isInsideCenter(double fx, double fz, double dX, double dZ, double dT, double inset) {
      double high = (double)1.0F - inset;
      double xEnd = fx + dX * dT;
      if (!(xEnd < inset) && !(fx < inset)) {
         if (!(xEnd >= high) && !(fx >= high)) {
            double zEnd = fz + dZ * dT;
            if (!(zEnd < inset) && !(fz < inset)) {
               return !(zEnd >= high) && !(fz >= high);
            } else {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static double getGroundMinHeight(BlockCache access, int x, int y, int z, int id, double[] bounds, long flags) {
      if ((flags & 16384L) != 0L) {
         int data = (access.getData(x, y, z) & 15) % 8;
         return data < 3 ? (double)0.0F : (double)0.5F;
      } else if ((flags & 32768L) != 0L) {
         int data = (access.getData(x, y, z) & 15) % 8;
         return (double)0.125F * (double)data;
      } else if ((flags & 64L) != 0L) {
         return (double)1.5F;
      } else if ((flags & 1L) != 0L) {
         return (access.getData(x, y, z) & 4) != 0 ? (double)1.0F : (double)0.5F;
      } else if (id == Material.SOUL_SAND.getId()) {
         return (double)0.875F;
      } else if (id == Material.CAULDRON.getId()) {
         return (double)0.3125F;
      } else if (id == Material.CACTUS.getId()) {
         return (double)0.9375F;
      } else if (id == Material.PISTON_EXTENSION.getId()) {
         return (double)0.625F;
      } else {
         return (flags & 4096L) != 0L ? (double)0.0F : bounds[4];
      }
   }

   public static final boolean isPassable(PlayerLocation loc) {
      return isPassable(loc.getBlockCache(), loc.getX(), loc.getY(), loc.getZ(), loc.getTypeId());
   }

   public static final boolean isPassable(Location loc) {
      blockCache.setAccess(loc.getWorld());
      boolean res = isPassable(blockCache, loc.getX(), loc.getY(), loc.getZ(), blockCache.getTypeId(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
      blockCache.cleanup();
      return res;
   }

   public static final boolean isPassable(Location from, Location to) {
      blockCache.setAccess(from.getWorld());
      PassableRayTracing rt = new PassableRayTracing();
      rt.setBlockCache(blockCache);
      rt.set(from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ());
      rt.loop();
      boolean collides = rt.collides();
      blockCache.cleanup();
      rt.cleanup();
      return !collides;
   }

   public static final boolean isPassable(BlockCache access, Location loc) {
      return isPassable(access, loc.getX(), loc.getY(), loc.getZ(), access.getTypeId(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
   }

   public static void applyConfig(RawConfigFile config, String pathPrefix) {
      for(String input : config.getStringList(pathPrefix + "ignorepassable")) {
         Integer id = RawConfigFile.parseTypeId(input);
         if (id != null && id >= 0 && id < 4096) {
            long[] var10000 = blockFlags;
            int var10001 = id;
            var10000[var10001] |= 8L;
         } else {
            LogUtil.logWarning("[NoCheatplus] Bad block id (" + pathPrefix + "ignorepassable" + "): " + input);
         }
      }

      for(String input : config.getStringList(pathPrefix + "allowinstantbreak")) {
         Integer id = RawConfigFile.parseTypeId(input);
         if (id != null && id >= 0 && id < 4096) {
            setBlockProps(id, instantType);
         } else {
            LogUtil.logWarning("[NoCheatplus] Bad block id (" + pathPrefix + "allowinstantbreak" + "): " + input);
         }
      }

      ConfigurationSection section = config.getConfigurationSection(pathPrefix + "overrideflags");
      if (section != null) {
         Map<String, Object> entries = section.getValues(false);
         boolean hasErrors = false;

         for(Map.Entry entry : entries.entrySet()) {
            String key = (String)entry.getKey();
            Integer id = RawConfigFile.parseTypeId(key);
            if (id != null && id >= 0 && id < 4096) {
               Object obj = entry.getValue();
               if (!(obj instanceof String)) {
                  LogUtil.logWarning("[NoCheatplus] Bad flags at " + pathPrefix + "overrideflags" + " for key: " + key);
                  hasErrors = true;
               } else {
                  Collection<String> split = StringUtil.split((String)obj, ' ', ',', '/', '|', '+', ';', '\t');
                  long flags = 0L;
                  boolean error = false;

                  for(String input : split) {
                     input = input.trim();
                     if (!input.isEmpty()) {
                        if (input.equalsIgnoreCase("default")) {
                           flags |= blockFlags[id];
                        } else {
                           try {
                              flags |= parseFlag(input);
                           } catch (InputMismatchException var17) {
                              LogUtil.logWarning("[NoCheatplus] Bad flag at " + pathPrefix + "overrideflags" + " for key " + key + " (skip setting flags for this block): " + input);
                              error = true;
                              hasErrors = true;
                              break;
                           }
                        }
                     }
                  }

                  if (!error) {
                     blockFlags[id] = flags;
                  }
               }
            } else {
               LogUtil.logWarning("[NoCheatplus] Bad block id (" + pathPrefix + "overrideflags" + "): " + key);
            }
         }

         if (hasErrors) {
            LogUtil.logInfo("[NoCheatPlus] Overriding block-flags was not entirely successful, all available flags: \n" + StringUtil.join(flagNameMap.values(), "|"));
         }
      }

   }

   public static final boolean hasAnyFlags(BlockCache access, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, long flags) {
      return hasAnyFlags(access, Location.locToBlock(minX), Location.locToBlock(minY), Location.locToBlock(minZ), Location.locToBlock(maxX), Location.locToBlock(maxY), Location.locToBlock(maxZ), flags);
   }

   public static final boolean hasAnyFlags(BlockCache access, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, long flags) {
      for(int x = minX; x <= maxX; ++x) {
         for(int z = minZ; z <= maxZ; ++z) {
            for(int y = minY; y <= maxY; ++y) {
               if ((blockFlags[access.getTypeId(x, y, z)] & flags) != 0L) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   public static final boolean collides(BlockCache access, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, long flags) {
      int iMinX = Location.locToBlock(minX);
      int iMaxX = Location.locToBlock(maxX);
      int iMinY = Location.locToBlock(minY - ((flags & 64L) != 0L ? (double)0.5625F : (double)0.0F));
      int iMaxY = Location.locToBlock(maxY);
      int iMinZ = Location.locToBlock(minZ);
      int iMaxZ = Location.locToBlock(maxZ);

      for(int x = iMinX; x <= iMaxX; ++x) {
         for(int z = iMinZ; z <= iMaxZ; ++z) {
            for(int y = iMinY; y <= iMaxY; ++y) {
               int id = access.getTypeId(x, y, z);
               long cFlags = blockFlags[id];
               if ((cFlags & flags) != 0L) {
                  double[] bounds = access.getBounds(x, y, z);
                  if (bounds != null && collidesBlock(access, minX, minY, minZ, maxX, maxY, maxZ, x, y, z, id, bounds, cFlags)) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   public static final boolean collidesId(BlockCache access, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, int id) {
      int iMinX = Location.locToBlock(minX);
      int iMaxX = Location.locToBlock(maxX);
      int iMinY = Location.locToBlock(minY - ((blockFlags[id] & 64L) != 0L ? (double)0.5625F : (double)0.0F));
      int iMaxY = Location.locToBlock(maxY);
      int iMinZ = Location.locToBlock(minZ);
      int iMaxZ = Location.locToBlock(maxZ);

      for(int x = iMinX; x <= iMaxX; ++x) {
         for(int z = iMinZ; z <= iMaxZ; ++z) {
            for(int y = iMinY; y <= iMaxY; ++y) {
               if (id == access.getTypeId(x, y, z)) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   public static final boolean collidesBlock(BlockCache access, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, int id) {
      int iMinX = Location.locToBlock(minX);
      int iMaxX = Location.locToBlock(maxX);
      int iMinY = Location.locToBlock(minY - ((blockFlags[id] & 64L) != 0L ? (double)0.5625F : (double)0.0F));
      int iMaxY = Location.locToBlock(maxY);
      int iMinZ = Location.locToBlock(minZ);
      int iMaxZ = Location.locToBlock(maxZ);

      for(int x = iMinX; x <= iMaxX; ++x) {
         for(int z = iMinZ; z <= iMaxZ; ++z) {
            for(int y = iMinY; y <= iMaxY; ++y) {
               if (id == access.getTypeId(x, y, z)) {
                  double[] bounds = access.getBounds(x, y, z);
                  if (bounds != null && collidesBlock(access, minX, minY, minZ, maxX, maxY, maxZ, x, y, z, id, bounds, blockFlags[id])) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   public static final boolean collidesBlock(BlockCache access, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, int x, int y, int z, int id) {
      double[] bounds = access.getBounds(x, y, z);
      if (bounds == null) {
         return false;
      } else {
         long flags = blockFlags[id];
         return collidesBlock(access, minX, minY, minZ, maxX, maxY, maxZ, x, y, z, id, bounds, flags);
      }
   }

   public static final boolean collidesBlock(BlockCache access, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, int x, int y, int z, int id, double[] bounds, long flags) {
      double bminX;
      double bminZ;
      double bminY;
      double bmaxX;
      double bmaxY;
      double bmaxZ;
      if ((flags & 1L) != 0L) {
         bminZ = (double)0.0F;
         bminY = (double)0.0F;
         bminX = (double)0.0F;
         bmaxZ = (double)1.0F;
         bmaxY = (double)1.0F;
         bmaxX = (double)1.0F;
      } else {
         if ((flags & 2048L) != 0L) {
            bminZ = (double)0.0F;
            bminX = (double)0.0F;
            bmaxZ = (double)1.0F;
            bmaxX = (double)1.0F;
         } else {
            bminX = bounds[0];
            bminZ = bounds[2];
            bmaxX = bounds[3];
            bmaxZ = bounds[5];
         }

         if ((flags & 16384L) != 0L) {
            bminY = (double)0.0F;
            int data = (access.getData(x, y, z) & 15) % 8;
            bmaxY = data < 3 ? (double)0.0F : (double)0.5F;
         } else if ((flags & 32768L) != 0L) {
            bminY = (double)0.0F;
            int data = (access.getData(x, y, z) & 15) % 8;
            bmaxY = (double)0.125F * (double)data;
         } else if ((flags & 64L) != 0L) {
            bminY = (double)0.0F;
            bmaxY = (double)1.5F;
         } else if ((flags & 256L) != 0L) {
            bminY = (double)0.0F;
            bmaxY = (double)1.0F;
         } else if ((flags & 8192L) != 0L) {
            bminY = (double)0.0F;
            int data = access.getData(x, y, z);
            if ((data & 8) == 0) {
               int data8 = (data & 15) % 8;
               if (data8 > 4) {
                  bmaxY = (double)0.5F;
               } else {
                  bmaxY = (double)1.0F;
               }
            } else {
               bmaxY = (double)1.0F;
            }
         } else {
            bminY = bounds[1];
            bmaxY = bounds[4];
         }
      }

      if (!(minX >= bmaxX + (double)x) && !(maxX < bminX + (double)x)) {
         if (!(minY >= bmaxY + (double)y) && !(maxY < bminY + (double)y)) {
            return !(minZ >= bmaxZ + (double)z) && !(maxZ < bminZ + (double)z);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   /** @deprecated */
   public static double[] getCorrectedBounds(BlockCache access, int x, int y, int z) {
      return getCorrectedBounds(x, y, z, access.getTypeId(x, y, z), access.getBounds(x, y, z));
   }

   /** @deprecated */
   public static double[] getCorrectedBounds(int x, int y, int z, int id, double[] bounds) {
      return bounds == null ? null : bounds;
   }

   public static final boolean isOnGround(BlockCache access, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
      return isOnGround(access, minX, minY, minZ, maxX, maxY, maxZ, 0L);
   }

   public static final boolean isOnGround(BlockCache access, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, long ignoreFlags) {
      int maxBlockY = access.getMaxBlockY();
      int iMinX = Location.locToBlock(minX);
      int iMaxX = Location.locToBlock(maxX);
      int iMinY = Location.locToBlock(minY - 0.5626);
      if (iMinY > maxBlockY) {
         return false;
      } else {
         int iMaxY = Math.min(Location.locToBlock(maxY), maxBlockY);
         int iMinZ = Location.locToBlock(minZ);
         int iMaxZ = Location.locToBlock(maxZ);

         for(int x = iMinX; x <= iMaxX; ++x) {
            int z = iMinZ;

            while(z <= iMaxZ) {
               int y = iMaxY;

               while(true) {
                  label129: {
                     if (y >= iMinY) {
                        int id = access.getTypeId(x, y, z);
                        long flags = blockFlags[id];
                        if ((flags & 128L) == 0L || (flags & ignoreFlags) != 0L) {
                           break label129;
                        }

                        double[] bounds = access.getBounds(x, y, z);
                        if (bounds == null) {
                           return true;
                        }

                        if (!collidesBlock(access, minX, minY, minZ, maxX, maxY, maxZ, x, y, z, id, bounds, flags) || isPassableWorkaround(access, x, y, z, minX - (double)x, minY - (double)y, minZ - (double)z, id, maxX - minX, maxY - minY, maxZ - minZ, (double)1.0F) && ((flags & 4096L) == 0L || getGroundMinHeight(access, x, y, z, id, bounds, flags) > maxY - (double)y)) {
                           break label129;
                        }

                        if (getGroundMinHeight(access, x, y, z, id, bounds, flags) > maxY - (double)y) {
                           if (!isFullBounds(bounds)) {
                              break label129;
                           }
                        } else {
                           if (maxY - (double)y < (double)1.0F) {
                              return true;
                           }

                           if (y >= maxBlockY) {
                              return true;
                           }

                           boolean variable = (flags & 1024L) != 0L;
                           if (y != iMaxY && !variable) {
                              return true;
                           }

                           int aboveId = access.getTypeId(x, y + 1, z);
                           long aboveFlags = blockFlags[aboveId];
                           if ((aboveFlags & 8L) != 0L) {
                              return true;
                           }

                           if ((aboveFlags & 128L) == 0L || (aboveFlags & 2L) != 0L || (aboveFlags & ignoreFlags) != 0L) {
                              return true;
                           }

                           variable |= (aboveFlags & 1024L) != 0L;
                           if (!variable && id == aboveId) {
                              if (!isFullBounds(bounds)) {
                                 break label129;
                              }
                           } else {
                              double[] aboveBounds = access.getBounds(x, y + 1, z);
                              if (aboveBounds == null) {
                                 return true;
                              }

                              if (!collidesBlock(access, minX, minY, minZ, maxX, Math.max(maxY, 1.49 + (double)y), maxZ, x, y + 1, z, aboveId, aboveBounds, aboveFlags)) {
                                 return true;
                              }

                              if (isPassableWorkaround(access, x, y + 1, z, minX - (double)x, minY - (double)(y + 1), minZ - (double)z, id, maxX - minX, maxY - minY, maxZ - minZ, (double)1.0F)) {
                                 return true;
                              }

                              if (!isFullBounds(aboveBounds)) {
                                 if (!variable) {
                                    break label129;
                                 }

                                 if (!isSameShape(bounds, aboveBounds)) {
                                    return true;
                                 }
                              }
                           }
                        }
                     }

                     ++z;
                     break;
                  }

                  --y;
               }
            }
         }

         return false;
      }
   }

   public static final boolean isFullBounds(double[] bounds) {
      for(int i = 0; i < 3; ++i) {
         if (bounds[i] != (double)0.0F || bounds[i + 3] != (double)1.0F) {
            return false;
         }
      }

      return true;
   }

   public static final boolean isSameShape(double[] bounds1, double[] bounds2) {
      for(int i = 0; i < 6; ++i) {
         if (bounds1[i] != bounds2[i]) {
            return false;
         }
      }

      return true;
   }

   public static final boolean isDownStream(BlockCache access, int x, int y, int z, int data, double dX, double dZ) {
      if ((data & 8) == 0) {
         if (dX > (double)0.0F) {
            if (data < 7 && isLiquid(access.getTypeId(x + 1, y, z)) && access.getData(x + 1, y, z) > data) {
               return true;
            }

            if (data > 0 && isLiquid(access.getTypeId(x - 1, y, z)) && access.getData(x - 1, y, z) < data) {
               return true;
            }
         } else if (dX < (double)0.0F) {
            if (data < 7 && isLiquid(access.getTypeId(x - 1, y, z)) && access.getData(x - 1, y, z) > data) {
               return true;
            }

            if (data > 0 && isLiquid(access.getTypeId(x + 1, y, z)) && access.getData(x + 1, y, z) < data) {
               return true;
            }
         }

         if (dZ > (double)0.0F) {
            if (data < 7 && isLiquid(access.getTypeId(x, y, z + 1)) && access.getData(x, y, z + 1) > data) {
               return true;
            }

            if (data > 0 && isLiquid(access.getTypeId(x, y, z - 1)) && access.getData(x, y, z - 1) < data) {
               return true;
            }
         } else if (dZ < (double)0.0F) {
            if (data < 7 && isLiquid(access.getTypeId(x, y, z - 1)) && access.getData(x, y, z - 1) > data) {
               return true;
            }

            if (data > 0 && isLiquid(access.getTypeId(x, y, z + 1)) && access.getData(x, y, z + 1) < data) {
               return true;
            }
         }
      }

      return false;
   }

   public static final long collectFlagsSimple(BlockCache access, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
      int iMinX = Location.locToBlock(minX);
      int iMaxX = Location.locToBlock(maxX);
      int iMinY = Location.locToBlock(minY);
      int iMaxY = Location.locToBlock(maxY);
      int iMinZ = Location.locToBlock(minZ);
      int iMaxZ = Location.locToBlock(maxZ);
      long flags = 0L;

      for(int x = iMinX; x <= iMaxX; ++x) {
         for(int z = iMinZ; z <= iMaxZ; ++z) {
            for(int y = iMinY; y <= iMaxY; ++y) {
               flags |= blockFlags[access.getTypeId(x, y, z)];
            }
         }
      }

      return flags;
   }

   public static float getBreakPenaltyInWater() {
      return breakPenaltyInWater;
   }

   public static void setBreakPenaltyInWater(float breakPenaltyInWater) {
      BlockProperties.breakPenaltyInWater = breakPenaltyInWater;
   }

   public static float getBreakPenaltyOffGround() {
      return breakPenaltyOffGround;
   }

   public static void setBreakPenaltyOffGround(float breakPenaltyOffGround) {
      BlockProperties.breakPenaltyOffGround = breakPenaltyOffGround;
   }

   public static void cleanup() {
      pLoc.cleanup();
      pLoc = null;
      blockCache.cleanup();
      blockCache = null;
   }

   public static final boolean isPassableRay(BlockCache access, int blockX, int blockY, int blockZ, double oX, double oY, double oZ, double dX, double dY, double dZ, double dT) {
      int id = access.getTypeId(blockX, blockY, blockZ);
      if (isPassable(id)) {
         return true;
      } else {
         double[] bounds = access.getBounds(blockX, blockY, blockZ);
         if (bounds == null) {
            return true;
         } else {
            double minX;
            double maxX;
            if (dX < (double)0.0F) {
               minX = dX * dT + oX + (double)blockX;
               maxX = oX + (double)blockX;
            } else {
               maxX = dX * dT + oX + (double)blockX;
               minX = oX + (double)blockX;
            }

            double minY;
            double maxY;
            if (dY < (double)0.0F) {
               minY = dY * dT + oY + (double)blockY;
               maxY = oY + (double)blockY;
            } else {
               maxY = dY * dT + oY + (double)blockY;
               minY = oY + (double)blockY;
            }

            double minZ;
            double maxZ;
            if (dX < (double)0.0F) {
               minZ = dZ * dT + oZ + (double)blockZ;
               maxZ = oZ + (double)blockZ;
            } else {
               maxZ = dZ * dT + oZ + (double)blockZ;
               minZ = oZ + (double)blockZ;
            }

            if (!collidesBlock(access, minX, minY, minZ, maxX, maxY, maxZ, blockX, blockY, blockZ, id, bounds, blockFlags[id])) {
               return true;
            } else {
               return isPassableWorkaround(access, blockX, blockY, blockZ, oX, oY, oZ, id, dX, dY, dZ, dT);
            }
         }
      }
   }

   static {
      noTool = new ToolProps(BlockProperties.ToolType.NONE, BlockProperties.MaterialBase.NONE);
      woodSword = new ToolProps(BlockProperties.ToolType.SWORD, BlockProperties.MaterialBase.WOOD);
      woodSpade = new ToolProps(BlockProperties.ToolType.SPADE, BlockProperties.MaterialBase.WOOD);
      woodPickaxe = new ToolProps(BlockProperties.ToolType.PICKAXE, BlockProperties.MaterialBase.WOOD);
      woodAxe = new ToolProps(BlockProperties.ToolType.AXE, BlockProperties.MaterialBase.WOOD);
      stonePickaxe = new ToolProps(BlockProperties.ToolType.PICKAXE, BlockProperties.MaterialBase.STONE);
      ironPickaxe = new ToolProps(BlockProperties.ToolType.PICKAXE, BlockProperties.MaterialBase.IRON);
      diamondPickaxe = new ToolProps(BlockProperties.ToolType.PICKAXE, BlockProperties.MaterialBase.DIAMOND);
      instantTimes = secToMs((double)0.0F);
      leafTimes = secToMs(0.3);
      glassTimes = secToMs(0.45);
      gravelTimes = secToMs(0.9, 0.45, (double)0.25F, 0.15, 0.15, 0.1);
      railsTimes = secToMs(1.05, 0.55, 0.3, 0.2, 0.15, 0.1);
      woodTimes = secToMs((double)3.0F, (double)1.5F, (double)0.75F, (double)0.5F, 0.4, (double)0.25F);
      ironTimes = secToMs((double)15.0F, (double)15.0F, 1.15, (double)0.75F, 0.6, (double)15.0F);
      diamondTimes = secToMs((double)15.0F, (double)15.0F, (double)15.0F, (double)0.75F, 0.6, (double)15.0F);
      indestructibleTimes = new long[]{Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE};
      instantType = new BlockProps(noTool, 0.0F, instantTimes);
      glassType = new BlockProps(noTool, 0.3F, glassTimes, 2.0F);
      gravelType = new BlockProps(woodSpade, 0.6F, gravelTimes);
      stoneType = new BlockProps(woodPickaxe, 1.5F);
      woodType = new BlockProps(woodAxe, 2.0F, woodTimes);
      brickType = new BlockProps(woodPickaxe, 2.0F);
      coalType = new BlockProps(woodPickaxe, 3.0F);
      ironType = new BlockProps(stonePickaxe, 3.0F, ironTimes);
      diamondType = new BlockProps(ironPickaxe, 3.0F, diamondTimes);
      goldBlockType = new BlockProps(woodPickaxe, 3.0F, secToMs((double)15.0F, (double)7.5F, (double)3.75F, 0.7, 0.55, 1.2));
      ironBlockType = new BlockProps(woodPickaxe, 5.0F, secToMs((double)25.0F, (double)12.5F, (double)2.0F, (double)1.25F, 0.95, (double)2.0F));
      diamondBlockType = new BlockProps(woodPickaxe, 5.0F, secToMs((double)25.0F, (double)12.5F, (double)6.0F, (double)1.25F, 0.95, (double)2.0F));
      hugeMushroomType = new BlockProps(woodAxe, 0.2F, secToMs(0.3, 0.15, 0.1, 0.05, 0.05, 0.05));
      leafType = new BlockProps(noTool, 0.2F, leafTimes);
      sandType = new BlockProps(woodSpade, 0.5F, secToMs((double)0.75F, 0.4, 0.2, 0.15, 0.1, 0.1));
      leverType = new BlockProps(noTool, 0.5F, secToMs((double)0.75F));
      sandStoneType = new BlockProps(woodPickaxe, 0.8F);
      pumpkinType = new BlockProps(woodAxe, 1.0F, secToMs((double)1.5F, (double)0.75F, 0.4, (double)0.25F, 0.2, 0.15));
      chestType = new BlockProps(woodAxe, 2.5F, secToMs((double)3.75F, 1.9, 0.95, 0.65, (double)0.5F, 0.35));
      woodDoorType = new BlockProps(woodAxe, 3.0F, secToMs((double)4.5F, (double)2.25F, 1.15, (double)0.75F, 0.6, 0.4));
      dispenserType = new BlockProps(woodPickaxe, 3.5F);
      ironDoorType = new BlockProps(woodPickaxe, 5.0F);
      indestructibleType = new BlockProps(noTool, -1.0F, indestructibleTimes);
      defaultBlockProps = instantType;
      instantMat = new Material[]{Material.CROPS, Material.TRIPWIRE_HOOK, Material.TRIPWIRE, Material.TORCH, Material.TNT, Material.SUGAR_CANE_BLOCK, Material.SAPLING, Material.RED_ROSE, Material.YELLOW_FLOWER, Material.REDSTONE_WIRE, Material.REDSTONE_TORCH_ON, Material.REDSTONE_TORCH_OFF, Material.DIODE_BLOCK_ON, Material.DIODE_BLOCK_OFF, Material.PUMPKIN_STEM, Material.NETHER_WARTS, Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.MELON_STEM, Material.WATER_LILY, Material.LONG_GRASS, Material.FIRE, Material.DEAD_BUSH, Material.CROPS, Material.COMMAND, Material.FLOWER_POT, Material.CARROT, Material.POTATO};
      blockCache = null;
      pLoc = null;
      blockFlags = new long[4096];
      flagNameMap = new LinkedHashMap();
      nameFlagMap = new LinkedHashMap();

      for(Field field : BlockProperties.class.getDeclaredFields()) {
         String name = field.getName();
         if (name.startsWith("F_")) {
            try {
               Long value = field.getLong(BlockProperties.class);
               flagNameMap.put(value, name.substring(2));
               nameFlagMap.put(name, value);
               nameFlagMap.put(name.substring(2), value);
            } catch (IllegalArgumentException var6) {
            } catch (IllegalAccessException var7) {
            }
         }
      }

      breakPenaltyInWater = 4.0F;
      breakPenaltyOffGround = 4.0F;
   }

   /** @deprecated */
   public static enum ToolType {
      NONE,
      SWORD,
      SHEARS,
      SPADE,
      AXE,
      PICKAXE;
   }

   /** @deprecated */
   public static enum MaterialBase {
      NONE(0, 1.0F),
      WOOD(1, 2.0F),
      STONE(2, 4.0F),
      IRON(3, 6.0F),
      DIAMOND(4, 8.0F),
      GOLD(5, 12.0F);

      public final int index;
      public final float breakMultiplier;

      private MaterialBase(int index, float breakMultiplier) {
         this.index = index;
         this.breakMultiplier = breakMultiplier;
      }

      public static final MaterialBase getById(int id) {
         for(MaterialBase base : values()) {
            if (base.index == id) {
               return base;
            }
         }

         throw new IllegalArgumentException("Bad id: " + id);
      }
   }

   /** @deprecated */
   public static class ToolProps {
      public final ToolType toolType;
      public final MaterialBase materialBase;

      public ToolProps(ToolType toolType, MaterialBase materialBase) {
         this.toolType = toolType;
         this.materialBase = materialBase;
      }

      public String toString() {
         return "ToolProps(" + this.toolType + "/" + this.materialBase + ")";
      }

      public void validate() {
         if (this.toolType == null) {
            throw new IllegalArgumentException("ToolType must not be null.");
         } else if (this.materialBase == null) {
            throw new IllegalArgumentException("MaterialBase must not be null");
         }
      }
   }

   /** @deprecated */
   public static class BlockProps {
      public final ToolProps tool;
      public final long[] breakingTimes;
      public final float hardness;
      public final float efficiencyMod;

      public BlockProps(ToolProps tool, float hardness) {
         this(tool, hardness, 1.0F);
      }

      public BlockProps(ToolProps tool, float hardness, float efficiencyMod) {
         this.tool = tool;
         this.hardness = hardness;
         this.breakingTimes = new long[6];

         for(int i = 0; i < 6; ++i) {
            float multiplier;
            if (tool.materialBase == null) {
               multiplier = 1.0F;
            } else if (i < tool.materialBase.index) {
               multiplier = 1.0F;
            } else {
               multiplier = BlockProperties.MaterialBase.getById(i).breakMultiplier * 3.33F;
            }

            this.breakingTimes[i] = (long)(5000.0F * hardness / multiplier);
         }

         this.efficiencyMod = efficiencyMod;
      }

      public BlockProps(ToolProps tool, float hardness, long[] breakingTimes) {
         this(tool, hardness, breakingTimes, 1.0F);
      }

      public BlockProps(ToolProps tool, float hardness, long[] breakingTimes, float efficiencyMod) {
         this.tool = tool;
         this.breakingTimes = breakingTimes;
         this.hardness = hardness;
         this.efficiencyMod = efficiencyMod;
      }

      public String toString() {
         return "BlockProps(" + this.hardness + " / " + this.tool.toString() + " / " + Arrays.toString(this.breakingTimes) + ")";
      }

      public void validate() {
         if (this.breakingTimes == null) {
            throw new IllegalArgumentException("Breaking times must not be null.");
         } else if (this.breakingTimes.length != 6) {
            throw new IllegalArgumentException("Breaking times length must match the number of available tool types (6).");
         } else if (this.tool == null) {
            throw new IllegalArgumentException("Tool must not be null.");
         } else {
            this.tool.validate();
         }
      }
   }
}
