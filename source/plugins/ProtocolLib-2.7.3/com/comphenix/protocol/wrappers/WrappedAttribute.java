package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.collection.CachedSet;
import com.comphenix.protocol.wrappers.collection.ConvertedSet;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;

public class WrappedAttribute {
   private static StructureModifier ATTRIBUTE_MODIFIER;
   private static Constructor ATTRIBUTE_CONSTRUCTOR;
   protected Object handle;
   protected StructureModifier modifier;
   private double computedValue;
   private Set attributeModifiers;

   public static WrappedAttribute fromHandle(@Nonnull Object handle) {
      return new WrappedAttribute(handle);
   }

   public static Builder newBuilder() {
      return new Builder((WrappedAttribute)null);
   }

   public static Builder newBuilder(@Nonnull WrappedAttribute template) {
      return new Builder((WrappedAttribute)Preconditions.checkNotNull(template, "template cannot be NULL."));
   }

   private WrappedAttribute(@Nonnull Object handle) {
      this.computedValue = Double.NaN;
      this.handle = Preconditions.checkNotNull(handle, "handle cannot be NULL.");
      if (!MinecraftReflection.getAttributeSnapshotClass().isAssignableFrom(handle.getClass())) {
         throw new IllegalArgumentException("handle (" + handle + ") must be a AttributeSnapshot.");
      } else {
         if (ATTRIBUTE_MODIFIER == null) {
            ATTRIBUTE_MODIFIER = new StructureModifier(MinecraftReflection.getAttributeSnapshotClass());
         }

         this.modifier = ATTRIBUTE_MODIFIER.withTarget(handle);
      }
   }

   public Object getHandle() {
      return this.handle;
   }

   public String getAttributeKey() {
      return (String)this.modifier.withType(String.class).read(0);
   }

   public double getBaseValue() {
      return (Double)this.modifier.withType(Double.TYPE).read(0);
   }

   public double getFinalValue() {
      if (Double.isNaN(this.computedValue)) {
         this.computedValue = this.computeValue();
      }

      return this.computedValue;
   }

   public PacketContainer getParentPacket() {
      return new PacketContainer(44, this.modifier.withType(MinecraftReflection.getPacketClass()).read(0));
   }

   public boolean hasModifier(UUID id) {
      return this.getModifiers().contains(WrappedAttributeModifier.newBuilder(id).build());
   }

   public WrappedAttributeModifier getModifierByUUID(UUID id) {
      if (this.hasModifier(id)) {
         for(WrappedAttributeModifier modifier : this.getModifiers()) {
            if (Objects.equal(modifier.getUUID(), id)) {
               return modifier;
            }
         }
      }

      return null;
   }

   public Set getModifiers() {
      if (this.attributeModifiers == null) {
         Collection<Object> collection = (Collection)this.modifier.withType(Collection.class).read(0);
         ConvertedSet<Object, WrappedAttributeModifier> converted = new ConvertedSet(getSetSafely(collection)) {
            protected Object toInner(WrappedAttributeModifier outer) {
               return outer.getHandle();
            }

            protected WrappedAttributeModifier toOuter(Object inner) {
               return WrappedAttributeModifier.fromHandle(inner);
            }
         };
         this.attributeModifiers = new CachedSet(converted);
      }

      return Collections.unmodifiableSet(this.attributeModifiers);
   }

