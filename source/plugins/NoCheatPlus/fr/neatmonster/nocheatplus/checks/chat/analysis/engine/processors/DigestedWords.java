package fr.neatmonster.nocheatplus.checks.chat.analysis.engine.processors;

import fr.neatmonster.nocheatplus.checks.chat.analysis.WordLetterCount;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class DigestedWords extends AbstractWordProcessor {
   protected boolean sort;
   protected boolean compress;
   protected boolean split;
   protected int minWordSize;
   protected int maxWordSize;
   protected final List letters;
   protected final List digits;
   protected final List other;

   public DigestedWords(String name, DigestedWordsSettings settings) {
      this(name);
      this.weight = settings.weight;
      this.minWordSize = settings.minWordSize;
      this.maxWordSize = settings.maxWordSize;
      this.sort = settings.sort;
      this.compress = settings.compress;
      this.split = settings.split;
   }

   public DigestedWords(String name) {
      super(name);
      this.sort = false;
      this.compress = false;
      this.split = false;
      this.minWordSize = 0;
      this.maxWordSize = 0;
      this.letters = new ArrayList(10);
      this.digits = new ArrayList(10);
      this.other = new ArrayList(10);
   }

   public float loop(long ts, int index, String key, WordLetterCount word) {
      this.letters.clear();
      this.digits.clear();
      this.other.clear();
      Collection<Character> chars;
      if (this.compress) {
         chars = word.counts.keySet();
      } else {
         chars = new ArrayList(word.word.length());

         for(int i = 0; i < word.word.length(); ++i) {
            char c = word.word.charAt(i);
            if (Character.isUpperCase(c)) {
               c = Character.toLowerCase(c);
            }

            chars.add(c);
         }
      }

      int len = chars.size();

      for(Character c : chars) {
         if (this.split && !Character.isLetter(c)) {
            if (Character.isDigit(c)) {
               this.digits.add(c);
            } else {
               this.other.add(c);
            }
         } else {
            this.letters.add(c);
         }
      }

      float score = 0.0F;
      if (this.prepare(this.letters)) {
         score += this.getScore(this.letters, ts) * (float)this.letters.size();
      }

      if (this.prepare(this.digits)) {
         score += this.getScore(this.digits, ts) * (float)this.digits.size();
      }

      if (this.prepare(this.other)) {
         score += this.getScore(this.other, ts) * (float)this.other.size();
      }

      return len == 0 ? 0.0F : score / (float)len;
   }

   protected boolean prepare(List chars) {
      if (chars.isEmpty()) {
         return false;
      } else {
         int size = chars.size();
         if (size < this.minWordSize) {
            return false;
         } else if (this.maxWordSize > 0 && size > this.maxWordSize) {
            return false;
         } else {
            if (this.sort) {
               Collections.sort(chars);
            }

            return true;
         }
      }
   }

   public void clear() {
      this.letters.clear();
      this.digits.clear();
      this.other.clear();
      super.clear();
   }

   public static final char[] toArray(Collection chars) {
      char[] a = new char[chars.size()];
      int i = 0;

      for(Character c : chars) {
         a[i] = c;
         ++i;
      }

      return a;
   }

   protected abstract float getScore(List var1, long var2);

   public static class DigestedWordsSettings {
      public boolean sort = false;
      public boolean compress = false;
      public boolean split = false;
      public float weight = 1.0F;
      public int minWordSize = 0;
      public int maxWordSize = 0;

      public DigestedWordsSettings applyConfig(ConfigFile config, String prefix) {
         this.sort = config.getBoolean(prefix + "sort", this.sort);
         this.compress = config.getBoolean(prefix + "compress", this.compress);
         this.split = config.getBoolean(prefix + "split", this.split);
         this.weight = (float)config.getDouble(prefix + "weight", (double)this.weight);
         this.minWordSize = config.getInt(prefix + "minwordsize", this.minWordSize);
         this.maxWordSize = config.getInt(prefix + "maxwordsize", this.maxWordSize);
         return this;
      }
   }
}
