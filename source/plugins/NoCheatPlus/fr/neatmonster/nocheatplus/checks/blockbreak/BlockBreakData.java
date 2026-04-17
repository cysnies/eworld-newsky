package fr.neatmonster.nocheatplus.checks.blockbreak;

import fr.neatmonster.nocheatplus.checks.access.ACheckData;
import fr.neatmonster.nocheatplus.checks.access.CheckDataFactory;
import fr.neatmonster.nocheatplus.checks.access.ICheckData;
import fr.neatmonster.nocheatplus.utilities.ActionFrequency;
import fr.neatmonster.nocheatplus.utilities.Stats;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;

public class BlockBreakData extends ACheckData {
   public static final CheckDataFactory factory = new CheckDataFactory() {
      public final ICheckData getData(Player player) {
         return BlockBreakData.getData(player);
      }

      public ICheckData removeData(String playerName) {
         return BlockBreakData.removeData(playerName);
      }

      public void removeAllData() {
         BlockBreakData.clear();
      }
   };
   private static final Map playersMap = new HashMap();
   public double directionVL;
   public double fastBreakVL;
   public double frequencyVL;
   public double noSwingVL;
   public double reachVL;
   public final ActionFrequency wrongBlockVL;
   public int clickedX;
   public int clickedY;
   public int clickedZ;
   public int clickedTick;
   public long wasInstaBreak;
   public final Stats stats;
   public final ActionFrequency fastBreakPenalties;
   public int fastBreakBuffer;
   public long fastBreakBreakTime = System.currentTimeMillis() - 1000L;
   public long fastBreakfirstDamage = System.currentTimeMillis();
   public final ActionFrequency frequencyBuckets;
   public int frequencyShortTermCount;
   public int frequencyShortTermTick;
   public boolean noSwingArmSwung = true;
   public double reachDistance;

   public static BlockBreakData getData(Player player) {
      if (!playersMap.containsKey(player.getName())) {
         playersMap.put(player.getName(), new BlockBreakData(BlockBreakConfig.getConfig(player)));
      }

      return (BlockBreakData)playersMap.get(player.getName());
   }

   public static ICheckData removeData(String playerName) {
      return (ICheckData)playersMap.remove(playerName);
   }

   public static void clear() {
      playersMap.clear();
   }

   public BlockBreakData(BlockBreakConfig cc) {
      this.stats = cc.fastBreakDebug ? new Stats("NCP/FASTBREAK") : null;
      this.fastBreakPenalties = new ActionFrequency(cc.fastBreakBuckets, cc.fastBreakBucketDur);
      this.frequencyBuckets = new ActionFrequency(cc.frequencyBuckets, cc.frequencyBucketDur);
      this.wrongBlockVL = new ActionFrequency(6, 20000L);
   }
}
