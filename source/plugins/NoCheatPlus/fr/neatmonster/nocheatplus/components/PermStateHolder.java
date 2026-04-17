package fr.neatmonster.nocheatplus.components;

public interface PermStateHolder {
   String[] getDefaultPermissions();

   boolean hasPermission(String var1, String var2);
}
