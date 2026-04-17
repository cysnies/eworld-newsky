package fr.neatmonster.nocheatplus.checks.blockinteract;

import fr.neatmonster.nocheatplus.checks.access.ACheckData;
import fr.neatmonster.nocheatplus.checks.access.CheckDataFactory;
import fr.neatmonster.nocheatplus.checks.access.ICheckData;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;

public class BlockInteractData extends ACheckData {
   public static final CheckDataFactory factory = new CheckDataFactory() {
      public final ICheckData getData(Player player) {
         return BlockInteractData.getData(player);
      }

      public ICheckData removeData(String playerName) {
         return BlockInteractData.removeData(playerName);
      }

      public void removeAllData() {
         BlockInteractData.clear();
      }
   };
   private static final Map playersMap = new HashMap();
   public double directionVL = (double)0.0F;
   public double reachVL = (double)0.0F;
   public double speedVL = (double)0.0F;
   public double visibleVL = (double)0.0F;
   public double reachDistance;
   public long speedTime = 0L;
   public int speedCount = 0;

   public static BlockInteractData getData(Player player) {
      if (!playersMap.containsKey(player.getName())) {
         playersMap.put(player.getName(), new BlockInteractData());
      }

      return (BlockInteractData)playersMap.get(player.getName());
   }

   public static ICheckData removeData(String playerName) {
      return (ICheckData)playersMap.remove(playerName);
   }

   public static void clear() {
      playersMap.clear();
   }
}
