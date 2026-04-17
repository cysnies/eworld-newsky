package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import java.lang.reflect.Constructor;
import java.util.UUID;
import javax.annotation.Nonnull;

public class WrappedAttributeModifier {
   private static StructureModifier BASE_MODIFIER;
   private static Constructor ATTRIBUTE_MODIFIER_CONSTRUCTOR;
   protected Object handle;
   protected StructureModifier modifier;
   private final UUID uuid;
   private final String name;
   private final Operation operation;
   private final double amount;

   public static Builder newBuilder() {
      return (new Builder((WrappedAttributeModifier)null)).uuid(UUID.randomUUID());
   }

   public static Builder newBuilder(UUID id) {
      return (new Builder((WrappedAttributeModifier)null)).uuid(id);
   }

   public static Builder newBuilder(@Nonnull WrappedAttributeModifier template) {
      return new Builder((WrappedAttributeModifier)Preconditions.checkNotNull(template, "template cannot be NULL."));
   }

   public static WrappedAttributeModifier fromHandle(@Nonnull Object handle) {
      return new WrappedAttributeModifier(handle);
   }

   protected WrappedAttributeModifier(UUID uuid, String name, double amount, Operation operation) {
      this.uuid = uuid;
      this.name = name;
      this.amount = amount;
      this.operation = operation;
   }

   protected WrappedAttributeModifier(@Nonnull Object handle) {
      this.setHandle(handle);
      this.initializeModifier(handle);
      this.uuid = (UUID)this.modifier.withType(UUID.class).read(0);
      this.name = (String)this.modifier.withType(String.class).read(0);
      this.amount = (Double)this.modifier.withType(Double.TYPE).read(0);
      this.operation = WrappedAttributeModifier.Operation.fromId((Integer)this.modifier.withType(Integer.TYPE).read(0));
   }

   protected WrappedAttributeModifier(@Nonnull Object handle, UUID uuid, String name, double amount, Operation operation) {
      this(uuid, name, amount, operation);
      this.setHandle(handle);
      this.initializeModifier(handle);
   }

   private void initializeModifier(@Nonnull Object handle) {
      if (BASE_MODIFIER == null) {
         BASE_MODIFIER = new StructureModifier(MinecraftReflection.getAttributeModifierClass());
      }

      this.modifier = BASE_MODIFIER.withTarget(handle);
   }

   private void setHandle(Object handle) {
      if (!MinecraftReflection.getAttributeModifierClass().isAssignableFrom(handle.getClass())) {
         throw new IllegalArgumentException("handle (" + handle + ") must be a AttributeModifier.");
      } else {
         this.handle = handle;
      }
   }

   public UUID getUUID() {
      return this.uuid;
   }

   public String getName() {
      return this.name;
   }

   public Operation getOperation() {
      return this.operation;
   }

   public double getAmount() {
      return this.amount;
   }

   protected void checkHandle() {
      if (this.handle == null) {
         this.handle = newBuilder(this).build().getHandle();
         this.initializeModifier(this.handle);
      }

   }

   public Object getHandle() {
      return this.handle;
   }

   public void setPendingSynchronization(boolean pending) {
      this.modifier.withType(Boolean.TYPE).write(0, pending);
   }

   public boolean isPendingSynchronization() {
      return (Boolean)this.modifier.withType(Boolean.TYPE).read(0);
   }

   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (obj instanceof WrappedAttributeModifier) {
         WrappedAttributeModifier other = (WrappedAttributeModifier)obj;
         return Objects.equal(this.uuid, other.getUUID());
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.uuid != null ? this.uuid.hashCode() : 0;
   }

   public String toString() {
      return "[amount=" + this.amount + ", operation=" + this.operation + ", name='" + this.name + "', id=" + this.uuid + ", serialize=" + this.isPendingSynchronization() + "]";
   }

   public static enum Operation {
      ADD_NUMBER(0),
      MULTIPLY_PERCENTAGE(1),
      ADD_PERCENTAGE(2);

      private int id;

      private Operation(int id) {
         this.id = id;
      }

      public int getId() {
         return this.id;
      }

      public static Operation fromId(int id) {
         for(Operation op : values()) {
            if (op.getId() == id) {
               return op;
            }
         }

         throw new IllegalArgumentException("Corrupt operation ID " + id + " detected.");
      }
   }

   public static class Builder {
      private Operation operation;
      private String name;
      private double amount;
      private UUID uuid;

      private Builder(WrappedAttributeModifier template) {
         this.operation = WrappedAttributeModifier.Operation.ADD_NUMBER;
         this.name = "Unknown";
         if (template != null) {
            this.operation = template.getOperation();
            this.name = template.getName();
            this.amount = template.getAmount();
            this.uuid = template.getUUID();
         }

      }

      public Builder uuid(@Nonnull UUID uuid) {
         this.uuid = (UUID)Preconditions.checkNotNull(uuid, "uuid cannot be NULL.");
         return this;
      }

      public Builder operation(@Nonnull Operation operation) {
         this.operation = (Operation)Preconditions.checkNotNull(operation, "operation cannot be NULL.");
         return this;
      }

      public Builder name(@Nonnull String name) {
         this.name = (String)Preconditions.checkNotNull(name, "name cannot be NULL.");
         return this;
      }

      public Builder amount(double amount) {
         this.amount = WrappedAttribute.checkDouble(amount);
         return this;
      }

      public WrappedAttributeModifier build() {
         Preconditions.checkNotNull(this.uuid, "uuid cannot be NULL.");
         if (WrappedAttributeModifier.ATTRIBUTE_MODIFIER_CONSTRUCTOR == null) {
            WrappedAttributeModifier.ATTRIBUTE_MODIFIER_CONSTRUCTOR = FuzzyReflection.fromClass(MinecraftReflection.getAttributeModifierClass(), true).getConstructor(FuzzyMethodContract.newBuilder().parameterCount(4).parameterDerivedOf(UUID.class, 0).parameterExactType(String.class, 1).parameterExactType(Double.TYPE, 2).parameterExactType(Integer.TYPE, 3).build());
            WrappedAttributeModifier.ATTRIBUTE_MODIFIER_CONSTRUCTOR.setAccessible(true);
         }

         try {
            return new WrappedAttributeModifier(WrappedAttributeModifier.ATTRIBUTE_MODIFIER_CONSTRUCTOR.newInstance(this.uuid, this.name, this.amount, this.operation.getId()), this.uuid, this.name, this.amount, this.operation);
         } catch (Exception e) {
            throw new RuntimeException("Cannot construct AttributeModifier.", e);
         }
      }
   }
}
