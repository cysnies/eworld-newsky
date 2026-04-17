package net.milkbowl.vault;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class Metrics {
   private static final int REVISION = 5;
   private static final String BASE_URL = "http://metrics.griefcraft.com";
   private static final String REPORT_URL = "/report/%s";
   private static final String CUSTOM_DATA_SEPARATOR = "~~";
   private static final String CONFIG_FILE = "plugins/PluginMetrics/config.yml";
   private static final int PING_INTERVAL = 10;
   private Map graphs = Collections.synchronizedMap(new HashMap());
   private Map defaultGraphs = Collections.synchronizedMap(new HashMap());
   private final YamlConfiguration configuration;
   private String guid;
   private final String pluginVersion;
   private final String authors;

   public Metrics(String version, String authors) throws IOException {
      this.pluginVersion = version;
      this.authors = authors;
      File file = new File("plugins/PluginMetrics/config.yml");
      this.configuration = YamlConfiguration.loadConfiguration(file);
      this.configuration.addDefault("opt-out", false);
      this.configuration.addDefault("guid", UUID.randomUUID().toString());
      if (this.configuration.get("guid", (Object)null) == null) {
         this.configuration.options().header("http://metrics.griefcraft.com").copyDefaults(true);
         this.configuration.save(file);
      }

      this.guid = this.configuration.getString("guid");
   }

   public Graph createGraph(Plugin plugin, Graph.Type type, String name) {
      if (plugin != null && type != null && name != null) {
         Graph graph = new Graph(type, name);
         Set<Graph> graphs = this.getOrCreateGraphs(plugin);
         graphs.add(graph);
         return graph;
      } else {
         throw new IllegalArgumentException("All arguments must not be null");
      }
   }

   public void findCustomData(Vault plugin) {
      Graph econGraph = this.createGraph(plugin, Metrics.Graph.Type.Pie, "Economy");
      RegisteredServiceProvider<Economy> rspEcon = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
      Economy econ = null;
      if (rspEcon != null) {
         econ = (Economy)rspEcon.getProvider();
      }

      String econName = econ != null ? econ.getName() : "No Economy";
      econGraph.addPlotter(new Plotter(econName) {
         public int getValue() {
            return 1;
         }
      });
      Graph permGraph = this.createGraph(plugin, Metrics.Graph.Type.Pie, "Permission");
      String permName = ((Permission)Bukkit.getServer().getServicesManager().getRegistration(Permission.class).getProvider()).getName();
      permGraph.addPlotter(new Plotter(permName) {
         public int getValue() {
            return 1;
         }
      });
      Graph chatGraph = this.createGraph(plugin, Metrics.Graph.Type.Pie, "Chat");
      RegisteredServiceProvider<Chat> rspChat = Bukkit.getServer().getServicesManager().getRegistration(Chat.class);
      Chat chat = null;
      if (rspChat != null) {
         chat = (Chat)rspChat.getProvider();
      }

      String chatName = chat != null ? chat.getName() : "No Chat";
      chatGraph.addPlotter(new Plotter(chatName) {
         public int getValue() {
            return 1;
         }
      });
   }

   public synchronized void addCustomData(Plugin plugin, Plotter plotter) {
      Graph graph = this.getOrCreateDefaultGraph(plugin);
      graph.addPlotter(plotter);
      this.getOrCreateGraphs(plugin).add(graph);
   }

   public void beginMeasuringPlugin(final Plugin plugin) throws IOException {
      if (!this.configuration.getBoolean("opt-out", false)) {
         plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
            private boolean firstPost = true;

            public void run() {
               try {
                  Metrics.this.postPlugin(plugin, !this.firstPost);
                  this.firstPost = false;
               } catch (IOException var2) {
               }

            }
         }, 0L, 12000L);
      }
   }

   private void postPlugin(Plugin plugin, boolean isPing) throws IOException {
      String data = encode("guid") + '=' + encode(this.guid) + encodeDataPair("authors", this.authors) + encodeDataPair("version", this.pluginVersion) + encodeDataPair("server", Bukkit.getVersion()) + encodeDataPair("players", Integer.toString(Bukkit.getServer().getOnlinePlayers().length)) + encodeDataPair("revision", String.valueOf(5));
      if (isPing) {
         data = data + encodeDataPair("ping", "true");
      }

      Set<Graph> graphs = this.getOrCreateGraphs(plugin);
      synchronized(graphs) {
         for(Graph graph : graphs) {
            for(Plotter plotter : graph.getPlotters()) {
               String key = String.format("C%s%s%s%s", "~~", graph.getName(), "~~", plotter.getColumnName());
               String value = Integer.toString(plotter.getValue());
               data = data + encodeDataPair(key, value);
            }
         }
      }

      URL url = new URL("http://metrics.griefcraft.com" + String.format("/report/%s", plugin.getDescription().getName()));
      URLConnection connection;
      if (this.isMineshafterPresent()) {
         connection = url.openConnection(Proxy.NO_PROXY);
      } else {
         connection = url.openConnection();
      }

      connection.setDoOutput(true);
      OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
      writer.write(data);
      writer.flush();
      BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      String response = reader.readLine();
      writer.close();
      reader.close();
      if (response != null && !response.startsWith("ERR")) {
         if (response.contains("OK This is your first update this hour")) {
            synchronized(graphs) {
               for(Graph graph : graphs) {
                  for(Plotter plotter : graph.getPlotters()) {
                     plotter.reset();
                  }
               }
            }
         }

      } else {
         throw new IOException(response);
      }
   }

   private Set getOrCreateGraphs(Plugin plugin) {
      Set<Graph> theGraphs = (Set)this.graphs.get(plugin);
      if (theGraphs == null) {
         theGraphs = Collections.synchronizedSet(new HashSet());
         this.graphs.put(plugin, theGraphs);
      }

      return theGraphs;
   }

   private Graph getOrCreateDefaultGraph(Plugin plugin) {
      Graph graph = (Graph)this.defaultGraphs.get(plugin);
      if (graph == null) {
         graph = new Graph(Metrics.Graph.Type.Line, "Default");
         this.defaultGraphs.put(plugin, graph);
      }

      return graph;
   }

   private boolean isMineshafterPresent() {
      try {
         Class.forName("mineshafter.MineServer");
         return true;
      } catch (Exception var2) {
         return false;
      }
   }

   private static String encodeDataPair(String key, String value) throws UnsupportedEncodingException {
      return "&" + encode(key) + "=" + encode(value);
   }

   private static String encode(String text) throws UnsupportedEncodingException {
      return URLEncoder.encode(text, "UTF-8");
   }

   public static class Graph {
      private final Type type;
      private final String name;
      private final Set plotters;

      private Graph(Type type, String name) {
         this.plotters = new LinkedHashSet();
         this.type = type;
         this.name = name;
      }

      public String getName() {
         return this.name;
      }

      public void addPlotter(Plotter plotter) {
         this.plotters.add(plotter);
      }

      public void removePlotter(Plotter plotter) {
         this.plotters.remove(plotter);
      }

      public Set getPlotters() {
         return Collections.unmodifiableSet(this.plotters);
      }

      public int hashCode() {
         return this.type.hashCode() * 17 ^ this.name.hashCode();
      }

      public boolean equals(Object object) {
         if (!(object instanceof Graph)) {
            return false;
         } else {
            Graph graph = (Graph)object;
            return graph.type == this.type && graph.name.equals(this.name);
         }
      }

      public static enum Type {
         Line,
         Area,
         Column,
         Pie;
      }
   }

   public abstract static class Plotter {
      private final String name;

      public Plotter() {
         this("Default");
      }

      public Plotter(String name) {
         this.name = name;
      }

      public String getColumnName() {
         return this.name;
      }

      public abstract int getValue();

      public int hashCode() {
         return this.getColumnName().hashCode() + this.getValue();
      }

      public void reset() {
      }

      public boolean equals(Object object) {
         if (!(object instanceof Plotter)) {
            return false;
         } else {
            Plotter plotter = (Plotter)object;
            return plotter.getColumnName().equals(this.getColumnName()) && plotter.getValue() == this.getValue();
         }
      }
   }
}
