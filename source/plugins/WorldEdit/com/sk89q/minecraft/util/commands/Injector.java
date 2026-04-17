package com.sk89q.minecraft.util.commands;

import java.lang.reflect.InvocationTargetException;

public interface Injector {
   Object getInstance(Class var1) throws InvocationTargetException, IllegalAccessException, InstantiationException;
}
