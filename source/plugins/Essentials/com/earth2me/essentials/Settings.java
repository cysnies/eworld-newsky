package com.earth2me.essentials;

import com.earth2me.essentials.commands.IEssentialsCommand;
import com.earth2me.essentials.signs.EssentialsSign;
import com.earth2me.essentials.signs.Signs;
import com.earth2me.essentials.textreader.IText;
import com.earth2me.essentials.textreader.SimpleTextInput;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;

public class Settings implements ISettings {
   private final transient EssentialsConf config;
   private static final Logger logger = Logger.getLogger("Minecraft");
   private final transient IEssentials ess;
   private boolean metricsEnabled = true;
   private int chatRadius = 0;
   private Set disabledCommands = new HashSet();
   private ConfigurationSection commandCosts;
   private Set socialSpyCommands = new HashSet();
   private String nicknamePrefix = "~";
   private ConfigurationSection kits;
   private ChatColor operatorColor = null;
   private Map chatFormats = Collections.synchronizedMap(new HashMap());
   private List itemSpawnBl = new ArrayList();
   private List enabledSigns = new ArrayList();
   private boolean signsEnabled = false;
   private boolean warnOnBuildDisallow;
   private boolean debug = false;
   private boolean configDebug = false;
   private static final double MAXMONEY = 1.0E13;
   private static final double MINMONEY = -1.0E13;
   private boolean changeDisplayName = true;
   private boolean changePlayerListName = false;
   private boolean prefixsuffixconfigured = false;
   private boolean addprefixsuffix = false;
   private boolean essentialsChatActive = false;
   private boolean disablePrefix = false;
   private boolean disableSuffix = false;
   private boolean getFreezeAfkPlayers;
   private boolean cancelAfkOnMove;
   private boolean cancelAfkOnInteract;
   private Set noGodWorlds = new HashSet();
   private boolean registerBackInListener;
   private boolean disableItemPickupWhileAfk;
   private long teleportInvulnerabilityTime;
   private boolean teleportInvulnerability;
   private long loginAttackDelay;
   private int signUsePerSecond;
   private int mailsPerMinute;

   public Settings(IEssentials ess) {
      this.ess = ess;
      this.config = new EssentialsConf(new File(ess.getDataFolder(), "config.yml"));
      this.config.setTemplateName("/config.yml");
      this.reloadConfig();
   }

   public boolean getRespawnAtHome() {
      return this.config.getBoolean("respawn-at-home", false);
   }

   public boolean getUpdateBedAtDaytime() {
      return this.config.getBoolean("update-bed-at-daytime", true);
   }

   public Set getMultipleHomes() {
      return this.config.getConfigurationSection("sethome-multiple").getKeys(false);
   }

   public int getHomeLimit(User user) {
      int limit = 1;
      if (user.isAuthorized("essentials.sethome.multiple")) {
         limit = this.getHomeLimit("default");
      }

      Set<String> homeList = this.getMultipleHomes();
      if (homeList != null) {
         for(String set : homeList) {
            if (user.isAuthorized("essentials.sethome.multiple." + set) && limit < this.getHomeLimit(set)) {
               limit = this.getHomeLimit(set);
            }
         }
      }

      return limit;
   }

   public int getHomeLimit(String set) {
      return this.config.getInt("sethome-multiple." + set, this.config.getInt("sethome-multiple.default", 3));
   }

   private int _getChatRadius() {
      return this.config.getInt("chat.radius", this.config.getInt("chat-radius", 0));
   }

   public int getChatRadius() {
      return this.chatRadius;
   }

   public double getTeleportDelay() {
      return this.config.getDouble("teleport-delay", (double)0.0F);
   }

   public int getOversizedStackSize() {
      return this.config.getInt("oversized-stacksize", 64);
   }

   public int getDefaultStackSize() {
      return this.config.getInt("default-stack-size", -1);
   }

   public int getStartingBalance() {
      return this.config.getInt("starting-balance", 0);
   }

   public boolean isCommandDisabled(IEssentialsCommand cmd) {
      return this.isCommandDisabled(cmd.getName());
   }

