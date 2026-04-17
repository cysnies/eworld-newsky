package com.comphenix.protocol.metrics;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.plugin.Plugin;

public class Statistics {
   private Metrics metrics;

   public Statistics(Plugin plugin) throws IOException {
      this.metrics = new Metrics(plugin);
      this.addPluginUserGraph(this.metrics);
      this.metrics.start();
   }

   private void addPluginUserGraph(Metrics metrics) {
      Metrics.Graph pluginUsers = metrics.createGraph("Plugin Users");

      for(Map.Entry entry : this.getPluginUsers(ProtocolLibrary.getProtocolManager()).entrySet()) {
         final int count = (Integer)entry.getValue();
         pluginUsers.addPlotter(new Metrics.Plotter((String)entry.getKey()) {
            public int getValue() {
               return count;
            }
         });
      }

   }

   private Map getPluginUsers(ProtocolManager manager) {
      Map<String, Integer> users = new HashMap();

      for(PacketListener listener : manager.getPacketListeners()) {
         String name = PacketAdapter.getPluginName(listener);
         if (!users.containsKey(name)) {
            users.put(name, 1);
         } else {
            users.put(name, (Integer)users.get(name) + 1);
         }
      }

      return users;
   }
}
