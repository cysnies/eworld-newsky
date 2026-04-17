package com.earth2me.essentials;

import com.earth2me.essentials.commands.IEssentialsCommand;
import com.earth2me.essentials.textreader.IText;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventPriority;

public interface ISettings extends IConf {
   boolean areSignsDisabled();

   IText getAnnounceNewPlayerFormat();

   boolean getAnnounceNewPlayers();

   String getNewPlayerKit();

   String getBackupCommand();

   long getBackupInterval();

   MessageFormat getChatFormat(String var1);

   int getChatRadius();

   double getCommandCost(IEssentialsCommand var1);

   double getCommandCost(String var1);

   String getCurrencySymbol();

   int getOversizedStackSize();

   int getDefaultStackSize();

   double getHealCooldown();

   Set getSocialSpyCommands();

   Map getKit(String var1);

   ConfigurationSection getKits();

   String getLocale();

   String getNewbieSpawn();

   String getNicknamePrefix();

   ChatColor getOperatorColor() throws Exception;

   boolean getPerWarpPermission();

   boolean getProtectBoolean(String var1, boolean var2);

   int getProtectCreeperMaxHeight();

   List getProtectList(String var1);

   boolean getProtectPreventSpawn(String var1);

   String getProtectString(String var1);

   boolean getRespawnAtHome();

   Set getMultipleHomes();

   int getHomeLimit(String var1);

   int getHomeLimit(User var1);

   int getSpawnMobLimit();

   int getStartingBalance();

   double getTeleportCooldown();

   double getTeleportDelay();

   boolean hidePermissionlessHelp();

   boolean isCommandDisabled(IEssentialsCommand var1);

   boolean isCommandDisabled(String var1);

   boolean isCommandOverridden(String var1);

   boolean isDebug();

   boolean isEcoDisabled();

   boolean isTradeInStacks(int var1);

   List itemSpawnBlacklist();

   List enabledSigns();

   boolean permissionBasedItemSpawn();

   boolean showNonEssCommandsInHelp();

   boolean warnOnBuildDisallow();

   boolean warnOnSmite();

   double getMaxMoney();

   double getMinMoney();

   boolean isEcoLogEnabled();

   boolean isEcoLogUpdateEnabled();

   boolean removeGodOnDisconnect();

   boolean changeDisplayName();

   boolean changePlayerListName();

   boolean isPlayerCommand(String var1);

   boolean useBukkitPermissions();

   boolean addPrefixSuffix();

   boolean disablePrefix();

   boolean disableSuffix();

   long getAutoAfk();

   long getAutoAfkKick();

   boolean getFreezeAfkPlayers();

   boolean cancelAfkOnMove();

   boolean cancelAfkOnInteract();

   boolean areDeathMessagesEnabled();

   void setDebug(boolean var1);

   Set getNoGodWorlds();

   boolean getUpdateBedAtDaytime();

   boolean allowUnsafeEnchantments();

   boolean getRepairEnchanted();

   boolean isWorldTeleportPermissions();

   boolean isWorldHomePermissions();

   boolean registerBackInListener();

   boolean getDisableItemPickupWhileAfk();

   EventPriority getRespawnPriority();

   long getTpaAcceptCancellation();

   boolean isMetricsEnabled();

   void setMetricsEnabled(boolean var1);

   long getTeleportInvulnerability();

   boolean isTeleportInvulnerability();

   long getLoginAttackDelay();

   int getSignUsePerSecond();

   double getMaxFlySpeed();

   double getMaxWalkSpeed();

   int getMailsPerMinute();

   void setEssentialsChatActive(boolean var1);

   long getMaxTempban();

   Map getListGroupConfig();
}