   public boolean isCommandDisabled(String label) {
      return this.disabledCommands.contains(label);
   }

   private Set getDisabledCommands() {
      Set<String> disCommands = new HashSet();

      for(String c : this.config.getStringList("disabled-commands")) {
         disCommands.add(c.toLowerCase(Locale.ENGLISH));
      }

      for(String c : this.config.getKeys(false)) {
         if (c.startsWith("disable-")) {
            disCommands.add(c.substring(8).toLowerCase(Locale.ENGLISH));
         }
      }

      return disCommands;
   }

   public boolean isPlayerCommand(String label) {
      for(String c : this.config.getStringList("player-commands")) {
         if (c.equalsIgnoreCase(label)) {
            return true;
         }
      }

      return false;
   }

   public boolean isCommandOverridden(String name) {
      for(String c : this.config.getStringList("overridden-commands")) {
         if (c.equalsIgnoreCase(name)) {
            return true;
         }
      }

      return this.config.getBoolean("override-" + name.toLowerCase(Locale.ENGLISH), false);
   }

   public double getCommandCost(IEssentialsCommand cmd) {
      return this.getCommandCost(cmd.getName());
   }

   public ConfigurationSection _getCommandCosts() {
      if (this.config.isConfigurationSection("command-costs")) {
         ConfigurationSection section = this.config.getConfigurationSection("command-costs");
         ConfigurationSection newSection = new MemoryConfiguration();

         for(String command : section.getKeys(false)) {
            PluginCommand cmd = this.ess.getServer().getPluginCommand(command);
            if (command.charAt(0) == '/') {
               this.ess.getLogger().warning("Invalid command cost. '" + command + "' should not start with '/'.");
            }

            if (section.isDouble(command)) {
               newSection.set(command.toLowerCase(Locale.ENGLISH), section.getDouble(command));
            } else if (section.isInt(command)) {
               newSection.set(command.toLowerCase(Locale.ENGLISH), (double)section.getInt(command));
            } else if (section.isString(command)) {
               String costString = section.getString(command);

               try {
                  double cost = Double.parseDouble(costString.trim().replace(this.getCurrencySymbol(), "").replaceAll("\\W", ""));
                  newSection.set(command.toLowerCase(Locale.ENGLISH), cost);
               } catch (NumberFormatException var9) {
                  this.ess.getLogger().warning("Invalid command cost for: " + command + " (" + costString + ")");
               }
            } else {
               this.ess.getLogger().warning("Invalid command cost for: " + command);
            }
         }

         return newSection;
      } else {
         return null;
      }
   }

   public double getCommandCost(String name) {
      name = name.replace('.', '_').replace('/', '_');
      return this.commandCosts != null ? this.commandCosts.getDouble(name, (double)0.0F) : (double)0.0F;
   }

   public Set _getSocialSpyCommands() {
      Set<String> socialspyCommands = new HashSet();
      if (this.config.isList("socialspy-commands")) {
         for(String c : this.config.getStringList("socialspy-commands")) {
            socialspyCommands.add(c.toLowerCase(Locale.ENGLISH));
         }
      } else {
         socialspyCommands.addAll(Arrays.asList("msg", "r", "mail", "m", "whisper", "emsg", "t", "tell", "er", "reply", "ereply", "email", "action", "describe", "eme", "eaction", "edescribe", "etell", "ewhisper", "pm"));
      }

      return socialspyCommands;
   }

   public Set getSocialSpyCommands() {
      return this.socialSpyCommands;
   }

   private String _getNicknamePrefix() {
      return this.config.getString("nickname-prefix", "~");
   }

   public String getNicknamePrefix() {
      return this.nicknamePrefix;
   }

   public double getTeleportCooldown() {
      return this.config.getDouble("teleport-cooldown", (double)0.0F);
   }

   public double getHealCooldown() {
      return this.config.getDouble("heal-cooldown", (double)0.0F);
   }

   public ConfigurationSection _getKits() {
      if (this.config.isConfigurationSection("kits")) {
         ConfigurationSection section = this.config.getConfigurationSection("kits");
         ConfigurationSection newSection = new MemoryConfiguration();

         for(String kitItem : section.getKeys(false)) {
            if (section.isConfigurationSection(kitItem)) {
               newSection.set(kitItem.toLowerCase(Locale.ENGLISH), section.getConfigurationSection(kitItem));
            }
         }

         return newSection;
      } else {
         return null;
      }
   }

