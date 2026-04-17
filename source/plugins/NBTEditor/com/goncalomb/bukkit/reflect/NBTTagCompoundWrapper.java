package com.goncalomb.bukkit.reflect;

import java.lang.reflect.Method;
import java.util.Collection;

public final class NBTTagCompoundWrapper extends NBTBaseWrapper {
   private static Method _getByte;
   private static Method _getShort;
   private static Method _getInt;
   private static Method _getLong;
   private static Method _getFloat;
   private static Method _getDouble;
   private static Method _getString;
   private static Method _getCompound;
   private static Method _getList;
   private static Method _get;
   private static Method _setByte;
   private static Method _setShort;
   private static Method _setInt;
   private static Method _setLong;
   private static Method _setDouble;
   private static Method _setFloat;
   private static Method _setString;
   private static Method _setCompound;
   private static Method _set;
   private static Method _hasKey;
   private static Method _remove;
   private static Method _c;
   private static Method _tagCompoundSerialize;
   private static Method _tagCompoundUnserialize;

   static void prepareReflectionz() throws SecurityException, NoSuchMethodException {
      _getByte = _nbtTagCompoundClass.getMethod("getByte", String.class);
      _getShort = _nbtTagCompoundClass.getMethod("getShort", String.class);
      _getInt = _nbtTagCompoundClass.getMethod("getInt", String.class);
      _getLong = _nbtTagCompoundClass.getMethod("getLong", String.class);
      _getFloat = _nbtTagCompoundClass.getMethod("getFloat", String.class);
      _getDouble = _nbtTagCompoundClass.getMethod("getDouble", String.class);
      _getString = _nbtTagCompoundClass.getMethod("getString", String.class);
      _getCompound = _nbtTagCompoundClass.getMethod("getCompound", String.class);
      _getList = _nbtTagCompoundClass.getMethod("getList", String.class);
      _get = _nbtTagCompoundClass.getMethod("get", String.class);
      _setByte = _nbtTagCompoundClass.getMethod("setByte", String.class, Byte.TYPE);
      _setShort = _nbtTagCompoundClass.getMethod("setShort", String.class, Short.TYPE);
      _setInt = _nbtTagCompoundClass.getMethod("setInt", String.class, Integer.TYPE);
      _setLong = _nbtTagCompoundClass.getMethod("setLong", String.class, Long.TYPE);
      _setFloat = _nbtTagCompoundClass.getMethod("setFloat", String.class, Float.TYPE);
      _setDouble = _nbtTagCompoundClass.getMethod("setDouble", String.class, Double.TYPE);
      _setString = _nbtTagCompoundClass.getMethod("setString", String.class, String.class);
      _setCompound = _nbtTagCompoundClass.getMethod("setCompound", String.class, _nbtTagCompoundClass);
      _set = _nbtTagCompoundClass.getMethod("set", String.class, _nbtBaseClass);
      _hasKey = _nbtTagCompoundClass.getMethod("hasKey", String.class);
      _remove = _nbtTagCompoundClass.getMethod("remove", String.class);
      _c = _nbtTagCompoundClass.getMethod("c");
      Class<?> nbtCompressedStreamToolsClass = BukkitReflect.getMinecraftClass("NBTCompressedStreamTools");
      _tagCompoundSerialize = nbtCompressedStreamToolsClass.getMethod("a", _nbtTagCompoundClass);
      _tagCompoundUnserialize = nbtCompressedStreamToolsClass.getMethod("a", byte[].class);
   }

   public NBTTagCompoundWrapper() {
      super(BukkitReflect.newInstance(_nbtTagCompoundClass));
   }

   NBTTagCompoundWrapper(Object nbtTagCompoundObject) {
      super(nbtTagCompoundObject);
   }

   public byte getByte(String key) {
      return (Byte)this.invokeMethod(_getByte, new Object[]{key});
   }

   public short getShort(String key) {
      return (Short)this.invokeMethod(_getShort, new Object[]{key});
   }

