package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.injector.BukkitUnwrapper;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.collection.ConvertedMap;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Iterators;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.Nullable;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

public class WrappedDataWatcher implements Iterable {
   private static Map typeMap;
   private static Field valueMapField;
   private static Field readWriteLockField;
   private static Method createKeyValueMethod;
   private static Method updateKeyValueMethod;
   private static Method getKeyValueMethod;
   private static volatile Field entityDataField;
   private static boolean hasInitialized;
   protected Object handle;
   private ReadWriteLock readWriteLock;
   private Map watchableObjects;
   private Map mapView;

   public WrappedDataWatcher() {
      try {
         this.handle = MinecraftReflection.getDataWatcherClass().newInstance();
         initialize();
      } catch (Exception e) {
         throw new RuntimeException("Unable to construct DataWatcher.", e);
      }
   }

   public WrappedDataWatcher(Object handle) {
      if (handle == null) {
         throw new IllegalArgumentException("Handle cannot be NULL.");
      } else if (!MinecraftReflection.isDataWatcher(handle)) {
         throw new IllegalArgumentException("The value " + handle + " is not a DataWatcher.");
      } else {
         this.handle = handle;
         initialize();
      }
   }

   public WrappedDataWatcher(List watchableObjects) throws FieldAccessException {
      this();
      Lock writeLock = this.getReadWriteLock().writeLock();
      Map<Integer, Object> map = this.getWatchableObjectMap();
      writeLock.lock();

      try {
         for(WrappedWatchableObject watched : watchableObjects) {
            map.put(watched.getIndex(), watched.handle);
         }
      } finally {
         writeLock.unlock();
      }

   }

   public Object getHandle() {
      return this.handle;
   }

   public static Integer getTypeID(Class clazz) throws FieldAccessException {
      initialize();
      return (Integer)typeMap.get(WrappedWatchableObject.getUnwrappedType(clazz));
   }

   public static Class getTypeClass(int id) throws FieldAccessException {
      initialize();

      for(Map.Entry entry : typeMap.entrySet()) {
         if (Objects.equal(entry.getValue(), id)) {
            return (Class)entry.getKey();
         }
      }

      return null;
   }

   public Byte getByte(int index) throws FieldAccessException {
      return (Byte)this.getObject(index);
   }

   public Short getShort(int index) throws FieldAccessException {
      return (Short)this.getObject(index);
   }

   public Integer getInteger(int index) throws FieldAccessException {
      return (Integer)this.getObject(index);
   }

   public Float getFloat(int index) throws FieldAccessException {
      return (Float)this.getObject(index);
   }

   public String getString(int index) throws FieldAccessException {
      return (String)this.getObject(index);
   }

   public ItemStack getItemStack(int index) throws FieldAccessException {
      return (ItemStack)this.getObject(index);
   }

   public WrappedChunkCoordinate getChunkCoordinate(int index) throws FieldAccessException {
      return (WrappedChunkCoordinate)this.getObject(index);
   }

   public Object getObject(int index) throws FieldAccessException {
      Object watchable = this.getWatchedObject(index);
      return watchable != null ? (new WrappedWatchableObject(watchable)).getValue() : null;
   }