   public ConfigurationSection getKits() {
      return this.kits;
   }

   public Map getKit(String name) {
      name = name.replace('.', '_').replace('/', '_');
      if (this.getKits() != null) {
         ConfigurationSection kits = this.getKits();
         if (kits.isConfigurationSection(name)) {
            return kits.getConfigurationSection(name).getValues(true);
         }
      }

      return null;
   }

   public ChatColor getOperatorColor() {
      return this.operatorColor;
   }

   private ChatColor _getOperatorColor() {
      String colorName = this.config.getString("ops-name-color", (String)null);
      if (colorName == null) {
         return ChatColor.DARK_RED;
      } else if (!"none".equalsIgnoreCase(colorName) && !colorName.isEmpty()) {
         try {
            return ChatColor.valueOf(colorName.toUpperCase(Locale.ENGLISH));
         } catch (IllegalArgumentException var3) {
            return ChatColor.getByChar(colorName);
         }
      } else {
         return null;
      }
   }

   public int getSpawnMobLimit() {
      return this.config.getInt("spawnmob-limit", 10);
   }

   public boolean showNonEssCommandsInHelp() {
      return this.config.getBoolean("non-ess-in-help", true);
   }

   public boolean hidePermissionlessHelp() {
      return this.config.getBoolean("hide-permissionless-help", true);
   }

   public int getProtectCreeperMaxHeight() {
      return this.config.getInt("protect.creeper.max-height", -1);
   }

   public boolean areSignsDisabled() {
      return !this.signsEnabled;
   }

   public long getBackupInterval() {
      return (long)this.config.getInt("backup.interval", 1440);
   }

   public String getBackupCommand() {
      return this.config.getString("backup.command", (String)null);
   }

   public MessageFormat getChatFormat(String group) {
      MessageFormat mFormat = (MessageFormat)this.chatFormats.get(group);
      if (mFormat == null) {
         String format = this.config.getString("chat.group-formats." + (group == null ? "Default" : group), this.config.getString("chat.format", "&7[{GROUP}]&f {DISPLAYNAME}&7:&f {MESSAGE}"));
         format = Util.replaceFormat(format);
         format = format.replace("{DISPLAYNAME}", "%1$s");
         format = format.replace("{GROUP}", "{0}");
         format = format.replace("{MESSAGE}", "%2$s");
         format = format.replace("{WORLDNAME}", "{1}");
         format = format.replace("{SHORTWORLDNAME}", "{2}");
         format = format.replaceAll("\\{(\\D*?)\\}", "\\[$1\\]");
         format = "§r".concat(format);
         mFormat = new MessageFormat(format);
         this.chatFormats.put(group, mFormat);
      }

      return mFormat;
   }

   public boolean getAnnounceNewPlayers() {
      return !this.config.getString("newbies.announce-format", "-").isEmpty();
   }

   public IText getAnnounceNewPlayerFormat() {
      return new SimpleTextInput(Util.replaceFormat(this.config.getString("newbies.announce-format", "&dWelcome {DISPLAYNAME} to the server!")));
   }

   public String getNewPlayerKit() {
      return this.config.getString("newbies.kit", "");
   }

   public String getNewbieSpawn() {
      return this.config.getString("newbies.spawnpoint", "default");
   }

   public boolean getPerWarpPermission() {
      return this.config.getBoolean("per-warp-permission", false);
   }

   public Map getListGroupConfig() {
      if (this.config.isConfigurationSection("list")) {
         Map<String, Object> values = this.config.getConfigurationSection("list").getValues(false);
         if (!values.isEmpty()) {
            return values;
         }
      }

      Map<String, Object> defaultMap = new HashMap();
      if (this.config.getBoolean("sort-list-by-groups", false)) {
         defaultMap.put("ListByGroup", "ListByGroup");
      } else {
         defaultMap.put("Players", "*");
      }

      return defaultMap;
   }

