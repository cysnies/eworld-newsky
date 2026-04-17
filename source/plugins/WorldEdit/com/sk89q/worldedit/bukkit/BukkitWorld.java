package com.sk89q.worldedit.bukkit;

import com.sk89q.worldedit.BiomeType;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalEntity;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.blocks.ContainerBlock;
import com.sk89q.worldedit.blocks.FurnaceBlock;
import com.sk89q.worldedit.blocks.MobSpawnerBlock;
import com.sk89q.worldedit.blocks.NoteBlock;
import com.sk89q.worldedit.blocks.SignBlock;
import com.sk89q.worldedit.blocks.SkullBlock;
import com.sk89q.worldedit.bukkit.entity.BukkitEntity;
import com.sk89q.worldedit.foundation.Block;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.TreeGenerator;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Furnace;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Ambient;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Golem;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class BukkitWorld extends LocalWorld {
   private static final Logger logger;
   private World world;
   private static boolean skipNmsAccess;
   private static boolean skipNmsSafeSet;
   private static boolean skipNmsValidBlockCheck;
   private static Class nmsBlockType;
   private static Method nmsSetMethod;
   private static Method nmsValidBlockMethod;
   private static Method nmsGetMethod;
   private static Method nmsSetSafeMethod;
   private static EntityType tntMinecartType;
   private static boolean checkMinecartType;
   private static final EnumMap treeTypeMapping;
   private static final Map effects;

   private static Enum tryEnum(Class enumType, String... values) {
      for(String val : values) {
         try {
            return Enum.valueOf(enumType, val);
         } catch (IllegalArgumentException var7) {
         }
      }

      return null;
   }

   public BukkitWorld(World world) {
      this.world = world;
      if (checkMinecartType) {
         tntMinecartType = (EntityType)tryEnum(EntityType.class, "MINECART_TNT");
         checkMinecartType = false;
      }

      if (nmsBlockType == null && !skipNmsAccess && !skipNmsSafeSet && !skipNmsValidBlockCheck) {
         Plugin plugin = Bukkit.getPluginManager().getPlugin("WorldEdit");
         if (plugin instanceof WorldEditPlugin) {
            WorldEditPlugin wePlugin = (WorldEditPlugin)plugin;
            File nmsBlocksDir = new File(wePlugin.getDataFolder() + File.separator + "nmsblocks" + File.separator);
            if (nmsBlocksDir.listFiles() == null) {
               skipNmsAccess = true;
               skipNmsSafeSet = true;
               skipNmsValidBlockCheck = true;
            } else {
               try {
                  NmsBlockClassLoader loader = new NmsBlockClassLoader(BukkitWorld.class.getClassLoader(), nmsBlocksDir);

                  for(File f : nmsBlocksDir.listFiles()) {
                     if (f.isFile()) {
                        String filename = f.getName();
                        Class<?> testBlock = null;

                        try {
                           testBlock = loader.loadClass("CL-NMS" + filename);
                        } catch (Throwable var17) {
                           continue;
                        }

                        filename = filename.replaceFirst(".class$", "");
                        if (NmsBlock.class.isAssignableFrom(testBlock)) {
                           Class<? extends NmsBlock> nmsClass = testBlock;
                           boolean canUse = false;

                           try {
                              canUse = (Boolean)nmsClass.getMethod("verify").invoke((Object)null);
                           } catch (Throwable var16) {
                              continue;
                           }

                           if (canUse) {
                              nmsBlockType = testBlock;
                              nmsSetMethod = nmsBlockType.getMethod("set", World.class, Vector.class, BaseBlock.class);
                              nmsValidBlockMethod = nmsBlockType.getMethod("isValidBlockType", Integer.TYPE);
                              nmsGetMethod = nmsBlockType.getMethod("get", World.class, Vector.class, Integer.TYPE, Integer.TYPE);
                              nmsSetSafeMethod = nmsBlockType.getMethod("setSafely", BukkitWorld.class, Vector.class, Block.class, Boolean.TYPE);
                              break;
                           }
                        }
                     }
                  }

                  if (nmsBlockType != null) {
                     logger.info("[WorldEdit] Using external NmsBlock for this version: " + nmsBlockType.getName());
                  } else {
                     try {
                        nmsBlockType = Class.forName("com.sk89q.worldedit.bukkit.DefaultNmsBlock");
                        boolean canUse = (Boolean)nmsBlockType.getMethod("verify").invoke((Object)null);
                        if (canUse) {
                           nmsSetMethod = nmsBlockType.getMethod("set", World.class, Vector.class, BaseBlock.class);
                           nmsValidBlockMethod = nmsBlockType.getMethod("isValidBlockType", Integer.TYPE);
                           nmsGetMethod = nmsBlockType.getMethod("get", World.class, Vector.class, Integer.TYPE, Integer.TYPE);
                           nmsSetSafeMethod = nmsBlockType.getMethod("setSafely", BukkitWorld.class, Vector.class, Block.class, Boolean.TYPE);
                           logger.info("[WorldEdit] Using inbuilt NmsBlock for this version.");
                        }
                     } catch (Throwable var15) {
                        skipNmsAccess = true;
                        skipNmsSafeSet = true;
                        skipNmsValidBlockCheck = true;
                        logger.warning("[WorldEdit] No compatible nms block class found.");
                     }
                  }
               } catch (Throwable e) {
                  logger.warning("[WorldEdit] Unable to load NmsBlock classes, make sure they are installed correctly.");
                  e.printStackTrace();
                  skipNmsAccess = true;
                  skipNmsSafeSet = true;
                  skipNmsValidBlockCheck = true;
               }

            }
         }
      }
   }

   public World getWorld() {
      return this.world;
   }

   public String getName() {
      return this.world.getName();
   }

   public boolean setBlockType(Vector pt, int type) {
      return this.world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).setTypeId(type);
   }

   public boolean setBlockTypeFast(Vector pt, int type) {
      return this.world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).setTypeId(type, false);
   }

   public boolean setTypeIdAndData(Vector pt, int type, int data) {
      return this.world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).setTypeIdAndData(type, (byte)data, true);
   }

   public boolean setTypeIdAndDataFast(Vector pt, int type, int data) {
      return this.world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).setTypeIdAndData(type, (byte)data, false);
   }

   public int getBlockType(Vector pt) {
      return this.world.getBlockTypeIdAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
   }

   public void setBlockData(Vector pt, int data) {
      this.world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).setData((byte)data);
   }

   public void setBlockDataFast(Vector pt, int data) {
      this.world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).setData((byte)data, false);
   }

   public int getBlockData(Vector pt) {
      return this.world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).getData();
   }

   public int getBlockLightLevel(Vector pt) {
      return this.world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).getLightLevel();
   }

   public BiomeType getBiome(Vector2D pt) {
      Biome bukkitBiome = this.world.getBiome(pt.getBlockX(), pt.getBlockZ());

      try {
         return BukkitBiomeType.valueOf(bukkitBiome.name());
      } catch (IllegalArgumentException var4) {
         return BiomeType.UNKNOWN;
      }
   }

   public void setBiome(Vector2D pt, BiomeType biome) {
      if (biome instanceof BukkitBiomeType) {
         Biome bukkitBiome = ((BukkitBiomeType)biome).getBukkitBiome();
         this.world.setBiome(pt.getBlockX(), pt.getBlockZ(), bukkitBiome);
      }

   }

   public boolean regenerate(Region region, EditSession editSession) {
      BaseBlock[] history = new BaseBlock[256 * (this.getMaxY() + 1)];

      for(Vector2D chunk : region.getChunks()) {
         Vector min = new Vector(chunk.getBlockX() * 16, 0, chunk.getBlockZ() * 16);

         for(int x = 0; x < 16; ++x) {
            for(int y = 0; y < this.getMaxY() + 1; ++y) {
               for(int z = 0; z < 16; ++z) {
                  Vector pt = min.add(x, y, z);
                  int index = y * 16 * 16 + z * 16 + x;
                  history[index] = editSession.getBlock(pt);
               }
            }
         }

         try {
            this.world.regenerateChunk(chunk.getBlockX(), chunk.getBlockZ());
         } catch (Throwable t) {
            t.printStackTrace();
         }

         for(int x = 0; x < 16; ++x) {
            for(int y = 0; y < this.getMaxY() + 1; ++y) {
               for(int z = 0; z < 16; ++z) {
                  Vector pt = min.add(x, y, z);
                  int index = y * 16 * 16 + z * 16 + x;
                  if (!region.contains(pt)) {
                     editSession.smartSetBlock(pt, history[index]);
                  } else {
                     editSession.rememberChange(pt, history[index], editSession.rawGetBlock(pt));
                  }
               }
            }
         }
      }

      return true;
   }

   public boolean copyToWorld(Vector pt, BaseBlock block) {
      if (block instanceof SignBlock) {
         this.setSignText(pt, ((SignBlock)block).getText());
         return true;
      } else if (block instanceof FurnaceBlock) {
         org.bukkit.block.Block bukkitBlock = this.world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
         if (bukkitBlock == null) {
            return false;
         } else {
            BlockState state = bukkitBlock.getState();
            if (!(state instanceof Furnace)) {
               return false;
            } else {
               Furnace bukkit = (Furnace)state;
               FurnaceBlock we = (FurnaceBlock)block;
               bukkit.setBurnTime(we.getBurnTime());
               bukkit.setCookTime(we.getCookTime());
               return this.setContainerBlockContents(pt, ((ContainerBlock)block).getItems());
            }
         }
      } else if (block instanceof ContainerBlock) {
         return this.setContainerBlockContents(pt, ((ContainerBlock)block).getItems());
      } else if (block instanceof MobSpawnerBlock) {
         org.bukkit.block.Block bukkitBlock = this.world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
         if (bukkitBlock == null) {
            return false;
         } else {
            BlockState state = bukkitBlock.getState();
            if (!(state instanceof CreatureSpawner)) {
               return false;
            } else {
               CreatureSpawner bukkit = (CreatureSpawner)state;
               MobSpawnerBlock we = (MobSpawnerBlock)block;
               bukkit.setCreatureTypeByName(we.getMobType());
               bukkit.setDelay(we.getDelay());
               return true;
            }
         }
      } else if (block instanceof NoteBlock) {
         org.bukkit.block.Block bukkitBlock = this.world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
         if (bukkitBlock == null) {
            return false;
         } else {
            BlockState state = bukkitBlock.getState();
            if (!(state instanceof org.bukkit.block.NoteBlock)) {
               return false;
            } else {
               org.bukkit.block.NoteBlock bukkit = (org.bukkit.block.NoteBlock)state;
               NoteBlock we = (NoteBlock)block;
               bukkit.setRawNote(we.getNote());
               return true;
            }
         }
      } else if (block instanceof SkullBlock) {
         org.bukkit.block.Block bukkitBlock = this.world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
         if (bukkitBlock == null) {
            return false;
         } else {
            BlockState state = bukkitBlock.getState();
            if (!(state instanceof Skull)) {
               return false;
            } else {
               Skull bukkit = (Skull)state;
               SkullBlock we = (SkullBlock)block;
               SkullType skullType = SkullType.SKELETON;
               switch (we.getSkullType()) {
                  case 0:
                     skullType = SkullType.SKELETON;
                     break;
                  case 1:
                     skullType = SkullType.WITHER;
                     break;
                  case 2:
                     skullType = SkullType.ZOMBIE;
                     break;
                  case 3:
                     skullType = SkullType.PLAYER;
                     break;
                  case 4:
                     skullType = SkullType.CREEPER;
               }

               bukkit.setSkullType(skullType);
               BlockFace rotation;
               switch (we.getRot()) {
                  case 0:
                     rotation = BlockFace.NORTH;
                     break;
                  case 1:
                     rotation = BlockFace.NORTH_NORTH_EAST;
                     break;
                  case 2:
                     rotation = BlockFace.NORTH_EAST;
                     break;
                  case 3:
                     rotation = BlockFace.EAST_NORTH_EAST;
                     break;
                  case 4:
                     rotation = BlockFace.EAST;
                     break;
                  case 5:
                     rotation = BlockFace.EAST_SOUTH_EAST;
                     break;
                  case 6:
                     rotation = BlockFace.SOUTH_EAST;
                     break;
                  case 7:
                     rotation = BlockFace.SOUTH_SOUTH_EAST;
                     break;
                  case 8:
                     rotation = BlockFace.SOUTH;
                     break;
                  case 9:
                     rotation = BlockFace.SOUTH_SOUTH_WEST;
                     break;
                  case 10:
                     rotation = BlockFace.SOUTH_WEST;
                     break;
                  case 11:
                     rotation = BlockFace.WEST_SOUTH_WEST;
                     break;
                  case 12:
                     rotation = BlockFace.WEST;
                     break;
                  case 13:
                     rotation = BlockFace.WEST_NORTH_WEST;
                     break;
                  case 14:
                     rotation = BlockFace.NORTH_WEST;
                     break;
                  case 15:
                     rotation = BlockFace.NORTH_NORTH_WEST;
                     break;
                  default:
                     rotation = BlockFace.NORTH;
               }

               bukkit.setRotation(rotation);
               if (we.getOwner() != null && !we.getOwner().isEmpty()) {
                  bukkit.setOwner(we.getOwner());
               }

               bukkit.update(true);
               return true;
            }
         }
      } else {
         if (!skipNmsAccess) {
            try {
               return (Boolean)nmsSetMethod.invoke((Object)null, this.world, pt, block);
            } catch (Throwable t) {
               logger.log(Level.WARNING, "WorldEdit: Failed to do NMS access for direct NBT data copy", t);
               skipNmsAccess = true;
            }
         }

         return false;
      }
   }

   public boolean copyFromWorld(Vector pt, BaseBlock block) {
      if (block instanceof SignBlock) {
         ((SignBlock)block).setText(this.getSignText(pt));
         return true;
      } else if (block instanceof FurnaceBlock) {
         org.bukkit.block.Block bukkitBlock = this.world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
         if (bukkitBlock == null) {
            return false;
         } else {
            BlockState state = bukkitBlock.getState();
            if (!(state instanceof Furnace)) {
               return false;
            } else {
               Furnace bukkit = (Furnace)state;
               FurnaceBlock we = (FurnaceBlock)block;
               we.setBurnTime(bukkit.getBurnTime());
               we.setCookTime(bukkit.getCookTime());
               ((ContainerBlock)block).setItems(this.getContainerBlockContents(pt));
               return true;
            }
         }
      } else if (block instanceof ContainerBlock) {
         ((ContainerBlock)block).setItems(this.getContainerBlockContents(pt));
         return true;
      } else if (block instanceof MobSpawnerBlock) {
         org.bukkit.block.Block bukkitBlock = this.world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
         if (bukkitBlock == null) {
            return false;
         } else {
            BlockState state = bukkitBlock.getState();
            if (!(state instanceof CreatureSpawner)) {
               return false;
            } else {
               CreatureSpawner bukkit = (CreatureSpawner)state;
               MobSpawnerBlock we = (MobSpawnerBlock)block;
               we.setMobType(bukkit.getCreatureTypeName());
               we.setDelay((short)bukkit.getDelay());
               return true;
            }
         }
      } else if (block instanceof NoteBlock) {
         org.bukkit.block.Block bukkitBlock = this.world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
         if (bukkitBlock == null) {
            return false;
         } else {
            BlockState state = bukkitBlock.getState();
            if (!(state instanceof org.bukkit.block.NoteBlock)) {
               return false;
            } else {
               org.bukkit.block.NoteBlock bukkit = (org.bukkit.block.NoteBlock)state;
               NoteBlock we = (NoteBlock)block;
               we.setNote(bukkit.getRawNote());
               return true;
            }
         }
      } else if (block instanceof SkullBlock) {
         org.bukkit.block.Block bukkitBlock = this.world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
         if (bukkitBlock == null) {
            return false;
         } else {
            BlockState state = bukkitBlock.getState();
            if (!(state instanceof Skull)) {
               return false;
            } else {
               Skull bukkit = (Skull)state;
               SkullBlock we = (SkullBlock)block;
               byte skullType = 0;
               switch (bukkit.getSkullType()) {
                  case SKELETON:
                     skullType = 0;
                     break;
                  case WITHER:
                     skullType = 1;
                     break;
                  case ZOMBIE:
                     skullType = 2;
                     break;
                  case PLAYER:
                     skullType = 3;
                     break;
                  case CREEPER:
                     skullType = 4;
               }

               we.setSkullType(skullType);
               byte rot = 0;
               switch (bukkit.getRotation()) {
                  case NORTH:
                     rot = 0;
                     break;
                  case NORTH_NORTH_EAST:
                     rot = 1;
                     break;
                  case NORTH_EAST:
                     rot = 2;
                     break;
                  case EAST_NORTH_EAST:
                     rot = 3;
                     break;
                  case EAST:
                     rot = 4;
                     break;
                  case EAST_SOUTH_EAST:
                     rot = 5;
                     break;
                  case SOUTH_EAST:
                     rot = 6;
                     break;
                  case SOUTH_SOUTH_EAST:
                     rot = 7;
                     break;
                  case SOUTH:
                     rot = 8;
                     break;
                  case SOUTH_SOUTH_WEST:
                     rot = 9;
                     break;
                  case SOUTH_WEST:
                     rot = 10;
                     break;
                  case WEST_SOUTH_WEST:
                     rot = 11;
                     break;
                  case WEST:
                     rot = 12;
                     break;
                  case WEST_NORTH_WEST:
                     rot = 13;
                     break;
                  case NORTH_WEST:
                     rot = 14;
                     break;
                  case NORTH_NORTH_WEST:
                     rot = 15;
               }

               we.setRot(rot);
               we.setOwner(bukkit.hasOwner() ? bukkit.getOwner() : "");
               return true;
            }
         }
      } else {
         return false;
      }
   }

   private Inventory getBlockInventory(Chest chest) {
      try {
         return chest.getBlockInventory();
      } catch (Throwable var4) {
         if (chest.getInventory() instanceof DoubleChestInventory) {
            DoubleChestInventory inven = (DoubleChestInventory)chest.getInventory();
            if (inven.getLeftSide().getHolder().equals(chest)) {
               return inven.getLeftSide();
            } else {
               return (Inventory)(inven.getRightSide().getHolder().equals(chest) ? inven.getRightSide() : inven);
            }
         } else {
            return chest.getInventory();
         }
      }
   }

   public boolean clearContainerBlockContents(Vector pt) {
      org.bukkit.block.Block block = this.world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
      if (block == null) {
         return false;
      } else {
         BlockState state = block.getState();
         if (!(state instanceof InventoryHolder)) {
            return false;
         } else {
            InventoryHolder chest = (InventoryHolder)state;
            Inventory inven = chest.getInventory();
            if (chest instanceof Chest) {
               inven = this.getBlockInventory((Chest)chest);
            }

            inven.clear();
            return true;
         }
      }
   }

   /** @deprecated */
   @Deprecated
   public boolean generateTree(EditSession editSession, Vector pt) {
      return this.generateTree(TreeGenerator.TreeType.TREE, editSession, pt);
   }

   /** @deprecated */
   @Deprecated
   public boolean generateBigTree(EditSession editSession, Vector pt) {
      return this.generateTree(TreeGenerator.TreeType.BIG_TREE, editSession, pt);
   }

   /** @deprecated */
   @Deprecated
   public boolean generateBirchTree(EditSession editSession, Vector pt) {
      return this.generateTree(TreeGenerator.TreeType.BIRCH, editSession, pt);
   }

   /** @deprecated */
   @Deprecated
   public boolean generateRedwoodTree(EditSession editSession, Vector pt) {
      return this.generateTree(TreeGenerator.TreeType.REDWOOD, editSession, pt);
   }

   /** @deprecated */
   @Deprecated
   public boolean generateTallRedwoodTree(EditSession editSession, Vector pt) {
      return this.generateTree(TreeGenerator.TreeType.TALL_REDWOOD, editSession, pt);
   }

   public static TreeType toBukkitTreeType(TreeGenerator.TreeType type) {
      return (TreeType)treeTypeMapping.get(type);
   }

   public boolean generateTree(TreeGenerator.TreeType type, EditSession editSession, Vector pt) {
      TreeType bukkitType = toBukkitTreeType(type);
      return type != null && this.world.generateTree(BukkitUtil.toLocation(this.world, pt), bukkitType, new EditSessionBlockChangeDelegate(editSession));
   }

   public void dropItem(Vector pt, BaseItemStack item) {
      ItemStack bukkitItem = new ItemStack(item.getType(), item.getAmount(), item.getData());
      this.world.dropItemNaturally(BukkitUtil.toLocation(this.world, pt), bukkitItem);
   }

   public int killMobs(Vector origin, double radius, int flags) {
      boolean killPets = (flags & 1) != 0;
      boolean killNPCs = (flags & 2) != 0;
      boolean killAnimals = (flags & 4) != 0;
      boolean withLightning = (flags & 1048576) != 0;
      boolean killGolems = (flags & 8) != 0;
      boolean killAmbient = (flags & 16) != 0;
      int num = 0;
      double radiusSq = radius * radius;
      Location bukkitOrigin = BukkitUtil.toLocation(this.world, origin);

      for(LivingEntity ent : this.world.getLivingEntities()) {
         if (!(ent instanceof HumanEntity) && (killAnimals || !(ent instanceof Animals)) && (killPets || !(ent instanceof Tameable) || !((Tameable)ent).isTamed()) && (killGolems || !(ent instanceof Golem)) && (killNPCs || !(ent instanceof Villager)) && (killAmbient || !(ent instanceof Ambient)) && (radius < (double)0.0F || bukkitOrigin.distanceSquared(ent.getLocation()) <= radiusSq)) {
            if (withLightning) {
               this.world.strikeLightningEffect(ent.getLocation());
            }

            ent.remove();
            ++num;
         }
      }

      return num;
   }

   public int removeEntities(com.sk89q.worldedit.EntityType type, Vector origin, int radius) {
      int num = 0;
      double radiusSq = Math.pow((double)radius, (double)2.0F);

      for(Entity ent : this.world.getEntities()) {
         if (radius == -1 || !(origin.distanceSq(BukkitUtil.toVector(ent.getLocation())) > radiusSq)) {
            switch (type) {
               case ALL:
                  if (ent instanceof Projectile || ent instanceof Boat || ent instanceof Item || ent instanceof FallingBlock || ent instanceof Minecart || ent instanceof Hanging || ent instanceof TNTPrimed || ent instanceof ExperienceOrb) {
                     ent.remove();
                     ++num;
                  }
                  break;
               case PROJECTILES:
               case ARROWS:
                  if (ent instanceof Projectile) {
                     ent.remove();
                     ++num;
                  }
                  break;
               case BOATS:
                  if (ent instanceof Boat) {
                     ent.remove();
                     ++num;
                  }
                  break;
               case ITEMS:
                  if (ent instanceof Item) {
                     ent.remove();
                     ++num;
                  }
                  break;
               case FALLING_BLOCKS:
                  if (ent instanceof FallingBlock) {
                     ent.remove();
                     ++num;
                  }
                  break;
               case MINECARTS:
                  if (ent instanceof Minecart) {
                     ent.remove();
                     ++num;
                  }
                  break;
               case PAINTINGS:
                  if (ent instanceof Painting) {
                     ent.remove();
                     ++num;
                  }
                  break;
               case ITEM_FRAMES:
                  if (ent instanceof ItemFrame) {
                     ent.remove();
                     ++num;
                  }
                  break;
               case TNT:
                  if (ent instanceof TNTPrimed || ent.getType() == tntMinecartType) {
                     ent.remove();
                     ++num;
                  }
                  break;
               case XP_ORBS:
                  if (ent instanceof ExperienceOrb) {
                     ent.remove();
                     ++num;
                  }
            }
         }
      }

      return num;
   }

   private boolean setSignText(Vector pt, String[] text) {
      org.bukkit.block.Block block = this.world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
      if (block == null) {
         return false;
      } else {
         BlockState state = block.getState();
         if (state != null && state instanceof Sign) {
            Sign sign = (Sign)state;
            sign.setLine(0, text[0]);
            sign.setLine(1, text[1]);
            sign.setLine(2, text[2]);
            sign.setLine(3, text[3]);
            sign.update();
            return true;
         } else {
            return false;
         }
      }
   }

   private String[] getSignText(Vector pt) {
      org.bukkit.block.Block block = this.world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
      if (block == null) {
         return new String[]{"", "", "", ""};
      } else {
         BlockState state = block.getState();
         if (state != null && state instanceof Sign) {
            Sign sign = (Sign)state;
            String line0 = sign.getLine(0);
            String line1 = sign.getLine(1);
            String line2 = sign.getLine(2);
            String line3 = sign.getLine(3);
            return new String[]{line0 != null ? line0 : "", line1 != null ? line1 : "", line2 != null ? line2 : "", line3 != null ? line3 : ""};
         } else {
            return new String[]{"", "", "", ""};
         }
      }
   }

   private BaseItemStack[] getContainerBlockContents(Vector pt) {
      org.bukkit.block.Block block = this.world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
      if (block == null) {
         return new BaseItemStack[0];
      } else {
         BlockState state = block.getState();
         if (!(state instanceof InventoryHolder)) {
            return new BaseItemStack[0];
         } else {
            InventoryHolder container = (InventoryHolder)state;
            Inventory inven = container.getInventory();
            if (container instanceof Chest) {
               inven = this.getBlockInventory((Chest)container);
            }

            int size = inven.getSize();
            BaseItemStack[] contents = new BaseItemStack[size];

            for(int i = 0; i < size; ++i) {
               ItemStack bukkitStack = inven.getItem(i);
               if (bukkitStack != null && bukkitStack.getTypeId() > 0) {
                  contents[i] = new BaseItemStack(bukkitStack.getTypeId(), bukkitStack.getAmount(), bukkitStack.getDurability());

                  try {
                     for(Map.Entry entry : bukkitStack.getEnchantments().entrySet()) {
                        contents[i].getEnchantments().put(((Enchantment)entry.getKey()).getId(), entry.getValue());
                     }
                  } catch (Throwable var12) {
                  }
               }
            }

            return contents;
         }
      }
   }

   private boolean setContainerBlockContents(Vector pt, BaseItemStack[] contents) {
      org.bukkit.block.Block block = this.world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
      if (block == null) {
         return false;
      } else {
         BlockState state = block.getState();
         if (!(state instanceof InventoryHolder)) {
            return false;
         } else {
            InventoryHolder chest = (InventoryHolder)state;
            Inventory inven = chest.getInventory();
            if (chest instanceof Chest) {
               inven = this.getBlockInventory((Chest)chest);
            }

            int size = inven.getSize();

            for(int i = 0; i < size && i < contents.length; ++i) {
               if (contents[i] != null) {
                  ItemStack toAdd = new ItemStack(contents[i].getType(), contents[i].getAmount(), contents[i].getData());

                  try {
                     for(Map.Entry entry : contents[i].getEnchantments().entrySet()) {
                        toAdd.addEnchantment(Enchantment.getById((Integer)entry.getKey()), (Integer)entry.getValue());
                     }
                  } catch (Throwable var12) {
                  }

                  inven.setItem(i, toAdd);
               } else {
                  inven.setItem(i, (ItemStack)null);
               }
            }

            return true;
         }
      }
   }

   public boolean isValidBlockType(int type) {
      if (!skipNmsValidBlockCheck) {
         try {
            return (Boolean)nmsValidBlockMethod.invoke((Object)null, type);
         } catch (Throwable var3) {
            skipNmsValidBlockCheck = true;
         }
      }

      return Material.getMaterial(type) != null && Material.getMaterial(type).isBlock();
   }

   public void checkLoadedChunk(Vector pt) {
      if (!this.world.isChunkLoaded(pt.getBlockX() >> 4, pt.getBlockZ() >> 4)) {
         this.world.loadChunk(pt.getBlockX() >> 4, pt.getBlockZ() >> 4);
      }

   }

   public boolean equals(Object other) {
      return !(other instanceof BukkitWorld) ? false : ((BukkitWorld)other).world.equals(this.world);
   }

   public int hashCode() {
      return this.world.hashCode();
   }

   public int getMaxY() {
      return this.world.getMaxHeight() - 1;
   }

   public void fixAfterFastMode(Iterable chunks) {
      for(BlockVector2D chunkPos : chunks) {
         this.world.refreshChunk(chunkPos.getBlockX(), chunkPos.getBlockZ());
      }

   }

   public boolean playEffect(Vector position, int type, int data) {
      Effect effect = (Effect)effects.get(type);
      if (effect == null) {
         return false;
      } else {
         this.world.playEffect(BukkitUtil.toLocation(this.world, position), effect, data);
         return true;
      }
   }

   public void simulateBlockMine(Vector pt) {
      this.world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).breakNaturally();
   }

   public LocalEntity[] getEntities(Region region) {
      List<BukkitEntity> entities = new ArrayList();

      for(Vector2D pt : region.getChunks()) {
         if (this.world.isChunkLoaded(pt.getBlockX(), pt.getBlockZ())) {
            Entity[] ents = this.world.getChunkAt(pt.getBlockX(), pt.getBlockZ()).getEntities();

            for(Entity ent : ents) {
               if (region.contains(BukkitUtil.toVector(ent.getLocation()))) {
                  entities.add(BukkitUtil.toLocalEntity(ent));
               }
            }
         }
      }

      return (LocalEntity[])entities.toArray(new BukkitEntity[entities.size()]);
   }

   public int killEntities(LocalEntity... entities) {
      int amount = 0;
      Set<UUID> toKill = new HashSet();

      for(LocalEntity entity : entities) {
         toKill.add(((BukkitEntity)entity).getEntityId());
      }

      for(Entity entity : this.world.getEntities()) {
         if (toKill.contains(entity.getUniqueId())) {
            entity.remove();
            ++amount;
         }
      }

      return amount;
   }

   public BaseBlock getBlock(Vector pt) {
      int type = this.getBlockType(pt);
      int data = this.getBlockData(pt);
      switch (type) {
         case 25:
         case 63:
         case 68:
         case 144:
            return super.getBlock(pt);
         default:
            if (!skipNmsAccess) {
               try {
                  NmsBlock block = null;
                  block = (NmsBlock)nmsGetMethod.invoke((Object)null, this.getWorld(), pt, type, data);
                  if (block != null) {
                     return block;
                  }
               } catch (Throwable t) {
                  logger.log(Level.WARNING, "WorldEdit: Failed to do NMS access for direct NBT data copy", t);
                  skipNmsAccess = true;
               }
            }

            return super.getBlock(pt);
      }
   }

   public boolean setBlock(Vector pt, Block block, boolean notifyAdjacent) {
      if (!skipNmsSafeSet) {
         try {
            return (Boolean)nmsSetSafeMethod.invoke((Object)null, this, pt, block, notifyAdjacent);
         } catch (Throwable t) {
            logger.log(Level.WARNING, "WorldEdit: Failed to do NMS safe block set", t);
            skipNmsSafeSet = true;
         }
      }

      return super.setBlock(pt, block, notifyAdjacent);
   }

   static {
      logger = WorldEdit.logger;
      skipNmsAccess = false;
      skipNmsSafeSet = false;
      skipNmsValidBlockCheck = false;
      checkMinecartType = true;
      treeTypeMapping = new EnumMap(TreeGenerator.TreeType.class);
      treeTypeMapping.put(TreeGenerator.TreeType.SWAMP, TreeType.TREE);
      treeTypeMapping.put(TreeGenerator.TreeType.JUNGLE_BUSH, TreeType.TREE);

      try {
         treeTypeMapping.put(TreeGenerator.TreeType.SHORT_JUNGLE, TreeType.valueOf("SMALL_JUNGLE"));
      } catch (IllegalArgumentException var6) {
         treeTypeMapping.put(TreeGenerator.TreeType.SHORT_JUNGLE, TreeType.TREE);
      }

      for(TreeGenerator.TreeType type : TreeGenerator.TreeType.values()) {
         try {
            TreeType bukkitType = TreeType.valueOf(type.name());
            treeTypeMapping.put(type, bukkitType);
         } catch (IllegalArgumentException var5) {
         }
      }

      treeTypeMapping.put(TreeGenerator.TreeType.RANDOM, TreeType.BROWN_MUSHROOM);
      treeTypeMapping.put(TreeGenerator.TreeType.RANDOM_REDWOOD, TreeType.REDWOOD);
      treeTypeMapping.put(TreeGenerator.TreeType.PINE, TreeType.REDWOOD);

      for(TreeGenerator.TreeType type : TreeGenerator.TreeType.values()) {
         if (treeTypeMapping.get(type) == null) {
            WorldEdit.logger.severe("No TreeType mapping for TreeGenerator.TreeType." + type);
         }
      }

      effects = new HashMap();

      for(Effect effect : Effect.values()) {
         effects.put(effect.getId(), effect);
      }

   }

   private class NmsBlockClassLoader extends ClassLoader {
      public File searchDir;

      public NmsBlockClassLoader(ClassLoader parent, File searchDir) {
         super(parent);
         this.searchDir = searchDir;
      }

      public Class loadClass(String name) throws ClassNotFoundException {
         if (!name.startsWith("CL-NMS")) {
            return super.loadClass(name);
         } else {
            name = name.replace("CL-NMS", "");

            try {
               URL url = (new File(this.searchDir, name)).toURI().toURL();
               InputStream input = url.openConnection().getInputStream();
               ByteArrayOutputStream buffer = new ByteArrayOutputStream();

               for(int data = input.read(); data != -1; data = input.read()) {
                  buffer.write(data);
               }

               input.close();
               byte[] classData = buffer.toByteArray();
               return this.defineClass(name.replaceFirst(".class$", ""), classData, 0, classData.length);
            } catch (Throwable var7) {
               throw new ClassNotFoundException();
            }
         }
      }
   }
}
