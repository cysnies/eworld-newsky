package com.sk89q.worldedit;

public class InvalidFilenameException extends FilenameException {
   private static final long serialVersionUID = 7377072269988014886L;

   public InvalidFilenameException(String filename) {
      super(filename);
   }

   public InvalidFilenameException(String filename, String msg) {
      super(filename, msg);
   }
}
