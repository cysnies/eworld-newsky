package com.earth2me.essentials.antibuild;

import java.util.Map;
import org.bukkit.plugin.Plugin;

public interface IAntiBuild extends Plugin {
   boolean checkProtectionItems(AntiBuildConfig var1, int var2);

   boolean getSettingBool(AntiBuildConfig var1);

   EssentialsConnect getEssentialsConnect();

   Map getSettingsBoolean();

   Map getSettingsList();
}
