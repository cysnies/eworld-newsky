package com.comphenix.protocol.injector.player;

import com.google.common.base.Joiner;
import java.util.Arrays;

class UnsupportedListener {
   private String message;
   private int[] packets;

   public UnsupportedListener(String message, int[] packets) {
      this.message = message;
      this.packets = packets;
   }

   public String getMessage() {
      return this.message;
   }

   public int[] getPackets() {
      return this.packets;
   }

   public String toString() {
      return String.format("%s (%s)", this.message, Joiner.on(", ").join(Arrays.asList(this.packets)));
   }
}
