package com.sk89q.worldedit.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

public class FileMcRegionChunkStore extends McRegionChunkStore {
   private File path;

   public FileMcRegionChunkStore(File path) {
      this.path = path;
   }

   protected InputStream getInputStream(String name, String world) throws IOException, DataException {
      Pattern ext = Pattern.compile(".*\\.mc[ra]$");
      File file = null;

      for(File f : (new File(this.path, "region" + File.separator)).listFiles()) {
         String tempName = f.getName().replaceFirst("mcr$", "mca");
         if (ext.matcher(f.getName()).matches() && name.equalsIgnoreCase(tempName)) {
            file = new File(this.path + File.separator + "region" + File.separator + f.getName());
            break;
         }
      }

      try {
         if (file == null) {
            throw new FileNotFoundException();
         } else {
            return new FileInputStream(file);
         }
      } catch (FileNotFoundException var10) {
         throw new MissingChunkException();
      }
   }

   public boolean isValid() {
      return (new File(this.path, "region")).isDirectory() || (new File(this.path, "DIM-1" + File.separator + "region")).isDirectory();
   }
}
