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
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import net.minecraft.server.v1_6_R2.NBTBase;
import net.minecraft.server.v1_6_R2.NBTTagByte;
import net.minecraft.server.v1_6_R2.NBTTagByteArray;
import net.minecraft.server.v1_6_R2.NBTTagCompound;
import net.minecraft.server.v1_6_R2.NBTTagDouble;
import net.minecraft.server.v1_6_R2.NBTTagEnd;
import net.minecraft.server.v1_6_R2.NBTTagFloat;
import net.minecraft.server.v1_6_R2.NBTTagInt;
import net.minecraft.server.v1_6_R2.NBTTagIntArray;
import net.minecraft.server.v1_6_R2.NBTTagList;
import net.minecraft.server.v1_6_R2.NBTTagLong;
import net.minecraft.server.v1_6_R2.NBTTagShort;
import net.minecraft.server.v1_6_R2.NBTTagString;
import net.minecraft.server.v1_6_R2.TileEntity;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_6_R2.CraftWorld;

public class CBXNmsBlock_162 extends NmsBlock {
   private static final Logger logger;
   private static Field compoundMapField;
   private static final Field nmsBlock_isTileEntityField;
   private NBTTagCompound nbtData = null;

   public static boolean verify() {
      return nmsBlock_isTileEntityField != null;
   }

   public CBXNmsBlock_162(int type, int data, TileEntityBlock tileEntityBlock) {
      super(type, data);
      this.nbtData = (NBTTagCompound)fromNative(tileEntityBlock.getNbtData());
   }

   public CBXNmsBlock_162(int type, int data, NBTTagCompound nbtData) {
      super(type, data);
      this.nbtData = nbtData;
   }

   private NBTTagCompound getNmsData(Vector pt) {
      if (this.nbtData == null) {
         return null;
      } else {
         this.nbtData.set("x", new NBTTagInt("x", pt.getBlockX()));
         this.nbtData.set("y", new NBTTagInt("y", pt.getBlockY()));
         this.nbtData.set("z", new NBTTagInt("z", pt.getBlockZ()));
         return this.nbtData;
      }
   }

   public boolean hasNbtData() {
      return this.nbtData != null;
   }

   public String getNbtId() {
      return this.nbtData == null ? "" : this.nbtData.getString("id");
   }

   public CompoundTag getNbtData() {
      return this.nbtData == null ? new CompoundTag(this.getNbtId(), new HashMap()) : (CompoundTag)toNative(this.nbtData);
   }

   public void setNbtData(CompoundTag tag) throws DataException {
      if (tag == null) {
         this.nbtData = null;
      }

      this.nbtData = (NBTTagCompound)fromNative(tag);
   }

   public static CBXNmsBlock_162 get(World world, Vector position, int type, int data) {
      if (!hasTileEntity(type)) {
         return null;
      } else {
         TileEntity te = ((CraftWorld)world).getHandle().getTileEntity(position.getBlockX(), position.getBlockY(), position.getBlockZ());
         if (te != null) {
            NBTTagCompound tag = new NBTTagCompound();
            te.b(tag);
            return new CBXNmsBlock_162(type, data, tag);
         } else {
            return null;
         }
      }
   }

   public static boolean set(World world, Vector position, BaseBlock block) {
      NBTTagCompound data = null;
      if (!hasTileEntity(world.getBlockTypeIdAt(position.getBlockX(), position.getBlockY(), position.getBlockZ()))) {
         return false;
      } else {
         if (block instanceof CBXNmsBlock_162) {
            CBXNmsBlock_162 nmsProxyBlock = (CBXNmsBlock_162)block;
            data = nmsProxyBlock.getNmsData(position);
         } else if (block instanceof TileEntityBlock) {
            CBXNmsBlock_162 nmsProxyBlock = new CBXNmsBlock_162(block.getId(), block.getData(), block);
            data = nmsProxyBlock.getNmsData(position);
         }

         if (data != null) {
            TileEntity te = ((CraftWorld)world).getHandle().getTileEntity(position.getBlockX(), position.getBlockY(), position.getBlockZ());
            if (te != null) {
               te.a(data);
               return true;
            }
         }

         return false;
      }
   }

