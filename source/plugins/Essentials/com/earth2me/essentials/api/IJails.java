package com.earth2me.essentials.api;

import com.earth2me.essentials.IUser;
import java.util.Collection;
import org.bukkit.Location;

public interface IJails extends IReload {
   Location getJail(String var1) throws Exception;

   Collection getList() throws Exception;

   int getCount();

   void removeJail(String var1) throws Exception;

   void sendToJail(IUser var1, String var2) throws Exception;

   void setJail(String var1, Location var2) throws Exception;
}