   public WrappedAttribute withModifiers(Collection modifiers) {
      return newBuilder(this).modifiers(modifiers).build();
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof WrappedAttribute)) {
         return false;
      } else {
         WrappedAttribute other = (WrappedAttribute)obj;
         return this.getBaseValue() == other.getBaseValue() && Objects.equal(this.getAttributeKey(), other.getAttributeKey()) && Sets.symmetricDifference(this.getModifiers(), other.getModifiers()).isEmpty();
      }
   }

   public int hashCode() {
      if (this.attributeModifiers == null) {
         this.getModifiers();
      }

      return Objects.hashCode(new Object[]{this.getAttributeKey(), this.getBaseValue(), this.attributeModifiers});
   }

   private double computeValue() {
      Collection<WrappedAttributeModifier> modifiers = this.getModifiers();
      double x = this.getBaseValue();
      double y = (double)0.0F;

      for(int phase = 0; phase < 3; ++phase) {
         for(WrappedAttributeModifier modifier : modifiers) {
            if (modifier.getOperation().getId() == phase) {
               switch (phase) {
                  case 0:
                     x += modifier.getAmount();
                     break;
                  case 1:
                     y += x * modifier.getAmount();
                     break;
                  case 2:
                     y *= (double)1.0F + modifier.getAmount();
                     break;
                  default:
                     throw new IllegalStateException("Unknown phase: " + phase);
               }
            }
         }

         if (phase == 0) {
            y = x;
         }
      }

      return y;
   }

   public String toString() {
      return Objects.toStringHelper("WrappedAttribute").add("key", this.getAttributeKey()).add("baseValue", this.getBaseValue()).add("finalValue", this.getFinalValue()).add("modifiers", this.getModifiers()).toString();
   }

   private static Set getSetSafely(Collection collection) {
      return (Set)(collection instanceof Set ? (Set)collection : Sets.newHashSet(collection));
   }

   static double checkDouble(double value) {
      if (Double.isInfinite(value)) {
         throw new IllegalArgumentException("value cannot be infinite.");
      } else if (Double.isNaN(value)) {
         throw new IllegalArgumentException("value cannot be NaN.");
      } else {
         return value;
      }
   }

   public static class Builder {
      private double baseValue;
      private String attributeKey;
      private PacketContainer packet;
      private Collection modifiers;

      private Builder(WrappedAttribute template) {
         this.baseValue = Double.NaN;
         this.modifiers = Collections.emptyList();
         if (template != null) {
            this.baseValue = template.getBaseValue();
            this.attributeKey = template.getAttributeKey();
            this.packet = template.getParentPacket();
            this.modifiers = template.getModifiers();
         }

      }

      public Builder baseValue(double baseValue) {
         this.baseValue = WrappedAttribute.checkDouble(baseValue);
         return this;
      }

      public Builder attributeKey(String attributeKey) {
         this.attributeKey = (String)Preconditions.checkNotNull(attributeKey, "attributeKey cannot be NULL.");
         return this;
      }

      public Builder modifiers(Collection modifiers) {
         this.modifiers = (Collection)Preconditions.checkNotNull(modifiers, "modifiers cannot be NULL - use an empty list instead.");
         return this;
      }

      public Builder packet(PacketContainer packet) {
         if (((PacketContainer)Preconditions.checkNotNull(packet, "packet cannot be NULL")).getID() != 44) {
            throw new IllegalArgumentException("Packet must be UPDATE_ATTRIBUTES (44)");
         } else {
            this.packet = packet;
            return this;
         }
      }

      private Set getUnwrappedModifiers() {
         Set<Object> output = Sets.newHashSet();

         for(WrappedAttributeModifier modifier : this.modifiers) {
            output.add(modifier.getHandle());
         }

         return output;
      }

      public WrappedAttribute build() {
         Preconditions.checkNotNull(this.packet, "packet cannot be NULL.");
         Preconditions.checkNotNull(this.attributeKey, "attributeKey cannot be NULL.");
         if (Double.isNaN(this.baseValue)) {
            throw new IllegalStateException("Base value has not been set.");
         } else {
            if (WrappedAttribute.ATTRIBUTE_CONSTRUCTOR == null) {
               WrappedAttribute.ATTRIBUTE_CONSTRUCTOR = FuzzyReflection.fromClass(MinecraftReflection.getAttributeSnapshotClass(), true).getConstructor(FuzzyMethodContract.newBuilder().parameterCount(4).parameterDerivedOf(MinecraftReflection.getPacketClass(), 0).parameterExactType(String.class, 1).parameterExactType(Double.TYPE, 2).parameterDerivedOf(Collection.class, 3).build());
               WrappedAttribute.ATTRIBUTE_CONSTRUCTOR.setAccessible(true);
            }

            try {
               Object handle = WrappedAttribute.ATTRIBUTE_CONSTRUCTOR.newInstance(this.packet.getHandle(), this.attributeKey, this.baseValue, this.getUnwrappedModifiers());
               return new WrappedAttribute(handle);
            } catch (Exception e) {
               throw new RuntimeException("Cannot construct AttributeSnapshot.", e);
            }
         }
      }
   }
}
