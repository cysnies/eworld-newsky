package com.sk89q.worldedit.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class FileLegacyChunkStore extends LegacyChunkStore {
   private File path;

   public FileLegacyChunkStore(File path) {
      this.path = path;
   }

   protected InputStream getInputStream(String f1, String f2, String name) throws DataException, IOException {
      String file = f1 + File.separator + f2 + File.separator + name;

      try {
         return new FileInputStream(new File(this.path, file));
      } catch (FileNotFoundException var6) {
         throw new MissingChunkException();
      }
   }

   public boolean isValid() {
      return true;
   }
}
