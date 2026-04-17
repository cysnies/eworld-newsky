package com.comphenix.protocol.reflect;

public interface EquivalentConverter {
   Object getSpecific(Object var1);

   Object getGeneric(Class var1, Object var2);

   Class getSpecificType();
}
