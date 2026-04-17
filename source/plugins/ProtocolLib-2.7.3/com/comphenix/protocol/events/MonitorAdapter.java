package com.comphenix.protocol.events;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.reflect.FieldAccessException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.plugin.Plugin;

public abstract class MonitorAdapter implements PacketListener {
   private Plugin plugin;
   private ListeningWhitelist sending;
   private ListeningWhitelist receiving;

   public MonitorAdapter(Plugin plugin, ConnectionSide side) {
      this.sending = ListeningWhitelist.EMPTY_WHITELIST;
      this.receiving = ListeningWhitelist.EMPTY_WHITELIST;
      this.initialize(plugin, side, this.getLogger(plugin));
   }

   public MonitorAdapter(Plugin plugin, ConnectionSide side, Logger logger) {
      this.sending = ListeningWhitelist.EMPTY_WHITELIST;
      this.receiving = ListeningWhitelist.EMPTY_WHITELIST;
      this.initialize(plugin, side, logger);
   }

   private void initialize(Plugin plugin, ConnectionSide side, Logger logger) {
      this.plugin = plugin;

      try {
         if (side.isForServer()) {
            this.sending = new ListeningWhitelist(ListenerPriority.MONITOR, Packets.Server.getSupported(), GamePhase.BOTH);
         }

         if (side.isForClient()) {
            this.receiving = new ListeningWhitelist(ListenerPriority.MONITOR, Packets.Client.getSupported(), GamePhase.BOTH);
         }
      } catch (FieldAccessException e) {
         if (side.isForServer()) {
            this.sending = new ListeningWhitelist(ListenerPriority.MONITOR, Packets.Server.getRegistry().values(), GamePhase.BOTH);
         }

         if (side.isForClient()) {
            this.receiving = new ListeningWhitelist(ListenerPriority.MONITOR, Packets.Client.getRegistry().values(), GamePhase.BOTH);
         }

         logger.log(Level.WARNING, "Defaulting to 1.3 packets.", e);
      }

   }

   private Logger getLogger(Plugin plugin) {
      try {
         return plugin.getLogger();
      } catch (NoSuchMethodError var3) {
         return Logger.getLogger("Minecraft");
      }
   }

   public ListeningWhitelist getSendingWhitelist() {
      return this.sending;
   }

   public ListeningWhitelist getReceivingWhitelist() {
      return this.receiving;
   }

   public Plugin getPlugin() {
      return this.plugin;
   }
}
