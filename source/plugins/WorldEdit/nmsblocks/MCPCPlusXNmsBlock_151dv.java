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
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagEnd;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_5_R2.CraftWorld;

public class MCPCPlusXNmsBlock_151dv extends NmsBlock {
   private static final Logger logger;
   private static Field compoundMapField;
   private static final Field nmsBlock_isTileEntityField;
   private NBTTagCompound nbtData = null;

   public static boolean verify() {
      try {
         Class.forName("org.bukkit.craftbukkit.v1_5_R2.CraftWorld");
      } catch (Throwable var1) {
         return false;
      }

      return nmsBlock_isTileEntityField != null;
   }

   public MCPCPlusXNmsBlock_151dv(int var1, int var2, TileEntityBlock var3) {
      super(var1, var2);
      this.nbtData = (NBTTagCompound)fromNative(var3.getNbtData());
   }

   public MCPCPlusXNmsBlock_151dv(int var1, int var2, NBTTagCompound var3) {
      super(var1, var2);
      this.nbtData = var3;
   }

   private NBTTagCompound getNmsData(Vector var1) {
      if (this.nbtData == null) {
         return null;
      } else {
         this.nbtData.func_74782_a("x", new NBTTagInt("x", var1.getBlockX()));
         this.nbtData.func_74782_a("y", new NBTTagInt("y", var1.getBlockY()));
         this.nbtData.func_74782_a("z", new NBTTagInt("z", var1.getBlockZ()));
         return this.nbtData;
      }
   }

   public boolean hasNbtData() {
      return this.nbtData != null;
   }

   public String getNbtId() {
      return this.nbtData == null ? "" : this.nbtData.func_74779_i("id");
   }

   public CompoundTag getNbtData() {
      return this.nbtData == null ? new CompoundTag(this.getNbtId(), new HashMap()) : (CompoundTag)toNative(this.nbtData);
   }

   public void setNbtData(CompoundTag var1) throws DataException {
      if (var1 == null) {
         this.nbtData = null;
      }

      this.nbtData = (NBTTagCompound)fromNative(var1);
   }

   public static MCPCPlusXNmsBlock_151dv get(World var0, Vector var1, int var2, int var3) {
      if (!hasTileEntity(var2)) {
         return null;
      } else {
         TileEntity var4 = ((CraftWorld)var0).getHandle().func_72796_p(var1.getBlockX(), var1.getBlockY(), var1.getBlockZ());
         if (var4 != null) {
            NBTTagCompound var5 = new NBTTagCompound();
            var4.func_70310_b(var5);
            return new MCPCPlusXNmsBlock_151dv(var2, var3, var5);
         } else {
            return null;
         }
      }
   }

