package com.sk89q.worldedit.data;

import com.sk89q.worldedit.Vector2D;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public class McRegionReader {
   protected static final int VERSION_GZIP = 1;
   protected static final int VERSION_DEFLATE = 2;
   protected static final int SECTOR_BYTES = 4096;
   protected static final int SECTOR_INTS = 1024;
   public static final int CHUNK_HEADER_SIZE = 5;
   protected ForwardSeekableInputStream stream;
   protected DataInputStream dataStream;
   protected int[] offsets;

   public McRegionReader(InputStream stream) throws DataException, IOException {
      this.stream = new ForwardSeekableInputStream(stream);
      this.dataStream = new DataInputStream(this.stream);
      this.readHeader();
   }

   private void readHeader() throws DataException, IOException {
      this.offsets = new int[1024];

      for(int i = 0; i < 1024; ++i) {
         int offset = this.dataStream.readInt();
         this.offsets[i] = offset;
      }

   }

   public synchronized InputStream getChunkInputStream(Vector2D pos) throws IOException, DataException {
      int x = pos.getBlockX() & 31;
      int z = pos.getBlockZ() & 31;
      if (x >= 0 && x < 32 && z >= 0 && z < 32) {
         int offset = this.getOffset(x, z);
         if (offset == 0) {
            throw new DataException("The chunk at " + x + "," + z + " is not generated");
         } else {
            int sectorNumber = offset >> 8;
            int numSectors = offset & 255;
            this.stream.seek((long)(sectorNumber * 4096));
            int length = this.dataStream.readInt();
            if (length > 4096 * numSectors) {
               throw new DataException("MCRegion chunk at " + x + "," + z + " has an invalid length of " + length);
            } else {
               byte version = this.dataStream.readByte();
               if (version == 1) {
                  byte[] data = new byte[length - 1];
                  if (this.dataStream.read(data) < length - 1) {
                     throw new DataException("MCRegion file does not contain " + x + "," + z + " in full");
                  } else {
                     return new GZIPInputStream(new ByteArrayInputStream(data));
                  }
               } else if (version == 2) {
                  byte[] data = new byte[length - 1];
                  if (this.dataStream.read(data) < length - 1) {
                     throw new DataException("MCRegion file does not contain " + x + "," + z + " in full");
                  } else {
                     return new InflaterInputStream(new ByteArrayInputStream(data));
                  }
               } else {
                  throw new DataException("MCRegion chunk at " + x + "," + z + " has an unsupported version of " + version);
               }
            }
         }
      } else {
         throw new DataException("MCRegion file does not contain " + x + "," + z);
      }
   }

   private int getOffset(int x, int z) {
      return this.offsets[x + z * 32];
   }

   public boolean hasChunk(int x, int z) {
      return this.getOffset(x, z) != 0;
   }

   public void close() throws IOException {
      this.stream.close();
   }
}
