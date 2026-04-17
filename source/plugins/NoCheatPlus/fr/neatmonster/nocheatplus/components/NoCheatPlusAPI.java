package fr.neatmonster.nocheatplus.components;

public interface NoCheatPlusAPI extends ComponentRegistry, ComponentRegistryProvider, MCAccessHolder {
   boolean addComponent(Object var1, boolean var2);

   int sendAdminNotifyMessage(String var1);

   boolean allowLogin(String var1);

   int allowLoginAll();

   void denyLogin(String var1, long var2);

   boolean isLoginDenied(String var1);

   String[] getLoginDeniedPlayers();

   boolean isLoginDenied(String var1, long var2);
}
