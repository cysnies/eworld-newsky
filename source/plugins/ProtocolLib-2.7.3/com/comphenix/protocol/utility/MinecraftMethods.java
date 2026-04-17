package com.comphenix.protocol.utility;

import com.comphenix.protocol.reflect.FuzzyReflection;
import java.lang.reflect.Method;
import java.util.Map;

public class MinecraftMethods {
   private static volatile Method sendPacketMethod;

   public static Method getSendPacketMethod() {
      if (sendPacketMethod == null) {
         Class<?> serverHandlerClass = MinecraftReflection.getNetServerHandlerClass();

         try {
            sendPacketMethod = FuzzyReflection.fromObject(serverHandlerClass).getMethodByName("sendPacket.*");
         } catch (IllegalArgumentException var6) {
            Map<String, Method> netServer = getMethodList(serverHandlerClass, MinecraftReflection.getPacketClass());
            Map<String, Method> netHandler = getMethodList(MinecraftReflection.getNetHandlerClass(), MinecraftReflection.getPacketClass());

            for(String methodName : netHandler.keySet()) {
               netServer.remove(methodName);
            }

            if (netServer.size() != 1) {
               throw new IllegalArgumentException("Unable to find the sendPacket method in NetServerHandler/PlayerConnection.");
            }

            Method[] methods = (Method[])netServer.values().toArray(new Method[0]);
            sendPacketMethod = methods[0];
         }
      }

      return sendPacketMethod;
   }

   private static Map getMethodList(Class source, Class... params) {
      FuzzyReflection reflect = FuzzyReflection.fromClass(source, true);
      return reflect.getMappedMethods(reflect.getMethodListByParameters(Void.TYPE, params));
   }
}
