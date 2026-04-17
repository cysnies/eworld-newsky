package com.sk89q.worldedit.cui;

import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;

public interface CUIRegion {
   void describeCUI(LocalSession var1, LocalPlayer var2);

   void describeLegacyCUI(LocalSession var1, LocalPlayer var2);

   int getProtocolVersion();

   String getTypeID();

   String getLegacyTypeID();
}
