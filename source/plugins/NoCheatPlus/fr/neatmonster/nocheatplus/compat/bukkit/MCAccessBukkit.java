package fr.neatmonster.nocheatplus.compat.bukkit;

import fr.neatmonster.nocheatplus.compat.AlmostBoolean;
import fr.neatmonster.nocheatplus.compat.BridgeHealth;
import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.compat.blocks.BlockPropertiesSetup;
import fr.neatmonster.nocheatplus.config.WorldConfigProvider;
import fr.neatmonster.nocheatplus.utilities.BlockCache;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.PotionUtil;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.ComplexEntityPart;
import org.bukkit.entity.ComplexLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffectType;

public class MCAccessBukkit implements MCAccess, BlockPropertiesSetup {
   public MCAccessBukkit() {
      Material.AIR.isSolid();
      Material.AIR.isOccluding();
      Material.AIR.isTransparent();
   }

   public String getMCVersion() {
      return "1.4.6|1.4.7|1.5.x|1.6.1|1.6.2|?";
   }

   public String getServerVersionTag() {
      return "Bukkit-API";
   }

   public CommandMap getCommandMap() {
      return null;
   }

   public BlockCache getBlockCache(World world) {
      return new BlockCacheBukkit(world);
   }

   public double getHeight(Entity entity) {
      double entityHeight = (double)1.0F;
      return entity instanceof LivingEntity ? Math.max(((LivingEntity)entity).getEyeHeight(), (double)1.0F) : (double)1.0F;
   }

   public AlmostBoolean isBlockSolid(int id) {
      Material mat = Material.getMaterial(id);
      return mat == null ? AlmostBoolean.MAYBE : AlmostBoolean.match(mat.isSolid());
   }

   public AlmostBoolean isBlockLiquid(int id) {
      Material mat = Material.getMaterial(id);
      if (mat == null) {
         return AlmostBoolean.MAYBE;
      } else {
         switch (mat) {
            case STATIONARY_LAVA:
            case STATIONARY_WATER:
            case WATER:
            case LAVA:
               return AlmostBoolean.YES;
            default:
               return AlmostBoolean.NO;
         }
      }
   }

   public double getWidth(Entity entity) {
      return (double)0.6F;
   }

   public AlmostBoolean isIllegalBounds(Player player) {
      if (player.isDead()) {
         return AlmostBoolean.NO;
      } else {
         if (!player.isSleeping()) {
         }

         return AlmostBoolean.MAYBE;
      }
   }

   public double getJumpAmplifier(Player player) {
      return PotionUtil.getPotionEffectAmplifier(player, PotionEffectType.JUMP);
   }

   public double getFasterMovementAmplifier(Player player) {
      return PotionUtil.getPotionEffectAmplifier(player, PotionEffectType.SPEED);
   }

   public int getInvulnerableTicks(Player player) {
      return player.getNoDamageTicks();
   }

   public void setInvulnerableTicks(Player player, int ticks) {
      player.setLastDamageCause(new EntityDamageEvent(player, DamageCause.CUSTOM, 500));
      player.setNoDamageTicks(ticks);
   }

   public void dealFallDamage(Player player, double damage) {
      player.damage((int)Math.round(damage));
   }

   public boolean isComplexPart(Entity entity) {
      return entity instanceof ComplexEntityPart || entity instanceof ComplexLivingEntity;
   }

   public boolean shouldBeZombie(Player player) {
      return BridgeHealth.getHealth(player) <= (double)0.0F && !player.isDead();
   }

   public void setDead(Player player, int deathTicks) {
      player.setHealth(0);
      player.damage(1);
   }

   public void setupBlockProperties(WorldConfigProvider worldConfigProvider) {
      Set<Integer> fullBlocks = new HashSet();

      for(Material mat : new Material[]{Material.GLASS, Material.GLOWSTONE, Material.ICE, Material.LEAVES, Material.COMMAND, Material.BEACON, Material.PISTON_BASE}) {
         fullBlocks.add(mat.getId());
      }

      for(Material mat : Material.values()) {
         if (mat.isBlock()) {
            int id = mat.getId();
            if (id >= 0 && id < 4096 && !fullBlocks.contains(id) && (!mat.isOccluding() || !mat.isSolid() || mat.isTransparent())) {
               long flags = 8L;
               if ((BlockProperties.isSolid(id) || BlockProperties.isGround(id)) && !BlockProperties.isLiquid(id)) {
                  flags |= 4096L;
               }

               BlockProperties.setBlockFlags(id, BlockProperties.getBlockFlags(id) | flags);
            }
         }
      }

      for(Material mat : new Material[]{Material.ENDER_PORTAL_FRAME}) {
         int id = mat.getId();
         long flags = 4104L;
         BlockProperties.setBlockFlags(id, BlockProperties.getBlockFlags(id) | 4104L);
      }

   }

   public long getKeepAliveTime(Player player) {
      return Long.MIN_VALUE;
   }

   public boolean hasGravity(Material mat) {
      try {
         return mat.hasGravity();
      } catch (Throwable var3) {
         switch (mat) {
            case SAND:
            case GRAVEL:
               return true;
            default:
               return false;
         }
      }
   }
}
