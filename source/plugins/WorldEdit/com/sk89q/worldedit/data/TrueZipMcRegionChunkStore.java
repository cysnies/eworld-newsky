package com.sk89q.worldedit.data;

import de.schlichtherle.util.zip.ZipEntry;
import de.schlichtherle.util.zip.ZipFile;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.zip.ZipException;

public class TrueZipMcRegionChunkStore extends McRegionChunkStore {
   protected File zipFile;
   protected ZipFile zip;
   protected String folder;

   public TrueZipMcRegionChunkStore(File zipFile, String folder) throws IOException, ZipException {
      this.zipFile = zipFile;
      this.folder = folder;
      this.zip = new ZipFile(zipFile);
   }

   public TrueZipMcRegionChunkStore(File zipFile) throws IOException, ZipException {
      this.zipFile = zipFile;
      this.zip = new ZipFile(zipFile);
   }

   protected InputStream getInputStream(String name, String worldname) throws IOException, DataException {
      if (this.folder != null) {
         if (!this.folder.equals("")) {
            name = this.folder + "/" + name;
         }
      } else {
         Pattern pattern = Pattern.compile(".*\\.mc[ra]$");
         Pattern worldPattern = Pattern.compile(worldname + "\\$");
         Enumeration<? extends ZipEntry> e = this.zip.entries();

         while(e.hasMoreElements()) {
            ZipEntry testEntry = (ZipEntry)e.nextElement();
            if (worldPattern.matcher(worldname).matches() && pattern.matcher(testEntry.getName()).matches()) {
               this.folder = testEntry.getName().substring(0, testEntry.getName().lastIndexOf("/"));
               name = this.folder + "/" + name;
               break;
            }
         }

         if (this.folder == null) {
            throw new MissingWorldException("Target world is not present in ZIP.", worldname);
         }
      }

      ZipEntry entry = this.getEntry(name);
      if (entry == null) {
         throw new MissingChunkException();
      } else {
         try {
            return this.zip.getInputStream(entry);
         } catch (ZipException var7) {
            throw new IOException("Failed to read " + name + " in ZIP");
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
      Enumeration<? extends ZipEntry> e = this.zip.entries();

      while(e.hasMoreElements()) {
         ZipEntry testEntry = (ZipEntry)e.nextElement();
         if (testEntry.getName().matches(".*\\.mcr$") || testEntry.getName().matches(".*\\.mca$")) {
            return true;
         }
      }

      return false;
   }
}
