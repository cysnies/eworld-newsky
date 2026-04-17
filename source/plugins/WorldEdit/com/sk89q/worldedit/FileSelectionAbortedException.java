package com.sk89q.worldedit;

public class FileSelectionAbortedException extends FilenameException {
   private static final long serialVersionUID = 7377072269988014886L;

   public FileSelectionAbortedException() {
      super("");
   }

   public FileSelectionAbortedException(String msg) {
      super("", msg);
   }
}
