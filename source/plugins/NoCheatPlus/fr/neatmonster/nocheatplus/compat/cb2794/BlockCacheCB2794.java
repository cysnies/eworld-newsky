package fr.neatmonster.nocheatplus.compat.cb2794;

import fr.neatmonster.nocheatplus.utilities.BlockCache;
import net.minecraft.server.v1_6_R1.AxisAlignedBB;
import net.minecraft.server.v1_6_R1.Block;
import net.minecraft.server.v1_6_R1.EntityBoat;
import net.minecraft.server.v1_6_R1.IBlockAccess;
import net.minecraft.server.v1_6_R1.Material;
import net.minecraft.server.v1_6_R1.TileEntity;
import net.minecraft.server.v1_6_R1.Vec3DPool;
import net.minecraft.server.v1_6_R1.World;
import org.bukkit.craftbukkit.v1_6_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_6_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;

public class BlockCacheCB2794 extends BlockCache implements IBlockAccess {
   protected static final AxisAlignedBB useBox = AxisAlignedBB.a((double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F);
   protected World world;

   public BlockCacheCB2794(org.bukkit.World world) {
      this.setAccess(world);
   }

   public void setAccess(org.bukkit.World world) {
      this.world = world == null ? null : ((CraftWorld)world).getHandle();
   }

   public int fetchTypeId(int x, int y, int z) {
      return this.world.getTypeId(x, y, z);
   }

   public int fetchData(int x, int y, int z) {
      return this.world.getData(x, y, z);
   }

   public double[] fetchBounds(int x, int y, int z) {
      int id = this.getTypeId(x, y, z);
      Block block = Block.byId[id];
      if (block == null) {
         return null;
      } else {
         block.updateShape(this, x, y, z);
         return new double[]{block.u(), block.w(), block.y(), block.v(), block.x(), block.z()};
      }
   }

   public boolean standsOnEntity(Entity entity, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
      try {
         net.minecraft.server.v1_6_R1.Entity mcEntity = ((CraftEntity)entity).getHandle();
         AxisAlignedBB box = useBox.b(minX, minY, minZ, maxX, maxY, maxZ);

         for(net.minecraft.server.v1_6_R1.Entity other : this.world.getEntities(mcEntity, box)) {
            if (other instanceof EntityBoat) {
               if (minY >= other.locY && minY - other.locY <= 0.7) {
                  return true;
               }

               AxisAlignedBB otherBox = other.boundingBox;
               if (!(box.a > otherBox.d) && !(box.d < otherBox.a) && !(box.b > otherBox.e) && !(box.e < otherBox.b) && !(box.c > otherBox.f) && !(box.f < otherBox.c)) {
                  return true;
               }
            }
         }
      } catch (Throwable var20) {
      }

      return false;
   }

   public void cleanup() {
      super.cleanup();
      this.world = null;
   }

   public Material getMaterial(int x, int y, int z) {
      return this.world.getMaterial(x, y, z);
   }

   public TileEntity getTileEntity(int x, int y, int z) {
      return this.world.getTileEntity(x, y, z);
   }

   public Vec3DPool getVec3DPool() {
      return this.world.getVec3DPool();
   }

   public int getBlockPower(int arg0, int arg1, int arg2, int arg3) {
      return this.world.getBlockPower(arg0, arg1, arg2, arg3);
   }

   public boolean u(int arg0, int arg1, int arg2) {
      return this.world.u(arg0, arg1, arg2);
   }
}
