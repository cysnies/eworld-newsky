package fr.neatmonster.nocheatplus.hooks;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.checks.access.IViolationInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class NCPHookManager {
   private static int maxHookId = 0;
   private static final Map allHooks = new HashMap();
   private static final Map hooksByChecks = new HashMap();
   private static Comparator HookComparator = new Comparator() {
      public int compare(NCPHook o1, NCPHook o2) {
         boolean s1 = o1 instanceof IStats;
         boolean f1 = o1 instanceof IFirst;
         boolean l1 = o1 instanceof ILast;
         boolean s2 = o2 instanceof IStats;
         boolean f2 = o2 instanceof IFirst;
         boolean l2 = o2 instanceof ILast;
         if (s1 && !s2) {
            return l1 ? 1 : -1;
         } else if (!s1 && s2) {
            return l2 ? -1 : 1;
         } else if (l2) {
            return -1;
         } else if (l1) {
            return 1;
         } else if (f1) {
            return -1;
         } else {
            return f2 ? 1 : 0;
         }
      }
   };

   public static Integer addHook(CheckType checkType, NCPHook hook) {
      Integer hookId = getId(hook);
      addToMappings(checkType, hook);
      logHookAdded(hook);
      return hookId;
   }

   public static Integer addHook(CheckType[] checkTypes, NCPHook hook) {
      if (checkTypes == null) {
         checkTypes = new CheckType[]{CheckType.ALL};
      }

      Integer hookId = getId(hook);

      for(CheckType checkType : checkTypes) {
         addToMappings(checkType, hook);
      }

      logHookAdded(hook);
      return hookId;
   }

   private static void addToMapping(CheckType checkType, NCPHook hook) {
      List<NCPHook> hooks = (List)hooksByChecks.get(checkType);
      if (!hooks.contains(hook)) {
         if (hook instanceof ILast || !(hook instanceof IStats) && !(hook instanceof IFirst)) {
            hooks.add(hook);
         } else {
            hooks.add(0, hook);
         }

         Collections.sort(hooks, HookComparator);
      }

   }

   private static void addToMappings(CheckType checkType, NCPHook hook) {
      if (checkType == CheckType.ALL) {
         for(CheckType refType : CheckType.values()) {
            addToMapping(refType, hook);
         }

      } else {
         addToMapping(checkType, hook);

         for(CheckType refType : CheckType.values()) {
            addToMappingsRecursively(checkType, refType, hook);
         }

      }
   }

   private static boolean addToMappingsRecursively(CheckType checkType, CheckType refType, NCPHook hook) {
      if (refType.getParent() == null) {
         return false;
      } else if (refType.getParent() == checkType) {
         addToMapping(refType, hook);
         return true;
      } else if (addToMappingsRecursively(checkType, refType.getParent(), hook)) {
         addToMapping(refType, hook);
         return true;
      } else {
         return false;
      }
   }

   private static final boolean applyHooks(CheckType checkType, Player player, IViolationInfo info, List hooks) {
      for(int i = 0; i < hooks.size(); ++i) {
         NCPHook hook = (NCPHook)hooks.get(i);

         try {
            if (hook.onCheckFailure(checkType, player, info) && !(hook instanceof IStats)) {
               return true;
            }
         } catch (Throwable t) {
            logHookFailure(checkType, player, hook, t);
         }
      }

      return false;
   }

   public static Collection getAllHooks() {
      List<NCPHook> hooks = new LinkedList();
      hooks.addAll(allHooks.values());
      return hooks;
   }

   private static final String getHookDescription(NCPHook hook) {
      return hook.getHookName() + " [" + hook.getHookVersion() + "]";
   }

   public static Collection getHooksByName(String hookName) {
      List<NCPHook> hooks = new LinkedList();

      for(Integer refId : allHooks.keySet()) {
         NCPHook hook = (NCPHook)allHooks.get(refId);
         if (hook.getHookName().equals(hookName) && !hooks.contains(hook)) {
            hooks.add(hook);
         }
      }

      return hooks;
   }

   private static Integer getId(NCPHook hook) {
      if (hook == null) {
         throw new NullPointerException("Hooks must not be null.");
      } else {
         Integer id = null;

         for(Integer refId : allHooks.keySet()) {
            if (hook == allHooks.get(refId)) {
               id = refId;
               break;
            }
         }

         if (id == null) {
            id = getNewHookId();
            allHooks.put(id, hook);
         }

         return id;
      }
   }

   private static Integer getNewHookId() {
      ++maxHookId;
      return maxHookId;
   }

   private static final void logHookAdded(NCPHook hook) {
      Bukkit.getLogger().info("[NoCheatPlus] Added hook: " + getHookDescription(hook) + ".");
   }

   private static final void logHookFailure(CheckType checkType, Player player, NCPHook hook, Throwable t) {
      StringBuilder builder = new StringBuilder(1024);
      builder.append("[NoCheatPlus] Hook " + getHookDescription(hook) + " encountered an unexpected exception:\n");
      builder.append("Processing: ");
      if (checkType.getParent() != null) {
         builder.append("Prent " + checkType.getParent() + " ");
      }

      builder.append("Check " + checkType);
      builder.append(" Player " + player.getName());
      builder.append("\n");
      builder.append("Exception (" + t.getClass().getSimpleName() + "): " + t.getMessage() + "\n");

      for(StackTraceElement el : t.getStackTrace()) {
         builder.append(el.toString());
      }

      Bukkit.getLogger().severe(builder.toString());
   }

   private static final void logHookRemoved(NCPHook hook) {
      Bukkit.getLogger().info("[NoCheatPlus] Removed hook: " + getHookDescription(hook) + ".");
   }

   public static Collection removeAllHooks() {
      Collection<NCPHook> hooks = getAllHooks();

      for(NCPHook hook : hooks) {
         removeHook(hook);
      }

      return hooks;
   }

   private static void removeFromMappings(NCPHook hook, Integer hookId) {
      allHooks.remove(hookId);

      for(CheckType checkId : hooksByChecks.keySet()) {
         ((List)hooksByChecks.get(checkId)).remove(hook);
      }

   }

   public static NCPHook removeHook(Integer hookId) {
      NCPHook hook = (NCPHook)allHooks.get(hookId);
      if (hook == null) {
         return null;
      } else {
         removeFromMappings(hook, hookId);
         logHookRemoved(hook);
         return hook;
      }
   }

   public static Integer removeHook(NCPHook hook) {
      Integer hookId = null;

      for(Integer refId : allHooks.keySet()) {
         if (hook == allHooks.get(refId)) {
            hookId = refId;
            break;
         }
      }

      if (hookId == null) {
         return null;
      } else {
         removeFromMappings(hook, hookId);
         logHookRemoved(hook);
         return hookId;
      }
   }

   public static Set removeHooks(Collection hooks) {
      Set<Integer> ids = new LinkedHashSet();

      for(NCPHook hook : hooks) {
         Integer id = removeHook(hook);
         if (id != null) {
            ids.add(id);
         }
      }

      return ids;
   }

   public static Collection removeHooks(String hookName) {
      Collection<NCPHook> hooks = getHooksByName(hookName);
      if (hooks.isEmpty()) {
         return null;
      } else {
         removeHooks(hooks);
         return hooks;
      }
   }

   public static final boolean shouldCancelVLProcessing(ViolationData violationData) {
      CheckType type = violationData.check.getType();
      List<NCPHook> hooksCheck = (List)hooksByChecks.get(type);
      if (!hooksCheck.isEmpty()) {
         if (APIUtils.needsSynchronization(type)) {
            synchronized(hooksCheck) {
               return applyHooks(type, violationData.player, violationData, hooksCheck);
            }
         } else {
            return applyHooks(type, violationData.player, violationData, hooksCheck);
         }
      } else {
         return false;
      }
   }

   static {
      for(CheckType type : CheckType.values()) {
         if (APIUtils.needsSynchronization(type)) {
            hooksByChecks.put(type, Collections.synchronizedList(new ArrayList()));
         } else {
            hooksByChecks.put(type, new ArrayList());
         }
      }

   }
}
