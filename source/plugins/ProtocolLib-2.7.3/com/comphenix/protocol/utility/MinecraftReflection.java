package com.comphenix.protocol.utility;

import com.comphenix.net.sf.cglib.asm.ClassReader;
import com.comphenix.net.sf.cglib.asm.MethodVisitor;
import com.comphenix.protocol.injector.BukkitUnwrapper;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.compiler.EmptyClassVisitor;
import com.comphenix.protocol.reflect.compiler.EmptyMethodVisitor;
import com.comphenix.protocol.reflect.fuzzy.AbstractFuzzyMatcher;
import com.comphenix.protocol.reflect.fuzzy.FuzzyClassContract;
import com.comphenix.protocol.reflect.fuzzy.FuzzyFieldContract;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMatchers;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.comphenix.protocol.wrappers.nbt.NbtType;
import com.google.common.base.Joiner;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;

public class MinecraftReflection {
   /** @deprecated */
   @Deprecated
   public static final String MINECRAFT_OBJECT = "net\\.minecraft(\\.\\w+)+";
   private static String DYNAMIC_PACKAGE_MATCHER = null;
   private static String MINECRAFT_PREFIX_PACKAGE = "net.minecraft.server";
   private static String MINECRAFT_FULL_PACKAGE = null;
   private static String CRAFTBUKKIT_PACKAGE = null;
   private static CachedPackage minecraftPackage;
   private static CachedPackage craftbukkitPackage;
   private static Constructor craftNMSConstructor;
   private static Constructor craftBukkitConstructor;
   private static AbstractFuzzyMatcher fuzzyMatcher;
   private static Method craftNMSMethod;
   private static Method craftBukkitMethod;
   private static boolean craftItemStackFailed;
   private static Class itemStackArrayClass;
   private static boolean initializing;

   private MinecraftReflection() {
   }

   public static String getMinecraftObjectRegex() {
      if (DYNAMIC_PACKAGE_MATCHER == null) {
         getMinecraftPackage();
      }

      return DYNAMIC_PACKAGE_MATCHER;
   }

   public static AbstractFuzzyMatcher getMinecraftObjectMatcher() {
      if (fuzzyMatcher == null) {
         fuzzyMatcher = FuzzyMatchers.matchRegex((String)getMinecraftObjectRegex(), 50);
      }

      return fuzzyMatcher;
   }

   public static String getMinecraftPackage() {
      if (MINECRAFT_FULL_PACKAGE != null) {
         return MINECRAFT_FULL_PACKAGE;
      } else if (initializing) {
         throw new IllegalStateException("Already initializing minecraft package!");
      } else {
         initializing = true;
         Server craftServer = Bukkit.getServer();
         if (craftServer != null) {
            String matcher;
            try {
               Class<?> craftClass = craftServer.getClass();
               CRAFTBUKKIT_PACKAGE = getPackage(craftClass.getCanonicalName());
               handleLibigot();
               Class<?> craftEntity = getCraftEntityClass();
               Method getHandle = craftEntity.getMethod("getHandle");
               MINECRAFT_FULL_PACKAGE = getPackage(getHandle.getReturnType().getCanonicalName());
               if (!MINECRAFT_FULL_PACKAGE.startsWith(MINECRAFT_PREFIX_PACKAGE)) {
                  MINECRAFT_PREFIX_PACKAGE = MINECRAFT_FULL_PACKAGE;
                  matcher = (MINECRAFT_PREFIX_PACKAGE.length() > 0 ? Pattern.quote(MINECRAFT_PREFIX_PACKAGE + ".") : "") + "\\w+";
                  setDynamicPackageMatcher("(" + matcher + ")|(" + "net\\.minecraft(\\.\\w+)+" + ")");
               } else {
                  setDynamicPackageMatcher("net\\.minecraft(\\.\\w+)+");
               }

               matcher = MINECRAFT_FULL_PACKAGE;
            } catch (SecurityException e) {
               throw new RuntimeException("Security violation. Cannot get handle method.", e);
            } catch (NoSuchMethodException e) {
               throw new IllegalStateException("Cannot find getHandle() method on server. Is this a modified CraftBukkit version?", e);
            } finally {
               initializing = false;
            }

            return matcher;
         } else {
            initializing = false;
            throw new IllegalStateException("Could not find Bukkit. Is it running?");
         }
      }
   }

