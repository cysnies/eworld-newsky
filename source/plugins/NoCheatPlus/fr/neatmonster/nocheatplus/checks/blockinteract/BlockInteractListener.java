package fr.neatmonster.nocheatplus.checks.blockinteract;

import fr.neatmonster.nocheatplus.checks.CheckListener;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.combined.CombinedConfig;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class BlockInteractListener extends CheckListener {
   private final Direction direction = (Direction)this.addCheck(new Direction());
   private final Reach reach = (Reach)this.addCheck(new Reach());
   private final Visible visible = (Visible)this.addCheck(new Visible());
   private final Speed speed = (Speed)this.addCheck(new Speed());

   public BlockInteractListener() {
      super(CheckType.BLOCKINTERACT);
   }

   @EventHandler(
      ignoreCancelled = false,
      priority = EventPriority.LOWEST
   )
   protected void onPlayerInteract(PlayerInteractEvent event) {
      Action action = event.getAction();
      Block block = event.getClickedBlock();
      if (block != null) {
         Player player = event.getPlayer();
         switch (action) {
            case RIGHT_CLICK_BLOCK:
               ItemStack stack = player.getItemInHand();
               if (stack != null && stack.getTypeId() == Material.ENDER_PEARL.getId() && !BlockProperties.isPassable(block.getTypeId())) {
                  CombinedConfig ccc = CombinedConfig.getConfig(player);
                  if (ccc.enderPearlCheck && ccc.enderPearlPreventClickBlock) {
                     event.setUseItemInHand(Result.DENY);
                  }
               }
            case LEFT_CLICK_BLOCK:
               if (event.isCancelled()) {
                  return;
               }

               BlockInteractData data = BlockInteractData.getData(player);
               BlockInteractConfig cc = BlockInteractConfig.getConfig(player);
               boolean cancelled = false;
               BlockFace face = event.getBlockFace();
               Location loc = player.getLocation();
               if (!cancelled && this.speed.isEnabled(player) && this.speed.check(player, data, cc)) {
                  cancelled = true;
               }

               if (!cancelled && this.reach.isEnabled(player) && this.reach.check(player, loc, block, data, cc)) {
                  cancelled = true;
               }

               if (!cancelled && this.direction.isEnabled(player) && this.direction.check(player, loc, block, data, cc)) {
                  cancelled = true;
               }

               if (!cancelled && this.visible.isEnabled(player) && this.visible.check(player, loc, block, face, action, data, cc)) {
                  cancelled = true;
               }

               if (cancelled) {
                  event.setUseInteractedBlock(Result.DENY);
                  event.setUseItemInHand(Result.DENY);
                  event.setCancelled(true);
               }

               return;
            default:
         }
      }
   }
}
