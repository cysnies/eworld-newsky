package fr.neatmonster.nocheatplus.checks.combined;

import fr.neatmonster.nocheatplus.checks.access.ACheckData;
import fr.neatmonster.nocheatplus.checks.access.CheckDataFactory;
import fr.neatmonster.nocheatplus.checks.access.ICheckData;
import fr.neatmonster.nocheatplus.utilities.ActionFrequency;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;

public class CombinedData extends ACheckData {
   public static final CheckDataFactory factory = new CheckDataFactory() {
      public final ICheckData getData(Player player) {
         return CombinedData.getData(player);
      }

      public ICheckData removeData(String playerName) {
         return CombinedData.removeData(playerName);
      }

      public void removeAllData() {
         CombinedData.clear();
      }
   };
   private static final Map playersMap = new HashMap();
   public double bedLeaveVL = (double)0.0F;
   public double improbableVL = (double)0.0F;
   public double munchHausenVL = (double)0.0F;
   public int invulnerableTick = Integer.MIN_VALUE;
   public float lastYaw;
   public long lastYawTime;
   public float sumYaw;
   public final ActionFrequency yawFreq = new ActionFrequency(3, 333L);
   public long timeFreeze = 0L;
   public boolean wasInBed = false;
   public final ActionFrequency improbableCount = new ActionFrequency(20, 3000L);
   public String lastWorld = "";
   public long lastJoinTime;
   public long lastLogoutTime;
   public long lastMoveTime;

   public static CombinedData getData(Player player) {
      String playerName = player.getName();
      CombinedData data = (CombinedData)playersMap.get(playerName);
      if (data == null) {
         data = new CombinedData(player);
         playersMap.put(playerName, data);
      }

      return data;
   }

   public static ICheckData removeData(String playerName) {
      return (ICheckData)playersMap.remove(playerName);
   }

   public static void clear() {
      playersMap.clear();
   }

   public CombinedData(Player player) {
   }
}
