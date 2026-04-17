package fr.neatmonster.nocheatplus.players;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationHistory;
import fr.neatmonster.nocheatplus.checks.access.CheckDataFactory;
import fr.neatmonster.nocheatplus.checks.blockbreak.BlockBreakConfig;
import fr.neatmonster.nocheatplus.checks.blockinteract.BlockInteractConfig;
import fr.neatmonster.nocheatplus.checks.blockplace.BlockPlaceConfig;
import fr.neatmonster.nocheatplus.checks.chat.ChatConfig;
import fr.neatmonster.nocheatplus.checks.combined.CombinedConfig;
import fr.neatmonster.nocheatplus.checks.combined.CombinedData;
import fr.neatmonster.nocheatplus.checks.fight.FightConfig;
import fr.neatmonster.nocheatplus.checks.inventory.InventoryConfig;
import fr.neatmonster.nocheatplus.checks.moving.MovingConfig;
import fr.neatmonster.nocheatplus.components.ComponentRegistry;
import fr.neatmonster.nocheatplus.components.ComponentWithName;
import fr.neatmonster.nocheatplus.components.ConsistencyChecker;
import fr.neatmonster.nocheatplus.components.IHaveCheckType;
import fr.neatmonster.nocheatplus.components.INeedConfig;
import fr.neatmonster.nocheatplus.components.INotifyReload;
import fr.neatmonster.nocheatplus.components.IRemoveData;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.hooks.APIUtils;
import fr.neatmonster.nocheatplus.logging.LogUtil;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class DataManager implements Listener, INotifyReload, INeedConfig, ComponentRegistry, ComponentWithName, ConsistencyChecker {
   protected static DataManager instance = null;
   private int foundInconsistencies = 0;
   protected final Map playerData = new LinkedHashMap(100);
   private final Map lastLogout = new LinkedHashMap(50, 0.75F, true);
   protected final Map onlinePlayers = new LinkedHashMap(100);
   protected final ArrayList iRemoveData = new ArrayList();
   protected final Map executionHistories = new HashMap();
   protected boolean doExpireData = false;
   protected long durExpireData = 0L;
   protected boolean deleteData = true;
   protected boolean deleteHistory = false;

   public DataManager() {
      instance = this;
   }

   public void checkExpiration() {
      if (this.doExpireData && this.durExpireData > 0L) {
         long now = System.currentTimeMillis();
         Set<CheckDataFactory> factories = new LinkedHashSet();
         Set<Map.Entry<String, Long>> entries = this.lastLogout.entrySet();

         for(Iterator<Map.Entry<String, Long>> iterator = entries.iterator(); iterator.hasNext(); iterator.remove()) {
            Map.Entry<String, Long> entry = (Map.Entry)iterator.next();
            long ts = (Long)entry.getValue();
            if (now - ts <= this.durExpireData) {
               break;
            }

            String playerName = (String)entry.getKey();
            if (this.deleteData) {
               factories.clear();

               for(CheckType type : CheckType.values()) {
                  CheckDataFactory factory = type.getDataFactory();
                  if (factory != null) {
                     factories.add(factory);
                  }
               }

               for(CheckDataFactory factory : factories) {
                  factory.removeData(playerName);
               }

               clearComponentData(CheckType.ALL, playerName);
               this.playerData.remove(playerName.toLowerCase());
            }

            if (this.deleteData || this.deleteHistory) {
               removeExecutionHistory(CheckType.ALL, playerName);
            }

            if (this.deleteHistory) {
               ViolationHistory.removeHistory(playerName);
            }
         }

      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      this.lastLogout.remove(player.getName());
      CombinedData.getData(player).lastJoinTime = System.currentTimeMillis();
      this.addOnlinePlayer(player);
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerQuit(PlayerQuitEvent event) {
      this.onLeave(event.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPlayerKick(PlayerKickEvent event) {
      this.onLeave(event.getPlayer());
   }

   private final void onLeave(Player player) {
      long now = System.currentTimeMillis();
      this.lastLogout.put(player.getName(), now);
      CombinedData.getData(player).lastLogoutTime = now;
      this.removeOnlinePlayer(player);
   }

   public void onReload() {
      this.adjustSettings();
   }

   private void adjustSettings() {
      ConfigFile config = ConfigManager.getConfigFile();
      this.doExpireData = config.getBoolean("data.expiration.active");
      this.durExpireData = config.getLong("data.expiration.duration", 1L, 1000000L, 60L) * 60000L;
      this.deleteData = config.getBoolean("data.expiration.data", true);
      this.deleteHistory = config.getBoolean("data.expiration.history");
   }

   public static void registerExecutionHistory(CheckType type, Map histories) {
      instance.executionHistories.put(type, histories);
   }

   public static ExecutionHistory getExecutionHistory(CheckType type, String playerName) {
      Map<String, ExecutionHistory> map = (Map)instance.executionHistories.get(type);
      return map != null ? (ExecutionHistory)map.get(playerName) : null;
   }

   public static boolean removeExecutionHistory(CheckType type, String playerName) {
      boolean removed = false;

      for(CheckType refType : APIUtils.getWithChildren(type)) {
         Map<String, ExecutionHistory> map = (Map)instance.executionHistories.get(refType);
         if (map != null && map.remove(playerName) != null) {
            removed = true;
         }
      }

      return removed;
   }

   /** @deprecated */
   public static void clear(CheckType checkType) {
      clearData(checkType);
   }

   public static void clearData(CheckType checkType) {
      Set<CheckDataFactory> factories = new HashSet();

      for(CheckType type : APIUtils.getWithChildren(checkType)) {
         Map<String, ExecutionHistory> map = (Map)instance.executionHistories.get(type);
         if (map != null) {
            map.clear();
         }

         CheckDataFactory factory = type.getDataFactory();
         if (factory != null) {
            factories.add(factory);
         }
      }

      for(CheckDataFactory factory : factories) {
         factory.removeAllData();
      }

      for(IRemoveData rmd : instance.iRemoveData) {
         if (checkType == CheckType.ALL) {
            rmd.removeAllData();
         } else if (rmd instanceof IHaveCheckType) {
            CheckType refType = ((IHaveCheckType)rmd).getCheckType();
            if (refType == checkType || APIUtils.isParent(checkType, refType)) {
               rmd.removeAllData();
            }
         }
      }

      ViolationHistory.clear(checkType);
      if (checkType == CheckType.ALL) {
         instance.playerData.clear();
      }

   }

   public static boolean removeData(String playerName, CheckType checkType) {
      if (checkType == null) {
         checkType = CheckType.ALL;
      }

      boolean had = false;
      if (clearComponentData(checkType, playerName)) {
         had = true;
      }

      Set<CheckDataFactory> factories = new HashSet();

      for(CheckType otherType : APIUtils.getWithChildren(checkType)) {
         CheckDataFactory otherFactory = otherType.getDataFactory();
         if (otherFactory != null) {
            factories.add(otherFactory);
         }
      }

      for(CheckDataFactory otherFactory : factories) {
         if (otherFactory.removeData(playerName) != null) {
            had = true;
         }
      }

      if (checkType == CheckType.ALL) {
         instance.playerData.remove(playerName.toLowerCase());
      }

      return had;
   }

   public static boolean clearComponentData(CheckType checkType, String PlayerName) {
      boolean removed = false;

      for(IRemoveData rmd : instance.iRemoveData) {
         if (checkType == CheckType.ALL) {
            if (rmd.removeData(PlayerName) != null) {
               removed = true;
            }
         } else if (rmd instanceof IHaveCheckType) {
            CheckType refType = ((IHaveCheckType)rmd).getCheckType();
            if ((refType == checkType || APIUtils.isParent(checkType, refType)) && rmd.removeData(PlayerName) != null) {
               removed = true;
            }
         }
      }

      return removed;
   }

   public static void clearConfigs() {
      BlockBreakConfig.clear();
      BlockInteractConfig.clear();
      BlockPlaceConfig.clear();
      ChatConfig.clear();
      CombinedConfig.clear();
      FightConfig.clear();
      InventoryConfig.clear();
      MovingConfig.clear();
   }

   public static Player getPlayerExact(String playerName) {
      return (Player)instance.onlinePlayers.get(playerName);
   }

   public static Player getPlayer(String playerName) {
      return (Player)instance.onlinePlayers.get(playerName.toLowerCase());
   }

   public boolean addComponent(IRemoveData obj) {
      if (this.iRemoveData.contains(obj)) {
         return false;
      } else {
         this.iRemoveData.add(obj);
         return true;
      }
   }

   public void removeComponent(IRemoveData obj) {
      this.iRemoveData.remove(obj);
   }

   public void onEnable() {
      Player[] players = Bukkit.getOnlinePlayers();

      for(Player player : players) {
         this.addOnlinePlayer(player);
      }

   }

   private void addOnlinePlayer(Player player) {
      String name = player.getName();
      this.onlinePlayers.put(name, player);
      this.onlinePlayers.put(name.toLowerCase(), player);
   }

   private void removeOnlinePlayer(Player player) {
      String name = player.getName();
      this.onlinePlayers.remove(name);
      this.onlinePlayers.remove(name.toLowerCase());
   }

   public void onDisable() {
      clearData(CheckType.ALL);
      this.iRemoveData.clear();
      clearConfigs();
      this.lastLogout.clear();
      this.executionHistories.clear();
      this.onlinePlayers.clear();
      if (this.foundInconsistencies > 0) {
         LogUtil.logWarning("[NoCheatPlus] DataMan found " + this.foundInconsistencies + " inconsistencies (warnings suppressed).");
         this.foundInconsistencies = 0;
      }

   }

   public String getComponentName() {
      return "NoCheatPlus_DataManager";
   }

   public void checkConsistency(Player[] onlinePlayers) {
      int missing = 0;
      int changed = 0;
      int expectedSize = 0;

      for(int i = 0; i < onlinePlayers.length; ++i) {
         Player player = onlinePlayers[i];
         String name = player.getName();
         expectedSize += 1 + (name.equals(name.toLowerCase()) ? 0 : 1);
         if (!this.onlinePlayers.containsKey(name)) {
            ++missing;
         }

         if (player != this.onlinePlayers.get(name)) {
            ++changed;
            this.addOnlinePlayer(player);
         }
      }

      int storedSize = this.onlinePlayers.size();
      if (missing != 0 || changed != 0 || expectedSize != storedSize) {
         ++this.foundInconsistencies;
         if (!ConfigManager.getConfigFile().getBoolean("data.consistencychecks.suppresswarnings")) {
            List<String> details = new LinkedList();
            if (missing != 0) {
               details.add("missing online players (" + missing + ")");
            }

            if (expectedSize != storedSize) {
               details.add("wrong number of online players (" + storedSize + " instead of " + expectedSize + ")");
            }

            if (changed != 0) {
               details.add("changed player instances (" + changed + ")");
            }

            LogUtil.logWarning("[NoCheatPlus] DataMan inconsistencies: " + StringUtil.join(details, " | "));
         }
      }

   }

   public static PlayerData getPlayerData(String playerName, boolean create) {
      String lcName = playerName.toLowerCase();
      PlayerData data = (PlayerData)instance.playerData.get(lcName);
      if (data != null) {
         return data;
      } else if (!create) {
         return null;
      } else {
         PlayerData newData = new PlayerData(lcName);
         instance.playerData.put(lcName, newData);
         return newData;
      }
   }
}
