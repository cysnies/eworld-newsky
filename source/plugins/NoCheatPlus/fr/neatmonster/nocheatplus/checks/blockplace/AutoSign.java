package fr.neatmonster.nocheatplus.checks.blockplace;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class AutoSign extends Check {
   private static long maxEditTime = 1500L;
   private static long minEditTime = 150L;
   private static long minLineTime = 50L;
   private static long minCharTime = 50L;
   private final List tags = new ArrayList();
   final Set chars = new HashSet(60);

   public AutoSign() {
      super(CheckType.BLOCKPLACE_AUTOSIGN);
   }

   public boolean check(Player player, Block block, String[] lines) {
      long time = System.currentTimeMillis();
      this.tags.clear();
      BlockPlaceData data = BlockPlaceData.getData(player);
      Material mat = block.getType();
      if (mat == Material.WALL_SIGN) {
         mat = Material.SIGN_POST;
      }

      if (data.autoSignPlacedHash != (long)BlockPlaceListener.getBlockPlaceHash(block, mat)) {
         this.tags.add("block_mismatch");
         return this.handleViolation(player, maxEditTime, data);
      } else if (time < data.autoSignPlacedTime) {
         data.autoSignPlacedTime = 0L;
         return false;
      } else {
         long editTime = time - data.autoSignPlacedTime;
         long expected = this.getExpectedEditTime(lines);
         expected = (long)((float)expected / TickTask.getLag(expected));
         if (expected > editTime) {
            this.tags.add("edit_time");
            return this.handleViolation(player, expected - editTime, data);
         } else {
            return false;
         }
      }
   }

   private long getExpectedEditTime(String[] lines) {
      long expected = minEditTime;
      int n = 0;

      for(String line : lines) {
         if (line != null) {
            line = line.trim().toLowerCase();
            if (!line.isEmpty()) {
               this.chars.clear();
               ++n;

               for(char c : line.toCharArray()) {
                  this.chars.add(c);
               }

               expected += minCharTime * (long)this.chars.size();
            }
         }
      }

      if (n > 1) {
         expected += minLineTime * (long)n;
      }

      return expected;
   }

   private boolean handleViolation(Player player, long violationTime, BlockPlaceData data) {
      double addedVL = (double)10.0F * (double)Math.min(maxEditTime, violationTime) / (double)maxEditTime;
      data.autoSignVL += addedVL;
      ViolationData vd = new ViolationData(this, player, data.autoSignVL, addedVL, BlockPlaceConfig.getConfig(player).autoSignActions);
      if (vd.needsParameters()) {
         vd.setParameter(ParameterName.TAGS, StringUtil.join(this.tags, "+"));
      }

      return this.executeActions(vd);
   }
}
