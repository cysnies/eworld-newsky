package com.comphenix.protocol.async;

import com.comphenix.protocol.events.PacketEvent;
import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;
import com.google.common.primitives.Longs;

class PacketEventHolder implements Comparable {
   private PacketEvent event;
   private long sendingIndex = 0L;

   public PacketEventHolder(PacketEvent event) {
      this.event = (PacketEvent)Preconditions.checkNotNull(event, "Event must be non-null");
      if (event.getAsyncMarker() != null) {
         this.sendingIndex = event.getAsyncMarker().getNewSendingIndex();
      }

   }

   public PacketEvent getEvent() {
      return this.event;
   }

   public int compareTo(PacketEventHolder other) {
      return ComparisonChain.start().compare(this.sendingIndex, other.sendingIndex).result();
   }

   public boolean equals(Object other) {
      if (other == this) {
         return true;
      } else if (other instanceof PacketEventHolder) {
         return this.sendingIndex == ((PacketEventHolder)other).sendingIndex;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Longs.hashCode(this.sendingIndex);
   }
}
