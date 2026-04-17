package com.comphenix.protocol.injector;

import com.comphenix.protocol.injector.packet.PacketRegistry;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.compiler.BackgroundCompiler;
import com.comphenix.protocol.reflect.compiler.CompileListener;
import com.comphenix.protocol.reflect.compiler.CompiledStructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class StructureCache {
   private static ConcurrentMap structureModifiers = new ConcurrentHashMap();
   private static Set compiling = new HashSet();

   public static Object newPacket(int id) {
      try {
         return PacketRegistry.getPacketClassFromID(id, true).newInstance();
      } catch (InstantiationException var2) {
         return null;
      } catch (IllegalAccessException e) {
         throw new RuntimeException("Access denied.", e);
      }
   }

   public static StructureModifier getStructure(int id) {
      return getStructure(id, true);
   }

   public static StructureModifier getStructure(Class packetType) {
      return getStructure(packetType, true);
   }

   public static StructureModifier getStructure(Class packetType, boolean compile) {
      return getStructure(PacketRegistry.getPacketID(packetType), compile);
   }

   public static StructureModifier getStructure(final int id, boolean compile) {
      StructureModifier<Object> result = (StructureModifier)structureModifiers.get(id);
      if (result == null) {
         StructureModifier<Object> value = new StructureModifier(PacketRegistry.getPacketClassFromID(id, true), MinecraftReflection.getPacketClass(), true);
         result = (StructureModifier)structureModifiers.putIfAbsent(id, value);
         if (result == null) {
            result = value;
         }
      }

      if (compile && !(result instanceof CompiledStructureModifier)) {
         synchronized(compiling) {
            BackgroundCompiler compiler = BackgroundCompiler.getInstance();
            if (!compiling.contains(id) && compiler != null) {
               compiler.scheduleCompilation(result, new CompileListener() {
                  public void onCompiled(StructureModifier compiledModifier) {
                     StructureCache.structureModifiers.put(id, compiledModifier);
                  }
               });
               compiling.add(id);
            }
         }
      }

      return result;
   }
}
