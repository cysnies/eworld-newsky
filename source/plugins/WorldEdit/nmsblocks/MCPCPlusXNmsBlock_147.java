import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.ByteTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.DoubleTag;
import com.sk89q.jnbt.EndTag;
import com.sk89q.jnbt.FloatTag;
import com.sk89q.jnbt.IntArrayTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.LongTag;
import com.sk89q.jnbt.NBTConstants;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.TileEntityBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.NmsBlock;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.foundation.Block;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_4_R1.CraftWorld;

public class MCPCPlusXNmsBlock_147 extends NmsBlock {
   private static final Logger logger;
   private static Field compoundMapField;
   private static final Field nmsBlock_isTileEntityField;
   private bq nbtData = null;

   public static boolean verify() {
      try {
         Class.forName("org.bukkit.craftbukkit.v1_4_R1.CraftWorld");
      } catch (Throwable var1) {
         return false;
      }

      return nmsBlock_isTileEntityField != null;
   }

   public MCPCPlusXNmsBlock_147(int var1, int var2, TileEntityBlock var3) {
      super(var1, var2);
      this.nbtData = (bq)fromNative(var3.getNbtData());
   }

   public MCPCPlusXNmsBlock_147(int var1, int var2, bq var3) {
      super(var1, var2);
      this.nbtData = var3;
   }

   private bq getNmsData(Vector var1) {
      if (this.nbtData == null) {
         return null;
      } else {
         this.nbtData.a("x", new bx("x", var1.getBlockX()));
         this.nbtData.a("y", new bx("y", var1.getBlockY()));
         this.nbtData.a("z", new bx("z", var1.getBlockZ()));
         return this.nbtData;
      }
   }

   public boolean hasNbtData() {
      return this.nbtData != null;
   }

   public String getNbtId() {
      return this.nbtData == null ? "" : this.nbtData.i("id");
   }

   public CompoundTag getNbtData() {
      return this.nbtData == null ? new CompoundTag(this.getNbtId(), new HashMap()) : (CompoundTag)toNative(this.nbtData);
   }

   public void setNbtData(CompoundTag var1) throws DataException {
      if (var1 == null) {
         this.nbtData = null;
      }

      this.nbtData = (bq)fromNative(var1);
   }

   public static MCPCPlusXNmsBlock_147 get(World var0, Vector var1, int var2, int var3) {
      if (!hasTileEntity(var2)) {
         return null;
      } else {
         any var4 = ((CraftWorld)var0).getHandle().q(var1.getBlockX(), var1.getBlockY(), var1.getBlockZ());
         if (var4 != null) {
            bq var5 = new bq();
            var4.b(var5);
            return new MCPCPlusXNmsBlock_147(var2, var3, var5);
         } else {
            return null;
         }
      }
   }

   public static boolean set(World var0, Vector var1, BaseBlock var2) {
      bq var3 = null;
      if (!hasTileEntity(var0.getBlockTypeIdAt(var1.getBlockX(), var1.getBlockY(), var1.getBlockZ()))) {
         return false;
      } else {
         if (var2 instanceof MCPCPlusXNmsBlock_147) {
            MCPCPlusXNmsBlock_147 var4 = (MCPCPlusXNmsBlock_147)var2;
            var3 = var4.getNmsData(var1);
         } else if (var2 instanceof TileEntityBlock) {
            MCPCPlusXNmsBlock_147 var5 = new MCPCPlusXNmsBlock_147(var2.getId(), var2.getData(), var2);
            var3 = var5.getNmsData(var1);
         }

         if (var3 != null) {
            any var6 = ((CraftWorld)var0).getHandle().q(var1.getBlockX(), var1.getBlockY(), var1.getBlockZ());
            if (var6 != null) {
               var6.a(var3);
               return true;
            }
         }

         return false;
      }
   }

   public static boolean setSafely(BukkitWorld var0, Vector var1, Block var2, boolean var3) {
      int var4 = var1.getBlockX();
      int var5 = var1.getBlockY();
      int var6 = var1.getBlockZ();
      CraftWorld var7 = (CraftWorld)var0.getWorld();
      boolean var8 = var7.getHandle().c(var4, var5, var6, var2.getId(), var2.getData());
      if (var2 instanceof BaseBlock) {
         var0.copyToWorld(var1, (BaseBlock)var2);
      }

      if (var8) {
         if (var3) {
            var7.getHandle().f(var4, var5, var6, var2.getId());
         } else {
            var7.getHandle().i(var4, var5, var6);
         }
      }

      return var8;
   }

   public static boolean hasTileEntity(int var0) {
      amq var1 = getNmsBlock(var0);
      if (var1 == null) {
         return false;
      } else {
         try {
            return nmsBlock_isTileEntityField.getBoolean(var1);
         } catch (IllegalAccessException var3) {
            return false;
         }
      }
   }

   public static amq getNmsBlock(int var0) {
      return var0 >= 0 && var0 < amq.p.length ? amq.p[var0] : null;
   }

