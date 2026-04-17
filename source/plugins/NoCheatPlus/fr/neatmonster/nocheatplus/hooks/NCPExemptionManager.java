package fr.neatmonster.nocheatplus.hooks;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.components.NCPListener;
import fr.neatmonster.nocheatplus.logging.LogUtil;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class NCPExemptionManager {
   private static final Map exempted = new HashMap();
   private static final Map registeredPlayers = new HashMap();

   public static final void clear() {
      registeredPlayers.clear();

      for(CheckType checkType : CheckType.values()) {
         if (APIUtils.needsSynchronization(checkType)) {
            exempted.put(checkType, Collections.synchronizedSet(new HashSet()));
         } else {
            exempted.put(checkType, new HashSet());
         }
      }

   }

   public static final void exemptPermanently(int entityId) {
      exemptPermanently(entityId, CheckType.ALL);
   }

   public static final void exemptPermanently(int entityId, CheckType checkType) {
      Integer id = entityId;
      ((Set)exempted.get(checkType)).add(id);

      for(CheckType child : APIUtils.getChildren(checkType)) {
         ((Set)exempted.get(child)).add(id);
      }

   }

   public static final void exemptPermanently(Player player) {
      exemptPermanently(player, CheckType.ALL);
   }

   public static final void exemptPermanently(Player player, CheckType checkType) {
      exemptPermanently(player.getEntityId(), checkType);
   }

   public static Listener getListener() {
      return new NCPListener() {
         @EventHandler(
            priority = EventPriority.LOWEST
         )
         public void onPlayerJoin(PlayerJoinEvent event) {
            NCPExemptionManager.registerPlayer(event.getPlayer());
         }

         @EventHandler(
            priority = EventPriority.MONITOR
         )
         public void onPlayerQuit(PlayerQuitEvent event) {
            NCPExemptionManager.tryToRemove(event.getPlayer());
         }

         @EventHandler(
            priority = EventPriority.MONITOR,
            ignoreCancelled = true
         )
         public void onPlayerKick(PlayerKickEvent event) {
            NCPExemptionManager.tryToRemove(event.getPlayer());
         }
      };
   }

   public static final boolean isExempted(int entityId, CheckType checkType) {
      return ((Set)exempted.get(checkType)).contains(entityId);
   }

   public static final boolean isExempted(Player player, CheckType checkType) {
      return isExempted(player.getEntityId(), checkType);
   }

   public static final boolean isExempted(String playerName, CheckType checkType) {
      Integer entityId = (Integer)registeredPlayers.get(playerName);
      return entityId == null ? false : isExempted(entityId, checkType);
   }

   public static final void registerPlayer(Player player) {
      int entityId = player.getEntityId();
      String name = player.getName();
      Integer registeredId = (Integer)registeredPlayers.get(name);
      if (registeredId == null) {
         registeredPlayers.put(name, entityId);
      } else if (entityId != registeredId) {
         for(Set set : exempted.values()) {
            if (set.remove(registeredId)) {
               set.add(entityId);
            }
         }

         registeredPlayers.put(name, entityId);
      }

   }

   protected static final void tryToRemove(Player player) {
      if (registeredPlayers.containsKey(player.getName())) {
         Integer entityId = player.getEntityId();

         for(CheckType checkType : CheckType.values()) {
            if (isExempted(entityId, checkType)) {
               return;
            }
         }

         registeredPlayers.remove(player.getName());
      }
   }

   public static final void unexempt(int entityId) {
      unexempt(entityId, CheckType.ALL);
   }

   public static final void unexempt(int entityId, CheckType checkType) {
      Integer id = entityId;
      ((Set)exempted.get(checkType)).remove(id);

      for(CheckType child : APIUtils.getChildren(checkType)) {
         ((Set)exempted.get(child)).remove(id);
      }

   }

   public static final void unexempt(Player player) {
      unexempt(player, CheckType.ALL);
   }

   public static final void unexempt(String playerName) {
      unexempt(playerName, CheckType.ALL);
   }

   public static final void unexempt(Player player, CheckType checkType) {
      unexempt(player.getEntityId(), checkType);
   }

   public static final void unexempt(String playerName, CheckType checkType) {
      Integer entityId = (Integer)registeredPlayers.get(playerName);
      if (entityId != null) {
         unexempt(entityId, checkType);
      }

   }

   public static void checkConsistency(Player[] onlinePlayers) {
      int wrong = 0;

      for(int i = 0; i < onlinePlayers.length; ++i) {
         Player player = onlinePlayers[i];
         int id = player.getEntityId();
         String name = player.getName();
         Integer presentId = (Integer)registeredPlayers.get(name);
         if (presentId != null && id != presentId) {
            ++wrong;
            registerPlayer(player);
         }
      }

      if (wrong != 0) {
         List<String> details = new LinkedList();
         if (wrong != 0) {
            details.add("wrong entity-ids (" + wrong + ")");
         }

         LogUtil.logWarning("[NoCheatPlus] ExemptionManager inconsistencies: " + StringUtil.join(details, " | "));
      }

   }

   static {
      clear();
   }
}
