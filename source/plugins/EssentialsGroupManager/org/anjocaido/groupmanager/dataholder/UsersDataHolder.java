package org.anjocaido.groupmanager.dataholder;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.anjocaido.groupmanager.data.User;

public class UsersDataHolder {
   private WorldDataHolder dataSource;
   private File usersFile;
   private boolean haveUsersChanged = false;
   private long timeStampUsers = 0L;
   private final Map users = Collections.synchronizedMap(new HashMap());

   protected UsersDataHolder() {
   }

   public void setDataSource(WorldDataHolder dataSource) {
      this.dataSource = dataSource;
      synchronized(this.users) {
         for(User user : this.users.values()) {
            user.setDataSource(this.dataSource);
         }

      }
   }

   public Map getUsers() {
      return this.users;
   }

   public WorldDataHolder getDataSource() {
      return this.dataSource;
   }

   public void resetUsers() {
      this.users.clear();
   }

   public File getUsersFile() {
      return this.usersFile;
   }

   public void setUsersFile(File usersFile) {
      this.usersFile = usersFile;
   }

   public boolean HaveUsersChanged() {
      return this.haveUsersChanged;
   }

   public void setUsersChanged(boolean haveUsersChanged) {
      this.haveUsersChanged = haveUsersChanged;
   }

   public long getTimeStampUsers() {
      return this.timeStampUsers;
   }

   public void setTimeStampUsers(long timeStampUsers) {
      this.timeStampUsers = timeStampUsers;
   }
}
