package com.comphenix.protocol.events;

import com.comphenix.protocol.utility.StreamSerializer;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;
import javax.annotation.Nonnull;

public class NetworkMarker {
   private PriorityQueue outputHandlers;
   private ByteBuffer inputBuffer;
   private final ConnectionSide side;
   private StreamSerializer serializer;

   public NetworkMarker(@Nonnull ConnectionSide side, byte[] inputBuffer) {
      this.side = (ConnectionSide)Preconditions.checkNotNull(side, "side cannot be NULL.");
      if (inputBuffer != null) {
         this.inputBuffer = ByteBuffer.wrap(inputBuffer);
      }

   }

   public ConnectionSide getSide() {
      return this.side;
   }

   public StreamSerializer getSerializer() {
      if (this.serializer == null) {
         this.serializer = new StreamSerializer();
      }

      return this.serializer;
   }

   public ByteBuffer getInputBuffer() {
      if (this.side.isForServer()) {
         throw new IllegalStateException("Server-side packets have no input buffer.");
      } else {
         return this.inputBuffer != null ? this.inputBuffer.asReadOnlyBuffer() : null;
      }
   }

   public DataInputStream getInputStream() {
      if (this.side.isForServer()) {
         throw new IllegalStateException("Server-side packets have no input buffer.");
      } else {
         return this.inputBuffer == null ? null : new DataInputStream(new ByteArrayInputStream(this.inputBuffer.array()));
      }
   }

   public boolean addOutputHandler(@Nonnull PacketOutputHandler handler) {
      this.checkServerSide();
      Preconditions.checkNotNull(handler, "handler cannot be NULL.");
      if (this.outputHandlers == null) {
         this.outputHandlers = new PriorityQueue(10, new Comparator() {
            public int compare(PacketOutputHandler o1, PacketOutputHandler o2) {
               return Ints.compare(o1.getPriority().getSlot(), o2.getPriority().getSlot());
            }
         });
      }

      return this.outputHandlers.add(handler);
   }

   public boolean removeOutputHandler(@Nonnull PacketOutputHandler handler) {
      this.checkServerSide();
      Preconditions.checkNotNull(handler, "handler cannot be NULL.");
      return this.outputHandlers != null ? this.outputHandlers.remove(handler) : false;
   }

   @Nonnull
   public Collection getOutputHandlers() {
      return (Collection)(this.outputHandlers != null ? this.outputHandlers : Collections.emptyList());
   }

   private void checkServerSide() {
      if (this.side.isForClient()) {
         throw new IllegalStateException("Must be a server side packet.");
      }
   }

   public static boolean hasOutputHandlers(NetworkMarker marker) {
      return marker != null && !marker.getOutputHandlers().isEmpty();
   }

   public static byte[] getByteBuffer(NetworkMarker marker) {
      if (marker != null) {
         ByteBuffer buffer = marker.getInputBuffer();
         if (buffer != null) {
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data, 0, data.length);
            return data;
         }
      }

      return null;
   }

   public static NetworkMarker getNetworkMarker(PacketEvent event) {
      return event.networkMarker;
   }
}
