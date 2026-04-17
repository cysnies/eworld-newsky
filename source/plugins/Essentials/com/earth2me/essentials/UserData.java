package com.earth2me.essentials;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class UserData extends PlayerExtension implements IConf {
   protected final transient IEssentials ess;
   private EssentialsConf config;
   private final File folder;
   private double money;
   private Map homes;
   private String nickname;
   private List unlimited;
   private Map powertools;
   private Location lastLocation;
   private Location logoutLocation;
   private long lastTeleportTimestamp;
   private long lastHealTimestamp;
   private String jail;
   private List mails;
   private boolean teleportEnabled;
   private List ignoredPlayers;
   private boolean godmode;
   private boolean muted;
   private long muteTimeout;
   private boolean jailed;
   private long jailTimeout;
   private long lastLogin;
   private long lastLogout;
   private String lastLoginAddress;
   private boolean afk;
   private boolean newplayer;
   private String geolocation;
   private boolean isSocialSpyEnabled;
   private boolean isNPC;
   private boolean arePowerToolsEnabled;
   private Map kitTimestamps;

   protected UserData(Player base, IEssentials ess) {
      super(base);
      this.ess = ess;
      this.folder = new File(ess.getDataFolder(), "userdata");
      if (!this.folder.exists()) {
         this.folder.mkdirs();
      }

      this.config = new EssentialsConf(new File(this.folder, Util.sanitizeFileName(base.getName()) + ".yml"));
      this.reloadConfig();
   }

   public final void reset() {
      this.config.getFile().delete();
      this.config = new EssentialsConf(new File(this.folder, Util.sanitizeFileName(this.base.getName()) + ".yml"));
      this.reloadConfig();
   }

   public final void reloadConfig() {
      this.config.load();
      this.money = this._getMoney();
      this.unlimited = this._getUnlimited();
      this.powertools = this._getPowertools();
      this.homes = this._getHomes();
      this.lastLocation = this._getLastLocation();
      this.lastTeleportTimestamp = this._getLastTeleportTimestamp();
      this.lastHealTimestamp = this._getLastHealTimestamp();
      this.jail = this._getJail();
      this.mails = this._getMails();
      this.teleportEnabled = this._getTeleportEnabled();
      this.godmode = this._getGodModeEnabled();
      this.muted = this._getMuted();
      this.muteTimeout = this._getMuteTimeout();
      this.jailed = this._getJailed();
      this.jailTimeout = this._getJailTimeout();
      this.lastLogin = this._getLastLogin();
      this.lastLogout = this._getLastLogout();
      this.lastLoginAddress = this._getLastLoginAddress();
      this.afk = this._getAfk();
      this.geolocation = this._getGeoLocation();
      this.isSocialSpyEnabled = this._isSocialSpyEnabled();
      this.isNPC = this._isNPC();
      this.arePowerToolsEnabled = this._arePowerToolsEnabled();
      this.kitTimestamps = this._getKitTimestamps();
      this.nickname = this._getNickname();
      this.setIgnoredPlayers(this._getIgnoredPlayers());
      this.logoutLocation = this._getLogoutLocation();
   }

   private double _getMoney() {
      double money = (double)this.ess.getSettings().getStartingBalance();
      if (this.config.hasProperty("money")) {
         money = this.config.getDouble("money", money);
      }

      if (Math.abs(money) > this.ess.getSettings().getMaxMoney()) {
         money = money < (double)0.0F ? -this.ess.getSettings().getMaxMoney() : this.ess.getSettings().getMaxMoney();
      }

      return money;
   }

   public double getMoney() {
      return this.money;
   }

   public void setMoney(double value) {
      this.money = value;
      if (Math.abs(this.money) > this.ess.getSettings().getMaxMoney()) {
         this.money = this.money < (double)0.0F ? -this.ess.getSettings().getMaxMoney() : this.ess.getSettings().getMaxMoney();
      }

      this.config.setProperty("money", (Object)value);
      this.config.save();
   }

   private Map _getHomes() {
      return (Map)(this.config.isConfigurationSection("homes") ? this.config.getConfigurationSection("homes").getValues(false) : new HashMap());
   }

   private String getHomeName(String search) {
      if (Util.isInt(search)) {
         try {
            search = (String)this.getHomes().get(Integer.parseInt(search) - 1);
         } catch (Exception var3) {
         }
      }

      return search;
   }

   public Location getHome(String name) throws Exception {
      String search = this.getHomeName(name);
      return this.config.getLocation("homes." + search, this.getServer());
   }

   public Location getHome(Location world) {
      try {
         for(String home : this.getHomes()) {
            Location loc = this.config.getLocation("homes." + home, this.getServer());
            if (world.getWorld() == loc.getWorld()) {
               return loc;
            }
         }

         Location loc = this.config.getLocation("homes." + (String)this.getHomes().get(0), this.getServer());
         return loc;
      } catch (Exception var5) {
         return null;
      }
   }

   public List getHomes() {
      return new ArrayList(this.homes.keySet());
   }

   public void setHome(String name, Location loc) {
      name = Util.safeString(name);
      this.homes.put(name, loc);
      this.config.setProperty("homes." + name, loc);
      this.config.save();
   }

   public void delHome(String name) throws Exception {
      String search = this.getHomeName(name);
      if (!this.homes.containsKey(search)) {
         search = Util.safeString(search);
      }

      if (this.homes.containsKey(search)) {
         this.homes.remove(search);
         this.config.removeProperty("homes." + search);
         this.config.save();
      } else {
         throw new Exception(I18n._("invalidHome", search));
      }
   }

   public boolean hasHome() {
      return this.config.hasProperty("home");
   }

   public String _getNickname() {
      return this.config.getString("nickname");
   }

   public String getNickname() {
      return this.nickname;
   }

   public void setNickname(String nick) {
      this.nickname = nick;
      this.config.setProperty("nickname", (Object)nick);
      this.config.save();
   }

   private List _getUnlimited() {
      return this.config.getIntegerList("unlimited");
   }

   public List getUnlimited() {
      return this.unlimited;
   }

   public boolean hasUnlimited(ItemStack stack) {
      return this.unlimited.contains(stack.getTypeId());
   }

   public void setUnlimited(ItemStack stack, boolean state) {
      if (this.unlimited.contains(stack.getTypeId())) {
         this.unlimited.remove(stack.getTypeId());
      }

      if (state) {
         this.unlimited.add(stack.getTypeId());
      }

      this.config.setProperty("unlimited", this.unlimited);
      this.config.save();
   }

   private Map _getPowertools() {
      return (Map)(this.config.isConfigurationSection("powertools") ? this.config.getConfigurationSection("powertools").getValues(false) : new HashMap());
   }

   public void clearAllPowertools() {
      this.powertools.clear();
      this.config.setProperty("powertools", this.powertools);
      this.config.save();
   }

   public List getPowertool(ItemStack stack) {
      return (List)this.powertools.get("" + stack.getTypeId());
   }

   public List getPowertool(int id) {
      return (List)this.powertools.get("" + id);
   }

   public void setPowertool(ItemStack stack, List commandList) {
      if (commandList != null && !commandList.isEmpty()) {
         this.powertools.put("" + stack.getTypeId(), commandList);
      } else {
         this.powertools.remove("" + stack.getTypeId());
      }

      this.config.setProperty("powertools", this.powertools);
      this.config.save();
   }

   public boolean hasPowerTools() {
      return !this.powertools.isEmpty();
   }

   private Location _getLastLocation() {
      try {
         return this.config.getLocation("lastlocation", this.getServer());
      } catch (Exception var2) {
         return null;
      }
   }

   public Location getLastLocation() {
      return this.lastLocation;
   }

   public void setLastLocation(Location loc) {
      if (loc != null && loc.getWorld() != null) {
         this.lastLocation = loc;
         this.config.setProperty("lastlocation", loc);
         this.config.save();
      }
   }

   private Location _getLogoutLocation() {
      try {
         return this.config.getLocation("logoutlocation", this.getServer());
      } catch (Exception var2) {
         return null;
      }
   }

   public Location getLogoutLocation() {
      return this.logoutLocation;
   }

   public void setLogoutLocation(Location loc) {
      if (loc != null && loc.getWorld() != null) {
         this.logoutLocation = loc;
         this.config.setProperty("logoutlocation", loc);
         this.config.save();
      }
   }

   private long _getLastTeleportTimestamp() {
      return this.config.getLong("timestamps.lastteleport", 0L);
   }

   public long getLastTeleportTimestamp() {
      return this.lastTeleportTimestamp;
   }

   public void setLastTeleportTimestamp(long time) {
      this.lastTeleportTimestamp = time;
      this.config.setProperty("timestamps.lastteleport", (Object)time);
      this.config.save();
   }

   private long _getLastHealTimestamp() {
      return this.config.getLong("timestamps.lastheal", 0L);
   }

   public long getLastHealTimestamp() {
      return this.lastHealTimestamp;
   }

   public void setLastHealTimestamp(long time) {
      this.lastHealTimestamp = time;
      this.config.setProperty("timestamps.lastheal", (Object)time);
      this.config.save();
   }

   private String _getJail() {
      return this.config.getString("jail");
   }

   public String getJail() {
      return this.jail;
   }

   public void setJail(String jail) {
      if (jail != null && !jail.isEmpty()) {
         this.jail = jail;
         this.config.setProperty("jail", (Object)jail);
      } else {
         this.jail = null;
         this.config.removeProperty("jail");
      }

      this.config.save();
   }

   private List _getMails() {
      return this.config.getStringList("mail");
   }

   public List getMails() {
      return this.mails;
   }

   public void setMails(List mails) {
      if (mails == null) {
         this.config.removeProperty("mail");
         mails = this._getMails();
      } else {
         this.config.setProperty("mail", mails);
      }

      this.mails = mails;
      this.config.save();
   }

   public void addMail(String mail) {
      this.mails.add(mail);
      this.setMails(this.mails);
   }

   private boolean _getTeleportEnabled() {
      return this.config.getBoolean("teleportenabled", true);
   }

   public boolean isTeleportEnabled() {
      return this.teleportEnabled;
   }

   public void setTeleportEnabled(boolean set) {
      this.teleportEnabled = set;
      this.config.setProperty("teleportenabled", (Object)set);
      this.config.save();
   }

   public boolean toggleTeleportEnabled() {
      boolean ret = !this.isTeleportEnabled();
      this.setTeleportEnabled(ret);
      return ret;
   }

   public boolean toggleSocialSpy() {
      boolean ret = !this.isSocialSpyEnabled();
      this.setSocialSpyEnabled(ret);
      return ret;
   }

   public List _getIgnoredPlayers() {
      return Collections.synchronizedList(this.config.getStringList("ignore"));
   }

   public void setIgnoredPlayers(List players) {
      if (players != null && !players.isEmpty()) {
         this.ignoredPlayers = players;
         this.config.setProperty("ignore", players);
      } else {
         this.ignoredPlayers = Collections.synchronizedList(new ArrayList());
         this.config.removeProperty("ignore");
      }

      this.config.save();
   }

   /** @deprecated */
   @Deprecated
   public boolean isIgnoredPlayer(String userName) {
      IUser user = this.ess.getUser(userName);
      return user != null && user.isOnline() ? this.isIgnoredPlayer(user) : false;
   }

   public boolean isIgnoredPlayer(IUser user) {
      return this.ignoredPlayers.contains(user.getName().toLowerCase(Locale.ENGLISH)) && !user.isIgnoreExempt();
   }

   public void setIgnoredPlayer(IUser user, boolean set) {
      if (set) {
         this.ignoredPlayers.add(user.getName().toLowerCase(Locale.ENGLISH));
      } else {
         this.ignoredPlayers.remove(user.getName().toLowerCase(Locale.ENGLISH));
      }

      this.setIgnoredPlayers(this.ignoredPlayers);
   }

   private boolean _getGodModeEnabled() {
      return this.config.getBoolean("godmode", false);
   }

   public boolean isGodModeEnabled() {
      return this.godmode;
   }

   public void setGodModeEnabled(boolean set) {
      this.godmode = set;
      this.config.setProperty("godmode", (Object)set);
      this.config.save();
   }

   public boolean _getMuted() {
      return this.config.getBoolean("muted", false);
   }

   public boolean getMuted() {
      return this.muted;
   }

   public boolean isMuted() {
      return this.muted;
   }

   public void setMuted(boolean set) {
      this.muted = set;
      this.config.setProperty("muted", (Object)set);
      this.config.save();
   }

   private long _getMuteTimeout() {
      return this.config.getLong("timestamps.mute", 0L);
   }

   public long getMuteTimeout() {
      return this.muteTimeout;
   }

   public void setMuteTimeout(long time) {
      this.muteTimeout = time;
      this.config.setProperty("timestamps.mute", (Object)time);
      this.config.save();
   }

   private boolean _getJailed() {
      return this.config.getBoolean("jailed", false);
   }

   public boolean isJailed() {
      return this.jailed;
   }

   public void setJailed(boolean set) {
      this.jailed = set;
      this.config.setProperty("jailed", (Object)set);
      this.config.save();
   }

   public boolean toggleJailed() {
      boolean ret = !this.isJailed();
      this.setJailed(ret);
      return ret;
   }

   private long _getJailTimeout() {
      return this.config.getLong("timestamps.jail", 0L);
   }

   public long getJailTimeout() {
      return this.jailTimeout;
   }

   public void setJailTimeout(long time) {
      this.jailTimeout = time;
      this.config.setProperty("timestamps.jail", (Object)time);
      this.config.save();
   }

   public String getBanReason() {
      return this.config.getString("ban.reason", "");
   }

   public void setBanReason(String reason) {
      this.config.setProperty("ban.reason", (Object)Util.sanitizeString(reason));
      this.config.save();
   }

   public long getBanTimeout() {
      return this.config.getLong("ban.timeout", 0L);
   }

   public void setBanTimeout(long time) {
      this.config.setProperty("ban.timeout", (Object)time);
      this.config.save();
   }

   private long _getLastLogin() {
      return this.config.getLong("timestamps.login", 0L);
   }

   public long getLastLogin() {
      return this.lastLogin;
   }

   private void _setLastLogin(long time) {
      this.lastLogin = time;
      this.config.setProperty("timestamps.login", (Object)time);
   }

   public void setLastLogin(long time) {
      this._setLastLogin(time);
      if (this.base.getAddress() != null && this.base.getAddress().getAddress() != null) {
         this._setLastLoginAddress(this.base.getAddress().getAddress().getHostAddress());
      }

      this.config.save();
   }

   private long _getLastLogout() {
      return this.config.getLong("timestamps.logout", 0L);
   }

   public long getLastLogout() {
      return this.lastLogout;
   }

   public void setLastLogout(long time) {
      this.lastLogout = time;
      this.config.setProperty("timestamps.logout", (Object)time);
      this.config.save();
   }

   private String _getLastLoginAddress() {
      return this.config.getString("ipAddress", "");
   }

   public String getLastLoginAddress() {
      return this.lastLoginAddress;
   }

   private void _setLastLoginAddress(String address) {
      this.lastLoginAddress = address;
      this.config.setProperty("ipAddress", (Object)address);
   }

   private boolean _getAfk() {
      return this.config.getBoolean("afk", false);
   }

   public boolean isAfk() {
      return this.afk;
   }

   public void setAfk(boolean set) {
      this.afk = set;
      this.config.setProperty("afk", (Object)set);
      this.config.save();
   }

   public boolean toggleAfk() {
      boolean ret = !this.isAfk();
      this.setAfk(ret);
      return ret;
   }

   private String _getGeoLocation() {
      return this.config.getString("geolocation");
   }

   public String getGeoLocation() {
      return this.geolocation;
   }

   public void setGeoLocation(String geolocation) {
      if (geolocation != null && !geolocation.isEmpty()) {
         this.geolocation = geolocation;
         this.config.setProperty("geolocation", (Object)geolocation);
      } else {
         this.geolocation = null;
         this.config.removeProperty("geolocation");
      }

      this.config.save();
   }

   private boolean _isSocialSpyEnabled() {
      return this.config.getBoolean("socialspy", false);
   }

   public boolean isSocialSpyEnabled() {
      return this.isSocialSpyEnabled;
   }

   public void setSocialSpyEnabled(boolean status) {
      this.isSocialSpyEnabled = status;
      this.config.setProperty("socialspy", (Object)status);
      this.config.save();
   }

   private boolean _isNPC() {
      return this.config.getBoolean("npc", false);
   }

   public boolean isNPC() {
      return this.isNPC;
   }

   public void setNPC(boolean set) {
      this.isNPC = set;
      this.config.setProperty("npc", (Object)set);
      this.config.save();
   }

   public boolean arePowerToolsEnabled() {
      return this.arePowerToolsEnabled;
   }

   public void setPowerToolsEnabled(boolean set) {
      this.arePowerToolsEnabled = set;
      this.config.setProperty("powertoolsenabled", (Object)set);
      this.config.save();
   }

   public boolean togglePowerToolsEnabled() {
      boolean ret = !this.arePowerToolsEnabled();
      this.setPowerToolsEnabled(ret);
      return ret;
   }

   private boolean _arePowerToolsEnabled() {
      return this.config.getBoolean("powertoolsenabled", true);
   }

   private Map _getKitTimestamps() {
      if (this.config.isConfigurationSection("timestamps.kits")) {
         ConfigurationSection section = this.config.getConfigurationSection("timestamps.kits");
         Map<String, Long> timestamps = new HashMap();

         for(String command : section.getKeys(false)) {
            if (section.isLong(command)) {
               timestamps.put(command.toLowerCase(Locale.ENGLISH), section.getLong(command));
            } else if (section.isInt(command)) {
               timestamps.put(command.toLowerCase(Locale.ENGLISH), (long)section.getInt(command));
            }
         }

         return timestamps;
      } else {
         return new HashMap();
      }
   }

   public long getKitTimestamp(String name) {
      name = name.replace('.', '_').replace('/', '_');
      return this.kitTimestamps != null && this.kitTimestamps.containsKey(name) ? (Long)this.kitTimestamps.get(name) : 0L;
   }

   public void setKitTimestamp(String name, long time) {
      this.kitTimestamps.put(name.toLowerCase(Locale.ENGLISH), time);
      this.config.setProperty("timestamps.kits", this.kitTimestamps);
      this.config.save();
   }

   public void setConfigProperty(String node, Object object) {
      String prefix = "info.";
      node = "info." + node;
      if (object instanceof Map) {
         this.config.setProperty(node, (Map)object);
      } else if (object instanceof List) {
         this.config.setProperty(node, (List)object);
      } else if (object instanceof Location) {
         this.config.setProperty(node, (Location)object);
      } else if (object instanceof ItemStack) {
         this.config.setProperty(node, (ItemStack)object);
      } else {
         this.config.setProperty(node, object);
      }

      this.config.save();
   }

   public Set getConfigKeys() {
      return (Set)(this.config.isConfigurationSection("info") ? this.config.getConfigurationSection("info").getKeys(true) : new HashSet());
   }

   public Map getConfigMap() {
      return (Map)(this.config.isConfigurationSection("info") ? this.config.getConfigurationSection("info").getValues(true) : new HashMap());
   }

   public Map getConfigMap(String node) {
      return (Map)(this.config.isConfigurationSection("info." + node) ? this.config.getConfigurationSection("info." + node).getValues(true) : new HashMap());
   }

   public void save() {
      this.config.save();
   }
}
