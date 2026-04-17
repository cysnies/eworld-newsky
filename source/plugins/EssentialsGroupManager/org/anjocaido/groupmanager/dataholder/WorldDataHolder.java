package org.anjocaido.groupmanager.dataholder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.anjocaido.groupmanager.GroupManager;
import org.anjocaido.groupmanager.data.Group;
import org.anjocaido.groupmanager.data.User;
import org.anjocaido.groupmanager.events.GMGroupEvent;
import org.anjocaido.groupmanager.events.GMSystemEvent;
import org.anjocaido.groupmanager.events.GMUserEvent;
import org.anjocaido.groupmanager.permissions.AnjoPermissionsHandler;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.reader.UnicodeReader;

public class WorldDataHolder {
   protected String name;
   protected GroupsDataHolder groups = new GroupsDataHolder();
   protected UsersDataHolder users = new UsersDataHolder();
   protected AnjoPermissionsHandler permissionsHandler;

   public WorldDataHolder(String worldName) {
      this.name = worldName;
   }

   public WorldDataHolder(String worldName, GroupsDataHolder groups, UsersDataHolder users) {
      this.name = worldName;
      this.groups = groups;
      this.users = users;
   }

   public void updateDataSource() {
      this.groups.setDataSource(this);
      this.users.setDataSource(this);
   }

   public User getUser(String userName) {
      if (this.getUsers().containsKey(userName.toLowerCase())) {
         return (User)this.getUsers().get(userName.toLowerCase());
      } else {
         User newUser = this.createUser(userName);
         return newUser;
      }
   }

   public void addUser(User theUser) {
      if (theUser.getDataSource() != this) {
         theUser = theUser.clone(this);
      }

      if (theUser != null) {
         if (theUser.getGroup() == null) {
            theUser.setGroup(this.groups.getDefaultGroup());
         }

         this.removeUser(theUser.getName());
         this.getUsers().put(theUser.getName().toLowerCase(), theUser);
         this.setUsersChanged(true);
         if (GroupManager.isLoaded()) {
            GroupManager.getGMEventHandler().callEvent(theUser, GMUserEvent.Action.USER_ADDED);
         }

      }
   }

