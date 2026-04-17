package fr.neatmonster.nocheatplus.components;

public interface PermStateReceiver extends PermStateHolder {
   void setPermission(String var1, String var2, boolean var3);

   void removePlayer(String var1);
}
