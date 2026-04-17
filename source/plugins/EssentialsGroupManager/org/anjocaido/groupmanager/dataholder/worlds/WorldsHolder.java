package org.anjocaido.groupmanager.dataholder.worlds;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.anjocaido.groupmanager.GroupManager;
import org.anjocaido.groupmanager.dataholder.OverloadedWorldHolder;
import org.anjocaido.groupmanager.dataholder.WorldDataHolder;
import org.anjocaido.groupmanager.permissions.AnjoPermissionsHandler;
import org.anjocaido.groupmanager.utils.Tasks;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class WorldsHolder {
   private Map worldsData = new HashMap();
   private Map mirrorsGroup = new HashMap();
   private Map mirrorsUser = new HashMap();
   private String serverDefaultWorldName;
   private GroupManager plugin;
   private File worldsFolder;

   public WorldsHolder(GroupManager plugin) {
      this.plugin = plugin;
      this.resetWorldsHolder();
   }

   public Map getMirrorsGroup() {
      return this.mirrorsGroup;
   }

   public Map getMirrorsUser() {
      return this.mirrorsUser;
   }

   public boolean isWorldKnown(String name) {
      return this.worldsData.containsKey(name.toLowerCase());
   }

   public void resetWorldsHolder() {
      this.worldsData = new HashMap();
      this.mirrorsGroup = new HashMap();
      this.mirrorsUser = new HashMap();
      this.verifyFirstRun();
      this.initialLoad();
      if (this.serverDefaultWorldName == null) {
         throw new IllegalStateException("There is no default group! OMG!");
      }
   }

   private void initialLoad() {
      this.initialWorldLoading();
      this.mirrorSetUp();
      this.loadAllSearchedWorlds();
   }

   private void initialWorldLoading() {
      this.loadWorld(this.serverDefaultWorldName);
   }

   private void loadAllSearchedWorlds() {
      for(World world : this.plugin.getServer().getWorlds()) {
         GroupManager.logger.log(Level.FINE, "Checking data for " + world.getName() + ".");
         if (!this.worldsData.containsKey(world.getName().toLowerCase()) && (!this.mirrorsGroup.containsKey(world.getName().toLowerCase()) || !this.mirrorsUser.containsKey(world.getName().toLowerCase()))) {
            if (this.worldsData.containsKey("all_unnamed_worlds")) {
               String usersMirror = (String)this.mirrorsUser.get("all_unnamed_worlds");
               String groupsMirror = (String)this.mirrorsGroup.get("all_unnamed_worlds");
               if (usersMirror != null) {
                  this.mirrorsUser.put(world.getName().toLowerCase(), usersMirror);
               }

               if (groupsMirror != null) {
                  this.mirrorsGroup.put(world.getName().toLowerCase(), groupsMirror);
               }
            }

            GroupManager.logger.log(Level.FINE, "Creating folders for " + world.getName() + ".");
            this.setupWorldFolder(world.getName());
         }
      }

      for(File folder : this.worldsFolder.listFiles()) {
         if (folder.isDirectory() && !folder.getName().startsWith(".")) {
            GroupManager.logger.info("World Found: " + folder.getName());
            if (!this.worldsData.containsKey(folder.getName().toLowerCase()) && (!this.mirrorsGroup.containsKey(folder.getName().toLowerCase()) || !this.mirrorsUser.containsKey(folder.getName().toLowerCase()))) {
               this.setupWorldFolder(folder.getName());
               this.loadWorld(folder.getName().toLowerCase());
            }
         }
      }

   }

   public void mirrorSetUp() {
      this.mirrorsGroup.clear();
      this.mirrorsUser.clear();
      Map<String, Object> mirrorsMap = this.plugin.getGMConfig().getMirrorsMap();
      HashSet<String> mirroredWorlds = new HashSet();
      if (mirrorsMap != null) {
         for(String source : mirrorsMap.keySet()) {
            this.setupWorldFolder(source);
            if (!this.worldsData.containsKey(source.toLowerCase())) {
               this.loadWorld(source);
            }

            if (mirrorsMap.get(source) instanceof ArrayList) {
               for(Object o : (ArrayList)mirrorsMap.get(source)) {
                  String world = o.toString().toLowerCase();
                  if (world != this.serverDefaultWorldName) {
                     try {
                        this.mirrorsGroup.remove(world);
                        this.mirrorsUser.remove(world);
                     } catch (Exception var14) {
                     }

                     this.mirrorsGroup.put(world, this.getWorldData(source).getName());
                     this.mirrorsUser.put(world, this.getWorldData(source).getName());
                     mirroredWorlds.add(o.toString());
                  } else {
                     GroupManager.logger.log(Level.WARNING, "Mirroring error with " + o.toString() + ". Recursive loop detected!");
                  }
               }
            } else if (mirrorsMap.get(source) instanceof Map) {
               Map subSection = (Map)mirrorsMap.get(source);

               for(Object key : subSection.keySet()) {
                  if (((String)key).toLowerCase() != this.serverDefaultWorldName) {
                     if (!(subSection.get(key) instanceof ArrayList)) {
                        throw new IllegalStateException("Unknown mirroring format for " + (String)key);
                     }

                     for(Object o : (ArrayList)subSection.get(key)) {
                        String type = o.toString().toLowerCase();

                        try {
                           if (type.equals("groups")) {
                              this.mirrorsGroup.remove(((String)key).toLowerCase());
                           }

                           if (type.equals("users")) {
                              this.mirrorsUser.remove(((String)key).toLowerCase());
                           }
                        } catch (Exception var13) {
                        }

                        if (type.equals("groups")) {
                           this.mirrorsGroup.put(((String)key).toLowerCase(), this.getWorldData(source).getName());
                           GroupManager.logger.log(Level.FINE, "Adding groups mirror for " + key + ".");
                        }

                        if (type.equals("users")) {
                           this.mirrorsUser.put(((String)key).toLowerCase(), this.getWorldData(source).getName());
                           GroupManager.logger.log(Level.FINE, "Adding users mirror for " + key + ".");
                        }
                     }

                     mirroredWorlds.add((String)key);
                  } else {
                     GroupManager.logger.log(Level.WARNING, "Mirroring error with " + (String)key + ". Recursive loop detected!");
                  }
               }
            }
         }

         for(String world : mirroredWorlds) {
            if (!this.worldsData.containsKey(world.toLowerCase())) {
               GroupManager.logger.log(Level.FINE, "No data for " + world + ".");
               this.setupWorldFolder(world);
               this.loadWorld(world, true);
            }
         }
      }

   }

   public void reloadAll() {
      GroupManager.getGlobalGroups().load();
      ArrayList<WorldDataHolder> alreadyDone = new ArrayList();

      for(WorldDataHolder w : this.worldsData.values()) {
         if (!alreadyDone.contains(w)) {
            if (!this.mirrorsGroup.containsKey(w.getName().toLowerCase())) {
               w.reloadGroups();
            }

            if (!this.mirrorsUser.containsKey(w.getName().toLowerCase())) {
               w.reloadUsers();
            }

            alreadyDone.add(w);
         }
      }

   }

   public void reloadWorld(String worldName) {
      if (!this.mirrorsGroup.containsKey(worldName.toLowerCase())) {
         this.getWorldData(worldName).reloadGroups();
      }

      if (!this.mirrorsUser.containsKey(worldName.toLowerCase())) {
         this.getWorldData(worldName).reloadUsers();
      }

   }

   public void saveChanges() {
      this.saveChanges(true);
   }

   public boolean saveChanges(boolean overwrite) {
      boolean changed = false;
      ArrayList<WorldDataHolder> alreadyDone = new ArrayList();
      Tasks.removeOldFiles(this.plugin, this.plugin.getBackupFolder());
      if (GroupManager.getGlobalGroups().haveGroupsChanged()) {
         GroupManager.getGlobalGroups().writeGroups(overwrite);
      } else if (GroupManager.getGlobalGroups().getTimeStampGroups() < GroupManager.getGlobalGroups().getGlobalGroupsFile().lastModified()) {
         System.out.print("Newer GlobalGroups file found (Loading changes)!");
         GroupManager.getGlobalGroups().load();
      }

      for(OverloadedWorldHolder w : this.worldsData.values()) {
         if (!alreadyDone.contains(w)) {
            if (w != null) {
               if (!this.mirrorsGroup.containsKey(w.getName().toLowerCase())) {
                  if (w.haveGroupsChanged()) {
                     if (!overwrite && (overwrite || w.getTimeStampGroups() < w.getGroupsFile().lastModified())) {
                        GroupManager.logger.log(Level.WARNING, "Newer Groups file found for " + w.getName() + ", but we have local changes!");
                        throw new IllegalStateException("Unable to save unless you issue a '/mansave force'");
                     }

                     this.backupFile(w, true);
                     WorldDataHolder.writeGroups(w, w.getGroupsFile());
                     changed = true;
                  } else if (w.getTimeStampGroups() < w.getGroupsFile().lastModified()) {
                     System.out.print("Newer Groups file found (Loading changes)!");
                     this.backupFile(w, true);
                     w.reloadGroups();
                     changed = true;
                  }
               }

               if (!this.mirrorsUser.containsKey(w.getName().toLowerCase())) {
                  if (w.haveUsersChanged()) {
                     if (!overwrite && (overwrite || w.getTimeStampUsers() < w.getUsersFile().lastModified())) {
                        GroupManager.logger.log(Level.WARNING, "Newer Users file found for " + w.getName() + ", but we have local changes!");
                        throw new IllegalStateException("Unable to save unless you issue a '/mansave force'");
                     }

                     this.backupFile(w, false);
                     WorldDataHolder.writeUsers(w, w.getUsersFile());
                     changed = true;
                  } else if (w.getTimeStampUsers() < w.getUsersFile().lastModified()) {
                     System.out.print("Newer Users file found (Loading changes)!");
                     this.backupFile(w, false);
                     w.reloadUsers();
                     changed = true;
                  }
               }

               alreadyDone.add(w);
            } else {
               GroupManager.logger.severe("WHAT HAPPENED?");
            }
         }
      }

      return changed;
   }

   private void backupFile(OverloadedWorldHolder w, Boolean groups) {
      File backupFile = new File(this.plugin.getBackupFolder(), "bkp_" + w.getName() + (groups ? "_g_" : "_u_") + Tasks.getDateString() + ".yml");

      try {
         Tasks.copy(groups ? w.getGroupsFile() : w.getUsersFile(), backupFile);
      } catch (IOException ex) {
         GroupManager.logger.log(Level.SEVERE, (String)null, ex);
      }

   }

   public OverloadedWorldHolder getWorldData(String worldName) {
      String worldNameLowered = worldName.toLowerCase();
      if (this.worldsData.containsKey(worldNameLowered)) {
         return this.getUpdatedWorldData(worldNameLowered);
      } else if (this.worldsData.containsKey("all_unnamed_worlds")) {
         GroupManager.logger.finest("Requested world " + worldName + " not found or badly mirrored. Returning all_unnamed_worlds world...");
         return this.getUpdatedWorldData("all_unnamed_worlds");
      } else {
         GroupManager.logger.finest("Requested world " + worldName + " not found or badly mirrored. Returning default world...");
         return this.getDefaultWorld();
      }
   }

   private OverloadedWorldHolder getUpdatedWorldData(String worldName) {
      String worldNameLowered = worldName.toLowerCase();
      if (this.worldsData.containsKey(worldNameLowered)) {
         OverloadedWorldHolder data = (OverloadedWorldHolder)this.worldsData.get(worldNameLowered);
         data.updateDataSource();
         return data;
      } else {
         return null;
      }
   }

   public OverloadedWorldHolder getWorldDataByPlayerName(String playerName) {
      List<Player> matchPlayer = this.plugin.getServer().matchPlayer(playerName);
      return matchPlayer.size() == 1 ? this.getWorldData((Player)matchPlayer.get(0)) : null;
   }

   public OverloadedWorldHolder getWorldData(Player player) {
      return this.getWorldData(player.getWorld().getName());
   }

   public AnjoPermissionsHandler getWorldPermissions(String worldName) {
      return this.getWorldData(worldName).getPermissionsHandler();
   }

   public AnjoPermissionsHandler getWorldPermissions(Player player) {
      return this.getWorldData(player).getPermissionsHandler();
   }

   public AnjoPermissionsHandler getWorldPermissionsByPlayerName(String playerName) {
      WorldDataHolder dh = this.getWorldDataByPlayerName(playerName);
      return dh != null ? dh.getPermissionsHandler() : null;
   }

   private void verifyFirstRun() {
      if (this.plugin.getServer().getName().equalsIgnoreCase("BukkitForge")) {
         this.serverDefaultWorldName = "overworld";
      } else {
         Properties server = new Properties();

         try {
            server.load(new FileInputStream(new File("server.properties")));
            this.serverDefaultWorldName = server.getProperty("level-name").toLowerCase();
         } catch (IOException ex) {
            GroupManager.logger.log(Level.SEVERE, (String)null, ex);
         }
      }

      this.setupWorldFolder(this.serverDefaultWorldName);
   }

   public void setupWorldFolder(String worldName) {
      String worldNameLowered = worldName.toLowerCase();
      this.worldsFolder = new File(this.plugin.getDataFolder(), "worlds");
      if (!this.worldsFolder.exists()) {
         this.worldsFolder.mkdirs();
      }

      File defaultWorldFolder = new File(this.worldsFolder, worldNameLowered);
      if (!defaultWorldFolder.exists() && !this.mirrorsGroup.containsKey(worldNameLowered) || !this.mirrorsUser.containsKey(worldNameLowered)) {
         File casedWorldFolder = new File(this.worldsFolder, worldName);
         if (casedWorldFolder.exists() && casedWorldFolder.getName().toLowerCase().equals(worldNameLowered)) {
            casedWorldFolder.renameTo(new File(this.worldsFolder, worldNameLowered));
         } else {
            defaultWorldFolder.mkdirs();
         }
      }

      if (defaultWorldFolder.exists()) {
         if (!this.mirrorsGroup.containsKey(worldNameLowered)) {
            File groupsFile = new File(defaultWorldFolder, "groups.yml");
            if (!groupsFile.exists() || groupsFile.length() == 0L) {
               InputStream template = this.plugin.getResourceAsStream("groups.yml");

               try {
                  Tasks.copy(template, groupsFile);
               } catch (IOException ex) {
                  GroupManager.logger.log(Level.SEVERE, (String)null, ex);
               }
            }
         }

         if (!this.mirrorsUser.containsKey(worldNameLowered)) {
            File usersFile = new File(defaultWorldFolder, "users.yml");
            if (!usersFile.exists() || usersFile.length() == 0L) {
               InputStream template = this.plugin.getResourceAsStream("users.yml");

               try {
                  Tasks.copy(template, usersFile);
               } catch (IOException ex) {
                  GroupManager.logger.log(Level.SEVERE, (String)null, ex);
               }
            }
         }
      }

   }

   public boolean cloneWorld(String fromWorld, String toWorld) {
      File fromWorldFolder = new File(this.worldsFolder, fromWorld.toLowerCase());
      File toWorldFolder = new File(this.worldsFolder, toWorld.toLowerCase());
      if (!toWorldFolder.exists() && fromWorldFolder.exists()) {
         File fromWorldGroups = new File(fromWorldFolder, "groups.yml");
         File fromWorldUsers = new File(fromWorldFolder, "users.yml");
         if (fromWorldGroups.exists() && fromWorldUsers.exists()) {
            File toWorldGroups = new File(toWorldFolder, "groups.yml");
            File toWorldUsers = new File(toWorldFolder, "users.yml");
            toWorldFolder.mkdirs();

            try {
               Tasks.copy(fromWorldGroups, toWorldGroups);
               Tasks.copy(fromWorldUsers, toWorldUsers);
               return true;
            } catch (IOException ex) {
               Logger.getLogger(WorldsHolder.class.getName()).log(Level.SEVERE, (String)null, ex);
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public void loadWorld(String worldName) {
      this.loadWorld(worldName, false);
   }

   public void loadWorld(String worldName, Boolean isMirror) {
      String worldNameLowered = worldName.toLowerCase();
      if (this.worldsData.containsKey(worldNameLowered)) {
         ((OverloadedWorldHolder)this.worldsData.get(worldNameLowered)).reload();
      } else {
         GroupManager.logger.finest("Trying to load world " + worldName + "...");
         File thisWorldFolder = new File(this.worldsFolder, worldNameLowered);
         if (isMirror || thisWorldFolder.exists() && thisWorldFolder.isDirectory()) {
            File groupsFile = this.mirrorsGroup.containsKey(worldNameLowered) ? null : new File(thisWorldFolder, "groups.yml");
            File usersFile = this.mirrorsUser.containsKey(worldNameLowered) ? null : new File(thisWorldFolder, "users.yml");
            if (groupsFile != null && !groupsFile.exists()) {
               throw new IllegalArgumentException("Groups file for world '" + worldName + "' doesnt exist: " + groupsFile.getPath());
            }

            if (usersFile != null && !usersFile.exists()) {
               throw new IllegalArgumentException("Users file for world '" + worldName + "' doesnt exist: " + usersFile.getPath());
            }

            WorldDataHolder tempHolder = new WorldDataHolder(worldNameLowered);
            if (this.mirrorsGroup.containsKey(worldNameLowered)) {
               tempHolder.setGroupsObject(this.getWorldData((String)this.mirrorsGroup.get(worldNameLowered)).getGroupsObject());
            } else {
               tempHolder.loadGroups(groupsFile);
            }

            if (this.mirrorsUser.containsKey(worldNameLowered)) {
               tempHolder.setUsersObject(this.getWorldData((String)this.mirrorsUser.get(worldNameLowered)).getUsersObject());
            } else {
               tempHolder.loadUsers(usersFile);
            }

            OverloadedWorldHolder thisWorldData = new OverloadedWorldHolder(tempHolder);
            WorldDataHolder var9 = null;
            thisWorldData.setTimeStamps();
            if (thisWorldData != null) {
               GroupManager.logger.finest("Successful load of world " + worldName + "...");
               this.worldsData.put(worldNameLowered, thisWorldData);
               return;
            }
         }

      }
   }

   public boolean isInList(String worldName) {
      return this.worldsData.containsKey(worldName.toLowerCase()) || this.mirrorsGroup.containsKey(worldName.toLowerCase()) || this.mirrorsUser.containsKey(worldName.toLowerCase());
   }

   public boolean hasOwnData(String worldName) {
      return this.worldsData.containsKey(worldName.toLowerCase()) && (!this.mirrorsGroup.containsKey(worldName.toLowerCase()) || !this.mirrorsUser.containsKey(worldName.toLowerCase()));
   }

   public OverloadedWorldHolder getDefaultWorld() {
      return this.getUpdatedWorldData(this.serverDefaultWorldName);
   }

   public ArrayList allWorldsDataList() {
      ArrayList<OverloadedWorldHolder> list = new ArrayList();

      for(String world : this.worldsData.keySet()) {
         if (!world.equalsIgnoreCase("all_unnamed_worlds")) {
            OverloadedWorldHolder data = this.getWorldData(world);
            if (!list.contains(data)) {
               String worldNameLowered = data.getName().toLowerCase();
               String usersMirror = (String)this.mirrorsUser.get(worldNameLowered);
               String groupsMirror = (String)this.mirrorsGroup.get(worldNameLowered);
               if (usersMirror != null && groupsMirror != null && usersMirror == groupsMirror) {
                  data = this.getWorldData(usersMirror.toLowerCase());
                  if (!list.contains(data)) {
                     list.add(data);
                  }
               } else {
                  list.add(data);
               }
            }
         }
      }

      return list;
   }
}
