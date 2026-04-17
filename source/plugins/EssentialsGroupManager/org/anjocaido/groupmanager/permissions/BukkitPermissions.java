package org.anjocaido.groupmanager.permissions;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import org.anjocaido.groupmanager.GroupManager;
import org.anjocaido.groupmanager.data.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.PluginManager;

public class BukkitPermissions {
   protected WeakHashMap attachments = new WeakHashMap();
   protected LinkedHashMap registeredPermissions = new LinkedHashMap();
   protected GroupManager plugin;
   protected boolean dumpAllPermissions = true;
   protected boolean dumpMatchedPermissions = true;
   private boolean player_join = false;
   private static Field permissions;

   public boolean isPlayer_join() {
      return this.player_join;
   }

   public void setPlayer_join(boolean player_join) {
      this.player_join = player_join;
   }

   public BukkitPermissions(GroupManager plugin) {
      this.plugin = plugin;
      this.reset();
      this.registerEvents();
      GroupManager.logger.info("Superperms support enabled.");
   }

   public void reset() {
      this.collectPermissions();
      this.updateAllPlayers();
   }

   private void registerEvents() {
      PluginManager manager = this.plugin.getServer().getPluginManager();
      manager.registerEvents(new PlayerEvents(), this.plugin);
      manager.registerEvents(new BukkitEvents(), this.plugin);
   }

   public void collectPermissions() {
      this.registeredPermissions.clear();

      for(Permission perm : Bukkit.getPluginManager().getPermissions()) {
         this.registeredPermissions.put(perm.getName().toLowerCase(), perm);
      }

   }

   public void updatePermissions(Player player) {
      this.updatePermissions(player, (String)null);
   }

   public void updatePermissions(Player player, String world) {
      if (player != null && GroupManager.isLoaded()) {
         String name = player.getName();
         User user = this.plugin.getWorldsHolder().getWorldData(player.getWorld().getName()).getUser(name);
         if (user != null) {
            user.updatePlayer(player);
         }

         PermissionAttachment attachment;
         if (this.attachments.containsKey(name)) {
            attachment = (PermissionAttachment)this.attachments.get(name);
         } else {
            attachment = player.addAttachment(this.plugin);
            this.attachments.put(name, attachment);
         }

         if (world == null) {
            world = player.getWorld().getName();
         }

         List<String> playerPermArray = new ArrayList(this.plugin.getWorldsHolder().getWorldData(world).getPermissionsHandler().getAllPlayersPermissions(name, false));
         LinkedHashMap<String, Boolean> newPerms = new LinkedHashMap();
         List var15 = this.sort(playerPermArray);
         Boolean value = false;

         for(String permission : var15) {
            value = !permission.startsWith("-");
            newPerms.put(value ? permission : permission.substring(1), value);
         }

         if (!Bukkit.getServer().getOnlineMode() && newPerms.containsKey("groupmanager.noofflineperms") && (Boolean)newPerms.get("groupmanager.noofflineperms")) {
            this.removeAttachment(name);
         } else {
            try {
               synchronized(attachment.getPermissible()) {
                  Map<String, Boolean> orig = (Map)permissions.get(attachment);
                  orig.clear();
                  orig.putAll(newPerms);
                  attachment.getPermissible().recalculatePermissions();
               }
            } catch (IllegalArgumentException e) {
               e.printStackTrace();
            } catch (IllegalAccessException e) {
               e.printStackTrace();
            }

            GroupManager.logger.finest("Attachment updated for: " + name);
         }
      }
   }

   private List sort(List permList) {
      List<String> result = new ArrayList();

      for(String key : permList) {
         if (!key.isEmpty()) {
            String a = key.charAt(0) == '-' ? key.substring(1) : key;
            Map<String, Boolean> allchildren = GroupManager.BukkitPermissions.getAllChildren(a, new HashSet());
            if (allchildren != null) {
               ListIterator<String> itr = result.listIterator();

               while(itr.hasNext()) {
                  String node = (String)itr.next();
                  String b = node.charAt(0) == '-' ? node.substring(1) : node;
                  if (allchildren.containsKey(b)) {
                     itr.set(key);
                     itr.add(node);
                     break;
                  }
               }
            }

            if (!result.contains(key)) {
               result.add(key);
            }
         }
      }

      return result;
   }

