package fr.neatmonster.nocheatplus.checks.access;

import fr.neatmonster.nocheatplus.components.IData;

public interface ICheckData extends IData {
   boolean hasCachedPermissionEntry(String var1);

   boolean hasCachedPermission(String var1);

   void setCachedPermission(String var1, boolean var2);
}
