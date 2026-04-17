package org.yi.acru.bukkit.Lockette;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.PluginManager;

public class LockettePrefixListener implements Listener {
   private static Lockette plugin;

   public LockettePrefixListener(Lockette instance) {
      plugin = instance;
   }

   protected void registerEvents() {
      PluginManager pm = plugin.getServer().getPluginManager();
      pm.registerEvents(this, plugin);
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onSignChange(SignChangeEvent event) {
      Block block = event.getBlock();
      Player player = event.getPlayer();
      boolean typeWallSign = block.getTypeId() == Material.WALL_SIGN.getId();
      boolean typeSignPost = block.getTypeId() == Material.SIGN_POST.getId();
      if (typeWallSign) {
         Sign sign = (Sign)block.getState();
         String text = sign.getLine(0).replaceAll("(?i)§[0-F]", "");
         if (text.equalsIgnoreCase("[Private]") || text.equalsIgnoreCase(Lockette.altPrivate) || text.equalsIgnoreCase("[More Users]") || text.equalsIgnoreCase(Lockette.altMoreUsers)) {
            event.setCancelled(true);
            event.setLine(0, sign.getLine(0));
            event.setLine(1, sign.getLine(1));
            event.setLine(2, sign.getLine(2));
            event.setLine(3, sign.getLine(3));
            Lockette.log.info("[" + plugin.getDescription().getName() + "] " + player.getName() + " just tried to change a non-editable sign. (Bukkit bug, or plugin conflict?)");
            return;
         }
      } else if (!typeSignPost) {
         event.setCancelled(true);
         Lockette.log.info("[" + plugin.getDescription().getName() + "] " + player.getName() + " just tried to set text for a non-sign. (Bukkit bug, or hacked client?)");
         return;
      }

      if (Lockette.colorTags) {
         event.setLine(0, event.getLine(0).replaceAll("&([0-9A-Fa-f])", "§$1"));
         event.setLine(1, event.getLine(1).replaceAll("&([0-9A-Fa-f])", "§$1"));
         event.setLine(2, event.getLine(2).replaceAll("&([0-9A-Fa-f])", "§$1"));
         event.setLine(3, event.getLine(3).replaceAll("&([0-9A-Fa-f])", "§$1"));
      }

   }
}
