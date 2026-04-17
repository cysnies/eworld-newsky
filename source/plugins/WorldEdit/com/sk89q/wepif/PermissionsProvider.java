package com.sk89q.wepif;

import org.bukkit.OfflinePlayer;

public interface PermissionsProvider {
   boolean hasPermission(String var1, String var2);

   boolean hasPermission(String var1, String var2, String var3);

   boolean inGroup(String var1, String var2);

   String[] getGroups(String var1);

   boolean hasPermission(OfflinePlayer var1, String var2);

   boolean hasPermission(String var1, OfflinePlayer var2, String var3);

   boolean inGroup(OfflinePlayer var1, String var2);

   String[] getGroups(OfflinePlayer var1);
}
