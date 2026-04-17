package com.comphenix.protocol.async;

import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import org.bukkit.plugin.Plugin;

class NullPacketListener implements PacketListener {
   private ListeningWhitelist sendingWhitelist;
   private ListeningWhitelist receivingWhitelist;
   private Plugin plugin;

   public NullPacketListener(PacketListener original) {
      this.sendingWhitelist = this.cloneWhitelist(ListenerPriority.LOW, original.getSendingWhitelist());
      this.receivingWhitelist = this.cloneWhitelist(ListenerPriority.LOW, original.getReceivingWhitelist());
      this.plugin = original.getPlugin();
   }

   public void onPacketSending(PacketEvent event) {
   }

   public void onPacketReceiving(PacketEvent event) {
   }

   public ListeningWhitelist getSendingWhitelist() {
      return this.sendingWhitelist;
   }

   public ListeningWhitelist getReceivingWhitelist() {
      return this.receivingWhitelist;
   }

   private ListeningWhitelist cloneWhitelist(ListenerPriority priority, ListeningWhitelist whitelist) {
      return whitelist != null ? ListeningWhitelist.newBuilder(whitelist).priority(priority).build() : null;
   }

   public Plugin getPlugin() {
      return this.plugin;
   }
}