   private static void setDynamicPackageMatcher(String regex) {
      DYNAMIC_PACKAGE_MATCHER = regex;
      fuzzyMatcher = null;
   }

   private static void handleLibigot() {
      try {
         getCraftEntityClass();
      } catch (RuntimeException var1) {
         craftbukkitPackage = null;
         CRAFTBUKKIT_PACKAGE = "org.bukkit.craftbukkit";
         getCraftEntityClass();
      }

   }

   public static void setMinecraftPackage(String minecraftPackage, String craftBukkitPackage) {
      MINECRAFT_FULL_PACKAGE = minecraftPackage;
      CRAFTBUKKIT_PACKAGE = craftBukkitPackage;
      if (getMinecraftServerClass() == null) {
         throw new IllegalArgumentException("Cannot find MinecraftServer for package " + minecraftPackage);
      } else {
         setDynamicPackageMatcher("net\\.minecraft(\\.\\w+)+");
      }
   }

   public static String getCraftBukkitPackage() {
      if (CRAFTBUKKIT_PACKAGE == null) {
         getMinecraftPackage();
      }

      return CRAFTBUKKIT_PACKAGE;
   }

   private static String getPackage(String fullName) {
      int index = fullName.lastIndexOf(".");
      return index > 0 ? fullName.substring(0, index) : "";
   }

   public static Object getBukkitEntity(Object nmsObject) {
      if (nmsObject == null) {
         return null;
      } else {
         try {
            return nmsObject.getClass().getMethod("getBukkitEntity").invoke(nmsObject);
         } catch (Exception e) {
            throw new RuntimeException("Cannot get Bukkit entity from " + nmsObject, e);
         }
      }
   }

   public static boolean isMinecraftObject(@Nonnull Object obj) {
      if (obj == null) {
         throw new IllegalArgumentException("Cannot determine the type of a null object.");
      } else {
         return obj.getClass().getName().startsWith(MINECRAFT_PREFIX_PACKAGE);
      }
   }

   public static boolean isMinecraftClass(@Nonnull Class clazz) {
      if (clazz == null) {
         throw new IllegalArgumentException("Class cannot be NULL.");
      } else {
         return getMinecraftObjectMatcher().isMatch(clazz, (Object)null);
      }
   }

   public static boolean isMinecraftObject(@Nonnull Object obj, String className) {
      if (obj == null) {
         throw new IllegalArgumentException("Cannot determine the type of a null object.");
      } else {
         String javaName = obj.getClass().getName();
         return javaName.startsWith(MINECRAFT_PREFIX_PACKAGE) && javaName.endsWith(className);
      }
   }

   public static boolean isChunkPosition(Object obj) {
      return getChunkPositionClass().isAssignableFrom(obj.getClass());
   }

   public static boolean isChunkCoordinates(Object obj) {
      return getChunkCoordinatesClass().isAssignableFrom(obj.getClass());
   }

   public static boolean isPacketClass(Object obj) {
      return getPacketClass().isAssignableFrom(obj.getClass());
   }

   public static boolean isLoginHandler(Object obj) {
      return getNetLoginHandlerClass().isAssignableFrom(obj.getClass());
   }

   public static boolean isServerHandler(Object obj) {
      return getNetServerHandlerClass().isAssignableFrom(obj.getClass());
   }

   public static boolean isMinecraftEntity(Object obj) {
      return getEntityClass().isAssignableFrom(obj.getClass());
   }

   public static boolean isItemStack(Object value) {
      return getItemStackClass().isAssignableFrom(value.getClass());
   }

   public static boolean isCraftPlayer(Object value) {
      return getCraftPlayerClass().isAssignableFrom(value.getClass());
   }

   public static boolean isMinecraftPlayer(Object obj) {
      return getEntityPlayerClass().isAssignableFrom(obj.getClass());
   }

   public static boolean isWatchableObject(Object obj) {
      return getWatchableObjectClass().isAssignableFrom(obj.getClass());
   }

   public static boolean isDataWatcher(Object obj) {
      return getDataWatcherClass().isAssignableFrom(obj.getClass());
   }

   public static boolean isIntHashMap(Object obj) {
      return getIntHashMapClass().isAssignableFrom(obj.getClass());
   }

   public static boolean isCraftItemStack(Object obj) {
      return getCraftItemStackClass().isAssignableFrom(obj.getClass());
   }

