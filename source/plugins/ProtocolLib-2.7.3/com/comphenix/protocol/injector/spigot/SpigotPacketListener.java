package com.comphenix.protocol.injector.spigot;

interface SpigotPacketListener {
   Object packetReceived(Object var1, Object var2, Object var3);

   Object packetQueued(Object var1, Object var2, Object var3);
}
