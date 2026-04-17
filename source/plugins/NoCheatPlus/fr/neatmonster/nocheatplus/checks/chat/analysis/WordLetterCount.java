package fr.neatmonster.nocheatplus.checks.chat.analysis;

import java.util.LinkedHashMap;
import java.util.Map;

public final class WordLetterCount {
   public final String word;
   public final Map counts;
   public final int upperCase;
   public final int notLetter;

   public WordLetterCount(String word) {
      this.word = word;
      char[] a = word.toCharArray();
      this.counts = new LinkedHashMap(a.length);
      int upperCase = 0;
      int notLetter = 0;

      for(int i = 0; i < a.length; ++i) {
         char c = a[i];
         if (!Character.isLetter(c)) {
            ++notLetter;
         }

         Character key;
         if (Character.isUpperCase(c)) {
            ++upperCase;
            key = Character.toLowerCase(c);
         } else {
            key = c;
         }

         Integer count = (Integer)this.counts.remove(key);
         if (count == null) {
            this.counts.put(key, 1);
         } else {
            this.counts.put(key, count + 1);
         }
      }

      this.notLetter = notLetter;
      this.upperCase = upperCase;
   }

   public float getNotLetterRatio() {
      return (float)this.notLetter / (float)this.word.length();
   }

   public float getLetterCountRatio() {
      return (float)this.counts.size() / (float)this.word.length();
   }

   public float getUpperCaseRatio() {
      return (float)this.upperCase / (float)this.word.length();
   }
}
