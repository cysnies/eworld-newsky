package fr.neatmonster.nocheatplus.checks.chat;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.AsyncCheck;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.checks.chat.analysis.MessageLetterCount;
import fr.neatmonster.nocheatplus.checks.chat.analysis.WordLetterCount;
import fr.neatmonster.nocheatplus.checks.chat.analysis.engine.LetterEngine;
import fr.neatmonster.nocheatplus.checks.combined.CombinedData;
import fr.neatmonster.nocheatplus.components.INotifyReload;
import fr.neatmonster.nocheatplus.components.NoCheatPlusAPI;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.logging.LogUtil;
import fr.neatmonster.nocheatplus.utilities.ColorUtil;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;

public class Text extends AsyncCheck implements INotifyReload {
   private LetterEngine engine = null;
   private String lastCancelledMessage = "";
   private long lastCancelledTime = 0L;
   private String lastGlobalMessage = "";
   private long lastGlobalTime = 0L;

   public Text() {
      super(CheckType.CHAT_TEXT);
      this.init();
   }

   public boolean check(Player player, String message, ICaptcha captcha, boolean isMainThread) {
      ChatConfig cc = ChatConfig.getConfig(player);
      ChatData data = ChatData.getData(player);
      synchronized(data) {
         return this.unsafeCheck(player, message, captcha, cc, data, isMainThread);
      }
   }

   private void init() {
      ConfigFile config = ConfigManager.getConfigFile();
      NoCheatPlusAPI api = NCPAPIProvider.getNoCheatPlusAPI();
      if (this.engine != null) {
         this.engine.clear();
         api.removeComponent(this.engine);
      }

      this.engine = new LetterEngine(config);
      api.addComponent(this.engine);
   }

   public void onReload() {
      synchronized(this.engine) {
         this.engine.clear();
      }

      this.init();
   }

