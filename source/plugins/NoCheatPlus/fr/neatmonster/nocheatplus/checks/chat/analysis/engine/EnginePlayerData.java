package fr.neatmonster.nocheatplus.checks.chat.analysis.engine;

import fr.neatmonster.nocheatplus.checks.chat.ChatConfig;
import fr.neatmonster.nocheatplus.checks.chat.analysis.engine.processors.FlatWords;
import fr.neatmonster.nocheatplus.checks.chat.analysis.engine.processors.SimilarWordsBKL;
import fr.neatmonster.nocheatplus.checks.chat.analysis.engine.processors.WordPrefixes;
import fr.neatmonster.nocheatplus.components.IData;
import java.util.ArrayList;
import java.util.List;

public class EnginePlayerData implements IData {
   public final List processors = new ArrayList(5);

   public EnginePlayerData(ChatConfig cc) {
      EnginePlayerConfig config = cc.textEnginePlayerConfig;
      if (config.ppWordsCheck) {
         this.processors.add(new FlatWords("ppWords", config.ppWordsSettings));
      }

      if (config.ppPrefixesCheck) {
         this.processors.add(new WordPrefixes("ppPrefixes", config.ppPrefixesSettings));
      }

      if (config.ppSimilarityCheck) {
         this.processors.add(new SimilarWordsBKL("ppSimilarity", config.ppSimilaritySettings));
      }

   }
}
