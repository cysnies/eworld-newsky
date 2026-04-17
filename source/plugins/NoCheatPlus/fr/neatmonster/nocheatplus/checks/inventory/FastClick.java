package fr.neatmonster.nocheatplus.checks.inventory;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.utilities.InventoryUtil;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class FastClick extends Check {
   public FastClick() {
      super(CheckType.INVENTORY_FASTCLICK);
   }

   public boolean check(Player player, long now, InventoryView view, int slot, ItemStack cursor, ItemStack clicked, boolean isShiftClick, InventoryData data, InventoryConfig cc) {
      float amount;
      if (cursor != null && cc.fastClickTweaks1_5) {
         Material cursorMat = cursor.getType();
         int cursorAmount = Math.max(1, cursor.getAmount());
         Material clickedMat = clicked == null ? Material.AIR : clicked.getType();
         if ((cursorMat == data.fastClickLastCursor || isShiftClick && clicked != null && clicked.getType() == data.fastClickLastClicked) && cursorMat != Material.AIR && cursorAmount == data.fastClickLastCursorAmount) {
            if (clickedMat == Material.AIR || clickedMat == cursorMat || isShiftClick && clickedMat == data.fastClickLastClicked) {
               amount = Math.min(cc.fastClickNormalLimit, cc.fastClickShortTermLimit) / (float)(isShiftClick && clickedMat != Material.AIR ? (double)1.0F + (double)Math.max(cursorAmount, InventoryUtil.getStackCount(view, clicked)) : (double)cursorAmount) * 0.75F;
            } else {
               amount = 1.0F;
            }
         } else {
            amount = 1.0F;
         }

         data.fastClickLastCursor = cursorMat;
         data.fastClickLastClicked = clickedMat;
         data.fastClickLastCursorAmount = cursorAmount;
      } else {
         data.fastClickLastCursor = null;
         data.fastClickLastClicked = null;
         data.fastClickLastCursorAmount = 0;
         amount = 1.0F;
      }

      data.fastClickFreq.add(now, amount);
      float shortTerm = data.fastClickFreq.bucketScore(0);
      if (shortTerm > cc.fastClickShortTermLimit) {
         shortTerm /= TickTask.getLag(data.fastClickFreq.bucketDuration());
      }

      shortTerm -= cc.fastClickShortTermLimit;
      float normal = data.fastClickFreq.score(1.0F);
      if (normal > cc.fastClickNormalLimit) {
         normal /= TickTask.getLag(data.fastClickFreq.bucketDuration() * (long)data.fastClickFreq.numberOfBuckets());
      }

      normal -= cc.fastClickNormalLimit;
      double violation = (double)Math.max(shortTerm, normal);
      boolean cancel = false;
      if (violation > (double)0.0F) {
         data.fastClickVL += violation;
         ViolationData vd = new ViolationData(this, player, data.fastClickVL + violation, violation, cc.fastClickActions);
         cancel = this.executeActions(vd);
      }

      if (cc.debug && player.hasPermission("nocheatplus.admin.debug")) {
         player.sendMessage("FastClick: " + data.fastClickFreq.bucketScore(0) + " | " + data.fastClickFreq.score(1.0F) + " | cursor=" + cursor + " | clicked=" + clicked);
      }

      return cancel;
   }
}
