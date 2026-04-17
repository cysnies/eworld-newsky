package com.earth2me.essentials;

import com.earth2me.essentials.commands.IEssentialsCommand;
import java.util.Map;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface IUser extends Player {
   long getLastTeleportTimestamp();

   boolean isAuthorized(String var1);

   boolean isAuthorized(IEssentialsCommand var1);

   boolean isAuthorized(IEssentialsCommand var1, String var2);

   void setLastTeleportTimestamp(long var1);

   Location getLastLocation();

   Player getBase();

   double getMoney();

   void takeMoney(double var1);

   void giveMoney(double var1);

   boolean canAfford(double var1);

   String getGroup();

   void setLastLocation();

   Location getHome(String var1) throws Exception;

   Location getHome(Location var1) throws Exception;

   boolean isHidden();

   Teleport getTeleport();

   void setJail(String var1);

   boolean isIgnoreExempt();

   boolean isAfk();

   void setAfk(boolean var1);

   void setLogoutLocation();

   Location getLogoutLocation();

   void setConfigProperty(String var1, Object var2);

   Set getConfigKeys();

   Map getConfigMap();

   Map getConfigMap(String var1);
}
