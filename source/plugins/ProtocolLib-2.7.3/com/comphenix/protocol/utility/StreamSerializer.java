package com.comphenix.protocol.utility;

import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import javax.annotation.Nonnull;
import org.bukkit.inventory.ItemStack;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public class StreamSerializer {
   private static Method READ_ITEM_METHOD;
   private static Method WRITE_ITEM_METHOD;
   private static Method READ_NBT_METHOD;
   private static Method WRITE_NBT_METHOD;
   private static Method READ_STRING_METHOD;
   private static Method WRITE_STRING_METHOD;

   public ItemStack deserializeItemStack(@Nonnull DataInputStream input) throws IOException {
      if (input == null) {
         throw new IllegalArgumentException("Input stream cannot be NULL.");
      } else {
         if (READ_ITEM_METHOD == null) {
            READ_ITEM_METHOD = FuzzyReflection.fromClass(MinecraftReflection.getPacketClass()).getMethod(FuzzyMethodContract.newBuilder().parameterCount(1).parameterDerivedOf(DataInput.class).returnDerivedOf(MinecraftReflection.getItemStackClass()).build());
         }

         try {
            Object nmsItem = READ_ITEM_METHOD.invoke((Object)null, input);
            return nmsItem != null ? MinecraftReflection.getBukkitItemStack(nmsItem) : null;
         } catch (Exception e) {
            throw new IOException("Cannot read item stack.", e);
         }
      }
   }

   public NbtCompound deserializeCompound(@Nonnull DataInputStream input) throws IOException {
      if (input == null) {
         throw new IllegalArgumentException("Input stream cannot be NULL.");
      } else {
         if (READ_NBT_METHOD == null) {
            READ_NBT_METHOD = FuzzyReflection.fromClass(MinecraftReflection.getPacketClass()).getMethod(FuzzyMethodContract.newBuilder().parameterCount(1).parameterDerivedOf(DataInput.class).returnDerivedOf(MinecraftReflection.getNBTBaseClass()).build());
         }

         try {
            Object nmsCompound = READ_NBT_METHOD.invoke((Object)null, input);
            return nmsCompound != null ? NbtFactory.fromNMSCompound(nmsCompound) : null;
         } catch (Exception e) {
            throw new IOException("Cannot read item stack.", e);
         }
      }
   }

   public String deserializeString(@Nonnull DataInputStream input, int maximumLength) throws IOException {
      if (input == null) {
         throw new IllegalArgumentException("Input stream cannot be NULL.");
      } else if (maximumLength > 32767) {
         throw new IllegalArgumentException("Maximum lenght cannot exceed 32767 characters.");
      } else if (maximumLength < 0) {
         throw new IllegalArgumentException("Maximum lenght cannot be negative.");
      } else {
         if (READ_STRING_METHOD == null) {
            READ_STRING_METHOD = FuzzyReflection.fromClass(MinecraftReflection.getPacketClass()).getMethod(FuzzyMethodContract.newBuilder().parameterCount(2).parameterDerivedOf(DataInput.class, 0).parameterExactType(Integer.TYPE, 1).returnTypeExact(String.class).build());
         }

         try {
            return (String)READ_STRING_METHOD.invoke((Object)null, input, maximumLength);
         } catch (Exception e) {
            throw new IOException("Cannot read Minecraft string.", e);
         }
      }
   }

   public ItemStack deserializeItemStack(@Nonnull String input) throws IOException {
      if (input == null) {
         throw new IllegalArgumentException("Input text cannot be NULL.");
      } else {
         ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(input));
         return this.deserializeItemStack(new DataInputStream(inputStream));
      }
   }

   public void serializeItemStack(@Nonnull DataOutputStream output, ItemStack stack) throws IOException {
      if (output == null) {
         throw new IllegalArgumentException("Output stream cannot be NULL.");
      } else {
         Object nmsItem = MinecraftReflection.getMinecraftItemStack(stack);
         if (WRITE_ITEM_METHOD == null) {
            WRITE_ITEM_METHOD = FuzzyReflection.fromClass(MinecraftReflection.getPacketClass()).getMethod(FuzzyMethodContract.newBuilder().parameterCount(2).parameterDerivedOf(MinecraftReflection.getItemStackClass(), 0).parameterDerivedOf(DataOutput.class, 1).build());
         }

         try {
            WRITE_ITEM_METHOD.invoke((Object)null, nmsItem, output);
         } catch (Exception e) {
            throw new IOException("Cannot write item stack " + stack, e);
         }
      }
   }

   public void serializeCompound(@Nonnull DataOutputStream output, NbtCompound compound) throws IOException {
      if (output == null) {
         throw new IllegalArgumentException("Output stream cannot be NULL.");
      } else {
         Object handle = compound != null ? NbtFactory.fromBase(compound).getHandle() : null;
         if (WRITE_NBT_METHOD == null) {
            WRITE_NBT_METHOD = FuzzyReflection.fromClass(MinecraftReflection.getPacketClass(), true).getMethod(FuzzyMethodContract.newBuilder().parameterCount(2).parameterDerivedOf(MinecraftReflection.getNBTBaseClass(), 0).parameterDerivedOf(DataOutput.class, 1).returnTypeVoid().build());
            WRITE_NBT_METHOD.setAccessible(true);
         }

         try {
            WRITE_NBT_METHOD.invoke((Object)null, handle, output);
         } catch (Exception e) {
            throw new IOException("Cannot write compound " + compound, e);
         }
      }
   }

   public void serializeString(@Nonnull DataOutputStream output, String text) throws IOException {
      if (output == null) {
         throw new IllegalArgumentException("output stream cannot be NULL.");
      } else if (text == null) {
         throw new IllegalArgumentException("text cannot be NULL.");
      } else {
         if (WRITE_STRING_METHOD == null) {
            WRITE_STRING_METHOD = FuzzyReflection.fromClass(MinecraftReflection.getPacketClass()).getMethod(FuzzyMethodContract.newBuilder().parameterCount(2).parameterExactType(String.class, 0).parameterDerivedOf(DataOutput.class, 1).returnTypeVoid().build());
         }

         try {
            WRITE_STRING_METHOD.invoke((Object)null, text, output);
         } catch (Exception e) {
            throw new IOException("Cannot read Minecraft string.", e);
         }
      }
   }

   public String serializeItemStack(ItemStack stack) throws IOException {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      DataOutputStream dataOutput = new DataOutputStream(outputStream);
      this.serializeItemStack(dataOutput, stack);
      return Base64Coder.encodeLines(outputStream.toByteArray());
   }
}
