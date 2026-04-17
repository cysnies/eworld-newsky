package io.nv.bukkit.CleanroomGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

public class CleanroomChunkGenerator extends ChunkGenerator {
   private Logger log;
   private short[] layer;
   private byte[] layerDataValues;

   public CleanroomChunkGenerator() {
      this("64,stone");
   }

   public CleanroomChunkGenerator(String id) {
      this.log = Logger.getLogger("Minecraft");
      if (id != null) {
         try {
            int y = 0;
            this.layer = new short[128];
            this.layerDataValues = null;
            if (id.length() > 0 && id.charAt(0) == '.') {
               id = id.substring(1);
            } else {
               this.layer[y++] = (short)Material.BEDROCK.getId();
            }

            if (id.length() > 0) {
               String[] tokens = id.split("[,]");
               if (tokens.length % 2 != 0) {
                  throw new Exception();
               }

               for(int i = 0; i < tokens.length; i += 2) {
                  int height = Integer.parseInt(tokens[i]);
                  if (height <= 0) {
                     this.log.warning("[CleanroomGenerator] Invalid height '" + tokens[i] + "'. Using 64 instead.");
                     height = 64;
                  }

                  String[] materialTokens = tokens[i + 1].split("[:]", 2);
                  byte dataValue = 0;
                  if (materialTokens.length == 2) {
                     try {
                        dataValue = Byte.parseByte(materialTokens[1]);
                     } catch (Exception var12) {
                        this.log.warning("[CleanroomGenerator] Invalid Data Value '" + materialTokens[1] + "'. Defaulting to 0.");
                        dataValue = 0;
                     }
                  }

                  Material mat = Material.matchMaterial(materialTokens[0]);
                  if (mat == null) {
                     try {
                        mat = Material.getMaterial(Integer.parseInt(materialTokens[0]));
                     } catch (Exception var11) {
                     }

                     if (mat == null) {
                        this.log.warning("[CleanroomGenerator] Invalid Block ID '" + materialTokens[0] + "'. Defaulting to stone.");
                        mat = Material.STONE;
                     }
                  }

                  if (!mat.isBlock()) {
                     this.log.warning("[CleanroomGenerator] Error, '" + materialTokens[0] + "' is not a block. Defaulting to stone.");
                     mat = Material.STONE;
                  }

                  if (y + height > this.layer.length) {
                     short[] newLayer = new short[Math.max(y + height, this.layer.length * 2)];
                     System.arraycopy(this.layer, 0, newLayer, 0, y);
                     this.layer = newLayer;
                     if (this.layerDataValues != null) {
                        byte[] newLayerDataValues = new byte[Math.max(y + height, this.layerDataValues.length * 2)];
                        System.arraycopy(this.layerDataValues, 0, newLayerDataValues, 0, y);
                        this.layerDataValues = newLayerDataValues;
                     }
                  }

                  Arrays.fill(this.layer, y, y + height, (short)mat.getId());
                  if (dataValue != 0) {
                     if (this.layerDataValues == null) {
                        this.layerDataValues = new byte[this.layer.length];
                     }

                     Arrays.fill(this.layerDataValues, y, y + height, dataValue);
                  }

                  y += height;
               }
            }

            if (this.layer.length > y) {
               short[] newLayer = new short[y];
               System.arraycopy(this.layer, 0, newLayer, 0, y);
               this.layer = newLayer;
            }

            if (this.layerDataValues != null && this.layerDataValues.length > y) {
               byte[] newLayerDataValues = new byte[y];
               System.arraycopy(this.layerDataValues, 0, newLayerDataValues, 0, y);
               this.layerDataValues = newLayerDataValues;
            }
         } catch (Exception e) {
            this.log.severe("[CleanroomGenerator] Error parsing CleanroomGenerator ID '" + id + "'. using defaults '64,1': " + e.toString());
            e.printStackTrace();
            this.layerDataValues = null;
            this.layer = new short[65];
            this.layer[0] = (short)Material.BEDROCK.getId();
            Arrays.fill(this.layer, 1, 65, (short)Material.STONE.getId());
         }
      } else {
         this.layerDataValues = null;
         this.layer = new short[65];
         this.layer[0] = (short)Material.BEDROCK.getId();
         Arrays.fill(this.layer, 1, 65, (short)Material.STONE.getId());
      }

   }

   public short[][] generateExtBlockSections(World world, Random random, int x, int z, ChunkGenerator.BiomeGrid biomes) {
      int maxHeight = world.getMaxHeight();
      if (this.layer.length > maxHeight) {
         this.log.warning("[CleanroomGenerator] Error, chunk height " + this.layer.length + " is greater than the world max height (" + maxHeight + "). Trimming to world max height.");
         short[] newLayer = new short[maxHeight];
         System.arraycopy(this.layer, 0, newLayer, 0, maxHeight);
         this.layer = newLayer;
      }

      short[][] result = new short[maxHeight / 16][];

      for(int i = 0; i < this.layer.length; i += 16) {
         result[i >> 4] = new short[4096];

         for(int y = 0; y < Math.min(16, this.layer.length - i); ++y) {
            Arrays.fill(result[i >> 4], y * 16 * 16, (y + 1) * 16 * 16, this.layer[i + y]);
         }
      }

      return result;
   }

   public List getDefaultPopulators(World world) {
      return (List)(this.layerDataValues != null ? Arrays.asList(new CleanroomBlockPopulator(this.layerDataValues)) : new ArrayList());
   }

   public Location getFixedSpawnLocation(World world, Random random) {
      if (!world.isChunkLoaded(0, 0)) {
         world.loadChunk(0, 0);
      }

      return world.getHighestBlockYAt(0, 0) <= 0 && world.getBlockAt(0, 0, 0).getType() == Material.AIR ? new Location(world, (double)0.0F, (double)64.0F, (double)0.0F) : new Location(world, (double)0.0F, (double)world.getHighestBlockYAt(0, 0), (double)0.0F);
   }
}
