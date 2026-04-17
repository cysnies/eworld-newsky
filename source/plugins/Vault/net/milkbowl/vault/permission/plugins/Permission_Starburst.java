package net.milkbowl.vault.permission.plugins;

import com.dthielke.starburst.Group;
import com.dthielke.starburst.GroupManager;
import com.dthielke.starburst.GroupSet;
import com.dthielke.starburst.StarburstPlugin;
import com.dthielke.starburst.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

public class Permission_Starburst extends Permission {
   private StarburstPlugin perms;
   private final String name = "Starburst";

   public Permission_Starburst(Plugin plugin) {
      this.plugin = plugin;
      Bukkit.getServer().getPluginManager().registerEvents(new PermissionServerListener(), plugin);
      if (this.perms == null) {
         Plugin p = plugin.getServer().getPluginManager().getPlugin("Starburst");
         if (p != null) {
            this.perms = (StarburstPlugin)p;
            log.info(String.format("[%s][Permission] %s hooked.", plugin.getDescription().getName(), "Starburst"));
         }
      }

   }

   public String[] getGroups() {
      String[] s = new String[this.perms.getGroupManager().getDefaultGroupSet().getGroups().size()];
      int i = 0;

      for(Group g : this.perms.getGroupManager().getDefaultGroupSet().getGroups()) {
         s[i] = g.getName();
         ++i;
      }

      return s;
   }

   public String getName() {
      return "Starburst";
   }

   public String[] getPlayerGroups(String world, String player) {
      OfflinePlayer op = Bukkit.getOfflinePlayer(player);
      GroupSet set = this.perms.getGroupManager().getWorldSet(Bukkit.getWorld(world));
      User user = set.getUser(op);
      Set<Group> children = user.getChildren(true);
      List<String> groups = new ArrayList();

      for(Group child : children) {
         groups.add(child.getName());
      }

      return (String[])groups.toArray(new String[groups.size()]);
   }

   public String getPrimaryGroup(String world, String player) {
      OfflinePlayer op = Bukkit.getOfflinePlayer(player);
      GroupSet set = this.perms.getGroupManager().getWorldSet(Bukkit.getWorld(world));
      User user = set.getUser(op);
      Set<Group> children = user.getChildren(false);
      return !children.isEmpty() ? ((Group)children.iterator().next()).getName() : null;
   }

   public boolean groupAdd(String world, String group, String permission) {
      GroupManager gm = this.perms.getGroupManager();
      GroupSet set = gm.getWorldSet(Bukkit.getWorld(world));
      if (!set.hasGroup(group)) {
         return false;
      } else {
         Group g = set.getGroup(group);
         boolean value = !permission.startsWith("^");
         permission = value ? permission : permission.substring(1);
         g.addPermission(permission, value, true, true);

         for(User user : gm.getAffectedUsers(g)) {
            user.applyPermissions(gm.getFactory());
         }

         return true;
      }
   }

   public boolean groupHas(String world, String group, String permission) {
      GroupSet set = this.perms.getGroupManager().getWorldSet(Bukkit.getWorld(world));
      if (set.hasGroup(group)) {
         Group g = set.getGroup(group);
         return g.hasPermission(permission, true);
      } else {
         return false;
      }
   }

   public boolean groupRemove(String world, String group, String permission) {
      GroupManager gm = this.perms.getGroupManager();
      GroupSet set = gm.getWorldSet(Bukkit.getWorld(world));
      if (!set.hasGroup(group)) {
         return false;
      } else {
         Group g = set.getGroup(group);
         boolean value = !permission.startsWith("^");
         permission = value ? permission : permission.substring(1);
         if (!g.hasPermission(permission, false)) {
            return false;
         } else {
            g.removePermission(permission, true);

            for(User user : gm.getAffectedUsers(g)) {
               user.applyPermissions(gm.getFactory());
            }

            return true;
         }
      }
   }

   public boolean hasSuperPermsCompat() {
      return true;
   }

   public boolean isEnabled() {
      return this.perms != null && this.perms.isEnabled();
   }

