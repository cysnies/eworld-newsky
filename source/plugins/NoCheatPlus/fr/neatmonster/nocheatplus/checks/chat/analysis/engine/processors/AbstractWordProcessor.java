package fr.neatmonster.nocheatplus.checks.chat.analysis.engine.processors;

import fr.neatmonster.nocheatplus.checks.chat.analysis.MessageLetterCount;
import fr.neatmonster.nocheatplus.checks.chat.analysis.WordLetterCount;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class AbstractWordProcessor implements WordProcessor {
   protected String name;
   protected float weight = 1.0F;

   public static final void releaseMap(Map map, int number) {
      List<K> rem = new LinkedList();
      int i = 0;

      for(Object key : map.keySet()) {
         rem.add(key);
         ++i;
         if (i > number) {
            break;
         }
      }

      for(Object key : rem) {
         map.remove(key);
      }

   }

   public AbstractWordProcessor(String name) {
      this.name = name;
   }

   public String getProcessorName() {
      return this.name;
   }

   public float getWeight() {
      return this.weight;
   }

   public void setWeight(float weight) {
      this.weight = weight;
   }

   public float process(MessageLetterCount message) {
      this.start(message);
      long ts = System.currentTimeMillis();
      float score = 0.0F;

      for(int index = 0; index < message.words.length; ++index) {
         WordLetterCount word = message.words[index];
         String key = word.word.toLowerCase();
         score += this.loop(ts, index, key, word) * (float)(word.word.length() + 1);
      }

      score /= (float)(message.message.length() + message.words.length);
      return score;
   }

   public void start(MessageLetterCount message) {
   }

   public void clear() {
   }

   public abstract float loop(long var1, int var3, String var4, WordLetterCount var5);
}
