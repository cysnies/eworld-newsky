package org.anjocaido.groupmanager.permissions;

import java.util.List;
import java.util.Set;
import org.anjocaido.groupmanager.data.Group;
import org.bukkit.entity.Player;

public abstract class PermissionsReaderInterface {
   public abstract boolean has(Player var1, String var2);

   public abstract boolean permission(Player var1, String var2);

   public abstract String getGroup(String var1);

   public abstract boolean inGroup(String var1, String var2);

   public abstract String getGroupPrefix(String var1);

   public abstract String getGroupSuffix(String var1);

   public abstract boolean canGroupBuild(String var1);

   public abstract String getGroupPermissionString(String var1, String var2);

   public abstract int getGroupPermissionInteger(String var1, String var2);

   public abstract boolean getGroupPermissionBoolean(String var1, String var2);

   public abstract double getGroupPermissionDouble(String var1, String var2);

   public abstract String getUserPermissionString(String var1, String var2);

   public abstract int getUserPermissionInteger(String var1, String var2);

   public abstract boolean getUserPermissionBoolean(String var1, String var2);

   public abstract double getUserPermissionDouble(String var1, String var2);

   public abstract String getPermissionString(String var1, String var2);

   public abstract int getPermissionInteger(String var1, String var2);

   public abstract boolean getPermissionBoolean(String var1, String var2);

   public abstract double getPermissionDouble(String var1, String var2);

   public abstract String getUserPrefix(String var1);

   public abstract String getUserSuffix(String var1);

   public abstract Group getDefaultGroup();

   public abstract String[] getGroups(String var1);

   public abstract String getInfoString(String var1, String var2, boolean var3);

   public abstract int getInfoInteger(String var1, String var2, boolean var3);

   public abstract double getInfoDouble(String var1, String var2, boolean var3);

   public abstract boolean getInfoBoolean(String var1, String var2, boolean var3);

   public abstract void addUserInfo(String var1, String var2, Object var3);

   public abstract void removeUserInfo(String var1, String var2);

   public abstract void addGroupInfo(String var1, String var2, Object var3);

   public abstract void removeGroupInfo(String var1, String var2);

   public abstract List getAllPlayersPermissions(String var1);

   public abstract Set getAllPlayersPermissions(String var1, Boolean var2);
}