   private static Tag toNative(cd var0) {
      if (var0 == null) {
         return null;
      } else if (var0 instanceof bq) {
         HashMap var9 = new HashMap();
         Collection var10 = null;
         if (compoundMapField == null) {
            try {
               var10 = ((bq)var0).c();
            } catch (Throwable var8) {
               try {
                  logger.warning("WorldEdit: Couldn't get bq.c(), so we're going to try to get at the 'map' field directly from now on");
                  if (compoundMapField == null) {
                     compoundMapField = bq.class.getDeclaredField("map");
                     compoundMapField.setAccessible(true);
                  }
               } catch (Throwable var7) {
                  throw new RuntimeException(var7);
               }
            }
         }

         if (compoundMapField != null) {
            try {
               var10 = ((HashMap)compoundMapField.get(var0)).values();
            } catch (Throwable var6) {
               throw new RuntimeException(var6);
            }
         }

         for(Object var13 : var10) {
            cd var14 = (cd)var13;
            var9.put(var14.e(), toNative(var14));
         }

         return new CompoundTag(var0.e(), var9);
      } else if (var0 instanceof bp) {
         return new ByteTag(var0.e(), ((bp)var0).a);
      } else if (var0 instanceof bo) {
         return new ByteArrayTag(var0.e(), ((bo)var0).a);
      } else if (var0 instanceof bt) {
         return new DoubleTag(var0.e(), ((bt)var0).a);
      } else if (var0 instanceof bv) {
         return new FloatTag(var0.e(), ((bv)var0).a);
      } else if (var0 instanceof bx) {
         return new IntTag(var0.e(), ((bx)var0).a);
      } else if (var0 instanceof bw) {
         return new IntArrayTag(var0.e(), ((bw)var0).a);
      } else if (!(var0 instanceof by)) {
         if (var0 instanceof bz) {
            return new LongTag(var0.e(), ((bz)var0).a);
         } else if (var0 instanceof cb) {
            return new ShortTag(var0.e(), ((cb)var0).a);
         } else if (var0 instanceof cc) {
            return new StringTag(var0.e(), ((cc)var0).a);
         } else if (var0 instanceof bu) {
            return new EndTag();
         } else {
            throw new IllegalArgumentException("Don't know how to make native " + var0.getClass().getCanonicalName());
         }
      } else {
         ArrayList var1 = new ArrayList();
         by var2 = (by)var0;
         byte var3 = 1;

         for(int var4 = 0; var4 < var2.c(); ++var4) {
            cd var5 = var2.b(var4);
            var1.add(toNative(var5));
            var3 = var5.a();
         }

         Class var12 = NBTConstants.getClassFromType(var3);
         return new ListTag(var0.e(), var12, var1);
      }
   }

   private static cd fromNative(Tag var0) {
      if (var0 == null) {
         return null;
      } else if (var0 instanceof CompoundTag) {
         bq var5 = new bq(var0.getName());

         for(Map.Entry var7 : ((CompoundTag)var0).getValue().entrySet()) {
            var5.a((String)var7.getKey(), fromNative((Tag)var7.getValue()));
         }

         return var5;
      } else if (var0 instanceof ByteTag) {
         return new bp(var0.getName(), ((ByteTag)var0).getValue());
      } else if (var0 instanceof ByteArrayTag) {
         return new bo(var0.getName(), ((ByteArrayTag)var0).getValue());
      } else if (var0 instanceof DoubleTag) {
         return new bt(var0.getName(), ((DoubleTag)var0).getValue());
      } else if (var0 instanceof FloatTag) {
         return new bv(var0.getName(), ((FloatTag)var0).getValue());
      } else if (var0 instanceof IntTag) {
         return new bx(var0.getName(), ((IntTag)var0).getValue());
      } else if (var0 instanceof IntArrayTag) {
         return new bw(var0.getName(), ((IntArrayTag)var0).getValue());
      } else if (!(var0 instanceof ListTag)) {
         if (var0 instanceof LongTag) {
            return new bz(var0.getName(), ((LongTag)var0).getValue());
         } else if (var0 instanceof ShortTag) {
            return new cb(var0.getName(), ((ShortTag)var0).getValue());
         } else if (var0 instanceof StringTag) {
            return new cc(var0.getName(), ((StringTag)var0).getValue());
         } else if (var0 instanceof EndTag) {
            return new bu();
         } else {
            throw new IllegalArgumentException("Don't know how to make NMS " + var0.getClass().getCanonicalName());
         }
      } else {
         by var1 = new by(var0.getName());
         ListTag var2 = (ListTag)var0;

         for(Tag var4 : var2.getValue()) {
            var1.a(fromNative(var4));
         }

         return var1;
      }
   }

   public static boolean isValidBlockType(int var0) throws NoClassDefFoundError {
      return var0 == 0 || var0 >= 1 && var0 < amq.p.length && amq.p[var0] != null;
   }

   static {
      logger = WorldEdit.logger;

      Field var0;
      try {
         var0 = amq.class.getDeclaredField("cs");
         var0.setAccessible(true);
      } catch (NoSuchFieldException var2) {
         var0 = null;
      }

      nmsBlock_isTileEntityField = var0;
   }
}
