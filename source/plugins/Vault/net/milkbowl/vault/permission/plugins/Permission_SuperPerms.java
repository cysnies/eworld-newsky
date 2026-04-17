package net.milkbowl.vault.permission.plugins;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class Permission_SuperPerms extends Permission {
   private final String name = "SuperPerms";

   public Permission_SuperPerms(Plugin plugin) {
      this.plugin = plugin;
   }

   public String getName() {
      return "SuperPerms";
   }

   public boolean isEnabled() {
      return true;
   }

   public boolean playerHas(String world, String player, String permission) {
      Player p = this.plugin.getServer().getPlayer(player);
      return p != null ? p.hasPermission(permission) : false;
   }

   public boolean playerAdd(String world, String player, String permission) {
      return false;
   }

   public boolean playerRemove(String world, String player, String permission) {
      return false;
   }

   public boolean groupHas(String world, String group, String permission) {
      throw new UnsupportedOperationException(this.getName() + " no group permissions.");
   }

   public boolean groupAdd(String world, String group, String permission) {
      throw new UnsupportedOperationException(this.getName() + " no group permissions.");
   }

   public boolean groupRemove(String world, String group, String permission) {
      throw new UnsupportedOperationException(this.getName() + " no group permissions.");
   }

   public boolean playerInGroup(String world, String player, String group) {
      return this.playerHas(world, player, "groups." + group);
   }

   public boolean playerAddGroup(String world, String player, String group) {
      throw new UnsupportedOperationException(this.getName() + " no group permissions.");
   }

   public boolean playerRemoveGroup(String world, String player, String group) {
      throw new UnsupportedOperationException(this.getName() + " no group permissions.");
   }

   public String[] getPlayerGroups(String world, String player) {
      throw new UnsupportedOperationException(this.getName() + " no group permissions.");
   }

   public String getPrimaryGroup(String world, String player) {
      throw new UnsupportedOperationException(this.getName() + " no group permissions.");
   }

   public String[] getGroups() {
      return new String[0];
   }

   public boolean hasSuperPermsCompat() {
      return true;
   }
}
