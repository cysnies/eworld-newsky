package fr.neatmonster.nocheatplus.checks.blockbreak;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.combined.Improbable;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class WrongBlock extends Check {
   public WrongBlock() {
      super(CheckType.BLOCKBREAK_WRONGBLOCK);
   }

   public boolean check(Player player, Block block, BlockBreakConfig cc, BlockBreakData data, boolean isInstaBreak) {
      boolean cancel = false;
      boolean wrongTime = data.fastBreakfirstDamage < data.fastBreakBreakTime;
      int dist = Math.abs(data.clickedX - block.getX()) + Math.abs(data.clickedY - block.getY()) + Math.abs(data.clickedZ - block.getZ());
      long now = System.currentTimeMillis();
      boolean wrongBlock;
      if (dist == 0) {
         if (wrongTime) {
            data.fastBreakBreakTime = now;
            data.fastBreakfirstDamage = now;
         }

         wrongBlock = false;
      } else if (dist == 1) {
         if (now - data.wasInstaBreak < 60L) {
            wrongBlock = false;
         } else {
            wrongBlock = true;
         }
      } else {
         wrongBlock = true;
      }

      if (wrongBlock) {
         if ((cc.fastBreakDebug || cc.debug) && player.hasPermission("nocheatplus.admin.debug")) {
            player.sendMessage("WrongBlock failure with dist: " + dist);
         }

         data.wrongBlockVL.add(now, (float)(dist + 1) / 2.0F);
         float score = data.wrongBlockVL.score(0.9F);
         if (score > cc.wrongBLockLevel) {
            if (this.executeActions(player, (double)score, (double)1.0F, cc.wrongBlockActions)) {
               cancel = true;
            }

            if (Improbable.check(player, 2.0F, now, "blockbreak.wrongblock")) {
               cancel = true;
            }
         }
      }

      return cancel;
   }
}