   public List getAllRegisteredPermissions(boolean includeChildren) {
      List<String> perms = new ArrayList();

      for(String key : this.registeredPermissions.keySet()) {
         if (!perms.contains(key)) {
            perms.add(key);
            if (includeChildren) {
               Map<String, Boolean> children = this.getAllChildren(key, new HashSet());
               if (children != null) {
                  for(String node : children.keySet()) {
                     if (!perms.contains(node)) {
                        perms.add(node);
                     }
                  }
               }
            }
         }
      }

      return perms;
   }

   public Map getAllChildren(String node, Set playerPermArray) {
      LinkedList<String> stack = new LinkedList();
      Map<String, Boolean> alreadyVisited = new HashMap();
      stack.push(node);
      alreadyVisited.put(node, true);

      while(!stack.isEmpty()) {
         String now = (String)stack.pop();
         Map<String, Boolean> children = this.getChildren(now);
         if (children != null && !playerPermArray.contains("-" + now)) {
            for(String childName : children.keySet()) {
               if (!alreadyVisited.containsKey(childName)) {
                  stack.push(childName);
                  alreadyVisited.put(childName, children.get(childName));
               }
            }
         }
      }

      alreadyVisited.remove(node);
      if (!alreadyVisited.isEmpty()) {
         return alreadyVisited;
      } else {
         return null;
      }
   }

   public Map getChildren(String node) {
      Permission perm = (Permission)this.registeredPermissions.get(node.toLowerCase());
      return perm == null ? null : perm.getChildren();
   }

   public List listPerms(Player player) {
      List<String> perms = new ArrayList();
      perms.add("Effective Permissions:");

      for(PermissionAttachmentInfo info : player.getEffectivePermissions()) {
         if (info.getValue()) {
            perms.add(" " + info.getPermission() + " = " + info.getValue());
         }
      }

      return perms;
   }

   public void updateAllPlayers() {
      for(Player player : Bukkit.getServer().getOnlinePlayers()) {
         this.updatePermissions(player);
      }

   }

   public void updatePlayer(Player player) {
      if (player != null) {
         this.updatePermissions(player, (String)null);
      }

   }

   private void removeAttachment(String playerName) {
      if (this.attachments.containsKey(playerName)) {
         ((PermissionAttachment)this.attachments.get(playerName)).remove();
         this.attachments.remove(playerName);
      }

   }

   public void removeAllAttachments() {
      for(String key : this.attachments.keySet()) {
         ((PermissionAttachment)this.attachments.get(key)).remove();
      }

      this.attachments.clear();
   }

   static {
      try {
         permissions = PermissionAttachment.class.getDeclaredField("permissions");
         permissions.setAccessible(true);
      } catch (SecurityException e) {
         e.printStackTrace();
      } catch (NoSuchFieldException e) {
         e.printStackTrace();
      }

   }

   protected class PlayerEvents implements Listener {
      @EventHandler(
         priority = EventPriority.LOWEST
      )
      public void onPlayerJoin(PlayerJoinEvent event) {
         BukkitPermissions.this.setPlayer_join(true);
         Player player = event.getPlayer();
         GroupManager.logger.finest("Player Join event: " + player.getName());
         BukkitPermissions.this.removeAttachment(player.getName());
         if (BukkitPermissions.this.plugin.getWorldsHolder().getWorldData(player.getWorld().getName()).getUser(player.getName()) != null) {
            BukkitPermissions.this.setPlayer_join(false);
            BukkitPermissions.this.updatePermissions(event.getPlayer());
         }

         BukkitPermissions.this.setPlayer_join(false);
      }

      @EventHandler(
         priority = EventPriority.LOWEST
      )
      public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
         BukkitPermissions.this.updatePermissions(event.getPlayer(), event.getPlayer().getWorld().getName());
      }

      @EventHandler(
         priority = EventPriority.HIGHEST
      )
      public void onPlayerQuit(PlayerQuitEvent event) {
         if (GroupManager.isLoaded()) {
            Player player = event.getPlayer();
            BukkitPermissions.this.removeAttachment(player.getName());
         }
      }
   }

   protected class BukkitEvents implements Listener {
      @EventHandler(
         priority = EventPriority.NORMAL
      )
      public void onPluginEnable(PluginEnableEvent event) {
         if (GroupManager.isLoaded()) {
            BukkitPermissions.this.collectPermissions();
            BukkitPermissions.this.updateAllPlayers();
         }
      }

      @EventHandler(
         priority = EventPriority.NORMAL
      )
      public void onPluginDisable(PluginDisableEvent event) {
         BukkitPermissions.this.collectPermissions();
      }
   }
}
