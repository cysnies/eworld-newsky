package fr.neatmonster.nocheatplus.checks.chat;

import fr.neatmonster.nocheatplus.checks.access.AsyncCheckData;
import fr.neatmonster.nocheatplus.checks.access.CheckDataFactory;
import fr.neatmonster.nocheatplus.checks.access.ICheckData;
import fr.neatmonster.nocheatplus.utilities.ActionFrequency;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;

public class ChatData extends AsyncCheckData {
   public static final CheckDataFactory factory = new CheckDataFactory() {
      public final ICheckData getData(Player player) {
         return ChatData.getData(player);
      }

      public ICheckData removeData(String playerName) {
         return ChatData.removeData(playerName);
      }

      public void removeAllData() {
         ChatData.clear();
      }
   };
   private static final Map playersMap = new HashMap();
   public double captchaVL;
   public double colorVL;
   public double commandsVL;
   public double textVL;
   public double relogVL;
   public int captchTries;
   public String captchaGenerated;
   public boolean captchaStarted;
   public final ActionFrequency commandsWeights = new ActionFrequency(5, 1000L);
   public long commandsShortTermTick;
   public double commandsShortTermWeight;
   public final ActionFrequency chatFrequency = new ActionFrequency(10, 3000L);
   public final ActionFrequency chatShortTermFrequency = new ActionFrequency(6, 500L);
   public String chatLastMessage;
   public long chatLastTime;
   public long chatWarningTime;
   public int relogWarnings;
   public long relogWarningTime;

   public static synchronized ChatData getData(Player player) {
      if (!playersMap.containsKey(player.getName())) {
         playersMap.put(player.getName(), new ChatData());
      }

      return (ChatData)playersMap.get(player.getName());
   }

   public static synchronized ICheckData removeData(String playerName) {
      return (ICheckData)playersMap.remove(playerName);
   }

   public static synchronized void clear() {
      playersMap.clear();
   }

   public synchronized void reset() {
      this.captchTries = this.relogWarnings = 0;
      this.captchaVL = (double)0.0F;
      this.textVL = (double)0.0F;
      long now = System.currentTimeMillis();
      this.chatFrequency.clear(now);
      this.chatShortTermFrequency.clear(now);
      this.chatLastTime = this.relogWarningTime = 0L;
      this.captchaGenerated = this.chatLastMessage = "";
      this.chatLastTime = 0L;
      this.chatWarningTime = 0L;
      this.commandsShortTermTick = 0L;
      this.commandsWeights.clear(now);
   }
}
