package net.milkbowl.vault.permission;

import java.util.logging.Logger;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

public abstract class Permission {
   protected static final Logger log = Logger.getLogger("Minecraft");
   protected Plugin plugin = null;

   public abstract String getName();

   public abstract boolean isEnabled();

   public abstract boolean hasSuperPermsCompat();

   public boolean has(String world, String player, String permission) {
      return world == null ? this.playerHas((String)null, player, permission) : this.playerHas(world, player, permission);
   }

   public boolean has(World world, String player, String permission) {
      return world == null ? this.playerHas((String)null, player, permission) : this.playerHas(world.getName(), player, permission);
   }

   public boolean has(CommandSender sender, String permission) {
      return sender.hasPermission(permission);
   }

   public boolean has(Player player, String permission) {
      return player.hasPermission(permission);
   }

   public abstract boolean playerHas(String var1, String var2, String var3);

   public boolean playerHas(World world, String player, String permission) {
      return world == null ? this.playerHas((String)null, player, permission) : this.playerHas(world.getName(), player, permission);
   }

   public boolean playerHas(Player player, String permission) {
      return this.has(player, permission);
   }

   public abstract boolean playerAdd(String var1, String var2, String var3);

   public boolean playerAdd(World world, String player, String permission) {
      return world == null ? this.playerAdd((String)null, player, permission) : this.playerAdd(world.getName(), player, permission);
   }

   public boolean playerAdd(Player player, String permission) {
      return this.playerAdd(player.getWorld().getName(), player.getName(), permission);
   }

   public boolean playerAddTransient(String player, String permission) throws UnsupportedOperationException {
      Player p = this.plugin.getServer().getPlayer(player);
      if (p == null) {
         throw new UnsupportedOperationException(this.getName() + " does not support offline player transient permissions!");
      } else {
         return this.playerAddTransient(p, permission);
      }
   }

   public boolean playerAddTransient(Player player, String permission) {
      for(PermissionAttachmentInfo paInfo : player.getEffectivePermissions()) {
         if (paInfo.getAttachment() != null && paInfo.getAttachment().getPlugin().equals(this.plugin)) {
            paInfo.getAttachment().setPermission(permission, true);
            return true;
         }
      }

      PermissionAttachment attach = player.addAttachment(this.plugin);
      attach.setPermission(permission, true);
      return true;
   }

   public boolean playerAddTransient(String worldName, Player player, String permission) {
      return this.playerAddTransient(player, permission);
   }

   public boolean playerAddTransient(String worldName, String player, String permission) {
      Player p = this.plugin.getServer().getPlayer(player);
      if (p == null) {
         throw new UnsupportedOperationException(this.getName() + " does not support offline player transient permissions!");
      } else {
         return this.playerAddTransient(p, permission);
      }
   }

   public boolean playerRemoveTransient(String worldName, String player, String permission) {
      Player p = this.plugin.getServer().getPlayer(player);
      return p == null ? false : this.playerRemoveTransient(p, permission);
   }

   public boolean playerRemoveTransient(String worldName, Player player, String permission) {
      return this.playerRemoveTransient(player, permission);
   }

   public abstract boolean playerRemove(String var1, String var2, String var3);

   public boolean playerRemove(World world, String player, String permission) {
      return world == null ? this.playerRemove((String)null, player, permission) : this.playerRemove(world.getName(), player, permission);
   }

   public boolean playerRemove(Player player, String permission) {
      return this.playerRemove(player.getWorld().getName(), player.getName(), permission);
   }

   public boolean playerRemoveTransient(String player, String permission) {
      Player p = this.plugin.getServer().getPlayer(player);
      return p == null ? false : this.playerRemoveTransient(p, permission);
   }

   public boolean playerRemoveTransient(Player player, String permission) {
      for(PermissionAttachmentInfo paInfo : player.getEffectivePermissions()) {
         if (paInfo.getAttachment() != null && paInfo.getAttachment().getPlugin().equals(this.plugin)) {
            paInfo.getAttachment().unsetPermission(permission);
            return true;
         }
      }

      return false;
   }

   public abstract boolean groupHas(String var1, String var2, String var3);

   public boolean groupHas(World world, String group, String permission) {
      return world == null ? this.groupHas((String)null, group, permission) : this.groupHas(world.getName(), group, permission);
   }

   public abstract boolean groupAdd(String var1, String var2, String var3);

   public boolean groupAdd(World world, String group, String permission) {
      return world == null ? this.groupAdd((String)null, group, permission) : this.groupAdd(world.getName(), group, permission);
   }

   public abstract boolean groupRemove(String var1, String var2, String var3);

   public boolean groupRemove(World world, String group, String permission) {
      return world == null ? this.groupRemove((String)null, group, permission) : this.groupRemove(world.getName(), group, permission);
   }

   public abstract boolean playerInGroup(String var1, String var2, String var3);

   public boolean playerInGroup(World world, String player, String group) {
      return world == null ? this.playerInGroup((String)null, player, group) : this.playerInGroup(world.getName(), player, group);
   }

   public boolean playerInGroup(Player player, String group) {
      return this.playerInGroup(player.getWorld().getName(), player.getName(), group);
   }

   public abstract boolean playerAddGroup(String var1, String var2, String var3);

   public boolean playerAddGroup(World world, String player, String group) {
      return world == null ? this.playerAddGroup((String)null, player, group) : this.playerAddGroup(world.getName(), player, group);
   }

   public boolean playerAddGroup(Player player, String group) {
      return this.playerAddGroup(player.getWorld().getName(), player.getName(), group);
   }

   public abstract boolean playerRemoveGroup(String var1, String var2, String var3);

   public boolean playerRemoveGroup(World world, String player, String group) {
      return world == null ? this.playerRemoveGroup((String)null, player, group) : this.playerRemoveGroup(world.getName(), player, group);
   }

   public boolean playerRemoveGroup(Player player, String group) {
      return this.playerRemoveGroup(player.getWorld().getName(), player.getName(), group);
   }

   public abstract String[] getPlayerGroups(String var1, String var2);

   public String[] getPlayerGroups(World world, String player) {
      return world == null ? this.getPlayerGroups((String)null, player) : this.getPlayerGroups(world.getName(), player);
   }

   public String[] getPlayerGroups(Player player) {
      return this.getPlayerGroups(player.getWorld().getName(), player.getName());
   }

   public abstract String getPrimaryGroup(String var1, String var2);

   public String getPrimaryGroup(World world, String player) {
      return world == null ? this.getPrimaryGroup((String)null, player) : this.getPrimaryGroup(world.getName(), player);
   }

   public String getPrimaryGroup(Player player) {
      return this.getPrimaryGroup(player.getWorld().getName(), player.getName());
   }

   public abstract String[] getGroups();
}