   public void reloadConfig() {
      this.config.load();
      this.noGodWorlds = new HashSet(this.config.getStringList("no-god-in-worlds"));
      this.enabledSigns = this._getEnabledSigns();
      this.teleportInvulnerabilityTime = this._getTeleportInvulnerability();
      this.teleportInvulnerability = this._isTeleportInvulnerability();
      this.disableItemPickupWhileAfk = this._getDisableItemPickupWhileAfk();
      this.registerBackInListener = this._registerBackInListener();
      this.cancelAfkOnInteract = this._cancelAfkOnInteract();
      this.cancelAfkOnMove = this._cancelAfkOnMove() && this.cancelAfkOnInteract;
      this.getFreezeAfkPlayers = this._getFreezeAfkPlayers();
      this.itemSpawnBl = this._getItemSpawnBlacklist();
      this.loginAttackDelay = this._getLoginAttackDelay();
      this.signUsePerSecond = this._getSignUsePerSecond();
      this.kits = this._getKits();
      this.chatFormats.clear();
      this.changeDisplayName = this._changeDisplayName();
      this.disabledCommands = this.getDisabledCommands();
      this.nicknamePrefix = this._getNicknamePrefix();
      this.operatorColor = this._getOperatorColor();
      this.changePlayerListName = this._changePlayerListName();
      this.configDebug = this._isDebug();
      this.prefixsuffixconfigured = this._isPrefixSuffixConfigured();
      this.addprefixsuffix = this._addPrefixSuffix();
      this.disablePrefix = this._disablePrefix();
      this.disableSuffix = this._disableSuffix();
      this.chatRadius = this._getChatRadius();
      this.commandCosts = this._getCommandCosts();
      this.socialSpyCommands = this._getSocialSpyCommands();
      this.warnOnBuildDisallow = this._warnOnBuildDisallow();
      this.mailsPerMinute = this._getMailsPerMinute();
   }

   public List itemSpawnBlacklist() {
      return this.itemSpawnBl;
   }

   private List _getItemSpawnBlacklist() {
      List<Integer> epItemSpwn = new ArrayList();
      if (this.ess.getItemDb() == null) {
         logger.log(Level.FINE, "Aborting ItemSpawnBL read, itemDB not yet loaded.");
         return epItemSpwn;
      } else {
         for(String itemName : this.config.getString("item-spawn-blacklist", "").split(",")) {
            itemName = itemName.trim();
            if (!itemName.isEmpty()) {
               try {
                  ItemStack iStack = this.ess.getItemDb().get(itemName);
                  epItemSpwn.add(iStack.getTypeId());
               } catch (Exception var7) {
                  logger.log(Level.SEVERE, I18n._("unknownItemInList", itemName, "item-spawn-blacklist"));
               }
            }
         }

         return epItemSpwn;
      }
   }

   public List enabledSigns() {
      return this.enabledSigns;
   }

   private List _getEnabledSigns() {
      List<EssentialsSign> newSigns = new ArrayList();

      for(String signName : this.config.getStringList("enabledSigns")) {
         signName = signName.trim().toUpperCase(Locale.ENGLISH);
         if (!signName.isEmpty()) {
            if (!signName.equals("COLOR") && !signName.equals("COLOUR")) {
               try {
                  newSigns.add(Signs.valueOf(signName).getSign());
               } catch (Exception var5) {
                  logger.log(Level.SEVERE, I18n._("unknownItemInList", signName, "enabledSigns"));
                  continue;
               }

               this.signsEnabled = true;
            } else {
               this.signsEnabled = true;
            }
         }
      }

      return newSigns;
   }

   private boolean _warnOnBuildDisallow() {
      return this.config.getBoolean("protect.disable.warn-on-build-disallow", false);
   }

   public boolean warnOnBuildDisallow() {
      return this.warnOnBuildDisallow;
   }

   private boolean _isDebug() {
      return this.config.getBoolean("debug", false);
   }

   public boolean isDebug() {
      return this.debug || this.configDebug;
   }

   public boolean warnOnSmite() {
      return this.config.getBoolean("warn-on-smite", true);
   }

