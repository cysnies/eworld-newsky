package com.comphenix.protocol.injector;

public enum GamePhase {
   LOGIN,
   PLAYING,
   BOTH;

   public boolean hasLogin() {
      return this == LOGIN || this == BOTH;
   }

   public boolean hasPlaying() {
      return this == PLAYING || this == BOTH;
   }
}
