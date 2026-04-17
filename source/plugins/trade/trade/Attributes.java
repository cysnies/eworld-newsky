package trade;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.server.v1_6_R2.ItemStack;
import net.minecraft.server.v1_6_R2.NBTBase;
import net.minecraft.server.v1_6_R2.NBTTagCompound;
import net.minecraft.server.v1_6_R2.NBTTagList;
import org.bukkit.craftbukkit.v1_6_R2.inventory.CraftItemStack;

public class Attributes {
   public ItemStack nmsStack;
   private NBTTagCompound parent;
   private NBTTagList attributes;

   public Attributes(org.bukkit.inventory.ItemStack stack) {
      this.nmsStack = CraftItemStack.asNMSCopy(stack);
      if (this.nmsStack.tag == null) {
         this.parent = this.nmsStack.tag = new NBTTagCompound();
      } else {
         this.parent = this.nmsStack.tag;
      }

      if (this.parent.hasKey("AttributeModifiers")) {
         this.attributes = this.parent.getList("AttributeModifiers");
      } else {
         this.attributes = new NBTTagList();
         this.parent.set("AttributeModifiers", this.attributes);
      }

   }

   public org.bukkit.inventory.ItemStack getStack() {
      return CraftItemStack.asCraftMirror(this.nmsStack);
   }

   public int size() {
      return this.attributes.size();
   }

   public void add(Attribute attribute) {
      this.attributes.add(attribute.data);
   }

   public boolean remove(Attribute attribute) {
      UUID uuid = attribute.getUUID();
      Iterator<Attribute> it = this.values().iterator();

      while(it.hasNext()) {
         if (Objects.equal(((Attribute)it.next()).getUUID(), uuid)) {
            it.remove();
            return true;
         }
      }

      return false;
   }

   public void clear() {
      this.parent.set("AttributeModifiers", this.attributes = new NBTTagList());
   }

   public Attribute get(int index) {
      return new Attribute((NBTTagCompound)this.attributes.get(index), (Attribute)null);
   }

   public Iterable values() {
      final List<NBTBase> list = this.getList();
      return new Iterable() {
         public Iterator iterator() {
            return Iterators.transform(list.iterator(), new Function() {
               public Attribute apply(@Nullable NBTBase data) {
                  return new Attribute((NBTTagCompound)data, (Attribute)null);
               }
            });
         }
      };
   }

   private List getList() {
      try {
         Field listField = NBTTagList.class.getDeclaredField("list");
         listField.setAccessible(true);
         return (List)listField.get(this.attributes);
      } catch (Exception e) {
         throw new RuntimeException("Unable to access reflection.", e);
      }
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
         Operation[] var4;
         for(Operation op : var4 = values()) {
            if (op.getId() == id) {
               return op;
            }
         }

         throw new IllegalArgumentException("Corrupt operation ID " + id + " detected.");
      }
   }

   public static class AttributeType {
      private static ConcurrentMap LOOKUP = Maps.newConcurrentMap();
      public static final AttributeType GENERIC_MAX_HEALTH = (new AttributeType("generic.maxHealth")).register();
      public static final AttributeType GENERIC_FOLLOW_RANGE = (new AttributeType("generic.followRange")).register();
      public static final AttributeType GENERIC_ATTACK_DAMAGE = (new AttributeType("generic.attackDamage")).register();
      public static final AttributeType GENERIC_MOVEMENT_SPEED = (new AttributeType("generic.movementSpeed")).register();
      public static final AttributeType GENERIC_KNOCKBACK_RESISTANCE = (new AttributeType("generic.knockbackResistance")).register();
      private final String minecraftId;

      public AttributeType(String minecraftId) {
         this.minecraftId = minecraftId;
      }

      public String getMinecraftId() {
         return this.minecraftId;
      }

      public AttributeType register() {
         AttributeType old = (AttributeType)LOOKUP.putIfAbsent(this.minecraftId, this);
         return old != null ? old : this;
      }

      public static AttributeType fromId(String minecraftId) {
         return (AttributeType)LOOKUP.get(minecraftId);
      }

      public static Iterable values() {
         return LOOKUP.values();
      }
   }

   public static class Attribute {
      private NBTTagCompound data;

      private Attribute(Builder builder) {
         this.data = new NBTTagCompound();
         this.setAmount(builder.amount);
         this.setOperation(builder.operation);
         this.setAttributeType(builder.type);
         this.setName(builder.name);
         this.setUUID(builder.uuid);
      }

      private Attribute(NBTTagCompound data) {
         this.data = data;
      }

      public double getAmount() {
         return this.data.getDouble("Amount");
      }

      public void setAmount(double amount) {
         this.data.setDouble("Amount", amount);
      }

      public Operation getOperation() {
         return Attributes.Operation.fromId(this.data.getInt("Operation"));
      }

      public void setOperation(@Nonnull Operation operation) {
         Preconditions.checkNotNull(operation, "operation cannot be NULL.");
         this.data.setInt("Operation", operation.getId());
      }

      public AttributeType getAttributeType() {
         return Attributes.AttributeType.fromId(this.data.getString("AttributeName"));
      }

      public void setAttributeType(@Nonnull AttributeType type) {
         Preconditions.checkNotNull(type, "type cannot be NULL.");
         this.data.setString("AttributeName", type.getMinecraftId());
      }

      public String getName() {
         return this.data.getString("Name");
      }

      public void setName(@Nonnull String name) {
         this.data.setString("Name", name);
      }

      public UUID getUUID() {
         return new UUID(this.data.getLong("UUIDMost"), this.data.getLong("UUIDLeast"));
      }

      public void setUUID(@Nonnull UUID id) {
         Preconditions.checkNotNull("id", "id cannot be NULL.");
         this.data.setLong("UUIDLeast", id.getLeastSignificantBits());
         this.data.setLong("UUIDMost", id.getMostSignificantBits());
      }

      public static Builder newBuilder() {
         return (new Builder((Builder)null)).uuid(UUID.randomUUID()).operation(Attributes.Operation.ADD_NUMBER);
      }

      // $FF: synthetic method
      Attribute(Builder var1, Attribute var2) {
         this(var1);
      }

      // $FF: synthetic method
      Attribute(NBTTagCompound var1, Attribute var2) {
         this(var1);
      }

      public static class Builder {
         private double amount;
         private Operation operation;
         private AttributeType type;
         private String name;
         private UUID uuid;

         private Builder() {
            this.operation = Attributes.Operation.ADD_NUMBER;
         }

         public Builder amount(double amount) {
            this.amount = amount;
            return this;
         }

         public Builder operation(Operation operation) {
            this.operation = operation;
            return this;
         }

         public Builder type(AttributeType type) {
            this.type = type;
            return this;
         }

         public Builder name(String name) {
            this.name = name;
            return this;
         }

         public Builder uuid(UUID uuid) {
            this.uuid = uuid;
            return this;
         }

         public Attribute build() {
            return new Attribute(this, (Attribute)null);
         }

         // $FF: synthetic method
         Builder(Builder var1) {
            this();
         }
      }
   }
}
