package fr.neatmonster.nocheatplus.checks.access;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class AsyncCheckData extends ACheckData {
   protected final Map cachedPermissions = Collections.synchronizedMap(new HashMap());

   public boolean hasCachedPermissionEntry(String permission) {
      return this.cachedPermissions.containsKey(permission);
   }

   public boolean hasCachedPermission(String permission) {
      Boolean has = (Boolean)this.cachedPermissions.get(permission);
      return has == null ? false : has;
   }

   public void setCachedPermission(String permission, boolean has) {
      this.cachedPermissions.put(permission, has);
   }
}
