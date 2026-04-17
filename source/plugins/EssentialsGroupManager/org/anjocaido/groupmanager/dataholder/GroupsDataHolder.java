package org.anjocaido.groupmanager.dataholder;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.anjocaido.groupmanager.data.Group;

public class GroupsDataHolder {
   private WorldDataHolder dataSource;
   private Group defaultGroup = null;
   private File groupsFile;
   private boolean haveGroupsChanged = false;
   private long timeStampGroups = 0L;
   private final Map groups = Collections.synchronizedMap(new HashMap());

   protected GroupsDataHolder() {
   }

   public void setDataSource(WorldDataHolder dataSource) {
      this.dataSource = dataSource;
      synchronized(this.groups) {
         for(Group group : this.groups.values()) {
            group.setDataSource(this.dataSource);
         }

      }
   }

   public WorldDataHolder getDataSource() {
      return this.dataSource;
   }

   public Group getDefaultGroup() {
      return this.defaultGroup;
   }

   public void setDefaultGroup(Group defaultGroup) {
      this.defaultGroup = defaultGroup;
   }

   public Map getGroups() {
      return this.groups;
   }

   public void resetGroups() {
      this.groups.clear();
   }

   public File getGroupsFile() {
      return this.groupsFile;
   }

   public void setGroupsFile(File groupsFile) {
      this.groupsFile = groupsFile;
   }

   public boolean HaveGroupsChanged() {
      return this.haveGroupsChanged;
   }

   public void setGroupsChanged(boolean haveGroupsChanged) {
      this.haveGroupsChanged = haveGroupsChanged;
   }

   public long getTimeStampGroups() {
      return this.timeStampGroups;
   }

   public void setTimeStampGroups(long timeStampGroups) {
      this.timeStampGroups = timeStampGroups;
   }
}
