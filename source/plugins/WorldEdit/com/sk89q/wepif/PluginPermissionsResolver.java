package com.sk89q.wepif;

import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class PluginPermissionsResolver implements PermissionsResolver {
   protected PermissionsProvider resolver;
   protected Plugin plugin;

   public static PermissionsResolver factory(Server server, YAMLProcessor config) {
      RegisteredServiceProvider<PermissionsProvider> serviceProvider = server.getServicesManager().getRegistration(PermissionsProvider.class);
      if (serviceProvider != null) {
         return new PluginPermissionsResolver((PermissionsProvider)serviceProvider.getProvider(), serviceProvider.getPlugin());
      } else {
         for(Plugin plugin : server.getPluginManager().getPlugins()) {
            if (plugin instanceof PermissionsProvider) {
               return new PluginPermissionsResolver((PermissionsProvider)plugin, plugin);
            }
         }

         return null;
      }
   }

   public PluginPermissionsResolver(PermissionsProvider resolver, Plugin permissionsPlugin) {
      this.resolver = resolver;
      this.plugin = permissionsPlugin;
   }

   public void load() {
   }

   public boolean hasPermission(String name, String permission) {
      return this.resolver.hasPermission(name, permission);
   }

   public boolean hasPermission(String worldName, String name, String permission) {
      return this.resolver.hasPermission(worldName, name, permission);
   }

   public boolean inGroup(String player, String group) {
      return this.resolver.inGroup(player, group);
   }

   public String[] getGroups(String player) {
      return this.resolver.getGroups(player);
   }

   public boolean hasPermission(OfflinePlayer player, String permission) {
      return this.resolver.hasPermission(player, permission);
   }

   public boolean hasPermission(String worldName, OfflinePlayer player, String permission) {
      return this.resolver.hasPermission(worldName, player, permission);
   }

   public boolean inGroup(OfflinePlayer player, String group) {
      return this.resolver.inGroup(player, group);
   }

   public String[] getGroups(OfflinePlayer player) {
      return this.resolver.getGroups(player);
   }

   public String getDetectionMessage() {
      return "Using plugin '" + this.plugin.getDescription().getName() + "' for permissions.";
   }
}