   public static boolean set(World var0, Vector var1, BaseBlock var2) {
      NBTTagCompound var3 = null;
      if (!hasTileEntity(var0.getBlockTypeIdAt(var1.getBlockX(), var1.getBlockY(), var1.getBlockZ()))) {
         return false;
      } else {
         if (var2 instanceof MCPCPlusXNmsBlock_151dv) {
            MCPCPlusXNmsBlock_151dv var4 = (MCPCPlusXNmsBlock_151dv)var2;
            var3 = var4.getNmsData(var1);
         } else if (var2 instanceof TileEntityBlock) {
            MCPCPlusXNmsBlock_151dv var5 = new MCPCPlusXNmsBlock_151dv(var2.getId(), var2.getData(), var2);
            var3 = var5.getNmsData(var1);
         }

         if (var3 != null) {
            TileEntity var6 = ((CraftWorld)var0).getHandle().func_72796_p(var1.getBlockX(), var1.getBlockY(), var1.getBlockZ());
            if (var6 != null) {
               var6.func_70307_a(var3);
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
      boolean var8 = var7.getHandle().func_72832_d(var4, var5, var6, var2.getId(), var2.getData(), 0);
      if (var2 instanceof BaseBlock) {
         var0.copyToWorld(var1, (BaseBlock)var2);
      }

      var8 = var7.getHandle().func_72921_c(var4, var5, var6, var2.getData(), 0) || var8;
      if (var8 && var3) {
         var7.getHandle().func_72845_h(var4, var5, var6);
         var7.getHandle().func_72851_f(var4, var5, var6, var2.getId());
      }

      return var8;
   }

   public static boolean hasTileEntity(int var0) {
      net.minecraft.block.Block var1 = getNmsBlock(var0);
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

   public static net.minecraft.block.Block getNmsBlock(int var0) {
      return var0 >= 0 && var0 < net.minecraft.block.Block.field_71973_m.length ? net.minecraft.block.Block.field_71973_m[var0] : null;
   }

   private static Tag toNative(NBTBase var0) {
      if (var0 == null) {
         return null;
      } else if (var0 instanceof NBTTagCompound) {
         HashMap var9 = new HashMap();
         Collection var10 = null;
         if (compoundMapField == null) {
            try {
               var10 = ((NBTTagCompound)var0).func_74758_c();
            } catch (Throwable var8) {
               try {
                  logger.warning("WorldEdit: Couldn't get NBTTagCompound.func_74758_c(), so we're going to try to get at the 'map' field directly from now on");
                  if (compoundMapField == null) {
                     compoundMapField = NBTTagCompound.class.getDeclaredField("field_74784_a");
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
            NBTBase var14 = (NBTBase)var13;
            var9.put(var14.func_74740_e(), toNative(var14));
         }

         return new CompoundTag(var0.func_74740_e(), var9);
      } else if (var0 instanceof NBTTagByte) {
         return new ByteTag(var0.func_74740_e(), ((NBTTagByte)var0).field_74756_a);
      } else if (var0 instanceof NBTTagByteArray) {
         return new ByteArrayTag(var0.func_74740_e(), ((NBTTagByteArray)var0).field_74754_a);
      } else if (var0 instanceof NBTTagDouble) {
         return new DoubleTag(var0.func_74740_e(), ((NBTTagDouble)var0).field_74755_a);
      } else if (var0 instanceof NBTTagFloat) {
         return new FloatTag(var0.func_74740_e(), ((NBTTagFloat)var0).field_74750_a);
      } else if (var0 instanceof NBTTagInt) {
         return new IntTag(var0.func_74740_e(), ((NBTTagInt)var0).field_74748_a);
      } else if (var0 instanceof NBTTagIntArray) {
         return new IntArrayTag(var0.func_74740_e(), ((NBTTagIntArray)var0).field_74749_a);
      } else if (!(var0 instanceof NBTTagList)) {
         if (var0 instanceof NBTTagLong) {
            return new LongTag(var0.func_74740_e(), ((NBTTagLong)var0).field_74753_a);
         } else if (var0 instanceof NBTTagShort) {
            return new ShortTag(var0.func_74740_e(), ((NBTTagShort)var0).field_74752_a);
         } else if (var0 instanceof NBTTagString) {
            return new StringTag(var0.func_74740_e(), ((NBTTagString)var0).field_74751_a);
         } else if (var0 instanceof NBTTagEnd) {
            return new EndTag();
         } else {
            throw new IllegalArgumentException("Don't know how to make native " + var0.getClass().getCanonicalName());
         }
      } else {
         ArrayList var1 = new ArrayList();
         NBTTagList var2 = (NBTTagList)var0;
         byte var3 = 1;

         for(int var4 = 0; var4 < var2.func_74745_c(); ++var4) {
            NBTBase var5 = var2.func_74743_b(var4);
            var1.add(toNative(var5));
            var3 = var5.func_74732_a();
         }

         Class var12 = NBTConstants.getClassFromType(var3);
         return new ListTag(var0.func_74740_e(), var12, var1);
      }
   }

   private static NBTBase fromNative(Tag var0) {
      if (var0 == null) {
         return null;
      } else if (var0 instanceof CompoundTag) {
         NBTTagCompound var5 = new NBTTagCompound(var0.getName());

         for(Map.Entry var7 : ((CompoundTag)var0).getValue().entrySet()) {
            var5.func_74782_a((String)var7.getKey(), fromNative((Tag)var7.getValue()));
         }

         return var5;
      } else if (var0 instanceof ByteTag) {
         return new NBTTagByte(var0.getName(), ((ByteTag)var0).getValue());
      } else if (var0 instanceof ByteArrayTag) {
         return new NBTTagByteArray(var0.getName(), ((ByteArrayTag)var0).getValue());
      } else if (var0 instanceof DoubleTag) {
         return new NBTTagDouble(var0.getName(), ((DoubleTag)var0).getValue());
      } else if (var0 instanceof FloatTag) {
         return new NBTTagFloat(var0.getName(), ((FloatTag)var0).getValue());
      } else if (var0 instanceof IntTag) {
         return new NBTTagInt(var0.getName(), ((IntTag)var0).getValue());
      } else if (var0 instanceof IntArrayTag) {
         return new NBTTagIntArray(var0.getName(), ((IntArrayTag)var0).getValue());
      } else if (!(var0 instanceof ListTag)) {
         if (var0 instanceof LongTag) {
            return new NBTTagLong(var0.getName(), ((LongTag)var0).getValue());
         } else if (var0 instanceof ShortTag) {
            return new NBTTagShort(var0.getName(), ((ShortTag)var0).getValue());
         } else if (var0 instanceof StringTag) {
            return new NBTTagString(var0.getName(), ((StringTag)var0).getValue());
         } else if (var0 instanceof EndTag) {
            return new NBTTagEnd();
         } else {
            throw new IllegalArgumentException("Don't know how to make NMS " + var0.getClass().getCanonicalName());
         }
      } else {
         NBTTagList var1 = new NBTTagList(var0.getName());
         ListTag var2 = (ListTag)var0;

         for(Tag var4 : var2.getValue()) {
            var1.func_74742_a(fromNative(var4));
         }

         return var1;
      }
   }

   public static boolean isValidBlockType(int var0) throws NoClassDefFoundError {
      return var0 == 0 || var0 >= 1 && var0 < net.minecraft.block.Block.field_71973_m.length && net.minecraft.block.Block.field_71973_m[var0] != null;
   }

   static {
      logger = WorldEdit.logger;

      Field var0;
      try {
         var0 = net.minecraft.block.Block.class.getDeclaredField("field_72025_cg");
         var0.setAccessible(true);
      } catch (NoSuchFieldException var2) {
         var0 = null;
      }

      nmsBlock_isTileEntityField = var0;
   }
}
