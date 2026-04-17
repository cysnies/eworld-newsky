package com.comphenix.protocol.wrappers.nbt.io;

import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtList;
import com.comphenix.protocol.wrappers.nbt.NbtWrapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public class NbtTextSerializer {
   public static final NbtTextSerializer DEFAULT = new NbtTextSerializer();
   private NbtBinarySerializer binarySerializer;

   public NbtTextSerializer() {
      this(new NbtBinarySerializer());
   }

   public NbtTextSerializer(NbtBinarySerializer binary) {
      this.binarySerializer = binary;
   }

   public NbtBinarySerializer getBinarySerializer() {
      return this.binarySerializer;
   }

   public String serialize(NbtBase value) {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      DataOutputStream dataOutput = new DataOutputStream(outputStream);
      this.binarySerializer.serialize(value, dataOutput);
      return Base64Coder.encodeLines(outputStream.toByteArray());
   }

   public NbtWrapper deserialize(String input) throws IOException {
      ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(input));
      return this.binarySerializer.deserialize(new DataInputStream(inputStream));
   }

   public NbtCompound deserializeCompound(String input) throws IOException {
      return (NbtCompound)this.deserialize(input);
   }

   public NbtList deserializeList(String input) throws IOException {
      return (NbtList)this.deserialize(input);
   }
}
