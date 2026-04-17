package fr.neatmonster.nocheatplus.checks;

import fr.neatmonster.nocheatplus.hooks.APIUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;

public class ViolationHistory {
   static Map checkTypeMap = new HashMap();
   private static Map violationHistories = new HashMap();
   private final List violationLevels = new ArrayList();

   public static ViolationHistory getHistory(Player player) {
      return getHistory(player.getName(), true);
   }

   public static ViolationHistory getHistory(Player player, boolean create) {
      return getHistory(player.getName(), create);
   }

   public static ViolationHistory getHistory(String playerName, boolean create) {
      ViolationHistory hist = (ViolationHistory)violationHistories.get(playerName);
      if (hist != null) {
         return hist;
      } else if (create) {
         ViolationHistory newHist = new ViolationHistory();
         violationHistories.put(playerName, newHist);
         return newHist;
      } else {
         return null;
      }
   }

   public static ViolationHistory removeHistory(String playerName) {
      return (ViolationHistory)violationHistories.remove(playerName);
   }

   public static void clear(CheckType checkType) {
      for(ViolationHistory hist : violationHistories.values()) {
         hist.remove(checkType);
      }

   }

   public ViolationLevel[] getViolationLevels() {
      ViolationLevel[] sortedLevels = new ViolationLevel[this.violationLevels.size()];
      this.violationLevels.toArray(sortedLevels);
      Arrays.sort(sortedLevels, ViolationHistory.ViolationLevel.VLComparator);
      return sortedLevels;
   }

   public void log(String check, double VL) {
      for(ViolationLevel violationLevel : this.violationLevels) {
         if (check.equals(violationLevel.check)) {
            violationLevel.add(VL);
            return;
         }
      }

      this.violationLevels.add(new ViolationLevel(check, VL));
   }

   public boolean remove(CheckType checkType) {
      if (checkType == CheckType.ALL) {
         boolean empty = this.violationLevels.isEmpty();
         this.violationLevels.clear();
         return !empty;
      } else {
         Iterator<ViolationLevel> it = this.violationLevels.iterator();
         boolean found = false;

         while(it.hasNext()) {
            ViolationLevel vl = (ViolationLevel)it.next();
            CheckType refType = (CheckType)checkTypeMap.get(vl.check);
            if (refType != null && (refType == checkType || APIUtils.isParent(checkType, refType))) {
               found = true;
               it.remove();
            }
         }

         return found;
      }
   }

   public static class ViolationLevel {
      public static Comparator VLComparator = new Comparator() {
         public int compare(ViolationLevel vl1, ViolationLevel vl2) {
            if (vl1.time == vl2.time) {
               return 0;
            } else {
               return vl1.time < vl2.time ? 1 : -1;
            }
         }
      };
      public final String check;
      public double sumVL;
      public int nVL;
      public double maxVL;
      public long time;

      public ViolationLevel(String check, double VL) {
         this.check = check;
         this.sumVL = VL;
         this.nVL = 1;
         this.maxVL = VL;
         this.time = System.currentTimeMillis();
      }

      public void add(double VL) {
         this.sumVL += VL;
         ++this.nVL;
         this.maxVL = Math.max(this.maxVL, VL);
         this.time = System.currentTimeMillis();
      }

      public boolean equals(Object obj) {
         return obj instanceof ViolationLevel ? this.check.equals(((ViolationLevel)obj).check) : false;
      }

      public int hashCode() {
         return this.check.hashCode();
      }
   }
}
