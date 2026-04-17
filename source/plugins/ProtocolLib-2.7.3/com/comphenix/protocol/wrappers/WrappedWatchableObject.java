package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.instances.DefaultInstances;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.base.Objects;
import java.lang.reflect.Constructor;
import org.bukkit.inventory.ItemStack;

public class WrappedWatchableObject {
   private static boolean hasInitialized;
   private static StructureModifier baseModifier;
   private static Constructor watchableConstructor;
   private static Class watchableObjectClass;
   protected Object handle;
   protected StructureModifier modifier;
   private Class typeClass;

   public WrappedWatchableObject(Object handle) {
      this.load(handle);
   }

   public WrappedWatchableObject(int index, Object value) {
      if (value == null) {
         throw new IllegalArgumentException("Value cannot be NULL.");
      } else {
         Integer typeID = WrappedDataWatcher.getTypeID(value.getClass());
         if (typeID != null) {
            if (watchableConstructor == null) {
               try {
                  watchableConstructor = MinecraftReflection.getWatchableObjectClass().getConstructor(Integer.TYPE, Integer.TYPE, Object.class);
               } catch (Exception e) {
                  throw new RuntimeException("Cannot get the WatchableObject(int, int, Object) constructor.", e);
               }
            }

            try {
               this.load(watchableConstructor.newInstance(typeID, index, getUnwrapped(value)));
            } catch (Exception e) {
               throw new RuntimeException("Cannot construct underlying WatchableObject.", e);
            }
         } else {
            throw new IllegalArgumentException("Cannot watch the type " + value.getClass());
         }
      }
   }

   private void load(Object handle) {
      initialize();
      this.handle = handle;
      this.modifier = baseModifier.withTarget(handle);
      if (!watchableObjectClass.isAssignableFrom(handle.getClass())) {
         throw new ClassCastException("Cannot cast the class " + handle.getClass().getName() + " to " + watchableObjectClass.getName());
      }
   }

   public Object getHandle() {
      return this.handle;
   }

   private static void initialize() {
      if (!hasInitialized) {
         hasInitialized = true;
         watchableObjectClass = MinecraftReflection.getWatchableObjectClass();
         baseModifier = new StructureModifier(watchableObjectClass, (Class)null, false);
      }

   }

   public Class getType() throws FieldAccessException {
      return getWrappedType(this.getTypeRaw());
   }

   private Class getTypeRaw() throws FieldAccessException {
      if (this.typeClass == null) {
         this.typeClass = WrappedDataWatcher.getTypeClass(this.getTypeID());
         if (this.typeClass == null) {
            throw new IllegalStateException("Unrecognized data type: " + this.getTypeID());
         }
      }

      return this.typeClass;
   }

   public int getIndex() throws FieldAccessException {
      return (Integer)this.modifier.withType(Integer.TYPE).read(1);
   }

   public void setIndex(int index) throws FieldAccessException {
      this.modifier.withType(Integer.TYPE).write(1, index);
   }

   public int getTypeID() throws FieldAccessException {
      return (Integer)this.modifier.withType(Integer.TYPE).read(0);
   }

   public void setTypeID(int id) throws FieldAccessException {
      this.modifier.withType(Integer.TYPE).write(0, id);
   }

   public void setValue(Object newValue) throws FieldAccessException {
      this.setValue(newValue, true);
   }

   public void setValue(Object newValue, boolean updateClient) throws FieldAccessException {
      if (newValue == null) {
         throw new IllegalArgumentException("Cannot watch a NULL value.");
      } else if (!this.getType().isAssignableFrom(newValue.getClass())) {
         throw new IllegalArgumentException("Object " + newValue + " must be of type " + this.getType().getName());
      } else {
         if (updateClient) {
            this.setDirtyState(true);
         }

         this.modifier.withType(Object.class).write(0, getUnwrapped(newValue));
      }
   }

   private Object getNMSValue() {
      return this.modifier.withType(Object.class).read(0);
   }

   public Object getValue() throws FieldAccessException {
      return getWrapped(this.modifier.withType(Object.class).read(0));
   }

   public void setDirtyState(boolean dirty) throws FieldAccessException {
      this.modifier.withType(Boolean.TYPE).write(0, dirty);
   }

   public boolean getDirtyState() throws FieldAccessException {
      return (Boolean)this.modifier.withType(Boolean.TYPE).read(0);
   }

   static Object getWrapped(Object value) {
      if (MinecraftReflection.isItemStack(value)) {
         return BukkitConverters.getItemStackConverter().getSpecific(value);
      } else {
         return MinecraftReflection.isChunkCoordinates(value) ? new WrappedChunkCoordinate((Comparable)value) : value;
      }
   }

   static Class getWrappedType(Class unwrapped) {
      if (unwrapped.equals(MinecraftReflection.getChunkPositionClass())) {
         return ChunkPosition.class;
      } else {
         return unwrapped.equals(MinecraftReflection.getChunkCoordinatesClass()) ? WrappedChunkCoordinate.class : unwrapped;
      }
   }

   static Object getUnwrapped(Object wrapped) {
      if (wrapped instanceof WrappedChunkCoordinate) {
         return ((WrappedChunkCoordinate)wrapped).getHandle();
      } else {
         return wrapped instanceof ItemStack ? BukkitConverters.getItemStackConverter().getGeneric(MinecraftReflection.getItemStackClass(), (ItemStack)wrapped) : wrapped;
      }
   }

   static Class getUnwrappedType(Class wrapped) {
      if (wrapped.equals(ChunkPosition.class)) {
         return MinecraftReflection.getChunkPositionClass();
      } else {
         return wrapped.equals(WrappedChunkCoordinate.class) ? MinecraftReflection.getChunkCoordinatesClass() : wrapped;
      }
   }

   public WrappedWatchableObject deepClone() throws FieldAccessException {
      WrappedWatchableObject clone = new WrappedWatchableObject(DefaultInstances.DEFAULT.getDefault(MinecraftReflection.getWatchableObjectClass()));
      clone.setDirtyState(this.getDirtyState());
      clone.setIndex(this.getIndex());
      clone.setTypeID(this.getTypeID());
      clone.setValue(this.getClonedValue(), false);
      return clone;
   }

   Object getClonedValue() throws FieldAccessException {
      Object value = this.getNMSValue();
      if (MinecraftReflection.isChunkPosition(value)) {
         EquivalentConverter<ChunkPosition> converter = ChunkPosition.getConverter();
         return converter.getGeneric(MinecraftReflection.getChunkPositionClass(), converter.getSpecific(value));
      } else {
         return MinecraftReflection.isItemStack(value) ? MinecraftReflection.getMinecraftItemStack(MinecraftReflection.getBukkitItemStack(value).clone()) : value;
      }
   }

   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (!(obj instanceof WrappedWatchableObject)) {
         return false;
      } else {
         WrappedWatchableObject other = (WrappedWatchableObject)obj;
         return Objects.equal(this.getIndex(), other.getIndex()) && Objects.equal(this.getTypeID(), other.getTypeID()) && Objects.equal(this.getValue(), other.getValue());
      }
   }

   public int hashCode() {
      return Objects.hashCode(new Object[]{this.getIndex(), this.getTypeID(), this.getValue()});
   }

   public String toString() {
      return String.format("[%s: %s (%s)]", this.getIndex(), this.getValue(), this.getType().getSimpleName());
   }
}