   private boolean unsafeCheck(Player player, String message, ICaptcha captcha, ChatConfig cc, ChatData data, boolean isMainThread) {
      if (captcha.shouldCheckCaptcha(cc, data)) {
         captcha.checkCaptcha(player, message, cc, data, isMainThread);
         return true;
      } else {
         long time = System.currentTimeMillis();
         String lcMessage = message.trim().toLowerCase();
         boolean cancel = false;
         boolean debug = cc.textDebug || cc.debug;
         List<String> debugParts;
         if (debug) {
            debugParts = new LinkedList();
            debugParts.add("[NoCheatPlus][chat.text] Message (" + player.getName() + "/" + message.length() + "): ");
         } else {
            debugParts = null;
         }

         data.chatFrequency.update(time);
         float score = 0.0F;
         MessageLetterCount letterCounts = new MessageLetterCount(message);
         int msgLen = message.length();
         if (letterCounts.fullCount.upperCase > msgLen / 3) {
            float wUpperCase = 0.6F * letterCounts.fullCount.getUpperCaseRatio();
            score += wUpperCase * cc.textMessageUpperCase;
         }

         if (msgLen > 4) {
            float fullRep = letterCounts.fullCount.getLetterCountRatio();
            float wRepetition = (float)msgLen / 15.0F * Math.abs(0.5F - fullRep);
            score += wRepetition * cc.textMessageLetterCount;
            float fnWords = (float)letterCounts.words.length / (float)msgLen;
            if (fnWords > 0.75F) {
               score += fnWords * cc.textMessagePartition;
            }
         }

         CombinedData cData = CombinedData.getData(player);
         long timeout = 8000L;
         if (cc.textMsgRepeatSelf != 0.0F && time - data.chatLastTime < 8000L && StringUtil.isSimilar(lcMessage, data.chatLastMessage, 0.8F)) {
            float timeWeight = (float)(8000L - (time - data.chatLastTime)) / 8000.0F;
            score += cc.textMsgRepeatSelf * timeWeight;
         }

         if (cc.textMsgRepeatGlobal != 0.0F && time - this.lastGlobalTime < 8000L && StringUtil.isSimilar(lcMessage, this.lastGlobalMessage, 0.8F)) {
            float timeWeight = (float)(8000L - (time - this.lastGlobalTime)) / 8000.0F;
            score += cc.textMsgRepeatGlobal * timeWeight;
         }

         if (cc.textMsgRepeatCancel != 0.0F && time - this.lastCancelledTime < 8000L && StringUtil.isSimilar(lcMessage, this.lastCancelledMessage, 0.8F)) {
            float timeWeight = (float)(8000L - (time - this.lastCancelledTime)) / 8000.0F;
            score += cc.textMsgRepeatCancel * timeWeight;
         }

         if (cc.textMsgAfterJoin != 0.0F && time - cData.lastJoinTime < 8000L) {
            float timeWeight = (float)(8000L - (time - cData.lastJoinTime)) / 8000.0F;
            score += cc.textMsgAfterJoin * timeWeight;
         }

         if (cc.textMsgNoMoving != 0.0F && time - cData.lastMoveTime > 8000L) {
            score += cc.textMsgNoMoving;
         }

         float wWords = 0.0F;
         float avwLen = (float)msgLen / (float)letterCounts.words.length;

         for(WordLetterCount word : letterCounts.words) {
            float wWord = 0.0F;
            int wLen = word.word.length();
            float fLenAv = Math.abs(avwLen - (float)wLen) / avwLen;
            wWord += fLenAv * cc.textMessageLengthAv;
            float fLenMsg = (float)wLen / (float)msgLen;
            wWord += fLenMsg * cc.textMessageLengthMsg;
            float notLetter = word.getNotLetterRatio();
            notLetter *= notLetter;
            wWord += notLetter * cc.textMessageNoLetter;
            wWord *= wWord;
            wWords += wWord;
         }

         wWords /= (float)letterCounts.words.length;
         score += wWords;
         if (debug && score > 0.0F) {
            debugParts.add("Simple score: " + StringUtil.fdec3.format((double)score));
         }

         float wEngine = 0.0F;
         Map<String, Float> engMap;
         synchronized(this.engine) {
            engMap = this.engine.process(letterCounts, player.getName(), cc, data);

            for(Float res : engMap.values()) {
               if (cc.textEngineMaximum) {
                  wEngine = Math.max(wEngine, res);
               } else {
                  wEngine += res;
               }
            }
         }

         score += wEngine;
         float normalScore = Math.max(cc.textFreqNormMin, score);
         data.chatFrequency.add(time, normalScore);
         float accumulated = cc.textFreqNormWeight * data.chatFrequency.score(cc.textFreqNormFactor);
         boolean normalViolation = (double)accumulated > cc.textFreqNormLevel;
         float shortTermScore = Math.max(cc.textFreqShortTermMin, score);
         data.chatShortTermFrequency.add(time, shortTermScore);
         float shortTermAccumulated = cc.textFreqShortTermWeight * data.chatShortTermFrequency.score(cc.textFreqShortTermFactor);
         boolean shortTermViolation = shortTermAccumulated > cc.textFreqShortTermLevel;
         if (!normalViolation && !shortTermViolation) {
            if (!cc.chatWarningCheck || time - data.chatWarningTime <= cc.chatWarningTimeout || !((double)(100.0F * accumulated) / cc.textFreqNormLevel > (double)cc.chatWarningLevel) && !(100.0F * shortTermAccumulated / cc.textFreqShortTermLevel > cc.chatWarningLevel)) {
               data.textVL *= 0.95;
               if (normalScore < 2.0F * cc.textFreqNormWeight && shortTermScore < 2.0F * cc.textFreqShortTermWeight) {
                  data.textVL = (double)0.0F;
               }
            } else {
               player.sendMessage(ColorUtil.replaceColors(cc.chatWarningMessage));
               data.chatWarningTime = time;
            }
         } else {
            this.lastCancelledMessage = lcMessage;
            this.lastCancelledTime = time;
            double added;
            if (shortTermViolation) {
               added = (double)(shortTermAccumulated - cc.textFreqShortTermLevel) / (double)3.0F;
            } else {
               added = ((double)accumulated - cc.textFreqNormLevel) / (double)10.0F;
            }

            data.textVL += added;
            if (captcha.shouldStartCaptcha(cc, data)) {
               captcha.sendNewCaptcha(player, cc, data);
               cancel = true;
            } else if (shortTermViolation) {
               if (this.executeActions(player, data.textVL, added, cc.textFreqShortTermActions, isMainThread)) {
                  cancel = true;
               }
            } else if (normalViolation && this.executeActions(player, data.textVL, added, cc.textFreqNormActions, isMainThread)) {
               cancel = true;
            }
         }

         if (debug) {
            List<String> keys = new LinkedList(engMap.keySet());
            Collections.sort(keys);

            for(String key : keys) {
               Float s = (Float)engMap.get(key);
               if (s > 0.0F) {
                  debugParts.add(key + ":" + StringUtil.fdec3.format(s));
               }
            }

            if (wEngine > 0.0F) {
               debugParts.add("Engine score (" + (cc.textEngineMaximum ? "max" : "sum") + "): " + StringUtil.fdec3.format((double)wEngine));
            }

            debugParts.add("Final score: " + StringUtil.fdec3.format((double)score));
            debugParts.add("Normal: min=" + StringUtil.fdec3.format((double)cc.textFreqNormMin) + ", weight=" + StringUtil.fdec3.format((double)cc.textFreqNormWeight) + " => accumulated=" + StringUtil.fdec3.format((double)accumulated));
            debugParts.add("Short-term: min=" + StringUtil.fdec3.format((double)cc.textFreqShortTermMin) + ", weight=" + StringUtil.fdec3.format((double)cc.textFreqShortTermWeight) + " => accumulated=" + StringUtil.fdec3.format((double)shortTermAccumulated));
            debugParts.add("vl: " + StringUtil.fdec3.format(data.textVL));
            LogUtil.scheduleLogInfo(debugParts, " | ");
            debugParts.clear();
         }

         this.lastGlobalMessage = data.chatLastMessage = lcMessage;
         this.lastGlobalTime = data.chatLastTime = time;
         return cancel;
      }
   }

   protected Map getParameterMap(ViolationData violationData) {
      Map<ParameterName, String> parameters = super.getParameterMap(violationData);
      parameters.put(ParameterName.IP, violationData.player.getAddress().toString().substring(1).split(":")[0]);
      return parameters;
   }
}
