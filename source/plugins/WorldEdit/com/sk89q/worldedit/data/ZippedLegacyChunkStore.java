package com.sk89q.worldedit.data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class ZippedLegacyChunkStore extends LegacyChunkStore {
   private File zipFile;
   private ZipFile zip;
   private String folder;

   public ZippedLegacyChunkStore(File zipFile, String folder) throws IOException, ZipException {
      this.zipFile = zipFile;
      this.folder = folder;
      this.zip = new ZipFile(zipFile);
   }

   public ZippedLegacyChunkStore(File zipFile) throws IOException, ZipException {
      this.zipFile = zipFile;
      this.zip = new ZipFile(zipFile);
   }

   protected InputStream getInputStream(String f1, String f2, String name) throws IOException, DataException {
      String file = f1 + "/" + f2 + "/" + name;
      if (this.folder != null) {
         if (!this.folder.equals("")) {
            file = this.folder + "/" + file;
         }
      } else {
         ZipEntry testEntry = this.zip.getEntry("level.dat");
         if (testEntry == null) {
            testEntry = this.getEntry("world/level.dat");
            Pattern pattern = Pattern.compile(".*[\\\\/]level\\.dat$");
            if (testEntry == null) {
               Enumeration<? extends ZipEntry> e = this.zip.entries();

               while(e.hasMoreElements()) {
                  testEntry = (ZipEntry)e.nextElement();
                  if (pattern.matcher(testEntry.getName()).matches()) {
                     this.folder = testEntry.getName().replaceAll("level\\.dat$", "");
                     this.folder = this.folder.substring(0, this.folder.length() - 1);
                     file = this.folder + file;
                     break;
                  }
               }
            } else {
               file = "world/" + file;
            }
         }
      }

      ZipEntry entry = this.getEntry(file);
      if (entry == null) {
         throw new MissingChunkException();
      } else {
         try {
            return this.zip.getInputStream(entry);
         } catch (ZipException var8) {
            throw new IOException("Failed to read " + file + " in ZIP");
         }
      }
   }

   private ZipEntry getEntry(String file) {
      ZipEntry entry = this.zip.getEntry(file);
      return entry != null ? entry : this.zip.getEntry(file.replace("/", "\\"));
   }

   public void close() throws IOException {
      this.zip.close();
   }

   public boolean isValid() {
      return true;
   }
}
