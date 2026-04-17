package com.goncalomb.bukkit.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import org.apache.commons.lang.ClassUtils;

final class NBTTagTypeHandler {
   private static HashMap _innerTypeMap;
   private static HashMap _outerTypeMap;
   private Class _class;
   private Constructor _contructor;
   private Field _data;
   private Class _dataType;

   public static void prepareReflection() throws SecurityException, NoSuchMethodException, NoSuchFieldException {
      _innerTypeMap = new HashMap();
      _outerTypeMap = new HashMap();
      registerNew("NBTTagByte");
      registerNew("NBTTagShort");
      registerNew("NBTTagInt");
      registerNew("NBTTagLong");
      registerNew("NBTTagFloat");
      registerNew("NBTTagDouble");
      registerNew("NBTTagString");
   }

   private static void registerNew(String tagClassName) throws SecurityException, NoSuchMethodException, NoSuchFieldException {
      NBTTagTypeHandler handler = new NBTTagTypeHandler(tagClassName);
      _innerTypeMap.put(handler._dataType.isPrimitive() ? ClassUtils.primitiveToWrapper(handler._dataType) : handler._dataType, handler);
      _outerTypeMap.put(handler._class, handler);
   }

   public static Object getTagFromObject(Object object) {
      if (object instanceof NBTBaseWrapper) {
         return ((NBTBaseWrapper)object)._nbtBaseObject;
      } else {
         NBTTagTypeHandler handler = (NBTTagTypeHandler)_innerTypeMap.get(object.getClass());
         if (handler != null) {
            return handler.wrapWithTag(object);
         } else {
            throw new Error(object.getClass().getSimpleName() + " is not a valid NBTTag type.");
         }
      }
   }

   public static Object getObjectFromTag(Object tagObject) {
      if (tagObject == null) {
         return null;
      } else {
         NBTTagTypeHandler handler = (NBTTagTypeHandler)_outerTypeMap.get(tagObject.getClass());
         return handler != null ? handler.unwrapTag(tagObject) : NBTBaseWrapper.wrap(tagObject);
      }
   }

   private NBTTagTypeHandler(String tagClassName) throws SecurityException, NoSuchMethodException, NoSuchFieldException {
      this._class = BukkitReflect.getMinecraftClass(tagClassName);
      this._data = this._class.getDeclaredField("data");
      this._dataType = this._data.getType();
      this._contructor = this._class.getConstructor(String.class, this._dataType);
   }

   private Object wrapWithTag(Object innerObject) {
      return BukkitReflect.newInstance(this._contructor, "", innerObject);
   }

   private Object unwrapTag(Object tagObject) {
      try {
         return this._data.get(tagObject);
      } catch (Exception e) {
         throw new Error("Error while acessing data from " + this._class.getSimpleName() + ".", e);
      }
   }
}
