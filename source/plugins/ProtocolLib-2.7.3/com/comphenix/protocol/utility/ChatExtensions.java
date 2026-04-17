package com.comphenix.protocol.utility;

import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.injector.PacketConstructor;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMatchers;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChatExtensions {
   private PacketConstructor chatConstructor;
   private ProtocolManager manager;
   private static Constructor jsonConstructor = getJsonFormatConstructor();
   private static Method messageFactory;

   public ChatExtensions(ProtocolManager manager) {
      this.manager = manager;
   }

   public void sendMessageSilently(CommandSender receiver, String message) throws InvocationTargetException {
      if (receiver == null) {
         throw new IllegalArgumentException("receiver cannot be NULL.");
      } else if (message == null) {
         throw new IllegalArgumentException("message cannot be NULL.");
      } else {
         if (receiver instanceof Player) {
            this.sendMessageSilently((Player)receiver, message);
         } else {
            receiver.sendMessage(message);
         }

      }
   }

   private void sendMessageSilently(Player player, String message) throws InvocationTargetException {
      if (jsonConstructor != null) {
         this.sendMessageAsJson(player, message);
      } else {
         this.sendMessageAsString(player, message);
      }

   }

   private void sendMessageAsJson(Player player, String message) throws InvocationTargetException {
      Object messageObject = null;
      if (this.chatConstructor == null) {
         Class<?> messageClass = jsonConstructor.getParameterTypes()[0];
         this.chatConstructor = this.manager.createPacketConstructor(3, messageClass);
         messageFactory = FuzzyReflection.fromClass(messageClass).getMethod(FuzzyMethodContract.newBuilder().requireModifier(8).parameterCount(1).parameterExactType(String.class).returnTypeMatches(FuzzyMatchers.matchParent()).build());
      }

      try {
         messageObject = messageFactory.invoke((Object)null, message);
      } catch (Exception e) {
         throw new InvocationTargetException(e);
      }

      try {
         this.manager.sendServerPacket(player, this.chatConstructor.createPacket(messageObject), false);
      } catch (FieldAccessException e) {
         throw new InvocationTargetException(e);
      }
   }

   private void sendMessageAsString(Player player, String message) throws InvocationTargetException {
      if (this.chatConstructor == null) {
         this.chatConstructor = this.manager.createPacketConstructor(3, message);
      }

      try {
         this.manager.sendServerPacket(player, this.chatConstructor.createPacket(message), false);
      } catch (FieldAccessException e) {
         throw new InvocationTargetException(e);
      }
   }

   public void broadcastMessageSilently(String message, String permission) throws InvocationTargetException {
      if (message == null) {
         throw new IllegalArgumentException("message cannot be NULL.");
      } else {
         for(Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (permission == null || player.hasPermission(permission)) {
               this.sendMessageSilently(player, message);
            }
         }

      }
   }

   public static String[] toFlowerBox(String[] message, String marginChar, int marginWidth, int marginHeight) {
      String[] output = new String[message.length + marginHeight * 2];
      int width = getMaximumLength(message);
      String topButtomMargin = Strings.repeat(marginChar, width + marginWidth * 2);
      String leftRightMargin = Strings.repeat(marginChar, marginWidth);

      for(int i = 0; i < message.length; ++i) {
         output[i + marginHeight] = leftRightMargin + Strings.padEnd(message[i], width, ' ') + leftRightMargin;
      }

      for(int i = 0; i < marginHeight; ++i) {
         output[i] = topButtomMargin;
         output[output.length - i - 1] = topButtomMargin;
      }

      return output;
   }

   private static int getMaximumLength(String[] lines) {
      int current = 0;

      for(int i = 0; i < lines.length; ++i) {
         if (current < lines[i].length()) {
            current = lines[i].length();
         }
      }

      return current;
   }

   static Constructor getJsonFormatConstructor() {
      Class<?> chatPacket = PacketRegistry.getPacketClassFromID(3, true);
      List<Constructor<?>> list = FuzzyReflection.fromClass(chatPacket).getConstructorList(FuzzyMethodContract.newBuilder().parameterCount(1).parameterMatches(MinecraftReflection.getMinecraftObjectMatcher()).build());
      return (Constructor)Iterables.getFirst(list, (Object)null);
   }
}
