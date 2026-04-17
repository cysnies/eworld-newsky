package org.anjocaido.groupmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.anjocaido.groupmanager.data.Group;
import org.anjocaido.groupmanager.events.GMGroupEvent;
import org.anjocaido.groupmanager.utils.PermissionCheckResult;
import org.anjocaido.groupmanager.utils.Tasks;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.reader.UnicodeReader;

public class GlobalGroups {
   private GroupManager plugin;
   private final Map groups = Collections.synchronizedMap(new HashMap());
   protected long timeStampGroups = 0L;
   protected boolean haveGroupsChanged = false;
   protected File GlobalGroupsFile = null;

   public GlobalGroups(GroupManager plugin) {
      this.plugin = plugin;
      this.load();
   }

   public boolean haveGroupsChanged() {
      if (this.haveGroupsChanged) {
         return true;
      } else {
         synchronized(this.groups) {
            for(Group g : this.groups.values()) {
               if (g.isChanged()) {
                  return true;
               }
            }

            return false;
         }
      }
   }

   public long getTimeStampGroups() {
      return this.timeStampGroups;
   }

   protected void setTimeStampGroups(long timeStampGroups) {
      this.timeStampGroups = timeStampGroups;
   }

   public void setGroupsChanged(boolean haveGroupsChanged) {
      this.haveGroupsChanged = haveGroupsChanged;
   }

   public void load() {
      Yaml GGroupYAML = new Yaml(new SafeConstructor());
      GroupManager.setLoaded(false);
      if (this.GlobalGroupsFile == null) {
         this.GlobalGroupsFile = new File(this.plugin.getDataFolder(), "globalgroups.yml");
      }

      if (!this.GlobalGroupsFile.exists()) {
         try {
            Tasks.copy(this.plugin.getResourceAsStream("globalgroups.yml"), this.GlobalGroupsFile);
         } catch (IOException ex) {
            GroupManager.logger.log(Level.SEVERE, (String)null, ex);
         }
      }

      Map<String, Object> GGroups;
      try {
         FileInputStream groupsInputStream = new FileInputStream(this.GlobalGroupsFile);
         GGroups = (Map)GGroupYAML.load(new UnicodeReader(groupsInputStream));
         groupsInputStream.close();
      } catch (Exception ex) {
         throw new IllegalArgumentException("The following file couldn't pass on Parser.\n" + this.GlobalGroupsFile.getPath(), ex);
      }

      this.resetGlobalGroups();
      if (!GGroups.keySet().isEmpty()) {
         new HashMap();

         Map groupsInputStream;
         try {
            groupsInputStream = (Map)GGroups.get("groups");
         } catch (Exception ex) {
            throw new IllegalArgumentException("Your " + this.GlobalGroupsFile.getPath() + " file is invalid. See console for details.", ex);
         }

         if (groupsInputStream != null) {
            Iterator<String> groupItr = groupsInputStream.keySet().iterator();

            Group newGroup;
            for(Integer groupCount = 0; groupItr.hasNext(); this.addGroup(newGroup)) {
               String groupName;
               try {
                  groupCount = groupCount + 1;
                  groupName = (String)groupItr.next();
               } catch (Exception ex) {
                  throw new IllegalArgumentException("Invalid group name for GlobalGroup entry (" + groupCount + ") in file: " + this.GlobalGroupsFile.getPath(), ex);
               }

               newGroup = new Group(groupName.toLowerCase());

               Object element;
               try {
                  element = ((Map)groupsInputStream.get(groupName)).get("permissions");
               } catch (Exception ex) {
                  throw new IllegalArgumentException("The GlobalGroup ' " + groupName + "' is formatted incorrectly: ", ex);
               }

               if (element != null) {
                  if (element instanceof List) {
                     try {
                        for(String node : (List)element) {
                           if (node != null && !node.isEmpty()) {
                              newGroup.addPermission(node);
                           }
                        }
                     } catch (ClassCastException ex) {
                        throw new IllegalArgumentException("Invalid permission node for global group:  " + groupName, ex);
                     }
                  } else {
                     if (!(element instanceof String)) {
                        throw new IllegalArgumentException("Unknown type of permission node for global group:  " + groupName);
                     }

                     if (element != null && !((String)element).isEmpty()) {
                        newGroup.addPermission((String)element);
                     }
                  }
               }
            }
         }

         this.removeGroupsChangedFlag();
      }

      this.setTimeStampGroups(this.GlobalGroupsFile.lastModified());
      GroupManager.setLoaded(true);
   }