   public static Class getEntityPlayerClass() {
      try {
         return getMinecraftClass("EntityPlayer");
      } catch (RuntimeException var3) {
         try {
            Method detect = FuzzyReflection.fromClass(getCraftBukkitClass("CraftServer")).getMethodByName("detectListNameConflict");
            return detect.getParameterTypes()[0];
         } catch (IllegalArgumentException var2) {
            return fallbackMethodReturn("EntityPlayer", "entity.CraftPlayer", "getHandle");
         }
      }
   }

   public static Class getEntityClass() {
      try {
         return getMinecraftClass("Entity");
      } catch (RuntimeException var1) {
         return fallbackMethodReturn("Entity", "entity.CraftEntity", "getHandle");
      }
   }

   public static Class getWorldServerClass() {
      try {
         return getMinecraftClass("WorldServer");
      } catch (RuntimeException var1) {
         return fallbackMethodReturn("WorldServer", "CraftWorld", "getHandle");
      }
   }

   private static Class fallbackMethodReturn(String nmsClass, String craftClass, String methodName) {
      Class<?> result = FuzzyReflection.fromClass(getCraftBukkitClass(craftClass)).getMethodByName(methodName).getReturnType();
      return setMinecraftClass(nmsClass, result);
   }

   public static Class getPacketClass() {
      try {
         return getMinecraftClass("Packet");
      } catch (RuntimeException var4) {
         FuzzyClassContract paketContract = FuzzyClassContract.newBuilder().field(FuzzyFieldContract.newBuilder().typeDerivedOf(Map.class).requireModifier(8)).field(FuzzyFieldContract.newBuilder().typeDerivedOf(Set.class).requireModifier(8)).method(FuzzyMethodContract.newBuilder().parameterSuperOf(DataInputStream.class).returnTypeVoid()).build();
         Method selected = FuzzyReflection.fromClass(getNetHandlerClass()).getMethod(FuzzyMethodContract.newBuilder().parameterMatches(paketContract, 0).parameterCount(1).build());
         Class<?> clazz = getTopmostClass(selected.getParameterTypes()[0]);
         return setMinecraftClass("Packet", clazz);
      }
   }

   private static Class getTopmostClass(Class clazz) {
      while(true) {
         Class<?> superClass = clazz.getSuperclass();
         if (superClass == Object.class || superClass == null) {
            return clazz;
         }

         clazz = superClass;
      }
   }

   public static Class getMinecraftServerClass() {
      try {
         return getMinecraftClass("MinecraftServer");
      } catch (RuntimeException var1) {
         useFallbackServer();
         return getMinecraftClass("MinecraftServer");
      }
   }

   private static void useFallbackServer() {
      Constructor<?> selected = FuzzyReflection.fromClass(getCraftBukkitClass("CraftServer")).getConstructor(FuzzyMethodContract.newBuilder().parameterMatches(getMinecraftObjectMatcher(), 0).parameterCount(2).build());
      Class<?>[] params = selected.getParameterTypes();
      setMinecraftClass("MinecraftServer", params[0]);
      setMinecraftClass("ServerConfigurationManager", params[1]);
   }

   public static Class getPlayerListClass() {
      try {
         return getMinecraftClass("ServerConfigurationManager", "PlayerList");
      } catch (RuntimeException var1) {
         useFallbackServer();
         return getMinecraftClass("ServerConfigurationManager");
      }
   }

   public static Class getNetLoginHandlerClass() {
      try {
         return getMinecraftClass("NetLoginHandler", "PendingConnection");
      } catch (RuntimeException var2) {
         Method selected = FuzzyReflection.fromClass(getPlayerListClass()).getMethod(FuzzyMethodContract.newBuilder().parameterMatches(FuzzyMatchers.matchExact(getEntityPlayerClass()).inverted(), 0).parameterExactType(String.class, 1).parameterExactType(String.class, 2).build());
         return setMinecraftClass("NetLoginHandler", selected.getParameterTypes()[0]);
      }
   }

   public static Class getNetServerHandlerClass() {
      try {
         return getMinecraftClass("NetServerHandler", "PlayerConnection");
      } catch (RuntimeException var1) {
         return setMinecraftClass("NetServerHandler", FuzzyReflection.fromClass(getEntityPlayerClass()).getFieldByType("playerConnection", getNetHandlerClass()).getType());
      }
   }

