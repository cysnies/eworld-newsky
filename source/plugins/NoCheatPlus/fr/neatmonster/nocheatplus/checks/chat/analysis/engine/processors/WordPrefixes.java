package fr.neatmonster.nocheatplus.checks.chat.analysis.engine.processors;

import fr.neatmonster.nocheatplus.checks.chat.analysis.MessageLetterCount;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.utilities.ds.prefixtree.SimpleTimedCharPrefixTree;
import fr.neatmonster.nocheatplus.utilities.ds.prefixtree.TimedCharPrefixTree;
import java.util.List;

public class WordPrefixes extends DigestedWords {
   protected final SimpleTimedCharPrefixTree tree = new SimpleTimedCharPrefixTree(true);
   protected final int maxAdd;
   protected int added = 0;
   protected final long durExpire;
   protected long lastAdd = System.currentTimeMillis();

   public WordPrefixes(String name, WordPrefixesSettings settings) {
      super(name, settings);
      this.durExpire = settings.durExpire;
      this.maxAdd = settings.maxAdd;
   }

   public void start(MessageLetterCount message) {
      if (this.added > this.maxAdd || System.currentTimeMillis() - this.lastAdd > this.durExpire) {
         this.tree.clear();
         this.added = 0;
      }

   }

   public void clear() {
      super.clear();
      this.tree.clear();
      this.added = 0;
   }

   protected float getScore(List chars, long ts) {
      this.lastAdd = ts;
      int len = chars.size();
      SimpleTimedCharPrefixTree.SimpleTimedCharLookupEntry entry = (SimpleTimedCharPrefixTree.SimpleTimedCharLookupEntry)this.tree.lookup(chars, true);
      int depth = entry.depth;
      float score = 0.0F;

      for(int i = 0; i < depth; ++i) {
         long age = ts - entry.timeInsertion[i];
         if (age < this.durExpire) {
            score += 1.0F / (float)(depth - i) * (float)(this.durExpire - age) / (float)this.durExpire;
         }
      }

      if (depth == len) {
         score = (float)((double)score + 0.2);
         if (((TimedCharPrefixTree.SimpleTimedCharNode)entry.insertion).isEnd) {
            score = (float)((double)score + 0.2);
         }
      }

      if (len != depth) {
         this.added += len - depth;
      }

      return score;
   }

   public static class WordPrefixesSettings extends DigestedWords.DigestedWordsSettings {
      public int maxAdd = 1000;
      public long durExpire = 30000L;

      public WordPrefixesSettings() {
         this.split = true;
         this.compress = true;
      }

      public WordPrefixesSettings applyConfig(ConfigFile config, String prefix) {
         super.applyConfig(config, prefix);
         this.maxAdd = config.getInt(prefix + "size", this.maxAdd);
         this.durExpire = (long)(config.getDouble(prefix + "time", (double)((float)this.durExpire / 1000.0F)) * (double)1000.0F);
         return this;
      }
   }
}
