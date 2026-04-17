package fr.neatmonster.nocheatplus.checks.chat;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.components.IData;
import fr.neatmonster.nocheatplus.components.IRemoveData;
import fr.neatmonster.nocheatplus.utilities.ActionFrequency;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;

public class Logins extends Check implements IRemoveData {
   private final Map counts = new HashMap();

   public Logins() {
      super(CheckType.CHAT_LOGINS);
   }

   private ActionFrequency getActionFrequency(String worldName, int buckets, long durBucket, boolean perWorldCount) {
      if (!perWorldCount) {
         worldName = "";
      }

      ActionFrequency freq = (ActionFrequency)this.counts.get(worldName);
      if (freq == null) {
         freq = new ActionFrequency(buckets, durBucket);
      }

      this.counts.put(worldName, freq);
      return freq;
   }

   public boolean check(Player player, ChatConfig cc, ChatData data) {
      long now = System.currentTimeMillis();
      if (now - TickTask.getTimeStart() < cc.loginsStartupDelay) {
         return false;
      } else {
         long durBucket = 1000L * (long)cc.loginsSeconds / 6L;
         ActionFrequency freq = this.getActionFrequency(player.getWorld().getName(), 6, durBucket, cc.loginsPerWorldCount);
         freq.update(now);
         boolean cancel = freq.score(1.0F) > (float)cc.loginsLimit;
         if (!cancel) {
            freq.add(1.0F);
         }

         return cancel;
      }
   }

   public void onReload() {
      this.counts.clear();
   }

   public IData removeData(String playerName) {
      return null;
   }

   public void removeAllData() {
      this.counts.clear();
   }
}
