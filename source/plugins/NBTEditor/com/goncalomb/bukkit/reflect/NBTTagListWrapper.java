package com.goncalomb.bukkit.reflect;

import java.lang.reflect.Method;

public final class NBTTagListWrapper extends NBTBaseWrapper {
   private static Method _get;
   private static Method _add;
   private static Method _size;

   static void prepareReflectionz() throws SecurityException, NoSuchMethodException {
      _get = _nbtTagListClass.getMethod("get", Integer.TYPE);
      _add = _nbtTagListClass.getMethod("add", _nbtBaseClass);
      _size = _nbtTagListClass.getMethod("size");
   }

   public NBTTagListWrapper() {
      super(BukkitReflect.newInstance(_nbtTagListClass));
   }

   NBTTagListWrapper(Object nbtTagListObject) {
      super(nbtTagListObject);
   }

   public NBTTagListWrapper(Object... values) {
      super(BukkitReflect.newInstance(_nbtTagListClass));

      for(Object value : values) {
         this.add(value);
      }

   }

   public Object get(int index) {
      return NBTTagTypeHandler.getObjectFromTag(this.invokeMethod(_get, new Object[]{index}));
   }

   public void add(Object value) {
      this.invokeMethod(_add, new Object[]{NBTTagTypeHandler.getTagFromObject(value)});
   }

   public int size() {
      return (Integer)this.invokeMethod(_size, new Object[0]);
   }

   public Object[] getAsArray() {
      int length = this.size();
      Object[] result = new Object[length];

      for(int i = 0; i < length; ++i) {
         result[i] = this.get(i);
      }

      return result;
   }

   public NBTTagListWrapper clone() {
      return (NBTTagListWrapper)super.clone();
   }
}
