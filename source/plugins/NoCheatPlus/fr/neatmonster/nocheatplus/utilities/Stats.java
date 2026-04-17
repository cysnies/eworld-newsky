package fr.neatmonster.nocheatplus.utilities;

import fr.neatmonster.nocheatplus.logging.LogUtil;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;

public final class Stats {
   private long tsStats;
   private long periodStats;
   private long nVerbose;
   private long nDone;
   private boolean logStats;
   private boolean showRange;
   private final Map entries;
   private final DecimalFormat f;
   private final String label;
   private final Map idKeyMap;
   private final Map keyIdMap;
   int maxId;

   public Stats() {
      this("[STATS]");
   }

   public Stats(String label) {
      this.tsStats = 0L;
      this.periodStats = 12345L;
      this.nVerbose = 500L;
      this.nDone = 0L;
      this.logStats = false;
      this.showRange = true;
      this.entries = new HashMap();
      this.idKeyMap = new HashMap();
      this.keyIdMap = new HashMap();
      this.maxId = 0;
      this.label = label;
      this.f = new DecimalFormat();
      this.f.setGroupingUsed(true);
      this.f.setGroupingSize(3);
      DecimalFormatSymbols s = this.f.getDecimalFormatSymbols();
      s.setGroupingSeparator(',');
      this.f.setDecimalFormatSymbols(s);
   }

   public final void addStats(Integer key, long value) {
      Entry entry = (Entry)this.entries.get(key);
      if (entry != null) {
         ++entry.n;
         entry.val += value;
         if (value < entry.min) {
            entry.min = value;
         } else if (value > entry.max) {
            entry.max = value;
         }
      } else {
         entry = new Entry();
         entry.val = value;
         entry.n = 1L;
         this.entries.put(key, entry);
         entry.min = value;
         entry.max = value;
      }

      if (this.logStats) {
         ++this.nDone;
         if (this.nDone > this.nVerbose) {
            this.nDone = 0L;
            long ts = System.currentTimeMillis();
            if (ts > this.tsStats + this.periodStats) {
               this.tsStats = ts;
               LogUtil.logInfo(this.getStatsStr());
            }
         }

      }
   }

   public final String getStatsStr() {
      return this.getStatsStr(false);
   }

   public final String getStatsStr(boolean colors) {
      StringBuilder b = new StringBuilder(400);
      b.append(this.label + " ");
      boolean first = true;

      for(Integer id : this.entries.keySet()) {
         if (!first) {
            b.append(" | ");
         }

         Entry entry = (Entry)this.entries.get(id);
         String av = this.f.format(entry.val / entry.n);
         String key = this.getKey(id);
         String n = this.f.format(entry.n);
         if (colors) {
            key = ChatColor.GREEN + key + ChatColor.WHITE;
            n = ChatColor.AQUA + n + ChatColor.WHITE;
            av = ChatColor.YELLOW + av + ChatColor.WHITE;
         }

         b.append(key + " av=" + av + " n=" + n);
         if (this.showRange) {
            b.append(" rg=" + this.f.format(entry.min) + "..." + this.f.format(entry.max));
         }

         first = false;
      }

      return b.toString();
   }

   public final String getKey(Integer id) {
      String key = (String)this.idKeyMap.get(id);
      if (key == null) {
         key = "<no key for id: " + id + ">";
         this.idKeyMap.put(id, key);
         this.keyIdMap.put(key, id);
      }

      return key;
   }

   public final Integer getNewId(String key) {
      ++this.maxId;

      while(this.idKeyMap.containsKey(this.maxId)) {
         ++this.maxId;
      }

      this.idKeyMap.put(this.maxId, key);
      this.keyIdMap.put(key, this.maxId);
      return this.maxId;
   }

   public final Integer getId(String key, boolean create) {
      Integer id = (Integer)this.keyIdMap.get(key);
      if (id == null) {
         return create ? this.getNewId(key) : null;
      } else {
         return id;
      }
   }

   public final Integer getId(String key) {
      return (Integer)this.keyIdMap.get(key);
   }

   public final void clear() {
      this.entries.clear();
   }

   public final void setLogStats(boolean log) {
      this.logStats = log;
   }

   public final void setShowRange(boolean set) {
      this.showRange = set;
   }

   public static final class Entry {
      public long val = 0L;
      public long n = 0L;
      public long min = Long.MAX_VALUE;
      public long max = Long.MIN_VALUE;
   }
}
