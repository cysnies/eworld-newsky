package fr.neatmonster.nocheatplus.config;

import java.util.Collection;

public interface WorldConfigProvider {
   RawConfigFile getDefaultConfig();

   RawConfigFile getConfig(String var1);

   Collection getAllConfigs();
}
