package fr.neatmonster.nocheatplus.checks.access;

public abstract class ACheckData implements ICheckData {
   public boolean hasCachedPermissionEntry(String permission) {
      return false;
   }

   public boolean hasCachedPermission(String permission) {
      return false;
   }

   public void setCachedPermission(String permission, boolean value) {
   }
}