   public boolean removeUser(String userName) {
      if (this.getUsers().containsKey(userName.toLowerCase())) {
         this.getUsers().remove(userName.toLowerCase());
         this.setUsersChanged(true);
         if (GroupManager.isLoaded()) {
            GroupManager.getGMEventHandler().callEvent(userName, GMUserEvent.Action.USER_REMOVED);
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean isUserDeclared(String userName) {
      return this.getUsers().containsKey(userName.toLowerCase());
   }

   public void setDefaultGroup(Group group) {
      if (!this.getGroups().containsKey(group.getName().toLowerCase()) || group.getDataSource() != this) {
         this.addGroup(group);
      }

      this.groups.setDefaultGroup(this.getGroup(group.getName()));
      this.setGroupsChanged(true);
      if (GroupManager.isLoaded()) {
         GroupManager.getGMEventHandler().callEvent(GMSystemEvent.Action.DEFAULT_GROUP_CHANGED);
      }

   }

   public Group getDefaultGroup() {
      return this.groups.getDefaultGroup();
   }

   public Group getGroup(String groupName) {
      return groupName.toLowerCase().startsWith("g:") ? GroupManager.getGlobalGroups().getGroup(groupName) : (Group)this.getGroups().get(groupName.toLowerCase());
   }

   public boolean groupExists(String groupName) {
      return groupName.toLowerCase().startsWith("g:") ? GroupManager.getGlobalGroups().hasGroup(groupName) : this.getGroups().containsKey(groupName.toLowerCase());
   }

   public void addGroup(Group groupToAdd) {
      if (groupToAdd.getName().toLowerCase().startsWith("g:")) {
         GroupManager.getGlobalGroups().addGroup(groupToAdd);
         GroupManager.getGMEventHandler().callEvent(groupToAdd, GMGroupEvent.Action.GROUP_ADDED);
      } else {
         if (groupToAdd.getDataSource() != this) {
            groupToAdd = groupToAdd.clone(this);
         }

         this.removeGroup(groupToAdd.getName());
         this.getGroups().put(groupToAdd.getName().toLowerCase(), groupToAdd);
         this.setGroupsChanged(true);
         if (GroupManager.isLoaded()) {
            GroupManager.getGMEventHandler().callEvent(groupToAdd, GMGroupEvent.Action.GROUP_ADDED);
         }

      }
   }

   public boolean removeGroup(String groupName) {
      if (groupName.toLowerCase().startsWith("g:")) {
         return GroupManager.getGlobalGroups().removeGroup(groupName);
      } else if (this.getDefaultGroup() != null && groupName.equalsIgnoreCase(this.getDefaultGroup().getName())) {
         return false;
      } else if (this.getGroups().containsKey(groupName.toLowerCase())) {
         this.getGroups().remove(groupName.toLowerCase());
         this.setGroupsChanged(true);
         if (GroupManager.isLoaded()) {
            GroupManager.getGMEventHandler().callEvent(groupName.toLowerCase(), GMGroupEvent.Action.GROUP_REMOVED);
         }

         return true;
      } else {
         return false;
      }
   }

   public User createUser(String userName) {
      if (this.getUsers().containsKey(userName.toLowerCase())) {
         return null;
      } else {
         User newUser = new User(this, userName);
         newUser.setGroup(this.groups.getDefaultGroup(), false);
         this.addUser(newUser);
         this.setUsersChanged(true);
         return newUser;
      }
   }

   public Group createGroup(String groupName) {
      if (groupName.toLowerCase().startsWith("g:")) {
         Group newGroup = new Group(groupName);
         return GroupManager.getGlobalGroups().newGroup(newGroup);
      } else if (this.getGroups().containsKey(groupName.toLowerCase())) {
         return null;
      } else {
         Group newGroup = new Group(this, groupName);
         this.addGroup(newGroup);
         this.setGroupsChanged(true);
         return newGroup;
      }
   }

   public Collection getGroupList() {
      synchronized(this.getGroups()) {
         return new ArrayList(this.getGroups().values());
      }
   }

   public Collection getUserList() {
      synchronized(this.getUsers()) {
         return new ArrayList(this.getUsers().values());
      }
   }

   public void reload() {
      try {
         this.reloadGroups();
         this.reloadUsers();
      } catch (Exception ex) {
         Logger.getLogger(WorldDataHolder.class.getName()).log(Level.SEVERE, (String)null, ex);
      }

   }

   public void reloadGroups() {
      GroupManager.setLoaded(false);

      try {
         WorldDataHolder ph = new WorldDataHolder(this.getName());
         loadGroups(ph, this.getGroupsFile());
         this.resetGroups();

         for(Group tempGroup : ph.getGroupList()) {
            tempGroup.clone(this);
         }

         this.setDefaultGroup(this.getGroup(ph.getDefaultGroup().getName()));
         this.removeGroupsChangedFlag();
         this.setTimeStampGroups(this.getGroupsFile().lastModified());
         ph = null;
      } catch (Exception ex) {
         Logger.getLogger(WorldDataHolder.class.getName()).log(Level.WARNING, (String)null, ex);
      }

      GroupManager.setLoaded(true);
      GroupManager.getGMEventHandler().callEvent(GMSystemEvent.Action.RELOADED);
   }

   public void reloadUsers() {
      GroupManager.setLoaded(false);

      try {
         WorldDataHolder ph = new WorldDataHolder(this.getName());

         for(Group tempGroup : this.getGroupList()) {
            tempGroup.clone(ph);
         }

         ph.setDefaultGroup(ph.getGroup(this.getDefaultGroup().getName()));
         loadUsers(ph, this.getUsersFile());
         this.resetUsers();

         for(User tempUser : ph.getUserList()) {
            tempUser.clone(this);
         }

         this.removeUsersChangedFlag();
         this.setTimeStampUsers(this.getUsersFile().lastModified());
         Object ex = null;
      } catch (Exception ex) {
         Logger.getLogger(WorldDataHolder.class.getName()).log(Level.WARNING, (String)null, ex);
      }

      GroupManager.setLoaded(true);
      GroupManager.getGMEventHandler().callEvent(GMSystemEvent.Action.RELOADED);
   }

   public void loadGroups(File groupsFile) {
      GroupManager.setLoaded(false);

      try {
         this.setGroupsFile(groupsFile);
         loadGroups(this, groupsFile);
      } catch (FileNotFoundException e) {
         e.printStackTrace();
         throw new IllegalArgumentException("The file which should contain groups does not exist!\n" + groupsFile.getPath());
      } catch (IOException e) {
         e.printStackTrace();
         throw new IllegalArgumentException("Error accessing the groups file!\n" + groupsFile.getPath());
      }

      GroupManager.setLoaded(true);
   }

   public void loadUsers(File usersFile) {
      GroupManager.setLoaded(false);

      try {
         this.setUsersFile(usersFile);
         loadUsers(this, usersFile);
      } catch (FileNotFoundException e) {
         e.printStackTrace();
         throw new IllegalArgumentException("The file which should contain users does not exist!\n" + usersFile.getPath());
      } catch (IOException e) {
         e.printStackTrace();
         throw new IllegalArgumentException("Error accessing the users file!\n" + usersFile.getPath());
      }

      GroupManager.setLoaded(true);
   }

   public static WorldDataHolder load(String worldName, File groupsFile, File usersFile) throws FileNotFoundException, IOException {
      WorldDataHolder ph = new WorldDataHolder(worldName);
      GroupManager.setLoaded(false);
      if (groupsFile != null) {
         loadGroups(ph, groupsFile);
      }

      if (usersFile != null) {
         loadUsers(ph, usersFile);
      }

      GroupManager.setLoaded(true);
      return ph;
   }

   protected static void loadGroups(WorldDataHolder ph, File groupsFile) throws FileNotFoundException, IOException {
      Yaml yamlGroups = new Yaml(new SafeConstructor());
      if (!groupsFile.exists()) {
         throw new IllegalArgumentException("The file which should contain groups does not exist!\n" + groupsFile.getPath());
      } else {
         FileInputStream groupsInputStream = new FileInputStream(groupsFile);

         Map<String, Object> groupsRootDataNode;
         try {
            groupsRootDataNode = (Map)yamlGroups.load(new UnicodeReader(groupsInputStream));
            if (groupsRootDataNode == null) {
               throw new NullPointerException();
            }
         } catch (Exception ex) {
            throw new IllegalArgumentException("The following file couldn't pass on Parser.\n" + groupsFile.getPath(), ex);
         } finally {
            groupsInputStream.close();
         }

         HashMap ex = new HashMap();
         Object var6 = null;

         try {
            var43 = (Map)groupsRootDataNode.get("groups");
         } catch (Exception ex) {
            throw new IllegalArgumentException("Your " + groupsFile.getPath() + " file is invalid. See console for details.", ex);
         }

         if (var43 == null) {
            throw new IllegalArgumentException("You have no groups in " + groupsFile.getPath() + ".");
         } else {
            Iterator<String> groupItr = var43.keySet().iterator();
            Integer groupCount = 0;

            label397:
            while(true) {
               if (groupItr.hasNext()) {
                  String groupKey;
                  try {
                     groupCount = groupCount + 1;
                     groupKey = (String)groupItr.next();
                  } catch (Exception ex) {
                     throw new IllegalArgumentException("Invalid group name for group entry (" + groupCount + ") in file: " + groupsFile.getPath(), ex);
                  }

                  Map<String, Object> thisGroupNode = null;

                  try {
                     thisGroupNode = (Map)var43.get(groupKey);
                  } catch (Exception ex) {
                     throw new IllegalArgumentException("Invalid child nodes for group '" + groupKey + "' in file: " + groupsFile.getPath(), ex);
                  }

                  Group thisGrp = ph.createGroup(groupKey);
                  if (thisGrp == null) {
                     throw new IllegalArgumentException("I think this Group was declared more than once: " + groupKey + " in file: " + groupsFile.getPath());
                  }

                  Object nodeData = null;

                  try {
                     nodeData = thisGroupNode.get("default");
                  } catch (Exception var35) {
                     throw new IllegalArgumentException("Bad format found in 'permissions' for group: " + groupKey + " in file: " + groupsFile.getPath());
                  }

                  if (nodeData != null && Boolean.parseBoolean(nodeData.toString())) {
                     if (ph.getDefaultGroup() != null) {
                        GroupManager.logger.warning("The group '" + thisGrp.getName() + "' is claiming to be default where '" + ph.getDefaultGroup().getName() + "' already was.");
                        GroupManager.logger.warning("Overriding first default request in file: " + groupsFile.getPath());
                     }

                     ph.setDefaultGroup(thisGrp);
                  }

                  nodeData = null;

                  try {
                     nodeData = thisGroupNode.get("permissions");
                  } catch (Exception var34) {
                     throw new IllegalArgumentException("Bad format found in 'permissions' for '" + groupKey + "' in file: " + groupsFile.getPath());
                  }

                  if (nodeData != null) {
                     if (nodeData instanceof List) {
                        try {
                           for(Object o : (List)nodeData) {
                              try {
                                 if (!o.toString().isEmpty()) {
                                    thisGrp.addPermission(o.toString());
                                 }
                              } catch (NullPointerException var33) {
                              }
                           }
                        } catch (Exception ex) {
                           throw new IllegalArgumentException("Invalid formatting found in 'permissions' section for group: " + thisGrp.getName() + " in file: " + groupsFile.getPath(), ex);
                        }
                     } else {
                        if (!(nodeData instanceof String)) {
                           throw new IllegalArgumentException("Unknown type of 'permissions' node(Should be String or List<String>) for group:  " + thisGrp.getName() + " in file: " + groupsFile.getPath());
                        }

                        if (!nodeData.toString().isEmpty()) {
                           thisGrp.addPermission((String)nodeData);
                        }
                     }

                     thisGrp.sortPermissions();
                  }

                  nodeData = null;

                  try {
                     nodeData = thisGroupNode.get("info");
                  } catch (Exception var32) {
                     throw new IllegalArgumentException("Bad format found in 'info' section for group: " + groupKey + " in file: " + groupsFile.getPath());
                  }

                  if (nodeData == null) {
                     GroupManager.logger.warning("The group '" + thisGrp.getName() + "' has no 'info' section!");
                     GroupManager.logger.warning("Using default values: " + groupsFile.getPath());
                  } else {
                     if (!(nodeData instanceof Map)) {
                        throw new IllegalArgumentException("Unknown entry found in 'info' section for group: " + thisGrp.getName() + " in file: " + groupsFile.getPath());
                     }

                     try {
                        if (nodeData != null) {
                           thisGrp.setVariables((Map)nodeData);
                        }
                     } catch (Exception ex) {
                        throw new IllegalArgumentException("Invalid formatting found in 'info' section for group: " + thisGrp.getName() + " in file: " + groupsFile.getPath(), ex);
                     }
                  }

                  nodeData = null;

                  try {
                     nodeData = thisGroupNode.get("inheritance");
                  } catch (Exception var30) {
                     throw new IllegalArgumentException("Bad format found in 'inheritance' section for group: " + groupKey + " in file: " + groupsFile.getPath());
                  }

                  if (nodeData != null && !(nodeData instanceof List)) {
                     throw new IllegalArgumentException("Unknown entry found in 'inheritance' section for group: " + thisGrp.getName() + " in file: " + groupsFile.getPath());
                  }

                  if (nodeData == null || !(nodeData instanceof List)) {
                     continue;
                  }

                  try {
                     Iterator i$ = ((List)nodeData).iterator();

                     while(true) {
                        if (!i$.hasNext()) {
                           continue label397;
                        }

                        String grp = (String)i$.next();
                        if (ex.get(groupKey) == null) {
                           ex.put(groupKey, new ArrayList());
                        }

                        ((List)ex.get(groupKey)).add(grp);
                     }
                  } catch (Exception ex) {
                     throw new IllegalArgumentException("Invalid formatting found in 'inheritance' section for group: " + thisGrp.getName() + " in file: " + groupsFile.getPath(), ex);
                  }
               }

               if (ph.getDefaultGroup() == null) {
                  throw new IllegalArgumentException("There was no Default Group declared in file: " + groupsFile.getPath());
               }

               for(String group : ex.keySet()) {
                  List<String> inheritedList = (List)ex.get(group);
                  Group thisGroup = ph.getGroup(group);
                  if (thisGroup != null) {
                     for(String inheritedKey : inheritedList) {
                        if (inheritedKey != null) {
                           Group inheritedGroup = ph.getGroup(inheritedKey);
                           if (inheritedGroup != null) {
                              thisGroup.addInherits(inheritedGroup);
                           } else {
                              GroupManager.logger.warning("Inherited group '" + inheritedKey + "' not found for group " + thisGroup.getName() + ". Ignoring entry in file: " + groupsFile.getPath());
                           }
                        }
                     }
                  }
               }

               ph.removeGroupsChangedFlag();
               ph.setGroupsFile(groupsFile);
               ph.setTimeStampGroups(groupsFile.lastModified());
               return;
            }
         }
      }
   }

   protected static void loadUsers(WorldDataHolder ph, File usersFile) throws FileNotFoundException, IOException {
      Yaml yamlUsers = new Yaml(new SafeConstructor());
      if (!usersFile.exists()) {
         throw new IllegalArgumentException("The file which should contain users does not exist!\n" + usersFile.getPath());
      } else {
         FileInputStream usersInputStream = new FileInputStream(usersFile);

         Map<String, Object> usersRootDataNode;
         try {
            usersRootDataNode = (Map)yamlUsers.load(new UnicodeReader(usersInputStream));
            if (usersRootDataNode == null) {
               throw new NullPointerException();
            }
         } catch (Exception ex) {
            throw new IllegalArgumentException("The following file couldn't pass on Parser.\n" + usersFile.getPath(), ex);
         } finally {
            usersInputStream.close();
         }

         Object ex = null;

         try {
            allUsersNode = (Map)usersRootDataNode.get("users");
         } catch (Exception ex) {
            throw new IllegalArgumentException("Your " + usersFile.getPath() + " file is invalid. See console for details.", ex);
         }

         if (allUsersNode != null) {
            Iterator<String> usersItr = allUsersNode.keySet().iterator();
            Integer userCount = 0;

            while(usersItr.hasNext()) {
               String usersKey;
               Object node;
               try {
                  userCount = userCount + 1;
                  node = usersItr.next();
                  if (node instanceof Integer) {
                     usersKey = Integer.toString((Integer)node);
                  } else {
                     usersKey = node.toString();
                  }
               } catch (Exception ex) {
                  throw new IllegalArgumentException("Invalid node type for user entry (" + userCount + ") in file: " + usersFile.getPath(), ex);
               }

               Map<String, Object> thisUserNode = null;

               try {
                  thisUserNode = (Map)allUsersNode.get(node);
               } catch (Exception var30) {
                  throw new IllegalArgumentException("Bad format found for user: " + usersKey + " in file: " + usersFile.getPath());
               }

               User thisUser = ph.createUser(usersKey);
               if (thisUser == null) {
                  throw new IllegalArgumentException("I think this user was declared more than once: " + usersKey + " in file: " + usersFile.getPath());
               }

               Object nodeData = null;

               try {
                  nodeData = thisUserNode.get("permissions");
               } catch (Exception var29) {
                  throw new IllegalArgumentException("Bad format found in 'permissions' for user: " + usersKey + " in file: " + usersFile.getPath());
               }

               if (nodeData != null) {
                  try {
                     if (nodeData instanceof List) {
                        for(Object o : (List)nodeData) {
                           if (!o.toString().isEmpty()) {
                              thisUser.addPermission(o.toString());
                           }
                        }
                     } else if (nodeData instanceof String && !nodeData.toString().isEmpty()) {
                        thisUser.addPermission(nodeData.toString());
                     }
                  } catch (NullPointerException var33) {
                  }

                  thisUser.sortPermissions();
               }

               nodeData = null;

               try {
                  nodeData = thisUserNode.get("subgroups");
               } catch (Exception var28) {
                  throw new IllegalArgumentException("Bad format found in 'subgroups' for user: " + usersKey + " in file: " + usersFile.getPath());
               }

               if (nodeData != null) {
                  if (nodeData instanceof List) {
                     for(Object o : (List)nodeData) {
                        if (o == null) {
                           GroupManager.logger.warning("Invalid Subgroup data for user: " + thisUser.getName() + ". Ignoring entry in file: " + usersFile.getPath());
                        } else {
                           Group subGrp = ph.getGroup(o.toString());
                           if (subGrp != null) {
                              thisUser.addSubGroup(subGrp);
                           } else {
                              GroupManager.logger.warning("Subgroup '" + o.toString() + "' not found for user: " + thisUser.getName() + ". Ignoring entry in file: " + usersFile.getPath());
                           }
                        }
                     }
                  } else if (nodeData instanceof String) {
                     Group subGrp = ph.getGroup(nodeData.toString());
                     if (subGrp != null) {
                        thisUser.addSubGroup(subGrp);
                     } else {
                        GroupManager.logger.warning("Subgroup '" + nodeData.toString() + "' not found for user: " + thisUser.getName() + ". Ignoring entry in file: " + usersFile.getPath());
                     }
                  }
               }

               nodeData = null;

               try {
                  nodeData = thisUserNode.get("info");
               } catch (Exception var27) {
                  throw new IllegalArgumentException("Bad format found in 'info' section for user: " + usersKey + " in file: " + usersFile.getPath());
               }

               if (nodeData != null) {
                  if (!(nodeData instanceof Map)) {
                     throw new IllegalArgumentException("Unknown entry found in 'info' section for user: " + thisUser.getName() + " in file: " + usersFile.getPath());
                  }

                  thisUser.setVariables((Map)nodeData);
               }

               nodeData = null;

               try {
                  nodeData = thisUserNode.get("group");
               } catch (Exception var26) {
                  throw new IllegalArgumentException("Bad format found in 'group' section for user: " + usersKey + " in file: " + usersFile.getPath());
               }

               if (nodeData != null) {
                  Group hisGroup = ph.getGroup(nodeData.toString());
                  if (hisGroup == null) {
                     GroupManager.logger.warning("There is no group " + thisUserNode.get("group").toString() + ", as stated for player " + thisUser.getName() + ": Set to '" + ph.getDefaultGroup().getName() + "' for file: " + usersFile.getPath());
                     hisGroup = ph.getDefaultGroup();
                  }

                  thisUser.setGroup(hisGroup);
               } else {
                  thisUser.setGroup(ph.getDefaultGroup());
               }
            }
         }

         ph.removeUsersChangedFlag();
         ph.setUsersFile(usersFile);
         ph.setTimeStampUsers(usersFile.lastModified());
      }
   }

   public static void writeGroups(WorldDataHolder ph, File groupsFile) {
      Map<String, Object> root = new HashMap();
      Map<String, Object> groupsMap = new HashMap();
      root.put("groups", groupsMap);
      synchronized(ph.getGroups()) {
         for(String groupKey : ph.getGroups().keySet()) {
            Group group = (Group)ph.getGroups().get(groupKey);
            Map<String, Object> aGroupMap = new HashMap();
            groupsMap.put(group.getName(), aGroupMap);
            if (ph.getDefaultGroup() == null) {
               GroupManager.logger.severe("There is no default group for world: " + ph.getName());
            }

            aGroupMap.put("default", group.equals(ph.getDefaultGroup()));
            Map<String, Object> infoMap = new HashMap();
            aGroupMap.put("info", infoMap);

            for(String infoKey : group.getVariables().getVarKeyList()) {
               infoMap.put(infoKey, group.getVariables().getVarObject(infoKey));
            }

            aGroupMap.put("inheritance", group.getInherits());
            aGroupMap.put("permissions", group.getPermissionList());
         }
      }

      if (!root.isEmpty()) {
         DumperOptions opt = new DumperOptions();
         opt.setDefaultFlowStyle(FlowStyle.BLOCK);
         Yaml yaml = new Yaml(opt);

         try {
            OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(groupsFile), "UTF-8");
            String newLine = System.getProperty("line.separator");
            out.write("# Group inheritance" + newLine);
            out.write("#" + newLine);
            out.write("# Any inherited groups prefixed with a g: are global groups" + newLine);
            out.write("# and are inherited from the GlobalGroups.yml." + newLine);
            out.write("#" + newLine);
            out.write("# Groups without the g: prefix are groups local to this world" + newLine);
            out.write("# and are defined in the this groups.yml file." + newLine);
            out.write("#" + newLine);
            out.write("# Local group inheritances define your promotion tree when using 'manpromote/mandemote'" + newLine);
            out.write(newLine);
            yaml.dump(root, out);
            out.close();
         } catch (UnsupportedEncodingException var15) {
         } catch (FileNotFoundException var16) {
         } catch (IOException var17) {
         }
      }

      ph.setGroupsFile(groupsFile);
      ph.setTimeStampGroups(groupsFile.lastModified());
      ph.removeGroupsChangedFlag();
      if (GroupManager.isLoaded()) {
         GroupManager.getGMEventHandler().callEvent(GMSystemEvent.Action.SAVED);
      }

   }

   public static void writeUsers(WorldDataHolder ph, File usersFile) {
      Map<String, Object> root = new HashMap();
      LinkedHashMap<String, Object> usersMap = new LinkedHashMap();
      root.put("users", usersMap);
      synchronized(ph.getUsers()) {
         for(String userKey : new TreeSet(ph.getUsers().keySet())) {
            User user = (User)ph.getUsers().get(userKey);
            if (user.getGroup() != null && !user.getGroup().equals(ph.getDefaultGroup()) || !user.getPermissionList().isEmpty() || !user.getVariables().isEmpty() || !user.isSubGroupsEmpty()) {
               LinkedHashMap<String, Object> aUserMap = new LinkedHashMap();
               usersMap.put(user.getName(), aUserMap);
               if (user.getGroup() == null) {
                  aUserMap.put("group", ph.getDefaultGroup().getName());
               } else {
                  aUserMap.put("group", user.getGroup().getName());
               }

               aUserMap.put("subgroups", user.subGroupListStringCopy());
               aUserMap.put("permissions", user.getPermissionList());
               if (user.getVariables().getSize() > 0) {
                  Map<String, Object> infoMap = new HashMap();
                  aUserMap.put("info", infoMap);

                  for(String infoKey : user.getVariables().getVarKeyList()) {
                     infoMap.put(infoKey, user.getVariables().getVarObject(infoKey));
                  }
               }
            }
         }
      }

      if (!root.isEmpty()) {
         DumperOptions opt = new DumperOptions();
         opt.setDefaultFlowStyle(FlowStyle.BLOCK);
         Yaml yaml = new Yaml(opt);

         try {
            OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(usersFile), "UTF-8");
            yaml.dump(root, out);
            out.close();
         } catch (UnsupportedEncodingException var15) {
         } catch (FileNotFoundException var16) {
         } catch (IOException var17) {
         }
      }

      ph.setUsersFile(usersFile);
      ph.setTimeStampUsers(usersFile.lastModified());
      ph.removeUsersChangedFlag();
      if (GroupManager.isLoaded()) {
         GroupManager.getGMEventHandler().callEvent(GMSystemEvent.Action.SAVED);
      }

   }

   /** @deprecated */
   @Deprecated
   public static void reloadOldPlugins(Server server) {
      PluginManager pm = server.getPluginManager();
      Plugin[] plugins = pm.getPlugins();

      for(int i = 0; i < plugins.length; ++i) {
         try {
            plugins[i].getClass().getMethod("setupPermissions").invoke(plugins[i]);
         } catch (Exception var5) {
         }
      }

   }

   public AnjoPermissionsHandler getPermissionsHandler() {
      if (this.permissionsHandler == null) {
         this.permissionsHandler = new AnjoPermissionsHandler(this);
      }

      return this.permissionsHandler;
   }

   public void setUsersChanged(boolean haveUsersChanged) {
      this.users.setUsersChanged(haveUsersChanged);
   }

   public boolean haveUsersChanged() {
      if (this.users.HaveUsersChanged()) {
         return true;
      } else {
         synchronized(this.users.getUsers()) {
            for(User u : this.users.getUsers().values()) {
               if (u.isChanged()) {
                  return true;
               }
            }

            return false;
         }
      }
   }

   public void setGroupsChanged(boolean setGroupsChanged) {
      this.groups.setGroupsChanged(setGroupsChanged);
   }

   public boolean haveGroupsChanged() {
      if (this.groups.HaveGroupsChanged()) {
         return true;
      } else {
         synchronized(this.groups.getGroups()) {
            for(Group g : this.groups.getGroups().values()) {
               if (g.isChanged()) {
                  return true;
               }
            }

            return false;
         }
      }
   }

   public void removeUsersChangedFlag() {
      this.setUsersChanged(false);
      synchronized(this.getUsers()) {
         for(User u : this.getUsers().values()) {
            u.flagAsSaved();
         }

      }
   }

   public void removeGroupsChangedFlag() {
      this.setGroupsChanged(false);
      synchronized(this.getGroups()) {
         for(Group g : this.getGroups().values()) {
            g.flagAsSaved();
         }

      }
   }

   public File getUsersFile() {
      return this.users.getUsersFile();
   }

   public void setUsersFile(File file) {
      this.users.setUsersFile(file);
   }

   public File getGroupsFile() {
      return this.groups.getGroupsFile();
   }

   public void setGroupsFile(File file) {
      this.groups.setGroupsFile(file);
   }

   public String getName() {
      return this.name;
   }

   public void resetGroups() {
      this.groups.resetGroups();
   }

   public void resetUsers() {
      this.users.resetUsers();
   }

   public Map getGroups() {
      return this.groups.getGroups();
   }

   public Map getUsers() {
      return this.users.getUsers();
   }

   public GroupsDataHolder getGroupsObject() {
      return this.groups;
   }

   public void setGroupsObject(GroupsDataHolder groupsDataHolder) {
      this.groups = groupsDataHolder;
   }

   public UsersDataHolder getUsersObject() {
      return this.users;
   }

   public void setUsersObject(UsersDataHolder usersDataHolder) {
      this.users = usersDataHolder;
   }

   public long getTimeStampGroups() {
      return this.groups.getTimeStampGroups();
   }

   public long getTimeStampUsers() {
      return this.users.getTimeStampUsers();
   }

   protected void setTimeStampGroups(long timeStampGroups) {
      this.groups.setTimeStampGroups(timeStampGroups);
   }

   protected void setTimeStampUsers(long timeStampUsers) {
      this.users.setTimeStampUsers(timeStampUsers);
   }

   public void setTimeStamps() {
      if (this.getGroupsFile() != null) {
         this.setTimeStampGroups(this.getGroupsFile().lastModified());
      }

      if (this.getUsersFile() != null) {
         this.setTimeStampUsers(this.getUsersFile().lastModified());
      }

   }
}
