package com.comphenix.protocol.timing;

import com.comphenix.protocol.events.PacketListener;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.plugin.Plugin;

public class TimedListenerManager {
   private static final TimedListenerManager INSTANCE = new TimedListenerManager();
   private static final AtomicBoolean timing = new AtomicBoolean();
   private volatile Date started;
   private volatile Date stopped;
   private ConcurrentMap map = Maps.newConcurrentMap();

   public static TimedListenerManager getInstance() {
      return INSTANCE;
   }

   public boolean startTiming() {
      if (this.setTiming(true)) {
         this.started = Calendar.getInstance().getTime();
         return true;
      } else {
         return false;
      }
   }

   public boolean stopTiming() {
      if (this.setTiming(false)) {
         this.stopped = Calendar.getInstance().getTime();
         return true;
      } else {
         return false;
      }
   }

   public Date getStarted() {
      return this.started;
   }

   public Date getStopped() {
      return this.stopped;
   }

   private boolean setTiming(boolean value) {
      return timing.compareAndSet(!value, value);
   }

   public boolean isTiming() {
      return timing.get();
   }

   public void clear() {
      this.map.clear();
   }

   public Set getTrackedPlugins() {
      return this.map.keySet();
   }

   public TimedTracker getTracker(Plugin plugin, ListenerType type) {
      return this.getTracker(plugin.getName(), type);
   }

   public TimedTracker getTracker(PacketListener listener, ListenerType type) {
      return this.getTracker(listener.getPlugin().getName(), type);
   }

   public TimedTracker getTracker(String pluginName, ListenerType type) {
      return (TimedTracker)this.getTrackers(pluginName).get(type);
   }

   private ImmutableMap getTrackers(String pluginName) {
      ImmutableMap<ListenerType, TimedTracker> trackers = (ImmutableMap)this.map.get(pluginName);
      if (trackers == null) {
         ImmutableMap<ListenerType, TimedTracker> created = this.newTrackerMap();
         trackers = (ImmutableMap)this.map.putIfAbsent(pluginName, created);
         if (trackers == null) {
            trackers = created;
         }
      }

      return trackers;
   }

   private ImmutableMap newTrackerMap() {
      ImmutableMap.Builder<ListenerType, TimedTracker> builder = ImmutableMap.builder();

      for(ListenerType type : TimedListenerManager.ListenerType.values()) {
         builder.put(type, new TimedTracker());
      }

      return builder.build();
   }

   public static enum ListenerType {
      ASYNC_SERVER_SIDE,
      ASYNC_CLIENT_SIDE,
      SYNC_SERVER_SIDE,
      SYNC_CLIENT_SIDE;
   }
}
