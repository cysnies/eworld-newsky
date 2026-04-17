package fr.neatmonster.nocheatplus.compat.cb2545;

import fr.neatmonster.nocheatplus.compat.AlmostBoolean;
import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.utilities.BlockCache;
import fr.neatmonster.nocheatplus.utilities.ReflectionUtil;
import net.minecraft.server.v1_4_6.AxisAlignedBB;
import net.minecraft.server.v1_4_6.Block;
import net.minecraft.server.v1_4_6.DamageSource;
import net.minecraft.server.v1_4_6.EntityComplexPart;
import net.minecraft.server.v1_4_6.EntityPlayer;
import net.minecraft.server.v1_4_6.MobEffectList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandMap;
import org.bukkit.craftbukkit.v1_4_6.CraftServer;
import org.bukkit.craftbukkit.v1_4_6.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_4_6.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class MCAccessCB2545 implements MCAccess {
   public MCAccessCB2545() {
      this.getCommandMap();
      ReflectionUtil.checkMembers("net.minecraft.server.v1_4_6.", new String[]{"Entity", "dead"});
   }

   public String getMCVersion() {
      return "1.4.6";
   }

   public String getServerVersionTag() {
      return "CB2545";
   }

   public CommandMap getCommandMap() {
      return ((CraftServer)Bukkit.getServer()).getCommandMap();
   }

   public BlockCache getBlockCache(World world) {
      return new BlockCacheCB2545(world);
   }

   public double getHeight(Entity entity) {
      net.minecraft.server.v1_4_6.Entity mcEntity = ((CraftEntity)entity).getHandle();
      double entityHeight = Math.max((double)mcEntity.length, Math.max((double)mcEntity.height, mcEntity.boundingBox.e - mcEntity.boundingBox.b));
      return entity instanceof LivingEntity ? Math.max(((LivingEntity)entity).getEyeHeight(), entityHeight) : entityHeight;
   }

   public AlmostBoolean isBlockSolid(int id) {
      Block block = Block.byId[id];
      return block != null && block.material != null ? AlmostBoolean.match(block.material.isSolid()) : AlmostBoolean.MAYBE;
   }

   public AlmostBoolean isBlockLiquid(int id) {
      Block block = Block.byId[id];
      return block != null && block.material != null ? AlmostBoolean.match(block.material.isLiquid()) : AlmostBoolean.MAYBE;
   }

   public double getWidth(Entity entity) {
      return (double)((CraftEntity)entity).getHandle().width;
   }

   public AlmostBoolean isIllegalBounds(Player player) {
      EntityPlayer entityPlayer = ((CraftPlayer)player).getHandle();
      if (entityPlayer.dead) {
         return AlmostBoolean.NO;
      } else {
         AxisAlignedBB box = entityPlayer.boundingBox;
         if (!entityPlayer.isSleeping()) {
            double dY = Math.abs(box.e - box.b);
            if (dY > 1.8) {
               return AlmostBoolean.YES;
            }

            if (dY < 0.1 && (double)entityPlayer.length >= 0.1) {
               return AlmostBoolean.YES;
            }
         }

         return AlmostBoolean.MAYBE;
      }
   }

   public double getJumpAmplifier(Player player) {
      EntityPlayer mcPlayer = ((CraftPlayer)player).getHandle();
      return mcPlayer.hasEffect(MobEffectList.JUMP) ? (double)mcPlayer.getEffect(MobEffectList.JUMP).getAmplifier() : Double.NEGATIVE_INFINITY;
   }

   public double getFasterMovementAmplifier(Player player) {
      EntityPlayer mcPlayer = ((CraftPlayer)player).getHandle();
      return mcPlayer.hasEffect(MobEffectList.FASTER_MOVEMENT) ? (double)mcPlayer.getEffect(MobEffectList.FASTER_MOVEMENT).getAmplifier() : Double.NEGATIVE_INFINITY;
   }

   public int getInvulnerableTicks(Player player) {
      return ((CraftPlayer)player).getHandle().invulnerableTicks;
   }

   public void setInvulnerableTicks(Player player, int ticks) {
      ((CraftPlayer)player).getHandle().invulnerableTicks = ticks;
   }

   public void dealFallDamage(Player player, double damage) {
      ((CraftPlayer)player).getHandle().damageEntity(DamageSource.FALL, (int)Math.round(damage));
   }

   public boolean isComplexPart(Entity entity) {
      return ((CraftEntity)entity).getHandle() instanceof EntityComplexPart;
   }

   public boolean shouldBeZombie(Player player) {
      EntityPlayer mcPlayer = ((CraftPlayer)player).getHandle();
      return !mcPlayer.dead && mcPlayer.getHealth() <= 0;
   }

   public void setDead(Player player, int deathTicks) {
      EntityPlayer mcPlayer = ((CraftPlayer)player).getHandle();
      mcPlayer.deathTicks = deathTicks;
      mcPlayer.dead = true;
   }

   public long getKeepAliveTime(Player player) {
      return Long.MIN_VALUE;
   }

   public boolean hasGravity(Material mat) {
      switch (mat) {
         case SAND:
         case GRAVEL:
            return true;
         default:
            return false;
      }
   }
}
