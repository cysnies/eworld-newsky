package fr.neatmonster.nocheatplus.checks.chat.analysis.engine;

import fr.neatmonster.nocheatplus.checks.chat.analysis.engine.processors.FlatWords;
import fr.neatmonster.nocheatplus.checks.chat.analysis.engine.processors.SimilarWordsBKL;
import fr.neatmonster.nocheatplus.checks.chat.analysis.engine.processors.WordPrefixes;
import fr.neatmonster.nocheatplus.config.ConfigFile;

public class EnginePlayerConfig {
   public final boolean ppPrefixesCheck;
   public final WordPrefixes.WordPrefixesSettings ppPrefixesSettings;
   public final boolean ppWordsCheck;
   public final FlatWords.FlatWordsSettings ppWordsSettings;
   public final boolean ppSimilarityCheck;
   public final SimilarWordsBKL.SimilarWordsBKLSettings ppSimilaritySettings;

   public EnginePlayerConfig(ConfigFile config) {
      this.ppWordsCheck = config.getBoolean("checks.chat.text.player.words.active", false);
      if (this.ppWordsCheck) {
         this.ppWordsSettings = new FlatWords.FlatWordsSettings();
         this.ppWordsSettings.maxSize = 150;
         this.ppWordsSettings.applyConfig(config, "checks.chat.text.player.words.");
      } else {
         this.ppWordsSettings = null;
      }

      this.ppPrefixesCheck = config.getBoolean("checks.chat.text.player.prefixes.active", false);
      if (this.ppPrefixesCheck) {
         this.ppPrefixesSettings = new WordPrefixes.WordPrefixesSettings();
         this.ppPrefixesSettings.maxAdd = 320;
         this.ppPrefixesSettings.applyConfig(config, "checks.chat.text.player.prefixes.");
      } else {
         this.ppPrefixesSettings = null;
      }

      this.ppSimilarityCheck = config.getBoolean("checks.chat.text.player.similarity.active", false);
      if (this.ppSimilarityCheck) {
         this.ppSimilaritySettings = new SimilarWordsBKL.SimilarWordsBKLSettings();
         this.ppSimilaritySettings.maxSize = 100;
         this.ppSimilaritySettings.applyConfig(config, "checks.chat.text.player.similarity.");
      } else {
         this.ppSimilaritySettings = null;
      }

   }
}
