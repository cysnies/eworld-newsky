package trade;

import com.google.common.base.Splitter;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.google.common.io.Closeables;
import com.google.common.io.InputSupplier;
import com.google.common.io.OutputSupplier;
import com.google.common.primitives.Primitives;
import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

public class NbtFactory {
   private static final BiMap NBT_CLASS = HashBiMap.create();
   private static final BiMap NBT_ENUM = HashBiMap.create();
   private Class BASE_CLASS;
   private Class COMPOUND_CLASS;
   private Method NBT_CREATE_TAG;
   private Method NBT_GET_TYPE;
   private final Field[] DATA_FIELD = new Field[12];
   private Class CRAFT_STACK;
   private Field CRAFT_HANDLE;
   private Field STACK_TAG;
   private Method LOAD_COMPOUND;
   private Method SAVE_COMPOUND;
   private static NbtFactory INSTANCE;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$trade$NbtFactory$NbtType;

   private static NbtFactory get() {
      if (INSTANCE == null) {
         INSTANCE = new NbtFactory();
      }

      return INSTANCE;
   }

   private NbtFactory() {
      if (this.BASE_CLASS == null) {
         try {
            ClassLoader loader = NbtFactory.class.getClassLoader();
            String packageName = Bukkit.getServer().getClass().getPackage().getName();
            Class<?> offlinePlayer = loader.loadClass(packageName + ".CraftOfflinePlayer");
            this.COMPOUND_CLASS = getMethod(0, 8, offlinePlayer, "getData").getReturnType();
            this.BASE_CLASS = this.COMPOUND_CLASS.getSuperclass();
            this.NBT_GET_TYPE = getMethod(0, 8, this.BASE_CLASS, "getTypeId");
            this.NBT_CREATE_TAG = getMethod(8, 0, this.BASE_CLASS, "createTag", Byte.TYPE, String.class);
            this.CRAFT_STACK = loader.loadClass(packageName + ".inventory.CraftItemStack");
            this.CRAFT_HANDLE = getField((Object)null, this.CRAFT_STACK, "handle");
            this.STACK_TAG = getField((Object)null, this.CRAFT_HANDLE.getType(), "tag");
            this.LOAD_COMPOUND = getMethod(8, 0, this.BASE_CLASS, (String)null, DataInput.class);
            this.SAVE_COMPOUND = getMethod(8, 0, this.BASE_CLASS, (String)null, this.BASE_CLASS, DataOutput.class);
         } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Unable to find offline player.", e);
         }
      }

   }

   private Map getDataMap(Object handle) {
      return (Map)getFieldValue(this.getDataField(NbtFactory.NbtType.TAG_COMPOUND, handle), handle);
   }

   private List getDataList(Object handle) {
      return (List)getFieldValue(this.getDataField(NbtFactory.NbtType.TAG_LIST, handle), handle);
   }

   public static NbtList createList(Object... content) {
      return createList((Iterable)Arrays.asList(content));
   }

   public static NbtList createList(Iterable iterable) {
      NbtList list = get().new NbtList(INSTANCE.createNbtTag(NbtFactory.NbtType.TAG_LIST, "", (Object)null), (NbtList)null);

      for(Object obj : iterable) {
         list.add(obj);
      }

      return list;
   }

   public static NbtCompound createCompound() {
      return get().new NbtCompound(INSTANCE.createNbtTag(NbtFactory.NbtType.TAG_COMPOUND, "", (Object)null), (NbtCompound)null);
   }

   public static NbtList fromList(Object nmsList) {
      return get().new NbtList(nmsList, (NbtList)null);
   }

   public static NbtCompound fromStream(InputSupplier stream, StreamOptions option) throws IOException {
      InputStream input = null;
      DataInputStream data = null;

      NbtCompound var5;
      try {
         input = (InputStream)stream.getInput();
         data = new DataInputStream(new BufferedInputStream((InputStream)(option == NbtFactory.StreamOptions.GZIP_COMPRESSION ? new GZIPInputStream(input) : input)));
         var5 = fromCompound(invokeMethod(get().LOAD_COMPOUND, (Object)null, data));
      } finally {
         if (data != null) {
            Closeables.closeQuietly(data);
         }

         if (input != null) {
            Closeables.closeQuietly(input);
         }

      }

      return var5;
   }

   public static void saveStream(NbtCompound source, OutputSupplier stream, StreamOptions option) throws IOException {
      OutputStream output = null;
      DataOutputStream data = null;

      try {
         output = (OutputStream)stream.getOutput();
         data = new DataOutputStream((OutputStream)(option == NbtFactory.StreamOptions.GZIP_COMPRESSION ? new GZIPOutputStream(output) : output));
         invokeMethod(get().SAVE_COMPOUND, (Object)null, source.getHandle(), data);
      } finally {
         if (data != null) {
            Closeables.closeQuietly(data);
         }

         if (output != null) {
            Closeables.closeQuietly(output);
         }

      }

   }

   public static NbtCompound fromCompound(Object nmsCompound) {
      return get().new NbtCompound(nmsCompound, (NbtCompound)null);
   }

   public static void setItemTag(ItemStack stack, NbtCompound compound) {
      checkItemStack(stack);
      Object nms = getFieldValue(get().CRAFT_HANDLE, stack);
      setFieldValue(get().STACK_TAG, nms, compound.getHandle());
   }

   public static NbtCompound fromItemTag(ItemStack stack) {
      checkItemStack(stack);
      Object nms = getFieldValue(get().CRAFT_HANDLE, stack);
      Object tag = getFieldValue(get().STACK_TAG, nms);
      if (tag == null) {
         NbtCompound compound = createCompound();
         setItemTag(stack, compound);
         return compound;
      } else {
         return fromCompound(tag);
      }
   }

   public static ItemStack getCraftItemStack(ItemStack stack) {
      if (stack != null && !get().CRAFT_STACK.isAssignableFrom(stack.getClass())) {
         try {
            Constructor<?> caller = INSTANCE.CRAFT_STACK.getDeclaredConstructor(ItemStack.class);
            caller.setAccessible(true);
            return (ItemStack)caller.newInstance(stack);
         } catch (Exception var2) {
            throw new IllegalStateException("Unable to convert " + stack + " + to a CraftItemStack.");
         }
      } else {
         return stack;
      }
   }

   private static void checkItemStack(ItemStack stack) {
      if (stack == null) {
         throw new IllegalArgumentException("Stack cannot be NULL.");
      } else if (!get().CRAFT_STACK.isAssignableFrom(stack.getClass())) {
         throw new IllegalArgumentException("Stack must be a CraftItemStack.");
      } else if (stack.getTypeId() == 0) {
         throw new IllegalArgumentException("ItemStacks representing air cannot store NMS information.");
      }
   }

   private Object unwrapValue(String name, Object value) {
      if (value == null) {
         return null;
      } else if (value instanceof Wrapper) {
         return ((Wrapper)value).getHandle();
      } else if (value instanceof List) {
         throw new IllegalArgumentException("Can only insert a WrappedList.");
      } else if (value instanceof Map) {
         throw new IllegalArgumentException("Can only insert a WrappedCompound.");
      } else {
         return this.createNbtTag(this.getPrimitiveType(value), name, value);
      }
   }

   private Object wrapNative(Object nms) {
      if (nms == null) {
         return null;
      } else if (this.BASE_CLASS.isAssignableFrom(nms.getClass())) {
         NbtType type = this.getNbtType(nms);
         switch (type) {
            case TAG_LIST:
               return new NbtList(nms, (NbtList)null);
            case TAG_COMPOUND:
               return new NbtCompound(nms, (NbtCompound)null);
            default:
               return getFieldValue(this.getDataField(type, nms), nms);
         }
      } else {
         throw new IllegalArgumentException("Unexpected type: " + nms);
      }
   }

   private Object createNbtTag(NbtType type, String name, Object value) {
      Object tag = invokeMethod(this.NBT_CREATE_TAG, (Object)null, (byte)type.id, name);
      if (value != null) {
         setFieldValue(this.getDataField(type, tag), tag, value);
      }

      return tag;
   }

   private Field getDataField(NbtType type, Object nms) {
      if (this.DATA_FIELD[type.id] == null) {
         this.DATA_FIELD[type.id] = getField(nms, (Class)null, type.getFieldName());
      }

      return this.DATA_FIELD[type.id];
   }

   private NbtType getNbtType(Object nms) {
      int type = (Byte)invokeMethod(this.NBT_GET_TYPE, nms);
      return (NbtType)NBT_ENUM.get(type);
   }

   private NbtType getPrimitiveType(Object primitive) {
      NbtType type = (NbtType)NBT_ENUM.get(NBT_CLASS.inverse().get(Primitives.unwrap(primitive.getClass())));
      if (type == null) {
         throw new IllegalArgumentException(String.format("Illegal type: %s (%s)", primitive.getClass(), primitive));
      } else {
         return type;
      }
   }

   private static Object invokeMethod(Method method, Object target, Object... params) {
      try {
         return method.invoke(target, params);
      } catch (Exception e) {
         throw new RuntimeException("Unable to invoke method " + method + " for " + target, e);
      }
   }

   private static void setFieldValue(Field field, Object target, Object value) {
      try {
         field.set(target, value);
      } catch (Exception e) {
         throw new RuntimeException("Unable to set " + field + " for " + target, e);
      }
   }

   private static Object getFieldValue(Field field, Object target) {
      try {
         return field.get(target);
      } catch (Exception e) {
         throw new RuntimeException("Unable to retrieve " + field + " for " + target, e);
      }
   }

   private static Method getMethod(int requireMod, int bannedMod, Class clazz, String methodName, Class... params) {
      Method[] var8;
      for(Method method : var8 = clazz.getDeclaredMethods()) {
         if ((method.getModifiers() & requireMod) == requireMod && (method.getModifiers() & bannedMod) == 0 && (methodName == null || method.getName().equals(methodName)) && Arrays.equals(method.getParameterTypes(), params)) {
            method.setAccessible(true);
            return method;
         }
      }

      if (clazz.getSuperclass() != null) {
         return getMethod(requireMod, bannedMod, clazz.getSuperclass(), methodName, params);
      } else {
         throw new IllegalStateException(String.format("Unable to find method %s (%s).", methodName, Arrays.asList(params)));
      }
   }

   private static Field getField(Object instance, Class clazz, String fieldName) {
      if (clazz == null) {
         clazz = instance.getClass();
      }

      Field[] var6;
      for(Field field : var6 = clazz.getDeclaredFields()) {
         if (field.getName().equals(fieldName)) {
            field.setAccessible(true);
            return field;
         }
      }

      if (clazz.getSuperclass() != null) {
         return getField(instance, clazz.getSuperclass(), fieldName);
      } else {
         throw new IllegalStateException("Unable to find field " + fieldName + " in " + instance);
      }
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$trade$NbtFactory$NbtType() {
      int[] var10000 = $SWITCH_TABLE$trade$NbtFactory$NbtType;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[NbtFactory.NbtType.values().length];

         try {
            var0[NbtFactory.NbtType.TAG_BYTE.ordinal()] = 2;
         } catch (NoSuchFieldError var12) {
         }

         try {
            var0[NbtFactory.NbtType.TAG_BYTE_ARRAY.ordinal()] = 8;
         } catch (NoSuchFieldError var11) {
         }

         try {
            var0[NbtFactory.NbtType.TAG_COMPOUND.ordinal()] = 12;
         } catch (NoSuchFieldError var10) {
         }

         try {
            var0[NbtFactory.NbtType.TAG_DOUBLE.ordinal()] = 7;
         } catch (NoSuchFieldError var9) {
         }

         try {
            var0[NbtFactory.NbtType.TAG_END.ordinal()] = 1;
         } catch (NoSuchFieldError var8) {
         }

         try {
            var0[NbtFactory.NbtType.TAG_FLOAT.ordinal()] = 6;
         } catch (NoSuchFieldError var7) {
         }

         try {
            var0[NbtFactory.NbtType.TAG_INT.ordinal()] = 4;
         } catch (NoSuchFieldError var6) {
         }

         try {
            var0[NbtFactory.NbtType.TAG_INT_ARRAY.ordinal()] = 9;
         } catch (NoSuchFieldError var5) {
         }

         try {
            var0[NbtFactory.NbtType.TAG_LIST.ordinal()] = 11;
         } catch (NoSuchFieldError var4) {
         }

         try {
            var0[NbtFactory.NbtType.TAG_LONG.ordinal()] = 5;
         } catch (NoSuchFieldError var3) {
         }

         try {
            var0[NbtFactory.NbtType.TAG_SHORT.ordinal()] = 3;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[NbtFactory.NbtType.TAG_STRING.ordinal()] = 10;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$trade$NbtFactory$NbtType = var0;
         return var0;
      }
   }

   public static enum StreamOptions {
      NO_COMPRESSION,
      GZIP_COMPRESSION;
   }

   private static enum NbtType {
      TAG_END(0, Void.class),
      TAG_BYTE(1, Byte.TYPE),
      TAG_SHORT(2, Short.TYPE),
      TAG_INT(3, Integer.TYPE),
      TAG_LONG(4, Long.TYPE),
      TAG_FLOAT(5, Float.TYPE),
      TAG_DOUBLE(6, Double.TYPE),
      TAG_BYTE_ARRAY(7, byte[].class),
      TAG_INT_ARRAY(11, int[].class),
      TAG_STRING(8, String.class),
      TAG_LIST(9, List.class),
      TAG_COMPOUND(10, Map.class);

      public final int id;

      private NbtType(int id, Class type) {
         this.id = id;
         NbtFactory.NBT_CLASS.put(id, type);
         NbtFactory.NBT_ENUM.put(id, this);
      }

      private String getFieldName() {
         if (this == TAG_COMPOUND) {
            return "map";
         } else {
            return this == TAG_LIST ? "list" : "data";
         }
      }
   }

   public final class NbtCompound extends ConvertedMap {
      private NbtCompound(Object handle) {
         super(handle, NbtFactory.this.getDataMap(handle));
      }

      public Byte getByte(String key, Byte defaultValue) {
         return this.containsKey(key) ? (Byte)this.get(key) : defaultValue;
      }

      public Short getShort(String key, Short defaultValue) {
         return this.containsKey(key) ? (Short)this.get(key) : defaultValue;
      }

      public Integer getInteger(String key, Integer defaultValue) {
         return this.containsKey(key) ? (Integer)this.get(key) : defaultValue;
      }

      public Long getLong(String key, Long defaultValue) {
         return this.containsKey(key) ? (Long)this.get(key) : defaultValue;
      }

      public Float getFloat(String key, Float defaultValue) {
         return this.containsKey(key) ? (Float)this.get(key) : defaultValue;
      }

      public Double getDouble(String key, Double defaultValue) {
         return this.containsKey(key) ? (Double)this.get(key) : defaultValue;
      }

      public String getString(String key, String defaultValue) {
         return this.containsKey(key) ? (String)this.get(key) : defaultValue;
      }

      public byte[] getByteArray(String key, byte[] defaultValue) {
         return this.containsKey(key) ? (byte[])this.get(key) : defaultValue;
      }

      public int[] getIntegerArray(String key, int[] defaultValue) {
         return this.containsKey(key) ? (int[])this.get(key) : defaultValue;
      }

      public NbtList getList(String key, boolean createNew) {
         NbtList list = (NbtList)this.get(key);
         if (list == null) {
            this.put(key, list = NbtFactory.createList());
         }

         return list;
      }

      public NbtCompound getMap(String key, boolean createNew) {
         return this.getMap((Iterable)Arrays.asList(key), createNew);
      }

      public NbtCompound putPath(String path, Object value) {
         List<String> entries = this.getPathElements(path);
         Map<String, Object> map = this.getMap((Iterable)entries.subList(0, entries.size() - 1), true);
         map.put((String)entries.get(entries.size() - 1), value);
         return this;
      }

      public Object getPath(String path) {
         List<String> entries = this.getPathElements(path);
         NbtCompound map = this.getMap((Iterable)entries.subList(0, entries.size() - 1), false);
         return map != null ? map.get(entries.get(entries.size() - 1)) : null;
      }

      public void saveTo(OutputSupplier stream, StreamOptions option) throws IOException {
         NbtFactory.saveStream(this, stream, option);
      }

      private NbtCompound getMap(Iterable path, boolean createNew) {
         NbtCompound current = this;

         for(String entry : path) {
            NbtCompound child = (NbtCompound)current.get(entry);
            if (child == null) {
               if (!createNew) {
                  throw new IllegalArgumentException("Cannot find " + entry + " in " + path);
               }

               current.put(entry, child = NbtFactory.createCompound());
            }

            current = child;
         }

         return current;
      }

      private List getPathElements(String path) {
         return Lists.newArrayList(Splitter.on(".").omitEmptyStrings().split(path));
      }

      // $FF: synthetic method
      NbtCompound(Object var2, NbtCompound var3) {
         this(var2);
      }
   }

   public final class NbtList extends ConvertedList {
      private NbtList(Object handle) {
         super(handle, NbtFactory.this.getDataList(handle));
      }

      // $FF: synthetic method
      NbtList(Object var2, NbtList var3) {
         this(var2);
      }
   }

   private final class CachedNativeWrapper {
      private final ConcurrentMap cache;

      private CachedNativeWrapper() {
         this.cache = (new MapMaker()).weakKeys().makeMap();
      }

      public Object wrap(Object value) {
         Object current = this.cache.get(value);
         if (current == null) {
            current = NbtFactory.this.wrapNative(value);
            if (current instanceof ConvertedMap || current instanceof ConvertedList) {
               this.cache.put(value, current);
            }
         }

         return current;
      }

      // $FF: synthetic method
      CachedNativeWrapper(CachedNativeWrapper var2) {
         this();
      }
   }

   private class ConvertedMap extends AbstractMap implements Wrapper {
      private final Object handle;
      private final Map original;
      private final CachedNativeWrapper cache = NbtFactory.this.new CachedNativeWrapper((CachedNativeWrapper)null);

      public ConvertedMap(Object handle, Map original) {
         this.handle = handle;
         this.original = original;
      }

      protected Object wrapOutgoing(Object value) {
         return this.cache.wrap(value);
      }

      protected Object unwrapIncoming(String key, Object wrapped) {
         return NbtFactory.this.unwrapValue(key, wrapped);
      }

      public Object put(String key, Object value) {
         return this.wrapOutgoing(this.original.put(key, this.unwrapIncoming(key, value)));
      }

      public Object get(Object key) {
         return this.wrapOutgoing(this.original.get(key));
      }

      public Object remove(Object key) {
         return this.wrapOutgoing(this.original.remove(key));
      }

      public boolean containsKey(Object key) {
         return this.original.containsKey(key);
      }

      public Set entrySet() {
         return new AbstractSet() {
            public boolean add(Map.Entry e) {
               String key = (String)e.getKey();
               Object value = e.getValue();
               ConvertedMap.this.original.put(key, ConvertedMap.this.unwrapIncoming(key, value));
               return true;
            }

            public int size() {
               return ConvertedMap.this.original.size();
            }

            public Iterator iterator() {
               return ConvertedMap.this.iterator();
            }
         };
      }

      private Iterator iterator() {
         final Iterator<Map.Entry<String, Object>> proxy = this.original.entrySet().iterator();
         return new Iterator() {
            public boolean hasNext() {
               return proxy.hasNext();
            }

            public Map.Entry next() {
               Map.Entry<String, Object> entry = (Map.Entry)proxy.next();
               return new AbstractMap.SimpleEntry((String)entry.getKey(), ConvertedMap.this.wrapOutgoing(entry.getValue()));
            }

            public void remove() {
               proxy.remove();
            }
         };
      }

      public Object getHandle() {
         return this.handle;
      }
   }

   private class ConvertedList extends AbstractList implements Wrapper {
      private final Object handle;
      private final List original;
      private final CachedNativeWrapper cache = NbtFactory.this.new CachedNativeWrapper((CachedNativeWrapper)null);

      public ConvertedList(Object handle, List original) {
         this.handle = handle;
         this.original = original;
      }

      protected Object wrapOutgoing(Object value) {
         return this.cache.wrap(value);
      }

      protected Object unwrapIncoming(Object wrapped) {
         return NbtFactory.this.unwrapValue("", wrapped);
      }

      public Object get(int index) {
         return this.wrapOutgoing(this.original.get(index));
      }

      public int size() {
         return this.original.size();
      }

      public Object set(int index, Object element) {
         return this.wrapOutgoing(this.original.set(index, this.unwrapIncoming(element)));
      }

      public void add(int index, Object element) {
         this.original.add(index, this.unwrapIncoming(element));
      }

      public Object remove(int index) {
         return this.wrapOutgoing(this.original.remove(index));
      }

      public boolean remove(Object o) {
         return this.original.remove(this.unwrapIncoming(o));
      }

      public Object getHandle() {
         return this.handle;
      }
   }

   public interface Wrapper {
      Object getHandle();
   }
}
