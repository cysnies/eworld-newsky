package org.anjocaido.groupmanager.permissions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.anjocaido.groupmanager.GroupManager;
import org.anjocaido.groupmanager.data.Group;
import org.anjocaido.groupmanager.data.User;
import org.anjocaido.groupmanager.dataholder.WorldDataHolder;
import org.anjocaido.groupmanager.utils.PermissionCheckResult;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class AnjoPermissionsHandler extends PermissionsReaderInterface {
   WorldDataHolder ph = null;

   public AnjoPermissionsHandler(WorldDataHolder holder) {
      this.ph = holder;
   }

   public boolean has(Player player, String permission) {
      return this.permission(player, permission);
   }

   public boolean permission(Player player, String permission) {
      return this.checkUserPermission(this.ph.getUser(player.getName()).updatePlayer(player), permission);
   }

   public boolean permission(String playerName, String permission) {
      return this.checkUserPermission(this.ph.getUser(playerName), permission);
   }

   public String getGroup(String userName) {
      return this.ph.getUser(userName).getGroup().getName();
   }

   public List getAllPlayersPermissions(String userName) {
      List<String> perms = new ArrayList();
      perms.addAll(this.getAllPlayersPermissions(userName, true));
      return perms;
   }

   public Set getAllPlayersPermissions(String userName, Boolean includeChildren) {
      Set<String> playerPermArray = new HashSet();
      playerPermArray.addAll(this.populatePerms(this.ph.getUser(userName).getPermissionList(), includeChildren));
      ArrayList<String> alreadyProcessed = new ArrayList();

      for(String group : this.getGroups(userName)) {
         if (!alreadyProcessed.contains(group)) {
            alreadyProcessed.add(group);
            new HashSet();
            Set groupPermArray;
            if (group.startsWith("g:") && GroupManager.getGlobalGroups().hasGroup(group)) {
               groupPermArray = this.populatePerms(GroupManager.getGlobalGroups().getGroupsPermissions(group), includeChildren);
            } else {
               groupPermArray = this.populatePerms(this.ph.getGroup(group).getPermissionList(), includeChildren);
            }

            for(String perm : groupPermArray) {
               boolean negated = perm.startsWith("-");
               if (!negated && !playerPermArray.contains(perm) && !playerPermArray.contains("-" + perm) || negated && !playerPermArray.contains(perm.substring(1)) && !playerPermArray.contains("-" + perm)) {
                  playerPermArray.add(perm);
               }
            }
         }
      }

      return playerPermArray;
   }

   private Set populatePerms(List permsList, boolean includeChildren) {
      List<String> perms = new ArrayList(permsList);
      Set<String> permArray = new HashSet();
      Boolean allPerms = false;
      if (perms.contains("*")) {
         permArray.addAll(GroupManager.BukkitPermissions.getAllRegisteredPermissions(includeChildren));
         allPerms = true;
         perms.remove("*");
         perms.remove("groupmanager.noofflineperms");
      }

      for(String perm : perms) {
         boolean negated = perm.startsWith("-");
         if (!permArray.contains(perm)) {
            permArray.add(perm);
            if (negated && permArray.contains(perm.substring(1))) {
               permArray.remove(perm.substring(1));
            }

            if (includeChildren || negated && allPerms) {
               Map<String, Boolean> children = GroupManager.BukkitPermissions.getAllChildren(negated ? perm.substring(1) : perm, new HashSet());
               if (children != null && negated) {
                  if (allPerms) {
                     for(String child : children.keySet()) {
                        if ((Boolean)children.get(child) && permArray.contains(child)) {
                           permArray.remove(child);
                        }
                     }
                  } else {
                     for(String child : children.keySet()) {
                        if ((Boolean)children.get(child) && !permArray.contains(child) && !permArray.contains("-" + child)) {
                           permArray.add(child);
                        }
                     }
                  }
               }
            }
         }
      }

      return permArray;
   }

   public boolean inGroup(String name, String group) {
      if (this.hasGroupInInheritance(this.ph.getUser(name).getGroup(), group)) {
         return true;
      } else {
         for(Group subGroup : this.ph.getUser(name).subGroupListCopy()) {
            if (this.hasGroupInInheritance(subGroup, group)) {
               return true;
            }
         }

         return false;
      }
   }

   public String getUserPrefix(String user) {
      String prefix = this.ph.getUser(user).getVariables().getVarString("prefix");
      return prefix.length() != 0 ? prefix : this.getGroupPrefix(this.getGroup(user));
   }

   public String getUserSuffix(String user) {
      String suffix = this.ph.getUser(user).getVariables().getVarString("suffix");
      return suffix.length() != 0 ? suffix : this.getGroupSuffix(this.getGroup(user));
   }

   public String getPrimaryGroup(String user) {
      return this.getGroup(user);
   }

   public boolean canUserBuild(String userName) {
      return this.getPermissionBoolean(userName, "build");
   }

   public String getGroupPrefix(String groupName) {
      Group g = this.ph.getGroup(groupName);
      return g == null ? "" : g.getVariables().getVarString("prefix");
   }

   public String getGroupSuffix(String groupName) {
      Group g = this.ph.getGroup(groupName);
      return g == null ? "" : g.getVariables().getVarString("suffix");
   }

   public boolean canGroupBuild(String groupName) {
      Group g = this.ph.getGroup(groupName);
      return g == null ? false : g.getVariables().getVarBoolean("build");
   }

   public String getGroupPermissionString(String groupName, String variable) {
      Group start = this.ph.getGroup(groupName);
      if (start == null) {
         return null;
      } else {
         Group result = this.nextGroupWithVariable(start, variable);
         return result == null ? null : result.getVariables().getVarString(variable);
      }
   }

   public int getGroupPermissionInteger(String groupName, String variable) {
      Group start = this.ph.getGroup(groupName);
      if (start == null) {
         return -1;
      } else {
         Group result = this.nextGroupWithVariable(start, variable);
         return result == null ? -1 : result.getVariables().getVarInteger(variable);
      }
   }

   public boolean getGroupPermissionBoolean(String group, String variable) {
      Group start = this.ph.getGroup(group);
      if (start == null) {
         return false;
      } else {
         Group result = this.nextGroupWithVariable(start, variable);
         return result == null ? false : result.getVariables().getVarBoolean(variable);
      }
   }

   public double getGroupPermissionDouble(String group, String variable) {
      Group start = this.ph.getGroup(group);
      if (start == null) {
         return (double)-1.0F;
      } else {
         Group result = this.nextGroupWithVariable(start, variable);
         return result == null ? (double)-1.0F : result.getVariables().getVarDouble(variable);
      }
   }

   public String getUserPermissionString(String user, String variable) {
      User auser = this.ph.getUser(user);
      return auser == null ? "" : auser.getVariables().getVarString(variable);
   }

   public int getUserPermissionInteger(String user, String variable) {
      User auser = this.ph.getUser(user);
      return auser == null ? -1 : auser.getVariables().getVarInteger(variable);
   }

   public boolean getUserPermissionBoolean(String user, String variable) {
      User auser = this.ph.getUser(user);
      return auser == null ? false : auser.getVariables().getVarBoolean(variable);
   }

   public double getUserPermissionDouble(String user, String variable) {
      User auser = this.ph.getUser(user);
      return auser == null ? (double)-1.0F : auser.getVariables().getVarDouble(variable);
   }

   public String getPermissionString(String user, String variable) {
      User auser = this.ph.getUser(user);
      if (auser == null) {
         return "";
      } else if (auser.getVariables().hasVar(variable)) {
         return auser.getVariables().getVarString(variable);
      } else {
         Group start = auser.getGroup();
         if (start == null) {
            return "";
         } else {
            Group result = this.nextGroupWithVariable(start, variable);
            if (result == null) {
               if (!auser.isSubGroupsEmpty()) {
                  for(Group subGroup : auser.subGroupListCopy()) {
                     result = this.nextGroupWithVariable(subGroup, variable);
                     if (result != null) {
                     }
                  }
               }

               if (result == null) {
                  return "";
               }
            }

            return result.getVariables().getVarString(variable);
         }
      }
   }

   public int getPermissionInteger(String user, String variable) {
      User auser = this.ph.getUser(user);
      if (auser == null) {
         return -1;
      } else if (auser.getVariables().hasVar(variable)) {
         return auser.getVariables().getVarInteger(variable);
      } else {
         Group start = auser.getGroup();
         if (start == null) {
            return -1;
         } else {
            Group result = this.nextGroupWithVariable(start, variable);
            if (result == null) {
               if (!auser.isSubGroupsEmpty()) {
                  for(Group subGroup : auser.subGroupListCopy()) {
                     result = this.nextGroupWithVariable(subGroup, variable);
                     if (result != null) {
                     }
                  }
               }

               if (result == null) {
                  return -1;
               }
            }

            return result.getVariables().getVarInteger(variable);
         }
      }
   }

   public boolean getPermissionBoolean(String user, String variable) {
      User auser = this.ph.getUser(user);
      if (auser == null) {
         return false;
      } else if (auser.getVariables().hasVar(variable)) {
         return auser.getVariables().getVarBoolean(variable);
      } else {
         Group start = auser.getGroup();
         if (start == null) {
            return false;
         } else {
            Group result = this.nextGroupWithVariable(start, variable);
            if (result == null) {
               if (!auser.isSubGroupsEmpty()) {
                  for(Group subGroup : auser.subGroupListCopy()) {
                     result = this.nextGroupWithVariable(subGroup, variable);
                     if (result != null) {
                     }
                  }
               }

               if (result == null) {
                  return false;
               }
            }

            return result.getVariables().getVarBoolean(variable);
         }
      }
   }

   public double getPermissionDouble(String user, String variable) {
      User auser = this.ph.getUser(user);
      if (auser == null) {
         return (double)-1.0F;
      } else if (auser.getVariables().hasVar(variable)) {
         return auser.getVariables().getVarDouble(variable);
      } else {
         Group start = auser.getGroup();
         if (start == null) {
            return (double)-1.0F;
         } else {
            Group result = this.nextGroupWithVariable(start, variable);
            if (result == null) {
               if (!auser.isSubGroupsEmpty()) {
                  for(Group subGroup : auser.subGroupListCopy()) {
                     result = this.nextGroupWithVariable(subGroup, variable);
                     if (result != null) {
                     }
                  }
               }

               if (result == null) {
                  return (double)-1.0F;
               }
            }

            return result.getVariables().getVarDouble(variable);
         }
      }
   }

   public PermissionCheckResult checkUserOnlyPermission(User user, String permission) {
      user.sortPermissions();
      PermissionCheckResult result = new PermissionCheckResult();
      result.askedPermission = permission;
      result.owner = user;

      for(String access : user.getPermissionList()) {
         result.resultType = this.comparePermissionString(access, permission);
         if (result.resultType != PermissionCheckResult.Type.NOTFOUND) {
            result.accessLevel = access;
            return result;
         }
      }

      result.resultType = PermissionCheckResult.Type.NOTFOUND;
      return result;
   }

   public PermissionCheckResult checkGroupOnlyPermission(Group group, String permission) {
      group.sortPermissions();
      PermissionCheckResult result = new PermissionCheckResult();
      result.owner = group;
      result.askedPermission = permission;

      for(String access : group.getPermissionList()) {
         result.resultType = this.comparePermissionString(access, permission);
         if (result.resultType != PermissionCheckResult.Type.NOTFOUND) {
            result.accessLevel = access;
            return result;
         }
      }

      result.resultType = PermissionCheckResult.Type.NOTFOUND;
      return result;
   }

   public boolean checkUserPermission(User user, String permission) {
      PermissionCheckResult result = this.checkFullGMPermission(user, permission, true);
      return result.resultType == PermissionCheckResult.Type.EXCEPTION || result.resultType == PermissionCheckResult.Type.FOUND;
   }

   public PermissionCheckResult checkFullUserPermission(User user, String targetPermission) {
      return this.checkFullGMPermission(user, targetPermission, true);
   }

   public PermissionCheckResult checkFullGMPermission(User user, String targetPermission, Boolean checkBukkit) {
      if (user != null && targetPermission != null && !targetPermission.isEmpty() && (Bukkit.getServer().getOnlineMode() || this.checkPermission(user, "groupmanager.noofflineperms", false).resultType != PermissionCheckResult.Type.FOUND)) {
         return this.checkPermission(user, targetPermission, checkBukkit);
      } else {
         PermissionCheckResult result = new PermissionCheckResult();
         result.accessLevel = targetPermission;
         result.resultType = PermissionCheckResult.Type.NOTFOUND;
         return result;
      }
   }

   private PermissionCheckResult checkPermission(User user, String targetPermission, Boolean checkBukkit) {
      PermissionCheckResult result = new PermissionCheckResult();
      result.accessLevel = targetPermission;
      result.resultType = PermissionCheckResult.Type.NOTFOUND;
      if (checkBukkit) {
         Player player = user.getBukkitPlayer();
         if (player != null && player.hasPermission(targetPermission)) {
            result.resultType = PermissionCheckResult.Type.FOUND;
            result.owner = user;
            return result;
         }
      }

      PermissionCheckResult resultUser = this.checkUserOnlyPermission(user, targetPermission);
      if (resultUser.resultType != PermissionCheckResult.Type.NOTFOUND) {
         resultUser.accessLevel = targetPermission;
         return resultUser;
      } else {
         PermissionCheckResult resultGroup = this.checkGroupPermissionWithInheritance(user.getGroup(), targetPermission);
         if (resultGroup.resultType != PermissionCheckResult.Type.NOTFOUND) {
            resultGroup.accessLevel = targetPermission;
            return resultGroup;
         } else {
            for(Group subGroup : user.subGroupListCopy()) {
               PermissionCheckResult resultSubGroup = this.checkGroupPermissionWithInheritance(subGroup, targetPermission);
               if (resultSubGroup.resultType != PermissionCheckResult.Type.NOTFOUND) {
                  resultSubGroup.accessLevel = targetPermission;
                  return resultSubGroup;
               }
            }

            return result;
         }
      }
   }

   public Group nextGroupWithVariable(Group start, String targetVariable) {
      if (start != null && targetVariable != null) {
         LinkedList<Group> stack = new LinkedList();
         ArrayList<Group> alreadyVisited = new ArrayList();
         stack.push(start);
         alreadyVisited.add(start);

         while(!stack.isEmpty()) {
            Group now = (Group)stack.pop();
            if (now.getVariables().hasVar(targetVariable)) {
               return now;
            }

            for(String sonName : now.getInherits()) {
               Group son = this.ph.getGroup(sonName);
               if (son != null && !alreadyVisited.contains(son)) {
                  stack.push(son);
                  alreadyVisited.add(son);
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public boolean hasGroupInInheritance(Group start, String askedGroup) {
      if (start != null && askedGroup != null) {
         LinkedList<Group> stack = new LinkedList();
         ArrayList<Group> alreadyVisited = new ArrayList();
         stack.push(start);
         alreadyVisited.add(start);

         while(!stack.isEmpty()) {
            Group now = (Group)stack.pop();
            if (now.getName().equalsIgnoreCase(askedGroup)) {
               return true;
            }

            for(String sonName : now.getInherits()) {
               Group son = this.ph.getGroup(sonName);
               if (son != null && !alreadyVisited.contains(son)) {
                  stack.push(son);
                  alreadyVisited.add(son);
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public PermissionCheckResult checkGroupPermissionWithInheritance(Group start, String targetPermission) {
      if (start != null && targetPermission != null) {
         LinkedList<Group> stack = new LinkedList();
         List<Group> alreadyVisited = new ArrayList();
         stack.push(start);
         alreadyVisited.add(start);

         while(!stack.isEmpty()) {
            Group now = (Group)stack.pop();
            PermissionCheckResult resultNow = this.checkGroupOnlyPermission(now, targetPermission);
            if (!resultNow.resultType.equals(PermissionCheckResult.Type.NOTFOUND)) {
               resultNow.accessLevel = targetPermission;
               return resultNow;
            }

            for(String sonName : now.getInherits()) {
               Group son = this.ph.getGroup(sonName);
               if (son != null && !alreadyVisited.contains(son)) {
                  stack.add(son);
                  alreadyVisited.add(son);
               }
            }
         }

         PermissionCheckResult result = new PermissionCheckResult();
         result.askedPermission = targetPermission;
         result.resultType = PermissionCheckResult.Type.NOTFOUND;
         return result;
      } else {
         return null;
      }
   }

   public ArrayList listAllGroupsInherited(Group start) {
      if (start == null) {
         return null;
      } else {
         LinkedList<Group> stack = new LinkedList();
         ArrayList<String> alreadyVisited = new ArrayList();
         stack.push(start);
         alreadyVisited.add(start.getName());

         while(!stack.isEmpty()) {
            Group now = (Group)stack.pop();

            for(String sonName : now.getInherits()) {
               Group son = this.ph.getGroup(sonName);
               if (son != null && !alreadyVisited.contains(son.getName())) {
                  stack.push(son);
                  alreadyVisited.add(son.getName());
               }
            }
         }

         return alreadyVisited;
      }
   }

   public PermissionCheckResult.Type comparePermissionString(String userAccessLevel, String fullPermissionName) {
      int userAccessLevelLength;
      if (userAccessLevel != null && fullPermissionName != null && fullPermissionName.length() != 0 && (userAccessLevelLength = userAccessLevel.length()) != 0) {
         PermissionCheckResult.Type result = PermissionCheckResult.Type.FOUND;
         int userAccessLevelOffset = 0;
         if (userAccessLevel.charAt(0) == '+') {
            userAccessLevelOffset = 1;
            result = PermissionCheckResult.Type.EXCEPTION;
         } else if (userAccessLevel.charAt(0) == '-') {
            userAccessLevelOffset = 1;
            result = PermissionCheckResult.Type.NEGATION;
         }

         if (fullPermissionName.equals(userAccessLevel)) {
            return result;
         } else {
            if ("groupmanager.noofflineperms".equals(fullPermissionName)) {
               result = PermissionCheckResult.Type.NOTFOUND;
            }

            if ("*".regionMatches(0, userAccessLevel, userAccessLevelOffset, userAccessLevelLength - userAccessLevelOffset)) {
               return result;
            } else {
               int fullPermissionNameOffset;
               if (fullPermissionName.charAt(0) != '+' && fullPermissionName.charAt(0) != '-') {
                  fullPermissionNameOffset = 0;
               } else {
                  fullPermissionNameOffset = 1;
               }

               if (userAccessLevel.charAt(userAccessLevel.length() - 1) == '*') {
                  return userAccessLevel.regionMatches(true, userAccessLevelOffset, fullPermissionName, fullPermissionNameOffset, userAccessLevelLength - userAccessLevelOffset - 1) ? result : PermissionCheckResult.Type.NOTFOUND;
               } else {
                  return userAccessLevel.regionMatches(true, userAccessLevelOffset, fullPermissionName, fullPermissionNameOffset, Math.max(userAccessLevelLength - userAccessLevelOffset, fullPermissionName.length() - fullPermissionNameOffset)) ? result : PermissionCheckResult.Type.NOTFOUND;
               }
            }
         }
      } else {
         return PermissionCheckResult.Type.NOTFOUND;
      }
   }

   public String[] getGroups(String userName) {
      ArrayList<String> allGroups = this.listAllGroupsInherited(this.ph.getUser(userName).getGroup());

      for(Group subg : this.ph.getUser(userName).subGroupListCopy()) {
         allGroups.addAll(this.listAllGroupsInherited(subg));
      }

      String[] arr = new String[allGroups.size()];
      return (String[])allGroups.toArray(arr);
   }

   private Group breadthFirstSearch(Group start, String targerPermission) {
      if (start != null && targerPermission != null) {
         LinkedList<Group> stack = new LinkedList();
         ArrayList<Group> alreadyVisited = new ArrayList();
         stack.push(start);
         alreadyVisited.add(start);

         while(!stack.isEmpty()) {
            Group now = (Group)stack.pop();
            PermissionCheckResult resultNow = this.checkGroupOnlyPermission(now, targerPermission);
            if (resultNow.resultType.equals(PermissionCheckResult.Type.EXCEPTION) || resultNow.resultType.equals(PermissionCheckResult.Type.FOUND)) {
               return now;
            }

            if (resultNow.resultType.equals(PermissionCheckResult.Type.NEGATION)) {
               return null;
            }

            for(String sonName : now.getInherits()) {
               Group son = this.ph.getGroup(sonName);
               if (son != null && !alreadyVisited.contains(son)) {
                  stack.push(son);
                  alreadyVisited.add(son);
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public Group getDefaultGroup() {
      return this.ph.getDefaultGroup();
   }

   public String getInfoString(String entryName, String path, boolean isGroup) {
      if (isGroup) {
         Group data = this.ph.getGroup(entryName);
         return data == null ? null : data.getVariables().getVarString(path);
      } else {
         User data = this.ph.getUser(entryName);
         return data == null ? null : data.getVariables().getVarString(path);
      }
   }

   public int getInfoInteger(String entryName, String path, boolean isGroup) {
      if (isGroup) {
         Group data = this.ph.getGroup(entryName);
         return data == null ? -1 : data.getVariables().getVarInteger(path);
      } else {
         User data = this.ph.getUser(entryName);
         return data == null ? -1 : data.getVariables().getVarInteger(path);
      }
   }

   public double getInfoDouble(String entryName, String path, boolean isGroup) {
      if (isGroup) {
         Group data = this.ph.getGroup(entryName);
         return data == null ? (double)-1.0F : data.getVariables().getVarDouble(path);
      } else {
         User data = this.ph.getUser(entryName);
         return data == null ? (double)-1.0F : data.getVariables().getVarDouble(path);
      }
   }

   public boolean getInfoBoolean(String entryName, String path, boolean isGroup) {
      if (isGroup) {
         Group data = this.ph.getGroup(entryName);
         return data == null ? false : data.getVariables().getVarBoolean(path);
      } else {
         User data = this.ph.getUser(entryName);
         return data == null ? false : data.getVariables().getVarBoolean(path);
      }
   }

   public void addUserInfo(String name, String path, Object data) {
      this.ph.getUser(name).getVariables().addVar(path, data);
   }

   public void removeUserInfo(String name, String path) {
      this.ph.getUser(name).getVariables().removeVar(path);
   }

   public void addGroupInfo(String name, String path, Object data) {
      this.ph.getGroup(name).getVariables().addVar(path, data);
   }

   public void removeGroupInfo(String name, String path) {
      this.ph.getGroup(name).getVariables().removeVar(path);
   }
}