   public static boolean setSafely(BukkitWorld world, Vector position, Block block, boolean notifyAdjacent) {
      int x = position.getBlockX();
      int y = position.getBlockY();
      int z = position.getBlockZ();
      CraftWorld craftWorld = (CraftWorld)world.getWorld();
      boolean changed = craftWorld.getHandle().setTypeIdAndData(x, y, z, block.getId(), block.getData(), 0);
      if (block instanceof BaseBlock) {
         world.copyToWorld(position, (BaseBlock)block);
      }

      changed = craftWorld.getHandle().setData(x, y, z, block.getData(), 0) || changed;
      if (changed && notifyAdjacent) {
         craftWorld.getHandle().notify(x, y, z);
         craftWorld.getHandle().update(x, y, z, block.getId());
      }

      return changed;
   }

   public static boolean hasTileEntity(int type) {
      net.minecraft.server.v1_6_R2.Block nmsBlock = getNmsBlock(type);
      if (nmsBlock == null) {
         return false;
      } else {
         try {
            return nmsBlock_isTileEntityField.getBoolean(nmsBlock);
         } catch (IllegalAccessException var3) {
            return false;
         }
      }
   }

   public static net.minecraft.server.v1_6_R2.Block getNmsBlock(int type) {
      return type >= 0 && type < net.minecraft.server.v1_6_R2.Block.byId.length ? net.minecraft.server.v1_6_R2.Block.byId[type] : null;
   }

   private static Tag toNative(NBTBase foreign) {
      if (foreign == null) {
         return null;
      } else if (foreign instanceof NBTTagCompound) {
         Map<String, Tag> values = new HashMap();
         Collection<Object> foreignValues = null;
         if (compoundMapField == null) {
            try {
               foreignValues = ((NBTTagCompound)foreign).c();
            } catch (Throwable var8) {
               try {
                  logger.warning("WorldEdit: Couldn't get NBTTagCompound.c(), so we're going to try to get at the 'map' field directly from now on");
                  if (compoundMapField == null) {
                     compoundMapField = NBTTagCompound.class.getDeclaredField("map");
                     compoundMapField.setAccessible(true);
                  }
               } catch (Throwable e) {
                  throw new RuntimeException(e);
               }
            }
         }

         if (compoundMapField != null) {
            try {
               foreignValues = ((HashMap)compoundMapField.get(foreign)).values();
            } catch (Throwable e) {
               throw new RuntimeException(e);
            }
         }

         for(Object obj : foreignValues) {
            NBTBase base = (NBTBase)obj;
            values.put(base.getName(), toNative(base));
         }

         return new CompoundTag(foreign.getName(), values);
      } else if (foreign instanceof NBTTagByte) {
         return new ByteTag(foreign.getName(), ((NBTTagByte)foreign).data);
      } else if (foreign instanceof NBTTagByteArray) {
         return new ByteArrayTag(foreign.getName(), ((NBTTagByteArray)foreign).data);
      } else if (foreign instanceof NBTTagDouble) {
         return new DoubleTag(foreign.getName(), ((NBTTagDouble)foreign).data);
      } else if (foreign instanceof NBTTagFloat) {
         return new FloatTag(foreign.getName(), ((NBTTagFloat)foreign).data);
      } else if (foreign instanceof NBTTagInt) {
         return new IntTag(foreign.getName(), ((NBTTagInt)foreign).data);
      } else if (foreign instanceof NBTTagIntArray) {
         return new IntArrayTag(foreign.getName(), ((NBTTagIntArray)foreign).data);
      } else if (!(foreign instanceof NBTTagList)) {
         if (foreign instanceof NBTTagLong) {
            return new LongTag(foreign.getName(), ((NBTTagLong)foreign).data);
         } else if (foreign instanceof NBTTagShort) {
            return new ShortTag(foreign.getName(), ((NBTTagShort)foreign).data);
         } else if (foreign instanceof NBTTagString) {
            return new StringTag(foreign.getName(), ((NBTTagString)foreign).data);
         } else if (foreign instanceof NBTTagEnd) {
            return new EndTag();
         } else {
            throw new IllegalArgumentException("Don't know how to make native " + foreign.getClass().getCanonicalName());
         }
      } else {
         List<Tag> values = new ArrayList();
         NBTTagList foreignList = (NBTTagList)foreign;
         int type = 1;

         for(int i = 0; i < foreignList.size(); ++i) {
            NBTBase foreignTag = foreignList.get(i);
            values.add(toNative(foreignTag));
            type = foreignTag.getTypeId();
         }

         Class<? extends Tag> cls = NBTConstants.getClassFromType(type);
         return new ListTag(foreign.getName(), cls, values);
      }
   }

