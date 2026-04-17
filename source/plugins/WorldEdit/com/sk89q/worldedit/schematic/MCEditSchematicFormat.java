package com.sk89q.worldedit.schematic;

import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.NBTConstants;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.NBTOutputStream;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.TileEntityBlock;
import com.sk89q.worldedit.data.DataException;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class MCEditSchematicFormat extends SchematicFormat {
   private static final int MAX_SIZE = 65535;

   protected MCEditSchematicFormat() {
      super("MCEdit", "mcedit", "mce");
   }

   public CuboidClipboard load(File file) throws IOException, DataException {
      FileInputStream stream = new FileInputStream(file);
      NBTInputStream nbtStream = new NBTInputStream(new GZIPInputStream(stream));
      Vector origin = new Vector();
      Vector offset = new Vector();
      CompoundTag schematicTag = (CompoundTag)nbtStream.readTag();
      nbtStream.close();
      if (!schematicTag.getName().equals("Schematic")) {
         throw new DataException("Tag \"Schematic\" does not exist or is not first");
      } else {
         Map<String, Tag> schematic = schematicTag.getValue();
         if (!schematic.containsKey("Blocks")) {
            throw new DataException("Schematic file is missing a \"Blocks\" tag");
         } else {
            short width = ((ShortTag)getChildTag(schematic, "Width", ShortTag.class)).getValue();
            short length = ((ShortTag)getChildTag(schematic, "Length", ShortTag.class)).getValue();
            short height = ((ShortTag)getChildTag(schematic, "Height", ShortTag.class)).getValue();

            try {
               int originX = ((IntTag)getChildTag(schematic, "WEOriginX", IntTag.class)).getValue();
               int originY = ((IntTag)getChildTag(schematic, "WEOriginY", IntTag.class)).getValue();
               int originZ = ((IntTag)getChildTag(schematic, "WEOriginZ", IntTag.class)).getValue();
               origin = new Vector(originX, originY, originZ);
            } catch (DataException var28) {
            }

            try {
               int offsetX = ((IntTag)getChildTag(schematic, "WEOffsetX", IntTag.class)).getValue();
               int offsetY = ((IntTag)getChildTag(schematic, "WEOffsetY", IntTag.class)).getValue();
               int offsetZ = ((IntTag)getChildTag(schematic, "WEOffsetZ", IntTag.class)).getValue();
               offset = new Vector(offsetX, offsetY, offsetZ);
            } catch (DataException var27) {
            }

            String materials = ((StringTag)getChildTag(schematic, "Materials", StringTag.class)).getValue();
            if (!materials.equals("Alpha")) {
               throw new DataException("Schematic file is not an Alpha schematic");
            } else {
               byte[] blockId = ((ByteArrayTag)getChildTag(schematic, "Blocks", ByteArrayTag.class)).getValue();
               byte[] blockData = ((ByteArrayTag)getChildTag(schematic, "Data", ByteArrayTag.class)).getValue();
               byte[] addId = new byte[0];
               short[] blocks = new short[blockId.length];
               if (schematic.containsKey("AddBlocks")) {
                  addId = ((ByteArrayTag)getChildTag(schematic, "AddBlocks", ByteArrayTag.class)).getValue();
               }

               for(int index = 0; index < blockId.length; ++index) {
                  if (index >> 1 >= addId.length) {
                     blocks[index] = (short)(blockId[index] & 255);
                  } else if ((index & 1) == 0) {
                     blocks[index] = (short)(((addId[index >> 1] & 15) << 8) + (blockId[index] & 255));
                  } else {
                     blocks[index] = (short)(((addId[index >> 1] & 240) << 4) + (blockId[index] & 255));
                  }
               }

               List<Tag> tileEntities = ((ListTag)getChildTag(schematic, "TileEntities", ListTag.class)).getValue();
               Map<BlockVector, Map<String, Tag>> tileEntitiesMap = new HashMap();

               for(Tag tag : tileEntities) {
                  if (tag instanceof CompoundTag) {
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
                     tileEntitiesMap.put(vec, values);
                  }
               }

               Vector size = new Vector(width, height, length);
               CuboidClipboard clipboard = new CuboidClipboard(size);
               clipboard.setOrigin(origin);
               clipboard.setOffset(offset);

               for(int x = 0; x < width; ++x) {
                  for(int y = 0; y < height; ++y) {
                     for(int z = 0; z < length; ++z) {
                        int index = y * width * length + z * width + x;
                        BlockVector pt = new BlockVector(x, y, z);
                        BaseBlock block = this.getBlockForId(blocks[index], (short)blockData[index]);
                        if (block instanceof TileEntityBlock && tileEntitiesMap.containsKey(pt)) {
                           block.setNbtData(new CompoundTag("", (Map)tileEntitiesMap.get(pt)));
                        }

                        clipboard.setBlock(pt, block);
                     }
                  }
               }

               return clipboard;
            }
         }
      }
   }

   public void save(CuboidClipboard clipboard, File file) throws IOException, DataException {
      int width = clipboard.getWidth();
      int height = clipboard.getHeight();
      int length = clipboard.getLength();
      if (width > 65535) {
         throw new DataException("Width of region too large for a .schematic");
      } else if (height > 65535) {
         throw new DataException("Height of region too large for a .schematic");
      } else if (length > 65535) {
         throw new DataException("Length of region too large for a .schematic");
      } else {
         HashMap<String, Tag> schematic = new HashMap();
         schematic.put("Width", new ShortTag("Width", (short)width));
         schematic.put("Length", new ShortTag("Length", (short)length));
         schematic.put("Height", new ShortTag("Height", (short)height));
         schematic.put("Materials", new StringTag("Materials", "Alpha"));
         schematic.put("WEOriginX", new IntTag("WEOriginX", clipboard.getOrigin().getBlockX()));
         schematic.put("WEOriginY", new IntTag("WEOriginY", clipboard.getOrigin().getBlockY()));
         schematic.put("WEOriginZ", new IntTag("WEOriginZ", clipboard.getOrigin().getBlockZ()));
         schematic.put("WEOffsetX", new IntTag("WEOffsetX", clipboard.getOffset().getBlockX()));
         schematic.put("WEOffsetY", new IntTag("WEOffsetY", clipboard.getOffset().getBlockY()));
         schematic.put("WEOffsetZ", new IntTag("WEOffsetZ", clipboard.getOffset().getBlockZ()));
         byte[] blocks = new byte[width * height * length];
         byte[] addBlocks = null;
         byte[] blockData = new byte[width * height * length];
         ArrayList<Tag> tileEntities = new ArrayList();

         for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
               for(int z = 0; z < length; ++z) {
                  int index = y * width * length + z * width + x;
                  BaseBlock block = clipboard.getPoint(new BlockVector(x, y, z));
                  if (block.getType() > 255) {
                     if (addBlocks == null) {
                        addBlocks = new byte[(blocks.length >> 1) + 1];
                     }

                     addBlocks[index >> 1] = (byte)((index & 1) == 0 ? addBlocks[index >> 1] & 240 | block.getType() >> 8 & 15 : addBlocks[index >> 1] & 15 | (block.getType() >> 8 & 15) << 4);
                  }

                  blocks[index] = (byte)block.getType();
                  blockData[index] = (byte)block.getData();
                  if (block instanceof TileEntityBlock) {
                     CompoundTag rawTag = block.getNbtData();
                     if (rawTag != null) {
                        Map<String, Tag> values = new HashMap();

                        for(Map.Entry entry : rawTag.getValue().entrySet()) {
                           values.put(entry.getKey(), entry.getValue());
                        }

                        values.put("id", new StringTag("id", block.getNbtId()));
                        values.put("x", new IntTag("x", x));
                        values.put("y", new IntTag("y", y));
                        values.put("z", new IntTag("z", z));
                        CompoundTag tileEntityTag = new CompoundTag("TileEntity", values);
                        tileEntities.add(tileEntityTag);
                     }
                  }
               }
            }
         }

         schematic.put("Blocks", new ByteArrayTag("Blocks", blocks));
         schematic.put("Data", new ByteArrayTag("Data", blockData));
         schematic.put("Entities", new ListTag("Entities", CompoundTag.class, new ArrayList()));
         schematic.put("TileEntities", new ListTag("TileEntities", CompoundTag.class, tileEntities));
         if (addBlocks != null) {
            schematic.put("AddBlocks", new ByteArrayTag("AddBlocks", addBlocks));
         }

         CompoundTag schematicTag = new CompoundTag("Schematic", schematic);
         NBTOutputStream stream = new NBTOutputStream(new FileOutputStream(file));
         stream.writeTag(schematicTag);
         stream.close();
      }
   }

   public boolean isOfFormat(File file) {
      DataInputStream str = null;

      boolean var3;
      try {
         str = new DataInputStream(new GZIPInputStream(new FileInputStream(file)));
         if ((str.readByte() & 255) == 10) {
            byte[] nameBytes = new byte[str.readShort() & '\uffff'];
            str.readFully(nameBytes);
            String name = new String(nameBytes, NBTConstants.CHARSET);
            boolean var5 = name.equals("Schematic");
            return var5;
         }

         var3 = false;
      } catch (IOException var16) {
         boolean ignore = false;
         return ignore;
      } finally {
         if (str != null) {
            try {
               str.close();
            } catch (IOException var15) {
            }
         }

      }

      return var3;
   }

   private static Tag getChildTag(Map items, String key, Class expected) throws DataException {
      if (!items.containsKey(key)) {
         throw new DataException("Schematic file is missing a \"" + key + "\" tag");
      } else {
         Tag tag = (Tag)items.get(key);
         if (!expected.isInstance(tag)) {
            throw new DataException(key + " tag is not of tag type " + expected.getName());
         } else {
            return (Tag)expected.cast(tag);
         }
      }
   }
}