   public void writeGroups(boolean overwrite) {
      if (this.haveGroupsChanged()) {
         if (!overwrite && (overwrite || this.getTimeStampGroups() < this.GlobalGroupsFile.lastModified())) {
            GroupManager.logger.log(Level.WARNING, "Newer GlobalGroups file found, but we have local changes!");
            throw new IllegalStateException("Unable to save unless you issue a '/mansave force'");
         }

         Map<String, Object> root = new HashMap();
         Map<String, Object> groupsMap = new HashMap();
         root.put("groups", groupsMap);
         synchronized(this.groups) {
            for(String groupKey : this.groups.keySet()) {
               Group group = (Group)this.groups.get(groupKey);
               Map<String, Object> aGroupMap = new HashMap();
               groupsMap.put(group.getName(), aGroupMap);
               aGroupMap.put("permissions", group.getPermissionList());
            }
         }

         if (!root.isEmpty()) {
            DumperOptions opt = new DumperOptions();
            opt.setDefaultFlowStyle(FlowStyle.BLOCK);
            Yaml yaml = new Yaml(opt);

            try {
               yaml.dump(root, new OutputStreamWriter(new FileOutputStream(this.GlobalGroupsFile), "UTF-8"));
            } catch (UnsupportedEncodingException var10) {
            } catch (FileNotFoundException var11) {
            }
         }

         this.setTimeStampGroups(this.GlobalGroupsFile.lastModified());
         this.removeGroupsChangedFlag();
      } else if (this.getTimeStampGroups() < this.GlobalGroupsFile.lastModified()) {
         System.out.print("Newer GlobalGroups file found (Loading changes)!");
         this.backupFile();
         this.load();
      }

   }

   private void backupFile() {
      File backupFile = new File(this.plugin.getBackupFolder(), "bkp_ggroups_" + Tasks.getDateString() + ".yml");

      try {
         Tasks.copy(this.GlobalGroupsFile, backupFile);
      } catch (IOException ex) {
         GroupManager.logger.log(Level.SEVERE, (String)null, ex);
      }

   }

   public void addGroup(Group groupToAdd) {
      if (this.hasGroup(groupToAdd.getName())) {
         groupToAdd = groupToAdd.clone();
         this.removeGroup(groupToAdd.getName());
      }

      this.newGroup(groupToAdd);
      this.haveGroupsChanged = true;
      if (GroupManager.isLoaded()) {
         GroupManager.getGMEventHandler().callEvent(groupToAdd, GMGroupEvent.Action.GROUP_ADDED);
      }

   }

   public Group newGroup(Group newGroup) {
      if (!this.groups.containsKey(newGroup.getName().toLowerCase())) {
         this.groups.put(newGroup.getName().toLowerCase(), newGroup);
         this.setGroupsChanged(true);
         return newGroup;
      } else {
         return null;
      }
   }

   public boolean removeGroup(String groupName) {
      if (this.groups.containsKey(groupName.toLowerCase())) {
         this.groups.remove(groupName.toLowerCase());
         this.setGroupsChanged(true);
         if (GroupManager.isLoaded()) {
            GroupManager.getGMEventHandler().callEvent(groupName.toLowerCase(), GMGroupEvent.Action.GROUP_REMOVED);
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean hasGroup(String groupName) {
      return this.groups.containsKey(groupName.toLowerCase());
   }

   public boolean hasPermission(String groupName, String permissionNode) {
      return !this.hasGroup(groupName) ? false : ((Group)this.groups.get(groupName.toLowerCase())).hasSamePermissionNode(permissionNode);
   }

   public PermissionCheckResult checkPermission(String groupName, String permissionNode) {
      PermissionCheckResult result = new PermissionCheckResult();
      result.askedPermission = permissionNode;
      result.resultType = PermissionCheckResult.Type.NOTFOUND;
      if (!this.hasGroup(groupName)) {
         return result;
      } else {
         Group tempGroup = (Group)this.groups.get(groupName.toLowerCase());
         if (tempGroup.hasSamePermissionNode(permissionNode)) {
            result.resultType = PermissionCheckResult.Type.FOUND;
         }

         if (tempGroup.hasSamePermissionNode("-" + permissionNode)) {
            result.resultType = PermissionCheckResult.Type.NEGATION;
         }

         if (tempGroup.hasSamePermissionNode("+" + permissionNode)) {
            result.resultType = PermissionCheckResult.Type.EXCEPTION;
         }

         return result;
      }
   }

   public List getGroupsPermissions(String groupName) {
      return !this.hasGroup(groupName) ? null : ((Group)this.groups.get(groupName.toLowerCase())).getPermissionList();
   }

   public void resetGlobalGroups() {
      this.groups.clear();
   }

   public Group[] getGroupList() {
      synchronized(this.groups) {
         return (Group[])this.groups.values().toArray(new Group[0]);
      }
   }

   public Group getGroup(String groupName) {
      return !this.hasGroup(groupName) ? null : (Group)this.groups.get(groupName.toLowerCase());
   }

   public File getGlobalGroupsFile() {
      return this.GlobalGroupsFile;
   }

   public void removeGroupsChangedFlag() {
      this.setGroupsChanged(false);
      synchronized(this.groups) {
         for(Group g : this.groups.values()) {
            g.flagAsSaved();
         }

      }
   }
}
