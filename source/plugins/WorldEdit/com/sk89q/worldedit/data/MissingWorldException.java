package com.sk89q.worldedit.data;

public class MissingWorldException extends ChunkStoreException {
   private static final long serialVersionUID = 6487395784195658467L;
   private String worldname;

   public MissingWorldException() {
   }

   public MissingWorldException(String worldname) {
      this.worldname = worldname;
   }

   public MissingWorldException(String msg, String worldname) {
      super(msg);
      this.worldname = worldname;
   }

   public String getWorldname() {
      return this.worldname;
   }
}
