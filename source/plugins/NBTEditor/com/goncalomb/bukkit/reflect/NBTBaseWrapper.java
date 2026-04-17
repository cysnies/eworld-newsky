package com.goncalomb.bukkit.reflect;

import java.lang.reflect.Method;

public class NBTBaseWrapper {
   private static boolean _isPrepared = false;
   protected static Class _nbtBaseClass;
   protected static Class _nbtTagCompoundClass;
   protected static Class _nbtTagListClass;
   private static Method _getName;
   private static Method _clone;
   protected Object _nbtBaseObject;

   public static final void prepareReflection() {
      if (!_isPrepared) {
         _nbtBaseClass = BukkitReflect.getMinecraftClass("NBTBase");
         _nbtTagCompoundClass = BukkitReflect.getMinecraftClass("NBTTagCompound");
         _nbtTagListClass = BukkitReflect.getMinecraftClass("NBTTagList");

         try {
            _getName = _nbtBaseClass.getMethod("getName");
            _clone = _nbtBaseClass.getMethod("clone");
            NBTTagCompoundWrapper.prepareReflectionz();
            NBTTagListWrapper.prepareReflectionz();
            NBTTagTypeHandler.prepareReflection();
            NBTUtils.prepareReflection();
         } catch (Exception e) {
            _nbtBaseClass = null;
            throw new Error("Error while preparing NBTWrapper classes.", e);
         }

         _isPrepared = true;
      }

   }

   protected static final NBTBaseWrapper wrap(Object nbtBaseObject) {
      if (_nbtTagCompoundClass.isInstance(nbtBaseObject)) {
         return new NBTTagCompoundWrapper(nbtBaseObject);
      } else {
         return (NBTBaseWrapper)(_nbtTagListClass.isInstance(nbtBaseObject) ? new NBTTagListWrapper(nbtBaseObject) : new NBTBaseWrapper(nbtBaseObject));
      }
   }

   protected static final String getName(Object nbtBaseObject) {
      return (String)BukkitReflect.invokeMethod(nbtBaseObject, _getName);
   }

   protected static final Object clone(Object nbtBaseObject) {
      return BukkitReflect.invokeMethod(nbtBaseObject, _clone);
   }

   protected NBTBaseWrapper(Object nbtBaseObject) {
      this._nbtBaseObject = nbtBaseObject;
   }

   protected final Object invokeMethod(Method method, Object... args) {
      return BukkitReflect.invokeMethod(this._nbtBaseObject, method, args);
   }

   public NBTBaseWrapper clone() {
      return wrap(this.invokeMethod(_clone));
   }
}
