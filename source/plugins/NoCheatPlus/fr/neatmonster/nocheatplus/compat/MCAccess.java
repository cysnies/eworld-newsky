package fr.neatmonster.nocheatplus.compat;

import fr.neatmonster.nocheatplus.utilities.BlockCache;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public interface MCAccess {
   String getMCVersion();

   String getServerVersionTag();

   CommandMap getCommandMap();

   BlockCache getBlockCache(World var1);

   double getHeight(Entity var1);

   double getWidth(Entity var1);

   AlmostBoolean isBlockSolid(int var1);

   AlmostBoolean isBlockLiquid(int var1);

   AlmostBoolean isIllegalBounds(Player var1);

   double getJumpAmplifier(Player var1);

   double getFasterMovementAmplifier(Player var1);

   int getInvulnerableTicks(Player var1);

   void setInvulnerableTicks(Player var1, int var2);

   void dealFallDamage(Player var1, double var2);

   boolean isComplexPart(Entity var1);

   boolean shouldBeZombie(Player var1);

   void setDead(Player var1, int var2);

   long getKeepAliveTime(Player var1);

   boolean hasGravity(Material var1);
}