   public int getInt(String key) {
      return (Integer)this.invokeMethod(_getInt, new Object[]{key});
   }

   public long getLong(String key) {
      return (Long)this.invokeMethod(_getLong, new Object[]{key});
   }

   public float getFloat(String key) {
      return (Float)this.invokeMethod(_getFloat, new Object[]{key});
   }

   public Double getDouble(String key) {
      return (Double)this.invokeMethod(_getDouble, new Object[]{key});
   }

   public String getString(String key) {
      return (String)this.invokeMethod(_getString, new Object[]{key});
   }

   public NBTTagCompoundWrapper getCompound(String key) {
      return this.hasKey(key) ? new NBTTagCompoundWrapper(this.invokeMethod(_getCompound, new Object[]{key})) : null;
   }

   public NBTTagListWrapper getList(String key) {
      return this.hasKey(key) ? new NBTTagListWrapper(this.invokeMethod(_getList, new Object[]{key})) : null;
   }

   public Object[] getListAsArray(String key) {
      NBTTagListWrapper list = this.getList(key);
      return list == null ? null : list.getAsArray();
   }

   public Object get(String key) {
      return NBTTagTypeHandler.getObjectFromTag(this.invokeMethod(_get, new Object[]{key}));
   }

   public void setByte(String key, byte value) {
      this.invokeMethod(_setByte, new Object[]{key, value});
   }

   public void setShort(String key, short value) {
      this.invokeMethod(_setShort, new Object[]{key, value});
   }

   public void setInt(String key, int value) {
      this.invokeMethod(_setInt, new Object[]{key, value});
   }

   public void setLong(String key, long value) {
      this.invokeMethod(_setLong, new Object[]{key, value});
   }

   public void setFloat(String key, float value) {
      this.invokeMethod(_setFloat, new Object[]{key, value});
   }

   public void setDouble(String key, double value) {
      this.invokeMethod(_setDouble, new Object[]{key, value});
   }

   public void setString(String key, String value) {
      this.invokeMethod(_setString, new Object[]{key, value});
   }

   public void setCompound(String key, NBTTagCompoundWrapper value) {
      this.invokeMethod(_setCompound, new Object[]{key, value._nbtBaseObject});
   }

   public void setList(String key, NBTTagListWrapper value) {
      this.invokeMethod(_set, new Object[]{key, value._nbtBaseObject});
   }

   public void setList(String key, Object... objects) {
      this.invokeMethod(_set, new Object[]{key, (new NBTTagListWrapper(objects))._nbtBaseObject});
   }

   public void set(String key, Object value) {
      this.invokeMethod(_set, new Object[]{key, NBTTagTypeHandler.getTagFromObject(value)});
   }

   public boolean hasKey(String key) {
      return (Boolean)this.invokeMethod(_hasKey, new Object[]{key});
   }

   public void remove(String key) {
      this.invokeMethod(_remove, new Object[]{key});
   }

   public static NBTTagCompoundWrapper unserialize(byte[] data) {
      prepareReflection();

      try {
         return new NBTTagCompoundWrapper(_tagCompoundUnserialize.invoke((Object)null, data));
      } catch (Exception e) {
         throw new Error("Error while unserializing NBTTagCompound.", e);
      }
   }

   public byte[] serialize() {
      try {
         return (byte[])_tagCompoundSerialize.invoke((Object)null, this._nbtBaseObject);
      } catch (Exception e) {
         throw new Error("Error while serializing NBTTagCompound.", e);
      }
   }

   public void merge(NBTTagCompoundWrapper other) {
      for(Object tag : (Collection)BukkitReflect.invokeMethod(other._nbtBaseObject, _c)) {
         this.invokeMethod(_set, new Object[]{NBTBaseWrapper.getName(tag), NBTBaseWrapper.clone(tag)});
      }

   }

   public NBTTagCompoundWrapper clone() {
      return (NBTTagCompoundWrapper)super.clone();
   }
}
