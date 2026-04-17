package fr.neatmonster.nocheatplus.checks.fight;

import fr.neatmonster.nocheatplus.checks.access.ACheckData;
import fr.neatmonster.nocheatplus.checks.access.CheckDataFactory;
import fr.neatmonster.nocheatplus.checks.access.ICheckData;
import fr.neatmonster.nocheatplus.utilities.ActionFrequency;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.bukkit.entity.Player;

public class FightData extends ACheckData {
   public static final CheckDataFactory factory = new CheckDataFactory() {
      public final ICheckData getData(Player player) {
         return FightData.getData(player);
      }

      public ICheckData removeData(String playerName) {
         return FightData.removeData(playerName);
      }

      public void removeAllData() {
         FightData.clear();
      }
   };
   private static final Map playersMap = new HashMap();
   public double angleVL;
   public double criticalVL;
   public double directionVL;
   public double fastHealVL;
   public double godModeVL;
   public double knockbackVL;
   public double noSwingVL;
   public double reachVL;
   public double speedVL;
   public String lastWorld = "";
   public int lastAttackTick = 0;
   public double lastAttackedX = (double)Integer.MAX_VALUE;
   public double lastAttackedY;
   public double lastAttackedZ;
   public int thornsId = Integer.MIN_VALUE;
   public long regainHealthTime = 0L;
   public long damageTakenByEntityTick;
   public TreeMap angleHits = new TreeMap();
   public long directionLastViolationTime = 0L;
   public long fastHealRefTime = 0L;
   public long fastHealBuffer = 0L;
   public int godModeBuffer;
   public int godModeLastAge;
   public long godModeLastTime;
   public int godModeHealthDecreaseTick = 0;
   public double godModeHealth = (double)0.0F;
   public int lastDamageTick = 0;
   public int lastNoDamageTicks = 0;
   public int godModeAcc = 0;
   public long knockbackSprintTime;
   public boolean noSwingArmSwung;
   public long reachLastViolationTime;
   public double reachMod = (double)1.0F;
   public ActionFrequency selfHitVL = new ActionFrequency(6, 5000L);
   public final ActionFrequency speedBuckets;
   public int speedShortTermCount;
   public int speedShortTermTick;

   public static FightData getData(Player player) {
      if (!playersMap.containsKey(player.getName())) {
         playersMap.put(player.getName(), new FightData(FightConfig.getConfig(player)));
      }

      return (FightData)playersMap.get(player.getName());
   }

   public static ICheckData removeData(String playerName) {
      return (ICheckData)playersMap.remove(playerName);
   }

   public static void clear() {
      playersMap.clear();
   }

   public FightData(FightConfig cc) {
      this.speedBuckets = new ActionFrequency(cc.speedBuckets, cc.speedBucketDur);
      this.fastHealBuffer = cc.fastHealBuffer;
   }
}
