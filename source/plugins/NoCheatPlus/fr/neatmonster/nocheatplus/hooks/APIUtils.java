package fr.neatmonster.nocheatplus.hooks;

import fr.neatmonster.nocheatplus.checks.CheckType;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class APIUtils {
   private static final Map childrenMap = new HashMap();
   private static final Map withChildrenMap = new HashMap();

   public static final Collection getChildren(CheckType type) {
      return Arrays.asList(childrenMap.get(type));
   }

   public static final Collection getWithChildren(CheckType type) {
      return Arrays.asList(withChildrenMap.get(type));
   }

   public static final boolean isParent(CheckType supposedParent, CheckType supposedChild) {
      if (supposedParent == supposedChild) {
         return false;
      } else if (supposedParent == CheckType.ALL) {
         return true;
      } else {
         for(CheckType parent = supposedChild.getParent(); parent != null; parent = parent.getParent()) {
            if (parent == supposedParent) {
               return true;
            }
         }

         return false;
      }
   }

   public static final boolean needsSynchronization(CheckType type) {
      return type == CheckType.CHAT || isParent(CheckType.CHAT, type);
   }

   static {
      Map<CheckType, Set<CheckType>> map = new HashMap();

      for(CheckType type : CheckType.values()) {
         map.put(type, new HashSet());
      }

      for(CheckType type : CheckType.values()) {
         if (type != CheckType.ALL) {
            ((Set)map.get(CheckType.ALL)).add(type);
         }

         for(CheckType other : CheckType.values()) {
            if (isParent(other, type)) {
               ((Set)map.get(other)).add(type);
            }
         }
      }

      for(CheckType parent : map.keySet()) {
         Set<CheckType> set = (Set)map.get(parent);
         CheckType[] a = new CheckType[set.size()];
         childrenMap.put(parent, set.toArray(a));
         CheckType[] aw = new CheckType[set.size() + 1];
         set.toArray(aw);
         aw[set.size()] = parent;
         withChildrenMap.put(parent, aw);
      }

   }
}
