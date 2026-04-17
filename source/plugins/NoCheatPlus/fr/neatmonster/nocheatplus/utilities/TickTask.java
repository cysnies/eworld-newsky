package fr.neatmonster.nocheatplus.utilities;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.checks.access.ICheckData;
import fr.neatmonster.nocheatplus.components.TickListener;
import fr.neatmonster.nocheatplus.logging.LogUtil;
import fr.neatmonster.nocheatplus.metrics.MetricsData;
import fr.neatmonster.nocheatplus.players.DataManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class TickTask implements Runnable {
   public static final int lagMaxTicks = 80;
   private static final Set permissionUpdates = new LinkedHashSet(50);
   private static final List delayedActions = new LinkedList();
   private static final Set tickListeners = new LinkedHashSet();
   private static final long[] tickDurations = new long[80];
   private static final long[] tickDurationsSq = new long[80];
   private static final long lagMaxCoveredMs = 324050L;
   private static long[] spikeDurations = new long[]{150L, 450L, 1000L, 5000L};
   private static ActionFrequency[] spikes;
   protected static int taskId;
   protected static int tick;
   protected static long timeStart;
   protected static long timeLast;
   protected static boolean locked;

   public static void executeActions() {
      List<ViolationData> copyActions;
      synchronized(delayedActions) {
         if (delayedActions.isEmpty()) {
            return;
         }

         copyActions = new ArrayList(delayedActions);
         delayedActions.clear();
      }

      for(int i = 0; i < copyActions.size(); ++i) {
         ((ViolationData)copyActions.get(i)).executeActions();
      }

   }

   public static void updatePermissions() {
      List<PermissionUpdateEntry> copyPermissions;
      synchronized(permissionUpdates) {
         if (permissionUpdates.isEmpty()) {
            return;
         }

         copyPermissions = new ArrayList(permissionUpdates);
         permissionUpdates.clear();
      }

      for(int i = 0; i < copyPermissions.size(); ++i) {
         PermissionUpdateEntry entry = (PermissionUpdateEntry)copyPermissions.get(i);
         Player player = DataManager.getPlayer(entry.playerName);
         if (player != null && player.isOnline()) {
            String[] perms = entry.checkType.getConfigFactory().getConfig(player).getCachePermissions();
            if (perms != null) {
               ICheckData data = entry.checkType.getDataFactory().getData(player);

               for(int j = 0; j < perms.length; ++j) {
                  String permission = perms[j];
                  data.setCachedPermission(permission, player.hasPermission(permission));
               }
            }
         }
      }

   }

   public static void requestPermissionUpdate(String playerName, CheckType checkType) {
      synchronized(permissionUpdates) {
         if (!locked) {
            permissionUpdates.add(new PermissionUpdateEntry(playerName, checkType));
         }
      }
   }

   public static void requestActionsExecution(ViolationData actions) {
      synchronized(delayedActions) {
         if (!locked) {
            delayedActions.add(actions);
         }
      }
   }

   public static void addTickListener(TickListener listener) {
      synchronized(tickListeners) {
         if (!locked) {
            if (!tickListeners.contains(listener)) {
               tickListeners.add(listener);
            }

            if (listener instanceof OnDemandTickListener) {
               ((OnDemandTickListener)listener).setRegistered(true);
            }

         }
      }
   }

   public static boolean removeTickListener(TickListener listener) {
      synchronized(tickListeners) {
         if (listener instanceof OnDemandTickListener) {
            ((OnDemandTickListener)listener).setRegistered(false);
         }

         return tickListeners.remove(listener);
      }
   }

   public static void removeAllTickListeners() {
      synchronized(tickListeners) {
         for(TickListener listener : tickListeners) {
            if (listener instanceof OnDemandTickListener) {
               try {
                  OnDemandTickListener odtl = (OnDemandTickListener)listener;
                  if (odtl.isRegistered()) {
                     odtl.setRegistered(false);
                  }
               } catch (Throwable t) {
                  LogUtil.logWarning("[NoCheatPlus] Failed to set OnDemandTickListener to unregistered state: " + t.getClass().getSimpleName());
                  LogUtil.logWarning(t);
               }
            }
         }

         tickListeners.clear();
      }
   }

   public static final int getTick() {
      return tick;
   }

   public static final long getTimeStart() {
      return timeStart;
   }

   public static final long getTimeLast() {
      return timeLast;
   }

   public static final float getLag(long ms) {
      return getLag(ms, false);
   }

   public static final float getLag(long ms, boolean exact) {
      if (ms < 0L) {
         return getLag(0L, exact);
      } else if (ms > 324050L) {
         return getLag(324050L, exact);
      } else {
         int tick = TickTask.tick;
         if (tick == 0) {
            return 1.0F;
         } else {
            int add = ms > 0L && ms % 50L == 0L ? 0 : 1;
            int totalTicks = Math.min(tick, add + (int)(ms / 50L));
            int maxTick = Math.min(80, totalTicks);
            long sum = tickDurations[maxTick - 1];
            long covered = (long)(maxTick * 50);
            if (totalTicks > 80) {
               int maxTickSq = Math.min(80, totalTicks / 80);
               if (80 * maxTickSq == totalTicks) {
                  --maxTickSq;
               }

               sum += tickDurationsSq[maxTickSq - 1];
               covered += (long)(4000 * maxTickSq);
            }

            if (exact) {
               long passed = System.currentTimeMillis() - timeLast;
               if (passed > 50L) {
                  covered += 50L;
                  sum += passed;
               }
            }

            return Math.max(1.0F, (float)sum / (float)covered);
         }
      }
   }

   /** @deprecated */
   public static final int getModerateLagSpikes() {
      spikes[0].update(System.currentTimeMillis());
      return (int)spikes[0].score(1.0F);
   }

   /** @deprecated */
   public static final int getHeavyLagSpikes() {
      spikes[1].update(System.currentTimeMillis());
      return (int)spikes[1].score(1.0F);
   }

   public static final int getNumberOfLagSpikes() {
      spikes[0].update(System.currentTimeMillis());
      return (int)spikes[0].score(1.0F);
   }

   public static final long[] getLagSpikeDurations() {
      return Arrays.copyOf(spikeDurations, spikeDurations.length);
   }

   public static final int[] getLagSpikes() {
      int[] out = new int[spikeDurations.length];
      long now = System.currentTimeMillis();

      for(int i = 0; i < spikeDurations.length; ++i) {
         spikes[i].update(now);
         out[i] = (int)spikes[i].score(1.0F);
      }

      return out;
   }

   public boolean isLocked() {
      return locked;
   }

   public static int start(Plugin plugin) {
      cancel();
      taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new TickTask(), 1L, 1L);
      if (taskId != -1) {
         timeStart = System.currentTimeMillis();
      } else {
         timeStart = 0L;
      }

      return taskId;
   }

   public static void cancel() {
      if (taskId != -1) {
         Bukkit.getScheduler().cancelTask(taskId);
         taskId = -1;
      }
   }

   public static void setLocked(boolean locked) {
      TickTask.locked = locked;
   }

   public static void purge() {
      synchronized(permissionUpdates) {
         permissionUpdates.clear();
      }

      synchronized(delayedActions) {
         delayedActions.clear();
      }

      synchronized(tickListeners) {
         tickListeners.clear();
      }
   }

   public static void reset() {
      tick = 0;
      timeLast = 0L;

      for(int i = 0; i < 80; ++i) {
         tickDurations[i] = 0L;
         tickDurationsSq[i] = 0L;
      }

      for(int i = 0; i < spikeDurations.length; ++i) {
         spikes[i].clear(0L);
      }

   }

   private final void notifyListeners() {
      List<TickListener> copyListeners;
      synchronized(tickListeners) {
         if (tickListeners.isEmpty()) {
            return;
         }

         copyListeners = new ArrayList(tickListeners);
      }

      for(int i = 0; i < copyListeners.size(); ++i) {
         TickListener listener = (TickListener)copyListeners.get(i);

         try {
            listener.onTick(tick, timeLast);
         } catch (Throwable t) {
            LogUtil.logSevere("[NoCheatPlus] (TickTask) TickListener generated an exception:");
            LogUtil.logSevere(t);
         }
      }

   }

   public void run() {
      executeActions();
      updatePermissions();
      this.notifyListeners();
      if (tick > 0 && tick % 20 == 0) {
         int ticks = Math.min(20, Math.round(1000.0F / (50.0F * getLag(1000L, true))));
         MetricsData.addTicks(ticks);
      }

      long time = System.currentTimeMillis();
      long lastDur;
      if (timeLast > time) {
         LogUtil.logSevere("[NoCheatPlus] System time ran backwards (" + timeLast + "->" + time + "), clear all data and history...");
         DataManager.clearData(CheckType.ALL);
         lastDur = 50L;

         for(int i = 0; i < spikeDurations.length; ++i) {
            spikes[i].clear(0L);
         }
      } else if (tick > 0) {
         lastDur = time - timeLast;
      } else {
         lastDur = 50L;
      }

      if (tick > 0 && tick % 80 == 0) {
         long sum = tickDurations[79];

         for(int i = 1; i < 80; ++i) {
            tickDurationsSq[i] = tickDurationsSq[i - 1] + sum;
         }

         tickDurationsSq[0] = sum;
      }

      for(int i = 1; i < 80; ++i) {
         tickDurations[i] = tickDurations[i - 1] + lastDur;
      }

      tickDurations[0] = lastDur;
      if (lastDur > spikeDurations[0] && tick > 0) {
         spikes[0].add(time, 1.0F);

         for(int i = 1; i < spikeDurations.length && lastDur > spikeDurations[i]; ++i) {
            spikes[i].add(time, 1.0F);
         }
      }

      ++tick;
      timeLast = time;
   }

   static {
      spikes = new ActionFrequency[spikeDurations.length];
      taskId = -1;
      tick = 0;
      timeStart = 0L;
      timeLast = 0L;
      locked = true;

      for(int i = 0; i < spikeDurations.length; ++i) {
         spikes[i] = new ActionFrequency(3, 1200000L);
      }

   }

   protected static final class PermissionUpdateEntry {
      public CheckType checkType;
      public String playerName;
      private final int hashCode;

      public PermissionUpdateEntry(String playerName, CheckType checkType) {
         this.playerName = playerName;
         this.checkType = checkType;
         this.hashCode = playerName.hashCode() ^ checkType.hashCode();
      }

      public boolean equals(Object obj) {
         if (!(obj instanceof PermissionUpdateEntry)) {
            return false;
         } else {
            PermissionUpdateEntry other = (PermissionUpdateEntry)obj;
            return this.playerName.equals(other.playerName) && this.checkType.equals(other.checkType);
         }
      }

      public int hashCode() {
         return this.hashCode;
      }
   }
}
