package com.sk89q.worldedit.data;

import com.sk89q.worldedit.Vector2D;

public class MissingChunkException extends ChunkStoreException {
   private static final long serialVersionUID = 8013715483709973489L;
   private Vector2D pos;

   public MissingChunkException() {
   }

   public MissingChunkException(Vector2D pos) {
      this.pos = pos;
   }

   public Vector2D getChunkPosition() {
      return this.pos;
   }
}
