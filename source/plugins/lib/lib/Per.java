package lib;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class Per {
   private Server server;
   private static Permission permission = null;

   public Per(Lib lib) {
      this.server = lib.getServer();
      this.setupPermissions();
   }

   private void setupPermissions() {
      RegisteredServiceProvider<Permission> permissionProvider = this.server.getServicesManager().getRegistration(Permission.class);
      permission = (Permission)permissionProvider.getProvider();
   }

   public boolean has(Player p, String per) {
      return per.trim().isEmpty() ? true : permission.playerHas(p, per);
   }

   public boolean has(String name, String per) {
      return per.trim().isEmpty() ? true : permission.playerHas("world", name, per);
   }

   public boolean add(Player p, String per) {
      return permission.playerAdd(p, per);
   }

   public boolean add(String name, String per) {
      return permission.playerAdd("world", name, per);
   }

   public boolean remove(Player p, String per) {
      return permission.playerRemove(p, per);
   }

   public boolean inGroup(Player p, String group) {
      return permission.playerInGroup(p, group);
   }

   public boolean addGroup(Player p, String group) {
      return permission.playerAddGroup(p, group);
   }
}
