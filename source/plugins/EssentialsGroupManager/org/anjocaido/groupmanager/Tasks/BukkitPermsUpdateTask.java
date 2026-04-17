package org.anjocaido.groupmanager.Tasks;

import org.anjocaido.groupmanager.GroupManager;

public class BukkitPermsUpdateTask implements Runnable {
   public void run() {
      GroupManager.setLoaded(true);
      GroupManager.BukkitPermissions.collectPermissions();
      GroupManager.BukkitPermissions.updateAllPlayers();
      GroupManager.logger.info("Bukkit Permissions Updated!");
   }
}
