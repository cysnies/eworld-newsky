package fr.neatmonster.nocheatplus.checks.chat.analysis.engine.processors;

import fr.neatmonster.nocheatplus.checks.chat.analysis.MessageLetterCount;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.utilities.ds.bktree.SimpleTimedBKLevenshtein;
import fr.neatmonster.nocheatplus.utilities.ds.bktree.TimedBKLevenshtein;
import java.util.List;

public class SimilarWordsBKL extends DigestedWords {
   protected final SimpleTimedBKLevenshtein tree = new SimpleTimedBKLevenshtein();
   protected int added = 0;
   protected final int maxSize;
   protected final int range;
   protected final long durExpire;
   protected final int maxSeek;
   protected long lastAdd = System.currentTimeMillis();

   public SimilarWordsBKL(String name, SimilarWordsBKLSettings settings) {
      super(name, settings);
      this.maxSize = settings.maxSize;
      this.range = settings.range;
      this.durExpire = settings.durExpire;
      this.maxSeek = settings.maxSeek;
   }

   public void clear() {
      super.clear();
      this.tree.clear();
      this.added = 0;
   }

   public void start(MessageLetterCount message) {
      if (this.added + message.words.length > this.maxSize || System.currentTimeMillis() - this.lastAdd > this.durExpire) {
         this.tree.clear();
      }

   }

   protected float getScore(List chars, long ts) {
      this.lastAdd = ts;
      char[] a = DigestedWords.toArray(chars);
      SimpleTimedBKLevenshtein.STBKLResult result = (SimpleTimedBKLevenshtein.STBKLResult)this.tree.lookup(a, this.range, this.maxSeek, true);
      if (result.isNew) {
         ++this.added;
      }

      float score = 0.0F;
      if (!result.isNew && result.match != null) {
         long age = ts - ((TimedBKLevenshtein.SimpleTimedLevenNode)result.match).ts;
         ((TimedBKLevenshtein.SimpleTimedLevenNode)result.match).ts = ts;
         if (age < this.durExpire) {
            score = Math.max(score, (float)(this.durExpire - age) / (float)this.durExpire);
         }
      }

      for(TimedBKLevenshtein.SimpleTimedLevenNode node : result.nodes) {
         long age = ts - node.ts;
         node.ts = ts;
         if (age < this.durExpire) {
            score = Math.max(score, (float)(this.durExpire - age) / (float)this.durExpire);
         }
      }

      return score;
   }

   public static class SimilarWordsBKLSettings extends DigestedWords.DigestedWordsSettings {
      public int maxSize = 1000;
      public int range = 2;
      public long durExpire = 30000L;
      public int maxSeek = 0;

      public SimilarWordsBKLSettings() {
         this.split = true;
         this.compress = true;
      }

      public SimilarWordsBKLSettings applyConfig(ConfigFile config, String prefix) {
         super.applyConfig(config, prefix);
         this.range = config.getInt(prefix + "range", this.range);
         this.maxSize = config.getInt(prefix + "size", this.maxSize);
         this.maxSeek = config.getInt(prefix + "seek", this.maxSeek);
         this.durExpire = (long)(config.getDouble(prefix + "time", (double)((float)this.durExpire / 1000.0F)) * (double)1000.0F);
         return this;
      }
   }
}
