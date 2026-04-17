package com.sk89q.worldedit.bukkit;

import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import org.bukkit.entity.Player;

public class WorldEditAPI {
   private WorldEditPlugin plugin;

   public WorldEditAPI(WorldEditPlugin plugin) {
      this.plugin = plugin;
   }

   public LocalSession getSession(Player player) {
      return this.plugin.getWorldEdit().getSession((LocalPlayer)(new BukkitPlayer(this.plugin, this.plugin.getServerInterface(), player)));
   }
}
