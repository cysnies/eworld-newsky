package org.anjocaido.groupmanager.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.anjocaido.groupmanager.GroupManager;
import org.anjocaido.groupmanager.dataholder.WorldDataHolder;
import org.anjocaido.groupmanager.events.GMGroupEvent;

public class Group extends DataUnit implements Cloneable {
   private List inherits = Collections.unmodifiableList(Collections.emptyList());
   private GroupVariables variables = new GroupVariables(this);

   public Group(WorldDataHolder source, String name) {
      super(source, name);
   }

   public Group(String name) {
      super(name);
   }

   public boolean isGlobal() {
      return this.getDataSource() == null;
   }

   public Group clone() {
      Group clone;
      if (this.isGlobal()) {
         clone = new Group(this.getName());
      } else {
         clone = new Group(this.getDataSource(), this.getName());
         clone.inherits = this.getInherits().isEmpty() ? Collections.unmodifiableList(Collections.emptyList()) : Collections.unmodifiableList(new ArrayList(this.getInherits()));
      }

      for(String perm : this.getPermissionList()) {
         clone.addPermission(perm);
      }

      clone.variables = this.variables.clone(clone);
      return clone;
   }

   public Group clone(WorldDataHolder dataSource) {
      if (dataSource.groupExists(this.getName())) {
         return null;
      } else {
         Group clone = dataSource.createGroup(this.getName());
         if (!this.isGlobal()) {
            clone.inherits = this.getInherits().isEmpty() ? Collections.unmodifiableList(Collections.emptyList()) : Collections.unmodifiableList(new ArrayList(this.getInherits()));
         }

         for(String perm : this.getPermissionList()) {
            clone.addPermission(perm);
         }

         clone.variables = this.variables.clone(clone);
         clone.flagAsChanged();
         return clone;
      }
   }

   public List getInherits() {
      return this.inherits;
   }

   public void addInherits(Group inherit) {
      if (!this.isGlobal()) {
         if (!this.getDataSource().groupExists(inherit.getName())) {
            this.getDataSource().addGroup(inherit);
         }

         if (!this.inherits.contains(inherit.getName().toLowerCase())) {
            List<String> clone = new ArrayList(this.inherits);
            clone.add(inherit.getName().toLowerCase());
            this.inherits = Collections.unmodifiableList(clone);
         }

         this.flagAsChanged();
         if (GroupManager.isLoaded()) {
            GroupManager.BukkitPermissions.updateAllPlayers();
            GroupManager.getGMEventHandler().callEvent(this, GMGroupEvent.Action.GROUP_INHERITANCE_CHANGED);
         }
      }

   }

   public boolean removeInherits(String inherit) {
      if (!this.isGlobal() && this.inherits.contains(inherit.toLowerCase())) {
         List<String> clone = new ArrayList(this.inherits);
         clone.remove(inherit.toLowerCase());
         this.inherits = Collections.unmodifiableList(clone);
         this.flagAsChanged();
         GroupManager.getGMEventHandler().callEvent(this, GMGroupEvent.Action.GROUP_INHERITANCE_CHANGED);
         return true;
      } else {
         return false;
      }
   }

   public GroupVariables getVariables() {
      return this.variables;
   }

   public void setVariables(Map varList) {
      if (!this.isGlobal()) {
         GroupVariables temp = new GroupVariables(this, varList);
         this.variables.clearVars();

         for(String key : temp.getVarKeyList()) {
            this.variables.addVar(key, temp.getVarObject(key));
         }

         this.flagAsChanged();
         if (GroupManager.isLoaded()) {
            GroupManager.BukkitPermissions.updateAllPlayers();
            GroupManager.getGMEventHandler().callEvent(this, GMGroupEvent.Action.GROUP_INFO_CHANGED);
         }
      }

   }
}
