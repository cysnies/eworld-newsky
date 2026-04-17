package org.anjocaido.groupmanager.dataholder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.anjocaido.groupmanager.data.User;

public class OverloadedWorldHolder extends WorldDataHolder {
   protected final Map overloadedUsers = Collections.synchronizedMap(new HashMap());

   public OverloadedWorldHolder(WorldDataHolder ph) {
      super(ph.getName());
      this.setGroupsFile(ph.getGroupsFile());
      this.setUsersFile(ph.getUsersFile());
      this.groups = ph.groups;
      this.users = ph.users;
   }

   public User getUser(String userName) {
      String userNameLowered = userName.toLowerCase();
      if (this.overloadedUsers.containsKey(userNameLowered)) {
         return (User)this.overloadedUsers.get(userNameLowered);
      } else if (this.getUsers().containsKey(userNameLowered)) {
         return (User)this.getUsers().get(userNameLowered);
      } else {
         User newUser = this.createUser(userName);
         this.setUsersChanged(true);
         return newUser;
      }
   }

   public void addUser(User theUser) {
      if (theUser.getDataSource() != this) {
         theUser = theUser.clone(this);
      }

      if (theUser != null) {
         if (theUser.getGroup() == null || !this.getGroups().containsKey(theUser.getGroupName().toLowerCase())) {
            theUser.setGroup(this.getDefaultGroup());
         }

         if (this.overloadedUsers.containsKey(theUser.getName().toLowerCase())) {
            this.overloadedUsers.remove(theUser.getName().toLowerCase());
            this.overloadedUsers.put(theUser.getName().toLowerCase(), theUser);
         } else {
            this.removeUser(theUser.getName());
            this.getUsers().put(theUser.getName().toLowerCase(), theUser);
            this.setUsersChanged(true);
         }
      }
   }

   public boolean removeUser(String userName) {
      if (this.overloadedUsers.containsKey(userName.toLowerCase())) {
         this.overloadedUsers.remove(userName.toLowerCase());
         return true;
      } else if (this.getUsers().containsKey(userName.toLowerCase())) {
         this.getUsers().remove(userName.toLowerCase());
         this.setUsersChanged(true);
         return true;
      } else {
         return false;
      }
   }

   public boolean removeGroup(String groupName) {
      if (groupName.equals(this.getDefaultGroup())) {
         return false;
      } else {
         synchronized(this.getGroups()) {
            for(String key : this.getGroups().keySet()) {
               if (groupName.equalsIgnoreCase(key)) {
                  this.getGroups().remove(key);
                  synchronized(this.getUsers()) {
                     for(String userKey : this.getUsers().keySet()) {
                        User user = (User)this.getUsers().get(userKey);
                        if (user.getGroupName().equalsIgnoreCase(key)) {
                           user.setGroup(this.getDefaultGroup());
                        }
                     }
                  }

                  synchronized(this.overloadedUsers) {
                     for(String userKey : this.overloadedUsers.keySet()) {
                        User user = (User)this.overloadedUsers.get(userKey);
                        if (user.getGroupName().equalsIgnoreCase(key)) {
                           user.setGroup(this.getDefaultGroup());
                        }
                     }
                  }

                  this.setGroupsChanged(true);
                  return true;
               }
            }

            return false;
         }
      }
   }

   public Collection getUserList() {
      Collection<User> overloadedList = new ArrayList();
      synchronized(this.getUsers()) {
         for(User u : this.getUsers().values()) {
            if (this.overloadedUsers.containsKey(u.getName().toLowerCase())) {
               overloadedList.add(this.overloadedUsers.get(u.getName().toLowerCase()));
            } else {
               overloadedList.add(u);
            }
         }

         return overloadedList;
      }
   }

   public boolean isOverloaded(String userName) {
      return this.overloadedUsers.containsKey(userName.toLowerCase());
   }

   public void overloadUser(String userName) {
      if (!this.isOverloaded(userName)) {
         User theUser = this.getUser(userName);
         theUser = theUser.clone();
         if (this.overloadedUsers.containsKey(theUser.getName().toLowerCase())) {
            this.overloadedUsers.remove(theUser.getName().toLowerCase());
         }

         this.overloadedUsers.put(theUser.getName().toLowerCase(), theUser);
      }

   }

   public void removeOverload(String userName) {
      this.overloadedUsers.remove(userName.toLowerCase());
   }

   public User surpassOverload(String userName) {
      if (!this.isOverloaded(userName)) {
         return this.getUser(userName);
      } else if (this.getUsers().containsKey(userName.toLowerCase())) {
         return (User)this.getUsers().get(userName.toLowerCase());
      } else {
         User newUser = this.createUser(userName);
         return newUser;
      }
   }
}
