package fr.neatmonster.nocheatplus.checks.chat;

import fr.neatmonster.nocheatplus.actions.ActionList;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.access.AsyncCheckConfig;
import fr.neatmonster.nocheatplus.checks.access.CheckConfigFactory;
import fr.neatmonster.nocheatplus.checks.access.ICheckConfig;
import fr.neatmonster.nocheatplus.checks.chat.analysis.engine.EnginePlayerConfig;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.utilities.ColorUtil;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;

public class ChatConfig extends AsyncCheckConfig {
   public static final CheckConfigFactory factory = new CheckConfigFactory() {
      public final ICheckConfig getConfig(Player player) {
         return ChatConfig.getConfig(player);
      }
   };
   private static final Map worldsMap = new HashMap();
   public final boolean captchaCheck;
   public final String captchaCharacters;
   public final int captchaLength;
   public final String captchaQuestion;
   public final String captchaSuccess;
   public final int captchaTries;
   public final ActionList captchaActions;
   public final boolean colorCheck;
   public final ActionList colorActions;
   public final boolean commandsCheck;
   public final double commandsLevel;
   public final int commandsShortTermTicks;
   public final double commandsShortTermLevel;
   public final ActionList commandsActions;
   public final boolean textCheck;
   public final boolean textGlobalCheck;
   public final boolean textPlayerCheck;
   public final EnginePlayerConfig textEnginePlayerConfig;
   public final float textFreqNormFactor;
   public final float textFreqNormWeight;
   public final float textFreqNormMin;
   public final double textFreqNormLevel;
   public final ActionList textFreqNormActions;
   public final float textFreqShortTermFactor;
   public final float textFreqShortTermWeight;
   public final float textFreqShortTermLevel;
   public final float textFreqShortTermMin;
   public final ActionList textFreqShortTermActions;
   public final float textMessageLetterCount;
   public final float textMessageUpperCase;
   public final float textMessagePartition;
   public final float textMsgRepeatCancel;
   public final float textMsgAfterJoin;
   public final float textMsgRepeatSelf;
   public final float textMsgRepeatGlobal;
   public final float textMsgNoMoving;
   public final float textMessageLengthAv;
   public final float textMessageLengthMsg;
   public final float textMessageNoLetter;
   public final float textGlobalWeight;
   public final float textPlayerWeight;
   public boolean textEngineMaximum;
   public final boolean textDebug;
   public final boolean chatWarningCheck;
   public final float chatWarningLevel;
   public final String chatWarningMessage;
   public final long chatWarningTimeout;
   public final boolean loginsCheck;
   public final boolean loginsPerWorldCount;
   public final int loginsSeconds;
   public final int loginsLimit;
   public final String loginsKickMessage;
   public final long loginsStartupDelay;
   public final boolean opInConsoleOnly;
   public final boolean protectPlugins;
   public final String noCommandPermMessage;
   public final boolean relogCheck;
   public final String relogKickMessage;
   public final long relogTimeout;
   public final String relogWarningMessage;
   public final int relogWarningNumber;
   public final long relogWarningTimeout;
   public final ActionList relogActions;

   public static void clear() {
      synchronized(worldsMap) {
         worldsMap.clear();
      }
   }

   public static ChatConfig getConfig(Player player) {
      synchronized(worldsMap) {
         if (!worldsMap.containsKey(player.getWorld().getName())) {
            worldsMap.put(player.getWorld().getName(), new ChatConfig(ConfigManager.getConfigFileSync(player.getWorld().getName())));
         }

         return (ChatConfig)worldsMap.get(player.getWorld().getName());
      }
   }

