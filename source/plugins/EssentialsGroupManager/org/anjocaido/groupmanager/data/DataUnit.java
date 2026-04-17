package org.anjocaido.groupmanager.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.anjocaido.groupmanager.GroupManager;
import org.anjocaido.groupmanager.dataholder.WorldDataHolder;
import org.anjocaido.groupmanager.utils.StringPermissionComparator;

public abstract class DataUnit {
   private WorldDataHolder dataSource;
   private String name;
   private boolean changed;
   private boolean sorted = false;
   private List permissions = Collections.unmodifiableList(Collections.emptyList());

   public DataUnit(WorldDataHolder dataSource, String name) {
      this.dataSource = dataSource;
      this.name = name;
   }

   public DataUnit(String name) {
      this.name = name;
   }

   public boolean equals(Object o) {
      if (o instanceof DataUnit) {
         DataUnit go = (DataUnit)o;
         if (this.getName().equalsIgnoreCase(go.getName())) {
            if (this.dataSource == null && go.getDataSource() == null) {
               return true;
            }

            if (this.dataSource == null && go.getDataSource() != null) {
               return false;
            }

            if (this.dataSource != null && go.getDataSource() == null) {
               return false;
            }

            if (this.dataSource.getName().equalsIgnoreCase(go.getDataSource().getName())) {
               return true;
            }
         }
      }

      return false;
   }

   public int hashCode() {
      int hash = 5;
      hash = 71 * hash + (this.name != null ? this.name.toLowerCase().hashCode() : 0);
      return hash;
   }

   public void setDataSource(WorldDataHolder source) {
      this.dataSource = source;
   }

   public WorldDataHolder getDataSource() {
      return this.dataSource;
   }

   public String getName() {
      return this.name;
   }

   public void flagAsChanged() {
      WorldDataHolder testSource = this.getDataSource();
      String source = "";
      if (testSource == null) {
         source = "GlobalGroups";
      } else {
         source = testSource.getName();
      }

      GroupManager.logger.finest("DataSource: " + source + " - DataUnit: " + this.getName() + " flagged as changed!");
      this.sorted = false;
      this.changed = true;
   }

   public boolean isChanged() {
      return this.changed;
   }

   public void flagAsSaved() {
      WorldDataHolder testSource = this.getDataSource();
      String source = "";
      if (testSource == null) {
         source = "GlobalGroups";
      } else {
         source = testSource.getName();
      }

      GroupManager.logger.finest("DataSource: " + source + " - DataUnit: " + this.getName() + " flagged as saved!");
      this.changed = false;
   }

   public boolean hasSamePermissionNode(String permission) {
      return this.permissions.contains(permission);
   }

   public void addPermission(String permission) {
      if (!this.hasSamePermissionNode(permission)) {
         List<String> clone = new ArrayList(this.permissions);
         clone.add(permission);
         this.permissions = Collections.unmodifiableList(clone);
      }

      this.flagAsChanged();
   }

   public boolean removePermission(String permission) {
      this.flagAsChanged();
      List<String> clone = new ArrayList(this.permissions);
      boolean ret = clone.remove(permission);
      this.permissions = Collections.unmodifiableList(clone);
      return ret;
   }

   public List getPermissionList() {
      return this.permissions;
   }

   public boolean isSorted() {
      return this.sorted;
   }

   public void sortPermissions() {
      if (!this.isSorted()) {
         List<String> clone = new ArrayList(this.permissions);
         Collections.sort(clone, StringPermissionComparator.getInstance());
         this.permissions = Collections.unmodifiableList(clone);
         this.sorted = true;
      }

   }
}
