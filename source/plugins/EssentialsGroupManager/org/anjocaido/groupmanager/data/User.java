package org.anjocaido.groupmanager.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.anjocaido.groupmanager.GroupManager;
import org.anjocaido.groupmanager.dataholder.WorldDataHolder;
import org.anjocaido.groupmanager.events.GMUserEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class User extends DataUnit implements Cloneable {
   private String group = null;
   private final List subGroups = Collections.synchronizedList(new ArrayList());
   private UserVariables variables = new UserVariables(this);
   private transient Player bukkitPlayer = null;

   public User(WorldDataHolder source, String name) {
      super(source, name);
      this.group = source.getDefaultGroup().getName();
   }

   public User clone() {
      User clone = new User(this.getDataSource(), this.getName());
      clone.group = this.group;

      for(String perm : this.getPermissionList()) {
         clone.addPermission(perm);
      }

      return clone;
   }

   public User clone(WorldDataHolder dataSource) {
      if (dataSource.isUserDeclared(this.getName())) {
         return null;
      } else {
         User clone = dataSource.createUser(this.getName());
         if (dataSource.getGroup(this.group) == null) {
            clone.setGroup(dataSource.getDefaultGroup());
         } else {
            clone.setGroup(dataSource.getGroup(this.getGroupName()));
         }

         for(String perm : this.getPermissionList()) {
            clone.addPermission(perm);
         }

         clone.variables = this.variables.clone(this);
         clone.flagAsChanged();
         return clone;
      }
   }

   public Group getGroup() {
      Group result = this.getDataSource().getGroup(this.group);
      if (result == null) {
         this.setGroup(this.getDataSource().getDefaultGroup());
         result = this.getDataSource().getDefaultGroup();
      }

      return result;
   }

   public String getGroupName() {
      Group result = this.getDataSource().getGroup(this.group);
      if (result == null) {
         this.group = this.getDataSource().getDefaultGroup().getName();
      }

      return this.group;
   }

   public void setGroup(Group group) {
      this.setGroup(group, true);
   }

   public void setGroup(Group group, Boolean updatePerms) {
      if (!this.getDataSource().groupExists(group.getName())) {
         this.getDataSource().addGroup(group);
      }

      group = this.getDataSource().getGroup(group.getName());
      String oldGroup = this.group;
      this.group = group.getName();
      this.flagAsChanged();
      if (GroupManager.isLoaded()) {
         if (!GroupManager.BukkitPermissions.isPlayer_join() && updatePerms) {
            GroupManager.BukkitPermissions.updatePlayer(this.getBukkitPlayer());
         }

         String defaultGroupName = this.getDataSource().getDefaultGroup().getName();
         boolean notify = !oldGroup.equalsIgnoreCase(defaultGroupName) || oldGroup.equalsIgnoreCase(defaultGroupName) && !this.group.equalsIgnoreCase(defaultGroupName);
         if (notify) {
            GroupManager.notify(this.getName(), String.format(" moved to the group %s in %s.", group.getName(), this.getDataSource().getName()));
         }

         GroupManager.getGMEventHandler().callEvent(this, GMUserEvent.Action.USER_GROUP_CHANGED);
      }

   }

   public boolean addSubGroup(Group subGroup) {
      if (this.group.equalsIgnoreCase(subGroup.getName())) {
         return false;
      } else if (this.containsSubGroup(subGroup)) {
         return false;
      } else {
         if (!this.getDataSource().groupExists(subGroup.getName())) {
            this.getDataSource().addGroup(subGroup);
         }

         this.subGroups.add(subGroup.getName());
         this.flagAsChanged();
         if (GroupManager.isLoaded()) {
            if (!GroupManager.BukkitPermissions.isPlayer_join()) {
               GroupManager.BukkitPermissions.updatePlayer(this.getBukkitPlayer());
            }

            GroupManager.getGMEventHandler().callEvent(this, GMUserEvent.Action.USER_SUBGROUP_CHANGED);
         }

         return true;
      }
   }

   public int subGroupsSize() {
      return this.subGroups.size();
   }

   public boolean isSubGroupsEmpty() {
      return this.subGroups.isEmpty();
   }

   public boolean containsSubGroup(Group subGroup) {
      return this.subGroups.contains(subGroup.getName());
   }

   public boolean removeSubGroup(Group subGroup) {
      try {
         if (this.subGroups.remove(subGroup.getName())) {
            this.flagAsChanged();
            if (GroupManager.isLoaded() && !GroupManager.BukkitPermissions.isPlayer_join()) {
               GroupManager.BukkitPermissions.updatePlayer(this.getBukkitPlayer());
            }

            GroupManager.getGMEventHandler().callEvent(this, GMUserEvent.Action.USER_SUBGROUP_CHANGED);
            return true;
         }
      } catch (Exception var3) {
      }

      return false;
   }

   public ArrayList subGroupListCopy() {
      ArrayList<Group> val = new ArrayList();
      synchronized(this.subGroups) {
         for(String gstr : this.subGroups) {
            Group g = this.getDataSource().getGroup(gstr);
            if (g == null) {
               this.removeSubGroup(g);
            } else {
               val.add(g);
            }
         }

         return val;
      }
   }

   public ArrayList subGroupListStringCopy() {
      synchronized(this.subGroups) {
         return new ArrayList(this.subGroups);
      }
   }

   public UserVariables getVariables() {
      return this.variables;
   }

   public void setVariables(Map varList) {
      this.variables.clearVars();

      for(String key : varList.keySet()) {
         this.variables.addVar(key, varList.get(key));
      }

      this.flagAsChanged();
      if (GroupManager.isLoaded()) {
         GroupManager.getGMEventHandler().callEvent(this, GMUserEvent.Action.USER_INFO_CHANGED);
      }

   }

   public User updatePlayer(Player player) {
      this.bukkitPlayer = player;
      return this;
   }

   public Player getBukkitPlayer() {
      if (this.bukkitPlayer == null) {
         this.bukkitPlayer = Bukkit.getPlayer(this.getName());
      }

      return this.bukkitPlayer;
   }
}
