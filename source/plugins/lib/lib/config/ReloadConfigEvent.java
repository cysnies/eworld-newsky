package lib.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ReloadConfigEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private String callPlugin;
   private YamlConfiguration config;

   public ReloadConfigEvent(String callPlugin, YamlConfiguration config) {
      this.callPlugin = callPlugin;
      this.config = config;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public YamlConfiguration getConfig() {
      return this.config;
   }

   public String getCallPlugin() {
      return this.callPlugin;
   }
}
