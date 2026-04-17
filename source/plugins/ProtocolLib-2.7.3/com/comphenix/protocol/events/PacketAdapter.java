package com.comphenix.protocol.events;

import com.comphenix.protocol.injector.GamePhase;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import org.bukkit.plugin.Plugin;

public abstract class PacketAdapter implements PacketListener {
   protected Plugin plugin;
   protected ConnectionSide connectionSide;
   protected ListeningWhitelist receivingWhitelist;
   protected ListeningWhitelist sendingWhitelist;

   public PacketAdapter(@Nonnull AdapterParameteters params) {
      this(checkValidity(params).plugin, params.connectionSide, params.listenerPriority, params.gamePhase, params.options, params.packets);
   }

   public PacketAdapter(Plugin plugin, ConnectionSide connectionSide, Integer... packets) {
      this(plugin, connectionSide, ListenerPriority.NORMAL, packets);
   }

   public PacketAdapter(Plugin plugin, ConnectionSide connectionSide, ListenerPriority listenerPriority, Set packets) {
      this(plugin, connectionSide, listenerPriority, GamePhase.PLAYING, (Integer[])packets.toArray(new Integer[0]));
   }

   public PacketAdapter(Plugin plugin, ConnectionSide connectionSide, GamePhase gamePhase, Set packets) {
      this(plugin, connectionSide, ListenerPriority.NORMAL, gamePhase, (Integer[])packets.toArray(new Integer[0]));
   }

   public PacketAdapter(Plugin plugin, ConnectionSide connectionSide, ListenerPriority listenerPriority, GamePhase gamePhase, Set packets) {
      this(plugin, connectionSide, listenerPriority, gamePhase, (Integer[])packets.toArray(new Integer[0]));
   }

   public PacketAdapter(Plugin plugin, ConnectionSide connectionSide, ListenerPriority listenerPriority, Integer... packets) {
      this(plugin, connectionSide, listenerPriority, GamePhase.PLAYING, packets);
   }

   public PacketAdapter(Plugin plugin, ConnectionSide connectionSide, ListenerOptions[] options, Integer... packets) {
      this(plugin, connectionSide, ListenerPriority.NORMAL, GamePhase.PLAYING, options, packets);
   }

   public PacketAdapter(Plugin plugin, ConnectionSide connectionSide, GamePhase gamePhase, Integer... packets) {
      this(plugin, connectionSide, ListenerPriority.NORMAL, gamePhase, packets);
   }

   public PacketAdapter(Plugin plugin, ConnectionSide connectionSide, ListenerPriority listenerPriority, GamePhase gamePhase, Integer... packets) {
      this(plugin, connectionSide, listenerPriority, gamePhase, new ListenerOptions[0], packets);
   }

   public PacketAdapter(Plugin plugin, ConnectionSide connectionSide, ListenerPriority listenerPriority, GamePhase gamePhase, ListenerOptions[] options, Integer... packets) {
      this.receivingWhitelist = ListeningWhitelist.EMPTY_WHITELIST;
      this.sendingWhitelist = ListeningWhitelist.EMPTY_WHITELIST;
      if (plugin == null) {
         throw new IllegalArgumentException("plugin cannot be null");
      } else if (connectionSide == null) {
         throw new IllegalArgumentException("connectionSide cannot be null");
      } else if (listenerPriority == null) {
         throw new IllegalArgumentException("listenerPriority cannot be null");
      } else if (gamePhase == null) {
         throw new IllegalArgumentException("gamePhase cannot be NULL");
      } else if (packets == null) {
         throw new IllegalArgumentException("packets cannot be null");
      } else if (options == null) {
         throw new IllegalArgumentException("options cannot be null");
      } else {
         ListenerOptions[] serverOptions = options;
         if (connectionSide == ConnectionSide.BOTH) {
            serverOptions = (ListenerOptions[])except(options, new ListenerOptions[0], ListenerOptions.INTERCEPT_INPUT_BUFFER);
         }

         if (connectionSide.isForServer()) {
            this.sendingWhitelist = new ListeningWhitelist(listenerPriority, packets, gamePhase, serverOptions);
         }

         if (connectionSide.isForClient()) {
            this.receivingWhitelist = new ListeningWhitelist(listenerPriority, packets, gamePhase, options);
         }

         this.plugin = plugin;
         this.connectionSide = connectionSide;
      }
   }

   private static Object[] except(Object[] values, Object[] buffer, Object except) {
      List<T> result = Lists.newArrayList(values);
      result.remove(except);
      return result.toArray(buffer);
   }

