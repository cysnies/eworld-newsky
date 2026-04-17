package fr.neatmonster.nocheatplus.checks.chat.analysis.engine;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.chat.ChatConfig;
import fr.neatmonster.nocheatplus.checks.chat.ChatData;
import fr.neatmonster.nocheatplus.checks.chat.analysis.MessageLetterCount;
import fr.neatmonster.nocheatplus.checks.chat.analysis.engine.processors.FlatWords;
import fr.neatmonster.nocheatplus.checks.chat.analysis.engine.processors.SimilarWordsBKL;
import fr.neatmonster.nocheatplus.checks.chat.analysis.engine.processors.WordPrefixes;
import fr.neatmonster.nocheatplus.checks.chat.analysis.engine.processors.WordProcessor;
import fr.neatmonster.nocheatplus.components.ConsistencyChecker;
import fr.neatmonster.nocheatplus.components.IData;
import fr.neatmonster.nocheatplus.components.IHaveCheckType;
import fr.neatmonster.nocheatplus.components.IRemoveData;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.logging.LogUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;

public class LetterEngine implements IRemoveData, IHaveCheckType, ConsistencyChecker {
   protected final List processors = new ArrayList();
   protected final EnginePlayerDataMap dataMap;

   public LetterEngine(ConfigFile config) {
      if (config.getBoolean("checks.chat.text.global.words.active", false)) {
         FlatWords.FlatWordsSettings settings = new FlatWords.FlatWordsSettings();
         settings.maxSize = 1000;
         settings.applyConfig(config, "checks.chat.text.global.words.");
         this.processors.add(new FlatWords("glWords", settings));
      }

      if (config.getBoolean("checks.chat.text.global.prefixes.active", false)) {
         WordPrefixes.WordPrefixesSettings settings = new WordPrefixes.WordPrefixesSettings();
         settings.maxAdd = 2000;
         settings.applyConfig(config, "checks.chat.text.global.prefixes.");
         this.processors.add(new WordPrefixes("glPrefixes", settings));
      }

      if (config.getBoolean("checks.chat.text.global.similarity.active", false)) {
         SimilarWordsBKL.SimilarWordsBKLSettings settings = new SimilarWordsBKL.SimilarWordsBKLSettings();
         settings.maxSize = 1000;
         settings.applyConfig(config, "checks.chat.text.global.similarity.");
         this.processors.add(new SimilarWordsBKL("glSimilarity", settings));
      }

      this.dataMap = new EnginePlayerDataMap(600000L, 100, 0.75F);
   }

   public Map process(MessageLetterCount letterCount, String playerName, ChatConfig cc, ChatData data) {
      Map<String, Float> result = new HashMap();
      if (cc.textGlobalCheck) {
         for(WordProcessor processor : this.processors) {
            try {
               result.put(processor.getProcessorName(), processor.process(letterCount) * cc.textGlobalWeight);
            } catch (Exception e) {
               LogUtil.logSevere("[NoCheatPlus] chat.text: processor(" + processor.getProcessorName() + ") generated an exception: " + e.getClass().getSimpleName() + ": " + e.getMessage());
               LogUtil.logSevere((Throwable)e);
            }
         }
      }

      if (cc.textPlayerCheck) {
         EnginePlayerData engineData = this.dataMap.get(playerName, cc);

         for(WordProcessor processor : engineData.processors) {
            try {
               result.put(processor.getProcessorName(), processor.process(letterCount) * cc.textPlayerWeight);
            } catch (Exception e) {
               LogUtil.logSevere("[NoCheatPlus] chat.text: processor(" + processor.getProcessorName() + ") generated an exception: " + e.getClass().getSimpleName() + ": " + e.getMessage());
               LogUtil.logSevere((Throwable)e);
            }
         }
      }

      return result;
   }

   public void clear() {
      for(WordProcessor processor : this.processors) {
         processor.clear();
      }

      this.processors.clear();
      this.dataMap.clear();
   }

   public IData removeData(String playerName) {
      return (IData)this.dataMap.remove(playerName);
   }

   public void removeAllData() {
      this.dataMap.clear();
   }

   public final CheckType getCheckType() {
      return CheckType.CHAT_TEXT;
   }

   public void checkConsistency(Player[] onlinePlayers) {
      long now = System.currentTimeMillis();
      if (now < this.dataMap.lastExpired) {
         this.dataMap.clear();
      } else {
         if (now - this.dataMap.lastExpired > this.dataMap.durExpire) {
            this.dataMap.expire(now - this.dataMap.durExpire);
         }

      }
   }
}