   public boolean playerAdd(String world, String player, String permission) {
      OfflinePlayer op = Bukkit.getOfflinePlayer(player);
      GroupSet set = this.perms.getGroupManager().getWorldSet(Bukkit.getWorld(world));
      User user = set.getUser(op);
      boolean value = !permission.startsWith("^");
      permission = value ? permission : permission.substring(1);
      user.addPermission(permission, value, true, true);
      if (user.isActive()) {
         user.applyPermissions(this.perms.getGroupManager().getFactory());
      }

      return true;
   }

   public boolean playerAddGroup(String world, String player, String group) {
      OfflinePlayer op = Bukkit.getOfflinePlayer(player);
      GroupSet set = this.perms.getGroupManager().getWorldSet(Bukkit.getWorld(world));
      User user = set.getUser(op);
      if (set.hasGroup(group)) {
         Group g = set.getGroup(group);
         if (!user.hasChild(g, false)) {
            user.addChild(g, true);
            if (user.isActive()) {
               user.applyPermissions(this.perms.getGroupManager().getFactory());
            }

            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean playerHas(String world, String player, String permission) {
      OfflinePlayer op = Bukkit.getOfflinePlayer(player);
      if (op.isOnline()) {
         Player p = (Player)op;
         if (p.getWorld().getName().equalsIgnoreCase(world)) {
            return p.hasPermission(permission);
         }
      }

      GroupSet set = this.perms.getGroupManager().getWorldSet(Bukkit.getWorld(world));
      Group user = set.getUser(op);
      return user.hasPermission(permission, true);
   }

   public boolean playerInGroup(String world, String player, String group) {
      OfflinePlayer op = Bukkit.getOfflinePlayer(player);
      GroupSet set = this.perms.getGroupManager().getWorldSet(Bukkit.getWorld(world));
      User user = set.getUser(op);
      if (set.hasGroup(group)) {
         Group g = set.getGroup(group);
         return user.hasChild(g, true);
      } else {
         return false;
      }
   }

   public boolean playerRemove(String world, String player, String permission) {
      OfflinePlayer op = Bukkit.getOfflinePlayer(player);
      GroupSet set = this.perms.getGroupManager().getWorldSet(Bukkit.getWorld(world));
      User user = set.getUser(op);
      boolean value = !permission.startsWith("^");
      permission = value ? permission : permission.substring(1);
      if (user.hasPermission(permission, false)) {
         user.removePermission(permission, true);
         if (user.isActive()) {
            user.applyPermissions(this.perms.getGroupManager().getFactory());
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean playerRemoveGroup(String world, String player, String group) {
      OfflinePlayer op = Bukkit.getOfflinePlayer(player);
      GroupSet set = this.perms.getGroupManager().getWorldSet(Bukkit.getWorld(world));
      User user = set.getUser(op);
      if (set.hasGroup(group)) {
         Group g = set.getGroup(group);
         if (user.hasChild(g, false)) {
            user.removeChild(g, true);
            if (user.isActive()) {
               user.applyPermissions(this.perms.getGroupManager().getFactory());
            }

            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public class PermissionServerListener implements Listener {
      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginEnable(PluginEnableEvent event) {
         if (Permission_Starburst.this.perms == null) {
            Plugin p = event.getPlugin();
            if (p.getDescription().getName().equals("Starburst") && p.isEnabled()) {
               Permission_Starburst.this.perms = (StarburstPlugin)p;
               Permission_Starburst.log.info(String.format("[%s][Permission] %s hooked.", Permission_Starburst.this.plugin.getDescription().getName(), "Starburst"));
            }
         }

      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginDisable(PluginDisableEvent event) {
         if (Permission_Starburst.this.perms != null && event.getPlugin().getDescription().getName().equals("Starburst")) {
            Permission_Starburst.this.perms = null;
            Permission_Starburst.log.info(String.format("[%s][Permission] %s un-hooked.", Permission_Starburst.this.plugin.getDescription().getName(), "Starburst"));
         }

      }
   }
}
