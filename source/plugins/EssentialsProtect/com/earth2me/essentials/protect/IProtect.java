package com.earth2me.essentials.protect;

import java.util.Map;
import org.bukkit.plugin.Plugin;

public interface IProtect extends Plugin {
   boolean getSettingBool(ProtectConfig var1);

   String getSettingString(ProtectConfig var1);

   EssentialsConnect getEssentialsConnect();

   Map getSettingsBoolean();

   Map getSettingsString();

   Map getSettingsList();
}
