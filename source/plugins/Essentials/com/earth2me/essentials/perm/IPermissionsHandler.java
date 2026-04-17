package com.earth2me.essentials.perm;

import java.util.List;
import org.bukkit.entity.Player;

public interface IPermissionsHandler {
   String getGroup(Player var1);

   List getGroups(Player var1);

   boolean canBuild(Player var1, String var2);

   boolean inGroup(Player var1, String var2);

   boolean hasPermission(Player var1, String var2);

   String getPrefix(Player var1);

   String getSuffix(Player var1);
}