   public List getWatchableObjects() throws FieldAccessException {
      Lock readLock = this.getReadWriteLock().readLock();
      readLock.lock();

      Object var8;
      try {
         List<WrappedWatchableObject> result = new ArrayList();

         for(Object watchable : this.getWatchableObjectMap().values()) {
            if (watchable != null) {
               result.add(new WrappedWatchableObject(watchable));
            } else {
               result.add((Object)null);
            }
         }

         var8 = result;
      } finally {
         readLock.unlock();
      }

      return (List)var8;
   }

   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (!(obj instanceof WrappedDataWatcher)) {
         return false;
      } else {
         WrappedDataWatcher other = (WrappedDataWatcher)obj;
         Iterator<WrappedWatchableObject> first = this.iterator();
         Iterator<WrappedWatchableObject> second = other.iterator();
         if (this.size() != other.size()) {
            return false;
         } else {
            while(first.hasNext() && second.hasNext()) {
               if (!((WrappedWatchableObject)first.next()).equals(second.next())) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   public int hashCode() {
      return this.getWatchableObjects().hashCode();
   }

   public Set indexSet() throws FieldAccessException {
      Lock readLock = this.getReadWriteLock().readLock();
      readLock.lock();

      HashSet var2;
      try {
         var2 = new HashSet(this.getWatchableObjectMap().keySet());
      } finally {
         readLock.unlock();
      }

      return var2;
   }

   public WrappedDataWatcher deepClone() {
      WrappedDataWatcher clone = new WrappedDataWatcher();

      for(WrappedWatchableObject watchable : this) {
         clone.setObject(watchable.getIndex(), watchable.getClonedValue());
      }

      return clone;
   }

   public int size() throws FieldAccessException {
      Lock readLock = this.getReadWriteLock().readLock();
      readLock.lock();

      int var2;
      try {
         var2 = this.getWatchableObjectMap().size();
      } finally {
         readLock.unlock();
      }

      return var2;
   }

   public WrappedWatchableObject removeObject(int index) {
      Lock writeLock = this.getReadWriteLock().writeLock();
      writeLock.lock();

      WrappedWatchableObject var4;
      try {
         Object removed = this.getWatchableObjectMap().remove(index);
         var4 = removed != null ? new WrappedWatchableObject(removed) : null;
      } finally {
         writeLock.unlock();
      }

      return var4;
   }

   public void setObject(int index, Object newValue) throws FieldAccessException {
      this.setObject(index, newValue, true);
   }

   public void setObject(int index, Object newValue, boolean update) throws FieldAccessException {
      Lock writeLock = this.getReadWriteLock().writeLock();
      writeLock.lock();

      try {
         Object watchable = this.getWatchedObject(index);
         if (watchable != null) {
            (new WrappedWatchableObject(watchable)).setValue(newValue, update);
         } else {
            createKeyValueMethod.invoke(this.handle, index, WrappedWatchableObject.getUnwrapped(newValue));
         }
      } catch (IllegalArgumentException e) {
         throw new FieldAccessException("Cannot convert arguments.", e);
      } catch (IllegalAccessException e) {
         throw new FieldAccessException("Illegal access.", e);
      } catch (InvocationTargetException e) {
         throw new FieldAccessException("Checked exception in Minecraft.", e);
      } finally {
         writeLock.unlock();
      }

   }

   private Object getWatchedObject(int index) throws FieldAccessException {
      if (getKeyValueMethod != null) {
         try {
            return getKeyValueMethod.invoke(this.handle, index);
         } catch (Exception e) {
            throw new FieldAccessException("Cannot invoke get key method for index " + index, e);
         }
      } else {
         Object e;
         try {
            this.getReadWriteLock().readLock().lock();
            e = this.getWatchableObjectMap().get(index);
         } finally {
            this.getReadWriteLock().readLock().unlock();
         }

         return e;
      }
   }

   protected ReadWriteLock getReadWriteLock() throws FieldAccessException {
      try {
         if (this.readWriteLock != null) {
            return this.readWriteLock;
         } else {
            return readWriteLockField != null ? (this.readWriteLock = (ReadWriteLock)FieldUtils.readField(readWriteLockField, this.handle, true)) : (this.readWriteLock = new ReentrantReadWriteLock());
         }
      } catch (IllegalAccessException e) {
         throw new FieldAccessException("Unable to read lock field.", e);
      }
   }

   protected Map getWatchableObjectMap() throws FieldAccessException {
      if (this.watchableObjects == null) {
         try {
            this.watchableObjects = (Map)FieldUtils.readField(valueMapField, this.handle, true);
         } catch (IllegalAccessException e) {
            throw new FieldAccessException("Cannot read watchable object field.", e);
         }
      }

      return this.watchableObjects;
   }

   public static WrappedDataWatcher getEntityWatcher(Entity entity) throws FieldAccessException {
      if (entityDataField == null) {
         entityDataField = FuzzyReflection.fromClass(MinecraftReflection.getEntityClass(), true).getFieldByType("datawatcher", MinecraftReflection.getDataWatcherClass());
      }

      BukkitUnwrapper unwrapper = new BukkitUnwrapper();

      try {
         Object nsmWatcher = FieldUtils.readField(entityDataField, unwrapper.unwrapItem(entity), true);
         return nsmWatcher != null ? new WrappedDataWatcher(nsmWatcher) : null;
      } catch (IllegalAccessException e) {
         throw new FieldAccessException("Cannot access DataWatcher field.", e);
      }
   }

   private static void initialize() throws FieldAccessException {
      if (!hasInitialized) {
         hasInitialized = true;
         FuzzyReflection fuzzy = FuzzyReflection.fromClass(MinecraftReflection.getDataWatcherClass(), true);

         for(Field lookup : fuzzy.getFieldListByType(Map.class)) {
            if (Modifier.isStatic(lookup.getModifiers())) {
               try {
                  typeMap = (Map)FieldUtils.readStaticField(lookup, true);
               } catch (IllegalAccessException e) {
                  throw new FieldAccessException("Cannot access type map field.", e);
               }
            } else {
               valueMapField = lookup;
            }
         }

         try {
            readWriteLockField = fuzzy.getFieldByType("readWriteLock", ReadWriteLock.class);
         } catch (IllegalArgumentException var4) {
         }

         initializeMethods(fuzzy);
      }
   }

   private static void initializeMethods(FuzzyReflection fuzzy) {
      List<Method> candidates = fuzzy.getMethodListByParameters(Void.TYPE, new Class[]{Integer.TYPE, Object.class});

      try {
         getKeyValueMethod = fuzzy.getMethodByParameters("getWatchableObject", MinecraftReflection.getWatchableObjectClass(), new Class[]{Integer.TYPE});
         getKeyValueMethod.setAccessible(true);
      } catch (IllegalArgumentException var5) {
      }

      for(Method method : candidates) {
         if (!method.getName().startsWith("watch")) {
            createKeyValueMethod = method;
         } else {
            updateKeyValueMethod = method;
         }
      }

      if (updateKeyValueMethod == null || createKeyValueMethod == null) {
         if (candidates.size() <= 1) {
            throw new IllegalStateException("Unable to find create and update watchable object. Update ProtocolLib.");
         }

         createKeyValueMethod = (Method)candidates.get(0);
         updateKeyValueMethod = (Method)candidates.get(1);

         try {
            WrappedDataWatcher watcher = new WrappedDataWatcher();
            watcher.setObject(0, 0);
            watcher.setObject(0, 1);
            if (watcher.getInteger(0) != 1) {
               throw new IllegalStateException("This cannot be!");
            }
         } catch (Exception var4) {
            updateKeyValueMethod = (Method)candidates.get(0);
            createKeyValueMethod = (Method)candidates.get(1);
         }
      }

   }

   public Iterator iterator() {
      return Iterators.transform(this.getWatchableObjectMap().values().iterator(), new Function() {
         public WrappedWatchableObject apply(@Nullable Object item) {
            return item != null ? new WrappedWatchableObject(item) : null;
         }
      });
   }

   public Map asMap() {
      if (this.mapView == null) {
         this.mapView = new ConvertedMap(this.getWatchableObjectMap()) {
            protected Object toInner(WrappedWatchableObject outer) {
               return outer == null ? null : outer.getHandle();
            }

            protected WrappedWatchableObject toOuter(Object inner) {
               return inner == null ? null : new WrappedWatchableObject(inner);
            }
         };
      }

      return this.mapView;
   }

   public String toString() {
      return this.asMap().toString();
   }
}
