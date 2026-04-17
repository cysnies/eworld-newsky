package com.sk89q.worldedit;

public class FilenameResolutionException extends FilenameException {
   private static final long serialVersionUID = 4673670296313383121L;

   public FilenameResolutionException(String filename) {
      super(filename);
   }

   public FilenameResolutionException(String filename, String msg) {
      super(filename, msg);
   }
}