   public static Class getNetworkManagerClass() {
      try {
         return getMinecraftClass("INetworkManager", "NetworkManager");
      } catch (RuntimeException var2) {
         Constructor<?> selected = FuzzyReflection.fromClass(getNetServerHandlerClass()).getConstructor(FuzzyMethodContract.newBuilder().parameterSuperOf(getMinecraftServerClass(), 0).parameterSuperOf(getEntityPlayerClass(), 2).build());
         return setMinecraftClass("INetworkManager", selected.getParameterTypes()[1]);
      }
   }

   public static Class getNetHandlerClass() {
      try {
         return getMinecraftClass("NetHandler", "Connection");
      } catch (RuntimeException var1) {
         return setMinecraftClass("NetHandler", getNetLoginHandlerClass().getSuperclass());
      }
   }

   public static Class getItemStackClass() {
      try {
         return getMinecraftClass("ItemStack");
      } catch (RuntimeException var1) {
         return setMinecraftClass("ItemStack", FuzzyReflection.fromClass(getCraftItemStackClass(), true).getFieldByName("handle").getType());
      }
   }

   public static Class getBlockClass() {
      try {
         return getMinecraftClass("Block");
      } catch (RuntimeException var9) {
         FuzzyReflection reflect = FuzzyReflection.fromClass(getItemStackClass());
         Set<Class<?>> candidates = new HashSet();

         for(Constructor constructor : reflect.getConstructors()) {
            for(Class clazz : constructor.getParameterTypes()) {
               if (isMinecraftClass(clazz)) {
                  candidates.add(clazz);
               }
            }
         }

         Method selected = reflect.getMethod(FuzzyMethodContract.newBuilder().parameterMatches(FuzzyMatchers.matchAnyOf(candidates)).returnTypeExact(Float.TYPE).build());
         return setMinecraftClass("Block", selected.getParameterTypes()[0]);
      }
   }

   public static Class getWorldTypeClass() {
      try {
         return getMinecraftClass("WorldType");
      } catch (RuntimeException var2) {
         Method selected = FuzzyReflection.fromClass(getMinecraftServerClass(), true).getMethod(FuzzyMethodContract.newBuilder().parameterExactType(String.class, 0).parameterExactType(String.class, 1).parameterMatches(getMinecraftObjectMatcher()).parameterExactType(String.class, 4).parameterCount(5).build());
         return setMinecraftClass("WorldType", selected.getParameterTypes()[3]);
      }
   }

   public static Class getDataWatcherClass() {
      try {
         return getMinecraftClass("DataWatcher");
      } catch (RuntimeException var3) {
         FuzzyClassContract dataWatcherContract = FuzzyClassContract.newBuilder().field(FuzzyFieldContract.newBuilder().requireModifier(8).typeDerivedOf(Map.class)).field(FuzzyFieldContract.newBuilder().banModifier(8).typeDerivedOf(Map.class)).method(FuzzyMethodContract.newBuilder().parameterExactType(Integer.TYPE).parameterExactType(Object.class).returnTypeVoid()).build();
         FuzzyFieldContract fieldContract = FuzzyFieldContract.newBuilder().typeMatches(dataWatcherContract).build();
         return setMinecraftClass("DataWatcher", FuzzyReflection.fromClass(getEntityClass(), true).getField(fieldContract).getType());
      }
   }

   public static Class getChunkPositionClass() {
      try {
         return getMinecraftClass("ChunkPosition");
      } catch (RuntimeException var3) {
         Class<?> normalChunkGenerator = getCraftBukkitClass("generator.NormalChunkGenerator");
         FuzzyMethodContract selected = FuzzyMethodContract.newBuilder().banModifier(8).parameterMatches(getMinecraftObjectMatcher(), 0).parameterExactType(String.class, 1).parameterExactType(Integer.TYPE, 2).parameterExactType(Integer.TYPE, 3).parameterExactType(Integer.TYPE, 4).build();
         return setMinecraftClass("ChunkPosition", FuzzyReflection.fromClass(normalChunkGenerator).getMethod(selected).getReturnType());
      }
   }

   public static Class getChunkCoordinatesClass() {
      try {
         return getMinecraftClass("ChunkCoordinates");
      } catch (RuntimeException var1) {
         return setMinecraftClass("ChunkCoordinates", WrappedDataWatcher.getTypeClass(6));
      }
   }

