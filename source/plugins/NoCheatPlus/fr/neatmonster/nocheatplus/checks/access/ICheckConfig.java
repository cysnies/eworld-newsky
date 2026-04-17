package fr.neatmonster.nocheatplus.checks.access;

import fr.neatmonster.nocheatplus.checks.CheckType;

public interface ICheckConfig {
   boolean isEnabled(CheckType var1);

   boolean getDebug();

   void setDebug(boolean var1);

   String[] getCachePermissions();
}