   public boolean permissionBasedItemSpawn() {
      return this.config.getBoolean("permission-based-item-spawn", false);
   }

   public String getLocale() {
      return this.config.getString("locale", "");
   }

   public String getCurrencySymbol() {
      return this.config.getString("currency-symbol", "$").concat("$").substring(0, 1).replaceAll("[0-9]", "$");
   }

   public boolean isTradeInStacks(int id) {
      return this.config.getBoolean("trade-in-stacks-" + id, false);
   }

   public boolean isEcoDisabled() {
      return this.config.getBoolean("disable-eco", false);
   }

   public boolean getProtectPreventSpawn(String creatureName) {
      return this.config.getBoolean("protect.prevent.spawn." + creatureName, false);
   }

   public List getProtectList(String configName) {
      List<Integer> list = new ArrayList();

      for(String itemName : this.config.getString(configName, "").split(",")) {
         itemName = itemName.trim();
         if (!itemName.isEmpty()) {
            try {
               ItemStack itemStack = this.ess.getItemDb().get(itemName);
               list.add(itemStack.getTypeId());
            } catch (Exception var9) {
               logger.log(Level.SEVERE, I18n._("unknownItemInList", itemName, configName));
            }
         }
      }

      return list;
   }

   public String getProtectString(String configName) {
      return this.config.getString(configName, (String)null);
   }

   public boolean getProtectBoolean(String configName, boolean def) {
      return this.config.getBoolean(configName, def);
   }

   public double getMaxMoney() {
      double max = this.config.getDouble("max-money", 1.0E13);
      if (Math.abs(max) > 1.0E13) {
         max = max < (double)0.0F ? -1.0E13 : 1.0E13;
      }

      return max;
   }

   public double getMinMoney() {
      double min = this.config.getDouble("min-money", -1.0E13);
      if (min > (double)0.0F) {
         min = -min;
      }

      if (min < -1.0E13) {
         min = -1.0E13;
      }

      return min;
   }

   public boolean isEcoLogEnabled() {
      return this.config.getBoolean("economy-log-enabled", false);
   }

   public boolean isEcoLogUpdateEnabled() {
      return this.config.getBoolean("economy-log-update-enabled", false);
   }

   public boolean removeGodOnDisconnect() {
      return this.config.getBoolean("remove-god-on-disconnect", false);
   }

   private boolean _changeDisplayName() {
      return this.config.getBoolean("change-displayname", true);
   }

   public boolean changeDisplayName() {
      return this.changeDisplayName;
   }

   private boolean _changePlayerListName() {
      return this.config.getBoolean("change-playerlist", false);
   }

   public boolean changePlayerListName() {
      return this.changePlayerListName;
   }

   public boolean useBukkitPermissions() {
      return this.config.getBoolean("use-bukkit-permissions", false);
   }

   private boolean _addPrefixSuffix() {
      return this.config.getBoolean("add-prefix-suffix", false);
   }

   private boolean _isPrefixSuffixConfigured() {
      return this.config.hasProperty("add-prefix-suffix");
   }

   public void setEssentialsChatActive(boolean essentialsChatActive) {
      this.essentialsChatActive = essentialsChatActive;
   }

   public boolean addPrefixSuffix() {
      return this.prefixsuffixconfigured ? this.addprefixsuffix : this.essentialsChatActive;
   }

   private boolean _disablePrefix() {
      return this.config.getBoolean("disablePrefix", false);
   }

   public boolean disablePrefix() {
      return this.disablePrefix;
   }

   private boolean _disableSuffix() {
      return this.config.getBoolean("disableSuffix", false);
   }

   public boolean disableSuffix() {
      return this.disableSuffix;
   }

   public long getAutoAfk() {
      return this.config.getLong("auto-afk", 300L);
   }

   public long getAutoAfkKick() {
      return this.config.getLong("auto-afk-kick", -1L);
   }

   public boolean getFreezeAfkPlayers() {
      return this.getFreezeAfkPlayers;
   }

   private boolean _getFreezeAfkPlayers() {
      return this.config.getBoolean("freeze-afk-players", false);
   }

   public boolean cancelAfkOnMove() {
      return this.cancelAfkOnMove;
   }

