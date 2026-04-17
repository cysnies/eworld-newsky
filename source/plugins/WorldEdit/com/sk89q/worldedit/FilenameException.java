package com.sk89q.worldedit;

public class FilenameException extends WorldEditException {
   private static final long serialVersionUID = 6072601657326106265L;
   private String filename;

   public FilenameException(String filename) {
      this.filename = filename;
   }

   public FilenameException(String filename, String msg) {
      super(msg);
      this.filename = filename;
   }

   public String getFilename() {
      return this.filename;
   }
}
