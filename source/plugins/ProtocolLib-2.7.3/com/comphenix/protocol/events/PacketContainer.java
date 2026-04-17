package com.comphenix.protocol.events;

import com.comphenix.protocol.concurrency.IntegerSet;
import com.comphenix.protocol.injector.StructureCache;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.ObjectWriter;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.cloning.AggregateCloner;
import com.comphenix.protocol.reflect.cloning.BukkitCloner;
import com.comphenix.protocol.reflect.cloning.Cloner;
import com.comphenix.protocol.reflect.cloning.CollectionCloner;
import com.comphenix.protocol.reflect.cloning.FieldCloner;
import com.comphenix.protocol.reflect.cloning.ImmutableDetector;
import com.comphenix.protocol.reflect.cloning.SerializableCloner;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.reflect.instances.DefaultInstances;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.StreamSerializer;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.ChunkPosition;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

public class PacketContainer implements Serializable {
   private static final long serialVersionUID = 2074805748222377230L;
   protected int id;
   protected transient Object handle;
   protected transient StructureModifier structureModifier;
   private static ConcurrentMap writeMethods = Maps.newConcurrentMap();
   private static ConcurrentMap readMethods = Maps.newConcurrentMap();
   private static final AggregateCloner DEEP_CLONER;
   private static final AggregateCloner SHALLOW_CLONER;
   private static final IntegerSet CLONING_UNSUPPORTED;

   public PacketContainer(int id) {
      this(id, StructureCache.newPacket(id));
   }

   public PacketContainer(int id, Object handle) {
      this(id, handle, StructureCache.getStructure(id).withTarget(handle));
   }

   public PacketContainer(int id, Object handle, StructureModifier structure) {
      if (handle == null) {
         throw new IllegalArgumentException("handle cannot be null.");
      } else {
         this.id = id;
         this.handle = handle;
         this.structureModifier = structure;
      }
   }

   protected PacketContainer() {
   }

   public Object getHandle() {
      return this.handle;
   }

   public StructureModifier getModifier() {
      return this.structureModifier;
   }

   public StructureModifier getSpecificModifier(Class primitiveType) {
      return this.structureModifier.withType(primitiveType);
   }

   public StructureModifier getBytes() {
      return this.structureModifier.withType(Byte.TYPE);
   }

   public StructureModifier getBooleans() {
      return this.structureModifier.withType(Boolean.TYPE);
   }

   public StructureModifier getShorts() {
      return this.structureModifier.withType(Short.TYPE);
   }

   public StructureModifier getIntegers() {
      return this.structureModifier.withType(Integer.TYPE);
   }

   public StructureModifier getLongs() {
      return this.structureModifier.withType(Long.TYPE);
   }

   public StructureModifier getFloat() {
      return this.structureModifier.withType(Float.TYPE);
   }

   public StructureModifier getDoubles() {
      return this.structureModifier.withType(Double.TYPE);
   }

   public StructureModifier getStrings() {
      return this.structureModifier.withType(String.class);
   }

   public StructureModifier getStringArrays() {
      return this.structureModifier.withType(String[].class);
   }

   public StructureModifier getByteArrays() {
      return this.structureModifier.withType(byte[].class);
   }

   public StreamSerializer getByteArraySerializer() {
      return new StreamSerializer();
   }

   public StructureModifier getIntegerArrays() {
      return this.structureModifier.withType(int[].class);
   }

   public StructureModifier getItemModifier() {
      return this.structureModifier.withType(MinecraftReflection.getItemStackClass(), BukkitConverters.getItemStackConverter());
   }

   public StructureModifier getItemArrayModifier() {
      return this.structureModifier.withType(MinecraftReflection.getItemStackArrayClass(), BukkitConverters.getIgnoreNull(new ItemStackArrayConverter()));
   }

   public StructureModifier getWorldTypeModifier() {
      return this.structureModifier.withType(MinecraftReflection.getWorldTypeClass(), BukkitConverters.getWorldTypeConverter());
   }

   public StructureModifier getDataWatcherModifier() {
      return this.structureModifier.withType(MinecraftReflection.getDataWatcherClass(), BukkitConverters.getDataWatcherConverter());
   }

   public StructureModifier getEntityModifier(@Nonnull World world) {
      Preconditions.checkNotNull(world, "world cannot be NULL.");
      return this.structureModifier.withType(Integer.TYPE, BukkitConverters.getEntityConverter(world));
   }

   public StructureModifier getEntityModifier(@Nonnull PacketEvent event) {
      Preconditions.checkNotNull(event, "event cannot be NULL.");
      return this.getEntityModifier(event.getPlayer().getWorld());
   }

   public StructureModifier getPositionModifier() {
      return this.structureModifier.withType(MinecraftReflection.getChunkPositionClass(), ChunkPosition.getConverter());
   }

   public StructureModifier getNbtModifier() {
      return this.structureModifier.withType(MinecraftReflection.getNBTBaseClass(), BukkitConverters.getNbtConverter());
   }

   public StructureModifier getAttributeCollectionModifier() {
      return this.structureModifier.withType(Collection.class, BukkitConverters.getListConverter(MinecraftReflection.getAttributeSnapshotClass(), BukkitConverters.getWrappedAttributeConverter()));
   }

   public StructureModifier getPositionCollectionModifier() {
      return this.structureModifier.withType(Collection.class, BukkitConverters.getListConverter(MinecraftReflection.getChunkPositionClass(), ChunkPosition.getConverter()));
   }