   public static Class getWatchableObjectClass() {
      try {
         return getMinecraftClass("WatchableObject");
      } catch (RuntimeException var2) {
         Method selected = FuzzyReflection.fromClass(getDataWatcherClass(), true).getMethod(FuzzyMethodContract.newBuilder().requireModifier(8).parameterDerivedOf(DataOutput.class, 0).parameterMatches(getMinecraftObjectMatcher(), 1).build());
         return setMinecraftClass("WatchableObject", selected.getParameterTypes()[1]);
      }
   }

   public static Class getServerConnectionClass() {
      try {
         return getMinecraftClass("ServerConnection");
      } catch (RuntimeException var3) {
         FuzzyClassContract serverConnectionContract = FuzzyClassContract.newBuilder().constructor(FuzzyMethodContract.newBuilder().parameterExactType(getMinecraftServerClass()).parameterCount(1)).method(FuzzyMethodContract.newBuilder().parameterExactType(getNetServerHandlerClass())).build();
         Method selected = FuzzyReflection.fromClass(getMinecraftServerClass()).getMethod(FuzzyMethodContract.newBuilder().requireModifier(1024).returnTypeMatches(serverConnectionContract).build());
         return setMinecraftClass("ServerConnection", selected.getReturnType());
      }
   }

   public static Class getNBTBaseClass() {
      try {
         return getMinecraftClass("NBTBase");
      } catch (RuntimeException var4) {
         FuzzyClassContract tagCompoundContract = FuzzyClassContract.newBuilder().constructor(FuzzyMethodContract.newBuilder().parameterExactType(String.class).parameterCount(1)).field(FuzzyFieldContract.newBuilder().typeDerivedOf(Map.class)).build();
         Method selected = FuzzyReflection.fromClass(getPacketClass()).getMethod(FuzzyMethodContract.newBuilder().requireModifier(8).parameterSuperOf(DataInputStream.class).parameterCount(1).returnTypeMatches(tagCompoundContract).build());
         Class<?> nbtBase = selected.getReturnType().getSuperclass();
         if (nbtBase != null && !nbtBase.equals(Object.class)) {
            return setMinecraftClass("NBTBase", nbtBase);
         } else {
            throw new IllegalStateException("Unable to find NBT base class: " + nbtBase);
         }
      }
   }

   public static Class getNBTCompoundClass() {
      try {
         return getMinecraftClass("NBTTagCompound");
      } catch (RuntimeException var1) {
         return setMinecraftClass("NBTTagCompound", NbtFactory.ofWrapper(NbtType.TAG_COMPOUND, "Test").getHandle().getClass());
      }
   }

   public static Class getEntityTrackerClass() {
      try {
         return getMinecraftClass("EntityTracker");
      } catch (RuntimeException var3) {
         FuzzyClassContract entityTrackerContract = FuzzyClassContract.newBuilder().field(FuzzyFieldContract.newBuilder().typeDerivedOf(Set.class)).method(FuzzyMethodContract.newBuilder().parameterSuperOf(getEntityClass()).parameterCount(1).returnTypeVoid()).method(FuzzyMethodContract.newBuilder().parameterSuperOf(getEntityClass(), 0).parameterSuperOf(Integer.TYPE, 1).parameterSuperOf(Integer.TYPE, 2).parameterCount(3).returnTypeVoid()).build();
         Field selected = FuzzyReflection.fromClass(getWorldServerClass(), true).getField(FuzzyFieldContract.newBuilder().typeMatches(entityTrackerContract).build());
         return setMinecraftClass("EntityTracker", selected.getType());
      }
   }

   public static Class getNetworkListenThreadClass() {
      try {
         return getMinecraftClass("NetworkListenThread");
      } catch (RuntimeException var3) {
         FuzzyClassContract networkListenContract = FuzzyClassContract.newBuilder().field(FuzzyFieldContract.newBuilder().typeDerivedOf(ServerSocket.class)).field(FuzzyFieldContract.newBuilder().typeDerivedOf(Thread.class)).field(FuzzyFieldContract.newBuilder().typeDerivedOf(List.class)).method(FuzzyMethodContract.newBuilder().parameterExactType(getNetServerHandlerClass())).build();
         Field selected = FuzzyReflection.fromClass(getMinecraftServerClass(), true).getField(FuzzyFieldContract.newBuilder().typeMatches(networkListenContract).build());
         return setMinecraftClass("NetworkListenThread", selected.getType());
      }
   }

