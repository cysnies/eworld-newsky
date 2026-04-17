package com.sk89q.worldedit.data;

import com.sk89q.jnbt.ByteArrayTag;
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

public class OldChunk implements Chunk {
   private CompoundTag rootTag;
   private byte[] blocks;
   private byte[] data;
   private int rootX;
   private int rootZ;
   private Map tileEntities;
   private LocalWorld world;

   public OldChunk(LocalWorld world, CompoundTag tag) throws DataException {
      this.rootTag = tag;
      this.world = world;
      this.blocks = ((ByteArrayTag)NBTUtils.getChildTag(this.rootTag.getValue(), "Blocks", ByteArrayTag.class)).getValue();
      this.data = ((ByteArrayTag)NBTUtils.getChildTag(this.rootTag.getValue(), "Data", ByteArrayTag.class)).getValue();
      this.rootX = ((IntTag)NBTUtils.getChildTag(this.rootTag.getValue(), "xPos", IntTag.class)).getValue();
      this.rootZ = ((IntTag)NBTUtils.getChildTag(this.rootTag.getValue(), "zPos", IntTag.class)).getValue();
      int size = 32768;
      if (this.blocks.length != size) {
         throw new InvalidFormatException("Chunk blocks byte array expected to be " + size + " bytes; found " + this.blocks.length);
      } else if (this.data.length != size / 2) {
         throw new InvalidFormatException("Chunk block data byte array expected to be " + size + " bytes; found " + this.data.length);
      }
   }

   public int getBlockID(Vector pos) throws DataException {
      if (pos.getBlockY() >= 128) {
         return 0;
      } else {
         int x = pos.getBlockX() - this.rootX * 16;
         int y = pos.getBlockY();
         int z = pos.getBlockZ() - this.rootZ * 16;
         int index = y + z * 128 + x * 128 * 16;

         try {
            return this.blocks[index];
         } catch (IndexOutOfBoundsException var7) {
            throw new DataException("Chunk does not contain position " + pos);
         }
      }
   }

   public int getBlockData(Vector pos) throws DataException {
      if (pos.getBlockY() >= 128) {
         return 0;
      } else {
         int x = pos.getBlockX() - this.rootX * 16;
         int y = pos.getBlockY();
         int z = pos.getBlockZ() - this.rootZ * 16;
         int index = y + z * 128 + x * 128 * 16;
         boolean shift = index % 2 == 0;
         index /= 2;

         try {
            return !shift ? (this.data[index] & 240) >> 4 : this.data[index] & 15;
         } catch (IndexOutOfBoundsException var8) {
            throw new DataException("Chunk does not contain position " + pos);
         }
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