   public ChatConfig(ConfigFile config) {
      super(config, "checks.chat.", new String[]{"nocheatplus.checks.chat.color", "nocheatplus.checks.chat.text", "nocheatplus.checks.chat.captcha"});
      this.captchaCheck = config.getBoolean("checks.chat.captcha.active");
      this.captchaCharacters = config.getString("checks.chat.captcha.characters");
      this.captchaLength = config.getInt("checks.chat.captcha.length");
      this.captchaQuestion = config.getString("checks.chat.captcha.question");
      this.captchaSuccess = config.getString("checks.chat.captcha.success");
      this.captchaTries = config.getInt("checks.chat.captcha.tries");
      this.captchaActions = (ActionList)config.getOptimizedActionList("checks.chat.captcha.actions", "nocheatplus.checks.chat.captcha");
      this.colorCheck = config.getBoolean("checks.chat.color.active");
      this.colorActions = (ActionList)config.getOptimizedActionList("checks.chat.color.actions", "nocheatplus.checks.chat.color");
      this.commandsCheck = config.getBoolean("checks.chat.commands.active");
      this.commandsLevel = config.getDouble("checks.chat.commands.level");
      this.commandsShortTermTicks = config.getInt("checks.chat.commands.shortterm.ticks");
      this.commandsShortTermLevel = config.getDouble("checks.chat.commands.shortterm.level");
      this.commandsActions = (ActionList)config.getOptimizedActionList("checks.chat.commands.actions", "nocheatplus.checks.chat.commands");
      this.textCheck = config.getBoolean("checks.chat.text.active");
      this.textGlobalCheck = config.getBoolean("checks.chat.text.global.active", true);
      this.textPlayerCheck = config.getBoolean("checks.chat.text.player.active", true);
      this.textEnginePlayerConfig = new EnginePlayerConfig(config);
      this.textFreqNormMin = (float)config.getDouble("checks.chat.text.frequency.normal.minimum");
      this.textFreqNormFactor = (float)config.getDouble("checks.chat.text.frequency.normal.factor");
      this.textFreqNormWeight = (float)config.getDouble("checks.chat.text.frequency.normal.weight");
      this.textFreqShortTermFactor = (float)config.getDouble("checks.chat.text.frequency.shortterm.factor");
      this.textFreqShortTermWeight = (float)config.getDouble("checks.chat.text.frequency.shortterm.weight");
      this.textFreqShortTermLevel = (float)config.getDouble("checks.chat.text.frequency.shortterm.level");
      this.textFreqShortTermMin = (float)config.getDouble("checks.chat.text.frequency.shortterm.minimum");
      this.textFreqShortTermActions = (ActionList)config.getOptimizedActionList("checks.chat.text.frequency.shortterm.actions", "nocheatplus.checks.chat.text");
      this.textMessageLetterCount = (float)config.getDouble("checks.chat.text.message.lettercount");
      this.textMessagePartition = (float)config.getDouble("checks.chat.text.message.partition");
      this.textMessageUpperCase = (float)config.getDouble("checks.chat.text.message.uppercase");
      this.textMsgRepeatCancel = (float)config.getDouble("checks.chat.text.message.repeatviolation");
      this.textMsgAfterJoin = (float)config.getDouble("checks.chat.text.message.afterjoin");
      this.textMsgRepeatSelf = (float)config.getDouble("checks.chat.text.message.repeatself");
      this.textMsgRepeatGlobal = (float)config.getDouble("checks.chat.text.message.repeatglobal");
      this.textMsgNoMoving = (float)config.getDouble("checks.chat.text.message.nomoving");
      this.textMessageLengthAv = (float)config.getDouble("checks.chat.text.message.words.lengthav");
      this.textMessageLengthMsg = (float)config.getDouble("checks.chat.text.message.words.lengthmsg");
      this.textMessageNoLetter = (float)config.getDouble("checks.chat.text.message.words.noletter");
      this.textGlobalWeight = (float)config.getDouble("checks.chat.text.global.weight", (double)1.0F);
      this.textPlayerWeight = (float)config.getDouble("checks.chat.text.player.weight", (double)1.0F);
      this.textFreqNormLevel = config.getDouble("checks.chat.text.frequency.normal.level");
      this.textEngineMaximum = config.getBoolean("checks.chat.text.maximum", true);
      this.textDebug = config.getBoolean("checks.chat.text.debug", false);
      this.textFreqNormActions = (ActionList)config.getOptimizedActionList("checks.chat.text.frequency.normal.actions", "nocheatplus.checks.chat.text");
      this.chatWarningCheck = config.getBoolean("checks.chat.warning.active");
      this.chatWarningLevel = (float)config.getDouble("checks.chat.warning.level");
      this.chatWarningMessage = config.getString("checks.chat.warning.message");
      this.chatWarningTimeout = config.getLong("checks.chat.warning.timeout") * 1000L;
      this.loginsCheck = config.getBoolean("checks.chat.logins.active");
      this.loginsPerWorldCount = config.getBoolean("checks.chat.logins.perworldcount");
      this.loginsSeconds = config.getInt("checks.chat.logins.seconds");
      this.loginsLimit = config.getInt("checks.chat.logins.limit");
      this.loginsKickMessage = config.getString("checks.chat.logins.kickmessage");
      this.loginsStartupDelay = (long)(config.getInt("checks.chat.logins.startupdelay") * 1000);
      this.relogCheck = config.getBoolean("checks.chat.relog.active");
      this.relogKickMessage = config.getString("checks.chat.relog.kickmessage");
      this.relogTimeout = config.getLong("checks.chat.relog.timeout");
      this.relogWarningMessage = config.getString("checks.chat.relog.warning.message");
      this.relogWarningNumber = config.getInt("checks.chat.relog.warning.number");
      this.relogWarningTimeout = config.getLong("checks.chat.relog.warning.timeout");
      this.relogActions = (ActionList)config.getOptimizedActionList("checks.chat.relog.actions", "nocheatplus.checks.chat.relog");
      this.opInConsoleOnly = config.getBoolean("miscellaneous.opinconsoleonly");
      ConfigFile globalConfig = ConfigManager.getConfigFile();
      this.protectPlugins = globalConfig.getBoolean("protection.plugins.hide.active");
      this.noCommandPermMessage = ColorUtil.replaceColors(globalConfig.getString("protection.plugins.hide.messages.nopermission"));
   }

   public boolean isEnabled(CheckType checkType) {
      switch (checkType) {
         case CHAT_COLOR:
            return this.colorCheck;
         case CHAT_TEXT:
            return this.textCheck;
         case CHAT_COMMANDS:
            return this.commandsCheck;
         case CHAT_CAPTCHA:
            return this.captchaCheck;
         case CHAT_RELOG:
            return this.relogCheck;
         case CHAT_LOGINS:
            return this.loginsCheck;
         default:
            return true;
      }
   }
}