   public void onPacketReceiving(PacketEvent event) {
      throw new IllegalStateException("Override onPacketReceiving to get notifcations of received packets!");
   }

   public void onPacketSending(PacketEvent event) {
      throw new IllegalStateException("Override onPacketSending to get notifcations of sent packets!");
   }

   public ListeningWhitelist getReceivingWhitelist() {
      return this.receivingWhitelist;
   }

   public ListeningWhitelist getSendingWhitelist() {
      return this.sendingWhitelist;
   }

   public Plugin getPlugin() {
      return this.plugin;
   }

   public static String getPluginName(PacketListener listener) {
      return getPluginName(listener.getPlugin());
   }

   public static String getPluginName(Plugin plugin) {
      try {
         return plugin == null ? "UNKNOWN" : plugin.getName();
      } catch (NoSuchMethodError var2) {
         return plugin.toString();
      }
   }

   public String toString() {
      return String.format("PacketAdapter[plugin=%s, sending=%s, receiving=%s]", getPluginName((PacketListener)this), this.sendingWhitelist, this.receivingWhitelist);
   }

   public static AdapterParameteters params() {
      return new AdapterParameteters();
   }

   public static AdapterParameteters params(Plugin plugin, Integer... packets) {
      return (new AdapterParameteters()).plugin(plugin).packets(packets);
   }

   private static AdapterParameteters checkValidity(AdapterParameteters params) {
      if (params == null) {
         throw new IllegalArgumentException("params cannot be NULL.");
      } else if (params.plugin == null) {
         throw new IllegalStateException("Plugin was never set in the parameters.");
      } else if (params.connectionSide == null) {
         throw new IllegalStateException("Connection side was never set in the parameters.");
      } else if (params.packets == null) {
         throw new IllegalStateException("Packet IDs was never set in the parameters.");
      } else {
         return params;
      }
   }

   public static class AdapterParameteters {
      private Plugin plugin;
      private ConnectionSide connectionSide;
      private Integer[] packets;
      private GamePhase gamePhase;
      private ListenerOptions[] options;
      private ListenerPriority listenerPriority;

      public AdapterParameteters() {
         this.gamePhase = GamePhase.PLAYING;
         this.options = new ListenerOptions[0];
         this.listenerPriority = ListenerPriority.NORMAL;
      }

      public AdapterParameteters plugin(@Nonnull Plugin plugin) {
         this.plugin = (Plugin)Preconditions.checkNotNull(plugin, "plugin cannot be NULL.");
         return this;
      }

      public AdapterParameteters connectionSide(@Nonnull ConnectionSide connectionSide) {
         this.connectionSide = (ConnectionSide)Preconditions.checkNotNull(connectionSide, "connectionside cannot be NULL.");
         return this;
      }

      public AdapterParameteters clientSide() {
         return this.connectionSide(ConnectionSide.add(this.connectionSide, ConnectionSide.CLIENT_SIDE));
      }

      public AdapterParameteters serverSide() {
         return this.connectionSide(ConnectionSide.add(this.connectionSide, ConnectionSide.SERVER_SIDE));
      }

      public AdapterParameteters listenerPriority(@Nonnull ListenerPriority listenerPriority) {
         this.listenerPriority = (ListenerPriority)Preconditions.checkNotNull(listenerPriority, "listener priority cannot be NULL.");
         return this;
      }

      public AdapterParameteters gamePhase(@Nonnull GamePhase gamePhase) {
         this.gamePhase = (GamePhase)Preconditions.checkNotNull(gamePhase, "gamePhase cannot be NULL.");
         return this;
      }

      public AdapterParameteters loginPhase() {
         return this.gamePhase(GamePhase.LOGIN);
      }

      public AdapterParameteters options(@Nonnull ListenerOptions... options) {
         this.options = (ListenerOptions[])Preconditions.checkNotNull(options, "options cannot be NULL.");
         return this;
      }

      public AdapterParameteters optionIntercept() {
         return this.options(ListenerOptions.INTERCEPT_INPUT_BUFFER);
      }

      public AdapterParameteters packets(@Nonnull Integer... packets) {
         this.packets = (Integer[])Preconditions.checkNotNull(packets, "packets cannot be NULL");
         return this;
      }

      public AdapterParameteters packets(@Nonnull Set packets) {
         return this.packets((Integer[])packets.toArray(new Integer[0]));
      }
   }
}
