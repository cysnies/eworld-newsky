package com.sk89q.worldedit.data;

import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.ByteTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.NBTUtils;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.TileEntityBlock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnvilChunk implements Chunk {
   private CompoundTag rootTag;
   private byte[][] blocks;
   private byte[][] blocksAdd;
   private byte[][] data;
   private int rootX;
   private int rootZ;
   private Map tileEntities;
   private LocalWorld world;

   public AnvilChunk(LocalWorld world, CompoundTag tag) throws DataException {
      this.rootTag = tag;
      this.world = world;
      this.rootX = ((IntTag)NBTUtils.getChildTag(this.rootTag.getValue(), "xPos", IntTag.class)).getValue();
      this.rootZ = ((IntTag)NBTUtils.getChildTag(this.rootTag.getValue(), "zPos", IntTag.class)).getValue();
      this.blocks = new byte[16][4096];
      this.blocksAdd = new byte[16][2048];
      this.data = new byte[16][2048];

      for(Tag rawSectionTag : ((ListTag)NBTUtils.getChildTag(this.rootTag.getValue(), "Sections", ListTag.class)).getValue()) {
         if (rawSectionTag instanceof CompoundTag) {
            CompoundTag sectionTag = (CompoundTag)rawSectionTag;
            if (sectionTag.getValue().containsKey("Y")) {
               int y = ((ByteTag)NBTUtils.getChildTag(sectionTag.getValue(), "Y", ByteTag.class)).getValue();
               if (y >= 0 && y < 16) {
                  this.blocks[y] = ((ByteArrayTag)NBTUtils.getChildTag(sectionTag.getValue(), "Blocks", ByteArrayTag.class)).getValue();
                  this.data[y] = ((ByteArrayTag)NBTUtils.getChildTag(sectionTag.getValue(), "Data", ByteArrayTag.class)).getValue();
                  if (sectionTag.getValue().containsKey("Add")) {
                     this.blocksAdd[y] = ((ByteArrayTag)NBTUtils.getChildTag(sectionTag.getValue(), "Add", ByteArrayTag.class)).getValue();
                  }
               }
            }
         }
      }

      int sectionsize = 4096;

      for(int i = 0; i < this.blocks.length; ++i) {
         if (this.blocks[i].length != sectionsize) {
            throw new InvalidFormatException("Chunk blocks byte array expected to be " + sectionsize + " bytes; found " + this.blocks[i].length);
         }
      }

      for(int i = 0; i < this.data.length; ++i) {
         if (this.data[i].length != sectionsize / 2) {
            throw new InvalidFormatException("Chunk block data byte array expected to be " + sectionsize + " bytes; found " + this.data[i].length);
         }
      }

   }

   public int getBlockID(Vector pos) throws DataException {
      int x = pos.getBlockX() - this.rootX * 16;
      int y = pos.getBlockY();
      int z = pos.getBlockZ() - this.rootZ * 16;
      int section = y >> 4;
      if (section >= 0 && section < this.blocks.length) {
         int yindex = y & 15;
         if (yindex >= 0 && yindex < 16) {
            int index = x + z * 16 + yindex * 16 * 16;

            try {
               int addId = 0;
               if (index % 2 == 0) {
                  addId = (this.blocksAdd[section][index >> 1] & 15) << 8;
               } else {
                  addId = (this.blocksAdd[section][index >> 1] & 240) << 4;
               }

               return (this.blocks[section][index] & 255) + addId;
            } catch (IndexOutOfBoundsException var9) {
               throw new DataException("Chunk does not contain position " + pos);
            }
         } else {
            throw new DataException("Chunk does not contain position " + pos);
         }
      } else {
         throw new DataException("Chunk does not contain position " + pos);
      }
   }

   public int getBlockData(Vector pos) throws DataException {
      int x = pos.getBlockX() - this.rootX * 16;
      int y = pos.getBlockY();
      int z = pos.getBlockZ() - this.rootZ * 16;
      int section = y >> 4;
      int yIndex = y & 15;
      if (section >= 0 && section < this.blocks.length) {
         if (yIndex >= 0 && yIndex < 16) {
            int index = x + z * 16 + yIndex * 16 * 16;
            boolean shift = index % 2 == 0;
            index /= 2;

            try {
               return !shift ? (this.data[section][index] & 240) >> 4 : this.data[section][index] & 15;
            } catch (IndexOutOfBoundsException var10) {
               throw new DataException("Chunk does not contain position " + pos);
            }
         } else {
            throw new DataException("Chunk does not contain position " + pos);
         }
      } else {
         throw new DataException("Chunk does not contain position " + pos);
      }
   }

   private void populateTileEntities() throws DataException {
      List<Tag> tags = ((ListTag)NBTUtils.getChildTag(this.rootTag.getValue(), "TileEntities", ListTag.class)).getValue();
      this.tileEntities = new HashMap();

      for(Tag tag : tags) {
         if (!(tag instanceof CompoundTag)) {
            throw new InvalidFormatException("CompoundTag expected in TileEntities");
         }

         CompoundTag t = (CompoundTag)tag;
         int x = 0;
         int y = 0;
         int z = 0;
         Map<String, Tag> values = new HashMap();

         for(Map.Entry entry : t.getValue().entrySet()) {
            if (((String)entry.getKey()).equals("x")) {
               if (entry.getValue() instanceof IntTag) {
                  x = ((IntTag)entry.getValue()).getValue();
               }
            } else if (((String)entry.getKey()).equals("y")) {
               if (entry.getValue() instanceof IntTag) {
                  y = ((IntTag)entry.getValue()).getValue();
               }
            } else if (((String)entry.getKey()).equals("z") && entry.getValue() instanceof IntTag) {
               z = ((IntTag)entry.getValue()).getValue();
            }

            values.put(entry.getKey(), entry.getValue());
         }

         BlockVector vec = new BlockVector(x, y, z);
         this.tileEntities.put(vec, values);
      }

   }

   private CompoundTag getBlockTileEntity(Vector pos) throws DataException {
      if (this.tileEntities == null) {
         this.populateTileEntities();
      }

      Map<String, Tag> values = (Map)this.tileEntities.get(new BlockVector(pos));
      return values == null ? null : new CompoundTag("", values);
   }

   public BaseBlock getBlock(Vector pos) throws DataException {
      int id = this.getBlockID(pos);
      int data = this.getBlockData(pos);
      BaseBlock block = new BaseBlock(id, data);
      if (block instanceof TileEntityBlock) {
         CompoundTag tileEntity = this.getBlockTileEntity(pos);
         if (tileEntity != null) {
            block.setNbtData(tileEntity);
         }
      }

      return block;
   }
}
