package com.comphenix.protocol.injector;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLoadOrder;

class PluginVerifier {
   private static final Set DYNAMIC_DEPENDENCY = Sets.newHashSet(new String[]{"mcore"});
   private final Set loadedAfter = new HashSet();
   private final Plugin dependency;

   public PluginVerifier(Plugin dependency) {
      if (dependency == null) {
         throw new IllegalArgumentException("dependency cannot be NULL.");
      } else if (this.safeConversion(dependency.getDescription().getLoadBefore()).size() > 0) {
         throw new IllegalArgumentException("dependency cannot have a load directives.");
      } else {
         this.dependency = dependency;
      }
   }

   private Plugin getPlugin(String pluginName) {
      Plugin plugin = this.getPluginOrDefault(pluginName);
      if (plugin != null) {
         return plugin;
      } else {
         throw new PluginNotFoundException("Cannot find plugin " + pluginName);
      }
   }

   private Plugin getPluginOrDefault(String pluginName) {
      return this.dependency.getServer().getPluginManager().getPlugin(pluginName);
   }

   public VerificationResult verify(String pluginName) {
      if (pluginName == null) {
         throw new IllegalArgumentException("pluginName cannot be NULL.");
      } else {
         return this.verify(this.getPlugin(pluginName));
      }
   }

   public VerificationResult verify(Plugin plugin) {
      if (plugin == null) {
         throw new IllegalArgumentException("plugin cannot be NULL.");
      } else {
         String name = plugin.getName();
         if (!this.dependency.equals(plugin) && !this.loadedAfter.contains(name) && !DYNAMIC_DEPENDENCY.contains(name)) {
            if (!this.verifyLoadOrder(this.dependency, plugin)) {
               return PluginVerifier.VerificationResult.NO_DEPEND;
            }

            this.loadedAfter.add(plugin.getName());
         }

         return PluginVerifier.VerificationResult.VALID;
      }
   }

   private boolean verifyLoadOrder(Plugin beforePlugin, Plugin afterPlugin) {
      if (this.hasDependency(afterPlugin, beforePlugin)) {
         return true;
      } else {
         return beforePlugin.getDescription().getLoad() == PluginLoadOrder.STARTUP && afterPlugin.getDescription().getLoad() == PluginLoadOrder.POSTWORLD;
      }
   }

   private boolean hasDependency(Plugin plugin, Plugin dependency) {
      return this.hasDependency(plugin, dependency, Sets.newHashSet());
   }

   private Set safeConversion(List list) {
      return (Set)(list == null ? Collections.emptySet() : Sets.newHashSet(list));
   }

   private boolean hasDependency(Plugin plugin, Plugin dependency, Set checking) {
      Set<String> childNames = Sets.union(this.safeConversion(plugin.getDescription().getDepend()), this.safeConversion(plugin.getDescription().getSoftDepend()));
      if (!checking.add(plugin.getName())) {
         throw new IllegalStateException("Cycle detected in dependency graph: " + plugin);
      } else if (childNames.contains(dependency.getName())) {
         return true;
      } else {
         for(String childName : childNames) {
            Plugin childPlugin = this.getPluginOrDefault(childName);
            if (childPlugin != null && this.hasDependency(childPlugin, dependency, checking)) {
               return true;
            }
         }

         checking.remove(plugin.getName());
         return false;
      }
   }

   public static class PluginNotFoundException extends RuntimeException {
      private static final long serialVersionUID = 8956699101336877611L;

      public PluginNotFoundException() {
      }

      public PluginNotFoundException(String message) {
         super(message);
      }
   }

   public static enum VerificationResult {
      VALID,
      NO_DEPEND;

      public boolean isValid() {
         return this == VALID;
      }
   }
}
