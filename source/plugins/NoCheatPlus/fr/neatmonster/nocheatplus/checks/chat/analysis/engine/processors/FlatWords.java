package fr.neatmonster.nocheatplus.checks.chat.analysis.engine.processors;

import fr.neatmonster.nocheatplus.checks.chat.analysis.MessageLetterCount;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.utilities.ActionFrequency;
import java.util.LinkedHashMap;
import java.util.List;

public class FlatWords extends DigestedWords {
   protected final int maxSize;
   protected final LinkedHashMap entries;
   protected final long durBucket;
   protected final int nBuckets;
   protected final float factor;
   protected long lastAdd = System.currentTimeMillis();

   public FlatWords(String name, FlatWordsSettings settings) {
      super(name, settings);
      this.maxSize = settings.maxSize;
      this.entries = new LinkedHashMap(this.maxSize);
      this.nBuckets = settings.nBuckets;
      this.durBucket = settings.durBucket;
      this.factor = settings.factor;
   }

   public void start(MessageLetterCount message) {
      if (System.currentTimeMillis() - this.lastAdd > (long)this.nBuckets * this.durBucket) {
         this.entries.clear();
      } else if (this.entries.size() + message.words.length > this.maxSize) {
         releaseMap(this.entries, Math.max(message.words.length, this.maxSize / 10));
      }

   }

   public void clear() {
      super.clear();
      this.entries.clear();
   }

   protected float getScore(List chars, long ts) {
      this.lastAdd = ts;
      char[] a = DigestedWords.toArray(chars);
      String key = new String(a);
      ActionFrequency freq = (ActionFrequency)this.entries.get(key);
      if (freq == null) {
         freq = new ActionFrequency(this.nBuckets, this.durBucket);
         this.entries.put(key, freq);
         return 0.0F;
      } else {
         freq.update(ts);
         float score = Math.min(1.0F, freq.score(this.factor));
         freq.add(ts, 1.0F);
         return score;
      }
   }

   public static class FlatWordsSettings extends DigestedWords.DigestedWordsSettings {
      public int maxSize = 1000;
      public long durBucket = 1500L;
      public int nBuckets = 4;
      public float factor = 0.9F;

      public FlatWordsSettings() {
         this.split = true;
      }

      public FlatWordsSettings applyConfig(ConfigFile config, String prefix) {
         super.applyConfig(config, prefix);
         this.maxSize = config.getInt(prefix + "size", this.maxSize);
         this.nBuckets = config.getInt(prefix + "buckets", this.nBuckets);
         this.durBucket = (long)(config.getDouble(prefix + "time", (double)((float)this.durBucket / 1000.0F)) * (double)1000.0F);
         this.factor = (float)config.getDouble(prefix + "factor", (double)this.factor);
         return this;
      }
   }
}
