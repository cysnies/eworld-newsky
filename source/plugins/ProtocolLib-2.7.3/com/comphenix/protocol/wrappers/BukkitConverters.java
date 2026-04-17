package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.instances.DefaultInstances;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

public class BukkitConverters {
   private static boolean hasWorldType = false;
   private static boolean hasAttributeSnapshot = false;
   private static Map specificConverters;
   private static Map genericConverters;
   private static Method worldTypeName;
   private static Method worldTypeGetType;

   public static EquivalentConverter getListConverter(final Class genericItemType, final EquivalentConverter itemConverter) {
      return new IgnoreNullConverter() {
         protected List getSpecificValue(Object generic) {
            if (generic instanceof Collection) {
               List<T> items = new ArrayList();

               for(Object item : (Collection)generic) {
                  T result = (T)itemConverter.getSpecific(item);
                  if (item != null) {
                     items.add(result);
                  }
               }

               return items;
            } else {
               return null;
            }
         }

         protected Object getGenericValue(Class genericType, List specific) {
            Collection<Object> newContainer = (Collection)DefaultInstances.DEFAULT.getDefault(genericType);

            for(Object position : specific) {
               Object converted = itemConverter.getGeneric(genericItemType, position);
               if (position == null) {
                  newContainer.add((Object)null);
               } else if (converted != null) {
                  newContainer.add(converted);
               }
            }

            return newContainer;
         }

         public Class getSpecificType() {
            Class<?> dummy = List.class;
            return dummy;
         }
      };
   }

   public static EquivalentConverter getWrappedAttributeConverter() {
      return new IgnoreNullConverter() {
         protected Object getGenericValue(Class genericType, WrappedAttribute specific) {
            return specific.getHandle();
         }

         protected WrappedAttribute getSpecificValue(Object generic) {
            return WrappedAttribute.fromHandle(generic);
         }

         public Class getSpecificType() {
            return WrappedAttribute.class;
         }
      };
   }

   public static EquivalentConverter getWatchableObjectConverter() {
      return new IgnoreNullConverter() {
         protected Object getGenericValue(Class genericType, WrappedWatchableObject specific) {
            return specific.getHandle();
         }

         protected WrappedWatchableObject getSpecificValue(Object generic) {
            if (MinecraftReflection.isWatchableObject(generic)) {
               return new WrappedWatchableObject(generic);
            } else if (generic instanceof WrappedWatchableObject) {
               return (WrappedWatchableObject)generic;
            } else {
               throw new IllegalArgumentException("Unrecognized type " + generic.getClass());
            }
         }

         public Class getSpecificType() {
            return WrappedWatchableObject.class;
         }
      };
   }

   public static EquivalentConverter getDataWatcherConverter() {
      return new IgnoreNullConverter() {
         protected Object getGenericValue(Class genericType, WrappedDataWatcher specific) {
            return specific.getHandle();
         }

         protected WrappedDataWatcher getSpecificValue(Object generic) {
            if (MinecraftReflection.isDataWatcher(generic)) {
               return new WrappedDataWatcher(generic);
            } else if (generic instanceof WrappedDataWatcher) {
               return (WrappedDataWatcher)generic;
            } else {
               throw new IllegalArgumentException("Unrecognized type " + generic.getClass());
            }
         }

         public Class getSpecificType() {
            return WrappedDataWatcher.class;
         }
      };
   }

   public static EquivalentConverter getWorldTypeConverter() {
      return !hasWorldType ? null : new IgnoreNullConverter() {
         protected Object getGenericValue(Class genericType, WorldType specific) {
            try {
               if (BukkitConverters.worldTypeGetType == null) {
                  BukkitConverters.worldTypeGetType = MinecraftReflection.getWorldTypeClass().getMethod("getType", String.class);
               }

               return BukkitConverters.worldTypeGetType.invoke(this, specific.getName());
            } catch (Exception e) {
               throw new FieldAccessException("Cannot find the WorldType.getType() method.", e);
            }
         }

         protected WorldType getSpecificValue(Object generic) {
            try {
               if (BukkitConverters.worldTypeName == null) {
                  BukkitConverters.worldTypeName = MinecraftReflection.getWorldTypeClass().getMethod("name");
               }

               String name = (String)BukkitConverters.worldTypeName.invoke(generic);
               return WorldType.getByName(name);
            } catch (Exception e) {
               throw new FieldAccessException("Cannot call the name method in WorldType.", e);
            }
         }

         public Class getSpecificType() {
            return WorldType.class;
         }
      };
   }

   public static EquivalentConverter getNbtConverter() {
      return new IgnoreNullConverter() {
         protected Object getGenericValue(Class genericType, NbtBase specific) {
            return NbtFactory.fromBase(specific).getHandle();
         }

         protected NbtBase getSpecificValue(Object generic) {
            return NbtFactory.fromNMS(generic);
         }

         public Class getSpecificType() {
            Class<?> dummy = NbtBase.class;
            return dummy;
         }
      };
   }

