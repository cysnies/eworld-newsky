package com.comphenix.protocol.events;

import com.comphenix.protocol.async.AsyncMarker;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.util.EventObject;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class PacketEvent extends EventObject implements Cancellable {
   private static final long serialVersionUID = -5360289379097430620L;
   private transient WeakReference playerReference;
   private transient Player offlinePlayer;
   private PacketContainer packet;
   private boolean serverPacket;
   private boolean cancel;
   private AsyncMarker asyncMarker;
   private boolean asynchronous;
   NetworkMarker networkMarker;
   private boolean readOnly;

   public PacketEvent(Object source) {
      super(source);
   }

   private PacketEvent(Object source, PacketContainer packet, Player player, boolean serverPacket) {
      this(source, packet, (NetworkMarker)null, player, serverPacket);
   }

   private PacketEvent(Object source, PacketContainer packet, NetworkMarker marker, Player player, boolean serverPacket) {
      super(source);
      this.packet = packet;
      this.playerReference = new WeakReference(player);
      this.networkMarker = marker;
      this.serverPacket = serverPacket;
   }

   private PacketEvent(PacketEvent origial, AsyncMarker asyncMarker) {
      super(origial.source);
      this.packet = origial.packet;
      this.playerReference = origial.playerReference;
      this.cancel = origial.cancel;
      this.serverPacket = origial.serverPacket;
      this.asyncMarker = asyncMarker;
      this.asynchronous = true;
   }

   public static PacketEvent fromClient(Object source, PacketContainer packet, Player client) {
      return new PacketEvent(source, packet, client, false);
   }

   public static PacketEvent fromClient(Object source, PacketContainer packet, NetworkMarker marker, Player client) {
      return new PacketEvent(source, packet, marker, client, false);
   }

   public static PacketEvent fromServer(Object source, PacketContainer packet, Player recipient) {
      return new PacketEvent(source, packet, recipient, true);
   }

   public static PacketEvent fromServer(Object source, PacketContainer packet, NetworkMarker marker, Player recipient) {
      return new PacketEvent(source, packet, marker, recipient, true);
   }

   public static PacketEvent fromSynchronous(PacketEvent event, AsyncMarker marker) {
      return new PacketEvent(event, marker);
   }

   public PacketContainer getPacket() {
      return this.packet;
   }

   public void setPacket(PacketContainer packet) {
      if (this.readOnly) {
         throw new IllegalStateException("The packet event is read-only.");
      } else {
         this.packet = packet;
      }
   }

   public int getPacketID() {
      return this.packet.getID();
   }

   public boolean isCancelled() {
      return this.cancel;
   }

   public NetworkMarker getNetworkMarker() {
      if (this.networkMarker == null) {
         if (!this.isServerPacket()) {
            throw new IllegalStateException("Add the option ListenerOptions.INTERCEPT_INPUT_BUFFER to your listener.");
         }

         this.networkMarker = new NetworkMarker(this.serverPacket ? ConnectionSide.SERVER_SIDE : ConnectionSide.CLIENT_SIDE, (byte[])null);
      }

      return this.networkMarker;
   }

   public void setNetworkMarker(NetworkMarker networkMarker) {
      this.networkMarker = (NetworkMarker)Preconditions.checkNotNull(networkMarker, "marker cannot be NULL");
   }

   public void setCancelled(boolean cancel) {
      if (this.readOnly) {
         throw new IllegalStateException("The packet event is read-only.");
      } else {
         this.cancel = cancel;
      }
   }

   public Player getPlayer() {
      return (Player)this.playerReference.get();
   }

   public boolean isServerPacket() {
      return this.serverPacket;
   }

   public AsyncMarker getAsyncMarker() {
      return this.asyncMarker;
   }

   public void setAsyncMarker(AsyncMarker asyncMarker) {
      if (this.isAsynchronous()) {
         throw new IllegalStateException("The marker is immutable for asynchronous events");
      } else if (this.readOnly) {
         throw new IllegalStateException("The packet event is read-only.");
      } else {
         this.asyncMarker = asyncMarker;
      }
   }

   public boolean isReadOnly() {
      return this.readOnly;
   }

   public void setReadOnly(boolean readOnly) {
      this.readOnly = readOnly;
   }

   public boolean isAsynchronous() {
      return this.asynchronous;
   }

   private void writeObject(ObjectOutputStream output) throws IOException {
      output.defaultWriteObject();
      output.writeObject(this.playerReference.get() != null ? new SerializedOfflinePlayer((OfflinePlayer)this.playerReference.get()) : null);
   }

   private void readObject(ObjectInputStream input) throws ClassNotFoundException, IOException {
      input.defaultReadObject();
      SerializedOfflinePlayer serialized = (SerializedOfflinePlayer)input.readObject();
      if (serialized != null) {
         this.offlinePlayer = serialized.getPlayer();
         this.playerReference = new WeakReference(this.offlinePlayer);
      }

   }
}
