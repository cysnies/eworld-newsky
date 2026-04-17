package fr.neatmonster.nocheatplus.checks.blockplace;

import fr.neatmonster.nocheatplus.checks.access.ACheckData;
import fr.neatmonster.nocheatplus.checks.access.CheckDataFactory;
import fr.neatmonster.nocheatplus.checks.access.ICheckData;
import fr.neatmonster.nocheatplus.utilities.ActionFrequency;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;

public class BlockPlaceData extends ACheckData {
   public static final CheckDataFactory factory = new CheckDataFactory() {
      public final ICheckData getData(Player player) {
         return BlockPlaceData.getData(player);
      }

      public ICheckData removeData(String playerName) {
         return BlockPlaceData.removeData(playerName);
      }

      public void removeAllData() {
         BlockPlaceData.clear();
      }
   };
   private static final Map playersMap = new HashMap();
   public double autoSignVL = (double)0.0F;
   public double directionVL = (double)0.0F;
   public double fastPlaceVL = (double)0.0F;
   public double noSwingVL = (double)0.0F;
   public double reachVL = (double)0.0F;
   public double speedVL = (double)0.0F;
   public long autoSignPlacedTime = 0L;
   public long autoSignPlacedHash = 0L;
   public final ActionFrequency fastPlaceBuckets = new ActionFrequency(2, 1000L);
   public int fastPlaceShortTermTick = 0;
   public int fastPlaceShortTermCount = 0;
   public boolean noSwingArmSwung = true;
   public double reachDistance;
   public boolean speedLastRefused;
   public long speedLastTime;

   public static BlockPlaceData getData(Player player) {
      if (!playersMap.containsKey(player.getName())) {
         playersMap.put(player.getName(), new BlockPlaceData());
      }

      return (BlockPlaceData)playersMap.get(player.getName());
   }

   public static ICheckData removeData(String playerName) {
      return (ICheckData)playersMap.remove(playerName);
   }

   public static void clear() {
      playersMap.clear();
   }
}
