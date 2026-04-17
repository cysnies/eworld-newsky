package fr.neatmonster.nocheatplus.checks.moving;

import fr.neatmonster.nocheatplus.checks.access.ACheckData;
import fr.neatmonster.nocheatplus.checks.access.CheckDataFactory;
import fr.neatmonster.nocheatplus.checks.access.ICheckData;
import fr.neatmonster.nocheatplus.utilities.ActionAccumulator;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class MovingData extends ACheckData {
   private static final MediumLiftOff defaultMediumLiftOff;
   public static final CheckDataFactory factory;
   private static Map playersMap;
   public double creativeFlyVL = (double)0.0F;
   public double morePacketsVL = (double)0.0F;
   public double morePacketsVehicleVL = (double)0.0F;
   public double noFallVL = (double)0.0F;
   public double survivalFlyVL = (double)0.0F;
   public int bunnyhopDelay;
   public double jumpAmplifier;
   public long timeSprinting = 0L;
   public int verticalVelocityCounter;
   public double verticalFreedom;
   public double verticalVelocity;
   public int verticalVelocityUsed = 0;
   public final List hVelActive = new LinkedList();
   public final List hVelQueued = new LinkedList();
   public double fromX = Double.MAX_VALUE;
   public double fromY;
   public double fromZ;
   public double toX = Double.MAX_VALUE;
   public double toY;
   public double toZ;
   public boolean toWasReset;
   public boolean fromWasReset;
   public MediumLiftOff mediumLiftOff;
   private Location setBack;
   private Location teleported;
   public boolean creativeFlyPreviousRefused;
   public int morePacketsBuffer;
   public long morePacketsLastTime;
   public int morePacketsPackets;
   private Location morePacketsSetback;
   public int morePacketsVehicleBuffer;
   public long morePacketsVehicleLastTime;
   public int morePacketsVehiclePackets;
   private Location morePacketsVehicleSetback;
   public int morePacketsVehicleTaskId;
   public float noFallFallDistance;
   public double noFallMaxY;
   public boolean noFallAssumeGround;
   public boolean noFallSkipAirCheck;
   public double passableVL;
   public double sfHorizontalBuffer;
   public int sfHBufExtra;
   public int sfJumpPhase;
   public boolean sfDirty;
   public boolean sfLowJump;
   public boolean sfNoLowJump;
   public double sfLastYDist;
   public int sfHoverTicks;
   public int sfHoverLoginTicks;
   public int sfFlyOnIce;
   public long sfCobwebTime;
   public double sfCobwebVL;
   public long sfVLTime;
   public final ActionAccumulator vDistAcc;
   public boolean wasInVehicle;

   public MovingData() {
      this.mediumLiftOff = defaultMediumLiftOff;
      this.setBack = null;
      this.teleported = null;
      this.morePacketsBuffer = 50;
      this.morePacketsSetback = null;
      this.morePacketsVehicleBuffer = 50;
      this.morePacketsVehicleSetback = null;
      this.morePacketsVehicleTaskId = -1;
      this.noFallFallDistance = 0.0F;
      this.noFallMaxY = (double)0.0F;
      this.noFallAssumeGround = false;
      this.noFallSkipAirCheck = false;
      this.sfHorizontalBuffer = (double)0.0F;
      this.sfHBufExtra = 0;
      this.sfJumpPhase = 0;
      this.sfDirty = false;
      this.sfLowJump = false;
      this.sfNoLowJump = false;
      this.sfLastYDist = Double.MAX_VALUE;
      this.sfHoverTicks = -1;
      this.sfHoverLoginTicks = 0;
      this.sfFlyOnIce = 0;
      this.sfCobwebTime = 0L;
      this.sfCobwebVL = (double)0.0F;
      this.sfVLTime = 0L;
      this.vDistAcc = new ActionAccumulator(3, 3);
      this.wasInVehicle = false;
   }

   public static MovingData getData(Player player) {
      if (!playersMap.containsKey(player.getName())) {
         playersMap.put(player.getName(), new MovingData());
      }

      return (MovingData)playersMap.get(player.getName());
   }

   public static ICheckData removeData(String playerName) {
      return (ICheckData)playersMap.remove(playerName);
   }

   public static void clear() {
      playersMap.clear();
   }

   public static void onWorldUnload(World world) {
      String worldName = world.getName();

      for(MovingData data : playersMap.values()) {
         data.onWorldUnload(worldName);
      }

   }

   public void clearFlyData() {
      this.bunnyhopDelay = 0;
      this.sfJumpPhase = 0;
      this.jumpAmplifier = (double)0.0F;
      this.setBack = null;
      this.sfLastYDist = Double.MAX_VALUE;
      this.fromX = this.toX = Double.MAX_VALUE;
      this.clearAccounting();
      this.clearNoFallData();
      this.removeAllVelocity();
      this.sfHorizontalBuffer = (double)0.0F;
      this.sfHBufExtra = 0;
      this.toWasReset = this.fromWasReset = false;
      this.sfHoverTicks = this.sfHoverLoginTicks = -1;
      this.sfDirty = false;
      this.sfLowJump = false;
      this.mediumLiftOff = defaultMediumLiftOff;
   }

   public void onSetBack(Location setBack) {
      this.resetPositions(this.teleported);
      this.setSetBack(this.teleported);
      this.morePacketsSetback = this.morePacketsVehicleSetback = null;
      this.clearAccounting();
      this.sfHorizontalBuffer = Math.min((double)0.0F, this.sfHorizontalBuffer);
      this.sfHBufExtra = 0;
      this.toWasReset = this.fromWasReset = false;
      this.sfHoverTicks = -1;
      this.sfDirty = false;
      this.sfLowJump = false;
      this.mediumLiftOff = defaultMediumLiftOff;
      this.removeAllVelocity();
   }

   public void resetPositions(Location loc) {
      if (loc == null) {
         this.resetPositions(Double.MAX_VALUE, (double)0.0F, (double)0.0F);
      } else {
         this.resetPositions(loc.getX(), loc.getY(), loc.getZ());
      }

   }

   public void resetPositions(PlayerLocation loc) {
      if (loc == null) {
         this.resetPositions(Double.MAX_VALUE, (double)0.0F, (double)0.0F);
      } else {
         this.resetPositions(loc.getX(), loc.getY(), loc.getZ());
      }

   }

   public void resetPositions(double x, double y, double z) {
      this.fromX = this.toX = x;
      this.fromY = this.toY = y;
      this.fromZ = this.toZ = z;
      this.sfLastYDist = Double.MAX_VALUE;
      this.sfDirty = false;
      this.sfLowJump = false;
      this.mediumLiftOff = defaultMediumLiftOff;
   }

   public void clearAccounting() {
      this.vDistAcc.clear();
   }

   public void clearMorePacketsData() {
      this.morePacketsSetback = null;
      this.morePacketsVehicleSetback = null;
   }

   public void clearNoFallData() {
      this.noFallFallDistance = 0.0F;
      this.noFallMaxY = (double)0.0F;
      this.noFallSkipAirCheck = false;
   }

   public void setSetBack(PlayerLocation loc) {
      if (this.setBack == null) {
         this.setBack = loc.getLocation();
      } else {
         LocUtil.set(this.setBack, loc);
      }

   }

   public void setSetBack(Location loc) {
      if (this.setBack == null) {
         this.setBack = LocUtil.clone(loc);
      } else {
         LocUtil.set(this.setBack, loc);
      }

   }

   public Location getSetBack(Location ref) {
      return LocUtil.clone(this.setBack, ref);
   }

   public Location getSetBack(PlayerLocation ref) {
      return LocUtil.clone(this.setBack, ref);
   }

   public boolean hasSetBack() {
      return this.setBack != null;
   }

   public boolean hasSetBackWorldChanged(Location loc) {
      return this.setBack == null ? true : this.setBack.getWorld().equals(loc.getWorld());
   }

   public double getSetBackX() {
      return this.setBack.getX();
   }

   public double getSetBackY() {
      return this.setBack.getY();
   }

   public double getSetBackZ() {
      return this.setBack.getZ();
   }

   public void setSetBackY(double y) {
      this.setBack.setY(y);
   }

   public final Location getTeleported() {
      return this.teleported == null ? this.teleported : LocUtil.clone(this.teleported);
   }

   public final void setTeleported(Location loc) {
      this.teleported = LocUtil.clone(loc);
   }

   public boolean hasMorePacketsSetBack() {
      return this.morePacketsSetback != null;
   }

   public final void setMorePacketsSetBack(PlayerLocation loc) {
      if (this.morePacketsSetback == null) {
         this.morePacketsSetback = loc.getLocation();
      } else {
         LocUtil.set(this.morePacketsSetback, loc);
      }

   }

   public final void setMorePacketsSetBack(Location loc) {
      if (this.morePacketsSetback == null) {
         this.morePacketsSetback = LocUtil.clone(loc);
      } else {
         LocUtil.set(this.morePacketsSetback, loc);
      }

   }

   public Location getMorePacketsSetBack() {
      return LocUtil.clone(this.morePacketsSetback);
   }

   public boolean hasMorePacketsVehicleSetBack() {
      return this.morePacketsVehicleSetback != null;
   }

   public final void setMorePacketsVehicleSetBack(PlayerLocation loc) {
      if (this.morePacketsVehicleSetback == null) {
         this.morePacketsVehicleSetback = loc.getLocation();
      } else {
         LocUtil.set(this.morePacketsVehicleSetback, loc);
      }

   }

   public final void setMorePacketsVehicleSetBack(Location loc) {
      if (this.morePacketsVehicleSetback == null) {
         this.morePacketsVehicleSetback = LocUtil.clone(loc);
      } else {
         LocUtil.set(this.morePacketsVehicleSetback, loc);
      }

   }

   public final Location getMorePacketsVehicleSetBack() {
      return LocUtil.clone(this.morePacketsVehicleSetback);
   }

   public final void resetTeleported() {
      this.teleported = null;
   }

   public final void resetSetBack() {
      this.setBack = null;
   }

   public final void setTo(Location to) {
      this.toX = to.getX();
      this.toY = to.getY();
      this.toZ = to.getZ();
   }

   public void addHorizontalVelocity(Velocity vel) {
      this.hVelQueued.add(vel);
   }

   public void removeAllVelocity() {
      this.hVelActive.clear();
      this.hVelQueued.clear();
   }

   public void removeInvalidVelocity(int tick) {
      Iterator<Velocity> it = this.hVelActive.iterator();

      while(it.hasNext()) {
         Velocity vel = (Velocity)it.next();
         if (vel.valCount <= 0 || vel.value <= 0.001) {
            it.remove();
         }
      }

      it = this.hVelQueued.iterator();

      while(it.hasNext()) {
         Velocity vel = (Velocity)it.next();
         if (vel.actCount <= 0 || vel.tick < tick) {
            it.remove();
         }
      }

   }

   public void velocityTick() {
      for(Velocity vel : this.hVelActive) {
         --vel.valCount;
         vel.sum += vel.value;
         vel.value *= 0.93;
      }

      for(Iterator<Velocity> it = this.hVelQueued.iterator(); it.hasNext(); --((Velocity)it.next()).actCount) {
      }

   }

   public double getHorizontalFreedom() {
      double f = (double)0.0F;

      for(Velocity vel : this.hVelActive) {
         f += vel.value;
      }

      return f;
   }

   public double useHorizontalVelocity(double amount) {
      Iterator<Velocity> it = this.hVelQueued.iterator();
      double used = (double)0.0F;

      while(it.hasNext()) {
         Velocity vel = (Velocity)it.next();
         used += vel.value;
         this.hVelActive.add(vel);
         it.remove();
         if (used >= amount) {
            break;
         }
      }

      return used;
   }

   public boolean isSetBack(Location loc) {
      if (loc != null && this.setBack != null) {
         if (!loc.getWorld().getName().equals(this.setBack.getWorld().getName())) {
            return false;
         } else {
            return loc.getX() == this.setBack.getX() && loc.getY() == this.setBack.getY() && loc.getZ() == this.setBack.getZ();
         }
      } else {
         return false;
      }
   }

   public void onPlayerLeave() {
      this.removeAllVelocity();
   }

   public void onWorldUnload(String worldName) {
      if (this.teleported != null && worldName.equalsIgnoreCase(this.teleported.getWorld().getName())) {
         this.resetTeleported();
      }

      if (this.setBack != null && worldName.equalsIgnoreCase(this.setBack.getWorld().getName())) {
         this.clearFlyData();
      }

      if (this.morePacketsSetback != null && worldName.equalsIgnoreCase(this.morePacketsSetback.getWorld().getName()) || this.morePacketsVehicleSetback != null && worldName.equalsIgnoreCase(this.morePacketsVehicleSetback.getWorld().getName())) {
         this.clearMorePacketsData();
         this.clearNoFallData();
      }

   }

   static {
      defaultMediumLiftOff = MediumLiftOff.LIMIT_JUMP;
      factory = new CheckDataFactory() {
         public final ICheckData getData(Player player) {
            return MovingData.getData(player);
         }

         public ICheckData removeData(String playerName) {
            return MovingData.removeData(playerName);
         }

         public void removeAllData() {
            MovingData.clear();
         }
      };
      playersMap = new HashMap();
   }
}
