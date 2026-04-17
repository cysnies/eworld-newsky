package com.sk89q.worldedit;

import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.schematic.SchematicFormat;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CuboidClipboard {
   private BaseBlock[][][] data;
   private Vector offset;
   private Vector origin;
   private Vector size;
   private List entities = new ArrayList();

   public CuboidClipboard(Vector size) {
      this.size = size;
      this.data = new BaseBlock[size.getBlockX()][size.getBlockY()][size.getBlockZ()];
      this.origin = new Vector();
      this.offset = new Vector();
   }

   public CuboidClipboard(Vector size, Vector origin) {
      this.size = size;
      this.data = new BaseBlock[size.getBlockX()][size.getBlockY()][size.getBlockZ()];
      this.origin = origin;
      this.offset = new Vector();
   }

   public CuboidClipboard(Vector size, Vector origin, Vector offset) {
      this.size = size;
      this.data = new BaseBlock[size.getBlockX()][size.getBlockY()][size.getBlockZ()];
      this.origin = origin;
      this.offset = offset;
   }

   public int getWidth() {
      return this.size.getBlockX();
   }

   public int getLength() {
      return this.size.getBlockZ();
   }

   public int getHeight() {
      return this.size.getBlockY();
   }

   public void rotate2D(int angle) {
      angle %= 360;
      if (angle % 90 == 0) {
         boolean reverse = angle < 0;
         int numRotations = Math.abs((int)Math.floor((double)angle / (double)90.0F));
         int width = this.getWidth();
         int length = this.getLength();
         int height = this.getHeight();
         Vector sizeRotated = this.size.transform2D((double)angle, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F);
         int shiftX = sizeRotated.getX() < (double)0.0F ? -sizeRotated.getBlockX() - 1 : 0;
         int shiftZ = sizeRotated.getZ() < (double)0.0F ? -sizeRotated.getBlockZ() - 1 : 0;
         BaseBlock[][][] newData = new BaseBlock[Math.abs(sizeRotated.getBlockX())][Math.abs(sizeRotated.getBlockY())][Math.abs(sizeRotated.getBlockZ())];

         for(int x = 0; x < width; ++x) {
            for(int z = 0; z < length; ++z) {
               Vector2D v = (new Vector2D(x, z)).transform2D((double)angle, (double)0.0F, (double)0.0F, (double)shiftX, (double)shiftZ);
               int newX = v.getBlockX();
               int newZ = v.getBlockZ();

               for(int y = 0; y < height; ++y) {
                  BaseBlock block = this.data[x][y][z];
                  newData[newX][y][newZ] = block;
                  if (block != null) {
                     if (reverse) {
                        for(int i = 0; i < numRotations; ++i) {
                           block.rotate90Reverse();
                        }
                     } else {
                        for(int i = 0; i < numRotations; ++i) {
                           block.rotate90();
                        }
                     }
                  }
               }
            }
         }

         this.data = newData;
         this.size = new Vector(Math.abs(sizeRotated.getBlockX()), Math.abs(sizeRotated.getBlockY()), Math.abs(sizeRotated.getBlockZ()));
         this.offset = this.offset.transform2D((double)angle, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F).subtract(shiftX, 0, shiftZ);
      }
   }

   public void flip(FlipDirection dir) {
      this.flip(dir, false);
   }

   public void flip(FlipDirection dir, boolean aroundPlayer) {
      int width = this.getWidth();
      int length = this.getLength();
      int height = this.getHeight();
      switch (dir) {
         case WEST_EAST:
            int wid = (int)Math.ceil((double)((float)width / 2.0F));

            for(int xs = 0; xs < wid; ++xs) {
               for(int z = 0; z < length; ++z) {
                  for(int y = 0; y < height; ++y) {
                     if (xs != width - xs - 1) {
                        BaseBlock block1 = this.data[xs][y][z];
                        if (block1 != null) {
                           block1.flip(dir);
                        }

                        BaseBlock block2 = this.data[width - xs - 1][y][z];
                        if (block2 != null) {
                           block2.flip(dir);
                        }

                        this.data[xs][y][z] = block2;
                        this.data[width - xs - 1][y][z] = block1;
                     }
                  }
               }
            }

            if (aroundPlayer) {
               this.offset = this.offset.setX((double)1.0F - this.offset.getX() - (double)width);
            }
            break;
         case NORTH_SOUTH:
            int len = (int)Math.ceil((double)((float)length / 2.0F));

            for(int zs = 0; zs < len; ++zs) {
               for(int x = 0; x < width; ++x) {
                  for(int y = 0; y < height; ++y) {
                     if (zs != length - zs - 1) {
                        BaseBlock block1 = this.data[x][y][zs];
                        if (block1 != null) {
                           block1.flip(dir);
                        }

                        BaseBlock block2 = this.data[x][y][length - zs - 1];
                        if (block2 != null) {
                           block2.flip(dir);
                        }

                        this.data[x][y][zs] = block2;
                        this.data[x][y][length - zs - 1] = block1;
                     }
                  }
               }
            }

            if (aroundPlayer) {
               this.offset = this.offset.setZ((double)1.0F - this.offset.getZ() - (double)length);
            }
            break;
         case UP_DOWN:
            int hei = (int)Math.ceil((double)((float)height / 2.0F));

            for(int ys = 0; ys < hei; ++ys) {
               for(int x = 0; x < width; ++x) {
                  for(int z = 0; z < length; ++z) {
                     if (ys != height - ys - 1) {
                        BaseBlock block1 = this.data[x][ys][z];
                        if (block1 != null) {
                           block1.flip(dir);
                        }

                        BaseBlock block2 = this.data[x][height - ys - 1][z];
                        if (block2 != null) {
                           block2.flip(dir);
                        }

                        this.data[x][ys][z] = block2;
                        this.data[x][height - ys - 1][z] = block1;
                     }
                  }
               }
            }

            if (aroundPlayer) {
               this.offset = this.offset.setY((double)1.0F - this.offset.getY() - (double)height);
            }
      }

   }

   public void copy(EditSession editSession) {
      for(int x = 0; x < this.size.getBlockX(); ++x) {
         for(int y = 0; y < this.size.getBlockY(); ++y) {
            for(int z = 0; z < this.size.getBlockZ(); ++z) {
               this.data[x][y][z] = editSession.getBlock((new Vector(x, y, z)).add(this.getOrigin()));
            }
         }
      }

   }

   public void copy(EditSession editSession, Region region) {
      for(int x = 0; x < this.size.getBlockX(); ++x) {
         for(int y = 0; y < this.size.getBlockY(); ++y) {
            for(int z = 0; z < this.size.getBlockZ(); ++z) {
               Vector pt = (new Vector(x, y, z)).add(this.getOrigin());
               if (region.contains(pt)) {
                  this.data[x][y][z] = editSession.getBlock(pt);
               } else {
                  this.data[x][y][z] = null;
               }
            }
         }
      }

   }

   public void paste(EditSession editSession, Vector newOrigin, boolean noAir) throws MaxChangedBlocksException {
      this.paste(editSession, newOrigin, noAir, false);
   }

   public void paste(EditSession editSession, Vector newOrigin, boolean noAir, boolean entities) throws MaxChangedBlocksException {
      this.place(editSession, newOrigin.add(this.offset), noAir);
      if (entities) {
         this.pasteEntities(newOrigin.add(this.offset));
      }

   }

   public void place(EditSession editSession, Vector pos, boolean noAir) throws MaxChangedBlocksException {
      for(int x = 0; x < this.size.getBlockX(); ++x) {
         for(int y = 0; y < this.size.getBlockY(); ++y) {
            for(int z = 0; z < this.size.getBlockZ(); ++z) {
               BaseBlock block = this.data[x][y][z];
               if (block != null && (!noAir || !block.isAir())) {
                  editSession.setBlock((new Vector(x, y, z)).add(pos), block);
               }
            }
         }
      }

   }

   public LocalEntity[] pasteEntities(Vector pos) {
      LocalEntity[] entities = new LocalEntity[this.entities.size()];

      for(int i = 0; i < this.entities.size(); ++i) {
         CopiedEntity copied = (CopiedEntity)this.entities.get(i);
         if (copied.entity.spawn(copied.entity.getPosition().setPosition(copied.relativePosition.add(pos)))) {
            entities[i] = copied.entity;
         }
      }

      return entities;
   }

   public void storeEntity(LocalEntity entity) {
      this.entities.add(new CopiedEntity(entity));
   }

   /** @deprecated */
   public BaseBlock getPoint(Vector pos) throws ArrayIndexOutOfBoundsException {
      BaseBlock block = this.getBlock(pos);
      return block == null ? new BaseBlock(0) : block;
   }

   public BaseBlock getBlock(Vector pos) throws ArrayIndexOutOfBoundsException {
      return this.data[pos.getBlockX()][pos.getBlockY()][pos.getBlockZ()];
   }

   public void setBlock(Vector pt, BaseBlock block) {
      this.data[pt.getBlockX()][pt.getBlockY()][pt.getBlockZ()] = block;
   }

   public Vector getSize() {
      return this.size;
   }

   /** @deprecated */
   @Deprecated
   public void saveSchematic(File path) throws IOException, DataException {
      SchematicFormat.MCEDIT.save(this, path);
   }

   /** @deprecated */
   @Deprecated
   public static CuboidClipboard loadSchematic(File path) throws DataException, IOException {
      return SchematicFormat.MCEDIT.load(path);
   }

   public Vector getOrigin() {
      return this.origin;
   }

   public void setOrigin(Vector origin) {
      this.origin = origin;
   }

   public Vector getOffset() {
      return this.offset;
   }

   public void setOffset(Vector offset) {
      this.offset = offset;
   }

   public List getBlockDistribution() {
      List<Countable<Integer>> distribution = new ArrayList();
      Map<Integer, Countable<Integer>> map = new HashMap();
      int maxX = this.getWidth();
      int maxY = this.getHeight();
      int maxZ = this.getLength();

      for(int x = 0; x < maxX; ++x) {
         for(int y = 0; y < maxY; ++y) {
            for(int z = 0; z < maxZ; ++z) {
               BaseBlock block = this.data[x][y][z];
               if (block != null) {
                  int id = block.getId();
                  if (map.containsKey(id)) {
                     ((Countable)map.get(id)).increment();
                  } else {
                     Countable<Integer> c = new Countable(id, 1);
                     map.put(id, c);
                     distribution.add(c);
                  }
               }
            }
         }
      }

      Collections.sort(distribution);
      return distribution;
   }

   public List getBlockDistributionWithData() {
      List<Countable<BaseBlock>> distribution = new ArrayList();
      Map<BaseBlock, Countable<BaseBlock>> map = new HashMap();
      int maxX = this.getWidth();
      int maxY = this.getHeight();
      int maxZ = this.getLength();

      for(int x = 0; x < maxX; ++x) {
         for(int y = 0; y < maxY; ++y) {
            for(int z = 0; z < maxZ; ++z) {
               BaseBlock block = this.data[x][y][z];
               if (block != null) {
                  BaseBlock bareBlock = new BaseBlock(block.getId(), block.getData());
                  if (map.containsKey(bareBlock)) {
                     ((Countable)map.get(bareBlock)).increment();
                  } else {
                     Countable<BaseBlock> c = new Countable(bareBlock, 1);
                     map.put(bareBlock, c);
                     distribution.add(c);
                  }
               }
            }
         }
      }

      Collections.sort(distribution);
      return distribution;
   }

   public static enum FlipDirection {
      NORTH_SOUTH,
      WEST_EAST,
      UP_DOWN;
   }

   private class CopiedEntity {
      private final LocalEntity entity;
      private final Vector relativePosition;

      public CopiedEntity(LocalEntity entity) {
         this.entity = entity;
         this.relativePosition = entity.getPosition().getPosition().subtract(CuboidClipboard.this.getOrigin());
      }
   }
}
