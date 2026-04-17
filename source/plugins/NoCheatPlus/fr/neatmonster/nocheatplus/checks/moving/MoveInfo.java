package fr.neatmonster.nocheatplus.checks.moving;

import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.utilities.BlockCache;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class MoveInfo {
   public final BlockCache cache;
   public final PlayerLocation from;
   public final PlayerLocation to;

   public MoveInfo(MCAccess mcAccess) {
      this.cache = mcAccess.getBlockCache((World)null);
      this.from = new PlayerLocation(mcAccess, (BlockCache)null);
      this.to = new PlayerLocation(mcAccess, (BlockCache)null);
   }

   public final void set(Player player, Location from, Location to, double yOnGround) {
      this.from.set(from, player, yOnGround);
      this.cache.setAccess(from.getWorld());
      this.from.setBlockCache(this.cache);
      if (to != null) {
         this.to.set(to, player, yOnGround);
         this.to.setBlockCache(this.cache);
      }

   }

   public final void cleanup() {
      this.from.cleanup();
      this.to.cleanup();
      this.cache.cleanup();
   }
}
