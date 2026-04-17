package com.sk89q.worldedit.bukkit;

import com.sk89q.worldedit.SessionCheck;
import com.sk89q.worldedit.WorldEdit;
import org.bukkit.Server;
import org.bukkit.entity.Player;

public class SessionTimer implements Runnable {
   private WorldEdit worldEdit;
   private SessionCheck checker;

   public SessionTimer(WorldEdit worldEdit, final Server server) {
      this.worldEdit = worldEdit;
      this.checker = new SessionCheck() {
         public boolean isOnlinePlayer(String name) {
            Player player = server.getPlayer(name);
            return player != null && player.isOnline();
         }
      };
   }

   public void run() {
      this.worldEdit.flushExpiredSessions(this.checker);
   }
}
