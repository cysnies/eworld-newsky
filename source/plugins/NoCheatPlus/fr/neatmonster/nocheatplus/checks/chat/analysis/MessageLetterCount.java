package fr.neatmonster.nocheatplus.checks.chat.analysis;

import java.util.HashMap;
import java.util.Map;

public class MessageLetterCount {
   public final String message;
   public final String split;
   public final WordLetterCount[] words;
   public final WordLetterCount fullCount;

   public MessageLetterCount(String message) {
      this(message, " ");
   }

   public MessageLetterCount(String message, String split) {
      this.message = message;
      this.split = split;
      String[] parts = message.split(split);
      this.words = new WordLetterCount[parts.length];
      this.fullCount = new WordLetterCount(message);
      Map<String, WordLetterCount> done = new HashMap(this.words.length);

      for(int i = 0; i < parts.length; ++i) {
         String word = parts[i];
         if (done.containsKey(word)) {
            this.words[i] = (WordLetterCount)done.get(word);
         } else {
            done.put(word, this.words[i] = new WordLetterCount(word));
         }
      }

      done.clear();
   }
}