   public static Class getAttributeSnapshotClass() {
      try {
         return getMinecraftClass("AttributeSnapshot");
      } catch (RuntimeException var5) {
         Class<?> packetUpdateAttributes = PacketRegistry.getPacketClassFromID(44, true);
         final String packetSignature = packetUpdateAttributes.getCanonicalName().replace('.', '/');

         try {
            ClassReader reader = new ClassReader(packetUpdateAttributes.getCanonicalName());
            reader.accept(new EmptyClassVisitor() {
               public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                  return desc.startsWith("(Ljava/io/DataInput") ? new EmptyMethodVisitor() {
                     public void visitMethodInsn(int opcode, String owner, String name, String desc) {
                        if (opcode == 183 && MinecraftReflection.isConstructor(name)) {
                           String className = owner.replace('/', '.');
                           if (desc.startsWith("(L" + packetSignature)) {
                              MinecraftReflection.setMinecraftClass("AttributeSnapshot", MinecraftReflection.getClass(className));
                           } else if (desc.startsWith("(Ljava/util/UUID;Ljava/lang/String")) {
                              MinecraftReflection.setMinecraftClass("AttributeModifier", MinecraftReflection.getClass(className));
                           }
                        }

                     }
                  } : null;
               }
            }, 0);
         } catch (IOException e1) {
            throw new RuntimeException("Unable to read the content of Packet44UpdateAttributes.", e1);
         }

         return getMinecraftClass("AttributeSnapshot");
      }
   }

   public static Class getIntHashMapClass() {
      try {
         return getMinecraftClass("IntHashMap");
      } catch (RuntimeException var4) {
         Class<?> parent = getEntityTrackerClass();
         FuzzyClassContract intHashContract = FuzzyClassContract.newBuilder().method(FuzzyMethodContract.newBuilder().parameterCount(2).parameterExactType(Integer.TYPE, 0).parameterExactType(Object.class, 1).requirePublic()).method(FuzzyMethodContract.newBuilder().parameterCount(1).parameterExactType(Integer.TYPE).returnTypeExact(Object.class).requirePublic()).field(FuzzyFieldContract.newBuilder().typeMatches(FuzzyMatchers.matchArray(FuzzyMatchers.matchAll()))).build();
         AbstractFuzzyMatcher<Field> intHashField = FuzzyFieldContract.newBuilder().typeMatches(getMinecraftObjectMatcher().and(intHashContract)).build();
         return setMinecraftClass("IntHashMap", FuzzyReflection.fromClass(parent).getField(intHashField).getType());
      }
   }

   public static Class getAttributeModifierClass() {
      try {
         return getMinecraftClass("AttributeModifier");
      } catch (RuntimeException var1) {
         getAttributeSnapshotClass();
         return getMinecraftClass("AttributeModifier");
      }
   }

   private static boolean isConstructor(String name) {
      return "<init>".equals(name);
   }

   public static Class getItemStackArrayClass() {
      if (itemStackArrayClass == null) {
         itemStackArrayClass = getArrayClass(getItemStackClass());
      }

      return itemStackArrayClass;
   }

   public static Class getArrayClass(Class componentType) {
      return Array.newInstance(componentType, 0).getClass();
   }

   public static Class getCraftItemStackClass() {
      return getCraftBukkitClass("inventory.CraftItemStack");
   }

   public static Class getCraftPlayerClass() {
      return getCraftBukkitClass("entity.CraftPlayer");
   }

   public static Class getCraftEntityClass() {
      return getCraftBukkitClass("entity.CraftEntity");
   }

   public static ItemStack getBukkitItemStack(ItemStack bukkitItemStack) {
      if (craftBukkitMethod != null) {
         return getBukkitItemByMethod(bukkitItemStack);
      } else {
         if (craftBukkitConstructor == null) {
            try {
               craftBukkitConstructor = getCraftItemStackClass().getConstructor(ItemStack.class);
            } catch (Exception e) {
               if (!craftItemStackFailed) {
                  return getBukkitItemByMethod(bukkitItemStack);
               }

               throw new RuntimeException("Cannot find CraftItemStack(org.bukkit.inventory.ItemStack).", e);
            }
         }

         try {
            return (ItemStack)craftBukkitConstructor.newInstance(bukkitItemStack);
         } catch (Exception e) {
            throw new RuntimeException("Cannot construct CraftItemStack.", e);
         }
      }
   }

   private static ItemStack getBukkitItemByMethod(ItemStack bukkitItemStack) {
      if (craftBukkitMethod == null) {
         try {
            craftBukkitMethod = getCraftItemStackClass().getMethod("asCraftCopy", ItemStack.class);
         } catch (Exception e) {
            craftItemStackFailed = true;
            throw new RuntimeException("Cannot find CraftItemStack.asCraftCopy(org.bukkit.inventory.ItemStack).", e);
         }
      }

      try {
         return (ItemStack)craftBukkitMethod.invoke((Object)null, bukkitItemStack);
      } catch (Exception e) {
         throw new RuntimeException("Cannot construct CraftItemStack.", e);
      }
   }

   public static ItemStack getBukkitItemStack(Object minecraftItemStack) {
      if (craftNMSMethod != null) {
         return getBukkitItemByMethod(minecraftItemStack);
      } else {
         if (craftNMSConstructor == null) {
            try {
               craftNMSConstructor = getCraftItemStackClass().getConstructor(minecraftItemStack.getClass());
            } catch (Exception e) {
               if (!craftItemStackFailed) {
                  return getBukkitItemByMethod(minecraftItemStack);
               }

               throw new RuntimeException("Cannot find CraftItemStack(net.mineraft.server.ItemStack).", e);
            }
         }

         try {
            return (ItemStack)craftNMSConstructor.newInstance(minecraftItemStack);
         } catch (Exception e) {
            throw new RuntimeException("Cannot construct CraftItemStack.", e);
         }
      }
   }

   private static ItemStack getBukkitItemByMethod(Object minecraftItemStack) {
      if (craftNMSMethod == null) {
         try {
            craftNMSMethod = getCraftItemStackClass().getMethod("asCraftMirror", minecraftItemStack.getClass());
         } catch (Exception e) {
            craftItemStackFailed = true;
            throw new RuntimeException("Cannot find CraftItemStack.asCraftMirror(net.mineraft.server.ItemStack).", e);
         }
      }

      try {
         return (ItemStack)craftNMSMethod.invoke((Object)null, minecraftItemStack);
      } catch (Exception e) {
         throw new RuntimeException("Cannot construct CraftItemStack.", e);
      }
   }

   public static Object getMinecraftItemStack(ItemStack stack) {
      if (!isCraftItemStack(stack)) {
         stack = getBukkitItemStack(stack);
      }

      BukkitUnwrapper unwrapper = new BukkitUnwrapper();
      return unwrapper.unwrapItem(stack);
   }

   private static Class getClass(String className) {
      try {
         return MinecraftReflection.class.getClassLoader().loadClass(className);
      } catch (ClassNotFoundException e) {
         throw new RuntimeException("Cannot find class " + className, e);
      }
   }

   public static Class getCraftBukkitClass(String className) {
      if (craftbukkitPackage == null) {
         craftbukkitPackage = new CachedPackage(getCraftBukkitPackage());
      }

      return craftbukkitPackage.getPackageClass(className);
   }

   public static Class getMinecraftClass(String className) {
      if (minecraftPackage == null) {
         minecraftPackage = new CachedPackage(getMinecraftPackage());
      }

      return minecraftPackage.getPackageClass(className);
   }

   private static Class setMinecraftClass(String className, Class clazz) {
      if (minecraftPackage == null) {
         minecraftPackage = new CachedPackage(getMinecraftPackage());
      }

      minecraftPackage.setPackageClass(className, clazz);
      return clazz;
   }

   public static Class getMinecraftClass(String className, String... aliases) {
      try {
         return getMinecraftClass(className);
      } catch (RuntimeException var10) {
         Class<?> success = null;

         for(String alias : aliases) {
            try {
               success = getMinecraftClass(alias);
               break;
            } catch (RuntimeException var9) {
            }
         }

         if (success != null) {
            minecraftPackage.setPackageClass(className, success);
            return success;
         } else {
            throw new RuntimeException(String.format("Unable to find %s (%s)", className, Joiner.on(", ").join(aliases)));
         }
      }
   }

   public static String getNetworkManagerName() {
      return getNetworkManagerClass().getSimpleName();
   }

   public static String getNetLoginHandlerName() {
      return getNetLoginHandlerClass().getSimpleName();
   }
}
