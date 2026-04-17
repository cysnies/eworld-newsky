package com.comphenix.protocol.wrappers.nbt;

import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.nbt.io.NbtBinarySerializer;
import com.google.common.base.Objects;
import java.io.DataOutput;
import java.lang.reflect.Method;

class WrappedElement implements NbtWrapper {
   private static volatile StructureModifier baseModifier;
   private static volatile Method methodGetTypeID;
   private static volatile Method methodClone;
   private static StructureModifier[] modifiers = new StructureModifier[NbtType.values().length];
   private Object handle;
   private NbtType type;

   public WrappedElement(Object handle) {
      this.handle = handle;
   }

   protected static StructureModifier getBaseModifier() {
      if (baseModifier == null) {
         Class<?> base = MinecraftReflection.getNBTBaseClass();
         baseModifier = (new StructureModifier(base, Object.class, false)).withType(String.class);
      }

      return baseModifier.withType(String.class);
   }

   protected StructureModifier getCurrentModifier() {
      NbtType type = this.getType();
      return this.getCurrentBaseModifier().withType(type.getValueType());
   }

   protected StructureModifier getCurrentBaseModifier() {
      int index = this.getType().ordinal();
      StructureModifier<Object> modifier = modifiers[index];
      if (modifier == null) {
         synchronized(this) {
            if (modifiers[index] == null) {
               modifiers[index] = new StructureModifier(this.handle.getClass(), MinecraftReflection.getNBTBaseClass(), false);
            }

            modifier = modifiers[index];
         }
      }

      return modifier;
   }

   public boolean accept(NbtVisitor visitor) {
      return visitor.visit(this);
   }

   public Object getHandle() {
      return this.handle;
   }

   public NbtType getType() {
      if (methodGetTypeID == null) {
         methodGetTypeID = FuzzyReflection.fromClass(MinecraftReflection.getNBTBaseClass()).getMethodByParameters("getTypeID", Byte.TYPE, new Class[0]);
      }

      if (this.type == null) {
         try {
            this.type = NbtType.getTypeFromID((Byte)methodGetTypeID.invoke(this.handle));
         } catch (Exception e) {
            throw new FieldAccessException("Cannot get NBT type of " + this.handle, e);
         }
      }

      return this.type;
   }

   public NbtType getSubType() {
      int subID = (Byte)this.getCurrentBaseModifier().withType(Byte.TYPE).withTarget(this.handle).read(0);
      return NbtType.getTypeFromID(subID);
   }

   public void setSubType(NbtType type) {
      byte subID = (byte)type.getRawID();
      this.getCurrentBaseModifier().withType(Byte.TYPE).withTarget(this.handle).write(0, subID);
   }

   public String getName() {
      return (String)getBaseModifier().withTarget(this.handle).read(0);
   }

   public void setName(String name) {
      getBaseModifier().withTarget(this.handle).write(0, name);
   }

   public Object getValue() {
      return this.getCurrentModifier().withTarget(this.handle).read(0);
   }

   public void setValue(Object newValue) {
      this.getCurrentModifier().withTarget(this.handle).write(0, newValue);
   }

   public void write(DataOutput destination) {
      NbtBinarySerializer.DEFAULT.serialize(this, destination);
   }

   public NbtBase deepClone() {
      if (methodClone == null) {
         Class<?> base = MinecraftReflection.getNBTBaseClass();
         methodClone = FuzzyReflection.fromClass(base).getMethodByParameters("clone", base, new Class[0]);
      }

      try {
         return NbtFactory.fromNMS(methodClone.invoke(this.handle));
      } catch (Exception e) {
         throw new FieldAccessException("Unable to clone " + this.handle, e);
      }
   }

   public int hashCode() {
      return Objects.hashCode(new Object[]{this.getName(), this.getType(), this.getValue()});
   }

   public boolean equals(Object obj) {
      if (obj instanceof NbtBase) {
         NbtBase<?> other = (NbtBase)obj;
         if (other.getType().equals(this.getType())) {
            return Objects.equal(this.getName(), other.getName()) && Objects.equal(this.getValue(), other.getValue());
         }
      }

      return false;
   }

   public String toString() {
      StringBuilder result = new StringBuilder();
      String name = this.getName();
      result.append("{");
      if (name != null && name.length() > 0) {
         result.append("name: '" + name + "', ");
      }

      result.append("value: ");
      if (this.getType() == NbtType.TAG_STRING) {
         result.append("'" + this.getValue() + "'");
      } else {
         result.append(this.getValue());
      }

      result.append("}");
      return result.toString();
   }
}
