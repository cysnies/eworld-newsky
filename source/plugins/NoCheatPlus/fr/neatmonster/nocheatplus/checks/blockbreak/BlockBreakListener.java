package fr.neatmonster.nocheatplus.checks.blockbreak;

import fr.neatmonster.nocheatplus.checks.CheckListener;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.inventory.Items;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class BlockBreakListener extends CheckListener {
   private final Direction direction = (Direction)this.addCheck(new Direction());
   private final FastBreak fastBreak = (FastBreak)this.addCheck(new FastBreak());
   private final Frequency frequency = (Frequency)this.addCheck(new Frequency());
   private final NoSwing noSwing = (NoSwing)this.addCheck(new NoSwing());
   private final Reach reach = (Reach)this.addCheck(new Reach());
   private final WrongBlock wrongBlock = (WrongBlock)this.addCheck(new WrongBlock());
   private boolean isInstaBreak = false;

   public BlockBreakListener() {
      super(CheckType.BLOCKBREAK);
   }

   @EventHandler(
      ignoreCancelled = false,
      priority = EventPriority.LOWEST
   )
   public void onBlockBreak(BlockBreakEvent event) {
      Player player = event.getPlayer();
      if (Items.checkIllegalEnchantments(player, player.getItemInHand())) {
         event.setCancelled(true);
      }

      if (event.isCancelled()) {
         this.isInstaBreak = false;
      } else {
         Block block = event.getBlock();
         boolean cancelled = false;
         BlockBreakConfig cc = BlockBreakConfig.getConfig(player);
         BlockBreakData data = BlockBreakData.getData(player);
         long now = System.currentTimeMillis();
         GameMode gameMode = player.getGameMode();
         if (this.wrongBlock.isEnabled(player) && this.wrongBlock.check(player, block, cc, data, this.isInstaBreak)) {
            cancelled = true;
         }

         if (!cancelled && this.frequency.isEnabled(player) && this.frequency.check(player, cc, data)) {
            cancelled = true;
         }

         if (!cancelled && gameMode != GameMode.CREATIVE && this.fastBreak.isEnabled(player) && this.fastBreak.check(player, block, this.isInstaBreak, cc, data)) {
            cancelled = true;
         }

         if (!cancelled && this.noSwing.isEnabled(player) && this.noSwing.check(player, data)) {
            cancelled = true;
         }

         if (!cancelled && this.reach.isEnabled(player) && this.reach.check(player, block, data)) {
            cancelled = true;
         }

         if (!cancelled && this.direction.isEnabled(player) && this.direction.check(player, block, data)) {
            cancelled = true;
         }

         if (!cancelled && BlockProperties.isLiquid(block.getTypeId()) && !player.hasPermission("nocheatplus.checks.blockbreak.break.liquid") && !NCPExemptionManager.isExempted(player, CheckType.BLOCKBREAK_BREAK)) {
            cancelled = true;
         }

         if (cancelled) {
            event.setCancelled(cancelled);
            data.clickedX = block.getX();
            data.clickedY = block.getY();
            data.clickedZ = block.getZ();
         }

         if (this.isInstaBreak) {
            data.wasInstaBreak = now;
         } else {
            data.wasInstaBreak = 0L;
         }

         data.fastBreakBreakTime = now;
         this.isInstaBreak = false;
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerAnimation(PlayerAnimationEvent event) {
      BlockBreakData.getData(event.getPlayer()).noSwingArmSwung = true;
   }

   @EventHandler(
      ignoreCancelled = false,
      priority = EventPriority.LOWEST
   )
   public void onPlayerInteract(PlayerInteractEvent event) {
      this.isInstaBreak = false;
      if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
         this.checkBlockDamage(event.getPlayer(), event.getClickedBlock(), event);
      }
   }

   @EventHandler(
      ignoreCancelled = false,
      priority = EventPriority.MONITOR
   )
   public void onBlockDamage(BlockDamageEvent event) {
      if (!event.isCancelled() && event.getInstaBreak()) {
         this.isInstaBreak = true;
      } else {
         this.isInstaBreak = false;
      }

      this.checkBlockDamage(event.getPlayer(), event.getBlock(), event);
   }

   private void checkBlockDamage(Player player, Block block, Cancellable event) {
      long now = System.currentTimeMillis();
      BlockBreakData data = BlockBreakData.getData(player);
      if (block != null) {
         int tick = TickTask.getTick();
         if (tick < data.clickedTick || data.fastBreakBreakTime >= data.fastBreakfirstDamage || data.clickedX != block.getX() || data.clickedZ != block.getZ() || data.clickedY != block.getY() || tick - data.clickedTick > 1) {
            data.fastBreakfirstDamage = now;
            data.clickedX = block.getX();
            data.clickedY = block.getY();
            data.clickedZ = block.getZ();
            data.clickedTick = tick;
         }
      }
   }
}