   public static EquivalentConverter getEntityConverter(World world) {
      final WeakReference<ProtocolManager> managerRef = new WeakReference(ProtocolLibrary.getProtocolManager());
      return new WorldSpecificConverter(world) {
         public Object getGenericValue(Class genericType, Entity specific) {
            return specific.getEntityId();
         }

         public Entity getSpecificValue(Object generic) {
            try {
               Integer id = (Integer)generic;
               ProtocolManager manager = (ProtocolManager)managerRef.get();
               return id != null && manager != null ? manager.getEntityFromID(this.world, id) : null;
            } catch (FieldAccessException e) {
               throw new RuntimeException("Cannot retrieve entity from ID.", e);
            }
         }

         public Class getSpecificType() {
            return Entity.class;
         }
      };
   }

   public static EquivalentConverter getItemStackConverter() {
      return new IgnoreNullConverter() {
         protected Object getGenericValue(Class genericType, ItemStack specific) {
            return MinecraftReflection.getMinecraftItemStack(specific);
         }

         protected ItemStack getSpecificValue(Object generic) {
            return MinecraftReflection.getBukkitItemStack(generic);
         }

         public Class getSpecificType() {
            return ItemStack.class;
         }
      };
   }

   public static EquivalentConverter getIgnoreNull(final EquivalentConverter delegate) {
      return new IgnoreNullConverter() {
         public Object getGenericValue(Class genericType, Object specific) {
            return delegate.getGeneric(genericType, specific);
         }

         public Object getSpecificValue(Object generic) {
            return delegate.getSpecific(generic);
         }

         public Class getSpecificType() {
            return delegate.getSpecificType();
         }
      };
   }

   public static Map getConvertersForSpecific() {
      if (specificConverters == null) {
         ImmutableMap.Builder<Class<?>, EquivalentConverter<Object>> builder = ImmutableMap.builder().put(WrappedDataWatcher.class, getDataWatcherConverter()).put(ItemStack.class, getItemStackConverter()).put(NbtBase.class, getNbtConverter()).put(NbtCompound.class, getNbtConverter()).put(WrappedWatchableObject.class, getWatchableObjectConverter());
         if (hasWorldType) {
            builder.put(WorldType.class, getWorldTypeConverter());
         }

         if (hasAttributeSnapshot) {
            builder.put(WrappedAttribute.class, getWrappedAttributeConverter());
         }

         specificConverters = builder.build();
      }

      return specificConverters;
   }

   public static Map getConvertersForGeneric() {
      if (genericConverters == null) {
         ImmutableMap.Builder<Class<?>, EquivalentConverter<Object>> builder = ImmutableMap.builder().put(MinecraftReflection.getDataWatcherClass(), getDataWatcherConverter()).put(MinecraftReflection.getItemStackClass(), getItemStackConverter()).put(MinecraftReflection.getNBTBaseClass(), getNbtConverter()).put(MinecraftReflection.getNBTCompoundClass(), getNbtConverter()).put(MinecraftReflection.getWatchableObjectClass(), getWatchableObjectConverter());
         if (hasWorldType) {
            builder.put(MinecraftReflection.getWorldTypeClass(), getWorldTypeConverter());
         }

         if (hasAttributeSnapshot) {
            builder.put(MinecraftReflection.getAttributeSnapshotClass(), getWrappedAttributeConverter());
         }

         genericConverters = builder.build();
      }

      return genericConverters;
   }

   static {
      try {
         MinecraftReflection.getWorldTypeClass();
         hasWorldType = true;
      } catch (Exception var2) {
      }

      try {
         MinecraftReflection.getAttributeSnapshotClass();
         hasAttributeSnapshot = true;
      } catch (Exception var1) {
      }

   }

   private abstract static class IgnoreNullConverter implements EquivalentConverter {
      private IgnoreNullConverter() {
      }

      public final Object getGeneric(Class genericType, Object specific) {
         return specific != null ? this.getGenericValue(genericType, specific) : null;
      }

      protected abstract Object getGenericValue(Class var1, Object var2);

      public final Object getSpecific(Object generic) {
         return generic != null ? this.getSpecificValue(generic) : null;
      }

      protected abstract Object getSpecificValue(Object var1);

      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else if (obj == null) {
            return false;
         } else if (obj instanceof EquivalentConverter) {
            EquivalentConverter other = (EquivalentConverter)obj;
            return Objects.equal(this.getSpecificType(), other.getSpecificType());
         } else {
            return false;
         }
      }
   }

   private abstract static class WorldSpecificConverter extends IgnoreNullConverter {
      protected World world;

      public WorldSpecificConverter(World world) {
         this.world = world;
      }

      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         } else if (obj == null) {
            return false;
         } else if (obj instanceof WorldSpecificConverter && super.equals(obj)) {
            WorldSpecificConverter other = (WorldSpecificConverter)obj;
            return Objects.equal(this.world, other.world);
         } else {
            return false;
         }
      }
   }
}