   private static NBTBase fromNative(Tag foreign) {
      if (foreign == null) {
         return null;
      } else if (foreign instanceof CompoundTag) {
         NBTTagCompound tag = new NBTTagCompound(foreign.getName());

         for(Map.Entry entry : ((CompoundTag)foreign).getValue().entrySet()) {
            tag.set((String)entry.getKey(), fromNative((Tag)entry.getValue()));
         }

         return tag;
      } else if (foreign instanceof ByteTag) {
         return new NBTTagByte(foreign.getName(), ((ByteTag)foreign).getValue());
      } else if (foreign instanceof ByteArrayTag) {
         return new NBTTagByteArray(foreign.getName(), ((ByteArrayTag)foreign).getValue());
      } else if (foreign instanceof DoubleTag) {
         return new NBTTagDouble(foreign.getName(), ((DoubleTag)foreign).getValue());
      } else if (foreign instanceof FloatTag) {
         return new NBTTagFloat(foreign.getName(), ((FloatTag)foreign).getValue());
      } else if (foreign instanceof IntTag) {
         return new NBTTagInt(foreign.getName(), ((IntTag)foreign).getValue());
      } else if (foreign instanceof IntArrayTag) {
         return new NBTTagIntArray(foreign.getName(), ((IntArrayTag)foreign).getValue());
      } else if (!(foreign instanceof ListTag)) {
         if (foreign instanceof LongTag) {
            return new NBTTagLong(foreign.getName(), ((LongTag)foreign).getValue());
         } else if (foreign instanceof ShortTag) {
            return new NBTTagShort(foreign.getName(), ((ShortTag)foreign).getValue());
         } else if (foreign instanceof StringTag) {
            return new NBTTagString(foreign.getName(), ((StringTag)foreign).getValue());
         } else if (foreign instanceof EndTag) {
            return new NBTTagEnd();
         } else {
            throw new IllegalArgumentException("Don't know how to make NMS " + foreign.getClass().getCanonicalName());
         }
      } else {
         NBTTagList tag = new NBTTagList(foreign.getName());
         ListTag foreignList = (ListTag)foreign;

         for(Tag t : foreignList.getValue()) {
            tag.add(fromNative(t));
         }

         return tag;
      }
   }

   public static boolean isValidBlockType(int type) throws NoClassDefFoundError {
      return type == 0 || type >= 1 && type < net.minecraft.server.v1_6_R2.Block.byId.length && net.minecraft.server.v1_6_R2.Block.byId[type] != null;
   }

   static {
      logger = WorldEdit.logger;

      Field field;
      try {
         field = net.minecraft.server.v1_6_R2.Block.class.getDeclaredField("isTileEntity");
         field.setAccessible(true);
      } catch (NoSuchFieldException var2) {
         field = null;
      }

      nmsBlock_isTileEntityField = field;
   }
}
