package fr.neatmonster.nocheatplus.checks.blockplace;

import fr.neatmonster.nocheatplus.checks.CheckListener;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.combined.Combined;
import fr.neatmonster.nocheatplus.checks.combined.CombinedConfig;
import fr.neatmonster.nocheatplus.checks.combined.Improbable;
import fr.neatmonster.nocheatplus.checks.moving.MovingConfig;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class BlockPlaceListener extends CheckListener {
   private static final int p1 = 73856093;
   private static final int p2 = 19349663;
   private static final int p3 = 83492791;
   private final AutoSign autoSign = (AutoSign)this.addCheck(new AutoSign());
   private final Direction direction = (Direction)this.addCheck(new Direction());
   private final FastPlace fastPlace = (FastPlace)this.addCheck(new FastPlace());
   private final NoSwing noSwing = (NoSwing)this.addCheck(new NoSwing());
   private final Reach reach = (Reach)this.addCheck(new Reach());
   private final Speed speed = (Speed)this.addCheck(new Speed());

   private static final int getHash(int x, int y, int z) {
      return 73856093 * x ^ 19349663 * y ^ 83492791 * z;
   }

   public static int getCoordHash(Block block) {
      return getHash(block.getX(), block.getY(), block.getZ());
   }

   public static int getBlockPlaceHash(Block block, Material mat) {
      int hash = getCoordHash(block);
      if (mat != null) {
         hash |= mat.name().hashCode();
      }

      hash |= block.getWorld().getName().hashCode();
      return hash;
   }

   public BlockPlaceListener() {
      super(CheckType.BLOCKPLACE);
   }

   @EventHandler(
      ignoreCancelled = true,
      priority = EventPriority.LOWEST
   )
   public void onBlockPlace(BlockPlaceEvent event) {
      Block block = event.getBlockPlaced();
      Block blockAgainst = event.getBlockAgainst();
      if (block != null && blockAgainst != null) {
         Material mat = block.getType();
         Player player = event.getPlayer();
         boolean cancelled = false;
         int againstId = blockAgainst.getTypeId();
         if (BlockProperties.isLiquid(againstId)) {
            if ((mat != Material.WATER_LILY || !BlockProperties.isLiquid(block.getRelative(BlockFace.DOWN).getTypeId())) && !player.hasPermission("nocheatplus.checks.blockplace.against.liquids") && !NCPExemptionManager.isExempted(player, CheckType.BLOCKPLACE_AGAINST)) {
               cancelled = true;
            }
         } else if (againstId == Material.AIR.getId() && !player.hasPermission("nocheatplus.checks.blockplace.against.air") && !NCPExemptionManager.isExempted(player, CheckType.BLOCKPLACE_AGAINST)) {
            cancelled = true;
         }

         BlockPlaceData data = BlockPlaceData.getData(player);
         if (mat == Material.SIGN_POST || mat == Material.WALL_SIGN) {
            data.autoSignPlacedTime = System.currentTimeMillis();
            data.autoSignPlacedHash = (long)getBlockPlaceHash(block, Material.SIGN_POST);
         }

         if (this.fastPlace.isEnabled(player)) {
            if (this.fastPlace.check(player, block)) {
               cancelled = true;
            } else {
               Improbable.feed(player, 0.5F, System.currentTimeMillis());
            }
         }

         if (!cancelled && mat != Material.WATER_LILY && this.noSwing.isEnabled(player) && this.noSwing.check(player, data)) {
            cancelled = true;
         }

         if (!cancelled && this.reach.isEnabled(player) && this.reach.check(player, block, data)) {
            cancelled = true;
         }

         if (!cancelled && this.direction.isEnabled(player) && this.direction.check(player, block, blockAgainst, data)) {
            cancelled = true;
         }

         if (cancelled) {
            event.setCancelled(cancelled);
         }

      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onSignChange(SignChangeEvent event) {
      if (event.getClass() == SignChangeEvent.class) {
         Player player = event.getPlayer();
         Block block = event.getBlock();
         String[] lines = event.getLines();
         if (block != null && lines != null && player != null) {
            if (this.autoSign.isEnabled(player) && this.autoSign.check(player, block, lines)) {
               event.setCancelled(true);
            }

         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerAnimation(PlayerAnimationEvent event) {
      BlockPlaceData.getData(event.getPlayer()).noSwingArmSwung = true;
   }

   @EventHandler(
      ignoreCancelled = true,
      priority = EventPriority.LOWEST
   )
   public void onPlayerInteract(PlayerInteractEvent event) {
      if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
         Player player = event.getPlayer();
         ItemStack stack = player.getItemInHand();
         if (stack != null) {
            Material type = stack.getType();
            if (type == Material.BOAT) {
               Block block = event.getClickedBlock();
               Material mat = block.getType();
               if (mat == Material.WATER || mat == Material.STATIONARY_WATER) {
                  return;
               }

               Block relBlock = block.getRelative(event.getBlockFace());
               Material relMat = relBlock.getType();
               if (relMat == Material.WATER || relMat == Material.STATIONARY_WATER) {
                  return;
               }

               if (!player.hasPermission("nocheatplus.checks.blockplace.boatsanywhere")) {
                  event.setCancelled(true);
               }
            } else if (type == Material.MONSTER_EGG && this.speed.isEnabled(player) && this.speed.check(player)) {
               event.setCancelled(true);
            }

         }
      }
   }

   @EventHandler(
      ignoreCancelled = true,
      priority = EventPriority.LOWEST
   )
   public void onProjectileLaunch(ProjectileLaunchEvent event) {
      Projectile entity = event.getEntity();
      Entity shooter = entity.getShooter();
      if (shooter instanceof Player) {
         EntityType type = event.getEntityType();
         switch (type) {
            case ENDER_PEARL:
            case ENDER_SIGNAL:
            case EGG:
            case SNOWBALL:
            case THROWN_EXP_BOTTLE:
            case SPLASH_POTION:
               Player player = (Player)shooter;
               boolean cancel = false;
               if (this.speed.isEnabled(player)) {
                  long now = System.currentTimeMillis();
                  Location loc = player.getLocation();
                  if (Combined.checkYawRate(player, loc.getYaw(), now, loc.getWorld().getName())) {
                     cancel = true;
                  }

                  if (this.speed.check(player)) {
                     cancel = true;
                  } else if (Improbable.check(player, 0.6F, now, "blockplace.speed")) {
                     cancel = true;
                  }
               }

               if (!cancel && type == EntityType.ENDER_PEARL && CombinedConfig.getConfig(player).enderPearlCheck) {
                  if (!BlockProperties.isPassable(entity.getLocation())) {
                     cancel = true;
                  } else if (!BlockProperties.isPassable(player.getEyeLocation(), entity.getLocation())) {
                     cancel = true;
                  } else {
                     Material mat = player.getLocation().getBlock().getType();
                     long flags = 522L;
                     if (mat != Material.AIR && (BlockProperties.getBlockFlags(mat.getId()) & 522L) == 0L && !this.mcAccess.hasGravity(mat) && !BlockProperties.isPassable(player.getLocation(), entity.getLocation()) && !BlockProperties.isOnGroundOrResetCond(player, player.getLocation(), MovingConfig.getConfig(player).yOnGround)) {
                        cancel = true;
                     }
                  }
               }

               if (cancel) {
                  event.setCancelled(true);
               }

               return;
            default:
         }
      }
   }
}