   private boolean _cancelAfkOnMove() {
      return this.config.getBoolean("cancel-afk-on-move", true);
   }

   public boolean cancelAfkOnInteract() {
      return this.cancelAfkOnInteract;
   }

   private boolean _cancelAfkOnInteract() {
      return this.config.getBoolean("cancel-afk-on-interact", true);
   }

   public boolean areDeathMessagesEnabled() {
      return this.config.getBoolean("death-messages", true);
   }

   public Set getNoGodWorlds() {
      return this.noGodWorlds;
   }

   public void setDebug(boolean debug) {
      this.debug = debug;
   }

   public boolean getRepairEnchanted() {
      return this.config.getBoolean("repair-enchanted", true);
   }

   public boolean allowUnsafeEnchantments() {
      return this.config.getBoolean("unsafe-enchantments", false);
   }

   public boolean isWorldTeleportPermissions() {
      return this.config.getBoolean("world-teleport-permissions", false);
   }

   public boolean isWorldHomePermissions() {
      return this.config.getBoolean("world-home-permissions", false);
   }

   public boolean registerBackInListener() {
      return this.registerBackInListener;
   }

   private boolean _registerBackInListener() {
      return this.config.getBoolean("register-back-in-listener", false);
   }

   public boolean getDisableItemPickupWhileAfk() {
      return this.disableItemPickupWhileAfk;
   }

   private boolean _getDisableItemPickupWhileAfk() {
      return this.config.getBoolean("disable-item-pickup-while-afk", false);
   }

   public EventPriority getRespawnPriority() {
      String priority = this.config.getString("respawn-listener-priority", "normal").toLowerCase(Locale.ENGLISH);
      if ("lowest".equals(priority)) {
         return EventPriority.LOWEST;
      } else if ("low".equals(priority)) {
         return EventPriority.LOW;
      } else if ("normal".equals(priority)) {
         return EventPriority.NORMAL;
      } else if ("high".equals(priority)) {
         return EventPriority.HIGH;
      } else {
         return "highest".equals(priority) ? EventPriority.HIGHEST : EventPriority.NORMAL;
      }
   }

   public long getTpaAcceptCancellation() {
      return this.config.getLong("tpa-accept-cancellation", 120L);
   }

   public boolean isMetricsEnabled() {
      return this.metricsEnabled;
   }

   public void setMetricsEnabled(boolean metricsEnabled) {
      this.metricsEnabled = metricsEnabled;
   }

   public long _getTeleportInvulnerability() {
      return this.config.getLong("teleport-invulnerability", 0L) * 1000L;
   }

   public long getTeleportInvulnerability() {
      return this.teleportInvulnerabilityTime;
   }

   private boolean _isTeleportInvulnerability() {
      return this.config.getLong("teleport-invulnerability", 0L) > 0L;
   }

   public boolean isTeleportInvulnerability() {
      return this.teleportInvulnerability;
   }

   private long _getLoginAttackDelay() {
      return this.config.getLong("login-attack-delay", 0L) * 1000L;
   }

   public long getLoginAttackDelay() {
      return this.loginAttackDelay;
   }

   private int _getSignUsePerSecond() {
      int perSec = this.config.getInt("sign-use-per-second", 4);
      return perSec > 0 ? perSec : 1;
   }

   public int getSignUsePerSecond() {
      return this.signUsePerSecond;
   }

   public double getMaxFlySpeed() {
      double maxSpeed = this.config.getDouble("max-fly-speed", 0.8);
      return maxSpeed > (double)1.0F ? (double)1.0F : Math.abs(maxSpeed);
   }

   public double getMaxWalkSpeed() {
      double maxSpeed = this.config.getDouble("max-walk-speed", 0.8);
      return maxSpeed > (double)1.0F ? (double)1.0F : Math.abs(maxSpeed);
   }

   private int _getMailsPerMinute() {
      return this.config.getInt("mails-per-minute", 1000);
   }

   public int getMailsPerMinute() {
      return this.mailsPerMinute;
   }

   public long getMaxTempban() {
      return this.config.getLong("max-tempban-time", -1L);
   }
}
