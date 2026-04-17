package com.comphenix.protocol.reflect.cloning;

public interface Cloner {
   boolean canClone(Object var1);

   Object clone(Object var1);
}
