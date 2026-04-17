package com.sk89q.worldedit;

import java.util.List;

public interface BiomeTypes {
   boolean has(String var1);

   BiomeType get(String var1) throws UnknownBiomeTypeException;

   List all();
}