   public StructureModifier getWatchableCollectionModifier() {
      return this.structureModifier.withType(Collection.class, BukkitConverters.getListConverter(MinecraftReflection.getWatchableObjectClass(), BukkitConverters.getWatchableObjectConverter()));
   }

   public int getID() {
      return this.id;
   }

   public PacketContainer shallowClone() {
      Object clonedPacket = SHALLOW_CLONER.clone(this.getHandle());
      return new PacketContainer(this.getID(), clonedPacket);
   }

   public PacketContainer deepClone() {
      Object clonedPacket = null;
      if (CLONING_UNSUPPORTED.contains(this.id)) {
         clonedPacket = ((PacketContainer)SerializableCloner.clone((Serializable)this)).getHandle();
      } else {
         clonedPacket = DEEP_CLONER.clone(this.getHandle());
      }

      return new PacketContainer(this.getID(), clonedPacket);
   }

   private static Function getSpecializedDeepClonerFactory() {
      return new Function() {
         public Cloner apply(@Nullable AggregateCloner.BuilderParameters param) {
            return new FieldCloner(param.getAggregateCloner(), param.getInstanceProvider()) {
               {
                  this.writer = new ObjectWriter() {
                     protected void transformField(StructureModifier modifierSource, StructureModifier modifierDest, int fieldIndex) {
                        if (modifierSource.getField(fieldIndex).getName().startsWith("inflatedBuffer")) {
                           modifierDest.write(fieldIndex, modifierSource.read(fieldIndex));
                        } else {
                           defaultTransform(modifierSource, modifierDest, getDefaultCloner(), fieldIndex);
                        }

                     }
                  };
               }
            };
         }
      };
   }

   private void writeObject(ObjectOutputStream output) throws IOException {
      output.defaultWriteObject();
      output.writeBoolean(this.handle != null);

      try {
         this.getMethodLazily(writeMethods, this.handle.getClass(), "write", DataOutput.class).invoke(this.handle, new DataOutputStream(output));
      } catch (IllegalArgumentException e) {
         throw new IOException("Minecraft packet doesn't support DataOutputStream", e);
      } catch (IllegalAccessException e) {
         throw new RuntimeException("Insufficient security privileges.", e);
      } catch (InvocationTargetException e) {
         throw new IOException("Could not serialize Minecraft packet.", e);
      }
   }

   private void readObject(ObjectInputStream input) throws ClassNotFoundException, IOException {
      input.defaultReadObject();
      this.structureModifier = StructureCache.getStructure(this.id);
      if (input.readBoolean()) {
         this.handle = StructureCache.newPacket(this.id);

         try {
            this.getMethodLazily(readMethods, this.handle.getClass(), "read", DataInput.class).invoke(this.handle, new DataInputStream(input));
         } catch (IllegalArgumentException e) {
            throw new IOException("Minecraft packet doesn't support DataInputStream", e);
         } catch (IllegalAccessException e) {
            throw new RuntimeException("Insufficient security privileges.", e);
         } catch (InvocationTargetException e) {
            throw new IOException("Could not deserialize Minecraft packet.", e);
         }

         this.structureModifier = this.structureModifier.withTarget(this.handle);
      }

   }

   private Method getMethodLazily(ConcurrentMap lookup, Class handleClass, String methodName, Class parameterClass) {
      Method method = (Method)lookup.get(handleClass);
      if (method == null) {
         Method initialized = FuzzyReflection.fromClass(handleClass).getMethod(FuzzyMethodContract.newBuilder().parameterCount(1).parameterDerivedOf(parameterClass).returnTypeVoid().build());
         method = (Method)lookup.putIfAbsent(handleClass, initialized);
         if (method == null) {
            method = initialized;
         }
      }

      return method;
   }

   static {
      DEEP_CLONER = AggregateCloner.newBuilder().instanceProvider(DefaultInstances.DEFAULT).andThen(BukkitCloner.class).andThen(ImmutableDetector.class).andThen(CollectionCloner.class).andThen(getSpecializedDeepClonerFactory()).build();
      SHALLOW_CLONER = AggregateCloner.newBuilder().instanceProvider(DefaultInstances.DEFAULT).andThen(new Function() {
         public Cloner apply(@Nullable AggregateCloner.BuilderParameters param) {
            if (param == null) {
               throw new IllegalArgumentException("Cannot be NULL.");
            } else {
               return new FieldCloner(param.getAggregateCloner(), param.getInstanceProvider()) {
                  {
                     this.writer = new ObjectWriter();
                  }
               };
            }
         }
      }).build();
      CLONING_UNSUPPORTED = new IntegerSet(256, Arrays.asList(44));
   }

   private static class ItemStackArrayConverter implements EquivalentConverter {
      final EquivalentConverter stackConverter;

      private ItemStackArrayConverter() {
         this.stackConverter = BukkitConverters.getItemStackConverter();
      }

      public Object getGeneric(Class genericType, ItemStack[] specific) {
         Class<?> nmsStack = MinecraftReflection.getItemStackClass();
         Object[] result = Array.newInstance(nmsStack, specific.length);

         for(int i = 0; i < result.length; ++i) {
            result[i] = this.stackConverter.getGeneric(nmsStack, specific[i]);
         }

         return result;
      }

      public ItemStack[] getSpecific(Object generic) {
         Object[] input = generic;
         ItemStack[] result = new ItemStack[input.length];

         for(int i = 0; i < result.length; ++i) {
            result[i] = (ItemStack)this.stackConverter.getSpecific(input[i]);
         }

         return result;
      }

      public Class getSpecificType() {
         return ItemStack[].class;
      }
   }
}
