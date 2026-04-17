package fr.neatmonster.nocheatplus.checks.chat.analysis.engine;

import fr.neatmonster.nocheatplus.checks.chat.ChatConfig;
import fr.neatmonster.nocheatplus.checks.chat.analysis.engine.processors.WordProcessor;
import fr.neatmonster.nocheatplus.utilities.ds.ManagedMap;
import java.util.Collection;

public class EnginePlayerDataMap extends ManagedMap {
   protected long durExpire;
   protected long lastAccess = System.currentTimeMillis();
   protected long lastExpired;

   public EnginePlayerDataMap(long durExpire, int defaultCapacity, float loadFactor) {
      super(defaultCapacity, loadFactor);
      this.lastExpired = this.lastAccess;
      this.durExpire = durExpire;
   }

   public EnginePlayerData get(String key, ChatConfig cc) {
      EnginePlayerData data = (EnginePlayerData)super.get(key);
      if (data == null) {
         data = new EnginePlayerData(cc);
         this.put(key, data);
      }

      long ts = System.currentTimeMillis();
      if (ts < this.lastExpired) {
         this.lastExpired = ts;
      } else if (ts - this.lastExpired > this.durExpire) {
         this.expire(ts - this.durExpire);
      }

      this.lastAccess = ts;
      return data;
   }

   public void clear() {
      long time = System.currentTimeMillis();

      for(ManagedMap.ValueWrap wrap : this.map.values()) {
         for(WordProcessor processor : ((EnginePlayerData)wrap.value).processors) {
            processor.clear();
         }
      }

      super.clear();
      this.lastAccess = this.lastExpired = time;
   }

   public Collection expire(long ts) {
      Collection<String> rem = super.expire(ts);
      if (!rem.isEmpty()) {
         this.lastExpired = System.currentTimeMillis();
      }

      return rem;
   }
}
